package org.dcm4chex.archive.dcm.stymgt;

import java.io.IOException;

import org.dcm4che.data.Command;
import org.dcm4che.data.Dataset;
import org.dcm4che.dict.Tags;
import org.dcm4che.net.ActiveAssociation;
import org.dcm4che.net.DcmServiceBase;
import org.dcm4che.net.DcmServiceException;
import org.dcm4che.net.Dimse;

class StudyMgtScp extends DcmServiceBase {

	final StudyMgtScpService service;

	public StudyMgtScp(StudyMgtScpService service) {
		this.service = service;
	}

	protected Dataset doNAction(ActiveAssociation assoc, Dimse rq, Command rspCmd) throws IOException, DcmServiceException {
		Command cmd = rq.getCommand();
		int actionTypeID = cmd.getInt(Tags.ActionTypeID, -1);
		String iuid = cmd.getRequestedSOPInstanceUID();
		Dataset ds = rq.getDataset();
		service.sendStudyMgtNotification(assoc, Command.N_ACTION_RQ, actionTypeID, iuid, ds);
		return null;
	}

	protected Dataset doNCreate(ActiveAssociation assoc, Dimse rq, Command rspCmd) throws IOException, DcmServiceException {
		Command cmd = rq.getCommand();
		String iuid = cmd.getAffectedSOPInstanceUID();
		Dataset ds = rq.getDataset();
		service.sendStudyMgtNotification(assoc, Command.N_CREATE_RQ, 0, iuid, ds);
		return null;
	}

	protected Dataset doNDelete(ActiveAssociation assoc, Dimse rq, Command rspCmd) throws IOException, DcmServiceException {
		Command cmd = rq.getCommand();
		String iuid = cmd.getRequestedSOPInstanceUID();
		Dataset ds = rq.getDataset();
		service.sendStudyMgtNotification(assoc, Command.N_DELETE_RQ, 0, iuid, null);
		return null;
	}

	protected Dataset doNSet(ActiveAssociation assoc, Dimse rq, Command rspCmd) throws IOException, DcmServiceException {
		Command cmd = rq.getCommand();
		String iuid = cmd.getRequestedSOPInstanceUID();
		Dataset ds = rq.getDataset();
		service.sendStudyMgtNotification(assoc, Command.N_SET_RQ, 0, iuid, ds);
		return null;
	}

}
