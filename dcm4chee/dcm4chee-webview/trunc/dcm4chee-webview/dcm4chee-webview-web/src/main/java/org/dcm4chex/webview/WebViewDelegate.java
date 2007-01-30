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

package org.dcm4chex.webview;

import java.util.Map;
import java.util.Properties;

import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.dcm4che2.data.DicomObject;
import org.jboss.mx.util.MBeanServerLocator;


/**
 * @author franz.willer@agfa.com
 * @version $Revision:$ $Date:$
 * @since 04.10.2006
 *
 */
public class WebViewDelegate {

    private static MBeanServer server;

    private static ObjectName webviewName;
    
    private Boolean ignorePR, selectPR;
    
    private void init() throws MalformedObjectNameException, NullPointerException {
        WebViewDelegate.server = MBeanServerLocator.locate();
        WebViewDelegate.webviewName = new ObjectName("dcm4chee.archive:service=WebViewService");
    }

    /**
     * @return Returns the ignorePR.
     */
    public Boolean getIgnorePR() {
        return ignorePR;
    }
    /**
     * @param ignorePR The ignorePR to set.
     */
    public void setIgnorePR(Boolean ignorePR) {
        this.ignorePR = ignorePR;
    }
    /**
     * @return Returns the selectPR.
     */
    public Boolean getSelectPR() {
        return selectPR;
    }
    /**
     * @param selectPR The selectPR to set.
     */
    public void setSelectPR(Boolean selectPR) {
        this.selectPR = selectPR;
    }
    public Properties getLaunchProperties(Map paraMap) {
        String method;
        String[] value;
        if ( (value = (String[]) paraMap.get("accNr")) != null ) {
            method = "getLaunchPropertiesForAccNr";
        } else if ( (value = (String[]) paraMap.get("studyUID")) != null ) {
            method = "getLaunchPropertiesForStudyUID";
        } else if ( (value = (String[]) paraMap.get("seriesUID")) != null ) {
            method = "getLaunchPropertiesForSeriesUID";
        } else if ( (value = (String[]) paraMap.get("prUID")) != null ) {
            return getLaunchPropertiesForPresentationState(null, null, value[0]);
        } else {
            return new Properties();
        }
        return getLaunchProperties(method, value[0], ignorePR, selectPR);
    }
    
    public Properties getLaunchPropertiesForAccNr(String accNr) {
        return getLaunchProperties("getLaunchPropertiesForAccNr", accNr, ignorePR, selectPR);
    }
    public Properties getLaunchPropertiesForStudyUID(String studyUID) {
        return getLaunchProperties("getLaunchPropertiesForStudyUID",studyUID, ignorePR, selectPR);
    }
    public Properties getLaunchPropertiesForSeriesUID(String seriesUID) {
        return getLaunchProperties("getLaunchPropertiesForSeriesUID",seriesUID, null, null);
    }
    public Properties getLaunchPropertiesForQuery(DicomObject keys) {
        return getLaunchProperties("getLaunchPropertiesForQuery",keys, ignorePR, selectPR);
    }
    public Properties getLaunchPropertiesForPresentationState( String studyUID, String seriesUID, String instanceUID) {
        try {
            if ( server == null ) init();
            return (Properties) server.invoke(webviewName,
                    "getLaunchPropertiesForPresentationState",
                    new Object[] { studyUID, seriesUID, instanceUID },
                    new String[] { String.class.getName(),String.class.getName(),String.class.getName() });
        } catch (Exception e) {
            System.out.println("Failed to get LaunchProperties for PresentationState! Exception:"+e);
            e.printStackTrace();
            return null;
        }
    }
     
    public Properties getLaunchProperties( String method, Object key, Boolean ignorePR, Boolean selectPR ) {
        try {
            if ( server == null ) init();
            return (Properties) server.invoke(webviewName,
                    method,
                    new Object[] { key, ignorePR, selectPR },
                    new String[] { key.getClass().getName(), Boolean.class.getName(), Boolean.class.getName() });
        } catch (Exception e) {
            System.out.println("Failed to get LaunchProperties with method:"+ method+"! Exception:"+e);
            return null;
        }
    }

   
}