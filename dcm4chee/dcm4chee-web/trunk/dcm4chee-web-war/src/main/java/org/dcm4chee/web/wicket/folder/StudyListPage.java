package org.dcm4chee.web.wicket.folder;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.PropertyListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.validation.validator.PatternValidator;
import org.dcm4chee.archive.entity.Patient;
import org.dcm4chee.archive.entity.Study;
import org.dcm4chee.archive.util.JNDIUtils;
import org.dcm4chee.web.dao.StudyListLocal;
import org.dcm4chee.web.wicket.WicketSession;
import org.dcm4chee.web.wicket.common.BaseForm;
import org.dcm4chee.web.wicket.common.DateTimeLabel;
import org.dcm4chee.web.wicket.common.TooltipBehaviour;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StudyListPage extends Panel {

    private static final String MODULE_NAME = "folder";
    private static final long serialVersionUID = 1L;
    private static int PAGESIZE = 10;
    private ViewPort viewport = ((WicketSession) getSession()).getViewPort();
    private List<String> sourceAETs = new ArrayList<String>();
    private List<String> modalities = new ArrayList<String>();
    private boolean notSearched = true;
    private TooltipBehaviour tooltipBehaviour = new TooltipBehaviour("folder.");
    
    private static Logger log = LoggerFactory.getLogger(StudyListPage.class);

    public StudyListPage(final String id) {
        super(id);
        BaseForm form = new BaseForm("form", new CompoundPropertyModel(viewport.getFilter()));
        form.setResourceIdPrefix("folder.");
        form.setTooltipBehaviour(tooltipBehaviour);
        add(form);
        form.addLabeledTextField("patientName");
        form.addLabel("patientIDDescr");
        form.addLabeledTextField("patientID");
        form.addLabeledTextField("issuerOfPatientID");
        PatternValidator datePatternValidator = new PatternValidator(
                "\\*|(((19)|(20))\\d{2}-((0[1-9])|(1[0-2]))-((0[1-9])|([12]\\d)|(3[01])))");
        form.addLabel("studyDate");
        form.addLabeledTextField("studyDateMin").add(datePatternValidator);
        form.addLabeledTextField("studyDateMax").add(datePatternValidator);
        form.addLabeledTextField("accessionNumber");
        form.addLabeledTextField("studyInstanceUID");
        form.addLabeledDropDownChoice("modality", null, modalities);
        form.addLabeledDropDownChoice("sourceAET", null, sourceAETs);
        form.addLabeledCheckBox("patientsWithoutStudies", null);
        form.addLabeledCheckBox("latestStudiesFirst", null);
        
        form.add(new Button("search", new ResourceModel("searchBtn")) {

            @Override
            public void onSubmit() {
                viewport.setOffset(0);
                queryStudies();
            }});
        form.add(new Button("prev", new ResourceModel("folder.prev")) {

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
        Model keySelectModel = new Model() {
            @Override
            public Serializable getObject() {
                return notSearched ? "folder.notSearched" :
                        viewport.getTotal() == 0 ? "folder.noMatchingStudiesFound" : 
                            "folder.studiesFound";
            }
        };
        form.add(new Label("viewport", new StringResourceModel("${}", StudyListPage.this, keySelectModel,new Object[]{"dummy"}){
            @Override
            protected Object[] getParameters() {
                return new Object[]{viewport.getOffset()+1,
                        Math.min(viewport.getOffset()+PAGESIZE, viewport.getTotal()),
                        viewport.getTotal()};
            }
        }));

        form.add(new StudyListHeader("thead"));
        form.add(new PatientListView("patients", viewport.getPatients()));
        initModalitiesAndSourceAETs();
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
        PatientModel patientModel = new PatientModel(patient);
        viewport.getPatients().add(patientModel);
        return patientModel;
    }
    
    public static String getModuleName() {
        return MODULE_NAME;
    }

    private final class PatientListView extends PropertyListView {

        private PatientListView(String id, List list) {
            super(id, list);
        }

        @Override
        protected void populateItem(final ListItem item) {
            item.setOutputMarkupId(true);
            final PatientModel patModel = (PatientModel) item.getModelObject();
            WebMarkupContainer cell = new WebMarkupContainer("cell"){
                @Override
                protected void onComponentTag(ComponentTag tag) {
                   super.onComponentTag(tag);
                   tag.put("rowspan", patModel.getRowspan());
                }
            };
            cell.add(new AjaxFallbackLink("collapse") {

                @Override
                public void onClick(AjaxRequestTarget target) {
                    patModel.collapse();
                    if (target != null) {
                        target.addComponent(item);
                    }
                }

                @Override
                public boolean isVisible() {
                    return !patModel.isCollapsed();
                }
            });
            cell.add(new AjaxFallbackLink("expand") {

                @Override
                public void onClick(AjaxRequestTarget target) {
                    patModel.expand(viewport.getFilter().isLatestStudiesFirst());
                    if (target != null) {
                        target.addComponent(item);
                    }
                }

                @Override
                public boolean isVisible() {
                    return patModel.isCollapsed();
                }
            });
            item.add(cell);
            item.add(new Label("name").add(tooltipBehaviour));
            item.add(new Label("id").add(tooltipBehaviour));
            item.add(new Label("issuer").add(tooltipBehaviour));
            item.add(new Label("birthdate").add(tooltipBehaviour));
            item.add(new Label("sex").add(tooltipBehaviour));
            item.add(new Label("comments").add(tooltipBehaviour));
            item.add(new Label("pk").add(new TooltipBehaviour("folder.","patPk")));
            item.add(new AjaxFallbackLink("toggledetails") {
                @Override
                public void onClick(AjaxRequestTarget target) {
                    patModel.setDetails(!patModel.isDetails());
                    if (target != null) {
                        target.addComponent(item);
                    }
                }

            }.add(new TooltipBehaviour("folder.","patDetail")));
            item.add( new Link("edit") {
                
                @Override
                public void onClick() {
                    setResponsePage(
                            new EditDicomObjectPage(StudyListPage.this.getPage(), patModel));
                }
            }.add(new TooltipBehaviour("folder.","patEdit")));
            item.add(new CheckBox("selected").add(new TooltipBehaviour("folder.","patSelect")));
            WebMarkupContainer details = new WebMarkupContainer("details") {
                
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

    private final class StudyListView extends PropertyListView {

        private final ListItem patientListItem;

        private StudyListView(String id, List<StudyModel> list,
                ListItem patientListItem) {
            super(id, list);
            this.patientListItem = patientListItem;
        }

        @Override
        protected void populateItem(final ListItem item) {
            final StudyModel studyModel = (StudyModel) item.getModelObject();
            WebMarkupContainer cell = new WebMarkupContainer("cell"){
                @Override
                protected void onComponentTag(ComponentTag tag) {
                   super.onComponentTag(tag);
                   tag.put("rowspan", studyModel.getRowspan());
                }
            };
            cell.add(new AjaxFallbackLink("collapse") {

                @Override
                public void onClick(AjaxRequestTarget target) {
                    studyModel.collapse();
                    if (target != null) {
                        target.addComponent(patientListItem);
                    }
                }

                @Override
                public boolean isVisible() {
                    return !studyModel.isCollapsed();
                }
            });
            cell.add(new AjaxFallbackLink("expand") {

                @Override
                public void onClick(AjaxRequestTarget target) {
                    studyModel.expand();
                    if (target != null) {
                        target.addComponent(patientListItem);
                    }
                }

                @Override
                public boolean isVisible() {
                    return studyModel.isCollapsed();
                }
            });
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
            item.add(new AjaxFallbackLink("toggledetails") {

                @Override
                public void onClick(AjaxRequestTarget target) {
                    studyModel.setDetails(!studyModel.isDetails());
                    if (target != null) {
                        target.addComponent(patientListItem);
                    }
                }

            }.add(new TooltipBehaviour("folder.","studyDetail")));
            item.add( new Link("edit") {
                
                @Override
                public void onClick() {
                    setResponsePage(
                            new EditDicomObjectPage(StudyListPage.this.getPage(), studyModel));
                }
            }.add(new TooltipBehaviour("folder.","studyEdit")));
            item.add( new CheckBox("selected").add(new TooltipBehaviour("folder.","studySelect")));
            WebMarkupContainer details = new WebMarkupContainer("details") {
                
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

    private final class PPSListView extends PropertyListView {

        private final ListItem patientListItem;

        private PPSListView(String id, List<PPSModel> list,
                ListItem patientListItem) {
            super(id, list);
            this.patientListItem = patientListItem;
        }

        @Override
        protected void populateItem(final ListItem item) {
            final PPSModel ppsModel = (PPSModel) item.getModelObject();
            WebMarkupContainer cell = new WebMarkupContainer("cell"){
                @Override
                protected void onComponentTag(ComponentTag tag) {
                   super.onComponentTag(tag);
                   tag.put("rowspan", ppsModel.getRowspan());
                }
            };
            cell.add(new AjaxFallbackLink("collapse") {

                @Override
                public void onClick(AjaxRequestTarget target) {
                    ppsModel.collapse();
                    if (target != null) {
                        target.addComponent(patientListItem);
                    }
                }

                @Override
                public boolean isVisible() {
                    return !ppsModel.isCollapsed();
                }
            });
            cell.add(new AjaxFallbackLink("expand") {

                @Override
                public void onClick(AjaxRequestTarget target) {
                    ppsModel.expand();
                    if (target != null) {
                        target.addComponent(patientListItem);
                    }
                }

                @Override
                public boolean isVisible() {
                    return ppsModel.isCollapsed();
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
            item.add(new AjaxFallbackLink("toggledetails") {

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
            }.add(new TooltipBehaviour("folder.","ppsDetail")));
            item.add(new Link("edit") {

                @Override
                public void onClick() {
                    setResponsePage(new EditDicomObjectPage(StudyListPage.this.getPage(), ppsModel));
                }

                @Override
                public boolean isVisible() {
                    return ppsModel.getDataset() != null;
                }
            }.add(new TooltipBehaviour("folder.","ppsEdit")));
            item.add(new CheckBox("selected").add(new TooltipBehaviour("folder.","ppsSelect")));
            WebMarkupContainer details = new WebMarkupContainer("details") {
                
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

    private final class SeriesListView extends PropertyListView {

        private final ListItem patientListItem;

        private SeriesListView(String id, List<SeriesModel> list,
                ListItem patientListItem) {
            super(id, list);
            this.patientListItem = patientListItem;
        }

        @Override
        protected void populateItem(final ListItem item) {
            final SeriesModel seriesModel = (SeriesModel) item.getModelObject();
            WebMarkupContainer cell = new WebMarkupContainer("cell"){
                @Override
                protected void onComponentTag(ComponentTag tag) {
                   super.onComponentTag(tag);
                   tag.put("rowspan", seriesModel.getRowspan());
                }
            };
            cell.add(new AjaxFallbackLink("collapse") {

                @Override
                public void onClick(AjaxRequestTarget target) {
                    seriesModel.collapse();
                    if (target != null) {
                        target.addComponent(patientListItem);
                    }
                }

                @Override
                public boolean isVisible() {
                    return !seriesModel.isCollapsed();
                }
            });
            cell.add(new AjaxFallbackLink("expand") {

                @Override
                public void onClick(AjaxRequestTarget target) {
                    seriesModel.expand();
                    if (target != null) {
                        target.addComponent(patientListItem);
                    }
                }

                @Override
                public boolean isVisible() {
                    return seriesModel.isCollapsed();
                }
            });
            item.add(cell);
            item.add(new Label("datetime").add(new TooltipBehaviour("folder.series","DateTime")));
            item.add(new Label("seriesNumber").add(new TooltipBehaviour("folder.","seriesNumber")));
            item.add(new Label("sourceAET").add(new TooltipBehaviour("folder.","sourceAET")));
            item.add(new Label("modality").add(new TooltipBehaviour("folder.series","Modality")));
            item.add(new Label("description").add(new TooltipBehaviour("folder.series","Description")));
            item.add(new Label("numberOfInstances").add(new TooltipBehaviour("folder.series","NoI")));
            item.add(new Label("availability").add(new TooltipBehaviour("folder.series","Availability")));
            item.add(new Label("pk").add(new TooltipBehaviour("folder.", "seriesPk")));
            item.add(new AjaxFallbackLink("toggledetails") {

                @Override
                public void onClick(AjaxRequestTarget target) {
                    seriesModel.setDetails(!seriesModel.isDetails());
                    if (target != null) {
                        target.addComponent(patientListItem);
                    }
                }

            }.add(new TooltipBehaviour("folder.","seriesDetail")));
            item.add(new Link("edit") {
                
                @Override
                public void onClick() {
                    setResponsePage(new EditDicomObjectPage(StudyListPage.this.getPage(), seriesModel));
                }
            }.add(new TooltipBehaviour("folder.","seriesEdit")));
            item.add(new CheckBox("selected").add(new TooltipBehaviour("folder.","seriesSelect")));
            WebMarkupContainer details = new WebMarkupContainer("details") {
                
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

    private final class InstanceListView extends PropertyListView {

        private final ListItem patientListItem;

        private InstanceListView(String id, List<InstanceModel> list,
                ListItem patientListItem) {
            super(id, list);
            this.patientListItem = patientListItem;
        }

        @Override
        protected void populateItem(final ListItem item) {
            final InstanceModel instModel = (InstanceModel) item.getModelObject();
            WebMarkupContainer cell = new WebMarkupContainer("cell"){
                @Override
                protected void onComponentTag(ComponentTag tag) {
                   super.onComponentTag(tag);
                   tag.put("rowspan", instModel.getRowspan());
                }
            };
            cell.add(new AjaxFallbackLink("collapse") {

                @Override
                public void onClick(AjaxRequestTarget target) {
                    instModel.collapse();
                    if (target != null) {
                        target.addComponent(patientListItem);
                    }
                }

                @Override
                public boolean isVisible() {
                    return !instModel.isCollapsed();
                }
            });
            cell.add(new AjaxFallbackLink("expand") {

                @Override
                public void onClick(AjaxRequestTarget target) {
                    instModel.expand();
                    if (target != null) {
                        target.addComponent(patientListItem);
                    }
                }

                @Override
                public boolean isVisible() {
                    return instModel.isCollapsed();
                }
            });
            item.add(cell);
            item.add(new Label("datetime").add(new TooltipBehaviour("folder.instance","DateTime")));
            item.add(new Label("instanceNumber").add(new TooltipBehaviour("folder.","instanceNumber")));
            item.add(new Label("sopClassUID").add(new TooltipBehaviour("folder.","sopClassUID")));
            item.add(new Label("description").add(new TooltipBehaviour("folder.instance","Description")));
            item.add(new Label("availability").add(new TooltipBehaviour("folder.instance","Availability")));
            item.add(new Label("pk").add(new TooltipBehaviour("folder.", "instancePk")));
            item.add(new AjaxFallbackLink("toggledetails") {

                @Override
                public void onClick(AjaxRequestTarget target) {
                    instModel.setDetails(!instModel.isDetails());
                    if (target != null) {
                        target.addComponent(patientListItem);
                    }
                }

            }.add(new TooltipBehaviour("folder.","instanceDetail")));
            item.add(new Link("edit") {
                
                @Override
                public void onClick() {
                    setResponsePage(new EditDicomObjectPage(StudyListPage.this.getPage(), instModel));
                }
            }.add(new TooltipBehaviour("folder.","instanceEdit")));
            item.add(new CheckBox("selected").add(new TooltipBehaviour("folder.","instanceSelect")));
            WebMarkupContainer details = new WebMarkupContainer("details") {
                
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

    private final static class FileListView extends PropertyListView {

        private final ListItem patientListItem;

        private FileListView(String id, List<FileModel> list,
                ListItem patientListItem) {
            super(id, list);
            this.patientListItem = patientListItem;
        }

        @Override
        protected void populateItem(final ListItem item) {
            final FileModel fileModel = (FileModel) item.getModelObject();
            item.add(new DateTimeLabel("file.createdTime").add(new TooltipBehaviour("folder.file.","createdTime")));
            item.add(new Label("file.fileSize").add(new TooltipBehaviour("folder.file.","fileSize")));
            item.add(new Label("file.transferSyntaxUID").add(new TooltipBehaviour("folder.file.","transferSyntaxUID")));
            item.add(new Label("file.fileSystem.directoryPath").add(new TooltipBehaviour("folder.file.fileSystem.","directoryPath")));
            item.add(new Label("file.filePath").add(new TooltipBehaviour("folder.file.","filePath")));
            item.add(new Label("file.fileSystem.availability").add(new TooltipBehaviour("folder.file.fileSystem.","availability")));
            item.add(new Label("file.pk").add(new TooltipBehaviour("folder.","filePk")));
            item.add(new AjaxFallbackLink("toggledetails") {

                @Override
                public void onClick(AjaxRequestTarget target) {
                    fileModel.setDetails(!fileModel.isDetails());
                    if (target != null) {
                        target.addComponent(patientListItem);
                    }
                }

            }.add(new TooltipBehaviour("folder.","fileDetail")));
            item.add(new CheckBox("selected").add(new TooltipBehaviour("folder.","fileSelect")));
            WebMarkupContainer details = new WebMarkupContainer("details") {
                
                @Override
                public boolean isVisible() {
                    return fileModel.isDetails();
                }
                
            };
            item.add(details);
            details.add(new FilePanel("file", fileModel));
        }
    }
}
