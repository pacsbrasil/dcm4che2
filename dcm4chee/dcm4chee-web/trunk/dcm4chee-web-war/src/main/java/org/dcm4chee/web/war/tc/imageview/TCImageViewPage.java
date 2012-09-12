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

package org.dcm4chee.web.war.tc.imageview;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.PageParameters;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.ajax.AbstractAjaxTimerBehavior;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.CSSPackageResource;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.resources.CompressedResourceReference;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.util.ListModel;
import org.apache.wicket.resource.loader.ClassStringResourceLoader;
import org.apache.wicket.security.components.SecureWebPage;
import org.apache.wicket.util.time.Duration;
import org.dcm4chee.archive.entity.Instance;
import org.dcm4chee.archive.entity.Series;
import org.dcm4chee.archive.entity.Study;
import org.dcm4chee.archive.entity.StudyPermission;
import org.dcm4chee.archive.util.JNDIUtils;
import org.dcm4chee.web.common.base.BaseWicketPage;
import org.dcm4chee.web.dao.folder.StudyListLocal;
import org.dcm4chee.web.dao.tc.TCQueryLocal;
import org.dcm4chee.web.war.StudyPermissionHelper;
import org.dcm4chee.web.war.tc.TCPanel;
import org.dcm4chee.web.war.tc.TCPopupManager;
import org.dcm4chee.web.war.tc.TCReferencedInstance;
import org.dcm4chee.web.war.tc.TCReferencedSeries;
import org.dcm4chee.web.war.tc.TCReferencedStudy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Bernhard Ableitinger <bernhard.ableitinger@gmail.com>
 * @since Jan 23, 2012
 */
public class TCImageViewPage extends SecureWebPage 
{
    private static final ResourceReference BASE_CSS = new CompressedResourceReference(
            BaseWicketPage.class, "base-style.css");
        
    private static final ResourceReference LAYOUT_CSS = new CompressedResourceReference(
            TCPanel.class, "css/tc-layout.css");
    
    private static final ResourceReference TC_CSS = new CompressedResourceReference(
            TCPanel.class, "css/tc-style.css");
    
    private static final ResourceReference THEME_CSS = new CompressedResourceReference(
            TCPanel.class, "css/theme/theme.css");
    
    protected static Logger log = LoggerFactory.getLogger(TCImageViewPage.class);

    private IModel<String> indicatingModel;
    private Label indicatingLabel;
    private Image indicatingImage;
    private WebMarkupContainer indicatingPanel;
    private WebMarkupContainer imagesPanel;
    private TCPopupManager popupManager;
    
    public TCImageViewPage(PageParameters params) {
        super();
        
        if (StudyPermissionHelper.get().isSSO())
        {
            add(new AbstractAjaxTimerBehavior(Duration.milliseconds(1)) {
                    private static final long serialVersionUID = 1L;
        
                    @Override
                    protected void onTimer(AjaxRequestTarget arg0) {
                        try {
                            StudyPermissionHelper.get().doDicomAuthentication();
                        } catch (Exception e) {
                            log.error(getClass().getName() + ": error doing dicom authentication: ", e);
                        } finally {
                            this.stop();
                        }
                    }
                }
            );
        }
        
        if (BASE_CSS != null) {
            add(CSSPackageResource.getHeaderContribution(BASE_CSS));
        }
        
        if (LAYOUT_CSS != null) {
            add(CSSPackageResource.getHeaderContribution(LAYOUT_CSS));
        }
        
        if (TC_CSS != null) {
            add(CSSPackageResource.getHeaderContribution(TC_CSS));
        }
        
        if (THEME_CSS != null) {
            add(CSSPackageResource.getHeaderContribution(THEME_CSS));
        }

        final String stuid = params!=null ? params.getString("stuid") : null;
        final String suid = params!=null ? params.getString("suid") : null;
        final String iuid = params!=null ? params.getString("iuid") : null;
        
        Label titleLabel = new Label("app_browser_title", new Model<String>(
                getLocalizedString("application.browser_title","Image Viewer")));
        
        indicatingModel = new Model<String>(getLocalizedString("loading.images.text"));
        indicatingLabel = new Label("tc-images-view-indicating-label", indicatingModel);
        indicatingLabel.setOutputMarkupId(true);
        
        indicatingImage = new Image("tc-images-view-indicating-image", 
                new ResourceReference(TCPanel.class, "theme/images/load-indicator-large.gif"));
        indicatingImage.setOutputMarkupId(true);
        
        indicatingPanel = new WebMarkupContainer("tc-images-view-indicating-panel");
        indicatingPanel.add(indicatingLabel);
        indicatingPanel.add(indicatingImage);
        indicatingPanel.setOutputMarkupId(true);
        indicatingPanel.setOutputMarkupPlaceholderTag(true);
        indicatingPanel.setVisible(true);
        
        imagesPanel = new WebMarkupContainer("tc-images-view-images-panel");
        imagesPanel.setOutputMarkupId(true);
        imagesPanel.setOutputMarkupPlaceholderTag(true);
        imagesPanel.setVisible(false);
        
        add(titleLabel);
        add(new Label("diagnosisWarning", new ResourceModel("warning.diagnosis.text"))
        	.add(new AttributeModifier("class", true, new Model<String>("diagnosis-warning"))));
        add(imagesPanel);
        add(indicatingPanel);     
        add(new StartImageLoadBehavior(stuid, suid, iuid));
        add((popupManager=new TCPopupManager()).getGlobalHideOnOutsideClickHandler());
    }
    
    private ListModel<? extends TCImageViewStudy> createImagesModelForInstanceUID(String iuid) 
        throws Exception
    {
        TCQueryLocal dao = (TCQueryLocal) JNDIUtils.lookup(TCQueryLocal.JNDI_NAME);
        Instance instance = dao.findInstanceByUID(iuid);
        if (instance!=null)
        {
            TCReferencedStudy study = new TCReferencedStudy(
                    instance.getSeries().getStudy().getStudyInstanceUID());
            TCReferencedSeries series = new TCReferencedSeries(
                    instance.getSeries().getSeriesInstanceUID(), study);
            TCReferencedInstance image = new TCReferencedInstance(series,
                    instance.getSOPInstanceUID(), instance.getSOPClassUID());

            if (image.isImage())
            {
                series.addInstance(image);
                study.addSeries(series);
                
                return new ListModel<TCReferencedStudy>(
                        Collections.singletonList(study));
            }
            else
            {
                throw new Exception("Instance " + iuid + " is not an image!");
            }
        }

        throw new Exception("Instance " + iuid + " not found!");
    }
    
    private ListModel<? extends TCImageViewStudy> createImagesModelForSeriesUID(String suid) 
        throws Exception
    {
        TCQueryLocal dao = (TCQueryLocal) JNDIUtils.lookup(TCQueryLocal.JNDI_NAME);
        Series series = dao.findSeriesByUID(suid);
        if (series!=null)
        {
            TCReferencedStudy refStudy = new TCReferencedStudy(
                    series.getStudy().getStudyInstanceUID());
            TCReferencedSeries refSeries = new TCReferencedSeries(
                    series.getSeriesInstanceUID(), refStudy);
            
            Set<Instance> instances = series.getInstances();
            if (instances!=null)
            {
                for (Instance instance : instances)
                {
                    refSeries.addInstance(new TCReferencedInstance(refSeries,
                            instance.getSOPInstanceUID(), instance.getSOPClassUID()));
                }
            }

            if (refSeries.getImageCount()>0)
            {
                refStudy.addSeries(refSeries);
                
                return new ListModel<TCReferencedStudy>(
                        Collections.singletonList(refStudy));
            }
            else
            {
                throw new Exception("Series " + suid + " doesn't contain any images!");
            }
        }

        throw new Exception("Series " + suid + " not found!");
    }
    
    private ListModel<? extends TCImageViewStudy> createImagesModelForStudyUID(String stuid) 
        throws Exception
    {
        TCQueryLocal dao = (TCQueryLocal) JNDIUtils.lookup(TCQueryLocal.JNDI_NAME);
        Study study = dao.findStudyByUID(stuid);
        if (study!=null)
        {
            boolean hasImages = false;
            
            TCReferencedStudy refStudy = new TCReferencedStudy(
                    study.getStudyInstanceUID());
            
            Set<Series> series = study.getSeries();
            if (series!=null)
            {
                for (Series s : series)
                {
                    TCReferencedSeries refSeries = new TCReferencedSeries(
                            s.getSeriesInstanceUID(), refStudy);
                    
                    refStudy.addSeries(refSeries);
                    
                    Set<Instance> instances = s.getInstances();
                    if (instances!=null)
                    {
                        for (Instance instance : instances)
                        {
                            refSeries.addInstance(new TCReferencedInstance(refSeries,
                                    instance.getSOPInstanceUID(), instance.getSOPClassUID()));
                        }
                    }
                    
                    if (!hasImages && refSeries.getImageCount()>0)
                    {
                        hasImages = true;
                    }
                }
            }
            
            if (hasImages)
            {
                return new ListModel<TCReferencedStudy>(
                        Collections.singletonList(refStudy));
            }
            else
            {
                throw new Exception("Study " + stuid + " doesn't contain any images!");
            }
        }

        throw new Exception("Study " + stuid + " not found!");
    }
    
    
    private String getLocalizedString(String key)
    {
        return getLocalizedString(key, null);
    }
    
    private String getLocalizedString(String key, String defaultValue)
    {
        ClassStringResourceLoader loader = new ClassStringResourceLoader(getClass());
        String value = loader.loadStringResource(getClass(), key, 
                getSession().getLocale(), null);
        
        return value!=null ? value : defaultValue;
    }
    
    public class StartImageLoadBehavior extends AbstractDefaultAjaxBehavior {

		private static final long serialVersionUID = 1L;
		
		private String iuid;
        private String suid;
        private String stuid;
        
        public StartImageLoadBehavior(String stuid, String suid, String iuid)
        {
            this.stuid = stuid;
            this.suid = suid;
            this.iuid = iuid;
        }
        
        @Override
        protected void respond(AjaxRequestTarget target) {
            try
            {
                if (iuid!=null || suid!=null || stuid!=null)
                {
                    ListModel<? extends TCImageViewStudy> dataModel = null;
                    
                    if (iuid!=null)
                    {
                        dataModel = createImagesModelForInstanceUID(iuid);
                    }
                    else if (suid!=null)
                    {
                        dataModel = createImagesModelForSeriesUID(suid);
                    }
                    else if (stuid!=null)
                    {
                        dataModel = createImagesModelForStudyUID(stuid);
                    }
                        
                    //check study permission
                    boolean granted = false;
                    if (StudyPermissionHelper.get().applyStudyPermissions())
                    {
                        StudyListLocal dao = (StudyListLocal) JNDIUtils.lookup(StudyListLocal.JNDI_NAME);
                        List<String> actions = dao.findStudyPermissionActions(
                                dataModel.getObject().get(0).getStudyUID(), 
                                StudyPermissionHelper.get().getDicomRoles());
                        
                        granted = actions!=null && 
                            actions.contains(StudyPermission.QUERY_ACTION) &&
                            actions.contains(StudyPermission.READ_ACTION);
                    }
                    else
                    {
                        granted = true;
                    }
                    
                    if (granted)
                    {
                        imagesPanel.add(new TCImageViewPanel(
                                "tc-images-view-images-panel-content",
                                dataModel) {

                        	private static final long serialVersionUID = 1L;

							@Override
                            protected TCPopupManager getPopupManager()
                            {
                                return popupManager;
                            }
                        });
                        
                        indicatingPanel.setVisible(false);
                        imagesPanel.setVisible(true);
                        
                        target.addComponent(indicatingPanel);
                        target.addComponent(imagesPanel);
                        target.appendJavascript("checkToLoadThumbnails();");
                    }
                    else
                    {
                        indicatingModel.setObject(getLocalizedString("no.study.permission.text"));
                        indicatingImage.setVisible(false);
                        target.addComponent(indicatingPanel);
                    }
                }
                else
                {
                    indicatingModel.setObject(getLocalizedString("no.images.defined.text"));
                    indicatingImage.setVisible(false);
                    target.addComponent(indicatingPanel);
                }
            }
            catch (Exception e)
            {
                log.warn(null, e);
                
                indicatingModel.setObject(getLocalizedString("no.images.found.text"));
                indicatingImage.setVisible(false);
                target.addComponent(indicatingPanel);
            }
            finally
            {
                target.appendJavascript("$('body').css('cursor','auto')");
            }
        }
        @Override
        public void renderHead(IHeaderResponse response) {
            response.renderOnLoadJavascript(getCallbackScript().toString());
        }
    }

}
