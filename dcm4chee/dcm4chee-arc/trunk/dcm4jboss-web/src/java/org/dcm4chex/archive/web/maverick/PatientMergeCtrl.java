/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
package org.dcm4chex.archive.web.maverick;

import org.dcm4che.data.Dataset;
import org.dcm4che.dict.Tags;
import org.dcm4chex.archive.web.maverick.model.PatientModel;

/**
 * @author umberto.cappellini@tiani.com
 * @author gunter.zeilinger@tiani.com
 * @version $Revision$ $Date$
 */
public class PatientMergeCtrl extends Errable {

    private int pk;

    private int[] to_be_merged;

    private String merge = null;

    private String cancel = null;

    protected String perform() {
        try {
            if (merge != null) executeMerge();
            return SUCCESS;
        } catch (Exception e1) {
            this.errorType = e1.getClass().getName();
            this.message = e1.getMessage();
            this.backURL = "folder.m";
            return ERROR_VIEW;
        }
    }
    
    private String makeMergeDesc(Dataset ds) {
        return "Merged with [" + ds.getString(Tags.PatientID) +
        	"]" +  ds.getString(Tags.PatientName);
    }

    private void executeMerge() throws Exception {
        Dataset dominant = getPatient(pk).toDataset();
        Dataset[] priors = new Dataset[to_be_merged.length - 1];
        for (int i = 0, j = 0; i < to_be_merged.length; i++) {
            if (to_be_merged[i] != pk)
                priors[j++] = getPatient(to_be_merged[i]).toDataset();
        }        
        lookupPatientUpdate().mergePatient(dominant, priors);
        for (int i = 0; i < priors.length; i++) {
            AuditLoggerDelegate.logPatientRecord(getCtx(),
                    AuditLoggerDelegate.MODIFY,
                    dominant.getString(Tags.PatientID),
                    dominant.getString(Tags.PatientName),
                    makeMergeDesc(priors[i]));
            AuditLoggerDelegate.logPatientRecord(getCtx(),
                    AuditLoggerDelegate.DELETE,
                    priors[i].getString(Tags.PatientID),
                    priors[i].getString(Tags.PatientName),
                    makeMergeDesc(dominant));
        }
    }

    public final void setPk(int pk) {
        this.pk = pk;
    }

    public final void setToBeMerged(int[] tbm) {
        this.to_be_merged = tbm;
    }

    public final void setMerge(String merge) {
        this.merge = merge;
    }

    public PatientModel getPatient(int ppk) {
        return FolderForm.getFolderForm(getCtx().getRequest())
                .getPatientByPk(ppk);
    }

}