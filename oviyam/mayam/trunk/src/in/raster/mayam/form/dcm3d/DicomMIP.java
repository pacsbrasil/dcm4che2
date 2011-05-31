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
import vtk.vtkVolumeRayCastMIPFunction;
import vtk.vtkVolumeRayCastMapper;

/**
 * DicomMIP.java
 *
 * Created on 1 Jul, 2010, 1:06:56 PM
 *
 *  This File Load the Series of Dicom images and
 *  Construct them as a Volume...
 *
 */

/*
 * @author Sathish Kumar V              (sathishkumar.v@raster.in)
 */
public class DicomMIP extends javax.swing.JFrame {

    private vtkDICOMImageReader reader;
    private vtkCanvas vpanMIP = new vtkCanvas();
    private int mode = 0, lastX, lastY, doDragFlag;
    private String directoryName = "", DisplayString = "3D Maximum Intensity Projection (MIP)";
    private vtkImageShiftScale vtkmyImageCast = new vtkImageShiftScale();
    private vtkPiecewiseFunction opacityTransferFunction = new vtkPiecewiseFunction();
    private vtkPiecewiseFunction grayTransferFunction = new vtkPiecewiseFunction();
    private vtkVolumeProperty volumeProperty = new vtkVolumeProperty();
    private vtkVolumeRayCastMIPFunction mip = new vtkVolumeRayCastMIPFunction();
    private vtkVolumeRayCastMapper mapper = new vtkVolumeRayCastMapper();
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

    /* Creates new form DicomMIP */
    public DicomMIP() {
        initComponents();
        vpanMIP.setSize(pnlMIPViewer.getSize());
        vpanMIP.setBackground(Color.white);
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnlMIPViewer = new javax.swing.JPanel();
        lblRotateMIP = new javax.swing.JLabel();
        lblMoveMIP = new javax.swing.JLabel();
        lblCubeBoxMIP = new javax.swing.JLabel();
        lblDisplayPurpose = new javax.swing.JLabel();
        lblCaptureScreenShot = new javax.swing.JLabel();
        pnlselectedBackgroundColor = new javax.swing.JPanel();
        lblZoomInOutMIP = new javax.swing.JLabel();
        lblResetMIP = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("3D Maximum Intensity Projection (MIP)");
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosed(java.awt.event.WindowEvent evt) {
                formWindowClosed(evt);
            }
        });

        pnlMIPViewer.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        javax.swing.GroupLayout pnlMIPViewerLayout = new javax.swing.GroupLayout(pnlMIPViewer);
        pnlMIPViewer.setLayout(pnlMIPViewerLayout);
        pnlMIPViewerLayout.setHorizontalGroup(
            pnlMIPViewerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 1004, Short.MAX_VALUE)
        );
        pnlMIPViewerLayout.setVerticalGroup(
            pnlMIPViewerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 686, Short.MAX_VALUE)
        );

        lblRotateMIP.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblRotateMIP.setIcon(new javax.swing.ImageIcon(getClass().getResource("/in/raster/mayam/form/images/RotateSurface.png"))); // NOI18N
        lblRotateMIP.setToolTipText("Rotate the Volume");
        lblRotateMIP.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);
        lblRotateMIP.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        lblRotateMIP.setIconTextGap(0);
        lblRotateMIP.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        lblRotateMIP.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lblRotateMIPMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                lblRotateMIPMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                lblRotateMIPMouseExited(evt);
            }
        });

        lblMoveMIP.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblMoveMIP.setIcon(new javax.swing.ImageIcon(getClass().getResource("/in/raster/mayam/form/images/Move.png"))); // NOI18N
        lblMoveMIP.setToolTipText("Move the Volume");
        lblMoveMIP.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);
        lblMoveMIP.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lblMoveMIPMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                lblMoveMIPMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                lblMoveMIPMouseExited(evt);
            }
        });

        lblCubeBoxMIP.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblCubeBoxMIP.setIcon(new javax.swing.ImageIcon(getClass().getResource("/in/raster/mayam/form/images/ResetInteractor.png"))); // NOI18N
        lblCubeBoxMIP.setToolTipText("Reset the Mouse Interaction");
        lblCubeBoxMIP.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);
        lblCubeBoxMIP.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lblCubeBoxMIPMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                lblCubeBoxMIPMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                lblCubeBoxMIPMouseExited(evt);
            }
        });

        lblDisplayPurpose.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblDisplayPurpose.setText("3D Maximum Intensity Projection (MIP)");

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

        lblZoomInOutMIP.setIcon(new javax.swing.ImageIcon(getClass().getResource("/in/raster/mayam/form/images/ZoomInOut.png"))); // NOI18N
        lblZoomInOutMIP.setToolTipText("Zoom In or Zoom Out the Volume");
        lblZoomInOutMIP.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lblZoomInOutMIPMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                lblZoomInOutMIPMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                lblZoomInOutMIPMouseExited(evt);
            }
        });

        lblResetMIP.setIcon(new javax.swing.ImageIcon(getClass().getResource("/in/raster/mayam/form/images/DrawCircle.png"))); // NOI18N
        lblResetMIP.setToolTipText("Reset the Volume");
        lblResetMIP.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lblResetMIPMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                lblResetMIPMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                lblResetMIPMouseExited(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(pnlMIPViewer, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(lblZoomInOutMIP)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblRotateMIP)
                        .addGap(9, 9, 9)
                        .addComponent(lblMoveMIP)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblCubeBoxMIP)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblResetMIP, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(57, 57, 57)
                        .addComponent(lblCaptureScreenShot)
                        .addGap(18, 18, 18)
                        .addComponent(pnlselectedBackgroundColor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(231, 231, 231)
                        .addComponent(lblDisplayPurpose, javax.swing.GroupLayout.DEFAULT_SIZE, 501, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(lblMoveMIP, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblResetMIP, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblCubeBoxMIP, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblZoomInOutMIP, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblDisplayPurpose, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblRotateMIP, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblCaptureScreenShot, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pnlselectedBackgroundColor, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(30, 30, 30)
                .addComponent(pnlMIPViewer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void lblRotateMIPMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblRotateMIPMouseEntered
        // Code for Display the Purpose of Rotate Icon
        lblDisplayPurpose.setText("Rotate the Volume");
    }//GEN-LAST:event_lblRotateMIPMouseEntered

    private void lblRotateMIPMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblRotateMIPMouseExited
        // Code for Remove the Purpose from label
        lblDisplayPurpose.setText(DisplayString);
    }//GEN-LAST:event_lblRotateMIPMouseExited

    private void lblMoveMIPMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblMoveMIPMouseEntered
        // Code for Display the Purpose of Move Icon
        lblDisplayPurpose.setText("Move the Volume");
    }//GEN-LAST:event_lblMoveMIPMouseEntered

    private void lblMoveMIPMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblMoveMIPMouseExited
        // Code for Remove the Purpose from label
        lblDisplayPurpose.setText(DisplayString);
    }//GEN-LAST:event_lblMoveMIPMouseExited

    private void lblCubeBoxMIPMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblCubeBoxMIPMouseEntered
        // Code for Display the Purpose of add Cube Box Icon
        lblDisplayPurpose.setText("Reset the Mouse Interaction");
    }//GEN-LAST:event_lblCubeBoxMIPMouseEntered

    private void lblCubeBoxMIPMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblCubeBoxMIPMouseExited
        // Code for Remove the Purpose from label
        lblDisplayPurpose.setText(DisplayString);
    }//GEN-LAST:event_lblCubeBoxMIPMouseExited

    private void lblCubeBoxMIPMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblCubeBoxMIPMouseClicked
        // Set the Default Interactor..

        vpanMIP.removeMouseListener(myMouseListener);
        vpanMIP.removeMouseMotionListener(myMouseMotionListener);

        mode = 0;
        vpanMIP.addMouseListener(vpanMIP);
        vpanMIP.addMouseMotionListener(vpanMIP);
    }//GEN-LAST:event_lblCubeBoxMIPMouseClicked

    private void lblRotateMIPMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblRotateMIPMouseClicked
        //  Set the interaction for Rotate...
        vpanMIP.removeMouseListener(vpanMIP);
        vpanMIP.removeMouseMotionListener(vpanMIP);

        mode = 2;
        vpanMIP.addMouseListener(myMouseListener);
        vpanMIP.addMouseMotionListener(myMouseMotionListener);
    }//GEN-LAST:event_lblRotateMIPMouseClicked

    private void lblMoveMIPMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblMoveMIPMouseClicked
        // Set the interaction for Move...

        vpanMIP.removeMouseListener(vpanMIP);
        vpanMIP.removeMouseMotionListener(vpanMIP);

        mode = 3;
        vpanMIP.addMouseListener(myMouseListener);
        vpanMIP.addMouseMotionListener(myMouseMotionListener);
    }//GEN-LAST:event_lblMoveMIPMouseClicked

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
                    vpanMIP.HardCopy(file.getAbsolutePath(), 1);
                } else {
                    vpanMIP.HardCopy(file.getAbsolutePath().concat(".tif"), 1);
                }
            } else {
                vpanMIP.HardCopy(file.getAbsolutePath().concat(".tif"), 1);
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
            vpanMIP.GetRenderer().SetBackground(Double.valueOf(pnlselectedBackgroundColor.getBackground().getRed()) / 255.0, Double.valueOf(pnlselectedBackgroundColor.getBackground().getGreen()) / 255.0, Double.valueOf(pnlselectedBackgroundColor.getBackground().getBlue()) / 255.0);
            vpanMIP.repaint();
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

    private void lblZoomInOutMIPMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblZoomInOutMIPMouseClicked
        // Set the interaction for Zoom In and Zoom Out...

        vpanMIP.removeMouseListener(vpanMIP);
        vpanMIP.removeMouseMotionListener(vpanMIP);

        mode = 1;
        vpanMIP.addMouseListener(myMouseListener);
        vpanMIP.addMouseMotionListener(myMouseMotionListener);
    }//GEN-LAST:event_lblZoomInOutMIPMouseClicked

    private void lblZoomInOutMIPMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblZoomInOutMIPMouseEntered
        // Code for Display the Purpose of Zoom In/Out Icon
        lblDisplayPurpose.setText("Zoom In or Zoom Out the Volume");
    }//GEN-LAST:event_lblZoomInOutMIPMouseEntered

    private void lblZoomInOutMIPMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblZoomInOutMIPMouseExited
        // Code for Remove the Purpose from label
        lblDisplayPurpose.setText(DisplayString);
    }//GEN-LAST:event_lblZoomInOutMIPMouseExited

    private void lblResetMIPMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblResetMIPMouseClicked
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

        vtkCamera camera = vpanMIP.GetRenderer().GetActiveCamera();
        camera.SetViewUp(vx, vy, vz);
        camera.SetFocalPoint(cx, cy, cz);
        camera.SetPosition(px, py, pz);
        camera.OrthogonalizeViewUp();

        // Reset the light...
        vtkLightCollection Lights = vpanMIP.GetRenderer().GetLights();
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

        vpanMIP.GetRenderer().ResetCameraClippingRange();
        vpanMIP.GetRenderer().ResetCamera();
        vpanMIP.GetRenderer().WorldToView();
        vpanMIP.repaint();
    }//GEN-LAST:event_lblResetMIPMouseClicked

    private void lblResetMIPMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblResetMIPMouseEntered
        // TODO add your handling code here:
        lblDisplayPurpose.setText("Reset the Volume");
    }//GEN-LAST:event_lblResetMIPMouseEntered

    private void lblResetMIPMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblResetMIPMouseExited
        // TODO add your handling code here:
        lblDisplayPurpose.setText(DisplayString);
    }//GEN-LAST:event_lblResetMIPMouseExited

    private void formWindowClosed(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosed
        vtkGlobalJavaHash.DeleteAll();
    }//GEN-LAST:event_formWindowClosed

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                new DicomMIP().setVisible(true);
            }
        });
    }

    public void readDicomDir(String path) {
        startTime = System.currentTimeMillis();
        directoryName = path;
        displayMIP();
    }

    private void displayMIP() {
        // Display the MIP

        reader = new vtkDICOMImageReader();
        if (new File(directoryName).isDirectory()) {
            reader.SetDirectoryName(directoryName);
        } else {
            System.out.println("Please select a directory");
        }
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

        double level = 0.5 * (range[0] + range[1]);
        double window = range[1] - range[0];

        System.out.println("Level : " + level);
        System.out.println("Window : " + window);

        opacityTransferFunction.AddPoint(0, 0.0);
        opacityTransferFunction.AddPoint(75, 0.0);
        opacityTransferFunction.AddPoint(95, 0.0);
        opacityTransferFunction.AddPoint(110, 0.4); //0.4
        opacityTransferFunction.AddPoint(120, 0.6); //0.6
        opacityTransferFunction.AddPoint(145, 1.0);
        opacityTransferFunction.AddPoint(255, 1.0);

        grayTransferFunction.AddSegment(level - window / 2, 0.0, level + window / 2, 1.0);
        grayTransferFunction.AddSegment(0, 0.0, 255, 1.0);

        vtkColorTransferFunction colorTransferFunction = new vtkColorTransferFunction();

        colorTransferFunction.AddRGBPoint(120.0, 0.2, 0.2, 0.2); //0.6
        colorTransferFunction.AddRGBPoint(145.0, 0.3, 0.3, 0.3); //0.4
        colorTransferFunction.AddRGBPoint(255.0, 1.0, 1.0, 1.0); //0.2

        volumeProperty.SetScalarOpacity(opacityTransferFunction);
        volumeProperty.SetColor(colorTransferFunction);
        volumeProperty.SetInterpolationTypeToLinear();
        mapper.SetInput(vtkmyImageCast.GetOutput());
        mapper.SetVolumeRayCastFunction(mip);
        mapper.SetBlendModeToMaximumIntensity();
        mapper.SetSampleDistance(1.0);
        volume.SetMapper(mapper);
        volume.SetProperty(volumeProperty);
        vpanMIP.GetRenderer().AddVolume(volume);

        vpanMIP.GetRenderer().SetBackground(Double.valueOf(pnlselectedBackgroundColor.getBackground().getRed()) / 255.0, Double.valueOf(pnlselectedBackgroundColor.getBackground().getGreen()) / 255.0, Double.valueOf(pnlselectedBackgroundColor.getBackground().getBlue()) / 255.0);
        vpanMIP.GetRenderer().ResetCamera();
        pnlMIPViewer.add(vpanMIP);
        vpanMIP.repaint();
        pnlMIPViewer.setVisible(true);

        System.out.println("Time taken : " + (System.currentTimeMillis() - startTime));
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
                vtkCamera cam = vpanMIP.GetRenderer().GetActiveCamera();

                if (mode == 1) {            // Zoom In/Out MIP...
                    if (vpanMIP.GetRenderer().VisibleActorCount() == 0) {
                        return;
                    }
                    double zoomFactor;
                    zoomFactor = Math.pow(1.02, (y - lastY));
                    if (cam.GetParallelProjection() == 1) {
                        cam.SetParallelScale(cam.GetParallelScale() / zoomFactor);
                    } else {
                        cam.Dolly(zoomFactor);
                        vpanMIP.resetCameraClippingRange();
                    }
                } else if (mode == 2) {     // Rotate MIP...

                    if (vpanMIP.GetRenderer().VisibleActorCount() == 0) {
                        return;
                    }
                    if (x > vpanMIP.getWidth() / 2.0) {
                        cam.Roll(-(y - lastY));
                    } else {
                        cam.Roll((y - lastY));
                    }
                    if (y < vpanMIP.getHeight() / 2.0) {
                        cam.Roll(-(x - lastX));
                    } else {
                        cam.Roll((x - lastX));
                    }
                    vpanMIP.resetCameraClippingRange();
                } else if (mode == 3) {     // Move MIP...
                    if (vpanMIP.GetRenderer().VisibleActorCount() == 0) {
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
                    vpanMIP.GetRenderer().SetWorldPoint(FPoint[0], FPoint[1], FPoint[2], 1.0);
                    vpanMIP.GetRenderer().WorldToDisplay();
                    focalDepth = vpanMIP.GetRenderer().GetDisplayPoint()[2];

                    APoint[0] = vpanMIP.GetRenderWindow().GetSize()[0] / 2.0 + (x - lastX);
                    APoint[1] = vpanMIP.GetRenderWindow().GetSize()[1] / 2.0 - (y - lastY);
                    APoint[2] = focalDepth;
                    vpanMIP.GetRenderer().SetDisplayPoint(APoint);
                    vpanMIP.GetRenderer().DisplayToWorld();
                    RPoint = vpanMIP.GetRenderer().GetWorldPoint();
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
                    vpanMIP.resetCameraClippingRange();
                }
                lastX = x;
                lastY = y;

                vpanMIP.Render();
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
    private javax.swing.JLabel lblCubeBoxMIP;
    private javax.swing.JLabel lblDisplayPurpose;
    private javax.swing.JLabel lblMoveMIP;
    private javax.swing.JLabel lblResetMIP;
    private javax.swing.JLabel lblRotateMIP;
    private javax.swing.JLabel lblZoomInOutMIP;
    private javax.swing.JPanel pnlMIPViewer;
    private javax.swing.JPanel pnlselectedBackgroundColor;
    // End of variables declaration//GEN-END:variables
}
