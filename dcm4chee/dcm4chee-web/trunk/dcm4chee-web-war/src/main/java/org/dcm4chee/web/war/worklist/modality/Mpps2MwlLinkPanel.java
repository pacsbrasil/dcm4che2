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

import java.io.IOException;
import java.util.Date;
import java.util.List;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.ReflectionException;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.link.PopupCloseLink.ClosePopupPage;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.dcm4chee.icons.ImageManager;
import org.dcm4chee.icons.behaviours.ImageSizeBehaviour;
import org.dcm4chee.web.common.markup.BaseForm;
import org.dcm4chee.web.dao.worklist.modality.ModalityWorklistFilter;
import org.dcm4chee.web.war.folder.ContentEditDelegate;
import org.dcm4chee.web.war.folder.model.PPSModel;
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
    
    private List<PPSModel> ppsModels;

    private static Logger log = LoggerFactory.getLogger(Mpps2MwlLinkPanel.class);
    
    public Mpps2MwlLinkPanel(final String id, List<PPSModel> ppsModels) {
        super(id);
        this.ppsModels = ppsModels;
        addMppsInfoPanel();
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
        form.addLabeledTextField("patientName", enabledModel);
        form.addLabel("patientIDDescr");
        form.addLabeledTextField("patientID", enabledModel);
        form.addLabeledTextField("issuerOfPatientID", enabledModel);
        
        form.addLabel("startDate");
        form.addLabeledDateTimeField("startDateMin", new PropertyModel<Date>(filter, "startDateMin"), enabledModel);
        form.addLabeledDateTimeField("startDateMax", new PropertyModel<Date>(filter, "startDateMax"), enabledModel);

        form.addLabeledTextField("accessionNumber", enabledModel);
        form.addLabeledDropDownChoice("modality", null, getModalityChoices(), enabledModel);
        form.addLabeledDropDownChoice("scheduledStationAET", null, getStationAETChoices(), enabledModel);
        form.addLabeledDropDownChoice("scheduledStationName", null, getStationNameChoices(), enabledModel);
        form.addLabeledDropDownChoice("scheduledProcedureStepStatus", null, getSpsStatusChoices(), enabledModel);
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
        }.add(new Image("linkImg",ImageManager.IMAGE_INSERT_LINK)
        .add(new ImageSizeBehaviour())));
    }    
    
    private void addMppsInfoPanel() {
        WebMarkupContainer p = new WebMarkupContainer("mppsInfo");
        PPSModel ppsModel = ppsModels.get(0);
        p.add(new Label("mppsInfoTitle", "MPPS INFO"));
        p.add(new Label("accessionNumber", ppsModel.getAccessionNumber()));
        p.add(new Label("modality", ppsModel.getModality()));
        p.add(new Label("startDateTime", ppsModel.getDatetime()));
        add(p);
    }
    public static String getModuleName() {
        return "mpps2mwl";
    }
}
