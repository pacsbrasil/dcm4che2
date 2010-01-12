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

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.dcm4chee.dashboard.model.ReportModel;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisState;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryTick;
import org.jfree.chart.axis.ExtendedCategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.Tick;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot3D;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.category.CategoryStepRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.text.TextBlock;
import org.jfree.text.TextFragment;
import org.jfree.text.TextLine;
import org.jfree.ui.RectangleEdge;

/**
 * @author Robert David <robert.david@agfa.com>
 * @version $Revision$ $Date$
 * @since 18.11.2009
 */
public class DisplayReportDiagramPage extends WebPage {
    
    public DisplayReportDiagramPage(ModalWindow modalWindow, ReportModel report) {

        Connection jdbcConnection = null;
        try {
            ResultSet resultSet = 
                (jdbcConnection  = DashboardMainPage.getDatabaseConnection())
                .createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY)
                .executeQuery(report.getStatement());

            ResultSetMetaData metaData = resultSet.getMetaData();
            JFreeChart chart = null;
            resultSet.beforeFirst();
            
            // Line chart - 1 numeric value
            if (report.getDiagram() == 0) {
                if (metaData.getColumnCount() != 1) throw new Exception(new ResourceModel("dashboard.report.reportdiagram.image.render.error.1numvalues").wrapOnAssignment(this).getObject());

                DefaultCategoryDataset dataset = new DefaultCategoryDataset();
                while (resultSet.next()) dataset.addValue(resultSet.getDouble(1), metaData.getColumnName(1), String.valueOf(resultSet.getRow()));

                chart = ChartFactory.createLineChart(new ResourceModel("dashboard.report.reportdiagram.image.label").wrapOnAssignment(this).getObject(),
                        new ResourceModel("dashboard.report.reportdiagram.image.row-label").wrapOnAssignment(this).getObject(),
                        metaData.getColumnName(1),
                        dataset,
                        PlotOrientation.VERTICAL,
                        true,
                        true,
                        true);

            // XY Series chart - 2 numeric values
            } else if (report.getDiagram() == 1) {
                if (metaData.getColumnCount() != 2) throw new Exception(new ResourceModel("dashboard.report.reportdiagram.image.render.error.2numvalues").wrapOnAssignment(this).getObject());             

                XYSeries series = new XYSeries(metaData.getColumnName(1) + " / " + metaData.getColumnName(2));
                while (resultSet.next()) series.add(resultSet.getDouble(1), resultSet.getDouble(2));

                chart = ChartFactory.createXYLineChart(new ResourceModel("dashboard.report.reportdiagram.image.label").wrapOnAssignment(this).getObject(), 
                        metaData.getColumnName(1),
                        metaData.getColumnName(2),
                        new XYSeriesCollection(series), 
                        PlotOrientation.VERTICAL,
                        true,
                        true,
                        true);

            } else if (report.getDiagram() == 2) {
                if (metaData.getColumnCount() != 2) throw new Exception(new ResourceModel("dashboard.report.reportdiagram.image.render.error.2values").wrapOnAssignment(this).getObject());                

                DefaultCategoryDataset dataset = new DefaultCategoryDataset();
                while (resultSet.next()) dataset.setValue(resultSet.getDouble(1), metaData.getColumnName(1) + " / " + metaData.getColumnName(2), resultSet.getString(2));

                chart = new JFreeChart(new ResourceModel("dashboard.report.reportdiagram.image.label").wrapOnAssignment(this).getObject(), 
                                       new CategoryPlot(dataset,

                                               // JJJJ -> 18
                                               // => Länge 4 => 18

                                               // JJJJMM -> xx
                                               // => Länge 6 -> 13, 14

                                               
                                               // JJJJMMDD -> 14 oder 13, wurscht
                                               // => Länge 8 -> 14, 13
                                               
                                               
                                               // JJJJ.MM.DD -> 14 oder 13, wurscht
                                               // => Länge 10 -> 14, 13
                                               
                                               
                                               
                                               // bis 9 -> 13 ticks, ab 8 -> 14 ticks

                                               // bis 10 -> 10 ticks, ab 11 -> 9 ticks
                                               
                                               // bis 15 -> 9 ticks, ab 16 -> 8 ticks
                                               
                                               // bis 19 -> 6 ticks, ab 20 -> 5 ticks
                                               
                                               // bis 25 -> 5 ticks, ab 26 -> 4 ticks
                                               
                                               // bis 35 -> 3 ticks, ab 36 -> 2 ticks
                                               
                                               
                                               // JJJJ.MM.DD -> 14 oder 13, wurscht
                                               
                                               
                                       new LabelAdaptingCategoryAxis(14, metaData.getColumnName(2)),
                                       new NumberAxis(metaData.getColumnName(1)), 
                                       new CategoryStepRenderer(true)));

            // Pie chart - 1 numeric value, 1 comparable value (used as category)
            } else if ((report.getDiagram() == 3) || (report.getDiagram() == 4)) {
                if (metaData.getColumnCount() != 2) throw new Exception(new ResourceModel("dashboard.report.reportdiagram.image.render.error.2values").wrapOnAssignment(this).getObject());

                DefaultPieDataset dataset = new DefaultPieDataset();
                while (resultSet.next()) dataset.setValue(resultSet.getString(2), resultSet.getDouble(1));

                if (report.getDiagram() == 3)
                    // Pie chart 2D
                    chart = ChartFactory.createPieChart(new ResourceModel("dashboard.report.reportdiagram.image.label").wrapOnAssignment(this).getObject(), 
                            dataset,
                            true,
                            true,
                            true);
                else if (report.getDiagram() == 4) {
                    // Pie chart 3D
                    chart = ChartFactory.createPieChart3D(new ResourceModel("dashboard.report.reportdiagram.image.label").wrapOnAssignment(this).getObject(), 
                            dataset,
                            true,
                            true,
                            true);
                    ((PiePlot3D) chart.getPlot()).setForegroundAlpha(Float.valueOf(new ResourceModel("dashboard.report.reportdiagram.image.alpha").wrapOnAssignment(this).getObject()));
                }
                
            // Bar chart - 1 numeric value, 2 comparable values (used as category, series)
            } else if (report.getDiagram() == 5) {
                if ((metaData.getColumnCount() != 2) && (metaData.getColumnCount() != 3)) throw new Exception(new ResourceModel("dashboard.report.reportdiagram.image.render.error.3values").wrapOnAssignment(this).getObject());

                DefaultCategoryDataset dataset = new DefaultCategoryDataset();
                while (resultSet.next()) dataset.setValue(resultSet.getDouble(1), resultSet.getString(2), resultSet.getString(metaData.getColumnCount()));

                chart = ChartFactory.createBarChart(new ResourceModel("dashboard.report.reportdiagram.image.label").wrapOnAssignment(this).getObject(), 
                        metaData.getColumnName(2),
                        metaData.getColumnName(1),
                        dataset,
                        PlotOrientation.VERTICAL,
                        true,
                        true,
                        true);
            }

            add(new JFreeChartImage("diagram", 
                                    chart, 
                                    new Integer(new ResourceModel("dashboard.report.reportdiagram.image.width").wrapOnAssignment(this).getObject().toString()), 
                                    new Integer(new ResourceModel("dashboard.report.reportdiagram.image.height").wrapOnAssignment(this).getObject().toString())));

            add(new Label("error-message", "").setVisible(false));
            add(new Label("error-reason", "").setVisible(false));            
        } catch (Exception e) {
            add(new Image("diagram"));
            add(new Label("error-message", new ResourceModel("dashboard.report.reportdiagram.statement.error").wrapOnAssignment(this).getObject()).add(new AttributeModifier("class", true, new Model<String>("message-error"))));
            add(new Label("error-reason", e.getMessage()).add(new AttributeModifier("class", true, new Model<String>("message-error"))));
        } finally {
            try {
                jdbcConnection.close();
            } catch (SQLException ignore) {
            }
        }
    }

    public class LabelAdaptingCategoryAxis extends CategoryAxis {
        
        private static final long serialVersionUID = 1L;

        private final int labeledTicks;

        public LabelAdaptingCategoryAxis(int labeledTicks, String label) {
            super(label);
            this.labeledTicks = labeledTicks;
        }

        @SuppressWarnings("unchecked")
        @Override
        public List<CategoryTick> refreshTicks(Graphics2D g2, AxisState state, Rectangle2D dataArea, RectangleEdge edge) {

            List<CategoryTick> currentTicks = super.refreshTicks(g2, state, dataArea, edge);
            if (currentTicks.isEmpty()) return currentTicks;
            int interval = currentTicks.size() / labeledTicks;
            if (interval < 1) return currentTicks;
           
            List<CategoryTick> newTicks = new ArrayList<CategoryTick>(currentTicks.size());
            for (int i = 0; i < currentTicks.size(); i++) {
                CategoryTick tick = currentTicks.get(i);
                TextBlock textBlock = new TextBlock();
                textBlock.addLine(tick.getCategory().toString(), 
                                  tick.getLabel().getLastLine().getFirstTextFragment().getFont(), 
                                  tick.getLabel().getLastLine().getFirstTextFragment().getPaint());
                tick = new CategoryTick(tick.getCategory(), 
                                        textBlock, 
                                        tick.getLabelAnchor(), 
                                        tick.getRotationAnchor(), 
                                        tick.getAngle());
                newTicks.add(tick);
            }
            return newTicks;
        }
    }
}
