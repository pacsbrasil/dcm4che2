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

import java.awt.Color;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.File;
import javax.swing.filechooser.FileFilter;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import vtk.vtkCamera;
import vtk.vtkCanvas;
import vtk.vtkDICOMImageReader;
import vtk.vtkGlobalJavaHash;
import vtk.vtkImageActor;
import vtk.vtkImageMapToColors;
import vtk.vtkImageReslice;
import vtk.vtkLight;
import vtk.vtkLightCollection;
import vtk.vtkLookupTable;
import vtk.vtkMatrix4x4;
import vtk.vtkPanel;

/**
 * @author Sathish Kumar V      (sathishkumar.v@raster.in)
 */
public class DicomMPR3DSlider extends javax.swing.JFrame {
    // Global Variable declarations...

    private vtkPanel vpan3DMPRView = new vtkPanel();
    public vtkDICOMImageReader reader = new vtkDICOMImageReader();
    private vtkImageActor axialActor = new vtkImageActor();
    private vtkImageActor coronalActor = new vtkImageActor();
    private vtkImageActor sagittalActor = new vtkImageActor();
    private vtkImageReslice axialReslice = new vtkImageReslice();
    private vtkImageReslice coronalReslice = new vtkImageReslice();
    private vtkImageReslice sagittalReslice = new vtkImageReslice();
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
    }

    /** Creates new form DicomMPR3DSlider */
    public DicomMPR3DSlider() {
        initComponents();
        vpan3DMPRView.setSize(pnlView.getSize());
        vpan3DMPRView.setBackground(Color.white);

    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnlView = new javax.swing.JPanel();
        lblZoomInOut = new javax.swing.JLabel();
        lblRotate = new javax.swing.JLabel();
        lblMove = new javax.swing.JLabel();
        lblMouseReset = new javax.swing.JLabel();
        lblScreenshot = new javax.swing.JLabel();
        cbxAxial = new javax.swing.JCheckBox();
        cbxCoronal = new javax.swing.JCheckBox();
        cbxSagittal = new javax.swing.JCheckBox();
        sdrAxial = new javax.swing.JSlider();
        sdrCoronal = new javax.swing.JSlider();
        sdrSagittal = new javax.swing.JSlider();
        lblAxialView = new javax.swing.JLabel();
        lblCoronalView = new javax.swing.JLabel();
        lblSagittalView = new javax.swing.JLabel();
        lblDisplayPurpose = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Dicom MPR ( Multi Planner Reconstruction ) 3D");
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosed(java.awt.event.WindowEvent evt) {
                formWindowClosed(evt);
            }
        });

        javax.swing.GroupLayout pnlViewLayout = new javax.swing.GroupLayout(pnlView);
        pnlView.setLayout(pnlViewLayout);
        pnlViewLayout.setHorizontalGroup(
            pnlViewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 794, Short.MAX_VALUE)
        );
        pnlViewLayout.setVerticalGroup(
            pnlViewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 531, Short.MAX_VALUE)
        );

        lblZoomInOut.setIcon(new javax.swing.ImageIcon(getClass().getResource("/in/raster/mayam/form/images/ZoomInOut.png"))); // NOI18N
        lblZoomInOut.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lblZoomInOutMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                lblZoomInOutMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                lblZoomInOutMouseExited(evt);
            }
        });

        lblRotate.setIcon(new javax.swing.ImageIcon(getClass().getResource("/in/raster/mayam/form/images/RotateSurface.png"))); // NOI18N
        lblRotate.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lblRotateMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                lblRotateMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                lblRotateMouseExited(evt);
            }
        });

        lblMove.setIcon(new javax.swing.ImageIcon(getClass().getResource("/in/raster/mayam/form/images/Move.png"))); // NOI18N
        lblMove.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lblMoveMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                lblMoveMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                lblMoveMouseExited(evt);
            }
        });

        lblMouseReset.setIcon(new javax.swing.ImageIcon(getClass().getResource("/in/raster/mayam/form/images/ResetInteractor.png"))); // NOI18N
        lblMouseReset.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lblMouseResetMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                lblMouseResetMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                lblMouseResetMouseExited(evt);
            }
        });

        lblScreenshot.setIcon(new javax.swing.ImageIcon(getClass().getResource("/in/raster/mayam/form/images/ScreenShot.png"))); // NOI18N
        lblScreenshot.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lblScreenshotMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                lblScreenshotMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                lblScreenshotMouseExited(evt);
            }
        });

        cbxAxial.setSelected(true);
        cbxAxial.setText("Axial");
        cbxAxial.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                cbxAxialStateChanged(evt);
            }
        });

        cbxCoronal.setSelected(true);
        cbxCoronal.setText("Coronal");
        cbxCoronal.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                cbxCoronalStateChanged(evt);
            }
        });

        cbxSagittal.setSelected(true);
        cbxSagittal.setText("Sagittal");
        cbxSagittal.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                cbxSagittalStateChanged(evt);
            }
        });

        sdrAxial.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                sdrAxialStateChanged(evt);
            }
        });

        sdrCoronal.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                sdrCoronalStateChanged(evt);
            }
        });

        sdrSagittal.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                sdrSagittalStateChanged(evt);
            }
        });

        lblAxialView.setIcon(new javax.swing.ImageIcon(getClass().getResource("/in/raster/mayam/form/images/AxialView.png"))); // NOI18N
        lblAxialView.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lblAxialViewMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                lblAxialViewMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                lblAxialViewMouseExited(evt);
            }
        });

        lblCoronalView.setIcon(new javax.swing.ImageIcon(getClass().getResource("/in/raster/mayam/form/images/CoronalView.png"))); // NOI18N
        lblCoronalView.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lblCoronalViewMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                lblCoronalViewMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                lblCoronalViewMouseExited(evt);
            }
        });

        lblSagittalView.setIcon(new javax.swing.ImageIcon(getClass().getResource("/in/raster/mayam/form/images/SagittalView.png"))); // NOI18N
        lblSagittalView.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lblSagittalViewMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                lblSagittalViewMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                lblSagittalViewMouseExited(evt);
            }
        });

        lblDisplayPurpose.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblDisplayPurpose.setText("Dicom MPR ( Multi Planner Reconstruction ) 3D");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(pnlView, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addComponent(lblZoomInOut)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblRotate)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblMove)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblMouseReset, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblScreenshot)
                        .addGap(42, 42, 42)
                        .addComponent(lblDisplayPurpose, javax.swing.GroupLayout.PREFERRED_SIZE, 442, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 70, Short.MAX_VALUE)
                        .addComponent(lblAxialView)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblCoronalView)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblSagittalView))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addComponent(cbxAxial)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(sdrAxial, javax.swing.GroupLayout.PREFERRED_SIZE, 178, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(cbxCoronal)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(sdrCoronal, javax.swing.GroupLayout.PREFERRED_SIZE, 178, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(cbxSagittal)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(sdrSagittal, javax.swing.GroupLayout.PREFERRED_SIZE, 178, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblDisplayPurpose, javax.swing.GroupLayout.DEFAULT_SIZE, 24, Short.MAX_VALUE)
                    .addComponent(lblZoomInOut, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblRotate, javax.swing.GroupLayout.DEFAULT_SIZE, 24, Short.MAX_VALUE)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(lblMove, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(lblMouseReset, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(lblScreenshot, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(lblSagittalView, javax.swing.GroupLayout.DEFAULT_SIZE, 24, Short.MAX_VALUE)
                    .addComponent(lblAxialView, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 24, Short.MAX_VALUE)
                    .addComponent(lblCoronalView, javax.swing.GroupLayout.DEFAULT_SIZE, 24, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(sdrAxial, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cbxAxial)
                    .addComponent(cbxCoronal)
                    .addComponent(sdrCoronal, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addComponent(sdrSagittal, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(cbxSagittal)))
                .addGap(18, 18, 18)
                .addComponent(pnlView, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void cbxAxialStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_cbxAxialStateChanged
        // show or hide the Axial slice...
        if (cbxAxial.isSelected()) {
            vpan3DMPRView.GetRenderer().AddActor(axialActor);
            vpan3DMPRView.repaint();
            sdrAxial.setEnabled(true);
        } else {
            if (cbxCoronal.isSelected() || cbxSagittal.isSelected()) {
                vpan3DMPRView.GetRenderer().RemoveActor(axialActor);
                vpan3DMPRView.repaint();
                sdrAxial.setEnabled(false);
            } else {
                cbxAxial.setSelected(true);
            }
        }

    }//GEN-LAST:event_cbxAxialStateChanged

    private void cbxCoronalStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_cbxCoronalStateChanged
        // show or hide the Coronal slice...
        if (cbxCoronal.isSelected()) {
            vpan3DMPRView.GetRenderer().AddActor(coronalActor);
            vpan3DMPRView.repaint();
            sdrCoronal.setEnabled(true);
        } else {
            if (cbxAxial.isSelected() || cbxSagittal.isSelected()) {
                vpan3DMPRView.GetRenderer().RemoveActor(coronalActor);
                vpan3DMPRView.repaint();
                sdrCoronal.setEnabled(false);
            } else {
                cbxCoronal.setSelected(true);
            }
        }
    }//GEN-LAST:event_cbxCoronalStateChanged

    private void cbxSagittalStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_cbxSagittalStateChanged
        // show or hide the Sagittal slice...
        if (cbxSagittal.isSelected()) {
            vpan3DMPRView.GetRenderer().AddActor(sagittalActor);
            vpan3DMPRView.repaint();
            sdrSagittal.setEnabled(true);
        } else {
            if (cbxAxial.isSelected() || cbxCoronal.isSelected()) {
                vpan3DMPRView.GetRenderer().RemoveActor(sagittalActor);
                vpan3DMPRView.repaint();
                sdrSagittal.setEnabled(false);
            } else {
                cbxSagittal.setSelected(true);
            }
        }
    }//GEN-LAST:event_cbxSagittalStateChanged

    private void sdrAxialStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_sdrAxialStateChanged
        // Change the Axial slice, according to the axilaSlider value...
        vtkMatrix4x4 matrixAxialChange = axialReslice.GetResliceAxes();
        matrixAxialChange.SetElement(2, 3, ((Double.valueOf(sdrAxial.getValue()) / 100) * (reader.GetOutput().GetCenter()[2] * 2)));
        axialActor.SetPosition(0, ((Double.valueOf(50 - sdrAxial.getValue()) / 100) * (reader.GetOutput().GetCenter()[2] * 2)), 0);
        vpan3DMPRView.GetRenderer().ResetCameraClippingRange();
        vpan3DMPRView.repaint();
    }//GEN-LAST:event_sdrAxialStateChanged

    private void sdrCoronalStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_sdrCoronalStateChanged
        // Change the Coronal slice, according to the axilaSlider value...
        vtkMatrix4x4 matrixCoronalChange = coronalReslice.GetResliceAxes();
        matrixCoronalChange.SetElement(1, 3, ((Double.valueOf(sdrCoronal.getValue()) / 100) * (reader.GetOutput().GetCenter()[1] * 2)));
        coronalActor.SetPosition(0, 0, ((Double.valueOf(sdrCoronal.getValue() - 50) / 100) * (reader.GetOutput().GetCenter()[1] * 2)));
        vpan3DMPRView.GetRenderer().ResetCameraClippingRange();
        vpan3DMPRView.repaint();
    }//GEN-LAST:event_sdrCoronalStateChanged

    private void sdrSagittalStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_sdrSagittalStateChanged
        // Change the Sagittal slice, according to the axilaSlider value...
        vtkMatrix4x4 matrixSagittalChange = sagittalReslice.GetResliceAxes();
        matrixSagittalChange.SetElement(0, 3, ((Double.valueOf(sdrSagittal.getValue()) / 100) * (reader.GetOutput().GetCenter()[0] * 2)));
        sagittalActor.SetPosition(((Double.valueOf(sdrSagittal.getValue() - 50) / 100) * (reader.GetOutput().GetCenter()[0] * 2)), 0, 0);
        vpan3DMPRView.GetRenderer().ResetCameraClippingRange();
        vpan3DMPRView.repaint();
    }//GEN-LAST:event_sdrSagittalStateChanged

    private void lblZoomInOutMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblZoomInOutMouseClicked
        vpan3DMPRView.removeMouseListener(vpan3DMPRView);
        vpan3DMPRView.removeMouseMotionListener(vpan3DMPRView);
        mode = 1;
        vpan3DMPRView.addMouseListener(myMouseListener);
        vpan3DMPRView.addMouseMotionListener(myMouseMotionListener);
    }//GEN-LAST:event_lblZoomInOutMouseClicked

    private void lblRotateMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblRotateMouseClicked
        //  Set the interaction for Rotate...
        vpan3DMPRView.removeMouseListener(vpan3DMPRView);
        vpan3DMPRView.removeMouseMotionListener(vpan3DMPRView);
        mode = 2;
        vpan3DMPRView.addMouseListener(myMouseListener);
        vpan3DMPRView.addMouseMotionListener(myMouseMotionListener);
    }//GEN-LAST:event_lblRotateMouseClicked

    private void lblMoveMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblMoveMouseClicked
        // Set the interaction for Move...
        vpan3DMPRView.removeMouseListener(vpan3DMPRView);
        vpan3DMPRView.removeMouseMotionListener(vpan3DMPRView);
        mode = 3;
        vpan3DMPRView.addMouseListener(myMouseListener);
        vpan3DMPRView.addMouseMotionListener(myMouseMotionListener);
    }//GEN-LAST:event_lblMoveMouseClicked

    private void lblMouseResetMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblMouseResetMouseClicked
        // Set the Default Interactor..
        vpan3DMPRView.removeMouseListener(myMouseListener);
        vpan3DMPRView.removeMouseMotionListener(myMouseMotionListener);
        mode = 0;
        vpan3DMPRView.addMouseListener(vpan3DMPRView);
        vpan3DMPRView.addMouseMotionListener(vpan3DMPRView);
    }//GEN-LAST:event_lblMouseResetMouseClicked

    private void lblScreenshotMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblScreenshotMouseClicked
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

            public String getDescription() {
                return "tiff/tif Images";
            }
        };

        fc.setFileFilter(filter);
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
                    vpan3DMPRView.HardCopy(file.getAbsolutePath(), 1);
                } else {
                    vpan3DMPRView.HardCopy(file.getAbsolutePath().concat(".tif"), 1);
                }
            } else {
                vpan3DMPRView.HardCopy(file.getAbsolutePath().concat(".tif"), 1);
            }
        }
        //Reset the file chooser for the next time it's shown.

        fc.setSelectedFile(null);
    }//GEN-LAST:event_lblScreenshotMouseClicked

    private void lblZoomInOutMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblZoomInOutMouseEntered
        // Code For Display the purpose of lblZoomInOut Icon
        lblDisplayPurpose.setText("Zoom in/out the 3D MPR");
    }//GEN-LAST:event_lblZoomInOutMouseEntered

    private void lblZoomInOutMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblZoomInOutMouseExited
        // Code For Display the purpose of lblZoomInOut Icon
        lblDisplayPurpose.setText("");
    }//GEN-LAST:event_lblZoomInOutMouseExited

    private void lblRotateMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblRotateMouseEntered
        // Code For Display the purpose of lblRotate Icon
        lblDisplayPurpose.setText("Rotate the 3D MPR");
    }//GEN-LAST:event_lblRotateMouseEntered

    private void lblRotateMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblRotateMouseExited
        // Code For Display the purpose of lblRotate Icon
        lblDisplayPurpose.setText("");
    }//GEN-LAST:event_lblRotateMouseExited

    private void lblMoveMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblMoveMouseEntered
        // Code For Display the purpose of lblMove Icon
        lblDisplayPurpose.setText("Move the 3D MPR");
    }//GEN-LAST:event_lblMoveMouseEntered

    private void lblMoveMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblMoveMouseExited
        // Code For Display the purpose of lblMove Icon
        lblDisplayPurpose.setText("");
    }//GEN-LAST:event_lblMoveMouseExited

    private void lblMouseResetMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblMouseResetMouseEntered
        // Code For Display the purpose of lblMouseReset Icon
        lblDisplayPurpose.setText("Reset the Mouse Operations");
    }//GEN-LAST:event_lblMouseResetMouseEntered

    private void lblMouseResetMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblMouseResetMouseExited
        // Code For Display the purpose of lblMouseReset Icon
        lblDisplayPurpose.setText("");
    }//GEN-LAST:event_lblMouseResetMouseExited

    private void lblScreenshotMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblScreenshotMouseEntered
        // Code For Display the purpose of lblScreenshot Icon
        lblDisplayPurpose.setText("Save the view as Screenshot");
    }//GEN-LAST:event_lblScreenshotMouseEntered

    private void lblScreenshotMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblScreenshotMouseExited
        // Code For Display the purpose of lblScreenshot Icon
        lblDisplayPurpose.setText("");
    }//GEN-LAST:event_lblScreenshotMouseExited

    private void lblAxialViewMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblAxialViewMouseClicked
        // TODO add your handling code here:
        AlignCamera(1);
    }//GEN-LAST:event_lblAxialViewMouseClicked

    private void lblAxialViewMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblAxialViewMouseEntered
        // Code For Display the purpose of lblAxialView Icon
        lblDisplayPurpose.setText("Display the 3D MPR in Axial View");
    }//GEN-LAST:event_lblAxialViewMouseEntered

    private void lblAxialViewMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblAxialViewMouseExited
        // Code For Display the purpose of lblAxialView Icon
        lblDisplayPurpose.setText("");
    }//GEN-LAST:event_lblAxialViewMouseExited

    private void lblCoronalViewMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblCoronalViewMouseClicked
        // TODO add your handling code here:
        AlignCamera(2);
    }//GEN-LAST:event_lblCoronalViewMouseClicked

    private void lblCoronalViewMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblCoronalViewMouseEntered
        // Code For Display the purpose of lblCoronalView Icon
        lblDisplayPurpose.setText("Display the 3D MPR in Coronal View");
    }//GEN-LAST:event_lblCoronalViewMouseEntered

    private void lblCoronalViewMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblCoronalViewMouseExited
        // Code For Display the purpose of lblCoronalView Icon
        lblDisplayPurpose.setText("");
    }//GEN-LAST:event_lblCoronalViewMouseExited

    private void lblSagittalViewMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblSagittalViewMouseClicked
        // TODO add your handling code here:
        AlignCamera(0);
    }//GEN-LAST:event_lblSagittalViewMouseClicked

    private void lblSagittalViewMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblSagittalViewMouseEntered
        // Code For Display the purpose of lblSagittalView Icon
        lblDisplayPurpose.setText("Display the 3D MPR in Sagittal View");
    }//GEN-LAST:event_lblSagittalViewMouseEntered

    private void lblSagittalViewMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblSagittalViewMouseExited
        // Code For Display the purpose of lblSagittalView Icon
        lblDisplayPurpose.setText("");
    }//GEN-LAST:event_lblSagittalViewMouseExited

    private void formWindowClosed(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosed
        vtkGlobalJavaHash.DeleteAll();
    }//GEN-LAST:event_formWindowClosed

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                new DicomMPR3DSlider().setVisible(true);
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox cbxAxial;
    private javax.swing.JCheckBox cbxCoronal;
    private javax.swing.JCheckBox cbxSagittal;
    private javax.swing.JLabel lblAxialView;
    private javax.swing.JLabel lblCoronalView;
    private javax.swing.JLabel lblDisplayPurpose;
    private javax.swing.JLabel lblMouseReset;
    private javax.swing.JLabel lblMove;
    private javax.swing.JLabel lblRotate;
    private javax.swing.JLabel lblSagittalView;
    private javax.swing.JLabel lblScreenshot;
    private javax.swing.JLabel lblZoomInOut;
    private javax.swing.JPanel pnlView;
    private javax.swing.JSlider sdrAxial;
    private javax.swing.JSlider sdrCoronal;
    private javax.swing.JSlider sdrSagittal;
    // End of variables declaration//GEN-END:variables

    public void readDicom(String dirName) {
        reader.SetDirectoryName(dirName);
        reader.Update();
        
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

        // Call the Method to display the slices...
        displayMPRView();
    }

    private void displayMPRView() {

        double[] center = reader.GetOutput().GetCenter();


        double[] axialElements = {1, 0, 0, center[0], 0, 1, 0, center[1], 0, 0, 1, center[2], 0, 0, 0, 1};
        double[] coronalElements = {1, 0, 0, center[0], 0, 0, 1, center[1], 0, -1, 0, center[2], 0, 0, 0, 1};
        double[] sagittalElements = {0, 0, -1, center[0], 1, 0, 0, center[1], 0, -1, 0, center[2], 0, 0, 0, 1};

        // For display the Axial Slice...

        // Set the slice orientation...
        vtkMatrix4x4 axialResliceAxes = new vtkMatrix4x4();
        axialResliceAxes.DeepCopy(axialElements);

        // Extract a slice in the desired orientation...        
        axialReslice.SetInputConnection(reader.GetOutputPort());
        axialReslice.SetOutputDimensionality(2);
        axialReslice.SetResliceAxes(axialResliceAxes);
        axialReslice.SetInterpolationModeToNearestNeighbor();

        // Create a greyscale lookup table...
        vtkLookupTable axialTable = new vtkLookupTable();
        axialTable.SetRange(0, 100); // image intensity range
        axialTable.SetValueRange(0.0, 1.0); // from black to white
        axialTable.SetSaturationRange(0.0, 0.0); // no color saturation
        axialTable.SetRampToLinear();
        axialTable.Build();

        // Map the image through the lookup table...
        vtkImageMapToColors axialColor = new vtkImageMapToColors();
        axialColor.SetLookupTable(axialTable);
        axialColor.SetInputConnection(axialReslice.GetOutputPort());

        // Display the image...
        axialActor.SetInput(axialColor.GetOutput());
        axialActor.SetInput(axialColor.GetOutput());

        // For display the Coronal Slice...

        // Set the slice orientation...
        vtkMatrix4x4 coronalResliceAxes = new vtkMatrix4x4();
        coronalResliceAxes.DeepCopy(coronalElements);

        // Extract a slice in the desired orientation...        
        coronalReslice.SetInputConnection(reader.GetOutputPort());
        coronalReslice.SetOutputDimensionality(2);
        coronalReslice.SetResliceAxes(coronalResliceAxes);
        coronalReslice.SetInterpolationModeToNearestNeighbor();

        // Create a greyscale lookup table...
        vtkLookupTable coronalTable = new vtkLookupTable();
        coronalTable.SetRange(0, 100); // image intensity range
        coronalTable.SetValueRange(0.0, 1.0); // from black to white
        coronalTable.SetSaturationRange(0.0, 0.0); // no color saturation
        coronalTable.SetRampToLinear();
        coronalTable.Build();

        // Map the image through the lookup table...
        vtkImageMapToColors coronalColor = new vtkImageMapToColors();
        coronalColor.SetLookupTable(coronalTable);
        coronalColor.SetInputConnection(coronalReslice.GetOutputPort());

        // Display the image...
        coronalActor.SetInput(coronalColor.GetOutput());

        // For display the Sagittal Slice...
        // Set the slice orientation...
        vtkMatrix4x4 sagittalResliceAxes = new vtkMatrix4x4();
        sagittalResliceAxes.DeepCopy(sagittalElements);

        // Extract a slice in the desired orientation        
        sagittalReslice.SetInputConnection(reader.GetOutputPort());
        sagittalReslice.SetOutputDimensionality(2);
        sagittalReslice.SetResliceAxes(sagittalResliceAxes);
        sagittalReslice.SetInterpolationModeToNearestNeighbor();

        // Create a greyscale lookup table
        vtkLookupTable sagittalTable = new vtkLookupTable();
        sagittalTable.SetRange(0, 100); // image intensity range
        sagittalTable.SetValueRange(0.0, 1.0); // from black to white
        sagittalTable.SetSaturationRange(0.0, 0.0); // no color saturation
        sagittalTable.SetRampToLinear();
        sagittalTable.Build();


        // Map the image through the lookup table
        vtkImageMapToColors sagittalColor = new vtkImageMapToColors();
        sagittalColor.SetLookupTable(sagittalTable);
        sagittalColor.SetInputConnection(sagittalReslice.GetOutputPort());

        // Display the image
        sagittalActor.SetInput(sagittalColor.GetOutput());

        //Consider for positioning
        axialActor.RotateX(90);
        sagittalActor.RotateY(270);

        // Display Axial, Coronal and Sagittal views into vtkPanel
        vpan3DMPRView.GetRenderer().AddActor(axialActor);
        vpan3DMPRView.GetRenderer().AddActor(coronalActor);
        vpan3DMPRView.GetRenderer().AddActor(sagittalActor);
        vpan3DMPRView.GetRenderer().SetBackground(0, 0, 0);
        vpan3DMPRView.GetRenderer().ResetCamera();
        pnlView.add(vpan3DMPRView);
        vpan3DMPRView.repaint();
    }

    void AlignCamera(int iaxis) {

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

        camera = vpan3DMPRView.GetRenderer().GetActiveCamera();
        camera.SetViewUp(vx, vy, vz);
        camera.SetFocalPoint(cx, cy, cz);
        camera.SetPosition(px, py, pz);
        camera.OrthogonalizeViewUp();
        if (iaxis == 0) {
            camera.Roll(-90);
        } else if (iaxis == 1) {
            camera.Roll(180);
        }

        // Reset the light...
        vtkLightCollection Lights = vpan3DMPRView.GetRenderer().GetLights();
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

        vpan3DMPRView.GetRenderer().ResetCameraClippingRange();
        vpan3DMPRView.GetRenderer().ResetCamera();
        vpan3DMPRView.GetRenderer().WorldToView();
        vpan3DMPRView.repaint();
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

            vpan3DMPRView.lock();
            if (doDragFlag > 0) {
                doDragFlag--;
            } else {
                int x = me.getX();
                int y = me.getY();
                vtkCamera cam = vpan3DMPRView.GetRenderer().GetActiveCamera();

                if (mode == 1) {            // Zoom In/Out Surface...
                    if (vpan3DMPRView.GetRenderer().VisibleActorCount() == 0) {
                        return;
                    }
                    double zoomFactor;
                    zoomFactor = Math.pow(1.02, (y - lastY));
                    if (cam.GetParallelProjection() == 1) {
                        cam.SetParallelScale(cam.GetParallelScale() / zoomFactor);
                    } else {
                        cam.Dolly(zoomFactor);
                        vpan3DMPRView.resetCameraClippingRange();
                    }
                } else if (mode == 2) {     // Rotate Surface...

                    if (vpan3DMPRView.GetRenderer().VisibleActorCount() == 0) {
                        return;
                    }
                    if (x > vpan3DMPRView.getWidth() / 2.0) {
                        cam.Roll(-(y - lastY));
                    } else {
                        cam.Roll((y - lastY));
                    }
                    if (y < vpan3DMPRView.getHeight() / 2.0) {
                        cam.Roll(-(x - lastX));
                    } else {
                        cam.Roll((x - lastX));
                    }
                    vpan3DMPRView.resetCameraClippingRange();
                } else if (mode == 3) {     // Move Surface...
                    if (vpan3DMPRView.GetRenderer().VisibleActorCount() == 0) {
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
                    vpan3DMPRView.GetRenderer().SetWorldPoint(FPoint[0], FPoint[1], FPoint[2], 1.0);
                    vpan3DMPRView.GetRenderer().WorldToDisplay();
                    focalDepth = vpan3DMPRView.GetRenderer().GetDisplayPoint()[2];

                    APoint[0] = vpan3DMPRView.GetRenderWindow().GetSize()[0] / 2.0 + (x - lastX);
                    APoint[1] = vpan3DMPRView.GetRenderWindow().GetSize()[1] / 2.0 - (y - lastY);
                    APoint[2] = focalDepth;
                    vpan3DMPRView.GetRenderer().SetDisplayPoint(APoint);
                    vpan3DMPRView.GetRenderer().DisplayToWorld();
                    RPoint = vpan3DMPRView.GetRenderer().GetWorldPoint();
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
                    vpan3DMPRView.resetCameraClippingRange();
                }
                lastX = x;
                lastY = y;

                vpan3DMPRView.Render();
                doDragFlag = 1;
            }
            vpan3DMPRView.unlock();
        }

        public void mouseMoved(MouseEvent me) {
            lastX = me.getX();
            lastY = me.getY();
        }
    };
}
