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
 * Java(TM), available at http://sourceforge.net/projects/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * TIANI Medgraph AG.
 * Portions created by the Initial Developer are Copyright (C) 2003-2005
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Gunter Zeilinger <gunter.zeilinger@tiani.com>
 * Franz Willer <franz.willer@gwi-ag.com>
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

package org.dcm4chex.webview.mbean;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

import org.dcm4che2.data.BasicDicomObject;
import org.dcm4che2.data.DicomElement;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.UID;
import org.dcm4che2.data.VR;
import org.dcm4che2.util.UIDUtils;
import org.dcm4chex.webview.CustomLaunchProperties;
import org.dcm4chex.webview.UIDQuery;
import org.jboss.system.ServiceMBeanSupport;



/**
 * The MBean to manage the WebView launcher service.
 * <p>
 * 
 * @author franz.willer@agfa.com
 * @version $Revision:$ $Date:$
 * @since 04.10.2006
 */
public class WebViewService extends ServiceMBeanSupport {

    //DICOM Query
    private String calledAET;
    private String callingAET;
    private String host;
    private int port;
    
    private LaunchProperties launchProperties = new LaunchProperties();
    private boolean ignorePRinStudy = false;
    private boolean selectMultiPR = false;
    
    //static Logger log = LoggerFactory.getLogger(WebViewService.class);
    
    /**
     * @return Returns the calledAET.
     */
    public String getCalledAET() {
        return calledAET;
    }
    /**
     * @param calledAET The calledAET to set.
     */
    public void setCalledAET(String calledAET) {
        this.calledAET = calledAET;
    }
    /**
     * @return Returns the callingAET.
     */
    public String getCallingAET() {
        return callingAET;
    }
    /**
     * @param callingAET The callingAET to set.
     */
    public void setCallingAET(String callingAET) {
        this.callingAET = callingAET;
    }
    /**
     * @return Returns the host.
     */
    public String getHost() {
        return host;
    }
    /**
     * @param host The host to set.
     */
    public void setHost(String host) {
        this.host = host;
    }
    /**
     * @return Returns the port.
     */
    public int getPort() {
        return port;
    }
    /**
     * @param port The port to set.
     */
    public void setPort(int port) {
        this.port = port;
    }
    
    public String getImageSOPClasses() {
        return toUIDListString(launchProperties.getImageCUIDs());
    }

    public void setImageSOPClasses(String s) {
        launchProperties.setImageCUIDs(parseUIDs(s));
    }
    
    public String getPresentationStateCUIDs() {
        return toUIDListString(launchProperties.getPresentationStateCUIDs());
    }

    public void setPresentationStateCUIDs(String s) {
        launchProperties.setPresentationStateCUIDs(parseUIDs(s));
    }
    
    public String getParaSeriesInstances() {
        return launchProperties.getParaSeriesInstances();
    }
 
    public void setParaSeriesInstances(String s) {
        launchProperties.setParaSeriesInstances(s);
    }

    public String getParaPresentationStates() {
        return launchProperties.getParaPresentationStates();
    }
 
    public void setParaPresentationStates(String s) {
        launchProperties.setParaPresentationStates(s);
    }
    
    /**
     * @return Returns the appletArchive.
     */
    public String getAppletArchive() {
        return launchProperties.getAppletArchive();
    }
    /**
     * @param appletArchive The appletArchive to set.
     */
    public void setAppletArchive(String appletArchive) {
        launchProperties.setAppletArchive( appletArchive );
    }
    /**
     * @return Returns the appletClass.
     */
    public String getAppletClass() {
        return launchProperties.getAppletClass();
    }
    /**
     * @param appletClass The appletClass to set.
     */
    public void setAppletClass(String appletClass) {
        launchProperties.setAppletClass( appletClass );
    }
    /**
     * @return Returns the appletParameterMap.
     */
    public String getAppletParameterMap() {
        return toParameterListString(launchProperties.getAppletParameterMap());
    }
    /**
     * @param appletParameterMap The appletParameterMap to set.
     */
    public void setAppletParameterMap(String appletParameterMap) {
        launchProperties.setAppletParameterMap( parseParameterList(appletParameterMap));
    }
    /**
     * @return Returns the customLaunchPropertiesClass.
     */
    public String getCustomLaunchPropertiesClass() {
        CustomLaunchProperties customProps = launchProperties.getCustomLaunchPropertiesClass();
        return customProps == null ? "NONE" : customProps.getClass().getName();
    }
    /**
     * @param customLaunchPropertiesClass The customLaunchPropertiesClass to set.
     * @throws ClassNotFoundException
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    public void setCustomLaunchPropertiesClass(String customPropsName) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        CustomLaunchProperties customProps = null;
        if ( customPropsName != null && !customPropsName.equalsIgnoreCase("NONE")) {
            customProps = (CustomLaunchProperties) Class.forName(customPropsName).newInstance();
        }
        launchProperties.setCustomLaunchPropertiesClass(customProps);
    }
    /**
     * @return Returns the result2appletParameterMap.
     */
    public String getResult2ParameterMap() {
        return toParameterListString(launchProperties.getResult2appletParameterMap());
    }
    /**
     * @param result2ParameterMap The result2appletParameterMap to set.
     */
    public void setResult2ParameterMap(String result2ParameterMap) {
        launchProperties.setResult2appletParameterMap( parseParameterList(result2ParameterMap));
    }
    public List query(DicomObject searchDS) {
        return null;
    }
    
    public Properties getLaunchPropertiesForAccNr(String accNr, Boolean ignorePR, Boolean selectPR) {
        DicomObject keys = new BasicDicomObject();
        keys.putString(Tag.AccessionNumber, VR.SH, accNr);
        return getLaunchPropertiesForQuery(keys, ignorePR, selectPR );
    }
    public Properties getLaunchPropertiesForStudyUID(String studyUID, Boolean ignorePR, Boolean selectPR) {
        DicomObject keys = new BasicDicomObject();
        keys.putString(Tag.StudyInstanceUID, VR.UI, studyUID);
        return getLaunchPropertiesForQuery(keys, ignorePR, selectPR );
    }
    public Properties getLaunchPropertiesForSeriesUID(String seriesUID, Boolean ignorePR, Boolean selectPR) {
        DicomObject keys = new BasicDicomObject();
        keys.putString(Tag.SeriesInstanceUID, VR.UI, seriesUID);
        return getLaunchPropertiesForQuery(keys, ignorePR, selectPR);
    }
    public Properties getLaunchPropertiesForPresentationState(String studyUID, String seriesUID, String instanceUID) {
        DicomObject keys = new BasicDicomObject();
        keys.putString(Tag.StudyInstanceUID, VR.UI, studyUID);
        keys.putString(Tag.SeriesInstanceUID, VR.UI, seriesUID);
        keys.putString(Tag.SOPInstanceUID, VR.UI, instanceUID);
        keys.putNull(new int[]{ Tag.ReferencedSeriesSequence, 0, Tag.SeriesInstanceUID },VR.UI);
        addResultAttributes(keys);
        List results = new UIDQuery(callingAET, calledAET, host, port).query(keys);
        if ( results.isEmpty() ) {
            //log.info("Can't find PresentationState Object:"+instanceUID);
        }
        DicomObject prObj = (DicomObject) results.get(0);
        Map map = new LinkedHashMap();//to ensure that the first iterator element is the query result (with all attributes)
        List l = new ArrayList();
        l.add(prObj);
        map.put( prObj.getString(Tag.SeriesInstanceUID), l);
        DicomElement serSeq = prObj.get(Tag.ReferencedSeriesSequence);
        if (serSeq != null) {
            DicomObject obj, imgItem, obj1;
            DicomElement imgSeq;
            for ( int i = 0,len=serSeq.countItems(); i < len ; i++ ) {
                l = new ArrayList();
                obj = serSeq.getDicomObject(i);
                map.put( obj.getString(Tag.SeriesInstanceUID), l );
                imgSeq = obj.get(Tag.ReferencedImageSequence);
                for ( int j=0,jLen=imgSeq.countItems() ; j < jLen ; j++) {
                    imgItem =  imgSeq.getDicomObject(j);
                    obj1 = new BasicDicomObject();
                    obj1.putString(Tag.SOPInstanceUID, VR.UI, imgItem.getString(Tag.ReferencedSOPInstanceUID) );
                    obj1.putString(Tag.SOPClassUID, VR.UI, imgItem.getString(Tag.ReferencedSOPClassUID) );
                    l.add(obj1);
                }
            }
            return launchProperties.getProperties(map, false, false);
        }
        return new Properties();
    }
    public Properties getLaunchPropertiesForQuery(DicomObject keys, Boolean ignorePR, Boolean selectPR) {
        addResultAttributes(keys);
        boolean ignorePRflag = ignorePR == null ? ignorePRinStudy:ignorePR.booleanValue();
        boolean selectPRflag = selectPR == null ? selectMultiPR:selectPR.booleanValue();
        if ( ! ignorePRflag && selectPRflag ) {
            keys.putNull(Tag.PresentationCreationDate, VR.DA);
            keys.putNull(Tag.PresentationCreationTime, VR.TM);
            keys.putNull(Tag.ContentDescription, VR.LO);
            keys.putNull(Tag.ContentLabel, VR.CS);
            keys.putNull(Tag.ContentCreatorName, VR.PN);
        }
        Map results = new UIDQuery(callingAET, calledAET, host, port).queryAndSort(keys);
        return launchProperties.getProperties(results, ignorePRflag, selectPRflag);
    }
    
    public boolean isIgnorePresentationStateForStudies() {
        return ignorePRinStudy;
    }
    public void setIgnorePresentationStateForStudies(boolean ignorePresentationStateForStudies) {
        ignorePRinStudy = ignorePresentationStateForStudies;
    }
    
    
    /**
     * @return Returns the selectMultiPR.
     */
    public boolean isSelectMultiPR() {
        return selectMultiPR;
    }
    /**
     * @param selectMultiPR The selectMultiPR to set.
     */
    public void setSelectMultiPR(boolean selectMultiPR) {
        this.selectMultiPR = selectMultiPR;
    }
    /**
     * @param keys
     */
    private void addResultAttributes(DicomObject keys) {
        Map map = launchProperties.getResult2appletParameterMap();
        if ( map != null && ! map.isEmpty()) {
            int tag;
            for ( Iterator iter = map.values().iterator() ; iter.hasNext() ; ) {
                tag = Tag.toTag((String) iter.next());
                if ( !keys.contains(tag)) {
                    keys.putNull(tag,null);
                }
            }
        }
        if ( launchProperties.getCustomLaunchPropertiesClass() != null ) {
            int[][] resultAttrs = launchProperties.getCustomLaunchPropertiesClass().getResultAttributes();
            if ( resultAttrs != null ) {
                for ( int i = 0 ; i < resultAttrs.length ; i++ ) {
                    if ( !keys.contains(resultAttrs[i][0])) {
                        keys.putNull(resultAttrs[i],null);
                    }
                    
                }
            }
        }
        
    }
    protected void startService() throws Exception {
    }

    /**
     * @param appletParameterMap
     */
    private String toParameterListString(Map paraMap) {
        if ( paraMap == null || paraMap.isEmpty() ) return "NONE";
        String nl = System.getProperty("line.separator", "\n");
        StringBuffer sb = new StringBuffer();
        Iterator iter = paraMap.entrySet().iterator();
        Map.Entry entry;
        while ( iter.hasNext() ) {
            entry = (Map.Entry) iter.next();
            sb.append(entry.getKey()).append('=').append(entry.getValue()).append(nl);
        }
        return sb.toString();
    }
    private static Map parseParameterList(String params) {
        Map map = new LinkedHashMap();
        if ( params == null || params.length() < 1 || params.equalsIgnoreCase("NONE")) {
            return map;
        }
        StringTokenizer st = new StringTokenizer(params, "\t\r\n;");
        String tk;
        int pos;
        while ( st.hasMoreTokens() ) {
            tk = st.nextToken().trim();
            pos = tk.indexOf('=');
            if ( pos != -1 ) {
                map.put(tk.substring(0,pos), tk.substring(pos+1));
            } else {
                map.put(tk,"");
            }
        }
        return map;
    }
    
    private String toUIDListString(Map uids) {
        if ( uids == null || uids.isEmpty() ) return "";
        String nl = System.getProperty("line.separator", "\n");
        StringBuffer sb = new StringBuffer();
        Iterator iter = uids.keySet().iterator();
        while ( iter.hasNext() ) {
            sb.append(iter.next()).append(nl);
        }
        return sb.toString();
    }
    
    private static Map parseUIDs(String uids) {
        StringTokenizer st = new StringTokenizer(uids, " \t\r\n;");
        String uid,name;
        Map map = new LinkedHashMap();
        while ( st.hasMoreTokens() ) {
            uid = st.nextToken().trim();
            name = uid;
            
            if (isDigit(uid.charAt(0))) {
                if ( ! UIDUtils.isValidUID(uid) ) 
                    throw new IllegalArgumentException("UID "+uid+" isn't a valid UID!");
            } else {
                uid = UID.forName( name );
            }
            map.put(name,uid);
        }
        return map;
    }
    
    private static boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }
    
}
