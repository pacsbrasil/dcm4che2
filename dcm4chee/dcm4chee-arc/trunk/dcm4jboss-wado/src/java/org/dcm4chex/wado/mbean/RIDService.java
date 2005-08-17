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
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeMap;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.xml.transform.TransformerConfigurationException;

import org.dcm4che.dict.UIDs;
import org.dcm4cheri.util.StringUtils;
import org.dcm4chex.wado.common.RIDRequestObject;
import org.dcm4chex.wado.common.WADOResponseObject;
import org.dcm4chex.wado.mbean.cache.WADOCacheImpl;
import org.xml.sax.SAXException;

/**
 * @author franz.willer
 *
 * The MBean to manage the IHE RID (Retrieve Information for Display service.
 * <p>
 * This class use RIDSupport for the Retrieve Information for Display methods and WADOCache for caching documents.
 */
public class RIDService extends AbstractCacheService  {

	private RIDSupport support = new RIDSupport( this );
	private float waveformCorrection = 1f;
	
	public RIDService() {
		cache = WADOCacheImpl.getRIDCache();
	}
	public float getWaveformCorrection() {
		return waveformCorrection;
	}
	public void setWaveformCorrection(float waveformCorrection) {
		this.waveformCorrection = waveformCorrection;
	}
	
	public String getWadoURL() {
		return support.getWadoURL();
	}
	public void setWadoURL( String wadoURL ) {
		support.setWadoURL( wadoURL );
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
		Map uids = support.getECGSopCuids();
		if ( uids == null || uids.size() < 1 ) return "";
		StringBuffer sb = new StringBuffer( uids.size() << 5);//StringBuffer initial size: nrOfUIDs x 32
		Iterator iter = uids.keySet().iterator();
		sb.append(iter.next());
		while ( iter.hasNext() ) {
			sb.append('\r').append('\n').append(iter.next());
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
		
        StringTokenizer st = new StringTokenizer(sopCuids, "\r\n;");
        String uid,name;
        Map map = new TreeMap();
        int i = 0;
        while ( st.hasMoreTokens() ) {
        	uid = st.nextToken().trim();
    		name = uid;
        	if ( isDigit(uid.charAt(0) ) ) {
        		if ( ! UIDs.isValid(uid) ) 
        			throw new IllegalArgumentException("UID "+uid+" isn't a valid UID!");
        	} else {
        		uid = UIDs.forName( name );
        	}
        	map.put(name,uid);
        }
		support.setECGSopCuids( map );
	}

    private static boolean isDigit(char c) {
        return c >= '0' && c <= '9';
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
	public WADOResponseObject getRIDSummary( RIDRequestObject reqVO ) throws SQLException, TransformerConfigurationException, IOException, SAXException {
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
	public WADOResponseObject getRIDDocument( RIDRequestObject reqVO ) {
		if ( log.isDebugEnabled() ) log.debug("getRIDDocument:"+reqVO );
		return support.getRIDDocument( reqVO );
	}
	
}
