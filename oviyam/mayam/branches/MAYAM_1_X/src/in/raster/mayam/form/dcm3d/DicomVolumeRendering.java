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
import javax.swing.JColorChooser;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;
import vtk.vtkCamera;
import vtk.vtkCanvas;
import vtk.vtkColorTransferFunction;
import vtk.vtkDICOMImageReader;
import vtk.vtkGlobalJavaHash;
import vtk.vtkImageShiftScale;
import vtk.vtkLight;
import vtk.vtkLightCollection;
import vtk.vtkPiecewiseFunction;
import vtk.vtkVolume;
import vtk.vtkVolumeProperty;
import vtk.vtkVolumeRayCastCompositeFunction;
import vtk.vtkVolumeRayCastMapper;

/**
 * DicomVolumeRendering.java
 *
 * Created on 20 Aug, 2010, 1:03:15 PM
 *
 *  This File Load the Series of Dicom images and
 *  Construct them as a Volume...
 *
 */

/*
 * @author Sathish Kumar V              (sathishkumar.v@raster.in)
 */
public class DicomVolumeRendering extends javax.swing.JFrame {

    private vtkDICOMImageReader reader;
    private vtkCanvas vpanVolume = new vtkCanvas();
    private int mode = 0, lastX, lastY, doDragFlag;
    private String directoryName = "", DisplayString = "3D Volume Rendering";
    private vtkImageShiftScale vtkmyImageCast = new vtkImageShiftScale();
    private vtkVolume volume = new vtkVolume();
    long startTime;
    private int xMin, xMax, yMin, yMax, zMin, zMax;
    private double sx, sy, sz, ox, oy, oz;

    static {
        System.loadLibrary("vtkCommonJava");
        System.loadLibrary("vtkFilteringJava");
        System.loadLibrary("vtkIOJava");
        System.loadLibrary("vtkImagingJava");
        System.loadLibrary("vtkGraphicsJava");
        System.loadLibrary("vtkRenderingJava");
        System.loadLibrary("vtkVolumeRenderingJava");
    }

    /* Creates new form DicomVolumeRendering */
    public DicomVolumeRendering() {
        initComponents();
        vpanVolume.setSize(pnlVolumeViewer.getSize());
        vpanVolume.setBackground(Color.white);
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnlVolumeViewer = new javax.swing.JPanel();
        lblRotate = new javax.swing.JLabel();
        lblMove = new javax.swing.JLabel();
        lblCubeBox = new javax.swing.JLabel();
        lblDisplayPurpose = new javax.swing.JLabel();
        lblCaptureScreenShot = new javax.swing.JLabel();
        pnlselectedBackgroundColor = new javax.swing.JPanel();
        lblZoomInOut = new javax.swing.JLabel();
        lblReset = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("3D Volume Rendering");
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosed(java.awt.event.WindowEvent evt) {
                formWindowClosed(evt);
            }
        });

        javax.swing.GroupLayout pnlVolumeViewerLayout = new javax.swing.GroupLayout(pnlVolumeViewer);
        pnlVolumeViewer.setLayout(pnlVolumeViewerLayout);
        pnlVolumeViewerLayout.setHorizontalGroup(
            pnlVolumeViewerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 1019, Short.MAX_VALUE)
        );
        pnlVolumeViewerLayout.setVerticalGroup(
            pnlVolumeViewerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 702, Short.MAX_VALUE)
        );

        lblRotate.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblRotate.setIcon(new javax.swing.ImageIcon(getClass().getResource("/in/raster/mayam/form/images/RotateSurface.png"))); // NOI18N
        lblRotate.setToolTipText("Rotate the Volume");
        lblRotate.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);
        lblRotate.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        lblRotate.setIconTextGap(0);
        lblRotate.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
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

        lblMove.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblMove.setIcon(new javax.swing.ImageIcon(getClass().getResource("/in/raster/mayam/form/images/Move.png"))); // NOI18N
        lblMove.setToolTipText("Move the Volume");
        lblMove.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);
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

        lblCubeBox.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblCubeBox.setIcon(new javax.swing.ImageIcon(getClass().getResource("/in/raster/mayam/form/images/ResetInteractor.png"))); // NOI18N
        lblCubeBox.setToolTipText("Reset the Mouse Interaction");
        lblCubeBox.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);
        lblCubeBox.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lblCubeBoxMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                lblCubeBoxMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                lblCubeBoxMouseExited(evt);
            }
        });

        lblDisplayPurpose.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblDisplayPurpose.setText("3D Volume Rendering");

        lblCaptureScreenShot.setIcon(new javax.swing.ImageIcon(getClass().getResource("/in/raster/mayam/form/images/ScreenShot.png"))); // NOI18N
        lblCaptureScreenShot.setToolTipText("Capture Screenshot");
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
        pnlselectedBackgroundColor.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
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
            .addGap(0, 22, Short.MAX_VALUE)
        );
        pnlselectedBackgroundColorLayout.setVerticalGroup(
            pnlselectedBackgroundColorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 22, Short.MAX_VALUE)
        );

        lblZoomInOut.setIcon(new javax.swing.ImageIcon(getClass().getResource("/in/raster/mayam/form/images/ZoomInOut.png"))); // NOI18N
        lblZoomInOut.setToolTipText("Zoom In or Zoom Out the Volume");
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

        lblReset.setIcon(new javax.swing.ImageIcon(getClass().getResource("/in/raster/mayam/form/images/DrawCircle.png"))); // NOI18N
        lblReset.setToolTipText("Reset the Volume");
        lblReset.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lblResetMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                lblResetMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                lblResetMouseExited(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pnlVolumeViewer, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(lblZoomInOut)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblRotate)
                        .addGap(9, 9, 9)
                        .addComponent(lblMove)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblCubeBox)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblReset, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblCaptureScreenShot)
                        .addGap(4, 4, 4)
                        .addComponent(pnlselectedBackgroundColor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 342, Short.MAX_VALUE)
                        .addComponent(lblDisplayPurpose, javax.swing.GroupLayout.PREFERRED_SIZE, 466, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblDisplayPurpose, javax.swing.GroupLayout.DEFAULT_SIZE, 24, Short.MAX_VALUE)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(pnlselectedBackgroundColor, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(lblMove, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(lblZoomInOut, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(lblRotate, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(lblCaptureScreenShot, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(lblReset, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(lblCubeBox, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addGap(18, 18, 18)
                .addComponent(pnlVolumeViewer, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void lblRotateMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblRotateMouseEntered
        // Code for Display the Purpose of Rotate Icon
        lblDisplayPurpose.setText("Rotate the Volume");
    }//GEN-LAST:event_lblRotateMouseEntered

    private void lblRotateMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblRotateMouseExited
        // Code for Remove the Purpose from label
        lblDisplayPurpose.setText(DisplayString);
    }//GEN-LAST:event_lblRotateMouseExited

    private void lblMoveMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblMoveMouseEntered
        // Code for Display the Purpose of Move Icon
        lblDisplayPurpose.setText("Move the Volume");
    }//GEN-LAST:event_lblMoveMouseEntered

    private void lblMoveMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblMoveMouseExited
        // Code for Remove the Purpose from label
        lblDisplayPurpose.setText(DisplayString);
    }//GEN-LAST:event_lblMoveMouseExited

    private void lblCubeBoxMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblCubeBoxMouseEntered
        // Code for Display the Purpose of add Cube Box Icon
        lblDisplayPurpose.setText("Reset the Mouse Interaction");
    }//GEN-LAST:event_lblCubeBoxMouseEntered

    private void lblCubeBoxMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblCubeBoxMouseExited
        // Code for Remove the Purpose from label
        lblDisplayPurpose.setText(DisplayString);
    }//GEN-LAST:event_lblCubeBoxMouseExited

    private void lblCubeBoxMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblCubeBoxMouseClicked
        // Set the Default Interactor..

        vpanVolume.removeMouseListener(myMouseListener);
        vpanVolume.removeMouseMotionListener(myMouseMotionListener);

        mode = 0;
        vpanVolume.addMouseListener(vpanVolume);
        vpanVolume.addMouseMotionListener(vpanVolume);
    }//GEN-LAST:event_lblCubeBoxMouseClicked

    private void lblRotateMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblRotateMouseClicked
        //  Set the interaction for Rotate...
        vpanVolume.removeMouseListener(vpanVolume);
        vpanVolume.removeMouseMotionListener(vpanVolume);

        mode = 2;
        vpanVolume.addMouseListener(myMouseListener);
        vpanVolume.addMouseMotionListener(myMouseMotionListener);
    }//GEN-LAST:event_lblRotateMouseClicked

    private void lblMoveMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblMoveMouseClicked
        // Set the interaction for Move...

        vpanVolume.removeMouseListener(vpanVolume);
        vpanVolume.removeMouseMotionListener(vpanVolume);

        mode = 3;
        vpanVolume.addMouseListener(myMouseListener);
        vpanVolume.addMouseMotionListener(myMouseMotionListener);
    }//GEN-LAST:event_lblMoveMouseClicked

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
                    vpanVolume.HardCopy(file.getAbsolutePath(), 1);
                } else {
                    vpanVolume.HardCopy(file.getAbsolutePath().concat(".tif"), 1);
                }
            } else {
                vpanVolume.HardCopy(file.getAbsolutePath().concat(".tif"), 1);
            }
        }
        //Reset the file chooser for the next time it's shown.

        fc.setSelectedFile(null);
    }//GEN-LAST:event_lblCaptureScreenShotMouseClicked

    private void lblCaptureScreenShotMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblCaptureScreenShotMouseEntered
        // Code for capture the view as an Image...
        lblDisplayPurpose.setText("Capture Screenshot");
    }//GEN-LAST:event_lblCaptureScreenShotMouseEntered

    private void lblCaptureScreenShotMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblCaptureScreenShotMouseExited
        // Code for Remove the Purpose from label
        lblDisplayPurpose.setText(DisplayString);
    }//GEN-LAST:event_lblCaptureScreenShotMouseExited

    private void pnlselectedBackgroundColorMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_pnlselectedBackgroundColorMouseClicked
        // Code for display the Color Pallette
        Color bgColor = JColorChooser.showDialog(this, "Select Color", pnlselectedBackgroundColor.getBackground());
        if (bgColor != null) {
            pnlselectedBackgroundColor.setBackground(bgColor);
            vpanVolume.GetRenderer().SetBackground(Double.valueOf(pnlselectedBackgroundColor.getBackground().getRed()) / 255.0, Double.valueOf(pnlselectedBackgroundColor.getBackground().getGreen()) / 255.0, Double.valueOf(pnlselectedBackgroundColor.getBackground().getBlue()) / 255.0);
            vpanVolume.repaint();
        }
    }//GEN-LAST:event_pnlselectedBackgroundColorMouseClicked

    private void pnlselectedBackgroundColorMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_pnlselectedBackgroundColorMouseEntered
        // TODO add your handling code here:
        lblDisplayPurpose.setText("Change Background Color");
    }//GEN-LAST:event_pnlselectedBackgroundColorMouseEntered

    private void pnlselectedBackgroundColorMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_pnlselectedBackgroundColorMouseExited
        // Code for Remove the Purpose from label
        lblDisplayPurpose.setText(DisplayString);

    }//GEN-LAST:event_pnlselectedBackgroundColorMouseExited

    private void lblZoomInOutMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblZoomInOutMouseClicked
        // Set the interaction for Zoom In and Zoom Out...

        vpanVolume.removeMouseListener(vpanVolume);
        vpanVolume.removeMouseMotionListener(vpanVolume);
        mode = 1;
        vpanVolume.addMouseListener(myMouseListener);
        vpanVolume.addMouseMotionListener(myMouseMotionListener);
    }//GEN-LAST:event_lblZoomInOutMouseClicked

    private void lblZoomInOutMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblZoomInOutMouseEntered
        // Code for Display the Purpose of Zoom In/Out Icon
        lblDisplayPurpose.setText("Zoom In or Zoom Out the Volume");
    }//GEN-LAST:event_lblZoomInOutMouseEntered

    private void lblZoomInOutMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblZoomInOutMouseExited
        // Code for Remove the Purpose from label
        lblDisplayPurpose.setText(DisplayString);
    }//GEN-LAST:event_lblZoomInOutMouseExited

    private void lblResetMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblResetMouseClicked
        // TODO add your handling code here:
        double cx = (ox + (0.5 * (xMax - xMin)) * sx);
        double cy = (oy + (0.5 * (yMax - yMin)) * sy);
        double cz = (oy + (0.5 * (zMax - zMin)) * sz);
        double vx = 0;
        double vy = 0;
        double vz = 0;
        double nx = 0;
        double ny = 0;
        double nz = 0;
        int iaxis = 2;
        if (iaxis == 0) {
            vz = -1;
            nx = (ox + xMax * sx);
            cx = (ox + 1 * sx);
        } else if (iaxis == 1) {
            vz = -1;
            ny = (oy + yMax * sy);
            cy = (oy + 1 * sy);
        } else {
            vy = 1;
            nz = (oz + zMax * sz);
            cz = (oz + 1 * sz);
        }
        double px = (cx + nx * 2);
        double py = (cy + ny * 2);
        double pz = (cz + nz * 3);

        vtkCamera camera = vpanVolume.GetRenderer().GetActiveCamera();
        camera.SetViewUp(vx, vy, vz);
        camera.SetFocalPoint(cx, cy, cz);
        camera.SetPosition(px, py, pz);
        camera.OrthogonalizeViewUp();

        // Reset the light...
        vtkLightCollection Lights = vpanVolume.GetRenderer().GetLights();
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

        vpanVolume.GetRenderer().ResetCameraClippingRange();
        vpanVolume.GetRenderer().ResetCamera();
        vpanVolume.GetRenderer().WorldToView();
        vpanVolume.repaint();
    }//GEN-LAST:event_lblResetMouseClicked

    private void lblResetMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblResetMouseEntered
        // TODO add your handling code here:
        lblDisplayPurpose.setText("Reset the Volume");
    }//GEN-LAST:event_lblResetMouseEntered

    private void lblResetMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblResetMouseExited
        // TODO add your handling code here:
        lblDisplayPurpose.setText(DisplayString);
    }//GEN-LAST:event_lblResetMouseExited

    private void formWindowClosed(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosed
          vtkGlobalJavaHash.DeleteAll();
    }//GEN-LAST:event_formWindowClosed

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                new DicomVolumeRendering().setVisible(true);
            }
        });
    }

    public void readDicomDir(String path) {
        directoryName = path;

        // Display the Volume
        reader = new vtkDICOMImageReader();
        if (new File(directoryName).isDirectory()) {
            reader.SetDirectoryName(directoryName);
        }
        reader.Update();
        reader.ReleaseDataFlagOn();

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


        double[] range = reader.GetOutput().GetScalarRange();
        double min = range[0];
        double max = range[1];
        double diff = max - min;
        double slop = 512.0 / diff;
        double inter = -slop * min;
        double shift = inter / slop;

        vtkmyImageCast.SetInput(reader.GetOutput());
        vtkmyImageCast.SetShift(shift);
        vtkmyImageCast.SetScale(slop);
        vtkmyImageCast.SetOutputScalarTypeToUnsignedChar();
        vtkmyImageCast.Update();
        vtkmyImageCast.GetOutput().GetScalarRange(range);

        DisplayString = reader.GetPatientName();
        reader.Delete();

        displayVolume();


    }

    private void displayVolume() {

        // Create transfer mapping scalar value to opacity        
        vtkPiecewiseFunction opacityTransferFunction = new vtkPiecewiseFunction();

        opacityTransferFunction.AddPoint(30, 0.0);
        opacityTransferFunction.AddPoint(100, 0.3);
        opacityTransferFunction.AddPoint(101, 0.3);
        opacityTransferFunction.AddPoint(175, 0.1);
        opacityTransferFunction.AddPoint(255, 0.1);

        // Create transfer mapping scalar value to color
        vtkColorTransferFunction colorTransferFunction = new vtkColorTransferFunction();

        colorTransferFunction.AddRGBPoint(120.0, 1.0, 0.6, 0.0);
        colorTransferFunction.AddRGBPoint(140.0, 1.0, 0.0, 0.0);
        colorTransferFunction.AddRGBPoint(150.0, 1.0, 0.0, 0.0);
        //colorTransferFunction.AddRGBPoint(175.0, 1.0, 1.0, 1.0);
        colorTransferFunction.AddRGBPoint(175.0, 1.0, 0.0, 0.0);
        colorTransferFunction.AddRGBPoint(255.0, 1.0, 1.0, 1.0);

        // The property describes how the data will look
        vtkVolumeProperty volumeProperty = new vtkVolumeProperty();
        volumeProperty.SetColor(colorTransferFunction);
        volumeProperty.SetScalarOpacity(opacityTransferFunction);
        volumeProperty.ShadeOn();
        volumeProperty.SetInterpolationTypeToLinear();
        volumeProperty.SetAmbient(0.1);
        volumeProperty.SetDiffuse(0.6);
        volumeProperty.SetSpecular(0.7);
        volumeProperty.SetSpecularPower(30);

        // The mapper / ray cast function know how to render the data
        vtkVolumeRayCastCompositeFunction compositeFunction = new vtkVolumeRayCastCompositeFunction();
        vtkVolumeRayCastMapper volumeMapper = new vtkVolumeRayCastMapper();
        volumeMapper.SetVolumeRayCastFunction(compositeFunction);
        volumeMapper.SetInput(vtkmyImageCast.GetOutput());

        // The volume holds the mapper and the property and can be used to position/orient the volume
        volume.SetMapper(volumeMapper);
        volume.SetProperty(volumeProperty);

        vpanVolume.GetRenderer().AddVolume(volume);
        vpanVolume.repaint();

        vpanVolume.GetRenderer().SetBackground(Double.valueOf(pnlselectedBackgroundColor.getBackground().getRed()) / 255.0, Double.valueOf(pnlselectedBackgroundColor.getBackground().getGreen()) / 255.0, Double.valueOf(pnlselectedBackgroundColor.getBackground().getBlue()) / 255.0);
        vpanVolume.GetRenderer().ResetCamera();
        pnlVolumeViewer.add(vpanVolume);
        vpanVolume.repaint();
        pnlVolumeViewer.setVisible(true);



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

            if (doDragFlag > 0) {
                doDragFlag--;
            } else {
                int x = me.getX();
                int y = me.getY();
                vtkCamera cam = vpanVolume.GetRenderer().GetActiveCamera();

                if (mode == 1) {            // Zoom In/Out Volume...
                    if (vpanVolume.GetRenderer().VisibleActorCount() == 0) {
                        return;
                    }
                    double zoomFactor;
                    zoomFactor = Math.pow(1.02, (y - lastY));
                    if (cam.GetParallelProjection() == 1) {
                        cam.SetParallelScale(cam.GetParallelScale() / zoomFactor);
                    } else {
                        cam.Dolly(zoomFactor);
                        vpanVolume.resetCameraClippingRange();
                    }
                } else if (mode == 2) {     // Rotate Volume...

                    if (vpanVolume.GetRenderer().VisibleActorCount() == 0) {
                        return;
                    }
                    if (x > vpanVolume.getWidth() / 2.0) {
                        cam.Roll(-(y - lastY));
                    } else {
                        cam.Roll((y - lastY));
                    }
                    if (y < vpanVolume.getHeight() / 2.0) {
                        cam.Roll(-(x - lastX));
                    } else {
                        cam.Roll((x - lastX));
                    }
                    vpanVolume.resetCameraClippingRange();
                } else if (mode == 3) {     // Move Volume...
                    if (vpanVolume.GetRenderer().VisibleActorCount() == 0) {
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
                    vpanVolume.GetRenderer().SetWorldPoint(FPoint[0], FPoint[1], FPoint[2], 1.0);
                    vpanVolume.GetRenderer().WorldToDisplay();
                    focalDepth = vpanVolume.GetRenderer().GetDisplayPoint()[2];

                    APoint[0] = vpanVolume.GetRenderWindow().GetSize()[0] / 2.0 + (x - lastX);
                    APoint[1] = vpanVolume.GetRenderWindow().GetSize()[1] / 2.0 - (y - lastY);
                    APoint[2] = focalDepth;
                    vpanVolume.GetRenderer().SetDisplayPoint(APoint);
                    vpanVolume.GetRenderer().DisplayToWorld();
                    RPoint = vpanVolume.GetRenderer().GetWorldPoint();
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
                    cam.SetEyeAngle(100);
                    vpanVolume.resetCameraClippingRange();
                }
                lastX = x;
                lastY = y;

                vpanVolume.Render();
                doDragFlag = 5;
            }
        }

        public void mouseMoved(MouseEvent me) {
            lastX = me.getX();
            lastY = me.getY();
        }
    };
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel lblCaptureScreenShot;
    private javax.swing.JLabel lblCubeBox;
    private javax.swing.JLabel lblDisplayPurpose;
    private javax.swing.JLabel lblMove;
    private javax.swing.JLabel lblReset;
    private javax.swing.JLabel lblRotate;
    private javax.swing.JLabel lblZoomInOut;
    private javax.swing.JPanel pnlVolumeViewer;
    private javax.swing.JPanel pnlselectedBackgroundColor;
    // End of variables declaration//GEN-END:variables
}
