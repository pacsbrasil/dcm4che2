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
 * Bill Wallace, Agfa HealthCare Inc., 
 * Portions created by the Initial Developer are Copyright (C) 2008
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Bill Wallace <bill.wallace@agfa.com>
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
package org.dcm4chee.xero.wado;

import java.io.IOException;
import java.util.Map;

import org.dcm4che2.audit.message.AuditEvent;
import org.dcm4che2.audit.message.AuditMessage;
import org.dcm4che2.audit.message.InstancesAccessedMessage;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.imageio.plugins.dcm.DicomStreamMetaData;
import org.dcm4che2.imageioimpl.plugins.dcm.DicomImageReader;
import org.dcm4chee.xero.search.StudyInfo;
import org.dcm4chee.xero.search.StudyInfoCache;
import org.dcm4chee.xero.search.filter.AuditFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Audits query commands - whether they succeed or fail.
 * 
 * @author bwallace
 */
public class ViewAudit<T> extends AuditFilter<T> {
	public static final String VIEW_AUDIT = "view";

	private static final Logger log = LoggerFactory.getLogger(ViewAudit.class);

	private StudyInfoCache studyInfoCache = StudyInfoCache.getSingleton();

	/** Creates an InstancesAccessedMessage */
	@Override
	public AuditMessage createAuditMessage(Map<String, Object> params, T value) {
		DicomObject dobj;
		InstancesAccessedMessage msg = new InstancesAccessedMessage(AuditEvent.ActionCode.READ);
		if (value == null) {
			log.debug("Logging minor failure because return view is null.");
			msg.setOutcomeIndicator(AuditEvent.OutcomeIndicator.MINOR_FAILURE);
			return msg;
		}
		if (value instanceof DicomImageReader) {
			DicomStreamMetaData streamData;
			try {
				streamData = (DicomStreamMetaData) ((DicomImageReader) value).getStreamMetadata();
			} catch (IOException e) {
				log.error("Unable to reader dicom header:" + e);
				return null;
			}
			dobj = streamData.getDicomObject();
		} else {
			dobj = (DicomObject) value;
		}
		String studyUid = dobj.getString(Tag.StudyInstanceUID);
		if (studyUid == null) {
			log.warn("Logging serious failure as study UID not found.");
			msg.setOutcomeIndicator(AuditEvent.OutcomeIndicator.SERIOUS_FAILURE);
			return msg;
		}

		StudyInfo si = studyInfoCache.get(studyUid);
		synchronized (si) {
			String user = getUser(params);
			if (si.isAudited(user, VIEW_AUDIT)) {
				log.debug("Not logging user/view as study {} already seen", studyUid);
				return null;
			}
			log.debug("Logging regular message as study {} not seen yet.", studyUid);
			// Note that setting it to audited is a bit premature - there is a race condition
			// where the audit can fail to happen on server crash- it isn't clear how to fix this without
			// a significant time delay problem that even then has some flaws.
			si.putAudited(user,VIEW_AUDIT);
		}
		
		String pid = dobj.getString(Tag.PatientID);
		String pname = dobj.getString(Tag.PatientName);
		msg.addPatient(pid,pname);
		msg.addStudy(studyUid, null);
		
		return msg;
	}

}
