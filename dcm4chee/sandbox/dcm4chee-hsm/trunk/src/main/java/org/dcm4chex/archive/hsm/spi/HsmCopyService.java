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

/**
 * A file copy service aware of HSM.
 *
 * In a standard use case this MBean copies files to the destination filesystem and
 * delegates to the configured implementation of <code>HsmClient</code> to archive
 * files invoking {@link HsmClient#archive(String, java.io.File)} method. After files
 * were archived it adds duplicated database entries for those files, changing their
 * file path, file system and file status (<code>TO_ARCHIVE</code> or <code>ARCHIVED</code>).
 * <p>
 * <b>Note:</b> at the moment this implementation will always pack files into a TAR archive
 * before copying. This feature will be extracted as a configuration option soon. 
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

    private Storage storage;

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
            String destination = FileUtils.toFile(HsmUtils.extractFileSpaceName(destPath)).getCanonicalPath();
            String tarFileLocation = packAndArchive(files, destination);
            updateFilesStatus(files,
                    fileStatus,
                    destPath,
                    tarFileLocationInFileSpace(tarFileLocation, destination));
        }
    }

    private String packAndArchive(List<FileInfo> files, String destination) throws Exception {
        File tarFile = packIntoTar(destination, files);
        try {
            archive(destination, tarFile);
            if (logger.isDebugEnabled()) {
                logger.debug(MessageFormat.format(M_DELETE, tarFile.getCanonicalPath()));
            }
        } finally {
            if (!tarFile.delete()) logger.warn(MessageFormat.format(COULD_NOT_DELETE_FILE, tarFile));
            HsmUtils.deleteParentsTill(tarFile, destination);
        }
        return tarFile.getCanonicalPath();
    }

    private void archive(String destination, File tarFile) throws Exception {
        server.invoke(hsmClientName,
                ARCHIVE,
                new Object[]{destination, tarFile},
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
}
