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
 * Portions created by the Initial Developer are Copyright (C) 2014
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
import java.awt.Component;
import java.awt.Graphics;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.table.TableCellRenderer;
import javax.swing.tree.TreeModel;

/**
 *
 * @author Devishree
 * @version 2.0
 */
public class TreeTableCellRenderer extends JTree implements TableCellRenderer {

    protected int visibleRow;
    private TreeTable treeTable;
    private Color whiteColor = new Color(254, 254, 254);
    private Color alternateColor = new Color(237, 243, 254);
    private Color selectedColor = new Color(142, 104, 104);
//    private Color alternateColor = new Color(237, 243, 254);
//    private Color selectedColor = new Color(176, 190, 217);
//    private Color odd = new Color(226, 228, 255);

    public TreeTableCellRenderer(TreeTable treeTable, TreeModel model) {
        super(model);
        this.treeTable = treeTable;
        setRowHeight(35);
    }

    @Override
    public void setRowHeight(int rowHeight) {
        if (rowHeight > 0) {
            super.setRowHeight(rowHeight);
            if (treeTable != null && treeTable.getRowHeight() != rowHeight) {
                treeTable.setRowHeight(getRowHeight());
            }
        }
    }

    @Override
    public void setBounds(int x, int y, int w, int h) {
        super.setBounds(x, 0, w, treeTable.getHeight());
    }

    @Override
    public void paint(Graphics g) {
        g.translate(0, -visibleRow * getRowHeight());
        super.paint(g);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        JLabel label = new JLabel();
//        Color fg;
        if (isSelected) {
//            fg = Color.white;
//            table.setForeground(fg);
            setBackground(selectedColor);
            table.setForeground(Color.WHITE);
        } else {
            setBackground((row % 2 == 0 ? alternateColor : whiteColor));
            setForeground(Color.BLACK);
//            if (row % 2 == 0) {
//                setBackground(Color.WHITE);
//            } else {
//                setBackground(odd);
//            }
//            fg = Color.black;
//             table.setForeground(fg);
//            setBackground(alternateColor);
//            table.setBackground(alternateColor);

        }
        visibleRow = row;
        if (ApplicationContext.isLocal && row == 0 && column == 0) {
            label.setIcon(new ImageIcon(getClass().getResource("/in/raster/mayam/form/images/search.png")));
            label.setOpaque(true);
            label.setBackground(Color.WHITE);
            return label;
        }
//        else if(row==0 && column==0) {
//            label.setIcon(null);
//            return label;
//        }
        return this;
    }
}