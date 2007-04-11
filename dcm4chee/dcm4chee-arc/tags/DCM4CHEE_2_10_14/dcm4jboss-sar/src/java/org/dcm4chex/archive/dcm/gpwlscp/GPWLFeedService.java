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

package org.dcm4chex.archive.dcm.gpwlscp;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.ejb.CreateException;
import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.ObjectName;

import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmElement;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.dict.Tags;
import org.dcm4che.dict.UIDs;
import org.dcm4che.util.UIDGenerator;
import org.dcm4chex.archive.common.DatasetUtils;
import org.dcm4chex.archive.dcm.mppsscp.MPPSScpService;
import org.dcm4chex.archive.ejb.interfaces.ContentManager;
import org.dcm4chex.archive.ejb.interfaces.ContentManagerHome;
import org.dcm4chex.archive.ejb.interfaces.GPWLManager;
import org.dcm4chex.archive.ejb.interfaces.GPWLManagerHome;
import org.dcm4chex.archive.ejb.interfaces.MPPSManager;
import org.dcm4chex.archive.ejb.interfaces.MPPSManagerHome;
import org.dcm4chex.archive.ejb.interfaces.MWLManager;
import org.dcm4chex.archive.ejb.interfaces.MWLManagerHome;
import org.dcm4chex.archive.util.EJBHomeFactory;
import org.dcm4chex.archive.util.FileUtils;
import org.dcm4chex.archive.util.HomeFactoryException;
import org.jboss.system.ServiceMBeanSupport;
import org.jboss.system.server.ServerConfig;
import org.jboss.system.server.ServerConfigLocator;
import org.xml.sax.InputSource;

/**
 * @author gunter.zeilinger@tiani.com
 * @version $Revision$ $Date$
 * @since 05.04.2005
 * 
 */

public class GPWLFeedService extends ServiceMBeanSupport implements
        NotificationListener {

    private static final int[] PAT_ATTR_TAGS = { Tags.PatientName,
            Tags.PatientID, Tags.PatientBirthDate, Tags.PatientSex, };

    private static final int[] REF_RQ_TAGS_FROM_MPPS_SSA = {
            Tags.AccessionNumber, Tags.RefStudySeq, Tags.StudyInstanceUID,
            Tags.RequestedProcedureDescription, Tags.RequestedProcedureID, };

    private static final int[] REF_RQ_TAGS_FROM_MWL_ITEM = {
            Tags.RequestedProcedureCodeSeq, Tags.RequestingPhysician, };

    private static final int[] REF_RQ_TAGS_IN_MPPS_SSA_AND_MWL_ITEM = {
            Tags.AccessionNumber, Tags.RequestedProcedureDescription,
            Tags.RequestedProcedureID, };

    private ObjectName mppsScpServiceName;

    private Map humanPerformer = null;

    private List templates = null;

    private File templatePath = null;

    private File mppsConfigFile;

    private Properties mppsConfig = new Properties();

    private static DcmObjectFactory dof = DcmObjectFactory.getInstance();

    private static ServerConfig config = ServerConfigLocator.locate();

    public String getEjbProviderURL() {
        return EJBHomeFactory.getEjbProviderURL();
    }

    public void setEjbProviderURL(String ejbProviderURL) {
        EJBHomeFactory.setEjbProviderURL(ejbProviderURL);
    }

    public final ObjectName getMppsScpServiceName() {
        return mppsScpServiceName;
    }

    public final void setMppsScpServiceName(ObjectName mppsScpServiceName) {
        this.mppsScpServiceName = mppsScpServiceName;
    }

    /**
     * @return Returns the physicians.
     */
    public String getHumanPerformer() {
        return codes2String(humanPerformer);
    }

    /**
     * @param performer
     *            The human performer(s) to set.
     */
    public void setHumanPerformer(String performer) {
        this.humanPerformer = string2Codes(performer, "DCM4CHEE");
    }

    /**
     * @return Returns the configURL.
     */
    public String getTemplatePath() {
        return templatePath.getPath();
    }

    /**
     * @param configURL
     *            The configURL to set.
     * @throws MalformedURLException
     */
    public void setTemplatePath(String path) throws MalformedURLException {
        templatePath = new File(path.replace('/', File.separatorChar));
    }

    public final String getMppsConfigFile() {
        return mppsConfigFile.getPath();
    }

    public final void setMppsConfigFile(String path) throws IOException {
        File f = new File(path.replace('/', File.separatorChar));
        Properties p = new Properties();
        FileInputStream in = new FileInputStream(FileUtils.resolve(f));
        try {
            p.load(in);
        } finally {
            in.close();
        }
        this.mppsConfig = p;
        this.mppsConfigFile = f;
    }

    private String codes2String(Map codes) {
        if (codes == null || codes.isEmpty())
            return "";
        StringBuffer sb = new StringBuffer();
        Dataset ds;
        String design;
        for (Iterator iter = codes.values().iterator(); iter.hasNext();) {
            ds = (Dataset) iter.next();
            design = ds.getString(Tags.CodingSchemeDesignator);
            sb.append(ds.getString(Tags.CodeValue)).append("^");
            if (design != null)
                sb.append(design).append("^");
            sb.append(ds.getString(Tags.CodeMeaning)).append(",");
        }

        return sb.substring(0, sb.length() - 1);
    }

    private Map string2Codes(String codes, String defaultDesign) {
        StringTokenizer st = new StringTokenizer(codes, ",");
        Map map = new HashMap();
        int nrOfTokens;
        StringTokenizer stCode;
        Dataset ds;
        String codeValue;
        while (st.hasMoreTokens()) {
            stCode = new StringTokenizer(st.nextToken(), "^");
            nrOfTokens = stCode.countTokens();
            if (nrOfTokens < 2) {
                throw new IllegalArgumentException(
                        "Wrong format of human performer configuration! (<codeValue>[^<designator>]^<meaning>)");
            }
            ds = dof.newDataset();
            codeValue = stCode.nextToken();
            ds.putSH(Tags.CodeValue, codeValue);
            if (nrOfTokens > 2) {
                ds.putSH(Tags.CodingSchemeDesignator, stCode.nextToken());
            } else if (defaultDesign != null) {
                ds.putSH(Tags.CodingSchemeDesignator, defaultDesign);
            }
            ds.putLO(Tags.CodeMeaning, stCode.nextToken());
            map.put(codeValue, ds);
        }
        return map;
    }

    protected void startService() throws Exception {
        server.addNotificationListener(mppsScpServiceName, this,
                MPPSScpService.NOTIF_FILTER, null);
    }

    protected void stopService() throws Exception {
        server.removeNotificationListener(mppsScpServiceName, this,
                MPPSScpService.NOTIF_FILTER, null);
    }

    public void handleNotification(Notification notif, Object handback) {
        Dataset mpps = (Dataset) notif.getUserData();
        // if N_CREATE
        if (mpps.contains(Tags.ScheduledStepAttributesSeq))
            return;
        if (!"COMPLETED".equals(mpps.getString(Tags.PPSStatus)))
            return;
        final String mppsiuid = mpps.getString(Tags.SOPInstanceUID);
        addWorklistItem(makeGPWLItem(getMPPS(mppsiuid)));
    }

    public List listTemplates() {
        if (templates == null) {
            File tmplPath = FileUtils.resolve(templatePath);
            File[] files = tmplPath.listFiles();
            templates = new ArrayList();
            String fn;
            for (int i = 0; i < files.length; i++) {
                fn = files[i].getName();
                if (fn.endsWith(".xml")) {
                    templates.add(fn.substring(0, fn.length() - 4));
                }
            }
        }
        log.info("Template List:" + templates);
        return templates;
    }

    public void clearTemplateList() {
        templates = null;
    }

    public void addWorklistItem(Long studyPk, String templateFile,
            String humanPerformerCode, Long scheduleDate) throws Exception {
        String uri = FileUtils.resolve(
                new File(templatePath, templateFile + ".xml")).toURI()
                .toString();
        if (log.isDebugEnabled())
            log.debug("load template file: " + uri);
        Dataset ds = DatasetUtils.fromXML(new InputSource(uri));

        ContentManager cm = getContentManager();
        // patient
        Dataset patDS = cm.getPatientForStudy(studyPk.longValue());
        if (log.isDebugEnabled()) {
            log.debug("Patient Dataset:");
            log.debug(patDS);
        }

        ds.putAll(patDS.subSet(PAT_ATTR_TAGS));
        //
        Dataset sopInstRef = cm.getSOPInstanceRefMacro(studyPk.longValue(),
                false);
        String studyIUID = sopInstRef.getString(Tags.StudyInstanceUID);
        ds.putUI(Tags.SOPInstanceUID, UIDGenerator.getInstance().createUID());
        ds.putUI(Tags.StudyInstanceUID, studyIUID);
        DcmElement inSq = ds.putSQ(Tags.InputInformationSeq);
        inSq.addItem(sopInstRef);

        // Scheduled Human Performer Seq
        DcmElement schedHPSq = ds.putSQ(Tags.ScheduledHumanPerformersSeq);
        Dataset item = schedHPSq.addNewItem();
        DcmElement hpCodeSq = item.putSQ(Tags.HumanPerformerCodeSeq);
        Dataset dsCode = (Dataset) this.humanPerformer.get(humanPerformerCode);
        log.info(dsCode);
        if (dsCode != null) {
            hpCodeSq.addItem(dsCode);
            item.putPN(Tags.HumanPerformerName, dsCode
                    .getString(Tags.CodeMeaning));
        }

        // Scheduled Procedure Step Start Date and Time
        ds.putDT(Tags.SPSStartDateAndTime, new Date(scheduleDate.longValue()));

        if (log.isDebugEnabled()) {
            log.debug("GPSPS Dataset:");
            log.debug(ds);
        }

        addWorklistItem(ds);
    }

    private void addWorklistItem(Dataset ds) {
        if (ds == null)
            return;
        try {
            getGPWLManager().addWorklistItem(ds);
        } catch (Exception e) {
            log.error("Failed to add Worklist Item:", e);
        }
    }

    private GPWLManager getGPWLManager() throws CreateException,
            RemoteException, HomeFactoryException {
        return ((GPWLManagerHome) EJBHomeFactory.getFactory().lookup(
                GPWLManagerHome.class, GPWLManagerHome.JNDI_NAME)).create();
    }

    private Dataset makeGPWLItem(Dataset mpps) {
        Dataset codeItem = mpps.getItem(Tags.ProcedureCodeSeq);
        String key = codeItem.getString(Tags.CodeValue) + '^'
                + codeItem.getString(Tags.CodingSchemeDesignator);
        String wkitmtpl = mppsConfig.getProperty(key);
        if (wkitmtpl == null) {
            log.info("no workitem configured for procedure");
            log.info(codeItem);
            return null;
        }

        String uri = FileUtils.resolve(mppsConfigFile).getParentFile().toURI()
                + wkitmtpl;
        try {
            Dataset gpsps = DatasetUtils.fromXML(new InputSource(uri));
            gpsps.putAll(mpps.subSet(PAT_ATTR_TAGS));
            gpsps.putUI(Tags.SOPClassUID,
                    UIDs.GeneralPurposeScheduledProcedureStepSOPClass);
            final String iuid = UIDGenerator.getInstance().createUID();
            gpsps.putUI(Tags.SOPInstanceUID, iuid);
            DcmElement ssaSq = mpps.get(Tags.ScheduledStepAttributesSeq);
            String siuid = ssaSq.getItem().getString(Tags.StudyInstanceUID);
            gpsps.putUI(Tags.StudyInstanceUID, siuid);
            gpsps.putSH(Tags.SPSID, mpps.getString(Tags.PPSID));
            if (!gpsps.contains(Tags.SPSStartDateAndTime)) {
                gpsps.putDT(Tags.SPSStartDateAndTime, new Date());
            }
            DcmElement ppsSq = gpsps.putSQ(Tags.RefPPSSeq);
            Dataset refPPS = ppsSq.addNewItem();
            refPPS.putUI(Tags.RefSOPClassUID, mpps.getString(Tags.SOPClassUID));
            refPPS.putUI(Tags.RefSOPInstanceUID, mpps
                    .getString(Tags.SOPInstanceUID));
            DcmElement perfSeriesSq = mpps.get(Tags.PerformedSeriesSeq);
            DcmElement inSq = gpsps.putSQ(Tags.InputInformationSeq);
            Dataset inputInfo = inSq.addNewItem();
            inputInfo.putUI(Tags.StudyInstanceUID, siuid);
            DcmElement inSeriesSq = inputInfo.putSQ(Tags.RefSeriesSeq);
            for (int i = 0, n = perfSeriesSq.countItems(); i < n; ++i) {
                Dataset perfSeries = perfSeriesSq.getItem(i);
                Dataset inSeries = inSeriesSq.addNewItem();
                inSeries.putUI(Tags.SeriesInstanceUID, perfSeries
                        .getString(Tags.SeriesInstanceUID));
                DcmElement inRefSopSq = inSeries.putSQ(Tags.RefSOPSeq);
                DcmElement refImgSopSq = perfSeries.get(Tags.RefImageSeq);
                for (int j = 0, m = refImgSopSq.countItems(); j < m; ++j) {
                    inRefSopSq.addItem(refImgSopSq.getItem(j));
                }
                DcmElement refNoImgSopSq = perfSeries
                        .get(Tags.RefNonImageCompositeSOPInstanceSeq);
                for (int j = 0, m = refNoImgSopSq.countItems(); j < m; ++j) {
                    inRefSopSq.addItem(refNoImgSopSq.getItem(j));
                }
            }
            if (!gpsps.contains(Tags.RefRequestSeq)) {
                initRefRequestSeq(gpsps, ssaSq);
            }
            log.info("create workitem using template " + wkitmtpl);
            log.debug(gpsps);
            return gpsps;
        } catch (Exception e) {
            log.error("Failed to load workitem configuration from " + uri, e);
        }
        return null;
    }

    private void initRefRequestSeq(Dataset gpsps, DcmElement ssaSq)
            throws Exception {
        DcmObjectFactory dof = DcmObjectFactory.getInstance();
        DcmElement refRqSq = gpsps.putSQ(Tags.RefRequestSeq);
        for (int i = 0, n = ssaSq.countItems(); i < n; ++i) {
            Dataset ssa = ssaSq.getItem(i);
            String spsid = ssa.getString(Tags.SPSID);
            if (spsid != null) {
                Dataset refRq = dof.newDataset();
                refRq.putAll(ssa.subSet(REF_RQ_TAGS_FROM_MPPS_SSA));
                try {
                    Dataset mwlItem = getMWLManager().getWorklistItem(spsid);
                    if (mwlItem == null) {
                        log.warn("No such MWL item[spsid=" + spsid
                                + "] -> use request info available in MPPS");
                    } else if (checkConsistency(mwlItem, ssa)) {
                        refRq.putAll(mwlItem.subSet(REF_RQ_TAGS_FROM_MWL_ITEM));
                    }
                } catch (Exception e) {
                    log.warn("Failed to access MWL item[spsid=" + spsid
                            + "] -> use request info available in MPPS", e);
                }
                refRqSq.addItem(refRq);
            }
        }
    }

    private MWLManager getMWLManager() throws CreateException, RemoteException,
            HomeFactoryException {
        return ((MWLManagerHome) EJBHomeFactory.getFactory().lookup(
                MWLManagerHome.class, MWLManagerHome.JNDI_NAME)).create();
    }

    private boolean checkConsistency(Dataset mwlItem, Dataset ssa) {
        boolean ok = true;
        DcmElement mwlAttr, ssaAttr;
        int tag;
        for (int i = 0; i < REF_RQ_TAGS_IN_MPPS_SSA_AND_MWL_ITEM.length; ++i) {
            tag = REF_RQ_TAGS_IN_MPPS_SSA_AND_MWL_ITEM[i];
            mwlAttr = mwlItem.get(tag);
            ssaAttr = ssa.get(tag);
            if (mwlAttr != null && ssaAttr != null && !mwlAttr.equals(ssaAttr)) {
                log.warn("MPPS SSA attribute: " + ssaAttr + " does not match "
                        + mwlAttr + " of referenced MWL Item[spsid="
                        + ssa.getString(Tags.SPSID));
                ok = false;
            }
        }
        return ok;
    }

    private Dataset getMPPS(String iuid) {
        try {
            return getMPPSManager().getMPPS(iuid);
        } catch (Exception e) {
            log.error("Failed to load MPPS - " + iuid, e);
            return null;
        }
    }

    private MPPSManager getMPPSManager() throws CreateException,
            RemoteException, HomeFactoryException {
        return ((MPPSManagerHome) EJBHomeFactory.getFactory().lookup(
                MPPSManagerHome.class, MPPSManagerHome.JNDI_NAME)).create();
    }

    private ContentManager getContentManager() throws Exception {
        ContentManagerHome home = (ContentManagerHome) EJBHomeFactory
                .getFactory().lookup(ContentManagerHome.class,
                        ContentManagerHome.JNDI_NAME);
        return home.create();
    }

}
