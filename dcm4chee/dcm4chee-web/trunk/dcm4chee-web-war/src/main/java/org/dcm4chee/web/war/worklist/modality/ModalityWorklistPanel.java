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

package org.dcm4chee.web.war.worklist.modality;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.wicket.ResourceReference;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.CSSPackageResource;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.html.resources.CompressedResourceReference;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.dcm4chee.archive.common.SPSStatus;
import org.dcm4chee.archive.entity.MWLItem;
import org.dcm4chee.archive.util.JNDIUtils;
import org.dcm4chee.icons.ImageManager;
import org.dcm4chee.icons.behaviours.ImageSizeBehaviour;
import org.dcm4chee.web.common.behaviours.TooltipBehaviour;
import org.dcm4chee.web.common.markup.BaseForm;
import org.dcm4chee.web.dao.worklist.modality.ModalityWorklist;
import org.dcm4chee.web.dao.worklist.modality.ModalityWorklistFilter;
import org.dcm4chee.web.war.WicketSession;
import org.dcm4chee.web.war.common.EditDicomObjectPage;
import org.dcm4chee.web.war.folder.DicomObjectPanel;
import org.dcm4chee.web.war.worklist.modality.MWLItemListView.MwlActionProvider;
import org.dcm4chee.web.war.worklist.modality.model.MWLItemModel;

/**
 * @author Robert David <robert.david@agfa.com>
 * @version $Revision$ $Date$
 * @since 20.04.2010
 */
public class ModalityWorklistPanel extends Panel implements MwlActionProvider {

    private static final ResourceReference CSS = new CompressedResourceReference(ModalityWorklistPanel.class, "modality-worklist-style.css");

    private static final long serialVersionUID = 1L;

    // TODO: put this into .properties file
    private static int PAGESIZE = 10;
    
    private static final String MODULE_NAME = "mw";
    private ViewPort viewport;
    private boolean notSearched = true;
    private TooltipBehaviour tooltipBehaviour = new TooltipBehaviour("mw.");
    
    private transient ModalityWorklist dao;

    public ModalityWorklistPanel(final String id) {
        super(id);
        viewport = initViewPort();
        if (ModalityWorklistPanel.CSS != null)
            add(CSSPackageResource.getHeaderContribution(ModalityWorklistPanel.CSS));

        final ModalityWorklistFilter filter = viewport.getFilter();
        BaseForm form = new BaseForm("form", new CompoundPropertyModel<Object>(filter));
        form.setResourceIdPrefix("mw.");
        form.setTooltipBehaviour(tooltipBehaviour);
        add(form);
        addQueryFields(filter, form);
        addExtendedStudySearch(form);
        addQueryOptions(form);
        addNavigation(form);
        form.add(new MWLItemListView("mwlitems", viewport.getMWLItemModels(), this));
    }
    
    protected ViewPort initViewPort() {
        return ((WicketSession) getSession()).getMwViewPort();
    }

    protected void addQueryFields(final ModalityWorklistFilter filter, BaseForm form) {
        final IModel<Boolean> enabledModel = new AbstractReadOnlyModel<Boolean>(){

            private static final long serialVersionUID = 1L;

            @Override
            public Boolean getObject() {
                return !filter.isExtendedQuery() || "*".equals(filter.getStudyInstanceUID());
            }
        };
        form.addLabeledTextField("patientName", enabledModel);
        form.addLabel("patientIDDescr");
        form.addLabeledTextField("patientID", enabledModel);
        form.addLabeledTextField("issuerOfPatientID", enabledModel);
        
        form.addLabel("startDate");
        form.addLabeledDateTimeField("startDateMin", new PropertyModel<Date>(filter, "startDateMin"), enabledModel, false);
        form.addLabeledDateTimeField("startDateMax", new PropertyModel<Date>(filter, "startDateMax"), enabledModel, true);

        form.addLabeledTextField("accessionNumber", enabledModel);
        form.addLabeledDropDownChoice("modality", null, getModalityChoices(), enabledModel);
        form.addLabeledDropDownChoice("scheduledStationAET", null, getStationAETChoices(), enabledModel);
        form.addLabeledDropDownChoice("scheduledStationName", null, getStationNameChoices(), enabledModel);
        form.addLabeledDropDownChoice("scheduledProcedureStepStatus", null, getSpsStatusChoices(), enabledModel);
    }

    protected void addQueryOptions(BaseForm form) {
        form.addLabeledCheckBox("latestItemsFirst", null);
    }

    protected void addNavigation(BaseForm form) {
        form.add(new Button("search", new ResourceModel("searchBtn")) {

            private static final long serialVersionUID = 1L;

            @Override
            public void onSubmit() {
                viewport.setOffset(0);
                queryMWLItems();
            }});
        form.add(new Button("prev", new ResourceModel("mw.prev")) {

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
                queryMWLItems();
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
                queryMWLItems();
            }});
        //viewport label: use StringResourceModel with key substitution to select 
        //property key according notSearched and getTotal.
        Model<?> keySelectModel = new Model<Serializable>() {

            private static final long serialVersionUID = 1L;

            @Override
            public Serializable getObject() {
                return notSearched ? "mw.notSearched" :
                        viewport.getTotal() == 0 ? "mw.noMatchingMppsFound" : 
                            "mw.mppsFound";
            }
        };
        form.add(new Label("viewport", new StringResourceModel("${}", ModalityWorklistPanel.this, keySelectModel,new Object[]{"dummy"}){

            private static final long serialVersionUID = 1L;

            @Override
            protected Object[] getParameters() {
                return new Object[]{viewport.getOffset()+1,
                        Math.min(viewport.getOffset()+PAGESIZE, viewport.getTotal()),
                        viewport.getTotal()};
            }
        }));
    }

    protected WebMarkupContainer addExtendedStudySearch(final Form<?> form) {
        final ModalityWorklistFilter filter = viewport.getFilter();
        final WebMarkupContainer extendedStudyFilter = new WebMarkupContainer("extendedStudyFilter") {

            private static final long serialVersionUID = 1L;

            @Override
            public boolean isVisible() {
                return filter.isExtendedQuery();
            }
        };
        extendedStudyFilter.add( new Label("studyInstanceUIDLabel", new ResourceModel("mw.studyInstanceUID")));
        extendedStudyFilter.add( new TextField<String>("studyInstanceUID"));
        form.add(extendedStudyFilter);
        AjaxFallbackLink<?> link = new AjaxFallbackLink<Object>("showExtendedStudyFilter") {

            private static final long serialVersionUID = 1L;

            @Override
            public void onClick(AjaxRequestTarget target) {
                filter.setExtendedQuery(!filter.isExtendedQuery());
                target.addComponent(form);
            }};
        link.add(new Image("showExtendedStudyFilterImg", new AbstractReadOnlyModel<ResourceReference>() {

            private static final long serialVersionUID = 1L;

            @Override
            public ResourceReference getObject() {
                return filter.isExtendedQuery() ? ImageManager.IMAGE_COLLAPSE : 
                    ImageManager.IMAGE_EXPAND;
            }
        })
        .add(new ImageSizeBehaviour()));
        
        form.add(link);
        return extendedStudyFilter;
    }

    protected void queryMWLItems() {

        ModalityWorklist dao = lookupMwlDAO();
        viewport.getMWLItemModels().clear();
        viewport.setTotal(dao.countMWLItems(viewport.getFilter()));
             
        List<MWLItemModel> current = viewport.getMWLItemModels();
        for (MWLItem mwlItem : dao.findMWLItems(viewport.getFilter(), PAGESIZE, viewport.getOffset()))
            current.add(new MWLItemModel(mwlItem, new Model<Boolean>(false)));
        
        notSearched = false;
    }

    private ModalityWorklist lookupMwlDAO() {
        if (dao == null)
            dao = (ModalityWorklist) JNDIUtils.lookup(ModalityWorklist.JNDI_NAME);
        return dao;
    }

    public static String getModuleName() {
        return MODULE_NAME;
    }

    protected List<String> getModalityChoices() {
        ModalityWorklist dao = lookupMwlDAO();
        List<String> modalities = new ArrayList<String>();
        modalities.add("*");
        modalities.addAll(dao.selectDistinctModalities());
        return modalities;
    }

    protected List<String> getStationAETChoices() {
        ModalityWorklist dao = lookupMwlDAO();
        List<String> scheduledStationAETs = new ArrayList<String>();
        scheduledStationAETs.add("*");
        scheduledStationAETs.addAll(dao.selectDistinctStationAETs());
        return scheduledStationAETs;
    }

    protected List<String> getStationNameChoices() {
        ModalityWorklist dao = lookupMwlDAO();
        List<String> scheduledStationNames = new ArrayList<String>();
        scheduledStationNames.add("*");
        scheduledStationNames.addAll(dao.selectDistinctStationNames());
        return scheduledStationNames;
    }

    protected List<String> getSpsStatusChoices() {
        List<String> status = new ArrayList<String>();
        status.add("*");
        for (SPSStatus spsStatus : SPSStatus.values())
            status.add(spsStatus.toString());
        return status;
    }

    //MwlActionProvider (details and edit)
    public void addMwlActions(final ListItem<MWLItemModel> item, final MWLItemListView mwlListView) {
        final MWLItemModel mwlItemModel = item.getModelObject();
        item.add(new AjaxFallbackLink<Object>("toggledetails") {

            private static final long serialVersionUID = 1L;

            @Override
            public void onClick(AjaxRequestTarget target) {
                mwlItemModel.setDetails(!mwlItemModel.isDetails());
                if (target != null) 
                    target.addComponent(item);
            }

        }.add(new Image("detailImg",ImageManager.IMAGE_DETAIL)
        .add(new ImageSizeBehaviour()))
        .add(new TooltipBehaviour("mw.","patDetail")))
        .add( new Link<Object>("edit") {

            private static final long serialVersionUID = 1L;

            @Override
            public void onClick() {
                setResponsePage(new EditDicomObjectPage(mwlListView.getPage(), mwlItemModel));
            }
        }.add(new Image("editImg",ImageManager.IMAGE_EDIT)
        .add(new ImageSizeBehaviour()))
        .add(new TooltipBehaviour("mw.","patEdit")))
        .add(new WebMarkupContainer("details") {

            private static final long serialVersionUID = 1L;

            @Override
            public boolean isVisible() {
                return mwlItemModel.isDetails();
            }
        }
        .add(new DicomObjectPanel("dicomobject", mwlItemModel.getDataset(), false)))
        .setOutputMarkupId(true);
    }
}
