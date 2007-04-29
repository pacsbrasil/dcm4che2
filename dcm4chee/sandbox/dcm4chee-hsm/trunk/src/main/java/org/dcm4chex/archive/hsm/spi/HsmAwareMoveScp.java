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

import org.dcm4chex.archive.dcm.qrscp.MoveScp;
import org.dcm4chex.archive.dcm.qrscp.QueryRetrieveScpService;
import org.dcm4chex.archive.dcm.qrscp.MoveTask;
import org.dcm4chex.archive.ejb.jdbc.FileInfo;
import org.dcm4chex.archive.ejb.interfaces.AEDTO;
import org.dcm4chex.archive.common.FileStatus;
import org.dcm4che.net.ActiveAssociation;
import org.dcm4che.net.DcmServiceException;
import org.dcm4che.data.Dataset;
import org.dcm4che.data.Command;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.*;
import java.text.MessageFormat;

/**
 * This class extends the original <code>MoveScp</code> class to filter out
 * files marked as <code>ARCHIVED</code>. For the archived files it queues
 * an HSM retrieve order with a deferred <code>C-MOVE</code> task.
 * In case if all requested files were archived, an empty <code>MoveTask</code>
 * is created to release the association with the requesting modality.
 * <p>
 * <b>Note:</b> In case if files were packed into a TAR archive, the
 * granularity of the deferred <code>C-MOVE</code> tasks will be a single series.
 *
 * @see MoveScp
 * @see HsmAwareQueryRetrieveScpService
 * @see MoveTask
 * @see #createMoveTask
 * @see HsmRetrieveOrder
 * @author Fuad Ibrahimov
 * @since Feb 5, 2007
 */
public class HsmAwareMoveScp extends MoveScp {
    public static final String LOCAL_FILES = "localFiles"; // NON-NLS
    public static final String ARCHIVED_FILES = "archivedFiles"; // NON-NLS

    public HsmAwareMoveScp(QueryRetrieveScpService service) {
        super(service);
    }

    /**
     * Creates new <code>MoveTask</code> to send the requested files to the requesting modality.
     * This method will filter all files marked as <code>ARCHIVED</code> and will queue for them
     * an HSM retrieve call with a following deferred <code>C-MOVE</code> task.
     * @param service will be used for an HSM retrieve call and a deferred <code>C-MOVE</code> task
     * @param moveAssoc
     * @param movePcid
     * @param moveRqCmd
     * @param moveRqData
     * @param fileInfo files to be sent
     * @param aeData
     * @param moveDest destination AE title
     * @return a new MoveTask
     * @throws DcmServiceException - in case of errors 
     */
    protected MoveTask createMoveTask(QueryRetrieveScpService service,
                                      ActiveAssociation moveAssoc,
                                      int movePcid,
                                      Command moveRqCmd,
                                      Dataset moveRqData,
                                      FileInfo[][] fileInfo,
                                      AEDTO aeData,
                                      String moveDest) throws DcmServiceException {
        Map<String, FileInfo[][]> localAndArchived = splitLocalAndArchivedFiles(fileInfo);
        FileInfo[][] archivedFiles = localAndArchived.get(ARCHIVED_FILES);
        if (archivedFiles != null && archivedFiles.length > 0) {
            addNewDelayedCMoveTask(service, archivedFiles, moveDest);
        }
        return newMoveTask(localAndArchived, service, moveAssoc, movePcid, moveRqCmd, moveRqData, aeData, moveDest);
    }

    private MoveTask newMoveTask(Map<String, FileInfo[][]> localAndArchived,
                                 QueryRetrieveScpService service,
                                 ActiveAssociation moveAssoc,
                                 int movePcid,
                                 Command moveRqCmd,
                                 Dataset moveRqData,
                                 AEDTO aeData,
                                 String moveDest) throws DcmServiceException {
        
        FileInfo[][] fileInfos = localAndArchived.get(LOCAL_FILES);
        if (fileInfos != null && (fileInfos.length > 0)) {
            return super.createMoveTask(service,
                    moveAssoc,
                    movePcid,
                    moveRqCmd,
                    moveRqData,
                    fileInfos,
                    aeData,
                    moveDest);
        } else {
            return new EmptyMoveTask(service, moveAssoc, movePcid, moveRqCmd, moveRqData, aeData, moveDest);
        }
    }

    private void addNewDelayedCMoveTask(QueryRetrieveScpService service, FileInfo[][] archivedFiles, String moveDest) {
        ((HsmAwareQueryRetrieveScpService) service).retriveAndSend(archivedFiles, moveDest);
    }

    Map<String, FileInfo[][]> splitLocalAndArchivedFiles(FileInfo[][] fileInfo) {
        List<FileInfo[]> localFiles = new ArrayList<FileInfo[]>();
        List<FileInfo[]> archivedFiles = new ArrayList<FileInfo[]>();
        Map<String, FileInfo[][]> map = new HashMap<String, FileInfo[][]>(2);

        doSplitFiles(fileInfo, localFiles, archivedFiles);

        if (!localFiles.isEmpty()) map.put(LOCAL_FILES, toArrays(localFiles));
        if (!archivedFiles.isEmpty()) map.put(ARCHIVED_FILES, toArrays(archivedFiles));

        return map;
    }

    private void doSplitFiles(FileInfo[][] fileInfo, List<FileInfo[]> localFiles, List<FileInfo[]> archivedFiles) {
        Set<String> availableFiles = new HashSet<String>();
        Set<Map<String, FileInfo>> retrievableFiles = new HashSet<Map<String, FileInfo>>();
        for (FileInfo[] finfos : fileInfo) {
            List<FileInfo> local = new ArrayList<FileInfo>();
            Map<String, FileInfo> archived = new HashMap<String, FileInfo>();
            for (FileInfo finfo : finfos) {
                if (finfo.status == FileStatus.ARCHIVED) {
                    // There can be several files for one instance with different MD5 sums
                    archived.put(finfo.sopIUID + "/" + finfo.md5, finfo);
                } else {
                    availableFiles.add(finfo.sopIUID + "/" + finfo.md5);
                    local.add(finfo);
                }
            }
            retrievableFiles.add(archived);
            if (!local.isEmpty()) localFiles.add(toArray(local));
        }
        for (Map<String, FileInfo> retrivables : retrievableFiles) {
            for (String sopMd5 : availableFiles) {
                retrivables.remove(sopMd5);
            }
            if (!retrivables.isEmpty()) archivedFiles.add(toArray(retrivables.values()));
        }
    }

    private FileInfo[][] toArrays(List<FileInfo[]> localFiles) {
        FileInfo[][] f = new FileInfo[localFiles.size()][];
        return localFiles.toArray(f);
    }

    private FileInfo[] toArray(Collection<FileInfo> files) {
        FileInfo[] f = new FileInfo[files.size()];
        return files.toArray(f);
    }

    static class EmptyMoveTask extends MoveTask {
        private static final Log logger = LogFactory.getLog(EmptyMoveTask.class);
        private static final String LOG_MESSAGE = "Files are archived. C-STORE to [{0}] will be issued after files are retrived from archive."; // NON-NLS

        public EmptyMoveTask(QueryRetrieveScpService service,
                             ActiveAssociation moveAssoc,
                             int movePcid,
                             Command moveRqCmd,
                             Dataset moveRqData,
                             AEDTO aeData,
                             String moveDest) throws DcmServiceException {
            super(service, moveAssoc, movePcid, moveRqCmd, moveRqData, new FileInfo[0][], aeData, moveDest);
            logger.info(MessageFormat.format(HsmAwareMoveScp.EmptyMoveTask.LOG_MESSAGE, aeData.getTitle()));
        }
    }
}
