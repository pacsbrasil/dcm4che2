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
import in.raster.mayam.delegate.DicomServerDelegate;
import in.raster.mayam.delegate.EchoService;
import in.raster.mayam.delegate.QueryService;
import in.raster.mayam.delegate.MoveDelegate;
import in.raster.mayam.form.dialog.EchoStatus;
import in.raster.mayam.form.dialog.StudyAvailabilityStatus;
import in.raster.mayam.form.display.Display;
import in.raster.mayam.model.AEModel;
import in.raster.mayam.model.StudyModel;
import in.raster.mayam.model.table.ServerTableModel;
import in.raster.mayam.model.table.StudyListModel;
import in.raster.mayam.param.QueryParam;
import in.raster.mayam.util.core.MoveScu;
import java.awt.Color;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.SpinnerDateModel;
import javax.swing.UIManager;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.event.ListSelectionEvent;
import org.dcm4che.data.Dataset;
import org.dcm4che.util.DcmURL;
import in.raster.mayam.model.table.renderer.CellRenderer;
import in.raster.mayam.model.table.renderer.HeaderRenderer;
import javax.swing.event.ListSelectionListener;

/**
 *
 * @author  BabuHussain
 * @version 0.5
 *
 */
public class QueryRetrive extends javax.swing.JFrame implements ItemListener, ServerChangeListener, ListSelectionListener {

    /** Creates new form QueryRetrive */
    public QueryRetrive() {
        initComponents();
        refreshModels();
        addModalityItemListener();
        addSearchDateitemListener();
    }

    private void addModalityItemListener() {
        allRadio.addItemListener(this);
        ctRadio.addItemListener(this);
        crRadio.addItemListener(this);
        xaRadio.addItemListener(this);
        mrRadio.addItemListener(this);
        usRadio.addItemListener(this);
        scRadio.addItemListener(this);
        dxRadio.addItemListener(this);
        nmRadio.addItemListener(this);
        otRadio.addItemListener(this);
        pxRadio.addItemListener(this);
        rfRadio.addItemListener(this);
        drRadio.addItemListener(this);
    }

    private void addSearchDateitemListener() {
        betweenRadio.addItemListener(this);
        lastmonthRadio.addItemListener(this);
        lastweekRadio.addItemListener(this);
        yesterdayRadio.addItemListener(this);
        todayRadio.addItemListener(this);
        anydateRadio.addItemListener(this);
    }

    public void refreshModels() {
        setServerTableModel();
        setServerName();
        setSpinnerDateModel();
    }

    private void setServerTableModel() {
        ServerTableModel serverTableModel = new ServerTableModel();
        serverTableModel.setEditable(false);
        serverTableModel.setData(ApplicationContext.databaseRef.getServerList());
        serverListTable.setModel(serverTableModel);
        serverListTable.getSelectionModel().addListSelectionListener(this);
        serverListTable.getColumnModel().getSelectionModel().addListSelectionListener(this);
        if (this.serverListTable.getRowCount() > 0) {
            serverListTable.setRowSelectionInterval(0, 0);
        }
    }

    private void setSpinnerDateModel() {
        SpinnerDateModel spm1 = new SpinnerDateModel();
        SpinnerDateModel spm2 = new SpinnerDateModel();
        SpinnerDateModel spm3 = new SpinnerDateModel();
        fromSpinner.setModel(spm1);
        fromSpinner.setEditor(new JSpinner.DateEditor(fromSpinner, "dd/MM/yyyy"));
        toSpinner.setModel(spm2);
        toSpinner.setEditor(new JSpinner.DateEditor(toSpinner, "dd/MM/yyyy"));
        birthDateSpinner.setModel(spm3);
        birthDateSpinner.setEditor(new JSpinner.DateEditor(birthDateSpinner, "dd/MM/yyyy"));
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        searchDaysGroup = new javax.swing.ButtonGroup();
        modalityGroup = new javax.swing.ButtonGroup();
        jPanel9 = new javax.swing.JPanel();
        jPanel7 = new javax.swing.JPanel();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel3 = new javax.swing.JPanel();
        patientNameText = new javax.swing.JTextField();
        jPanel4 = new javax.swing.JPanel();
        patientIDText = new javax.swing.JTextField();
        jPanel5 = new javax.swing.JPanel();
        accessionNoText = new javax.swing.JTextField();
        jPanel6 = new javax.swing.JPanel();
        birthDateSpinner = new javax.swing.JSpinner();
        jPanel1 = new javax.swing.JPanel();
        anydateRadio = new javax.swing.JRadioButton();
        todayRadio = new javax.swing.JRadioButton();
        yesterdayRadio = new javax.swing.JRadioButton();
        lastweekRadio = new javax.swing.JRadioButton();
        lastmonthRadio = new javax.swing.JRadioButton();
        betweenRadio = new javax.swing.JRadioButton();
        fromSpinner = new javax.swing.JSpinner();
        toSpinner = new javax.swing.JSpinner();
        jPanel2 = new javax.swing.JPanel();
        allRadio = new javax.swing.JRadioButton();
        ctRadio = new javax.swing.JRadioButton();
        crRadio = new javax.swing.JRadioButton();
        xaRadio = new javax.swing.JRadioButton();
        mrRadio = new javax.swing.JRadioButton();
        usRadio = new javax.swing.JRadioButton();
        scRadio = new javax.swing.JRadioButton();
        dxRadio = new javax.swing.JRadioButton();
        nmRadio = new javax.swing.JRadioButton();
        otRadio = new javax.swing.JRadioButton();
        pxRadio = new javax.swing.JRadioButton();
        rfRadio = new javax.swing.JRadioButton();
        drRadio = new javax.swing.JRadioButton();
        serverlistScroll = new javax.swing.JScrollPane();
        serverListTable = new javax.swing.JTable();
        jPanel8 = new javax.swing.JPanel();
        verifyButton = new javax.swing.JButton();
        queryButton = new javax.swing.JButton();
        retrieveButton = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        studyListTable = new javax.swing.JTable();
        serverNameLabel = new javax.swing.JLabel();
        headerLabel = new javax.swing.JLabel();

        setTitle("Query/Retrieve");

        jPanel3.setLayout(new java.awt.BorderLayout());
        jPanel3.add(patientNameText, java.awt.BorderLayout.CENTER);

        jTabbedPane1.addTab("Patient Name", jPanel3);

        jPanel4.setLayout(new java.awt.BorderLayout());
        jPanel4.add(patientIDText, java.awt.BorderLayout.CENTER);

        jTabbedPane1.addTab("Patient ID", jPanel4);

        jPanel5.setLayout(new java.awt.BorderLayout());
        jPanel5.add(accessionNoText, java.awt.BorderLayout.CENTER);

        jTabbedPane1.addTab("Accession #", jPanel5);

        jPanel6.add(birthDateSpinner);

        jTabbedPane1.addTab("Birthdate", jPanel6);

        jPanel1.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        searchDaysGroup.add(anydateRadio);
        anydateRadio.setSelected(true);
        anydateRadio.setText("Any date");

        searchDaysGroup.add(todayRadio);
        todayRadio.setText("Today");

        searchDaysGroup.add(yesterdayRadio);
        yesterdayRadio.setText("Yesterday");

        searchDaysGroup.add(lastweekRadio);
        lastweekRadio.setText("Last week");

        searchDaysGroup.add(lastmonthRadio);
        lastmonthRadio.setText("Last month");

        searchDaysGroup.add(betweenRadio);
        betweenRadio.setText("Between");

        fromSpinner.setEnabled(false);

        toSpinner.setEnabled(false);

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .add(anydateRadio)
                .addContainerGap(183, Short.MAX_VALUE))
            .add(jPanel1Layout.createSequentialGroup()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(yesterdayRadio)
                    .add(lastweekRadio)
                    .add(todayRadio)
                    .add(lastmonthRadio))
                .add(11, 11, 11)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(betweenRadio)
                    .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                        .add(toSpinner, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 156, Short.MAX_VALUE)
                        .add(fromSpinner, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 156, Short.MAX_VALUE))))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .add(anydateRadio, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 15, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(todayRadio, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 23, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(yesterdayRadio, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 10, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(lastweekRadio)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(lastmonthRadio, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 23, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
            .add(jPanel1Layout.createSequentialGroup()
                .add(betweenRadio, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 19, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(fromSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 21, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(toSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );

        jPanel1Layout.linkSize(new java.awt.Component[] {betweenRadio, yesterdayRadio}, org.jdesktop.layout.GroupLayout.VERTICAL);

        jPanel2.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        modalityGroup.add(allRadio);
        allRadio.setFont(new java.awt.Font("Lucida Grande", 1, 10));
        allRadio.setSelected(true);
        allRadio.setText("All");

        modalityGroup.add(ctRadio);
        ctRadio.setFont(new java.awt.Font("Lucida Grande", 1, 10));
        ctRadio.setText("CT");

        modalityGroup.add(crRadio);
        crRadio.setFont(new java.awt.Font("Lucida Grande", 1, 10));
        crRadio.setText("CR");

        modalityGroup.add(xaRadio);
        xaRadio.setFont(new java.awt.Font("Lucida Grande", 1, 10));
        xaRadio.setText("XA");

        modalityGroup.add(mrRadio);
        mrRadio.setFont(new java.awt.Font("Lucida Grande", 1, 10));
        mrRadio.setText("MR");

        modalityGroup.add(usRadio);
        usRadio.setFont(new java.awt.Font("Lucida Grande", 1, 10));
        usRadio.setText("US");

        modalityGroup.add(scRadio);
        scRadio.setFont(new java.awt.Font("Lucida Grande", 1, 10));
        scRadio.setText("SC");

        modalityGroup.add(dxRadio);
        dxRadio.setFont(new java.awt.Font("Lucida Grande", 1, 10));
        dxRadio.setText("DX");

        modalityGroup.add(nmRadio);
        nmRadio.setFont(new java.awt.Font("Lucida Grande", 1, 10));
        nmRadio.setText("NM");

        modalityGroup.add(otRadio);
        otRadio.setFont(new java.awt.Font("Lucida Grande", 1, 10));
        otRadio.setText("OT");

        modalityGroup.add(pxRadio);
        pxRadio.setFont(new java.awt.Font("Lucida Grande", 1, 10));
        pxRadio.setText("PX");

        modalityGroup.add(rfRadio);
        rfRadio.setFont(new java.awt.Font("Lucida Grande", 1, 10));
        rfRadio.setText("RF");

        modalityGroup.add(drRadio);
        drRadio.setFont(new java.awt.Font("Lucida Grande", 1, 10));
        drRadio.setText("DR");
        drRadio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                drRadioActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout jPanel2Layout = new org.jdesktop.layout.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel2Layout.createSequentialGroup()
                        .add(allRadio, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 49, Short.MAX_VALUE)
                        .add(10, 10, 10)
                        .add(crRadio, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 49, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(jPanel2Layout.createSequentialGroup()
                        .add(xaRadio, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 53, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(rfRadio, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 49, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(jPanel2Layout.createSequentialGroup()
                        .add(ctRadio, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 53, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(scRadio, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 49, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(jPanel2Layout.createSequentialGroup()
                        .add(mrRadio, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 53, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(nmRadio)))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel2Layout.createSequentialGroup()
                        .add(dxRadio)
                        .addContainerGap())
                    .add(usRadio, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 57, Short.MAX_VALUE)
                    .add(otRadio, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 57, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, pxRadio, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 57, Short.MAX_VALUE)
                    .add(drRadio, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 57, Short.MAX_VALUE)))
        );

        jPanel2Layout.linkSize(new java.awt.Component[] {crRadio, nmRadio, rfRadio, scRadio}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel2Layout.createSequentialGroup()
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(allRadio)
                    .add(dxRadio)
                    .add(crRadio))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(ctRadio)
                    .add(scRadio)
                    .add(pxRadio))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(mrRadio)
                    .add(nmRadio)
                    .add(usRadio))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel2Layout.createSequentialGroup()
                        .add(otRadio, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(drRadio, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .add(xaRadio)
                    .add(rfRadio))
                .addContainerGap())
        );

        jPanel2Layout.linkSize(new java.awt.Component[] {crRadio, nmRadio, rfRadio, scRadio}, org.jdesktop.layout.GroupLayout.VERTICAL);

        org.jdesktop.layout.GroupLayout jPanel7Layout = new org.jdesktop.layout.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel7Layout.createSequentialGroup()
                .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
            .add(jTabbedPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 467, Short.MAX_VALUE)
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel7Layout.createSequentialGroup()
                .add(jTabbedPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 74, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel7Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 149, Short.MAX_VALUE))
                .addContainerGap())
        );

        serverListTable.setModel(new ServerTableModel());
        serverlistScroll.setViewportView(serverListTable);

        verifyButton.setText("Verify");
        verifyButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                verifyButtonActionPerformed(evt);
            }
        });

        queryButton.setText("Query");
        queryButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                queryButtonActionPerformed(evt);
            }
        });

        retrieveButton.setText("Retrieve");
        retrieveButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                retrieveButtonActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout jPanel8Layout = new org.jdesktop.layout.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel8Layout.createSequentialGroup()
                .add(verifyButton)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(queryButton)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(retrieveButton)
                .addContainerGap(189, Short.MAX_VALUE))
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel8Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                .add(verifyButton)
                .add(queryButton)
                .add(retrieveButton))
        );

        studyListTable.setModel(new StudyListModel());
        studyListTable.getTableHeader().setDefaultRenderer(new HeaderRenderer());
        studyListTable.setDefaultRenderer(Object.class, new CellRenderer());
        studyListTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                studyListTableMouseClicked(evt);
            }
        });
        jScrollPane2.setViewportView(studyListTable);

        serverNameLabel.setBackground(new java.awt.Color(41, 116, 217));
        serverNameLabel.setFont(new java.awt.Font("Lucida Grande", 1, 13));
        serverNameLabel.setForeground(new java.awt.Color(255, 255, 255));
        serverNameLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        serverNameLabel.setText(" Server Name");
        serverNameLabel.setOpaque(true);

        headerLabel.setBackground(new java.awt.Color(41, 116, 217));
        headerLabel.setFont(new java.awt.Font("Lucida Grande", 1, 13));
        headerLabel.setForeground(new java.awt.Color(255, 255, 255));
        headerLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        headerLabel.setText(" DICOM Nodes ");
        headerLabel.setOpaque(true);

        org.jdesktop.layout.GroupLayout jPanel9Layout = new org.jdesktop.layout.GroupLayout(jPanel9);
        jPanel9.setLayout(jPanel9Layout);
        jPanel9Layout.setHorizontalGroup(
            jPanel9Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel9Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel9Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel9Layout.createSequentialGroup()
                        .add(jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 963, Short.MAX_VALUE)
                        .addContainerGap())
                    .add(jPanel9Layout.createSequentialGroup()
                        .add(jPanel9Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(serverlistScroll, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 496, Short.MAX_VALUE)
                            .add(headerLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 496, Short.MAX_VALUE)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, serverNameLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 496, Short.MAX_VALUE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jPanel9Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                            .add(jPanel7, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(jPanel8, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))))
        );
        jPanel9Layout.setVerticalGroup(
            jPanel9Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel9Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel9Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(headerLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 24, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jPanel9Layout.createSequentialGroup()
                        .add(jPanel9Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jPanel7, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(jPanel9Layout.createSequentialGroup()
                                .add(29, 29, 29)
                                .add(serverlistScroll, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 182, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jPanel9Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jPanel8, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(serverNameLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 23, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 483, Short.MAX_VALUE)
                .addContainerGap())
        );

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel9, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel9, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void setPatientInfoToQueryParam() {
        queryParam.setPatientId(patientIDText.getText());
        queryParam.setPatientName(patientNameText.getText());
        queryParam.setAccessionNo(accessionNoText.getText());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
        Date d1 = (Date) birthDateSpinner.getModel().getValue();
        String dateOfBirth = sdf.format(d1);
        queryParam.setBirthDate(dateOfBirth);
        if (!queryParam.getSearchDays().equalsIgnoreCase("Between")) {
            resetFromAndToDate();
        } else {
            setFromToDate();
        }

    }
    private boolean startSearch = false;
    private void queryButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_queryButtonActionPerformed
        String serverName = ((ServerTableModel) serverListTable.getModel()).getValueAt(serverListTable.getSelectedRow(), 0);
        try {
            startSearch = true;
            AEModel ae = ApplicationContext.databaseRef.getServerDetail(serverName);
            DcmURL url = new DcmURL("dicom://" + ae.getAeTitle() + "@" + ae.getHostName() + ":" + ae.getPort());
            QueryService qs = new QueryService();
            setPatientInfoToQueryParam();
            qs.callFindWithQuery(queryParam.getPatientId(), queryParam.getPatientName(), "", queryParam.getSearchDate(), queryParam.getModality(), queryParam.getAccessionNo(), url);
            Vector studyList = new Vector();
            for (int dataSetCount = 0; dataSetCount < qs.getDatasetVector().size(); dataSetCount++) {
                try {
                    Dataset dataSet = (Dataset) qs.getDatasetVector().elementAt(dataSetCount);
                    StudyModel studyModel = new StudyModel(dataSet);
                    studyList.addElement(studyModel);
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            }
            StudyListModel studyListModel = new StudyListModel();
            studyListModel.setData(studyList);
            studyListTable.setModel(studyListModel);
            boolean dicomServerDetailAlreadyPresentInArray = false;
            if (dicomServerArray != null) {
                for (int i = 0; i < dicomServerArray.size(); i++) {
                    if (dicomServerArray.get(i).getName().equalsIgnoreCase(ae.getServerName())) {
                        dicomServerDetailAlreadyPresentInArray = true;
                        dicomServerArray.get(i).setAe(ae);
                        dicomServerArray.get(i).setStudyListModel(studyListModel);
                    }
                }
            }
            if (!dicomServerDetailAlreadyPresentInArray) {
                DicomServerDelegate dsd = new DicomServerDelegate(ae.getServerName());
                dsd.setAe(ae);
                dsd.setStudyListModel(studyListModel);
                dicomServerArray.add(dsd);
            }
        } catch (Exception e) {
            System.out.println("Select a Server");
            e.printStackTrace();
        }
        startSearch = false;
}//GEN-LAST:event_queryButtonActionPerformed
    /**
     * This routine is the handler for retrieve button.
     * @param evt
     */
    private void retrieveButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_retrieveButtonActionPerformed
        String serverName = ((ServerTableModel) serverListTable.getModel()).getValueAt(serverListTable.getSelectedRow(), 0);
        String[] s = ApplicationContext.databaseRef.getListenerDetails();
        if (dicomServerArray != null) {
            for (int i = 0; i < dicomServerArray.size(); i++) {
                if (dicomServerArray.get(i).getName().equalsIgnoreCase(serverName)) {
                    int index[] = studyListTable.getSelectedRows();
                    for (int tempI = 0; tempI < index.length; tempI++) {
                        String tem[] = new String[]{
                            "dicom" + "://" + dicomServerArray.get(i).getAe().getAeTitle() + "@" + dicomServerArray.get(i).getAe().getHostName() + ":" + dicomServerArray.get(i).getAe().getPort(),
                            "--dest", s[0], "--pid", dicomServerArray.get(i).getStudyListModel().getValueAt(index[tempI], 0), "--suid",
                            dicomServerArray.get(i).getStudyListModel().getValueAt(index[tempI], 8)};
                        try {
                            if (!ApplicationContext.databaseRef.checkRecordExists("study", "StudyInstanceUID", dicomServerArray.get(i).getStudyListModel().getValueAt(index[tempI], 8))) {
                                MainScreen.sndRcvFrm.setVisible(true);
                                MoveDelegate moveDelegate = new MoveDelegate(tem);
                            } else {
                                MainScreen.sndRcvFrm.setVisible(true);
                            }
                        } catch (Exception ex) {
                            Logger.getLogger(MainScreen.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
            }
        }
    }//GEN-LAST:event_retrieveButtonActionPerformed

    /**
     * This routine used to set the server name
     */
    private void setServerName() {
        if (serverListTable.getSelectedRow() == -1) {
            serverNameLabel.setText(((ServerTableModel) serverListTable.getModel()).getValueAt(0, 0));
        } else {
            serverNameLabel.setText(((ServerTableModel) serverListTable.getModel()).getValueAt(serverListTable.getSelectedRow(), 0));
        }
    }
    private void verifyButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_verifyButtonActionPerformed
        try {
            String serverName = ((ServerTableModel) serverListTable.getModel()).getValueAt(serverListTable.getSelectedRow(), 0);
            AEModel ae = ApplicationContext.databaseRef.getServerDetail(serverName);
            DcmURL url = new DcmURL("dicom://" + ae.getAeTitle() + "@" + ae.getHostName() + ":" + ae.getPort());
            EchoService echo = new EchoService();
            EchoStatus echoStatus = new EchoStatus(this, true);
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
                System.out.println(e.getStackTrace());
            }
        } catch (Exception e) {
            System.out.println("Select a Server");
        }
    }//GEN-LAST:event_verifyButtonActionPerformed

    private void serverSelectionPerformed() {
        setServerName();
        StudyListModel studyList = new StudyListModel();
        studyListTable.setModel(studyList);
        if (serverListTable.getSelectedRow() > -1) {
            if (dicomServerArray != null) {
                for (int i = 0; i < dicomServerArray.size(); i++) {
                    if (dicomServerArray.get(i).getName().equalsIgnoreCase(((ServerTableModel) serverListTable.getModel()).getValueAt(serverListTable.getSelectedRow(), 0))) {
                        studyListTable.setModel(dicomServerArray.get(i).getStudyListModel());
                    }
                }
            }
        }
    }
    private void studyListTableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_studyListTableMouseClicked
        String serverName = ((ServerTableModel) serverListTable.getModel()).getValueAt(serverListTable.getSelectedRow(), 0);
        if (evt.getClickCount() == 2) {
            if (dicomServerArray != null) {
                for (int i = 0; i < dicomServerArray.size(); i++) {
                    if (dicomServerArray.get(i).getName().equalsIgnoreCase(serverName)) {

                        String tem[] = new String[]{
                            "dicom" + "://" + dicomServerArray.get(i).getAe().getAeTitle() + "@" + dicomServerArray.get(i).getAe().getHostName() + ":" + dicomServerArray.get(i).getAe().getPort(),
                            "--dest", "MAYAM", "--pid", dicomServerArray.get(i).getStudyListModel().getValueAt(studyListTable.getSelectedRow(), 0), "--suid",
                            dicomServerArray.get(i).getStudyListModel().getValueAt(studyListTable.getSelectedRow(), 8)};
                        try {
                            if (ApplicationContext.databaseRef.checkRecordExists("study", "StudyInstanceUID", dicomServerArray.get(i).getStudyListModel().getValueAt(studyListTable.getSelectedRow(), 8))) {
                                StudyAvailabilityStatus studyStatus = new StudyAvailabilityStatus(this, true);
                                Display.alignScreen(studyStatus);
                                studyStatus.setVisible(true);
                            } else {
                                MainScreen.sndRcvFrm.setVisible(true);
                                MoveScu.main(tem);
                            }
                        } catch (Exception ex) {
                            Logger.getLogger(MainScreen.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
            }
        }
    }//GEN-LAST:event_studyListTableMouseClicked

    private void drRadioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_drRadioActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_drRadioActionPerformed
    public void itemStateChanged(ItemEvent e) {
        if (e.getStateChange() == ItemEvent.SELECTED) {
            if (searchDaysGroup.getSelection() == ((JRadioButton) e.getItem()).getModel()) {
                if (((JRadioButton) e.getItem()).getActionCommand().equalsIgnoreCase("Between")) {
                    fromSpinner.setEnabled(true);
                    toSpinner.setEnabled(true);
                } else {
                    fromSpinner.setEnabled(false);
                    toSpinner.setEnabled(false);
                }
                queryParam.setSearchDays(((JRadioButton) e.getItem()).getActionCommand());
            } else {
                queryParam.setModality(((JRadioButton) e.getItem()).getActionCommand());
            }
        }
    }

    private void osSpecifics() {
        this.setSize(1030, 750);
        if (System.getProperty("os.name").startsWith("Mac")) {
            Border border = UIManager.getBorder("InsetBorder.aquaVariant");
            if (border == null) {
                border = new BevelBorder(1);
            }
            jPanel1.setBorder(border);
            jPanel2.setBorder(border);
            jPanel9.setBackground(new Color(216, 216, 216));
            jTabbedPane1.setBackground(new Color(216, 216, 216));
            jPanel1.setBackground(new Color(216, 216, 216));
            jPanel2.setBackground(new Color(216, 216, 216));
            jPanel3.setBackground(new Color(216, 216, 216));
            jPanel4.setBackground(new Color(216, 216, 216));
            jPanel5.setBackground(new Color(216, 216, 216));
            jPanel6.setBackground(new Color(216, 216, 216));
            jPanel7.setBackground(new Color(216, 216, 216));
            jPanel8.setBackground(new Color(216, 216, 216));
            serverlistScroll.setBackground(new Color(216, 216, 216));

        }
    }

    /**
     * This is implemented handler method for ServerChangeListener.
     */
    public void onServerChange() {
        setServerTableModel();
        setServerName();
    }

    public void valueChanged(ListSelectionEvent e) {
        serverSelectionPerformed();
    }

    public void setFromToDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
        Date d1 = (Date) fromSpinner.getModel().getValue();
        Date d2 = (Date) toSpinner.getModel().getValue();
        String from = sdf.format(d1);
        String to = sdf.format(d2);
        queryParam.setFrom(from);
        queryParam.setTo(to);
    }

    /**
     *This routine used to reset the from and to date.
     */
    public void resetFromAndToDate() {
        queryParam.setFrom(null);
        queryParam.setTo(null);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                new QueryRetrive().setVisible(true);
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField accessionNoText;
    private javax.swing.JRadioButton allRadio;
    private javax.swing.JRadioButton anydateRadio;
    private javax.swing.JRadioButton betweenRadio;
    private javax.swing.JSpinner birthDateSpinner;
    private javax.swing.JRadioButton crRadio;
    private javax.swing.JRadioButton ctRadio;
    private javax.swing.JRadioButton drRadio;
    private javax.swing.JRadioButton dxRadio;
    private javax.swing.JSpinner fromSpinner;
    private javax.swing.JLabel headerLabel;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JRadioButton lastmonthRadio;
    private javax.swing.JRadioButton lastweekRadio;
    private javax.swing.ButtonGroup modalityGroup;
    private javax.swing.JRadioButton mrRadio;
    private javax.swing.JRadioButton nmRadio;
    private javax.swing.JRadioButton otRadio;
    private javax.swing.JTextField patientIDText;
    private javax.swing.JTextField patientNameText;
    private javax.swing.JRadioButton pxRadio;
    private javax.swing.JButton queryButton;
    private javax.swing.JButton retrieveButton;
    private javax.swing.JRadioButton rfRadio;
    private javax.swing.JRadioButton scRadio;
    private javax.swing.ButtonGroup searchDaysGroup;
    private javax.swing.JTable serverListTable;
    private javax.swing.JLabel serverNameLabel;
    private javax.swing.JScrollPane serverlistScroll;
    private javax.swing.JTable studyListTable;
    private javax.swing.JSpinner toSpinner;
    private javax.swing.JRadioButton todayRadio;
    private javax.swing.JRadioButton usRadio;
    private javax.swing.JButton verifyButton;
    private javax.swing.JRadioButton xaRadio;
    private javax.swing.JRadioButton yesterdayRadio;
    // End of variables declaration//GEN-END:variables
    public ArrayList<DicomServerDelegate> dicomServerArray = new ArrayList<DicomServerDelegate>();
    private QueryParam queryParam = new QueryParam();
}
