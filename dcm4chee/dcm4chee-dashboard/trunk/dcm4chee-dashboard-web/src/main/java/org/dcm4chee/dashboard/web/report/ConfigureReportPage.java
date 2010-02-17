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
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MalformedObjectNameException;
import javax.management.ReflectionException;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxFallbackButton;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.extensions.yui.calendar.DatePicker;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.form.validation.IFormValidator;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.dcm4chee.dashboard.model.ReportModel;
import org.dcm4chee.dashboard.web.DashboardMainPage;
import org.dcm4chee.dashboard.web.report.display.DynamicDisplayPage;
import org.dcm4chee.dashboard.web.validator.ValidatorMessageLabel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Robert David <robert.david@agfa.com>
 * @version $Revision$ $Date$
 * @since 28.09.2009
 */
public class ConfigureReportPage extends WebPage {
    
    private static final long serialVersionUID = 1L;
    
    private static Logger log = LoggerFactory.getLogger(ConfigureReportPage.class);
   
    private ReportModel report;
    protected ModalWindow window;
    
    private boolean diagram;
    private boolean table;
    
    public ConfigureReportPage(final ModalWindow window, final ReportModel report, boolean diagram, boolean table) {
        super();

        try {
            this.report = report;
            this.window = window;
            
            this.diagram = diagram;
            this.table = table;

            Label resultMessage;
            add(resultMessage = new Label("result-message"));
            resultMessage.setOutputMarkupId(true);
            add(new ConfigureReportForm("configure-report-form", this.report, resultMessage, this.window));
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
            if (!DashboardMainPage.isConfigurableStatement(this.report.getStatement()))
                redirectToInterceptPage(new DynamicDisplayPage(this.report, this.diagram, this.table));

            addOrReplace(new Label("page-title", new ResourceModel("dashboard.report.configure.title").wrapOnAssignment(this).getObject()));
            addOrReplace(new AjaxLink<Object>("close") {

                private static final long serialVersionUID = 1L;

                @Override
                public void onClick(AjaxRequestTarget target) {
                    window.close(target);
                }
            }
            .setOutputMarkupId(true)
            .setOutputMarkupPlaceholderTag(true));
        } catch (Exception e) {
            log.error(this.getClass().toString() + ": " + "onBeforeRender: " + e.getMessage());
            log.debug("Exception: ", e);
            this.getApplication().getSessionStore().setAttribute(getRequest(), "exception", e);
            throw new RuntimeException();
        }
    }
    
    private final class ConfigureReportForm extends Form<Object> {

        private static final long serialVersionUID = 1L;

        private Map<String, String> parameterNames = new HashMap<String, String>();
        private Map<String, Boolean> parameterTypes = new HashMap<String, Boolean>();
        
        public ConfigureReportForm(final String id, final ReportModel report, final Label resultMessage, final ModalWindow window) throws InstanceNotFoundException, MalformedObjectNameException, AttributeNotFoundException, ReflectionException, MBeanException, NullPointerException {
            super(id);

            this.add(new Label("dashboard.report.configure.form.title.input", new PropertyModel<String>(report, "title"))
            .add(new AttributeModifier("size", true, new ResourceModel("dashboard.report.configure.form.title.columns"))));

            this.add(new TextArea<String>("dashboard.report.configure.form.statement.input", new PropertyModel<String>(report, "statement"))
            .setRequired(true)
            .add(new AttributeModifier("rows", true, new ResourceModel("dashboard.report.configure.form.statement.rows")))
            .add(new AttributeModifier("cols", true, new ResourceModel("dashboard.report.configure.form.statement.columns")))
            .setEnabled(false));

            RepeatingView variableRows = new RepeatingView("variable-rows");
            add(variableRows);

            for (final String parameterName : DashboardMainPage.getParameterSet(report.getStatement())) {

                TextField<String> textField;
                variableRows.add(
                        (((new WebMarkupContainer(parameterName)
                        .add(new Label("variable-name", parameterName.toString())))
                        .add((textField = new TextField<String>("variable-value", new Model<String>() {

                            private static final long serialVersionUID = 1L;

                            @Override
                            public void setObject(String value) {
                                parameterNames.put(parameterName, value != null ? value : "");
                            }                            
                        }))
                        .add(new DatePicker())))
                        .add(new CheckBox("variable-numeric", new Model<Boolean>() {

                            private static final long serialVersionUID = 1L;

                            @Override
                            public void setObject(Boolean value) {
                                parameterTypes.put(parameterName, value != null ? value : false);
                            }                            
                        })))
                        .add(new ValidatorMessageLabel("report-variable-validator-message-label", textField).setOutputMarkupId(true))
                );
            }

            add(new IFormValidator() {

                private static final long serialVersionUID = 1L;

                @Override
                public FormComponent<?>[] getDependentFormComponents() {
                    return null;
                }

                @Override
                public void validate(Form<?> form) {
                    for (String parameterName : parameterNames.keySet()) {                      
                        if ((((CheckBox) get("variable-rows:" + parameterName + ":variable-numeric")).getValue() != null)
                            && (!((TextField<?>) get("variable-rows:" + parameterName + ":variable-value")).getValue().matches("^([0-9\\.]+)$"))) 
                                get("variable-rows:" + parameterName + ":variable-value").error(
                                        new ResourceModel("dashboard.report.configure.form.variable.value.numeric.error").wrapOnAssignment(getParent()).getObject());
                        }
                    }
                }                
            );
            
            add(new AjaxFallbackButton("statement-test-submit", ConfigureReportForm.this) {
                
                private static final long serialVersionUID = 1L;

                @Override
                protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                    
                    String message = null;
                    Connection jdbcConnection = null;
                    try {
                        if (report.getDataSource() == null) {
                            message = new ResourceModel("dashboard.report.configure.form.statement-test-submit.no-datasource-message").wrapOnAssignment(this.getParent()).getObject();
                            return;
                        }
                        (jdbcConnection = DashboardMainPage.getDatabaseConnection(report.getDataSource().toString())).createStatement().executeQuery(configureStatement());
                    } catch (Exception e) {
                        message = e.getLocalizedMessage();
                        log.debug("Exception: ", e);
                    } finally {
                        try {
                            jdbcConnection.close();
                        } catch (SQLException ignore) {
                        } catch (NullPointerException ignore) {
                        }
                        resultMessage.setDefaultModel(new Model<String>(new ResourceModel(message == null ? "dashboard.report.configure.form.statement-test-submit.success-message" : 
                                                                                          "dashboard.report.configure.form.statement-test-submit.failure-message")
                                                            .wrapOnAssignment(this.getParent()).getObject().toString()
                                                            + (message == null ? "" : message)))
                        .add(new AttributeModifier("class", true, new Model<String>(message == null ? "message-system" : "message-error")))              
                        .setVisible(true);
                        target.addComponent(resultMessage);                        
                    }
                }
            });

            add(new AjaxFallbackButton("form-submit", ConfigureReportForm.this) {
                private static final long serialVersionUID = 1L;
    
                @Override
                protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                    try {
                        report.setStatement(configureStatement());
                        window.redirectToInterceptPage(new DynamicDisplayPage(report, diagram, table));
                    } catch (Exception e) {
                      log.error(this.getClass().toString() + ": " + "onSubmit: " + e.getMessage());
                      log.debug("Exception: ", e);

                      resultMessage.setDefaultModel(new ResourceModel("dashboard.report.configure.form.form-submit.failure-message"))
                      .add(new AttributeModifier("class", true, new Model<String>("message-error")))
                      .setVisible(true);
                      setResponsePage(this.getPage());
                    }
                }

                @Override
                protected void onError(AjaxRequestTarget target, Form<?> form) {
                    target.addComponent(form);
                }
            });
        }
        
        private String configureStatement() {
            String statement = DashboardMainPage.createSQLStatement(report.getStatement());
            for (String parameterName : DashboardMainPage.getParameterOccurences(report.getStatement())) {
                if (!statement.contains("?")) break;
                statement = statement.replaceFirst("\\?", (" " + (parameterTypes.get(parameterName) ? "'" : "") + parameterNames.get(parameterName) + (parameterTypes.get(parameterName) ? "'" : "") + " "));
            }
            return statement;
        }
    }
}
