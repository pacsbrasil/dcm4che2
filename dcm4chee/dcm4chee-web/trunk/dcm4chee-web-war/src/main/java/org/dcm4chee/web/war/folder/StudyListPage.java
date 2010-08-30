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
import java.util.UUID;

import org.apache.wicket.Application;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.PageMap;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.ajax.AbstractAjaxTimerBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormSubmitBehavior;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.ajax.markup.html.form.AjaxCheckBox;
import org.apache.wicket.ajax.markup.html.form.AjaxFallbackButton;
import org.apache.wicket.authorization.strategies.role.metadata.MetaDataRoleAuthorizationStrategy;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow.WindowClosedCallback;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.CSSPackageResource;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.link.PopupSettings;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.PropertyListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.html.resources.CompressedResourceReference;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.util.time.Duration;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4chee.archive.entity.Patient;
import org.dcm4chee.archive.entity.Study;
import org.dcm4chee.archive.util.JNDIUtils;
import org.dcm4chee.icons.ImageManager;
import org.dcm4chee.icons.behaviours.ImageSizeBehaviour;
import org.dcm4chee.web.common.behaviours.CheckOneDayBehaviour;
import org.dcm4chee.web.common.behaviours.TooltipBehaviour;
import org.dcm4chee.web.common.markup.BaseForm;
import org.dcm4chee.web.common.markup.DateTimeLabel;
import org.dcm4chee.web.common.markup.ModalWindowLink;
import org.dcm4chee.web.common.markup.PopupLink;
import org.dcm4chee.web.common.markup.SimpleDateTimeField;
import org.dcm4chee.web.common.markup.modal.ConfirmationWindow;
import org.dcm4chee.web.common.markup.modal.MessageWindow;
import org.dcm4chee.web.common.validators.UIDValidator;
import org.dcm4chee.web.common.webview.link.WebviewerLinkProvider;
import org.dcm4chee.web.dao.folder.StudyListFilter;
import org.dcm4chee.web.dao.folder.StudyListLocal;
import org.dcm4chee.web.dao.util.QueryUtil;
import org.dcm4chee.web.war.WicketSession;
import org.dcm4chee.web.war.common.EditDicomObjectPanel;
import org.dcm4chee.web.war.common.SimpleEditDicomObjectPanel;
import org.dcm4chee.web.war.common.model.AbstractDicomModel;
import org.dcm4chee.web.war.common.model.AbstractEditableDicomModel;
import org.dcm4chee.web.war.folder.model.FileModel;
import org.dcm4chee.web.war.folder.model.InstanceModel;
import org.dcm4chee.web.war.folder.model.PPSModel;
import org.dcm4chee.web.war.folder.model.PatientModel;
import org.dcm4chee.web.war.folder.model.SeriesModel;
import org.dcm4chee.web.war.folder.model.StudyModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StudyListPage extends Panel {

    private static final ResourceReference CSS = new CompressedResourceReference(StudyListPage.class, "folder-style.css");
    
    private ModalWindow modalWindow;
    
    private static int PAGESIZE_ENTRIES = 6;
    private static int PAGESIZE_STEP = 5;
    private Model<Integer> pagesize = new Model<Integer>();

    private static final String MODULE_NAME = "folder";
    private static final long serialVersionUID = 1L;
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
    private MessageWindow msgWin = new MessageWindow("msgWin");
    private Mpps2MwlLinkPage linkPage = new Mpps2MwlLinkPage("linkPage");
    private ConfirmationWindow<PPSModel> confirmUnlinkMpps;
    private ImageSelectionWindow imageSelection = new ImageSelectionWindow("imgSelection");
    private ModalWindow wadoWindow = new ModalWindow("wadoWindow");
    
    private WebviewerLinkProvider webviewerLinkProvider;
    
    private List<WebMarkupContainer> searchTableComponents = new ArrayList<WebMarkupContainer>();
     
    protected boolean ajaxRunning = false;
    protected boolean ajaxDone = false;
    protected Image hourglassImage;

    public StudyListPage(final String id) {
        super(id);
        
        if (StudyListPage.CSS != null)
            add(CSSPackageResource.getHeaderContribution(StudyListPage.CSS));
        
        add(modalWindow = new ModalWindow("modal-window"));
        modalWindow.setWindowClosedCallback(new WindowClosedCallback() {
            private static final long serialVersionUID = 1L;

            public void onClose(AjaxRequestTarget target) {
                getPage().setOutputMarkupId(true);
                target.addComponent(getPage());
            }            
        });
        
        webviewerLinkProvider = new WebviewerLinkProvider(((WebApplication)Application.get()).getInitParameter("webviewerName"));
        webviewerLinkProvider.setBaseUrl(((WebApplication)Application.get()).getInitParameter("webviewerBaseUrl"));
        
        final StudyListFilter filter = viewport.getFilter();
        add(form = new BaseForm("form", new CompoundPropertyModel<Object>(filter)));
        form.setResourceIdPrefix("folder.");

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
        .add(new TooltipBehaviour("folder.", "searchToggleImg", new AbstractReadOnlyModel<Boolean>() {

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
        addHourglass(form);
        addActions(form);
        
        form.add(header);
        form.add(new PatientListView("patients", viewport.getPatients()));
        msgWin.setTitle(MessageWindow.TITLE_WARNING);
        add(msgWin);
        add(linkPage);
        add(imageSelection);
        imageSelection.setWindowClosedCallback(new WindowClosedCallback(){
            private static final long serialVersionUID = 1L;

            public void onClose(AjaxRequestTarget target) {
                if (imageSelection.isSelectionChanged())
                    target.addComponent(form);
            }            
        });
        add(wadoWindow);
    }

    @SuppressWarnings("unchecked")
    private void addQueryFields(final StudyListFilter filter, final BaseForm form) {
        final IModel<Boolean> enabledModel = new AbstractReadOnlyModel<Boolean>(){

            private static final long serialVersionUID = 1L;

            @Override
            public Boolean getObject() {
                return (!filter.isExtendedQuery() || QueryUtil.isUniversalMatch(filter.getStudyInstanceUID())) &&
                       (!filter.isExtendedQuery() || QueryUtil.isUniversalMatch(filter.getSeriesInstanceUID()));
            }
        };
        
        searchTableComponents.add(form.createAjaxParent("searchLabels"));
        
        form.addInternalLabel("patientName");
        form.addInternalLabel("patientIDDescr");
        form.addInternalLabel("studyDate");
        form.addInternalLabel("accessionNumber");
        
        searchTableComponents.add(form.createAjaxParent("searchFields"));
        
        form.addTextField("patientName", enabledModel, false);
        form.addTextField("patientID", enabledModel, true);
        form.addTextField("issuerOfPatientID", enabledModel, true);
        SimpleDateTimeField dtf = form.addDateTimeField("studyDateMin", new PropertyModel<Date>(filter, "studyDateMin"), 
                enabledModel, false, true);
        SimpleDateTimeField dtfEnd = form.addDateTimeField("studyDateMax", new PropertyModel<Date>(filter, "studyDateMax"), enabledModel, true, true);
        dtf.addToDateField(new CheckOneDayBehaviour(dtf, dtfEnd, "onchange"));
        form.addTextField("accessionNumber", enabledModel, false);

        searchTableComponents.add(form.createAjaxParent("searchDropdowns"));
        
        form.addInternalLabel("modality");
        form.addInternalLabel("sourceAET");
        
        form.addDropDownChoice("modality", null, modalities, enabledModel, false).setModelObject("*");
        form.addDropDownChoice("sourceAET", null, viewport.getSourceAetChoices(sourceAETs), enabledModel, false).setModelObject("*");

        final WebMarkupContainer extendedFilter = new WebMarkupContainer("extendedFilter") {

            private static final long serialVersionUID = 1L;

            @Override
            public boolean isVisible() {
                return showSearch && filter.isExtendedQuery();
            }
        };
        extendedFilter.add( new Label("birthDate.label", new ResourceModel("folder.extendedFilter.birthDate.label")));
        extendedFilter.add( new Label("birthDateMin.label", new ResourceModel("folder.extendedFilter.birthDateMin.label")));
        extendedFilter.add( new Label("birthDateMax.label", new ResourceModel("folder.extendedFilter.birthDateMax.label")));
        SimpleDateTimeField dtfB = form.getDateTextField("birthDateMin", null, "extendedFilter.", enabledModel);
        SimpleDateTimeField dtfBEnd = form.getDateTextField("birthDateMax", null, "extendedFilter.", enabledModel);
        dtfB.addToDateField(new CheckOneDayBehaviour(dtfB, dtfBEnd, "onchange"));
        extendedFilter.add(dtfB);
        extendedFilter.add(dtfBEnd);
        extendedFilter.add( new Label("studyInstanceUID.label", new ResourceModel("folder.extendedFilter.studyInstanceUID.label")));
        extendedFilter.add( new TextField<String>("studyInstanceUID").add(new UIDValidator()));

        extendedFilter.add( new Label("seriesInstanceUID.label", new ResourceModel("folder.extendedFilter.seriesInstanceUID.label")));
        extendedFilter.add( new TextField<String>("seriesInstanceUID") {

            private static final long serialVersionUID = 1L;

            @Override
            public boolean isEnabled() {
                return !filter.isExtendedQuery() || QueryUtil.isUniversalMatch(filter.getStudyInstanceUID());
            }
        }.add(new UIDValidator()));
        form.add(extendedFilter);
        
        searchTableComponents.add(form.createAjaxParent("searchFooter"));
        
        AjaxFallbackLink<?> link = new AjaxFallbackLink<Object>("showExtendedFilter") {

            private static final long serialVersionUID = 1L;

            @Override
            public void onClick(AjaxRequestTarget target) {
                filter.setExtendedQuery(!filter.isExtendedQuery());
                target.addComponent(form);
            }
        };
        link.add((new Image("showExtendedFilterImg", new AbstractReadOnlyModel<ResourceReference>() {

                private static final long serialVersionUID = 1L;

                @Override
                public ResourceReference getObject() {
                    return filter.isExtendedQuery() ? ImageManager.IMAGE_COMMON_COLLAPSE : 
                        ImageManager.IMAGE_COMMON_EXPAND;
                }
        })
        .add(new TooltipBehaviour("folder.search.", "showExtendedFilterImg", new AbstractReadOnlyModel<Boolean>() {

            private static final long serialVersionUID = 1L;

            @Override
            public Boolean getObject() {
                return filter.isExtendedQuery();
            }
        })))
        .add(new ImageSizeBehaviour()));
        form.addComponent(link);
    }

    private void addQueryOptions(BaseForm form) {
        form.addLabeledCheckBox("patientsWithoutStudies", null);
        form.addLabeledCheckBox("latestStudiesFirst", null);
        form.addLabeledCheckBox("ppsWithoutMwl", null);
    }

    private void addNavigation(final BaseForm form) {

        Button resetBtn = new AjaxButton("resetBtn") {
            
            private static final long serialVersionUID = 1L;

            @SuppressWarnings("unchecked")
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {

                form.clearInput();
                viewport.clear();
                ((DropDownChoice) ((WebMarkupContainer) form.get("searchDropdowns")).get("modality")).setModelObject("*");
                ((DropDownChoice) ((WebMarkupContainer) form.get("searchDropdowns")).get("sourceAET")).setModelObject("*");
                pagesize.setObject((PAGESIZE_ENTRIES / 2) * PAGESIZE_STEP);
                notSearched = true;
                form.setOutputMarkupId(true);
                target.addComponent(form);
            }
        };
        resetBtn.setDefaultFormProcessing(false);
        resetBtn.add(new Image("resetImg",ImageManager.IMAGE_COMMON_RESET)
        .add(new ImageSizeBehaviour("vertical-align: middle;"))
        );
        resetBtn.add(new Label("resetText", new ResourceModel("folder.searchFooter.resetBtn.text"))
            .add(new AttributeModifier("style", true, new Model<String>("vertical-align: middle")))
        );
        form.addComponent(resetBtn);

        final Button searchBtn = new AjaxButton("searchBtn") {
 
            private static final long serialVersionUID = 1L;
            
            @Override
            public void onSubmit(AjaxRequestTarget target, final Form<?> form) {
                  ajaxRunning = ajaxDone = false;
                  
                  target.addComponent(
                      form.add(new AbstractAjaxTimerBehavior(Duration.milliseconds(1)) {
                          
                          private static final long serialVersionUID = 1L;
    
                          @Override
                          protected void onTimer(final AjaxRequestTarget target) {
    
                              if (!ajaxRunning) {
                                  if (!ajaxDone) {
                                      ajaxRunning = true;
                                      new Thread(new Runnable() {
                                          public void run() {
                                              try {
                                                  viewport.setOffset(0);
                                                  queryStudies();
                                              } catch (Throwable t) {
                                                  log.error("search failed: ", t);
                                              } finally {
                                                  ajaxRunning = false;
                                                  ajaxDone = true;
                                              }
                                          }
                                      }).start();
                                  } else {
                                      this.stop();
                                      target.addComponent(form);
                                  }
                                  target.addComponent(hourglassImage);
                              }
                          }
                      })
                  );
            }
            
            @Override
            public void onError(AjaxRequestTarget target, Form<?> form) {
                BaseForm.addInvalidComponentsToAjaxRequestTarget(target, form);
            }
            
            @Override
            public boolean isEnabled() {
                return !ajaxRunning;
            }
        };
        searchBtn.setOutputMarkupId(true);
        searchBtn.add(new Image("searchImg",ImageManager.IMAGE_COMMON_SEARCH)
            .add(new ImageSizeBehaviour("vertical-align: middle;"))
        );
        searchBtn.add(new Label("searchText", new ResourceModel("folder.searchFooter.searchBtn.text"))
            .add(new AttributeModifier("style", true, new Model<String>("vertical-align: middle;")))
            .setOutputMarkupId(true)
        );
        
        form.addComponent(searchBtn);
        form.setDefaultButton(searchBtn);
        form.clearParent();
        
        List<Integer> pagesizes = new ArrayList<Integer>();
        pagesizes.add(1);
        for (int i = 1; i <= PAGESIZE_ENTRIES; i++)
            pagesizes.add(i * PAGESIZE_STEP);
        pagesize.setObject((PAGESIZE_ENTRIES / 2) * PAGESIZE_STEP);
        
        form.addDropDownChoice("pagesize", pagesize, pagesizes, new Model<Boolean>() {
                    
            private static final long serialVersionUID = 1L;

            @Override
            public Boolean getObject() {
                return !ajaxRunning;
            }
        }, true).setNullValid(false)
        .add(new AjaxFormSubmitBehavior(form, "onchange") {

            private static final long serialVersionUID = 1L;

            @Override
            protected void onSubmit(AjaxRequestTarget target) {
                ajaxRunning = ajaxDone = false;
                
                target.addComponent(
                    form.add(new AbstractAjaxTimerBehavior(Duration.milliseconds(1)) {
                        
                        private static final long serialVersionUID = 1L;
  
                        @Override
                        protected void onTimer(final AjaxRequestTarget target) {
  
                            if (!ajaxRunning) {
                                if (!ajaxDone) {
                                    ajaxRunning = true;
                                    new Thread(new Runnable() {
                                        public void run() {
                                            try {
                                                queryStudies();
                                            } catch (Throwable t) {
                                                log.error("search failed: ", t);
                                            } finally {
                                                ajaxRunning = false;
                                                ajaxDone = true;
                                            }
                                        }
                                    }).start();
                                } else {
                                    this.stop();
                                    target.addComponent(form);
                                }
                                target.addComponent(hourglassImage);
                            }
                        }
                    })
                );
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
        .add(new TooltipBehaviour("folder.search.")))
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
        .add(new TooltipBehaviour("folder.search.")))
        .setVisible(!notSearched)
        );

        //viewport label: use StringResourceModel with key substitution to select 
        //property key according notSearched and getTotal.
        Model<?> keySelectModel = new Model<Serializable>() {

            private static final long serialVersionUID = 1L;

            @Override
            public Serializable getObject() {
                return notSearched ? "folder.search.notSearched" :
                        viewport.getTotal() == 0 ? "folder.search.noMatchingStudiesFound" : 
                            "folder.search.studiesFound";
            }
        };
        form.add(new Label("viewport", new StringResourceModel("${}", StudyListPage.this, keySelectModel,new Object[]{"dummy"}){

            private static final long serialVersionUID = 1L;

            @Override
            protected Object[] getParameters() {
                return new Object[]{viewport.getOffset()+1,
                        Math.min(viewport.getOffset() + pagesize.getObject(), viewport.getTotal()),
                        viewport.getTotal()};
            }
        }));
    }

    private void addHourglass(final BaseForm form) {
        form.add((hourglassImage = new Image("hourglass-image", ImageManager.IMAGE_COMMON_AJAXLOAD) {
            private static final long serialVersionUID = 1L;
    
            @Override
            public boolean isVisible() {
                return ajaxRunning;
            }
        })
        .setOutputMarkupPlaceholderTag(true)
        .setOutputMarkupId(true)
        );
    }
    
    private void addActions(final BaseForm form) {
        
        final ConfirmationWindow<SelectedEntities> confirmDelete = new ConfirmationWindow<SelectedEntities>("confirmDelete") {

            private static final long serialVersionUID = 1L;

            private transient ContentEditDelegate delegate;
            
            private ContentEditDelegate getDelegate() {
                if (delegate == null) {
                    delegate = ContentEditDelegate.getInstance();
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
            public void onConfirmation(AjaxRequestTarget target, final SelectedEntities selected) {
                ajaxRunning = ajaxDone = false;
                
                this.setStatus(new StringResourceModel("folder.message.delete.running", StudyListPage.this, null));
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
                                            if (getDelegate().moveToTrash(selected)) {
                                                setStatus(new StringResourceModel("folder.message.deleteDone", StudyListPage.this,null));
                                                if (selected.hasPatients()) {
                                                    viewport.getPatients().clear();
                                                    queryStudies();
                                                } else
                                                    selected.refreshView(true);
                                            } else
                                                setStatus(new StringResourceModel("folder.message.deleteFailed", StudyListPage.this,null));
                                        } catch (Throwable t) {
                                            log.error("moveToTrash failed: ", t);
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
                        } else 
                            okBtn.setVisible(false);
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
                        this.setStatus(new StringResourceModel("folder.message.deleteDone", StudyListPage.this,null));
                        selected.refreshView(true);
                    } else 
                        this.setStatus(new StringResourceModel("folder.message.deleteFailed", StudyListPage.this,null));
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
                    confirmDelete.confirmWithCancel(target, new StringResourceModel("folder.message.confirmPpsDelete",this, null,new Object[]{selected}), selected);
                } else if (selected.hasDicomSelection()) {
                    confirmDelete.confirm(target, new StringResourceModel("folder.message.confirmDelete",this, null,new Object[]{selected}), selected);
                } else 
                    msgWin.show(target, getString("folder.message.noSelection"));
            }
        };
        deleteBtn.add(new Image("deleteImg", ImageManager.IMAGE_FOLDER_DELETE)
            .add(new ImageSizeBehaviour("vertical-align: middle;"))
        );
        deleteBtn.add(new Label("deleteText", new ResourceModel("folder.deleteBtn.text"))
            .add(new AttributeModifier("style", true, new Model<String>("vertical-align: middle")))
        );
        form.add(deleteBtn);
        
        AjaxFallbackButton moveBtn = new AjaxFallbackButton("moveBtn", form) {
            
            private static final long serialVersionUID = 1L;
            
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                selected.update(viewport.getPatients(), true);
                //selected.deselectChildsOfSelectedEntities();
                log.info("Selected Entities:"+selected);
                if (selected.hasDicomSelection()) {
                    modalWindow
                    .setPageCreator(new ModalWindow.PageCreator() {
                        
                        private static final long serialVersionUID = 1L;
                          
                        @Override
                        public Page createPage() {
                            return new MoveEntitiesPage(
                                  modalWindow, 
                                  selected, 
                                  viewport.getPatients()
                          );
                        }
                    });
                    modalWindow.show(target);
                } else
                    msgWin.show(target, getString("folder.message.noSelection"));
            }
        };
        moveBtn.add(new Image("moveImg",ImageManager.IMAGE_FOLDER_MOVE)
            .add(new ImageSizeBehaviour("vertical-align: middle;"))
        );
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
        exportBtn.add(new Label("exportText", new ResourceModel("folder.exportBtn.text"))
            .add(new AttributeModifier("style", true, new Model<String>("vertical-align: middle")))
        );
        form.add(exportBtn);

        confirmUnlinkMpps = new ConfirmationWindow<PPSModel>("confirmUnlink") {
 
            private static final long serialVersionUID = 1L;

            private transient ContentEditDelegate delegate;
                       
            private ContentEditDelegate getDelegate() {
                if (delegate == null) {
                    delegate = ContentEditDelegate.getInstance();
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
            public void onConfirmation(AjaxRequestTarget target, final PPSModel ppsModel) {
                ajaxRunning = ajaxDone = false;
                           
                this.setStatus(new StringResourceModel("folder.message.unlink.running", StudyListPage.this, null));
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
                                            if (ContentEditDelegate.getInstance().unlink(ppsModel)) {
                                                setStatus(new StringResourceModel("folder.message.unlinkDone", StudyListPage.this,null));
                                                if (selected.hasPatients()) {
                                                    viewport.getPatients().clear();
                                                    queryStudies();
                                                } else
                                                    selected.refreshView(true);
                                            } else 
                                                setStatus(new StringResourceModel("folder.message.unlinkFailed", StudyListPage.this,null));
                                        } catch (Throwable t) {
                                            log.error("Unlink of MPPS failed:"+ppsModel, t);
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
        confirmUnlinkMpps.setInitialHeight(150);
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
        updatePatients(dao.findStudies(viewport.getFilter(), pagesize.getObject(), viewport.getOffset()));
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
        StudyModel m = new StudyModel(study, patient);
        if (viewport.getFilter().isPpsWithoutMwl()) {
            m.expand();
            for (PPSModel pps : m.getPPSs()) {
                pps.collapse();
            }
        }
        studies.add(m);
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
            
            TooltipBehaviour tooltip = new TooltipBehaviour("folder.content.data.patient.");
            item.add(new Label("name").add(tooltip));        
            item.add(new Label("id", new AbstractReadOnlyModel<String>(){

                private static final long serialVersionUID = 1L;

                @Override
                public String getObject() {
                    return patModel.getIssuer() == null ? patModel.getId() :
                        patModel.getId()+" / "+patModel.getIssuer();
                }
            })
            .add(tooltip));
            DateTimeLabel dtl = new DateTimeLabel("birthdate").setWithoutTime(true);
            dtl.add(tooltip.newWithSubstitution(new PropertyModel<String>(dtl, "textFormat")));
            item.add(dtl);
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
            item.add(getEditLink(modalWindow, patModel, tooltip)
                    .add(tooltip));
            item.add(getAddStudyLink(patModel, tooltip)
                    .add(tooltip));
            item.add(new ExternalLink("webview", webviewerLinkProvider.getUrlForPatient(patModel.getId(), patModel.getIssuer())) {
                private static final long serialVersionUID = 1L;
                @Override
                public boolean isVisible() {
                    return webviewerLinkProvider.supportPatientLevel();
                }
            }
            .setPopupSettings(new PopupSettings(PageMap.forName("webviewPage"), 
                    PopupSettings.RESIZABLE|PopupSettings.SCROLLBARS))
            .add(new Image("webviewImg",ImageManager.IMAGE_FOLDER_VIEWER).add(new ImageSizeBehaviour())
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
            
            TooltipBehaviour tooltip = new TooltipBehaviour("folder.content.data.study.");
            
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
            item.add(getEditLink(modalWindow, studyModel, tooltip));
            item.add(getAddSeriesLink(studyModel, tooltip));
            item.add(new AjaxLink<Object>("imgSelect") {
                private static final long serialVersionUID = 1L;

                @Override
                public void onClick(AjaxRequestTarget target) {
                    imageSelection.setInitialWidth(new Integer(getString("folder.imageSelection.window.width")))
                    .setInitialHeight(new Integer(getString("folder.imageSelection.window.height")));
                    imageSelection.show(target, studyModel);
                }
                
            }.add(new Image("selectImg",ImageManager.IMAGE_COMMON_SEARCH)
            .add(new ImageSizeBehaviour())) );
            item.add( new AjaxCheckBox("selected") {

                private static final long serialVersionUID = 1L;

                @Override
                protected void onUpdate(AjaxRequestTarget target) {
                    target.addComponent(this);
                }}.setOutputMarkupId(true).add(tooltip));
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
            .add(tooltip)));
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
            
            TooltipBehaviour tooltip = new TooltipBehaviour("folder.content.data.pps.");
            
            item.add(new DateTimeLabel("datetime").add(tooltip));
            item.add(new Label("id").add(tooltip));
            item.add(new Label("spsid").add(tooltip));
            item.add(new Label("modality").add(tooltip));
            item.add(new Label("description").add(tooltip));
            item.add(new Label("numberOfSeries").add(tooltip));
            item.add(new Label("numberOfInstances").add(tooltip));
            item.add(new Label("status").add(tooltip));
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
                .add(tooltip))
            );
            item.add(getEditLink(modalWindow, ppsModel,tooltip));
            
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
            .add(tooltip));
            item.add(linkBtn);

            item.add(new AjaxFallbackLink<Object>("unlinkBtn") {

                private static final long serialVersionUID = 1L;

                @Override
                public void onClick(AjaxRequestTarget target) {
                    confirmUnlinkMpps.confirm(target, new StringResourceModel("folder.message.confirmUnlink",this, null,new Object[]{ppsModel}), ppsModel);
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
                }}.setOutputMarkupId(true)
                .add(tooltip)
            );
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
            
            TooltipBehaviour tooltip = new TooltipBehaviour("folder.content.data.series.");
            
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
                .add(tooltip))
            );
            item.add(getEditLink(modalWindow, seriesModel, tooltip));
            item.add(new AjaxCheckBox("selected") {

                private static final long serialVersionUID = 1L;

                @Override
                protected void onUpdate(AjaxRequestTarget target) {
                    target.addComponent(this);
                }
            }.setOutputMarkupId(true)
            .add(tooltip));
            item.add(new AjaxLink<Object>("imgSelect") {
                private static final long serialVersionUID = 1L;

                @Override
                public void onClick(AjaxRequestTarget target) {
                    imageSelection.setInitialWidth(new Integer(getString("folder.imageSelection.window.width")))
                    .setInitialHeight(new Integer(getString("folder.imageSelection.window.height")));
                    imageSelection.show(target, seriesModel);
                }
                
            }.add(new Image("selectImg",ImageManager.IMAGE_COMMON_SEARCH)
            .add(new ImageSizeBehaviour())) );
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
                    .add(tooltip)));
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
            
            TooltipBehaviour tooltip = new TooltipBehaviour("folder.content.data.instance.");
            
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
            item.add(getEditLink(modalWindow, instModel, tooltip));
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
            .add(tooltip)));

            item.add(new AjaxLink<Object>("wado") {
                private static final long serialVersionUID = 1L;

                @Override
                public void onClick(AjaxRequestTarget target) {

                    wadoWindow.setInitialWidth(new Integer(getString("folder.wadoImage.window.width")))
                    .setInitialHeight(new Integer(getString("folder.wadoImage.window.height")));
                    wadoWindow.setPageCreator(new ModalWindow.PageCreator() {
                          
                        private static final long serialVersionUID = 1L;
                          
                        @Override
                        public Page createPage() {
                            return new WadoImagePage(wadoWindow, instModel);                        
                        }
                    });
                    wadoWindow.show(target);
                }
            }
            .add(new Image("wadoImg",ImageManager.IMAGE_FOLDER_WADO)
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
            
            TooltipBehaviour tooltip = new TooltipBehaviour("folder.content.data.file.");
            
            final FileModel fileModel = (FileModel) item.getModelObject();
            item.add(new DateTimeLabel("fileObject.createdTime").add(tooltip));
            item.add(new Label("fileObject.fileSize").add(tooltip));
            item.add(new Label("fileObject.transferSyntaxUID").add(tooltip));
            item.add(new Label("fileObject.fileSystem.directoryPath").add(tooltip));
            item.add(new Label("fileObject.filePath").add(tooltip));
            item.add(new Label("fileObject.fileSystem.availability").add(tooltip));
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
            .add(tooltip)));
            item.add(new AjaxCheckBox("selected") {
                private static final long serialVersionUID = 1L;

                @Override
                public boolean isVisible() {
                    return false;//no action on file level at the moment
                }
                @Override
                protected void onUpdate(AjaxRequestTarget target) {
                    target.addComponent(this);
                }}.setOutputMarkupId(true)
                .add(tooltip));
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
    
    private Link<Object> getEditLink(final ModalWindow modalWindow, final AbstractEditableDicomModel model, TooltipBehaviour tooltip) {
       
        ModalWindowLink editLink
         = new ModalWindowLink("edit", modalWindow,
                new Integer(new ResourceModel("folder.edit.window.width").wrapOnAssignment(this).getObject().toString()).intValue(), 
                new Integer(new ResourceModel("folder.edit.window.height").wrapOnAssignment(this).getObject().toString()).intValue()
        ) {
            private static final long serialVersionUID = 1L;

            @Override
            public void onClick(AjaxRequestTarget target) {
                modalWindow.setContent(new EditDicomObjectPanel(
                        "content", 
                        modalWindow, 
                        (DicomObject) model.getDataset(), 
                        model.getClass().getSimpleName()
                ) {
                   private static final long serialVersionUID = 1L;

                   @Override
                   protected void onSubmit() {
                       model.update(getDicomObject());
                       super.onCancel();
                   }
                });
                modalWindow.show(target);
                super.onClick(target);
            }
            
            @Override
            public boolean isVisible() {
                return model.getDataset() != null;
            }
        };
        Image image = new Image("editImg",ImageManager.IMAGE_COMMON_DICOM_EDIT);
        image.add(new ImageSizeBehaviour("vertical-align: middle;"));
        if (tooltip != null) image.add(tooltip);
        editLink.add(image);
        
        MetaDataRoleAuthorizationStrategy.authorize(editLink, RENDER, "WebAdmin");
        return editLink;
    }

    private Link<Object> getAddStudyLink(final PatientModel model, TooltipBehaviour tooltip) {
        
        final ModalWindowLink addStudyLink 
            = new ModalWindowLink("add", modalWindow,
                    new Integer(new ResourceModel("folder.add.study.window.width").wrapOnAssignment(this).getObject().toString()).intValue(),                
                    new Integer(new ResourceModel("folder.add.study.window.height").wrapOnAssignment(this).getObject().toString()).intValue()
       ) {
           private static final long serialVersionUID = 1L;

           @Override
           public void onClick(AjaxRequestTarget target) {
               modalWindow.setContent(new SimpleEditDicomObjectPanel(
                     "content", 
                     modalWindow, 
                     (DicomObject) model.getDataset(), 
                     new ResourceModel("folder.add.study.text").wrapOnAssignment(getParent()).getObject(), 
                     new int[][]{{Tag.StudyInstanceUID},
                                 {Tag.StudyID},
                                 {Tag.StudyDescription},
                                 {Tag.AccessionNumber},
                                 {Tag.StudyDate, Tag.StudyTime}}, 
                     true
             ) {
                private static final long serialVersionUID = 1L;

                @Override
                protected void onSubmit() {
                    model.update(getDicomObject());
                    super.onCancel();
                }
               });
               modalWindow.show(target);
               super.onClick(target);
           }
           
           @Override
           public boolean isVisible() {
               return model.getDataset() != null;
           }
        };
        Image image = new Image("addImg",ImageManager.IMAGE_COMMON_ADD);
        image.add(new ImageSizeBehaviour());
        if (tooltip != null) image.add(tooltip);
        addStudyLink.add(image);
        return addStudyLink;
    }

    private Link<Object> getAddSeriesLink(final StudyModel model, TooltipBehaviour tooltip) {
        
        ModalWindowLink addSeriesLink
            = new ModalWindowLink("add", modalWindow,
                    new Integer(new ResourceModel("folder.add.series.window.width").wrapOnAssignment(this).getObject().toString()).intValue(), 
                    new Integer(new ResourceModel("folder.add.series.window.height").wrapOnAssignment(this).getObject().toString()).intValue()
           ) {
               private static final long serialVersionUID = 1L;
           
               @Override
               public void onClick(AjaxRequestTarget target) {
                   modalWindow.setContent(new SimpleEditDicomObjectPanel(
                           "content", 
                           modalWindow, 
                           (DicomObject) model.getDataset(), 
                           new ResourceModel("folder.add.series.text").wrapOnAssignment(getParent()).getObject(), 
                           new int[][]{{Tag.SeriesInstanceUID},
                                       {Tag.SeriesNumber},
                                       {Tag.Modality},
                                       {Tag.SeriesDate, Tag.SeriesTime},
                                       {Tag.SeriesDescription},
                                       {Tag.BodyPartExamined},{Tag.Laterality}}, 
                           true
           ) {
               private static final long serialVersionUID = 1L;

               @Override
                  protected void onSubmit() {
                      model.update(getDicomObject());
                      super.onCancel();
                  }
              });
              modalWindow.show(target);
              super.onClick(target);
          }
          
          @Override
          public boolean isVisible() {
              return model.getDataset() != null;
          }
       };
        Image image = new Image("addImg",ImageManager.IMAGE_COMMON_ADD);
        image.add(new ImageSizeBehaviour());
        if (tooltip != null) image.add(tooltip);
        addSeriesLink.add(image);
        return addSeriesLink;
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
