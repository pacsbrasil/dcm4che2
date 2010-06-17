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

import java.util.Date;
import java.util.List;

import org.apache.wicket.ResourceReference;
import org.apache.wicket.markup.html.CSSPackageResource;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.link.PopupCloseLink.ClosePopupPage;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.resources.CompressedResourceReference;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.dcm4chee.icons.ImageManager;
import org.dcm4chee.icons.behaviours.ImageSizeBehaviour;
import org.dcm4chee.web.common.markup.BaseForm;
import org.dcm4chee.web.common.markup.DateTimeLabel;
import org.dcm4chee.web.dao.worklist.modality.ModalityWorklistFilter;
import org.dcm4chee.web.war.folder.ContentEditDelegate;
import org.dcm4chee.web.war.folder.model.PPSModel;
import org.dcm4chee.web.war.folder.model.PatientModel;
import org.dcm4chee.web.war.folder.model.StudyModel;
import org.dcm4chee.web.war.worklist.modality.model.MWLItemModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Franz Willer <franz.willer@gmail.com>
 * @version $Revision$ $Date$
 * @since 07.05.2010
 */
public class Mpps2MwlLinkPanel extends ModalityWorklistPanel {

    private static final long serialVersionUID = 1L;
    
    private static final ResourceReference CSS = new CompressedResourceReference(Mpps2MwlLinkPanel.class, "mpps-link-style.css");
    
    private List<PPSModel> ppsModels;
    private TextField<String> tfPatName;
    private DropDownChoice<?> tfModality;

    private static Logger log = LoggerFactory.getLogger(Mpps2MwlLinkPanel.class);
    
    public Mpps2MwlLinkPanel(final String id, List<PPSModel> ppsModels) {
        super(id);
        this.ppsModels = ppsModels;
        presetSearchfields();
        addMppsInfoPanel();
        if (Mpps2MwlLinkPanel.CSS != null)
            add(CSSPackageResource.getHeaderContribution(Mpps2MwlLinkPanel.CSS));
    }

    protected ViewPort initViewPort() {
        return new ViewPort();
    }

    protected void addQueryFields(final ModalityWorklistFilter filter, BaseForm form) {
        final IModel<Boolean> enabledModel = new AbstractReadOnlyModel<Boolean>(){

            private static final long serialVersionUID = 1L;

            @Override
            public Boolean getObject() {
                return !filter.isExtendedQuery() || "*".equals(filter.getStudyInstanceUID());
            }
        };
        tfPatName = form.addTextField("patientName", enabledModel, true);
        form.addLabel("patientIDDescr");
        form.addTextField("patientID", enabledModel, true);
        form.addTextField("issuerOfPatientID", enabledModel, true);
        
        form.addLabel("startDate");
        form.addDateTimeField("startDateMin", new PropertyModel<Date>(filter, "startDateMin"), enabledModel, false, true);
        form.addDateTimeField("startDateMax", new PropertyModel<Date>(filter, "startDateMax"), enabledModel, true, true);

        form.addTextField("accessionNumber", enabledModel, true);
        tfModality = form.addDropDownChoice("modality", null, getModalityChoices(), enabledModel, true);
        form.addDropDownChoice("scheduledStationAET", null, getStationAETChoices(), enabledModel, true);
        form.addDropDownChoice("scheduledStationName", null, getStationNameChoices(), enabledModel, true);
        form.addDropDownChoice("scheduledProcedureStepStatus", null, getSpsStatusChoices(), enabledModel, true);
    }

    protected WebMarkupContainer addExtendedStudySearch(final Form<?> form) {
        return null;
    }

    public void addMwlActions(final ListItem<MWLItemModel> item, final MWLItemListView mwlListView) {
        final MWLItemModel mwlItemModel = item.getModelObject();
        item.add(new Link<Object>("link") {
            private static final long serialVersionUID = 1L;
            @Override
            public void onClick() {
                log.info("Link MPPS to MWL!:"+mwlItemModel);
                try {
                    ContentEditDelegate.getInstance().linkMppsToMwl(ppsModels, mwlItemModel);
                    setResponsePage(ClosePopupPage.class);
                } catch (Exception e) {
                    log.error("MPPS to MWL link failed!", e);
                }
            }
        }.add(new Image("linkImg",ImageManager.IMAGE_COMMON_LINK)
        .add(new ImageSizeBehaviour())));
    }    
    
    private void addMppsInfoPanel() {
        PPSModel ppsModel = ppsModels.get(0);
        StudyModel studyModel= ppsModel.getParent();
        PatientModel patModel = studyModel.getParent();
        WebMarkupContainer p = new WebMarkupContainer("mppsInfo");
        p.add(new Label("mppsInfoTitle", new ResourceModel("link.ppsInfoTitle")));
        p.add(new Label("patNameLabel", new ResourceModel("link.patNameLabel")));
        p.add(new Label("patName", patModel.getName()));
        p.add(new Label("patIdLabel", new ResourceModel("link.patIdLabel")));
        p.add(new Label("patId", patModel.getId()));
        p.add(new Label("patIssuerLabel", new ResourceModel("link.patIssuerLabel")));
        p.add(new Label("patIssuer", patModel.getIssuer()));
        p.add(new Label("modalityLabel", new ResourceModel("link.modalityLabel")));
        p.add(new Label("modality", ppsModel.getModality()));
        p.add(new Label("startDateLabel", new ResourceModel("link.startDateLabel")));
        p.add(new DateTimeLabel("datetime", new PropertyModel<Date>(ppsModel, "datetime")));
        add(p);
    }
    
    @SuppressWarnings("unchecked")
    private void presetSearchfields() {
        PPSModel ppsModel = ppsModels.get(0);
        PatientModel patModel = ppsModel.getParent().getParent();
        String name = patModel.getName();
        if (name != null && name.length() > 3)
            name = name.substring(0,4);
        tfPatName.getModel().setObject(name);
        ((IModel<String>)tfModality.getModel()).setObject(ppsModel.getModality());
        queryMWLItems();
    }

    public static String getModuleName() {
        return "mpps2mwl";
    }
}
