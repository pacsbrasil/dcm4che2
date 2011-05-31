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

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import javax.swing.JOptionPane;
import vtk.vtkDICOMImageReader;
import vtk.vtkGlobalJavaHash;
import vtk.vtkImageActor;
import vtk.vtkImageMapToColors;
import vtk.vtkImageReslice;
import vtk.vtkInteractorStyleImage;
import vtk.vtkLookupTable;
import vtk.vtkMatrix4x4;
import vtk.vtkPanel;
import vtk.vtkPointPicker;

/**
 *
 * @author Sathish Kumar V              (sathishkumar.v@raster.in)
 */
public class DicomMPR2D extends javax.swing.JFrame {

    vtkPanel vpanAxial = new vtkPanel();
    vtkPanel vpanCoronal = new vtkPanel();
    vtkPanel vpanSagittal = new vtkPanel();
    vtkImageReslice resliceAxial = new vtkImageReslice();
    vtkImageReslice resliceCoronal = new vtkImageReslice();
    vtkImageReslice resliceSagittal = new vtkImageReslice();
    vtkInteractorStyleImage interactorStyle = new vtkInteractorStyleImage();
    vtkImageActor actorAxial = new vtkImageActor();
    vtkImageActor actorCoronal = new vtkImageActor();
    vtkImageActor actorSagittal = new vtkImageActor();
    Point mouseXYAxial, mouseXYCoronal, mouseXYSagittal;
    private vtkDICOMImageReader reader;
    double[] dataExtent;

    static {
        System.loadLibrary("vtkCommonJava");
        System.loadLibrary("vtkFilteringJava");
        System.loadLibrary("vtkIOJava");
        System.loadLibrary("vtkImagingJava");
        System.loadLibrary("vtkGraphicsJava");
        System.loadLibrary("vtkRenderingJava");
    }

    /** Creates new form DicomMPR2D */
    public DicomMPR2D() {
        initComponents();
        try {
            vpanAxial.GetRenderer().SetBackground(0.0, 0.0, 0.0);
            vpanAxial.setSize(pnlAxialView.getSize());
            pnlAxialView.add(vpanAxial);

            vpanCoronal.GetRenderer().SetBackground(0.0, 0.0, 0.0);
            vpanCoronal.setSize(pnlCoronalView.getSize());
            pnlCoronalView.add(vpanCoronal);

            vpanSagittal.GetRenderer().SetBackground(0.0, 0.0, 0.0);
            vpanSagittal.setSize(pnlSagittalView.getSize());
            pnlSagittalView.add(vpanSagittal);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnlAxialView = new javax.swing.JPanel();
        pnlCoronalView = new javax.swing.JPanel();
        pnlSagittalView = new javax.swing.JPanel();
        lblAxialDisplay = new javax.swing.JLabel();
        lblCoronalDisplay = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("2D Orthogonal MPR(Multi Planner Reconstruction)");
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosed(java.awt.event.WindowEvent evt) {
                formWindowClosed(evt);
            }
        });

        pnlAxialView.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        pnlAxialView.setPreferredSize(new java.awt.Dimension(360, 350));

        javax.swing.GroupLayout pnlAxialViewLayout = new javax.swing.GroupLayout(pnlAxialView);
        pnlAxialView.setLayout(pnlAxialViewLayout);
        pnlAxialViewLayout.setHorizontalGroup(
            pnlAxialViewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 563, Short.MAX_VALUE)
        );
        pnlAxialViewLayout.setVerticalGroup(
            pnlAxialViewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 630, Short.MAX_VALUE)
        );

        pnlCoronalView.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        pnlCoronalView.setPreferredSize(new java.awt.Dimension(362, 350));

        javax.swing.GroupLayout pnlCoronalViewLayout = new javax.swing.GroupLayout(pnlCoronalView);
        pnlCoronalView.setLayout(pnlCoronalViewLayout);
        pnlCoronalViewLayout.setHorizontalGroup(
            pnlCoronalViewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 415, Short.MAX_VALUE)
        );
        pnlCoronalViewLayout.setVerticalGroup(
            pnlCoronalViewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 285, Short.MAX_VALUE)
        );

        pnlSagittalView.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        pnlSagittalView.setPreferredSize(new java.awt.Dimension(362, 350));

        javax.swing.GroupLayout pnlSagittalViewLayout = new javax.swing.GroupLayout(pnlSagittalView);
        pnlSagittalView.setLayout(pnlSagittalViewLayout);
        pnlSagittalViewLayout.setHorizontalGroup(
            pnlSagittalViewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 415, Short.MAX_VALUE)
        );
        pnlSagittalViewLayout.setVerticalGroup(
            pnlSagittalViewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 310, Short.MAX_VALUE)
        );

        lblAxialDisplay.setFont(new java.awt.Font("Tahoma", 1, 10));
        lblAxialDisplay.setText("Axial View");

        lblCoronalDisplay.setFont(new java.awt.Font("Tahoma", 1, 10));
        lblCoronalDisplay.setText("Coronal View");

        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 10));
        jLabel1.setText("Sagittal View");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(jLabel1)
                        .addComponent(pnlSagittalView, javax.swing.GroupLayout.DEFAULT_SIZE, 417, Short.MAX_VALUE)
                        .addComponent(pnlCoronalView, javax.swing.GroupLayout.PREFERRED_SIZE, 417, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(lblCoronalDisplay))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pnlAxialView, javax.swing.GroupLayout.PREFERRED_SIZE, 565, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblAxialDisplay))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(96, 96, 96)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(lblCoronalDisplay)
                    .addComponent(lblAxialDisplay, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addComponent(pnlCoronalView, javax.swing.GroupLayout.PREFERRED_SIZE, 287, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(pnlSagittalView, javax.swing.GroupLayout.PREFERRED_SIZE, 312, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(pnlAxialView, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 632, Short.MAX_VALUE))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void formWindowClosed(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosed
        vtkGlobalJavaHash.DeleteAll();
    }//GEN-LAST:event_formWindowClosed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                new DicomMPR2D().setVisible(true);
            }
        });
    }

    public void readDicomDir(String path) {
        reader = new vtkDICOMImageReader();
        reader.SetDirectoryName(path);
        reader.SetDataScalarTypeToUnsignedShort();
        reader.UpdateWholeExtent();
        reader.Update();

        
        int[] extent = reader.GetOutput().GetWholeExtent();
        double[] spacing = reader.GetOutput().GetSpacing();
        double[] origin = reader.GetOutput().GetOrigin();
        dataExtent = reader.GetOutput().GetCenter();


        int xMin = extent[0];
        int xMax = extent[1];
        int yMin = extent[2];
        int yMax = extent[3];
        int zMin = extent[4];
        int zMax = extent[5];

        double xSpacing = spacing[0];
        double ySpacing = spacing[1];
        double zSpacing = spacing[2];

        double x0 = origin[0];
        double y0 = origin[1];
        double z0 = origin[2];

        double[] center = {x0 + xSpacing * 0.5 * (xMin + xMax), y0 + ySpacing * 0.5 * (yMin + yMax), z0 + zSpacing * 0.5 * (zMin + zMax)};

        double[] axialElements = {1.0, 0.0, 0.0, center[0], 0.0, 1.0, 0.0, center[1], 0.0, 0.0, 1.0, center[2], 0.0, 0.0, 0.0, 1.0};

        double[] coronalElements = {1.0, 0.0, 0.0, center[0], 0.0, 0.0, 1.0, center[1], 0.0, -1.0, 0.0, center[2], 0.0, 0.0, 0.0, 1.0};

        double[] sagittalElements = {0.0, 0.0, -1.0, center[0], 1.0, 0.0, 0.0, center[1], 0.0, -1.0, 0.0, center[2], 0.0, 0.0, 0.0, 1.0};

        double[] obiliqueElements = {1.0, 0.0, 0.0, center[0], 0.0, 0.866025, -0.5, center[1], 0.0, 0.5, 0.866025, center[2], 0.0, 0.0, 0.0, 1.0};

        vtkMatrix4x4 axial = new vtkMatrix4x4();
        axial.DeepCopy(axialElements);
        vtkMatrix4x4 coronal = new vtkMatrix4x4();
        coronal.DeepCopy(coronalElements);
        vtkMatrix4x4 sagittal = new vtkMatrix4x4();
        sagittal.DeepCopy(sagittalElements);
        vtkMatrix4x4 obilique = new vtkMatrix4x4();
        obilique.DeepCopy(obiliqueElements);

        resliceAxial.SetInputConnection(reader.GetOutputPort());
        resliceAxial.SetOutputDimensionality(2);
        resliceAxial.SetResliceAxes(axial);
        resliceAxial.SetInterpolationModeToLinear();

        resliceCoronal.SetInputConnection(reader.GetOutputPort());
        resliceCoronal.SetOutputDimensionality(2);
        resliceCoronal.SetResliceAxes(coronal);
        resliceCoronal.SetInterpolationModeToLinear();

        resliceSagittal.SetInputConnection(reader.GetOutputPort());
        resliceSagittal.SetOutputDimensionality(2);
        resliceSagittal.SetResliceAxes(sagittal);
        resliceSagittal.SetInterpolationModeToLinear();

        // Release the memory...
        reader.Delete();

        vtkLookupTable tableAxial = new vtkLookupTable();
        tableAxial.SetRange(25.0, 100.0);
        tableAxial.SetValueRange(0.0, 1.0);
        tableAxial.SetSaturationRange(0.0, 0.0);
        tableAxial.SetRampToLinear();
        tableAxial.Build();

        vtkLookupTable tableCoronal = new vtkLookupTable();
        tableCoronal.SetRange(25.0, 100.0);
        tableCoronal.SetValueRange(0.0, 1.0);
        tableCoronal.SetSaturationRange(0.0, 0.0);
        tableCoronal.SetRampToLinear();
        tableCoronal.Build();

        vtkLookupTable tableSagittal = new vtkLookupTable();
        tableSagittal.SetRange(25.0, 100.0);
        tableSagittal.SetValueRange(0.0, 1.0);
        tableSagittal.SetSaturationRange(0.0, 0.0);
        tableSagittal.SetRampToLinear();
        tableSagittal.Build();

        vtkImageMapToColors colorAxial = new vtkImageMapToColors();
        colorAxial.SetLookupTable(tableAxial);
        colorAxial.SetInputConnection(resliceAxial.GetOutputPort());

        vtkImageMapToColors colorCoronal = new vtkImageMapToColors();
        colorCoronal.SetLookupTable(tableCoronal);
        colorCoronal.SetInputConnection(resliceCoronal.GetOutputPort());

        vtkImageMapToColors colorSagittal = new vtkImageMapToColors();
        colorSagittal.SetLookupTable(tableSagittal);
        colorSagittal.SetInputConnection(resliceSagittal.GetOutputPort());

        actorAxial.SetInput(colorAxial.GetOutput());
        actorCoronal.SetInput(colorCoronal.GetOutput());
        actorSagittal.SetInput(colorSagittal.GetOutput());

        vpanAxial.GetRenderer().AddActor(actorAxial);
        vpanAxial.GetRenderer().ResetCamera();

        vpanCoronal.GetRenderer().AddActor(actorCoronal);
        vpanCoronal.GetRenderer().ResetCamera();

        vpanSagittal.GetRenderer().AddActor(actorSagittal);
        vpanSagittal.GetRenderer().ResetCamera();

        vpanAxial.removeMouseListener(vpanAxial);
        vpanAxial.removeMouseMotionListener(vpanAxial);
        vpanAxial.addMouseListener(new MouseListener() {

            public void mouseClicked(MouseEvent e) {
                double[] axialMapperPosition = new double[4];
                mouseXYAxial = e.getPoint();
                vtkPointPicker pickerAxial = new vtkPointPicker();
                int pickTest = pickerAxial.Pick(mouseXYAxial.x, mouseXYAxial.y, 0.0, vpanAxial.GetRenderer());
                if (pickTest != 0) {
                    axialMapperPosition = pickerAxial.GetMapperPosition();
                    vtkMatrix4x4 matrixCoronalChange = resliceCoronal.GetResliceAxes();
                    matrixCoronalChange.SetElement(1, 3, dataExtent[1] - axialMapperPosition[1]);
                    vpanCoronal.Render();
                    vpanCoronal.resetCamera();

                    vtkMatrix4x4 matrixSagittalChange = resliceSagittal.GetResliceAxes();
                    matrixSagittalChange.SetElement(0, 3, dataExtent[0] + axialMapperPosition[0]);
                    vpanSagittal.Render();
                    vpanSagittal.resetCamera();
                }
            }

            public void mousePressed(MouseEvent e) {
            }

            public void mouseReleased(MouseEvent e) {
            }

            public void mouseEntered(MouseEvent e) {
            }

            public void mouseExited(MouseEvent e) {
            }
        });
        vpanAxial.addMouseMotionListener(new MouseMotionListener() {

            public void mouseDragged(MouseEvent e) {
                double[] axialMapperPosition = new double[4];
                mouseXYAxial = e.getPoint();
                vtkPointPicker pickerAxial = new vtkPointPicker();
                int pickTest = pickerAxial.Pick(mouseXYAxial.x, mouseXYAxial.y, 0.0, vpanAxial.GetRenderer());
                if (pickTest != 0) {
                    axialMapperPosition = pickerAxial.GetMapperPosition();
                    vtkMatrix4x4 matrixCoronalChange = resliceCoronal.GetResliceAxes();
                    matrixCoronalChange.SetElement(1, 3, dataExtent[1] - axialMapperPosition[1]);
                    vpanCoronal.Render();
                    vpanCoronal.resetCamera();

                    vtkMatrix4x4 matrixSagittalChange = resliceSagittal.GetResliceAxes();
                    matrixSagittalChange.SetElement(0, 3, dataExtent[0] + axialMapperPosition[0]);
                    vpanSagittal.Render();
                    vpanSagittal.resetCamera();
                }
            }

            public void mouseMoved(MouseEvent e) {
            }
        });
        vpanCoronal.removeMouseListener(vpanCoronal);
        vpanCoronal.removeMouseMotionListener(vpanCoronal);
        vpanCoronal.addMouseListener(new MouseListener() {

            public void mouseClicked(MouseEvent e) {
                double[] CoronalMapperPosition = new double[4];
                mouseXYCoronal = e.getPoint();
                vtkPointPicker pickerCoronal = new vtkPointPicker();
                int pickTest = pickerCoronal.Pick(mouseXYCoronal.x, mouseXYCoronal.y, 0.0, vpanCoronal.GetRenderer());
                if (pickTest != 0) {
                    CoronalMapperPosition = pickerCoronal.GetMapperPosition();
                    //double[] dataExtent = reader.GetOutput().GetCenter();
                    vtkMatrix4x4 matrixCoronalChange = resliceAxial.GetResliceAxes();
                    matrixCoronalChange.SetElement(2, 3, dataExtent[2] + CoronalMapperPosition[1]);
                    vpanAxial.Render();
                    vpanAxial.resetCamera();

                    vtkMatrix4x4 matrixSagittalChange = resliceSagittal.GetResliceAxes();
                    matrixSagittalChange.SetElement(0, 3, dataExtent[0] + CoronalMapperPosition[0]);
                    vpanSagittal.Render();
                    vpanSagittal.resetCamera();
                }
            }

            public void mousePressed(MouseEvent e) {
            }

            public void mouseReleased(MouseEvent e) {
            }

            public void mouseEntered(MouseEvent e) {
            }

            public void mouseExited(MouseEvent e) {
            }
        });
        vpanCoronal.addMouseMotionListener(new MouseMotionListener() {

            public void mouseDragged(MouseEvent e) {
                double[] CoronalMapperPosition = new double[4];
                mouseXYCoronal = e.getPoint();
                vtkPointPicker pickerCoronal = new vtkPointPicker();
                int pickTest = pickerCoronal.Pick(mouseXYCoronal.x, mouseXYCoronal.y, 0.0, vpanCoronal.GetRenderer());
                if (pickTest != 0) {
                    CoronalMapperPosition = pickerCoronal.GetMapperPosition();
                    vtkMatrix4x4 matrixCoronalChange = resliceAxial.GetResliceAxes();
                    matrixCoronalChange.SetElement(2, 3, dataExtent[2] + CoronalMapperPosition[1]);
                    vpanAxial.Render();
                    vpanAxial.resetCamera();

                    vtkMatrix4x4 matrixSagittalChange = resliceSagittal.GetResliceAxes();
                    matrixSagittalChange.SetElement(0, 3, dataExtent[0] + CoronalMapperPosition[0]);
                    vpanSagittal.Render();
                    vpanSagittal.resetCamera();
                }
            }

            public void mouseMoved(MouseEvent e) {
            }
        });
        vpanSagittal.removeMouseListener(vpanSagittal);
        vpanSagittal.removeMouseMotionListener(vpanSagittal);
        vpanSagittal.addMouseListener(new MouseListener() {

            public void mouseClicked(MouseEvent e) {
                double[] sagittalMapperPosition = new double[4];
                mouseXYSagittal = e.getPoint();
                vtkPointPicker pickerSagittal = new vtkPointPicker();
                int pickTest = pickerSagittal.Pick(mouseXYSagittal.x, mouseXYSagittal.y, 0.0, vpanSagittal.GetRenderer());
                if (pickTest != 0) {
                    sagittalMapperPosition = pickerSagittal.GetMapperPosition();
                    vtkMatrix4x4 matrixAxialChange = resliceAxial.GetResliceAxes();
                    matrixAxialChange.SetElement(2, 3, dataExtent[2] + sagittalMapperPosition[1]);
                    vpanAxial.Render();
                    vpanAxial.resetCamera();
                    vtkMatrix4x4 matrixCoronalChange = resliceCoronal.GetResliceAxes();
                    matrixCoronalChange.SetElement(1, 3, dataExtent[1] + sagittalMapperPosition[0]);
                    vpanCoronal.Render();
                    vpanCoronal.resetCamera();
                }
            }

            public void mousePressed(MouseEvent e) {
            }

            public void mouseReleased(MouseEvent e) {
            }

            public void mouseEntered(MouseEvent e) {
            }

            public void mouseExited(MouseEvent e) {
            }
        });
        vpanSagittal.addMouseMotionListener(new MouseMotionListener() {

            public void mouseDragged(MouseEvent e) {
                double[] sagittalMapperPosition = new double[4];
                mouseXYSagittal = e.getPoint();
                vtkPointPicker pickerSagittal = new vtkPointPicker();
                int pickTest = pickerSagittal.Pick(mouseXYSagittal.x, mouseXYSagittal.y, 0.0, vpanSagittal.GetRenderer());
                if (pickTest != 0) {
                    sagittalMapperPosition = pickerSagittal.GetMapperPosition();
                    //double[] dataExtent = reader.GetOutput().GetCenter();
                    vtkMatrix4x4 matrixAxialChange = resliceAxial.GetResliceAxes();
                    matrixAxialChange.SetElement(2, 3, dataExtent[2] + sagittalMapperPosition[1]);
                    vpanAxial.Render();
                    vpanAxial.resetCamera();
                    vtkMatrix4x4 matrixCoronalChange = resliceCoronal.GetResliceAxes();
                    matrixCoronalChange.SetElement(1, 3, dataExtent[1] + sagittalMapperPosition[0]);
                    vpanCoronal.Render();
                    vpanCoronal.resetCamera();
                }
            }

            public void mouseMoved(MouseEvent e) {
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel lblAxialDisplay;
    private javax.swing.JLabel lblCoronalDisplay;
    private javax.swing.JPanel pnlAxialView;
    private javax.swing.JPanel pnlCoronalView;
    private javax.swing.JPanel pnlSagittalView;
    // End of variables declaration//GEN-END:variables
}
