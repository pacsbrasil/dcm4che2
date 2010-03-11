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

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.AbstractList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MalformedObjectNameException;
import javax.management.ReflectionException;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
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
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.util.ListModel;
import org.dcm4chee.dashboard.model.ReportModel;
import org.dcm4chee.dashboard.util.DatabaseUtils;
import org.dcm4chee.dashboard.web.WicketApplication;
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
    private ReportModel report;
 
    public CreateOrEditReportPage(final ModalWindow window, final ReportModel report) {
        super();

        try {
            this.report = (report == null) ? new ReportModel() : report;
            if (this.report.getGroupUuid() == null) {
                this.report.setGroupUuid(this.report.getUuid());
                this.report.setUuid(null);
                this.report.setTitle(null);
            }
            this.window = window;
            
            Label resultMessage;
            add(resultMessage = new Label("result-message"));
            add(new CreateOrEditReportForm("create-or-edit-report-form", this.report, resultMessage, this.window));
        } catch (Exception e) {
            log.error(this.getClass().toString() + ": " + "init: " + e.getMessage());
            log.debug("Exception: ", e);
            this.getApplication().getSessionStore().setAttribute(getRequest(), "exception", e);
            throw new RuntimeException();
        }
    }

    @Override
    public void onBeforeRender() {
        super.onBeforeRender();
        
        try {
            addOrReplace(new Label("page-title", new ResourceModel(this.report.getUuid() == null ? "dashboard.report.createoredit.create.title" : "dashboard.report.createoredit.edit.title")));
        } catch (Exception e) {
            log.error(this.getClass().toString() + ": " + "onBeforeRender: " + e.getMessage());
            log.debug("Exception: ", e);
            this.getApplication().getSessionStore().setAttribute(getRequest(), "exception", e);
            throw new RuntimeException();
        }
    }
    
    private final class CreateOrEditReportForm extends Form<Object> {

        private static final long serialVersionUID = 1L;
        
        public CreateOrEditReportForm(String id, final ReportModel report, final Label resultMessage, final ModalWindow window) throws InstanceNotFoundException, MalformedObjectNameException, AttributeNotFoundException, ReflectionException, MBeanException, NullPointerException {
            super(id);

            final ReportModel thisReport = report == null ? new ReportModel(UUID.randomUUID().toString(), null, null, null, null, false, null, null) : report;
            this.add(new TextField<String>("dashboard.report.createoredit.form.title.input", new PropertyModel<String>(thisReport, "title"))
            .setRequired(true)
            .add(new ReportTitleValidator())
            .add(new AttributeModifier("size", true, new ResourceModel("dashboard.report.createoredit.form.title.columns"))));
            this.add(new ValidatorMessageLabel("report-title-validator-message-label", (FormComponent<?>) this.get(0)).setOutputMarkupId(true));
            
            add(new Label("report-datasource-dropdown-label", new ResourceModel("dashboard.report.createoredit.form.datasource.dropdown.title")));
            add(new DropDownChoice<String>("report-datasource-dropdown-choice", new PropertyModel<String>(thisReport, "dataSource"), Arrays.asList(((WicketApplication) getApplication()).getDashboardService().getDataSources())).setNullValid(true));

            this.add(new TextArea<String>("dashboard.report.createoredit.form.statement.input", new PropertyModel<String>(thisReport, "statement"))
            .setRequired(true)
            .add(new SQLSelectStatementValidator())
            .add(new AttributeModifier("rows", true, new ResourceModel("dashboard.report.createoredit.form.statement.rows")))
            .add(new AttributeModifier("cols", true, new ResourceModel("dashboard.report.createoredit.form.statement.columns"))));
            this.add(new ValidatorMessageLabel("report-statement-validator-message-label", (FormComponent<?>) this.get("dashboard.report.createoredit.form.statement.input")).setOutputMarkupId(true));

            add(new AjaxFallbackButton("statement-test-submit", CreateOrEditReportForm.this) {
                private static final long serialVersionUID = 1L;

                @Override
                protected void onSubmit(AjaxRequestTarget target, Form<?> form) {

                    String message = null;
                    Connection jdbcConnection = null;

                    try {
                        if (DatabaseUtils.isConfigurableStatement(thisReport.getStatement())) {
                            message = new ResourceModel("dashboard.report.createoredit.form.statement-test-submit.configurable-statement-message").wrapOnAssignment(this.getParent()).getObject();
                            return;
                        }
                            
                        Object dataSourceName = null;
                        if ((dataSourceName = form.get("report-datasource-dropdown-choice").getDefaultModelObject()) == null) {
                            message = new ResourceModel("dashboard.report.createoredit.form.statement-test-submit.no-datasource-message").wrapOnAssignment(this.getParent()).getObject();
                            return;
                        }

                        (jdbcConnection = ((DataSource) (new InitialContext())
                                .lookup(dataSourceName.toString()))
                                .getConnection())
                        .createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY)
                        .executeQuery(thisReport.getStatement())
                        .close();
                    } catch (Exception e) {
                        message = e.getLocalizedMessage();
                        log.debug("Exception: ", e);
                    } finally {
                        try {
                            jdbcConnection.close();
                        } catch (Exception ignore) {
                        }
                        resultMessage.setDefaultModel(new Model<String>(new ResourceModel(message == null ? "dashboard.report.createoredit.form.statement-test-submit.success-message" : 
                                                                                          "dashboard.report.createoredit.form.statement-test-submit.failure-message")
                                                            .wrapOnAssignment(this.getParent()).getObject().toString()
                                                            + (message == null ? "" : message)))
                        .add(new AttributeModifier("class", true, new Model<String>(message == null ? "message-system" : "message-error")))              
                        .setVisible(true);
                        setResponsePage(this.getPage());
                    }
                }
                
                @Override
                protected void onError(AjaxRequestTarget target, Form<?> form) {
                    target.addComponent(form.get("report-statement-validator-message-label"));
                }
            });

            final String[] diagramOptionsTypes = new ResourceModel("dashboard.report.diagram.options.types").wrapOnAssignment(this.getParent()).getObject().split(";");
            final String[] diagramOptionsTooltips = new ResourceModel("dashboard.report.diagram.options.tooltips").wrapOnAssignment(this.getParent()).getObject().split(";");
            add(new Label("report-diagram-dropdown-label", new ResourceModel("dashboard.report.createoredit.form.diagram.dropdown.title")));
            add(new DropDownChoice<Integer>("report-diagram-dropdown-choice", new PropertyModel<Integer>(thisReport, "diagram"), new ListModel<Integer>() {

                private static final long serialVersionUID = 1L;

                @Override
                public List<Integer> getObject() {
                    return new AbstractList<Integer>() {
                        public Integer get(int i) { return new Integer(i); }
                        public int size() { return diagramOptionsTypes.length; }
                    };
                }
            }, new ChoiceRenderer<Integer>() {

                private static final long serialVersionUID = 1L;
                
                @Override
                public Object getDisplayValue(Integer index) {
                    return (index == null) ? null : diagramOptionsTypes[index] + " (" + diagramOptionsTooltips[index] + ")";
                }
            }).setNullValid(true));

            add(new CheckBox("report-table-checkbox", new PropertyModel<Boolean>(thisReport, "table")));

            add(new AjaxFallbackButton("form-submit", CreateOrEditReportForm.this) {
                
                private static final long serialVersionUID = 1L;
    
                @Override
                protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                    try {
                        if (thisReport == null || thisReport.getUuid() == null)
                            ((WicketApplication) getApplication()).getDashboardService().createReport(thisReport, false);
                        else 
                            ((WicketApplication) getApplication()).getDashboardService().updateReport(thisReport);
                        window.close(target);
                    } catch (Exception e) {
                      log.error(this.getClass().toString() + ": " + "onSubmit: " + e.getMessage());
                      log.debug("Exception: ", e);

                      resultMessage.setDefaultModel(new ResourceModel("dashboard.report.createoredit.form.form-submit.failure-message"))
                      .add(new AttributeModifier("class", true, new Model<String>("message-error")))
                      .setVisible(true);
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
