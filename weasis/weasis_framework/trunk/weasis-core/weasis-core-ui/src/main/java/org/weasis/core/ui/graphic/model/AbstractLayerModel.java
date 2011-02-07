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
package org.weasis.core.ui.graphic.model;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.media.jai.PlanarImage;
import javax.media.jai.TiledImage;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import org.weasis.core.api.gui.util.ActionW;
import org.weasis.core.api.image.util.LayoutUtil;
import org.weasis.core.ui.Messages;
import org.weasis.core.ui.graphic.AbstractDragGraphic;
import org.weasis.core.ui.graphic.DragLayer;
import org.weasis.core.ui.graphic.Graphic;
import org.weasis.core.ui.graphic.PolygonGraphic;
import org.weasis.core.ui.graphic.SelectGraphic;
import org.weasis.core.ui.graphic.TempLayer;

public class AbstractLayerModel implements LayerModel {

    private static final SelectGraphic selectGraphic = new SelectGraphic();
    public static final Cursor DEFAULT_CURSOR = new Cursor(Cursor.DEFAULT_CURSOR);
    public static final Cursor HAND_CURSOR = getCustomCursor("hand.gif", "hand", 16, 16); //$NON-NLS-1$ //$NON-NLS-2$
    public static final Cursor MOVE_CURSOR = new Cursor(Cursor.MOVE_CURSOR);
    public static final Cursor N_CURSOR = new Cursor(Cursor.N_RESIZE_CURSOR);
    public static final Cursor S_CURSOR = new Cursor(Cursor.S_RESIZE_CURSOR);
    public static final Cursor E_CURSOR = new Cursor(Cursor.E_RESIZE_CURSOR);
    public static final Cursor W_CURSOR = new Cursor(Cursor.W_RESIZE_CURSOR);
    public static final Cursor NE_CURSOR = new Cursor(Cursor.NE_RESIZE_CURSOR);
    public static final Cursor NW_CURSOR = new Cursor(Cursor.NW_RESIZE_CURSOR);
    public static final Cursor SE_CURSOR = new Cursor(Cursor.SE_RESIZE_CURSOR);
    public static final Cursor SW_CURSOR = new Cursor(Cursor.SW_RESIZE_CURSOR);
    public static final Cursor CROSS_CURSOR = new Cursor(Cursor.CROSSHAIR_CURSOR);
    public static final Cursor WAIT_CURSOR = new Cursor(Cursor.WAIT_CURSOR);
    protected Cursor cursor = DEFAULT_CURSOR;
    protected final GraphicsPane canvas;
    protected boolean shapeAction = false;
    private final ArrayList<AbstractLayer> layers;
    private final ArrayList<Graphic> singleList;
    private PropertyChangeSupport pcs;
    private final ArrayList<Graphic> selectedGraphics;
    private Graphic createGraphic;
    private final ArrayList listenerList;
    private boolean layerModelChangeFireingSuspended;
    private float alpha;

    public Object antialiasingOn = RenderingHints.VALUE_ANTIALIAS_ON;
    public Object antialiasingOff = RenderingHints.VALUE_ANTIALIAS_OFF;
    private final boolean crossHairMode = false;
    private Object antialiasing;

    /**
     * The Class PropertyChangeHandler.
     * 
     * @author Nicolas Roduit
     */
    class PropertyChangeHandler implements PropertyChangeListener {

        public void propertyChange(PropertyChangeEvent propertychangeevent) {
            String s = propertychangeevent.getPropertyName();
            if ("visible".equals(s)) { //$NON-NLS-1$
                layerVisibilityChanged((AbstractLayer) propertychangeevent.getSource());
            }
        }

        private PropertyChangeHandler() {
        }
    }

    public AbstractLayerModel(GraphicsPane canvas) {
        this.canvas = canvas;
        layers = new ArrayList<AbstractLayer>();
        selectedGraphics = new ArrayList<Graphic>();
        singleList = new ArrayList(1);
        listenerList = new ArrayList();
        setAlpha(0f);
        setAntialiasing(false);
    }

    public void changeCursorDesign(MouseEvent mouseevent) {
        Point p = mouseevent.getPoint();
        shapeAction = false;
        boolean shift = mouseevent.isShiftDown();
        java.util.List dragGaphs = getSelectedDragableGraphics();
        if (dragGaphs.size() == 1 && !shift) {
            if (dragGaphs.get(0) instanceof AbstractDragGraphic) {
                AbstractDragGraphic graph = (AbstractDragGraphic) dragGaphs.get(0);
                if (graph instanceof SelectGraphic) {
                    canvas.setCursor(cursor);
                    return;
                }
                int direction = graph.getResizeCorner(p);
                if (direction < 0) {
                    if (graph.getArea().contains(p)) {
                        canvas.setCursor(MOVE_CURSOR);
                        shapeAction = true;
                    }
                } else {
                    shapeAction = true;
                    if (dragGaphs.get(0) instanceof PolygonGraphic) {
                        canvas.setCursor(HAND_CURSOR);
                    } else {
                        switch (direction) {
                            case 2:
                            case 6:
                                canvas.setCursor(N_CURSOR);
                                break;
                            case 0:
                            case 4:
                                canvas.setCursor(E_CURSOR);
                                break;
                            case 3:
                            case 7:
                                canvas.setCursor(NE_CURSOR);
                                break;
                            case 1:
                            case 5:
                                canvas.setCursor(NW_CURSOR);
                                break;
                            default:
                                shapeAction = false;
                                break;
                        }
                    }
                }
            }
        } else if (dragGaphs.size() > 1 && !shift) {
            for (int i = 0; i < dragGaphs.size(); i++) {
                AbstractDragGraphic graph = (AbstractDragGraphic) dragGaphs.get(i);
                if (graph.getResizeCorner(p) < 0) {
                    if (graph.getArea().contains(p)) {
                        canvas.setCursor(MOVE_CURSOR);
                        // setCreateGraphic(null);
                        shapeAction = true;
                    }
                }
            }
        }
        if (!shapeAction) {
            canvas.setCursor(cursor);
        }
    }

    public AbstractDragGraphic createGraphic(MouseEvent mouseevent) {
        Graphic obj = getCreateGraphic();
        Tools tool = Tools.MEASURE;
        if (obj == null) {
            tool = Tools.TEMPDRAGLAYER;
            obj = selectGraphic;
        }
        obj = ((AbstractDragGraphic) (obj)).clone(mouseevent.getX(), mouseevent.getY());
        if (obj != null) {
            AbstractLayer layer = getLayer(tool);
            if (!layer.isVisible() || !(Boolean) canvas.getActionValue(ActionW.DRAW.cmd())) {
                JOptionPane
                    .showMessageDialog(
                        canvas,
                        Messages.getString("AbstractLayerModel.msg_not_vis"), Messages.getString("AbstractLayerModel.draw"), //$NON-NLS-1$ //$NON-NLS-2$
                        JOptionPane.ERROR_MESSAGE);
                return null;
            }
            if (layer instanceof DragLayer) {
                ((DragLayer) layer).addGraphic((obj));
            }
        }
        return ((AbstractDragGraphic) (obj));
    }

    public boolean isShapeAction() {
        return shapeAction;
    }

    public void repaint() {
        // repaint du composant ImageDisplay
        canvas.repaint();
    }

    public void repaint(Rectangle rectangle) {

        double viewScale = canvas.getViewModel().getViewScale();
        rectangle.x -= canvas.getViewModel().getModelOffsetX() * viewScale;
        rectangle.y -= canvas.getViewModel().getModelOffsetY() * viewScale;
        // System.out.println(rectangle.toString());
        // rectangle.grow(5, 5);
        canvas.repaint(rectangle);
        // WeasisWin.getInstance().getZoomOptionPanel().repaintZoomWindow(rectangle);
    }

    public static Cursor getCustomCursor(String filename, String cursorName, int hotSpotX, int hotSpotY) {
        Toolkit defaultToolkit = Toolkit.getDefaultToolkit();
        ImageIcon icon = new ImageIcon(AbstractLayerModel.class.getResource("/icon/cursor/" + filename)); //$NON-NLS-1$
        Dimension bestCursorSize = defaultToolkit.getBestCursorSize(icon.getIconWidth(), icon.getIconHeight());
        Point hotSpot =
            new Point((hotSpotX * bestCursorSize.width) / icon.getIconWidth(), (hotSpotY * bestCursorSize.height)
                / icon.getIconHeight());
        return defaultToolkit.createCustomCursor(icon.getImage(), hotSpot, cursorName);
    }

    public void setCursor(Cursor cursor) {
        this.cursor = cursor;
    }

    public void resetCursor() {
        this.cursor = DEFAULT_CURSOR;
        canvas.setCursor(cursor);
    }

    private void layerVisibilityChanged(AbstractLayer layer) {
        repaint();
    }

    public void setSelectedGraphics(List<Graphic> list) {
        boolean flag = false;
        for (int i = selectedGraphics.size() - 1; i >= 0; i--) {
            Graphic graphic = selectedGraphics.get(i);
            if (list == null || !list.contains(graphic)) {
                graphic.setSelected(false);
                flag = true;
            }
        }
        selectedGraphics.clear();
        if (list != null) {
            selectedGraphics.addAll(list);
            for (int j = selectedGraphics.size() - 1; j >= 0; j--) {
                (selectedGraphics.get(j)).setSelected(true);
                flag = true;
            }
        }
        if (flag && pcs != null) {
            if (selectedGraphics.size() == 1) {
                oneSelectedGraphicUpdateInterface();
            } else {
                pcs.firePropertyChange("selectedGraphics", null, null); //$NON-NLS-1$
            }
        }
    }

    public void oneSelectedGraphicUpdateInterface() {
        if (pcs != null) {
            pcs.firePropertyChange("selectedGraphic", null, null); //$NON-NLS-1$
        }
    }

    public ArrayList<Graphic> getSelectedGraphics() {
        return selectedGraphics;
    }

    public Rectangle getBounds() {
        return canvas.getBounds();
    }

    public void repaintWithRelativeCoord(Rectangle rectangle) {
        canvas.repaint(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
    }

    public void addLayer(AbstractLayer layer) {
        layers.add(layer);
        layer.setShowDrawing(this);
    }

    public void removeLayer(AbstractLayer layer) {
        layers.remove(layer);
        repaint();
    }

    public void setLayers(java.util.List list) {
        for (int i = layers.size() - 1; i >= 0; i--) {
            layers.remove(i);
        }
        if (list != null) {
            for (int j = list.size() - 1; j >= 0; j--) {
                addLayer((AbstractLayer) list.get(j));
            }
        }
        // setActiveLayer(naewin.getToolsBar().getCurrentToolsLayerID());
    }

    public AbstractLayer[] getLayers() {
        return layers.toArray(new AbstractLayer[layers.size()]);
    }

    public SelectGraphic getSelectionGraphic() {
        for (Graphic gr : selectedGraphics) {
            if (gr instanceof SelectGraphic) {
                return (SelectGraphic) gr;
            }
        }
        return null;
    }

    public java.util.List<Graphic> getSelectedDragableGraphics() {
        ArrayList<Graphic> arraylist = new ArrayList<Graphic>();
        for (int i = selectedGraphics.size() - 1; i >= 0; i--) {
            Graphic graphic = selectedGraphics.get(i);
            if (graphic instanceof AbstractDragGraphic) {
                arraylist.add(graphic);
            }
        }
        return arraylist;
    }

    public java.util.List<Graphic> getSelectedClassifGraphics() {
        ArrayList<Graphic> arraylist = new ArrayList<Graphic>();
        for (int i = selectedGraphics.size() - 1; i >= 0; i--) {
            Graphic graphic = selectedGraphics.get(i);
            // if (graphic instanceof AbstractClassifGraphic) {
            // arraylist.add(graphic);
            // }
        }
        return arraylist;
    }

    public java.util.List<Graphic> getSelectedAllGraphicsIntersecting(Rectangle rectangle) {
        ArrayList<Graphic> arraylist = new ArrayList<Graphic>();
        for (int i = layers.size() - 1; i >= 0; i--) {
            AbstractLayer layer = layers.get(i);
            if (layer.isVisible()) {
                arraylist.addAll(layer.getGraphicsSurfaceInArea(rectangle));
            }
        }
        return arraylist;
    }

    public java.util.List<Graphic> getdAllGraphics() {
        ArrayList<Graphic> arraylist = new ArrayList<Graphic>();
        for (int i = layers.size() - 1; i >= 0; i--) {
            AbstractLayer layer = layers.get(i);
            if (layer.isVisible()) {
                arraylist.addAll(layer.getGraphics());
            }
        }
        return arraylist;
    }

    public Graphic getFirstGraphicIntersecting(Point pos) {
        for (int i = layers.size() - 1; i >= 0; i--) {
            AbstractLayer layer = layers.get(i);
            if (layer.isVisible()) {
                Graphic graph = layer.getGraphicContainPoint(pos);
                if (graph != null) {
                    return graph;
                }
            }
        }
        return null;
    }

    public void setSelectedGraphic(Graphic graphic) {
        singleList.clear();
        if (graphic != null) {
            singleList.add(graphic);
        }
        setSelectedGraphics(singleList);
    }

    public void addPropertyChangeListener(PropertyChangeListener propertychangelistener) {
        if (pcs == null) {
            pcs = new PropertyChangeSupport(this);
        }
        pcs.addPropertyChangeListener(propertychangelistener);
    }

    public void removePropertyChangeListener(PropertyChangeListener propertychangelistener) {
        if (pcs != null) {
            pcs.removePropertyChangeListener(propertychangelistener);
        }
    }

    public void deleteAllGraphics() {
        final AbstractLayer[] layerList = getLayers();
        final int n = layerList.length;
        for (int i = n - 1; i >= 0; i--) {
            layerList[i].deleteAllGraphic();
        }
    }

    public void deleteSelectedGraphics() {
        java.util.List list = getSelectedGraphics();
        if (list != null && list.size() > 0) {
            int response =
                JOptionPane
                    .showConfirmDialog(
                        canvas,
                        String.format(Messages.getString("AbstractLayerModel.del_conf"), list.size()), Messages.getString("AbstractLayerModel.del_graphs"), //$NON-NLS-1$ //$NON-NLS-2$
                        JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (response == 0) {
                for (int i = list.size() - 1; i >= 0; i--) {
                    Graphic graphic = null;
                    graphic = (Graphic) list.get(i);
                    AbstractLayer layer = graphic.getLayer();
                    layer.removeGraphic(graphic);
                    // fireRemoveAction() peut utiliser le layer pour dï¿½clencher l'action
                    graphic.setLayer(layer);
                    graphic.fireRemoveAction();
                    graphic.setLayer(null);
                }
            }
            repaint();
        }
    }

    public void moveSelectedGraphics(int x, int y) {
        java.util.List dragGaphs = getSelectedDragableGraphics();
        if (dragGaphs != null && dragGaphs.size() > 0) {
            for (int i = dragGaphs.size() - 1; i >= 0; i--) {
                AbstractDragGraphic graphic = (AbstractDragGraphic) dragGaphs.get(i);
                graphic.move(0, x, y, null);
            }
            if (dragGaphs.size() == 1) {
                oneSelectedGraphicUpdateInterface();
            }
        }
    }

    public static PlanarImage getGraphicAsImage(Shape shape) {
        Rectangle bound = shape.getBounds();
        TiledImage image =
            new TiledImage(0, 0, bound.width + 1, bound.height + 1, 0, 0, LayoutUtil.createBinarySampelModel(),
                LayoutUtil.createBinaryIndexColorModel());
        Graphics2D g2d = image.createGraphics();
        g2d.translate(-bound.x, -bound.y);
        g2d.setPaint(Color.white);
        g2d.setStroke(new BasicStroke(1.0f));
        g2d.fill(shape);
        g2d.draw(shape);
        return image;
    }

    public int getLayerCount() {
        return layers.size();
    }

    // public Rectangle2D getVisibleBoundingBox(Rectangle2D r) {
    // return naewin.getSource().getBounds();
    // }

    public void draw(Graphics2D g2d, AffineTransform transform, AffineTransform inverseTransform) {
        Rectangle bound = null;
        // Get the visible view in real coordinates
        Shape area = inverseTransform.createTransformedShape(g2d.getClipBounds());
        bound = area.getBounds();
        g2d.translate(0.5, 0.5);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, antialiasingOn);
        // g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        for (int i = 0; i < layers.size(); i++) {
            AbstractLayer layer = layers.get(i);
            if (layer.isVisible()) {
                layer.paint(g2d, transform, inverseTransform, bound);
            }
        }
        // g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, antialiasingOff);
        g2d.translate(-0.5, -0.5);
    }

    public void dispose() {
        final AbstractLayer[] layerList = getLayers();
        layers.clear();
        listenerList.clear();
        final int n = layerList.length;
        for (int i = n - 1; i >= 0; i--) {
            layerList[i].deleteAllGraphic();
        }
    }

    public boolean isLayerModelChangeFireingSuspended() {
        return layerModelChangeFireingSuspended;
    }

    public void setLayerModelChangeFireingSuspended(boolean layerModelChangeFireingSuspended) {
        this.layerModelChangeFireingSuspended = layerModelChangeFireingSuspended;
    }

    public void setAlpha(float alpha) {
        this.alpha = alpha;
    }

    public void setAntialiasing(boolean antialiasing) {
        this.antialiasing = antialiasing ? antialiasingOn : antialiasingOff;
    }

    /**
     * Gets all layer manager listeners of this layer.
     */
    public LayerModelChangeListener[] getLayerModelChangeListeners() {
        return (LayerModelChangeListener[]) listenerList.toArray(new LayerModelChangeListener[listenerList.size()]);
    }

    /**
     * Adds a layer manager listener to this layer.
     */
    public void addLayerModelChangeListener(LayerModelChangeListener listener) {
        if (listener != null && !listenerList.contains(listener)) {
            listenerList.add(listener);
        }
    }

    /**
     * Removes a layer manager listener from this layer.
     */
    public void removeLayerModelChangeListener(LayerModelChangeListener listener) {
        if (listener != null) {
            listenerList.remove(listener);
        }
    }

    public void fireLayerModelChanged() {
        if (!isLayerModelChangeFireingSuspended()) {
            for (int i = 0; i < listenerList.size(); i++) {
                ((LayerModelChangeListener) listenerList.get(i)).handleLayerModelChanged(this);
            }
        }
    }

    public static PlanarImage getGraphicsAsImage(Rectangle bound, java.util.List graphics2dlist) {
        TiledImage image =
            new TiledImage(0, 0, bound.width + 1, bound.height + 1, 0, 0, LayoutUtil.createBinarySampelModel(),
                LayoutUtil.createBinaryIndexColorModel());
        Graphics2D g2d = image.createGraphics();
        g2d.translate(-bound.x, -bound.y);
        g2d.setPaint(Color.white);
        g2d.setStroke(new BasicStroke(1.0f));
        for (int i = 0; i < graphics2dlist.size(); i++) {
            Graphic graph = (Graphic) graphics2dlist.get(i);
            g2d.fill(graph.getShape());
            g2d.draw(graph.getShape());
        }
        return image;
    }

    public void setActiveLayer(int drawType) {
        Tools.setLevelToLayers(layers);
        for (int j = layers.size() - 1; j >= 0; j--) {
            AbstractLayer layerTemp = layers.get(j);
            if (layerTemp.getDrawType() == drawType) {
                layerTemp.setLevel(9);
            }
        }
        Collections.sort(layers);
        repaint();
    }

    public AbstractLayer getLayer(Tools tool) {
        if (tool.isLayer()) {
            return getLayer(tool.getId());
        }
        return getLayer(Tools.TEMPDRAGLAYER);
    }

    public AbstractLayer getLayer(int drawType) {
        for (int j = layers.size() - 1; j >= 0; j--) {
            AbstractLayer layerTemp = layers.get(j);
            if (layerTemp.getDrawType() == drawType) {
                return layerTemp;
            }
        }
        return getLayer(Tools.TEMPDRAGLAYER);
    }

    public DragLayer getMeasureLayer() {
        return (DragLayer) getLayer(Tools.MEASURE);
    }

    public DragLayer getNoteLayer() {
        return (DragLayer) getLayer(Tools.NOTE);
    }

    public TempLayer getTemporyDragLayer() {
        return (TempLayer) getLayer(Tools.TEMPDRAGLAYER);
    }

    //
    // public ObjectsLayer getObjectExtractLayer() {
    // return (ObjectsLayer) getLayer(Tools.OBJECTEXTRACT);
    // }
    //
    // public TempClassLayer getTemporyClassifLayer() {
    // return (TempClassLayer) getLayer(Tools.TEMPCLASSIFLAYER);
    // }
    //
    // public RoiLayer getRoiLayer() {
    // return (RoiLayer) getLayer(Tools.DELIMIT);
    // }

    // public void paintSVG(SVGGraphics2D g2) {
    // for (int i = 0; i < layers.size(); i++) {
    // AbstractLayer layer = layers.get(i);
    // if (layer.isVisible() && layer.getDrawType() != Tools.TEMPCLASSIFLAYER.getId()) {
    // String name = Tools.getToolName(layer.getDrawType());
    // g2.writeStartGroup(name, false);
    // layer.paintSVG(g2);
    // g2.writeEndGroup(name);
    // }
    // }
    // }

    public void setCreateGraphic(Graphic graphic) {
        createGraphic = graphic;
    }

    public Graphic getCreateGraphic() {
        return createGraphic;
    }

}
