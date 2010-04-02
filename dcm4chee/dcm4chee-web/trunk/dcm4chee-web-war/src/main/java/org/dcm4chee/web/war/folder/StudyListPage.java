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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.ResourceReference;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.ajax.markup.html.form.AjaxCheckBox;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.PropertyListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.validation.validator.PatternValidator;
import org.dcm4chee.archive.entity.Patient;
import org.dcm4chee.archive.entity.Study;
import org.dcm4chee.archive.util.JNDIUtils;
import org.dcm4chee.web.dao.StudyListFilter;
import org.dcm4chee.web.dao.StudyListLocal;
import org.dcm4chee.web.war.WicketApplication;
import org.dcm4chee.web.war.WicketSession;
import org.dcm4chee.web.common.markup.BaseForm;
import org.dcm4chee.web.common.markup.DateTimeLabel;
import org.dcm4chee.web.common.markup.PopupLink;
import org.dcm4chee.web.common.behaviours.TooltipBehaviour;

public class StudyListPage extends Panel {

    private static final String MODULE_NAME = "folder";
    private static final long serialVersionUID = 1L;
    private static int PAGESIZE = 10;
    private ViewPort viewport = ((WicketSession) getSession()).getViewPort();
    private StudyListHeader header = new StudyListHeader("thead");
    private IModel<Boolean> latestStudyFirst = new AbstractReadOnlyModel<Boolean>() {

        private static final long serialVersionUID = 1L;

        @Override
        public Boolean getObject() {
            return viewport.getFilter().isLatestStudiesFirst();
        }
    };
    private List<String> sourceAETs = new ArrayList<String>();
    private List<String> modalities = new ArrayList<String>();
    private boolean notSearched = true;
    private TooltipBehaviour tooltipBehaviour = new TooltipBehaviour("folder.");
    
    public StudyListPage(final String id) {
        super(id);
        final StudyListFilter filter = viewport.getFilter();
        BaseForm form = new BaseForm("form", new CompoundPropertyModel<Object>(filter));
        form.setResourceIdPrefix("folder.");
        form.setTooltipBehaviour(tooltipBehaviour);
        add(form);
        addQueryFields(filter, form);
        addQueryOptions(form);
        addNavigation(form);
        addActions(form);
        form.add(header);
        form.add(new PatientListView("patients", viewport.getPatients()));
        initModalitiesAndSourceAETs();
    }

    private void addQueryFields(final StudyListFilter filter, BaseForm form) {
        IModel<Boolean> enabledModel = new AbstractReadOnlyModel<Boolean>(){

            private static final long serialVersionUID = 1L;

            @Override
            public Boolean getObject() {
                return !filter.isExtendedStudyQuery() || "*".equals(filter.getStudyInstanceUID());
            }
            
        };
        form.addLabeledTextField("patientName", enabledModel);
        form.addLabel("patientIDDescr");
        form.addLabeledTextField("patientID", enabledModel);
        form.addLabeledTextField("issuerOfPatientID", enabledModel);
        PatternValidator datePatternValidator = new PatternValidator(
                "\\*|(((19)|(20))\\d{2}-((0[1-9])|(1[0-2]))-((0[1-9])|([12]\\d)|(3[01])))");
        addExtendedPatientSearch(form);
        form.addLabel("studyDate");
        form.addLabeledTextField("studyDateMin", enabledModel).add(datePatternValidator);
        form.addLabeledTextField("studyDateMax", enabledModel).add(datePatternValidator);
        form.addLabeledTextField("accessionNumber", enabledModel);
        addExtendedStudySearch(form);
        form.addLabeledDropDownChoice("modality", null, modalities);
        form.addLabeledDropDownChoice("sourceAET", null, sourceAETs);
    }

    private void addQueryOptions(BaseForm form) {
        form.addLabeledCheckBox("patientsWithoutStudies", null);
        form.addLabeledCheckBox("latestStudiesFirst", null);
    }

    private void addNavigation(BaseForm form) {
        form.add(new Button("search", new ResourceModel("searchBtn")) {

            private static final long serialVersionUID = 1L;

            @Override
            public void onSubmit() {
                viewport.setOffset(0);
                queryStudies();
            }});
        form.add(new Button("prev", new ResourceModel("folder.prev")) {

            private static final long serialVersionUID = 1L;

            @Override
            protected void onComponentTag(ComponentTag tag) {
                super.onComponentTag(tag);
                if (viewport.getOffset() == 0) {
                    tag.put("disabled", "");
                }
            }

            @Override
            public void onSubmit() {
                viewport.setOffset(Math.max(0, viewport.getOffset() - PAGESIZE));
                queryStudies();
            }});
        form.add(new Button("next", new ResourceModel("nextBtn")) {

            private static final long serialVersionUID = 1L;

            @Override
            protected void onComponentTag(ComponentTag tag) {
                super.onComponentTag(tag);
                if (viewport.getTotal() - viewport.getOffset() <= PAGESIZE) {
                    tag.put("disabled", "");
                }
            }

            @Override
            public void onSubmit() {
                viewport.setOffset(viewport.getOffset() + PAGESIZE);
                queryStudies();
            }});
        //viewport label: use StringResourceModel with key substitution to select 
        //property key according notSearched and getTotal.
        Model<?> keySelectModel = new Model<Serializable>() {

            private static final long serialVersionUID = 1L;

            @Override
            public Serializable getObject() {
                return notSearched ? "folder.notSearched" :
                        viewport.getTotal() == 0 ? "folder.noMatchingStudiesFound" : 
                            "folder.studiesFound";
            }
        };
        form.add(new Label("viewport", new StringResourceModel("${}", StudyListPage.this, keySelectModel,new Object[]{"dummy"}){

            private static final long serialVersionUID = 1L;

            @Override
            protected Object[] getParameters() {
                return new Object[]{viewport.getOffset()+1,
                        Math.min(viewport.getOffset()+PAGESIZE, viewport.getTotal()),
                        viewport.getTotal()};
            }
        }));
    }

    private void addActions(BaseForm form) {
        PopupLink l = new PopupLink("export", "exportPage") {

            private static final long serialVersionUID = 1L;

            @Override
            public void onClick() {
                ExportPage page = new ExportPage(viewport.getPatients());
                this.setResponsePage(page);
            }
        };
        l.setPopupHeight(400);
        l.setPopupWidth(550);
        form.add(l);
    }

    private WebMarkupContainer addExtendedPatientSearch(final Form<?> form) {
        final StudyListFilter filter = viewport.getFilter();
        final WebMarkupContainer extendedPatFilter = new WebMarkupContainer("extendedPatFilter") {

            private static final long serialVersionUID = 1L;

            @Override
            public boolean isVisible() {
                return filter.isExtendedPatQuery();
            }
        };
        extendedPatFilter.add( new Label("birthDateLabel", new ResourceModel("folder.birthDate")));
        extendedPatFilter.add( new Label("birthDateMinLabel", new ResourceModel("folder.birthDateMin")));
        extendedPatFilter.add( new Label("birthDateMaxLabel", new ResourceModel("folder.birthDateMax")));
        extendedPatFilter.add( new TextField<String>("birthDateMin"));
        extendedPatFilter.add( new TextField<String>("birthDateMax"));
        form.add(extendedPatFilter);
        AjaxFallbackLink<?> l = new AjaxFallbackLink<Object>("showExtendedPatFilter") {

            private static final long serialVersionUID = 1L;

            @Override
            public void onClick(AjaxRequestTarget target) {
                filter.setExtendedPatQuery(!filter.isExtendedPatQuery());
                target.addComponent(form);
            }};
        l.add(new Image("showExtendedPatFilterImg", new AbstractReadOnlyModel<ResourceReference>() {

            private static final long serialVersionUID = 1L;

            @Override
            public ResourceReference getObject() {
                return filter.isExtendedPatQuery() ? WicketApplication.IMAGE_COLLAPSE : 
                    WicketApplication.IMAGE_EXPAND;
            }
        }));
        form.add(l);
        return extendedPatFilter;
    }
    private WebMarkupContainer addExtendedStudySearch(final Form<?> form) {
        final StudyListFilter filter = viewport.getFilter();
        final WebMarkupContainer extendedStudyFilter = new WebMarkupContainer("extendedStudyFilter") {

            private static final long serialVersionUID = 1L;

            @Override
            public boolean isVisible() {
                return filter.isExtendedStudyQuery();
            }
        };
        extendedStudyFilter.add( new Label("studyInstanceUIDLabel", new ResourceModel("folder.studyInstanceUID")));
        extendedStudyFilter.add( new TextField<String>("studyInstanceUID"));
        form.add(extendedStudyFilter);
        AjaxFallbackLink<?> l = new AjaxFallbackLink<Object>("showExtendedStudyFilter") {

            private static final long serialVersionUID = 1L;

            @Override
            public void onClick(AjaxRequestTarget target) {
                filter.setExtendedStudyQuery(!filter.isExtendedStudyQuery());
                target.addComponent(form);
            }};
        l.add(new Image("showExtendedStudyFilterImg", new AbstractReadOnlyModel<ResourceReference>() {

            private static final long serialVersionUID = 1L;

            @Override
            public ResourceReference getObject() {
                return filter.isExtendedStudyQuery() ? WicketApplication.IMAGE_COLLAPSE : 
                    WicketApplication.IMAGE_EXPAND;
            }
        }));
        form.add(l);
        return extendedStudyFilter;
    }

    private void initModalitiesAndSourceAETs() {
        StudyListLocal dao = (StudyListLocal)
                JNDIUtils.lookup(StudyListLocal.JNDI_NAME);
        modalities.clear();
        modalities.add("*");
        modalities.addAll(dao.selectDistinctModalities());
        sourceAETs.clear();
        sourceAETs.add("*");
        sourceAETs.addAll(dao.selectDistinctSourceAETs());
    }

    private void queryStudies() {
        StudyListLocal dao = (StudyListLocal)
                JNDIUtils.lookup(StudyListLocal.JNDI_NAME);
        viewport.setTotal(dao.countStudies(viewport.getFilter()));
        updatePatients(dao.findStudies(viewport.getFilter(), PAGESIZE, viewport.getOffset()));
        notSearched = false;
    }

    private void updatePatients(List<Object[]> patientAndStudies) {
        retainSelectedPatients();
        for (Object[] patientAndStudy : patientAndStudies) {
            PatientModel patientModel = addPatient((Patient) patientAndStudy[0]);
            if (patientAndStudy[1] != null) {
                addStudy((Study) patientAndStudy[1], patientModel);
            }
        }
        header.setExpandAllLevel(1);
    }

    private boolean addStudy(Study study, PatientModel patient) {
        List<StudyModel> studies = patient.getStudies();
        for (StudyModel studyModel : studies) {
            if (studyModel.getPk() == study.getPk()) {
                return false;
            }
        }
        studies.add(new StudyModel(study));
        return true;
    }

    private void retainSelectedPatients() {
        for (Iterator<PatientModel> it = viewport.getPatients().iterator(); it.hasNext();) {
            PatientModel patient = it.next();
            patient.retainSelectedStudies();
            if (patient.isCollapsed() && !patient.isSelected()) {
                it.remove();
            }
        }
     }

    private PatientModel addPatient(Patient patient) {
        long pk = patient.getPk();
        for (PatientModel patientModel : viewport.getPatients()) {
            if (patientModel.getPk() == pk) {
                return patientModel;
            }
        }
        PatientModel patientModel = new PatientModel(patient, latestStudyFirst);
        viewport.getPatients().add(patientModel);
        return patientModel;
    }
    
    private boolean expandLevelChanged(AbstractDicomModel model) {
        int currLevel = header.getExpandAllLevel();
        int level = model.levelOfModel();
        if (model.isCollapsed() || currLevel > level) {
            level = getExpandedLevel( 0, viewport.getPatients());
        } else {
            level = getExpandedLevel( ++level, model.getDicomModelsOfNextLevel());
        }
        header.setExpandAllLevel(level);
        return level != currLevel;
    }
    
    private int getExpandedLevel(int startLevel, List<? extends AbstractDicomModel> list) {
        int level = startLevel; 
        if (list != null) {
            startLevel++;
            int l;
            for ( AbstractDicomModel m1 : list ) {
                if (!m1.isCollapsed()) {
                    l = getExpandedLevel( startLevel, m1.getDicomModelsOfNextLevel());
                    if ( l > level) 
                        level = l;
                }
            }
        }
        return level;
    }
    
    public static String getModuleName() {
        return MODULE_NAME;
    }

    private final class PatientListView extends PropertyListView<Object> {

        private static final long serialVersionUID = 1L;

        private PatientListView(String id, List<?> list) {
            super(id, list);
        }

        @Override
        protected void populateItem(final ListItem<Object> item) {
            item.setOutputMarkupId(true);
            final PatientModel patModel = (PatientModel) item.getModelObject();
            WebMarkupContainer cell = new WebMarkupContainer("cell"){

                private static final long serialVersionUID = 1L;

                @Override
                protected void onComponentTag(ComponentTag tag) {
                   super.onComponentTag(tag);
                   tag.put("rowspan", patModel.getRowspan());
                }
            };
            cell.add(new ExpandCollapseLink("expand", patModel, item));
            item.add(cell);
            item.add(new Label("name").add(tooltipBehaviour));
            item.add(new Label("id").add(tooltipBehaviour));
            item.add(new Label("issuer").add(tooltipBehaviour));
            item.add(new Label("birthdate").add(tooltipBehaviour));
            item.add(new Label("sex").add(tooltipBehaviour));
            item.add(new Label("comments").add(tooltipBehaviour));
            item.add(new Label("pk").add(new TooltipBehaviour("folder.","patPk")));
            item.add(new AjaxFallbackLink<Object>("toggledetails") {

                private static final long serialVersionUID = 1L;

                @Override
                public void onClick(AjaxRequestTarget target) {
                    patModel.setDetails(!patModel.isDetails());
                    if (target != null) {
                        target.addComponent(item);
                    }
                }

            }.add(new Image("detailImg",WicketApplication.IMAGE_DETAIL))
             .add(new TooltipBehaviour("folder.","patDetail")));
            item.add( new Link<Object>("edit") {
                
                private static final long serialVersionUID = 1L;

                @Override
                public void onClick() {
                    setResponsePage(
                            new EditDicomObjectPage(StudyListPage.this.getPage(), patModel));
                }
            }.add(new Image("editImg",WicketApplication.IMAGE_EDIT))
             .add(new TooltipBehaviour("folder.","patEdit")));
            item.add(new AjaxCheckBox("selected"){

                private static final long serialVersionUID = 1L;

                @Override
                protected void onUpdate(AjaxRequestTarget target) {
                    target.addComponent(this);
                }}.setOutputMarkupId(true).add(new TooltipBehaviour("folder.","patSelect")));
            WebMarkupContainer details = new WebMarkupContainer("details") {
                
                private static final long serialVersionUID = 1L;

                @Override
                public boolean isVisible() {
                    return patModel.isDetails();
                }
                
            };
            item.add(details);
            details.add(new DicomObjectPanel("dicomobject", patModel.getDataset(), false));
            item.add(new StudyListView("studies", patModel.getStudies(), item));
        }
    }

    private final class StudyListView extends PropertyListView<Object> {

        private static final long serialVersionUID = 1L;
        
        private final ListItem<?> patientListItem;

        private StudyListView(String id, List<StudyModel> list,
                ListItem<?> patientListItem) {
            super(id, list);
            this.patientListItem = patientListItem;
        }

        @Override
        protected void populateItem(final ListItem<Object> item) {
            item.setOutputMarkupId(true);
            final StudyModel studyModel = (StudyModel) item.getModelObject();
            WebMarkupContainer cell = new WebMarkupContainer("cell"){

                private static final long serialVersionUID = 1L;

                @Override
                protected void onComponentTag(ComponentTag tag) {
                   super.onComponentTag(tag);
                   tag.put("rowspan", studyModel.getRowspan());
                }
            };
            cell.add(new ExpandCollapseLink("expand", studyModel, patientListItem));
            item.add(cell);
            item.add(new Label("datetime").add(new TooltipBehaviour("folder.study","DateTime")));
            item.add(new Label("id").add(new TooltipBehaviour("folder.study","Id")));
            item.add(new Label("accessionNumber").add(new TooltipBehaviour("folder.","accessionNumber")));
            item.add(new Label("modalities").add(new TooltipBehaviour("folder.","modalities")));
            item.add(new Label("description").add(new TooltipBehaviour("folder.study","Description")));
            item.add(new Label("numberOfSeries").add(new TooltipBehaviour("folder.study","NoS")));
            item.add(new Label("numberOfInstances").add(new TooltipBehaviour("folder.study","NoI")));
            item.add(new Label("availability").add(new TooltipBehaviour("folder.study","Availability")));
            item.add(new Label("pk").add(new TooltipBehaviour("folder.", "studyPk")));
            item.add(new AjaxFallbackLink<Object>("toggledetails") {

                private static final long serialVersionUID = 1L;

                @Override
                public void onClick(AjaxRequestTarget target) {
                    studyModel.setDetails(!studyModel.isDetails());
                    if (target != null) {
                        target.addComponent(patientListItem);
                    }
                }

            }.add(new Image("detailImg",WicketApplication.IMAGE_DETAIL))
             .add(new TooltipBehaviour("folder.","studyDetail")));
            item.add( new Link<Object>("edit") {
                
                private static final long serialVersionUID = 1L;

                @Override
                public void onClick() {
                    setResponsePage(
                            new EditDicomObjectPage(StudyListPage.this.getPage(), studyModel));
                }
            }.add(new Image("editImg",WicketApplication.IMAGE_EDIT))
             .add(new TooltipBehaviour("folder.","studyEdit")));
            item.add( new AjaxCheckBox("selected"){

                private static final long serialVersionUID = 1L;

                @Override
                protected void onUpdate(AjaxRequestTarget target) {
                    target.addComponent(this);
                }}.setOutputMarkupId(true).add(new TooltipBehaviour("folder.","studySelect")));
            WebMarkupContainer details = new WebMarkupContainer("details") {
                
                private static final long serialVersionUID = 1L;

                @Override
                public boolean isVisible() {
                    return studyModel.isDetails();
                }
            };
            item.add(details);
            details.add(new DicomObjectPanel("dicomobject", studyModel.getDataset(), false));
            item.add(new PPSListView("ppss",
                    studyModel.getPPSs(), patientListItem));
        }
    }

    private final class PPSListView extends PropertyListView<Object> {

        private static final long serialVersionUID = 1L;
        
        private final ListItem<?> patientListItem;

        private PPSListView(String id, List<PPSModel> list,
                ListItem<?> patientListItem) {
            super(id, list);
            this.patientListItem = patientListItem;
        }

        @Override
        protected void populateItem(final ListItem<Object> item) {
            item.setOutputMarkupId(true);
            final PPSModel ppsModel = (PPSModel) item.getModelObject();
            WebMarkupContainer cell = new WebMarkupContainer("cell"){

                private static final long serialVersionUID = 1L;

                @Override
                protected void onComponentTag(ComponentTag tag) {
                   super.onComponentTag(tag);
                   tag.put("rowspan", ppsModel.getRowspan());
                }
            };
            cell.add(new ExpandCollapseLink("expand", ppsModel, patientListItem){

                private static final long serialVersionUID = 1L;

                @Override
                public boolean isVisible() {
                    return ppsModel.getUid() != null;
                }
            });
            item.add(cell);
            item.add(new Label("datetime").add(new TooltipBehaviour("folder.pps","DateTime")));
            item.add(new Label("id").add(new TooltipBehaviour("folder.pps","Id")));
            item.add(new Label("spsid").add(new TooltipBehaviour("folder.","spsid")));
            item.add(new Label("modality").add(new TooltipBehaviour("folder.pps","Modality")));
            item.add(new Label("description").add(new TooltipBehaviour("folder.pps","Description")));
            item.add(new Label("numberOfSeries").add(new TooltipBehaviour("folder.pps","NoS")));
            item.add(new Label("numberOfInstances").add(new TooltipBehaviour("folder.pps","NoI")));
            item.add(new Label("status").add(new TooltipBehaviour("folder.pps","Status")));
            item.add(new Label("pk").add(new TooltipBehaviour("folder.", "ppsPk")));
            item.add(new AjaxFallbackLink<Object>("toggledetails") {

                private static final long serialVersionUID = 1L;

                @Override
                public void onClick(AjaxRequestTarget target) {
                    ppsModel.setDetails(!ppsModel.isDetails());
                    if (target != null) {
                        target.addComponent(patientListItem);
                    }
                }

                @Override
                public boolean isVisible() {
                    return ppsModel.getDataset() != null;
                }
            }.add(new Image("detailImg",WicketApplication.IMAGE_DETAIL))
             .add(new TooltipBehaviour("folder.","ppsDetail")));
            item.add(new Link<Object>("edit") {

                private static final long serialVersionUID = 1L;

                @Override
                public void onClick() {
                    setResponsePage(new EditDicomObjectPage(StudyListPage.this.getPage(), ppsModel));
                }

                @Override
                public boolean isVisible() {
                    return ppsModel.getDataset() != null;
                }
            }.add(new Image("editImg",WicketApplication.IMAGE_EDIT))
             .add(new TooltipBehaviour("folder.","ppsEdit")));
            item.add(new AjaxCheckBox("selected"){

                private static final long serialVersionUID = 1L;

                @Override
                protected void onUpdate(AjaxRequestTarget target) {
                    target.addComponent(this);
                }}.setOutputMarkupId(true).add(new TooltipBehaviour("folder.","ppsSelect")));
            WebMarkupContainer details = new WebMarkupContainer("details") {
                
                private static final long serialVersionUID = 1L;

                @Override
                public boolean isVisible() {
                    return ppsModel.isDetails();
                }
                
            };
            item.add(details);
            details.add(new DicomObjectPanel("dicomobject", ppsModel.getDataset(), false));
            item.add(new SeriesListView("series",
                    ppsModel.getSeries(), patientListItem));
        }
    }

    private final class SeriesListView extends PropertyListView<Object> {

        private static final long serialVersionUID = 1L;
        
        private final ListItem<?> patientListItem;

        private SeriesListView(String id, List<SeriesModel> list,
                ListItem<?> patientListItem) {
            super(id, list);
            this.patientListItem = patientListItem;
        }

        @Override
        protected void populateItem(final ListItem<Object> item) {
            item.setOutputMarkupId(true);
            final SeriesModel seriesModel = (SeriesModel) item.getModelObject();
            WebMarkupContainer cell = new WebMarkupContainer("cell"){

                private static final long serialVersionUID = 1L;

                @Override
                protected void onComponentTag(ComponentTag tag) {
                   super.onComponentTag(tag);
                   tag.put("rowspan", seriesModel.getRowspan());
                }
            };
            cell.add(new ExpandCollapseLink("expand", seriesModel, patientListItem));
            item.add(cell);
            item.add(new Label("datetime").add(new TooltipBehaviour("folder.series","DateTime")));
            item.add(new Label("seriesNumber").add(new TooltipBehaviour("folder.","seriesNumber")));
            item.add(new Label("sourceAET").add(new TooltipBehaviour("folder.","sourceAET")));
            item.add(new Label("modality").add(new TooltipBehaviour("folder.series","Modality")));
            item.add(new Label("description").add(new TooltipBehaviour("folder.series","Description")));
            item.add(new Label("numberOfInstances").add(new TooltipBehaviour("folder.series","NoI")));
            item.add(new Label("availability").add(new TooltipBehaviour("folder.series","Availability")));
            item.add(new Label("pk").add(new TooltipBehaviour("folder.", "seriesPk")));
            item.add(new AjaxFallbackLink<Object>("toggledetails") {

                private static final long serialVersionUID = 1L;

                @Override
                public void onClick(AjaxRequestTarget target) {
                    seriesModel.setDetails(!seriesModel.isDetails());
                    if (target != null) {
                        target.addComponent(patientListItem);
                    }
                }

            }.add(new Image("detailImg",WicketApplication.IMAGE_DETAIL))
             .add(new TooltipBehaviour("folder.","seriesDetail")));
            item.add(new Link<Object>("edit") {
                
                private static final long serialVersionUID = 1L;

                @Override
                public void onClick() {
                    setResponsePage(new EditDicomObjectPage(StudyListPage.this.getPage(), seriesModel));
                }
            }.add(new Image("editImg",WicketApplication.IMAGE_EDIT))
             .add(new TooltipBehaviour("folder.","seriesEdit")));
            item.add(new AjaxCheckBox("selected"){

                private static final long serialVersionUID = 1L;

                @Override
                protected void onUpdate(AjaxRequestTarget target) {
                    target.addComponent(this);
                }}.setOutputMarkupId(true).add(new TooltipBehaviour("folder.","seriesSelect")));
            WebMarkupContainer details = new WebMarkupContainer("details") {
                
                private static final long serialVersionUID = 1L;

                @Override
                public boolean isVisible() {
                    return seriesModel.isDetails();
                }
                
            };
            item.add(details);
            details.add(new DicomObjectPanel("dicomobject", seriesModel.getDataset(), false));
            item.add(new InstanceListView("instances",
                    seriesModel.getInstances(), patientListItem));
        }
    }

    private final class InstanceListView extends PropertyListView<Object> {

        private static final long serialVersionUID = 1L;
        
        private final ListItem<?> patientListItem;

        private InstanceListView(String id, List<InstanceModel> list,
                ListItem<?> patientListItem) {
            super(id, list);
            this.patientListItem = patientListItem;
        }

        @Override
        protected void populateItem(final ListItem<Object> item) {
            item.setOutputMarkupId(true);
            final InstanceModel instModel = (InstanceModel) item.getModelObject();
            WebMarkupContainer cell = new WebMarkupContainer("cell"){

                private static final long serialVersionUID = 1L;

                @Override
                protected void onComponentTag(ComponentTag tag) {
                   super.onComponentTag(tag);
                   tag.put("rowspan", instModel.getRowspan());
                }
            };
            cell.add(new ExpandCollapseLink("expand", instModel, patientListItem));
            item.add(cell);
            item.add(new Label("datetime").add(new TooltipBehaviour("folder.instance","DateTime")));
            item.add(new Label("instanceNumber").add(new TooltipBehaviour("folder.","instanceNumber")));
            item.add(new Label("sopClassUID").add(new TooltipBehaviour("folder.","sopClassUID")));
            item.add(new Label("description").add(new TooltipBehaviour("folder.instance","Description")));
            item.add(new Label("availability").add(new TooltipBehaviour("folder.instance","Availability")));
            item.add(new Label("pk").add(new TooltipBehaviour("folder.", "instancePk")));
            item.add(new AjaxFallbackLink<Object>("toggledetails") {

                private static final long serialVersionUID = 1L;

                @Override
                public void onClick(AjaxRequestTarget target) {
                    instModel.setDetails(!instModel.isDetails());
                    if (target != null) {
                        target.addComponent(patientListItem);
                    }
                }

            }.add(new Image("detailImg",WicketApplication.IMAGE_DETAIL))
             .add(new TooltipBehaviour("folder.","instanceDetail")));
            item.add(new Link<Object>("edit") {
                
                private static final long serialVersionUID = 1L;

                @Override
                public void onClick() {
                    setResponsePage(new EditDicomObjectPage(StudyListPage.this.getPage(), instModel));
                }
            }.add(new Image("editImg",WicketApplication.IMAGE_EDIT))
             .add(new TooltipBehaviour("folder.","instanceEdit")));
            item.add(new AjaxCheckBox("selected"){

                private static final long serialVersionUID = 1L;

                @Override
                protected void onUpdate(AjaxRequestTarget target) {
                    target.addComponent(this);
                }}.setOutputMarkupId(true).add(new TooltipBehaviour("folder.","instanceSelect")));
            WebMarkupContainer details = new WebMarkupContainer("details") {
                
                private static final long serialVersionUID = 1L;

                @Override
                public boolean isVisible() {
                    return instModel.isDetails();
                }
                
            };
            item.add(details);
            details.add(new DicomObjectPanel("dicomobject", instModel.getDataset(), false));
            item.add(new FileListView("files", instModel.getFiles(), patientListItem));
        }
    }

    private final static class FileListView extends PropertyListView<Object> {

        private static final long serialVersionUID = 1L;
        
        private final ListItem<?> patientListItem;

        private FileListView(String id, List<FileModel> list,
                ListItem<?> patientListItem) {
            super(id, list);
            this.patientListItem = patientListItem;
        }

        @Override
        protected void populateItem(final ListItem<Object> item) {
            item.setOutputMarkupId(true);
            final FileModel fileModel = (FileModel) item.getModelObject();
            item.add(new DateTimeLabel("file.createdTime").add(new TooltipBehaviour("folder.file.","createdTime")));
            item.add(new Label("file.fileSize").add(new TooltipBehaviour("folder.file.","fileSize")));
            item.add(new Label("file.transferSyntaxUID").add(new TooltipBehaviour("folder.file.","transferSyntaxUID")));
            item.add(new Label("file.fileSystem.directoryPath").add(new TooltipBehaviour("folder.file.fileSystem.","directoryPath")));
            item.add(new Label("file.filePath").add(new TooltipBehaviour("folder.file.","filePath")));
            item.add(new Label("file.fileSystem.availability").add(new TooltipBehaviour("folder.file.fileSystem.","availability")));
            item.add(new Label("file.pk").add(new TooltipBehaviour("folder.","filePk")));
            item.add(new AjaxFallbackLink<Object>("toggledetails") {

                private static final long serialVersionUID = 1L;

                @Override
                public void onClick(AjaxRequestTarget target) {
                    fileModel.setDetails(!fileModel.isDetails());
                    if (target != null) {
                        target.addComponent(patientListItem);
                    }
                }

            }.add(new Image("detailImg",WicketApplication.IMAGE_DETAIL))
             .add(new TooltipBehaviour("folder.","fileDetail")));
            item.add(new AjaxCheckBox("selected"){

                private static final long serialVersionUID = 1L;

                @Override
                protected void onUpdate(AjaxRequestTarget target) {
                    target.addComponent(this);
                }}.setOutputMarkupId(true).add(new TooltipBehaviour("folder.","fileSelect")));
            WebMarkupContainer details = new WebMarkupContainer("details") {
                
                private static final long serialVersionUID = 1L;

                @Override
                public boolean isVisible() {
                    return fileModel.isDetails();
                }
                
            };
            item.add(details);
            details.add(new FilePanel("file", fileModel));
        }
    }
    
    private class ExpandCollapseLink extends AjaxFallbackLink<Object> {

        private static final long serialVersionUID = 1L;
        
        private AbstractDicomModel model;
        private ListItem<?> patientListItem;
        
        private ExpandCollapseLink(String id, AbstractDicomModel m, ListItem<?> patientListItem) {
            super(id);
            this.model = m;
            this.patientListItem = patientListItem;
            add( new Image(id+"Img", new AbstractReadOnlyModel<ResourceReference>() {

                private static final long serialVersionUID = 1L;

                @Override
                public ResourceReference getObject() {
                    return model.isCollapsed() ? WicketApplication.IMAGE_EXPAND : 
                        WicketApplication.IMAGE_COLLAPSE;
                }
            }));
        }
        
        @Override
        public void onClick(AjaxRequestTarget target) {
            if (model.isCollapsed()) {
                model.expand();
            } else {
                model.collapse();
            }
            boolean chgd = expandLevelChanged(model);
            if (target != null) {
                target.addComponent(patientListItem);
                if (chgd)
                    target.addComponent(header);
            }
        }
    }
}
