/*
 * Created on 09.12.2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.dcm4chex.wado.mbean;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.xml.transform.TransformerConfigurationException;

import org.apache.log4j.Logger;
import org.dcm4cheri.util.StringUtils;
import org.dcm4chex.wado.common.RIDRequestObject;
import org.dcm4chex.wado.common.RIDResponseObject;
import org.dcm4chex.wado.mbean.cache.WADOCache;
import org.dcm4chex.wado.mbean.cache.WADOCacheImpl;
import org.jboss.system.ServiceMBeanSupport;
import org.xml.sax.SAXException;

/**
 * @author franz.willer
 *
 * The MBean to manage the IHE RID (Retrieve Information for Display service.
 * <p>
 * This class use RIDSupport for the Retrieve Information for Display methods and WADOCache for caching documents.
 */
public class RIDService extends ServiceMBeanSupport  {

	private Logger log = Logger.getLogger( getClass().getName() );
	private WADOCache cache = WADOCacheImpl.getRIDCache();
	private RIDSupport support = new RIDSupport( this.server );
	
	private static final long GIGA = 1000000000L;
	private static final long MEGA = 1000000L;
	
	public void setCacheRoot( String newRoot ) {
		cache.setCacheRoot( newRoot );
	}
	
	public String getCacheRoot() {
		return cache.getCacheRoot();
	}
	
	public void setMinFreeSpace( int minFree ) {
		cache.setMinFreeSpace( minFree * MEGA );
	}
	
	public int getMinFreeSpace() {
		return (int) (cache.getMinFreeSpace() / MEGA);
	}

	public void setPreferredFreeSpace( int minFree ) {
		cache.setPreferredFreeSpace( minFree * MEGA );
	}
	
	public int getPreferredFreeSpace() {
		return (int) (cache.getPreferredFreeSpace() / MEGA);
	}
	
	/**
	 * Clears the cache.
	 * <p>
	 * Remove all files in the cache.
	 *
	 */
	public void clearCache() {
		cache.clearCache();
	}
	
	/**
	 * Cleans the cache if necessary.
	 *
	 */
	public void cleanCache() {
		cache.cleanCache( false ); //cleans the cache and wait until clean process is done. (not in background)
	}
	
	/**
	 * Returns the available space of the cache drive.
	 * <p>
	 * 
	 * @return
	 */
	public String getFreeSpace() {
		return getSizeWithUnit( cache.getFreeSpace() );
	}
	
	public String getRIDSummaryXsl() {
		String s = support.getRIDSummaryXsl();
		if ( s == null ) s = "";
		return s;
	}
	
	public void setRIDSummaryXsl( String xslFile ) {
		if ( xslFile != null && xslFile.trim().length() < 1 ) xslFile = null;
		support.setRIDSummaryXsl( xslFile );
	}
	
	/**
	 * @return Returns the useXSLInstruction.
	 */
	public boolean isUseXSLInstruction() {
		return support.isUseXSLInstruction();
	}
	/**
	 * @param useXSLInstruction The useXSLInstruction to set.
	 */
	public void setUseXSLInstruction(boolean useXSLInstruction) {
		support.setUseXSLInstruction( useXSLInstruction );
	}
	

	/**
	 * Returns a String with all defined SOP Class UIDs that are used to find ECG documents.
	 * <p>
	 * The uids are seperated with '|'.
	 * 
	 * @return SOP Class UIDs to find ECG related files.
	 */
	public String getECGSopCuids() {
		StringBuffer sb = new StringBuffer();
		Set set = support.getECGSopCuids();
		if ( set == null ) return "";
		Iterator iter = set.iterator();
		if ( iter.hasNext() ) {
			sb.append( iter.next() );
			while ( iter.hasNext() ) {
				sb.append("|").append( iter.next() );
			}
		}
		return sb.toString();
	}
	
	/**
	 * Set a list of SOP Class UIDs that are used to find ECG documents.
	 * <p>
	 * The UIDs are seperated with '|'.
	 * 
	 * @param sopCuids String with SOP class UIDs seperated with '|'
	 */
	public void setECGSopCuids( String sopCuids ) {
		String[] sa = StringUtils.split( sopCuids, '|');
		Set set = new HashSet();
		if ( sa != null && sa.length > 0 ) {
			for ( int i = 0, len = sa.length ; i < len ; i++ ) {
				if ( sa[i].trim().length() > 1 ) set.add( sa[i] );
			}
		}
		support.setECGSopCuids( set );
	}
	
	/**
	 * Getter for the name of the FileSystemMgt MBean.
	 * <p>
	 * This bean is used to locate the DICOM file.
	 *  
	 * @return Name of the MBean
	 */
	public String getFileSystemMgtName() {
		return support.getFileSystemMgtName().toString();
	}
	
	/**
	 * Setter for the name of the FileSystemMgt MBean.
	 * <p>
	 * This bean is used to locate the DICOM file.
	 *  
	 * @param Name of the MBean
	 */
	public void setFileSystemMgtName( String name ) {
		try {
			ObjectName on = new ObjectName( name );
			support.setFileSystemMgtName( on );
		} catch (MalformedObjectNameException e) {
		}
	}
	
	/**
	 * Get the requested Summary information object as Stream packed in a WADOResponseObject.
	 * <p>
	 *  
	 * @param reqVO The request parameters packed in an value object.
	 * 
	 * @return The value object containing the retrieved object or an error.
	 * @throws SQLException
	 * @throws SAXException
	 * @throws IOException
	 * @throws TransformerConfigurationException
	 */
	public RIDResponseObject getRIDSummary( RIDRequestObject reqVO ) throws SQLException, TransformerConfigurationException, IOException, SAXException {
		if ( log.isDebugEnabled() ) log.debug("getRIDSummary:"+reqVO );
		return support.getRIDSummary( reqVO );
	}
	
	/**
	 * Get the requested Document object as Stream packed in a WADOResponseObject.
	 * <p>
	 *  
	 * @param reqVO The request parameters packed in an value object.
	 * 
	 * @return The value object containing the retrieved object or an error.
	 */
	public RIDResponseObject getRIDDocument( RIDRequestObject reqVO ) {
		if ( log.isDebugEnabled() ) log.debug("getRIDDocument:"+reqVO );
		return support.getRIDDocument( reqVO );
	}

	
	/**
	 * Returns a String representation with trailing (G|M|K)B.
	 * <p>
	 * Rounds the value to a more readable form.
	 * <p>
	 * Use 1000 instead of 1024 for one KB.
	 * 
	 * @param size the size in bytes.
	 * 
	 * @return The size with unit. e.g. 2GB
	 */
	private String getSizeWithUnit(long size) {
		if ( size >= GIGA ){
				return (size/GIGA)+" GB";
		} else if ( size > MEGA ) {
			if ( (size % MEGA) == 0 ) {
				return (size/MEGA)+" MB";
			} else {
				return (size/1000L)+" KB";
			}
		} else {
			return (size/1000L)+" KB";
		}
	}

}
