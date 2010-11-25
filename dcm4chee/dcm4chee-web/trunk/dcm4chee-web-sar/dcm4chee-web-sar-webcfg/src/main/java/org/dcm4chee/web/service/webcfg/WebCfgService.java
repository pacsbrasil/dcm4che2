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
package org.dcm4chee.web.service.webcfg;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.imageio.spi.ServiceRegistry;
import javax.management.Attribute;
import javax.management.Notification;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import javax.management.timer.TimerNotification;

import org.dcm4che2.data.UID;
import org.dcm4che2.util.UIDUtils;
import org.dcm4chee.archive.util.JNDIUtils;
import org.dcm4chee.web.common.webview.link.spi.WebviewerLinkProviderSPI;
import org.dcm4chee.web.dao.folder.StudyListLocal;
import org.dcm4chee.web.dao.folder.StudyPermissionsLocal;
import org.dcm4chee.web.dao.worklist.modality.ModalityWorklistLocal;
import org.jboss.system.ServiceMBeanSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author franz.willer@gmail.com
 * @version $Revision$ $Date$
 * @since July 26, 2010
 */
public class WebCfgService extends ServiceMBeanSupport implements NotificationListener, NotificationFilter {

    private static final long serialVersionUID = 1L;
    
    private static Logger log = LoggerFactory.getLogger(WebCfgService.class);
    
    private static final String DEFAULT_TIMER_SERVICE = "jboss:service=Timer";
    private static final long ONE_DAY_IN_MILLIS = 60000*60*24;

    private String webConfigPath;
    private String wadoBaseURL;
    private String webviewerName;
    private String webviewerBaseUrl;
    
    private ObjectName aeServiceName;
    private ObjectName echoServiceName;
    private ObjectName moveScuServiceName;
    private ObjectName contentEditServiceName;
    private ObjectName storeBridgeServiceName;
    private ObjectName mppsEmulatorServiceName;
    private ObjectName timerServiceName;
        
    private Map<String,int[]> windowsizeMap = new LinkedHashMap<String, int[]>();
    
    private Map<String, String> imageCUIDS = new LinkedHashMap<String, String>();
    private Map<String, String> srCUIDS = new LinkedHashMap<String, String>();
    private Map<String, String> waveformCUIDS = new LinkedHashMap<String, String>();
    private Map<String, String> videoCUIDS = new LinkedHashMap<String, String>();
    private Map<String, String> encapsulatedCUIDS = new LinkedHashMap<String, String>();
    
    private List<String> modalities = new ArrayList<String>();
    private List<String> sourceAETs = new ArrayList<String>();
    private List<String> stationAETs = new ArrayList<String>();
    private List<String> stationNames = new ArrayList<String>();
    private boolean autoUpdateModalities;
    private boolean autoUpdateSourceAETs;
    private boolean autoUpdateStationAETs;
    private boolean autoUpdateStationNames;
    private Integer autoUpdateTimerId;

    private List<Integer> pagesizes = new ArrayList<Integer>();
    private int defaultFolderPagesize;
    private int defaultMWLPagesize;
    private boolean queryAfterPagesizeChange; 
    
    private String mpps2mwlPresetPatientname;
    private String mpps2mwlPresetModality;
    private String mpps2mwlPresetStartDate;
    
    private boolean useFamilyAndGivenNameQueryFields;
    
    private String loginAllowedRolename;
    private String rolesMappingFilename;
    private String studyPermissionsRoleType;
    private String studyPermissionsAllRolename;
    private String studyPermissionsOwnRolename;
    
    private static final String NONE = "NONE";
    private static final String NEWLINE = System.getProperty("line.separator", "\n");
    
    public WebCfgService() {
    }
    
    protected void startService() throws Exception {
        timerServiceName = new ObjectName(DEFAULT_TIMER_SERVICE);
        updateAutoUpdateTimer();
    }
    
    public String getWebConfigPath() {
        return webConfigPath;
    }

    public void setWebConfigPath(String webConfigPath) {
        this.webConfigPath = webConfigPath;
    }

    public String getWadoBaseURL() {
        return wadoBaseURL;
    }

    public void setWadoBaseURL(String wadoBaseURL) {
        this.wadoBaseURL = wadoBaseURL;
    }
    
    public String getInstalledWebViewer() {
        Iterator<WebviewerLinkProviderSPI> iter = ServiceRegistry.lookupProviders(WebviewerLinkProviderSPI.class);
        StringBuilder sb = new StringBuilder();
        while (iter.hasNext()) {
            sb.append(iter.next().getName()).append(NEWLINE);
        }
        return sb.toString();
    }
    
    public String getWebviewerName() {
        return webviewerName;
    }

    public void setWebviewerName(String webviewerName) {
        this.webviewerName = webviewerName;
    }

    public String getWebviewerBaseUrl() {
        return webviewerBaseUrl;
    }

    public void setWebviewerBaseUrl(String webviewerBaseUrl) {
        this.webviewerBaseUrl = webviewerBaseUrl;
    }

    public String getImageCUIDS() {
        return uidsToString(imageCUIDS);
    }

    public void setImageCUIDS(String imageCUIDS) {
        this.imageCUIDS = parseUIDs(imageCUIDS);
    }

    public String getSrCUIDS() {
        return uidsToString(srCUIDS);
    }

    public void setSrCUIDS(String srCUIDS) {
        this.srCUIDS = parseUIDs(srCUIDS);
    }

    public String getWaveformCUIDS() {
        return uidsToString(waveformCUIDS);
    }

    public void setWaveformCUIDS(String waveformCUIDS) {
        this.waveformCUIDS = parseUIDs(waveformCUIDS);
    }

    public String getVideoCUIDS() {
        return uidsToString(videoCUIDS);
    }

    public void setVideoCUIDS(String videoCUIDS) {
        this.videoCUIDS = parseUIDs(videoCUIDS);
    }

    public String getEncapsulatedCUIDS() {
        return uidsToString(encapsulatedCUIDS);
    }

    public void setEncapsulatedCUIDS(String encapsulatedCUIDS) {
        this.encapsulatedCUIDS = parseUIDs(encapsulatedCUIDS);
    }

    public String getWindowSizeConfig() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, int[]> e : windowsizeMap.entrySet()) {
            sb.append(e.getKey()).append(':').
            append(e.getValue()[0]).append('x').append(e.getValue()[1]).
            append(NEWLINE);
        }
        return sb.toString();
    }
    public void setWindowSizeConfig(String s) {
        windowsizeMap.clear();
        StringTokenizer st = new StringTokenizer(s, " \t\r\n;");
        String t;
        int pos;
        while (st.hasMoreTokens()) {
            t = st.nextToken();
            if ((pos = t.indexOf(':')) == -1) {
                throw new IllegalArgumentException("Format must be:<name>:<width>x<height>! "+t);
            } else {
                windowsizeMap.put(t.substring(0, pos), parseSize(t.substring(++pos)));
            }
        }
    }
    
    public int[] getWindowSize(String name) {
        int[] size = windowsizeMap.get(name);
        if (size==null)
            size = windowsizeMap.get("default");
        if (size==null) {
            log.warn("No default window size is configured! use 800x600 as default!");
            return new int[]{800,600};
        }
        return size;
    }
    private int[] parseSize(String s) {
        int pos = s.indexOf('x');
        if (pos == -1)
            throw new IllegalArgumentException("Windowsize must be <width>x<height>! "+s);
        return new int[]{Integer.parseInt(s.substring(0,pos).trim()), 
                Integer.parseInt(s.substring(++pos).trim())};
    }
    
    public String getModalities() {
        return listAsString(modalities);
    }

    public List<String> getModalityList() {
        if (modalities.isEmpty()) {
            this.updateModalities();
        }
        return copyOfList(modalities);
    }
    
    public void setModalities(String s) {
        updateList(modalities, s);
    }
    
    public boolean isAutoUpdateModalities() {
        return autoUpdateModalities;
    }
    public void setAutoUpdateModalities(boolean b) {
        autoUpdateModalities = b;
        updateAutoUpdateTimer();
    }

    public String getSourceAETs() {
        return listAsString(sourceAETs);
    }
    public List<String> getSourceAETList() {
        if (sourceAETs.isEmpty()) {
            this.updateSourceAETs();
        }
        return copyOfList(sourceAETs);
    }
    public void setSourceAETs(String s) {
        updateList(sourceAETs, s);
    }
    public boolean isAutoUpdateSourceAETs() {
        return autoUpdateSourceAETs;
    }
    public void setAutoUpdateSourceAETs(boolean b) {
        autoUpdateSourceAETs = b;
        updateAutoUpdateTimer();
    }

    public String getStationAETs() {
        return listAsString(stationAETs);
    }
    public List<String> getStationAETList() {
        if (stationAETs.isEmpty()) {
            this.updateStationAETs();
        }
        return copyOfList(stationAETs);
    }
    public void setStationAETs(String s) {
        updateList(stationAETs, s);
    }
    public boolean isAutoUpdateStationAETs() {
        return autoUpdateStationAETs;
    }
    public void setAutoUpdateStationAETs(boolean b) {
        autoUpdateStationAETs = b;
        updateAutoUpdateTimer();
    }

    public String getStationNames() {
        return listAsString(stationNames);
    }
    public List<String> getStationNameList() {
        if (stationNames.isEmpty()) {
            this.updateStationNames();
        }
        return copyOfList(stationNames);
    }
    public void setStationNames(String s) {
        updateList(stationNames, s);
    }
    public boolean isAutoUpdateStationNames() {
        return autoUpdateStationNames;
    }
    public void setAutoUpdateStationNames(boolean b) {
        autoUpdateStationNames = b;
        updateAutoUpdateTimer();
    }

    public List<Integer> getPagesizeList() {
        return pagesizes;
    }
    public String getPagesizes() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0, len = pagesizes.size() ; i < len ; i++ ) {
            sb.append(pagesizes.get(i)).append(NEWLINE);
        }
        return sb.toString();
    }

    public void setPagesizes(String s) {
        pagesizes.clear();
        StringTokenizer st = new StringTokenizer(s.trim(), " \t\r\n;");
        String t;
        while (st.hasMoreTokens()) {
            t = st.nextToken().trim();
            if (t.length() > 0)
                pagesizes.add(new Integer(t));
        }
    }

    public int getDefaultFolderPagesize() {
        return defaultFolderPagesize;
    }

    public void setDefaultFolderPagesize(int size) {
        this.defaultFolderPagesize = size;
        if (!pagesizes.contains(new Integer(size))) {
            pagesizes.add(0,size);
        }
    }

    public int getDefaultMWLPagesize() {
        return defaultMWLPagesize;
    }

    public void setDefaultMWLPagesize(int size) {
        this.defaultMWLPagesize = size;
        if (!pagesizes.contains(new Integer(size))) {
            pagesizes.add(0,size);
        }
    }

    public boolean isQueryAfterPagesizeChange() {
        return queryAfterPagesizeChange;
    }

    public void setQueryAfterPagesizeChange(boolean queryAfterPagesizeChange) {
        this.queryAfterPagesizeChange = queryAfterPagesizeChange;
    }

    public boolean isUseFamilyAndGivenNameQueryFields() {
        return useFamilyAndGivenNameQueryFields;
    }

    public void setUseFamilyAndGivenNameQueryFields(boolean b) {
        this.useFamilyAndGivenNameQueryFields = b;
    }

    private String listAsString(List<String> list) {
        if (list.isEmpty()) {
            return NONE;
        }
        StringBuilder sb = new StringBuilder();
        for (String m : list) {
            sb.append(m).append('|');
        }
        return sb.substring(0, sb.length()-1);
    }
    private void updateList(List<String> list, String s) {
        list.clear();
        if (!NONE.equals(s)) {
            StringTokenizer st = new StringTokenizer(s, "|");
            while (st.hasMoreTokens()) {
                list.add(st.nextToken());
            }
        }
    }
    private List<String> copyOfList(List<String> src) {
        List<String> dest = new ArrayList<String>(src.size());
        dest.addAll(src);
        return dest;
    }

    public String getMpps2mwlPresetPatientname() {
        return mpps2mwlPresetPatientname;
    }

    public void setMpps2mwlPresetPatientname(String mpps2mwlPresetPatientname) {
        this.mpps2mwlPresetPatientname = mpps2mwlPresetPatientname;
    }

    public String getMpps2mwlPresetModality() {
        return mpps2mwlPresetModality;
    }

    public void setMpps2mwlPresetModality(String mpps2mwlPresetModality) {
        this.mpps2mwlPresetModality = mpps2mwlPresetModality;
    }

    public String getMpps2mwlPresetStartDate() {
        return mpps2mwlPresetStartDate;
    }

    public void setMpps2mwlPresetStartDate(String mpps2mwlPresetStartDate) {
        this.mpps2mwlPresetStartDate = mpps2mwlPresetStartDate;
    }

    public ObjectName getAEServiceName() {
        return aeServiceName;
    }
    public void setAEServiceName(ObjectName name) {
        this.aeServiceName = name;
    }

    public ObjectName getEchoServiceName() {
        return echoServiceName;
    }
    public void setEchoServiceName(ObjectName echoServiceName) {
        this.echoServiceName = echoServiceName;
    }

    public ObjectName getMoveScuServiceName() {
        return moveScuServiceName;
    }

    public void setMoveScuServiceName(ObjectName moveScuServiceName) {
        this.moveScuServiceName = moveScuServiceName;
    }

    public ObjectName getContentEditServiceName() {
        return contentEditServiceName;
    }

    public void setContentEditServiceName(ObjectName contentEditServiceName) {
        this.contentEditServiceName = contentEditServiceName;
    }

    public ObjectName getStoreBridgeServiceName() {
        return storeBridgeServiceName;
    }

    public void setStoreBridgeServiceName(ObjectName storeBridgeServiceName) {
        this.storeBridgeServiceName = storeBridgeServiceName;
    }

    public ObjectName getMppsEmulatorServiceName() {
        return mppsEmulatorServiceName;
    }

    public void setMppsEmulatorServiceName(ObjectName mppsEmulatorServiceName) {
        this.mppsEmulatorServiceName = mppsEmulatorServiceName;
    }

    public int checkCUID(String cuid) {
        if (isInCuids(cuid, imageCUIDS)) {
            return 0;
        } else if (isInCuids(cuid, srCUIDS)) {
            return 1;
        } else if (isInCuids(cuid, videoCUIDS)) {
            return 2;
        } else if (isInCuids(cuid, encapsulatedCUIDS)) {
            return 3;
        } else if (isInCuids(cuid, waveformCUIDS)) {
            return 4;
        }
        return -1;
    }
    
    private boolean isInCuids(String cuid, Map<String, String> cuids) {
        if (cuid == null)
            return false;
        if (!isDigit(cuid.charAt(0))) {
            try {
                cuid = UID.forName(cuid);
            } catch (Throwable t) {
                log.warn("Unknown UID:" +cuid,t);
                return false;
            }
        }
        return cuids.values().contains(cuid);
    }

    private String uidsToString(Map<String, String> uids) {
        if (uids.isEmpty())
            return NONE;
        StringBuilder sb = new StringBuilder();
        Iterator<String> iter = uids.keySet().iterator();
        while (iter.hasNext()) {
            sb.append(iter.next()).append(NEWLINE);
        }
        return sb.toString();
    }
    
    private static Map<String, String> parseUIDs(String uids) {
        StringTokenizer st = new StringTokenizer(uids, " \t\r\n;");
        String uid, name;
        Map<String, String> map = new LinkedHashMap<String, String>();
        while (st.hasMoreTokens()) {
            uid = st.nextToken().trim();
            name = uid;
            if (isDigit(uid.charAt(0))) {
                UIDUtils.verifyUID(uid);
            } else {
                uid = UID.forName(name);
            }
            map.put(name, uid);
        }
        return map;
    }

    private static boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    public void updateModalities() {
        log.info("Update Modality List");
        StudyListLocal dao = (StudyListLocal)
                JNDIUtils.lookup(StudyListLocal.JNDI_NAME);
        String mods= listAsString(dao.selectDistinctModalities());
        Attribute attribute = new Attribute("Modalities", mods);
        try {
            server.setAttribute(getServiceName(), attribute );
        } catch (Exception x) {
            log.error("Update Modalities failed! Modality list is only valid for service lifetime: "+mods, x);
            this.setModalities(mods);
        }
    }
    public void updateSourceAETs() {
        log.info("Update SourceAET List");
        StudyListLocal dao = (StudyListLocal)
                JNDIUtils.lookup(StudyListLocal.JNDI_NAME);
        String aets = listAsString(dao.selectDistinctSourceAETs());
        Attribute attribute = new Attribute("SourceAETs", aets);
        try {
            server.setAttribute(getServiceName(), attribute );
        } catch (Exception x) {
            log.error("Update SourceAETs failed! List of source AETs is only valid for service lifetime: "+aets, x);
            this.setSourceAETs(aets);
        }
    }
    public void updateStationAETs() {
        log.info("Update StationAET List");
        ModalityWorklistLocal dao = (ModalityWorklistLocal) JNDIUtils.lookup(ModalityWorklistLocal.JNDI_NAME);
        String aets = listAsString(dao.selectDistinctStationAETs());
        Attribute attribute = new Attribute("StationAETs", aets);
        try {
            server.setAttribute(getServiceName(), attribute );
        } catch (Exception x) {
            log.error("Update StationAETs failed! List of Station AETs is only valid for service lifetime: "+aets, x);
            this.setStationAETs(aets);
        }
    }
    public void updateStationNames() {
        log.info("Update Station Name List");
        ModalityWorklistLocal dao = (ModalityWorklistLocal) JNDIUtils.lookup(ModalityWorklistLocal.JNDI_NAME);
        String names = listAsString(dao.selectDistinctStationNames());
        Attribute attribute = new Attribute("StationNames", names);
        try {
            server.setAttribute(getServiceName(), attribute );
        } catch (Exception x) {
            log.error("Update StationNames failed! List of Station Names is only valid for service lifetime: "+names, x);
            this.setStationNames(names);
        }
    }

    private Date nextMidnight() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        return cal.getTime();
    }

    public void handleNotification(Notification notif, Object handback) {
        new Thread(new Runnable() {
            public void run() {
                if(isAutoUpdateModalities()) updateModalities();
                if(isAutoUpdateSourceAETs()) updateSourceAETs();
                if(isAutoUpdateStationAETs()) updateStationAETs();
                if(isAutoUpdateStationNames()) updateStationNames();
            }
        }).start();
    }
    public boolean isNotificationEnabled(Notification notification) {
        if (autoUpdateTimerId != null && (notification instanceof TimerNotification)) {
            TimerNotification lTimerNotification = (TimerNotification) notification;
            return lTimerNotification.getNotificationID().equals(this.autoUpdateTimerId);
        }
        return false;
    }

    private void updateAutoUpdateTimer() {
        if (server == null) return;
        if (autoUpdateModalities || autoUpdateSourceAETs || autoUpdateStationAETs || autoUpdateStationNames) {
            if (autoUpdateTimerId == null) {
                log.info("Start AutoUpdate Scheduler with period of 24h at 23:59:59");
                try {
                    autoUpdateTimerId = (Integer) server.invoke(timerServiceName, "addNotification",
                            new Object[] { "Schedule", "Scheduler Notification", null,
                            nextMidnight(), new Long(ONE_DAY_IN_MILLIS) }, new String[] {
                            String.class.getName(), String.class.getName(),
                            Object.class.getName(), Date.class.getName(),
                            Long.TYPE.getName() });
                    server.addNotificationListener(timerServiceName, this, this, null);
                } catch (Exception x) {
                    log.error("Start AutoUpdate Scheduler failed!", x);
                }
            }                
        } else if (autoUpdateTimerId != null) {
            try {
                log.info("Stop AutoUpdate Scheduler");
                server.removeNotificationListener(timerServiceName, this);
                server.invoke(timerServiceName, "removeNotification", new Object[] { autoUpdateTimerId },
                        new String[] { Integer.class.getName() });
                autoUpdateTimerId = null;
            } catch (Exception e) {
                log.error("operation failed", e);
            }
            
        }
    }
    
    public void updateDicomRoles() {
        ((StudyPermissionsLocal) JNDIUtils.lookup(StudyPermissionsLocal.JNDI_NAME)).updateDicomRoles(studyPermissionsRoleType);
    }

    public void setRolesMappingFilename(String rolesMappingFilename) {
        this.rolesMappingFilename = rolesMappingFilename;
    }

    public String getRolesMappingFilename() {
        return rolesMappingFilename;
    }

    public void setStudyPermissionsRoleType(String studyPermissionsRoleType) {
        this.studyPermissionsRoleType = studyPermissionsRoleType;
    }

    public String getStudyPermissionsRoleType() {
        return studyPermissionsRoleType;
    }

    public void setLoginAllowedRolename(String loginAllowedRolename) {
        this.loginAllowedRolename = loginAllowedRolename;
    }

    public String getLoginAllowedRolename() {
        return loginAllowedRolename;
    }

    public void setStudyPermissionsAllRolename(
            String studyPermissionsAllRolename) {
        this.studyPermissionsAllRolename = studyPermissionsAllRolename;
    }

    public String getStudyPermissionsAllRolename() {
        return studyPermissionsAllRolename;
    }

    public void setStudyPermissionsOwnRolename(
            String studyPermissionsOwnRolename) {
        this.studyPermissionsOwnRolename = studyPermissionsOwnRolename;
    }

    public String getStudyPermissionsOwnRolename() {
        return studyPermissionsOwnRolename;
    }
}

