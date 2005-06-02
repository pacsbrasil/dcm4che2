package org.dcm4chex.archive.dcm.stymgt;

import java.io.Serializable;

import org.dcm4che.data.Command;
import org.dcm4che.data.Dataset;

public class PrivateStudyMgtOrder implements Serializable {
	
	private static final long serialVersionUID = 3258417226779603505L;

	private final String callingAET;

	private final String calledAET;
	
	private final int cmdField;
	
	private final int actionID;

	private final String iuid;

	private final Dataset ds;

	private int failureCount;

	private int failureStatus;

	public PrivateStudyMgtOrder(String callingAET, String calledAET,
			int cmdField, int actionID, String iuid, Dataset dataset){
		this.callingAET = callingAET;
		this.calledAET = calledAET;
		this.cmdField = cmdField;
		this.actionID = actionID;
		this.iuid = iuid;
		this.ds = dataset;
	}

	public final String getCalledAET() {
		return calledAET;
	}

	public final String getCallingAET() {
		return callingAET;
	}
	
	public final int getActionID() {
		return actionID;
	}

	public final int getCommandField() {
		return cmdField;
	}

	public final String getSOPInstanceUID() {
		return iuid;
	}
	
	public final Dataset getDataset() {
		return ds;
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

    public String toString() {
		StringBuffer sb = new StringBuffer("StudyMgtOrder[");
		
        return "StudyMgtOrder[" + cmdFieldAsString()
                + ", actionID=" + actionID
        		+ ", iuid=" + iuid
                + ", failureStatus="
                + Integer.toHexString(failureStatus).toUpperCase()
                + "H, failureCount=" + failureCount + "]";
    }

	private String cmdFieldAsString() {
	      switch (cmdField) {
	         case Command.N_SET_RQ:
	            return "N_SET_RQ";
	         case Command.N_ACTION_RQ:
	            return "N_ACTION_RQ";
	         case Command.N_CREATE_RQ:
	            return "N_CREATE_RQ";
	         case Command.N_DELETE_RQ:
	            return "N_DELETE_RQ";
	      }
		  return Integer.toHexString(cmdField).toUpperCase();
	}
}
