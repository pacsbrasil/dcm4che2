/*
 * Created on 20.12.2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.dcm4chex.archive.web.maverick.mcmc.model;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.dcm4chex.archive.ejb.interfaces.MediaDTO;

/**
 * @author franz.willer
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class MediaData {

	private static final SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");
	private static Map mapDefinedStati;
	public static final Collection DEFINED_MEDIA_STATI = _getMediaStatusList();
	
	private int mediaPk;
	private Date createdTime;
	private Date updatedTime;
	private String filesetID;
	private String filesetIUID;
	private String mediaCreationRequestIUID;
	private int mediaStatus;
	private String mediaStatusString;
	private String mediaStatusInfo;
	private long mediaUsage;
	
	public MediaData( MediaDTO mediaDTO ) {
		mediaPk = mediaDTO.getPk();
		createdTime = mediaDTO.getCreatedTime();
		updatedTime = mediaDTO.getUpdatedTime();
		filesetID = mediaDTO.getFilesetId();
		filesetIUID = mediaDTO.getFilesetIuid();
		mediaCreationRequestIUID = mediaDTO.getMediaCreationRequestIuid();
		mediaStatus = mediaDTO.getMediaStatus();
		mediaStatusString = getStatusString( mediaStatus );
		mediaStatusInfo = mediaDTO.getMediaStatusInfo();
		mediaUsage = mediaDTO.getMediaUsage();
	}
	
	public MediaData( int pk ) {
		mediaPk = pk;
	}
	
	public MediaDTO asMediaDTO() {
		MediaDTO dto = new MediaDTO();
		dto.setPk( this.mediaPk );
		dto.setFilesetId( this.filesetID );
		dto.setFilesetIuid( this.filesetIUID );
		dto.setMediaStatus( this.mediaStatus );
		dto.setMediaStatusInfo( this.mediaStatusInfo );
		dto.setMediaUsage( this.mediaUsage );
		dto.setCreatedTime( this.createdTime );
		dto.setUpdatedTime( this.updatedTime );
		dto.setMediaCreationRequestIuid( this.mediaCreationRequestIUID );
		return dto;
	}
	
	public int getMediaPk() {
		return mediaPk;
	}
	
	/**
	 * @return Returns the createdTime.
	 */
	public String getCreatedTime() {
		return formatter.format(createdTime);
	}
	/**
	 * @return Returns the filesetID.
	 */
	public String getFilesetID() {
		return filesetID;
	}
	/**
	 * @return Returns the filesetIUID.
	 */
	public String getFilesetIUID() {
		return filesetIUID;
	}
	/**
	 * @return Returns the mediaCreationRequestIUID.
	 */
	public String getMediaCreationRequestIUID() {
		return mediaCreationRequestIUID;
	}
	
	public void setMediaStatus( int status ) {
		mediaStatus = status;
		mediaStatusString = getStatusString( mediaStatus );
	}
	
	/**
	 * @return Returns the mediaStatus.
	 */
	public int getMediaStatus() {
		return mediaStatus;
	}
	/**
	 * @return Returns the mediaStatusString.
	 */
	public String getMediaStatusString() {
		return mediaStatusString;
	}
	/**
	 * @return Returns the mediaStatusInfo.
	 */
	public String getMediaStatusInfo() {
		return mediaStatusInfo;
	}
	
	public void setMediaStatusInfo( String info ) {
		this.mediaStatusInfo = info;
	}
	
	/**
	 * @return Returns the mediaUsage.
	 */
	public long getMediaUsage() {
		return mediaUsage;
	}
	/**
	 * @return Returns the updatedTime.
	 */
	public String getUpdatedTime() {
		return formatter.format(updatedTime);
	}
	
	public static String getStatusString( int status ) {
		MediaStatus ms = (MediaStatus) mapDefinedStati.get( new Integer( status ) );
		if ( ms == null ) {
			return "unknown ("+status+")";
		} else {
			return ms.getDescription();
		}
	}
	
	/**
	 * @return
	 */
	private static Collection _getMediaStatusList() {
		mapDefinedStati = new HashMap();//TODO get from MediaDTO!
		mapDefinedStati.put( new Integer(MediaDTO.COLLECTING), new MediaStatus( MediaDTO.COLLECTING, "COLLECTING" ) );
		mapDefinedStati.put( new Integer(MediaDTO.QUEUED), new MediaStatus( MediaDTO.QUEUED, "QUEUED" ) );
		mapDefinedStati.put( new Integer(MediaDTO.PROCESSING), new MediaStatus( MediaDTO.PROCESSING, "PROCESSING" ) );
		mapDefinedStati.put( new Integer(MediaDTO.COMPLETED), new MediaStatus( MediaDTO.COMPLETED, "COMPLETED" ) );
		mapDefinedStati.put( new Integer(MediaDTO.QUEUE_ERROR), new MediaStatus( MediaDTO.QUEUE_ERROR, "QUEUE_ERROR" ) );
		return mapDefinedStati.values();
	}
	
    public boolean equals( Object o ) {
    	if ( o == null || ! ( o instanceof MediaData ) ) return false;
    	return this.getMediaPk() == ( (MediaData) o ).getMediaPk(); 
    }
    
    public int hashCode() {
    	return mediaPk;
    }
	
	/**
	 * Container Class for MediaStatus.
	 * <p>
	 * encapsulate status value as int and description (String)
	 * 
	 * @author franz.willer
	 *
	 * TODO To change the template for this generated type comment go to
	 * Window - Preferences - Java - Code Style - Code Templates
	 */
	public static class MediaStatus {
		int status;
		String description;
		
		public MediaStatus( int status, String desc ) {
			this.status = status;
			this.description = desc;
		}
		/**
		 * @return Returns the description.
		 */
		public String getDescription() {
			return description;
		}
		/**
		 * @return Returns the status.
		 */
		public int getStatus() {
			return status;
		}
	}

}

