/*
 * Created on 21.12.2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.dcm4chex.archive.web.maverick.mwl.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmElement;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.dict.Tags;
import org.dcm4chex.archive.web.maverick.mwl.MWLConsoleCtrl;

/**
 * @author franz.willer
 *
 * The Model for Media Creation Managment WEB interface.
 */
public class MWLModel {

	/** The session attribute name to store the model in http session. */
	public static final String MWLMODEL_ATTR_NAME = "mwlModel";
	
	/** Errorcode: no error */
    public static final String NO_ERROR ="OK";
    /** Errorcode: unsupported action */
	public static final String ERROR_UNSUPPORTED_ACTION = "UNSUPPORTED_ACTION";

	/** holds current error code. */
	private String errorCode = NO_ERROR;
	/** Holds the current offset for paging */
	private int offset = 0;
	/** Holds the limit for paging */
	private int limit = 10;
	/** Holds the total number of results of last search. */
	private int total = 0;

	private List mwlEntries = new ArrayList();

	private MWLFilter mwlFilter;
	
	private boolean isLocal = false;
	/**
	 * Creates the model.
	 * <p>
	 * Perform an initial media search with the default filter. <br>
	 * (search for all media with status COLLECTING)
	 * <p>
	 * performs an initial availability check for MCM_SCP service.
	 */
	private MWLModel() {
		getFilter();
	}
	
	/**
	 * Get the model for an http request.
	 * <p>
	 * Look in the session for an associated model via <code>MWLMODEL_ATTR_NAME</code><br>
	 * If there is no model stored in session (first request) a new model is created and stored in session.
	 * 
	 * @param request A http request.
	 * 
	 * @return The model for given request.
	 */
	public static final MWLModel getModel( HttpServletRequest request ) {
		MWLModel model = (MWLModel) request.getSession().getAttribute(MWLMODEL_ATTR_NAME);
		if (model == null) {
				model = new MWLModel();
				request.getSession().setAttribute(MWLMODEL_ATTR_NAME, model);
				model.setErrorCode( NO_ERROR ); //reset error code
				model.filterWorkList( true );
		}
		return model;
	}

	/**
	 * Set the error code of this model.
	 * 
	 * @param errorCode The error code
	 */
	public void setErrorCode(String errorCode) {
		this.errorCode  = errorCode;
		
	}
	
	/**
	 * Get current error code of this model.
	 * 
	 * @return error code.
	 */
	public String getErrorCode() {
		return errorCode;
	}
	
	
	/**
	 * Returns current page limit.
	 * 
	 * @return Returns the limit.
	 */
	public int getLimit() {
		return limit;
	}
	/**
	 * Set current page limit.
	 * 
	 * @param limit The limit to set.
	 */
	public void setLimit(int limit) {
		this.limit = limit;
	}
	/**
	 * Return current offset (page number; starts with 0).
	 * 
	 * @return Returns the offset.
	 */
	public int getOffset() {
		return offset;
	}
	/**
	 * Set current page offset
	 * @param offset The offset to set.
	 */
	public void setOffset(int offset) {
		this.offset = offset;
	}
	/**
	 * Return the total number of results of the last search.
	 * 
	 * @return Returns the total.
	 */
	public int getTotal() {
		return total;
	}
	
	public MWLFilter getFilter() {
		if ( mwlFilter == null ) {
			mwlFilter = new MWLFilter();
		}
		return mwlFilter;
	}
	
	/**
	 * @return Returns the mwlEntries.
	 */
	public List getMwlEntries() {
		return mwlEntries;
	}
	/**
	 * @return Returns the isLocal.
	 */
	public boolean isLocal() {
		return isLocal;
	}

	/**
	 * @param newSearch
	 */
	public void filterWorkList(boolean newSearch) {
		// TODO Auto-generated method stub
		if ( newSearch ) offset = 0;
		//_filterTest();
		Dataset searchDS = getSearchDS( mwlFilter );
		isLocal = MWLConsoleCtrl.getMwlScuDelegate().isLocal();
		List l = MWLConsoleCtrl.getMwlScuDelegate().findMWLEntries( searchDS );
		total = l.size();
		int end;
		if ( offset >= total ) {
			offset = 0;
			end = limit < total ? limit : total;
		} else {
			end = offset + limit;
			if ( end > total ) end = total;
		}
		Dataset ds;
		mwlEntries.clear();
		for ( int i = offset ; i < end ; i++ ){
			ds = (Dataset) l.get( i );
			if ( ds != null )
				mwlEntries.add( new MWLEntry( ds ) );
		}
	}

	/**
	 * @param mwlFilter2
	 * @return
	 */
	private Dataset getSearchDS(MWLFilter filter) {
		Dataset ds = DcmObjectFactory.getInstance().newDataset();
		DcmElement elem = ds.putSQ( Tags.SPSSeq );
		Dataset ds1 = elem.addNewItem();
		ds1.putAE( Tags.ScheduledStationAET, filter.getStationAET() );
		ds1.putSH( Tags.SPSID );
		ds1.putCS( Tags.Modality, filter.getModality() );
		ds1.putPN( Tags.ScheduledPerformingPhysicianName );
		ds1.putLO( Tags.SPSDescription );
		if ( filter.getStartDate() != null || filter.getEndDate() != null ) {
			Date startDate = null, endDate = null;
			if ( filter.startDateAsLong() != null ) 
				startDate = new Date ( filter.startDateAsLong().longValue() );
			if ( filter.endDateAsLong() != null ) 
				endDate = new Date ( filter.endDateAsLong().longValue() );
			ds1.putDA( Tags.SPSStartDate, startDate, endDate );
			ds1.putTM( Tags.SPSStartTime, startDate, endDate );
		} else {
			ds1.putDA( Tags.SPSStartDate );
			ds1.putTM( Tags.SPSStartTime );
		}
		ds1.putSH( Tags.ScheduledStationName, mwlFilter.getStationName() );
		ds.putPN( Tags.PatientName, mwlFilter.getPatientName() );
		ds.putLO( Tags.PatientID);
		ds.putSH( Tags.AccessionNumber, mwlFilter.getAccessionNumber() );
		ds.putSH( Tags.RequestedProcedureID );
		return ds;
	}

	private void _filterTest() {
		int len = offset+limit;
		total = 100;
		if ( len > total ) len = total;
		mwlEntries.clear();
		for ( int i = offset ; i < len ; i++ ) {
			mwlEntries.add( new MWLEntry( _getTestDS(i) ) );
		}
		
	}
	/**
	 * @param i
	 * @return
	 */
	private Dataset _getTestDS(int i) {
		Dataset ds = DcmObjectFactory.getInstance().newDataset();
		DcmElement elem = ds.putSQ( Tags.SPSSeq );
		Dataset ds1 = elem.addNewItem();
		ds1.putAE( Tags.ScheduledStationAET, "AET"+i );
		ds1.putSH( Tags.SPSID, "101."+i);
		if ( mwlFilter.getModality() == null ) {
			ds1.putCS( Tags.Modality, i % 2 == 0 ? "MR":"CT" );
		} else {
			ds1.putCS( Tags.Modality, mwlFilter.getModality() );
		}
		ds1.putPN( Tags.ScheduledPerformingPhysicianName, "Last"+(i/3)+"^First"+(i%3));
		ds1.putLO( Tags.SPSDescription, "desc"+i);
		long l = System.currentTimeMillis() - i*3600000l;
		ds1.putDT( Tags.SPSStartDateAndTime, new Date(l) );
		if ( mwlFilter.getStationName() == null ) {
			ds1.putSH( Tags.ScheduledStationName, ( i % 2 == 0 ? "MR":"CT") + "-Station"+(i/5) );
		} else {
			ds1.putSH( Tags.ScheduledStationName, mwlFilter.getStationName() );
		}
		ds.putPN( Tags.PatientName, mwlFilter.getPatientName()+i );
		ds.putLO( Tags.PatientID, "pat"+i);
		ds.putSH( Tags.AccessionNumber, "acc"+i);
		ds.putSH( Tags.RequestedProcedureID, "req"+i);
		return ds;
	}

	/**
	 * Goto previous page.
	 */
	public void performPrevious() {
		if ( offset - limit >= 0 ) {
			offset -= limit;
			filterWorkList( false );
		}
	}

	/**
	 * Goto next page.
	 *
	 */
	public void performNext() {
		if ( offset + limit < total ) {
			offset += limit;
			filterWorkList( false );
		}
	}
}
