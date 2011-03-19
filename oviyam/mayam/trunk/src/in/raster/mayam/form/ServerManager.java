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
package in.raster.mayam.form;

import in.raster.mayam.context.ApplicationContext;
import in.raster.mayam.delegate.EchoService;
import in.raster.mayam.form.dialog.EchoStatus;
import in.raster.mayam.form.display.Display;
import in.raster.mayam.model.AEModel;
import in.raster.mayam.model.ServerModel;
import in.raster.mayam.model.table.PresetTableModel;
import in.raster.mayam.model.table.ServerTableModel;
import in.raster.mayam.model.table.renderer.CellRenderer;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultCellEditor;
import javax.swing.InputMap;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.table.TableCellEditor;
import org.dcm4che.util.DcmURL;

/**
 *
 * @author  BabuHussain
 * @version 0.5
 *
 */
public class ServerManager extends javax.swing.JPanel implements KeyListener {

    /** Creates new form WindowingManagerPanel */
    private JFrame outerContainer;
    private ServerChangeListener serverChangeListener;

    public ServerManager(JFrame outerContainer) {
        this.outerContainer = outerContainer;
        initComponents();
        initializeDefault();
    }

    public ServerManager() {
        initComponents();
        initializeDefault();
    }

    public void initializeDefault() {
        setServerTableModel();
        serverListTable.addKeyListener(this);
        changeTabActionForTable(serverListTable);

    }

    public void addServerChangeListener(ServerChangeListener serverChangeListener) {
        this.serverChangeListener = serverChangeListener;
        ((ServerTableModel) serverListTable.getModel()).addChangeListener(serverChangeListener);
    }

    public void addListenerToModel() {
        ((ServerTableModel) serverListTable.getModel()).addChangeListener(serverChangeListener);
    }

    private void setServerTableModel() {
        ServerTableModel serverTableModel = new ServerTableModel();
        serverTableModel.setData(ApplicationContext.databaseRef.getServerList());
        serverListTable.setModel(serverTableModel);
        setServerRetrieveComboEditor();
        serverListTable.getColumnModel().getColumn(0).setMinWidth(80);
        serverListTable.getColumnModel().getColumn(1).setMinWidth(80);
        serverListTable.getColumnModel().getColumn(2).setMinWidth(110);
        serverListTable.getColumnModel().getColumn(3).setMaxWidth(55);
        if (serverChangeListener != null) {
            addListenerToModel();
        }

    }

    private void setServerRetrieveComboEditor() {
        String[] retrieveTypeArray = {"C-MOVE", "C-GET", "WADO"};
        JComboBox comboBox = new JComboBox(retrieveTypeArray);
        comboBox.setMaximumRowCount(4);
        TableCellEditor editor = new DefaultCellEditor(comboBox);
        serverListTable.getColumnModel().getColumn(4).setCellEditor(editor);
    }

    private void addOrDeleteServerNotification() {
        serverChangeListener.onServerChange();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        serverListTable = new javax.swing.JTable();
        addButton = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();
        DeleteButton = new javax.swing.JButton();

        jLabel1.setBackground(new java.awt.Color(0, 0, 0));
        jLabel1.setFont(new java.awt.Font("Courier New", 1, 16)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(255, 138, 0));
        jLabel1.setText("Servers");
        jLabel1.setOpaque(true);

        serverListTable.setFont(new java.awt.Font("Courier", 1, 12)); // NOI18N
        serverListTable.setModel(new PresetTableModel());
        serverListTable.setDefaultRenderer(Object.class, new CellRenderer());
        //serverListTable.getTableHeader().setPreferredSize(new Dimension(jScrollPane1.WIDTH,25));
        //serverListTable.setRowHeight(25);

        serverListTable.getTableHeader().setPreferredSize(new Dimension(this.getWidth(), 25));
        Font ff=new Font("Courier New",Font.BOLD,12);
        serverListTable.getTableHeader().setFont(ff);
        serverListTable.setRowHeight(20);
        serverListTable.getTableHeader().setForeground(new Color(255,138,0));
        serverListTable.getTableHeader().setBackground(new Color(0,0,0));
        jScrollPane1.setViewportView(serverListTable);

        addButton.setText("Add");
        addButton.setFocusable(false);
        addButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        addButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        addButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addButtonActionPerformed(evt);
            }
        });

        jButton1.setText("Verify");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        DeleteButton.setText("Delete");
        DeleteButton.setFocusable(false);
        DeleteButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        DeleteButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        DeleteButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                DeleteButtonActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jLabel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 557, Short.MAX_VALUE)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap(261, Short.MAX_VALUE)
                .add(jButton1)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(addButton)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(DeleteButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 89, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 517, Short.MAX_VALUE)
                .addContainerGap())
        );

        layout.linkSize(new java.awt.Component[] {DeleteButton, addButton, jButton1}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(jLabel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 24, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(30, 30, 30)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(DeleteButton)
                    .add(addButton)
                    .add(jButton1))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 187, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    public void changeTabActionForTable(JTable table) {
        InputMap im = table.getInputMap(JTable.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        //  Have the enter key work the same as the tab key

        KeyStroke tab = KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0);
        //  KeyStroke enter = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
        // im.put(enter, im.get(tab));

        //  Disable the right arrow key

        KeyStroke right = KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0);
        im.put(right, "none");

        //  Override the default tab behaviour
        //  Tab to the next editable cell. When no editable cells goto next cell.

        final Action oldTabAction = table.getActionMap().get(im.get(tab));
        Action tabAction = new AbstractAction() {

            public void actionPerformed(ActionEvent e) {
                oldTabAction.actionPerformed(e);
                JTable table = (JTable) e.getSource();
                int rowCount = table.getRowCount();
                int columnCount = table.getColumnCount();
                int row = table.getSelectedRow();
                int column = table.getSelectedColumn();

                while (!table.isCellEditable(row, column)) {
                    column += 1;

                    if (column == columnCount) {
                        column = 0;
                        row += 1;
                    }

                    if (row == rowCount) {
                        row = 0;
                    }

                    //  Back to where we started, get out.

                    if (row == table.getSelectedRow()
                            && column == table.getSelectedColumn()) {
                        break;
                    }
                }
                table.changeSelection(row, column, true, false);
            }
        };
        table.getActionMap().put(im.get(tab), tabAction);
    }

    private void addButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addButtonActionPerformed
        ServerModel serverModel = new ServerModel();
        serverModel.setServerName("Description");
        serverModel.setAeTitle("AETITLE");
        serverModel.setHostName("localhost");
        serverModel.setPort(104);
        serverModel.setRetrieveType("C-MOVE");
        serverModel.setWadoContextPath("wado");
        serverModel.setWadoPort(0);
        serverModel.setWadoProtocol("http");
        serverModel.setRetrieveTransferSyntax("");
        ApplicationContext.databaseRef.insertServer(serverModel);
        setServerTableModel();
        addOrDeleteServerNotification();

}//GEN-LAST:event_addButtonActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        try {
            String serverName = ((ServerTableModel) serverListTable.getModel()).getValueAt(serverListTable.getSelectedRow(), 0);
            AEModel ae = ApplicationContext.databaseRef.getServerDetail(serverName);
            DcmURL url = new DcmURL("dicom://" + ae.getAeTitle() + "@" + ae.getHostName() + ":" + ae.getPort());
            EchoService echo = new EchoService();
            EchoStatus echoStatus = new EchoStatus(outerContainer, true);
            Display.alignScreen(echoStatus);
            echo.checkEcho(url);
            echoStatus.setTitle("Echo Status");
            try {
                if (echo.getStatus().trim().equalsIgnoreCase("EchoSuccess")) {
                    echoStatus.status.setText("Echo dicom://" + ae.getAeTitle() + "@" + ae.getHostName() + ":" + ae.getPort() + " successfully!");
                    echoStatus.setVisible(true);
                } else {
                    echoStatus.status.setText("Echo dicom://" + ae.getAeTitle() + "@" + ae.getHostName() + ":" + ae.getPort() + " not successfully!");
                    echoStatus.setVisible(true);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }//GEN-LAST:event_jButton1ActionPerformed

    private void DeleteButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_DeleteButtonActionPerformed
        if (serverListTable.getSelectedRow() != -1) {
            int canDelete = 0;
            canDelete = JOptionPane.showConfirmDialog(null, "Are you sure want to delete the server", "Delete Confirmation", JOptionPane.YES_NO_OPTION);
            if (canDelete == 0) {
                ServerModel serverModel = ((ServerTableModel) serverListTable.getModel()).getRow(serverListTable.getSelectedRow());
                ApplicationContext.databaseRef.deleteServer(serverModel);
                setServerTableModel();
                addOrDeleteServerNotification();
            }
        }
    }//GEN-LAST:event_DeleteButtonActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton DeleteButton;
    private javax.swing.JButton addButton;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable serverListTable;
    // End of variables declaration//GEN-END:variables

    public void keyTyped(KeyEvent e) {
    }

    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_DELETE) {
            int canDelete = 0;
            canDelete = JOptionPane.showConfirmDialog(null, "Are you sure want to delete the server", "Delete Confirmation", JOptionPane.YES_NO_OPTION);
            if (canDelete == 0) {
                if (serverListTable.getSelectedRow() != -1) {
                    ServerModel serverModel = ((ServerTableModel) serverListTable.getModel()).getRow(serverListTable.getSelectedRow());
                    ApplicationContext.databaseRef.deleteServer(serverModel);
                    setServerTableModel();
                    addOrDeleteServerNotification();
                }
            }
        }
    }
    public void keyReleased(KeyEvent e) {
    }
}
