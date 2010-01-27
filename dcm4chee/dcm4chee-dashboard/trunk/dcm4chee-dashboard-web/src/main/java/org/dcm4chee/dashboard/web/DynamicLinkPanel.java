package org.dcm4chee.dashboard.web;

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
import org.dcm4chee.dashboard.web.common.InternalErrorPage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DynamicLinkPanel extends Panel {

    private static final long serialVersionUID = 1L;

    private static Logger log = LoggerFactory.getLogger(DynamicLinkPanel.class);

    private Link<Object> link;
    private ReportModel report;

    @SuppressWarnings("unchecked")
    public DynamicLinkPanel(String id, String className, ReportModel report, ModalWindow modalWindow) {
        super(id);
        
        try {
            this.report = report;
            add((this.link = (Link<Object>) ((Class<? extends Link<Object>>) Class.forName("org.dcm4chee.dashboard.web.DynamicLinkPanel$" + className)).getConstructors()[0].newInstance(new Object[] {
                    this, 
                    "report-table-link", 
                    report, 
                    modalWindow
            })));
            this.link.add(new Image("image"));
            
            if (link instanceof org.dcm4chee.dashboard.web.DynamicLinkPanel.DiagramLink
                    || link instanceof org.dcm4chee.dashboard.web.DynamicLinkPanel.TableLink) {
                this.link.setPopupSettings(new PopupSettings(PopupSettings.RESIZABLE | PopupSettings.SCROLLBARS));
            }
            
            String labeltext = null;
            if ((link instanceof org.dcm4chee.dashboard.web.DynamicLinkPanel.DiagramLink) 
                && (report.getDiagram() != null)) {
                labeltext = new ResourceModel("dashboard.report.diagram.options.types").wrapOnAssignment(this).getObject().split(";")[report.getDiagram()];
                this.link.add(new AttributeModifier("title", true, new Model<String>(new ResourceModel("dashboard.report.diagram.options.tooltips").wrapOnAssignment(this).getObject().split(";")[report.getDiagram()])));
            }
            
          this.link.add(new Label("text", labeltext));
//            this.link.add(new Label("text", text != null ? 
//                    text : 
//                    (link instanceof org.dcm4chee.dashboard.web.DynamicLinkPanel.DiagramLink) 
//                    && (report.getDiagram() != null) ?
//                            new ResourceModel("dashboard.report.diagram.options.types").wrapOnAssignment(this).getObject().split(";")[report.getDiagram()] : 
//                            ""));
                
//System.out.println("is null: " + (report.getDataSource() == null));
//System.out.println("is empty: " + report.getDataSource());

//          this.link.setVisible(!(report.getDataSource() == null || report.getDataSource().equals("")
//                  && (link instanceof org.dcm4chee.dashboard.web.DynamicLinkPanel.TableLink 
//                      || link instanceof org.dcm4chee.dashboard.web.DynamicLinkPanel.DiagramLink)));

            this.link.setVisible(
                    ((link instanceof org.dcm4chee.dashboard.web.DynamicLinkPanel.CreateOrEditReportLink
                            && (report != null))
                    || link instanceof org.dcm4chee.dashboard.web.DynamicLinkPanel.RemoveLink
                    || (link instanceof org.dcm4chee.dashboard.web.DynamicLinkPanel.DiagramLink
                            && (report != null && report.getDiagram() != null && report.getDataSource() != null && !report.getDataSource().equals("")))
                    || (link instanceof org.dcm4chee.dashboard.web.DynamicLinkPanel.TableLink
                            && (report != null && report.getTable() && report.getDataSource() != null && !report.getDataSource().equals("")))
            ));
            
        } catch (Exception e) {
            log.error(this.getClass().toString() + ": " + "init: " + e.getMessage());
            log.debug("Exception: ", e);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onBeforeRender() {
        super.onBeforeRender();

        try {
            Image image = (Image) this.link.get("image");
            image.add(new AttributeModifier("src", true, new AbstractReadOnlyModel() {
    
                private static final long serialVersionUID = 1L;
    
                @Override
                public Object getObject() {
                    if (link instanceof org.dcm4chee.dashboard.web.DynamicLinkPanel.CreateOrEditReportLink)
                        if (report == null || report.getGroupUuid() == null)
                            return "images/file.png";
                        else
                            return "images/reply.png";
                    else if (link instanceof org.dcm4chee.dashboard.web.DynamicLinkPanel.RemoveLink)
                        return "images/action_delete.png";
                    else if (link instanceof org.dcm4chee.dashboard.web.DynamicLinkPanel.TableLink)
                        return "images/application.png";
                    else return "";
                }
            }));
    
            // set the tooltip
            if (this.link instanceof org.dcm4chee.dashboard.web.DynamicLinkPanel.CreateOrEditReportLink)
                if (this.report.getGroupUuid() == null)
                    image.add(new AttributeModifier("title", true, new ResourceModel("dashboard.dynamiclink.report.create").wrapOnAssignment(this)));
            if (this.link instanceof org.dcm4chee.dashboard.web.DynamicLinkPanel.RemoveLink) {
                if (report.getGroupUuid() == null)
                    image.add(new AttributeModifier("title", true, new ResourceModel("dashboard.dynamiclink.report.group.remove").wrapOnAssignment(this)));
                else
                    image.add(new AttributeModifier("title", true, new ResourceModel("dashboard.dynamiclink.report.remove").wrapOnAssignment(this)));
            }
            // set the image
            if (this.link instanceof org.dcm4chee.dashboard.web.DynamicLinkPanel.CreateOrEditReportLink)
                image.setImageResourceReference(new ResourceReference(DynamicLinkPanel.class, "images/file.png"));
            else if (this.link instanceof org.dcm4chee.dashboard.web.DynamicLinkPanel.RemoveLink)
                image.setImageResourceReference(new ResourceReference(DynamicLinkPanel.class, "images/application.png"));
    
            if (this.link instanceof RemoveLink)
                if (report.getGroupUuid() == null)
                    link.add(new AttributeModifier("onclick", true, new Model<String>("return confirm('" + new ResourceModel("dashboard.dynamiclink.report.group.remove_confirmation").wrapOnAssignment(this).getObject() + "');")));
                else
                    link.add(new AttributeModifier("onclick", true, new Model<String>("return confirm('" + new ResourceModel("dashboard.dynamiclink.report.remove_confirmation").wrapOnAssignment(this).getObject() + "');")));
        } catch (Exception e) {
            log.error(this.getClass().toString() + ": " + "onBeforeRender: " + e.getMessage());
            log.debug("Exception: ", e);
        }
    }

    private class CreateOrEditReportLink extends AjaxFallbackLink<Object> {
        
        private static final long serialVersionUID = 1L;
        
        private ReportModel report;
        private ModalWindow modalWindow;
        
        public CreateOrEditReportLink(String id, ReportModel report, ModalWindow modalWindow) {
            super(id);

            this.report = report;
            this.modalWindow = modalWindow;
        }
        
        @Override
        public void onClick(AjaxRequestTarget target) {

            this.modalWindow.setWindowClosedCallback(new ModalWindow.WindowClosedCallback() {              
                
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
                    return new CreateOrEditReportPage(modalWindow, report);
                }                
            });

            class DisableDefaultConfirmBehavior extends AbstractBehavior implements IHeaderContributor {

                private static final long serialVersionUID = 1L;

                @Override
                public void renderHead(IHeaderResponse response) {
                    response.renderOnDomReadyJavascript ("Wicket.Window.unloadConfirmation = false");
                }
            }
            
            ((ModalWindow) this.modalWindow.add(new DisableDefaultConfirmBehavior()))
            .setInitialWidth(new Integer(new ResourceModel("dashboard.dynamiclink.report.createoredit.window.width").wrapOnAssignment(this).getObject().toString()))
            .setInitialHeight(new Integer(new ResourceModel("dashboard.dynamiclink.report.createoredit.window.height").wrapOnAssignment(this).getObject().toString()))
            .show(target);
        }
    }    

    private class RemoveLink extends Link<Object> {
        
        private static final long serialVersionUID = 1L;
        
        private ReportModel report;
        
        public RemoveLink(String id, ReportModel report, ModalWindow modalWindow) {
            super(id);
            
            this.report = report;
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
                this.redirectToInterceptPage(new InternalErrorPage());
            }
        }
    }
    
    private class DiagramLink extends Link<Object> {
        
        private static final long serialVersionUID = 1L;
        
        private ReportModel report;
        
        public DiagramLink(String id, ReportModel report, ModalWindow modalWindow) {
            super(id);

            this.report = report;
        }

        @Override
        public void onClick() {
            setResponsePage(new DisplayReportDiagramPage(this.report));
        }
    }

    private class TableLink extends Link<Object> {
        
        private static final long serialVersionUID = 1L;
        
        private ReportModel report;
        
        public TableLink(String id, ReportModel report, ModalWindow modalWindow) {
            super(id);

            this.report = report;
        }

        @Override
        public void onClick() {
            setResponsePage(new DisplayReportTablePage(this.report));
        }
    }
}
