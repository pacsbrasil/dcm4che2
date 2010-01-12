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

import java.awt.Color;
import java.awt.Paint;
import java.io.File;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import javax.management.MBeanException;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.extensions.markup.html.tree.table.ColumnLocation;
import org.apache.wicket.extensions.markup.html.tree.table.IColumn;
import org.apache.wicket.extensions.markup.html.tree.table.IRenderable;
import org.apache.wicket.extensions.markup.html.tree.table.PropertyRenderableColumn;
import org.apache.wicket.extensions.markup.html.tree.table.PropertyTreeColumn;
import org.apache.wicket.extensions.markup.html.tree.table.TreeTable;
import org.apache.wicket.extensions.markup.html.tree.table.ColumnLocation.Alignment;
import org.apache.wicket.extensions.markup.html.tree.table.ColumnLocation.Unit;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.pages.InternalErrorPage;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.dcm4chee.dashboard.mbean.DashboardDelegator;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.StackedBarRenderer;
import org.jfree.data.Range;
import org.jfree.data.category.DefaultCategoryDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Robert David <robert.david@agfa.com>
 * @version $Revision$ $Date$
 * @since 15.11.2009
 */
public class FileSystemPanel extends Panel {

    private static final long serialVersionUID = 1L;

    private static Logger log = LoggerFactory.getLogger(FileSystemPanel.class);

    public FileSystemPanel(String id) {
        super(id);

        try {
            DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(new FileSystemModel());
            DashboardDelegator delegator = new DashboardDelegator(((WicketApplication) getApplication()).getDashboardServiceName());
            String[] fileSystemGroups = delegator.listAllFileSystemGroups();

            if (fileSystemGroups != null) {
                for (String groupname : fileSystemGroups) {
                    FileSystemModel group = new FileSystemModel();
                    
                    int index = groupname.indexOf("group=");
                    if (index < 0) continue;
                    group.setDirectoryPath(groupname.substring(index + 6));
                    
                    group.setDescription(groupname);
                    group.setGroup(true);
                    DefaultMutableTreeNode groupNode = new DefaultMutableTreeNode();

                    File[] fileSystems;
                    try {
                        fileSystems = delegator.listFileSystemsOfGroup(groupname);
                    } catch (MBeanException mbe) {
                        // if groupname does not exist
                        fileSystems = null;
                    }
                    
                    if (!((fileSystems == null) || (fileSystems.length == 0))) {
                        long minBytesFree = delegator.getMinimumFreeDiskSpaceOfGroup(groupname);
                        
                        for (File file : fileSystems) {
                            FileSystemModel fsm = new FileSystemModel();
                            fsm.setDirectoryPath(file.getAbsolutePath());
                            fsm.setOverallDiskSpace(file.getTotalSpace()/ FileSystemModel.MEGA);
                            fsm.setUsableDiskSpace(Math.max((file.getTotalSpace() - minBytesFree)/ FileSystemModel.MEGA, 0));
                            fsm.setFreeDiskSpace(Math.max((file.getUsableSpace() - minBytesFree)/ FileSystemModel.MEGA, 0));
                            fsm.setMinimumFreeDiskSpace(minBytesFree/ FileSystemModel.MEGA);
                            fsm.setUsedDiskSpace(Math.max((file.getTotalSpace() - file.getUsableSpace())/ FileSystemModel.MEGA, 0));

                            group.setOverallDiskSpace(group.getOverallDiskSpaceLong() + fsm.getOverallDiskSpaceLong());
                            group.setUsedDiskSpace(group.getUsedDiskSpaceLong() + fsm.getUsedDiskSpaceLong());
                            group.setUsableDiskSpace(group.getUsableDiskSpaceLong() + fsm.getUsableDiskSpaceLong());
                            group.setFreeDiskSpace(group.getFreeDiskSpaceLong() + fsm.getFreeDiskSpaceLong());
                            group.setMinimumFreeDiskSpace(group.getMinimumFreeDiskSpaceLong() + (minBytesFree/ FileSystemModel.MEGA));
                            groupNode.add(new DefaultMutableTreeNode(fsm));
                        }
                    }
                    groupNode.setUserObject(group);
                    rootNode.add(groupNode);
                }
            }

            FileSystemTreeTable fileSystemTreeTable = new FileSystemTreeTable("filesystem-tree-table", 
                    new DefaultTreeModel(rootNode), new IColumn[] {
                new PropertyTreeColumn(new ColumnLocation(Alignment.LEFT,
                        25, Unit.PERCENT), new StringResourceModel(
                                "filesystemlist.table.column.name", this, null)
                .getObject(), "userObject.directoryPath"),
                new ImageRenderableColumn(new ColumnLocation(
                        Alignment.RIGHT, 30, Unit.PERCENT), new StringResourceModel(
                                "filesystemlist.table.column.image", this, null).getObject(),
                "userObject.directoryPath"),
                new PropertyRenderableColumn(new ColumnLocation(
                        Alignment.RIGHT, 9, Unit.PERCENT),
                        new StringResourceModel(
                                "filesystemlist.table.column.used", this,
                                null).getObject(),
                "userObject.usedDiskSpaceString"),
                new PropertyRenderableColumn(new ColumnLocation(
                        Alignment.RIGHT, 9, Unit.PERCENT),
                        new StringResourceModel(
                                "filesystemlist.table.column.free", this,
                                null).getObject(),
                "userObject.freeDiskSpaceString"),
                new PropertyRenderableColumn(new ColumnLocation(
                        Alignment.RIGHT, 9, Unit.PERCENT),
                        new StringResourceModel(
                                "filesystemlist.table.column.minimumfree", this,
                                null).getObject(),
                "userObject.minimumFreeDiskSpaceString"),
                new PropertyRenderableColumn(new ColumnLocation(
                        Alignment.RIGHT, 9, Unit.PERCENT),
                        new StringResourceModel(
                                "filesystemlist.table.column.usable", this,
                                null).getObject(),
                "userObject.usableDiskSpaceString"),
                new PropertyRenderableColumn(new ColumnLocation(
                        Alignment.RIGHT, 9, Unit.PERCENT),
                        new StringResourceModel(
                                "filesystemlist.table.column.overall",
                                this, null).getObject(),
                "userObject.overallDiskSpaceString")
            });
            fileSystemTreeTable.getTreeState().setAllowSelectMultiple(true);
            fileSystemTreeTable.getTreeState().collapseAll();
            fileSystemTreeTable.setRootLess(true);

            add(fileSystemTreeTable);
        } catch (Exception e) {
            log.error(this.getClass().toString() + ": " + "init: " + e.getMessage());
            log.debug("Exception: ", e);
            this.redirectToInterceptPage(new InternalErrorPage());
        }
    }

    private class ImageRenderableColumn extends PropertyTreeColumn {

        private static final long serialVersionUID = 1L;

        public ImageRenderableColumn(ColumnLocation location, String header, String propertyExpression) {
            super(location, header, propertyExpression);
        }

        @Override
        public IRenderable newCell(javax.swing.tree.TreeNode node, int level) {
            return null;
        }
        
        @Override
        public Component newCell(MarkupContainer parent, String id, final TreeNode node, int level) {
            if (!((node instanceof DefaultMutableTreeNode) && (((DefaultMutableTreeNode) node)
                    .getUserObject() instanceof FileSystemModel)))
                return null;

            FileSystemModel fsm = (FileSystemModel) ((DefaultMutableTreeNode) node).getUserObject();

            double used = fsm.getOverallDiskSpaceLong() == 0 ? 0 : (100 * fsm
                    .getUsedDiskSpaceLong())
                    / fsm.getOverallDiskSpaceLong();
            double usable = fsm.getOverallDiskSpaceLong() == 0 ? 0 : (100 * fsm
                    .getUsableDiskSpaceLong())
                    / fsm.getOverallDiskSpaceLong();

            DefaultCategoryDataset dataset = new DefaultCategoryDataset();
            dataset.addValue(used, new Integer(1), "");
            dataset.addValue(usable - used, new Integer(2), "");
            dataset.addValue(100 - (usable == 0 ? 100 : usable), new Integer(3), "");

            final JFreeChart chart = ChartFactory.createStackedBarChart(null,
                    null, null, dataset, PlotOrientation.HORIZONTAL, false,
                    false, false);
            chart.setBackgroundPaint(new Color(13421772));

            final CategoryPlot plot = chart.getCategoryPlot();
            plot.setBackgroundPaint(Color.white);

            NumberAxis numberaxis = new NumberAxis();
            numberaxis.setRange(new Range(0, 100));            
            plot.setRangeAxis(numberaxis);

            CategoryAxis categoryaxis = new CategoryAxis();
            categoryaxis.setLabel("%");
            plot.setDomainAxis((CategoryAxis) categoryaxis);
            StackedBarRenderer renderer = new StackedBarRenderer() {

                private static final long serialVersionUID = 1L;

                @Override
                public Paint getItemPaint(final int row, final int column) {
                    return (row == 0) ? Color.red : (row == 1) ? Color.green
                            : (row == 2) ? Color.blue : Color.white;
                }
            };
            renderer.setItemLabelsVisible(false);
            plot.setRenderer(renderer);

            return new JFreeChartImage("image", chart, 350, 40) {

                private static final long serialVersionUID = 1L;

                @Override
                protected void onComponentTag(ComponentTag tag) {
                    tag.setName("img");
                    super.onComponentTag(tag);
                }
            };
        }
    }

    private class FileSystemTreeTable extends TreeTable {

        private static final long serialVersionUID = 1L;

        public FileSystemTreeTable(String id, TreeModel model, IColumn[] columns) {
            super(id, model, columns);
            add(new AttributeModifier("class", true, new Model<String>("table")));
        }

        private class TreeFragment extends Fragment {

            private static final long serialVersionUID = 1L;

            public TreeFragment(String id, final TreeNode node, int level,
                    final IRenderNodeCallback renderNodeCallback) {
                super(id, "fragment", FileSystemTreeTable.this);

                add(newIndentation(this, "indent", node, level));
                add(newJunctionLink(this, "link", "image", node));

                MarkupContainer nodeLink = newNodeLink(this, "nodeLink", node);
                nodeLink.setEnabled(false);
                nodeLink.add(newNodeIcon(nodeLink, "icon", node));
                nodeLink.add(new Label("label", new AbstractReadOnlyModel<Object>() {

                    private static final long serialVersionUID = 1L;

                    @Override
                    public Object getObject() {
                        return renderNodeCallback.renderNode(node);
                    }
                }));
                add(nodeLink);
            }
        }

        @Override
        protected Component newTreePanel(MarkupContainer parent, String id, final TreeNode node, 
                                         int level, IRenderNodeCallback renderNodeCallback) {
            return new TreeFragment(id, node, level, renderNodeCallback);
        }

        @Override
        protected Component newNodeIcon(MarkupContainer parent, String id, final TreeNode node) {

            return new WebMarkupContainer(id) {

                private static final long serialVersionUID = 1L;

                @Override
                protected void onComponentTag(ComponentTag tag) {
                    super.onComponentTag(tag);

                    if (((FileSystemModel) ((DefaultMutableTreeNode) node).getUserObject()).isGroup())
                        tag.put("style", "background-image: url('images/server.png')");
                    else 
                        tag.put("style", "background-image: url('images/hard_disk.png')");
                    tag.put("title", ((FileSystemModel) ((DefaultMutableTreeNode) node).getUserObject()).getDescription());
                }
            };
        }
    };

    protected class FileSystemModel implements Serializable {

        private static final long serialVersionUID = -1L;

        public static final int KILO = 1000;
        public static final int MEGA = 1000000;
        
        private final int diskSpaceDisplayLength = 8;
        
        private NumberFormat formatter;
        
        private String directoryPath;
        private String description;
        
        private long overallDiskSpace = 0;
        private long usedDiskSpace = 0;
        private long usableDiskSpace = 0;
        private long freeDiskSpace = 0;
        private long minimumFreeDiskSpace = 0;
        
        private boolean isGroup = false;
        
        public FileSystemModel() {
            this.formatter = DecimalFormat.getInstance();
            this.formatter.setMaximumFractionDigits(3);
            this.formatter.setMinimumIntegerDigits(1);
        }

        public void setDirectoryPath(String directoryPath) {
            this.directoryPath = directoryPath;
        }

        public String getDirectoryPath() {
            return directoryPath;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }

        public void setOverallDiskSpace(long overallDiskSpace) {
            this.overallDiskSpace = overallDiskSpace;
        }

        public long getOverallDiskSpaceLong() {
            return this.overallDiskSpace;
        }

        public String getOverallDiskSpaceString() {
            String overallDiskSpaceString = this.formatter.format(new Float(this.overallDiskSpace)/FileSystemModel.KILO);
            return overallDiskSpaceString.substring(0, Math.min(overallDiskSpaceString.length(), this.diskSpaceDisplayLength)) + " GB";
        }

        public void setUsedDiskSpace(long usedDiskSpace) {
            this.usedDiskSpace = usedDiskSpace;
        }

        public long getUsedDiskSpaceLong() {
            return this.usedDiskSpace;
        }

        public String getUsedDiskSpaceString() {
            String usedDiskSpaceString = this.formatter.format(new Float(this.usedDiskSpace)/FileSystemModel.KILO);
            return usedDiskSpaceString.substring(0, Math.min(usedDiskSpaceString.length(), this.diskSpaceDisplayLength)) + " GB";
        }

        public void setUsableDiskSpace(long usableDiskSpace) {
            this.usableDiskSpace = usableDiskSpace;
        }

        public long getUsableDiskSpaceLong() {
            return this.usableDiskSpace;
        }

        public String getUsableDiskSpaceString() {
            String usableDiskSpaceString = this.formatter.format(new Float(this.usableDiskSpace)/FileSystemModel.KILO);
            return usableDiskSpaceString.substring(0, Math.min(usableDiskSpaceString.length(), this.diskSpaceDisplayLength)) + " GB";
        }

        public void setFreeDiskSpace(long freeDiskSpace) {
            this.freeDiskSpace = freeDiskSpace;
        }

        public long getFreeDiskSpaceLong() {
            return this.freeDiskSpace;
        }

        public String getFreeDiskSpaceString() {
            String freeDiskSpaceString = this.formatter.format(new Float(this.freeDiskSpace)/FileSystemModel.KILO);
            return freeDiskSpaceString.substring(0, Math.min(freeDiskSpaceString.length(), this.diskSpaceDisplayLength)) + " GB";
        }

        public void setMinimumFreeDiskSpace(long minimumFreeDiskSpace) {
            this.minimumFreeDiskSpace = minimumFreeDiskSpace;
        }

        public long getMinimumFreeDiskSpaceLong() {
            return this.minimumFreeDiskSpace;
        }

        public String getMinimumFreeDiskSpaceString() {
            String minimumFreeDiskSpaceString = this.formatter.format(new Float(this.minimumFreeDiskSpace)/FileSystemModel.KILO);
            return minimumFreeDiskSpaceString.substring(0, Math.min(minimumFreeDiskSpaceString.length(), this.diskSpaceDisplayLength)) + " GB";
        }

        public void setGroup(boolean isGroup) {
            this.isGroup = isGroup;
        }

        public boolean isGroup() {
            return isGroup;
        }
    }
}