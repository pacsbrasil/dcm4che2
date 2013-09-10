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
 * Guru Rajan R
 * Meer Asgar Hussain B
 * Prakash J
 * Sathish Kumar V
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
package in.raster.mayam.form.dcm3d;

import java.io.File;
import java.awt.Color;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.JColorChooser;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

import vtk.vtkCamera;
import vtk.vtkCellPicker;
import vtk.vtkDICOMImageReader;
import vtk.vtkActor;
import vtk.vtkGlobalJavaHash;
import vtk.vtkLight;
import vtk.vtkLightCollection;
import vtk.vtkMarchingCubes;
import vtk.vtkOutlineFilter;
import vtk.vtkPanel;
import vtk.vtkPolyDataMapper;

/**
 * SurfaceRendering.java
 *
 * Created on 1 Jul, 2010, 1:06:56 PM
 *
 *  This File Load the Series of Dicom images and
 *  Construct them as a Surfaces like Skin, Bone, and Metal according to the User Inputs...
 *
 */

/*
 * @author Sathish Kumar V              (sathishkumar.v@raster.in)
 */
public class SurfaceRendering extends javax.swing.JFrame {

    private vtkDICOMImageReader reader;
    private vtkPanel vpanSurface = new vtkPanel();
    private String directoryName = "", DisplayString = "3D Surface Rendering";
    private vtkMarchingCubes mCube;
    private int xMin, xMax, yMin, yMax, zMin, zMax, mode, lastX, lastY, doDragFlag = 0;
    private double sx, sy, sz, ox, oy, oz;
    private vtkCamera camera;

    static {
        System.loadLibrary("vtkCommonJava");
        System.loadLibrary("vtkFilteringJava");
        System.loadLibrary("vtkIOJava");
        System.loadLibrary("vtkImagingJava");
        System.loadLibrary("vtkGraphicsJava");
        System.loadLibrary("vtkRenderingJava");
        System.loadLibrary("vtkWidgetsJava");
        System.loadLibrary("vtkVolumeRenderingJava");
    }


    /* Creates new form SurfaceRendering */
    public SurfaceRendering() {
        initComponents();
        vpanSurface.setSize(pnlSurfaceViewer.getSize());
        vpanSurface.setBackground(Color.white);
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        dlgSurfaceSettings = new javax.swing.JDialog();
        btnSurfaceSettingsOk = new javax.swing.JButton();
        btnSurfaceSettingsCancel = new javax.swing.JButton();
        pnlSurface2 = new javax.swing.JPanel();
        lblColor2 = new javax.swing.JLabel();
        sdrTransparency2 = new javax.swing.JSlider();
        chbxSecondSurface = new javax.swing.JCheckBox();
        lblPixelValues2 = new javax.swing.JLabel();
        tfPixelValues2 = new javax.swing.JTextField();
        lblTransparency2 = new javax.swing.JLabel();
        cbxPredefinedValues2 = new javax.swing.JComboBox();
        lblPredefinedValues2 = new javax.swing.JLabel();
        pnlSelectedColor2 = new javax.swing.JPanel();
        pnlSurface1 = new javax.swing.JPanel();
        cbxPredefinedValues1 = new javax.swing.JComboBox();
        sdrTransparency1 = new javax.swing.JSlider();
        lblPredefinedValues1 = new javax.swing.JLabel();
        pnlSelectedColor1 = new javax.swing.JPanel();
        tfPixelValues1 = new javax.swing.JTextField();
        lblTransparency1 = new javax.swing.JLabel();
        lblPixelValues1 = new javax.swing.JLabel();
        lblColor1 = new javax.swing.JLabel();
        chbxFirstSurface = new javax.swing.JCheckBox();
        pnlSurfaceViewer = new javax.swing.JPanel();
        lblRotateSurface = new javax.swing.JLabel();
        lblSurfaceMove = new javax.swing.JLabel();
        lblSurfaceCubeBox = new javax.swing.JLabel();
        lblSurfaceSettings = new javax.swing.JLabel();
        lblAxial = new javax.swing.JLabel();
        lblCoronal = new javax.swing.JLabel();
        lblSagittal = new javax.swing.JLabel();
        lblDisplayPurpose = new javax.swing.JLabel();
        lblCaptureScreenShot = new javax.swing.JLabel();
        pnlselectedBackgroundColor = new javax.swing.JPanel();
        lblSurfaceZoomInOut = new javax.swing.JLabel();

        dlgSurfaceSettings.setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        dlgSurfaceSettings.setAlwaysOnTop(true);
        dlgSurfaceSettings.setResizable(false);

        btnSurfaceSettingsOk.setText("Ok");
        btnSurfaceSettingsOk.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSurfaceSettingsOkActionPerformed(evt);
            }
        });

        btnSurfaceSettingsCancel.setText("Cancel");
        btnSurfaceSettingsCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSurfaceSettingsCancelActionPerformed(evt);
            }
        });

        pnlSurface2.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        lblColor2.setText("Color");

        sdrTransparency2.setValue(100);

        chbxSecondSurface.setText("Second Surface");

        lblPixelValues2.setText("Pixel Values");

        lblTransparency2.setText("Transparency");

        cbxPredefinedValues2.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Custom", "Skin", "Bone", "Metal" }));
        cbxPredefinedValues2.setKeySelectionManager(null);
        cbxPredefinedValues2.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                cbxPredefinedValues2ItemStateChanged(evt);
            }
        });

        lblPredefinedValues2.setText("Predefined Values");

        pnlSelectedColor2.setBackground(new java.awt.Color(235, 149, 105));
        pnlSelectedColor2.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2), javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED)));
        pnlSelectedColor2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                pnlSelectedColor2MouseClicked(evt);
            }
        });

        javax.swing.GroupLayout pnlSelectedColor2Layout = new javax.swing.GroupLayout(pnlSelectedColor2);
        pnlSelectedColor2.setLayout(pnlSelectedColor2Layout);
        pnlSelectedColor2Layout.setHorizontalGroup(
            pnlSelectedColor2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 60, Short.MAX_VALUE)
        );
        pnlSelectedColor2Layout.setVerticalGroup(
            pnlSelectedColor2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 28, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout pnlSurface2Layout = new javax.swing.GroupLayout(pnlSurface2);
        pnlSurface2.setLayout(pnlSurface2Layout);
        pnlSurface2Layout.setHorizontalGroup(
            pnlSurface2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlSurface2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlSurface2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(chbxSecondSurface)
                    .addGroup(pnlSurface2Layout.createSequentialGroup()
                        .addGroup(pnlSurface2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblPredefinedValues2)
                            .addComponent(lblPixelValues2)
                            .addComponent(lblTransparency2)
                            .addComponent(lblColor2))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(pnlSurface2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(tfPixelValues2, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(pnlSelectedColor2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(sdrTransparency2, 0, 0, Short.MAX_VALUE)
                            .addComponent(cbxPredefinedValues2, 0, 193, Short.MAX_VALUE))))
                .addContainerGap())
        );
        pnlSurface2Layout.setVerticalGroup(
            pnlSurface2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlSurface2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(chbxSecondSurface)
                .addGap(8, 8, 8)
                .addGroup(pnlSurface2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cbxPredefinedValues2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblPredefinedValues2, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlSurface2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(tfPixelValues2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblPixelValues2, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlSurface2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlSurface2Layout.createSequentialGroup()
                        .addComponent(sdrTransparency2, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(pnlSelectedColor2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(pnlSurface2Layout.createSequentialGroup()
                        .addComponent(lblTransparency2, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(lblColor2, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );

        pnlSurface1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        cbxPredefinedValues1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Custom", "Skin", "Bone", "Metal" }));
        cbxPredefinedValues1.setKeySelectionManager(null);
        cbxPredefinedValues1.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                cbxPredefinedValues1ItemStateChanged(evt);
            }
        });

        sdrTransparency1.setValue(100);

        lblPredefinedValues1.setText("Predefined Values");

        pnlSelectedColor1.setBackground(new java.awt.Color(254, 254, 254));
        pnlSelectedColor1.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2), javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED)));
        pnlSelectedColor1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                pnlSelectedColor1MouseClicked(evt);
            }
        });

        javax.swing.GroupLayout pnlSelectedColor1Layout = new javax.swing.GroupLayout(pnlSelectedColor1);
        pnlSelectedColor1.setLayout(pnlSelectedColor1Layout);
        pnlSelectedColor1Layout.setHorizontalGroup(
            pnlSelectedColor1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 61, Short.MAX_VALUE)
        );
        pnlSelectedColor1Layout.setVerticalGroup(
            pnlSelectedColor1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 26, Short.MAX_VALUE)
        );

        lblTransparency1.setText("Transparency");

        lblPixelValues1.setText("Pixel Values");

        lblColor1.setText("Color");

        chbxFirstSurface.setText("First Surface");

        javax.swing.GroupLayout pnlSurface1Layout = new javax.swing.GroupLayout(pnlSurface1);
        pnlSurface1.setLayout(pnlSurface1Layout);
        pnlSurface1Layout.setHorizontalGroup(
            pnlSurface1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlSurface1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlSurface1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlSurface1Layout.createSequentialGroup()
                        .addComponent(chbxFirstSurface)
                        .addGap(184, 184, 184))
                    .addGroup(pnlSurface1Layout.createSequentialGroup()
                        .addGroup(pnlSurface1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblPixelValues1)
                            .addComponent(lblTransparency1)
                            .addGroup(pnlSurface1Layout.createSequentialGroup()
                                .addGap(12, 12, 12)
                                .addComponent(lblColor1)))
                        .addGap(45, 45, 45)
                        .addGroup(pnlSurface1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(pnlSelectedColor1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(pnlSurface1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                .addComponent(cbxPredefinedValues1, javax.swing.GroupLayout.Alignment.LEADING, 0, 189, Short.MAX_VALUE)
                                .addComponent(sdrTransparency1, javax.swing.GroupLayout.DEFAULT_SIZE, 189, Short.MAX_VALUE))
                            .addComponent(tfPixelValues1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(lblPredefinedValues1))
                .addContainerGap())
        );
        pnlSurface1Layout.setVerticalGroup(
            pnlSurface1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlSurface1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(chbxFirstSurface)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pnlSurface1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(cbxPredefinedValues1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblPredefinedValues1, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(8, 8, 8)
                .addGroup(pnlSurface1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(lblPixelValues1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(tfPixelValues1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pnlSurface1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(lblTransparency1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(sdrTransparency1, javax.swing.GroupLayout.PREFERRED_SIZE, 28, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addGroup(pnlSurface1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(lblColor1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pnlSelectedColor1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout dlgSurfaceSettingsLayout = new javax.swing.GroupLayout(dlgSurfaceSettings.getContentPane());
        dlgSurfaceSettings.getContentPane().setLayout(dlgSurfaceSettingsLayout);
        dlgSurfaceSettingsLayout.setHorizontalGroup(
            dlgSurfaceSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(dlgSurfaceSettingsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(dlgSurfaceSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, dlgSurfaceSettingsLayout.createSequentialGroup()
                        .addComponent(btnSurfaceSettingsOk, javax.swing.GroupLayout.PREFERRED_SIZE, 58, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnSurfaceSettingsCancel, javax.swing.GroupLayout.PREFERRED_SIZE, 86, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(dlgSurfaceSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(pnlSurface1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(pnlSurface2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        dlgSurfaceSettingsLayout.setVerticalGroup(
            dlgSurfaceSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(dlgSurfaceSettingsLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(pnlSurface1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(pnlSurface2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(dlgSurfaceSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnSurfaceSettingsOk)
                    .addComponent(btnSurfaceSettingsCancel))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Surface Rendering");
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosed(java.awt.event.WindowEvent evt) {
                formWindowClosed(evt);
            }
        });

        javax.swing.GroupLayout pnlSurfaceViewerLayout = new javax.swing.GroupLayout(pnlSurfaceViewer);
        pnlSurfaceViewer.setLayout(pnlSurfaceViewerLayout);
        pnlSurfaceViewerLayout.setHorizontalGroup(
            pnlSurfaceViewerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 1012, Short.MAX_VALUE)
        );
        pnlSurfaceViewerLayout.setVerticalGroup(
            pnlSurfaceViewerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 708, Short.MAX_VALUE)
        );

        lblRotateSurface.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblRotateSurface.setIcon(new javax.swing.ImageIcon(getClass().getResource("/in/raster/mayam/form/images/RotateSurface.png"))); // NOI18N
        lblRotateSurface.setToolTipText("Rotate the Surface Volume");
        lblRotateSurface.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);
        lblRotateSurface.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        lblRotateSurface.setIconTextGap(0);
        lblRotateSurface.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        lblRotateSurface.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lblRotateSurfaceMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                lblRotateSurfaceMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                lblRotateSurfaceMouseExited(evt);
            }
        });

        lblSurfaceMove.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblSurfaceMove.setIcon(new javax.swing.ImageIcon(getClass().getResource("/in/raster/mayam/form/images/Move.png"))); // NOI18N
        lblSurfaceMove.setToolTipText("Move the Surface Volume");
        lblSurfaceMove.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);
        lblSurfaceMove.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lblSurfaceMoveMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                lblSurfaceMoveMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                lblSurfaceMoveMouseExited(evt);
            }
        });

        lblSurfaceCubeBox.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblSurfaceCubeBox.setIcon(new javax.swing.ImageIcon(getClass().getResource("/in/raster/mayam/form/images/ResetInteractor.png"))); // NOI18N
        lblSurfaceCubeBox.setToolTipText("Reset the Mouse Operations");
        lblSurfaceCubeBox.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);
        lblSurfaceCubeBox.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lblSurfaceCubeBoxMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                lblSurfaceCubeBoxMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                lblSurfaceCubeBoxMouseExited(evt);
            }
        });

        lblSurfaceSettings.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblSurfaceSettings.setIcon(new javax.swing.ImageIcon(getClass().getResource("/in/raster/mayam/form/images/FusionSurfaceSettings.png"))); // NOI18N
        lblSurfaceSettings.setToolTipText("Settings for Surface Rendering");
        lblSurfaceSettings.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);
        lblSurfaceSettings.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lblSurfaceSettingsMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                lblSurfaceSettingsMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                lblSurfaceSettingsMouseExited(evt);
            }
        });

        lblAxial.setIcon(new javax.swing.ImageIcon(getClass().getResource("/in/raster/mayam/form/images/AxialView.png"))); // NOI18N
        lblAxial.setToolTipText("Display the Surface Volume in Axial View");
        lblAxial.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lblAxialMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                lblAxialMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                lblAxialMouseExited(evt);
            }
        });

        lblCoronal.setIcon(new javax.swing.ImageIcon(getClass().getResource("/in/raster/mayam/form/images/CoronalView.png"))); // NOI18N
        lblCoronal.setToolTipText("Display the Surface Volume in Coronal View");
        lblCoronal.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lblCoronalMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                lblCoronalMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                lblCoronalMouseExited(evt);
            }
        });

        lblSagittal.setIcon(new javax.swing.ImageIcon(getClass().getResource("/in/raster/mayam/form/images/SagittalView.png"))); // NOI18N
        lblSagittal.setToolTipText("Display the Surface Volume in Sagittal View");
        lblSagittal.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lblSagittalMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                lblSagittalMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                lblSagittalMouseExited(evt);
            }
        });

        lblDisplayPurpose.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblDisplayPurpose.setText("3D Surface Rendering");

        lblCaptureScreenShot.setIcon(new javax.swing.ImageIcon(getClass().getResource("/in/raster/mayam/form/images/ScreenShot.png"))); // NOI18N
        lblCaptureScreenShot.setToolTipText("Capture ScreenShot");
        lblCaptureScreenShot.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lblCaptureScreenShotMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                lblCaptureScreenShotMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                lblCaptureScreenShotMouseExited(evt);
            }
        });

        pnlselectedBackgroundColor.setBackground(new java.awt.Color(3, 2, 4));
        pnlselectedBackgroundColor.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 160, 255), 2));
        pnlselectedBackgroundColor.setToolTipText("Change Background Color");
        pnlselectedBackgroundColor.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                pnlselectedBackgroundColorMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                pnlselectedBackgroundColorMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                pnlselectedBackgroundColorMouseExited(evt);
            }
        });

        javax.swing.GroupLayout pnlselectedBackgroundColorLayout = new javax.swing.GroupLayout(pnlselectedBackgroundColor);
        pnlselectedBackgroundColor.setLayout(pnlselectedBackgroundColorLayout);
        pnlselectedBackgroundColorLayout.setHorizontalGroup(
            pnlselectedBackgroundColorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 23, Short.MAX_VALUE)
        );
        pnlselectedBackgroundColorLayout.setVerticalGroup(
            pnlselectedBackgroundColorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 20, Short.MAX_VALUE)
        );

        lblSurfaceZoomInOut.setIcon(new javax.swing.ImageIcon(getClass().getResource("/in/raster/mayam/form/images/ZoomInOut.png"))); // NOI18N
        lblSurfaceZoomInOut.setToolTipText("Zoom In or Zoom Out the Surface Volume");
        lblSurfaceZoomInOut.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lblSurfaceZoomInOutMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                lblSurfaceZoomInOutMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                lblSurfaceZoomInOutMouseExited(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pnlSurfaceViewer, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(lblSurfaceZoomInOut)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblRotateSurface)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblSurfaceMove)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblSurfaceCubeBox)
                        .addGap(18, 18, 18)
                        .addComponent(lblSurfaceSettings)
                        .addGap(18, 18, 18)
                        .addComponent(lblCaptureScreenShot)
                        .addGap(37, 37, 37)
                        .addComponent(lblDisplayPurpose, javax.swing.GroupLayout.PREFERRED_SIZE, 615, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 40, Short.MAX_VALUE)
                        .addComponent(lblAxial)
                        .addGap(5, 5, 5)
                        .addComponent(lblCoronal, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblSagittal, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(pnlselectedBackgroundColor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblDisplayPurpose, javax.swing.GroupLayout.DEFAULT_SIZE, 24, Short.MAX_VALUE)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(lblCaptureScreenShot, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(lblSurfaceSettings, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(lblSurfaceCubeBox, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(lblSurfaceMove, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(lblRotateSurface, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(lblSurfaceZoomInOut, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(pnlselectedBackgroundColor, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(lblSagittal, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(lblCoronal, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(lblAxial))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(pnlSurfaceViewer, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void lblRotateSurfaceMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblRotateSurfaceMouseEntered
        // Code for Display the Purpose of Rotate Icon
        lblDisplayPurpose.setText("Rotate the Surface Volume");
    }//GEN-LAST:event_lblRotateSurfaceMouseEntered

    private void lblRotateSurfaceMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblRotateSurfaceMouseExited
        // Code for Remove the Purpose from label
        lblDisplayPurpose.setText(DisplayString);
    }//GEN-LAST:event_lblRotateSurfaceMouseExited

    private void lblSurfaceMoveMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblSurfaceMoveMouseEntered
        // Code for Display the Purpose of Move Icon
        lblDisplayPurpose.setText("Move the Surface Volume");
    }//GEN-LAST:event_lblSurfaceMoveMouseEntered

    private void lblSurfaceMoveMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblSurfaceMoveMouseExited
        // Code for Remove the Purpose from label
        lblDisplayPurpose.setText(DisplayString);
    }//GEN-LAST:event_lblSurfaceMoveMouseExited

    private void lblSurfaceCubeBoxMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblSurfaceCubeBoxMouseEntered
        // Code for Display the Purpose of add Cube Box Icon
        lblDisplayPurpose.setText("Reset the Mouse Operations");
    }//GEN-LAST:event_lblSurfaceCubeBoxMouseEntered

    private void lblSurfaceCubeBoxMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblSurfaceCubeBoxMouseExited
        // Code for Remove the Purpose from label
        lblDisplayPurpose.setText(DisplayString);
    }//GEN-LAST:event_lblSurfaceCubeBoxMouseExited

    private void lblSurfaceSettingsMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblSurfaceSettingsMouseEntered
        // Code for Display the Purpose of SurfaceRenderingSettings Icon
        lblDisplayPurpose.setText("Settings for Surface Rendering");
    }//GEN-LAST:event_lblSurfaceSettingsMouseEntered

    private void lblSurfaceSettingsMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblSurfaceSettingsMouseExited
        // Code for Remove the Purpose from label
        lblDisplayPurpose.setText(DisplayString);
    }//GEN-LAST:event_lblSurfaceSettingsMouseExited

    private void lblAxialMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblAxialMouseEntered
        // Code For Display the purpose of Axial View Icon
        lblDisplayPurpose.setText("Display the Surface Volume in Axial View");
    }//GEN-LAST:event_lblAxialMouseEntered

    private void lblAxialMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblAxialMouseExited
        // Code for Remove the Purpose from label
        lblDisplayPurpose.setText(DisplayString);
    }//GEN-LAST:event_lblAxialMouseExited

    private void lblCoronalMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblCoronalMouseEntered
        // Code For Display the purpose of Coronal View Icon
        lblDisplayPurpose.setText("Display the Surface Volume in Coronal View");
    }//GEN-LAST:event_lblCoronalMouseEntered

    private void lblCoronalMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblCoronalMouseExited
        // Code for Remove the Purpose from label
        lblDisplayPurpose.setText(DisplayString);
    }//GEN-LAST:event_lblCoronalMouseExited

    private void lblSagittalMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblSagittalMouseEntered
        // Code For Display the purpose of Sagittal View Icon
        lblDisplayPurpose.setText("Display the Surface Volume in Sagittal View");
    }//GEN-LAST:event_lblSagittalMouseEntered

    private void lblSagittalMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblSagittalMouseExited
        // Code for Remove the Purpose from label
        lblDisplayPurpose.setText(DisplayString);
    }//GEN-LAST:event_lblSagittalMouseExited

    private void lblCoronalMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblCoronalMouseClicked
        // Code for display the Surface Volume in Coronal View by rotate the original volume...
        AlignCamera(1);
    }//GEN-LAST:event_lblCoronalMouseClicked

    private void lblSurfaceCubeBoxMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblSurfaceCubeBoxMouseClicked
        // Set the Default Interactor..
        vpanSurface.removeMouseListener(myMouseListener);
        vpanSurface.removeMouseMotionListener(myMouseMotionListener);
        mode = 0;
        vpanSurface.addMouseListener(vpanSurface);
        vpanSurface.addMouseMotionListener(vpanSurface);
    }//GEN-LAST:event_lblSurfaceCubeBoxMouseClicked

    private void lblSurfaceSettingsMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblSurfaceSettingsMouseClicked
        // Code for Display the Surface Rendering settings...
        dlgSurfaceSettings.setSize(390, 494);
        dlgSurfaceSettings.setVisible(true);

    }//GEN-LAST:event_lblSurfaceSettingsMouseClicked

    private void cbxPredefinedValues1ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cbxPredefinedValues1ItemStateChanged
        // Code for Set the predefined values...
        if (cbxPredefinedValues1.getSelectedItem().equals("Skin")) {
            tfPixelValues1.setText("-500");
            lblPixelValues1.setEnabled(false);
            tfPixelValues1.setEnabled(false);
        } else if (cbxPredefinedValues1.getSelectedItem().equals("Bone")) {
            tfPixelValues1.setText("500");
            lblPixelValues1.setEnabled(false);
            tfPixelValues1.setEnabled(false);
        } else if (cbxPredefinedValues1.getSelectedItem().equals("Metal")) {
            tfPixelValues1.setText("2000");
            lblPixelValues1.setEnabled(false);
            tfPixelValues1.setEnabled(false);
        } else if (cbxPredefinedValues1.getSelectedItem().equals("Custom")) {
            tfPixelValues1.setText("300");
            lblPixelValues1.setEnabled(true);
            tfPixelValues1.setEnabled(true);
        }
    }//GEN-LAST:event_cbxPredefinedValues1ItemStateChanged

    private void cbxPredefinedValues2ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cbxPredefinedValues2ItemStateChanged

        // Code for Set the predefined values...

        if (cbxPredefinedValues2.getSelectedItem().equals("Skin")) {
            tfPixelValues2.setText("-500");
            lblPixelValues2.setEnabled(false);
            tfPixelValues2.setEnabled(false);
        } else if (cbxPredefinedValues2.getSelectedItem().equals("Bone")) {
            tfPixelValues2.setText("500");
            lblPixelValues2.setEnabled(false);
            tfPixelValues2.setEnabled(false);
        } else if (cbxPredefinedValues2.getSelectedItem().equals("Metal")) {
            tfPixelValues2.setText("2000");
            lblPixelValues2.setEnabled(false);
            tfPixelValues2.setEnabled(false);
        } else if (cbxPredefinedValues2.getSelectedItem().equals("Custom")) {
            tfPixelValues2.setText("300");
            lblPixelValues2.setEnabled(true);
            tfPixelValues2.setEnabled(true);
        }
    }//GEN-LAST:event_cbxPredefinedValues2ItemStateChanged

    private void pnlSelectedColor1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_pnlSelectedColor1MouseClicked
        // Code for display the Color Pallette
        pnlSelectedColor1.setBackground(JColorChooser.showDialog(dlgSurfaceSettings, "Select Color", pnlSelectedColor1.getBackground()));
    }//GEN-LAST:event_pnlSelectedColor1MouseClicked

    private void pnlSelectedColor2MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_pnlSelectedColor2MouseClicked
        // Code for display the Color Pallette
        pnlSelectedColor2.setBackground(JColorChooser.showDialog(dlgSurfaceSettings, "Select Color", pnlSelectedColor2.getBackground()));
    }//GEN-LAST:event_pnlSelectedColor2MouseClicked

    private void btnSurfaceSettingsOkActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSurfaceSettingsOkActionPerformed
        // Code for Display the Surface(s) with given parameters...

        if (chbxFirstSurface.isSelected() && chbxSecondSurface.isSelected()) {
            dlgSurfaceSettings.setVisible(false);
            vpanSurface.GetRenderer().RemoveAllViewProps();
            int pixelValue1 = Integer.parseInt(tfPixelValues1.getText());
            double opacity1 = Double.valueOf(sdrTransparency1.getValue()) / 100.0;
            double colorRed1 = Double.valueOf(pnlSelectedColor1.getBackground().getRed()) / 255.0;
            double colorGreen1 = Double.valueOf(pnlSelectedColor1.getBackground().getGreen()) / 255.0;
            double colorBlue1 = Double.valueOf(pnlSelectedColor1.getBackground().getBlue()) / 255.0;
            int pixelValue2 = Integer.parseInt(tfPixelValues2.getText());
            double opacity2 = Double.valueOf(sdrTransparency2.getValue()) / 100.0;
            double colorRed2 = Double.valueOf(pnlSelectedColor2.getBackground().getRed()) / 255.0;
            double colorGreen2 = Double.valueOf(pnlSelectedColor2.getBackground().getGreen()) / 255.0;
            double colorBlue2 = Double.valueOf(pnlSelectedColor2.getBackground().getBlue()) / 255.0;
            DisplaySurfaces(pixelValue1, opacity1, colorRed1, colorGreen1, colorBlue1, pixelValue2, opacity2, colorRed2, colorGreen2, colorBlue2);

        } else if (chbxFirstSurface.isSelected()) {
            dlgSurfaceSettings.setVisible(false);
            vpanSurface.GetRenderer().RemoveAllViewProps();
            int pixelValue1 = Integer.parseInt(tfPixelValues1.getText());
            double opacity1 = Double.valueOf(sdrTransparency1.getValue()) / 100.0;
            double colorRed1 = Double.valueOf(pnlSelectedColor1.getBackground().getRed()) / 255.0;
            double colorGreen1 = Double.valueOf(pnlSelectedColor1.getBackground().getGreen()) / 255.0;
            double colorBlue1 = Double.valueOf(pnlSelectedColor1.getBackground().getBlue()) / 255.0;
            DisplaySurfaces(pixelValue1, opacity1, colorRed1, colorGreen1, colorBlue1);

        } else if (chbxSecondSurface.isSelected()) {
            dlgSurfaceSettings.setVisible(false);
            vpanSurface.GetRenderer().RemoveAllViewProps();
            int pixelValue2 = Integer.parseInt(tfPixelValues2.getText());
            double opacity2 = Double.valueOf(sdrTransparency2.getValue()) / 100.0;
            double colorRed2 = Double.valueOf(pnlSelectedColor2.getBackground().getRed()) / 255.0;
            double colorGreen2 = Double.valueOf(pnlSelectedColor2.getBackground().getGreen()) / 255.0;
            double colorBlue2 = Double.valueOf(pnlSelectedColor2.getBackground().getBlue()) / 255.0;
            DisplaySurfaces(pixelValue2, opacity2, colorRed2, colorGreen2, colorBlue2);
        }

        vpanSurface.repaint();
        reader.Delete();
        this.setCursor(DEFAULT_CURSOR);
    }//GEN-LAST:event_btnSurfaceSettingsOkActionPerformed

    private void btnSurfaceSettingsCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSurfaceSettingsCancelActionPerformed
        // Code for cancel the Surface Settings...
        dlgSurfaceSettings.setVisible(false);
        if (vpanSurface.GetRenderer().VisibleActorCount() == 0) {
            if (JOptionPane.showConfirmDialog(this, "Do you want to close the application?", "Dicom Surface Viewer", 0) == 0) {
                this.dispose();
            } else {
                dlgSurfaceSettings.setVisible(true);
            }
        }
    }//GEN-LAST:event_btnSurfaceSettingsCancelActionPerformed

    private void lblAxialMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblAxialMouseClicked
        // TODO add your handling code here:
        AlignCamera(2);
    }//GEN-LAST:event_lblAxialMouseClicked

    private void lblSagittalMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblSagittalMouseClicked
        // TODO add your handling code here:        
        AlignCamera(0);
    }//GEN-LAST:event_lblSagittalMouseClicked

    private void lblRotateSurfaceMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblRotateSurfaceMouseClicked
        //  Set the interaction for Rotate...
        vpanSurface.removeMouseListener(vpanSurface);
        vpanSurface.removeMouseMotionListener(vpanSurface);

        mode = 2;
        vpanSurface.addMouseListener(myMouseListener);
        vpanSurface.addMouseMotionListener(myMouseMotionListener);
    }//GEN-LAST:event_lblRotateSurfaceMouseClicked

    private void lblSurfaceMoveMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblSurfaceMoveMouseClicked
        // Set the interaction for Move...
        vpanSurface.removeMouseListener(vpanSurface);
        vpanSurface.removeMouseMotionListener(vpanSurface);
        mode = 3;
        vpanSurface.addMouseListener(myMouseListener);
        vpanSurface.addMouseMotionListener(myMouseMotionListener);
    }//GEN-LAST:event_lblSurfaceMoveMouseClicked

    private void lblCaptureScreenShotMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblCaptureScreenShotMouseClicked
        // Code for capture the view as an Image...

        // ask for the filename...
        JFileChooser fc = new JFileChooser();
        FileFilter filter = new FileFilter() {

            @Override
            public boolean accept(File file) {
                if (file.isDirectory()) {
                    return true;
                }
                String extension = null;
                String s = file.getName();
                int i = s.lastIndexOf('.');
                if (i > 0 && i < s.length() - 1) {
                    extension = s.substring(i + 1).toLowerCase();
                }
                if (extension != null) {
                    if (extension.equals("tiff") || extension.equals("tif")) {
                        return true;
                    } else {
                        return false;
                    }
                } else {
                    return false;
                }
            }

            @Override
            public String getDescription() {
                return "tiff/tif Images";
            }
        };

        fc.setFileFilter(filter);
        fc.setAcceptAllFileFilterUsed(false);
        int returnVal = fc.showSaveDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            // Capture and Save the image...

            String extension = null;
            String s = file.getName();
            int i = s.lastIndexOf('.');
            if (i > 0 && i < s.length() - 1) {
                extension = s.substring(i + 1).toLowerCase();
                if (extension.equals("tif") || extension.equals("tiff")) {
                    vpanSurface.HardCopy(file.getAbsolutePath(), 1);
                } else {
                    vpanSurface.HardCopy(file.getAbsolutePath().concat(".tif"), 1);
                }
            } else {
                vpanSurface.HardCopy(file.getAbsolutePath().concat(".tif"), 1);
            }
        }
        //Reset the file chooser for the next time it's shown.

        fc.setSelectedFile(null);
    }//GEN-LAST:event_lblCaptureScreenShotMouseClicked

    private void lblCaptureScreenShotMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblCaptureScreenShotMouseEntered
        // Code for capture the view as an Image...
        lblDisplayPurpose.setText("Capture the view as an Image");
    }//GEN-LAST:event_lblCaptureScreenShotMouseEntered

    private void lblCaptureScreenShotMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblCaptureScreenShotMouseExited
        // Code for Remove the Purpose from label
        lblDisplayPurpose.setText(DisplayString);
    }//GEN-LAST:event_lblCaptureScreenShotMouseExited

    private void pnlselectedBackgroundColorMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_pnlselectedBackgroundColorMouseClicked
        // Code for display the Color Pallette
        pnlselectedBackgroundColor.setBackground(JColorChooser.showDialog(dlgSurfaceSettings, "Select Color", pnlselectedBackgroundColor.getBackground()));
        vpanSurface.GetRenderer().SetBackground(Double.valueOf(pnlselectedBackgroundColor.getBackground().getRed()) / 255.0, Double.valueOf(pnlselectedBackgroundColor.getBackground().getGreen()) / 255.0, Double.valueOf(pnlselectedBackgroundColor.getBackground().getBlue()) / 255.0);
        vpanSurface.repaint();


    }//GEN-LAST:event_pnlselectedBackgroundColorMouseClicked

    private void pnlselectedBackgroundColorMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_pnlselectedBackgroundColorMouseEntered
        // TODO add your handling code here:
        lblDisplayPurpose.setText("Change Background Color");
    }//GEN-LAST:event_pnlselectedBackgroundColorMouseEntered

    private void pnlselectedBackgroundColorMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_pnlselectedBackgroundColorMouseExited
        // Code for Remove the Purpose from label
        lblDisplayPurpose.setText(DisplayString);

    }//GEN-LAST:event_pnlselectedBackgroundColorMouseExited

    private void lblSurfaceZoomInOutMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblSurfaceZoomInOutMouseClicked
        // Set the interaction for Zoom In and Zoom Out...

        vpanSurface.removeMouseListener(vpanSurface);
        vpanSurface.removeMouseMotionListener(vpanSurface);
        mode = 1;
        vpanSurface.addMouseListener(myMouseListener);
        vpanSurface.addMouseMotionListener(myMouseMotionListener);

    }//GEN-LAST:event_lblSurfaceZoomInOutMouseClicked

    private void lblSurfaceZoomInOutMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblSurfaceZoomInOutMouseEntered
        // Code for Display the Purpose of Zoom In/Out Icon
        lblDisplayPurpose.setText("Zoom In or Zoom Out the Surface Volume");
    }//GEN-LAST:event_lblSurfaceZoomInOutMouseEntered

    private void lblSurfaceZoomInOutMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblSurfaceZoomInOutMouseExited
        // Code for Remove the Purpose from label
        lblDisplayPurpose.setText(DisplayString);
    }//GEN-LAST:event_lblSurfaceZoomInOutMouseExited

    private void formWindowClosed(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosed
        vtkGlobalJavaHash.DeleteAll();
    }//GEN-LAST:event_formWindowClosed

    private void setDefaultSettings() {
        chbxFirstSurface.setSelected(true);
        cbxPredefinedValues1.setSelectedItem("Bone");
        tfPixelValues1.setText("500");
        tfPixelValues1.setEnabled(false);
        lblPixelValues1.setEnabled(true);

        chbxSecondSurface.setSelected(false);
        cbxPredefinedValues2.setSelectedItem("Skin");
        tfPixelValues2.setText("-500");
        tfPixelValues2.setEnabled(false);
        lblPixelValues2.setEnabled(true);
    }

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                new SurfaceRendering().setVisible(true);
            }
        });
    }

    public void readDicomDir(String path) {
        directoryName = path;
        setDefaultSettings();
        lblSurfaceSettingsMouseClicked(null);
    }

    private void DisplaySurfaces(int val, double opacityValue, double redValue, double greenValue, double blueValue) {
        // For Single Surface...

        reader = new vtkDICOMImageReader();
        if (new File(directoryName).isDirectory()) {
            reader.SetDirectoryName(directoryName);
        }
        reader.Update();
        reader.GetOutput().ReleaseDataFlagOn();
        
        int[] xyminmax = reader.GetOutput().GetWholeExtent();
        xMin = xyminmax[0];
        xMax = xyminmax[1];
        yMin = xyminmax[2];
        yMax = xyminmax[3];
        zMin = xyminmax[4];
        zMax = xyminmax[5];

        double[] spacing = reader.GetOutput().GetSpacing();
        sx = spacing[0];
        sy = spacing[1];
        sz = spacing[2];

        double[] origin = reader.GetOutput().GetOrigin();
        ox = origin[0];
        oy = origin[1];
        oz = origin[2];

        vtkOutlineFilter outline = new vtkOutlineFilter();
        outline.SetInput(reader.GetOutput());

        vtkPolyDataMapper outlineMapper = new vtkPolyDataMapper();
        outlineMapper.SetInput(outline.GetOutput());

        vtkActor outlineActor = new vtkActor();
        outlineActor.SetMapper(outlineMapper);


        vtkCellPicker picker = new vtkCellPicker();
        picker.SetTolerance(0.005);

        mCube = new vtkMarchingCubes();
        mCube.SetInput(reader.GetOutput());
        mCube.SetValue(0, val);
        mCube.ComputeNormalsOn();
        mCube.ComputeGradientsOff();
        mCube.UpdateWholeExtent();

        vtkPolyDataMapper pdMapper = new vtkPolyDataMapper();
        pdMapper.SetInput(mCube.GetOutput());
        pdMapper.ImmediateModeRenderingOn();
        pdMapper.ScalarVisibilityOff();

        // Create and Add Surface Actor...

        vtkActor actor = new vtkActor();
        actor.GetProperty().SetOpacity(opacityValue);
        actor.GetProperty().SetColor(redValue, greenValue, blueValue);
        actor.SetMapper(pdMapper);


        vpanSurface.GetRenderer().AddActor(actor);
        vpanSurface.GetRenderWindow().AddRenderer(vpanSurface.GetRenderer());

        // Add the outline actor to the renderer, set the background color and size
        vpanSurface.GetRenderer().AddActor(outlineActor);

        vpanSurface.GetRenderer().SetBackground(Double.valueOf(pnlselectedBackgroundColor.getBackground().getRed()) / 255.0, Double.valueOf(pnlselectedBackgroundColor.getBackground().getGreen()) / 255.0, Double.valueOf(pnlselectedBackgroundColor.getBackground().getBlue()) / 255.0);
        vpanSurface.GetRenderer().ResetCamera();
        pnlSurfaceViewer.add(vpanSurface);
        pnlSurfaceViewer.setVisible(true);

        DisplayString = reader.GetPatientName();
    }

    private void DisplaySurfaces(int val1, double opacityValue1, double redValue1, double greenValue1, double blueValue1, int val2, double opacityValue2, double redValue2, double greenValue2, double blueValue2) {
        // for 2 surfaces...
        reader = new vtkDICOMImageReader();
        if (new File(directoryName).isDirectory()) {
            reader.SetDirectoryName(directoryName);
        }
        reader.Update();
        reader.GetOutput().ReleaseDataFlagOn();

        int[] xyminmax = reader.GetOutput().GetWholeExtent();
        xMin = xyminmax[0];
        xMax = xyminmax[1];
        yMin = xyminmax[2];
        yMax = xyminmax[3];
        zMin = xyminmax[4];
        zMax = xyminmax[5];

        double[] spacing = reader.GetOutput().GetSpacing();
        sx = spacing[0];
        sy = spacing[1];
        sz = spacing[2];

        double[] origin = reader.GetOutput().GetOrigin();
        ox = origin[0];
        oy = origin[1];
        oz = origin[2];

        vtkOutlineFilter outline = new vtkOutlineFilter();
        outline.SetInput(reader.GetOutput());

        vtkPolyDataMapper outlineMapper = new vtkPolyDataMapper();
        outlineMapper.SetInput(outline.GetOutput());
        outlineMapper.ImmediateModeRenderingOn();

        vtkActor outlineActor = new vtkActor();
        outlineActor.SetMapper(outlineMapper);

        vtkCellPicker picker = new vtkCellPicker();
        picker.SetTolerance(0.005);

        vtkMarchingCubes mCube1 = new vtkMarchingCubes();
        mCube1.SetInput(reader.GetOutput());
        mCube1.SetValue(0, val1);
        mCube1.ComputeNormalsOn();
        mCube1.ComputeGradientsOff();
        mCube1.UpdateWholeExtent();

        vtkPolyDataMapper pdMapper1 = new vtkPolyDataMapper();
        pdMapper1.SetInput(mCube1.GetOutput());
        pdMapper1.ImmediateModeRenderingOn();
        pdMapper1.ScalarVisibilityOff();

        // Create and Add Surface Actor...
        vtkActor actor1 = new vtkActor();
        actor1.GetProperty().SetOpacity(opacityValue1);
        actor1.GetProperty().SetColor(redValue1, greenValue1, blueValue1);
        actor1.SetMapper(pdMapper1);
//        actor1.SetNumberOfCloudPoints(10000);

        vtkMarchingCubes mCube2 = new vtkMarchingCubes();
        mCube2.SetInput(reader.GetOutput());
        mCube2.SetValue(0, val2);
        mCube2.ComputeNormalsOn();
        mCube2.ComputeGradientsOff();
        mCube2.UpdateWholeExtent();

        vtkPolyDataMapper pdMapper2 = new vtkPolyDataMapper();
        pdMapper2.SetInput(mCube2.GetOutput());
        pdMapper2.ImmediateModeRenderingOn();
        pdMapper2.ScalarVisibilityOff();

        // Create and Add Surface Actor...
        vtkActor actor2 = new vtkActor();
        actor2.GetProperty().SetOpacity(opacityValue2);
        actor2.GetProperty().SetColor(redValue2, greenValue2, blueValue2);
        actor2.SetMapper(pdMapper2);
        //      actor2.SetNumberOfCloudPoints(10000);

        vpanSurface.GetRenderer().AddActor(actor1);
        vpanSurface.GetRenderer().AddActor(actor2);
        vpanSurface.GetRenderWindow().AddRenderer(vpanSurface.GetRenderer());

        // Add the outline actor to the renderer, set the background color and size
        vpanSurface.GetRenderer().AddActor(outlineActor);

        vpanSurface.GetRenderer().SetBackground(Double.valueOf(pnlselectedBackgroundColor.getBackground().getRed()) / 255.0, Double.valueOf(pnlselectedBackgroundColor.getBackground().getGreen()) / 255.0, Double.valueOf(pnlselectedBackgroundColor.getBackground().getBlue()) / 255.0);
        vpanSurface.GetRenderer().ResetCamera();
        pnlSurfaceViewer.add(vpanSurface);
        pnlSurfaceViewer.setVisible(true);

        DisplayString = reader.GetPatientName();
    }

    void AlignCamera(int iaxis) {
        //global ox oy oz sx sy sz xMax xMin yMax yMin zMax zMin slice_number
        //global current_widget
        double cx = (ox + (0.5 * (xMax - xMin)) * sx);
        double cy = (oy + (0.5 * (yMax - yMin)) * sy);
        double cz = (oy + (0.5 * (zMax - zMin)) * sz);
        double vx = 0;
        double vy = 0;
        double vz = 0;
        double nx = 0;
        double ny = 0;
        double nz = 0;
        if (iaxis == 0) {
            vz = -1;
            nx = (ox + xMax * sx);
            cx = (ox + sx);
        } else if (iaxis == 1) {
            vz = -1;
            ny = (oy + yMax * sy);
            cy = (oy + sy);
        } else {
            vy = 1;
            nz = (oz + zMax * sz);
            cz = (oz + sz);
        }
        double px = (cx + nx * 2);
        double py = (cy + ny * 2);
        double pz = (cz + nz * 3);

        camera = vpanSurface.GetRenderer().GetActiveCamera();
        camera.SetViewUp(vx, vy, vz);
        camera.SetFocalPoint(cx, cy, cz);
        camera.SetPosition(px, py, pz);
        camera.OrthogonalizeViewUp();

        // Reset the light...
        vtkLightCollection Lights = vpanSurface.GetRenderer().GetLights();
        int nLights = Lights.GetNumberOfItems();
        vtkLight light;
        double[] pos = camera.GetPosition();
        double[] fp = camera.GetFocalPoint();
        Lights.InitTraversal();
        for (int i = 0; i < nLights; i++) {
            light = Lights.GetNextItem();
            light.SetPosition(pos);
            light.SetFocalPoint(fp);
            light.Modified();
        }

        vpanSurface.GetRenderer().ResetCameraClippingRange();
        vpanSurface.GetRenderer().ResetCamera();
        vpanSurface.GetRenderer().WorldToView();
        vpanSurface.repaint();
    }
    MouseListener myMouseListener = new MouseListener() {

        public void mouseClicked(MouseEvent me) {
        }

        public void mousePressed(MouseEvent me) {
            lastX = me.getX();
            lastY = me.getY();
        }

        public void mouseReleased(MouseEvent me) {
        }

        public void mouseEntered(MouseEvent me) {
        }

        public void mouseExited(MouseEvent me) {
        }
    };
    MouseMotionListener myMouseMotionListener = new MouseMotionListener() {

        public void mouseDragged(MouseEvent me) {

            vpanSurface.lock();
            if (doDragFlag > 0) {
                doDragFlag--;
            } else {
                int x = me.getX();
                int y = me.getY();
                vtkCamera cam = vpanSurface.GetRenderer().GetActiveCamera();

                if (mode == 1) {            // Zoom In/Out Surface...
                    if (vpanSurface.GetRenderer().VisibleActorCount() == 0) {
                        return;
                    }
                    double zoomFactor;
                    zoomFactor = Math.pow(1.02, (y - lastY));
                    if (cam.GetParallelProjection() == 1) {
                        cam.SetParallelScale(cam.GetParallelScale() / zoomFactor);
                    } else {
                        cam.Dolly(zoomFactor);
                        vpanSurface.resetCameraClippingRange();
                    }
                } else if (mode == 2) {     // Rotate Surface...

                    if (vpanSurface.GetRenderer().VisibleActorCount() == 0) {
                        return;
                    }
                    if (x > vpanSurface.getWidth() / 2.0) {
                        cam.Roll(-(y - lastY));
                    } else {
                        cam.Roll((y - lastY));
                    }
                    if (y < vpanSurface.getHeight() / 2.0) {
                        cam.Roll(-(x - lastX));
                    } else {
                        cam.Roll((x - lastX));
                    }
                    vpanSurface.resetCameraClippingRange();
                } else if (mode == 3) {     // Move Surface...
                    if (vpanSurface.GetRenderer().VisibleActorCount() == 0) {
                        return;
                    }
                    double FPoint[];
                    double PPoint[];
                    double APoint[] = new double[3];
                    double RPoint[];
                    double focalDepth;

                    // get the current focal point and position
                    FPoint = cam.GetFocalPoint();
                    PPoint = cam.GetPosition();

                    // calculate the focal depth since we'll be using it a lot
                    vpanSurface.GetRenderer().SetWorldPoint(FPoint[0], FPoint[1], FPoint[2], 1.0);
                    vpanSurface.GetRenderer().WorldToDisplay();
                    focalDepth = vpanSurface.GetRenderer().GetDisplayPoint()[2];

                    APoint[0] = vpanSurface.GetRenderWindow().GetSize()[0] / 2.0 + (x - lastX);
                    APoint[1] = vpanSurface.GetRenderWindow().GetSize()[1] / 2.0 - (y - lastY);
                    APoint[2] = focalDepth;
                    vpanSurface.GetRenderer().SetDisplayPoint(APoint);
                    vpanSurface.GetRenderer().DisplayToWorld();
                    RPoint = vpanSurface.GetRenderer().GetWorldPoint();
                    if (RPoint[3] != 0.0) {
                        RPoint[0] = RPoint[0] / RPoint[3];
                        RPoint[1] = RPoint[1] / RPoint[3];
                        RPoint[2] = RPoint[2] / RPoint[3];
                    }
                    /*
                     * Compute a translation vector, moving everything 1/2
                     * the distance to the cursor. (Arbitrary scale factor)
                     */
                    cam.SetFocalPoint(
                            (FPoint[0] - RPoint[0]) / 2.0 + FPoint[0],
                            (FPoint[1] - RPoint[1]) / 2.0 + FPoint[1],
                            (FPoint[2] - RPoint[2]) / 2.0 + FPoint[2]);
                    cam.SetPosition(
                            (FPoint[0] - RPoint[0]) / 2.0 + PPoint[0],
                            (FPoint[1] - RPoint[1]) / 2.0 + PPoint[1],
                            (FPoint[2] - RPoint[2]) / 2.0 + PPoint[2]);
                    vpanSurface.resetCameraClippingRange();
                }
                lastX = x;
                lastY = y;

                vpanSurface.Render();
                doDragFlag = 1;
            }
            vpanSurface.unlock();
        }

        public void mouseMoved(MouseEvent me) {
            lastX = me.getX();
            lastY = me.getY();
        }
    };
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnSurfaceSettingsCancel;
    private javax.swing.JButton btnSurfaceSettingsOk;
    private javax.swing.JComboBox cbxPredefinedValues1;
    private javax.swing.JComboBox cbxPredefinedValues2;
    private javax.swing.JCheckBox chbxFirstSurface;
    private javax.swing.JCheckBox chbxSecondSurface;
    private javax.swing.JDialog dlgSurfaceSettings;
    private javax.swing.JLabel lblAxial;
    private javax.swing.JLabel lblCaptureScreenShot;
    private javax.swing.JLabel lblColor1;
    private javax.swing.JLabel lblColor2;
    private javax.swing.JLabel lblCoronal;
    private javax.swing.JLabel lblDisplayPurpose;
    private javax.swing.JLabel lblPixelValues1;
    private javax.swing.JLabel lblPixelValues2;
    private javax.swing.JLabel lblPredefinedValues1;
    private javax.swing.JLabel lblPredefinedValues2;
    private javax.swing.JLabel lblRotateSurface;
    private javax.swing.JLabel lblSagittal;
    private javax.swing.JLabel lblSurfaceCubeBox;
    private javax.swing.JLabel lblSurfaceMove;
    private javax.swing.JLabel lblSurfaceSettings;
    private javax.swing.JLabel lblSurfaceZoomInOut;
    private javax.swing.JLabel lblTransparency1;
    private javax.swing.JLabel lblTransparency2;
    private javax.swing.JPanel pnlSelectedColor1;
    private javax.swing.JPanel pnlSelectedColor2;
    private javax.swing.JPanel pnlSurface1;
    private javax.swing.JPanel pnlSurface2;
    private javax.swing.JPanel pnlSurfaceViewer;
    private javax.swing.JPanel pnlselectedBackgroundColor;
    private javax.swing.JSlider sdrTransparency1;
    private javax.swing.JSlider sdrTransparency2;
    private javax.swing.JTextField tfPixelValues1;
    private javax.swing.JTextField tfPixelValues2;
    // End of variables declaration//GEN-END:variables
}
