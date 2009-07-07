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

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.dcm4che2.data.BasicDicomObject;
import org.dcm4che2.data.DicomElement;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.VR;
import org.dcm4che2.net.Association;
import org.dcm4che2.net.CommandUtils;
import org.dcm4che2.net.DimseRSP;
import org.dcm4che2.net.NetworkApplicationEntity;
import org.dcm4che2.net.NetworkConnection;
import org.dcm4che2.net.TransferCapability;
import org.dcm4chee.xero.dicom.ApplicationEntityProvider;
import org.dcm4chee.xero.dicom.DicomConnector;
import org.dcm4chee.xero.dicom.DicomDateTimeHandler;
import org.dcm4chee.xero.dicom.TransferCapabilitySelector;
import org.dcm4chee.xero.metadata.filter.Filter;
import org.dcm4chee.xero.metadata.filter.FilterItem;
import org.dcm4chee.xero.metadata.filter.FilterUtil;
import org.dcm4chee.xero.metadata.servlet.ResponseException;
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
/**
 * @author smohan
 *
 */
public abstract class DicomCFindFilter implements Filter<ResultFromDicom>
{
   private static Logger log = LoggerFactory.getLogger(DicomCFindFilter.class);
   
   private static final int DEFAULT_MAX_RESULTS = 100000;
   public static final String EXTEND_RESULTS_KEY = "EXTEND_RESULTS";
   
   private static DicomConnector dicomConnector = new DicomConnector();

   private int priority = 0;
   private int cancelAfter = Integer.MAX_VALUE;

   private DicomDateTimeHandler dateTime = new DicomDateTimeHandler();
   private TransferCapabilitySelector tcs = new TransferCapabilitySelector();
   private ApplicationEntityProvider aeProvider = new ApplicationEntityProvider();

   public DicomCFindFilter() {
	}
    
    /**
     * Return the CUID values to try to negotiate.  This must be over-ridden by the inheriting class. 
     */
    protected abstract String[] getCuids();


    /** This method must return the query level, eg STUDY, PATIENT etc */
    protected abstract String getQueryLevel();
    
    /** Return the set of key to negotiate for */
    protected abstract Set<Integer> getReturnKeys();
    
    /**
     * Handles various types of date values such as Today, Yesterday etc
     * @param value
     * @return
     */
    // TODO: Move to DicomDatTimeHandler
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
       ret = (start!=null ? dateTime.toDicomDate(start) : "")+"-"+(end!=null ? dateTime.toDicomDate(end) : "");
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
    	log.debug("Query level is {}",getQueryLevel());
    	Set<Integer> returnKeys = getReturnKeys();
    	Iterator<Integer> it = returnKeys.iterator();
    	while (it.hasNext())
    	{
    	    int tag = it.next().intValue();
    	    ret.putNull(tag,null);
    	}
    	Map<String,TableColumn> searchCondition = searchCriteria.getAttributeByName();
    	for(String key : searchCondition.keySet()) {
    		String keyTag;
    		if( key.equals("seriesUID") ) keyTag = "SeriesInstanceUID";
    		else if( key.equals("studyUID") ) keyTag = "StudyInstanceUID";
    		else if( key.equals("objectUID") ) keyTag = "SOPInstanceUID";
    		else keyTag = Character.toUpperCase(key.charAt(0))+key.substring(1);
    		TableColumn tc = searchCondition.get(key);
    		List<?> content = tc.getContent();
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
    			if( keyTag.startsWith("PPS") ) keyTag = "PerformedProcedureStep"+keyTag.substring(3);
    		} 
    		else {
    			if( key.endsWith("UID") || key.equals("ModalitiesInStudy") || key.equals("PatientID") ) {
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
    
    /** Adds this object to the result - allows overriding to test for specific conditions in the data */
    protected boolean addResult(ResultFromDicom resultFromDicom, DicomObject data) {
       resultFromDicom.addResult(data);
       return true;
    }
    
    /**
     * This method performs a remote query against the local DCM4CHEE ae instance, on port 11112, and
     * returns the result as a set of objects of type E.
     */
	public boolean cfind(SearchCriteria searchCriteria, ResultFromDicom resultFromDicom, NetworkApplicationEntity remoteAE, NetworkApplicationEntity localAE, int maxResults) {
	   if (log.isInfoEnabled()) {
         NetworkConnection conn = remoteAE.getNetworkConnection()[0];
         log.info("Connecting to {}@{} from AE="+localAE.getAETitle(),remoteAE.getAETitle(),conn.getHostname());
      }


      long start = System.nanoTime();
      Association association = null;
      try {   
         // Use the remote AE to start the connection so it dictates the security context
          association = dicomConnector.connect(localAE, remoteAE);
	       TransferCapability tc = tcs.selectTransferCapability(association, getCuids());
	       if ( tc==null )
	    	   throw new RuntimeException("Can't agree on any transfer capabilities with device.  Attempted: "+ Arrays.asList(getCuids()));
	       String cuid = tc.getSopClass();
	       log.debug("Selected sop q/r class {}",cuid);
	       String tsuid = tcs.selectBestTransferSyntaxUID(tc);
	       DicomObject keys = generateKeys(searchCriteria);
	       DimseRSP rsp = association.cfind(cuid, priority, keys, tsuid, cancelAfter);
	       int cntResults = 0;
	       while(rsp.next()) {
	    	   DicomObject cmd = rsp.getCommand();
	    	   if (CommandUtils.isPending(cmd)) {	    		  
	    		   DicomObject data = rsp.getDataset();
	    		   if( data.contains(Tag.DirectoryRecordSequence) ) {
	    			  log.debug("Using blocked structure.");
	    			  DicomElement dir = data.get(Tag.DirectoryRecordSequence);
	    			  for(int i=0, n=dir.countItems(); i<n; i++) {
	    				 data = dir.getDicomObject(i);
	    				 if( !addResult(resultFromDicom,data) ) return false;
	    				 cntResults++;
	    				 maxResults--;
	    			  }
	    		   } 
	    		   else {
	    			  if( !addResult(resultFromDicom,data) ) return false;
	    			  cntResults++;
	    			  maxResults--;
	    		   }
	    		   if( maxResults<0 ) {
	    		        if( resultFromDicom instanceof HasMaxResults ) {
	    		           ((HasMaxResults) resultFromDicom).tooManyResults();
	    		        }
	    		      break;
	    		   }
	    	   }
	       }
	       association.release(true);
	       log.debug("Found "+cntResults+" in "+(System.nanoTime()-start)/1e6+" ms");
		} catch (RuntimeException e) {
			throw e;
		} catch(SocketTimeoutException e) {
		    throw new ResponseException(502,"Unable to contact SCU/SCP");
        } catch(ConnectException e) {
            throw new ResponseException(502,"Unable to contact SCU/SCP");
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		finally {
		   dicomConnector.release(association, true);
		}
		return true;
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

		int maxResults = FilterUtil.getInt(params,"maxResults",DEFAULT_MAX_RESULTS);
		SearchCriteria searchCriteria= searchParser.filter(null,params);
		if( searchCriteria==null ) {
			log.warn("No search conditions found for parameters "+params);
			return null;
		}
		
      ResultFromDicom resultFromDicom = getResultFromDicom(params);
      String remoteAETitle = FilterUtil.getString(params, "ae", "local");
	   try
	   {
         NetworkApplicationEntity remoteAE = aeProvider.getAE(remoteAETitle);
         NetworkApplicationEntity localAE = aeProvider.getLocalAE(remoteAETitle,getCuids());

   		if( !cfind(searchCriteria, resultFromDicom, remoteAE, localAE, maxResults) ) return null;
   		log.debug("Found result(s) - returning from filter.");
	   }
	   catch(IOException e)
	   {
	      throw new RuntimeException("C-FIND failed for ae="+remoteAETitle,e);
	   }
	   
		return resultFromDicom;
	}
	
	/** Handles creating the results bean, given the ae information */
	public static ResultFromDicom getResultFromDicom(Map<String,Object> params) {
        ResultFromDicom resultFromDicom = (ResultFromDicom) params.get(EXTEND_RESULTS_KEY);
        if( resultFromDicom==null ) resultFromDicom = new ResultsBean();
        Map<String,Object> ae = AEProperties.getAE(params);
        String name = (String) ae.get(AEProperties.AE_PROPERTY_NAME);
        String defaultIssuer = (String) ae.get(AEProperties.DEFAULT_ISSUER);
        
        if( resultFromDicom instanceof RecordsAE ) {
            ((RecordsAE) resultFromDicom).setAe(name);
         }
         if( defaultIssuer!=null && (resultFromDicom instanceof ResultsBean) ) {
             ((ResultsBean) resultFromDicom).setDefaultIssuer(defaultIssuer);
         }
         return resultFromDicom;
	}

	private Filter<SearchCriteria> searchParser;
	
	public Filter<SearchCriteria> getSearchParser() {
   	return searchParser;
   }

	/**
	 * Set the filter that determines the search criteria to use for this query.
	 * Defaults to the study search condition - if you want series or image level queries, you had
	 * better modify this to use those explicitly, otherwise you will have to include a study level 
	 * criteria.
	 * 
	 * @param searchCondition
	 */
	public void setSearchParser(Filter<SearchCriteria> searchParser) {
   	this.searchParser = searchParser;
   }

}
