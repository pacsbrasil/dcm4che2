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
 * Portions created by the Initial Developer are Copyright (C) 2007
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
package org.dcm4chee.xero.cycle;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.faces.context.FacesContext;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Begin;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Out;
import org.jboss.seam.log.Log;

@Name("CyclePageAction")
public class CyclePageAction {
	@Logger protected Log log;

	@In
	FacesContext facesContext;

	@In(required=false)
	@Out(required=false, scope=ScopeType.SESSION)
	Set<String> cycleList;
	
	@In(required=false)
	@Out(required=false, scope=ScopeType.SESSION)
	String cycleUrl;
	

	/** This method handles the cycle list submissions.  This includes all updates,
	 * even if the eventualy destination is NOT the cycle page - this will cause a 
	 * redirect to occur.
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@Begin(join=true)
	public String cycle() {
		Map<String,String[]> map = facesContext.getExternalContext().getRequestParameterValuesMap();
		String[] submitValues = map.get("submit");
		String submit = (submitValues==null ? null : submitValues[0]);
		if( submit==null ) return "success";
		if( submit.equals("Remove") ) {
			remove(map.get("patient"));
		}
		log.info("Cycle list called with submit="+submit);
		return submit;
	}
	
	/**
	 * Remove the given uids from the cycle list.  Uids maybe encoded with \
	 * to separate different elements.
	 * @param uids
	 */
	protected void remove(String[] uids) {
		if( cycleList==null ) return;
		if( uids==null ) return;
		int length = cycleList.size();
		for(String uid : uids) {
			if( uid.indexOf('\\')>=0 ) {
				String[] subUids = uid.split("\\\\");
				for(String subUid : subUids) {
					log.info("Removing sub-uid #0",subUid);
					cycleList.remove(subUid);
				}
			}
			else {
				log.info("Removing cycle list uid #0",uid);
				cycleList.remove(uid);
			}
		}
		if( length!=cycleList.size() ) updateCycleUrl();
		log.info("After removing items, cycle list length is #0, where it was #1 before.", cycleList.size(), length);
	}
	
	/**
	 * This method handles ensuring that cycle list is updated with the
	 * given study UID's, so as to allow displaying a given study.
	 */
	@SuppressWarnings("unchecked")
	@Begin(join=true)
	public String ensureStudyPresent() {
		Map<String,String[]> map = facesContext.getExternalContext().getRequestParameterValuesMap();
		String[] studyUids = map.get("studyUID");
		if( studyUids!=null ) {
			addCycle(Arrays.asList(studyUids));
		}
		return "success";
	}
	
	/**
	 * Handles adding to the cycle list
	 */
	protected void addCycle(List<String> uids) {
		int length = 0;
		if( cycleList==null ) {
			cycleList = new HashSet<String>(uids);
		} 
		else {
		  length = cycleList.size();
		  cycleList.addAll(uids);
		}
		// Optimization to avoid updates if nothing added.
		if( length!=cycleList.size() ) updateCycleUrl();
		log.info("Cycle url set to "+cycleUrl);
	}
	
	protected void updateCycleUrl() {
		StringBuffer sb = new StringBuffer("../study/study.xml");
		boolean isFirst = true;
		for(String uid : cycleList) {
			if( isFirst ) {
				sb.append("?StudyInstanceUID=");
				isFirst = false;
			}
			else {
				sb.append('\\');
			}
			sb.append(uid);
		}
		cycleUrl = sb.toString();
	}
}
