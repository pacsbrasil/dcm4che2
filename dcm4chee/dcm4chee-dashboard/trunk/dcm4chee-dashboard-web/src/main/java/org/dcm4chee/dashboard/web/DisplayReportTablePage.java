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

package org.dcm4chee.dashboard.web;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.dcm4chee.dashboard.model.ReportModel;
import org.dcm4chee.dashboard.util.CSSUtils;

/**
 * @author Robert David <robert.david@agfa.com>
 * @version $Revision$ $Date$
 * @since 18.11.2009
 */
public class DisplayReportTablePage extends WebPage {

    public DisplayReportTablePage(ReportModel report) {

        RepeatingView columnHeaders = new RepeatingView("column-headers"); 
        add(columnHeaders);
        RepeatingView reportRows = new RepeatingView("report-rows"); 
        add(reportRows);

        Connection jdbcConnection = null;
        try {
            ResultSet resultSet = 
                (jdbcConnection  = DashboardMainPage.getDatabaseConnection(report.getDataSource()))
                .createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY)
                .executeQuery(report.getStatement());

            for (int i = 1; i <= resultSet.getMetaData().getColumnCount(); i++)
                columnHeaders.add(new Label(columnHeaders.newChildId(), resultSet.getMetaData().getColumnName(i)));
            
            resultSet.beforeFirst();
            while (resultSet.next()) {
                
                WebMarkupContainer parent = new WebMarkupContainer(reportRows.newChildId());
                parent.add(new AttributeModifier("class", true, new Model<String>(CSSUtils.getRowClass(resultSet.getRow() - 1))));
                reportRows.add(parent);
                RepeatingView columnValues = new RepeatingView("column-values");
                parent.add(columnValues);
                
                for (int i = 1; i <= resultSet.getMetaData().getColumnCount(); i++)
                    columnValues.add(new WebMarkupContainer(columnValues.newChildId()).add(new Label("column-value", resultSet.getString(i))));
            }
            resultSet.close();
            add(new Label("error-message", "").setVisible(false));
            add(new Label("error-reason", "").setVisible(false));
        } catch (Exception e) {
            add(new Label("error-message", new ResourceModel("dashboard.report.reporttable.statement.error").wrapOnAssignment(this).getObject()).add(new AttributeModifier("class", true, new Model<String>("message-error"))));
            add(new Label("error-reason", e.getMessage()).add(new AttributeModifier("class", true, new Model<String>("message-error"))));
        } finally {
            try {
                jdbcConnection.close();
            } catch (SQLException ignore) {
            }
        }
    }
}
