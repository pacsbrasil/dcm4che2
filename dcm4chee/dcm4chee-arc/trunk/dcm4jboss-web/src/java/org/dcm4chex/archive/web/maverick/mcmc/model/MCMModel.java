/*
 * Created on 21.12.2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.dcm4chex.archive.web.maverick.mcmc.model;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.ejb.FinderException;
import javax.servlet.http.HttpServletRequest;

import org.dcm4chex.archive.ejb.interfaces.MediaComposer;
import org.dcm4chex.archive.ejb.interfaces.MediaComposerHome;
import org.dcm4chex.archive.ejb.interfaces.MediaDTO;
import org.dcm4chex.archive.util.EJBHomeFactory;

/**
 * @author franz.willer
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class MCMModel {

	public static final String MCMMODEL_ATTR_NAME = "mcmModel";
	public static final String STATI_FOR_QUEUE = String.valueOf( MediaDTO.COLLECTING );
	
    public static final String NO_ERROR ="OK";
	public static final String ERROR_UNSUPPORTED_ACTION = "UNSUPPORTED_ACTION";

	private String errorCode = NO_ERROR;
	private int offset = 0;
	private int limit = 3;
	private int total = 0;
	private MediaList mediaList;
	private MCMFilter filter;

	
	private MCMModel() {
		getFilter();
		filterMediaList( true );
	}
	
	public static final MCMModel getModel( HttpServletRequest request ) {
		MCMModel model = (MCMModel) request.getSession().getAttribute(MCMMODEL_ATTR_NAME);
		if (model == null) {
				model = new MCMModel();
				request.getSession().setAttribute(MCMMODEL_ATTR_NAME, model);
				model.setErrorCode( NO_ERROR ); //reset error code
		}
		return model;
	}

	/**
	 * @param no_error2
	 */
	public void setErrorCode(String errorCode) {
		this.errorCode  = errorCode;
		
	}
	
	public String getErrorCode() {
		return errorCode;
	}
	
	public String getStatiForQueue() {
		return STATI_FOR_QUEUE;
	}
	
	public List getMediaList() {
		return mediaList;
	}
	
	public MCMFilter getFilter() {
		if ( filter == null ) filter = new MCMFilter();
		return filter;
	}
	
	public void filterMediaList( boolean newSearch ) {
		if ( newSearch ) offset = 0;
		try {
			Collection col = new ArrayList();
			Long start = null;
			Long end = null;
			Integer stati = null;
			if ( ! MCMFilter.MEDIA_TYPE_ALL.equals( filter.getSelectedStatus() ) ) { //not all
				try {
					stati = new Integer( filter.getSelectedStatus() );
				} catch ( Exception x ) {
					filter.setSelectedStatus( MCMFilter.MEDIA_TYPE_DEFAULT ); //set to default media type (COLLECTING)
				} 
			}
			if ( MCMFilter.CREATED_FILTER.equals( filter.getCreateOrUpdateDate() ) ) {
				start = filter.startCreationAsLong();
				end = filter.endCreationAsLong();
				total = lookupMediaComposer().findByCreatedTime( col, start, end, stati, 
				 						new Integer( offset ), new Integer( limit ), filter.isDescent() );
				
			} else if ( MCMFilter.UPDATED_FILTER.equals( filter.getCreateOrUpdateDate() ) ) {
				start = filter.startUpdateAsLong();
				end = filter.endUpdateAsLong();
				total = lookupMediaComposer().findByUpdatedTime( col, start, end, stati, 
 						new Integer( offset ), new Integer( limit ), filter.isDescent() );
			} else {
				lookupMediaComposer().getWithStatus( MediaDTO.COLLECTING );
			}
			
			//lookupMediaComposer().getWithStatus( MediaDTO.COLLECTING );
			mediaList = new MediaList( col );
			col.clear();
		} catch ( Exception x ) {
			//TODO
			x.printStackTrace();
			mediaList = new MediaList();
		}
	}
	
	protected MediaComposer lookupMediaComposer() throws Exception
	{
		MediaComposerHome home =
			(MediaComposerHome) EJBHomeFactory.getFactory().lookup(
					MediaComposerHome.class,
					MediaComposerHome.JNDI_NAME);
		return home.create();
	}			
	
	/**
	 * @return Returns the limit.
	 */
	public int getLimit() {
		return limit;
	}
	/**
	 * @param limit The limit to set.
	 */
	public void setLimit(int limit) {
		this.limit = limit;
	}
	/**
	 * @return Returns the offset.
	 */
	public int getOffset() {
		return offset;
	}
	/**
	 * @param offset The offset to set.
	 */
	public void setOffset(int offset) {
		this.offset = offset;
	}
	/**
	 * @return Returns the total.
	 */
	public int getTotal() {
		return total;
	}
	/**
	 * @param total The total to set.
	 */
	public void setTotal(int total) {
		this.total = total;
	}

	/**
	 * 
	 */
	public void performPrevious() {
		if ( offset - limit >= 0 ) {
			offset -= limit;
			filterMediaList( false );
		}
	}

	public void performNext() {
		if ( offset + limit < total ) {
			offset += limit;
			filterMediaList( false );
		}
	}

	/**
	 * @param queued
	 * @param string
	 */
	public void updateMediaStatus(int mediaPk, int status, String statusInfo) {
		MediaData md = mediaDataFromList( mediaPk );
		if ( md != null ) {
			md.setMediaStatus( status );
			md.setMediaStatusInfo( statusInfo );
			try {
				this.lookupMediaComposer().setMediaStatus( mediaPk, status, statusInfo );
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	
	public MediaData mediaDataFromList( int mediaPk ) {
		int pos = getMediaList().indexOf( new MediaData( mediaPk) );
		if ( pos != -1 ) {
			return (MediaData) this.mediaList.get( pos );
		}
		return null;
		
	}
	
}
