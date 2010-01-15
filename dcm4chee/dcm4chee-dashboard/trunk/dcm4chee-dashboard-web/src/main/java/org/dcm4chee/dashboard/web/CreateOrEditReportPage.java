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

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.AbstractList;
import java.util.List;
import java.util.UUID;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxFallbackButton;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.pages.InternalErrorPage;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.util.ListModel;
import org.dcm4chee.dashboard.model.ReportModel;
import org.dcm4chee.dashboard.web.validator.ReportTitleValidator;
import org.dcm4chee.dashboard.web.validator.SQLSelectStatementValidator;
import org.dcm4chee.dashboard.web.validator.ValidatorMessageLabel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Robert David <robert.david@agfa.com>
 * @version $Revision$ $Date$
 * @since 28.09.2009
 */
public class CreateOrEditReportPage extends WebPage {
    
    private static final long serialVersionUID = 1L;
    
    private static Logger log = LoggerFactory.getLogger(CreateOrEditReportPage.class);
   
    protected ModalWindow window;
 
    public CreateOrEditReportPage(final ModalWindow window, final ReportModel forReport) {
        try {
            add(new Label("page-title", new ResourceModel(forReport == null ? "dashboard.report.createoredit.create.title" : "dashboard.report.createoredit.edit.title")));
            
            Label resultMessage;
            add(resultMessage = new Label("result-message"));
            add(new CreateOrEditReportForm("create-or-edit-report-form", forReport, resultMessage, window));

            add(new AjaxLink<Object>("close") {

                private static final long serialVersionUID = 1L;

                @Override
                public void onClick(AjaxRequestTarget target) {
                    window.close(target);
                }
            }.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true));
        } catch (Exception e) {
            log.error(this.getClass().toString() + ": " + "init: " + e.getMessage());
            log.debug("Exception: ", e);
            this.redirectToInterceptPage(new InternalErrorPage());
        }
    }

    private final class CreateOrEditReportForm extends Form<Object> {

        private static final long serialVersionUID = 1L;
        
        public CreateOrEditReportForm(String id, final ReportModel forReport, final Label resultMessage, final ModalWindow window) {
            super(id);

            final ReportModel report = forReport == null ? new ReportModel(UUID.randomUUID().toString(), null, null, null, false) : forReport;
            this.add(new TextField<String>("dashboard.report.createoredit.form.title.input", new PropertyModel<String>(report, "title"))
            .setRequired(true)
            .add(new ReportTitleValidator())
            .add(new AttributeModifier("size", true, new ResourceModel("dashboard.report.createoredit.form.title.columns"))));
            this.add(new ValidatorMessageLabel("report-title-validator-message-label", (FormComponent<?>) this.get(0)).setOutputMarkupId(true));
            
            this.add(new TextArea<String>("dashboard.report.createoredit.form.statement.input", new PropertyModel<String>(report, "statement"))
            .setRequired(true)
            .add(new SQLSelectStatementValidator())
            .add(new AttributeModifier("rows", true, new ResourceModel("dashboard.report.createoredit.form.statement.rows")))
            .add(new AttributeModifier("cols", true, new ResourceModel("dashboard.report.createoredit.form.statement.columns"))));
            this.add(new ValidatorMessageLabel("report-statement-validator-message-label", (FormComponent<?>) this.get(2)).setOutputMarkupId(true));
            
            add(new AjaxFallbackButton("statement-test-submit", CreateOrEditReportForm.this) {
                private static final long serialVersionUID = 1L;
    
                @Override
                protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                    Connection jdbcConnection = null;
                    String message = null;
                    try {
                        (jdbcConnection = DashboardMainPage.getDatabaseConnection())
                        .createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY)
                        .executeQuery(report.getStatement())
                        .close();
                    } catch (Exception e) {
                        message = e.getLocalizedMessage();
                        log.debug("Exception: ", e);
                    } finally {
                        try {
                            jdbcConnection.close();
                        } catch (SQLException ignore) {
                        }
                        resultMessage.setDefaultModel(new Model<String>(new ResourceModel(message == null ? "dashboard.report.createoredit.form.statement-test-submit.success-message" : 
                                                                                          "dashboard.report.createoredit.form.statement-test-submit.failure-message")
                                                            .wrapOnAssignment(this.getParent()).getObject().toString()
                                                            + (message == null ? "" : message)));
                        resultMessage.add(new AttributeModifier("class", true, new Model<String>(message == null ? "message-system" : "message-error")));              
                        resultMessage.setVisible(true);
                        setResponsePage(this.getPage());
                    }
                }
            });
            
            final String[] diagramOptions = new ResourceModel("dashboard.report.diagram.options").wrapOnAssignment(this.getParent()).getObject().split(";");
            add(new Label("report-diagram-dropdown-label", new ResourceModel("dashboard.report.createoredit.form.diagram.dropdown.title")));
            add(new DropDownChoice<Integer>("report-diagram-dropdown-choice", new PropertyModel<Integer>(report, "diagram"), new ListModel<Integer>() {

                private static final long serialVersionUID = 1L;

                @Override
                public List<Integer> getObject() {
                    return new AbstractList<Integer>() {
                        public Integer get(int i) { return new Integer(i); }
                        public int size() { return diagramOptions.length; }
                    };
                }
            }, new ChoiceRenderer<Integer>() {

                private static final long serialVersionUID = 1L;
                
                @Override
                public Object getDisplayValue(Integer index) {
                    return (index == null) ? null : diagramOptions[index];
                }
            }).setNullValid(true));

            add(new CheckBox("report-table-checkbox", new PropertyModel<Boolean>(report, "table")));

            add(new AjaxFallbackButton("form-submit", CreateOrEditReportForm.this) {
                private static final long serialVersionUID = 1L;
    
                @Override
                protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                    try {
                        if (forReport == null)
                            ((WicketApplication) getApplication()).getDashboardService().createReport(report);
                        else 
                            ((WicketApplication) getApplication()).getDashboardService().updateReport(report);
                        window.close(target);
                    } catch (Exception e) {
                      log.error(this.getClass().toString() + ": " + "onSubmit: " + e.getMessage());
                      log.debug("Exception: ", e);

                      resultMessage.setDefaultModel(new ResourceModel("dashboard.report.createoredit.form.form-submit.failure-message"));
                      resultMessage.add(new AttributeModifier("class", true, new Model<String>("message-error")));
                      resultMessage.setVisible(true);
                      setResponsePage(this.getPage());
                    }
                }
                
                @Override
                protected void onError(AjaxRequestTarget target, Form<?> form) {
                    target.addComponent(form.get("report-title-validator-message-label"));
                    target.addComponent(form.get("report-statement-validator-message-label"));
                }
            });
        }
    }
}
