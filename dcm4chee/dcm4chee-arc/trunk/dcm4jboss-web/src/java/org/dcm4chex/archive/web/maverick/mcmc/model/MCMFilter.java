/*
 * Created on 21.12.2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.dcm4chex.archive.web.maverick.mcmc.model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;

import org.dcm4chex.archive.ejb.interfaces.MediaDTO;

/**
 * @author franz.willer
 *
 * The Filter for searching media.
 */
public class MCMFilter {

	/** Identifier for searching within a creation time range. */
	public static final String CREATED_FILTER = "create";
	/** Identifier for searching within a update time range. */
	public static final String UPDATED_FILTER = "update";
	/** Identifier for searching all media stati. */
	public static final String MEDIA_TYPE_ALL = "-all-";
	/** The default stati to search for. (COLLECTING) */
	public static final String MEDIA_TYPE_DEFAULT = String.valueOf( MediaDTO.COLLECTING );

	/** The Date/Time formatter to parse input field values. (dd.MM.yyyy) */
	private static final SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");
	/** Collection with all defined media stati. (defined in MediaData) */ 
	private Collection mediaStatusList = null;
	/** holds the 'left' string value of creation time range. */
	private String startCreationDate = "";
	/** holds the 'right' string value of creation time range. */
	private String endCreationDate = "";
	/** holds the 'left' string value of update time range. */
	private String startUpdateDate = "";
	/** holds the 'right' string value of update time range. */
	private String endUpdateDate = "";
	/** holds the 'left' Long value of creation time range. (null if string value is empty) */
	private Long startCreationAsLong;
	/** holds the 'right' Long value of creation time range. (null if string value is empty) */
	private Long endCreationAsLong;
	/** holds the 'left' Long value of update time range. (null if string value is empty) */
	private Long startUpdateAsLong;
	/** holds the 'right' Long value of update time range. (null if string value is empty) */
	private Long endUpdateAsLong;
	/** holds the selected status for this filter */
	private String selectedStatus = "0";
	/** holds the switch between search of 'created' or 'updated' time range. */ 
	private String createOrUpdateDate = "create";
	/** Change status of this filter. */
	private boolean isChanged;
	/** holds sort order of this filter. */
	private boolean descent = true;
	
	/**
	 * Creates a new Filer for media search.
	 * <p>
	 * Set the Collection of defined media stati.
	 */
	public MCMFilter() { 
		mediaStatusList = MediaData.DEFINED_MEDIA_STATI;//List of all (in MediaData) defined media stati.
	}
	
	/**
	 * Returns the collection of defined media stati.
	 * 
	 * @return all defined media stati.
	 */
	public Collection getMediaStatusList() {
		return mediaStatusList;
	}
	/**
	 * @return Returns the endCreationDate.
	 */
	public String getEndCreationDate() {
		return endCreationDate;
	}
	/**
	 * Set the end creation date.
	 * <p>
	 * Set both <code>endCreationDate and endCreationAsLong</code>.<br>
	 * If the parameter is null or empty, both values are set to <code>null</code>
	 * 
	 * @param endCreationDate The endCreatenDate to set.
	 * 
	 * @throws ParseException If param is not a date/time string of format specified in formatter.
	 */
	public void setEndCreationDate(String endCreationDate) throws ParseException {
		if ( ! check( this.endCreationDate, endCreationDate ) ) return;
		if ( endCreationDate == null || endCreationDate.trim().length() < 1 ) {
			this.endCreationAsLong = null;
			this.endCreationDate = null;
		} else {
			this.endCreationAsLong = new Long( formatter.parse( endCreationDate ).getTime() );
			this.endCreationDate = endCreationDate;
		}
	}
	/**
	 * @return Returns the endUpdateDate.
	 */
	public String getEndUpdateDate() {
		return endUpdateDate;
	}
	/**
	 * Set the end update date.
	 * <p>
	 * Set both <code>endUpdateDate and endUpdateAsLong</code>.<br>
	 * If the parameter is null or empty, both values are set to <code>null</code>
	 *
	 * @param endUpdateDate The endUpdateDate to set.
	 * @throws ParseException
	 */
	public void setEndUpdateDate(String endUpdateDate) throws ParseException {
		if ( ! check( this.endUpdateDate, endUpdateDate ) ) return;
		if ( endUpdateDate == null || endUpdateDate.trim().length() < 1 ) {
			this.endUpdateAsLong = null;
			this.endUpdateDate = null;
		} else {
			this.endUpdateAsLong = new Long( formatter.parse( endUpdateDate ).getTime() );
			this.endUpdateDate = endUpdateDate;
		}
	}
	/**
	 * @return Returns the selectedStatus.
	 */
	public String getSelectedStatus() {
		return selectedStatus;
	}
	/**
	 * @param selectedStatus The selectedStatus to set.
	 */
	public void setSelectedStatus(String selectedStatus) {
		check( this.selectedStatus, selectedStatus );
		this.selectedStatus = selectedStatus;
	}
	/**
	 * @return Returns the startUpdateDate.
	 */
	public String getStartUpdateDate() {
		return startUpdateDate;
	}
	/**
	 * Set the start update date.
	 * <p>
	 * Set both <code>startUpdateDate and startUpdateAsLong</code>.<br>
	 * If the parameter is null or empty, both values are set to <code>null</code>
	 * 
	 * @param startUpdateDate The startUpdateDate to set.
	 * @throws ParseException
	 */
	public void setStartUpdateDate(String startUpdateDate) throws ParseException {
		if ( ! check( this.startUpdateDate, startUpdateDate ) ) return;
		if ( startUpdateDate == null || startUpdateDate.trim().length() < 1 ) {
			this.startUpdateAsLong = null;
			this.startUpdateDate = null;
		} else {
			this.startUpdateAsLong = new Long( formatter.parse( startUpdateDate ).getTime() );
			this.startUpdateDate = startUpdateDate;
		}
	}

	/**
	 * @return Returns the startCreationDate.
	 */
	public String getStartCreationDate() {
		return startCreationDate;
	}
	/**
	 * Set the start creation date.
	 * <p>
	 * Set both <code>startCreationDate and startCreationAsLong</code>.<br>
	 * If the parameter is null or empty, both values are set to <code>null</code>
	 * 
	 * @param startCreationDate The startCreationDate to set.
	 * @throws ParseException
	 */
	public void setStartCreationDate(String startCreationDate) throws ParseException {
		check( this.startCreationDate, startCreationDate );
		this.startCreationDate = startCreationDate;

		if ( ! check( this.startCreationDate, startCreationDate ) ) return;
		if ( startCreationDate == null || startCreationDate.trim().length() < 1 ) {
			this.startCreationAsLong = null;
			this.startCreationDate = null;
		} else {
			this.startCreationAsLong = new Long( formatter.parse( startCreationDate ).getTime() );
			this.startCreationDate = startCreationDate;
		}
	
	}
	/**
	 * @return Returns the createOrUpdateDate.
	 */
	public String getCreateOrUpdateDate() {
		return createOrUpdateDate;
	}
	/**
	 * @param createOrUpdateDate The createOrUpdateDate to set.
	 */
	public void setCreateOrUpdateDate(String createOrUpdateDate) {

		check( this.createOrUpdateDate, createOrUpdateDate );
		
		this.createOrUpdateDate = createOrUpdateDate;
	}
	
	/**
	 * Return sort order flag.
	 * 
	 * @return true for descending, false for ascending sort order
	 */
	public boolean isDescent() {
		return descent ;
	}
	
	/**
	 * Set the sort order.
	 * <p>
	 * true for descending, false for ascending.
	 * 
	 * @param desc.
	 */
	public void setDescent( boolean desc ) {
		isChanged = isChanged || ( desc ^ descent );
		descent = desc;
	}

	/**
	 * Set isChecked if params are not equal.
	 * <p>
	 * Used to check if this filter has changed.
	 * 
	 * @param o1 first param
	 * @param o2 second param
	 * 
	 * @return the current isChanged value;
	 */
	private boolean check(Object o1, Object o2 ) {
		if ( o1 == null ) {
			isChanged = isChanged || (o2 != null);
		} else {
			isChanged = isChanged || ( !o1.equals( o2 ));
		}
		return isChanged;
	}
	
	/**
	 * @return Returns the endCreationAsLong.
	 */
	public Long endCreationAsLong() {
		return endCreationAsLong;
	}
	/**
	 * @return Returns the endUpdateAsLong.
	 */
	public Long endUpdateAsLong() {
		return endUpdateAsLong;
	}
	/**
	 * @return Returns the startCreationAsLong.
	 */
	public Long startCreationAsLong() {
		return startCreationAsLong;
	}
	/**
	 * @return Returns the startUpdateAsLong.
	 */
	public Long startUpdateAsLong() {
		return startUpdateAsLong;
	}

	/**
	 * Returns the changed status of this filter.
	 * <p>
	 * Set the current changed state to false! 
	 * So further calls return always false until filter is changed.
	 * 
	 * @return true if this filter has been changed.
	 */
	public boolean isChanged() {
		if ( isChanged ) {
			isChanged = false;
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Return a short description of this filter.
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("MCMFilter: mediaStatus:").append(selectedStatus);
		sb.append(" createOrUpdateDate:").append(createOrUpdateDate);
		sb.append(" createDate:").append(startCreationDate).append(" - ").append(endCreationDate);
		sb.append(" updateDate:").append(startUpdateDate).append(" - ").append(endUpdateDate);
		sb.append(" Descent:").append(isDescent());
		sb.append(" changed:").append(isChanged);
		return sb.toString();
	}
}
