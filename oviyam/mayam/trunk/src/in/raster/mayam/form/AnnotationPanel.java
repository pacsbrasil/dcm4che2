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
import in.raster.mayam.form.shapes.ShapeCoordinates;
import in.raster.mayam.util.measurement.Annotation;
import in.raster.mayam.util.measurement.AnnotationObj;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Iterator;

/**
 *
 * @author  BabuHussain
 * @version 0.5 
 */
public class AnnotationPanel extends javax.swing.JPanel implements MouseMotionListener, MouseWheelListener, MouseListener, FocusListener {

    private boolean firstTime = true;
    LayeredCanvas layeredCanvas;
    private int mouseLocX1;
    private int mouseLocY1;
    private int mouseLocX2;
    private int mouseLocY2;
    private boolean startAnnotation = true;
    private boolean showAnnotation = true;
    private int lineCount = 0;
    private int rectCount = 0;
    private int ellipticalCount = 0;
    private static boolean addLine = false;
    private static boolean addRect = false;
    private static boolean addEllipse = false;
    private boolean deleteMeasurement = false;
    private boolean resizeMeasurement = false;
    private boolean moveMeasurement = false;
    private float cosninety = 0;
    private float sineninety = 1;
    private Shape seletedShape = null;
    private String selectedShapeType = "";
    private String selectedShapeDisplayStringValue = "";
    private Rectangle boundingRect = null;
    private Cursor curCursor;
    private ArrayList<AnnotationObj> lineObj;
    private ArrayList<AnnotationObj> rectObj;
    private ArrayList<AnnotationObj> ellipseObj;
    private ArrayList<AnnotationObj> scoutObj;
    private Annotation annotation;
    private boolean focusGained;
    private static String tool = "";

    /** Creates new form DateFormatPanel */
    public AnnotationPanel(LayeredCanvas l) {
        initComponents();
        setOpaque(false);
        layeredCanvas = l;
        this.addFocusListener(this);
        this.addMouseListener(this);
        this.addMouseMotionListener(this);
        this.addMouseWheelListener(this);
        lineObj = new ArrayList<AnnotationObj>();
        rectObj = new ArrayList<AnnotationObj>();
        ellipseObj = new ArrayList<AnnotationObj>();
        scoutObj = new ArrayList<AnnotationObj>();
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

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
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
        this.layeredCanvas.canvas.requestFocus();

        if (addLine || addRect || addEllipse) {
            /* if (startAnnotation) {
            mouseLocX1 = evt.getX();
            mouseLocY1 = evt.getY();
            // this.addMouseMotionListener(this);
            startAnnotation = false;
            }
             */
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
    }

    /**
     * This routine used to deleted the rectangle seleced.
     * @param evt
     */
    private void deleteRectangle(java.awt.event.MouseEvent evt) {
        Iterator<AnnotationObj> ite1 = rectObj.iterator();
        while (ite1.hasNext()) {
            AnnotationObj t = ite1.next();
            ShapeCoordinates shapeCoordinates = new ShapeCoordinates(t.getX1(), t.getY1(), t.getX2(), t.getY2());
            Shape test2 = new Rectangle2D.Float(shapeCoordinates.getX(), shapeCoordinates.getY(), shapeCoordinates.getWidth(), shapeCoordinates.getHeight());
            if (test2.contains(evt.getX() / this.layeredCanvas.imgpanel.getScaleFactor(), evt.getY() / this.layeredCanvas.imgpanel.getScaleFactor())) {
                rectObj.remove(t);
                break;
            }
        }
    }

    /**
     * This routine used to deleted line selected.
     * @param evt
     */
    private void deleteLine(java.awt.event.MouseEvent evt) {
        Iterator<AnnotationObj> ite = lineObj.iterator();
        while (ite.hasNext()) {
            AnnotationObj t = ite.next();
            ShapeCoordinates shapeCoordinates = new ShapeCoordinates(t.getX1(), t.getY1(), t.getX2(), t.getY2());
            Shape test = new Rectangle2D.Float(shapeCoordinates.getX(), shapeCoordinates.getY(), shapeCoordinates.getWidth(), shapeCoordinates.getHeight());
            if (test.contains(evt.getX() / this.layeredCanvas.imgpanel.getScaleFactor(), evt.getY() / this.layeredCanvas.imgpanel.getScaleFactor())) {
                lineObj.remove(t);
                break;

            }
        }
    }

    /**
     * This routine used to delete the eclipse.
     * @param evt
     */
    private void deleteEllipse(java.awt.event.MouseEvent evt) {
        Iterator<AnnotationObj> ite2 = ellipseObj.iterator();
        while (ite2.hasNext()) {
            AnnotationObj t = ite2.next();
            ShapeCoordinates shapeCoordinates = new ShapeCoordinates(t.getX1(), t.getY1(), t.getX2(), t.getY2());
            Shape test2 = new Ellipse2D.Float(shapeCoordinates.getX(), shapeCoordinates.getY(), shapeCoordinates.getWidth(), shapeCoordinates.getHeight());
            if (test2.contains(evt.getX() / this.layeredCanvas.imgpanel.getScaleFactor(), evt.getY() / this.layeredCanvas.imgpanel.getScaleFactor())) {
                ellipseObj.remove(t);
                break;
            }
        }
    }

    /**
     * This routine used to resize the measurement.
     * @param evt
     */
    private void measurementResize(java.awt.event.MouseEvent evt) {
        Iterator<AnnotationObj> ite = lineObj.iterator();
        while (ite.hasNext()) {
            AnnotationObj t = ite.next();
            Shape test = new Line2D.Float(t.getX1(), t.getY1(), t.getX2(), t.getY2());
            if (test.contains(evt.getX(), evt.getY())) {
                System.out.println("line found");
            }

        }
        Iterator<AnnotationObj> ite1 = rectObj.iterator();
        while (ite1.hasNext()) {
            System.out.println("while loop rect");
            AnnotationObj t = ite1.next();
            Shape test2 = new Rectangle2D.Float(t.getX1(), t.getY1(), t.getX2() - t.getX1(), t.getY2() - t.getY1());

            if (test2.contains(evt.getX(), evt.getY())) {
                System.out.println("rect found");
                rectObj.remove(t);
                break;
            }
        }
    }

    /**
     * This routine used to move the measurement.
     * @param evt
     */
    private void measurementMove(java.awt.event.MouseEvent evt) {
        addShapeToArray();
        moveLine(evt);
        moveRectangle(evt);
        moveEllipse(evt);
    }
    private String selectedShapeOrientation = "left";

    /**
     * This routine used to move the line.
     * @param evt
     */
    private void moveLine(java.awt.event.MouseEvent evt) {
        Iterator<AnnotationObj> ite = lineObj.iterator();
        while (ite.hasNext()) {
            AnnotationObj t = ite.next();
            ShapeCoordinates shapeCoordinates = new ShapeCoordinates(t.getX1(), t.getY1(), t.getX2(), t.getY2());
            Shape test = new Rectangle2D.Float(shapeCoordinates.getX(), shapeCoordinates.getY(), shapeCoordinates.getWidth(), shapeCoordinates.getHeight());
            if (test.contains(evt.getX() / this.layeredCanvas.imgpanel.getScaleFactor(), evt.getY() / this.layeredCanvas.imgpanel.getScaleFactor())) {
                int diffX = t.getX2() - t.getX1();
                int diffY = t.getY2() - t.getY1();
                if ((diffX < 0 && diffY < 0) || (diffX > 0 && diffY > 0)) {
                    selectedShapeOrientation = "left";
                } else {
                    selectedShapeOrientation = "right";
                }
                boundingRect = test.getBounds();
                seletedShape = test;
                selectedShapeType = "line";
                selectedShapeDisplayStringValue = t.getLength();
                lineObj.remove(t);
                break;
            }
        }
    }

    /**
     * This routine used to move the rectangle.
     * @param evt
     */
    private void moveRectangle(java.awt.event.MouseEvent evt) {
        Iterator<AnnotationObj> ite1 = rectObj.iterator();
        while (ite1.hasNext()) {
            AnnotationObj t = ite1.next();
            ShapeCoordinates shapeCoordinates = new ShapeCoordinates(t.getX1(), t.getY1(), t.getX2(), t.getY2());
            Shape test2 = new Rectangle2D.Float(shapeCoordinates.getX(), shapeCoordinates.getY(), shapeCoordinates.getWidth(), shapeCoordinates.getHeight());
            if (test2.contains(evt.getX() / this.layeredCanvas.imgpanel.getScaleFactor(), evt.getY() / this.layeredCanvas.imgpanel.getScaleFactor())) {
                boundingRect = test2.getBounds();
                seletedShape = test2;
                selectedShapeType = "rect";
                selectedShapeDisplayStringValue = t.getArea();
                rectObj.remove(t);
                break;
            }
        }
    }

    /**
     * This routine used to move eclipse.
     * @param evt
     */
    private void moveEllipse(java.awt.event.MouseEvent evt) {
        Iterator<AnnotationObj> ite2 = ellipseObj.iterator();
        while (ite2.hasNext()) {
            AnnotationObj t = ite2.next();
            ShapeCoordinates shapeCoordinates = new ShapeCoordinates(t.getX1(), t.getY1(), t.getX2(), t.getY2());
            Shape test2 = new Ellipse2D.Float(shapeCoordinates.getX(), shapeCoordinates.getY(), shapeCoordinates.getWidth(), shapeCoordinates.getHeight());
            if (test2.contains(evt.getX() / this.layeredCanvas.imgpanel.getScaleFactor(), evt.getY() / this.layeredCanvas.imgpanel.getScaleFactor())) {
                boundingRect = test2.getBounds();
                seletedShape = test2;
                selectedShapeType = "ellipse";
                selectedShapeDisplayStringValue = t.getArea();
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
        if (seletedShape != null) {
            if (selectedShapeType.equalsIgnoreCase("line")) {
                AnnotationObj newLine = new AnnotationObj();
                newLine.setMidX(256);
                newLine.setMidY(256);
                if (selectedShapeOrientation.equalsIgnoreCase("left")) {
                    newLine.setLocation(seletedShape.getBounds().x, seletedShape.getBounds().y, seletedShape.getBounds().x + seletedShape.getBounds().width, seletedShape.getBounds().y + seletedShape.getBounds().height);
                } else {
                    newLine.setLocation(seletedShape.getBounds().x + seletedShape.getBounds().width, seletedShape.getBounds().y, seletedShape.getBounds().x, seletedShape.getBounds().y + seletedShape.getBounds().height);
                }
                newLine.setType("line");
                newLine.setLength(selectedShapeDisplayStringValue);
                lineObj.add(newLine);
                lineCount++;
                seletedShape = null;
                boundingRect = null;
            } else if (selectedShapeType.equalsIgnoreCase("rect")) {
                AnnotationObj newRect = new AnnotationObj();
                newRect.setMidX(256);
                newRect.setMidY(256);
                newRect.setLocation(seletedShape.getBounds().x, seletedShape.getBounds().y, seletedShape.getBounds().x + seletedShape.getBounds().width, seletedShape.getBounds().y + seletedShape.getBounds().height);
                newRect.setType("rect");
                newRect.setArea(selectedShapeDisplayStringValue);
                rectObj.add(newRect);
                rectCount++;
                seletedShape = null;
                boundingRect = null;
            } else {
                AnnotationObj newEllipse = new AnnotationObj();
                newEllipse.setMidX(256);
                newEllipse.setMidY(256);
                newEllipse.setLocation(seletedShape.getBounds().x, seletedShape.getBounds().y, seletedShape.getBounds().x + seletedShape.getBounds().width, seletedShape.getBounds().y + seletedShape.getBounds().height);
                newEllipse.setType("ellipse");
                newEllipse.setArea(selectedShapeDisplayStringValue);
                ellipseObj.add(newEllipse);
                ellipticalCount++;
                seletedShape = null;
                boundingRect = null;
            }
        }
    }

    /**
     * This routine used to delete the measurement.
     */
    public void doDeleteMeasurement() {
        if (deleteMeasurement) {
            deleteMeasurement = false;
        } else {
            deleteMeasurement = true;
            resizeMeasurement = false;
            resetSelectionMeasurement();
            addEllipse = false;
            addLine = false;
            addRect = false;
        }
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
            deleteMeasurement = false;
            moveMeasurement = false;
        }
    }

    /**
     * This routine used to move the measurement.
     */
    public void doMoveMeasurement() {
        if (moveMeasurement) {
            moveMeasurement = false;
        } else {
            moveMeasurement = true;
            addEllipse = false;
            addLine = false;
            addRect = false;
            deleteMeasurement = false;
            resizeMeasurement = false;
        }
    }

    /**
     * This routine the used to reset the annotation overlay.
     */
    public void reset() {
        this.setSize(512, 512);
        firstTime = true;
        repaint();
    }

    public void resizeHandler() {
        firstTime = true;
        repaint();
    }

    /**
     * This routine used to find out the mode whether a ellipse can be added to the overlay.
     * @return
     */
    public boolean isAddEllipse() {
        return addEllipse;
    }

    /**
     * This routine used to set the annotation overlay mode to draw the ellipse based on the coordinates.
     * @param addEllipse
     */
    public void setAddEllipse(boolean addEllipse) {
        this.addEllipse = addEllipse;
        resetEditMeasurement();
    }

    /**
     * This routine used to find out the mode whether a line can be added to the overlay.
     * @return
     */
    public boolean isAddLine() {
        return addLine;
    }

    /**
     * This routine used to set the annotation overlay mode to draw the line based on the coordinates.
     * @param addLine
     */
    public void setAddLine(boolean addLine) {
        this.addLine = addLine;
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
        rotateRectangleRight();
        rotateEllipseRight();
        resetMousePoints();
        this.repaint();

    }

    /**
     * This routine used to reset the mouse points stored in the annotation overlay.
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
            int x1 = (int) -1 * t.getCenterBasedY1();
            int y1 = (int) 1 * t.getCenterBasedX1();
            int x2 = (int) -1 * t.getCenterBasedY2();
            int y2 = (int) 1 * t.getCenterBasedX2();
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
            int x1 = (int) -1 * t.getCenterBasedY1();
            int y1 = (int) 1 * t.getCenterBasedX1();
            int x2 = (int) -1 * t.getCenterBasedY2();
            int y2 = (int) 1 * t.getCenterBasedX2();
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
            int x1 = (int) -1 * t.getCenterBasedY1();
            int y1 = (int) 1 * t.getCenterBasedX1();
            int x2 = (int) -1 * t.getCenterBasedY2();
            int y2 = (int) 1 * t.getCenterBasedX2();
            t.setCenterBasedLocation(x1, y1, x2, y2);
        }
    }

    /**
     * This routine used to rotate all the annotation to 90 degree left
     */
    public void doRotateLeft() {
        addShapeToArray();
        rotateLineLeft();
        rotateRectangleLeft();
        rotateEllipseLeft();
        resetMousePoints();
        this.repaint();
    }

    /**
     * This routine used to rotate all the lines to 90 degree left
     */
    private void rotateLineLeft() {
        Iterator<AnnotationObj> ite = lineObj.iterator();
        while (ite.hasNext()) {
            AnnotationObj t = ite.next();
            int x1 = (int) 1 * t.getCenterBasedY1();
            int y1 = (int) -1 * t.getCenterBasedX1();
            int x2 = (int) 1 * t.getCenterBasedY2();
            int y2 = (int) -1 * t.getCenterBasedX2();
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
            int x1 = (int) 1 * t.getCenterBasedY1();
            int y1 = (int) -1 * t.getCenterBasedX1();
            int x2 = (int) 1 * t.getCenterBasedY2();
            int y2 = (int) -1 * t.getCenterBasedX2();
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
            int x1 = (int) 1 * t.getCenterBasedY1();
            int y1 = (int) -1 * t.getCenterBasedX1();
            int x2 = (int) 1 * t.getCenterBasedY2();
            int y2 = (int) -1 * t.getCenterBasedX2();
            t.setCenterBasedLocation(x1, y1, x2, y2);
        }
    }

    /**
     * This routine used to flip all annotations vertically
     */
    public void doFlipVertical() {
        addShapeToArray();
        flipLineVertical();
        flipRectangleVertical();
        flipEllipseVertical();
        resetMousePoints();
        this.repaint();
    }

    /**
     * This routine used to flip all lines vertically
     */
    private void flipLineVertical() {
        Iterator<AnnotationObj> ite = lineObj.iterator();
        while (ite.hasNext()) {
            AnnotationObj t = ite.next();
            int x1 = (int) 1 * t.getCenterBasedX1();
            int y1 = (int) -1 * t.getCenterBasedY1();
            int x2 = (int) 1 * t.getCenterBasedX2();
            int y2 = (int) -1 * t.getCenterBasedY2();
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
            int x1 = (int) 1 * t.getCenterBasedX1();
            int y1 = (int) -1 * t.getCenterBasedY1();
            int x2 = (int) 1 * t.getCenterBasedX2();
            int y2 = (int) -1 * t.getCenterBasedY2();
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
            int x1 = (int) 1 * t.getCenterBasedX1();
            int y1 = (int) -1 * t.getCenterBasedY1();
            int x2 = (int) 1 * t.getCenterBasedX2();
            int y2 = (int) -1 * t.getCenterBasedY2();
            t.setCenterBasedLocation(x1, y1, x2, y2);
        }
    }

    /**
     * This routine used to flip all the annotation horizontally
     */
    public void doFlipHorizontal() {
        addShapeToArray();
        flipLineHorizontal();
        flipRectangleHorizontal();
        flipEllipseHorizontal();
        resetMousePoints();
        this.repaint();

    }

    /**
     * This routine used to flip all the lines horizontally
     */
    private void flipLineHorizontal() {
        Iterator<AnnotationObj> ite = lineObj.iterator();
        while (ite.hasNext()) {
            AnnotationObj t = ite.next();
            int x1 = (int) -1 * t.getCenterBasedX1();
            int y1 = (int) 1 * t.getCenterBasedY1();
            int x2 = (int) -1 * t.getCenterBasedX2();
            int y2 = (int) 1 * t.getCenterBasedY2();
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
            int x1 = (int) -1 * t.getCenterBasedX1();
            int y1 = (int) 1 * t.getCenterBasedY1();
            int x2 = (int) -1 * t.getCenterBasedX2();
            int y2 = (int) 1 * t.getCenterBasedY2();
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
            int x1 = (int) -1 * t.getCenterBasedX1();
            int y1 = (int) 1 * t.getCenterBasedY1();
            int x2 = (int) -1 * t.getCenterBasedX2();
            int y2 = (int) 1 * t.getCenterBasedY2();
            t.setCenterBasedLocation(x1, y1, x2, y2);
        }
    }
    /*
     * Following code will be helpful in future. 
     * int x1 = (int) (-cosninety * t.getCenterBasedX1() + sineninety * t.getCenterBasedY1());
     * int y1 = (int) (-sineninety * t.getCenterBasedX1() - cosninety * t.getCenterBasedY1());
     * int x2 = (int) (-cosninety * t.getCenterBasedX2() + sineninety * t.getCenterBasedY2());
     * int y2 = (int) (-sineninety * t.getCenterBasedX2() - cosninety * t.getCenterBasedY2());*/

    /**
     * This routine used to clear all the measurement from the annotation layer
     */
    public void clearAllMeasurement() {
        lineObj = new ArrayList<AnnotationObj>();
        rectObj = new ArrayList<AnnotationObj>();
        ellipseObj = new ArrayList<AnnotationObj>();
        mouseLocX1 = 0;
        mouseLocX2 = 0;
        mouseLocY1 = 0;
        mouseLocY2 = 0;
        seletedShape = null;
        boundingRect = null;
        this.repaint();
    }

    public void stopPanning() {
        tool = "";
        this.layeredCanvas.imgpanel.tool = "";
    }

    public void focusGained(FocusEvent e) {
        ApplicationContext.annotationPanel = this;
        focusGained = true;
        repaint();
    }

    public void focusLost(FocusEvent e) {
        focusGained = false;
        repaint();
    }

    public boolean isFocusGained() {
        return focusGained;
    }

    @Override
    public void paint(Graphics gs) {
        super.paint(gs);
        if (showAnnotation) {
            Graphics2D g = (Graphics2D) gs;
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (this.layeredCanvas.imgpanel.isScaleFlag()) {
                g.scale(this.layeredCanvas.imgpanel.getScaleFactor(), this.layeredCanvas.imgpanel.getScaleFactor());
            }
            gs.setColor(Color.YELLOW);
            //Condition used to draw new line as per the line flag values and coordinates of the annotation mouse point
            if ((mouseLocX1 != -1 && mouseLocX2 != -1 && mouseLocY1 != -1 && mouseLocY2 != -1) && addLine) {
                gs.drawString("Length:" + calculateDiff((int) (mouseLocX1 / this.layeredCanvas.imgpanel.getScaleFactor()), (int) (mouseLocY1 / this.layeredCanvas.imgpanel.getScaleFactor()), (int) (mouseLocX2 / this.layeredCanvas.imgpanel.getScaleFactor()), (int) (mouseLocY2 / this.layeredCanvas.imgpanel.getScaleFactor())) + " cm", (int) (mouseLocX1 / this.layeredCanvas.imgpanel.getScaleFactor()), (int) ((mouseLocY1 / this.layeredCanvas.imgpanel.getScaleFactor()) - 20));
                g.drawLine((int) (mouseLocX1 / this.layeredCanvas.imgpanel.getScaleFactor()), (int) (mouseLocY1 / this.layeredCanvas.imgpanel.getScaleFactor()), (int) (mouseLocX2 / this.layeredCanvas.imgpanel.getScaleFactor()), (int) (mouseLocY2 / this.layeredCanvas.imgpanel.getScaleFactor()));

            }
            // As per the lineObj present in the tile it will display the lines
            Iterator<AnnotationObj> ite = lineObj.iterator();
            while (ite.hasNext()) {
                AnnotationObj t = ite.next();
                gs.drawString("Length:" + t.getLength() + " cm", t.getX1(), t.getY1() - 20);
                gs.drawLine(t.getX1(), t.getY1(), t.getX2(), t.getY2());

            }
            //Condition used to check the coordinate position and rectangle flag
            if ((mouseLocX1 != -1 && mouseLocX2 != -1 && mouseLocY1 != -1 && mouseLocY2 != -1) && addRect) {

                ShapeCoordinates shapeCoordinates = new ShapeCoordinates((int) (mouseLocX1 / this.layeredCanvas.imgpanel.getScaleFactor()), (int) (mouseLocY1 / this.layeredCanvas.imgpanel.getScaleFactor()), (int) (mouseLocX2 / this.layeredCanvas.imgpanel.getScaleFactor()), (int) (mouseLocY2 / this.layeredCanvas.imgpanel.getScaleFactor()));
                //Draw new string for area,mean and stddev for the current shape
                gs.drawString("Area:" + calculateArea((int) (mouseLocX1 / this.layeredCanvas.imgpanel.getScaleFactor()), (int) (mouseLocY1 / this.layeredCanvas.imgpanel.getScaleFactor()), (int) (mouseLocX2 / this.layeredCanvas.imgpanel.getScaleFactor()), (int) (mouseLocY2 / this.layeredCanvas.imgpanel.getScaleFactor())) + " cm^2", shapeCoordinates.getX(), shapeCoordinates.getY() - 60);
                gs.drawString("Mean:", shapeCoordinates.getX(), shapeCoordinates.getY() - 40);
                gs.drawString("Std Dev:", shapeCoordinates.getX(), shapeCoordinates.getY() - 20);
                gs.drawRect(shapeCoordinates.getX(), shapeCoordinates.getY(), shapeCoordinates.getWidth(), shapeCoordinates.getHeight());
            }
            //As per the rectObj present in the list it will iterate and draw the rectanlge
            Iterator<AnnotationObj> ite1 = rectObj.iterator();
            while (ite1.hasNext()) {
                AnnotationObj t = ite1.next();
                //Draws new string for area,mean,stddev for the current shape
                ShapeCoordinates shapeCoordinates = new ShapeCoordinates(t.getX1(), t.getY1(), t.getX2(), t.getY2());
                gs.drawString("Area:" + t.getArea() + " cm^2", shapeCoordinates.getX(), shapeCoordinates.getY() - 60);
                gs.drawString("Mean:", shapeCoordinates.getX(), shapeCoordinates.getY() - 40);
                gs.drawString("Std Dev:", shapeCoordinates.getX(), shapeCoordinates.getY() - 20);
                gs.drawRect(shapeCoordinates.getX(), shapeCoordinates.getY(), shapeCoordinates.getWidth(), shapeCoordinates.getHeight());
            }
            //Condition used to check the current cooridnates and ellipse flag
            if ((mouseLocX1 != -1 && mouseLocX2 != -1 && mouseLocY1 != -1 && mouseLocY2 != -1) && addEllipse) {
//           //Draws the strings such as area,mean,stddev for the current shape
                ShapeCoordinates shapeCoordinates = new ShapeCoordinates((int) (mouseLocX1 / this.layeredCanvas.imgpanel.getScaleFactor()), (int) (mouseLocY1 / this.layeredCanvas.imgpanel.getScaleFactor()), (int) (mouseLocX2 / this.layeredCanvas.imgpanel.getScaleFactor()), (int) (mouseLocY2 / this.layeredCanvas.imgpanel.getScaleFactor()));
                gs.drawString("Area:" + calculateOvalArea((int) (mouseLocX1 / this.layeredCanvas.imgpanel.getScaleFactor()), (int) (mouseLocY1 / this.layeredCanvas.imgpanel.getScaleFactor()), (int) (mouseLocX2 / this.layeredCanvas.imgpanel.getScaleFactor()), (int) (mouseLocY2 / this.layeredCanvas.imgpanel.getScaleFactor())) + " cm^2", shapeCoordinates.getX(), shapeCoordinates.getY() - 60);
                gs.drawString("Mean:", shapeCoordinates.getX(), shapeCoordinates.getY() - 40);
                gs.drawString("Std Dev:", shapeCoordinates.getX(), shapeCoordinates.getY() - 20);
                gs.drawOval(shapeCoordinates.getX(), shapeCoordinates.getY(), shapeCoordinates.getWidth(), shapeCoordinates.getHeight());
            }
            //As per the ellipseObj present in the list it will iterate and draw the ellipse
            Iterator<AnnotationObj> ite2 = ellipseObj.iterator();
            while (ite2.hasNext()) {
                AnnotationObj t = ite2.next();
                //Used to draw the area,mean and std dev values in the annotation panel
                ShapeCoordinates shapeCoordinates = new ShapeCoordinates(t.getX1(), t.getY1(), t.getX2(), t.getY2());
                gs.drawString("Area:" + t.getArea() + " cm^2", shapeCoordinates.getX(), shapeCoordinates.getY() - 60);
                gs.drawString("Mean:", shapeCoordinates.getX(), shapeCoordinates.getY() - 40);
                gs.drawString("Std Dev:", shapeCoordinates.getX(), shapeCoordinates.getY() - 20);
                gs.drawOval(shapeCoordinates.getX(), shapeCoordinates.getY(), shapeCoordinates.getWidth(), shapeCoordinates.getHeight());
            }
            //Condition used to check the panel is called for the first time
            if (firstTime) {
                centerImage();
                this.setSize(layeredCanvas.imgpanel.getSize().width, layeredCanvas.imgpanel.getSize().height);
                firstTime = false;
                this.repaint();
            }
            // Selecting the annotations
            if (boundingRect != null) {
                drawHighlightSquares(g, boundingRect);
            }
            if (curCursor != null) {
                setCursor(curCursor);
            }
            if (seletedShape != null) {
                g.draw(seletedShape);
                if (selectedShapeType.equalsIgnoreCase("line")) {
                    gs.drawString("Length:" + selectedShapeDisplayStringValue + " cm", seletedShape.getBounds().x, seletedShape.getBounds().y - 20);
                    if (selectedShapeOrientation.equalsIgnoreCase("left")) {
                        gs.drawLine(seletedShape.getBounds().x, seletedShape.getBounds().y, seletedShape.getBounds().x + seletedShape.getBounds().width, seletedShape.getBounds().y + seletedShape.getBounds().height);
                    } else {
                        gs.drawLine(seletedShape.getBounds().x + seletedShape.getBounds().width, seletedShape.getBounds().y, seletedShape.getBounds().x, seletedShape.getBounds().y + seletedShape.getBounds().height);
                    }
                } else if (selectedShapeType.equalsIgnoreCase("ellipse")) {
                    //Used to draw the area,mean and std dev values in the annotation panel
                    gs.drawString("Area:" + selectedShapeDisplayStringValue + " cm^2", seletedShape.getBounds().x, seletedShape.getBounds().y - 60);
                    gs.drawString("Mean:", seletedShape.getBounds().x, seletedShape.getBounds().y - 40);
                    gs.drawString("Std Dev:", seletedShape.getBounds().x, seletedShape.getBounds().y - 20);
                    gs.drawOval(seletedShape.getBounds().x, seletedShape.getBounds().y, seletedShape.getBounds().width, seletedShape.getBounds().height);

                } else {
                    gs.drawString("Area:" + selectedShapeDisplayStringValue + " cm^2", seletedShape.getBounds().x, seletedShape.getBounds().y - 60);
                    gs.drawString("Mean:", seletedShape.getBounds().x, seletedShape.getBounds().y - 40);
                    gs.drawString("Std Dev:", seletedShape.getBounds().x, seletedShape.getBounds().y - 20);
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

        this.repaint();

    }

    /**
     * This routine used to draw the highlight square around the selected measurement.
     * @param g2D
     * @param r
     */
    public void drawHighlightSquares(Graphics2D g2D, Rectangle r) {
        double x = r.getX();
        double y = r.getY();
        double w = r.getWidth();
        double h = r.getHeight();
        g2D.setColor(Color.RED);
        g2D.fill(new Rectangle.Double(x - 3.0, y - 3.0, 6.0, 6.0));
        g2D.fill(new Rectangle.Double(x + w * 0.5 - 3.0, y - 3.0, 6.0, 6.0));
        g2D.fill(new Rectangle.Double(x + w - 3.0, y - 3.0, 6.0, 6.0));
        g2D.fill(new Rectangle.Double(x - 3.0, y + h * 0.5 - 3.0, 6.0, 6.0));
        g2D.fill(new Rectangle.Double(x + w - 3.0, y + h * 0.5 - 3.0, 6.0, 6.0));
        g2D.fill(new Rectangle.Double(x - 3.0, y + h - 3.0, 6.0, 6.0));
        g2D.fill(new Rectangle.Double(x + w * 0.5 - 3.0, y + h - 3.0, 6.0, 6.0));
        g2D.fill(new Rectangle.Double(x + w - 3.0, y + h - 3.0, 6.0, 6.0));
    }

    public void mouseDragged(MouseEvent e) {
        if (tool.equalsIgnoreCase("panning")) {
            mouseLocX2 = e.getX();
            mouseLocY2 = e.getY();
            // curCursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
            this.setLocation(this.getBounds().x + mouseLocX2 - mouseLocX1, this.getBounds().y + mouseLocY2 - mouseLocY1);
            repaint();
        } else if (moveMeasurement) {
            if (seletedShape != null) {
                mouseLocX2 = e.getX();
                mouseLocY2 = e.getY();
                Shape temp = new Rectangle2D.Float(seletedShape.getBounds().x + mouseLocX2 - mouseLocX1, seletedShape.getBounds().y + mouseLocY2 - mouseLocY1, seletedShape.getBounds().width, seletedShape.getBounds().height);
                seletedShape = temp;
                boundingRect = temp.getBounds();
                mouseLocX1 = mouseLocX2;
                mouseLocY1 = mouseLocY2;
                repaint();
            }
        } else if (this.isAddEllipse() || this.isAddRect() || this.isAddLine()) {
            if (!startAnnotation) {
                mouseLocX2 = e.getX();
                mouseLocY2 = e.getY();
                repaint();
            }
        } else if (resizeMeasurement) {
            if (seletedShape != null) {
            }
        }
        this.layeredCanvas.imgpanel.mouseDragged(e);
    }

    private void centerImage() {
        int xPosition = (layeredCanvas.getSize().width - this.getSize().width) / 2;
        int yPosition = (layeredCanvas.getSize().height - this.getSize().height) / 2;
        this.setBounds(xPosition, yPosition, this.getSize().width, this.getSize().height);
    }

    public void mouseMoved(MouseEvent e) {
        if (this.isAddEllipse() || this.isAddRect() || this.isAddLine()) {
//            if (!startAnnotation) {
//                mouseLocX2 = e.getX();
//                mouseLocY2 = e.getY();
//                repaint();
//            }
        } else if (moveMeasurement) {
        } else {
            this.layeredCanvas.imgpanel.mouseMoved(e);
        }
    }

    public void doZoomIn() {
        scaleProcess();
    }

    public void doZoomOut() {
        if (this.layeredCanvas.imgpanel.getScaleFactor() > 0) {
            scaleProcess();
        }
    }

    private void scaleProcess() {
        double currentWidth = this.getSize().width;
        double currentHeight = this.getSize().height;
        double newWidth = this.layeredCanvas.imgpanel.getOriginalWidth() * this.layeredCanvas.imgpanel.getScaleFactor();
        double newHeight = this.layeredCanvas.imgpanel.getOriginalHeight() * this.layeredCanvas.imgpanel.getScaleFactor();
        double widthDiff = newWidth - currentWidth;
        double heightDiff = newHeight - currentHeight;
        int currentX = this.getBounds().x;
        int currentY = this.getBounds().y;
        double newX = currentX - (widthDiff / 2);
        double newY = currentY - (heightDiff / 2);
        this.setBounds((int) newX, (int) newY, (int) newWidth, (int) newHeight);
        this.revalidate();
        repaint();
    }

    /**
     * This routine used to calculate the difference based on the coordinates passed as parameter.
     * @param mouseLocX1
     * @param mouseLocY1
     * @param mouseLocX2
     * @param mouseLocY2
     * @return
     */
    private String calculateDiff(int mouseLocX1, int mouseLocY1, int mouseLocX2, int mouseLocY2) {
        double diff;
        if (mouseLocX1 == mouseLocX2) {
            diff = (double) ((mouseLocY2 - mouseLocY1) * (this.layeredCanvas.imgpanel.getPixelSpacingY()));
        } else if (mouseLocY1 == mouseLocY2) {
            diff = (double) ((mouseLocX2 - mouseLocX1) * (this.layeredCanvas.imgpanel.getPixelSpacingX()));
        } else {
            diff = (double) Math.sqrt(Math.pow((mouseLocY2 - mouseLocY1) * (this.layeredCanvas.imgpanel.getPixelSpacingY()), 2) + Math.pow((mouseLocX2 - mouseLocX1) * (this.layeredCanvas.imgpanel.getPixelSpacingX()), 2));
        }
        double diff1 = diff / 10;
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(3);

        return "" + nf.format(diff1);

    }

    /**
     * This routine used to calculate the area based on coordinates passed as parameter.
     * @param mouseLocX1
     * @param mouseLocY1
     * @param mouseLocX2
     * @param mouseLocY2
     * @return
     */
    private String calculateArea(int mouseLocX1, int mouseLocY1, int mouseLocX2, int mouseLocY2) {
        int diffY, diffX;
        double diff;
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
        diff = (double) (((diffY) * this.layeredCanvas.imgpanel.getPixelSpacingY()) * ((diffX) * this.layeredCanvas.imgpanel.getPixelSpacingX()));
        double diff1 = diff / 100;
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(3);
        return "" + nf.format(diff1);

    }

    /**
     * This routine used to calculate the oval area based on the coordinates passed as parameter.
     * @param mouseLocX1
     * @param mouseLocY1
     * @param mouseLocX2
     * @param mouseLocY2
     * @return
     */
    public String calculateOvalArea(int mouseLocX1, int mouseLocY1, int mouseLocX2, int mouseLocY2) {
        int diffY, diffX;
        double diff;
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

        width = ((diffX) * this.layeredCanvas.imgpanel.getPixelSpacingX());
        height = ((diffY) * this.layeredCanvas.imgpanel.getPixelSpacingY());

        diff = (double) (Math.PI * (width * 0.5) * (height * 0.5));

        //in order to get cm2 values
        double diff1 = diff / 100;

        NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(3);

        return "" + nf.format(diff1);
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

    public ArrayList<AnnotationObj> getRectObj() {
        return rectObj;
    }

    public void setRectObj(ArrayList<AnnotationObj> rectObj) {
        this.rectObj = rectObj;
    }

    public Annotation getAnnotation() {
        annotation = new Annotation();
        if(seletedShape!=null)
            addShapeToArray();
        annotation.setEllipse(ellipseObj);
        annotation.setRect(rectObj);
        annotation.setLine(lineObj);
        resetCursorPoints();
        return annotation;
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
            rectObj = annotation.getRect();
            ellipseObj = annotation.getEllipse();
        } else {
            lineObj = new ArrayList<AnnotationObj>();
            rectObj = new ArrayList<AnnotationObj>();
            ellipseObj = new ArrayList<AnnotationObj>();
        }
        this.repaint();
    }

    public void mouseClicked(MouseEvent e) {
    }

    public void mousePressed(MouseEvent e) {
        this.requestFocus();
        this.layeredCanvas.imgpanel.mousePressed(e);
        if (tool.equalsIgnoreCase("panning")) {
            mouseLocX1 = e.getX();
            mouseLocY1 = e.getY();
        } else if (moveMeasurement) {
            mouseLocX1 = e.getX();
            mouseLocY1 = e.getY();
            measurementMove(e);
        }
        if (addLine || addRect || addEllipse) {
            if (startAnnotation) {
                mouseLocX1 = e.getX();
                mouseLocY1 = e.getY();
                mouseLocX2 = e.getX();
                mouseLocY2 = e.getY();
                startAnnotation = false;
            }
        }
    }

    public void mouseReleased(MouseEvent e) {
        if (addLine || addRect || addEllipse) {

            if (!startAnnotation) {
                startAnnotation = true;

                if (mouseLocX1 != mouseLocX2 || mouseLocY1 != mouseLocY2) {

                    if (addLine) {
                        AnnotationObj newLine = new AnnotationObj();
                        newLine.setMidX(256);
                        newLine.setMidY(256);
                        newLine.setLocation((int) (mouseLocX1 / this.layeredCanvas.imgpanel.getScaleFactor()), (int) (mouseLocY1 / this.layeredCanvas.imgpanel.getScaleFactor()), (int) (e.getX() / this.layeredCanvas.imgpanel.getScaleFactor()), (int) (e.getY() / this.layeredCanvas.imgpanel.getScaleFactor()));
                        newLine.setType("line");
                        newLine.setLength(calculateDiff((int) (mouseLocX1 / this.layeredCanvas.imgpanel.getScaleFactor()), (int) (mouseLocY1 / this.layeredCanvas.imgpanel.getScaleFactor()), (int) (e.getX() / this.layeredCanvas.imgpanel.getScaleFactor()), (int) (e.getY() / this.layeredCanvas.imgpanel.getScaleFactor())));
                        lineObj.add(newLine);
                        lineCount++;
                    }
                    if (addRect) {
                        AnnotationObj newRect = new AnnotationObj();
                        newRect.setMidX(256);
                        newRect.setMidY(256);
                        newRect.setLocation((int) (mouseLocX1 / this.layeredCanvas.imgpanel.getScaleFactor()), (int) (mouseLocY1 / this.layeredCanvas.imgpanel.getScaleFactor()), (int) (e.getX() / this.layeredCanvas.imgpanel.getScaleFactor()), (int) (e.getY() / this.layeredCanvas.imgpanel.getScaleFactor()));
                        newRect.setType("rect");
                        newRect.setArea(calculateArea((int) (mouseLocX1 / this.layeredCanvas.imgpanel.getScaleFactor()), (int) (mouseLocY1 / this.layeredCanvas.imgpanel.getScaleFactor()), (int) (e.getX() / this.layeredCanvas.imgpanel.getScaleFactor()), (int) (e.getY() / this.layeredCanvas.imgpanel.getScaleFactor())));
                        rectObj.add(newRect);
                        rectCount++;
                    }
                    if (addEllipse) {
                        AnnotationObj newEllipse = new AnnotationObj();
                        newEllipse.setMidX(256);
                        newEllipse.setMidY(256);
                        newEllipse.setLocation((int) (mouseLocX1 / this.layeredCanvas.imgpanel.getScaleFactor()), (int) (mouseLocY1 / this.layeredCanvas.imgpanel.getScaleFactor()), (int) (e.getX() / this.layeredCanvas.imgpanel.getScaleFactor()), (int) (e.getY() / this.layeredCanvas.imgpanel.getScaleFactor()));
                        newEllipse.setType("ellipse");
                        newEllipse.setArea(calculateOvalArea((int) (mouseLocX1 / this.layeredCanvas.imgpanel.getScaleFactor()), (int) (mouseLocY1 / this.layeredCanvas.imgpanel.getScaleFactor()), (int) (e.getX() / this.layeredCanvas.imgpanel.getScaleFactor()), (int) (e.getY() / this.layeredCanvas.imgpanel.getScaleFactor())));
                        ellipseObj.add(newEllipse);
                        ellipticalCount++;
                    }
                } else {
                    mouseLocX1 = -1;
                    mouseLocX2 = -1;
                    mouseLocY1 = -1;
                    mouseLocY2 = -1;
                }

            }//
        }
        this.layeredCanvas.imgpanel.mouseReleased(e);
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mouseWheelMoved(MouseWheelEvent e) {
        this.requestFocus();
        this.layeredCanvas.imgpanel.mouseWheelMoved(e);
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
