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
package in.raster.mayam.form;

import in.raster.mayam.form.shapes.ShapeCoordinates;
import in.raster.mayam.models.Annotation;
import in.raster.mayam.models.AnnotationObj;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Iterator;

/**
 *
 * @author BabuHussain
 * @version 0.5
 *
 */
public class AnnotationPanel extends javax.swing.JPanel implements MouseMotionListener, MouseWheelListener, MouseListener {

    LayeredCanvas layeredCanvas;
    private int mouseLocX1;
    private int mouseLocY1;
    private int mouseLocX2;
    private int mouseLocY2;
    private boolean startAnnotation = true;
    private boolean showAnnotation = true;
    private static boolean addLine = false;
    private static boolean addRect = false;
    private static boolean addEllipse = false;
    private static boolean addArrow = false;
    private boolean deleteMeasurement = false;
    private boolean resizeMeasurement = false;
    private static boolean moveMeasurement = false;
    private Shape selectedShape = null;
    private String selectedShapeType = "";
    private String selectedShapeDisplayStringValue = "";
    private String selectedShapeMean = "";
    private String selectedShapeStandardDevi = "";
    private double mean, standardDev;
    private Rectangle boundingRect = null;
    private ArrayList<AnnotationObj> lineObj;
    private ArrayList<AnnotationObj> arrowObj;
    private ArrayList<AnnotationObj> rectObj;
    private ArrayList<AnnotationObj> ellipseObj;
    private Annotation annotation;
    public static String tool = "";
    private boolean isMoveMeasurement = false;
    Shape handle1 = null, handle2 = null, handle3 = null, handle4 = null, handle5 = null, handle6 = null, handle7 = null, handle8 = null;
    Ellipse2D.Double lineHandle1, lineHandle2;

    /**
     * Creates new form DateFormatPanel
     */
    public AnnotationPanel(LayeredCanvas l) {
        initComponents();
        setOpaque(false);
        layeredCanvas = l;
        addMouseListener(this);
        addMouseMotionListener(this);
        addMouseWheelListener(this);
        lineObj = new ArrayList<AnnotationObj>();
        arrowObj = new ArrayList<AnnotationObj>();
        rectObj = new ArrayList<AnnotationObj>();
        ellipseObj = new ArrayList<AnnotationObj>();
    }

    public void doPan() {
        if (tool.equalsIgnoreCase("panning")) {
            tool = "";
        } else {
            tool = "panning";
        }
    }

    public int getMouseLocX1() {
        return mouseLocX1;
    }

    public void setMouseLocX1(int mouseLocX1) {
        this.mouseLocX1 = mouseLocX1;
    }

    public int getMouseLocX2() {
        return mouseLocX2;
    }

    public void setMouseLocX2(int mouseLocX2) {
        this.mouseLocX2 = mouseLocX2;
    }

    public int getMouseLocY1() {
        return mouseLocY1;
    }

    public void setMouseLocY1(int mouseLocY1) {
        this.mouseLocY1 = mouseLocY1;
    }

    public int getMouseLocY2() {
        return mouseLocY2;
    }

    public void setMouseLocY2(int mouseLocY2) {
        this.mouseLocY2 = mouseLocY2;
    }

    private void setMean(double mean) {
        this.mean = mean;
    }

    private void setStandardDev(double standardDevi) {
        this.standardDev = standardDevi;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                formMouseClicked(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 436, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 322, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void formMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_formMouseClicked
        layeredCanvas.canvas.requestFocus();
        if (addLine || addRect || addEllipse) {
        } else if (deleteMeasurement) {
            measurementDelete(evt);
        } else if (resizeMeasurement) {
            measurementResize(evt);
        }
    }//GEN-LAST:event_formMouseClicked
    private void measurementDelete(java.awt.event.MouseEvent evt) {
        deleteRectangle(evt);
        deleteEllipse(evt);
        deleteLine(evt);
        deleteArrow(evt);
        if (ellipseObj.isEmpty() && lineObj.isEmpty() && arrowObj.isEmpty() && rectObj.isEmpty()) {
            layeredCanvas.imgpanel.removeAllAnnotations();
        }
    }

    /**
     * This routine used to deleted the rectangle selected.
     *
     * @param evt
     */
    private void deleteRectangle(java.awt.event.MouseEvent evt) {
        Iterator<AnnotationObj> ite1 = rectObj.iterator();
        while (ite1.hasNext()) {
            AnnotationObj t = ite1.next();
            ShapeCoordinates shapeCoordinates = new ShapeCoordinates(t.getX1(), t.getY1(), t.getX2(), t.getY2());
            Shape test2 = new Rectangle2D.Float(shapeCoordinates.getX(), shapeCoordinates.getY(), shapeCoordinates.getWidth(), shapeCoordinates.getHeight());
            if (test2.contains((evt.getX() - layeredCanvas.imgpanel.getOriginX()) / layeredCanvas.imgpanel.getScaleFactor(), (evt.getY() - layeredCanvas.imgpanel.getOriginY()) / layeredCanvas.imgpanel.getScaleFactor())) {
                rectObj.remove(t);
                repaint();
                break;
            }
        }
    }

    /**
     * This routine used to deleted line selected.
     *
     * @param evt
     */
    private void deleteLine(java.awt.event.MouseEvent evt) {
        Iterator<AnnotationObj> ite = lineObj.iterator();
        while (ite.hasNext()) {
            AnnotationObj t = ite.next();
            int mousePointBoxSize = 2;
            Shape lineObject = new Line2D.Float(t.getX1(), t.getY1(), t.getX2(), t.getY2());
            int pointX = (int) Math.round((evt.getX() - layeredCanvas.imgpanel.getOriginX()) / layeredCanvas.imgpanel.getScaleFactor());
            int pointY = (int) Math.round((evt.getY() - layeredCanvas.imgpanel.getOriginY()) / layeredCanvas.imgpanel.getScaleFactor());
            int width = mousePointBoxSize;
            int height = mousePointBoxSize;
            if (lineObject.intersects(pointX, pointY, width, height)) {
                lineObj.remove(t);
                repaint();
                break;
            }

        }
    }

    /**
     * This routine used to deleted Arrow selected.
     *
     * @param evt
     */
    private void deleteArrow(java.awt.event.MouseEvent evt) {
        Iterator<AnnotationObj> ite = arrowObj.iterator();
        while (ite.hasNext()) {
            AnnotationObj t = ite.next();
            int mousePointBoxSize = 2;
            Shape arrowObject = new Line2D.Float(t.getX1(), t.getY1(), t.getX2(), t.getY2());
            int pointX = (int) Math.round((evt.getX() - layeredCanvas.imgpanel.getOriginX()) / layeredCanvas.imgpanel.getScaleFactor());
            int pointY = (int) Math.round((evt.getY() - layeredCanvas.imgpanel.getOriginY()) / layeredCanvas.imgpanel.getScaleFactor());
            int width = mousePointBoxSize;
            int height = mousePointBoxSize;
            if (arrowObject.intersects(pointX, pointY, width, height)) {
                arrowObj.remove(t);
                repaint();
                break;
            }
        }
    }

    /**
     * This routine used to delete the eclipse.
     *
     * @param evt
     */
    private void deleteEllipse(java.awt.event.MouseEvent evt) {
        Iterator<AnnotationObj> ite2 = ellipseObj.iterator();
        while (ite2.hasNext()) {
            AnnotationObj t = ite2.next();
            ShapeCoordinates shapeCoordinates = new ShapeCoordinates(t.getX1(), t.getY1(), t.getX2(), t.getY2());
            Shape test2 = new Ellipse2D.Float(shapeCoordinates.getX(), shapeCoordinates.getY(), shapeCoordinates.getWidth(), shapeCoordinates.getHeight());
            if (test2.contains((evt.getX() - layeredCanvas.imgpanel.getOriginX()) / layeredCanvas.imgpanel.getScaleFactor(), (evt.getY() - layeredCanvas.imgpanel.getOriginY()) / layeredCanvas.imgpanel.getScaleFactor())) {
                ellipseObj.remove(t);
                repaint();
                break;
            }
        }
    }

    /**
     * This routine used to resize the measurement.
     *
     * @param evt
     */
    private void measurementResize(java.awt.event.MouseEvent evt) {
        Iterator<AnnotationObj> ite = lineObj.iterator();
        while (ite.hasNext()) {
            AnnotationObj t = ite.next();
            Shape test = new Line2D.Float(t.getX1(), t.getY1(), t.getX2(), t.getY2());
            if (test.contains(evt.getX(), evt.getY())) {
            }
        }
        Iterator<AnnotationObj> ite1 = rectObj.iterator();
        while (ite1.hasNext()) {
            AnnotationObj t = ite1.next();
            Shape test2 = new Rectangle2D.Float(t.getX1(), t.getY1(), t.getX2() - t.getX1(), t.getY2() - t.getY1());
            if (test2.contains(evt.getX(), evt.getY())) {
                rectObj.remove(t);
                break;
            }
        }
    }

    /**
     * This routine used to move the measurement.
     *
     * @param evt
     */
    private void measurementMove(java.awt.event.MouseEvent evt) {
        addShapeToArray();
        moveLine(evt);
        moveArrow(evt);
        moveRectangle(evt);
        moveEllipse(evt);
    }
    private String selectedShapeOrientation = "left";
    private String selectedArrowOrientString = "";

    /**
     * This routine used to move the line.
     *
     * @param evt
     */
    private void moveLine(java.awt.event.MouseEvent evt) {
        Iterator<AnnotationObj> ite = lineObj.iterator();
        while (ite.hasNext()) {
            AnnotationObj t = ite.next();
            int mousePointBoxSize = 2;
            Shape lineObject = new Line2D.Float(t.getX1(), t.getY1(), t.getX2(), t.getY2());
            int pointX = (int) Math.round((evt.getX() - layeredCanvas.imgpanel.getOriginX()) / layeredCanvas.imgpanel.getScaleFactor());
            int pointY = (int) Math.round((evt.getY() - layeredCanvas.imgpanel.getOriginY()) / layeredCanvas.imgpanel.getScaleFactor());
            int width = mousePointBoxSize;
            int height = mousePointBoxSize;
            if (lineObject.intersects(pointX, pointY, width, height)) {
//                isMoveMeasurement = true;
                int diffX = t.getX2() - t.getX1();
                int diffY = t.getY2() - t.getY1();
                if ((diffX < 0 && diffY < 0) || (diffX > 0 && diffY > 0) || (diffX == 0 && diffY > 0) || (diffY == 0 && diffX > 0)) {
                    selectedShapeOrientation = "left";
                } else {
                    selectedShapeOrientation = "right";
                }
                selectedShape = new Rectangle2D.Float(t.getX1(), t.getY1(), t.getX2() - t.getX1(), t.getY2() - t.getY1());
                boundingRect = selectedShape.getBounds();
                selectedShapeType = "line";
                selectedShapeDisplayStringValue = t.getLength();
                lineObj.remove(t);
                break;
            }
        }
    }

    /**
     * This routine used to move the Arrow.
     *
     * @param evt
     */
    private void moveArrow(java.awt.event.MouseEvent evt) {
        Iterator<AnnotationObj> ite = arrowObj.iterator();
        while (ite.hasNext()) {
            AnnotationObj t = ite.next();
            int mousePointBoxSize = 2;
            Shape lineObject = new Line2D.Float(t.getX1(), t.getY1(), t.getX2(), t.getY2());
            int pointX = (int) Math.round((evt.getX() - layeredCanvas.imgpanel.getOriginX()) / layeredCanvas.imgpanel.getScaleFactor());
            int pointY = (int) Math.round((evt.getY() - layeredCanvas.imgpanel.getOriginY()) / layeredCanvas.imgpanel.getScaleFactor());
            int width = mousePointBoxSize;
            int height = mousePointBoxSize;
            if (lineObject.intersects(pointX, pointY, width, height)) {
                int diffX = t.getX2() - t.getX1();
                int diffY = t.getY2() - t.getY1();
                if ((diffX < 0 && diffY < 0) || (diffX > 0 && diffY > 0) || (diffX == 0 && diffY > 0) || (diffY == 0 && diffX > 0)) {
                    selectedShapeOrientation = "left";
                } else {
                    selectedShapeOrientation = "right";
                }
                boundingRect = lineObject.getBounds();
                selectedShape = lineObject;
                selectedShapeType = "arrow";
                selectedShapeDisplayStringValue = t.getLength();
                selectedArrowOrientString = setArrowFlipText(t.getX1(), t.getY1(), t.getX2(), t.getY2(), selectedShapeOrientation);
                t.setText(selectedArrowOrientString);
                arrowObj.remove(t);
                break;
            }
        }
    }

    /**
     * This routine used to move the rectangle.
     *
     * @param evt
     */
    private void moveRectangle(java.awt.event.MouseEvent evt) {
        Iterator<AnnotationObj> ite1 = rectObj.iterator();
        while (ite1.hasNext()) {
            AnnotationObj t = ite1.next();
            ShapeCoordinates shapeCoordinates = new ShapeCoordinates(t.getX1(), t.getY1(), t.getX2(), t.getY2());
            Shape test2 = new Rectangle2D.Float(shapeCoordinates.getX(), shapeCoordinates.getY(), shapeCoordinates.getWidth(), shapeCoordinates.getHeight());
            if (test2.contains((evt.getX() - layeredCanvas.imgpanel.getOriginX()) / layeredCanvas.imgpanel.getScaleFactor(), (evt.getY() - layeredCanvas.imgpanel.getOriginY()) / layeredCanvas.imgpanel.getScaleFactor())) {
                boundingRect = test2.getBounds();
                selectedShape = test2;
                selectedShapeType = "rect";
                selectedShapeDisplayStringValue = t.getArea();
                calculateMeanDeviation((int) ((evt.getX() - layeredCanvas.imgpanel.getOriginX()) / layeredCanvas.imgpanel.getScaleFactor()),
                        (int) ((evt.getY() - layeredCanvas.imgpanel.getOriginY()) / layeredCanvas.imgpanel.getScaleFactor()),
                        (int) ((evt.getX() - layeredCanvas.imgpanel.getOriginX()) / layeredCanvas.imgpanel.getScaleFactor()) + test2.getBounds().width,
                        (int) ((evt.getY() - layeredCanvas.imgpanel.getOriginY()) / layeredCanvas.imgpanel.getScaleFactor()) + test2.getBounds().height);
                NumberFormat nf = NumberFormat.getInstance();
                nf.setMaximumFractionDigits(3);
                if (layeredCanvas.imgpanel.getPixelSpacingY() == 0 && layeredCanvas.imgpanel.getPixelSpacingX() == 0) {
                    selectedShapeMean = nf.format(mean); //+ " pix";
                    selectedShapeStandardDevi = nf.format(standardDev); //+ " pix";
                } else {
                    selectedShapeMean = nf.format(mean);// + " HU";                    
                    selectedShapeStandardDevi = nf.format(standardDev);// + " HU";
                }
                rectObj.remove(t);
                break;
            }
        }
    }

    /**
     * This routine used to move eclipse.
     *
     * @param evt
     */
    private void moveEllipse(java.awt.event.MouseEvent evt) {
        Iterator<AnnotationObj> ite2 = ellipseObj.iterator();
        while (ite2.hasNext()) {
            AnnotationObj t = ite2.next();
            ShapeCoordinates shapeCoordinates = new ShapeCoordinates(t.getX1(), t.getY1(), t.getX2(), t.getY2());
            Shape test2 = new Ellipse2D.Float(shapeCoordinates.getX(), shapeCoordinates.getY(), shapeCoordinates.getWidth(), shapeCoordinates.getHeight());
            if (test2.contains((evt.getX() - layeredCanvas.imgpanel.getOriginX()) / layeredCanvas.imgpanel.getScaleFactor(), (evt.getY() - layeredCanvas.imgpanel.getOriginY()) / layeredCanvas.imgpanel.getScaleFactor())) {
                boundingRect = test2.getBounds();
                selectedShape = test2;
                selectedShapeType = "ellipse";
                selectedShapeDisplayStringValue = t.getArea();
                calculateMeanDeviation((int) ((evt.getX() - layeredCanvas.imgpanel.getOriginX()) / layeredCanvas.imgpanel.getScaleFactor()),
                        (int) ((evt.getY() - layeredCanvas.imgpanel.getOriginY()) / layeredCanvas.imgpanel.getScaleFactor()),
                        (int) ((evt.getX() - layeredCanvas.imgpanel.getOriginX()) / layeredCanvas.imgpanel.getScaleFactor()) + test2.getBounds().width,
                        (int) ((evt.getY() - layeredCanvas.imgpanel.getOriginY()) / layeredCanvas.imgpanel.getScaleFactor()) + test2.getBounds().height);
                NumberFormat nf = NumberFormat.getInstance();
                nf.setMaximumFractionDigits(3);
                if (layeredCanvas.imgpanel.getPixelSpacingY() == 0 && layeredCanvas.imgpanel.getPixelSpacingX() == 0) {
                    selectedShapeMean = nf.format(mean); //+ " pix";
                    selectedShapeStandardDevi = nf.format(standardDev); //+ " pix";
                } else {
                    selectedShapeMean = nf.format(mean);// + " HU";                    
                    selectedShapeStandardDevi = nf.format(standardDev);// + " HU";
                }
                ellipseObj.remove(t);
                break;
            }
        }
    }

    /**
     * This routine used to add the shape coordinates to the array.
     */
    private void addShapeToArray() {
        //Adding the current selected shape to the array
        if (selectedShape != null) {
            if (selectedShapeType.equalsIgnoreCase("line")) {
                AnnotationObj newLine = new AnnotationObj();
                if (selectedShapeOrientation.equalsIgnoreCase("left")) {
                    newLine.setLocation(selectedShape.getBounds().x, selectedShape.getBounds().y, selectedShape.getBounds().x + selectedShape.getBounds().width, selectedShape.getBounds().y + selectedShape.getBounds().height);
                } else {
                    newLine.setLocation(selectedShape.getBounds().x + selectedShape.getBounds().width, selectedShape.getBounds().y, selectedShape.getBounds().x, selectedShape.getBounds().y + selectedShape.getBounds().height);
                }
                newLine.setType("line");
                newLine.setLength(selectedShapeDisplayStringValue);
                lineObj.add(newLine);
                selectedShape = null;
                boundingRect = null;
            } else if (selectedShapeType.equalsIgnoreCase("arrow")) {
                AnnotationObj newArrow = new AnnotationObj();
                int x1, y1, x2, y2;
                if (selectedShapeOrientation.equalsIgnoreCase("left")) {
                    x1 = selectedShape.getBounds().x;
                    y1 = selectedShape.getBounds().y;
                    x2 = selectedShape.getBounds().x + selectedShape.getBounds().width;
                    y2 = selectedShape.getBounds().y + selectedShape.getBounds().height;
                    if (selectedArrowOrientString.equalsIgnoreCase("flip")) {
                        newArrow.setLocation(x2, y2, x1, y1);
                    } else {
                        newArrow.setLocation(x1, y1, x2, y2);
                    }
                } else {
                    x1 = selectedShape.getBounds().x + selectedShape.getBounds().width;
                    y1 = selectedShape.getBounds().y;
                    x2 = selectedShape.getBounds().x;
                    y2 = selectedShape.getBounds().y + selectedShape.getBounds().height;
                    if (selectedArrowOrientString.equalsIgnoreCase("flip")) {
                        newArrow.setLocation(x2, y2, x1, y1);
                    } else {
                        newArrow.setLocation(x1, y1, x2, y2);
                    }
                }
                newArrow.setType("arrow");
                newArrow.setLength(selectedShapeDisplayStringValue);
                arrowObj.add(newArrow);
                selectedShape = null;
                boundingRect = null;
            } else if (selectedShapeType.equalsIgnoreCase("rect")) {
                AnnotationObj newRect = new AnnotationObj();
                newRect.setLocation(selectedShape.getBounds().x, selectedShape.getBounds().y, selectedShape.getBounds().x + selectedShape.getBounds().width, selectedShape.getBounds().y + selectedShape.getBounds().height);
                newRect.setType("rect");
                newRect.setArea(selectedShapeDisplayStringValue);
                newRect.setMean(selectedShapeMean);
                newRect.setStdDev(selectedShapeStandardDevi);
                rectObj.add(newRect);
                selectedShape = null;
                boundingRect = null;
            } else {
                AnnotationObj newEllipse = new AnnotationObj();
                newEllipse.setLocation(selectedShape.getBounds().x, selectedShape.getBounds().y, selectedShape.getBounds().x + selectedShape.getBounds().width, selectedShape.getBounds().y + selectedShape.getBounds().height);
                newEllipse.setType("ellipse");
                newEllipse.setArea(selectedShapeDisplayStringValue);
                newEllipse.setMean(selectedShapeMean);
                newEllipse.setStdDev(selectedShapeStandardDevi);
                ellipseObj.add(newEllipse);
                selectedShape = null;
                boundingRect = null;
            }
        }
    }

    /**
     * This routine used to delete the measurement.
     */
    public boolean doDeleteMeasurement() {
        if (deleteMeasurement) {
            deleteMeasurement = false;
        } else {
            deleteMeasurement = true;
            resizeMeasurement = false;
            resetSelectionMeasurement();
            addEllipse = false;
            addLine = false;
            addRect = false;
            addArrow = false;
        }
        return deleteMeasurement;
    }

    public boolean isDeleteMeasurement() {
        return deleteMeasurement;
    }

    public void setDeleteMeasurement(boolean deleteMeasurement) {
        this.deleteMeasurement = deleteMeasurement;
    }

    /**
     * This routine used to reset the selected measurement.
     */
    private void resetSelectionMeasurement() {
        if (moveMeasurement) {
            addShapeToArray();
            moveMeasurement = false;
        }
    }

    /**
     * This routine used to reset the edited measurement.
     */
    public void resetEditMeasurement() {
        deleteMeasurement = false;
        resizeMeasurement = false;
        resetSelectionMeasurement();
    }

    /**
     * This routine used to resize the measurement.
     */
    public void doResizeMeasurement() {
        if (resizeMeasurement) {
            resizeMeasurement = false;
        } else {
            resizeMeasurement = true;
            addEllipse = false;
            addLine = false;
            addRect = false;
            addArrow = false;
            deleteMeasurement = false;
            moveMeasurement = false;
        }
    }

    /**
     * This routine used to move the measurement.
     */
    public boolean doMoveMeasurement() {
        if (moveMeasurement) {
            moveMeasurement = false;
        } else {
            moveMeasurement = true;
            addEllipse = false;
            addLine = false;
            addRect = false;
            addArrow = false;
            deleteMeasurement = false;
            resizeMeasurement = false;
        }
        return moveMeasurement;
    }

    public static boolean isMoveMeasurement() {
        return moveMeasurement;
    }

    public static void setMoveMeasurement(boolean moveMeasurement) {
        moveMeasurement = moveMeasurement;
    }

    public void resizeHandler() {
        repaint();
    }

    /**
     * This routine used to find out the mode whether a ellipse can be added to
     * the overlay.
     *
     * @return
     */
    public boolean isAddEllipse() {
        return addEllipse;
    }

    /**
     * This routine used to set the annotation overlay mode to draw the ellipse
     * based on the coordinates.
     *
     * @param addEllipse
     */
    public void setAddEllipse(boolean addEllipse) {
        this.addEllipse = addEllipse;
        resetEditMeasurement();
    }

    /**
     * This routine used to find out the mode whether a line can be added to the
     * overlay.
     *
     * @return
     */
    public boolean isAddLine() {
        return addLine;
    }

    /**
     * This routine used to find out the mode whether a line can be added to the
     * overlay.
     *
     * @return
     */
    public boolean isAddArrow() {
        return addArrow;
    }

    /**
     * This routine used to set the annotation overlay mode to draw the line
     * based on the coordinates.
     *
     * @param addLine
     */
    public void setAddLine(boolean addLine) {
        this.addLine = addLine;
        resetEditMeasurement();
    }

    /**
     * This routine used to set the annotation overlay mode to draw the line
     * based on the coordinates.
     *
     * @param addLine
     */
    public void setAddArrow(boolean addArrow) {
        this.addArrow = addArrow;
        resetEditMeasurement();
    }

    public boolean isAddRect() {
        return addRect;
    }

    public void setAddRect(boolean addRect) {
        this.addRect = addRect;
        resetEditMeasurement();
    }

    /**
     * This routine used to rotate all the annotation to 90 degree right
     */
    public void doRotateRight() {
        addShapeToArray();
        rotateLineRight();
        rotateArrowRight();
        rotateRectangleRight();
        rotateEllipseRight();
        resetMousePoints();
        repaint();

    }

    public void resetAnnotation() {
        if (layeredCanvas.imgpanel.isRotate) {
            if (layeredCanvas.imgpanel.rotateRightAngle == 90) {
                doRotateLeft();
            } else if (layeredCanvas.imgpanel.rotateRightAngle == 180) {
                doRotateLeft();
                doRotateLeft();
            } else if (layeredCanvas.imgpanel.rotateRightAngle == 270) {
                doRotateRight();
            }
        }
        if (layeredCanvas.imgpanel.flipHorizontalFlag) {
            doFlipHorizontal();
        }
        if (layeredCanvas.imgpanel.flipVerticalFlag) {
            doFlipVertical();
        }

    }

    public void setCurrentAnnotation() {
        if (layeredCanvas.imgpanel.isRotate) {
            if (layeredCanvas.imgpanel.rotateRightAngle == 90) {
                doRotateRight();
            } else if (layeredCanvas.imgpanel.rotateRightAngle == 180) {
                doRotateRight();
                doRotateRight();
            } else if (layeredCanvas.imgpanel.rotateRightAngle == 270) {
                doRotateLeft();
            }
        }
        if (layeredCanvas.imgpanel.flipHorizontalFlag) {
            doFlipHorizontal();
        }
        if (layeredCanvas.imgpanel.flipVerticalFlag) {
            doFlipVertical();
        }
    }

    /**
     * This routine used to reset the mouse points stored in the annotation
     * overlay.
     */
    private void resetMousePoints() {
        mouseLocX1 = 0;
        mouseLocX2 = 0;
        mouseLocY1 = 0;
        mouseLocY2 = 0;
    }

    /**
     * This routine used to rotate all the line to 90 degree right
     */
    private void rotateLineRight() {
        Iterator<AnnotationObj> ite = lineObj.iterator();
        while (ite.hasNext()) {
            AnnotationObj t = ite.next();
            int x1 = -1 * t.getCenterBasedY1();
            int y1 = 1 * t.getCenterBasedX1();
            int x2 = -1 * t.getCenterBasedY2();
            int y2 = 1 * t.getCenterBasedX2();
            t.setCenterBasedLocation(x1, y1, x2, y2);
        }
    }

    /**
     * This routine used to rotate all the arrow to 90 degree right
     */
    private void rotateArrowRight() {
        Iterator<AnnotationObj> ite = arrowObj.iterator();
        while (ite.hasNext()) {
            AnnotationObj t = ite.next();
            int x1 = -1 * t.getCenterBasedY1();
            int y1 = 1 * t.getCenterBasedX1();
            int x2 = -1 * t.getCenterBasedY2();
            int y2 = 1 * t.getCenterBasedX2();
            t.setCenterBasedLocation(x1, y1, x2, y2);
        }
    }

    /**
     * This routine used to rotate the rectangle to 90 degree right
     */
    private void rotateRectangleRight() {
        Iterator<AnnotationObj> ite1 = rectObj.iterator();
        while (ite1.hasNext()) {
            AnnotationObj t = ite1.next();
            int x1 = -1 * t.getCenterBasedY1();
            int y1 = 1 * t.getCenterBasedX1();
            int x2 = -1 * t.getCenterBasedY2();
            int y2 = 1 * t.getCenterBasedX2();
            t.setCenterBasedLocation(x1, y1, x2, y2);
        }
    }

    /**
     * This routine used to rotate all the ellipse to 90 degree right
     */
    private void rotateEllipseRight() {
        Iterator<AnnotationObj> ite2 = ellipseObj.iterator();
        while (ite2.hasNext()) {
            AnnotationObj t = ite2.next();
            int x1 = -1 * t.getCenterBasedY1();
            int y1 = 1 * t.getCenterBasedX1();
            int x2 = -1 * t.getCenterBasedY2();
            int y2 = 1 * t.getCenterBasedX2();
            t.setCenterBasedLocation(x1, y1, x2, y2);
        }
    }

    /**
     * This routine used to rotate all the annotation to 90 degree left
     */
    public void doRotateLeft() {
        addShapeToArray();
        rotateLineLeft();
        rotateArrowLeft();
        rotateRectangleLeft();
        rotateEllipseLeft();
        resetMousePoints();
        repaint();
    }

    /**
     * This routine used to rotate all the lines to 90 degree left
     */
    private void rotateLineLeft() {
        Iterator<AnnotationObj> ite = lineObj.iterator();
        while (ite.hasNext()) {
            AnnotationObj t = ite.next();
            int x1 = 1 * t.getCenterBasedY1();
            int y1 = -1 * t.getCenterBasedX1();
            int x2 = 1 * t.getCenterBasedY2();
            int y2 = -1 * t.getCenterBasedX2();
            t.setCenterBasedLocation(x1, y1, x2, y2);
        }
    }

    /**
     * This routine used to rotate all the lines to 90 degree left
     */
    private void rotateArrowLeft() {
        Iterator<AnnotationObj> ite = arrowObj.iterator();
        while (ite.hasNext()) {
            AnnotationObj t = ite.next();
            int x1 = 1 * t.getCenterBasedY1();
            int y1 = -1 * t.getCenterBasedX1();
            int x2 = 1 * t.getCenterBasedY2();
            int y2 = -1 * t.getCenterBasedX2();
            t.setCenterBasedLocation(x1, y1, x2, y2);
        }
    }

    /**
     * This routine used to rotate all the rectangle to 90 degree left
     */
    private void rotateRectangleLeft() {
        Iterator<AnnotationObj> ite1 = rectObj.iterator();
        while (ite1.hasNext()) {
            AnnotationObj t = ite1.next();
            int x1 = 1 * t.getCenterBasedY1();
            int y1 = -1 * t.getCenterBasedX1();
            int x2 = 1 * t.getCenterBasedY2();
            int y2 = -1 * t.getCenterBasedX2();
            t.setCenterBasedLocation(x1, y1, x2, y2);
        }
    }

    /**
     * This routine used to rotate all the ellipse to 90 degree left
     */
    private void rotateEllipseLeft() {
        Iterator<AnnotationObj> ite2 = ellipseObj.iterator();
        while (ite2.hasNext()) {
            AnnotationObj t = ite2.next();
            int x1 = 1 * t.getCenterBasedY1();
            int y1 = -1 * t.getCenterBasedX1();
            int x2 = 1 * t.getCenterBasedY2();
            int y2 = -1 * t.getCenterBasedX2();
            t.setCenterBasedLocation(x1, y1, x2, y2);
        }
    }

    /**
     * This routine used to flip all annotations vertically
     */
    public void doFlipVertical() {
        addShapeToArray();
        flipLineVertical();
        flipArrowVertical();
        flipRectangleVertical();
        flipEllipseVertical();
        resetMousePoints();
        repaint();
    }

    /**
     * This routine used to flip all lines vertically
     */
    private void flipLineVertical() {
        Iterator<AnnotationObj> ite = lineObj.iterator();
        while (ite.hasNext()) {
            AnnotationObj t = ite.next();
            int x1 = 1 * t.getCenterBasedX1();
            int y1 = -1 * t.getCenterBasedY1();
            int x2 = 1 * t.getCenterBasedX2();
            int y2 = -1 * t.getCenterBasedY2();
            t.setCenterBasedLocation(x1, y1, x2, y2);
        }
    }

    /**
     * This routine used to flip all Arrows vertically
     */
    private void flipArrowVertical() {
        Iterator<AnnotationObj> ite = arrowObj.iterator();
        while (ite.hasNext()) {
            AnnotationObj t = ite.next();
            int x1 = 1 * t.getCenterBasedX1();
            int y1 = -1 * t.getCenterBasedY1();
            int x2 = 1 * t.getCenterBasedX2();
            int y2 = -1 * t.getCenterBasedY2();
            t.setCenterBasedLocation(x1, y1, x2, y2);
        }
    }

    /**
     * This routine used to flip all rectangle vertically
     */
    private void flipRectangleVertical() {
        Iterator<AnnotationObj> ite1 = rectObj.iterator();
        while (ite1.hasNext()) {
            AnnotationObj t = ite1.next();
            int x1 = 1 * t.getCenterBasedX1();
            int y1 = -1 * t.getCenterBasedY1();
            int x2 = 1 * t.getCenterBasedX2();
            int y2 = -1 * t.getCenterBasedY2();
            t.setCenterBasedLocation(x1, y1, x2, y2);
        }
    }

    /**
     * This routine is used to flip all ellipse vertically
     */
    private void flipEllipseVertical() {
        Iterator<AnnotationObj> ite2 = ellipseObj.iterator();
        while (ite2.hasNext()) {
            AnnotationObj t = ite2.next();
            int x1 = 1 * t.getCenterBasedX1();
            int y1 = -1 * t.getCenterBasedY1();
            int x2 = 1 * t.getCenterBasedX2();
            int y2 = -1 * t.getCenterBasedY2();
            t.setCenterBasedLocation(x1, y1, x2, y2);
        }
    }

    /**
     * This routine used to flip all the annotation horizontally
     */
    public void doFlipHorizontal() {
        addShapeToArray();
        flipLineHorizontal();
        flipArrowHorizontal();
        flipRectangleHorizontal();
        flipEllipseHorizontal();
        resetMousePoints();
        repaint();

    }

    /**
     * This routine used to flip all the lines horizontally
     */
    private void flipLineHorizontal() {
        Iterator<AnnotationObj> ite = lineObj.iterator();
        while (ite.hasNext()) {
            AnnotationObj t = ite.next();
            int x1 = -1 * t.getCenterBasedX1();
            int y1 = 1 * t.getCenterBasedY1();
            int x2 = -1 * t.getCenterBasedX2();
            int y2 = 1 * t.getCenterBasedY2();
            t.setCenterBasedLocation(x1, y1, x2, y2);
        }
    }

    /**
     * This routine used to flip all the Arrows horizontally
     */
    private void flipArrowHorizontal() {
        Iterator<AnnotationObj> ite = arrowObj.iterator();
        while (ite.hasNext()) {
            AnnotationObj t = ite.next();
            int x1 = -1 * t.getCenterBasedX1();
            int y1 = 1 * t.getCenterBasedY1();
            int x2 = -1 * t.getCenterBasedX2();
            int y2 = 1 * t.getCenterBasedY2();
            t.setCenterBasedLocation(x1, y1, x2, y2);
        }
    }

    /**
     * This routine used to flip all the rectangle vertically
     */
    private void flipRectangleHorizontal() {
        Iterator<AnnotationObj> ite1 = rectObj.iterator();
        while (ite1.hasNext()) {
            AnnotationObj t = ite1.next();
            int x1 = -1 * t.getCenterBasedX1();
            int y1 = 1 * t.getCenterBasedY1();
            int x2 = -1 * t.getCenterBasedX2();
            int y2 = 1 * t.getCenterBasedY2();
            t.setCenterBasedLocation(x1, y1, x2, y2);
        }
    }

    /**
     * This routine used to flip all the elliptical horizontally
     */
    private void flipEllipseHorizontal() {
        Iterator<AnnotationObj> ite2 = ellipseObj.iterator();
        while (ite2.hasNext()) {
            AnnotationObj t = ite2.next();
            int x1 = -1 * t.getCenterBasedX1();
            int y1 = 1 * t.getCenterBasedY1();
            int x2 = -1 * t.getCenterBasedX2();
            int y2 = 1 * t.getCenterBasedY2();
            t.setCenterBasedLocation(x1, y1, x2, y2);
        }
    }
    /*
     * Following code will be helpful in future. int x1 = (int) (-cosninety *
     * t.getCenterBasedX1() + sineninety * t.getCenterBasedY1()); int y1 = (int)
     * (-sineninety * t.getCenterBasedX1() - cosninety * t.getCenterBasedY1());
     * int x2 = (int) (-cosninety * t.getCenterBasedX2() + sineninety *
     * t.getCenterBasedY2()); int y2 = (int) (-sineninety * t.getCenterBasedX2()
     * - cosninety * t.getCenterBasedY2());
     */

    /**
     * This routine used to clear all the measurement from the annotation layer
     */
    public void clearAllMeasurement() {
        lineObj = new ArrayList<AnnotationObj>();
        arrowObj = new ArrayList<AnnotationObj>();
        rectObj = new ArrayList<AnnotationObj>();
        ellipseObj = new ArrayList<AnnotationObj>();
        mouseLocX1 = 0;
        mouseLocX2 = 0;
        mouseLocY1 = 0;
        mouseLocY2 = 0;
        selectedShape = null;
        boundingRect = null;
        repaint();
        layeredCanvas.imgpanel.removeAllAnnotations();
    }

    public void resetMeasurements() {
        lineObj.clear();
        arrowObj.clear();
        rectObj.clear();
        ellipseObj.clear();
        mouseLocX1 = mouseLocX2 = mouseLocY1 = mouseLocY2 = 0;
        selectedShape = null;
        boundingRect = null;
        startAnnotation = showAnnotation = true;
        addLine = addRect = addEllipse = addArrow = false;
        deleteMeasurement = resizeMeasurement = moveMeasurement = false;
        selectedShapeType = "";
        selectedShapeDisplayStringValue = "";
        selectedShapeMean = "";
        selectedShapeStandardDevi = "";
        tool = "";
        repaint();
        layeredCanvas.imgpanel.removeAllAnnotations();
    }

    public void stopPanning() {
        if (tool.equalsIgnoreCase("panning")) {
            tool = "";
            layeredCanvas.imgpanel.tool = "";
        }
    }

    @Override
    public void paint(Graphics gs) {
        super.paint(gs);
        if (showAnnotation) {
            Graphics2D g = (Graphics2D) gs;
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g.setColor(new Color(255, 138, 0));
            gs.setColor(new Color(255, 138, 0));
            g.scale(layeredCanvas.imgpanel.getScaleFactor(), layeredCanvas.imgpanel.getScaleFactor());
            //Condition used to draw new line as per the line flag values and coordinates of the annotation mouse point
            if ((mouseLocX1 != -1 && mouseLocX2 != -1 && mouseLocY1 != -1 && mouseLocY2 != -1) && addLine) {
                g.drawString("Length:" + calculateDiff((int) ((mouseLocX1 - layeredCanvas.imgpanel.getOriginX()) / layeredCanvas.imgpanel.getScaleFactor()), (int) ((mouseLocY1 - layeredCanvas.imgpanel.getOriginY()) / layeredCanvas.imgpanel.getScaleFactor()), (int) ((mouseLocX2 - layeredCanvas.imgpanel.getOriginX()) / layeredCanvas.imgpanel.getScaleFactor()), (int) ((mouseLocY2 - layeredCanvas.imgpanel.getOriginY()) / layeredCanvas.imgpanel.getScaleFactor())), (int) (mouseLocX1 / layeredCanvas.imgpanel.getScaleFactor()), (int) ((mouseLocY1 / layeredCanvas.imgpanel.getScaleFactor()) - 20));
                g.drawLine((int) (mouseLocX1 / layeredCanvas.imgpanel.getScaleFactor()), (int) (mouseLocY1 / layeredCanvas.imgpanel.getScaleFactor()), (int) (mouseLocX2 / layeredCanvas.imgpanel.getScaleFactor()), (int) (mouseLocY2 / layeredCanvas.imgpanel.getScaleFactor()));
                g.fill(new Ellipse2D.Double((mouseLocX1 / layeredCanvas.imgpanel.getScaleFactor()) - 3.0, (mouseLocY1 / layeredCanvas.imgpanel.getScaleFactor()) - 3.0, 6.0, 6.0));
                g.fill(new Ellipse2D.Double((mouseLocX2 / layeredCanvas.imgpanel.getScaleFactor()) - 3.0, (mouseLocY2 / layeredCanvas.imgpanel.getScaleFactor()) - 3.0, 6.0, 6.0));
            }
            // As per the lineObj present in the tile it will display the lines
            Iterator<AnnotationObj> ite = lineObj.iterator();
            while (ite.hasNext()) {
                AnnotationObj t = ite.next();
                g.drawString("Length:" + t.getLength(), (int) ((t.getX1() * layeredCanvas.imgpanel.getScaleFactor() + layeredCanvas.imgpanel.getOriginX()) / layeredCanvas.imgpanel.getScaleFactor()), (int) (((t.getY1() * layeredCanvas.imgpanel.getScaleFactor() + layeredCanvas.imgpanel.getOriginY()) / layeredCanvas.imgpanel.getScaleFactor()) - 20));
                g.drawLine((int) ((t.getX1() * layeredCanvas.imgpanel.getScaleFactor() + layeredCanvas.imgpanel.getOriginX()) / layeredCanvas.imgpanel.getScaleFactor()), (int) ((t.getY1() * layeredCanvas.imgpanel.getScaleFactor() + layeredCanvas.imgpanel.getOriginY()) / layeredCanvas.imgpanel.getScaleFactor()), (int) ((t.getX2() * layeredCanvas.imgpanel.getScaleFactor() + layeredCanvas.imgpanel.getOriginX()) / layeredCanvas.imgpanel.getScaleFactor()), (int) ((t.getY2() * layeredCanvas.imgpanel.getScaleFactor() + layeredCanvas.imgpanel.getOriginY()) / layeredCanvas.imgpanel.getScaleFactor()));
                g.fill(new Ellipse2D.Double(((t.getX1() * layeredCanvas.imgpanel.getScaleFactor() + layeredCanvas.imgpanel.getOriginX()) / layeredCanvas.imgpanel.getScaleFactor()) - 3.0, ((t.getY1() * layeredCanvas.imgpanel.getScaleFactor() + layeredCanvas.imgpanel.getOriginY()) / layeredCanvas.imgpanel.getScaleFactor()) - 3.0, 6.0, 6.0));
                g.fill(new Ellipse2D.Double(((t.getX2() * layeredCanvas.imgpanel.getScaleFactor() + layeredCanvas.imgpanel.getOriginX()) / layeredCanvas.imgpanel.getScaleFactor()) - 3.0, ((t.getY2() * layeredCanvas.imgpanel.getScaleFactor() + layeredCanvas.imgpanel.getOriginY()) / layeredCanvas.imgpanel.getScaleFactor()) - 3.0, 6.0, 6.0));
            }
            //Condition used to draw new arrow as per the arrow flag values and coordinates of the annotation mouse point
            if ((mouseLocX1 != -1 && mouseLocX2 != -1 && mouseLocY1 != -1 && mouseLocY2 != -1) && addArrow) {
                Point startPoint = new Point((int) (mouseLocX1 / layeredCanvas.imgpanel.getScaleFactor()), (int) (mouseLocY1 / layeredCanvas.imgpanel.getScaleFactor()));
                Point endPoint = new Point((int) (mouseLocX2 / layeredCanvas.imgpanel.getScaleFactor()), (int) (mouseLocY2 / layeredCanvas.imgpanel.getScaleFactor()));
                renderArrow(endPoint, startPoint, Color.getColor("114,143,206"), new BasicStroke(2), g);
            }
            // As per the arrowObj present in the tile it will display the lines
            if (arrowObj != null) {
                Iterator<AnnotationObj> iae = arrowObj.iterator();
                while (iae.hasNext()) {
                    AnnotationObj t = iae.next();
                    renderArrow(new Point((int) ((t.getX2() * layeredCanvas.imgpanel.getScaleFactor() + layeredCanvas.imgpanel.getOriginX()) / layeredCanvas.imgpanel.getScaleFactor()), (int) ((t.getY2() * layeredCanvas.imgpanel.getScaleFactor() + layeredCanvas.imgpanel.getOriginY()) / layeredCanvas.imgpanel.getScaleFactor())), new Point((int) ((t.getX1() * layeredCanvas.imgpanel.getScaleFactor() + layeredCanvas.imgpanel.getOriginX()) / layeredCanvas.imgpanel.getScaleFactor()), (int) ((t.getY1() * layeredCanvas.imgpanel.getScaleFactor() + layeredCanvas.imgpanel.getOriginY()) / layeredCanvas.imgpanel.getScaleFactor())), Color.getColor("114,143,206"), new BasicStroke(2), g);
                }
            }
            //Condition used to check the coordinate position and rectangle flag
            if ((mouseLocX1 != -1 && mouseLocX2 != -1 && mouseLocY1 != -1 && mouseLocY2 != -1) && addRect) {

                ShapeCoordinates shapeCoordinates = new ShapeCoordinates((int) (mouseLocX1 / layeredCanvas.imgpanel.getScaleFactor()), (int) (mouseLocY1 / layeredCanvas.imgpanel.getScaleFactor()), (int) (mouseLocX2 / layeredCanvas.imgpanel.getScaleFactor()), (int) (mouseLocY2 / layeredCanvas.imgpanel.getScaleFactor()));
                //Draw new string for area,mean and stddev for the current shape
                gs.drawString("Area:" + calculateArea((int) ((mouseLocX1 - layeredCanvas.imgpanel.getOriginX()) / layeredCanvas.imgpanel.getScaleFactor()), (int) ((mouseLocY1 - layeredCanvas.imgpanel.getOriginY()) / layeredCanvas.imgpanel.getScaleFactor()), (int) ((mouseLocX2 - layeredCanvas.imgpanel.getOriginX()) / layeredCanvas.imgpanel.getScaleFactor()), (int) ((mouseLocY2 - layeredCanvas.imgpanel.getOriginY()) / layeredCanvas.imgpanel.getScaleFactor())), shapeCoordinates.getX(), shapeCoordinates.getY() - 60);
                gs.drawString("Mean:", shapeCoordinates.getX(), shapeCoordinates.getY() - 40);
                gs.drawString("Std Dev:", shapeCoordinates.getX(), shapeCoordinates.getY() - 20);
                gs.drawRect(shapeCoordinates.getX(), shapeCoordinates.getY(), shapeCoordinates.getWidth(), shapeCoordinates.getHeight());
            }
            //As per the rectObj present in the list it will iterate and draw the rectanlge
            Iterator<AnnotationObj> ite1 = rectObj.iterator();
            while (ite1.hasNext()) {
                AnnotationObj t = ite1.next();
                //Draws new string for area,mean,stddev for the current shape
                ShapeCoordinates shapeCoordinates = new ShapeCoordinates((int) ((t.getX1() * layeredCanvas.imgpanel.getScaleFactor() + layeredCanvas.imgpanel.getOriginX()) / layeredCanvas.imgpanel.getScaleFactor()), (int) ((t.getY1() * layeredCanvas.imgpanel.getScaleFactor() + layeredCanvas.imgpanel.getOriginY()) / layeredCanvas.imgpanel.getScaleFactor()), (int) ((t.getX2() * layeredCanvas.imgpanel.getScaleFactor() + layeredCanvas.imgpanel.getOriginX()) / layeredCanvas.imgpanel.getScaleFactor()), (int) ((t.getY2() * layeredCanvas.imgpanel.getScaleFactor() + layeredCanvas.imgpanel.getOriginY()) / layeredCanvas.imgpanel.getScaleFactor()));
                gs.drawString("Area:" + t.getArea(), shapeCoordinates.getX(), shapeCoordinates.getY() - 60);
                gs.drawString("Mean:" + t.getMean(), shapeCoordinates.getX(), shapeCoordinates.getY() - 40);
                gs.drawString("Std Dev:" + t.getStdDev(), shapeCoordinates.getX(), shapeCoordinates.getY() - 20);
                gs.drawRect(shapeCoordinates.getX(), shapeCoordinates.getY(), shapeCoordinates.getWidth(), shapeCoordinates.getHeight());
            }
            //Condition used to check the current cooridnates and ellipse flag
            if ((mouseLocX1 != -1 && mouseLocX2 != -1 && mouseLocY1 != -1 && mouseLocY2 != -1) && addEllipse) {
//           //Draws the strings such as area,mean,stddev for the current shape
                ShapeCoordinates shapeCoordinates = new ShapeCoordinates((int) (mouseLocX1 / layeredCanvas.imgpanel.getScaleFactor()), (int) (mouseLocY1 / layeredCanvas.imgpanel.getScaleFactor()), (int) (mouseLocX2 / layeredCanvas.imgpanel.getScaleFactor()), (int) (mouseLocY2 / layeredCanvas.imgpanel.getScaleFactor()));
                gs.drawString("Area:" + calculateOvalArea((int) ((mouseLocX1 - layeredCanvas.imgpanel.getOriginX()) / layeredCanvas.imgpanel.getScaleFactor()), (int) ((mouseLocY1 - layeredCanvas.imgpanel.getOriginY()) / layeredCanvas.imgpanel.getScaleFactor()), (int) ((mouseLocX2 - layeredCanvas.imgpanel.getOriginX()) / layeredCanvas.imgpanel.getScaleFactor()), (int) ((mouseLocY2 - layeredCanvas.imgpanel.getOriginY()) / layeredCanvas.imgpanel.getScaleFactor())), shapeCoordinates.getX(), shapeCoordinates.getY() - 60);
                gs.drawString("Mean:", shapeCoordinates.getX(), shapeCoordinates.getY() - 40);
                gs.drawString("Std Dev:", shapeCoordinates.getX(), shapeCoordinates.getY() - 20);
                gs.drawOval(shapeCoordinates.getX(), shapeCoordinates.getY(), shapeCoordinates.getWidth(), shapeCoordinates.getHeight());
            }
            //As per the ellipseObj present in the list it will iterate and draw the ellipse
            Iterator<AnnotationObj> ite2 = ellipseObj.iterator();
            while (ite2.hasNext()) {
                AnnotationObj t = ite2.next();
                //Used to draw the area,mean and std dev values in the annotation panel
                ShapeCoordinates shapeCoordinates = new ShapeCoordinates((int) ((t.getX1() * layeredCanvas.imgpanel.getScaleFactor() + layeredCanvas.imgpanel.getOriginX()) / layeredCanvas.imgpanel.getScaleFactor()), (int) ((t.getY1() * layeredCanvas.imgpanel.getScaleFactor() + layeredCanvas.imgpanel.getOriginY()) / layeredCanvas.imgpanel.getScaleFactor()), (int) ((t.getX2() * layeredCanvas.imgpanel.getScaleFactor() + layeredCanvas.imgpanel.getOriginX()) / layeredCanvas.imgpanel.getScaleFactor()), (int) ((t.getY2() * layeredCanvas.imgpanel.getScaleFactor() + layeredCanvas.imgpanel.getOriginY()) / layeredCanvas.imgpanel.getScaleFactor()));
                gs.drawString("Area:" + t.getArea(), shapeCoordinates.getX(), shapeCoordinates.getY() - 60);
                gs.drawString("Mean:" + t.getMean(), shapeCoordinates.getX(), shapeCoordinates.getY() - 40);
                gs.drawString("Std Dev:" + t.getStdDev(), shapeCoordinates.getX(), shapeCoordinates.getY() - 20);
                gs.drawOval(shapeCoordinates.getX(), shapeCoordinates.getY(), shapeCoordinates.getWidth(), shapeCoordinates.getHeight());
            }
            // Selecting the annotations
            if (boundingRect != null) {
                if (selectedShapeType.equalsIgnoreCase("line") || selectedShapeType.equalsIgnoreCase("arrow")) {
                    drawHighlightLines(g, boundingRect);
                } else {
                    drawHighlightSquares(g, boundingRect);
                }
            }
            if (selectedShape != null) {
                if (selectedShapeType.equalsIgnoreCase("line")) {
                    gs.drawString("Length:" + selectedShapeDisplayStringValue, (int) ((selectedShape.getBounds().x * layeredCanvas.imgpanel.getScaleFactor() + layeredCanvas.imgpanel.getOriginX()) / layeredCanvas.imgpanel.getScaleFactor()), (int) ((selectedShape.getBounds().y * layeredCanvas.imgpanel.getScaleFactor() + layeredCanvas.imgpanel.getOriginY()) / layeredCanvas.imgpanel.getScaleFactor()) - 20);
//                    gs.drawLine((int) ((selectedShape.getBounds().x * layeredCanvas.imgpanel.getScaleFactor() + layeredCanvas.imgpanel.getOriginX()) / layeredCanvas.imgpanel.getScaleFactor()), (int) ((selectedShape.getBounds().y * layeredCanvas.imgpanel.getScaleFactor() + layeredCanvas.imgpanel.getOriginY()) / layeredCanvas.imgpanel.getScaleFactor()), (int) ((selectedShape.getBounds().width * layeredCanvas.imgpanel.getScaleFactor() + layeredCanvas.imgpanel.getOriginX()) / layeredCanvas.imgpanel.getScaleFactor()), (int) ((selectedShape.getBounds().height * layeredCanvas.imgpanel.getScaleFactor() + layeredCanvas.imgpanel.getOriginY()) / layeredCanvas.imgpanel.getScaleFactor()));
                    if (selectedShapeOrientation.equalsIgnoreCase("left")) {
                        gs.drawLine((int) ((selectedShape.getBounds().x * layeredCanvas.imgpanel.getScaleFactor() + layeredCanvas.imgpanel.getOriginX()) / layeredCanvas.imgpanel.getScaleFactor()), (int) ((selectedShape.getBounds().y * layeredCanvas.imgpanel.getScaleFactor() + layeredCanvas.imgpanel.getOriginY()) / layeredCanvas.imgpanel.getScaleFactor()), (int) (((selectedShape.getBounds().x + selectedShape.getBounds().width) * layeredCanvas.imgpanel.getScaleFactor() + layeredCanvas.imgpanel.getOriginX()) / layeredCanvas.imgpanel.getScaleFactor()), (int) (((selectedShape.getBounds().y + selectedShape.getBounds().height) * layeredCanvas.imgpanel.getScaleFactor() + layeredCanvas.imgpanel.getOriginY()) / layeredCanvas.imgpanel.getScaleFactor()));
                    } else {
                        gs.drawLine((int) (((selectedShape.getBounds().x + selectedShape.getBounds().width) * layeredCanvas.imgpanel.getScaleFactor() + layeredCanvas.imgpanel.getOriginX()) / layeredCanvas.imgpanel.getScaleFactor()), (int) ((selectedShape.getBounds().y * layeredCanvas.imgpanel.getScaleFactor() + layeredCanvas.imgpanel.getOriginY()) / layeredCanvas.imgpanel.getScaleFactor()), (int) ((selectedShape.getBounds().x * layeredCanvas.imgpanel.getScaleFactor() + layeredCanvas.imgpanel.getOriginX()) / layeredCanvas.imgpanel.getScaleFactor()), (int) (((selectedShape.getBounds().y + selectedShape.getBounds().height) * layeredCanvas.imgpanel.getScaleFactor() + layeredCanvas.imgpanel.getOriginY()) / layeredCanvas.imgpanel.getScaleFactor()));
                    }
                } else if (selectedShapeType.equalsIgnoreCase("arrow")) {
                    Point startPoint = null;
                    Point endPoint = null;
                    if (selectedShapeOrientation.equalsIgnoreCase("left")) {
                        startPoint = new Point((int) ((selectedShape.getBounds().x * layeredCanvas.imgpanel.getScaleFactor() + layeredCanvas.imgpanel.getOriginX()) / layeredCanvas.imgpanel.getScaleFactor()), (int) ((selectedShape.getBounds().y * layeredCanvas.imgpanel.getScaleFactor() + layeredCanvas.imgpanel.getOriginY()) / layeredCanvas.imgpanel.getScaleFactor()));
                        endPoint = new Point((int) (((selectedShape.getBounds().x + selectedShape.getBounds().width) * layeredCanvas.imgpanel.getScaleFactor() + layeredCanvas.imgpanel.getOriginX()) / layeredCanvas.imgpanel.getScaleFactor()), (int) (((selectedShape.getBounds().y + selectedShape.getBounds().height) * layeredCanvas.imgpanel.getScaleFactor() + layeredCanvas.imgpanel.getOriginY()) / layeredCanvas.imgpanel.getScaleFactor()));
                    } else {
                        startPoint = new Point((int) (((selectedShape.getBounds().x + selectedShape.getBounds().width) * layeredCanvas.imgpanel.getScaleFactor() + layeredCanvas.imgpanel.getOriginX()) / layeredCanvas.imgpanel.getScaleFactor()), (int) ((selectedShape.getBounds().y * layeredCanvas.imgpanel.getScaleFactor() + layeredCanvas.imgpanel.getOriginY()) / layeredCanvas.imgpanel.getScaleFactor()));
                        endPoint = new Point((int) ((selectedShape.getBounds().x * layeredCanvas.imgpanel.getScaleFactor() + layeredCanvas.imgpanel.getOriginX()) / layeredCanvas.imgpanel.getScaleFactor()), (int) (((selectedShape.getBounds().y + selectedShape.getBounds().height) * layeredCanvas.imgpanel.getScaleFactor() + layeredCanvas.imgpanel.getOriginY()) / layeredCanvas.imgpanel.getScaleFactor()));
                    }
                    if (selectedArrowOrientString.equalsIgnoreCase("flip")) {
                        renderArrow(startPoint, endPoint, Color.red, new BasicStroke(2), g);
                    } else {
                        renderArrow(endPoint, startPoint, Color.red, new BasicStroke(2), g);
                    }
                } else if (selectedShapeType.equalsIgnoreCase("ellipse")) {
                    //Used to draw the area,mean and std dev values in the annotation panel                    
                    gs.drawString("Area:" + selectedShapeDisplayStringValue, (int) ((selectedShape.getBounds().x * layeredCanvas.imgpanel.getScaleFactor() + layeredCanvas.imgpanel.getOriginX()) / layeredCanvas.imgpanel.getScaleFactor()), (int) ((selectedShape.getBounds().y * layeredCanvas.imgpanel.getScaleFactor() + layeredCanvas.imgpanel.getOriginY()) / layeredCanvas.imgpanel.getScaleFactor()) - 60);
                    gs.drawString("Mean:", (int) ((selectedShape.getBounds().x * layeredCanvas.imgpanel.getScaleFactor() + layeredCanvas.imgpanel.getOriginX()) / layeredCanvas.imgpanel.getScaleFactor()), (int) ((selectedShape.getBounds().y * layeredCanvas.imgpanel.getScaleFactor() + layeredCanvas.imgpanel.getOriginY()) / layeredCanvas.imgpanel.getScaleFactor()) - 40);
                    gs.drawString("Std Dev:", (int) ((selectedShape.getBounds().x * layeredCanvas.imgpanel.getScaleFactor() + layeredCanvas.imgpanel.getOriginX()) / layeredCanvas.imgpanel.getScaleFactor()), (int) ((selectedShape.getBounds().y * layeredCanvas.imgpanel.getScaleFactor() + layeredCanvas.imgpanel.getOriginY()) / layeredCanvas.imgpanel.getScaleFactor()) - 20);
                    gs.drawOval((int) ((selectedShape.getBounds().x * layeredCanvas.imgpanel.getScaleFactor() + layeredCanvas.imgpanel.getOriginX()) / layeredCanvas.imgpanel.getScaleFactor()), (int) ((selectedShape.getBounds().y * layeredCanvas.imgpanel.getScaleFactor() + layeredCanvas.imgpanel.getOriginY()) / layeredCanvas.imgpanel.getScaleFactor()), selectedShape.getBounds().width, selectedShape.getBounds().height);
                } else {
                    gs.drawString("Area:" + selectedShapeDisplayStringValue, (int) ((selectedShape.getBounds().x * layeredCanvas.imgpanel.getScaleFactor() + layeredCanvas.imgpanel.getOriginX()) / layeredCanvas.imgpanel.getScaleFactor()), (int) ((selectedShape.getBounds().y * layeredCanvas.imgpanel.getScaleFactor() + layeredCanvas.imgpanel.getOriginY()) / layeredCanvas.imgpanel.getScaleFactor()) - 60);
                    gs.drawString("Mean:", (int) ((selectedShape.getBounds().x * layeredCanvas.imgpanel.getScaleFactor() + layeredCanvas.imgpanel.getOriginX()) / layeredCanvas.imgpanel.getScaleFactor()), (int) ((selectedShape.getBounds().y * layeredCanvas.imgpanel.getScaleFactor() + layeredCanvas.imgpanel.getOriginY()) / layeredCanvas.imgpanel.getScaleFactor()) - 40);
                    gs.drawString("Std Dev:", (int) ((selectedShape.getBounds().x * layeredCanvas.imgpanel.getScaleFactor() + layeredCanvas.imgpanel.getOriginX()) / layeredCanvas.imgpanel.getScaleFactor()), (int) ((selectedShape.getBounds().y * layeredCanvas.imgpanel.getScaleFactor() + layeredCanvas.imgpanel.getOriginY()) / layeredCanvas.imgpanel.getScaleFactor()) - 20);
                    gs.drawRect((int) ((selectedShape.getBounds().x * layeredCanvas.imgpanel.getScaleFactor() + layeredCanvas.imgpanel.getOriginX()) / layeredCanvas.imgpanel.getScaleFactor()), (int) ((selectedShape.getBounds().y * layeredCanvas.imgpanel.getScaleFactor() + layeredCanvas.imgpanel.getOriginY()) / layeredCanvas.imgpanel.getScaleFactor()), selectedShape.getBounds().width, selectedShape.getBounds().height);
                }
            }
        }

    }

    /**
     * This routine used to toggle the annotation overlay visibility.
     */
    public void toggleAnnotation() {
        if (showAnnotation) {
            showAnnotation = false;
        } else {
            showAnnotation = true;
        }
        repaint();
    }

    public boolean isShowAnnotation() {
        return showAnnotation;
    }

    /**
     * This routine used to draw the highlight square around the selected
     * measurement.
     *
     * @param g2D
     * @param r
     */
    public void drawHighlightSquares(Graphics2D g2D, Rectangle r) {
        double x = (r.getX() * layeredCanvas.imgpanel.getScaleFactor() + layeredCanvas.imgpanel.getOriginX()) / layeredCanvas.imgpanel.getScaleFactor();
        double y = (r.getY() * layeredCanvas.imgpanel.getScaleFactor() + layeredCanvas.imgpanel.getOriginY()) / layeredCanvas.imgpanel.getScaleFactor();
        double w = r.getWidth();
        double h = r.getHeight();
        g2D.setColor(Color.RED);
        handle1 = new Rectangle.Double(x - 3.0, y - 3.0, 6.0, 6.0);
        g2D.fill(handle1);
        handle2 = new Rectangle.Double(x + w * 0.5 - 3.0, y - 3.0, 6.0, 6.0);
        g2D.fill(handle2);
        handle3 = new Rectangle.Double(x + w - 3.0, y - 3.0, 6.0, 6.0);
        g2D.fill(handle3);
        handle4 = new Rectangle.Double(x - 3.0, y + h * 0.5 - 3.0, 6.0, 6.0);
        g2D.fill(handle4);
        handle5 = new Rectangle.Double(x + w - 3.0, y + h * 0.5 - 3.0, 6.0, 6.0);
        g2D.fill(handle5);
        handle6 = new Rectangle.Double(x - 3.0, y + h - 3.0, 6.0, 6.0);
        g2D.fill(handle6);
        handle7 = new Rectangle.Double(x + w * 0.5 - 3.0, y + h - 3.0, 6.0, 6.0);
        g2D.fill(handle7);
        handle8 = new Rectangle.Double(x + w - 3.0, y + h - 3.0, 6.0, 6.0);
        g2D.fill(handle8);
    }

    public void drawHighlightLines(Graphics2D g2D, Rectangle r) {
        double x = (r.getX() * layeredCanvas.imgpanel.getScaleFactor() + layeredCanvas.imgpanel.getOriginX()) / layeredCanvas.imgpanel.getScaleFactor();
        double y = (r.getY() * layeredCanvas.imgpanel.getScaleFactor() + layeredCanvas.imgpanel.getOriginY()) / layeredCanvas.imgpanel.getScaleFactor();
        double w = r.getWidth();
        double h = r.getHeight();
        g2D.setColor(Color.RED);
        if (selectedShapeOrientation.equalsIgnoreCase("left")) {
            lineHandle1 = new Ellipse2D.Double(x - 3.0, y - 3.0, 6.0, 6.0);
            g2D.fill(lineHandle1);
            lineHandle2 = new Ellipse2D.Double((x + w) - 3.0, (y + h) - 3.0, 6.0, 6.0);
            g2D.fill(lineHandle2);
        } else {
            lineHandle1 = new Ellipse2D.Double((x + w) - 3.0, y - 3.0, 6.0, 6.0);
            g2D.fill(lineHandle1);
            lineHandle2 = new Ellipse2D.Double(x - 3.0, (y + h) - 3.0, 6.0, 6.0);
            g2D.fill(lineHandle2);
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (tool.equalsIgnoreCase("panning")) {
            mouseLocX2 = e.getX();
            mouseLocY2 = e.getY();
        } else if (moveMeasurement) {
            if (selectedShape != null) {
                mouseLocX2 = e.getX();
                mouseLocY2 = e.getY();
                Shape temp = new Rectangle2D.Float(selectedShape.getBounds().x + mouseLocX2 - mouseLocX1, selectedShape.getBounds().y + mouseLocY2 - mouseLocY1, selectedShape.getBounds().width, selectedShape.getBounds().height);
                selectedShape = temp;
                boundingRect = temp.getBounds();
                mouseLocX1 = mouseLocX2;
                mouseLocY1 = mouseLocY2;
                repaint();
//                selectedShape = temp;
//                boundingRect = temp.getBounds();
//                mouseLocX1 = mouseLocX2;
//                mouseLocY1 = mouseLocY2;
//                repaint();
            }
        } else if (isAddEllipse() || isAddRect() || isAddLine() || isAddArrow()) {
            if (!startAnnotation) {
                mouseLocX2 = e.getX();
                mouseLocY2 = e.getY();
                repaint();
            }
        }
        layeredCanvas.imgpanel.mouseDragged(e);
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        if (!addEllipse || !addLine || !addRect) {
            layeredCanvas.imgpanel.mouseMoved(e);
        }
    }

    /**
     * This routine used to calculate the difference based on the coordinates
     * passed as parameter.
     *
     * @param mouseLocX1
     * @param mouseLocY1
     * @param mouseLocX2
     * @param mouseLocY2
     * @return
     */
    private String calculateDiff(int mouseLocX1, int mouseLocY1, int mouseLocX2, int mouseLocY2) {
        double diff, returnValue;
        String returnString = "";
        if (mouseLocX1 == mouseLocX2) {
            if (layeredCanvas.imgpanel.getPixelSpacingY() != 0) {
                diff = (double) (((mouseLocY2 - mouseLocY1) / layeredCanvas.imgpanel.getCurrentScaleFactor()) * (layeredCanvas.imgpanel.getPixelSpacingY()));
            } else {
                diff = (double) ((mouseLocY2 - mouseLocY1) / layeredCanvas.imgpanel.getCurrentScaleFactor());
            }

        } else if (mouseLocY1 == mouseLocY2) {
            if (layeredCanvas.imgpanel.getPixelSpacingX() != 0) {
                diff = (double) (((mouseLocX2 - mouseLocX1) / layeredCanvas.imgpanel.getCurrentScaleFactor()) * (layeredCanvas.imgpanel.getPixelSpacingX()));
            } else {
                diff = (double) ((mouseLocX2 - mouseLocX1) / layeredCanvas.imgpanel.getCurrentScaleFactor());
            }
        } else {
            if (layeredCanvas.imgpanel.getPixelSpacingY() != 0 && layeredCanvas.imgpanel.getPixelSpacingX() != 0) {
                diff = (double) Math.sqrt(Math.pow(((mouseLocY2 - mouseLocY1) / layeredCanvas.imgpanel.getCurrentScaleFactor()) * (layeredCanvas.imgpanel.getPixelSpacingY()), 2) + Math.pow(((mouseLocX2 - mouseLocX1) / layeredCanvas.imgpanel.getCurrentScaleFactor()) * (layeredCanvas.imgpanel.getPixelSpacingX()), 2));
            } else {
                diff = (double) Math.sqrt(Math.pow(((mouseLocY2 - mouseLocY1) / layeredCanvas.imgpanel.getCurrentScaleFactor()), 2) + Math.pow(((mouseLocX2 - mouseLocX1) / layeredCanvas.imgpanel.getCurrentScaleFactor()), 2));
            }
        }
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(3);
        if (layeredCanvas.imgpanel.getPixelSpacingY() == 0 && layeredCanvas.imgpanel.getPixelSpacingX() == 0) {
            returnString = nf.format(diff) + " pix";
        } else {
            returnValue = diff / 10;
            returnString = nf.format(returnValue) + " cm";
        }
        return returnString;
    }

    /**
     * This routine used to calculate the area based on coordinates passed as
     * parameter.
     *
     * @param mouseLocX1
     * @param mouseLocY1
     * @param mouseLocX2
     * @param mouseLocY2
     * @return
     */
    private String calculateArea(int mouseLocX1, int mouseLocY1, int mouseLocX2, int mouseLocY2) {
        int diffY, diffX;
        String returnString = "";
        double diff, diff1;
        if (mouseLocY2 - mouseLocY1 < 0) {
            diffY = -(mouseLocY2 - mouseLocY1);
        } else {
            diffY = mouseLocY2 - mouseLocY1;
        }
        if (mouseLocX2 - mouseLocX1 < 0) {
            diffX = -(mouseLocX2 - mouseLocX1);
        } else {
            diffX = mouseLocX2 - mouseLocX1;
        }
        if (layeredCanvas.imgpanel.getPixelSpacingY() != 0 && layeredCanvas.imgpanel.getPixelSpacingX() != 0) {
            diff = (double) (((diffY / layeredCanvas.imgpanel.getCurrentScaleFactor()) * layeredCanvas.imgpanel.getPixelSpacingY()) * ((diffX / layeredCanvas.imgpanel.getCurrentScaleFactor()) * layeredCanvas.imgpanel.getPixelSpacingX()));
        } else {
            diff = (double) ((diffY / layeredCanvas.imgpanel.getCurrentScaleFactor()) * ((diffX / layeredCanvas.imgpanel.getCurrentScaleFactor())));
        }
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(3);
        if (layeredCanvas.imgpanel.getPixelSpacingY() == 0 && layeredCanvas.imgpanel.getPixelSpacingX() == 0) {
            returnString = nf.format(diff) + " pix2";
        } else {
            diff1 = diff / 100;
            returnString = nf.format(diff1) + " cm2";
        }
        return returnString;
    }

    /**
     * This routine used to calculate the oval area based on the coordinates
     * passed as parameter.
     *
     * @param mouseLocX1
     * @param mouseLocY1
     * @param mouseLocX2
     * @param mouseLocY2
     * @return
     */
    public String calculateOvalArea(int mouseLocX1, int mouseLocY1, int mouseLocX2, int mouseLocY2) {
        int diffY, diffX;
        String returnString = "";
        double diff, diff1;
        double width, height;
        if (mouseLocY2 - mouseLocY1 < 0) {
            diffY = -(mouseLocY2 - mouseLocY1);
        } else {
            diffY = mouseLocY2 - mouseLocY1;
        }
        if (mouseLocX2 - mouseLocX1 < 0) {
            diffX = -(mouseLocX2 - mouseLocX1);
        } else {
            diffX = mouseLocX2 - mouseLocX1;
        }
        if (layeredCanvas.imgpanel.getPixelSpacingY() != 0 && layeredCanvas.imgpanel.getPixelSpacingX() != 0) {
            width = ((diffX / layeredCanvas.imgpanel.getCurrentScaleFactor()) * layeredCanvas.imgpanel.getPixelSpacingX());
            height = ((diffY / layeredCanvas.imgpanel.getCurrentScaleFactor()) * layeredCanvas.imgpanel.getPixelSpacingY());
        } else {
            width = ((diffX / layeredCanvas.imgpanel.getCurrentScaleFactor()));
            height = ((diffY / layeredCanvas.imgpanel.getCurrentScaleFactor()));
        }
        diff = (double) (Math.PI * (width * 0.5) * (height * 0.5));
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(3);
        if (layeredCanvas.imgpanel.getPixelSpacingY() == 0 && layeredCanvas.imgpanel.getPixelSpacingX() == 0) {
            returnString = nf.format(diff) + " pix2";
        } else {
            //in order to get cm2 values
            diff1 = diff / 100;
            returnString = nf.format(diff1) + " cm2";
        }
        return returnString;
    }

    public void calculateMeanDeviation(int mouseLocX1, int mouseLocY1, int mouseLocX2, int mouseLocY2) {
        int diffY, diffX;
        double width, height;
        if (mouseLocY2 - mouseLocY1 < 0) {
            diffY = -(mouseLocY2 - mouseLocY1);
        } else {
            diffY = mouseLocY2 - mouseLocY1;
        }
        if (mouseLocX2 - mouseLocX1 < 0) {
            diffX = -(mouseLocX2 - mouseLocX1);
        } else {
            diffX = mouseLocX2 - mouseLocX1;
        }
        if (layeredCanvas.imgpanel.getPixelSpacingY() != 0 && layeredCanvas.imgpanel.getPixelSpacingX() != 0) {
            width = ((diffX / layeredCanvas.imgpanel.getCurrentScaleFactor()) * layeredCanvas.imgpanel.getPixelSpacingX());
            height = ((diffY / layeredCanvas.imgpanel.getCurrentScaleFactor()) * layeredCanvas.imgpanel.getPixelSpacingY());
        } else {
            width = ((diffX / layeredCanvas.imgpanel.getCurrentScaleFactor()));
            height = ((diffY / layeredCanvas.imgpanel.getCurrentScaleFactor()));
        }
        setMean(layeredCanvas.imgpanel.calculateMean(mouseLocX1, mouseLocY1, (int) Math.round(width), (int) Math.round(height)));
        setStandardDev(layeredCanvas.imgpanel.calculateStandardDeviation(mean, mouseLocX1, mouseLocY1, (int) Math.round(width), (int) Math.round(height)));
        repaint();
    }

    public ArrayList<AnnotationObj> getEllipseObj() {
        return ellipseObj;
    }

    public void setEllipseObj(ArrayList<AnnotationObj> ellipseObj) {
        this.ellipseObj = ellipseObj;
    }

    public ArrayList<AnnotationObj> getLineObj() {
        return lineObj;
    }

    public void setLineObj(ArrayList<AnnotationObj> lineObj) {
        this.lineObj = lineObj;
    }

    public ArrayList<AnnotationObj> getArrowObj() {
        return arrowObj;
    }

    public void setArrowObj(ArrayList<AnnotationObj> arrowObj) {
        this.arrowObj = arrowObj;
    }

    public ArrayList<AnnotationObj> getRectObj() {
        return rectObj;
    }

    public void setRectObj(ArrayList<AnnotationObj> rectObj) {
        this.rectObj = rectObj;
    }

    public Annotation getAnnotation() {
        resetAnnotation();
        if (ellipseObj.size() > 0 || lineObj.size() > 0 || rectObj.size() > 0 || arrowObj.size() > 0) {
            annotation = new Annotation();
            if (selectedShape != null) {
                addShapeToArray();
            }
            annotation.setEllipse(ellipseObj);
            annotation.setRect(rectObj);
            annotation.setLine(lineObj);
            annotation.setArrow(arrowObj);
            resetCursorPoints();
            return annotation;
        }
        return null;
    }

    /**
     * This routine used to reset the cursor points.
     */
    public void resetCursorPoints() {
        mouseLocX1 = -1;
        mouseLocX2 = -1;
        mouseLocY1 = -1;
        mouseLocY2 = -1;
    }

    public void setAnnotation(Annotation annotation) {
        if (annotation != null) {
            this.annotation = annotation;
            lineObj = annotation.getLine();
            arrowObj = annotation.getArrow();
            rectObj = annotation.getRect();
            ellipseObj = annotation.getEllipse();
        } else {
            lineObj = new ArrayList<AnnotationObj>();
            arrowObj = new ArrayList<AnnotationObj>();
            rectObj = new ArrayList<AnnotationObj>();
            ellipseObj = new ArrayList<AnnotationObj>();
        }
        setCurrentAnnotation();
        repaint();
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
        requestFocus();
        layeredCanvas.imgpanel.mousePressed(e);
        if (tool.equalsIgnoreCase("panning")) {
            mouseLocX1 = e.getX();
            mouseLocY1 = e.getY();
        }
        if (moveMeasurement) {
            measurementMove(e);
        }
        if (addLine || addRect || addEllipse || addArrow) {
            mouseLocX1 = e.getX();
            mouseLocY1 = e.getY();
            if (!isMoveMeasurement && startAnnotation) {
                mouseLocX1 = e.getX();
                mouseLocY1 = e.getY();
                mouseLocX2 = e.getX();
                mouseLocY2 = e.getY();
                startAnnotation = false;
            }
        }
    }

    public void resetAnnotaionTools() {
        addEllipse = false;
        addLine = false;
        addRect = false;
        addArrow = false;
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (addLine || addRect || addEllipse || addArrow) {
            if (!startAnnotation) {
                startAnnotation = true;

                if (mouseLocX1 != mouseLocX2 || mouseLocY1 != mouseLocY2) {
                    if (addLine) {
                        AnnotationObj newLine = new AnnotationObj();
                        newLine.setLocation((int) ((mouseLocX1 - layeredCanvas.imgpanel.getOriginX()) / layeredCanvas.imgpanel.getScaleFactor()), (int) ((mouseLocY1 - layeredCanvas.imgpanel.getOriginY()) / layeredCanvas.imgpanel.getScaleFactor()), (int) ((e.getX() - layeredCanvas.imgpanel.getOriginX()) / layeredCanvas.imgpanel.getScaleFactor()), (int) ((e.getY() - layeredCanvas.imgpanel.getOriginY()) / layeredCanvas.imgpanel.getScaleFactor()));
                        newLine.setType("line");
                        newLine.setLength(calculateDiff((int) ((mouseLocX1 - layeredCanvas.imgpanel.getOriginX()) / layeredCanvas.imgpanel.getScaleFactor()), (int) ((mouseLocY1 - layeredCanvas.imgpanel.getOriginY()) / layeredCanvas.imgpanel.getScaleFactor()), (int) ((e.getX() - layeredCanvas.imgpanel.getOriginX()) / layeredCanvas.imgpanel.getScaleFactor()), (int) ((e.getY() - layeredCanvas.imgpanel.getOriginY()) / layeredCanvas.imgpanel.getScaleFactor())));
                        lineObj.add(newLine);
                    }
                    if (addArrow) {
                        AnnotationObj newArrow = new AnnotationObj();
                        int x1 = (int) ((mouseLocX1 - layeredCanvas.imgpanel.getOriginX()) / layeredCanvas.imgpanel.getScaleFactor());
                        int y1 = (int) ((mouseLocY1 - layeredCanvas.imgpanel.getOriginY()) / layeredCanvas.imgpanel.getScaleFactor());
                        int x2 = (int) ((e.getX() - layeredCanvas.imgpanel.getOriginX()) / layeredCanvas.imgpanel.getScaleFactor());
                        int y2 = (int) ((e.getY() - layeredCanvas.imgpanel.getOriginY()) / layeredCanvas.imgpanel.getScaleFactor());
                        newArrow.setLocation(x1, y1, x2, y2);
                        newArrow.setType("arrow");
                        arrowObj.add(newArrow);
                    }
                    if (addRect) {
                        AnnotationObj newRect = new AnnotationObj();
                        newRect.setLocation((int) ((mouseLocX1 - layeredCanvas.imgpanel.getOriginX()) / layeredCanvas.imgpanel.getScaleFactor()), (int) ((mouseLocY1 - layeredCanvas.imgpanel.getOriginY()) / layeredCanvas.imgpanel.getScaleFactor()), (int) ((e.getX() - layeredCanvas.imgpanel.getOriginX()) / layeredCanvas.imgpanel.getScaleFactor()), (int) ((e.getY() - layeredCanvas.imgpanel.getOriginY()) / layeredCanvas.imgpanel.getScaleFactor()));
                        newRect.setType("rect");
                        newRect.setArea(calculateArea((int) ((mouseLocX1 - layeredCanvas.imgpanel.getOriginX()) / layeredCanvas.imgpanel.getScaleFactor()), (int) ((mouseLocY1 - layeredCanvas.imgpanel.getOriginY()) / layeredCanvas.imgpanel.getScaleFactor()), (int) ((e.getX() - layeredCanvas.imgpanel.getOriginX()) / layeredCanvas.imgpanel.getScaleFactor()), (int) ((e.getY() - layeredCanvas.imgpanel.getOriginY()) / layeredCanvas.imgpanel.getScaleFactor())));
                        calculateMeanDeviation((int) ((mouseLocX1 - layeredCanvas.imgpanel.getOriginX()) / layeredCanvas.imgpanel.getScaleFactor()), (int) ((mouseLocY1 - layeredCanvas.imgpanel.getOriginY()) / layeredCanvas.imgpanel.getScaleFactor()), (int) ((e.getX() - layeredCanvas.imgpanel.getOriginX()) / layeredCanvas.imgpanel.getScaleFactor()), (int) ((e.getY() - layeredCanvas.imgpanel.getOriginY()) / layeredCanvas.imgpanel.getScaleFactor()));
                        NumberFormat nf = NumberFormat.getInstance();
                        nf.setMaximumFractionDigits(3);
                        if (layeredCanvas.imgpanel.getPixelSpacingY() == 0 && layeredCanvas.imgpanel.getPixelSpacingX() == 0) {
                            newRect.setMean(nf.format(mean) + "");
                            newRect.setStdDev(nf.format(standardDev) + "");
                        } else {
                            newRect.setMean(nf.format(mean) + "");
                            newRect.setStdDev(nf.format(standardDev) + "");
                        }
                        rectObj.add(newRect);
                    }
                    if (addEllipse) {
                        AnnotationObj newEllipse = new AnnotationObj();
                        newEllipse.setLocation((int) ((mouseLocX1 - layeredCanvas.imgpanel.getOriginX()) / layeredCanvas.imgpanel.getScaleFactor()), (int) ((mouseLocY1 - layeredCanvas.imgpanel.getOriginY()) / layeredCanvas.imgpanel.getScaleFactor()), (int) ((e.getX() - layeredCanvas.imgpanel.getOriginX()) / layeredCanvas.imgpanel.getScaleFactor()), (int) ((e.getY() - layeredCanvas.imgpanel.getOriginY()) / layeredCanvas.imgpanel.getScaleFactor()));
                        newEllipse.setType("ellipse");
                        newEllipse.setArea(calculateOvalArea((int) ((mouseLocX1 - layeredCanvas.imgpanel.getOriginX()) / layeredCanvas.imgpanel.getScaleFactor()), (int) ((mouseLocY1 - layeredCanvas.imgpanel.getOriginY()) / layeredCanvas.imgpanel.getScaleFactor()), (int) ((e.getX() - layeredCanvas.imgpanel.getOriginX()) / layeredCanvas.imgpanel.getScaleFactor()), (int) ((e.getY() - layeredCanvas.imgpanel.getOriginY()) / layeredCanvas.imgpanel.getScaleFactor())));
                        calculateMeanDeviation((int) ((mouseLocX1 - layeredCanvas.imgpanel.getOriginX()) / layeredCanvas.imgpanel.getScaleFactor()), (int) ((mouseLocY1 - layeredCanvas.imgpanel.getOriginY()) / layeredCanvas.imgpanel.getScaleFactor()), (int) ((e.getX() - layeredCanvas.imgpanel.getOriginX()) / layeredCanvas.imgpanel.getScaleFactor()), (int) ((e.getY() - layeredCanvas.imgpanel.getOriginY()) / layeredCanvas.imgpanel.getScaleFactor()));
                        NumberFormat nf = NumberFormat.getInstance();
                        nf.setMaximumFractionDigits(3);
                        if (layeredCanvas.imgpanel.getPixelSpacingY() == 0 && layeredCanvas.imgpanel.getPixelSpacingX() == 0) {
                            newEllipse.setMean(nf.format(mean) + "");
                            newEllipse.setStdDev(nf.format(standardDev) + "");
                        } else {
                            newEllipse.setMean(nf.format(mean) + "");
                            newEllipse.setStdDev(nf.format(standardDev) + "");
                        }
                        ellipseObj.add(newEllipse);
                    }
                } else {
                    mouseLocX1 = -1;
                    mouseLocX2 = -1;
                    mouseLocY1 = -1;
                    mouseLocY2 = -1;
                    repaint();
                }
            }
        }
    }

    private String setArrowFlipText(int x1, int y1, int x2, int y2, String orientation) {
        String doFlip = "";
        if (x1 > x2 && y1 == y2) {
            doFlip = "flip";
        } else if (x1 > x2 && y1 > y2) {
            doFlip = "flip";
        } else if (x1 > x2 && y1 < y2 && orientation.equalsIgnoreCase("left")) {
            doFlip = "flip";
        } else if (x1 == x2 && y1 > y2) {
            doFlip = "flip";
        } else if (x1 < x2 && y1 > y2) {
            doFlip = "flip";
        } else {
            doFlip = "";
        }
        return doFlip;
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        requestFocus();
        layeredCanvas.imgpanel.mouseWheelMoved(e);
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables

    public void renderArrow(Point destination, Point origin, Color c, Stroke lineStroke, Graphics2D g) {
        Color oldColor = g.getColor();
        g.setColor(c);
        double theta = Math.atan2((origin.y - destination.y), (origin.x - destination.x));
        g.setStroke(new BasicStroke(1));
        Shape arrowHead = getArrowHead(origin.x, origin.y, 10, 15, theta);
        g.fill(arrowHead);

        g.setStroke(lineStroke);
        // draw line
        g.drawLine(destination.x, destination.y, origin.x, origin.y);
        g.setColor(oldColor);
    }

    private Shape getArrowHead(int x, int y, int base, int length, double theta) {
        int[] xPoints = new int[3];
        int[] yPoints = new int[3];

        xPoints[0] = -length;
        xPoints[1] = 0;
        xPoints[2] = -length;

        yPoints[0] = base / 2;
        yPoints[1] = 0;
        yPoints[2] = -base / 2;

        Polygon p = new Polygon(xPoints, yPoints, 3);
        AffineTransform at = new AffineTransform();
        at.rotate(theta);
        Shape s = at.createTransformedShape(p);
        at = new AffineTransform();
        at.translate(x, y);
        s = at.createTransformedShape(s);
        return s;
    }
}