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

package org.dcm4chex.archive.web.maverick.permission;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ejb.CreateException;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.apache.log4j.Logger;
import org.dcm4chex.archive.ejb.interfaces.StudyPermissionDTO;
import org.dcm4chex.archive.hl7.StudyPermissionDelegate;
import org.dcm4chex.archive.util.EJBHomeFactory;
import org.dcm4chex.archive.util.HomeFactoryException;
import org.dcm4chex.archive.web.conf.StudyPermissionConfig;
import org.dcm4chex.archive.web.maverick.Dcm4cheeFormController;
import org.infohazard.maverick.flow.ControllerContext;
import org.jboss.mx.util.MBeanServerLocator;

/**
 * 
 * @author franz.willer@agfa.com
 * @version $Revision: 2531 $ $Date: 2006-06-20 16:49:49 +0200 (Di, 20 Jun 2006) $
 * @since 19.10.2007
 */
public class StudyPermissionCtrl extends Dcm4cheeFormController {

    private static final String CANCEL = "cancel";
    private static final int firstColumnWidth = 10;
    
    private Map permissions = new HashMap();
    
    private String cmdAdd;
    private String cmdDel;
    private String cmdCancel;
    
    private String suid;
    private String role;
    private String action;
    
    private Long patPk;
    private String patName;
    
    private static StudyPermissionConfig permissionCfg;
    private StudyPermissionDelegate delegate;
    
    private String popupMsg = null;
    
    private static Logger log = Logger.getLogger(StudyPermissionCtrl.class);
    
    protected String getCtrlName() {
	return "study_permission";
    }

    public void setAdd(String cmdAdd) {
        this.cmdAdd = cmdAdd;
    }

    public void setRemove(String cmd) {
        this.cmdDel = cmd;
    }

    public void setCancel(String cmdCancel) {
        this.cmdCancel = cmdCancel;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public void setStudyIUID(String suid) {
        this.suid = suid != null && suid.trim().length() > 0 ? suid : null;
    }
    public String getStudyIUID() {
        return suid;
    }
    
    public void setPatPk(Long patPk) {
		this.patPk = patPk;
	}
    
    public Long getPatPk() {
    	return patPk;
    }

	public void setPatName(String patName) {
		this.patName = patName;
	}
    public String getPatName() {
        return patName;
    }
    
    public StudyPermissionConfig getPermissionConfig() {
    	if ( permissionCfg == null ) {
    		permissionCfg = new StudyPermissionConfig();
    	}
    	return permissionCfg;
    }
    
    public List getColumnWidths() {
       	int countActions = getPermissionConfig().getActions().size();
       	ArrayList l = new ArrayList(countActions+1);
       	String colWidth = (100-firstColumnWidth)/countActions+"%";
       	l.add( firstColumnWidth+"%" );
       	for ( int i = 0; i < countActions ; i++) {
       		l.add( colWidth );
       	}
       	return l;
    }

    public Map getRolesWithActions() {
        return permissions;
    }
    
    /**
     * Return count of studies.
     * <p>
     * If study IUID is given: return 1.<br/>
     * If patPk is given: return total number of studies for this patient.
     * <p>
     * This count is used to determine if a permission is given to all studies of the patient.
     * 
     * @return
     */
    public int getCountStudies() throws Exception {
    	if ( patPk != null ) {
    		return delegate.countStudiesOfPatient(patPk);
    	} else {
    		return 1;
    	}
    }
    
    public String getPopupMsg() {
        return popupMsg;
    }    
    
    protected String perform() throws Exception {
    	initStudyPermissionDelegate(getCtx());
    	log.info("perform called: suid:"+suid+" patPk:"+patPk);
        if ( this.patPk == null && (suid == null || suid.trim().length() == 0) ) {
        	log.info("Missing SUID or patPk! suid:"+suid+" patPk:"+patPk);
            this.popupMsg = "Can't open Study Permission overview! StudyInstance UID or patient Pk required!";
            return CANCEL;
        }
        if ( cmdAdd != null ) {
            addPermission();
        } else if (cmdDel != null ) {
            removePermission();
        } else if ( cmdCancel != null ) {
            return CANCEL;
        }
        query();
        return SUCCESS;
    }

    private void removePermission() throws Exception {
        if ( checkParams("remove") ) {
        	if ( suid != null ) {
        		StudyPermissionDTO dto = new StudyPermissionDTO();
        		dto.setStudyIuid(suid);
        		dto.setRole(role);
        		dto.setAction(action);
        		delegate.revoke(dto);
        	} else {
        		delegate.revokeForPatient(patPk.longValue(), action, role);
        	}
        }
    }

    private void addPermission() throws Exception {
        if ( checkParams("add") ) {
        	if ( suid != null ) {
        		delegate.grant(suid, action, role);
        	} else {
        		delegate.grantForPatient(patPk.longValue(), action, role);
        	}
        }
    }

    private boolean checkParams(String cmd) {
        if ( role == null || role.trim().length() == 0 ) {
        	log.info("Missing role!");
            this.popupMsg = "Can't "+cmd+"Permission! Missing role!";
            return false;
        }
        if ( action == null || action.trim().length() == 0 ) {
        	log.info("Missing action!");
            this.popupMsg = "Can't "+cmd+"Permission! Missing action!";
            return false;
        }
        return true;
    }

    private void query() throws Exception {
    	Collection studyPermissions;
    	if ( patPk != null) {
    		if ( suid != null ) {
    			log.warn("Both Study Instance UID and Patient Pk are requested! Study Instance UID will be ignored!");
    			suid = null;
    		}
    		log.info("delegate.findByPatientPk("+patPk+")");
    		studyPermissions = delegate.findByPatientPk(patPk.longValue());
    	} else {
    		log.info("delegate.findByStudyIuid("+suid+")");
    		studyPermissions = delegate.findByStudyIuid(suid);
    	}
        StudyPermissionDTO dto;
        Map actions;
        List suids;
        for (Iterator iter = studyPermissions.iterator(); iter.hasNext(); ) {
            dto = (StudyPermissionDTO) iter.next();
            actions = (Map) permissions.get(dto.getRole());
            if ( actions == null ) {
                actions = new HashMap();
                permissions.put(dto.getRole(), actions);
            }
            suids = (List) actions.get(dto.getAction());
            if ( suids == null ) {
            	suids = new ArrayList();
                actions.put(dto.getAction(), suids);
            }
            suids.add( dto.getStudyIuid() );
        }
    }

    private StudyPermissionDelegate initStudyPermissionDelegate(ControllerContext ctx) throws MalformedObjectNameException, NullPointerException {
    	if ( delegate == null ) {
    		delegate = new StudyPermissionDelegate( MBeanServerLocator.locate() );
    		String s = ctx.getServletConfig().getInitParameter("studyPermissionServiceName");
    		delegate.setStudyPermissionServiceName(new ObjectName(s));
    	}
    	return delegate;
    }
}
