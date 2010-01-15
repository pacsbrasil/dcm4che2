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

package org.dcm4chee.dashboard.web;

import java.util.ArrayList;
import java.util.Arrays;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Page;
import org.apache.wicket.PageParameters;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.behavior.AbstractBehavior;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.extensions.ajax.markup.html.tabs.AjaxTabbedPanel;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.ContextImage;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.link.PopupSettings;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.pages.InternalErrorPage;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.dcm4chee.dashboard.mbean.DashboardDelegator;
import org.dcm4chee.dashboard.model.ReportModel;
import org.dcm4chee.dashboard.util.CSSUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Robert David <robert.david@agfa.com>
 * @version $Revision$ $Date$
 * @since 24.11.2009
 */
public class ReportPanel extends Panel {

    private static final long serialVersionUID = 1L;
    
    private static Logger log = LoggerFactory.getLogger(ReportPanel.class);
   
    private ModalWindow modalWindow;
    
    public ReportPanel(String id) {
        super(id);
        
        try {
            final String[] diagramOptions = new ResourceModel("dashboard.report.diagram.options").wrapOnAssignment(this).getObject().split(";");
            add(this.modalWindow = new ModalWindow("modal-window"));
            add(new CreateOrEditReportLink("add-report-link", null).add(new ContextImage("add-report-image", "images/file.png")));

            add(new ListView<ReportModel>("report-rows", ((WicketApplication) getApplication()).getDashboardService().listAllReports() != null ? Arrays.asList(((WicketApplication) getApplication()).getDashboardService().listAllReports()) : new ArrayList<ReportModel>()) {

                private static final long serialVersionUID = 1L;
                
                @SuppressWarnings("unchecked")
                @Override
                protected void populateItem(ListItem<ReportModel> item) {
                    final ReportModel report = (ReportModel) item.getModelObject();
                    
                    item.add(new Label("report-title", String.valueOf(report.getTitle())).add(new AttributeModifier("title", true, new Model<String>(report.getStatement().replace(System.getProperty("line.separator"), " ")))));                   
                    item.add(new CreateOrEditReportLink("edit-report-link", ((ReportModel) item.getModelObject())));
                    item.add(new RemoveReportLink("remove-report-link", report).add(new AttributeModifier("onclick", true, new Model<String>("return confirm('" + new ResourceModel("dashboard.report.table.remove_confirmation").wrapOnAssignment(this).getObject() + "');"))));

                    final PageParameters parameters = this.getPage().getPageParameters() != null ? this.getPage().getPageParameters() : new PageParameters();
                    parameters.add("uuid", report.getUuid());

                    item.add(new Link("report-diagram-link") {

                        private static final long serialVersionUID = 1L;

                        @Override
                        public void onClick() {
                            setResponsePage(new DisplayReportDiagramPage(report));
                        }
                    }
                    .setPopupSettings(new PopupSettings(PopupSettings.RESIZABLE | PopupSettings.SCROLLBARS))
                    .add(new Label("report-diagram-dropdown-choice-label", new Model<String>(report.getDiagram() != null ? diagramOptions[report.getDiagram()] : "")))
                    .setVisible(report.getDiagram() != null));

                    item.add(new Link("report-table-link") {

                        private static final long serialVersionUID = 1L;

                        @Override
                        public void onClick() {
                            setResponsePage(new DisplayReportTablePage(report));
                        }
                    }
                    .setPopupSettings(new PopupSettings(PopupSettings.RESIZABLE | PopupSettings.SCROLLBARS))
                    .setVisible(report.getTable()));

                    item.add(new AttributeModifier("class", true, new Model<String>(CSSUtils.getRowClass(item.getIndex()))));
                }
              });
        } catch (Exception e) {
            log.error(this.getClass().toString() + ": " + "init: " + e.getMessage());
            log.debug("Exception: ", e);
            this.redirectToInterceptPage(new InternalErrorPage());
        }
    }
    
    private final class CreateOrEditReportLink extends AjaxFallbackLink<Object> {
        
        private static final long serialVersionUID = 1L;
        
        private ReportModel forReport;
        
        public CreateOrEditReportLink(String id, ReportModel report) {
            super(id);
            this.forReport = report;
        }
        
        @Override
        public void onClick(AjaxRequestTarget target) {
            
            modalWindow.setWindowClosedCallback(new ModalWindow.WindowClosedCallback() {              
                
                private static final long serialVersionUID = 1L;

                @Override
                public void onClose(AjaxRequestTarget target) {
                    setResponsePage(DashboardMainPage.class, new PageParameters("tab=" + new ResourceModel("dashboard.tabs.tab2.number").getObject()));
                }
            })
            .setPageCreator(new ModalWindow.PageCreator() {
                
                private static final long serialVersionUID = 1L;
                
                @Override
                public Page createPage() {
                    return new CreateOrEditReportPage(modalWindow, forReport);
                }                
            });

            class DisableDefaultConfirmBehavior extends AbstractBehavior implements IHeaderContributor {

                private static final long serialVersionUID = 1L;

                @Override
                public void renderHead(IHeaderResponse response) {
                    response.renderOnDomReadyJavascript ("Wicket.Window.unloadConfirmation = false");
                }
            }
            
            ((ModalWindow) modalWindow.add(new DisableDefaultConfirmBehavior()))
            .setInitialWidth(new Integer(new ResourceModel("dashboard.report.createoredit.window.width").wrapOnAssignment(this).getObject().toString()))
            .setInitialHeight(new Integer(new ResourceModel("dashboard.report.createoredit.window.height").wrapOnAssignment(this).getObject().toString()))
            .show(target);
        }        
    };
    
    private final class RemoveReportLink extends Link<Object> {
        
        private static final long serialVersionUID = 1L;
        
        private ReportModel forReport;
        
        public RemoveReportLink(String id, ReportModel report) {
            super(id);
            this.forReport = report;
        }

        @Override
        public void onClick() {
            try {
                ((WicketApplication) getApplication()).getDashboardService().deleteReport(this.forReport);
                DashboardMainPage page = new DashboardMainPage(this.getPage().getPageParameters());
                ((AjaxTabbedPanel) page.get("tabs")).setSelectedTab(new Integer(new ResourceModel("dashboard.tabs.tab2.number").getObject()));
                setResponsePage(page);
            } catch (Exception e) {
                log.error(this.getClass().toString() + ": " + "onClick: " + e.getMessage());
                log.debug("Exception: ", e);
                this.redirectToInterceptPage(new InternalErrorPage());
            }
        }
    }
}