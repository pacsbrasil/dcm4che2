package org.dcm4chee.xero.display;

import org.jboss.seam.annotations.Begin;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.jboss.seam.ScopeType.CONVERSATION;

/** Control the display mode being used.  Defaults to window level */
@Name("DisplayMode")
@Scope(CONVERSATION)
public class DisplayMode {
	private static final Logger log = LoggerFactory.getLogger(DisplayMode.class);

	String mode="windowLevel";
	private int counter = 0;

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
