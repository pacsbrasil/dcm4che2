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

import org.dcm4chee.xero.search.study.WindowLevelMacro;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.ScopeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The window level class defines the window-level termporary data and validation
 * of that data, as well as what action is performed on an update.
 * 
 * @author bwallace
 *
 */
@Name("WindowLevel")
@Scope(ScopeType.EVENT)
public class WindowLevelAction {
	private static Logger log = LoggerFactory.getLogger(WindowLevelAction.class);

	float windowCenter=127.5f, windowWidth=256f;
	boolean wlSet = false;
	
	@In(value="SessionStudyModel", create=true)
	StudyModel studyModel;
	
	@In(value="DisplayMode", create=true)
	DisplayMode mode;

	/** Retrieve the window level center */
	public float getWindowCenter() {
		return windowCenter;
	}


	/** Set the window level center */
	public void setWindowCenter(float windowCenter) {
		this.windowCenter = windowCenter;
		this.wlSet = true;
	}


	/** Retrieve thew window width for window levelling */
	public float getWindowWidth() {
		return windowWidth;
	}


	/** Sets the window widths for window levelling */
	public void setWindowWidth(float windowWidth) {
		this.windowWidth = windowWidth;
		this.wlSet = true;
	}

    /** Indicate if the window level values were set */
	public boolean isWlSet() {
		return wlSet;
	}
	
	/** The window level has changed - apply this change to the appropriate level in the tree */
	public String action() {
		if( !isWlSet() ) return "failure";
	    DisplayMode.ApplyLevel applyLevel;
	    WindowLevelMacro wl = new WindowLevelMacro(getWindowCenter(), getWindowWidth(), "User");
	    if( mode!=null ) applyLevel = mode.getApplyLevel();
	    else applyLevel = DisplayMode.ApplyLevel.SERIES;
	    studyModel.apply(applyLevel,wl);
		return "success";
	}


	public StudyModel getStudyModel() {
		return studyModel;
	}


	public void setStudyModel(StudyModel studyModel) {
		this.studyModel = studyModel;
	}


	public DisplayMode getMode() {
		return mode;
	}


	public void setMode(DisplayMode mode) {
		this.mode = mode;
	}

}
