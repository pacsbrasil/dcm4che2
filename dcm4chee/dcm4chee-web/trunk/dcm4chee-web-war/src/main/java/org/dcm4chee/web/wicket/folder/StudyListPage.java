package org.dcm4chee.web.wicket.folder;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.PageParameters;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.PropertyListView;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.validation.validator.PatternValidator;
import org.dcm4chee.archive.entity.Patient;
import org.dcm4chee.archive.entity.Study;
import org.dcm4chee.web.dao.StudyListLocal;
import org.dcm4chee.web.wicket.WicketSession;
import org.dcm4chee.web.wicket.util.JNDIUtils;

public class StudyListPage extends WebPage {

    private static int PAGESIZE = 10;

    private ViewPort viewport = ((WicketSession) getSession()).getViewPort();
    private List<String> sourceAETs = new ArrayList<String>();
    private List<String> modalities = new ArrayList<String>();

    public StudyListPage(final PageParameters parameters) {
        Form form = new Form("form", new CompoundPropertyModel(viewport.getFilter()));
        add(form);
        form.add(new FeedbackPanel("feedback"));
        form.add(new TextField("patientName"));
        form.add(new TextField("patientID"));
        form.add(new TextField("issuerOfPatientID"));
        PatternValidator datePatternValidator = new PatternValidator(
                "\\*|(((19)|(20))\\d{2}-((0[1-9])|(1[0-2]))-((0[1-9])|([12]\\d)|(3[01])))");
        form.add(new TextField("studyDateMin").add(datePatternValidator));
        form.add(new TextField("studyDateMax").add(datePatternValidator));
        form.add(new TextField("accessionNumber"));
        form.add(new TextField("studyInstanceUID"));
        form.add(new DropDownChoice("modality", modalities));
        form.add(new DropDownChoice("sourceAET", sourceAETs));
        form.add(new CheckBox("patientsWithoutStudies"));
        form.add(new CheckBox("latestStudiesFirst"));
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
        form.add(new Label("viewport", new Model(){

            @Override
            public Object getObject() {
                return viewport.getTotal() == 0 ? "No matching studies found!"
                        : MessageFormat.format(
                                "Displaying studies {0} to {1} of {2} matching studies",
                                viewport.getOffset()+1,
                                Math.min(viewport.getOffset()+PAGESIZE, viewport.getTotal()),
                                viewport.getTotal());
            }}));
        form.add(new StudyListHeader("thead"));
        form.add(new PatientListView("patients", viewport.getPatients()));
        initModalitiesAndSourceAETs();
    }

    private void initModalitiesAndSourceAETs() {
        try {
            StudyListLocal dao = (StudyListLocal)
                    JNDIUtils.lookup(StudyListLocal.JNDI_NAME);
            modalities.clear();
            modalities.add("*");
            modalities.addAll(dao.selectDistinctModalities());
            sourceAETs.clear();
            sourceAETs.add("*");
            sourceAETs.addAll(dao.selectDistinctSourceAETs());
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void queryStudies() {
        try {
            StudyListLocal dao = (StudyListLocal)
                    JNDIUtils.lookup(StudyListLocal.JNDI_NAME);
            viewport.setTotal(dao.countStudies(viewport.getFilter()));
            updatePatients(dao.findStudies(viewport.getFilter(), PAGESIZE, viewport.getOffset()));
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void updatePatients(List<Object[]> patientAndStudies)
            throws IOException {
        retainSelectedPatients();
        for (Object[] patientAndStudy : patientAndStudies) {
            PatientModel patientModel = addPatient((Patient) patientAndStudy[0]);
            if (patientAndStudy[1] != null) {
                addStudy((Study) patientAndStudy[1], patientModel);
            }
        }
    }

    private boolean addStudy(Study study, PatientModel patient)
            throws IOException {
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

    private PatientModel addPatient(Patient patient) throws IOException {
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
                    try {
                        patModel.expand(viewport.getFilter().isLatestStudiesFirst());
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
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
            item.add(new Label("name"));
            item.add(new Label("id"));
            item.add(new Label("issuer"));
            item.add(new Label("birthdate"));
            item.add(new Label("sex"));
            item.add(new AjaxFallbackLink("toggledetails") {

                @Override
                public void onClick(AjaxRequestTarget target) {
                    patModel.setDetails(!patModel.isDetails());
                    if (target != null) {
                        target.addComponent(item);
                    }
                }

            });
            item.add(new Link("edit") {
                
                @Override
                public void onClick() {
                    setResponsePage(
                            new EditPatientPage(StudyListPage.this, patModel));
                }
            });
            item.add(new CheckBox("selected"));
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
                    try {
                        studyModel.expand();
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
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
            item.add(new Label("datetime"));
            item.add(new Label("accessionNumber"));
            item.add(new Label("modalities"));
            item.add(new Label("description"));
            item.add(new Label("numberOfSeries"));
            item.add(new Label("numberOfInstances"));
            item.add(new AjaxFallbackLink("toggledetails") {

                @Override
                public void onClick(AjaxRequestTarget target) {
                    studyModel.setDetails(!studyModel.isDetails());
                    if (target != null) {
                        target.addComponent(patientListItem);
                    }
                }

            });
            item.add(new Link("edit") {
                
                @Override
                public void onClick() {
                    setResponsePage(
                            new EditStudyPage(StudyListPage.this, studyModel));
                }
            });
            item.add(new CheckBox("selected"));
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
                    try {
                        ppsModel.expand();
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
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
            item.add(new Label("datetime"));
            item.add(new Label("id"));
            item.add(new Label("modality"));
            item.add(new Label("description"));
            item.add(new Label("numberOfSeries"));
            item.add(new Label("numberOfInstances"));
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
            });
            item.add(new Link("edit") {

                @Override
                public void onClick() {
                    setResponsePage(new EditPPSPage(StudyListPage.this, ppsModel));
                }

                @Override
                public boolean isVisible() {
                    return ppsModel.getDataset() != null;
                }
            });
            item.add(new CheckBox("selected"));
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
                    try {
                        seriesModel.expand();
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
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
            item.add(new Label("datetime"));
            item.add(new Label("seriesNumber"));
            item.add(new Label("modality"));
            item.add(new Label("description"));
            item.add(new Label("numberOfInstances"));
            item.add(new AjaxFallbackLink("toggledetails") {

                @Override
                public void onClick(AjaxRequestTarget target) {
                    seriesModel.setDetails(!seriesModel.isDetails());
                    if (target != null) {
                        target.addComponent(patientListItem);
                    }
                }

            });
            item.add(new Link("edit") {
                
                @Override
                public void onClick() {
                    setResponsePage(new EditSeriesPage(StudyListPage.this, seriesModel));
                }
            });
            item.add(new CheckBox("selected"));
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
                    try {
                        instModel.expand();
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
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
            item.add(new Label("datetime"));
            item.add(new Label("instanceNumber"));
            item.add(new Label("sopClassAsString"));
            item.add(new Label("description"));
            item.add(new AjaxFallbackLink("toggledetails") {

                @Override
                public void onClick(AjaxRequestTarget target) {
                    instModel.setDetails(!instModel.isDetails());
                    if (target != null) {
                        target.addComponent(patientListItem);
                    }
                }

            });
            item.add(new Link("edit") {
                
                @Override
                public void onClick() {
                    setResponsePage(new EditInstancePage(StudyListPage.this, instModel));
                }
            });
            item.add(new CheckBox("selected"));
            WebMarkupContainer details = new WebMarkupContainer("details") {
                
                @Override
                public boolean isVisible() {
                    return instModel.isDetails();
                }
                
            };
            item.add(details);
            details.add(new DicomObjectPanel("dicomobject", instModel.getDataset(), false));
            item.add(new FileListView("files", instModel.getFiles()));
        }
    }

    private final static class FileListView extends PropertyListView {

        private FileListView(String id, List<FileModel> list) {
            super(id, list);
        }

        @Override
        protected void populateItem(final ListItem item) {
            item.add(new Label("datetime"));
            item.add(new Label("size"));
            item.add(new Label("transferSyntaxAsString"));
            item.add(new Label("path"));
        }
    }
}
