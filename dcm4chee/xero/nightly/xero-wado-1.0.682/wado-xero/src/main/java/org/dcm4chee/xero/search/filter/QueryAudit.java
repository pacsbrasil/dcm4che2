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
package org.dcm4chee.xero.search.filter;

import java.util.Map;

import org.dcm4che2.audit.message.AuditMessage;
import org.dcm4che2.audit.message.ParticipantObject;
import org.dcm4che2.audit.message.QueryMessage;
import org.dcm4chee.xero.metadata.filter.MemoryCacheFilter;
import org.dcm4chee.xero.search.StudyInfo;
import org.dcm4chee.xero.search.StudyInfoCache;
import org.dcm4chee.xero.search.study.PatientType;
import org.dcm4chee.xero.search.study.ResultsBean;
import org.dcm4chee.xero.search.study.StudyType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Audits query commands - whether they succeed or fail.
 * 
 * @author bwallace
 */
public class QueryAudit<T> extends AuditFilter<T> {
	private static final Logger log = LoggerFactory.getLogger(QueryAudit.class);

	private StudyInfoCache studyInfoCache = StudyInfoCache.getSingleton();

	public static final String QUERY_AUDIT = "query";

	/**
	 * Create a QueryMessage as a response. Returns null only if the results
	 * contains exactly 1 study and that study has already been audited by the
	 * given user. Otherwise, returns a QueryMessage to audit.
	 */
	@Override
	public AuditMessage createAuditMessage(Map<String, Object> params, T value) {
		if (checkAlreadyAudited(params, value))
			return null;
		QueryMessage msg = new QueryMessage();

		String queryParams = (String) params.get(MemoryCacheFilter.KEY_NAME);
		if( queryParams==null || queryParams.length()==0 ) {
			log.info("Not auditing because no parameters were provided.");
			return null;
		}
		ParticipantObject poQuery = new ParticipantObject(queryParams,
		      ParticipantObject.IDTypeCode.SEARCH_CRITERIA);
		msg.addParticipantObject(poQuery);
		return msg;
	}

	/**
	 * Check to see if the return has already been audited - this prevents a ton
	 * of small audit messages on every single render.
	 */
	protected boolean checkAlreadyAudited(Map<String, Object> params, Object value) {
		if (value == null)
			return false;
		if (!(value instanceof ResultsBean))
			return false;
		ResultsBean rb = (ResultsBean) value;
		if (rb.getPatient().size() != 1)
			return false;
		PatientType pt = rb.getPatient().get(0);
		if (pt.getStudy().size() != 1)
			return false;
		StudyType st = pt.getStudy().get(0);
		String studyUid = st.getStudyUID();
		String user = getUser(params);

		if( studyUid==null ) {
		    return false;
		}
		StudyInfo si = studyInfoCache.get(studyUid);
		synchronized (si) {
			if (si.isAudited(user, QUERY_AUDIT)) {
				log.debug("Not logging user/query as study {} already seen", studyUid);
				return true;
			}
			log.debug("Logging regular message as study {} not seen yet.", studyUid);
			// Note that setting it to audited is a bit premature - there is a race
			// condition
			// where the audit can fail to happen on server crash- it isn't clear
			// how to fix this without
			// a significant time delay problem that even then has some flaws.
			si.putAudited(user, QUERY_AUDIT);
		}

		return false;
	}
}
