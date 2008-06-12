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
package org.dcm4chee.xero.display;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.jboss.seam.ScopeType.CONVERSATION;

/** Control the display mode and level being used.  Defaults to window level, series */
@Name("DisplayMode")
@Scope(CONVERSATION)
public class DisplayMode {
	private static final Logger log = LoggerFactory.getLogger(DisplayMode.class);

	public enum ApplyLevel {
		  PATIENT, STUDY, SERIES, IMAGE
	}

	private String mode="windowLevel";
	private int counter = 0;
	ApplyLevel applyLevel = ApplyLevel.SERIES;
	
	/** Trace whether to include XSLT in the current response */
	private String xslt=null;

	public ApplyLevel getApplyLevel() {
		return applyLevel;
	}

	/** Set what level a change applies to. */
	public void setApplyLevel(ApplyLevel applyLevel) {
		this.applyLevel = applyLevel;
	}
	
	/** Sets the apply level as a string */
	public void setApplyLevelStr(String applyLevel) {
		if(applyLevel==null || applyLevel.length()==0 ) this.applyLevel = null;
		else if( applyLevel.equalsIgnoreCase("patient") ) this.applyLevel=ApplyLevel.PATIENT;
		else if( applyLevel.equalsIgnoreCase("study") ) this.applyLevel=ApplyLevel.STUDY;
		else if( applyLevel.equalsIgnoreCase("series") ) this.applyLevel=ApplyLevel.SERIES;
		else if( applyLevel.equalsIgnoreCase("image") ) this.applyLevel=ApplyLevel.IMAGE;
		else throw new IllegalArgumentException("Apply level must be one of patient, study, series and image but is "+applyLevel);
		log.info("Setting apply level string to "+applyLevel+" final value "+this.applyLevel);
	}
	public String getApplyLevelStr() {
		if( applyLevel==null ) return "";
		return applyLevel.toString();
	}

	/** Get the major mode in use - controlling mouse listening, keyboard etc */
	public String getMode() {
		return mode;
	}

	/** Sets the mode in use 
	 * @param mode is the name of the mode that controls the mouse etc.
	 */
	public void setMode(String mode) {
		this.mode = mode;
	}
	
	/**
	 * Change mode action
	 */
	public String action() {
		log.info("Change mode to "+getMode());
		return "success";
	}
	
	/** Get a counter to cause IE to re-fetch a new page... */
	public int getCounter() {
		return counter++;
	}
	public void setCounter(int value) {
		// No-op
	}

   public String getXslt() {
      return xslt;
   }

   public void setXslt(String xslt) {
      this.xslt = xslt;
   }
}
