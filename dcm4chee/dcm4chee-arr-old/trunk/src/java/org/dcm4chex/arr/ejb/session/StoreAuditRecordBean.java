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
 * Joe Foraci <jforaci@users.sourceforge.net>
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

package org.dcm4chex.arr.ejb.session;

import java.sql.Timestamp;
import java.util.Date;
import javax.ejb.CreateException;
import javax.ejb.EJBException;

import javax.ejb.SessionBean;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.log4j.Logger;

import org.dcm4chex.arr.ejb.entity.AuditRecordLocalHome;

/**
 *  StoreAuditRecord bean.
 *
 * @author  <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
 * @author  <a href="mailto:joseph@tiani.com">joseph foraci</a>
 * @created  February 15, 2003
 * @version  $Revision$ $Date$
 * @ejb:bean  type="Stateless" name="StoreAuditRecord" view-type="local"
 *      local-jndi-name="ejb/StoreAuditRecord"
 * @ejb:transaction  type="Required"
 * @ejb:transaction-type  type="Container"
 * @ejb:ejb-ref  ejb-name="AuditRecord" view-type="local" ref-name="ejb/AuditRecord"
 */
public abstract class StoreAuditRecordBean implements SessionBean
{
    private final static Logger log = Logger.getLogger(StoreAuditRecordBean.class);
    private AuditRecordLocalHome home = null;
    
    private AuditRecordLocalHome getAuditRecordHome() {
    	if (home == null) {
			try {
				Context ctx = new InitialContext();
				home = (AuditRecordLocalHome) ctx.lookup("java:comp/env/ejb/AuditRecord");
			} catch (NamingException e) {
				log.error("Failed lookup ns:", e);
				throw new EJBException(e);
			}
    	}
    	return home;
	}

    /**
     * @param  xmldata Description of the Parameter
     * @ejb:interface-method
     */
    public void store(String content)
    {
        int start = content.indexOf('<');
		int end = content.lastIndexOf('>');
        if (start == -1 || end == -1)
            throw new IllegalArgumentException("No XML content: " + content);
        String xmldata = content.substring(start, end+1);
        Timestamp ts = null;
        String type, host, aet, userName, patientName, patientId;
        
        type = host = aet = userName = patientName = patientId = null;
        // parse xmldata for audit log
        ArrMsgParser msgParser;
        try {
            msgParser = new ArrMsgParserImpl(false); //non-validating
        	int retcode = msgParser.parse(xmldata);
            //if fields are missing: type, host, etc can display null, but for
            // timestamp we should generate the current time
        	type = msgParser.getType();
        	host = msgParser.getHost();
            aet = msgParser.getAet();
            userName = msgParser.getUserName();
            patientName = msgParser.getPatientName();
            patientId = msgParser.getPatientId();
            Date date = msgParser.getTimeStamp();
            if (date == null)
                date = new Date();
        	ts = new Timestamp(date.getTime());
        }
        catch (ArrInputException e) {
            log.error("ArrInputException: " + e);
            if (e.getCause() != null)
                log.error("  Cause: " + e.getCause());
        }
        try {
            log.info("Store AuditRecord[type=" + type
                     + ", host=" + host
                     + ", timestamp=" + ts
                     + "]");
            getAuditRecordHome().create(type, host, ts, aet, userName, patientName, patientId, xmldata);
        } catch (CreateException e) {
            log.error("Failed to insert new AuditRecord[" + type + "] in DB:", e);
            throw new EJBException(e);
        }
    }
}

