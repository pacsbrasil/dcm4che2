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
