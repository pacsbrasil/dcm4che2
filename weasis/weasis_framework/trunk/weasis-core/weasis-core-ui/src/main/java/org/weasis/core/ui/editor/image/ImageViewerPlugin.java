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
// Placed in public domain by Dmitry Olshansky, 2006
package org.weasis.core.ui.editor.image;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JPanel;

import org.weasis.core.api.gui.util.ActionState;
import org.weasis.core.api.gui.util.ActionW;
import org.weasis.core.api.gui.util.ComboItemListener;
import org.weasis.core.api.image.GridBagLayoutModel;
import org.weasis.core.api.image.LayoutConstraints;
import org.weasis.core.api.media.data.ImageElement;
import org.weasis.core.api.media.data.MediaSeries;
import org.weasis.core.ui.Messages;
import org.weasis.core.ui.editor.SeriesViewerEvent;
import org.weasis.core.ui.editor.SeriesViewerEvent.EVENT;
import org.weasis.core.ui.util.WtoolBar;

public abstract class ImageViewerPlugin<E extends ImageElement> extends ViewerPlugin<E> {

    private static final String view2dClass = DefaultView2d.class.getName();
    public static final GridBagLayoutModel VIEWS_1x1 =
        new GridBagLayoutModel(
            String.format(Messages.getString("ImageViewerPlugin.1"), "1x1"), 1, 1, view2dClass, new ImageIcon(ImageViewerPlugin.class //$NON-NLS-1$ //$NON-NLS-2$
                    .getResource("/icon/22x22/layout1x1.png"))); //$NON-NLS-1$
    public static final GridBagLayoutModel VIEWS_2x1 =
        new GridBagLayoutModel(
            String.format(Messages.getString("ImageViewerPlugin.2"), "2x1"), 2, 1, view2dClass, new ImageIcon(ImageViewerPlugin.class //$NON-NLS-1$ //$NON-NLS-2$
                    .getResource("/icon/22x22/layout2x1.png"))); //$NON-NLS-1$
    public static final GridBagLayoutModel VIEWS_1x2 =
        new GridBagLayoutModel(
            String.format(Messages.getString("ImageViewerPlugin.2"), "1x2"), 1, 2, view2dClass, new ImageIcon(ImageViewerPlugin.class //$NON-NLS-1$ //$NON-NLS-2$
                    .getResource("/icon/22x22/layout1x2.png"))); //$NON-NLS-1$
    public static final GridBagLayoutModel VIEWS_2x2_f2 =
        new GridBagLayoutModel(
            ImageViewerPlugin.class.getResourceAsStream("/config/layoutModel2x2_f2.xml"), Messages.getString("ImageViewerPlugin.layout_c2x1"), //$NON-NLS-1$ //$NON-NLS-2$
            new ImageIcon(ImageViewerPlugin.class.getResource("/icon/22x22/layout2x2_f2.png"))); //$NON-NLS-1$
    public static final GridBagLayoutModel VIEWS_2_f1x2 =
        new GridBagLayoutModel(
            ImageViewerPlugin.class.getResourceAsStream("/config/layoutModel2_f1x2.xml"), Messages.getString("ImageViewerPlugin.layout_c1x2"), //$NON-NLS-1$ //$NON-NLS-2$
            new ImageIcon(ImageViewerPlugin.class.getResource("/icon/22x22/layout2_f1x2.png"))); //$NON-NLS-1$
    public static final GridBagLayoutModel VIEWS_2x2 =
        new GridBagLayoutModel(
            String.format(Messages.getString("ImageViewerPlugin.2"), "2x2"), 2, 2, view2dClass, new ImageIcon(ImageViewerPlugin.class //$NON-NLS-1$ //$NON-NLS-2$
                    .getResource("/icon/22x22/layout2x2.png"))); //$NON-NLS-1$
    public static final GridBagLayoutModel VIEWS_3x2 =
        new GridBagLayoutModel(
            String.format(Messages.getString("ImageViewerPlugin.2"), "3x2"), 3, 2, view2dClass, new ImageIcon(ImageViewerPlugin.class //$NON-NLS-1$ //$NON-NLS-2$
                    .getResource("/icon/22x22/layout3x2.png"))); //$NON-NLS-1$
    public static final GridBagLayoutModel VIEWS_3x3 =
        new GridBagLayoutModel(
            String.format(Messages.getString("ImageViewerPlugin.2"), "3x3"), 3, 3, view2dClass, new ImageIcon(ImageViewerPlugin.class //$NON-NLS-1$ //$NON-NLS-2$
                    .getResource("/icon/22x22/layout3x3.png"))); //$NON-NLS-1$
    public static final GridBagLayoutModel VIEWS_4x3 =
        new GridBagLayoutModel(
            String.format(Messages.getString("ImageViewerPlugin.2"), "4x3"), 4, 3, view2dClass, new ImageIcon(ImageViewerPlugin.class //$NON-NLS-1$ //$NON-NLS-2$
                    .getResource("/icon/22x22/layout4x3.png"))); //$NON-NLS-1$
    public static final GridBagLayoutModel VIEWS_4x4 =
        new GridBagLayoutModel(
            String.format(Messages.getString("ImageViewerPlugin.2"), "4x4"), 4, 4, view2dClass, new ImageIcon(ImageViewerPlugin.class //$NON-NLS-1$ //$NON-NLS-2$
                    .getResource("/icon/22x22/layout4x4.png"))); //$NON-NLS-1$

    /**
     * The current focused <code>ImagePane</code>. The default is 0.
     */

    protected DefaultView2d<E> selectedImagePane = null;
    /**
     * The array of display panes located in this image view panel.
     */

    protected final ArrayList<DefaultView2d<E>> view2ds;
    protected final ArrayList<JComponent> components;

    protected SynchView synchView = SynchView.NONE;

    protected final ImageViewerEventManager<E> eventManager;
    protected final JPanel grid;
    protected GridBagLayoutModel layoutModel;

    public ImageViewerPlugin(ImageViewerEventManager<E> eventManager, String PluginName) {
        this(eventManager, VIEWS_1x1, PluginName, null, null);
    }

    public ImageViewerPlugin(ImageViewerEventManager<E> eventManager, GridBagLayoutModel layoutModel,
        String pluginName, Icon icon, String tooltips) {
        super(pluginName, icon, tooltips);
        if (eventManager == null) {
            throw new IllegalArgumentException("EventManager cannot be null"); //$NON-NLS-1$
        }
        this.eventManager = eventManager;
        view2ds = new ArrayList<DefaultView2d<E>>();
        components = new ArrayList<JComponent>();
        grid = new JPanel();
        grid.setBackground(Color.BLACK);
        grid.setFocusCycleRoot(true);
        grid.setLayout(new GridBagLayout());
        add(grid, BorderLayout.CENTER);

        setLayoutModel(layoutModel);
    }

    public abstract DefaultView2d<E> createDefaultView();

    public abstract JComponent createUIcomponent(String clazz);

    public DefaultView2d<E> getSelectedImagePane() {
        return selectedImagePane;
    }

    /** Get the layout of this view panel. */

    public GridBagLayoutModel getLayoutModel() {
        return layoutModel;
    }

    @Override
    public void addSeries(MediaSeries<E> sequence) {
        // TODO set series in specific place and if does not exist in
        // the first free place
        if (sequence != null) {
            if (SynchView.Mode.Tile.equals(synchView.getMode())) {
                selectedImagePane.setSeries(sequence, -1);
                updateSynchView();
                return;
            }
            DefaultView2d<E> viewPane = getSelectedImagePane();
            if (viewPane != null) {
                viewPane.setSeries(sequence);
                viewPane.repaint();

                // Set selection to the next view
                setSelectedImagePane(getNextSelectedImagePane());
            }
        }
    }

    public void removeSeries(MediaSeries<E> series) {
        if (series != null) {
            for (int i = 0; i < view2ds.size(); i++) {
                DefaultView2d<E> v = view2ds.get(i);
                if (v.getSeries() == series) {
                    v.setSeries(null, -1);
                }
            }
        }
    }

    public List<MediaSeries<E>> getOpenSeries() {
        List<MediaSeries<E>> list = new ArrayList<MediaSeries<E>>();
        for (DefaultView2d<E> v : view2ds) {
            MediaSeries<E> s = v.getSeries();
            if (s != null) {
                list.add(s);
            }
        }
        return list;
    }

    public void changeLayoutModel(GridBagLayoutModel layoutModel) {
        ActionState layout = eventManager.getAction(ActionW.LAYOUT);
        if (layout instanceof ComboItemListener) {
            ((ComboItemListener) layout).setSelectedItem(layoutModel);
        }
    }

    /**
     * Set a layout to this view panel. The layout is defined by the provided number corresponding the layout definition
     * in the property file.
     */

    protected void setLayoutModel(GridBagLayoutModel layoutModel) {
        this.layoutModel = layoutModel == null ? VIEWS_1x1 : layoutModel;
        grid.removeAll();
        // Keep views containing images
        ArrayList<DefaultView2d<E>> oldViews = new ArrayList<DefaultView2d<E>>();
        for (DefaultView2d<E> v : view2ds) {
            if (v.getSeries() != null && v.getImage() != null) {
                oldViews.add(v);
            } else {
                v.dispose();
            }
        }
        view2ds.clear();

        int nbview = this.layoutModel.getViewerNumber(view2dClass);
        if (oldViews.size() > nbview) {
            for (int i = oldViews.size() - 1; i >= nbview; i--) {
                oldViews.remove(i).dispose();
            }
        }

        final LinkedHashMap<LayoutConstraints, JComponent> elements = this.layoutModel.getConstraints();
        Iterator<LayoutConstraints> enumVal = elements.keySet().iterator();
        while (enumVal.hasNext()) {
            LayoutConstraints e = enumVal.next();
            boolean typeView2d = view2dClass.equals(e.getType());
            if (typeView2d) {
                DefaultView2d<E> oldView;
                if (oldViews.size() > 0) {
                    oldView = oldViews.remove(0);
                } else {
                    oldView = createDefaultView();
                    oldView.registerDefaultListeners();
                }
                view2ds.add(oldView);
                elements.put(e, oldView);
                grid.add(oldView, e);
                if (oldView.getSeries() != null) {
                    oldView.getSeries().setOpen(true);
                }
            } else {
                JComponent component = createUIcomponent(e.getType());
                if (component != null) {
                    component.setOpaque(true);
                    components.add(component);
                    elements.put(e, component);
                    grid.add(component, e);
                }

            }
        }
        grid.revalidate();
        selectedImagePane = view2ds.get(0);

        MouseActions mouseActions = eventManager.getMouseActions();
        for (int i = 0; i < view2ds.size(); i++) {
            DefaultView2d<E> v = view2ds.get(i);
            if (SynchView.Mode.Tile.equals(synchView)) {
                v.setTileOffset(i);
                v.setSeries(selectedImagePane.getSeries(), -1);
            }
            v.enableMouseAndKeyListener(mouseActions);
        }
        setDrawActions();
        selectedImagePane.setSelected(true);
        eventManager.updateComponentsListener(selectedImagePane);
        if (selectedImagePane.getSeries() != null) {
            eventManager.fireSeriesViewerListeners(new SeriesViewerEvent(this, selectedImagePane.getSeries().getMedia(
                selectedImagePane.getFrameIndex()), EVENT.LAYOUT));
        }
    }

    public void setSelectedImagePane(DefaultView2d<E> defaultView2d) {
        if (this.selectedImagePane.getSeries() != null) {
            this.selectedImagePane.getSeries().setSelected(false, 0);
        }
        if (defaultView2d != null && defaultView2d.getSeries() != null) {
            defaultView2d.getSeries().setSelected(true, defaultView2d.getFrameIndex());
        }
        if (this.selectedImagePane != defaultView2d && defaultView2d != null) {
            this.selectedImagePane.setSelected(false);
            defaultView2d.setSelected(true);
            this.selectedImagePane = defaultView2d;
            eventManager.updateComponentsListener(defaultView2d);

        }
        if (defaultView2d != null && defaultView2d.getSeries() != null) {
            eventManager.fireSeriesViewerListeners(new SeriesViewerEvent(this, defaultView2d.getSeries().getMedia(
                defaultView2d.getFrameIndex()), EVENT.SELECT));
        }
    }

    public void maximizedSelectedImagePane(final DefaultView2d<E> defaultView2d) {
        final LinkedHashMap<LayoutConstraints, JComponent> elements = layoutModel.getConstraints();
        if (elements.size() > 1) {
            for (DefaultView2d<E> v : view2ds) {
                v.removeFocusListener(v);
            }
            int nb = grid.getComponentCount();
            grid.removeAll();
            if (nb > 1) {
                Iterator<Entry<LayoutConstraints, JComponent>> enumVal = elements.entrySet().iterator();
                while (enumVal.hasNext()) {
                    Entry<LayoutConstraints, JComponent> entry = enumVal.next();
                    if (entry.getValue().equals(defaultView2d)) {
                        GridBagConstraints c = (GridBagConstraints) entry.getKey().clone();
                        c.weightx = 1.0;
                        c.weighty = 1.0;
                        grid.add(defaultView2d, c);
                        defaultView2d.addFocusListener(defaultView2d);
                        break;
                    }
                }
            } else {
                Iterator<Entry<LayoutConstraints, JComponent>> enumVal = elements.entrySet().iterator();
                while (enumVal.hasNext()) {
                    Entry<LayoutConstraints, JComponent> entry = enumVal.next();
                    grid.add(entry.getValue(), entry.getKey());
                }
                for (DefaultView2d<E> v : view2ds) {
                    v.addFocusListener(v);
                }
            }
            grid.revalidate();
            grid.repaint();
            defaultView2d.requestFocusInWindow();
        }
    }

    public synchronized void setDrawActions() {
        WtoolBar toolBar = getViewerToolBar();
        if (toolBar != null) {
            ((ViewerToolBar) toolBar).getMeasureToolBar().setDrawActions();
        }
    }

    /** Return the image in the image display panel. */

    public E getImage(int i) {
        if (i >= 0 && i < view2ds.size()) {
            return view2ds.get(i).getImage();
        }
        return null;
    }

    /** Return all the <code>ImagePanel</code>s. */

    public ArrayList<DefaultView2d<E>> getImagePanels() {
        return new ArrayList<DefaultView2d<E>>(view2ds);
    }

    public DefaultView2d<E> getNextSelectedImagePane() {
        for (int i = 0; i < view2ds.size() - 1; i++) {
            if (view2ds.get(i) == selectedImagePane) {
                return view2ds.get(i + 1);
            }
        }
        return selectedImagePane;
    }

    public boolean isContainingView(DefaultView2d<E> view2DPane) {
        for (DefaultView2d<E> v : view2ds) {
            if (v == view2DPane) {
                return true;
            }
        }
        return false;
    }

    public SynchView getSynchView() {
        return synchView;
    }

    public void setSynchView(SynchView synchView) {
        this.synchView = synchView;
        updateSynchView();
        eventManager.updateAllListeners(this, synchView);
    }

    protected void updateSynchView() {
        if (SynchView.Mode.Tile.equals(synchView.getMode())) {
            MediaSeries<E> series = null;
            if (selectedImagePane.getSeries() != null) {
                series = selectedImagePane.getSeries();
            } else {
                for (DefaultView2d<E> v : view2ds) {
                    if (v.getSeries() != null) {
                        series = v.getSeries();
                        break;
                    }
                }
            }
            if (series != null) {
                int limit = series.getMedias().size();
                for (int i = 0; i < view2ds.size(); i++) {
                    DefaultView2d<E> v = view2ds.get(i);
                    if (i < limit) {
                        v.setTileOffset(i);
                        v.setSeries(series, -1);
                    } else {
                        v.setSeries(null, -1);
                    }
                }
            }
        } else {
            for (DefaultView2d<E> v : view2ds) {
                v.setTileOffset(0);
            }
        }
    }

    public synchronized void setMouseActions(MouseActions mouseActions) {
        if (mouseActions == null) {
            for (DefaultView2d<E> v : view2ds) {
                v.disableMouseAndKeyListener();
                // Let the possibility to get the focus
                v.iniDefaultMouseListener();
            }
        } else {
            for (DefaultView2d<E> v : view2ds) {
                v.enableMouseAndKeyListener(mouseActions);
            }
        }
    }

    public GridBagLayoutModel getBestDefaultViewLayout(int size) {
        if (size <= 1) {
            return VIEWS_1x1;
        }
        ActionState layout = eventManager.getAction(ActionW.LAYOUT);
        if (layout instanceof ComboItemListener) {
            Object[] list = ((ComboItemListener) layout).getAllItem();
            for (Object m : list) {
                if (m instanceof GridBagLayoutModel) {
                    if (((GridBagLayoutModel) m).getViewerNumber(view2dClass) >= size) {
                        return (GridBagLayoutModel) m;
                    }
                }
            }
        }

        return VIEWS_4x4;
    }
}
