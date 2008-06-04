package org.dcm4chee.xero.controller;

import java.util.Map;

/**
 * Supports multiple actions being defined by a single object
 * @author bwallace
 *
 */
public interface MultiAction {

	Map<String,Action> getActions();
}
