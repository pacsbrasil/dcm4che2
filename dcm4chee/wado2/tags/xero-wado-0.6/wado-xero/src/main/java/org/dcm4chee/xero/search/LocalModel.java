package org.dcm4chee.xero.search;

/**
 * This interface supports local changes using the external model objects, and has
 * information such as getting the id, clearing all non-empty children etc.
 * @author bwallace
 *
 */
public interface LocalModel<T> {

	/** Clear any children of this object that are empty, and return true if this object is
	 * empty.
	 */
	boolean clearEmpty();
	
	/**
	 * Gets the identifier for this object
	 */
	T getId();
}
