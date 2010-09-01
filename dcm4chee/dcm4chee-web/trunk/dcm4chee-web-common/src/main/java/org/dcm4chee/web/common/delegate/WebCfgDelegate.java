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
 * Agfa-Gevaert AG.
 * Portions created by the Initial Developer are Copyright (C) 2008
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * See listed authors below.
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

package org.dcm4chee.web.common.delegate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.management.MBeanServerConnection;
import javax.management.MBeanServerFactory;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.apache.wicket.Application;
import org.apache.wicket.protocol.http.WebApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Franz Willer <franz.willer@gmail.com>
 * @version $Revision$ $Date$
 * @since Aug 5, 2010
 */
public class WebCfgDelegate {

    private static WebCfgDelegate singleton;
    protected ObjectName serviceObjectName;
    protected MBeanServerConnection server;
      
    protected static Logger log = LoggerFactory.getLogger(WebCfgDelegate.class);
    
    private WebCfgDelegate() {
        init();
    }
    
    public static WebCfgDelegate getInstance() {
        if (singleton == null)
            singleton = new WebCfgDelegate();
        return singleton;
    }

    public String getWebConfigPath() {
        return getString("WebConfigPath");
    }
    
    public String getWadoBaseURL() {
        return noneAsNull(getString("WadoBaseURL"));
    }

    public String getWebviewerName() {
        return noneAsNull(getString("WebviewerName"));
    }

    public String getWebviewerBaseUrl() {
        return noneAsNull(getString("WebviewerBaseUrl"));
    }
   
    public int[] getWindowSize(String name) {
        try {
            return (int[]) server.invoke(serviceObjectName, "getWindowSize", 
                    new Object[]{name}, new String[]{String.class.getName()});
        } catch (Exception x) {
            log.warn("Cant invoke getWindowWidth! use 800,600 as default!", x);
            return new int[]{800,600};
        }
    }
    public List<String> getModalityList() {
        List<String> mods = getStringList("getModalityList");
        mods.add(0, "*");
        return mods;
    }
    public List<String> getSourceAETList() {
        return getStringList("getSourceAETList"); 
    }
    public List<String> getStationAETList() {
        return getStringList("getStationAETList"); 
    }
    public List<String> getStationNameList() {
        List<String> names = getStringList("getStationNameList");
        names.add(0, "*");
        return names;
    }
    
    @SuppressWarnings("unchecked")
    public List<Integer> getPagesizeList() {
        try {
            return (List<Integer>) server.invoke(serviceObjectName, "getPagesizeList", 
                    new Object[]{}, new String[]{});
        } catch (Exception x) {
            log.warn("Cant invoke 'getPagesizeList'! Return default list (10,25,50)!", x);
            return Arrays.asList(10,25,50);
        }
    }
    public Integer getDefaultFolderPagesize() {
        try {
            return (Integer) server.getAttribute(serviceObjectName, "DefaultFolderPagesize"); 
        } catch (Exception x) {
            log.warn("Cant get DefaultFolderPagesize attribute! return 10 as default!", x);
            return 10;
        }
    }
    public Integer getDefaultMWLPagesize() {
        try {
            return (Integer) server.getAttribute(serviceObjectName, "DefaultMWLPagesize"); 
        } catch (Exception x) {
            log.warn("Cant get DefaultMWLPagesize attribute! return 10 as default!", x);
            return 10;
        }
    }
    public boolean isQueryAfterPagesizeChange() {
        try {
            return (Boolean) server.getAttribute(serviceObjectName, "QueryAfterPagesizeChange"); 
        } catch (Exception x) {
            log.warn("Cant get QueryAfterPagesizeChange attribute! return true as default!", x);
            return true;
        }
    }
    
    public String getMpps2mwlPresetPatientname() {
        return getString("Mpps2mwlPresetPatientname");
    }
    public String getMpps2mwlPresetStartDate() {
        return getString("Mpps2mwlPresetStartDate");
    }
    public String getMpps2mwlPresetModality() {
        return getString("Mpps2mwlPresetModality");
    }

    private String noneAsNull(String s) {
        return "NONE".equals(s) ? null : s;
    }

    public String getString(String attrName) {
        try {
            return (String) server.getAttribute(serviceObjectName, attrName);
        } catch (Exception x) {
            log.warn("Cant get "+attrName+"! Ignored by return null!", x);
            return null;
        }
    }

    public int checkCUID(String cuid) {
        try {
            return (Integer) server.invoke(serviceObjectName, "checkCUID", 
                    new Object[]{cuid}, new String[]{String.class.getName()});
        } catch (Exception x) {
            log.warn("Cant invoke checkCUID! Ignored by return -1!", x);
            return -1;
        }
    }

    public ObjectName getObjectName(String attrName, String defaultName) throws MalformedObjectNameException, NullPointerException {
        try {
            return (ObjectName) server.getAttribute(serviceObjectName, attrName);
        } catch (Throwable t) {
            log.error("Can't get ObjectName for "+attrName+" ! use default:"+defaultName, t);
            return defaultName == null ? null : new ObjectName(defaultName);
        }
    }

    @SuppressWarnings("unchecked")
    private List<String> getStringList(String name) {
        try {
            return (List<String>) server.invoke(serviceObjectName, name, 
                    new Object[]{}, new String[]{});
        } catch (Exception x) {
            log.warn("Cant invoke '"+name+"'! Return empty list!", x);
            return new ArrayList<String>();
        }
    }

    protected void init() {
        log.info("Init WebCfgDelegate!");
        List<?> servers = MBeanServerFactory.findMBeanServer(null);
        if (servers != null && !servers.isEmpty()) {
            server = (MBeanServerConnection) servers.get(0);
            log.debug("Found MBeanServer:"+server);
        } else {
            log.error("Failed to get MBeanServerConnection! MbeanDelegate class:"+getClass().getName());
            return;
        }
        String s = ((WebApplication)Application.get()).getInitParameter("webCfgServiceName");
        if (s == null)
            s = "dcm4chee.web:service=WebConfig";
        try {
            serviceObjectName = new ObjectName(s);
            log.info("WebCfgDelegate initialized! WebConfig serviceName:"+serviceObjectName);
        } catch (Exception e) {
            log.error( "Failed to set ObjectName for WebCfgDelegate! name:"+s, e);
        }
    }
}
