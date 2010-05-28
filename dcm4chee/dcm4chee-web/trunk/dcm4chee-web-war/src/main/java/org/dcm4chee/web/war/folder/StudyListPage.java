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
import org.apache.wicket.PageMap;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxCheckBox;
import org.apache.wicket.authorization.strategies.role.metadata.MetaDataRoleAuthorizationStrategy;
import org.apache.wicket.datetime.markup.html.form.DateTextField;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.CSSPackageResource;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
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
import org.dcm4chee.web.common.webview.link.WebviewerLinkProvider;
import org.dcm4chee.web.dao.folder.StudyListFilter;
import org.dcm4chee.web.dao.folder.StudyListLocal;
import org.dcm4chee.web.dao.util.QueryUtil;
import org.dcm4chee.web.war.WicketSession;
import org.dcm4chee.web.war.common.EditDicomObjectPage;
import org.dcm4chee.web.war.common.model.AbstractDicomModel;
import org.dcm4chee.web.war.folder.model.FileModel;
import org.dcm4chee.web.war.folder.model.InstanceModel;
import org.dcm4chee.web.war.folder.model.PPSModel;
import org.dcm4chee.web.war.folder.model.PatientModel;
import org.dcm4chee.web.war.folder.model.SeriesModel;
import org.dcm4chee.web.war.folder.model.StudyModel;
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
        
        final StudyListFilter filter = viewport.getFilter();
        form = new BaseForm("form", new CompoundPropertyModel<Object>(filter));
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

    private void addQueryFields(final StudyListFilter filter, BaseForm form) {
        IModel<Boolean> enabledModel = new AbstractReadOnlyModel<Boolean>(){

            private static final long serialVersionUID = 1L;

            @Override
            public Boolean getObject() {
                return (!filter.isExtendedStudyQuery() || QueryUtil.isUniversalMatch(filter.getStudyInstanceUID())) &&
                       (!filter.isExtendedSeriesQuery() || QueryUtil.isUniversalMatch(filter.getSeriesInstanceUID()));
            }
            
        };
        form.addLabeledTextField("patientName", enabledModel);
        form.addLabel("patientIDDescr");
        form.addLabeledTextField("patientID", enabledModel);
        form.addLabeledTextField("issuerOfPatientID", enabledModel);
        addExtendedPatientSearch(form);
        
        form.addLabel("studyDate");
        form.addLabeledDateTimeField("studyDateMin", new PropertyModel<Date>(filter, "studyDateMin"), enabledModel, false);
        form.addLabeledDateTimeField("studyDateMax", new PropertyModel<Date>(filter, "studyDateMax"), enabledModel, true);

        form.addLabeledTextField("accessionNumber", enabledModel);
        addExtendedStudySearch(form);
        form.addLabeledDropDownChoice("modality", null, modalities, enabledModel);
        List<String> choices = viewport.getSourceAetChoices(sourceAETs);
        if (choices.size() > 0)
            filter.setSourceAET(choices.get(0));
        form.addLabeledDropDownChoice("sourceAET", null, choices, enabledModel);
        addExtendedSeriesSearch(form);
    }

    private void addQueryOptions(BaseForm form) {
        form.addLabeledCheckBox("patientsWithoutStudies", null);
        form.addLabeledCheckBox("latestStudiesFirst", null);
        form.addLabeledCheckBox("ppsWithoutMwl", null);
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

    private void addActions(final BaseForm form) {
        PopupLink exportBtn = new PopupLink("exportBtn", "exportPage") {
            private static final long serialVersionUID = 1L;
            @Override
            public void onClick() {
                ExportPage page = new ExportPage(viewport.getPatients());
                this.setResponsePage(page);
            }
        };
        exportBtn.setPopupHeight(400);
        exportBtn.setPopupWidth(550);
        exportBtn.add(new Image("exportImg", ImageManager.IMAGE_EXPORT)
        .add(new ImageSizeBehaviour()));
        form.add(exportBtn);
        final ConfirmationWindow<SelectedEntities> confirmMove = new ConfirmationWindow<SelectedEntities>("confirmMove"){
            private static final long serialVersionUID = 1L;
            @Override
            public void onConfirmation(AjaxRequestTarget target, SelectedEntities selected) {
                try {
                    int nrOfMovedInstances = ContentEditDelegate.getInstance().moveEntities(selected);
                    if (nrOfMovedInstances != -1) {
                        this.setStatus(new StringResourceModel("folder.moveDone", StudyListPage.this,null));
                        viewport.getPatients().clear();
                    } else {
                        this.setStatus(new StringResourceModel("folder.moveFailed", StudyListPage.this,null));
                    }
                } catch (SelectionException x) {
                    log.warn(x.getMessage());
                    this.setStatus(new StringResourceModel(x.getMsgId(), StudyListPage.this,null));
                }
                queryStudies();
            }
            @Override
            public void onOk(AjaxRequestTarget target) {
                target.addComponent(form);
            }
        };
        confirmMove.setInitialHeight(150);
        form.add(confirmMove);
        AjaxLink<?> moveBtn = new AjaxLink<Object>("moveBtn") {
            private static final long serialVersionUID = 1L;
            @Override
            public void onClick(AjaxRequestTarget target) {
                selected.update(viewport.getPatients());
                selected.deselectChildsOfSelectedEntities();
                log.info("Selected Entities:"+selected);
                if (selected.hasDicomSelection()) {
                    confirmMove.confirm(target, new StringResourceModel("folder.confirmMove",this, null,new Object[]{selected}), selected);
                } else {
                    msgWin.show(target, getString("folder.noSelection"));
                }
            }
        };
        moveBtn.add(new Image("moveImg",ImageManager.IMAGE_MOVE)
        .add(new ImageSizeBehaviour()));
        form.add(moveBtn);
        final ConfirmationWindow<SelectedEntities> confirmDelete = new ConfirmationWindow<SelectedEntities>("confirmDelete"){
            private static final long serialVersionUID = 1L;
            @Override
            public void onConfirmation(AjaxRequestTarget target, SelectedEntities selected) {
                if (ContentEditDelegate.getInstance().moveToTrash(selected)) {
                    this.setStatus(new StringResourceModel("folder.deleteDone", StudyListPage.this,null));
                    viewport.getPatients().clear();
                } else {
                    this.setStatus(new StringResourceModel("folder.deleteFailed", StudyListPage.this,null));
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
                if (selected.hasDicomSelection()) {
                    confirmDelete.confirm(target, new StringResourceModel("folder.confirmDelete",this, null,new Object[]{selected}), selected);
                } else {
                    msgWin.show(target, getString("folder.noSelection"));
                }
            }
        };
        deleteBtn.add(new Image("deleteImg",ImageManager.IMAGE_TRASH)
        .add(new ImageSizeBehaviour()))
        .add(tooltipBehaviour);
        form.add(deleteBtn);

        confirmUnlinkMpps = new ConfirmationWindow<PPSModel>("confirmUnlink"){
            private static final long serialVersionUID = 1L;
            @Override
            public void onConfirmation(AjaxRequestTarget target, PPSModel ppsModel) {
                try {
                    if (ContentEditDelegate.getInstance().unlink(ppsModel)) {
                        this.setStatus(new StringResourceModel("folder.unlinkDone", StudyListPage.this,null));
                        return;
                    }
                } catch (Exception x) {
                    log.error("Unlink of MPPS failed:"+ppsModel, x);
                }
                this.setStatus(new StringResourceModel("folder.unlinkFailed", StudyListPage.this,null));

            }
            @Override
            public void onOk(AjaxRequestTarget target) {
                target.addComponent(form);
            }
        };
        form.add(confirmUnlinkMpps);
    }

    private WebMarkupContainer addExtendedPatientSearch(final BaseForm form) {
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
        extendedPatFilter.add(form.getDateTextField("birthDateMin", null, true));
        extendedPatFilter.add(form.getDateTextField("birthDateMax", null, true));
        form.add(extendedPatFilter);
        AjaxFallbackLink<?> link = new AjaxFallbackLink<Object>("showExtendedPatFilter") {

            private static final long serialVersionUID = 1L;

            @Override
            public void onClick(AjaxRequestTarget target) {
                filter.setExtendedPatQuery(!filter.isExtendedPatQuery());
                target.addComponent(form);
            }
        };
        link.add(new Image("showExtendedPatFilterImg", new AbstractReadOnlyModel<ResourceReference>() {

                private static final long serialVersionUID = 1L;

                @Override
                public ResourceReference getObject() {
                    return filter.isExtendedPatQuery() ? ImageManager.IMAGE_COLLAPSE : 
                        ImageManager.IMAGE_EXPAND;
                }
        })
        .add(new ImageSizeBehaviour()));
        
        form.add(link);
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
        AjaxFallbackLink<?> link = new AjaxFallbackLink<Object>("showExtendedStudyFilter") {

            private static final long serialVersionUID = 1L;

            @Override
            public void onClick(AjaxRequestTarget target) {
                filter.setExtendedStudyQuery(!filter.isExtendedStudyQuery());
                target.addComponent(form);
            }
        };
        link.add(new Image("showExtendedStudyFilterImg", new AbstractReadOnlyModel<ResourceReference>() {

            private static final long serialVersionUID = 1L;

            @Override
            public ResourceReference getObject() {
                return filter.isExtendedStudyQuery() ? ImageManager.IMAGE_COLLAPSE : 
                    ImageManager.IMAGE_EXPAND;
            }
        })
        .add(new ImageSizeBehaviour()));
        
        form.add(link);
        return extendedStudyFilter;
    }
    
    private WebMarkupContainer addExtendedSeriesSearch(final Form<?> form) {
        final StudyListFilter filter = viewport.getFilter();
        final WebMarkupContainer extendedSeriesFilter = new WebMarkupContainer("extendedSeriesFilter") {

            private static final long serialVersionUID = 1L;

            @Override
            public boolean isVisible() {
                return filter.isExtendedSeriesQuery();
            }
        };
        extendedSeriesFilter.add( new Label("seriesInstanceUIDLabel", new ResourceModel("folder.seriesInstanceUID")));
        extendedSeriesFilter.add( new TextField<String>("seriesInstanceUID") {

            private static final long serialVersionUID = 1L;

            @Override
            public boolean isEnabled() {
                return !filter.isExtendedStudyQuery() || QueryUtil.isUniversalMatch(filter.getStudyInstanceUID());
            }
        });
        form.add(extendedSeriesFilter);
        AjaxFallbackLink<?> link = new AjaxFallbackLink<Object>("showExtendedSeriesFilter") {

            private static final long serialVersionUID = 1L;

            @Override
            public void onClick(AjaxRequestTarget target) {
                filter.setExtendedSeriesQuery(!filter.isExtendedSeriesQuery());
                target.addComponent(form);
            }
        };
        link.add(new Image("showExtendedSeriesFilterImg", new AbstractReadOnlyModel<ResourceReference>() {

            private static final long serialVersionUID = 1L;

            @Override
            public ResourceReference getObject() {
                return filter.isExtendedSeriesQuery() ? ImageManager.IMAGE_COLLAPSE : 
                    ImageManager.IMAGE_EXPAND;
            }
        })
        .add(new ImageSizeBehaviour()));
        
        form.add(link);
        return extendedSeriesFilter;
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
            dtl.add(tooltipBehaviour.newWithSubstitution(new PropertyModel(dtl, "textFormat")));
            item.add(dtl);
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

            }.add(new Image("detailImg",ImageManager.IMAGE_DETAIL)
            .add(new ImageSizeBehaviour()))
            .add(new TooltipBehaviour("folder.","patDetail")));
            item.add(getEditLink(patModel, "patEdit"));
            item.add( new ExternalLink("webview", webviewerLinkProvider.getUrlForPatient(patModel.getId(), patModel.getIssuer())) {
                private static final long serialVersionUID = 1L;
                @Override
                public boolean isVisible() {
                    return webviewerLinkProvider.supportPatientLevel();
                }
            }
            .setPopupSettings(new PopupSettings(PageMap.forName("webviewPage"), 
                    PopupSettings.RESIZABLE|PopupSettings.SCROLLBARS))
            .add(new Image("webviewImg",ImageManager.IMAGE_WEBVIEWER).add(new ImageSizeBehaviour()))
            .add(new TooltipBehaviour("folder.","patWebviewer")));
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
            item.add(new DateTimeLabel("datetime").add(new TooltipBehaviour("folder.study","DateTime")));
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

            }.add(new Image("detailImg",ImageManager.IMAGE_DETAIL)
            .add(new ImageSizeBehaviour()))
            .add(new TooltipBehaviour("folder.","studyDetail")));
            item.add( getEditLink(studyModel, "studyEdit"));
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
            .add(new Image("webviewImg",ImageManager.IMAGE_WEBVIEWER).add(new ImageSizeBehaviour()))
            .add(new TooltipBehaviour("folder.","studyWebviewer")));
            item.add(details);
            details.add(new DicomObjectPanel("dicomobject", studyModel.getDataset(), false));
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
                    return ppsModel.getAccessionNumber() == null ? ImageManager.IMAGE_STATUS_UNLINKED : 
                        ImageManager.IMAGE_STATUS_LINKED;
                }
            }) {
                private static final long serialVersionUID = 1L;
                @Override
                public boolean isVisible() {
                    return ppsModel.getDataset() != null;
                }
            }
            .add(new ImageSizeBehaviour()));

            item.add(new Label("pk").add(new TooltipBehaviour("folder.", "ppsPk")));
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
            }.add(new Image("detailImg",ImageManager.IMAGE_DETAIL)
            .add(new ImageSizeBehaviour()))
            .add(new TooltipBehaviour("folder.","ppsDetail")));
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
                            .setInitialWidth(new Integer(new ResourceModel("folder.mpps2mwl.window.width","800").wrapOnAssignment(this).getObject().toString()))
                            .setInitialHeight(new Integer(new ResourceModel("folder.mpps2mwl.window.height","600").wrapOnAssignment(this).getObject().toString()))
                    )
                    .show(target, ppsModel, form);
                }
                @Override
                public boolean isVisible() {
                    return ppsModel.getDataset() != null && ppsModel.getAccessionNumber()==null;
                }
            };
            linkBtn.add(new Image("linkImg", ImageManager.IMAGE_INSERT_LINK)
            .add(new ImageSizeBehaviour())).add(new TooltipBehaviour("folder.","ppsLink"));
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
            }.add(new Image("unlinkImg",ImageManager.IMAGE_STATUS_UNLINKED).add(new ImageSizeBehaviour())));
            
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

            }.add(new Image("detailImg",ImageManager.IMAGE_DETAIL)
            .add(new ImageSizeBehaviour()))
            .add(new TooltipBehaviour("folder.","seriesDetail")));
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
            item.add( new ExternalLink("webview", webviewerLinkProvider.getUrlForStudy(seriesModel.getSeriesInstanceUID())) {
                private static final long serialVersionUID = 1L;
                @Override
                public boolean isVisible() {
                    return webviewerLinkProvider.supportSeriesLevel();
                }
            }
            .setPopupSettings(new PopupSettings(PageMap.forName("webviewPage"), 
                    PopupSettings.RESIZABLE|PopupSettings.SCROLLBARS))
            .add(new Image("webviewImg",ImageManager.IMAGE_WEBVIEWER).add(new ImageSizeBehaviour()))
            .add(new TooltipBehaviour("folder.","seriesWebviewer")));
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
            item.add(new DateTimeLabel("datetime").add(new TooltipBehaviour("folder.instance","DateTime")));
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

            }.add(new Image("detailImg",ImageManager.IMAGE_DETAIL)
            .add(new ImageSizeBehaviour()))
            .add(new TooltipBehaviour("folder.","instanceDetail")));
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
            .add(new Image("webviewImg",ImageManager.IMAGE_WEBVIEWER).add(new ImageSizeBehaviour()))
            .add(new TooltipBehaviour("folder.","instanceWebviewer")));
/*            item.add( new PopupLink("wado", "wadoPage") {
                private static final long serialVersionUID = 1L;
                @Override
                public void onClick() {
                    setResponsePage(
                            new WadoPage(new AbstractReadOnlyModel<String>(){
                                public String getObject() {
                                    return WADODelegate.getInstance().getURL(instModel);
                                }
                            }) );
                }
                @Override
                public boolean isVisible() {
                    return WADODelegate.getInstance().getRenderType(instModel.getSopClassUID()) != WADODelegate.NOT_RENDERABLE;
                }
            }.add(new Image("wadoImg",ImageManager.IMAGE_WADO)
            .add(new ImageSizeBehaviour()))
            .add(new TooltipBehaviour("folder.","wado")));
*/
            item.add(new ExternalLink("wado", WADODelegate.getInstance().getURL(instModel)){

                private static final long serialVersionUID = 1L;

                @Override
                public boolean isVisible() {
                    return WADODelegate.getInstance().getRenderType(instModel.getSopClassUID()) != WADODelegate.NOT_RENDERABLE;
                }
                
            }.add(new Image("wadoImg",ImageManager.IMAGE_WADO).add(new ImageSizeBehaviour())
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

            }.add(new Image("detailImg",ImageManager.IMAGE_DETAIL)
            .add(new ImageSizeBehaviour()))
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
    
    private Link<Object> getEditLink(final AbstractDicomModel model, String tooltipId) {
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
        link.add(new Image("editImg",ImageManager.IMAGE_EDIT).add(new ImageSizeBehaviour()));
        link.add(new TooltipBehaviour("folder.", tooltipId));
        MetaDataRoleAuthorizationStrategy.authorize(link, RENDER, "WebAdmin");
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
                    return model.isCollapsed() ? ImageManager.IMAGE_EXPAND : 
                        ImageManager.IMAGE_COLLAPSE;
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
