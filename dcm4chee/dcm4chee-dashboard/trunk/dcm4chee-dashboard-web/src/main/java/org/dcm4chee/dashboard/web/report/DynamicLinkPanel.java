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
 * Portions created by the Initial Developer are Copyright (C) 2002-2005
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

package org.dcm4chee.dashboard.web.report;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Page;
import org.apache.wicket.PageParameters;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.behavior.AbstractBehavior;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.extensions.ajax.markup.html.tabs.AjaxTabbedPanel;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.link.PopupSettings;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.dcm4chee.dashboard.model.ReportModel;
import org.dcm4chee.dashboard.web.DashboardMainPage;
import org.dcm4chee.dashboard.web.WicketApplication;
import org.dcm4chee.dashboard.web.report.display.DynamicDisplayPage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Robert David <robert.david@agfa.com>
 * @version $Revision$ $Date$
 * @since 18.01.2010
 */
public class DynamicLinkPanel extends Panel {

    private static final long serialVersionUID = 1L;

    private static Logger log = LoggerFactory.getLogger(DynamicLinkPanel.class);

    private Link<Object> link;
    
    private String className;
    private ReportModel report;
    private Object modalWindow;

    public DynamicLinkPanel(String id, String className, ReportModel report, ModalWindow modalWindow) {
        super(id);

        this.className = className;
        this.report = report;
        this.modalWindow = modalWindow;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onBeforeRender() {
        super.onBeforeRender();

        try {
            add((this.link = (Link<Object>) ((Class<? extends Link<Object>>) Class.forName("org.dcm4chee.dashboard.web.report.DynamicLinkPanel$" + className)).getConstructors()[0].newInstance(new Object[] {
                    this, 
                    "report-table-link", 
                    report, 
                    this.modalWindow
            })));
            this.link.add(new Image("image"));
            
            if (link instanceof org.dcm4chee.dashboard.web.report.DynamicLinkPanel.DisplayDiagramLink
                    || link instanceof org.dcm4chee.dashboard.web.report.DynamicLinkPanel.DisplayTableLink
                    || link instanceof org.dcm4chee.dashboard.web.report.DynamicLinkPanel.DisplayDiagramAndTableLink) {
                this.link.setPopupSettings(new PopupSettings(PopupSettings.RESIZABLE | PopupSettings.SCROLLBARS));
            }
            
            this.link.add(new Label("text", (link instanceof org.dcm4chee.dashboard.web.report.DynamicLinkPanel.DisplayDiagramLink || link instanceof org.dcm4chee.dashboard.web.report.DynamicLinkPanel.DisplayDiagramAndTableLink) && report.getDiagram() != null ? 
                    new ResourceModel("dashboard.report.diagram.options.types").wrapOnAssignment(this).getObject().split(";")[report.getDiagram()] : 
                    "")
            );

            this.link.setVisible(
                    ((link instanceof org.dcm4chee.dashboard.web.report.DynamicLinkPanel.CreateOrEditReportLink
                            && (report != null))
                    || link instanceof org.dcm4chee.dashboard.web.report.DynamicLinkPanel.RemoveLink
                    || (link instanceof org.dcm4chee.dashboard.web.report.DynamicLinkPanel.DisplayDiagramLink
                            && (report != null && report.getDiagram() != null && report.getDataSource() != null && !report.getDataSource().equals("")))
                    || (link instanceof org.dcm4chee.dashboard.web.report.DynamicLinkPanel.DisplayTableLink
                            && (report != null && report.getTable() && report.getDataSource() != null && !report.getDataSource().equals("")))
                    || (link instanceof org.dcm4chee.dashboard.web.report.DynamicLinkPanel.DisplayDiagramAndTableLink
                            && (report != null && (report.getDiagram() != null || report.getTable()) && report.getDataSource() != null && !report.getDataSource().equals("")))
            ));

            Image image = (Image) this.link.get("image");
            image.add(new AttributeModifier("src", true, new AbstractReadOnlyModel() {
    
                private static final long serialVersionUID = 1L;
    
                @Override
                public Object getObject() {
                    if (link instanceof org.dcm4chee.dashboard.web.report.DynamicLinkPanel.CreateOrEditReportLink)
                        if (report == null || report.getGroupUuid() == null)
                            return "images/file.png";
                        else
                            return "images/reply.png";
                    else if (link instanceof org.dcm4chee.dashboard.web.report.DynamicLinkPanel.RemoveLink)
                        return "images/action_delete.png";
                    else if (link instanceof org.dcm4chee.dashboard.web.report.DynamicLinkPanel.DisplayTableLink)
                        return "images/application.png";
                    else if (link instanceof org.dcm4chee.dashboard.web.report.DynamicLinkPanel.DisplayDiagramAndTableLink)
                        return "images/application.png";
                    else return "";
                }
            }));
    
            // set the tooltip
            if (this.link instanceof org.dcm4chee.dashboard.web.report.DynamicLinkPanel.CreateOrEditReportLink)
                if (this.report.getGroupUuid() == null)
                    image.add(new AttributeModifier("title", true, new ResourceModel("dashboard.dynamiclink.report.create").wrapOnAssignment(this)));
            if (this.link instanceof org.dcm4chee.dashboard.web.report.DynamicLinkPanel.RemoveLink) {
                if (report.getGroupUuid() == null)
                    image.add(new AttributeModifier("title", true, new ResourceModel("dashboard.dynamiclink.report.group.remove").wrapOnAssignment(this)));
                else
                    image.add(new AttributeModifier("title", true, new ResourceModel("dashboard.dynamiclink.report.remove").wrapOnAssignment(this)));
            }
            // set the image
            if (this.link instanceof org.dcm4chee.dashboard.web.report.DynamicLinkPanel.CreateOrEditReportLink)
                image.setImageResourceReference(new ResourceReference(DynamicLinkPanel.class, "images/file.png"));
            else if (this.link instanceof org.dcm4chee.dashboard.web.report.DynamicLinkPanel.RemoveLink)
                image.setImageResourceReference(new ResourceReference(DynamicLinkPanel.class, "images/application.png"));
    
            if (this.link instanceof RemoveLink)
                if (report.getGroupUuid() == null)
                    link.add(new AttributeModifier("onclick", true, new Model<String>("return confirm('" + new ResourceModel("dashboard.dynamiclink.report.group.remove_confirmation").wrapOnAssignment(this).getObject() + "');")));
                else
                    link.add(new AttributeModifier("onclick", true, new Model<String>("return confirm('" + new ResourceModel("dashboard.dynamiclink.report.remove_confirmation").wrapOnAssignment(this).getObject() + "');")));
        } catch (Exception e) {
            log.error(this.getClass().toString() + ": " + "onBeforeRender: " + e.getMessage());
            log.debug("Exception: ", e);
            this.getApplication().getSessionStore().setAttribute(getRequest(), "exception", e);
            throw new RuntimeException();
        }
    }

    abstract private class AjaxDisplayLink extends AjaxFallbackLink<Object> {
        
        private static final long serialVersionUID = 1L;
        
        ReportModel report;
        ModalWindow modalWindow;
        
        public AjaxDisplayLink(String id, ReportModel report, ModalWindow modalWindow) {
            super(id);

            this.report = report;
            this.modalWindow = modalWindow;
        }
        
        void setAjaxDisplayProperties() {
            
            this.modalWindow.setWindowClosedCallback(new ModalWindow.WindowClosedCallback() {              
                
                private static final long serialVersionUID = 1L;

                @Override
                public void onClose(AjaxRequestTarget target) {
                    setResponsePage(DashboardMainPage.class, new PageParameters("tab=" + new ResourceModel("dashboard.tabs.tab2.number").getObject()));
                }
            });
//            .setPageCreator(new ModalWindow.PageCreator() {
//                
//                private static final long serialVersionUID = 1L;
//                
//                @Override
//                public Page createPage() {
//                    return new CreateOrEditReportPage(modalWindow, report);
//                }                
//            });
        }
        
        class DisableDefaultConfirmBehavior extends AbstractBehavior implements IHeaderContributor {

            private static final long serialVersionUID = 1L;

            @Override
            public void renderHead(IHeaderResponse response) {
                response.renderOnDomReadyJavascript ("Wicket.Window.unloadConfirmation = false");
            }
        }
    }    

    abstract private class DisplayLink extends Link<Object> {
        
        private static final long serialVersionUID = 1L;
      
        ReportModel report;
      
        public DisplayLink(String id, ReportModel report, ModalWindow modalWindow) {
            super(id);

            this.report = report;
        }
    }

    private class CreateOrEditReportLink extends AjaxDisplayLink {
        
        private static final long serialVersionUID = 1L;
        
        public CreateOrEditReportLink(String id, ReportModel report, ModalWindow modalWindow) {
            super(id, report, modalWindow);
        }
        
        @Override
        public void onClick(AjaxRequestTarget target) {

            setAjaxDisplayProperties();
            
            this.modalWindow.setPageCreator(new ModalWindow.PageCreator() {
                  
                private static final long serialVersionUID = 1L;
                  
                @Override
                public Page createPage() {
                    return new CreateOrEditReportPage(modalWindow, report);
                }                
            });

            ((ModalWindow) this.modalWindow.add(new DisableDefaultConfirmBehavior()))
            .setInitialWidth(new Integer(new ResourceModel("dashboard.dynamiclink.report.createoredit.window.width").wrapOnAssignment(this).getObject().toString()))
            .setInitialHeight(new Integer(new ResourceModel("dashboard.dynamiclink.report.createoredit.window.height").wrapOnAssignment(this).getObject().toString()))
            .show(target);
        }
    }    

    private class RemoveLink extends DisplayLink {
        
        private static final long serialVersionUID = 1L;
        
        public RemoveLink(String id, ReportModel report, ModalWindow modalWindow) {
            super(id, report, modalWindow);
        }

        @Override
        public void onClick() {
            try {
                if (this.report.getGroupUuid() == null)
                    ((WicketApplication) getApplication()).getDashboardService().deleteGroup(this.report);
                else
                    ((WicketApplication) getApplication()).getDashboardService().deleteReport(this.report);

                DashboardMainPage page = new DashboardMainPage(this.getPage().getPageParameters());
                ((AjaxTabbedPanel) page.get("tabs")).setSelectedTab(new Integer(new ResourceModel("dashboard.tabs.tab2.number").getObject()));
                setResponsePage(page);
            } catch (Exception e) {
                log.error(this.getClass().toString() + ": " + "onClick: " + e.getMessage());
                log.debug("Exception: ", e);
            }
        }
    }

    private class DisplayDiagramLink extends DisplayLink {

        private static final long serialVersionUID = 1L;

        public DisplayDiagramLink(String id, ReportModel report, ModalWindow modalWindow) {
            super(id, report, modalWindow);
        }

        @Override
        public void onClick() {
            setResponsePage(new DynamicDisplayPage(this.report, true, false));
        }
    }

    private class DisplayTableLink extends DisplayLink {
        
        public DisplayTableLink(String id, ReportModel report, ModalWindow modalWindow) {
            super(id, report, modalWindow);
        }
        
        private static final long serialVersionUID = 1L;
      
        @Override
        public void onClick() {
            setResponsePage(new DynamicDisplayPage(this.report, false, true));
        }
    }

    private class DisplayDiagramAndTableLink extends AjaxDisplayLink {

        private static final long serialVersionUID = 1L;

        public DisplayDiagramAndTableLink(String id, ReportModel report, ModalWindow modalWindow) {
            super(id, report, modalWindow);
        }

        @Override
        public void onClick(AjaxRequestTarget target) {
            setAjaxDisplayProperties();
            
            this.modalWindow.setPageCreator(new ModalWindow.PageCreator() {
                
                private static final long serialVersionUID = 1L;
                  
                @Override
                public Page createPage() {
                    return new DynamicDisplayPage(report, true, true);
                }                
            });

            ((ModalWindow) this.modalWindow.add(new DisableDefaultConfirmBehavior()))
            .setInitialWidth(new Integer(new ResourceModel("dashboard.dynamiclink.report.displaydiagramandtable.window.width").wrapOnAssignment(this).getObject().toString()))
            .setInitialHeight(new Integer(new ResourceModel("dashboard.dynamiclink.report.displaydiagramandtable.window.height").wrapOnAssignment(this).getObject().toString()))
            .show(target);
        }
    }
}
