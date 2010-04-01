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
 * Java(TM), available at http://sourceforge.net/projects/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * TIANI Medgraph AG.
 * Portions created by the Initial Developer are Copyright (C) 2003-2005
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Gunter Zeilinger <gunter.zeilinger@tiani.com>
 * Franz Willer <franz.willer@gwi-ag.com>
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
package org.dcm4chee.web.service;

import java.io.IOException;
import java.security.GeneralSecurityException;

import org.dcm4che2.data.BasicDicomObject;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.UID;
import org.dcm4che2.data.UIDDictionary;
import org.dcm4che2.data.VR;
import org.dcm4che2.net.Association;
import org.dcm4che2.net.CommandUtils;
import org.dcm4che2.net.DimseRSPHandler;
import org.dcm4che2.net.ExtRetrieveTransferCapability;
import org.dcm4che2.net.NoPresentationContextException;
import org.dcm4che2.net.TransferCapability;
import org.dcm4chee.web.service.common.AbstractScuService;

/**
 * @author franz.willer@gmail.com
 * @version $Revision$ $Date$
 * @since Jul 29, 2009
 */
public class MoveScuService extends AbstractScuService {

    private String calledAET;
    private boolean relationQR = true;

    private static final String[] ASSOC_CUIDS = {
        UID.StudyRootQueryRetrieveInformationModelMOVE,
        UID.PatientRootQueryRetrieveInformationModelMOVE,
        UID.PatientStudyOnlyQueryRetrieveInformationModelMOVERetired,
        UID.VerificationSOPClass };
    
    private static final String[] PATIENT_LEVEL_MOVE_CUID = {
        UID.PatientRootQueryRetrieveInformationModelMOVE,
        UID.PatientStudyOnlyQueryRetrieveInformationModelMOVERetired };

    private static final String[] STUDY_LEVEL_MOVE_CUID = {
        UID.StudyRootQueryRetrieveInformationModelMOVE,
        UID.PatientRootQueryRetrieveInformationModelMOVE,
        UID.PatientStudyOnlyQueryRetrieveInformationModelMOVERetired };

    private static final String[] SERIES_LEVEL_MOVE_CUID = {
        UID.StudyRootQueryRetrieveInformationModelMOVE,
        UID.PatientRootQueryRetrieveInformationModelMOVE };
    
    private String[] QR_LEVELS = {"IMAGE", "SERIES", "STUDY", "PATIENT"};
    private String[][] QR_MOVE_CUIDS = {SERIES_LEVEL_MOVE_CUID, SERIES_LEVEL_MOVE_CUID,
            STUDY_LEVEL_MOVE_CUID, PATIENT_LEVEL_MOVE_CUID};
    
    public MoveScuService() {
        super();
        configureTransferCapability(ASSOC_CUIDS, NATIVE_LE_TS);
    }

    public String getCalledAET() {
        return calledAET;
    }

    public void setCalledAET(String calledAET) {
        this.calledAET = calledAET;
    }

    public boolean isRelationQR() {
        return relationQR;
    }

    public void setRelationQR(boolean relationQR) {
        this.relationQR = relationQR;
    }

    public boolean move(String retrieveAET, String moveDest, String patId, String studyIUID, String seriesIUID) throws IOException, InterruptedException, GeneralSecurityException {
        MoveRspHandler rspHandler = new MoveRspHandler();
        this.move(retrieveAET, moveDest, patId, 
                "".equals(studyIUID) ? null : new String[]{studyIUID}, 
                "".equals(seriesIUID) ? null : new String[]{seriesIUID}, null, rspHandler, true); 
        return rspHandler.getStatus() == 0;
    }
    
    /**
     * Perform a DICOM C-MOVE request to given Application Entity Title.
     * @throws IOException 
     * @throws InterruptedException 
     * @throws GeneralSecurityException 
     */
    public void move(String retrieveAET, String moveDest, String patId, String[] studyIUIDs, 
            String[] seriesIUIDs, String[] sopIUIDs, DimseRSPHandler rspHandler, boolean waitAndCloseAssoc ) throws IOException, InterruptedException, GeneralSecurityException {
        if ( retrieveAET == null || "".equals(retrieveAET) )
            retrieveAET = calledAET;
        int qrLevelIdx = sopIUIDs != null ? 0 : seriesIUIDs != null ? 1
                : studyIUIDs != null ? 2 : 3;
        String qrLevel = QR_LEVELS[qrLevelIdx];
        String[] moveCUIDs = QR_MOVE_CUIDS[qrLevelIdx];
        Association assoc = open(retrieveAET);
        TransferCapability tc = selectTransferCapability(assoc, moveCUIDs);
        if ( tc == null ) {
            throw new NoPresentationContextException(UIDDictionary.getDictionary().prompt(moveCUIDs[0]));
        }
        String cuid = tc.getSopClass();
        String tsuid = tc.getTransferSyntax()[0];
        DicomObject keys = new BasicDicomObject();
        keys.putString(Tag.QueryRetrieveLevel, VR.CS, qrLevel);
        if (patId != null ) keys.putString(Tag.PatientID, VR.LO, patId);
        if (studyIUIDs != null ) keys.putStrings(Tag.StudyInstanceUID, VR.UI, studyIUIDs);
        if (seriesIUIDs != null ) keys.putStrings(Tag.SeriesInstanceUID, VR.UI, seriesIUIDs);
        if (sopIUIDs != null ) keys.putStrings(Tag.SOPInstanceUID, VR.UI, sopIUIDs);
        LOG.info("Send C-MOVE request using {}:\n{}",cuid, keys);
        if (rspHandler == null)
            rspHandler = new MoveRspHandler();
        assoc.cmove(cuid, priority, keys, tsuid, moveDest, rspHandler);
        if (waitAndCloseAssoc) {
            assoc.waitForDimseRSP();
            try {
                assoc.release(true);
            } catch (InterruptedException t) {
                LOG.error("Association release failed! aet:"+retrieveAET, t);
            }
        }
    }

    public void configureTransferCapability(String[] cuids, String[] ts) {
        TransferCapability[] tcs = new TransferCapability[cuids.length];
        ExtRetrieveTransferCapability tc;
        for (int i = 0 ; i < cuids.length ; i++) {
            tc = new ExtRetrieveTransferCapability(
                    cuids[i], ts, TransferCapability.SCU);
            tc.setExtInfoBoolean(
                    ExtRetrieveTransferCapability.RELATIONAL_RETRIEVAL, relationQR);
            tcs[i] = tc;
        }    
        setTransferCapability(tcs);
    }
    
    /**
     * Perform a DICOM Echo to given Application Entity Title.
     */
    public boolean echo(String title) {
        Association assoc = null;
        try {
            assoc = open(title);
        } catch (Throwable t) {
            log.error("Failed to establish Association aet:"+title, t);
            return false;
        }
        try {
            assoc.cecho().next();
        } catch (Throwable t) {
            log.error("Echo failed! aet:"+title, t);
            return false;
        }
        try {
            assoc.release(true);
        } catch (InterruptedException t) {
            log.error("Association release failed! aet:"+title, t);
        }
        return true;
    }

/*_*/   
    private class MoveRspHandler extends DimseRSPHandler {
        private int status;

        public int getStatus() {
            return status;
        }

        @Override
        public void onDimseRSP(Association as, DicomObject cmd,
                DicomObject data) {
            log.info("received C-MOVE-RSP:"+cmd);
            if (!CommandUtils.isPending(cmd)) {
                status = cmd.getInt(Tag.Status);
            }
        }
    }
    
}

