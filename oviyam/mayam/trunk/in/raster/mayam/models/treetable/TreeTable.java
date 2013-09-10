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
 *
 * The Initial Developer of the Original Code is
 * Raster Images
 * Portions created by the Initial Developer are Copyright (C) 2009-2010
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Babu Hussain A
 * Devishree V
 * Meer Asgar Hussain B
 * Prakash J
 * Suresh V
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
package in.raster.mayam.models.treetable;

import in.raster.mayam.context.ApplicationContext;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.JTable;

/**
 *
 * @author Devishree
 * @version 2.0
 */
public class TreeTable extends JTable {

    private TreeTableCellRenderer tree;

    public TreeTable() {
        super();
    }

    public void setTreeTableModel(AbstractTreeTableModel treeTableModel) {
        tree = new TreeTableCellRenderer(this, treeTableModel);
        if (!ApplicationContext.isLocal) {
            tree.setRootVisible(false);
        }
        super.setModel(new TreeTableModelAdapter(treeTableModel, tree));

        TreeTableSelectionModel selectionmodel = new TreeTableSelectionModel();
        tree.setSelectionModel(selectionmodel); //For the tree
        setSelectionModel(selectionmodel.getListSelectionModel()); //For the table

        setDefaultRenderer(TreeTableModel.class, tree);
        setDefaultEditor(TreeTableModel.class, new TreeTableCellEditor(tree, this));
        getTableHeader().setPreferredSize(new Dimension(this.getWidth(), 25));
        getTableHeader().setFont(new Font("Lucida Grande", Font.BOLD, 14));
        getTableHeader().setForeground(new Color(255, 138, 0));
        getTableHeader().setBackground(new Color(0, 0, 0));
        setShowGrid(false);
//        setShowGrid(true);
//        setShowVerticalLines(true);
//        setShowHorizontalLines(true);
        setIntercellSpacing(new Dimension(0, 0));
    }
}
