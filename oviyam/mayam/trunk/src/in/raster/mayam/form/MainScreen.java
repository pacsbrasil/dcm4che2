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

import com.nilo.plaf.nimrod.NimRODLookAndFeel;
import com.sun.java.swing.plaf.motif.MotifLookAndFeel;
import in.raster.mayam.context.ApplicationContext;
import in.raster.mayam.delegate.ImportDcmDirDelegate;
import in.raster.mayam.delegate.InputArgumentsParser;
import in.raster.mayam.delegate.ReceiveDelegate;
import in.raster.mayam.delegate.SendingDelegate;
import in.raster.mayam.delegate.SeriesThumbUpdator;
import in.raster.mayam.delegate.ShowComparisonViewerDelegate;
import in.raster.mayam.delegate.ShowViewerDelegate;
import in.raster.mayam.delegate.StudyListUpdator;
import in.raster.mayam.delegate.WadoRetrieveDelegate;
import in.raster.mayam.form.dialog.About;
import in.raster.mayam.form.dialog.ConfirmDelete;
import in.raster.mayam.form.dialog.FileChooserDialog;
import in.raster.mayam.form.dialog.ExportLocationChooser;
import in.raster.mayam.form.dialog.ServerListDialog;
import in.raster.mayam.form.dialog.SettingsDialog;
import in.raster.mayam.util.DicomTags;
import in.raster.mayam.util.DicomTagsReader;
import in.raster.mayam.model.AEModel;
import in.raster.mayam.model.InputArgumentValues;
import in.raster.mayam.model.ServerModel;
import in.raster.mayam.model.Study;
import in.raster.mayam.model.table.StudyListModel;
import in.raster.mayam.model.table.renderer.CellRenderer;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.io.File;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.SwingUtilities;
import javax.swing.UIDefaults;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

/**
 *
 * @author  BabuHussain
 * @version 0.5
 *
 */
public class MainScreen extends javax.swing.JFrame {

    /** Creates new form MainScreen */
    private MainScreen() {
        initComponents();
        initAppDefaults();
    }

    private void initAppDefaults() {
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        ApplicationContext.mainScreen = this;
        checkLogFileExist();
        initDB();
        initQR();
        startListening();
        showLocalDBStorage();
        showThumbnails();
        initNetworkQueue();
        initSendingProgress();
        setTheme();
        ImportDcmDirDelegate.findAndLoadDcmDirFiles();
        loadStudiesBasedOnInputParameter();

    }

    private void setTheme() {
        systemLFmenu.setText(System.getProperty("os.name"));
        String activeTheme = ApplicationContext.databaseRef.getActiveTheme();
        if (activeTheme.equalsIgnoreCase("Nimrod")) {
            setNimrodTheme();
        } else if (activeTheme.equalsIgnoreCase("Motif")) {
            setMotifTheme();
        } else if (activeTheme.equalsIgnoreCase("System")) {
            setSystemTheme();
        }
    }

    public void loadStudiesBasedOnInputParameter() {
        InputArgumentValues inputArgumentValues = InputArgumentsParser.inputArgumentValues;
        if (inputArgumentValues.getAeTitle() != null && inputArgumentValues.getPort() != 0 && inputArgumentValues.getHostName() != null && inputArgumentValues.getWadoContext() != null && inputArgumentValues.getWadoPort() != 0 && inputArgumentValues.getWadoProtocol() != null) {
            ServerModel serverModel = new ServerModel();
            serverModel.setAeTitle(inputArgumentValues.getAeTitle());
            serverModel.setHostName(inputArgumentValues.getHostName());
            serverModel.setPort(inputArgumentValues.getPort());
            serverModel.setWadoContextPath(inputArgumentValues.getWadoContext());
            serverModel.setWadoPort(inputArgumentValues.getWadoPort());
            serverModel.setWadoProtocol(inputArgumentValues.getWadoProtocol());
            WadoRetrieveDelegate wadoRetrieveDelegate = new WadoRetrieveDelegate();
            wadoRetrieveDelegate.retrieveStudy(serverModel);
        }
    }

    /**
     * This routine used to initialize the sending progress
     */
    private void initSendingProgress() {
        SendingProgress sendingProgress = new SendingProgress();
        ApplicationContext.sendingProgress = sendingProgress;
    }

    /**
     * This routine used to initialize the query
     */
    private void initQR() {
        queryRetrieve = new QueryRetrieve();
        queryRetrieve.setLocationRelativeTo(this);
    }

    /**
     * This routine used to initialize the network queue
     */
    public void initNetworkQueue() {
        sndRcvFrm = new SendReceiveFrame();
        sndRcvFrm.setLocationRelativeTo(this);
        sndRcvFrm.setVisible(false);
    }

    /**
     * This routine used to initialize the database
     */
    public void initDB() {
        ApplicationContext.openOrCreateDB();
    }

    /**
     * This routine used to find out whether log file exist or not in the application
     */
    public void checkLogFileExist() {
        ApplicationContext.createLogFile();
    }

    /**
     * This routine used to start the receiver
     */
    private void startListening() {
        try {
            startReceiver();
            System.out.println("Start Server listening on port " + receiveDelegate.getPort());
        } catch (Exception e) {
            ApplicationContext.writeLog(e.toString());
            e.printStackTrace();
        }
    }

    /**
     * This routine used to start the receiver
     */
    public void startReceiver() throws Exception {
        receiveDelegate = new ReceiveDelegate();
        receiveDelegate.start();
    }

    /**
     * This routine used to stop the receiver
     */
    public void stopReceiver() {
        receiveDelegate.stop();
    }

    /**
     * This routine is used to restart the receiver
     */
    public void restartReceiver() {
        try {
            stopReceiver();
            System.out.println("Stop Server listening on port " + receiveDelegate.getPort());
            startReceiver();
            System.out.println("Start Server listening on port " + receiveDelegate.getPort());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * This routine is used to update the local database storage
     */
    public static void showLocalDBStorage() {
        refreshLocalDBStorage();
        if (studyListTable.getRowCount() > 0) {
            studyListTable.setRowSelectionInterval(0, 0);
        }
    }

    /**
     * This routine used to refresh the local database study list.
     */
    public synchronized static void refreshLocalDBStorage() {
        int i = 0;
        if (studyListTable.getRowCount() > 2) {
            i = studyListTable.getSelectedRow();
        }
        StudyListModel studyListModel = new StudyListModel();
        studyListModel.setData(ApplicationContext.databaseRef.listAllStudiesOfDB());
        studyListTable.setModel((TableModel) studyListModel);
        TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(studyListModel);
        studyListTable.setRowSorter(sorter);
        if (studyListTable.getRowCount() > 0) {
            if (i < studyListTable.getRowCount()) {
                studyListTable.setRowSelectionInterval(i, i);
            } else {
                studyListTable.setRowSelectionInterval(i - 1, i - 1);
            }
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        container = new javax.swing.JPanel();
        contentArea = new javax.swing.JPanel();
        localDatabaseLabel = new javax.swing.JLabel();
        jSplitPane1 = new javax.swing.JSplitPane();
        studyTableScroll = new javax.swing.JScrollPane();
        studyListTable = new javax.swing.JTable();
        studyAndSeriesDisplayPanel = new javax.swing.JPanel();
        windowingPanelCanvas = new javax.swing.JPanel();
        thumbnailScroll = new javax.swing.JScrollPane();
        thumbnailDisplay = new javax.swing.JPanel();
        seriesLabel = new javax.swing.JLabel();
        headerPanel = new javax.swing.JPanel();
        importButton = new javax.swing.JButton();
        exportButton = new javax.swing.JButton();
        cdImportButton = new javax.swing.JButton();
        deleteButton = new javax.swing.JButton();
        metaDataButton = new javax.swing.JButton();
        sendButton = new javax.swing.JButton();
        queryRetrieveButton = new javax.swing.JButton();
        viewerButton = new javax.swing.JButton();
        queueButton = new javax.swing.JButton();
        menuBar = new javax.swing.JMenuBar();
        fileMenu = new javax.swing.JMenu();
        importMenuItem = new javax.swing.JMenuItem();
        exportMenuItem = new javax.swing.JMenuItem();
        jSeparator2 = new javax.swing.JSeparator();
        deleteExamMenuItem = new javax.swing.JMenuItem();
        anonymizeMenuItem = new javax.swing.JMenuItem();
        jSeparator3 = new javax.swing.JSeparator();
        resetMenuItem = new javax.swing.JMenuItem();
        jSeparator4 = new javax.swing.JSeparator();
        exitMenuItem = new javax.swing.JMenuItem();
        toolsMenu = new javax.swing.JMenu();
        preferenceMenuItem = new javax.swing.JMenuItem();
        networkMenu = new javax.swing.JMenu();
        queueMenuItem = new javax.swing.JMenuItem();
        sendMenuItem = new javax.swing.JMenuItem();
        QRMenuItem1 = new javax.swing.JMenuItem();
        themeMenu = new javax.swing.JMenu();
        nimrodLFMenu = new javax.swing.JMenuItem();
        motifLFMenu = new javax.swing.JMenuItem();
        systemLFmenu = new javax.swing.JMenuItem();
        helpMenu = new javax.swing.JMenu();
        userManualItem = new javax.swing.JMenuItem();
        aboutMenuItem = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Mayam");
        setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/in/raster/mayam/form/images/fav_mayam.png")));

        localDatabaseLabel.setFont(new java.awt.Font("Lucida Grande", 1, 14));
        localDatabaseLabel.setText(" Local Database");

        jSplitPane1.setDividerLocation(256);
        jSplitPane1.setDividerSize(4);
        jSplitPane1.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);

        studyListTable.setForeground(new java.awt.Color(252, 138, 0));
        studyListTable.setModel(new StudyListModel());
        studyListTable.setShowGrid(true);
        studyListTable.setDefaultRenderer(Object.class, new CellRenderer());
        studyListTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                studyListTableMouseClicked(evt);
            }
        });
        studyListTable.getTableHeader().setPreferredSize(new Dimension(this.getWidth(), 25));
        studyListTable.getTableHeader().setFont(new Font("Lucida Grande",Font.BOLD,12));
        studyListTable.setRowHeight(25);
        studyListTable.getTableHeader().setForeground(new Color(255,138,0));
        studyListTable.getTableHeader().setBackground(new Color(0,0,0));
        studyTableScroll.setViewportView(studyListTable);

        jSplitPane1.setTopComponent(studyTableScroll);

        windowingPanelCanvas.setBackground(new java.awt.Color(0, 0, 0));
        windowingPanelCanvas.setAutoscrolls(true);
        windowingPanelCanvas.setPreferredSize(new java.awt.Dimension(50, 50));

        org.jdesktop.layout.GroupLayout windowingPanelCanvasLayout = new org.jdesktop.layout.GroupLayout(windowingPanelCanvas);
        windowingPanelCanvas.setLayout(windowingPanelCanvasLayout);
        windowingPanelCanvasLayout.setHorizontalGroup(
            windowingPanelCanvasLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 641, Short.MAX_VALUE)
        );
        windowingPanelCanvasLayout.setVerticalGroup(
            windowingPanelCanvasLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 520, Short.MAX_VALUE)
        );

        thumbnailScroll.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        thumbnailScroll.getVerticalScrollBar().setUnitIncrement(24);

        thumbnailDisplay.setAutoscrolls(true);
        thumbnailDisplay.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        thumbnailDisplay.setMaximumSize(new java.awt.Dimension(1000, 1000));
        thumbnailDisplay.setMinimumSize(new java.awt.Dimension(13, 2));
        thumbnailScroll.setViewportView(thumbnailDisplay);

        seriesLabel.setBackground(new java.awt.Color(0, 0, 0));
        seriesLabel.setFont(new java.awt.Font("Lucida Grande", 1, 14));
        seriesLabel.setForeground(new java.awt.Color(255, 138, 0));
        seriesLabel.setText("Series ");
        seriesLabel.setOpaque(true);

        org.jdesktop.layout.GroupLayout studyAndSeriesDisplayPanelLayout = new org.jdesktop.layout.GroupLayout(studyAndSeriesDisplayPanel);
        studyAndSeriesDisplayPanel.setLayout(studyAndSeriesDisplayPanelLayout);
        studyAndSeriesDisplayPanelLayout.setHorizontalGroup(
            studyAndSeriesDisplayPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(studyAndSeriesDisplayPanelLayout.createSequentialGroup()
                .add(studyAndSeriesDisplayPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                    .add(thumbnailScroll, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 330, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, seriesLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 330, Short.MAX_VALUE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(windowingPanelCanvas, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 641, Short.MAX_VALUE))
        );
        studyAndSeriesDisplayPanelLayout.setVerticalGroup(
            studyAndSeriesDisplayPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(studyAndSeriesDisplayPanelLayout.createSequentialGroup()
                .add(seriesLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 20, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(thumbnailScroll, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 492, Short.MAX_VALUE))
            .add(windowingPanelCanvas, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 520, Short.MAX_VALUE)
        );

        jSplitPane1.setRightComponent(studyAndSeriesDisplayPanel);

        org.jdesktop.layout.GroupLayout contentAreaLayout = new org.jdesktop.layout.GroupLayout(contentArea);
        contentArea.setLayout(contentAreaLayout);
        contentAreaLayout.setHorizontalGroup(
            contentAreaLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(contentAreaLayout.createSequentialGroup()
                .addContainerGap()
                .add(contentAreaLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jSplitPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 985, Short.MAX_VALUE)
                    .add(localDatabaseLabel))
                .addContainerGap())
        );
        contentAreaLayout.setVerticalGroup(
            contentAreaLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(contentAreaLayout.createSequentialGroup()
                .add(localDatabaseLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jSplitPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 782, Short.MAX_VALUE))
        );

        headerPanel.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, new java.awt.Color(13, 13, 13)));

        importButton.setFont(new java.awt.Font("Lucida Grande", 1, 12)); // NOI18N
        importButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/in/raster/mayam/form/images/import.png"))); // NOI18N
        importButton.setText("Import");
        importButton.setToolTipText("");
        importButton.setBorderPainted(false);
        importButton.setContentAreaFilled(false);
        importButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        importButton.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/in/raster/mayam/form/images/import1.png"))); // NOI18N
        importButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        importButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                importButtonActionPerformed(evt);
            }
        });

        exportButton.setFont(new java.awt.Font("Lucida Grande", 1, 12)); // NOI18N
        exportButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/in/raster/mayam/form/images/export_study.png"))); // NOI18N
        exportButton.setText("Export");
        exportButton.setToolTipText("");
        exportButton.setBorderPainted(false);
        exportButton.setContentAreaFilled(false);
        exportButton.setDefaultCapable(false);
        exportButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        exportButton.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/in/raster/mayam/form/images/export_study1.png"))); // NOI18N
        exportButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        exportButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportButtonActionPerformed(evt);
            }
        });

        cdImportButton.setFont(new java.awt.Font("Lucida Grande", 1, 12)); // NOI18N
        cdImportButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/in/raster/mayam/form/images/cd_import.png"))); // NOI18N
        cdImportButton.setText("CD-Rom");
        cdImportButton.setToolTipText("");
        cdImportButton.setBorderPainted(false);
        cdImportButton.setContentAreaFilled(false);
        cdImportButton.setDefaultCapable(false);
        cdImportButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        cdImportButton.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/in/raster/mayam/form/images/cd_import1.png"))); // NOI18N
        cdImportButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        cdImportButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cdImportButtonActionPerformed(evt);
            }
        });

        deleteButton.setFont(new java.awt.Font("Lucida Grande", 1, 12)); // NOI18N
        deleteButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/in/raster/mayam/form/images/delete_study.png"))); // NOI18N
        deleteButton.setText("Delete");
        deleteButton.setToolTipText("");
        deleteButton.setBorderPainted(false);
        deleteButton.setContentAreaFilled(false);
        deleteButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        deleteButton.setPreferredSize(new java.awt.Dimension(52, 50));
        deleteButton.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/in/raster/mayam/form/images/delete_study1.png"))); // NOI18N
        deleteButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        deleteButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteButtonActionPerformed(evt);
            }
        });

        metaDataButton.setFont(new java.awt.Font("Lucida Grande", 1, 12)); // NOI18N
        metaDataButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/in/raster/mayam/form/images/metadata_mainpage.png"))); // NOI18N
        metaDataButton.setText("Meta Data");
        metaDataButton.setToolTipText("");
        metaDataButton.setBorderPainted(false);
        metaDataButton.setContentAreaFilled(false);
        metaDataButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        metaDataButton.setPreferredSize(new java.awt.Dimension(52, 50));
        metaDataButton.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/in/raster/mayam/form/images/metadata_mainpage1.png"))); // NOI18N
        metaDataButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        metaDataButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                metaDataButtonActionPerformed(evt);
            }
        });

        sendButton.setFont(new java.awt.Font("Lucida Grande", 1, 12)); // NOI18N
        sendButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/in/raster/mayam/form/images/send.png"))); // NOI18N
        sendButton.setText("Send");
        sendButton.setToolTipText("");
        sendButton.setBorderPainted(false);
        sendButton.setContentAreaFilled(false);
        sendButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        sendButton.setPreferredSize(new java.awt.Dimension(52, 50));
        sendButton.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/in/raster/mayam/form/images/send1.png"))); // NOI18N
        sendButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        sendButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sendButtonActionPerformed(evt);
            }
        });

        queryRetrieveButton.setFont(new java.awt.Font("Lucida Grande", 1, 12)); // NOI18N
        queryRetrieveButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/in/raster/mayam/form/images/query.png"))); // NOI18N
        queryRetrieveButton.setText("Query");
        queryRetrieveButton.setToolTipText("");
        queryRetrieveButton.setBorderPainted(false);
        queryRetrieveButton.setContentAreaFilled(false);
        queryRetrieveButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        queryRetrieveButton.setPreferredSize(new java.awt.Dimension(52, 50));
        queryRetrieveButton.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/in/raster/mayam/form/images/query1.png"))); // NOI18N
        queryRetrieveButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        queryRetrieveButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                queryRetrieveButtonActionPerformed(evt);
            }
        });

        viewerButton.setFont(new java.awt.Font("Lucida Grande", 1, 12)); // NOI18N
        viewerButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/in/raster/mayam/form/images/viewer.png"))); // NOI18N
        viewerButton.setText("Viewer");
        viewerButton.setToolTipText("");
        viewerButton.setBorderPainted(false);
        viewerButton.setContentAreaFilled(false);
        viewerButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        viewerButton.setPreferredSize(new java.awt.Dimension(52, 50));
        viewerButton.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/in/raster/mayam/form/images/viewer1.png"))); // NOI18N
        viewerButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        viewerButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                viewerButtonActionPerformed(evt);
            }
        });

        queueButton.setFont(new java.awt.Font("Lucida Grande", 1, 12)); // NOI18N
        queueButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/in/raster/mayam/form/images/queue.png"))); // NOI18N
        queueButton.setText("Queue");
        queueButton.setToolTipText("");
        queueButton.setBorderPainted(false);
        queueButton.setContentAreaFilled(false);
        queueButton.setFocusPainted(false);
        queueButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        queueButton.setPreferredSize(new java.awt.Dimension(52, 50));
        queueButton.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/in/raster/mayam/form/images/queue1.png"))); // NOI18N
        queueButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        queueButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                queueButtonActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout headerPanelLayout = new org.jdesktop.layout.GroupLayout(headerPanel);
        headerPanel.setLayout(headerPanelLayout);
        headerPanelLayout.setHorizontalGroup(
            headerPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(headerPanelLayout.createSequentialGroup()
                .add(5, 5, 5)
                .add(importButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 79, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(exportButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 79, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(cdImportButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 85, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(deleteButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 78, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(metaDataButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 114, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(sendButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 69, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(queryRetrieveButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 78, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(viewerButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 79, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(queueButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 78, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(277, 277, 277))
        );
        headerPanelLayout.setVerticalGroup(
            headerPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(headerPanelLayout.createSequentialGroup()
                .add(5, 5, 5)
                .add(headerPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(metaDataButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(exportButton)
                    .add(importButton)
                    .add(deleteButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(sendButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(queryRetrieveButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(viewerButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(queueButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(cdImportButton))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        headerPanelLayout.linkSize(new java.awt.Component[] {cdImportButton, deleteButton, exportButton, importButton, metaDataButton, queryRetrieveButton, queueButton, sendButton, viewerButton}, org.jdesktop.layout.GroupLayout.VERTICAL);

        org.jdesktop.layout.GroupLayout containerLayout = new org.jdesktop.layout.GroupLayout(container);
        container.setLayout(containerLayout);
        containerLayout.setHorizontalGroup(
            containerLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(contentArea, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .add(headerPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 1025, Short.MAX_VALUE)
        );
        containerLayout.setVerticalGroup(
            containerLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(containerLayout.createSequentialGroup()
                .add(headerPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(contentArea, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        fileMenu.setText("File");

        importMenuItem.setText("Import");
        importMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                importMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(importMenuItem);

        exportMenuItem.setText("Export");
        exportMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(exportMenuItem);
        fileMenu.add(jSeparator2);

        deleteExamMenuItem.setText("Delete Selected Exam");
        deleteExamMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteExamMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(deleteExamMenuItem);

        anonymizeMenuItem.setText("Anonymize");
        anonymizeMenuItem.setEnabled(false);
        fileMenu.add(anonymizeMenuItem);
        fileMenu.add(jSeparator3);

        resetMenuItem.setText("Reset DB");
        resetMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resetMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(resetMenuItem);
        fileMenu.add(jSeparator4);

        exitMenuItem.setText("Exit");
        exitMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exitMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(exitMenuItem);

        menuBar.add(fileMenu);

        toolsMenu.setText("Tools");

        preferenceMenuItem.setText("Preferences");
        preferenceMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                preferenceMenuItemActionPerformed(evt);
            }
        });
        toolsMenu.add(preferenceMenuItem);

        menuBar.add(toolsMenu);

        networkMenu.setText("Network");

        queueMenuItem.setText("Queue");
        queueMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                queueMenuItemActionPerformed(evt);
            }
        });
        networkMenu.add(queueMenuItem);

        sendMenuItem.setText("Send");
        sendMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sendMenuItemActionPerformed(evt);
            }
        });
        networkMenu.add(sendMenuItem);

        QRMenuItem1.setText("Query/Retrieve");
        QRMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                QRMenuItem1ActionPerformed(evt);
            }
        });
        networkMenu.add(QRMenuItem1);

        menuBar.add(networkMenu);

        themeMenu.setText("Theme");

        nimrodLFMenu.setText("Nimrod");
        nimrodLFMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nimrodLFMenuActionPerformed(evt);
            }
        });
        themeMenu.add(nimrodLFMenu);

        motifLFMenu.setText("Motif");
        motifLFMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                motifLFMenuActionPerformed(evt);
            }
        });
        themeMenu.add(motifLFMenu);

        systemLFmenu.setText("System L&F");
        systemLFmenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                systemLFmenuActionPerformed(evt);
            }
        });
        themeMenu.add(systemLFmenu);

        menuBar.add(themeMenu);

        helpMenu.setText("Help");

        userManualItem.setText("User Manual");
        helpMenu.add(userManualItem);

        aboutMenuItem.setText("About");
        aboutMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                aboutMenuItemActionPerformed(evt);
            }
        });
        helpMenu.add(aboutMenuItem);

        menuBar.add(helpMenu);

        setJMenuBar(menuBar);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(container, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(container, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void importButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_importButtonActionPerformed
        importHandler();
    }//GEN-LAST:event_importButtonActionPerformed
    private void importHandler() {
        FileChooserDialog fcd = new FileChooserDialog(this, true);
        fcd.setLocationRelativeTo(this);
        fcd.setVisible(true);
    }
    private void queryRetrieveButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_queryRetrieveButtonActionPerformed
        queryRetrieve.setVisible(true);
    }//GEN-LAST:event_queryRetrieveButtonActionPerformed

    private void exportButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportButtonActionPerformed
        exportHandler();
    }//GEN-LAST:event_exportButtonActionPerformed
    private void exportHandler() {
        if (studyListTable.getSelectedRow() != -1) {
            int selection = studyListTable.convertRowIndexToModel(studyListTable.getSelectedRow());
            String siuid = ((StudyListModel) studyListTable.getModel()).getValueAt(selection, 8);
            ExportLocationChooser jpegChooser = new ExportLocationChooser(ApplicationContext.imgView, true);
            jpegChooser.setLocationRelativeTo(this);
            jpegChooser.setSeriesOrInstanceLevel(false);
            jpegChooser.setStudyUID(siuid);
            jpegChooser.setVisible(true);
        }
    }
    private void deleteButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteButtonActionPerformed
        if (studyListTable.getSelectedRow() != -1) {
            ConfirmDelete confirmDelete = new ConfirmDelete(this, true);
            confirmDelete.setLocationRelativeTo(this);
            confirmDelete.setVisible(true);
        }
        removeThumbnailComponents();
        removeWindowingImage();
        showThumbnails();
    }//GEN-LAST:event_deleteButtonActionPerformed
    private void sendButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sendButtonActionPerformed
        doSend();
    }//GEN-LAST:event_sendButtonActionPerformed
    private void viewerButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_viewerButtonActionPerformed
        if (studyListTable.getSelectedRow() != -1) {

            int index[] = studyListTable.getSelectedRows();
            if (index.length > 1) {
                String[] studies = new String[index.length];
                for (int j = 0; j < index.length; j++) {
                    index[j] = studyListTable.convertRowIndexToModel(index[j]);
                }
                for (int tempI = 0; tempI < index.length; tempI++) {
                    studies[tempI] = ((StudyListModel) studyListTable.getModel()).getValueAt(index[tempI], 8);
                }
                ArrayList tempRef1 = ApplicationContext.databaseRef.getFirstInstanceListFromMultipleStudies(studies);

                openComparisonView(index.length, studies, tempRef1);
                for (String studyUID : studies) {
                    StudyListUpdator studyListUpdator = new StudyListUpdator();
                    studyListUpdator.addStudyToStudyList(studyUID, studyList, ((File) tempRef1.get(0)).getAbsolutePath());
                }

            } else {


                int selection = studyListTable.convertRowIndexToModel(studyListTable.getSelectedRow());
                String siuid = ((StudyListModel) studyListTable.getModel()).getValueAt(selection, 8);
                int rowColumnArray[] = new int[2];
                try {
                    rowColumnArray = ApplicationContext.databaseRef.getRowColumnBasedStudyUID(siuid);
                } catch (Exception e) {
                    rowColumnArray[0] = 1;
                    rowColumnArray[1] = 1;
                }
                ArrayList tempRef = ApplicationContext.databaseRef.getUrlBasedOnStudyIUID(siuid);
                openImageView(siuid, tempRef, rowColumnArray[0], rowColumnArray[1]);
                StudyListUpdator studyListUpdator = new StudyListUpdator();
                studyListUpdator.addStudyToStudyList(siuid, studyList, ((File) tempRef.get(0)).getAbsolutePath());
            }
        }
    }//GEN-LAST:event_viewerButtonActionPerformed
    /**
     * This routine used to process the exit menu
     * @param evt
     */
    private void exitMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exitMenuItemActionPerformed
        System.exit(2);
    }//GEN-LAST:event_exitMenuItemActionPerformed
    private void queueButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_queueButtonActionPerformed
        sndRcvFrm.setLocationRelativeTo(this);
        sndRcvFrm.setVisible(true);
    }//GEN-LAST:event_queueButtonActionPerformed
    private void preferenceMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_preferenceMenuItemActionPerformed
        showPreference();
    }//GEN-LAST:event_preferenceMenuItemActionPerformed
    /**
     * This routine used to show the preference dialogue
     */
    private void showPreference() {
        settingsDialog = new SettingsDialog(this, true);
        settingsDialog.setLocationRelativeTo(this);
        settingsDialog.setVisible(true);
    }

    private void doSend() {
        String forwardAET = "";
        String forwardHost = "";
        int forwardPort;
        ServerListDialog configuredServer = new ServerListDialog(this, true);
        configuredServer.setLocationRelativeTo(this);
        configuredServer.setVisible(true);
        if (configuredServer.getAe() != null) {
            AEModel ae = configuredServer.getAe();
            forwardAET = ae.getAeTitle();
            forwardHost = ae.getHostName();
            forwardPort = ae.getPort();
            if (studyListTable.getSelectedRow() != -1) {
                int index[] = studyListTable.getSelectedRows();
                for (int j = 0; j < index.length; j++) {
                    index[j] = studyListTable.convertRowIndexToModel(index[j]);
                }
                for (int tempI = 0; tempI < index.length; tempI++) {
                    String studyIUID = (String) studyListTable.getModel().getValueAt(index[tempI], 8);
                    SendingDelegate sendingDelegate = new SendingDelegate(studyIUID, ae);
                }
            }
        }
    }

    public SettingsDialog getPreference() {
        return settingsDialog;
    }

    /**
     * This routine used to get the query screen
     * @return
     */
    public QueryRetrieve getQueryScreen() {
        return this.queryRetrieve;
    }

    /**
     * This routine is used to process the mouse event for the study list table
     * @param evt
     */
    private void studyListTableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_studyListTableMouseClicked
        if (evt.getClickCount() == 2) {
            if (studyListTable.getSelectedRow() != -1) {
                int selection = studyListTable.convertRowIndexToModel(studyListTable.getSelectedRow());
                String siuid = ((StudyListModel) studyListTable.getModel()).getValueAt(selection, 8);
                int rowColumnArray[] = ApplicationContext.databaseRef.getRowColumnBasedStudyUID(siuid);
                ArrayList tempRef = ApplicationContext.databaseRef.getUrlBasedOnStudyIUID(siuid);
                openImageView(siuid, tempRef, rowColumnArray[0], rowColumnArray[1]);
                StudyListUpdator studyListUpdator = new StudyListUpdator();
                studyListUpdator.addStudyToStudyList(siuid, studyList, ((File) tempRef.get(0)).getAbsolutePath());
            }
        } else {
            showThumbnails();
        }
    }//GEN-LAST:event_studyListTableMouseClicked
    private void metaDataButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_metaDataButtonActionPerformed
        try {
            if (studyListTable.getSelectedRow() != -1) {
                int selection = studyListTable.convertRowIndexToModel(studyListTable.getSelectedRow());
                String siuid = ((StudyListModel) studyListTable.getModel()).getValueAt(selection, 8);
                ArrayList<DicomTags> dcmTags = DicomTagsReader.getTags(new File(this.canvas.getFilePath()));
                dicomTagsViewer.setDataModelOnTable(dcmTags);
                dicomTagsViewer.setLocationRelativeTo(this);
                dicomTagsViewer.setVisible(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }//GEN-LAST:event_metaDataButtonActionPerformed
    private void aboutMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_aboutMenuItemActionPerformed
        About about = new About(null, true);
        about.setLocationRelativeTo(this);
        about.setVisible(true);
    }//GEN-LAST:event_aboutMenuItemActionPerformed
    private void importMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_importMenuItemActionPerformed
        importHandler();
    }//GEN-LAST:event_importMenuItemActionPerformed
    private void exportMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportMenuItemActionPerformed
        exportHandler();
    }//GEN-LAST:event_exportMenuItemActionPerformed
    private void queueMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_queueMenuItemActionPerformed
        sndRcvFrm.setLocationRelativeTo(this);
        sndRcvFrm.setVisible(true);
    }//GEN-LAST:event_queueMenuItemActionPerformed

    private void resetMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resetMenuItemActionPerformed
        int resetDB = 0;
        resetDB = JOptionPane.showConfirmDialog(this, "Are you sure want to reset the database", "Confirmation Dialog", JOptionPane.YES_NO_OPTION);
        if (resetDB == 0) {
            ApplicationContext.databaseRef.rebuild();
            MainScreen.showLocalDBStorage();
            removeThumbnailComponents();
            removeWindowingImage();
        }
    }//GEN-LAST:event_resetMenuItemActionPerformed
    private void deleteExamMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteExamMenuItemActionPerformed
        if (studyListTable.getSelectedRow() != -1) {
            ConfirmDelete confirmDelete = new ConfirmDelete(this, true);
            confirmDelete.setLocationRelativeTo(this);
            confirmDelete.setVisible(true);
        }
        removeThumbnailComponents();
        removeWindowingImage();
        showThumbnails();
    }//GEN-LAST:event_deleteExamMenuItemActionPerformed
    /**
     * This routine used to remove the windowing image panel
     */
    private void removeWindowingImage() {
        for (int i = windowingPanelCanvas.getComponentCount() - 1; i >= 0; i--) {
            windowingPanelCanvas.remove(i);
        }
        windowingPanelCanvas.repaint();
    }

    /***
     * This is cd import action handler used to import the dicom files.
     * @param evt
     */
    private void cdImportButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cdImportButtonActionPerformed
        ImportDcmDirDelegate importDcmDirDelegate = new ImportDcmDirDelegate();
        importDcmDirDelegate.setCopyAsLink(true);
        importDcmDirDelegate.findAndRun();
    }//GEN-LAST:event_cdImportButtonActionPerformed

    private void systemLFmenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_systemLFmenuActionPerformed
        setSystemTheme();
        updateThemeStatus("System");
    }//GEN-LAST:event_systemLFmenuActionPerformed

    private void motifLFMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_motifLFMenuActionPerformed
        setMotifTheme();
        updateThemeStatus("Motif");
    }//GEN-LAST:event_motifLFMenuActionPerformed

    private void nimrodLFMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nimrodLFMenuActionPerformed
        setNimrodTheme();
        updateThemeStatus("Nimrod");

    }//GEN-LAST:event_nimrodLFMenuActionPerformed

    private void sendMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sendMenuItemActionPerformed
        doSend();
    }//GEN-LAST:event_sendMenuItemActionPerformed

    private void QRMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_QRMenuItem1ActionPerformed
        queryRetrieve.setVisible(true);
    }//GEN-LAST:event_QRMenuItem1ActionPerformed

    private void setNimrodTheme() {
        try {
            UIManager.setLookAndFeel(new NimRODLookAndFeel());
            UIDefaults uIDefaults = UIManager.getDefaults();
            uIDefaults.put("Menu.font", new Font("Lucida Grande", Font.BOLD, 12));
            uIDefaults.put("MenuItem.font", new Font("Lucida Grande", Font.BOLD, 12));
            uIDefaults.put("Menu.font", new Font("Lucida Grande", Font.BOLD, 12));
            uIDefaults.put("MenuItem.font", new Font("Lucida Grande", Font.BOLD, 12));
            uIDefaults.put("Button.font", new Font("Lucida Grande", Font.BOLD, 12));
            uIDefaults.put("Label.font", new Font("Lucida Grande", Font.BOLD, 12));
            uIDefaults.put("RadioButton.font", new Font("Lucida Grande", Font.BOLD, 12));
            uIDefaults.put("CheckBox.font", new Font("Lucida Grande", Font.BOLD, 12));

            updateComponentsTreeUI();
        } catch (UnsupportedLookAndFeelException ex) {
            Logger.getLogger(MainScreen.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void setMotifTheme() {
        try {
            UIManager.setLookAndFeel(new MotifLookAndFeel());
            updateComponentsTreeUI();
        } catch (UnsupportedLookAndFeelException ex) {
            Logger.getLogger(MainScreen.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void setSystemTheme() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            updateComponentsTreeUI();
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(MainScreen.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            Logger.getLogger(MainScreen.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(MainScreen.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnsupportedLookAndFeelException ex) {
            Logger.getLogger(MainScreen.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void updateComponentsTreeUI() {
        SwingUtilities.updateComponentTreeUI(this);
        SwingUtilities.updateComponentTreeUI(queryRetrieve);
        if (ApplicationContext.imageViewExist()) {
            SwingUtilities.updateComponentTreeUI(ApplicationContext.imgView);
        }
        SwingUtilities.updateComponentTreeUI(sndRcvFrm);
        SwingUtilities.updateComponentTreeUI(dicomTagsViewer);
    }

    private void updateThemeStatus(String themeName) {
        ApplicationContext.databaseRef.updateThemeStatus(themeName);
    }
    SeriesThumbUpdator thumbUpdator;

    public void showThumbnails() {
        try {
            this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            if (studyListTable.getSelectedRow() != -1) {
                if (thumbUpdator != null) {
                    thumbUpdator.setCanRun(false);
                    removeThumbnailComponents();
                }
                int selection = studyListTable.convertRowIndexToModel(studyListTable.getSelectedRow());
                String studyUID = ((StudyListModel) studyListTable.getModel()).getValueAt(selection, 8);
                selectedStudy = studyUID;
                thumbUpdator = new SeriesThumbUpdator(studyUID);
            }
        } finally {
            this.setCursor(Cursor.getDefaultCursor());
        }
    }

    public void openImageView(String studyUID, ArrayList tempRef, int gridRowCount, int gridColCount) {
        if (!ApplicationContext.imageViewExist()) {
            ApplicationContext.createImageView();
        }
        ShowViewerDelegate showViewer = new ShowViewerDelegate(studyUID, tempRef, gridRowCount, gridColCount);
    }

    public void openComparisonView(int numberOfStudies, String[] studies, ArrayList tempRef) {
        if (!ApplicationContext.imageViewExist()) {
            ApplicationContext.createImageView();
        }
        ShowComparisonViewerDelegate showComparisonViewerDelegate = new ShowComparisonViewerDelegate(numberOfStudies, studies, tempRef);
    }

    private void removeThumbnailComponents() {
        for (int i = thumbnailDisplay.getComponentCount() - 1; i >= 0; i--) {
            SeriesPanel thumbImage = (SeriesPanel) thumbnailDisplay.getComponent(i);
            thumbImage.clearAllMemoryReference();
            thumbnailDisplay.remove(thumbImage);
            thumbImage = null;
        }
        thumbnailDisplay.repaint();
    }

    /**
     * Getter method for thumbnailDisplay.
     * @return
     */
    public JPanel getThumbnailDisplay() {
        return thumbnailDisplay;
    }

    public JScrollPane getThumbnailScroll() {
        return this.thumbnailScroll;
    }

    /**
     * Getter method for windowingPanelCanvas.
     * @return
     */
    public JPanel getWindowingPanelCanvas() {
        return windowingPanelCanvas;
    }

    /**
     * Setter method for the instance of WindowingLayeredCanvas.
     * @param canvas
     */
    public void setCanvas(WindowingLayeredCanvas canvas) {
        this.canvas = canvas;
    }

    /**
     * Setter method for the instance of WindowingLayeredCanvas.
     * @param canvas
     */
    public WindowingLayeredCanvas getCanvas() {
        return canvas;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                new MainScreen().setVisible(true);
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem QRMenuItem1;
    private javax.swing.JMenuItem aboutMenuItem;
    private javax.swing.JMenuItem anonymizeMenuItem;
    private javax.swing.JButton cdImportButton;
    private javax.swing.JPanel container;
    private javax.swing.JPanel contentArea;
    private javax.swing.JButton deleteButton;
    private javax.swing.JMenuItem deleteExamMenuItem;
    private javax.swing.JMenuItem exitMenuItem;
    private javax.swing.JButton exportButton;
    private javax.swing.JMenuItem exportMenuItem;
    private javax.swing.JMenu fileMenu;
    private javax.swing.JPanel headerPanel;
    private javax.swing.JMenu helpMenu;
    private javax.swing.JButton importButton;
    private javax.swing.JMenuItem importMenuItem;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JSeparator jSeparator4;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JLabel localDatabaseLabel;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JButton metaDataButton;
    private javax.swing.JMenuItem motifLFMenu;
    private javax.swing.JMenu networkMenu;
    private javax.swing.JMenuItem nimrodLFMenu;
    private javax.swing.JMenuItem preferenceMenuItem;
    private javax.swing.JButton queryRetrieveButton;
    private javax.swing.JButton queueButton;
    private javax.swing.JMenuItem queueMenuItem;
    private javax.swing.JMenuItem resetMenuItem;
    private javax.swing.JButton sendButton;
    private javax.swing.JMenuItem sendMenuItem;
    private javax.swing.JLabel seriesLabel;
    private javax.swing.JPanel studyAndSeriesDisplayPanel;
    public static javax.swing.JTable studyListTable;
    private javax.swing.JScrollPane studyTableScroll;
    private javax.swing.JMenuItem systemLFmenu;
    private javax.swing.JMenu themeMenu;
    private javax.swing.JPanel thumbnailDisplay;
    private javax.swing.JScrollPane thumbnailScroll;
    private javax.swing.JMenu toolsMenu;
    private javax.swing.JMenuItem userManualItem;
    private javax.swing.JButton viewerButton;
    private javax.swing.JPanel windowingPanelCanvas;
    // End of variables declaration//GEN-END:variables
    private ReceiveDelegate receiveDelegate = null;
    public static ArrayList<Study> studyList = new ArrayList<Study>();
    private QueryRetrieve queryRetrieve = null;
    public static SendReceiveFrame sndRcvFrm;
    private WindowingLayeredCanvas canvas = null;
    public static MainScreen mainScreenObj;
    public static DicomTagsViewer dicomTagsViewer = new DicomTagsViewer();
    public static String selectedStudy = "";
    public static String selectedSeries = "";
    public SettingsDialog settingsDialog = null;

    public static MainScreen getInstance() {
        if (mainScreenObj == null) {
            mainScreenObj = new MainScreen();
        }
        return mainScreenObj;
    }
}
