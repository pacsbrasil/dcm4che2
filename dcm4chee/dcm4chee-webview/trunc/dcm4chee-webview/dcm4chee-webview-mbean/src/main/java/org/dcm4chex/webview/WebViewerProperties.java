/*
 * Created on Oct 6, 2006
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.dcm4chex.webview;

import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;

/**
 * CustomLaunchProperties Implementation.
 * 
 * @author franz.willer@agfa.com
 * @version $Revision:$ $Date:$
 * @since 04.10.2006
 */
public class WebViewerProperties implements CustomLaunchProperties {

    /* (non-Javadoc)
     * @see org.dcm4chex.webview.CustomLaunchProperties#addCustomProperties(java.util.Properties, java.util.Map)
     */
    public void addCustomProperties(Properties p, Map queryResult) {
        DicomObject dcm = (DicomObject)((List) queryResult.values().iterator().next()).get(0);
        String patientName = dcm.getString(Tag.PatientName);
        if ( patientName == null ) patientName ="";
     	String firstName = "";
     	String lastName = patientName;
     	int pos = patientName.indexOf('^');
     	if ( pos != -1 ) {
     		firstName=patientName.substring(pos+1);
     		lastName=patientName.substring(0,pos);
     	}
     	String sex = dcm.getString(Tag.PatientSex);
     	if ( sex == null ) sex ="";
        String birthDate = dcm.getString(Tag.PatientBirthDate);
     	if ( birthDate == null ) birthDate ="";
        String title = lastName+"("+sex+")";
        
        p.setProperty("title", title);
        p.setProperty("DB_FIRST_NAME", firstName);
        p.setProperty("DB_LAST_NAME", lastName);
        p.setProperty("DB_SEX", sex);
        p.setProperty("DB_BIRTH_DATE", birthDate == null ? "" : birthDate);
    }

    /* (non-Javadoc)
     * @see org.dcm4chex.webview.CustomLaunchProperties#getResultAttributes()
     */
    public int[][] getResultAttributes() {
        int[][] attrs = new int[3][];
        attrs[0] = new int[]{ Tag.PatientName }; 
        attrs[1] = new int[]{ Tag.PatientSex }; 
        attrs[2] = new int[]{ Tag.PatientBirthDate }; 
        return attrs;
    }

}
