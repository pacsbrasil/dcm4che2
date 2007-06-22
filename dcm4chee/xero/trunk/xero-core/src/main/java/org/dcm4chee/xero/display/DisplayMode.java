package org.dcm4chee.xero.display;

import org.jboss.seam.annotations.Begin;
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
		  PATIENT_LEVEL, STUDY_LEVEL, SERIES_LEVEL, IMAGE_LEVEL
	}

	private String mode="windowLevel";
	private int counter = 0;
	ApplyLevel applyLevel = ApplyLevel.SERIES_LEVEL;

	public ApplyLevel getApplyLevel() {
		return applyLevel;
	}

	/** Set what level a change applies to. */
	public void setApplyLevel(String applyLevel) {
		if(applyLevel==null || applyLevel.length()==0 ) this.applyLevel = null;
		else if( applyLevel.equalsIgnoreCase("patient") ) this.applyLevel=ApplyLevel.PATIENT_LEVEL;
		else if( applyLevel.equalsIgnoreCase("study") ) this.applyLevel=ApplyLevel.STUDY_LEVEL;
		else if( applyLevel.equalsIgnoreCase("series") ) this.applyLevel=ApplyLevel.SERIES_LEVEL;
		else if( applyLevel.equalsIgnoreCase("image") ) this.applyLevel=ApplyLevel.IMAGE_LEVEL;
		else throw new IllegalArgumentException("Apply level must be one of patient, study, series and image but is "+applyLevel);
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
	 * An empty action that just returns "ok"
	 */
	@Begin(join=true)
	public String emptyAction() {
		log.info("Empty action - returning ok.");
		return "ok";
	}
	
	/** Get a counter to cause IE to re-fetch a new page... */
	public int getCounter() {
		return counter++;
	}
	public void setCounter(int value) {
		// No-op
	}
}
