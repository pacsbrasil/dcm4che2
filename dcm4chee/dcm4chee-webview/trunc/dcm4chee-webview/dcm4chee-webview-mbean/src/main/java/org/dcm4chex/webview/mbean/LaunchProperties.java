/*
 * Created on Oct 3, 2006
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.dcm4chex.webview.mbean;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4chex.webview.CustomLaunchProperties;

/**
 * @author franz.willer@agfa.com
 * @version $Revision:$ $Date:$
 * @since 04.10.2006
 */
public class LaunchProperties {

    
    private String appletClass;
    private String appletArchive;
    private Map result2appletParameterMap = new HashMap();
    private Map appletParameterMap = new HashMap();
    private Map imageCUIDs = null;
    private Map psCUIDs = null;//Presentation state SOPClass UIDs
    private String[] paraSeriesInstances = new String[]{null,"","",";",";",""};
    private String[] paraPresentationStates = new String[]{null,"","","",";",""};
    private CustomLaunchProperties customProps = null;

    public LaunchProperties() {
    }
    
    /**
     * @return Returns the appletArchive.
     */
    public String getAppletArchive() {
        return appletArchive;
    }
    /**
     * @param appletArchive The appletArchive to set.
     */
    public void setAppletArchive(String appletArchive) {
        this.appletArchive = appletArchive;
    }
    /**
     * @return Returns the appletClass.
     */
    public String getAppletClass() {
        return appletClass;
    }
    /**
     * @param appletClass The appletClass to set.
     */
    public void setAppletClass(String appletClass) {
        this.appletClass = appletClass;
    }
    /**
     * @return Returns the appletParameterMap.
     */
    public Map getAppletParameterMap() {
        return appletParameterMap;
    }
    /**
     * @param appletParameterMap The appletParameterMap to set.
     */
    public void setAppletParameterMap(Map appletParameterMap) {
        this.appletParameterMap = appletParameterMap;
    }
    /**
     * @return Returns the customProps.
     */
    public CustomLaunchProperties getCustomProps() {
        return customProps;
    }
    /**
     * @param customProps The customProps to set.
     */
    public void setCustomProps(CustomLaunchProperties customProps) {
        this.customProps = customProps;
    }
    /**
     * @return Returns the imageCUIDs.
     */
    public Map getImageCUIDs() {
        return imageCUIDs;
    }
    /**
     * @param imageCUIDs The imageCUIDs to set.
     */
    public void setPresentationStateCUIDs(Map cuids) {
        this.psCUIDs = cuids;
    }
    
    /**
     * @return Returns the imageCUIDs.
     */
    public Map getPresentationStateCUIDs() {
        return psCUIDs;
    }
    /**
     * @param imageCUIDs The imageCUIDs to set.
     */
    public void setImageCUIDs(Map imageCUIDs) {
        this.imageCUIDs = imageCUIDs;
    }
     
    /**
     * @return Returns the paraPresentationStates.
     */
    public String getParaPresentationStates() {
        return toListConfigString( paraPresentationStates );
    }
    /**
     * @param paraPresentationStates The paraPresentationStates to set.
     */
    public void setParaPresentationStates(String cfg) {
        setListConfigString( paraPresentationStates, cfg );
    }
    /**
     * @return Returns the paraSeriesInstances.
     */
    public String getParaSeriesInstances() {
        return toListConfigString( paraSeriesInstances );
    }
    /**
     * @param paraSeriesInstances The paraSeriesInstances to set.
     */
    public void setParaSeriesInstances(String cfg) {
        setListConfigString( paraSeriesInstances, cfg );
    }
    
    private String toListConfigString(String[] sa) {
        if ( sa == null || sa[0] == null) return "NONE";
        return sa[0]+"|"+sa[1]+"|"+sa[2]+"|"+sa[3]+"|"+sa[4]+"|"+sa[5];
    }
    
    private void setListConfigString(String[] sa, String cfg) {
        if ( cfg == null || cfg.equalsIgnoreCase("NONE")) {
            sa[0] = null;
        } else {
            int pos=0, pos1=0, i=0;
    	    while ( (pos1 = cfg.indexOf('|',pos)) > 0 ) {
    	        sa[i++]=cfg.substring(pos,pos1);
    	        pos=++pos1;
    	    }
            sa[i]=cfg.substring(pos);
        }
    }
    /**
     * @return Returns the result2appletParameterMap.
     */
    public Map getResult2appletParameterMap() {
        return result2appletParameterMap;
    }
    /**
     * @param result2appletParameterMap The result2appletParameterMap to set.
     */
    public void setResult2appletParameterMap(Map result2appletParameterMap) {
        this.result2appletParameterMap = result2appletParameterMap;
    }
    /**
     * @return Returns the customProps.
     */
    public CustomLaunchProperties getCustomLaunchPropertiesClass() {
        return customProps;
    }
    /**
     * @param customProps The customProps to set.
     * @throws ClassNotFoundException
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    public void setCustomLaunchPropertiesClass(CustomLaunchProperties customProps) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        this.customProps = customProps;
    }

    /**
     * Return either APPLET or Selection parameter.
     * <p />
     * This method returns a list of presentation state selection when:<br/>
     * The query result (in map) contains more than 1 presentation states and selectPR is enabled.
     * <p/>
     * The 'meaning' of the returned properties is appointed by the property 'launchMode'!
     * <dl>
     * <dt>Property 'launchMode':</dt>
     * <dd>  applet: Properties contains applet parameter. </dd>
     * <dd>  select: Properties contains presentation state list (key=IUID/value=Description).
     * </dl>
     * @param map       Contains query Results (each map value is a list of DicomObject.
     * @param ignorePR  If true: Ignore DicomObjects with SOPClassUID listed in psCUIDs.
     * @param selectPR  Enable/disable PresentationState Selection when result contains >1 PRs.
     * 
     * @return either APPLET or Selection parameter.
     */
    public Properties getProperties( Map map, boolean ignorePR, boolean selectPR ) {
        Properties p = new Properties();
        p.setProperty("launchMode", "applet");
        Properties prProps = new Properties();
        
        p.setProperty("CODE", appletClass);
        p.setProperty("ARCHIVE", appletArchive);
        p.putAll(appletParameterMap);
        if ( map.isEmpty() ) {
            return p;
        }
        addParameterFromResult(p, (DicomObject)((List) map.values().iterator().next()).get(0));
        
        StringBuffer sbSeries = new StringBuffer();
        StringBuffer sbPR = null;
        if ( !ignorePR && paraPresentationStates[0] != null ) {
            sbPR = new StringBuffer(paraPresentationStates[1]); //prefix
            appendDescription( sbPR, paraPresentationStates, null, 0);
        }
        int i = 1;
        List seriess;
        DicomObject dcm;
        String cuid;
        boolean imgSeries;
        boolean noSelect = true; //set to false if PR selection is necessary (selectPR=true and more than one PR)
        for (  Iterator iter = map.values().iterator(); iter.hasNext() ; ){
            seriess = (List) iter.next();
            sbSeries.setLength(0);
            if (paraSeriesInstances[0] != null) {
                appendDescription( sbSeries, paraSeriesInstances, (DicomObject) seriess.get(0), i);
            }
            imgSeries = false;
            for ( int j = 0 ; j < seriess.size() ; j++) {
                dcm = (DicomObject) seriess.get(j);
                cuid = dcm.getString(Tag.SOPClassUID);
                if ( noSelect && (imageCUIDs == null || imageCUIDs.values().contains(cuid)) ) {
                    sbSeries.append(dcm.getString(Tag.SOPInstanceUID)).append(paraSeriesInstances[4]);//list seperator
                    imgSeries = true;
                } else if ( !ignorePR && psCUIDs.values().contains(cuid)) {
                    if ( selectPR ) {
                        String descr = dcm.getString(Tag.ContentDescription)+"("+
                        dcm.getDate(Tag.PresentationCreationDate,Tag.PresentationCreationTime)+") - "+
                        dcm.getString(Tag.ContentLabel)+" - "+dcm.getString(Tag.ContentCreatorName);
                        prProps.setProperty( dcm.getString(Tag.SOPInstanceUID), descr );
                        noSelect = prProps.size() < 2;
                    }
                    if ( noSelect ) {
                        sbPR.append(dcm.getString(Tag.SOPInstanceUID)).append(paraPresentationStates[4]);//list seperator
                    }
                }
            }
            if ( imgSeries ) {
                sbSeries.append(paraSeriesInstances[5]);//postfix
                p.setProperty(paraSeriesInstances[0]+i++, sbSeries.toString());
            }
        }
        if ( noSelect ) {
            if ( sbPR != null ) { 
                sbPR.append(paraPresentationStates[5]);
                p.setProperty(paraPresentationStates[0], sbPR.toString());
            }
            if ( customProps != null ) {
                customProps.addCustomProperties(p,map);
            }
            return p;
        } else {
            prProps.setProperty("launchMode", "select");
            return prProps;
        }
    }
    
    private void appendDescription(StringBuffer sb, String[] sa, DicomObject dcm, int idx) {
        sb.append(sa[1]);//descr. part
        String s = sa[2];//descr. value
        if ( s.length() > 0 ) {
            if ( s.charAt(0) == '#') {
                if ( s.length() > 1 ) {
                    char ch = s.charAt(1);
                    if ( ch != '-' && ( ch < '0' || ch > '9') ) {
                        sb.append((char)(ch+idx-1));
                    } else {
                        sb.append(idx+Integer.parseInt(s.substring(1)));
                    }
                } else {
                    sb.append(idx);
                }
            } else {
                if ( dcm != null )
                    sb.append( dcm.getString(Tag.toTagPath(s))); //value from DICOM attribute
            }
        }
        sb.append( sa[3] );//delimiter description part - uid list
    }

    /**
     * @param object
     */
    private void addParameterFromResult(Properties p, DicomObject dcm) {
        Map.Entry entry;
        String value;
        for ( Iterator iter = result2appletParameterMap.entrySet().iterator() ; iter.hasNext() ; ) {
            entry = (Map.Entry) iter.next();
            value = dcm.getString(Tag.toTag((String)entry.getValue()));
            p.setProperty((String) entry.getKey(), value == null ?  "":value);
        }
    }
    
    
    

}
