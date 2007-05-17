package org.dcm4chee.xero.display;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import static org.jboss.seam.ScopeType.CONVERSATION;

/** Control the display mode being used.  Defaults to window level */
@Name("DisplayMode")
@Scope(CONVERSATION)
public class DisplayMode {

	String mode="windowLevel";

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
}
