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

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.ajax.AbstractAjaxTimerBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormSubmitBehavior;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.ajax.markup.html.form.AjaxCheckBox;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.CSSPackageResource;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.link.Link;
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
import org.apache.wicket.util.time.Duration;
import org.dcm4che2.data.DicomObject;
import org.dcm4chee.archive.entity.File;
import org.dcm4chee.archive.entity.Instance;
import org.dcm4chee.archive.entity.PrivateFile;
import org.dcm4chee.archive.entity.PrivateInstance;
import org.dcm4chee.archive.entity.PrivatePatient;
import org.dcm4chee.archive.entity.PrivateSeries;
import org.dcm4chee.archive.entity.PrivateStudy;
import org.dcm4chee.archive.util.JNDIUtils;
import org.dcm4chee.dashboard.ui.filesystem.FileSystemPanel;
import org.dcm4chee.icons.ImageManager;
import org.dcm4chee.icons.behaviours.ImageSizeBehaviour;
import org.dcm4chee.web.common.behaviours.TooltipBehaviour;
import org.dcm4chee.web.common.markup.BaseForm;
import org.dcm4chee.web.common.markup.DateTimeLabel;
import org.dcm4chee.web.common.markup.modal.ConfirmationWindow;
import org.dcm4chee.web.common.markup.modal.MessageWindow;
import org.dcm4chee.web.dao.folder.StudyListLocal;
import org.dcm4chee.web.dao.trash.TrashListFilter;
import org.dcm4chee.web.dao.trash.TrashListLocal;
import org.dcm4chee.web.dao.util.QueryUtil;
import org.dcm4chee.web.service.common.FileImportOrder;
import org.dcm4chee.web.war.WicketApplication;
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

    private static int PAGESIZE_ENTRIES = 6;
    private static int PAGESIZE_STEP = 5;
    private Model<Integer> pagesize = new Model<Integer>();
    
    private static final String MODULE_NAME = "trash";
    private static final long serialVersionUID = 1L;
    private ViewPort viewport = new ViewPort();
    private TrashListHeader header = new TrashListHeader("thead");
    private PrivSelectedEntities selected = new PrivSelectedEntities();
    
    private List<String> sourceAETs = new ArrayList<String>();
    private boolean showSearch = true;
    private boolean notSearched = true;
    private MessageWindow msgWin = new MessageWindow("msgWin");
    
    private List<WebMarkupContainer> searchTableComponents = new ArrayList<WebMarkupContainer>();
    
    public TrashListPage(final String id) {
        super(id);

        if (TrashListPage.CSS != null)
            add(CSSPackageResource.getHeaderContribution(TrashListPage.CSS));
       
        final TrashListFilter filter = viewport.getFilter();
        final BaseForm form = new BaseForm("form", new CompoundPropertyModel<Object>(filter));
        form.setResourceIdPrefix("trash.");
        form.setOutputMarkupId(true);
        add(form);
        
        form.add(new AjaxFallbackLink<Object>("searchToggle") {

            private static final long serialVersionUID = 1L;

            @Override
            public void onClick(AjaxRequestTarget target) {
                showSearch = !showSearch;
                for (WebMarkupContainer wmc : searchTableComponents)
                    wmc.setVisible(showSearch);               
                target.addComponent(form);
            }
        }
        .add((new Image("searchToggleImg", new AbstractReadOnlyModel<ResourceReference>() {

                private static final long serialVersionUID = 1L;

                @Override
                public ResourceReference getObject() {
                    return showSearch ? ImageManager.IMAGE_COMMON_COLLAPSE : 
                        ImageManager.IMAGE_COMMON_EXPAND;
                }
        })
        .add(new TooltipBehaviour("trash", "searchToggleImg", new AbstractReadOnlyModel<Boolean>() {

            private static final long serialVersionUID = 1L;

            @Override
            public Boolean getObject() {
                return showSearch;
            }
        })))
        .add(new ImageSizeBehaviour())));

        initModalitiesAndSourceAETs();
        addQueryFields(filter, form);
        addQueryOptions(form);
        addNavigation(form);
        addActions(form);
        
        form.add(header);
        form.add(new PatientListView("patients", viewport.getPatients()));
        msgWin.setTitle(MessageWindow.TITLE_WARNING);
        add(msgWin);
    }

    @SuppressWarnings("unchecked")
    private void addQueryFields(final TrashListFilter filter, BaseForm form) {
        IModel<Boolean> enabledModel = new AbstractReadOnlyModel<Boolean>(){
            private static final long serialVersionUID = 1L;
            @Override
            public Boolean getObject() {
                return QueryUtil.isUniversalMatch(filter.getStudyInstanceUID());
            }
        };
        
        searchTableComponents.add(form.createAjaxParent("searchLabels"));
        
        form.addInternalLabel("patientName");
        form.addInternalLabel("patientIDDescr");
        form.addInternalLabel("accessionNumber");
        form.addInternalLabel("sourceAET");
        
        searchTableComponents.add(form.createAjaxParent("searchFields"));
        
        form.addTextField("patientName", enabledModel, false);
        form.addTextField("patientID", enabledModel, true);
        form.addTextField("issuerOfPatientID", enabledModel, true);
        form.addTextField("accessionNumber", enabledModel, false);
        form.addDropDownChoice("sourceAET", null, viewport.getSourceAetChoices(sourceAETs), enabledModel, false).setModelObject("*");

        searchTableComponents.add(form.createAjaxParent("searchFooter"));
    }

    private void addQueryOptions(BaseForm form) {
        form.addLabeledCheckBox("patientsWithoutStudies", null);
    }

    private void addNavigation(final BaseForm form) {
        
        Button resetBtn = new AjaxButton("resetBtn") {
            
            private static final long serialVersionUID = 1L;

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {

                form.clearInput();
                viewport.clear();
                form.setOutputMarkupId(true);
                target.addComponent(form);
            }
        };
        resetBtn.setDefaultFormProcessing(false);
        resetBtn.add(new Image("resetImg",ImageManager.IMAGE_COMMON_RESET)
        .add(new ImageSizeBehaviour("vertical-align: middle;"))
        );
        resetBtn.add(new Label("resetText", new ResourceModel("trash.searchFooter.resetBtn.text"))
            .add(new AttributeModifier("style", true, new Model<String>("vertical-align: middle")))
        );
        form.addComponent(resetBtn);
        
        Button searchBtn = new AjaxButton("searchBtn") {
            
            private static final long serialVersionUID = 1L;
            
            @Override
            public void onSubmit(AjaxRequestTarget target, Form<?> form) {
                viewport.setOffset(0);
                queryStudies();
                target.addComponent(form);
            }
            @Override
            public void onError(AjaxRequestTarget target, Form<?> form) {
                BaseForm.addInvalidComponentsToAjaxRequestTarget(target, form);
            }
        };
        searchBtn.add(new Image("searchImg",ImageManager.IMAGE_COMMON_SEARCH)
            .add(new ImageSizeBehaviour("vertical-align: middle;"))
        );
        searchBtn.add(new Label("searchText", new ResourceModel("trash.searchFooter.searchBtn.text"))
            .add(new AttributeModifier("style", true, new Model<String>("vertical-align: middle;")))
        );
        form.addComponent(searchBtn);
        form.setDefaultButton(searchBtn);
        
        form.clearParent();

        List<Integer> pagesizes = new ArrayList<Integer>();
        pagesizes.add(1);
        for (int i = 1; i <= PAGESIZE_ENTRIES; i++)
            pagesizes.add(i * PAGESIZE_STEP);
        pagesize.setObject((PAGESIZE_ENTRIES / 2) * PAGESIZE_STEP);
        form.addDropDownChoice("pagesize", pagesize, pagesizes, new Model<Boolean>(true), true).setNullValid(false)
        .add(new AjaxFormSubmitBehavior(form, "onchange") {

            private static final long serialVersionUID = 1L;

            @Override
            protected void onSubmit(AjaxRequestTarget target) {
                queryStudies();
                target.addComponent(form);
            }

            @Override
            protected void onError(AjaxRequestTarget target) {
            }
        });

        form.add(new Link<Object>("prev") {

            private static final long serialVersionUID = 1L;

            @Override
            public void onClick() {
                viewport.setOffset(Math.max(0, viewport.getOffset() - pagesize.getObject()));
                queryStudies();               
            }
            
            @Override
            public boolean isVisible() {
                return (!notSearched && !(viewport.getOffset() == 0));
            }
        }
        .add(new Image("prevImg", ImageManager.IMAGE_COMMON_BACK)
        .add(new ImageSizeBehaviour("vertical-align: middle;"))
        .add(new TooltipBehaviour("trash.search.")))
        );
 
        form.add(new Link<Object>("next") {

            private static final long serialVersionUID = 1L;

            @Override
            public void onClick() {
                viewport.setOffset(viewport.getOffset() + pagesize.getObject());
                queryStudies();
            }

            @Override
            public boolean isVisible() {
                return (!notSearched && !(viewport.getTotal() - viewport.getOffset() <= pagesize.getObject()));
            }
        }
        .add(new Image("nextImg", ImageManager.IMAGE_COMMON_FORWARD)
        .add(new ImageSizeBehaviour("vertical-align: middle;"))
        .add(new TooltipBehaviour("trash.search.")))
        .setVisible(!notSearched)
        );

        //viewport label: use StringResourceModel with key substitution to select 
        //property key according notSearched and getTotal.
        Model<?> keySelectModel = new Model<Serializable>() {

            private static final long serialVersionUID = 1L;

            @Override
            public Serializable getObject() {
                return notSearched ? "trash.search.notSearched" :
                        viewport.getTotal() == 0 ? "trash.search.noMatchingStudiesFound" : 
                            "trash.search.studiesFound";
            }
        };
        form.add(new Label("viewport", new StringResourceModel("${}", TrashListPage.this, keySelectModel,new Object[]{"dummy"}){

            private static final long serialVersionUID = 1L;

            @Override
            protected Object[] getParameters() {
                return new Object[]{viewport.getOffset()+1,
                        Math.min(viewport.getOffset() + pagesize.getObject(), viewport.getTotal()),
                        viewport.getTotal()};
            }
        }));
    }

    private void addActions(final BaseForm form) {
        
        final ConfirmationWindow<PrivSelectedEntities> confirmRestore = new ConfirmationWindow<PrivSelectedEntities>("confirmRestore") {

            private static final long serialVersionUID = 1L;
            
            private transient StoreBridgeDelegate delegate;
            
            private StoreBridgeDelegate getDelegate() {
                if (delegate == null) {
                    try {
                        delegate = StoreBridgeDelegate.getInstance(((WicketApplication) getApplication()).getInitParameter("storeBridgeServiceName"));
                    } catch (Exception e) {
                        log.error("Exception fetching delegate:"+e.getMessage(), e);
                    }
                }
                return delegate;
            }

            @Override
            public void onOk(AjaxRequestTarget target) {
                target.addComponent(form);
            }

            @Override
            public void close(AjaxRequestTarget target) {
                target.addComponent(form);
                super.close(target);
            }
            
            @Override
            public void onConfirmation(AjaxRequestTarget target, final PrivSelectedEntities selected) {
                ajaxRunning = false;
                ajaxDone = false;
                
                this.setStatus(new StringResourceModel("trash.message.restore.running", TrashListPage.this, null));
                okBtn.setVisible(false);
                
                msgLabel.add(new AbstractAjaxTimerBehavior(Duration.milliseconds(1)) {
                    
                    private static final long serialVersionUID = 1L;

                    @Override
                    protected void onTimer(final AjaxRequestTarget target) {

                        if (!ajaxRunning) {
                            if (!ajaxDone) {
                                ajaxRunning = true;
                                getDelegate();
                                new Thread(new Runnable() {
                                    public void run() {
                                        try {
                                            FileImportOrder fio = new FileImportOrder();
                                            List<PrivateFile> files = getFilesToRestore();
                                            TrashListLocal dao = (TrashListLocal) JNDIUtils.lookup(TrashListLocal.JNDI_NAME);
                                          
                                            for (PrivateFile privateFile : files) {
                                                DicomObject dio = dao.getDicomAttributes(privateFile.getPk());
                                                File file = new File();
                                                file.setFilePath(privateFile.getFilePath());
                                                file.setFileSize(privateFile.getFileSize());
                                                file.setFileStatus(privateFile.getFileStatus());
                                                file.setFileSystem(privateFile.getFileSystem());
                                                file.setMD5Sum(privateFile.getFileMD5());
                                                file.setTransferSyntaxUID(privateFile.getTransferSyntaxUID());
                                                Instance instance = new Instance();
                                                file.setInstance(instance);
                                                fio.addFile(file, dio);
                                            }
                                            delegate.importFile(fio);
                                            removeRestoredEntries();                            

                                            setStatus(new StringResourceModel("trash.message.restoreDone", TrashListPage.this,null));
                                            if (selected.hasPatients()) {
                                                viewport.getPatients().clear();
                                                queryStudies();
                                            } else
                                                selected.refreshView(true);
                                        } catch (Throwable t) {
                                            setStatus(new StringResourceModel("trash.message.restoreFailed", TrashListPage.this,null));
                                            log.error("Exception restoring entry:"+t.getMessage(), t);
                                        } finally {
                                            ajaxRunning = false;
                                            ajaxDone = true;
                                        }
                                    }
                                }).start();
                            } else {
                                okBtn.setVisible(true);
                                this.stop();        
                            }
                        } else {
                            okBtn.setVisible(false);
                        }
                        target.addComponent(msgLabel);
                        target.addComponent(hourglassImage);
                        target.addComponent(okBtn);
                    }
                });
            }
        };
        confirmRestore.setInitialHeight(150);
        form.add(confirmRestore);
        
        AjaxButton restoreBtn = new AjaxButton("restoreBtn") {

            private static final long serialVersionUID = 1L;

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                selected.update(viewport.getPatients());
                selected.deselectChildsOfSelectedEntities();
                if (selected.hasDicomSelection())
                    confirmRestore.confirm(target, new StringResourceModel("trash.message.confirmRestore", this, null,new Object[]{selected}), selected);
                else
                    msgWin.show(target, getString("trash.message.noSelection"));
            }
        };
        restoreBtn.add(new Image("restoreImg",ImageManager.IMAGE_TRASH_RESTORE)
            .add(new ImageSizeBehaviour("vertical-align: middle;"))
        );
        restoreBtn.add(new Label("restoreText", new ResourceModel("trash.restoreBtn.text"))
            .add(new AttributeModifier("style", true, new Model<String>("vertical-align: middle")))
        );
        form.add(restoreBtn);

        final ConfirmationWindow<PrivSelectedEntities> confirmDelete = new ConfirmationWindow<PrivSelectedEntities>("confirmDelete") {
 
            private static final long serialVersionUID = 1L;

            @Override
            public void onOk(AjaxRequestTarget target) {
                target.addComponent(form);
            }

            @Override
            public void close(AjaxRequestTarget target) {
                target.addComponent(form);
                super.close(target);
            }

            @Override
            public void onConfirmation(AjaxRequestTarget target, final PrivSelectedEntities selected) {
                ajaxRunning = false;
                ajaxDone = false;
           
                this.setStatus(new StringResourceModel("trash.message.delete.running", TrashListPage.this, null));
                okBtn.setVisible(false);
           
                msgLabel.add(new AbstractAjaxTimerBehavior(Duration.milliseconds(1)) {
               
                    private static final long serialVersionUID = 1L;

                    @Override
                    protected void onTimer(final AjaxRequestTarget target) {

                        if (!ajaxRunning) {
                            if (!ajaxDone) {
                                ajaxRunning = true;
                                new Thread(new Runnable() {
                                    public void run() {
                                        try {
                                            if (selected == null ? removeTrashAll() : removeTrashItems(selected)) {
                                                setStatus(new StringResourceModel("trash.message.deleteDone", TrashListPage.this,null));
                                                if (selected == null || selected.hasPatients()) {
                                                    viewport.getPatients().clear();
                                                    queryStudies();
                                                } else
                                                    selected.refreshView(true);
                                            } else
                                                setStatus(new StringResourceModel("trash.message.deleteFailed", TrashListPage.this,null));
                                        } catch (Throwable t) {
                                            log.error((selected == null ? "removeTrashAll" : "removeTrashItems") + " failed: ", t);
                                        } finally {
                                            ajaxRunning = false;
                                            ajaxDone = true;
                                        }
                                    }
                                }).start();
                            } else {
                                okBtn.setVisible(true);
                                this.stop();        
                            }
                        } else {
                            okBtn.setVisible(false);
                        }
                        target.addComponent(msgLabel);
                        target.addComponent(hourglassImage);
                        target.addComponent(okBtn);
                    }
                });
            }
        };
        confirmDelete.setInitialHeight(150);
        form.add(confirmDelete);

        AjaxButton deleteAllBtn = new AjaxButton("deleteAllBtn") {

            private static final long serialVersionUID = 1L;

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                confirmDelete.confirm(target, new StringResourceModel("trash.message.confirmDeleteAll",this, null), null);
            }
        };
        deleteAllBtn.add(new Image("deleteAllImg",ImageManager.IMAGE_TRASH_EMPTY)
            .add(new ImageSizeBehaviour("vertical-align: middle;"))
        );
        deleteAllBtn.add(new Label("deleteAllText", new ResourceModel("trash.deleteAllBtn.text"))
            .add(new AttributeModifier("style", true, new Model<String>("vertical-align: middle")))
        );
        form.add(deleteAllBtn);

        AjaxButton deleteBtn = new AjaxButton("deleteBtn") {

            private static final long serialVersionUID = 1L;

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                selected.update(viewport.getPatients());
                selected.deselectChildsOfSelectedEntities();
                log.info("Selected Entities: :"+selected);
                if (selected.hasDicomSelection()) {
                    confirmDelete.confirm(target, new StringResourceModel("trash.message.confirmDelete",this, null,new Object[]{selected}), selected);
                } else {
                    msgWin.show(target, getString("trash.message.noSelection"));
                }
            }
        };
        deleteBtn.add(new Image("deleteImg", ImageManager.IMAGE_TRASH_DELETE_SELECTED)
            .add(new ImageSizeBehaviour("vertical-align: middle;"))
        );
        deleteBtn.add(new Label("deleteText", new ResourceModel("trash.deleteBtn.text"))
            .add(new AttributeModifier("style", true, new Model<String>("vertical-align: middle")))
        );
        form.add(deleteBtn);
    }

    private void initModalitiesAndSourceAETs() {
        StudyListLocal dao = (StudyListLocal)
                JNDIUtils.lookup(StudyListLocal.JNDI_NAME);
        sourceAETs.clear();
        sourceAETs.addAll(dao.selectDistinctSourceAETs());
    }

    private void queryStudies() {
        TrashListLocal dao = (TrashListLocal)
                JNDIUtils.lookup(TrashListLocal.JNDI_NAME);
        viewport.setTotal(dao.countStudies(viewport.getFilter()));
        updatePatients(dao.findStudies(viewport.getFilter(), pagesize.getObject(), viewport.getOffset()));
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
        studies.add(new PrivStudyModel(study, patient));
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
            
            TooltipBehaviour tooltip = new TooltipBehaviour("trash.content.data.patient.");
            
            item.add(new Label("name").add(tooltip));
            item.add(new Label("id").add(tooltip));
            item.add(new Label("issuer").add(tooltip));
            item.add(new DateTimeLabel("birthdate").setWithoutTime(true).add(tooltip));
            item.add(new Label("sex").add(tooltip));
            item.add(new Label("comments").add(tooltip));
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
            .add(tooltip)));
            item.add(new AjaxCheckBox("selected"){

                private static final long serialVersionUID = 1L;

                @Override
                protected void onUpdate(AjaxRequestTarget target) {
                    target.addComponent(this);
                }}.setOutputMarkupId(true)
                .add(tooltip));
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
            
            TooltipBehaviour tooltip = new TooltipBehaviour("trash.content.data.study.");
            
            item.add(new DateTimeLabel("datetime").add(tooltip));
            item.add(new Label("id").add(tooltip));
            item.add(new Label("accessionNumber").add(tooltip));
            item.add(new Label("modalities").add(tooltip));
            item.add(new Label("description").add(tooltip));
            item.add(new Label("numberOfSeries").add(tooltip));
            item.add(new Label("numberOfInstances").add(tooltip));
            item.add(new Label("availability").add(tooltip));
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
            .add(tooltip)));
            item.add( new AjaxCheckBox("selected"){

                private static final long serialVersionUID = 1L;

                @Override
                protected void onUpdate(AjaxRequestTarget target) {
                    target.addComponent(this);
                }}.setOutputMarkupId(true)
                .add(tooltip));
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
            
            TooltipBehaviour tooltip = new TooltipBehaviour("trash.content.data.series.");
            
            item.add(new DateTimeLabel("datetime").add(tooltip));
            item.add(new Label("seriesNumber").add(tooltip));
            item.add(new Label("sourceAET").add(tooltip));
            item.add(new Label("modality").add(tooltip));
            item.add(new Label("description").add(tooltip));
            item.add(new Label("numberOfInstances").add(tooltip));
            item.add(new Label("availability").add(tooltip));
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
            .add(tooltip)));
            item.add(new AjaxCheckBox("selected") {

                private static final long serialVersionUID = 1L;

                @Override
                protected void onUpdate(AjaxRequestTarget target) {
                    target.addComponent(this);
                }}.setOutputMarkupId(true)
                .add(tooltip));
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
            
            TooltipBehaviour tooltip = new TooltipBehaviour("trash.content.data.instance.");
            
            item.add(new DateTimeLabel("datetime").add(tooltip));
            item.add(new Label("instanceNumber").add(tooltip));
            item.add(new Label("sopClassUID").add(tooltip));
            item.add(new Label("description").add(tooltip));
            item.add(new Label("availability").add(tooltip));
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
            .add(tooltip)));
            item.add(new AjaxCheckBox("selected"){

                private static final long serialVersionUID = 1L;

                @Override
                protected void onUpdate(AjaxRequestTarget target) {
                    target.addComponent(this);
                }}.setOutputMarkupId(true)
                .add(tooltip));
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
    
    private boolean removeTrashItems(PrivSelectedEntities selected) {
        try {
            TrashListLocal dao = (TrashListLocal) JNDIUtils.lookup(TrashListLocal.JNDI_NAME);
            
            List<Long> pks = new ArrayList<Long>();
            for (PrivInstanceModel instanceModel : selected.getInstances())
                pks.add(instanceModel.getPk());
            dao.removeTrashEntities(pks, PrivateInstance.class, false);

            pks = new ArrayList<Long>();
            for (PrivSeriesModel seriesModel : selected.getSeries())
                pks.add(seriesModel.getPk());
            dao.removeTrashEntities(pks, PrivateSeries.class, false);
            
            pks = new ArrayList<Long>();
            for (PrivStudyModel studyModel : selected.getStudies())
                pks.add(studyModel.getPk());
            dao.removeTrashEntities(pks, PrivateStudy.class, false);

            pks = new ArrayList<Long>();
            for (PrivPatientModel patientModel : selected.getPatients())
                pks.add(patientModel.getPk());
            dao.removeTrashEntities(pks, PrivatePatient.class, false);
        } catch (Exception x) {
            log.error("Delete failed! Reason:"+x.getMessage(),x);
            return false;
        }
        return true;
    }
    
    private boolean removeTrashAll() {
        try {
            ((TrashListLocal) JNDIUtils.lookup(TrashListLocal.JNDI_NAME)).removeTrashAll();
        } catch (Exception x) {
            log.error("Delete failed! Reason:"+x.getMessage(),x);
            return false;
        }
        return true;
    }
    
    private List<PrivateFile> getFilesToRestore() {

        List<PrivateFile> files = new ArrayList<PrivateFile>();
        TrashListLocal dao = (TrashListLocal) JNDIUtils.lookup(TrashListLocal.JNDI_NAME);
        
        if (selected.hasPatients()) {
            for (PrivPatientModel pp : selected.getPatients())
                files.addAll(dao.getFilesForEntity(pp.getPk(), PrivatePatient.class));
        }
        if (selected.hasStudies()) {
            for (PrivStudyModel pst : selected.getStudies())
                files.addAll(dao.getFilesForEntity(pst.getPk(), PrivateStudy.class));
        }
        if (selected.hasSeries()) {
            for (PrivSeriesModel pse : selected.getSeries())
                files.addAll(dao.getFilesForEntity(pse.getPk(), PrivateSeries.class));
        }
        if (selected.hasInstances()) {
            for (PrivInstanceModel pi : selected.getInstances())
                files.addAll(dao.getFilesForEntity(pi.getPk(), PrivateInstance.class));
        }
        return files;
    }

    private void removeRestoredEntries() {

        TrashListLocal dao = (TrashListLocal) JNDIUtils.lookup(TrashListLocal.JNDI_NAME);

        if (selected.hasInstances()) {
            List<Long> pks = new ArrayList<Long>();
            for (PrivInstanceModel pi : selected.getInstances())
                pks.add(pi.getPk());
            dao.removeTrashEntities(pks, PrivateInstance.class, true);
        }
        if (selected.hasSeries()) {
            List<Long> pks = new ArrayList<Long>();
            for (PrivSeriesModel pse : selected.getSeries())
                pks.add(pse.getPk());
            dao.removeTrashEntities(pks, PrivateSeries.class, true);
        }
        if (selected.hasStudies()) {
            List<Long> pks = new ArrayList<Long>();
            for (PrivStudyModel pst : selected.getStudies())
                pks.add(pst.getPk());
            dao.removeTrashEntities(pks, PrivateStudy.class, true);
        }
        if (selected.hasPatients()) {
            List<Long> pks = new ArrayList<Long>();
            for (PrivPatientModel pp : selected.getPatients())
                pks.add(pp.getPk());
            dao.removeTrashEntities(pks, PrivatePatient.class, true);
        }
    }
}
