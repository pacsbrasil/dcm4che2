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
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.PropertyListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.validation.validator.PatternValidator;
import org.dcm4chee.archive.entity.Patient;
import org.dcm4chee.archive.entity.Study;
import org.dcm4chee.archive.util.JNDIUtils;
import org.dcm4chee.web.dao.StudyListLocal;
import org.dcm4chee.web.wicket.WicketSession;
import org.dcm4chee.web.wicket.common.ComponentUtil;
import org.dcm4chee.web.wicket.common.DateTimeLabel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StudyListPage extends Panel {

    private static final String MODULE_NAME = "folder";
    private static final long serialVersionUID = 1L;
    private static int PAGESIZE = 10;
    private static ComponentUtil util = new ComponentUtil(StudyListPage.getModuleName());
    private ViewPort viewport = ((WicketSession) getSession()).getViewPort();
    private List<String> sourceAETs = new ArrayList<String>();
    private List<String> modalities = new ArrayList<String>();
    private boolean notSearched = true;
    
    private static Logger log = LoggerFactory.getLogger(StudyListPage.class);

    public StudyListPage(final String id) {
        super(id);
        Form form = new Form("form", new CompoundPropertyModel(viewport.getFilter()));
        add(form);
        util.addLabeledTextField(form, "patientName");
        util.addLabel(form, "patientIDDescr");
        util.addLabeledTextField(form, "patientID");
        util.addLabeledTextField(form, "issuerOfPatientID");
        PatternValidator datePatternValidator = new PatternValidator(
                "\\*|(((19)|(20))\\d{2}-((0[1-9])|(1[0-2]))-((0[1-9])|([12]\\d)|(3[01])))");
        util.addLabel(form, "studyDate");
        util.addLabeledTextField(form, "studyDateMin").add(datePatternValidator);
        util.addLabeledTextField(form, "studyDateMax").add(datePatternValidator);
        util.addLabeledTextField(form, "accessionNumber");
        util.addLabeledTextField(form, "studyInstanceUID");
        util.addLabeledDropDownChoice(form, "modality", null, modalities);
        util.addLabeledDropDownChoice(form, "sourceAET", null, sourceAETs);
        util.addLabeledCheckBox(form, "patientsWithoutStudies", null);
        util.addLabeledCheckBox(form, "latestStudiesFirst", null);
        
        form.add(new Button("search") {

            @Override
            public void onSubmit() {
                viewport.setOffset(0);
                queryStudies();
            }});
        form.add(new Button("prev") {

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
        form.add(new Button("next") {

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
            item.add(util.addDescription(new Label("name")));
            item.add(util.addDescription(new Label("id")));
            item.add(util.addDescription(new Label("issuer")));
            item.add(util.addDescription(new Label("birthdate")));
            item.add(util.addDescription(new Label("sex")));
            item.add(util.addDescription(new Label("comments")));
            item.add(util.addDescription(new Label("pk"), "patpk"));
            item.add(util.addDescription(new AjaxFallbackLink("toggledetails") {

                @Override
                public void onClick(AjaxRequestTarget target) {
                    patModel.setDetails(!patModel.isDetails());
                    if (target != null) {
                        target.addComponent(item);
                    }
                }

            }, "patDetail") );
            item.add(util.addDescription( new Link("edit") {
                
                @Override
                public void onClick() {
                    setResponsePage(
                            new EditPatientPage(StudyListPage.this.getPage(), patModel));
                }
            }, "patEdit") );
            item.add(util.addDescription( new CheckBox("selected"), "patSelect"));
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
            item.add(util.addDescription(new Label("datetime"), "studydatetime"));
            item.add(util.addDescription(new Label("id"), "studyId"));
            item.add(util.addDescription(new Label("accessionNumber")));
            item.add(util.addDescription(new Label("modalities")));
            item.add(util.addDescription(new Label("description"), "studyDescription"));
            item.add(util.addDescription(new Label("numberOfSeries"), "studyNoS"));
            item.add(util.addDescription(new Label("numberOfInstances"), "studyNoI"));
            item.add(util.addDescription(new Label("availability"), "studyAvailability"));
            item.add(util.addDescription(new Label("pk"), "studyPk"));
            item.add(util.addDescription(new AjaxFallbackLink("toggledetails") {

                @Override
                public void onClick(AjaxRequestTarget target) {
                    studyModel.setDetails(!studyModel.isDetails());
                    if (target != null) {
                        target.addComponent(patientListItem);
                    }
                }

            }, "studyDetail"));
            item.add( util.addDescription( new Link("edit") {
                
                @Override
                public void onClick() {
                    setResponsePage(
                            new EditStudyPage(StudyListPage.this.getPage(), studyModel));
                }
            }, "studyEdit"));
            item.add( util.addDescription( new CheckBox("selected"), "studySelect"));
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
            item.add(util.addDescription(new Label("datetime"),"ppsDateTime"));
            item.add(util.addDescription(new Label("id"),"ppsId"));
            item.add(util.addDescription(new Label("spsid")));
            item.add(util.addDescription(new Label("modality"), "spsModality"));
            item.add(util.addDescription(new Label("description"),"ppsDescription"));
            item.add(util.addDescription(new Label("numberOfSeries"),"ppsNoS"));
            item.add(util.addDescription(new Label("numberOfInstances"),"ppsNoI"));
            item.add(util.addDescription(new Label("status"),"ppsStatus"));
            item.add(util.addDescription(new Label("pk"),"ppsPk"));
            item.add(util.addDescription(new AjaxFallbackLink("toggledetails") {

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
            },"ppsDetail"));
            item.add(util.addDescription(new Link("edit") {

                @Override
                public void onClick() {
                    setResponsePage(new EditPPSPage(StudyListPage.this.getPage(), ppsModel));
                }

                @Override
                public boolean isVisible() {
                    return ppsModel.getDataset() != null;
                }
            },"ppsEdit"));
            item.add(util.addDescription(new CheckBox("selected"),"ppsSelect"));
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
            item.add(util.addDescription(new Label("datetime"), "seriesDatetime"));
            item.add(util.addDescription(new Label("seriesNumber")));
            item.add(util.addDescription(new Label("sourceAET")));
            item.add(util.addDescription(new Label("modality"), "seriesModality"));
            item.add(util.addDescription(new Label("description"), "seriesDescription"));
            item.add(util.addDescription(new Label("numberOfInstances"), "seriesNoI"));
            item.add(util.addDescription(new Label("availability"), "seriesAvailability"));
            item.add(util.addDescription(new Label("pk"), "seriesPk"));
            item.add(util.addDescription(new AjaxFallbackLink("toggledetails") {

                @Override
                public void onClick(AjaxRequestTarget target) {
                    seriesModel.setDetails(!seriesModel.isDetails());
                    if (target != null) {
                        target.addComponent(patientListItem);
                    }
                }

            }, "seriesDetail"));
            item.add(util.addDescription(new Link("edit") {
                
                @Override
                public void onClick() {
                    setResponsePage(new EditSeriesPage(StudyListPage.this.getPage(), seriesModel));
                }
            }, "seriesEdit"));
            item.add(util.addDescription(new CheckBox("selected"), "seriesSelect"));
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
            item.add(util.addDescription(new Label("datetime"), "instanceDatetime"));
            item.add(util.addDescription(new Label("instanceNumber")));
            item.add(util.addDescription(new Label("sopClassUID")));
            item.add(util.addDescription(new Label("description"), "instanceDescription"));
            item.add(util.addDescription(new Label("availability"), "instanceAvailability"));
            item.add(util.addDescription(new Label("pk"), "instancePk"));
            item.add(util.addDescription(new AjaxFallbackLink("toggledetails") {

                @Override
                public void onClick(AjaxRequestTarget target) {
                    instModel.setDetails(!instModel.isDetails());
                    if (target != null) {
                        target.addComponent(patientListItem);
                    }
                }

            }, "instanceDetail"));
            item.add(util.addDescription(new Link("edit") {
                
                @Override
                public void onClick() {
                    setResponsePage(new EditInstancePage(StudyListPage.this.getPage(), instModel));
                }
            }, "instanceEdit"));
            item.add(util.addDescription(new CheckBox("selected"), "instanceSelect"));
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
            item.add(util.addDescription(new DateTimeLabel("file.createdTime")));
            item.add(util.addDescription(new Label("file.fileSize")));
            item.add(util.addDescription(new Label("file.transferSyntaxUID")));
            item.add(util.addDescription(new Label("file.fileSystem.directoryPath")));
            item.add(util.addDescription(new Label("file.filePath")));
            item.add(util.addDescription(new Label("file.fileSystem.availability")));
            item.add(util.addDescription(new Label("file.pk")));
            item.add(util.addDescription(new AjaxFallbackLink("toggledetails") {

                @Override
                public void onClick(AjaxRequestTarget target) {
                    fileModel.setDetails(!fileModel.isDetails());
                    if (target != null) {
                        target.addComponent(patientListItem);
                    }
                }

            }, "fileDetail"));
            item.add(util.addDescription(new CheckBox("selected"), "fileSelect"));
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
