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
package org.weasis.dicom.viewer2d;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseWheelEvent;
import java.util.ArrayList;

import javax.swing.BoundedRangeModel;
import javax.swing.event.ChangeEvent;

import org.noos.xing.mydoggy.Content;
import org.osgi.service.prefs.Preferences;
import org.weasis.core.api.gui.util.ActionState;
import org.weasis.core.api.gui.util.ActionW;
import org.weasis.core.api.gui.util.ComboItemListener;
import org.weasis.core.api.gui.util.GuiExecutor;
import org.weasis.core.api.gui.util.SliderChangeListener;
import org.weasis.core.api.gui.util.SliderCineListener;
import org.weasis.core.api.gui.util.SliderCineListener.TIME;
import org.weasis.core.api.gui.util.ToggleButtonListener;
import org.weasis.core.api.image.GridBagLayoutModel;
import org.weasis.core.api.image.op.ByteLut;
import org.weasis.core.api.image.util.KernelData;
import org.weasis.core.api.image.util.ZoomSetting;
import org.weasis.core.api.media.data.ImageElement;
import org.weasis.core.api.media.data.MediaElement;
import org.weasis.core.api.media.data.MediaSeries;
import org.weasis.core.api.media.data.MediaSeries.MEDIA_POSITION;
import org.weasis.core.api.media.data.Series;
import org.weasis.core.api.media.data.TagW;
import org.weasis.core.api.service.BundlePreferences;
import org.weasis.core.ui.docking.UIManager;
import org.weasis.core.ui.editor.SeriesViewerEvent;
import org.weasis.core.ui.editor.SeriesViewerEvent.EVENT;
import org.weasis.core.ui.editor.image.DefaultView2d;
import org.weasis.core.ui.editor.image.ImageViewerEventManager;
import org.weasis.core.ui.editor.image.ImageViewerPlugin;
import org.weasis.core.ui.editor.image.MeasureToolBar;
import org.weasis.core.ui.editor.image.PannerListener;
import org.weasis.core.ui.editor.image.SynchView;
import org.weasis.core.ui.editor.image.SynchView.Mode;
import org.weasis.core.ui.graphic.Graphic;
import org.weasis.core.ui.graphic.model.AbstractLayer;
import org.weasis.core.ui.graphic.model.Tools;
import org.weasis.dicom.codec.DicomImageElement;
import org.weasis.dicom.codec.SortSeriesStack;
import org.weasis.dicom.codec.display.LutManager;
import org.weasis.dicom.codec.display.PresetWindowLevel;
import org.weasis.dicom.codec.display.ViewingProtocols;
import org.weasis.dicom.codec.geometry.ImageOrientation;
import org.weasis.dicom.viewer2d.internal.Activator;

/**
 * The event processing center for this application. This class responses for loading data sets, processing the events
 * from the utility menu that includes changing the operation scope, the layout, window/level, rotation angle, zoom
 * factor, starting/stoping the cining-loop and etc.
 * 
 */

public class EventManager extends ImageViewerEventManager<DicomImageElement> implements ActionListener {

    /** The single instance of this singleton class. */

    private static EventManager instance;

    private final SliderCineListener moveTroughSliceAction;
    private final SliderChangeListener windowAction;
    private final SliderChangeListener levelAction;
    private final SliderChangeListener rotateAction;
    private final SliderChangeListener zoomAction;
    private final SliderChangeListener lensZoomAction;

    private final ToggleButtonListener flipAction;
    private final ToggleButtonListener inverseLutAction;
    private final ToggleButtonListener inverseStackAction;
    private final ToggleButtonListener showLensAction;
    private final ToggleButtonListener imageOverlayAction;

    private final ComboItemListener presetAction;
    private final ComboItemListener lutAction;
    private final ComboItemListener filterAction;
    private final ComboItemListener sortStackAction;
    private final ComboItemListener viewingProtocolAction;
    private final ComboItemListener layoutAction;
    private final ComboItemListener synchAction;
    private final ComboItemListener measureAction;

    private final PannerListener panAction;

    public final static ArrayList<SynchView> SYNCH_LIST = new ArrayList<SynchView>();
    static {
        SYNCH_LIST.add(SynchView.NONE);
        SYNCH_LIST.add(SynchView.DEFAULT_STACK);
        SYNCH_LIST.add(SynchView.DEFAULT_TILE);
    }

    /**
     * Return the single instance of this class. This method guarantees the singleton property of this class.
     */
    public static synchronized EventManager getInstance() {
        if (instance == null) {
            instance = new EventManager();
        }
        return instance;
    }

    /**
     * The default private constructor to guarantee the singleton property of this class.
     */

    private EventManager() {
        iniAction(moveTroughSliceAction = getMoveTroughSliceAction(20, TIME.second, 0.1));
        iniAction(windowAction = newWindowAction());
        iniAction(levelAction = newLevelAction());
        iniAction(rotateAction = newRotateAction());
        iniAction(zoomAction = newZoomAction());

        iniAction(flipAction = newFlipAction());
        iniAction(inverseLutAction = newInverseLutAction());
        iniAction(inverseStackAction = newInverseStackAction());
        iniAction(showLensAction = newLensAction());
        iniAction(lensZoomAction = newLensZoomAction());
        iniAction(imageOverlayAction = newImageOverlayAction());

        iniAction(presetAction = newPresetAction());
        iniAction(lutAction = newLutAction());
        iniAction(filterAction = newFilterAction());
        iniAction(sortStackAction = newSortStackAction());
        iniAction(viewingProtocolAction = newViewingProtocolAction());
        iniAction(layoutAction = newLayoutAction(View2dContainer.MODELS));
        iniAction(synchAction = newSynchAction(SYNCH_LIST.toArray(new SynchView[SYNCH_LIST.size()])));
        iniAction(measureAction =
            newMeasurementAction(MeasureToolBar.graphicList.toArray(new Graphic[MeasureToolBar.graphicList.size()])));
        iniAction(panAction = newPanAction());

        Preferences prefs = Activator.PREFERENCES.getDefaultPreferences();
        zoomSetting.applyPreferences(prefs);

        mouseActions.applyPreferences(prefs);

        if (prefs != null) {
            Preferences prefNode = prefs.node("mouse.sensivity"); //$NON-NLS-1$
            windowAction.setMouseSensivity(prefNode.getDouble(windowAction.getActionW().cmd(), 1.25));
            levelAction.setMouseSensivity(prefNode.getDouble(levelAction.getActionW().cmd(), 1.25));
            moveTroughSliceAction.setMouseSensivity(prefNode.getDouble(moveTroughSliceAction.getActionW().cmd(), 0.1));
            rotateAction.setMouseSensivity(prefNode.getDouble(rotateAction.getActionW().cmd(), 0.25));
            zoomAction.setMouseSensivity(prefNode.getDouble(zoomAction.getActionW().cmd(), 0.1));

        }

        initializeParameters();
    }

    private void iniAction(ActionState action) {
        actions.put(action.getActionW(), action);
    }

    private void initializeParameters() {
        enableActions(false);
        windowAction.getModel().addChangeListener(presetAction);
        levelAction.getModel().addChangeListener(presetAction);
    }

    private ComboItemListener newFilterAction() {
        return new ComboItemListener(ActionW.FILTER, KernelData.ALL_FILTERS) {

            @Override
            public void itemStateChanged(Object object) {
                if (object instanceof KernelData) {
                    firePropertyChange(action.cmd(), null, object);
                }
            }
        };
    }

    @Override
    protected SliderCineListener getMoveTroughSliceAction(int speed, TIME time, double mouseSensivity) {
        return new SliderCineListener(ActionW.SCROLL_SERIES, 1, 2, 1, speed, time, mouseSensivity) {

            private volatile boolean cining = true;

            protected CineThread currentCine;

            @Override
            public void stateChanged(BoundedRangeModel model) {

                int index = model.getValue() - 1;
                Series series = null;
                ImageElement image = null;
                if (selectedView2dContainer != null) {
                    DefaultView2d selectedImagePane = selectedView2dContainer.getSelectedImagePane();
                    if (selectedImagePane.getSeries() instanceof Series) {
                        series = (Series) selectedImagePane.getSeries();
                        MediaElement media = series.getMedia(index);
                        if (media instanceof ImageElement) {
                            image = (ImageElement) media;
                            if (image != null) {
                                int min = (int) image.getMinValue();
                                int max = (int) image.getMaxValue();
                                if (min == 0 && max == 0) {
                                    // media.getImage() will load the image to determine the min and the max value
                                    image.getImage();
                                    min = (int) image.getMinValue();
                                    max = (int) image.getMaxValue();
                                }
                                if (PresetWindowLevel.DEFAULT.equals(presetAction.getSelectedItem())) {
                                    windowAction.getModel().removeChangeListener(presetAction);
                                    levelAction.getModel().removeChangeListener(presetAction);
                                    windowAction.setMinMaxValueWithoutTriggerAction(0, (max - min),
                                        (int) image.getDefaultWindow());
                                    levelAction.setMinMaxValueWithoutTriggerAction(min, max,
                                        (int) image.getDefaultLevel());
                                    windowAction.getModel().addChangeListener(presetAction);
                                    levelAction.getModel().addChangeListener(presetAction);
                                } else if (PresetWindowLevel.AUTO.equals(presetAction.getSelectedItem())) {
                                    windowAction.getModel().removeChangeListener(presetAction);
                                    levelAction.getModel().removeChangeListener(presetAction);
                                    windowAction.setMinMaxValueWithoutTriggerAction(0, max - min, max - min);
                                    levelAction.setMinMaxValueWithoutTriggerAction(min, max, (max - min) / 2 + min);
                                    windowAction.getModel().addChangeListener(presetAction);
                                    levelAction.getModel().addChangeListener(presetAction);
                                }

                            }
                        }
                    }
                }
                Number location = index;
                GridBagLayoutModel layout = (GridBagLayoutModel) layoutAction.getSelectedItem();
                ActionState synch = getAction(ActionW.SYNCH);
                if (image != null && layout.getViewerNumber(DefaultView2d.class.getName()) > 1
                    && synch instanceof ComboItemListener) {
                    SynchView synchview = (SynchView) ((ComboItemListener) synch).getSelectedItem();
                    if (synchview.isActionEnable(ActionW.SCROLL_SERIES)) {
                        double[] val = (double[]) image.getTagValue(TagW.SlicePosition);
                        if (val != null) {
                            location = val[0] + val[1] + val[2];
                        }
                    } else {
                        final ArrayList<DefaultView2d<DicomImageElement>> panes =
                            selectedView2dContainer.getImagePanels();
                        for (DefaultView2d<DicomImageElement> p : panes) {
                            Boolean cutlines = (Boolean) p.getActionValue(ActionW.SYNCH_CROSSLINE.cmd());
                            if (cutlines != null && cutlines) {
                                double[] val = (double[]) image.getTagValue(TagW.SlicePosition);
                                if (val != null) {
                                    location = val[0] + val[1] + val[2];
                                } else {
                                    return; // Do not throw event because
                                }
                                break;
                            }
                        }
                    }
                }

                // Model contains display value, value-1 is the index value of a sequence
                firePropertyChange(action.cmd(), null, location);
                if (image != null) {
                    fireSeriesViewerListeners(new SeriesViewerEvent(selectedView2dContainer, series, image,
                        EVENT.SELECT));
                }
            }

            @Override
            public void setSpeed(int speed) {
                super.setSpeed(speed);
                if (currentCine != null) {
                    currentCine.iniSpeed();
                }
            }

            /** Create a thread to cine the images. */

            class CineThread extends Thread {

                private volatile int iteration;
                private volatile int wait;
                private volatile int currentCineRate;
                private volatile long start;

                @Override
                public void run() {
                    iniSpeed();
                    // Create a robot to monitor the paint
                    // Robot robot = null;
                    //
                    // try {
                    // robot = new java.awt.Robot();
                    // }
                    // catch (Exception e) {
                    // }

                    while (cining) {
                        GuiExecutor.instance().execute(new Runnable() {

                            public void run() {
                                int frameIndex = getValue() + 1;
                                frameIndex = frameIndex > getMax() ? 0 : frameIndex;
                                setValue(frameIndex);
                            }
                        });

                        iteration++;
                        // Wait until the paint is finished
                        // robot.waitForIdle();

                        // adjust the delay time based on the current performance
                        long elapsed = (System.currentTimeMillis() - start) / 1000;
                        if (elapsed > 0) {
                            currentCineRate = (int) (iteration / elapsed);
                            // System.out.println("fps:" + fps);

                            if (currentCineRate < getSpeed()) {
                                wait--;
                            } else {
                                wait++;
                            }
                            if (wait < 0) {
                                wait = 0;
                            }
                        }

                        // wait
                        if (wait > 0) {
                            try {
                                Thread.sleep(wait);
                            } catch (Exception e) {
                            }
                        }
                    }
                }

                public void iniSpeed() {
                    iteration = 0;
                    wait = 1000 / getSpeed();
                    currentCineRate = getSpeed();
                    start = System.currentTimeMillis();
                }

                public int getCurrentCineRate() {
                    return currentCineRate;
                }

            }

            /** Start the cining. */

            @Override
            public synchronized void start() {
                if (currentCine != null) {
                    stop();
                }
                if (getMax() - getMin() > 0) {
                    cining = true;
                    currentCine = new CineThread();
                    currentCine.start();
                }
            }

            /** Stop the cining. */

            @Override
            public synchronized void stop() {
                CineThread moribund = currentCine;
                currentCine = null;
                if (moribund != null) {
                    cining = false;
                    moribund.interrupt();
                }
            }

            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                setValue(getValue() + e.getWheelRotation());
            }

            @Override
            public int getCurrentCineRate() {
                if (currentCine != null) {
                    return currentCine.getCurrentCineRate();
                }
                return 0;
            }

        };
    }

    private ComboItemListener newPresetAction() {
        return new ComboItemListener(ActionW.PRESET, PresetWindowLevel.getPresetCollection("UNKOWN")) { //$NON-NLS-1$

            @Override
            public void itemStateChanged(Object object) {
                if (object instanceof PresetWindowLevel) {
                    windowAction.getModel().removeChangeListener(this);
                    levelAction.getModel().removeChangeListener(this);
                    ImageElement img = null;
                    if (selectedView2dContainer != null) {
                        img = selectedView2dContainer.getSelectedImagePane().getImage();
                    }
                    if (img == null) {
                        return;
                    }
                    PresetWindowLevel preset = (PresetWindowLevel) object;

                    if (preset.equals(PresetWindowLevel.DEFAULT)) {
                        windowAction.setValue((int) img.getDefaultWindow());
                        levelAction.setValue((int) img.getDefaultLevel());
                    } else if (preset.equals(PresetWindowLevel.AUTO)) {
                        int min = (int) img.getMinValue();
                        int max = (int) img.getMaxValue();
                        windowAction.setValue(max - min);
                        levelAction.setValue((max - min) / 2 + min);
                    } else if (!preset.equals(PresetWindowLevel.CUSTOM)) {
                        windowAction.setValue((int) preset.getWindow());
                        levelAction.setValue((int) preset.getLevel());
                    }

                    firePropertyChange(action.cmd(), null, preset);
                    windowAction.getModel().addChangeListener(this);
                    levelAction.getModel().addChangeListener(this);
                }
            }

            @Override
            public void stateChanged(ChangeEvent evt) {
                model.removeListDataListener(this);
                model.setSelectedItem(PresetWindowLevel.CUSTOM);
                model.addListDataListener(this);
                firePropertyChange(action.cmd(), null, PresetWindowLevel.CUSTOM);
            }

        };
    }

    private ComboItemListener newLutAction() {
        return new ComboItemListener(ActionW.LUT, LutManager.getLutCollection()) {

            @Override
            public void itemStateChanged(Object object) {
                if (object instanceof ByteLut) {
                    // customPreset = false;
                    firePropertyChange(action.cmd(), null, object);
                }
            }
        };
    }

    private ToggleButtonListener newImageOverlayAction() {
        return new ToggleButtonListener(ActionW.IMAGE_OVERLAY, true) {

            @Override
            public void actionPerformed(boolean selected) {
                firePropertyChange(action.cmd(), null, selected);
            }
        };
    }

    private ComboItemListener newSortStackAction() {
        return new ComboItemListener(ActionW.SORTSTACK, SortSeriesStack.getValues()) {

            @Override
            public void itemStateChanged(Object object) {
                firePropertyChange(action.cmd(), null, object);
            }
        };
    }

    private ComboItemListener newViewingProtocolAction() {
        return new ComboItemListener(ActionW.VIEWINGPROTOCOL, ViewingProtocols.getValues()) {

            @Override
            public void itemStateChanged(Object object) {
                firePropertyChange(action.cmd(), null, object);
            }
        };
    }

    @Override
    protected ToggleButtonListener newInverseStackAction() {
        return new ToggleButtonListener(ActionW.INVERSESTACK, false) {

            @Override
            public void actionPerformed(boolean selected) {
                firePropertyChange(action.cmd(), null, selected);
            }
        };
    }

    @Override
    public void setSelectedView2dContainer(ImageViewerPlugin<DicomImageElement> selectedView2dContainer) {
        if (this.selectedView2dContainer != null) {
            this.selectedView2dContainer.setMouseActions(null);
            this.selectedView2dContainer.setDrawActions(null);
            moveTroughSliceAction.stop();

        }
        this.selectedView2dContainer = selectedView2dContainer;
        if (selectedView2dContainer != null) {
            synchAction.setSelectedItemWithoutTriggerAction(selectedView2dContainer.getSynchView());
            layoutAction.setSelectedItemWithoutTriggerAction(selectedView2dContainer.getLayoutModel());
            updateComponentsListener(selectedView2dContainer.getSelectedImagePane());
            selectedView2dContainer.setMouseActions(mouseActions);
            Graphic graphic = null;
            ActionState action = getAction(ActionW.DRAW_MEASURE);
            if (action instanceof ComboItemListener) {
                graphic = (Graphic) ((ComboItemListener) action).getSelectedItem();
            }
            selectedView2dContainer.setDrawActions(graphic);
        }
    }

    @Override
    public ZoomSetting getZoomSetting() {
        return zoomSetting;
    }

    /** process the action events. */

    public void actionPerformed(ActionEvent evt) {
        String command = evt.getActionCommand();

        if (command.equals(ActionW.CINESTART.cmd())) {
            // turn cining on.
            moveTroughSliceAction.start();
        } else if (command.equals(ActionW.CINESTOP.cmd())) {
            // turn cine off.
            moveTroughSliceAction.stop();
        }
    }

    public void resetAllActions() {
        firePropertyChange(ActionW.ZOOM.cmd(), null, 0.0);
        if (selectedView2dContainer != null) {
            DefaultView2d viewPane = selectedView2dContainer.getSelectedImagePane();
            if (viewPane != null) {
                viewPane.center();
            }
        }
        presetAction.setSelectedItem(PresetWindowLevel.DEFAULT);
        flipAction.setSelected(false);
        rotateAction.setValue(0);
        inverseLutAction.setSelected(false);
        lutAction.setSelectedItem(ByteLut.defaultLUT);
        filterAction.setSelectedItem(KernelData.NONE);
    }

    public void reset(ResetTools action) {
        if (ResetTools.All.equals(action)) {
            resetAllActions();
        } else if (ResetTools.Zoom.equals(action)) {
            // Pass the value 0.0 (convention: best fit zoom value) directly to the property change, otherwise the
            // value is adjusted by the BoundedRangeModel
            firePropertyChange(ActionW.ZOOM.cmd(), null, 0.0);

        } else if (ResetTools.Rotation.equals(action)) {
            rotateAction.setValue(0);
        } else if (ResetTools.WindowLevel.equals(action)) {
            presetAction.setSelectedItem(PresetWindowLevel.DEFAULT);
        } else if (ResetTools.Pan.equals(action)) {
            if (selectedView2dContainer != null) {
                DefaultView2d viewPane = selectedView2dContainer.getSelectedImagePane();
                if (viewPane != null) {
                    viewPane.center();
                }
            }
        }
    }

    @Override
    public synchronized boolean updateComponentsListener(DefaultView2d<DicomImageElement> defaultView2d) {
        if (defaultView2d == null) {
            return false;
        }
        Content selectedContent = UIManager.toolWindowManager.getContentManager().getSelectedContent();
        if (selectedContent == null || selectedContent.getComponent() != selectedView2dContainer) {
            return false;
        }
        if (selectedView2dContainer == null || defaultView2d != selectedView2dContainer.getSelectedImagePane()) {
            return false;
        }
        // System.out.println(v.getId() + ": udpate");
        // selectedView2dContainer.setSelectedImagePane(v);
        clearAllPropertyChangeListeners();
        if (defaultView2d.getSourceImage() == null) {
            enableActions(false);
            return false;
        }
        if (!enabledAction) {
            enableActions(true);
        }
        ImageElement image = defaultView2d.getImage();
        MediaSeries<DicomImageElement> series = defaultView2d.getSeries();
        windowAction.setMinMaxValueWithoutTriggerAction(0, (int) (image.getMaxValue() - image.getMinValue()),
            ((Float) defaultView2d.getActionValue(ActionW.WINDOW.cmd())).intValue());
        levelAction.setMinMaxValueWithoutTriggerAction((int) image.getMinValue(), (int) image.getMaxValue(),
            ((Float) defaultView2d.getActionValue(ActionW.LEVEL.cmd())).intValue());
        rotateAction.setValueWithoutTriggerAction((Integer) defaultView2d.getActionValue(ActionW.ROTATION.cmd()));
        flipAction.setSelectedWithoutTriggerAction((Boolean) defaultView2d.getActionValue(ActionW.FLIP.cmd()));
        zoomAction.setValueWithoutTriggerAction(viewScaleToSliderValue(Math.abs((Double) defaultView2d
            .getActionValue(ActionW.ZOOM.cmd()))));
        showLensAction.setSelectedWithoutTriggerAction((Boolean) defaultView2d.getActionValue(ActionW.LENS.cmd()));
        Double lensZoom = (Double) defaultView2d.getLensActionValue(ActionW.ZOOM.cmd());
        if (lensZoom != null) {
            lensZoomAction.setValueWithoutTriggerAction(viewScaleToSliderValue(Math.abs(lensZoom)));
        }
        PresetWindowLevel[] presets = PresetWindowLevel.getPresetCollection((String) series.getTagValue(TagW.Modality));
        presetAction.setDataList(presets);
        presetAction.setSelectedItemWithoutTriggerAction(defaultView2d.getActionValue(ActionW.PRESET.cmd()));
        moveTroughSliceAction.setMinMaxValue(1, series.size(), defaultView2d.getFrameIndex() + 1);
        Integer speed = (Integer) series.getTagValue(TagW.CineRate);
        if (speed != null) {
            moveTroughSliceAction.setSpeed(speed);
        }
        lutAction.setSelectedItemWithoutTriggerAction(defaultView2d.getActionValue(ActionW.LUT.cmd()));
        inverseLutAction.setSelectedWithoutTriggerAction((Boolean) defaultView2d.getActionValue(ActionW.INVERSELUT
            .cmd()));
        filterAction.setSelectedItemWithoutTriggerAction(defaultView2d.getActionValue(ActionW.FILTER.cmd()));
        imageOverlayAction.setSelectedWithoutTriggerAction((Boolean) defaultView2d.getActionValue(ActionW.IMAGE_OVERLAY
            .cmd()));
        sortStackAction.setSelectedItemWithoutTriggerAction(defaultView2d.getActionValue(ActionW.SORTSTACK.cmd()));
        viewingProtocolAction.setSelectedItemWithoutTriggerAction(defaultView2d.getActionValue(ActionW.VIEWINGPROTOCOL
            .cmd()));
        inverseStackAction.setSelectedWithoutTriggerAction((Boolean) defaultView2d.getActionValue(ActionW.INVERSESTACK
            .cmd()));
        // register all actions for the selected view and for the other views register according to synchview.
        updateAllListeners(selectedView2dContainer, (SynchView) synchAction.getSelectedItem());
        return true;
    }

    @Override
    protected boolean isCompatible(MediaSeries<DicomImageElement> series1, MediaSeries<DicomImageElement> series2) {
        // Have the two series the same image plane orientation
        return ImageOrientation.hasSameOrientation(series1, series2);
    }

    @Override
    public void updateAllListeners(ImageViewerPlugin<DicomImageElement> viewerPlugin, SynchView synchView) {
        clearAllPropertyChangeListeners();
        if (viewerPlugin != null) {
            DefaultView2d<DicomImageElement> viewPane = viewerPlugin.getSelectedImagePane();
            // if (viewPane == null || viewPane.getSeries() == null) {
            if (viewPane == null) {
                return;
            }
            MediaSeries<DicomImageElement> series = viewPane.getSeries();
            if (series != null) {
                addPropertyChangeListeners(viewPane);
                final ArrayList<DefaultView2d<DicomImageElement>> panes = viewerPlugin.getImagePanels();
                panes.remove(viewPane);
                viewPane.setActionsInView(ActionW.SYNCH_CROSSLINE.cmd(), false);

                if (SynchView.NONE.equals(synchView)) {
                    for (int i = 0; i < panes.size(); i++) {
                        DefaultView2d<DicomImageElement> pane = panes.get(i);
                        AbstractLayer layer = pane.getLayerModel().getLayer(Tools.CROSSLINES.getId());
                        if (layer != null) {
                            layer.deleteAllGraphic();
                        }
                        MediaSeries<DicomImageElement> s = pane.getSeries();
                        String fruid = (String) series.getTagValue(TagW.FrameOfReferenceUID);
                        if (s != null && fruid != null) {
                            if (fruid.equals(s.getTagValue(TagW.FrameOfReferenceUID))) {
                                if (!ImageOrientation.hasSameOrientation(series, s)) {
                                    pane.setActionsInView(ActionW.SYNCH_CROSSLINE.cmd(), true);
                                    propertySupport.addPropertyChangeListener(ActionW.SCROLL_SERIES.cmd(), pane);
                                }
                                // Force to draw crosslines without changing the slice position
                                moveTroughSliceAction.stateChanged(moveTroughSliceAction.getModel());
                            }
                        }
                    }
                } else {
                    // TODO if Pan is activated than rotation is required
                    if (Mode.Stack.equals(synchView.getMode())) {
                        boolean hasLink = false;
                        String fruid = (String) series.getTagValue(TagW.FrameOfReferenceUID);
                        DicomImageElement img = series.getMedia(MEDIA_POSITION.MIDDLE);
                        double[] val = img == null ? null : (double[]) img.getTagValue(TagW.SlicePosition);

                        for (int i = 0; i < panes.size(); i++) {
                            DefaultView2d<DicomImageElement> pane = panes.get(i);
                            AbstractLayer layer = pane.getLayerModel().getLayer(Tools.CROSSLINES.getId());
                            if (layer != null) {
                                layer.deleteAllGraphic();
                            }
                            MediaSeries<DicomImageElement> s = pane.getSeries();
                            if (s != null && fruid != null && val != null) {
                                if (fruid.equals(s.getTagValue(TagW.FrameOfReferenceUID))) {
                                    if (ImageOrientation.hasSameOrientation(series, s)) {
                                        hasLink = true;
                                        pane.setActionsInView(ActionW.SYNCH_LINK.cmd(), true);
                                        pane.setActionsInView(ActionW.SYNCH_CROSSLINE.cmd(), false);
                                        if (hasSameSizeAndSpatialCalibration(series, s)) {
                                            // If the image has the same reference and the same spatial calibration, all
                                            // the actions are synchronized
                                            addPropertyChangeListeners(pane, synchView);
                                        } else {
                                            propertySupport
                                                .addPropertyChangeListener(ActionW.SCROLL_SERIES.cmd(), pane);
                                        }
                                    } else {
                                        pane.setActionsInView(ActionW.SYNCH_LINK.cmd(), false);
                                        pane.setActionsInView(ActionW.SYNCH_CROSSLINE.cmd(), true);
                                        propertySupport.addPropertyChangeListener(ActionW.SCROLL_SERIES.cmd(), pane);
                                    }
                                }
                            }
                        }

                        viewPane.setActionsInView(ActionW.SYNCH_LINK.cmd(), hasLink);
                        // Force to draw crosslines without changing the slice position
                        moveTroughSliceAction.stateChanged(moveTroughSliceAction.getModel());

                    } else if (Mode.Tile.equals(synchView.getMode())) {
                        for (int i = 0; i < panes.size(); i++) {
                            DefaultView2d<DicomImageElement> pane = panes.get(i);
                            pane.setActionsInView(ActionW.SYNCH_LINK.cmd(), true);
                            pane.setActionsInView(ActionW.SYNCH_CROSSLINE.cmd(), false);
                            addPropertyChangeListeners(pane, synchView);
                        }
                    }
                }
            }
        }
    }

    public static boolean hasSameSizeAndSpatialCalibration(MediaSeries<DicomImageElement> series1,
        MediaSeries<DicomImageElement> series2) {
        // Test if the two series has the same orientation
        if (series1 != null && series2 != null) {
            DicomImageElement image1 = series1.getMedia(MEDIA_POSITION.MIDDLE);
            DicomImageElement image2 = series2.getMedia(MEDIA_POSITION.MIDDLE);
            if (image1 != null && image2 != null) {
                return image1.hasSameSizeAndSpatialCalibration(image2);
            }
        }
        return false;
    }

    public void savePreferences() {
        Preferences prefs = Activator.PREFERENCES.getDefaultPreferences();
        mouseActions.savePreferences(prefs);
        if (prefs != null) {
            Preferences prefNode = prefs.node("mouse.sensivity"); //$NON-NLS-1$
            BundlePreferences.putDoublePreferences(prefNode, windowAction.getActionW().cmd(),
                windowAction.getMouseSensivity());
            BundlePreferences.putDoublePreferences(prefNode, levelAction.getActionW().cmd(),
                levelAction.getMouseSensivity());
            BundlePreferences.putDoublePreferences(prefNode, moveTroughSliceAction.getActionW().cmd(),
                moveTroughSliceAction.getMouseSensivity());
            BundlePreferences.putDoublePreferences(prefNode, rotateAction.getActionW().cmd(),
                rotateAction.getMouseSensivity());
            BundlePreferences.putDoublePreferences(prefNode, zoomAction.getActionW().cmd(),
                zoomAction.getMouseSensivity());
        }
    }
}
