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
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.Application;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.PageMap;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.ajax.AbstractAjaxTimerBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.ajax.markup.html.form.AjaxCheckBox;
import org.apache.wicket.authorization.strategies.role.metadata.MetaDataRoleAuthorizationStrategy;
import org.apache.wicket.datetime.markup.html.form.DateTextField;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.extensions.yui.calendar.DateTimeField;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.CSSPackageResource;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.link.PopupSettings;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.PropertyListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.util.time.Duration;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;
import org.dcm4che2.data.BasicDicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4chee.archive.entity.Patient;
import org.dcm4chee.archive.entity.Study;
import org.dcm4chee.archive.util.JNDIUtils;
import org.dcm4chee.icons.ImageManager;
import org.dcm4chee.icons.behaviours.ImageSizeBehaviour;
import org.dcm4chee.web.common.behaviours.TooltipBehaviour;
import org.dcm4chee.web.common.exceptions.SelectionException;
import org.dcm4chee.web.common.markup.BaseForm;
import org.dcm4chee.web.common.markup.DateTimeLabel;
import org.dcm4chee.web.common.markup.PopupLink;
import org.dcm4chee.web.common.markup.modal.ConfirmationWindow;
import org.dcm4chee.web.common.markup.modal.MessageWindow;
import org.dcm4chee.web.common.validators.UIDValidator;
import org.dcm4chee.web.common.webview.link.WebviewerLinkProvider;
import org.dcm4chee.web.dao.folder.StudyListFilter;
import org.dcm4chee.web.dao.folder.StudyListLocal;
import org.dcm4chee.web.dao.util.QueryUtil;
import org.dcm4chee.web.war.WicketSession;
import org.dcm4chee.web.war.common.EditDicomObjectPage;
import org.dcm4chee.web.war.common.SimpleEditDicomObjectPage;
import org.dcm4chee.web.war.common.model.AbstractDicomModel;
import org.dcm4chee.web.war.common.model.AbstractEditableDicomModel;
import org.dcm4chee.web.war.folder.model.FileModel;
import org.dcm4chee.web.war.folder.model.InstanceModel;
import org.dcm4chee.web.war.folder.model.PPSModel;
import org.dcm4chee.web.war.folder.model.PatientModel;
import org.dcm4chee.web.war.folder.model.SeriesModel;
import org.dcm4chee.web.war.folder.model.StudyModel;
import org.dcm4chee.web.war.trash.TrashListPage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StudyListPage extends Panel {

    private static final String MODULE_NAME = "folder";
    private static final long serialVersionUID = 1L;
    private static int PAGESIZE = 10;
    private static Logger log = LoggerFactory.getLogger(StudyListPage.class);
    private ViewPort viewport = ((WicketSession) getSession()).getFolderViewPort();
    private StudyListHeader header = new StudyListHeader("thead");
    private SelectedEntities selected = new SelectedEntities();

    private IModel<Boolean> latestStudyFirst = new AbstractReadOnlyModel<Boolean>() {
        private static final long serialVersionUID = 1L;
        @Override
        public Boolean getObject() {
            return viewport.getFilter().isLatestStudiesFirst();
        }
    };
    private static List<String> sourceAETs = new ArrayList<String>();
    private static List<String> modalities = new ArrayList<String>();
    private boolean showSearch = true;
    private boolean notSearched = true;
    private BaseForm form;
    private TooltipBehaviour tooltipBehaviour = new TooltipBehaviour("folder.");
    private MessageWindow msgWin = new MessageWindow("msgWin");
    private Mpps2MwlLinkPage linkPage = new Mpps2MwlLinkPage("linkPage");
    private ConfirmationWindow<PPSModel> confirmUnlinkMpps;
    
    private WebviewerLinkProvider webviewerLinkProvider;
    
    public StudyListPage(final String id) {
        super(id);
        webviewerLinkProvider = new WebviewerLinkProvider(((WebApplication)Application.get()).getInitParameter("webviewerName"));
        add(CSSPackageResource.getHeaderContribution(StudyListPage.class, "folder-style.css"));

        final WebMarkupContainer searchHeader = new WebMarkupContainer("searchHeader");
//        searchHeader.setOutputMarkupId(true);
//        AjaxFallbackLink<?> link = new AjaxFallbackLink<Object>("search") {
//
//            private static final long serialVersionUID = 1L;
//
//            @Override
//            public void onClick(AjaxRequestTarget target) {
//                showSearch = !showSearch;
//                target.addComponent(searchHeader);
//                target.addComponent(form);
//            }
//        };
//        link.add(new Image("searchImg", new AbstractReadOnlyModel<ResourceReference>() {
//
//                private static final long serialVersionUID = 1L;
//
//                @Override
//                public ResourceReference getObject() {
//                    return showSearch ? ImageManager.IMAGE_COMMON_COLLAPSE : 
//                        ImageManager.IMAGE_COMMON_EXPAND;
//                }
//        })
//        .add(new ImageSizeBehaviour()));
//        searchHeader.add(link);
//        searchHeader.add(new Label("searchText", new AbstractReadOnlyModel<String>() {
//
//            private static final long serialVersionUID = 1L;
//
//            @Override
//            public String getObject() {
//                return showSearch ? new ResourceModel("folder.search.hide.text").wrapOnAssignment(searchHeader).getObject()
//                        : new ResourceModel("folder.search.show.text").wrapOnAssignment(searchHeader).getObject();
//            }
//        }));
        add(searchHeader);

        final StudyListFilter filter = viewport.getFilter();
        form = new BaseForm("form", new CompoundPropertyModel<Object>(filter)) {

            private static final long serialVersionUID = 1L;
            
            @Override
            public boolean isVisible() {
                return showSearch;
            }
        };
        form.setResourceIdPrefix("folder.");
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
        add(linkPage);
        initModalitiesAndSourceAETs();
    }

    private void addQueryFields(final StudyListFilter filter, final BaseForm form) {
        IModel<Boolean> enabledModel = new AbstractReadOnlyModel<Boolean>(){

            private static final long serialVersionUID = 1L;

            @Override
            public Boolean getObject() {
                return (!filter.isExtendedQuery() || QueryUtil.isUniversalMatch(filter.getStudyInstanceUID())) &&
                       (!filter.isExtendedQuery() || QueryUtil.isUniversalMatch(filter.getSeriesInstanceUID()));
            }
            
        };
        form.addLabeledTextField("patientName", enabledModel);
        form.addLabel("patientIDDescr");
        form.addLabeledTextField("patientID", enabledModel);
        form.addLabeledTextField("issuerOfPatientID", enabledModel);
        
        form.addLabel("studyDate");
        form.addLabeledDateTimeField("studyDateMin", new PropertyModel<Date>(filter, "studyDateMin"), enabledModel, false);
        form.addLabeledDateTimeField("studyDateMax", new PropertyModel<Date>(filter, "studyDateMax"), enabledModel, true);

        form.addLabeledTextField("accessionNumber", enabledModel);
        form.addLabeledDropDownChoice("modality", null, modalities, enabledModel);
        List<String> choices = viewport.getSourceAetChoices(sourceAETs);
        if (choices.size() > 0)
            filter.setSourceAET(choices.get(0));
        form.addLabeledDropDownChoice("sourceAET", null, choices, enabledModel);

        final WebMarkupContainer extendedFilter = new WebMarkupContainer("extendedFilter") {

            private static final long serialVersionUID = 1L;

            @Override
            public boolean isVisible() {
                return filter.isExtendedQuery();
            }
        };
        extendedFilter.add( new Label("birthDateLabel", new ResourceModel("folder.birthDate")));
        extendedFilter.add( new Label("birthDateMinLabel", new ResourceModel("folder.birthDateMin")));
        extendedFilter.add( new Label("birthDateMaxLabel", new ResourceModel("folder.birthDateMax")));
        extendedFilter.add(form.getDateTextField("birthDateMin", null, true, enabledModel));
        extendedFilter.add(form.getDateTextField("birthDateMax", null, true, enabledModel));
        
        extendedFilter.add( new Label("studyInstanceUIDLabel", new ResourceModel("folder.studyInstanceUID")));
        extendedFilter.add( new TextField<String>("studyInstanceUID").add(new UIDValidator()));

        extendedFilter.add( new Label("seriesInstanceUIDLabel", new ResourceModel("folder.seriesInstanceUID")));
        extendedFilter.add( new TextField<String>("seriesInstanceUID") {

            private static final long serialVersionUID = 1L;

            @Override
            public boolean isEnabled() {
                return !filter.isExtendedQuery() || QueryUtil.isUniversalMatch(filter.getStudyInstanceUID());
            }
        });
        form.add(extendedFilter);
        
        AjaxFallbackLink<?> link = new AjaxFallbackLink<Object>("showExtendedFilter") {

            private static final long serialVersionUID = 1L;

            @Override
            public void onClick(AjaxRequestTarget target) {
                filter.setExtendedQuery(!filter.isExtendedQuery());
                target.addComponent(form);
            }
        };
        link.add(new Image("showExtendedFilterImg", new AbstractReadOnlyModel<ResourceReference>() {

                private static final long serialVersionUID = 1L;

                @Override
                public ResourceReference getObject() {
                    return filter.isExtendedQuery() ? ImageManager.IMAGE_COMMON_COLLAPSE : 
                        ImageManager.IMAGE_COMMON_EXPAND;
                }
        })
        .add(new ImageSizeBehaviour()));
        form.add(link);
        form.add(new Label("showExtendedFilterText", new ResourceModel("folder.showExtendedFilter.text")));
    }

    private void addQueryOptions(BaseForm form) {
        form.addLabeledCheckBox("patientsWithoutStudies", null);
        form.addLabeledCheckBox("latestStudiesFirst", null);
        form.addLabeledCheckBox("ppsWithoutMwl", null);
    }

    @SuppressWarnings("unchecked")
    private void addNavigation(BaseForm form) {
        
        form.add(new AjaxButton("reset", new ResourceModel("folder.reset")) {
            
            private static final long serialVersionUID = 1L;

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                viewport.clear();
                form.clearInput();
                form.setOutputMarkupId(true);
                target.addComponent(form);
            }
        }.setDefaultFormProcessing(false));
        
        Button searchBtn = new Button("search", new ResourceModel("folder.search")) {

            private static final long serialVersionUID = 1L;

            @Override
            public void onSubmit() {
                viewport.setOffset(0);
                queryStudies();
            }};
        form.add(searchBtn);
        form.setDefaultButton(searchBtn);
        form.add(new Link("prev") {

            private static final long serialVersionUID = 1L;

            @Override
            protected void onComponentTag(ComponentTag tag) {
                super.onComponentTag(tag);
                if (viewport.getOffset() == 0) 
                    disableLink(tag);
            }

            @Override
            public void onClick() {
                viewport.setOffset(Math.max(0, viewport.getOffset() - PAGESIZE));
                queryStudies();               
            }
            
            @Override
            public boolean isVisible() {
                return !notSearched;
            }
        }
        .add(new Image("prevImg", ImageManager.IMAGE_COMMON_BACK)
        .add(new ImageSizeBehaviour("vertical-align: middle;"))
        .add(new TooltipBehaviour("folder.")))
        );
 
        form.add(new Link("next") {

            private static final long serialVersionUID = 1L;

            @Override
            protected void onComponentTag(ComponentTag tag) {
                super.onComponentTag(tag);
                if (viewport.getTotal() - viewport.getOffset() <= PAGESIZE)
                    disableLink(tag);
            }

            @Override
            public void onClick() {
                viewport.setOffset(viewport.getOffset() + PAGESIZE);
                queryStudies();
            }

            @Override
            public boolean isVisible() {
                return !notSearched;
            }
        }
        .add(new Image("nextImg", ImageManager.IMAGE_COMMON_FORWARD)
        .add(new ImageSizeBehaviour("vertical-align: middle;"))
        .add(new TooltipBehaviour("folder.")))
        .setVisible(!notSearched)
        );

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

    private void addActions(final BaseForm form) {
        
        final ConfirmationWindow<SelectedEntities> confirmDelete = new ConfirmationWindow<SelectedEntities>("confirmDelete") {

            private static final long serialVersionUID = 1L;

            @Override
            public void onOk(AjaxRequestTarget target) {
                target.addComponent(form);
            }

            @Override
            public void onConfirmation(AjaxRequestTarget target, final SelectedEntities selected) {

                this.setStatus(new StringResourceModel("folder.delete.running", StudyListPage.this, null));
                okBtn.setVisible(false);
                ajaxRunning = true;

                msgLabel.add(new AbstractAjaxTimerBehavior(Duration.milliseconds(1)) {
                    
                    private static final long serialVersionUID = 1L;

                    @Override
                    protected void onTimer(AjaxRequestTarget target) {
                        if (ContentEditDelegate.getInstance().moveToTrash(selected)) {
                            setStatus(new StringResourceModel("folder.deleteDone", StudyListPage.this,null));
                            if (selected.hasPatients()) {
                                viewport.getPatients().clear();
                                queryStudies();
                            } else
                                selected.refreshView(true);
                        } else
                            setStatus(new StringResourceModel("folder.deleteFailed", StudyListPage.this,null));
                        this.stop();
                        ajaxRunning = false;
                        okBtn.setVisible(true);
                        
                        target.addComponent(msgLabel);
                        target.addComponent(hourglassImage);
                        target.addComponent(okBtn);
                    }
                });
            }
            
            @Override
            public void onDecline(AjaxRequestTarget target, SelectedEntities selected) {
                if (selected.getPpss().size() != 0) {
                    if (ContentEditDelegate.getInstance().deletePps(selected)) {
                        this.setStatus(new StringResourceModel("folder.deleteDone", StudyListPage.this,null));
                        selected.refreshView(true);
                    } else {
                        this.setStatus(new StringResourceModel("folder.deleteFailed", StudyListPage.this,null));
                    }
                }
            }
        };
        confirmDelete.setInitialHeight(150);
        form.add(confirmDelete);
        
        AjaxButton deleteBtn = new AjaxButton("deleteBtn") {
                    
            private static final long serialVersionUID = 1L;
            
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                selected.update(viewport.getPatients());
                selected.deselectChildsOfSelectedEntities();
                if (selected.hasPPS()) {
                    confirmDelete.confirmWithCancel(target, new StringResourceModel("folder.confirmPpsDelete",this, null,new Object[]{selected}), selected);
                } else if (selected.hasDicomSelection()) {
                    confirmDelete.confirm(target, new StringResourceModel("folder.confirmDelete",this, null,new Object[]{selected}), selected);
                } else {
                    msgWin.show(target, getString("folder.noSelection"));
                }
            }
        };
        deleteBtn.add(new Image("deleteImg", ImageManager.IMAGE_FOLDER_DELETE)
            .add(new ImageSizeBehaviour("vertical-align: middle;"))
        );
        deleteBtn.add(new TooltipBehaviour("folder.", "deleteBtn"));
        deleteBtn.add(new Label("deleteText", new ResourceModel("folder.deleteBtn.text"))
            .add(new AttributeModifier("style", true, new Model<String>("vertical-align: middle")))
        );
        form.add(deleteBtn);
        
        final ConfirmationWindow<SelectedEntities> confirmMove = new ConfirmationWindow<SelectedEntities>("confirmMove"){

            private static final long serialVersionUID = 1L;

            @Override
            public void onOk(AjaxRequestTarget target) {
                target.addComponent(form);
            }

            @Override
            public void onConfirmation(AjaxRequestTarget target, final SelectedEntities selected) {
                
                this.setStatus(new StringResourceModel("folder.move.running", StudyListPage.this, null));
                okBtn.setVisible(false);
                ajaxRunning = true;
                
                msgLabel.add(new AbstractAjaxTimerBehavior(Duration.milliseconds(1)) {
                    
                    private static final long serialVersionUID = 1L;

                    @Override
                    protected void onTimer(AjaxRequestTarget target) {
                        try {
                            int nrOfMovedInstances = ContentEditDelegate.getInstance().moveEntities(selected);
                            if (nrOfMovedInstances != -1) {
                                setStatus(new StringResourceModel("folder.moveDone", StudyListPage.this,null));
                                viewport.getPatients().clear();
                            } else
                                setStatus(new StringResourceModel("folder.moveFailed", StudyListPage.this,null));
                        } catch (SelectionException x) {
                            log.warn(x.getMessage());
                            setStatus(new StringResourceModel(x.getMsgId(), StudyListPage.this,null));
                        }
                        queryStudies();
                        this.stop();
                        ajaxRunning = false;
                        okBtn.setVisible(true);
                        
                        target.addComponent(msgLabel);
                        target.addComponent(hourglassImage);
                        target.addComponent(okBtn);
                    }
                });
            }
        };
        confirmMove.setInitialHeight(150);
        form.add(confirmMove);
        
        AjaxButton moveBtn = new AjaxButton("moveBtn") {
            
            private static final long serialVersionUID = 1L;
            
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                selected.update(viewport.getPatients());
                selected.deselectChildsOfSelectedEntities();
                log.info("Selected Entities:"+selected);
                if (selected.hasDicomSelection())
                    confirmMove.confirm(target, new StringResourceModel("folder.confirmMove",this, null,new Object[]{selected}), selected);
                else
                    msgWin.show(target, getString("folder.noSelection"));
            }
        };
        moveBtn.add(new Image("moveImg",ImageManager.IMAGE_FOLDER_MOVE)
            .add(new ImageSizeBehaviour("vertical-align: middle;"))
        );
        moveBtn.add(new TooltipBehaviour("folder.", "moveBtn"));
        moveBtn.add(new Label("moveText", new ResourceModel("folder.moveBtn.text"))
            .add(new AttributeModifier("style", true, new Model<String>("vertical-align: middle")))
        );
        form.add(moveBtn);
        
        PopupLink exportBtn = new PopupLink("exportBtn", "exportPage") {

            private static final long serialVersionUID = 1L;

            @Override
            public void onClick() {
                ExportPage page = new ExportPage(viewport.getPatients());
                SelectedEntities.deselectAll(viewport.getPatients());
                this.setResponsePage(page);
            }
        };
        exportBtn.setPopupHeight(new Integer(new ResourceModel("folder.exportpage.window.height","500").wrapOnAssignment(this).getObject().toString()));
        exportBtn.setPopupWidth(new Integer(new ResourceModel("folder.exportpage.window.width","650").wrapOnAssignment(this).getObject().toString()));

        exportBtn.add(new Image("exportImg",ImageManager.IMAGE_FOLDER_EXPORT)
            .add(new ImageSizeBehaviour("vertical-align: middle;"))
        );
        exportBtn.add(new TooltipBehaviour("folder.", "exportBtn"));
        exportBtn.add(new Label("exportText", new ResourceModel("folder.exportBtn.text"))
            .add(new AttributeModifier("style", true, new Model<String>("vertical-align: middle")))
        );
        form.add(exportBtn);

        confirmUnlinkMpps = new ConfirmationWindow<PPSModel>("confirmUnlink") {
            
            private static final long serialVersionUID = 1L;

            @Override
            public void onOk(AjaxRequestTarget target) {
                target.addComponent(form);
            }

            @Override
            public void onConfirmation(AjaxRequestTarget target, final PPSModel ppsModel) {
                
                this.setStatus(new StringResourceModel("folder.unlink.running", StudyListPage.this, null));
                okBtn.setVisible(false);
                ajaxRunning = true;
                
                msgLabel.add(new AbstractAjaxTimerBehavior(Duration.milliseconds(1)) {
                    
                    private static final long serialVersionUID = 1L;

                    @Override
                    protected void onTimer(AjaxRequestTarget target) {
                        try {
                            if (ContentEditDelegate.getInstance().unlink(ppsModel))
                                setStatus(new StringResourceModel("folder.unlinkDone", StudyListPage.this,null));
                        } catch (Exception x) {
                            log.error("Unlink of MPPS failed:"+ppsModel, x);
                            setStatus(new StringResourceModel("folder.unlinkFailed", StudyListPage.this,null));
                        }
                        queryStudies();
                        this.stop();
                        ajaxRunning = false;
                        okBtn.setVisible(true);
                        
                        target.addComponent(msgLabel);
                        target.addComponent(hourglassImage);
                        target.addComponent(okBtn);
                    }
                });
            }
        };
        form.add(confirmUnlinkMpps);
    }

    private void initModalitiesAndSourceAETs() {
        if (modalities.isEmpty() || sourceAETs.isEmpty()) {
            StudyListLocal dao = (StudyListLocal)
                    JNDIUtils.lookup(StudyListLocal.JNDI_NAME);
            modalities.clear();
            modalities.add("*");
            modalities.addAll(dao.selectDistinctModalities());
            sourceAETs.clear();
            sourceAETs.addAll(dao.selectDistinctSourceAETs());
        }
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
        studies.add(new StudyModel(study, patient));
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
            DateTimeLabel dtl = new DateTimeLabel("birthdate").setWithoutTime(true);
            dtl.add(tooltipBehaviour.newWithSubstitution(new PropertyModel<String>(dtl, "textFormat")));
            item.add(dtl);
            item.add(new Label("sex").add(tooltipBehaviour));
            item.add(new Label("comments").add(tooltipBehaviour));
            item.add(new AjaxFallbackLink<Object>("toggledetails") {

                private static final long serialVersionUID = 1L;

                @Override
                public void onClick(AjaxRequestTarget target) {
                    patModel.setDetails(!patModel.isDetails());
                    if (target != null) {
                        target.addComponent(item);
                    }
                }

            }.add(new Image("detailImg",ImageManager.IMAGE_COMMON_DICOM_DETAILS)
            .add(new ImageSizeBehaviour())
            .add(new TooltipBehaviour("folder.","patDetail"))));
            item.add(getEditLink(patModel, "patEdit"));
            item.add(getAddStudyLink(patModel, "addStudy"));
            item.add( new ExternalLink("webview", webviewerLinkProvider.getUrlForPatient(patModel.getId(), patModel.getIssuer())) {
                private static final long serialVersionUID = 1L;
                @Override
                public boolean isVisible() {
                    return webviewerLinkProvider.supportPatientLevel();
                }
            }
            .setPopupSettings(new PopupSettings(PageMap.forName("webviewPage"), 
                    PopupSettings.RESIZABLE|PopupSettings.SCROLLBARS))
            .add(new Image("webviewImg",ImageManager.IMAGE_FOLDER_VIEWER).add(new ImageSizeBehaviour())
            .add(new TooltipBehaviour("folder.","patWebviewer"))));
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
            details.add(new DicomObjectPanel("dicomobject", patModel, false));
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
            item.add(new DateTimeLabel("datetime").add(new TooltipBehaviour("folder.study","DateTime")));
            item.add(new Label("id").add(new TooltipBehaviour("folder.study","Id")));
            item.add(new Label("accessionNumber").add(new TooltipBehaviour("folder.","accessionNumber")));
            item.add(new Label("modalities").add(new TooltipBehaviour("folder.","modalities")));
            item.add(new Label("description").add(new TooltipBehaviour("folder.study","Description")));
            item.add(new Label("numberOfSeries").add(new TooltipBehaviour("folder.study","NoS")));
            item.add(new Label("numberOfInstances").add(new TooltipBehaviour("folder.study","NoI")));
            item.add(new Label("availability").add(new TooltipBehaviour("folder.study","Availability")));
            item.add(new AjaxFallbackLink<Object>("toggledetails") {

                private static final long serialVersionUID = 1L;

                @Override
                public void onClick(AjaxRequestTarget target) {
                    studyModel.setDetails(!studyModel.isDetails());
                    if (target != null) {
                        target.addComponent(patientListItem);
                    }
                }

            }.add(new Image("detailImg",ImageManager.IMAGE_COMMON_DICOM_DETAILS)
            .add(new ImageSizeBehaviour())
            .add(new TooltipBehaviour("folder.","studyDetail"))));
            item.add( getEditLink(studyModel, "studyEdit"));
            item.add(getAddSeriesLink(studyModel, "addSeries"));
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
            item.add( new ExternalLink("webview", webviewerLinkProvider.getUrlForStudy(studyModel.getStudyInstanceUID())) {
                private static final long serialVersionUID = 1L;
                @Override
                public boolean isVisible() {
                    return webviewerLinkProvider.supportStudyLevel();
                }
            }
            .setPopupSettings(new PopupSettings(PageMap.forName("webviewPage"), 
                    PopupSettings.RESIZABLE|PopupSettings.SCROLLBARS))
            .add(new Image("webviewImg",ImageManager.IMAGE_FOLDER_VIEWER).add(new ImageSizeBehaviour())
            .add(new TooltipBehaviour("folder.","studyWebviewer"))));
            item.add(details);
            details.add(new DicomObjectPanel("dicomobject", studyModel, false));
            item.add(new PPSListView("ppss",
                    studyModel.getPPSs(), patientListItem));
        }
    }

    private final class PPSListView extends PropertyListView<Object> {

        private static final long serialVersionUID = 1L;
        
        private final ListItem<?> ppsListItem;

        private PPSListView(String id, List<PPSModel> list,
                ListItem<?> patientListItem) {
            super(id, list);
            this.ppsListItem = patientListItem;
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
            cell.add(new ExpandCollapseLink("expand", ppsModel, ppsListItem){

                private static final long serialVersionUID = 1L;

                @Override
                public boolean isVisible() {
                    return ppsModel.getUid() != null;
                }
            });
            item.add(cell);
            item.add(new DateTimeLabel("datetime").add(new TooltipBehaviour("folder.pps","DateTime")));
            item.add(new Label("id").add(new TooltipBehaviour("folder.pps","Id")));
            item.add(new Label("spsid").add(new TooltipBehaviour("folder.","spsid")));
            item.add(new Label("modality").add(new TooltipBehaviour("folder.pps","Modality")));
            item.add(new Label("description").add(new TooltipBehaviour("folder.pps","Description")));
            item.add(new Label("numberOfSeries").add(new TooltipBehaviour("folder.pps","NoS")));
            item.add(new Label("numberOfInstances").add(new TooltipBehaviour("folder.pps","NoI")));
            item.add(new Label("status").add(new TooltipBehaviour("folder.pps","Status")));
            item.add( new Image("linkStatus", new AbstractReadOnlyModel<ResourceReference>() {
                private static final long serialVersionUID = 1L;
                @Override
                public ResourceReference getObject() {
                    return ppsModel.getAccessionNumber() == null ? ImageManager.IMAGE_FOLDER_UNLINK : 
                        ImageManager.IMAGE_COMMON_LINK;
                }
            }) {
                private static final long serialVersionUID = 1L;
                @Override
                public boolean isVisible() {
                    return ppsModel.getDataset() != null;
                }
            }
            .add(new ImageSizeBehaviour()));

            item.add(new AjaxFallbackLink<Object>("toggledetails") {

                private static final long serialVersionUID = 1L;

                @Override
                public void onClick(AjaxRequestTarget target) {
                    ppsModel.setDetails(!ppsModel.isDetails());
                    if (target != null) {
                        target.addComponent(StudyListPage.this.get("form"));
                    }
                }

                @Override
                public boolean isVisible() {
                    return ppsModel.getDataset() != null;
                }
            }.add(new Image("detailImg",ImageManager.IMAGE_COMMON_DICOM_DETAILS)
            .add(new ImageSizeBehaviour())
            .add(new TooltipBehaviour("folder.","ppsDetail"))));
            item.add(getEditLink(ppsModel, "ppsEdit"));
            
            AjaxFallbackLink<?> linkBtn = new AjaxFallbackLink<Object>("linkBtn") {
                
                private static final long serialVersionUID = 1L;

                @Override
                public void onClick(AjaxRequestTarget target) {

                    linkPage.setWindowClosedCallback(new ModalWindow.WindowClosedCallback() {              
                        
                        private static final long serialVersionUID = 1L;

                        @Override
                        public void onClose(AjaxRequestTarget target) {
                            linkPage.setOutputMarkupId(true);
                            target.addComponent(linkPage);
                        }
                    });
                    
                    ((Mpps2MwlLinkPage) linkPage
                            .setInitialWidth(new Integer(getString("folder.mpps2mwl.window.width")))
                            .setInitialHeight(new Integer(getString("folder.mpps2mwl.window.height")))
                    )
                    .show(target, ppsModel, form);
                }

                @Override
                public boolean isVisible() {
                    return ppsModel.getDataset() != null && ppsModel.getAccessionNumber()==null;
                }
            };
            linkBtn.add(new Image("linkImg", ImageManager.IMAGE_COMMON_LINK)
            .add(new ImageSizeBehaviour())
            .add(new TooltipBehaviour("folder.","ppsLink")));
            item.add(linkBtn);

            item.add(new AjaxFallbackLink<Object>("unlinkBtn") {

                private static final long serialVersionUID = 1L;

                @Override
                public void onClick(AjaxRequestTarget target) {
                    confirmUnlinkMpps.confirm(target, new StringResourceModel("folder.confirmUnlink",this, null,new Object[]{ppsModel}), ppsModel);
                }

                @Override
                public boolean isVisible() {
                    return ppsModel.getDataset() != null && ppsModel.getAccessionNumber()!=null;
                }
            }.add(new Image("unlinkImg",ImageManager.IMAGE_FOLDER_UNLINK)
            .add(new ImageSizeBehaviour())));
            
            item.add(new AjaxCheckBox("selected"){
                private static final long serialVersionUID = 1L;

                @Override
                public boolean isVisible() {
                    return ppsModel.getDataset() != null;
                }
                
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
            details.add(new DicomObjectPanel("dicomobject", ppsModel, false));
            item.add(new SeriesListView("series",
                    ppsModel.getSeries(), ppsListItem));
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
            item.add(new DateTimeLabel("datetime").add(new TooltipBehaviour("folder.series","DateTime")));
            item.add(new Label("seriesNumber").add(new TooltipBehaviour("folder.","seriesNumber")));
            item.add(new Label("sourceAET").add(new TooltipBehaviour("folder.","sourceAET")));
            item.add(new Label("modality").add(new TooltipBehaviour("folder.series","Modality")));
            item.add(new Label("description").add(new TooltipBehaviour("folder.series","Description")));
            item.add(new Label("numberOfInstances").add(new TooltipBehaviour("folder.series","NoI")));
            item.add(new Label("availability").add(new TooltipBehaviour("folder.series","Availability")));
            item.add(new AjaxFallbackLink<Object>("toggledetails") {

                private static final long serialVersionUID = 1L;

                @Override
                public void onClick(AjaxRequestTarget target) {
                    seriesModel.setDetails(!seriesModel.isDetails());
                    if (target != null) {
                        target.addComponent(patientListItem);
                    }
                }

            }.add(new Image("detailImg",ImageManager.IMAGE_COMMON_DICOM_DETAILS)
            .add(new ImageSizeBehaviour())
            .add(new TooltipBehaviour("folder.","seriesDetail"))));
            item.add(getEditLink(seriesModel, "seriesEdit"));
            item.add(new AjaxCheckBox("selected"){

                private static final long serialVersionUID = 1L;

                @Override
                protected void onUpdate(AjaxRequestTarget target) {
                    target.addComponent(this);
                }}.setOutputMarkupId(true).add(new TooltipBehaviour("folder.","seriesSelect")));
            final WebMarkupContainer details = new WebMarkupContainer("details") {
                
                private static final long serialVersionUID = 1L;

                @Override
                public boolean isVisible() {
                    return seriesModel.isDetails();
                }
                
            };
            item.add( new ExternalLink("webview", webviewerLinkProvider.getUrlForSeries(seriesModel.getSeriesInstanceUID())) {
                private static final long serialVersionUID = 1L;
                @Override
                public boolean isVisible() {
                    return webviewerLinkProvider.supportSeriesLevel();
                }
            }
            .setPopupSettings(new PopupSettings(PageMap.forName("webviewPage"), 
                    PopupSettings.RESIZABLE|PopupSettings.SCROLLBARS))
            .add(new Image("webviewImg",ImageManager.IMAGE_FOLDER_VIEWER).add(new ImageSizeBehaviour())
            .add(new TooltipBehaviour("folder.","seriesWebviewer"))));
            item.add(details);
            details.add(new DicomObjectPanel("dicomobject", seriesModel, false));
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
            item.add(new DateTimeLabel("datetime").add(new TooltipBehaviour("folder.instance","DateTime")));
            item.add(new Label("instanceNumber").add(new TooltipBehaviour("folder.","instanceNumber")));
            item.add(new Label("sopClassUID").add(new TooltipBehaviour("folder.","sopClassUID")));
            item.add(new Label("description").add(new TooltipBehaviour("folder.instance","Description")));
            item.add(new Label("availability").add(new TooltipBehaviour("folder.instance","Availability")));
            item.add(new AjaxFallbackLink<Object>("toggledetails") {

                private static final long serialVersionUID = 1L;

                @Override
                public void onClick(AjaxRequestTarget target) {
                    instModel.setDetails(!instModel.isDetails());
                    if (target != null) {
                        target.addComponent(patientListItem);
                    }
                }

            }.add(new Image("detailImg",ImageManager.IMAGE_COMMON_DICOM_DETAILS)
            .add(new ImageSizeBehaviour())
            .add(new TooltipBehaviour("folder.","instanceDetail"))));
            item.add(getEditLink(instModel, "instanceEdit"));
            item.add( new ExternalLink("webview", webviewerLinkProvider.getUrlForInstance(instModel.getSOPInstanceUID())) {
                private static final long serialVersionUID = 1L;
                @Override
                public boolean isVisible() {
                    return webviewerLinkProvider.supportInstanceLevel();
                }
            }
            .setPopupSettings(new PopupSettings(PageMap.forName("webviewPage"), 
                    PopupSettings.RESIZABLE|PopupSettings.SCROLLBARS))
            .add(new Image("webviewImg",ImageManager.IMAGE_FOLDER_VIEWER).add(new ImageSizeBehaviour())
            .add(new TooltipBehaviour("folder.","instanceWebviewer"))));
            item.add(new ExternalLink("wado", WADODelegate.getInstance().getURL(instModel)){

                private static final long serialVersionUID = 1L;

                @Override
                public boolean isVisible() {
                    return WADODelegate.getInstance().getRenderType(instModel.getSopClassUID()) != WADODelegate.NOT_RENDERABLE;
                }
                
            }.add(new Image("wadoImg",ImageManager.IMAGE_FOLDER_WADO).add(new ImageSizeBehaviour())
                    .add(new TooltipBehaviour("folder.","wado"))));
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
            details.add(new DicomObjectPanel("dicomobject", instModel, false));
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
            item.add(new AjaxFallbackLink<Object>("toggledetails") {

                private static final long serialVersionUID = 1L;

                @Override
                public void onClick(AjaxRequestTarget target) {
                    fileModel.setDetails(!fileModel.isDetails());
                    if (target != null) {
                        target.addComponent(patientListItem);
                    }
                }

            }.add(new Image("detailImg",ImageManager.IMAGE_COMMON_DICOM_DETAILS)
            .add(new ImageSizeBehaviour())
            .add(new TooltipBehaviour("folder.","fileDetail"))));
            item.add(new AjaxCheckBox("selected"){
                private static final long serialVersionUID = 1L;

                @Override
                public boolean isVisible() {
                    return false;//no action on file level at the moment
                }
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
    
    private Link<Object> getEditLink(final AbstractEditableDicomModel model, String tooltipId) {
        Link<Object> link = new Link<Object>("edit") {
            
            private static final long serialVersionUID = 1L;

            @Override
            public void onClick() {
                setResponsePage(
                        new EditDicomObjectPage(StudyListPage.this.getPage(), model));
            }
            @Override
            public boolean isVisible() {
                return model.getDataset() != null;
            }
        };
        link.add(new Image("editImg",ImageManager.IMAGE_COMMON_DICOM_EDIT).add(new ImageSizeBehaviour())
        .add(new TooltipBehaviour("folder.", tooltipId)));
        MetaDataRoleAuthorizationStrategy.authorize(link, RENDER, "WebAdmin");
        return link;
    }

    private Link<Object> getAddStudyLink(final PatientModel model, String tooltipId) {
        Link<Object> link = new Link<Object>("add") {
            
            private static final long serialVersionUID = 1L;

            @Override
            public void onClick() {
                StudyModel newStudyModel = new StudyModel(null, model);
                setResponsePage(
                        new SimpleEditDicomObjectPage(StudyListPage.this.getPage(), new ResourceModel("folder.addStudy", "Add Study"),
                                newStudyModel, new int[][]{{Tag.StudyInstanceUID},
                                                    {Tag.StudyID},
                                                    {Tag.AccessionNumber},
                                                    {Tag.StudyDate, Tag.StudyTime}}, model));
            }
            @Override
            public boolean isVisible() {
                return model.getDataset() != null;
            }
        };
        link.add(new Image("addImg",ImageManager.IMAGE_COMMON_ADD).add(new ImageSizeBehaviour())
        .add(new TooltipBehaviour("folder.", tooltipId)));
        return link;
    }

    private Link<Object> getAddSeriesLink(final StudyModel model, String tooltipId) {
        Link<Object> link = new Link<Object>("add") {
            
            private static final long serialVersionUID = 1L;

            @Override
            public void onClick() {
                SeriesModel newSeriesModel = new SeriesModel(null, null);
                setResponsePage(
                        new SimpleEditDicomObjectPage(StudyListPage.this.getPage(), new ResourceModel("folder.addSeries", "Add Series"),
                                newSeriesModel, new int[][]{{Tag.SeriesInstanceUID},
                                                    {Tag.SeriesNumber},
                                                    {Tag.Modality},
                                                    {Tag.SeriesDate, Tag.SeriesTime},
                                                    {Tag.SeriesDescription},
                                                    {Tag.BodyPartExamined},{Tag.Laterality}}, model));
            }
            @Override
            public boolean isVisible() {
                return model.getDataset() != null;
            }
        };
        link.add(new Image("addImg",ImageManager.IMAGE_COMMON_ADD).add(new ImageSizeBehaviour())
        .add(new TooltipBehaviour("folder.", tooltipId)));
        return link;
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
                    return model.isCollapsed() ? ImageManager.IMAGE_COMMON_EXPAND : 
                        ImageManager.IMAGE_COMMON_COLLAPSE;
                }
            })
            .add(new ImageSizeBehaviour()));
        }
        
        @Override
        public void onClick(AjaxRequestTarget target) {
            if (model.isCollapsed()) model.expand();
            else model.collapse();
            if (target != null) {
                target.addComponent(patientListItem);
                if (expandLevelChanged(model))
                    target.addComponent(header);
            }
        }
    }
}
