/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is part of dcm4che, an implementation of DICOM(TM) in
 * Java(TM), hosted at http://sourceforge.net/projects/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * Bill Wallace, Agfa HealthCare Inc., 
 * Portions created by the Initial Developer are Copyright (C) 2007
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Bill Wallace <bill.wallace@agfa.com>
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 *
 * ***** END LICENSE BLOCK ***** */
package org.dcm4chee.xero.search;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.dcm4che2.data.BasicDicomObject;
import org.dcm4che2.data.DicomElement;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.UID;
import org.dcm4che2.data.VR;
import org.dcm4che2.net.Association;
import org.dcm4che2.net.CommandUtils;
import org.dcm4che2.net.Device;
import org.dcm4che2.net.DimseRSP;
import org.dcm4che2.net.Executor;
import org.dcm4che2.net.ExtQueryTransferCapability;
import org.dcm4che2.net.NetworkApplicationEntity;
import org.dcm4che2.net.NetworkConnection;
import org.dcm4che2.net.NewThreadExecutor;
import org.dcm4che2.net.TransferCapability;
import org.dcm4chee.xero.metadata.filter.Filter;
import org.dcm4chee.xero.metadata.filter.FilterItem;
import org.dcm4chee.xero.search.study.ResultsBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a finder for DICOM results.  By default, it looks for items from clazz
 * and returns results of that type.
 * @author bwallace
 *
 * @param <clazz>
 */
public abstract class DicomCFindFilter implements Filter<ResultFromDicom>
{
	public static final String EXTEND_RESULTS_KEY = "EXTEND_RESULTS";

	static Logger log = LoggerFactory.getLogger(DicomCFindFilter.class);
	
    private Executor executor = new NewThreadExecutor("XERO_QUERY");
    private NetworkApplicationEntity remoteAE = new NetworkApplicationEntity();
    private NetworkConnection remoteConn = new NetworkConnection();
    private Device device = new Device("XERO");
    private NetworkApplicationEntity ae = new NetworkApplicationEntity();

    private NetworkConnection conn = new NetworkConnection();
    private Association assoc;
    private int priority = 0;
    private int cancelAfter = Integer.MAX_VALUE;
	private String hostname = "localhost";
    
    private static final String[] NATIVE_LE_TS = {
        UID.ImplicitVRLittleEndian,
        UID.ExplicitVRLittleEndian  };
    
    
    public DicomCFindFilter() {
		remoteAE.setInstalled(true);
		remoteAE.setAssociationAcceptor(true);
		remoteAE.setNetworkConnection(new NetworkConnection[] { remoteConn });
		remoteAE.setAETitle("DCM4CHEE");
		device.setNetworkApplicationEntity(ae);
		device.setNetworkConnection(conn);
		ae.setNetworkConnection(conn);
		ae.setAssociationInitiator(true);
		ae.setAETitle("XERO");

		try {
			// The hostname is sometimes provided as upper case, but DCM4CHEE
			// doesn't like that.
			if( hostname==null ) {
				hostname = InetAddress.getLocalHost().getHostName().toLowerCase();
			}
			log.debug("Using DICOM host "+hostname);
		} catch (UnknownHostException e) {
			log.warn("Unable to get local hostname:"+e);
		}
		conn.setHostname(hostname);
		
		remoteConn.setHostname(hostname);
		remoteConn.setPort(11112);
		
		ae.setPackPDV(true);
		conn.setTcpNoDelay(true);
		ae.setMaxOpsInvoked(1);
		configureTransferCapability();
	}
    
    /**
     * Return the CUID values to try to negotiate.  This must be over-ridden by the inheriting class. 
     */
    protected abstract String[] getCuids();

    /**
     * Configure the transfer capabilities to request - based on the type of the object being requested from.
     */
    protected void configureTransferCapability()
    {
    	String[] cuids = getCuids();
    	TransferCapability[] tc = new TransferCapability[cuids.length];
    	int i=0;
    	for(String cuid : cuids ) {
    		tc[i++] = mkFindTC(cuid,NATIVE_LE_TS);
    	}
    	ae.setTransferCapability(tc);
    }
    
    protected boolean getRelationQR()
    {
    	return false;
    }
    
    protected boolean getSemanticPersonNameMatching()
    {
    	return false;
    }

    /** Make a find transfer capability object for use in requesting the transfer capabilities. */
    private TransferCapability mkFindTC(String cuid, String[] ts) {
        ExtQueryTransferCapability tc = new ExtQueryTransferCapability(cuid,
                ts, TransferCapability.SCU);
        tc.setExtInfoBoolean(ExtQueryTransferCapability.RELATIONAL_QUERIES, getRelationQR());
        tc.setExtInfoBoolean(ExtQueryTransferCapability.DATE_TIME_MATCHING, false);
        tc.setExtInfoBoolean(ExtQueryTransferCapability.FUZZY_SEMANTIC_PN_MATCHING,
                getSemanticPersonNameMatching());
        return tc;
    }

    /** This method must return the query level, eg STUDY, PATIENT etc */
    protected abstract String getQueryLevel();
    
    /** Return the set of key to negotiate for */
    protected abstract int[] getReturnKeys();
    
    
    /**
     * Format a date to the DICOM internal format for dates only.
     * @param d
     * @return DICOM formatted date.
     */
    protected String formatDate(Calendar cal) {
       SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd", Locale.US);
       return sdf.format(cal.getTime());
    }
        
    /**
     * Handles various types of date values such as Today, Yesterday etc
     * @param value
     * @return
     */
   protected String parseDateValue(String value) {
       if( value.length()==0 ) return "";
       if( Character.isDigit(value.charAt(0)) || value.charAt(0)=='-' ) return value;
       // It must be an enumerated value
       String ret = value;
       GregorianCalendar start = new GregorianCalendar();
       Calendar end=null;
       if( value.equalsIgnoreCase("TODAY") ) {
    	  // No-op
       }
       else if( value.equalsIgnoreCase("SINCEYESTERDAY") ) {
    	  start.add(Calendar.DAY_OF_MONTH, -1);
       }
       else if( value.equalsIgnoreCase("YESTERDAY") ) {
    	  start.add(Calendar.DAY_OF_MONTH, -1);
    	  end = new GregorianCalendar();
       }
       else if( value.equalsIgnoreCase("LAST10YEARS") ) {
    	  start.add(Calendar.YEAR,-10);
       }
       else if( value.equalsIgnoreCase("LAST7DAYS") ) {
    	  start.add(Calendar.DAY_OF_MONTH,-7);
       }
       else if( value.equalsIgnoreCase("Last4Weeks") ) {
    	  start.add(Calendar.DAY_OF_MONTH,-28);
       }
       else if( value.equalsIgnoreCase("ThisMonth") ) {
    	  start.set(Calendar.DAY_OF_MONTH, 1);
       }
       else if( value.equalsIgnoreCase("LastMonth") ) {
    	  start.set(Calendar.DAY_OF_MONTH, 1);
    	  start.add(Calendar.MONTH,-1);
    	  end = new GregorianCalendar();
    	  end.set(Calendar.DAY_OF_MONTH,1);
       }
       else if( value.equalsIgnoreCase("ThisYear") ) {
    	  start.set(Calendar.DAY_OF_MONTH,1);
    	  start.set(Calendar.MONTH,0);
       }
       else if(value.equalsIgnoreCase("LastYear") ) {
    	  start.set(Calendar.DAY_OF_MONTH,1);
    	  start.set(Calendar.MONTH,0);
    	  start.add(Calendar.YEAR,-1);
    	  end = new GregorianCalendar();
    	  end.set(Calendar.DAY_OF_MONTH,1);
    	  end.set(Calendar.MONTH,0);
       }
       else if(value.equalsIgnoreCase("SinceLastYear") ) {
    	  start.set(Calendar.DAY_OF_MONTH,1);
    	  start.set(Calendar.MONTH,0);
    	  start.add(Calendar.YEAR,-1);
       }
       else {
    	  log.warn("Unknown date/time format string "+value);
    	  return value;
       }
       ret = (start!=null ? formatDate(start) : "")+"-"+(end!=null ? formatDate(end) : "");
       log.info("Using "+ret+" for date time key "+value);
       return ret;
    }
    
    /**
     * Generates a set of dicom keys related to the search condition, as well as ensuring that
     * the required fields are returned.
     * @param searchCondition
     * @return
     */
    protected DicomObject generateKeys(SearchCriteria searchCriteria)
    {
    	DicomObject ret = new BasicDicomObject();
    	ret.putString(Tag.QueryRetrieveLevel, VR.CS, getQueryLevel());
    	int[] returnKeys = getReturnKeys();
    	for( int itag : returnKeys)
    	{
    		ret.putNull(itag,null);
    	}
    	Map<String,TableColumn> searchCondition = searchCriteria.getAttributeByName();
    	for(String key : searchCondition.keySet()) {
    		String keyTag = Character.toUpperCase(key.charAt(0))+key.substring(1);
    		TableColumn tc = searchCondition.get(key);
    		List content = tc.getContent();
    		String value;
    		if( content.size()== 1 ) {
    			Object contentVal = content.get(0);
    			if( contentVal instanceof String ) {
    				value = (String) contentVal;
    			}
    			else {
    				throw new UnsupportedOperationException("Can't search on anything except simple values.");
    			}
    			// TODO - this probably needs to be decided by looking up the tag
    			// and seeing if it really is a datetime tag, but for now just support date tags.
    			if( key.endsWith("DateTime") ) {
    			   keyTag = keyTag.substring(0,keyTag.length()-4);
    			   value = parseDateValue(value);
    			}
    		} 
    		else {
    			if( key.endsWith("UID") ) {
    				String first = (String) content.get(0);
    				StringBuffer sb = new StringBuffer(first);
    				for(int i=1; i<content.size(); i++) {
    					String next = (String) content.get(i);
    					if( next.length()==0 ) continue;
    					sb.append("\\").append(next);
    				}
    				value = sb.toString();
    			}
    			else {
    				throw new UnsupportedOperationException("Can't search on multi-valued key "+key);
    			}
    		}
    		log.debug("Search condition "+keyTag+"="+value+" of type "+value.getClass());
    		try {
    			ret.putString(Tag.toTagPath(keyTag), null, value );
    		}
    		catch(IllegalArgumentException e) {
    			e.printStackTrace();
    			throw new IllegalArgumentException("Unknown tag name '"+keyTag+"'");
    		}
    	}
    	return ret;
    }
    
    /**
     * This method searches through the provided transfer capabilities until one is found that
     * is supported at the remote end.
     * @param cuid
     * @return
     */
    protected TransferCapability selectTransferCapability(String[] cuid)
    {
        TransferCapability tc;
        for (int i = 0; i < cuid.length; i++) {
        	log.debug("Looking for transfer capability "+cuid[i]);
            tc = assoc.getTransferCapabilityAsSCU(cuid[i]);
            if (tc != null)
                return tc;
        }
        return null;
    }

    /**
     * This searches through the various transfer syntaxes to try to find a supported one.
     * The preference is DeflatedExplicitVRLittleEnding, otherwise the first one on the list.
     * @param tc
     * @return
     */
    private String selectTransferSyntax(TransferCapability tc) {
        String[] tcuids = tc.getTransferSyntax();
        if (Arrays.asList(tcuids).indexOf(UID.DeflatedExplicitVRLittleEndian) != -1)
            return UID.DeflatedExplicitVRLittleEndian;
        return tcuids[0];
    }

    /** Adds this object to the result - allows overriding to test for specific conditions in the data */
    protected void addResult(ResultFromDicom resultFromDicom, DicomObject data) {
       resultFromDicom.addResult(data);
    }
    
    /**
     * This method performs a remote query against the local DCM4CHEE ae instance, on port 11112, and
     * returns the result as a set of objects of type E.
     */
	public void find(SearchCriteria searchCriteria, ResultFromDicom resultFromDicom) {
		try {
		   log.info("Connecting to "+hostname+" remoteAE="+remoteAE+" on conn="+conn+" at level "+getQueryLevel());
		   long start = System.nanoTime();
	       assoc = ae.connect(remoteAE, executor);
	       TransferCapability tc = selectTransferCapability(getCuids());
	       if ( tc==null )
	    	   throw new RuntimeException("Can't agree on any transfer capabilities with device.");
	       String cuid = tc.getSopClass();
	       String tsuid = selectTransferSyntax(tc);
	       DicomObject keys = generateKeys(searchCriteria);
	       DimseRSP rsp = assoc.cfind(cuid, priority, keys, tsuid, cancelAfter);
	       int cntResults = 0;
	       while(rsp.next()) {
	    	   DicomObject cmd = rsp.getCommand();
	    	   if (CommandUtils.isPending(cmd)) {	    		  
	    		   DicomObject data = rsp.getDataset();
	    		   if( data.contains(Tag.DirectoryRecordSequence) ) {
	    			  log.info("Using blocked structure.");
	    			  DicomElement dir = data.get(Tag.DirectoryRecordSequence);
	    			  for(int i=0, n=dir.countItems(); i<n; i++) {
	    				 data = dir.getDicomObject(i);
	    				 addResult(resultFromDicom,data);
	    				 cntResults++;
	    			  }
	    		   } 
	    		   else {
	    			  addResult(resultFromDicom,data);
	    			  cntResults++;
	    		   }
	    	   }
	       }
	       assoc.release(true);
	       log.debug("Found "+cntResults+" in "+(System.nanoTime()-start)/1e6+" ms");
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	/**
	 * Perform the query by using the search criteria named filter to get the criteria to search on,
	 * and using the default configuration for where to search.
	 * @param filterItem
	 * @param params
	 * @return
	 */
	public ResultFromDicom filter(FilterItem<ResultFromDicom> filterItem, Map<String, Object> params) {
		log.debug("DICOM CFind filter starting to search.");
		ResultFromDicom resultFromDicom = (ResultFromDicom) params.get(EXTEND_RESULTS_KEY);
		if( resultFromDicom==null ) resultFromDicom = new ResultsBean();
		SearchCriteria searchCondition = (SearchCriteria) 
			filterItem.callNamedFilter("searchCondition", params);
		if( searchCondition==null ) {
			log.warn("No search conditions found for parameters "+params);
			return null;
		}
		find(searchCondition, resultFromDicom);
		log.debug("Found result(s) - returning from filter.");
		return resultFromDicom;
	}
}
