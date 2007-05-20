/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is part of dcm4che, an implementation of DICOM(TM) in
 * Java(TM), hosted at http://sourceforge.net/projects/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * Fuad Ibrahimov, Diagnoseklinik Muenchen.de GmbH,
 * Portions created by the Initial Developer are Copyright (C) 2007
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Fuad Ibrahimov <fuad@ibrahimov.de>
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 *
 * ***** END LICENSE BLOCK ***** */
package org.dcm4chex.archive.hsm.spi;

import org.dcm4chex.archive.hsm.AbstractFileCopyService;
import org.dcm4chex.archive.hsm.FileCopyOrder;
import org.dcm4chex.archive.hsm.spi.utils.HsmUtils;
import org.dcm4chex.archive.common.BaseJmsOrder;
import org.dcm4chex.archive.ejb.jdbc.FileInfo;
import org.dcm4chex.archive.ejb.interfaces.Storage;
import org.dcm4chex.archive.util.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dcm4che.data.Dataset;

import javax.management.ObjectName;
import java.util.List;
import java.text.MessageFormat;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.FileOutputStream;

/**
 * A file copy service aware of HSM.
 *
 * In a standard use case this MBean copies files to the destination filesystem and
 * delegates to the configured implementation of <code>HsmClient</code> to archive
 * files invoking {@link HsmClient#archive(String, java.io.File)} method. After files
 * were archived it adds duplicated database entries for those files, changing their
 * file path, file system and file status (<code>TO_ARCHIVE</code> or <code>ARCHIVED</code>).
 * <p>
 * If the destination file system name is a tar URI (tar:), instances of one series are
 * packed into one tarball. Otherwise instance files are copied individually to the destination file system.
 * 
 * @see org.dcm4chex.archive.hsm.spi.HsmClient
 * @see org.dcm4chex.archive.hsm.spi.TarService
 * @author Fuad Ibrahimov
 * @since Nov 28, 2006
 */
public class HsmCopyService extends AbstractFileCopyService {

    private Log logger = LogFactory.getLog(HsmCopyService.class);

    private static final String ABOUT_TO_PROCESS_A_FILE_COPY_ORDER = "About to process a file copy order"; //NON-NLS
    private static final String FINISHED_PROCRSSING_THE_FILE_COPY_ORDER = "Finished processing the file copy order"; // NON-NLS
    private static final String COULD_NOT_DELETE_FILE = "Could not delete file: [{0}]"; // NON-NLS
    private static final String M_DELETE = "M-DELETE [{0}]"; // NON-NLS
    static final String PACK = "pack"; // NON-NLS
    static final String ARCHIVE = "archive"; // NON-NLS
    private static final String TAR_PREFIX = "tar:"; // NON-NLS

    private Storage storage;
    private boolean cleanupAfterCopy = false;

    private ObjectName tarServiceName;
    private ObjectName hsmClientName;

    protected BaseJmsOrder createOrder(Dataset ian) {
        return new FileCopyOrder(ian, destination, getRetrieveAET());
    }

    protected void startService() throws Exception {
        super.startService();
        this.storage = getStorageHome().create();
    }

    /**
     * Overrides {@link org.dcm4chex.archive.hsm.AbstractFileCopyService#process(org.dcm4chex.archive.common.BaseJmsOrder)}
     * to copy-archive files and to insert new file descriptors into the DB.
     * @param order <code>FileCopyOrder</code> to process
     * @throws Exception in case of error during processing the file copy order
     */
    protected void process(BaseJmsOrder order) throws Exception {
        logger.debug(ABOUT_TO_PROCESS_A_FILE_COPY_ORDER);
        doProcess(order);
        logger.debug(FINISHED_PROCRSSING_THE_FILE_COPY_ORDER);
    }

    private void doProcess(BaseJmsOrder order) throws Exception {
        FileCopyOrder fileCopyOrder = (FileCopyOrder) order;
        //noinspection unchecked
        List<FileInfo> files = fileCopyOrder.getFileInfos();
        if (files.size() > 0) {
            String destPath = fileCopyOrder.getDestinationFileSystemPath();
            if(destPath.startsWith(TAR_PREFIX)) {
                String destination = FileUtils.toFile(HsmUtils.extractFileSpaceName(destPath)).getCanonicalPath();
                String tarFileLocation = packAndArchive(files, destination);
                updateFilesStatus(files,
                        fileStatus,
                        destPath,
                        tarFileLocationInFileSpace(tarFileLocation, destination));
            } else {
                copyAndArchive(files, destPath);
                storage.storeFiles(files, destPath, fileStatus);
            }
        }
    }

    private void copyAndArchive(List<FileInfo> files, String destPath) throws Exception {
        for(FileInfo finfo : files) {
            File copy = copyTo(finfo, destPath);
            try {
                archive(destPath, copy);
            } finally {
                cleanup(copy, destPath);
            }
            finfo.basedir = destPath;
        }
    }

    private File copyTo(FileInfo finfo, String destPath) throws IOException {
        File original = FileUtils.toFile(finfo.basedir, finfo.fileID);
        File copy = FileUtils.toFile(destPath, finfo.fileID);
        HsmUtils.copy(original, copy, bufferSize);
        return copy;
    }

    private String packAndArchive(List<FileInfo> files, String destination) throws Exception {
        File tarFile = packIntoTar(destination, files);
        try {
            archive(destination, tarFile);
            if (logger.isDebugEnabled()) {
                logger.debug(MessageFormat.format(M_DELETE, tarFile.getCanonicalPath()));
            }
        } finally {
            cleanup(tarFile, destination);
        }
        return tarFile.getCanonicalPath();
    }

    private void cleanup(File file, String destination) throws IOException {
        if (this.cleanupAfterCopy) {
            if (!file.delete()) logger.warn(MessageFormat.format(COULD_NOT_DELETE_FILE, file));
            HsmUtils.deleteParentsTill(file, destination);
        }
    }

    private void archive(String destination, File file) throws Exception {
        server.invoke(hsmClientName,
                ARCHIVE,
                new Object[]{destination, file},
                new String[]{String.class.getName(), File.class.getName()});
    }

    private File packIntoTar(String destination, List<FileInfo> files) throws Exception {
        return (File) server.invoke(tarServiceName,
                PACK,
                new Object[]{destination, files},
                new String[]{String.class.getName(), List.class.getName()});
    }

    private void updateFilesStatus(List fileInfos,
                                   int fileStatus,
                                   String destPath,
                                   String tarFileLocInFilespace) throws Exception {
        for (Object fileInfo : fileInfos) {
            FileInfo finfo = (FileInfo) fileInfo;
            finfo.fileID = mkTarEntryPath(tarFileLocInFilespace, finfo.fileID);
        }
        storage.storeFiles(fileInfos, destPath, fileStatus);
    }

    private String mkTarEntryPath(String tarFileLocation, String filePath) {
        return tarFileLocation + "!" + filePath;
    }

    private String tarFileLocationInFileSpace(String tarFileLocation, String fileSpaceName) {
        return tarFileLocation.startsWith(fileSpaceName) ? tarFileLocation.replace(fileSpaceName, "") : "";
    }

    public void setStorage(Storage storage) {
        this.storage = storage;
    }

    public ObjectName getTarServiceName() {
        return tarServiceName;
    }

    public void setTarServiceName(ObjectName tarServiceName) {
        this.tarServiceName = tarServiceName;
    }

    public ObjectName getHsmClientName() {
        return hsmClientName;
    }

    public void setHsmClientName(ObjectName hsmClientName) {
        this.hsmClientName = hsmClientName;
    }


    public boolean isCleanupAfterCopy() {
        return cleanupAfterCopy;
    }

    /**
     * Sets the flag showing if cleanup is needed after files were delivered to the HSM archive.
     * It is useful for cases when explicit HSM calls (API or command line) were used to deliver
     * files to the HSM archive. In this case all copied files will be deleted from the
     * disk after they were successfully archived.
     * <p>
     * <b>Note:</b> set it to <code>false</code> if you are using a virtual HSM file system or relying
     * on <code>SyncFileStatus</code> to check if files were successfully archived.
     * @param cleanupAfterCopy <code>true</code> if copied files must be cleaned up, <code>false</code> - otherwise
     */
    public void setCleanupAfterCopy(boolean cleanupAfterCopy) {
        this.cleanupAfterCopy = cleanupAfterCopy;
    }
}
