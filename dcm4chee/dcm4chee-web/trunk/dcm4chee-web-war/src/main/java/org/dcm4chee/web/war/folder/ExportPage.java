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

package org.dcm4chee.web.war.folder;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.wicket.MetaDataKey;
import org.apache.wicket.ajax.AbstractAjaxTimerBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxCheckBox;
import org.apache.wicket.markup.html.CSSPackageResource;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.link.PopupCloseLink.ClosePopupPage;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.util.time.Duration;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.net.Association;
import org.dcm4che2.net.CommandUtils;
import org.dcm4che2.net.DimseRSPHandler;
import org.dcm4che2.util.StringUtils;
import org.dcm4chee.archive.entity.Study;
import org.dcm4chee.archive.util.JNDIUtils;
import org.dcm4chee.web.dao.AEHomeLocal;
import org.dcm4chee.web.dao.StudyListLocal;
import org.dcm4chee.web.common.base.BaseWicketPage;
import org.dcm4chee.web.common.markup.BaseForm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Franz Willer <franz.willer@gmail.com>
 * @version $Revision$ $Date$
 * @since Jan 11, 2010
 */
public class ExportPage extends BaseWicketPage {
    private static final MetaDataKey<String> LAST_DESTINATION_AET_ATTRIBUTE = new MetaDataKey<String>(){

        private static final long serialVersionUID = 1L;
    };
    private static final MetaDataKey<HashMap<Integer,ExportResult>> EXPORT_RESULTS = new MetaDataKey<HashMap<Integer,ExportResult>>(){

        private static final long serialVersionUID = 1L;
    };
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH.mm.ss,SSS");
    private String destinationAET;
    private boolean closeOnFinished;
    private static int id_count = 0;
    private static int id_req_count = 0;

    private List<String> destinationAETs = new ArrayList<String>();
    private int resultId;
    private ExportInfo exportInfo;
    
    private IModel<String> destinationModel = new IModel<String>(){

        private static final long serialVersionUID = 1L;
        
        public String getObject() {
            return destinationAET;
        }
        public void setObject(String dest) {
            destinationAET = dest;
        }
        public void detach() {}
    };
        
    private static Logger log = LoggerFactory.getLogger(ExportPage.class);
    
    @SuppressWarnings("serial")
    public ExportPage(List<PatientModel> list) {
        super();
        HashMap<Integer,ExportResult> results = getSession().getMetaData(EXPORT_RESULTS);
        exportInfo = new ExportInfo(list);
        if ( results == null ) {
            results = new HashMap<Integer,ExportResult>();
            getSession().setMetaData(EXPORT_RESULTS, results);
        }
        resultId = id_count++;
        ExportResult result = new ExportResult(resultId);
        results.put(resultId, result);
        destinationAET = getSession().getMetaData(LAST_DESTINATION_AET_ATTRIBUTE);
        add(CSSPackageResource.getHeaderContribution(ExportPage.class, "style.css"));
        initDestinationAETs();
        final BaseForm form = new BaseForm("form");
        add(form);
        form.add( new Label("label","DICOM Export"));
        form.addLabel("selectedItems");
        form.addLabel("selectedPats");
        form.add( new Label("selectedPatsValue", new PropertyModel<Integer>(exportInfo, "nrOfPatients")));
        form.addLabel("selectedStudies");
        form.add( new Label("selectedStudiesValue", new PropertyModel<Integer>(exportInfo, "nrOfStudies")));
        form.addLabel("selectedSeries");
        form.add( new Label("selectedSeriesValue", new PropertyModel<Integer>(exportInfo, "nrOfSeries")));
        form.addLabel("selectedInstances");
        form.add( new Label("selectedInstancesValue", new PropertyModel<Integer>(exportInfo, "nrOfInstances")));
        form.add(new DropDownChoice<String>("destinationAETs", destinationModel, destinationAETs){
            @Override
            public boolean isEnabled() {
                return exportInfo.hasSelection() && isExportInactive();
            }
        }.setNullValid(false).setOutputMarkupId(true));
        form.addLabel("destinationAETsLabel");
        form.addLabel("exportResultLabel");
        form.add( new Label("exportResult", new AbstractReadOnlyModel<String>() {
            @Override
            public String getObject() {
                if (exportInfo.hasSelection()) {
                    ExportResult r = getExportResults().get(resultId);
                    return (r == null ? getString("exportDone") : r.getResultString());
                } else {
                    return getString("noSelectionForExport");
                }
            }}).setOutputMarkupId(true));
        form.add( new Button("export", new ResourceModel("exportBtn")){
            @Override
            public void onSubmit() {
                getSession().setMetaData(LAST_DESTINATION_AET_ATTRIBUTE, destinationAET);
                exportSelected();
            }
            @Override
            public boolean isEnabled() {
                return exportInfo.hasSelection() && isExportInactive();
            }
        }.setOutputMarkupId(true));
        form.add(new Button("close", new ResourceModel("closeBtn")){
            @Override
            public void onSubmit() {
                getExportResults().remove(resultId);
                getPage().getPageMap().remove(ExportPage.this);
                // Web page closes window using javascript code in PopupCloseLink$1.html
                setResponsePage(ClosePopupPage.class);
            }
        });
        form.add(new AjaxCheckBox("closeOnFinished", new IModel<Boolean>(){
            public Boolean getObject() {
                return closeOnFinished;
            }
            public void setObject(Boolean object) {
                closeOnFinished = object;
            }
            public void detach() {}
        }){

            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                target.addComponent(this);
            }
            
        }.setEnabled(exportInfo.hasSelection()));
        form.addLabel("closeOnFinishedLabel");
        
        form.add(new AbstractAjaxTimerBehavior(Duration.milliseconds(700)){
            @Override
            protected void onTimer(AjaxRequestTarget target) {
                ExportResult result = getExportResults().get(resultId);
                if (result != null && !result.isRendered) {
                    target.addComponent(form.get("exportResult"));
                    if (result.nrOfMoverequests == 0) {
                        target.addComponent(form.get("export"));
                        target.addComponent(form.get("destinationAETs"));
                        if (  closeOnFinished && result.failedRequests.isEmpty() ) {
                            getExportResults().remove(resultId);
                            getPage().getPageMap().remove(ExportPage.this);
                            target.appendJavascript("javascript:self.close()");
                        }
                    }
                    result.isRendered = true;
                }
            }
        });
    }

    private void initDestinationAETs() {
        destinationAETs.clear();
        AEHomeLocal dao = (AEHomeLocal) JNDIUtils.lookup(AEHomeLocal.JNDI_NAME);
        destinationAETs.addAll(dao.listAETitles());
        if ( destinationAET == null && destinationAETs.size() > 0) {
            destinationAET = destinationAETs.get(0);
        }
        
    }

    private HashMap<Integer,ExportResult> getExportResults() {
        return getSession().getMetaData(EXPORT_RESULTS);
    }

    private void exportSelected() {
        ExportResult result = getExportResults().get(resultId);
        if ( result == null ) {
            result = new ExportResult(resultId);
            getExportResults().put(resultId, result);
        } else {
            getExportResults().get(resultId).clear();
        }
        for (MoveRequest rq : exportInfo.getMoveRequests()) {
            export(destinationAET, rq.patId, rq.studyIUIDs, rq.seriesIUIDs, rq.sopIUIDs, rq.toString(), result);
        }
    }


    private void export(String destAET, String patID, String[] studyIUIDs, String[] seriesIUIDs, String[] sopIUIDs, String descr, ExportResult result) {
        ExportResponseHandler rq = result.newRequest(destAET, descr);
        if ( !ExportDelegate.getInstance().export(destAET, patID, studyIUIDs, seriesIUIDs, sopIUIDs, rq )) {
            rq.reqDescr += " failed!";
            result.requestDone(rq, false);
        }
    }
    
    private String[] toArray(List<String> l) {
        if (l == null) return null;
        return l.toArray(new String[l.size()]);
    }
    
    private List<Study> getStudiesOfPatient(PatientModel pat) {
        StudyListLocal dao = (StudyListLocal)
        JNDIUtils.lookup(StudyListLocal.JNDI_NAME);
        return dao.findStudiesOfPatient(pat.getPk(), true);
    }

    private boolean isExportInactive() {
        ExportResult r = getExportResults().get(resultId);
        return r == null || r.nrOfMoverequests==0;
    }

    private class ExportInfo implements Serializable {
        private static final long serialVersionUID = 1L;
        List<MoveRequest> requests;
        int nrPat, nrStudy, nrSeries, nrInstances;
        
        private ExportInfo(List<PatientModel> patients) {
            this.requests = new ArrayList<MoveRequest>(patients.size());
            for (PatientModel pat : patients) {
                if (pat.isSelected()) {
                    prepareStudiesOfPatientRequests(pat);
                } else {
                    prepareStudyRequests(pat.getStudies());
                }
            }
        }
        
        public List<MoveRequest> getMoveRequests() {
            return requests;
        }

        public int getNrOfPatients() {
            return nrPat;
        }
        public int getNrOfStudies() {
            return nrStudy;
        }
        public int getNrOfSeries() {
            return nrSeries;
        }
        public int getNrOfInstances() {
            return nrInstances;
        }
        
        public boolean hasSelection() {
            return (nrPat | nrStudy | nrSeries | nrInstances) != 0;
        }
        
        private void prepareStudiesOfPatientRequests(PatientModel pat) {
            nrPat++;
            ArrayList<String> uids = new ArrayList<String>();
            List<Study> studies = getStudiesOfPatient(pat);
            for (Study study : studies) {
                uids.add(study.getStudyInstanceUID());
            }
            log.debug("Selected for export: Studies of Patient:{} StudyUIDs:{}", pat.getId(), uids);
            requests.add( new MoveRequest().setStudyMoveRequest(pat.getId(), toArray(uids)));
        }

        private void prepareStudyRequests(List<StudyModel> studies) {
            ArrayList<String> uids = new ArrayList<String>();
            for (StudyModel study : studies ) {
                if (study.isSelected()) {
                    nrStudy++;
                    uids.add(study.getStudyInstanceUID());
                } else {
                    prepareSeriesRequests(study.getStudyInstanceUID(), study.getPPSs());
                }
            }
            if ( !uids.isEmpty()) {
                log.debug("Selected for export: Studies:{}",uids);
                requests.add( new MoveRequest().setStudyMoveRequest(null, toArray(uids)));
            }
        }
        
        private void prepareSeriesRequests(String studyIUID, List<PPSModel> ppss) {
            ArrayList<String> uids = new ArrayList<String>();
            for (PPSModel pps : ppss ) {
                if (pps.isSelected()) {
                    log.debug("Selected for export: Series of selected PPS! AccNr:{}",pps.getAccessionNumber());
                    for (SeriesModel series : pps.getSeries()) {
                        nrSeries++;
                        uids.add(series.getSeriesInstanceUID());
                    }
                } else {
                    for (SeriesModel series : pps.getSeries()) {
                        if (series.isSelected()) {
                            nrSeries++;
                            uids.add(series.getSeriesInstanceUID());
                        } else {
                            prepareInstanceRequest(studyIUID, series.getSeriesInstanceUID(), series.getInstances());
                        }
                    }
                } 
            }
            if ( !uids.isEmpty()) {
                log.debug("Selected for export: Series (selected PPS and selcted Series):{}",uids);
                requests.add( new MoveRequest().setSeriesMoveRequest(null, studyIUID, toArray(uids)));
            }
        }
        
        private void prepareInstanceRequest(String studyIUID, String seriesIUID,
                List<InstanceModel> instances) {
            ArrayList<String> uids = new ArrayList<String>();
            for (InstanceModel instance : instances) {
                if (instance.isSelected()) {
                    nrInstances++;
                    uids.add(instance.getSOPInstanceUID());
                }
            }
            if ( !uids.isEmpty()) {
                log.debug("Selected for export: Instances:{}",uids);
                requests.add( new MoveRequest().setInstanceMoveRequest(null, studyIUID, seriesIUID, toArray(uids)));
            }
        }
        
    }
    
    private class ExportResult implements Serializable {
        private static final long serialVersionUID = 1L;
        private long start, end;
        private List<ExportResponseHandler> moveRequests = new ArrayList<ExportResponseHandler>(); 
        private List<ExportResponseHandler> failedRequests = new ArrayList<ExportResponseHandler>();
        private int id;
        private int nrOfMoverequests;
        private boolean isRendered = true;
        
        public ExportResult(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }
        
        public int[] calcTotal() {
            int[] total = new int[4];
            for ( ExportResponseHandler h : moveRequests) {
                total[0] += h.completed;
                total[1] += h.warning;
                total[2] += h.failed;
                total[3] += h.remaining;
            }
            return total;
        }
        
        public void clear() {
            moveRequests.clear();
            failedRequests.clear();
            nrOfMoverequests = 0;
        }

        public ExportResponseHandler newRequest(String destAET, String descr) {
            if (start==0)
                start = System.currentTimeMillis();
            nrOfMoverequests++;
            ExportResponseHandler handler = new ExportResponseHandler(this, descr);
            moveRequests.add(handler);
            isRendered = false;
            return handler;
        }
        
        public void requestDone(ExportResponseHandler h, boolean success) {
            if ( --nrOfMoverequests == 0)
                end = System.currentTimeMillis();
            if (!success)
                failedRequests.add(h);
            this.isRendered = false;
        }

        public String getResultString() {
            int totalRequests = moveRequests.size();
            if (totalRequests == 0) {
                return ExportPage.this.getString("exportNotStarted");
            } else {
                int[] total = calcTotal();
                StringBuilder sb = new StringBuilder();
                if ( this.nrOfMoverequests == 0) {
                    sb.append(totalRequests).append(" C-MOVE requests done in ")
                    .append(end-start).append(" ms!\n");
                } else {
                    sb.append(moveRequests.size()).append(" of ").append(totalRequests)
                    .append(" C-MOVE requests pending!\n");
                }
                sb.append("Instances completed:").append(total[0])
                .append(" warning:").append(total[1]).append(" failed:").append(total[2])
                .append(" remaining:").append(total[3]);
                if (!failedRequests.isEmpty()) {
                    sb.append("\nFailed C-MOVE requests:\n");
                    for ( ExportResponseHandler h : failedRequests) {
                        sb.append(h.id).append(": ").append(h.reqDescr).append("\n");
                    }
                }
                return sb.toString();
            }
        }
    }

    private class ExportResponseHandler extends DimseRSPHandler implements Serializable {
        private static final long serialVersionUID = 1L;
        private String reqDescr;
        
        private int id;
        private ExportResult exportResult;
        private long started;

        private int remaining;
        private int completed;
        private int warning;
        private int failed;
        private int status;

        
        public ExportResponseHandler(ExportResult result, String descr) {
            started = System.currentTimeMillis();
            id = id_req_count++;
            this.exportResult = result;
            reqDescr = descr;
        }
        
        @Override
        public int hashCode() {
            return id;
        }
        
        @Override
        public boolean equals(Object o) {
            return ((ExportResponseHandler) o).id == id;
        }


        @Override
        public void onDimseRSP(Association as, DicomObject cmd,
                DicomObject data) {
            log.info("ExportResponseHandler (msgId{}) received C-MOVE-RSP:{}", getMessageID(), cmd);
            exportResult.isRendered = false;
            remaining = cmd.getInt(Tag.NumberOfRemainingSuboperations);
            completed = cmd.getInt(Tag.NumberOfCompletedSuboperations);
            warning = cmd.getInt(Tag.NumberOfWarningSuboperations);
            failed = cmd.getInt(Tag.NumberOfFailedSuboperations);
            if (!CommandUtils.isPending(cmd)) {
                status = cmd.getInt(Tag.Status);
                synchronized (exportResult) {
                    if (status == 0) {
                        exportResult.requestDone(this, true);
                    } else {
                        reqDescr += "\n  failed with status "+StringUtils.shortToHex(status)+
                            " Error Comment:"+cmd.getString(Tag.ErrorComment)+
                            "\n  Started at "+sdf.format(new Date(started))+" failed at "+sdf.format(new Date());
                        exportResult.requestDone(this, false);
                    }
                }
                log.info("Move Request Done. close assoc! calledAET:{}",as.getCalledAET());
                try {
                    as.release(true);
                } catch (InterruptedException e) {
                    log.error("Association release failed! AET:{}", as.getCalledAET());
                }
            }
        }
    }
    
    private class MoveRequest implements Serializable {

        private static final long serialVersionUID = 1L;
        
        String patId;
        String[] studyIUIDs, seriesIUIDs, sopIUIDs;
        
        public MoveRequest setStudyMoveRequest(String patId, String[] studyIUIDs) {
            this.patId = patId;
            this.studyIUIDs = studyIUIDs;
            return this;
        }
        public MoveRequest setSeriesMoveRequest(String patId, String studyIUID, String[] seriesIUIDs) {
            this.patId = patId;
            this.studyIUIDs = new String[]{studyIUID};
            this.seriesIUIDs = seriesIUIDs;
            return this;
        }
        public MoveRequest setInstanceMoveRequest(String patId, String studyIUID, String seriesIUID, String[] sopIUIDs) {
            this.patId = patId;
            this.studyIUIDs = new String[]{studyIUID};
            this.seriesIUIDs = new String[]{seriesIUID};
            this.sopIUIDs = sopIUIDs;
            return this;
        }
        
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("PatId:").append(patId == null ? "[unknown]" : patId);
            if (studyIUIDs != null) 
                sb.append(",studyIUIDs:").append(StringUtils.join(studyIUIDs, ','));
            if (seriesIUIDs != null) 
                sb.append(", seriesIUIDs:").append(StringUtils.join(seriesIUIDs, ','));
            if (sopIUIDs != null) 
                sb.append(", sopIUIDs:").append(StringUtils.join(sopIUIDs, ','));
            return sb.toString();
        }
    }
}
