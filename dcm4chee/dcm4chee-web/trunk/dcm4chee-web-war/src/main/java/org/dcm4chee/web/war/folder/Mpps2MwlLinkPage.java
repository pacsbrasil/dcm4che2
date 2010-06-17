 /* Version: MPL 1.1/GPL 2.0/LGPL 2.1
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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.CSSPackageResource;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.resources.CompressedResourceReference;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.dcm4chee.archive.entity.Study;
import org.dcm4chee.icons.ImageManager;
import org.dcm4chee.icons.behaviours.ImageSizeBehaviour;
import org.dcm4chee.web.common.markup.BaseForm;
import org.dcm4chee.web.common.markup.DateTimeLabel;
import org.dcm4chee.web.dao.vo.MppsToMwlLinkResult;
import org.dcm4chee.web.dao.worklist.modality.ModalityWorklistFilter;
import org.dcm4chee.web.war.WicketSession;
import org.dcm4chee.web.war.folder.model.PPSModel;
import org.dcm4chee.web.war.folder.model.PatientModel;
import org.dcm4chee.web.war.folder.model.StudyModel;
import org.dcm4chee.web.war.worklist.modality.MWLItemListView;
import org.dcm4chee.web.war.worklist.modality.ModalityWorklistPanel;
import org.dcm4chee.web.war.worklist.modality.ViewPort;
import org.dcm4chee.web.war.worklist.modality.model.MWLItemModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Franz Willer <franz.willer@gmail.com>
 * @version $Revision$ $Date$
 * @since May 07, 2010
 */
public class Mpps2MwlLinkPage extends ModalWindow {

    private static final long serialVersionUID = 1L;
    private Mpps2MwlLinkPanelM panel = new Mpps2MwlLinkPanelM("content");
    private List<PPSModel> ppsModels;
    private PPSModel ppsModelForInfo;
    private PatientModel ppsPatModelForInfo;
    private Component comp;

    private static final ResourceReference CSS = new CompressedResourceReference(Mpps2MwlLinkPage.class, "mpps-link-style.css");
    private static Logger log = LoggerFactory.getLogger(Mpps2MwlLinkPage.class);
    
    public Mpps2MwlLinkPage(String id) {
        super(id);
        add(CSSPackageResource.getHeaderContribution(Mpps2MwlLinkPage.CSS));
        setContent(panel);
    }
    public void show(AjaxRequestTarget target, PPSModel ppsModel, Component c) {
        ppsModels  = toList(ppsModel);
        ppsModelForInfo = ppsModels.get(0);
        ppsPatModelForInfo = ppsModelForInfo.getParent().getParent();
        panel.presetSearchfields();
        comp = c;
        super.show(target);
    }
    private static List<PPSModel> toList(PPSModel ppsModel) {
        ArrayList<PPSModel> l = new ArrayList<PPSModel>(1);
        l.add(ppsModel);
        return l;
    }

    public class Mpps2MwlLinkPanelM extends ModalityWorklistPanel {

        private static final long serialVersionUID = 1L;
        private DropDownChoice<?> tfModality;

        public Mpps2MwlLinkPanelM(final String id) {
            super(id);
            addMppsInfoPanel();
        }
        
        protected ViewPort initViewPort() {
            return new ViewPort();
        }

        protected void addQueryFields(final ModalityWorklistFilter filter, BaseForm form) {
            final IModel<Boolean> enabledModel = new AbstractReadOnlyModel<Boolean>(){

                private static final long serialVersionUID = 1L;

                @Override
                public Boolean getObject() {
                    return !filter.isExtendedQuery() || "*".equals(filter.getStudyInstanceUID());
                }
            };
            form.addTextField("patientName", enabledModel, true);
            form.addLabel("patientIDDescr");
            form.addTextField("patientID", enabledModel, true);
            form.addTextField("issuerOfPatientID", enabledModel, true);
            
            form.addLabel("startDate");
            form.addDateTimeField("startDateMin", new PropertyModel<Date>(filter, "startDateMin"), enabledModel, false, true);
            form.addDateTimeField("startDateMax", new PropertyModel<Date>(filter, "startDateMax"), enabledModel, true, true);

            form.addTextField("accessionNumber", enabledModel, true);
            tfModality = form.addDropDownChoice("modality", null, getModalityChoices(), enabledModel, true);
            form.addDropDownChoice("scheduledStationAET", null, getStationAETChoices(), enabledModel, true);
            form.addDropDownChoice("scheduledStationName", null, getStationNameChoices(), enabledModel, true);
            form.addDropDownChoice("scheduledProcedureStepStatus", null, getSpsStatusChoices(), enabledModel, true);
        }

        protected WebMarkupContainer addExtendedStudySearch(final Form<?> form) {
            return null;
        }

        public void addMwlActions(final ListItem<MWLItemModel> item, final MWLItemListView mwlListView) {
            final MWLItemModel mwlItemModel = item.getModelObject();
            item.add(new AjaxFallbackLink<Object>("link") {
                private static final long serialVersionUID = 1L;
                @Override
                public void onClick(AjaxRequestTarget target) {
                    log.info("Link MPPS to MWL!:"+mwlItemModel);
                    try {
                        MppsToMwlLinkResult result = ContentEditDelegate.getInstance().linkMppsToMwl(ppsModels, mwlItemModel);
                        int nrOfStudies = result.getStudiesToMove().size();
                        if (nrOfStudies == 0) {
                            for (PPSModel mpps : ppsModels) {
                                mpps.getParent().collapse();
                                mpps.getParent().expand();
                            }
                        } else {
                            org.dcm4chee.web.war.folder.ViewPort viewport = ((WicketSession) getSession()).getFolderViewPort();
                            viewport.clear();
                            viewport.setTotal(nrOfStudies);
                            List<PatientModel> pats = viewport.getPatients();
                            PatientModel patModel = new PatientModel(result.getMwl().getPatient(), new Model<Boolean>(false));
                            pats.add(patModel);
                            StudyModel sm;
                            for (Study s : result.getStudiesToMove()) {
                                sm = new StudyModel(s, patModel);
                                sm.expand();
                                patModel.getStudies().add(sm);
                            }
                        }
                        target.addComponent(comp);
                        close(target);
                    } catch (Exception e) {
                        log.error("MPPS to MWL link failed!", e);
                    }
                }
            }.add(new Image("linkImg",ImageManager.IMAGE_COMMON_LINK)
            .add(new ImageSizeBehaviour())));
        }    
        
        @SuppressWarnings("unchecked")
        private void addMppsInfoPanel() {
            WebMarkupContainer p = new WebMarkupContainer("mppsInfo", new CompoundPropertyModel(new PpsInfoModel()));
            p.add(new Label("mppsInfoTitle", new ResourceModel("link.ppsInfoTitle")));
            p.add(new Label("patNameLabel", new ResourceModel("link.patNameLabel")));
            p.add(new Label("patName"));
            p.add(new Label("patIdLabel", new ResourceModel("link.patIdLabel")));
            p.add(new Label("patId"));
            p.add(new Label("patIssuerLabel", new ResourceModel("link.patIssuerLabel")));
            p.add(new Label("patIssuer"));
            p.add(new Label("modalityLabel", new ResourceModel("link.modalityLabel")));
            p.add(new Label("modality"));
            p.add(new Label("startDateLabel", new ResourceModel("link.startDateLabel")));
            p.add(new DateTimeLabel("datetime"));
            add(p);
        }
        
        private void presetSearchfields() {
            PPSModel ppsModel = ppsModels.get(0);
            String patPreset = this.getString("folder.mpps2mwl.preset.patientname");
            if ("delete".equals(patPreset)) {
                getViewPort().getFilter().setPatientName(null);
            } else if (patPreset != null) {
                PatientModel patModel = ppsModel.getParent().getParent();
                String name = patModel.getName();
                if ( !"*".equals(patPreset)) {
                    int nrofChars = Integer.parseInt(patPreset);
                    if (name != null && name.length() > nrofChars)
                        name = name.substring(0,nrofChars);
                }
                getViewPort().getFilter().setPatientName(name);
            }
            String modPreset = this.getString("folder.mpps2mwl.preset.modality");
            if ("delete".equals(modPreset)) {
                getViewPort().getFilter().setModality(null);
            } else if ("mpps".equals(modPreset)){
                String mod = ppsModel.getModality();
                if(tfModality.getChoices().contains(mod))
                    getViewPort().getFilter().setModality(mod);
            }
            String startPreset = this.getString("folder.mpps2mwl.preset.startdate");
            if ("delete".equals(startPreset)) {
                getViewPort().getFilter().setStartDateMin(null);
                getViewPort().getFilter().setStartDateMax(null);
            } else if (startPreset != null) {
                Calendar cal = Calendar.getInstance();
                if ("mpps".equals(startPreset)) {
                    cal.setTime(ppsModel.getDatetime());
                }
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.MILLISECOND, 0);
                getViewPort().getFilter().setStartDateMin(cal.getTime());
                cal.set(Calendar.HOUR_OF_DAY, 23);
                cal.set(Calendar.MINUTE, 59);
                cal.set(Calendar.MILLISECOND, 999);
                getViewPort().getFilter().setStartDateMax(cal.getTime());
            }
            queryMWLItems();
        }

        
    }

    private class PpsInfoModel implements Serializable{
        private static final long serialVersionUID = 1L;

        @SuppressWarnings("unused")
        public String getPatName() {
            return ppsPatModelForInfo.getName();
        }
        @SuppressWarnings("unused")
        public String getPatId() {
            return ppsPatModelForInfo.getId();
        }
        @SuppressWarnings("unused")
        public String getPatIssuer() {
            return ppsPatModelForInfo.getIssuer();
        }
        @SuppressWarnings("unused")
        public String getModality() {
            return ppsModelForInfo.getModality();
        }
        @SuppressWarnings("unused")
        public Date getDatetime() {
            return ppsModelForInfo.getDatetime();
        }
    }
}
