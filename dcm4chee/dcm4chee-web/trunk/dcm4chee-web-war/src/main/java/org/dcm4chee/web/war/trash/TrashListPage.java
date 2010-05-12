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

package org.dcm4chee.web.war.trash;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.ResourceReference;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxCheckBox;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.CSSPackageResource;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.PropertyListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.html.resources.CompressedResourceReference;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.dcm4chee.archive.entity.PrivatePatient;
import org.dcm4chee.archive.entity.PrivateStudy;
import org.dcm4chee.archive.util.JNDIUtils;
import org.dcm4chee.dashboard.ui.filesystem.FileSystemPanel;
import org.dcm4chee.icons.ImageManager;
import org.dcm4chee.icons.behaviours.ImageSizeBehaviour;
import org.dcm4chee.web.common.behaviours.TooltipBehaviour;
import org.dcm4chee.web.common.markup.BaseForm;
import org.dcm4chee.web.common.markup.modal.ConfirmationWindow;
import org.dcm4chee.web.common.markup.modal.MessageWindow;
import org.dcm4chee.web.dao.folder.StudyListLocal;
import org.dcm4chee.web.dao.trash.TrashListFilter;
import org.dcm4chee.web.dao.trash.TrashListLocal;
import org.dcm4chee.web.dao.util.QueryUtil;
import org.dcm4chee.web.war.common.model.AbstractDicomModel;
import org.dcm4chee.web.war.folder.DicomObjectPanel;
import org.dcm4chee.web.war.trash.model.PrivInstanceModel;
import org.dcm4chee.web.war.trash.model.PrivPatientModel;
import org.dcm4chee.web.war.trash.model.PrivSeriesModel;
import org.dcm4chee.web.war.trash.model.PrivStudyModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Franz Willer <franz.willer@gmail.com>
 * @version $Revision$ $Date$
 * @since May 10, 2010
 */
public class TrashListPage extends Panel {

    private static Logger log = LoggerFactory.getLogger(FileSystemPanel.class);

    private static final ResourceReference CSS = new CompressedResourceReference(TrashListPage.class, "trash-style.css");

    private static final String MODULE_NAME = "trash";
    private static final long serialVersionUID = 1L;
    private static int PAGESIZE = 10;
    private TrashViewPort viewport = new TrashViewPort();
    private TrashListHeader header = new TrashListHeader("thead");
    private PrivSelectedEntities selected = new PrivSelectedEntities();
    
    private List<String> sourceAETs = new ArrayList<String>();
    private boolean notSearched = true;
    private TooltipBehaviour tooltipBehaviour = new TooltipBehaviour("trash.");
    private MessageWindow msgWin = new MessageWindow("msgWin");
    
    public TrashListPage(final String id) {
        super(id);
        
        if (TrashListPage.CSS != null)
            add(CSSPackageResource.getHeaderContribution(TrashListPage.CSS));
       
        final TrashListFilter filter = viewport.getFilter();
        BaseForm form = new BaseForm("form", new CompoundPropertyModel<Object>(filter));
        form.setResourceIdPrefix("trash.");
        form.setTooltipBehaviour(tooltipBehaviour);
        add(form);
        addQueryFields(filter, form);
        addQueryOptions(form);
        addNavigation(form);
        addActions(form);
        form.add(header);
        form.add(new PatientListView("patients", viewport.getPatients()));
        msgWin.setTitle(MessageWindow.TITLE_WARNING);
        add(msgWin);
        initModalitiesAndSourceAETs();
    }

    private void addQueryFields(final TrashListFilter filter, BaseForm form) {
        IModel<Boolean> enabledModel = new AbstractReadOnlyModel<Boolean>(){
            private static final long serialVersionUID = 1L;
            @Override
            public Boolean getObject() {
                return QueryUtil.isUniversalMatch(filter.getStudyInstanceUID());
            }
        };
        form.addLabeledTextField("patientName", enabledModel);
        form.addLabel("patientIDDescr");
        form.addLabeledTextField("patientID", enabledModel);
        form.addLabeledTextField("issuerOfPatientID", enabledModel);
        form.addLabeledTextField("accessionNumber", enabledModel);
        form.addLabeledDropDownChoice("sourceAET", null, sourceAETs);
    }

    private void addQueryOptions(BaseForm form) {
        form.addLabeledCheckBox("patientsWithoutStudies", null);
    }

    private void addNavigation(BaseForm form) {
        Button searchBtn = new Button("search", new ResourceModel("searchBtn")) {

            private static final long serialVersionUID = 1L;

            @Override
            public void onSubmit() {
                viewport.setOffset(0);
                queryStudies();
            }};
        form.add(searchBtn);
        form.setDefaultButton(searchBtn);
        form.add(new Button("prev", new ResourceModel("trash.prev")) {

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
                return notSearched ? "trash.notSearched" :
                        viewport.getTotal() == 0 ? "trash.noMatchingStudiesFound" : 
                            "trash.studiesFound";
            }
        };
        form.add(new Label("viewport", new StringResourceModel("${}", TrashListPage.this, keySelectModel,new Object[]{"dummy"}){

            private static final long serialVersionUID = 1L;

            @Override
            protected Object[] getParameters() {
                return new Object[]{viewport.getOffset()+1,
                        Math.min(viewport.getOffset()+PAGESIZE, viewport.getTotal()),
                        viewport.getTotal()};
            }
        }));
    }

    private void addActions(final BaseForm form) {
        final ConfirmationWindow<PrivSelectedEntities> confirmDelete = new ConfirmationWindow<PrivSelectedEntities>("confirmDelete"){

            private static final long serialVersionUID = 1L;
            
            @Override
            public void onConfirmation(AjaxRequestTarget target, PrivSelectedEntities selected) {
                if (removeTrashItems(selected)) {
                    this.setStatus(new StringResourceModel("trash.deleteDone", TrashListPage.this,null));
                    viewport.getPatients().clear();
                } else {
                    this.setStatus(new StringResourceModel("trash.deleteFailed", TrashListPage.this,null));
                }
                queryStudies();
            }
            
            @Override
            public void onOk(AjaxRequestTarget target) {
                target.addComponent(form);
            }
        };
        confirmDelete.setInitialHeight(150);
        form.add(confirmDelete);
        AjaxLink<?> deleteBtn = new AjaxLink<Object>("deleteBtn") {

            private static final long serialVersionUID = 1L;

            @Override
            public void onClick(AjaxRequestTarget target) {
                selected.update(viewport.getPatients());
                selected.deselectChildsOfSelectedEntities();
                log.info("Selected Entities: :"+selected);
                if (selected.hasDicomSelection()) {
                    confirmDelete.confirm(target, new StringResourceModel("trash.confirmDelete",this, null,new Object[]{selected}), selected);
                } else {
                    msgWin.show(target, getString("trash.noSelection"));
                }
            }
        };
        deleteBtn.add(new Image("deleteImg",ImageManager.IMAGE_TRASH)
        .add(new ImageSizeBehaviour()))
        .add(tooltipBehaviour);
        form.add(deleteBtn);
    }

    private void initModalitiesAndSourceAETs() {
        StudyListLocal dao = (StudyListLocal)
                JNDIUtils.lookup(StudyListLocal.JNDI_NAME);
        sourceAETs.clear();
        sourceAETs.add("*");
        sourceAETs.addAll(dao.selectDistinctSourceAETs());
    }

    private void queryStudies() {
        TrashListLocal dao = (TrashListLocal)
                JNDIUtils.lookup(TrashListLocal.JNDI_NAME);
        viewport.setTotal(dao.countStudies(viewport.getFilter()));
        updatePatients(dao.findStudies(viewport.getFilter(), PAGESIZE, viewport.getOffset()));
        notSearched = false;
    }

    private void updatePatients(List<Object[]> patientAndStudies) {
        retainSelectedPatients();
        for (Object[] patientAndStudy : patientAndStudies) {
            PrivPatientModel patientModel = addPatient((PrivatePatient) patientAndStudy[0]);
            if (patientAndStudy[1] != null) {
                addStudy((PrivateStudy) patientAndStudy[1], patientModel);
            }
        }
        header.setExpandAllLevel(1);
    }

    private boolean addStudy(PrivateStudy study, PrivPatientModel patient) {
        List<PrivStudyModel> studies = patient.getStudies();
        for (PrivStudyModel studyModel : studies) {
            if (studyModel.getPk() == study.getPk()) {
                return false;
            }
        }
        studies.add(new PrivStudyModel(study));
        return true;
    }

    private void retainSelectedPatients() {
        for (Iterator<PrivPatientModel> it = viewport.getPatients().iterator(); it.hasNext();) {
            PrivPatientModel patient = it.next();
            patient.retainSelectedStudies();
            if (patient.isCollapsed() && !patient.isSelected()) {
                it.remove();
            }
        }
     }

    private PrivPatientModel addPatient(PrivatePatient patient) {
        long pk = patient.getPk();
        for (PrivPatientModel patientModel : viewport.getPatients()) {
            if (patientModel.getPk() == pk) {
                return patientModel;
            }
        }
        PrivPatientModel patientModel = new PrivPatientModel(patient);
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
            final PrivPatientModel patModel = (PrivPatientModel) item.getModelObject();
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
            item.add(new Label("pk").add(new TooltipBehaviour("trash.","patPk")));
            item.add(new AjaxFallbackLink<Object>("toggledetails") {

                private static final long serialVersionUID = 1L;

                @Override
                public void onClick(AjaxRequestTarget target) {
                    patModel.setDetails(!patModel.isDetails());
                    if (target != null) {
                        target.addComponent(item);
                    }
                }

            }.add(new Image("detailImg",ImageManager.IMAGE_DETAIL)
            .add(new ImageSizeBehaviour()))
            .add(new TooltipBehaviour("trash.","patDetail")));
            item.add(new AjaxCheckBox("selected"){

                private static final long serialVersionUID = 1L;

                @Override
                protected void onUpdate(AjaxRequestTarget target) {
                    target.addComponent(this);
                }}.setOutputMarkupId(true).add(new TooltipBehaviour("trash.","patSelect")));
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

        private StudyListView(String id, List<PrivStudyModel> list,
                ListItem<?> patientListItem) {
            super(id, list);
            this.patientListItem = patientListItem;
        }

        @Override
        protected void populateItem(final ListItem<Object> item) {
            item.setOutputMarkupId(true);
            final PrivStudyModel studyModel = (PrivStudyModel) item.getModelObject();
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
            item.add(new Label("datetime").add(new TooltipBehaviour("trash.study","DateTime")));
            item.add(new Label("id").add(new TooltipBehaviour("trash.study","Id")));
            item.add(new Label("accessionNumber").add(new TooltipBehaviour("trash.","accessionNumber")));
            item.add(new Label("modalities").add(new TooltipBehaviour("trash.","modalities")));
            item.add(new Label("description").add(new TooltipBehaviour("trash.study","Description")));
            item.add(new Label("numberOfSeries").add(new TooltipBehaviour("trash.study","NoS")));
            item.add(new Label("numberOfInstances").add(new TooltipBehaviour("trash.study","NoI")));
            item.add(new Label("availability").add(new TooltipBehaviour("trash.study","Availability")));
            item.add(new Label("pk").add(new TooltipBehaviour("trash.", "studyPk")));
            item.add(new AjaxFallbackLink<Object>("toggledetails") {

                private static final long serialVersionUID = 1L;

                @Override
                public void onClick(AjaxRequestTarget target) {
                    studyModel.setDetails(!studyModel.isDetails());
                    if (target != null) {
                        target.addComponent(patientListItem);
                    }
                }

            }.add(new Image("detailImg",ImageManager.IMAGE_DETAIL)
            .add(new ImageSizeBehaviour()))
            .add(new TooltipBehaviour("trash.","studyDetail")));
            item.add( new AjaxCheckBox("selected"){

                private static final long serialVersionUID = 1L;

                @Override
                protected void onUpdate(AjaxRequestTarget target) {
                    target.addComponent(this);
                }}.setOutputMarkupId(true).add(new TooltipBehaviour("trash.","studySelect")));
            WebMarkupContainer details = new WebMarkupContainer("details") {
                
                private static final long serialVersionUID = 1L;

                @Override
                public boolean isVisible() {
                    return studyModel.isDetails();
                }
            };
            item.add(details);
            details.add(new DicomObjectPanel("dicomobject", studyModel.getDataset(), false));
            item.add(new SeriesListView("series",
                    studyModel.getSeries(), patientListItem));
        }
    }
    private final class SeriesListView extends PropertyListView<Object> {

        private static final long serialVersionUID = 1L;
        
        private final ListItem<?> patientListItem;

        private SeriesListView(String id, List<PrivSeriesModel> list,
                ListItem<?> patientListItem) {
            super(id, list);
            this.patientListItem = patientListItem;
        }

        @Override
        protected void populateItem(final ListItem<Object> item) {
            item.setOutputMarkupId(true);
            final PrivSeriesModel seriesModel = (PrivSeriesModel) item.getModelObject();
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
            item.add(new Label("datetime").add(new TooltipBehaviour("trash.series","DateTime")));
            item.add(new Label("seriesNumber").add(new TooltipBehaviour("trash.","seriesNumber")));
            item.add(new Label("sourceAET").add(new TooltipBehaviour("trash.","sourceAET")));
            item.add(new Label("modality").add(new TooltipBehaviour("trash.series","Modality")));
            item.add(new Label("description").add(new TooltipBehaviour("trash.series","Description")));
            item.add(new Label("numberOfInstances").add(new TooltipBehaviour("trash.series","NoI")));
            item.add(new Label("availability").add(new TooltipBehaviour("trash.series","Availability")));
            item.add(new Label("pk").add(new TooltipBehaviour("trash.", "seriesPk")));
            item.add(new AjaxFallbackLink<Object>("toggledetails") {

                private static final long serialVersionUID = 1L;

                @Override
                public void onClick(AjaxRequestTarget target) {
                    seriesModel.setDetails(!seriesModel.isDetails());
                    if (target != null) {
                        target.addComponent(patientListItem);
                    }
                }

            }.add(new Image("detailImg",ImageManager.IMAGE_DETAIL)
            .add(new ImageSizeBehaviour()))
            .add(new TooltipBehaviour("trash.","seriesDetail")));
            item.add(new AjaxCheckBox("selected"){

                private static final long serialVersionUID = 1L;

                @Override
                protected void onUpdate(AjaxRequestTarget target) {
                    target.addComponent(this);
                }}.setOutputMarkupId(true).add(new TooltipBehaviour("trash.","seriesSelect")));
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

        private InstanceListView(String id, List<PrivInstanceModel> list,
                ListItem<?> patientListItem) {
            super(id, list);
            this.patientListItem = patientListItem;
        }

        @Override
        protected void populateItem(final ListItem<Object> item) {
            item.setOutputMarkupId(true);
            final PrivInstanceModel instModel = (PrivInstanceModel) item.getModelObject();
            item.add(new Label("datetime").add(new TooltipBehaviour("trash.instance","DateTime")));
            item.add(new Label("instanceNumber").add(new TooltipBehaviour("trash.","instanceNumber")));
            item.add(new Label("sopClassUID").add(new TooltipBehaviour("trash.","sopClassUID")));
            item.add(new Label("description").add(new TooltipBehaviour("trash.instance","Description")));
            item.add(new Label("availability").add(new TooltipBehaviour("trash.instance","Availability")));
            item.add(new Label("pk").add(new TooltipBehaviour("trash.", "instancePk")));
            item.add(new AjaxFallbackLink<Object>("toggledetails") {

                private static final long serialVersionUID = 1L;

                @Override
                public void onClick(AjaxRequestTarget target) {
                    instModel.setDetails(!instModel.isDetails());
                    if (target != null) {
                        target.addComponent(patientListItem);
                    }
                }

            }.add(new Image("detailImg",ImageManager.IMAGE_DETAIL)
            .add(new ImageSizeBehaviour()))
            .add(new TooltipBehaviour("trash.","instanceDetail")));
            item.add(new AjaxCheckBox("selected"){

                private static final long serialVersionUID = 1L;

                @Override
                protected void onUpdate(AjaxRequestTarget target) {
                    target.addComponent(this);
                }}.setOutputMarkupId(true).add(new TooltipBehaviour("trash.","instanceSelect")));
            WebMarkupContainer details = new WebMarkupContainer("details") {
                
                private static final long serialVersionUID = 1L;

                @Override
                public boolean isVisible() {
                    return instModel.isDetails();
                }
                
            };
            item.add(details);
            details.add(new DicomObjectPanel("dicomobject", instModel.getDataset(), false));
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
                    return model.isCollapsed() ? ImageManager.IMAGE_EXPAND : 
                        ImageManager.IMAGE_COLLAPSE;
                }
            })
            .add(new ImageSizeBehaviour()));
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
    
    private boolean removeTrashItems(PrivSelectedEntities selected) {
        try {
            TrashListLocal dao = (TrashListLocal) JNDIUtils.lookup(TrashListLocal.JNDI_NAME);
            
            List<Long> pks = new ArrayList<Long>();
            for (PrivInstanceModel instanceModel : selected.getInstances())
                pks.add(instanceModel.getPk());
            dao.removeTrashInstances(pks);

            pks = new ArrayList<Long>();
            for (PrivSeriesModel seriesModel : selected.getSeries())
                pks.add(seriesModel.getPk());
            dao.removeTrashSeries(pks);
            
            pks = new ArrayList<Long>();
            for (PrivStudyModel studyModel : selected.getStudies())
                pks.add(studyModel.getPk());
            dao.removeTrashStudies(pks);

            pks = new ArrayList<Long>();
            for (PrivPatientModel patientModel : selected.getPatients())
                pks.add(patientModel.getPk());
            dao.removeTrashPatients(pks);               
        } catch (Exception x) {
            String msg = "Delete failed! Reason:"+x.getMessage();
            log.error(msg,x);
            return false;
        }
        return true;
    }
}
