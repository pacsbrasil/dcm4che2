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

package org.dcm4chee.dashboard.web.filesystem;

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
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.dcm4chee.dashboard.web.WicketApplication;
import org.dcm4chee.dashboard.web.common.JFreeChartImage;
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
    }
    
    @Override
    public void onBeforeRender() {
        super.onBeforeRender();

        try {
            DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(new FileSystemModel());
            for (String groupname : ((WicketApplication) getApplication()).getDashboardService().listAllFileSystemGroups()) {
                FileSystemModel group = new FileSystemModel();
                
                int index = groupname.indexOf("group=");
                if (index < 0) continue;
                group.setDirectoryPath(groupname.substring(index + 6));
                group.setDescription(groupname + ",AET=" + ((WicketApplication) getApplication())
                        .getDashboardService().getDefaultRetrieveAETitle(groupname));
                group.setGroup(true);
                DefaultMutableTreeNode groupNode;
                rootNode.add(groupNode = new DefaultMutableTreeNode(group));

                File[] fileSystems = null;
                try {
                    fileSystems = ((WicketApplication) getApplication()).getDashboardService().listFileSystemsOfGroup(groupname);
                } catch (MBeanException mbe) {
                }
                
                if (!((fileSystems == null) || (fileSystems.length == 0))) {
                    long minBytesFree = ((WicketApplication) getApplication()).getDashboardService().getMinimumFreeDiskSpaceOfGroup(groupname);
                    
                    for (File file : fileSystems) {
                        FileSystemModel fsm = new FileSystemModel();
                        fsm.setDirectoryPath(file.getName());                            
                        fsm.setDescription(file.getName().startsWith("tar:") ? file.getName() : file.getAbsolutePath());
                        fsm.setOverallDiskSpace(file.getTotalSpace() / FileSystemModel.MEGA);
                        fsm.setUsedDiskSpace(Math.max((file.getTotalSpace() - file.getUsableSpace()) / FileSystemModel.MEGA, 0));
                        fsm.setFreeDiskSpace(Math.max(file.getUsableSpace() / FileSystemModel.MEGA, 0));
                        fsm.setMinimumFreeDiskSpace(fsm.getOverallDiskSpaceLong() == 0 ? 0 : minBytesFree / FileSystemModel.MEGA);
                        fsm.setUsableDiskSpace(Math.max((file.getUsableSpace() - minBytesFree) / FileSystemModel.MEGA, 0));
                        fsm.setRemainingTime(Math.max((file.getUsableSpace() - minBytesFree) / 
                                ((WicketApplication) getApplication()).getDashboardService().getExpectedDataVolumePerDay(groupname), 0));
                        
                        group.setOverallDiskSpace(group.getOverallDiskSpaceLong() + fsm.getOverallDiskSpaceLong());
                        group.setUsedDiskSpace(group.getUsedDiskSpaceLong() + fsm.getUsedDiskSpaceLong());
                        group.setFreeDiskSpace(group.getFreeDiskSpaceLong() + fsm.getFreeDiskSpaceLong());
                        group.setMinimumFreeDiskSpace(group.getMinimumFreeDiskSpaceLong() + fsm.getMinimumFreeDiskSpaceLong());
                        group.setUsableDiskSpace(group.getUsableDiskSpaceLong() + fsm.getUsableDiskSpaceLong());
                        group.setRemainingTime(group.getRemainingTime() + fsm.getRemainingTime());
                        groupNode.add(new DefaultMutableTreeNode(fsm));
                    }
                }
            }

            String[] otherFileSystems = ((WicketApplication) getApplication()).getDashboardService().listOtherFileSystems();
            if (otherFileSystems != null && otherFileSystems.length > 0) {

                FileSystemModel group = new FileSystemModel();
                group.setDirectoryPath(new ResourceModel("dashboard.filesystem.group.other").wrapOnAssignment(this).getObject());
                group.setGroup(true);
                group.setRemainingTime(-1);
                DefaultMutableTreeNode groupNode;
                rootNode.add(groupNode = new DefaultMutableTreeNode(group));

                for (String otherFileSystem : otherFileSystems) {
                    File file = new File(otherFileSystem);
                    FileSystemModel fsm = new FileSystemModel();
                    fsm.setDirectoryPath(file.getAbsolutePath());                            
                    fsm.setDescription(file.getName().startsWith("tar:") ? file.getName() : file.getAbsolutePath());
                    fsm.setOverallDiskSpace(file.getTotalSpace() / FileSystemModel.MEGA);
                    fsm.setUsedDiskSpace(Math.max((file.getTotalSpace() - file.getUsableSpace()) / FileSystemModel.MEGA, 0));
                    fsm.setFreeDiskSpace(Math.max(file.getUsableSpace() / FileSystemModel.MEGA, 0));
                    fsm.setMinimumFreeDiskSpace(fsm.getOverallDiskSpaceLong() / FileSystemModel.MEGA);
                    fsm.setUsableDiskSpace(Math.max(file.getUsableSpace() / FileSystemModel.MEGA, 0));
                    fsm.setRemainingTime(-1);
                    
                    group.setOverallDiskSpace(group.getOverallDiskSpaceLong() + fsm.getOverallDiskSpaceLong());
                    group.setUsedDiskSpace(group.getUsedDiskSpaceLong() + fsm.getUsedDiskSpaceLong());
                    group.setFreeDiskSpace(group.getFreeDiskSpaceLong() + fsm.getFreeDiskSpaceLong());
                    group.setMinimumFreeDiskSpace(group.getMinimumFreeDiskSpaceLong() + fsm.getMinimumFreeDiskSpaceLong());
                    group.setUsableDiskSpace(group.getUsableDiskSpaceLong() + fsm.getUsableDiskSpaceLong());
                    group.setVisible(false);
                    groupNode.add(new DefaultMutableTreeNode(fsm));
                }
            }
            
            FileSystemTreeTable fileSystemTreeTable = new FileSystemTreeTable("filesystem-tree-table", 
                    new DefaultTreeModel(rootNode), new IColumn[] {
                new PropertyTreeColumn(new ColumnLocation(
                        Alignment.LEFT, 17, Unit.PERCENT), 
                        new ResourceModel(
                                "dashboard.filesystem.table.column.name").wrapOnAssignment(this).getObject(), 
                                "userObject.directoryPath"),
                new ImageRenderableColumn(new ColumnLocation(
                        Alignment.RIGHT, 30, Unit.PERCENT), 
                        new ResourceModel(
                                "dashboard.filesystem.table.column.image").wrapOnAssignment(this).getObject(),
                                "userObject.directoryPath"),
                new PropertyRenderableColumn(new ColumnLocation(
                        Alignment.RIGHT, 8, Unit.PERCENT),
                        new ResourceModel(
                                "dashboard.filesystem.table.column.overall").wrapOnAssignment(this).getObject(),
                                "userObject.overallDiskSpaceString"), 
                new PropertyRenderableColumn(new ColumnLocation(
                        Alignment.RIGHT, 8, Unit.PERCENT),
                        new ResourceModel(
                                "dashboard.filesystem.table.column.used").wrapOnAssignment(this).getObject(),
                                "userObject.usedDiskSpaceString"),
                new PropertyRenderableColumn(new ColumnLocation(
                        Alignment.RIGHT, 8, Unit.PERCENT),
                        new ResourceModel(
                                "dashboard.filesystem.table.column.free").wrapOnAssignment(this).getObject(),
                                "userObject.freeDiskSpaceString"),
                new PropertyRenderableColumn(new ColumnLocation(
                        Alignment.RIGHT, 8, Unit.PERCENT),
                        new ResourceModel(
                                "dashboard.filesystem.table.column.minimumfree").wrapOnAssignment(this).getObject(),
                                "userObject.minimumFreeDiskSpaceString"),
                new PropertyRenderableColumn(new ColumnLocation(
                        Alignment.RIGHT, 8, Unit.PERCENT),
                        new ResourceModel(
                                "dashboard.filesystem.table.column.usable").wrapOnAssignment(this).getObject(),
                                "userObject.usableDiskSpaceString"), 
                new PropertyRenderableColumn(new ColumnLocation(
                        Alignment.RIGHT, 13, Unit.PERCENT),
                        new ResourceModel(
                                "dashboard.filesystem.table.column.remainingtime").wrapOnAssignment(this).getObject(),
                                "userObject.remainingTimeString")
            });
            fileSystemTreeTable.getTreeState().setAllowSelectMultiple(true);
            fileSystemTreeTable.getTreeState().collapseAll();
            fileSystemTreeTable.setRootLess(true);
            add(fileSystemTreeTable);
        } catch (Exception e) {
e.printStackTrace();
            log.error(this.getClass().toString() + ": " + "onBeforeRender: " + e.getMessage());
            log.debug("Exception: ", e);
            this.getApplication().getSessionStore().setAttribute(getRequest(), "exception", e);
            throw new RuntimeException();
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

            if (!((FileSystemModel) ((DefaultMutableTreeNode) node).getUserObject()).isVisible())
                return new Label("image") {
                
                    private static final long serialVersionUID = 1L;

                    @Override
                    protected void onComponentTag(ComponentTag tag) {
                        super.onComponentTag(tag);
                    }
                };

            
            FileSystemModel fsm = (FileSystemModel) ((DefaultMutableTreeNode) node).getUserObject();

            DefaultCategoryDataset dataset = new DefaultCategoryDataset();
            dataset.addValue(fsm.getOverallDiskSpaceLong() == 0 ? 0 : (100 * 
                    fsm.getUsedDiskSpaceLong())
                    / fsm.getOverallDiskSpaceLong(), new Integer(1), "");
            dataset.addValue(fsm.getOverallDiskSpaceLong() == 0 ? 0 : (100 * 
                    fsm.getUsableDiskSpaceLong()
                    / fsm.getOverallDiskSpaceLong()), new Integer(2), "");
            dataset.addValue(fsm.getOverallDiskSpaceLong() == 0 ? 0 : 100, new Integer(3), "");
            
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
            plot.setDomainAxis(categoryaxis);
            StackedBarRenderer renderer = new StackedBarRenderer() {

                private static final long serialVersionUID = 1L;

                @Override
                public Paint getItemPaint(final int row, final int column) {
                    return (row == 0) ? Color.red : (row == 1) ? Color.green
                            : (row == 2) ? Color.blue : Color.white;
                }
            };
            renderer.setBaseItemLabelsVisible(false);
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

                add(newNodeLink(this, "nodeLink", node)
                .add(newNodeIcon(this, "icon", node))
                .add(new Label("label", new AbstractReadOnlyModel<Object>() {

                    private static final long serialVersionUID = 1L;

                    @Override
                    public Object getObject() {
                        return renderNodeCallback.renderNode(node);
                    }
                }))
                .setEnabled(false));
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
                        tag.put("style", "background-image: url('images/filesystemgroup.gif')");
                    else 
                        if (((FileSystemModel) ((DefaultMutableTreeNode) node).getUserObject()).getDirectoryPath().contains("tar:"))
                            tag.put("style", "background-image: url('images/folder_files.gif')");
                        else
                            tag.put("style", "background-image: url('images/filesystem.gif')");

                    tag.put("style", "background-image: url('images/" + 
                            (((FileSystemModel) ((DefaultMutableTreeNode) node).getUserObject()).isGroup() ?  
                                    "filesystemgroup" :
                                    ((FileSystemModel) ((DefaultMutableTreeNode) node).getUserObject()).getDirectoryPath().contains("tar:") ?
                                            "folder_files" : 
                                            "filesystem") + ".gif')"
                    );
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
        
        private NumberFormat memoryFormatter;
        private NumberFormat daysFormatter;
        
        private String directoryPath;
        private String description;
        
        private long overallDiskSpace = 0;
        private long usedDiskSpace = 0;
        private long usableDiskSpace = 0;
        private long freeDiskSpace = 0;
        private long minimumFreeDiskSpace = 0;
        
        private boolean isGroup = false;
        private boolean visible = true;
        
        private long remainingTime = 0;
        
        public FileSystemModel() {
            this.memoryFormatter = DecimalFormat.getInstance();
            this.memoryFormatter.setMaximumFractionDigits(3);
            this.memoryFormatter.setMinimumIntegerDigits(1);

            this.daysFormatter = DecimalFormat.getInstance();
            this.daysFormatter.setMaximumFractionDigits(0);
            this.daysFormatter.setMinimumIntegerDigits(1);
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
            String overallDiskSpaceString = this.memoryFormatter.format(new Float(this.overallDiskSpace)/FileSystemModel.KILO);
            return overallDiskSpaceString.substring(0, Math.min(overallDiskSpaceString.length(), this.diskSpaceDisplayLength)) + " GB";
        }

        public void setUsedDiskSpace(long usedDiskSpace) {
            this.usedDiskSpace = usedDiskSpace;
        }

        public long getUsedDiskSpaceLong() {
            return this.usedDiskSpace;
        }

        public String getUsedDiskSpaceString() {
            String usedDiskSpaceString = this.memoryFormatter.format(new Float(this.usedDiskSpace)/FileSystemModel.KILO);
            return usedDiskSpaceString.substring(0, Math.min(usedDiskSpaceString.length(), this.diskSpaceDisplayLength)) + " GB";
        }

        public void setUsableDiskSpace(long usableDiskSpace) {
            this.usableDiskSpace = usableDiskSpace;
        }

        public long getUsableDiskSpaceLong() {
            return this.usableDiskSpace;
        }

        public String getUsableDiskSpaceString() {
            String usableDiskSpaceString = this.memoryFormatter.format(new Float(this.usableDiskSpace)/FileSystemModel.KILO);
            return usableDiskSpaceString.substring(0, Math.min(usableDiskSpaceString.length(), this.diskSpaceDisplayLength)) + " GB";
        }

        public void setFreeDiskSpace(long freeDiskSpace) {
            this.freeDiskSpace = freeDiskSpace;
        }

        public long getFreeDiskSpaceLong() {
            return this.freeDiskSpace;
        }

        public String getFreeDiskSpaceString() {
            String freeDiskSpaceString = this.memoryFormatter.format(new Float(this.freeDiskSpace)/FileSystemModel.KILO);
            return freeDiskSpaceString.substring(0, Math.min(freeDiskSpaceString.length(), this.diskSpaceDisplayLength)) + " GB";
        }

        public void setMinimumFreeDiskSpace(long minimumFreeDiskSpace) {
            this.minimumFreeDiskSpace = minimumFreeDiskSpace;
        }

        public long getMinimumFreeDiskSpaceLong() {
            return this.minimumFreeDiskSpace;
        }

        public String getMinimumFreeDiskSpaceString() {
            String minimumFreeDiskSpaceString = this.memoryFormatter.format(new Float(this.minimumFreeDiskSpace)/FileSystemModel.KILO);
            return minimumFreeDiskSpaceString.substring(0, Math.min(minimumFreeDiskSpaceString.length(), this.diskSpaceDisplayLength)) + " GB";
        }

        public void setGroup(boolean isGroup) {
            this.isGroup = isGroup;
        }

        public boolean isGroup() {
            return isGroup;
        }

        public void setVisible(boolean visible) {
            this.visible = visible;
        }

        public boolean isVisible() {
            return visible;
        }

        public void setRemainingTime(long l) {
            this.remainingTime = l;
        }

        public long getRemainingTime() {
            return remainingTime;
        }
        
        public String getRemainingTimeString() {
            return this.remainingTime >= 0 ? "~ " + this.daysFormatter.format(new Float(this.remainingTime)) : ""; 
        }
    }
}