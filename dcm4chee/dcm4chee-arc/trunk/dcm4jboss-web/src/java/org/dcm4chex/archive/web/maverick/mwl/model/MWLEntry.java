/*
 * Created on 22.02.2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.dcm4chex.archive.web.maverick.mwl.model;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.dcm4che.data.Dataset;
import org.dcm4che.data.PersonName;
import org.dcm4che.dict.Tags;

/**
 * @author franz.willer
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class MWLEntry {

	private Dataset ds;
	private Dataset spsItem;
	
	/** The Date/Time formatter to format date/time values. */
	private static final SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
	
	
	public MWLEntry( Dataset ds ) {
		this.ds = ds;
		System.out.println("ds:"+ds);
		System.out.println("seq:"+ds.get( Tags.SPSSeq ));
		spsItem = ds.get( Tags.SPSSeq ).getItem();//scheduled procedure step sequence item.
	}
	
	public String getSpsID() {
		return spsItem.getString( Tags.SPSID );
	}
	/**
	 * @return Returns the modality.
	 */
	public String getModality() {
		return spsItem.getString( Tags.Modality );
	}
	/**
	 * @return Returns the physiciansName.
	 */
	public String getPhysiciansName() {
		PersonName pn =  spsItem.getPersonName( Tags.ScheduledPerformingPhysicianName );
		return pn.get( PersonName.GIVEN )+" "+pn.get( PersonName.FAMILY );
	}
	/**
	 * @return Returns the reqProcedureDesc.
	 */
	public String getReqProcedureDesc() {
		return ds.getString( Tags.RequestedProcedureDescription );
	}
	/**
	 * @return Returns the reqProcedureID.
	 */
	public String getReqProcedureID() {
		return ds.getString( Tags.RequestedProcedureID );
	}
	/**
	 * @return Returns the spsDesc.
	 */
	public String getSpsDesc() {
		return spsItem.getString( Tags.SPSDescription );
	}
	/**
	 * @return Returns the spsStartDateTime.
	 */
	public String getSpsStartDateTime() {
		Date d = spsItem.getDate( Tags.SPSStartDateAndTime );
		if ( d == null ) {
			d = spsItem.getDateTime( Tags.SPSStartDate, Tags.SPSStartTime );
		}
		if ( d == null ) return "";
		
		return formatter.format( d );
	}

	/**
	 * @return Returns the stationAET.
	 */
	public String getStationAET() {
		return spsItem.getString( Tags.ScheduledStationAET );
	}
	/**
	 * @return Returns the stationName.
	 */
	public String getStationName() {
		return spsItem.getString( Tags.ScheduledStationName );
	}
	
	public String getAccessionNumber() {
		return ds.getString( Tags.AccessionNumber );
	}
	
	public String getPatientName() {
		return ds.getString( Tags.PatientName );
	}
	public String getPatientID() {
		return ds.getString( Tags.PatientID );
	}
}
