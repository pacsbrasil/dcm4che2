/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
package org.dcm4chex.archive.dcm.stgcmt;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.dcm4che.data.Dataset;
import org.dcm4chex.archive.ejb.jdbc.AEData;
import org.dcm4chex.archive.ejb.jdbc.FileInfo;

/**
 * @author <a href="mailto:gunter@tiani.com">Gunter Zeilinger</a>
 * @version $Revision$ $Date$
 * @since Jan 5, 2005
 */
class StgCmtOrder implements Serializable {

    private static final long serialVersionUID = 3258129137502991415L;

    private final Dataset actionInfo;

    private final AEData calledAE;

    private final String callingAET;

    private int failureCount;

    private int failureStatus;

    private final HashMap fileInfos;

    public StgCmtOrder(Dataset actionInfo, AEData calledAE, String callingAET,
            FileInfo[][] fileInfos) {
        this.actionInfo = actionInfo;
        this.calledAE = calledAE;
        this.callingAET = callingAET;
        if (fileInfos != null) {
            this.fileInfos = new HashMap();
            for (int i = 0; i < fileInfos.length; i++) {
                this.fileInfos.put(fileInfos[i][0].sopIUID, fileInfos[i]);
            }
        } else {
            this.fileInfos = null;
        }
    }

    public final int getFailureCount() {
        return failureCount;
    }

    public final void setFailureCount(int failureCount) {
        this.failureCount = failureCount;
    }

    public final int getFailureStatus() {
        return failureStatus;
    }

    public final void setFailureStatus(int failureStatus) {
        this.failureStatus = failureStatus;
    }

    public final Dataset getActionInfo() {
        return actionInfo;
    }

    public final Map getFileInfos() {
        return fileInfos;
    }
    
    public final AEData getCalledAE() {
        return calledAE;
    }
    
    public final String getCallingAET() {
        return callingAET;
    }
}
