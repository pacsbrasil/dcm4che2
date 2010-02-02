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

package org.dcm4chee.dashboard.web.systeminfo;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.extensions.markup.html.tree.table.ColumnLocation;
import org.apache.wicket.extensions.markup.html.tree.table.IColumn;
import org.apache.wicket.extensions.markup.html.tree.table.PropertyRenderableColumn;
import org.apache.wicket.extensions.markup.html.tree.table.PropertyTreeColumn;
import org.apache.wicket.extensions.markup.html.tree.table.TreeTable;
import org.apache.wicket.extensions.markup.html.tree.table.ColumnLocation.Alignment;
import org.apache.wicket.extensions.markup.html.tree.table.ColumnLocation.Unit;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.dcm4chee.dashboard.model.SystemPropertyModel;
import org.dcm4chee.dashboard.util.CSSUtils;
import org.dcm4chee.dashboard.web.WicketApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Robert David <robert.david@agfa.com>
 * @version $Revision$ $Date$
 * @since 18.11.2009
 */
public class SystemInfoPanel extends Panel {
    
    private static final long serialVersionUID = 1L;
    
    private static Logger log = LoggerFactory.getLogger(SystemInfoPanel.class);

    public SystemInfoPanel(String id) {
        super(id);
    }
    
    @Override
    public void onBeforeRender() {
        super.onBeforeRender();
        
        try {
            Class<?> memoryPoolMXBean = Thread.currentThread().getContextClassLoader()
                .loadClass("java.lang.management.MemoryPoolMXBean");
            Class<?> memoryUsage = Thread.currentThread().getContextClassLoader()
                .loadClass("java.lang.management.MemoryUsage");
            
            List<MemoryInstanceModel> memoryInstanceList = new ArrayList<MemoryInstanceModel>();

            for (Object pool : (List<?>) Thread.currentThread().getContextClassLoader()
                                    .loadClass("java.lang.management.ManagementFactory")
                                    .getMethod("getMemoryPoolMXBeans", new Class[0])
                                    .invoke(null, new Object[0])) {
                Object usage = memoryPoolMXBean
                                .getMethod("getUsage", new Class[0])
                                .invoke(pool, new Object[0]);
                if (usage != null) {
                    memoryInstanceList.add(
                            new MemoryInstanceModel(
                                    memoryPoolMXBean
                                        .getMethod("getName", new Class[0])
                                        .invoke(pool, new Object[0]).toString(), 
                                    memoryPoolMXBean
                                        .getMethod("getType", new Class[0])
                                        .invoke(pool, new Object[0]).toString(), 
                                    (Long) memoryUsage.getMethod("getInit", new Class[0]).invoke(usage, new Object[0]), 
                                    (Long) memoryUsage.getMethod("getUsed", new Class[0]).invoke(usage, new Object[0]), 
                                    (Long) memoryUsage.getMethod("getCommitted", new Class[0]).invoke(usage, new Object[0]), 
                                    (Long) memoryUsage.getMethod("getMax", new Class[0]).invoke(usage, new Object[0])));
                }
            }

            add(new ListView<MemoryInstanceModel>("memory-instance-rows", memoryInstanceList) {

                private static final long serialVersionUID = 1L;

                @Override
                protected void populateItem(ListItem<MemoryInstanceModel> item) {
                    MemoryInstanceModel memoryInstanceModel = (MemoryInstanceModel) item.getModelObject();
                    item.add(new Label("name", String.valueOf(memoryInstanceModel.getName())));
                    item.add(new Label("type", String.valueOf(memoryInstanceModel.getType())));
                    item.add(new Label("init", String.valueOf(memoryInstanceModel.getInit())));
                    item.add(new Label("used", String.valueOf(memoryInstanceModel.getUsed())));
                    item.add(new Label("committed", String.valueOf(memoryInstanceModel.getCommitted())));
                    item.add(new Label("max", String.valueOf(memoryInstanceModel.getMax())));
                    item.add(new AttributeModifier("class", true, new Model<String>(CSSUtils.getRowClass(item.getIndex()))));
                }
            });
            
            DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(new SystemPropertyModel());

            Map<String, List<SystemPropertyModel>> propertyMap = ((WicketApplication) getApplication()).getDashboardService().getSystemProperties();
            for (String key : propertyMap.keySet()) {
                
                SystemPropertyModel group = new SystemPropertyModel();
                group.setLabel(key);
                DefaultMutableTreeNode groupNode;
                rootNode.add(groupNode = new DefaultMutableTreeNode(group));

                for (SystemPropertyModel propertyModel : propertyMap.get(key)) {
                    groupNode.add(new DefaultMutableTreeNode(propertyModel));
                }
            }

            SystemPropertyTreeTable systemPropertyTreeTable = new SystemPropertyTreeTable("systemproperty-tree-table", 
                    new DefaultTreeModel(rootNode), new IColumn[] {
                new PropertyTreeColumn(new ColumnLocation(
                        Alignment.LEFT, 40, Unit.PERCENT), 
                        new ResourceModel(
                                "dashboard.systemproperty.table.column.label").wrapOnAssignment(this).getObject(), 
                                "userObject.label"),
                new PropertyRenderableColumn(new ColumnLocation(
                        Alignment.RIGHT, 30, Unit.PERCENT), 
                        new ResourceModel(
                                "dashboard.systemproperty.table.column.name").wrapOnAssignment(this).getObject(),
                                "userObject.name"), 
                new PropertyRenderableColumn(new ColumnLocation(
                        Alignment.RIGHT, 30, Unit.PERCENT), 
                        new ResourceModel(
                                "dashboard.systemproperty.table.column.value").wrapOnAssignment(this).getObject(),
                                "userObject.value")
            });
            systemPropertyTreeTable.getTreeState().setAllowSelectMultiple(true);
            systemPropertyTreeTable.getTreeState().expandAll();
            systemPropertyTreeTable.setRootLess(true);
            add(systemPropertyTreeTable);
        } catch (Exception e) {
            log.error(this.getClass().toString() + ": " + "onBeforeRender: " + e.getMessage());
            log.debug("Exception: ", e);
            this.getApplication().getSessionStore().setAttribute(getRequest(), "exception", e);
            throw new RuntimeException();
        }
    }
    
    private class SystemPropertyTreeTable extends TreeTable {

        private static final long serialVersionUID = 1L;

        public SystemPropertyTreeTable(String id, TreeModel model, IColumn[] columns) {
            super(id, model, columns);
            add(new AttributeModifier("class", true, new Model<String>("table")));
        }

        private class TreeFragment extends Fragment {

            private static final long serialVersionUID = 1L;

            public TreeFragment(String id, final TreeNode node, int level,
                    final IRenderNodeCallback renderNodeCallback) {
                super(id, "fragment", SystemPropertyTreeTable.this);

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

                    tag.put("style", "background-image: url('images/" + 
                    ((((SystemPropertyModel) ((DefaultMutableTreeNode) node).getUserObject()).getGroup() == null) ? 
                        "folder_open" : "property") + ".gif')");
                }
            };
        }
    };

    private class MemoryInstanceModel implements Serializable {
        
        private static final long serialVersionUID = 1L;
        
        public static final int MEGA = 1048576;
        
        private final int memoryDisplayLength = 8;
        
        private NumberFormat formatter;

        private String name;
        private String type;
        
        private long init;
        private long used;
        private long committed;
        private long max;
        
        public MemoryInstanceModel(String name, String type, long init, long used, long committed, long max) {
            this.name = name;
            this.type = type;
            this.init = init;
            this.used = used;
            this.committed = committed;
            this.max = max;
            
            this.formatter = DecimalFormat.getInstance();
            this.formatter.setMaximumFractionDigits(3);
            this.formatter.setMinimumIntegerDigits(1);
        }
        
        public String getName() {
            return name;
        }
        
        public String getType() {
            return type;
        }
        
        public String getInit() {
            String initMemoryString = this.formatter.format(new Float(this.init)/MemoryInstanceModel.MEGA);
            return initMemoryString.substring(0, Math.min(initMemoryString.length(), this.memoryDisplayLength)) + " MB";
        }
        
        public String getUsed() {
            String usedMemoryString = this.formatter.format(new Float(this.used)/MemoryInstanceModel.MEGA);
            return usedMemoryString.substring(0, Math.min(usedMemoryString.length(), this.memoryDisplayLength)) + " MB";
        }
        
        public String getCommitted() {
            String committedMemoryString = this.formatter.format(new Float(this.committed)/MemoryInstanceModel.MEGA);
            return committedMemoryString.substring(0, Math.min(committedMemoryString.length(), this.memoryDisplayLength)) + " MB";
        }
        
        public String getMax() {
            String maxMemoryString = this.formatter.format(new Float(this.max)/MemoryInstanceModel.MEGA);
            return maxMemoryString.substring(0, Math.min(maxMemoryString.length(), this.memoryDisplayLength)) + " MB";
        }
    }
}