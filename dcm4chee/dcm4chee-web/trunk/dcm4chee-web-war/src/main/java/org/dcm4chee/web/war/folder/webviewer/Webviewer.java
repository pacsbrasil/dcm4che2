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

package org.dcm4chee.web.war.folder.webviewer;

import org.apache.wicket.PageMap;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.link.PopupSettings;
import org.dcm4chee.archive.entity.StudyPermission;
import org.dcm4chee.icons.ImageManager;
import org.dcm4chee.icons.behaviours.ImageSizeBehaviour;
import org.dcm4chee.web.common.behaviours.TooltipBehaviour;
import org.dcm4chee.web.common.webview.link.WebviewerLinkProvider;
import org.dcm4chee.web.war.StudyPermissionHelper;
import org.dcm4chee.web.war.common.model.AbstractDicomModel;
import org.dcm4chee.web.war.folder.model.InstanceModel;
import org.dcm4chee.web.war.folder.model.PatientModel;
import org.dcm4chee.web.war.folder.model.SeriesModel;
import org.dcm4chee.web.war.folder.model.StudyModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Franz Willer <franz.willer@gmail.com>
 * @version $Revision$ $Date$
 * @since May 24, 2011
 */
public class Webviewer  {

    private static final String WEBVIEW_ID = "webview";
    private static Logger log = LoggerFactory.getLogger(Webviewer.class);

    public static AbstractLink getLink(final AbstractDicomModel model, final WebviewerLinkProvider[] providers,
            final StudyPermissionHelper studyPermissionHelper, TooltipBehaviour tooltip) {
        if (providers == null || providers.length < 2) {
            String url = isModelSupported(model, providers) ? getUrlForModel(model, providers[0]) : null;
            ExternalLink link = new ExternalLink(WEBVIEW_ID, url == null ? "http://dummy" : url);
            boolean visible = false;
            if (url != null) {
                if (model instanceof PatientModel) {
                    visible =  studyPermissionHelper.checkPermission(model.getDicomModelsOfNextLevel(), 
                            StudyPermission.READ_ACTION, false);
                } else {
                    visible =  studyPermissionHelper.checkPermission(model, StudyPermission.READ_ACTION);
                }
            }
            link.setVisible(visible);
            return prepareLink(link, tooltip);
        } else {
            return getWebviewerSelectionPageLink(model, providers, studyPermissionHelper);
        }
    }

    private static AbstractLink getWebviewerSelectionPageLink(final AbstractDicomModel model, final WebviewerLinkProvider[] providers,
            final StudyPermissionHelper studyPermissionHelper) {
        log.debug("Use SelectionLINK for model:"+model);
        Link<Object> link =  new Link<Object>(WEBVIEW_ID) {
            private static final long serialVersionUID = 1L;

            @Override
            public void onClick() {
                setResponsePage(new WebviewerSelectionPage(model, providers));
            }
        };
        boolean visible = false;
        if (isModelSupported(model, providers)) {
            if (model instanceof PatientModel) {
                visible =  studyPermissionHelper.checkPermission(model.getDicomModelsOfNextLevel(), 
                        StudyPermission.READ_ACTION, false);
            } else {
                visible =  studyPermissionHelper.checkPermission(model, StudyPermission.READ_ACTION);
            }
        }
        link.setVisible(visible);
        link.setPopupSettings(new PopupSettings(PageMap.forName("webviewPage"), 
                PopupSettings.RESIZABLE|PopupSettings.SCROLLBARS))
        .add(new Image("webviewImg",ImageManager.IMAGE_FOLDER_VIEWER).add(new ImageSizeBehaviour()));
        return link;
    }

    private static ExternalLink prepareLink(ExternalLink link, TooltipBehaviour tooltip) {
        link.setPopupSettings(new PopupSettings(PageMap.forName("webviewPage"), 
                PopupSettings.RESIZABLE|PopupSettings.SCROLLBARS))
        .add(new Image("webviewImg",ImageManager.IMAGE_FOLDER_VIEWER).add(new ImageSizeBehaviour()).add(tooltip));
        return link;
    }
    
    public static String getUrlForModel(AbstractDicomModel model, WebviewerLinkProvider provider) {
        switch (model.levelOfModel()) {
            case AbstractDicomModel.PATIENT_LEVEL:
                if (provider.supportPatientLevel()) {
                    PatientModel pat = (PatientModel) model;
                    return provider.getUrlForPatient(pat.getId(), pat.getId());
                }
                break;
            case AbstractDicomModel.STUDY_LEVEL:
                if (provider.supportStudyLevel()) {
                    return provider.getUrlForStudy(((StudyModel) model).getStudyInstanceUID());
                }
                break;
            case AbstractDicomModel.SERIES_LEVEL:
                if (provider.supportSeriesLevel()) {
                    return provider.getUrlForSeries(((SeriesModel) model).getSeriesInstanceUID());
                }
                break;
            case AbstractDicomModel.INSTANCE_LEVEL:
                if (provider.supportInstanceLevel()) {
                    return provider.getUrlForInstance(((InstanceModel) model).getSOPInstanceUID());
                }
                break;
            default:
                log.warn("Level of Model not supported by this Webviewer Selection Page! model:"+
                        model.getClass().getName()+" level:"+model.levelOfModel());
                return null;
        }
        log.info("WebviewerProvider "+provider.getName()+" doesn't support DICOM model with level:"+model.levelOfModel());
        return null;
    }

    public static boolean isModelSupported(AbstractDicomModel model, WebviewerLinkProvider[] providers) {
        if (providers != null) {
            for (int i = 0 ; i < providers.length ; i++) {
                if (getUrlForModel(model, providers[i]) != null)
                    return true;
            }
        }
        return false;
    }
}
