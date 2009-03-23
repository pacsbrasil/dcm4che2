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

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.dcm4che2.audit.message.ActiveParticipant;
import org.dcm4che2.audit.message.AuditEvent;
import org.dcm4che2.audit.message.AuditMessage;
import org.dcm4che2.audit.message.ParticipantObject;
import org.dcm4che2.audit.message.ParticipantObject.IDTypeCode;
import org.dcm4che2.audit.message.ParticipantObject.TypeCode;
import org.dcm4che2.audit.message.ParticipantObject.TypeCodeRole;
import org.dcm4chee.xero.metadata.filter.Filter;
import org.dcm4chee.xero.metadata.filter.FilterItem;
import org.dcm4chee.xero.metadata.servlet.MetaDataServlet;

/**
 * Handles creation of audit messages based on a filter event.
 * 
 * @author bwallace
 *
 */
public abstract class AuditFilter<T> implements Filter<T> {
	// Need raw access to the log4j logger as it handles the audit message directly.
	private Logger auditLogger = Logger.getLogger("auditlog");
	private Logger log = Logger.getLogger(AuditFilter.class);

	/** Sets the audit logger to use */
	public void setAuditLogger(Logger auditLogger) {
		this.auditLogger = auditLogger;
	}
	
	/** Log the given audit message */
	public void log(AuditMessage msg) {
		auditLogger.info(msg);
	}

	/** Override to create the appropriate type of audit message, and to fill with any
	 * default types of values.  value can be null if no value is found.
	 */
	public abstract AuditMessage createAuditMessage(Map<String,Object> params, T value);
	
	/** Gets the user name for the current user */
	public String getUser(Map<String,Object> params) {
		return (String) params.get(MetaDataServlet.USER_KEY);
	}
	
	/** Override to create the message to be used on an exception - defaults to null to NOT
	 * audit exceptions.
	 * @param params
	 * @param e
	 * @return
	 */
	public AuditMessage createExceptionAuditMessage(Map<String,Object> params, Exception e) {
	  return null;
	}
	
	/** Fills the standard fields such as user id etc 
	 * Default outcome is minor failure if no value is found, or
	 * success if a value is found.
	 */
	protected void fillAuditMessage(Map<String,Object> params, AuditMessage msg, T value) {
		if( msg==null ) return;
		if( value==null ) {
	   	msg.setOutcomeIndicator(AuditEvent.OutcomeIndicator.MINOR_FAILURE);
	   } else {
	   	msg.setOutcomeIndicator(AuditEvent.OutcomeIndicator.SUCCESS);
	   }
		
		// Assign the local host informatoin
		ActiveParticipant apSrc = ActiveParticipant.createActivePerson(
				AuditMessage.getProcessID(),
				(String)params.get(MetaDataServlet.REQUEST_URI),
				AuditMessage.getProcessName(),
				AuditMessage.getLocalHostName(), false);
		msg.addActiveParticipant(apSrc);
		
		// Assign the user
		String user = (String) params.get(MetaDataServlet.USER_KEY);
		if( user!=null ) {
			ActiveParticipant ap = ActiveParticipant.createActivePerson(user,null,user,null,true);
			msg.addActiveParticipant(ap);
		} else {
			log.warn("User name is null - not setting user in audit message.");
		}
		
		// Assign the remote system information
		HttpServletRequest hsr = (HttpServletRequest) params.get(MetaDataServlet.REQUEST);
		if( hsr!=null ) {
			String url = hsr.getRequestURL().toString();
			ParticipantObject po = new ParticipantObject(url,IDTypeCode.URI);
			po.setParticipantObjectTypeCode(TypeCode.SYSTEM);
			po.setParticipantObjectTypeCodeRole(TypeCodeRole.USER);
			msg.addParticipantObject(po);
		}
		else {
			log.warn("No request information available.");
		}
		
	}
	
	/** Create the filter message etc */
   public T filter(FilterItem<T> filterItem, Map<String, Object> params) {
		T ret;
		try {
			ret = filterItem.callNextFilter(params);
		}
		catch(RuntimeException e) {
			AuditMessage msg = createExceptionAuditMessage(params,e);
			if( msg==null ) throw e;
			fillAuditMessage(params,msg,null);
			msg.setOutcomeIndicator(AuditEvent.OutcomeIndicator.SERIOUS_FAILURE);
			log(msg);
			throw e;
		}
		AuditMessage msg = createAuditMessage(params,ret);
		if( msg!=null ) {
			fillAuditMessage(params,msg,ret);
			log(msg);
		}
	   return ret;
   }
}
