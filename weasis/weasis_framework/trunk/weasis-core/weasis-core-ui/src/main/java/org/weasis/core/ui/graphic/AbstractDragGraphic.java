/*******************************************************************************
 * Copyright (c) 2010 Nicolas Roduit.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Nicolas Roduit - initial API and implementation
 ******************************************************************************/
package org.weasis.core.ui.graphic;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;

import javax.media.jai.PlanarImage;
import javax.media.jai.iterator.RectIter;
import javax.media.jai.iterator.RectIterFactory;

import org.weasis.core.api.gui.Image2DViewer;
import org.weasis.core.api.image.util.ImageLayer;
import org.weasis.core.api.media.data.ImageElement;
import org.weasis.core.ui.graphic.model.GraphicsPane;

/**
 * The Class AbstractDragGraphic.
 * 
 * @author Nicolas Roduit
 */
public abstract class AbstractDragGraphic implements Graphic, Cloneable {

    protected float lineThickness;
    protected Paint paint;
    protected boolean filled;
    // private transient AbstractLayer layer;
    protected transient PropertyChangeSupport pcs;
    protected transient boolean selected;
    protected transient boolean showLabel;
    protected transient boolean createPoints;
    protected transient Stroke stroke;
    protected transient AffineTransform affineTransform;
    protected transient Shape transformedShape;
    protected transient Shape shape;
    private transient String[] label;
    protected transient Rectangle2D labelBound;

    /**
     * The Class DefaultDragSequence.
     * 
     * @author Nicolas Roduit
     */
    protected class DefaultDragSequence implements DragSequence {

        public void startDrag(MouseEvent mouseevent) {
            update(mouseevent);
        }

        public void drag(MouseEvent mouseevent) {
            int i = mouseevent.getX();
            int j = mouseevent.getY();
            if (i - getLastX() != 0 || j - getLastY() != 0) {
                switch (getType()) {
                    case -1:
                        move(getType(), i - getLastX(), j - getLastY(), mouseevent);
                        break;
                    default:
                        int k;
                        // For a better synchronization, use the difference between the previous position
                        k = resizeOnDrawing(getType(), i - getLastX(), j - getLastY(), mouseevent);

                        type = invertCorner(getType(), k);
                        break;
                }
                update(mouseevent);
            }
        }

        public boolean completeDrag(MouseEvent mouseevent) {
            if (createPoints) {
                // closed = true;
                updateShape();
                createPoints = false;
            }
            update(mouseevent);
            return true;
        }

        protected int getType() {
            return type;
        }

        protected int getLastX() {
            return lastX;
        }

        protected int getLastY() {
            return lastY;
        }

        protected void update(int x, int y) {
            lastX = x;
            lastY = y;
        }

        protected void update(MouseEvent mouseevent) {
            lastX = mouseevent.getX();
            lastY = mouseevent.getY();
        }

        private int invertCorner(int i, int j) {
            /*
             * 1 2 3 8 4 7 6 5
             */
            if ((j & 1) == 1) {
                switch (i) {
                    case 1: // '\001'
                        i = 3;
                        break;
                    case 3: // '\003'
                        i = 1;
                        break;
                    case 0: // '\004'
                        i = 4;
                        break;
                    case 4: // '\006'
                        i = 0;
                        break;
                    case 7: // '\007'
                        i = 5;
                        break;
                    case 5: // '\t'
                        i = 7;
                        break;
                }
            }
            if ((j & 2) == 2) {
                switch (i) {
                    case 1: // '\001'
                        i = 7;
                        break;
                    case 2: // '\002'
                        i = 6;
                        break;
                    case 3: // '\003'
                        i = 5;
                        break;
                    case 7: // '\007'
                        i = 1;
                        break;
                    case 6: // '\b'
                        i = 2;
                        break;
                    case 5: // '\t'
                        i = 3;
                        break;
                }
            }
            return i;
        }

        private int lastX;
        private int lastY;
        private int type;

        protected DefaultDragSequence() {
            this(false, -1);
        }

        protected DefaultDragSequence(boolean flag, int i) {
            createPoints = flag;
            // handle point type
            type = i;
        }
    }

    public AbstractDragGraphic() {
        this.showLabel = true;
    }

    // public void setLayer(AbstractLayer layer1) {
    // layer = layer1;
    // }
    //
    // public AbstractLayer getLayer() {
    // return layer;
    // }

    protected AffineTransform getAffineTransform(MouseEvent mouseevent) {
        if (mouseevent != null && mouseevent.getSource() instanceof Image2DViewer) {
            return ((Image2DViewer) mouseevent.getSource()).getAffineTransform();
        }
        return null;
    }

    protected ImageElement getImageElement(MouseEvent mouseevent) {
        if (mouseevent != null && mouseevent.getSource() instanceof Image2DViewer) {
            return ((Image2DViewer) mouseevent.getSource()).getImage();
        }
        return null;
    }

    protected ImageLayer getImageLayer(MouseEvent mouseevent) {
        if (mouseevent != null && mouseevent.getSource() instanceof Image2DViewer) {
            return ((Image2DViewer) mouseevent.getSource()).getImageLayer();
        }
        return null;
    }

    public void setSelected(boolean flag) {
        if (selected != flag) {
            selected = flag;
            // firePropertyChange("selected", !flag, flag);
            firePropertyChange("bounds", null, getTransformedBounds()); //$NON-NLS-1$
        }
    }

    @Override
    public String toString() {
        return getUIName();
    }

    public boolean isSelected() {
        return selected;
    }

    public void setShape(Shape shape, MouseEvent mouseevent) {
        if (shape != null) {
            Rectangle rectangle = getTransformedBounds();
            this.shape = shape;
            affineTransform = getAffineTransform(mouseevent);
            this.transformedShape = affineTransform == null ? shape : affineTransform.createTransformedShape(shape);

            if (showLabel && label != null) {
                Rectangle rectangleOld = labelBound == null ? null : labelBound.getBounds();
                buildLabelBound(mouseevent);
                firePropertyChange("bounds", null, rectangleUnion(rectangleOld, labelBound.getBounds())); //$NON-NLS-1$
            }
            // Fire event to repaint the old position and the new postion of the graphic
            firePropertyChange("bounds", null, rectangleUnion(rectangle, getTransformedBounds())); //$NON-NLS-1$
        }
    }

    protected Rectangle rectangleUnion(Rectangle rectangle, Rectangle rectangle1) {
        if (rectangle == null) {
            return rectangle1;
        }
        return rectangle1 == null ? rectangle : rectangle.union(rectangle1);
    }

    protected void buildLabelBound(MouseEvent mouseevent) {
        if (showLabel && label != null) {
            if (mouseevent != null && mouseevent.getSource() instanceof GraphicsPane
                && ((GraphicsPane) mouseevent.getSource()).getGraphics() instanceof Graphics2D) {
                Graphics2D g2d = (Graphics2D) ((GraphicsPane) mouseevent.getSource()).getGraphics();
                labelBound = null;
                for (String l : label) {
                    Rectangle2D bound = g2d.getFont().getStringBounds(l, g2d.getFontRenderContext());
                    if (labelBound == null || bound.getWidth() > labelBound.getWidth()) {
                        labelBound = bound;
                    }
                }
                if (labelBound != null) {
                    Point p = getLabelPosition();
                    double h = labelBound.getHeight() * label.length + 6;
                    labelBound.setRect(p.x, p.y - h, labelBound.getWidth() + 6, h);
                }
            }
        }
    }

    public Rectangle2D getLabelBound() {
        return labelBound;
    }

    public Shape getShape() {
        return shape;
    }

    public int getHandleSize() {
        // if (layer.getSettingsData() == null) {
        return 6;
        // }
        // else {
        // return layer.getSettingsData().getDis_handleDisplaySize();
        // }

    }

    public void setPaint(Paint paint1) {
        paint = paint1;
        // firePropertyChange("paint", paint2, paint1);
        firePropertyChange("bounds", null, getTransformedBounds()); //$NON-NLS-1$
    }

    public Paint getPaint() {
        return paint;
    }

    public void setFilled(boolean flag) {
        if (filled != flag) {
            filled = flag;
            firePropertyChange("bounds", null, getTransformedBounds()); //$NON-NLS-1$
            // firePropertyChange("selected", !flag, flag);
        }
    }

    public boolean isFilled() {
        return filled;
    }

    protected void updateShape() {
        firePropertyChange("bounds", null, getTransformedBounds()); //$NON-NLS-1$
        // firePropertyChange("shape", null, shape);
    }

    // public void repaint() {
    // AbstractLayer layer1 = getLayer();
    // if (layer1 != null) {
    // layer1.repaint(getAllShapeRepaintBounds());
    // }
    // }

    protected void updateStroke() {
        stroke = new BasicStroke(lineThickness, BasicStroke.CAP_BUTT, BasicStroke.CAP_ROUND);
    }

    public float getLineThickness() {
        return lineThickness;
    }

    public Point getLabelPosition() {
        if (labelBound != null) {
            Rectangle rect = transformedShape.getBounds();
            return new Point(rect.x + 3 + rect.width, (int) (rect.y + 6 + rect.height / 2 + labelBound.getHeight()
                * label.length * 0.5));
        }
        return null;
    }

    public void paint(Graphics2D g2d, AffineTransform transform) {
        Shape shape = getShape();
        if (shape != null) {
            g2d.setPaint(getPaint());
            g2d.setStroke(stroke);
            affineTransform = transform;
            transformedShape = transform == null ? shape : transform.createTransformedShape(shape);
            if (isFilled()) {
                // draw filled shape
                g2d.fill(transformedShape);
            }
            // draw outline shape
            g2d.draw(transformedShape);
            if (selected && !createPoints) {
                paintHandles(g2d, transform);
            }
            if (showLabel && label != null) {
                labelBound = null;
                for (String l : label) {
                    Rectangle2D bound = g2d.getFont().getStringBounds(l, g2d.getFontRenderContext());
                    if (labelBound == null || bound.getWidth() > labelBound.getWidth()) {
                        labelBound = bound;
                    }
                }
                if (labelBound != null) {
                    Point p = getLabelPosition();
                    for (int i = 0; i < label.length; i++) {
                        paintFontOutline(g2d, label[i], p.x + 3, p.y - 6 - (int) labelBound.getHeight() * i);
                    }
                    double h = labelBound.getHeight() * label.length + 6;
                    labelBound.setRect(p.x, p.y - h, labelBound.getWidth() + 6, h);
                }
                // Test, show bound to repaint
                // g2d.draw(getTransformedBounds());
                // g2d.draw(labelBound);
            }
        }
    }

    public void paintFontOutline(Graphics2D g2, String str, float x, float y) {
        g2.setPaint(Color.BLACK);
        g2.drawString(str, x - 1f, y - 1f);
        g2.drawString(str, x - 1f, y);
        g2.drawString(str, x - 1f, y + 1f);
        g2.drawString(str, x, y - 1f);
        g2.drawString(str, x, y + 1f);
        g2.drawString(str, x + 1f, y - 1f);
        g2.drawString(str, x + 1f, y);
        g2.drawString(str, x + 1f, y + 1f);
        g2.setPaint(Color.WHITE);
        g2.drawString(str, x, y);
    }

    protected Stroke getStroke() {
        return stroke;
    }

    /*
     * (non-Javadoc) draw the handles, when the graphic is selected
     * 
     * @see org.weasis.core.ui.graphic.Graphic#paintHandles(java.awt.Graphics2D, java.awt.geom.AffineTransform)
     */
    public void paintHandles(Graphics2D graphics2d, AffineTransform transform) {
        Rectangle rect = getBounds();
        graphics2d.setPaint(Color.black);
        int numPoints = 8;
        int i = getHandleSize();
        int j = i / 2;
        float x = rect.x;
        float y = rect.y;
        float w = x + rect.width;
        float h = y + rect.height;
        float mw = x + rect.width / 2.0f;
        float mh = y + rect.height / 2.0f;

        float[] dstPts = new float[] { x, y, mw, y, w, y, x, mh, w, mh, x, h, mw, h, w, h };
        if (transform != null) {
            transform.transform(dstPts, 0, dstPts, 0, numPoints);
        }
        int k = 0;
        for (int l = numPoints * 2; k < l; k++) {
            graphics2d.fill(new Rectangle2D.Float(dstPts[k] - j, dstPts[++k] - j, i, i));
        }

        k = 0;
        graphics2d.setPaint(Color.white);
        graphics2d.setStroke(new BasicStroke(1.0f));
        for (int l = numPoints * 2; k < l; k++) {
            graphics2d.draw(new Rectangle2D.Float(dstPts[k] - j, dstPts[++k] - j, i, i));
        }
    }

    public void setLineThickness(float f) {
        lineThickness = f;
        updateStroke();
        updateShape();
    }

    /**
     * showProperties
     * 
     * @param p
     *            Point
     */
    public void showProperties() {
        /*
         * JDialog dialog = new MeasureDialog(this); ImageDisplay canvas =
         * getLayer().getShowDrawing().getImageFrame().getImageCanvas(); Point p = canvas.getLocationOnScreen(); Point
         * p2 = canvas.updateMouseToRelativeCoord(new Point(getBounds().x, getBounds().y)); p2.x += p.x; p2.y += p.y;
         * WinUtil.adjustLocationToFitScreen(dialog, p2); dialog.setVisible(true);
         */
    }

    public boolean intersects(Rectangle rectangle) {
        return getArea().intersects(rectangle);
    }

    /*
     * return the rectangle that corresponds to the bounding box of the graphic (when is selected it is the bounding box
     * of handles)
     */
    public Rectangle getRepaintBounds() {
        Shape shape1 = getShape();
        if (shape1 == null) {
            return null;
        } else {
            Rectangle rectangle = shape1.getBounds();
            growHandles(rectangle);
            return rectangle;
        }
    }

    private void growHandles(Rectangle rectangle) {
        int i = getHandleSize();
        int thick = (int) Math.ceil(lineThickness);
        if (thick > i) {
            i = thick;
        }
        // Add 2 pixels tolerance to ensure that the graphic is correctly repainted
        i += 4;
        rectangle.width += i;
        rectangle.height += i;
        i /= 2;
        rectangle.x -= i;
        rectangle.y -= i;
    }

    public Rectangle getTransformedBounds() {
        if (affineTransform == null) {
            return getRepaintBounds();
        }
        Rectangle rectangle = affineTransform.createTransformedShape(getBounds()).getBounds();
        growHandles(rectangle);
        return rectangle;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.weasis.core.ui.graphic.Graphic#getBounds()
     */
    public Rectangle getBounds() {
        Shape shape1 = getShape();
        if (shape1 == null) {
            return null;
        }
        // bound is not accurate with complex shape (it is true for rectangle or ellipse with rotation)
        Rectangle bound = shape1.getBounds();
        if (bound.width < 1) {
            bound.width = 1;
        }
        if (bound.height < 1) {
            bound.height = 1;
        }
        return bound;
    }

    public DragSequence createDragSequence(DragSequence dragsequence, MouseEvent mouseevent) {
        int i = 1;
        if (mouseevent != null && (dragsequence != null || (i = getResizeCorner(mouseevent.getPoint())) == -1)) {
            return createMoveDrag(dragsequence, mouseevent);
        } else {
            // let drag the handle i to resize the graphic
            return createResizeDrag(mouseevent, i);
        }
    }

    // Start a drag sequence to move the graphic
    protected DragSequence createMoveDrag(DragSequence dragsequence, MouseEvent mouseevent) {
        return new DefaultDragSequence();
    }

    protected DragSequence createResizeDrag(MouseEvent mouseevent, int i) {
        // let drag the handle i to resize the graphic
        return new DefaultDragSequence((mouseevent == null), i);
    }

    public Area getArea() {
        return new Area(getShape());
    }

    public int getResizeCorner(final Point pos) {
        // return the selected handle point position
        /*
         * 1 2 3 8 4 7 6 5
         */
        Rectangle rect = getBounds();
        int k = getHandleSize() + 2;
        // Enable to get a better selection of the handle with a low or high magnification zoom
        if (affineTransform != null) {
            double scale = affineTransform.getScaleX();
            k = (int) Math.ceil(k / scale + 1);
        }

        int l = k / 2;
        int i = pos.x - rect.x + l;
        int j = pos.y - rect.y + l;
        int i1 = -1;
        if (i >= 0) {
            if (i < k) {
                i1 = 1;
            } else if (i >= rect.width / 2 && i < rect.width / 2 + k) {
                i1 = 2;
            } else if (i >= rect.width && i < rect.width + k) {
                i1 = 3;
            }
        }
        if (i1 != -1 && j >= 0) {
            if (j >= rect.height / 2 && j < rect.height / 2 + k) {
                if (i1 == 2) { // 5 is the center of the graphic
                    return -1;
                }
                i1 = i1 == 1 ? 0 : 4;
            } else if (j >= rect.height && j < rect.height + k) {
                i1 = i1 == 1 ? 7 : i1 == 2 ? 6 : 5;
            } else if (j >= k) {
                i1 = -1;
            }
        } else {
            i1 = -1;
        }
        return i1;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        AbstractDragGraphic abstractgraphic = (AbstractDragGraphic) super.clone();
        abstractgraphic.pcs = null;
        abstractgraphic.selected = false;
        abstractgraphic.filled = filled;
        return abstractgraphic;
    }

    protected abstract int resizeOnDrawing(int i, int j, int k, MouseEvent mouseevent);

    public abstract Graphic clone(int i, int j);

    protected abstract void updateShapeOnDrawing(MouseEvent mouseevent);

    public void move(int i, int j, int k, MouseEvent mouseevent) {
        // WeasisWin.getInstance().getImageCanvas().moveOrigin(j, k);
    }

    public Point needToMoveCanvas(int x, int y) {
        // WeasisWin imageFrame = WeasisWin.getInstance();
        // if (imageFrame.getProjectSettingsData().isDis_autoMove()) {
        // return null;
        // // return imageFrame.getImageCanvas().stepToMoveCanvas(getRepaintBounds(), x, y);
        // }
        // else {
        return null;
        // }
    }

    protected int adjustBoundsForResize(Rectangle rectangle, int i, int j, int k) {
        switch (i) {
            // handle points
            /*
             * 1 2 3 0 4 7 6 5
             */
            case 0:
                rectangle.x += j;
                rectangle.width -= j;
                break;
            case 1:
                rectangle.x += j;
                rectangle.y += k;
                rectangle.width -= j;
                rectangle.height -= k;
                break;
            case 2:
                rectangle.y += k;
                rectangle.height -= k;
                break;
            case 3:
                rectangle.width += j;
                rectangle.y += k;
                rectangle.height -= k;
                break;
            case 4:
                rectangle.width += j;
                break;
            case 5:
                rectangle.width += j;
                rectangle.height += k;
                break;
            case 6:
                rectangle.height += k;
                break;
            case 7:
                rectangle.x += j;
                rectangle.height += k;
                rectangle.width -= j;
                break;
        }
        int l = 0;
        if (rectangle.width < 0) {
            rectangle.x += rectangle.width;
            rectangle.width = -rectangle.width;
            l |= 1;
        }
        if (rectangle.height < 0) {
            rectangle.y += rectangle.height;
            rectangle.height = -rectangle.height;
            l |= 2;
        }
        return l;
    }

    protected void firePropertyChange(String s, Object obj, Object obj1) {
        if (pcs != null) {
            pcs.firePropertyChange(s, obj, obj1);
        }
    }

    protected void firePropertyChange(String s, int i, int j) {
        if (pcs != null) {
            pcs.firePropertyChange(s, i, j);
        }
    }

    protected void firePropertyChange(String s, boolean flag, boolean flag1) {
        if (pcs != null) {
            pcs.firePropertyChange(s, flag, flag1);
        }
    }

    protected void fireAddAction() {
        if (pcs != null) {
            pcs.firePropertyChange("add.graphic", null, this);
        }
    }

    protected void fireMoveAction() {
        if (pcs != null) {
            pcs.firePropertyChange("move.graphic", null, this);
        }
    }

    public void fireRemoveAction() {
        if (pcs != null) {
            pcs.firePropertyChange("remove.graphic", null, this);
        }
    }

    public void fireRemoveAndRepaintAction() {
        if (pcs != null) {
            pcs.firePropertyChange("remove.repaint.graphic", null, this);
        }
    }

    public void addPropertyChangeListener(PropertyChangeListener propertychangelistener) {
        if (pcs == null) {
            pcs = new PropertyChangeSupport(this);
        }
        // Do not add if already exists
        for (PropertyChangeListener listener : pcs.getPropertyChangeListeners()) {
            if (listener == propertychangelistener) {
                return;
            }
        }
        pcs.addPropertyChangeListener(propertychangelistener);
    }

    public void removePropertyChangeListener(PropertyChangeListener propertychangelistener) {
        if (pcs != null) {
            pcs.removePropertyChangeListener(propertychangelistener);
        }
    }

    public boolean isCreatePoints() {
        return createPoints;
    }

    public String[] getLabel() {
        return label;
    }

    public void setLabel(String[] label) {
        this.label = label;
    }

    protected ArrayList<Integer> getValueFromArea(PlanarImage imageData) {
        if (imageData == null || shape == null) {
            return null;
        }
        Area area = new Area(shape);
        Rectangle bound = area.getBounds();
        bound = imageData.getBounds().intersection(bound);
        if (bound.width == 0 || bound.height == 0) {
            return null;
        }
        RectIter it;
        try {
            it = RectIterFactory.create(imageData, bound);
        } catch (Exception ex) {
            it = null;
        }
        ArrayList<Integer> list = null;

        if (it != null) {
            int band = imageData.getSampleModel().getNumBands();
            list = new ArrayList<Integer>();
            int[] c = { 0, 0, 0 };
            it.startBands();
            it.startLines();
            int y = bound.y;
            while (!it.finishedLines()) {
                it.startPixels();
                int x = bound.x;
                while (!it.finishedPixels()) {
                    if (shape.contains(x, y)) {
                        it.getPixel(c);
                        for (int i = 0; i < band; i++) {
                            list.add(c[i]);
                        }
                    }
                    it.nextPixel();
                    x++;
                }
                it.nextLine();
                y++;
            }
        }
        return list;
    }
}
