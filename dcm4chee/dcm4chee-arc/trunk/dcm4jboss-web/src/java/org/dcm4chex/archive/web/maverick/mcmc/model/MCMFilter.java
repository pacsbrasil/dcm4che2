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
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class MCMFilter {

	public static final String CREATED_FILTER = "create";
	public static final String UPDATED_FILTER = "update";
	public static final String MEDIA_TYPE_ALL = "-all-";
	public static final String MEDIA_TYPE_DEFAULT = String.valueOf( MediaDTO.COLLECTING );

	private static final SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");
	
	private Collection mediaStatusList = null;
	private String startCreationDate = "";
	private String endCreationDate = "";
	private String startUpdateDate = "";
	private String endUpdateDate = "";
	private Long startCreationAsLong;
	private Long endCreationAsLong;
	private Long startUpdateAsLong;
	private Long endUpdateAsLong;
	private String selectedStatus = "0";
	private String createOrUpdateDate = "create";
	private boolean isChanged;
	private boolean descent = true;
	
	public MCMFilter() { 
		mediaStatusList = MediaData.DEFINED_MEDIA_STATI;//List of all (in MediaData) defined media stati.
	}
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
	 * @param endCreationDate The endCreatenDate to set.
	 * @throws ParseException
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
	 * @return
	 */
	public boolean isDescent() {
		return descent ;
	}
	
	public void setDescent( boolean desc ) {
		isChanged = isChanged || ( desc ^ descent );
		descent = desc;
	}

	
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
	 * @return
	 */
	public boolean isChanged() {
		if ( isChanged ) {
			isChanged = false;
			return true;
		} else {
			return false;
		}
	}
	
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
