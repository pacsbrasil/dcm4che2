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
package org.weasis.base.ui.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Frame;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import org.noos.xing.mydoggy.Content;
import org.noos.xing.mydoggy.ContentManager;
import org.noos.xing.mydoggy.ContentManagerListener;
import org.noos.xing.mydoggy.ContentManagerUIListener;
import org.noos.xing.mydoggy.MultiSplitConstraint;
import org.noos.xing.mydoggy.MultiSplitContentManagerUI;
import org.noos.xing.mydoggy.TabbedContentManagerUI;
import org.noos.xing.mydoggy.TabbedContentUI;
import org.noos.xing.mydoggy.event.ContentManagerEvent;
import org.noos.xing.mydoggy.event.ContentManagerUIEvent;
import org.noos.xing.mydoggy.plaf.ui.content.MyDoggyMultiSplitContentManagerUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.base.ui.Messages;
import org.weasis.base.ui.action.ExitAction;
import org.weasis.base.ui.action.OpenPreferencesAction;
import org.weasis.core.api.explorer.DataExplorerView;
import org.weasis.core.api.explorer.ObservableEvent;
import org.weasis.core.api.explorer.model.DataExplorerModel;
import org.weasis.core.api.explorer.model.TreeModel;
import org.weasis.core.api.explorer.model.TreeModelNode;
import org.weasis.core.api.gui.util.AbstractProperties;
import org.weasis.core.api.gui.util.GhostGlassPane;
import org.weasis.core.api.gui.util.GuiExecutor;
import org.weasis.core.api.gui.util.JMVUtils;
import org.weasis.core.api.media.data.MediaSeries;
import org.weasis.core.api.media.data.MediaSeriesGroup;
import org.weasis.core.ui.docking.PluginTool;
import org.weasis.core.ui.docking.UIManager;
import org.weasis.core.ui.editor.SeriesViewerFactory;
import org.weasis.core.ui.editor.ViewerPluginBuilder;
import org.weasis.core.ui.editor.image.DefaultView2d;
import org.weasis.core.ui.editor.image.ImageViewerPlugin;
import org.weasis.core.ui.editor.image.ViewerPlugin;
import org.weasis.core.ui.util.ToolBarContainer;
import org.weasis.core.ui.util.WtoolBar;

public class WeasisWin extends JFrame implements PropertyChangeListener {

    private static final Logger log = LoggerFactory.getLogger(WeasisWin.class);

    private static final JMenu menuFile = new JMenu(Messages.getString("WeasisWin.file")); //$NON-NLS-1$
    private static final JMenu menuSelectedPlugin = new JMenu();
    private static ViewerPlugin selectedPlugin = null;
    private final static WeasisWin instance = new WeasisWin();

    private final ToolBarContainer toolbarContainer;

    private final boolean busy = false;

    private WeasisWin() {
        this.setJMenuBar(createMenuBar());
        toolbarContainer = new ToolBarContainer();
        toolbarContainer.setLayout(new BoxLayout(toolbarContainer, BoxLayout.PAGE_AXIS));
        this.getContentPane().add(toolbarContainer, BorderLayout.NORTH);
        this.setTitle("Weasis v" + AbstractProperties.WEASIS_VERSION); //$NON-NLS-1$
        this.setIconImage(new ImageIcon(getClass().getResource("/logo-button.png")).getImage()); //$NON-NLS-1$
    }

    public static WeasisWin getInstance() {
        return instance;
    }

    // Overridden so we can exit when window is closed
    @Override
    protected void processWindowEvent(WindowEvent e) {
        if (e.getID() == WindowEvent.WINDOW_CLOSING) {
            if (!closeWindow()) {
                return;
            }
        }
        super.processWindowEvent(e);
    }

    public boolean closeWindow() {
        if (busy) {
            // TODO add a message, Please wait or kill
            return false;
        }
        int option = JOptionPane.showConfirmDialog(instance, Messages.getString("WeasisWin.exit_mes")); //$NON-NLS-1$
        if (option == JOptionPane.YES_OPTION) {
            System.exit(0);
            return true;
        }
        return false;
    }

    private void initToolWindowManager() throws Exception {
        initContentManager();
        // Add myDoggyToolWindowManager to the frame. MyDoggyToolWindowManager is an extension of a JPanel
        this.getContentPane().add(UIManager.toolWindowManager, BorderLayout.CENTER);
    }

    protected void initContentManager() throws Exception {

        ContentManager contentManager = UIManager.toolWindowManager.getContentManager();
        MultiSplitContentManagerUI contentManagerUI = new MyDoggyMultiSplitContentManagerUI();
        contentManager.setContentManagerUI(contentManagerUI);
        contentManagerUI.setMinimizable(false);
        contentManagerUI.setShowAlwaysTab(true);
        contentManagerUI.setTabPlacement(TabbedContentManagerUI.TabPlacement.TOP);
        contentManagerUI.addContentManagerUIListener(new ContentManagerUIListener() {

            public boolean contentUIRemoving(ContentManagerUIEvent event) {
                // boolean ok = JOptionPane.showConfirmDialog(instance, "Are you sure?") == JOptionPane.OK_OPTION;
                // if (ok) {
                Component c = event.getContentUI().getContent().getComponent();
                if (c instanceof ViewerPlugin) {
                    UIManager.VIEWER_PLUGINS.remove(c);
                    // close the content of the plugin
                    ((ViewerPlugin) c).close();
                }
                // }
                return true;
            }

            public void contentUIDetached(ContentManagerUIEvent event) {
            }
        });

        contentManager.addContentManagerListener(new ContentManagerListener() {

            @Override
            public void contentSelected(ContentManagerEvent event) {
                Component plugin = event.getContent().getComponent();
                if (plugin instanceof ViewerPlugin) {
                    if (ContentManagerEvent.ActionId.CONTENT_SELECTED.equals(event.getId())) {
                        setSelectedPlugin((ViewerPlugin) plugin);
                    }
                }
            }

            @Override
            public void contentRemoved(ContentManagerEvent event) {
            }

            @Override
            public void contentAdded(ContentManagerEvent event) {
            }
        });
    }

    public void createMainPanel() throws Exception {
        initToolWindowManager();
        this.setGlassPane(AbstractProperties.glassPane);

    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        // Get only ObservableEvent
        if (evt instanceof ObservableEvent) {
            ObservableEvent event = (ObservableEvent) evt;
            ObservableEvent.BasicAction action = event.getActionCommand();
            Object source = event.getNewValue();
            if (evt.getSource() instanceof DataExplorerModel) {
                if (ObservableEvent.BasicAction.Select.equals(action)) {
                    if (source instanceof DataExplorerModel) {
                        DataExplorerModel model = (DataExplorerModel) source;
                        DataExplorerView view = null;
                        synchronized (UIManager.EXPLORER_PLUGINS) {
                            List<DataExplorerView> explorers = UIManager.EXPLORER_PLUGINS;
                            for (DataExplorerView dataExplorerView : explorers) {
                                if (dataExplorerView.getDataExplorerModel() == model) {
                                    view = dataExplorerView;
                                    break;
                                }
                            }
                            if (view instanceof PluginTool) {
                                ((PluginTool) view).showDockable();
                            }
                        }
                    }
                    // Select a plugin from that as the same key as the
                    // MediaSeriesGroup
                    else if (source instanceof MediaSeriesGroup) {
                        MediaSeriesGroup group = (MediaSeriesGroup) source;
                        synchronized (UIManager.VIEWER_PLUGINS) {
                            for (int i = UIManager.VIEWER_PLUGINS.size() - 1; i >= 0; i--) {
                                ViewerPlugin p = UIManager.VIEWER_PLUGINS.get(i);
                                if (group.equals(p.getGroupID())) {
                                    p.setSelectedAndGetFocus();
                                    break;
                                }
                            }
                        }
                    }
                } else if (ObservableEvent.BasicAction.Register.equals(action)) {
                    if (source instanceof ViewerPlugin) {
                        registerPlugin((ViewerPlugin) source);
                    } else if (source instanceof ViewerPluginBuilder) {
                        ViewerPluginBuilder builder = (ViewerPluginBuilder) source;
                        SeriesViewerFactory factory = builder.getFactory();
                        DataExplorerModel model = builder.getModel();
                        MediaSeries[] series = builder.getSeries();

                        if (builder.isCompareEntryToBuildNewViewer() && builder.getEntry() != null
                            && model instanceof TreeModel) {
                            TreeModel treeModel = (TreeModel) model;
                            if (series.length == 1) {
                                MediaSeries s = series[0];
                                MediaSeriesGroup group = treeModel.getParent(s, builder.getEntry());
                                openSeriesInViewerPlugin(factory, model, group, series);
                            } else if (series.length > 1) {
                                HashMap<MediaSeriesGroup, List<MediaSeries>> map =
                                    getSeriesByEntry(treeModel, series, builder.getEntry());
                                for (Iterator<Entry<MediaSeriesGroup, List<MediaSeries>>> iterator =
                                    map.entrySet().iterator(); iterator.hasNext();) {
                                    Entry<MediaSeriesGroup, List<MediaSeries>> entry = iterator.next();
                                    MediaSeriesGroup group = entry.getKey();
                                    List<MediaSeries> seriesList = entry.getValue();
                                    openSeriesInViewerPlugin(factory, model, group, seriesList
                                        .toArray(new MediaSeries[seriesList.size()]));

                                }
                            }

                        } else {
                            openSeriesInViewerPlugin(factory, model, null, series);

                        }

                    }
                } else if (ObservableEvent.BasicAction.Unregister.equals(action)) {
                    if (source instanceof SeriesViewerFactory) {
                        SeriesViewerFactory viewerFactory = (SeriesViewerFactory) source;
                        final List<ViewerPlugin> pluginsToRemove = new ArrayList<ViewerPlugin>();
                        String name = viewerFactory.getUIName();
                        synchronized (UIManager.VIEWER_PLUGINS) {
                            for (final ViewerPlugin plugin : UIManager.VIEWER_PLUGINS) {
                                if (name.equals(plugin.getName())) {
                                    // Do not close Series directly, it can produce deadlock.
                                    pluginsToRemove.add(plugin);
                                }
                            }
                        }
                        GuiExecutor.instance().execute(new Runnable() {

                            @Override
                            public void run() {
                                for (final ViewerPlugin viewerPlugin : pluginsToRemove) {
                                    viewerPlugin.close();
                                    Content content =
                                        UIManager.toolWindowManager.getContentManager().getContent(
                                            viewerPlugin.getDockableUID());
                                    if (content != null) {
                                        UIManager.toolWindowManager.getContentManager().removeContent(content);
                                    }
                                }
                            }
                        });
                    }
                }
            }
        }
    }

    private HashMap<MediaSeriesGroup, List<MediaSeries>> getSeriesByEntry(TreeModel treeModel, MediaSeries[] series,
        TreeModelNode entry) {
        HashMap<MediaSeriesGroup, List<MediaSeries>> map = new HashMap<MediaSeriesGroup, List<MediaSeries>>();
        for (MediaSeries s : series) {
            MediaSeriesGroup entry1 = treeModel.getParent(s, entry);
            List<MediaSeries> seriesList = map.get(entry1);
            if (seriesList == null) {
                seriesList = new ArrayList<MediaSeries>();
            }
            seriesList.add(s);
            map.put(entry1, seriesList);
        }
        return map;
    }

    private void openSeriesInViewerPlugin(SeriesViewerFactory factory, DataExplorerModel model, MediaSeriesGroup group,
        MediaSeries[] seriesList) {
        if (seriesList == null || seriesList.length == 0) {
            return;
        }
        ImageViewerPlugin viewer = null;
        if (factory != null && group != null) {
            synchronized (UIManager.VIEWER_PLUGINS) {
                for (final ViewerPlugin p : UIManager.VIEWER_PLUGINS) {
                    if (p instanceof ImageViewerPlugin && p.getName().equals(factory.getUIName())
                        && group.equals(p.getGroupID())) {

                        viewer = ((ImageViewerPlugin) p);
                        viewer.changeLayoutModel(viewer.getBestDefaultViewLayout(seriesList.length));

                        p.setSelectedAndGetFocus();
                        selectLayoutPositionForAddingSeries(viewer, seriesList.length);
                        for (int i = 0; i < seriesList.length; i++) {
                            viewer.addSeries(seriesList[i]);
                        }
                        p.setSelected(true);
                        p.repaint();
                        return;
                    }
                }
            }
        }
        // Pass the DataExplorerModel to the viewer
        Hashtable<String, Object> properties = new Hashtable<String, Object>();
        properties.put(DataExplorerModel.class.getName(), model);
        if (seriesList.length > 1) {
            properties.put(DefaultView2d.class.getName(), seriesList.length);
        }
        viewer = (ImageViewerPlugin) factory.createSeriesViewer(properties);
        if (viewer != null) {
            if (group != null) {
                viewer.setGroupID(group);
                viewer.setPluginName(group.toString());
            }
            registerPlugin(viewer);
            viewer.setSelectedAndGetFocus();
            selectLayoutPositionForAddingSeries(viewer, seriesList.length);
            for (int i = 0; i < seriesList.length; i++) {
                viewer.addSeries(seriesList[i]);
            }
            viewer.setSelected(true);
        }
    }

    private void selectLayoutPositionForAddingSeries(ImageViewerPlugin viewer, int seriesNumber) {
        ArrayList<DefaultView2d> view2ds = viewer.getImagePanels();
        int pos = view2ds.size() - seriesNumber;
        if (pos < 0) {
            pos = 0;
        }
        viewer.setSelectedImagePane(view2ds.get(pos));
    }

    public void registerPlugin(final ViewerPlugin plugin) {
        synchronized (UIManager.VIEWER_PLUGINS) {
            if (plugin == null || UIManager.VIEWER_PLUGINS.contains(plugin)) {
                return;
            }
            UIManager.VIEWER_PLUGINS.add(plugin);
            ContentManager contentManager = UIManager.toolWindowManager.getContentManager();
            if (contentManager.getContentCount() > 0) {
                Content win = contentManager.getContent(plugin.getDockableUID());
                if (win == null) {
                    contentManager.addContent(plugin.getDockableUID(), plugin.getPluginName(), plugin.getIcon(),
                        plugin, null, new MultiSplitConstraint(contentManager.getContent(0), 0));
                }
            } else {
                contentManager.addContent(plugin.getDockableUID(), plugin.getPluginName(), plugin.getIcon(), plugin);
                TabbedContentUI contentUI =
                    (TabbedContentUI) UIManager.toolWindowManager.getContentManager().getContent(0).getContentUI();
                // Or you can use :
                // TabbedContentUI contentUI =
                // contentManagerUI.getContentUI(toolWindowManager.getContentManager().getContent(0));
                // without the need of the cast

                contentUI.setCloseable(true);
                contentUI.setDetachable(true);
                contentUI.setTransparentMode(true);
                contentUI.setTransparentRatio(0.7f);
                contentUI.setTransparentDelay(1000);
                // contentUI.setAddToTaskBarWhenDetached(true);
                contentUI.setMinimizable(false);
            }
        }
    }

    public synchronized ViewerPlugin getSelectedPlugin() {
        return selectedPlugin;
    }

    public synchronized void setSelectedPlugin(ViewerPlugin plugin) {
        if (plugin == null) {
            return;
        }
        if (selectedPlugin == plugin) {
            plugin.requestFocusInWindow();
            return;
        }
        ViewerPlugin oldPlugin = selectedPlugin;
        selectedPlugin = plugin;
        selectedPlugin.setSelected(true);
        selectedPlugin.fillSelectedPluginMenu(menuSelectedPlugin);

        PluginTool[] tool = selectedPlugin.getToolPanel();
        PluginTool[] oldTool = oldPlugin == null ? null : oldPlugin.getToolPanel();

        if (tool == null) {
            if (oldTool != null) {
                for (PluginTool pluginTool : oldTool) {
                    pluginTool.closeDockable();
                }
            }
        } else {
            if (tool != oldTool) {
                if (oldTool != null) {
                    for (PluginTool pluginTool : oldTool) {
                        pluginTool.closeDockable();
                    }
                }
                for (PluginTool pluginTool : tool) {
                    pluginTool.registerToolAsDockable();
                }
            }
        }

        WtoolBar toolBar = selectedPlugin.getToolBar();
        WtoolBar oldToolBar = oldPlugin == null ? null : oldPlugin.getToolBar();

        if (toolBar != oldToolBar) {
            toolbarContainer.registerToolBar(toolBar);
            toolbarContainer.revalidate();
            toolbarContainer.repaint();
        }

    }

    @Override
    public GhostGlassPane getGlassPane() {
        return AbstractProperties.glassPane;
    }

    public void showWindow() throws Exception {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        Toolkit kit = Toolkit.getDefaultToolkit();
        // Rectangle maxBound = ge.getMaximumWindowBounds();
        // TODO command line maximize screen: 0 => all screens, 1,2 => first,
        // second screen or 1-2 for two screens, or 2-4
        // three screens from the second one
        int minScreen = 0;
        int maxScreen = 0;
        Rectangle bound = null;
        // Get size of each screen
        GraphicsDevice[] gs = ge.getScreenDevices();
        minScreen = minScreen < gs.length ? minScreen : gs.length - 1;
        maxScreen = maxScreen < minScreen ? minScreen : maxScreen < gs.length ? maxScreen : gs.length - 1;
        for (int j = minScreen; j <= maxScreen; j++) {
            GraphicsConfiguration config = gs[j].getDefaultConfiguration();
            Rectangle b = config.getBounds();
            Insets inset = kit.getScreenInsets(config);
            b.x -= inset.left;
            b.y -= inset.top;
            b.width -= inset.right;
            b.height -= inset.bottom;
            if (bound == null) {
                bound = b;
            } else {
                bound = bound.union(b);
            }
        }
        setMaximizedBounds(bound);
        setLocation(bound.x, bound.y);
        // set a valid size, insets of screen is often non consistent
        setSize(bound.width - 100, bound.height - 100);

        setVisible(true);

        // Let time to paint
        try {
            Thread.sleep(250);
        } catch (InterruptedException e) {
        }
        // Must be execute after setVisible() to work on Linux
        setExtendedState(Frame.MAXIMIZED_BOTH);
        log.info("End of loading the GUI..."); //$NON-NLS-1$
    }

    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        buildMenuFile();
        menuBar.add(menuFile);
        menuBar.add(menuSelectedPlugin);
        final JMenu helpMenuItem = new JMenu(Messages.getString("WeasisWin.help")); //$NON-NLS-1$

        final JMenuItem aboutMenuItem = new JMenuItem(Messages.getString("WeasisAboutBox.title")); //$NON-NLS-1$
        aboutMenuItem.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                WeasisAboutBox about = new WeasisAboutBox();
                JMVUtils.showCenterScreen(about, instance);
            }
        });
        helpMenuItem.add(aboutMenuItem);
        menuBar.add(helpMenuItem);
        return menuBar;
    }

    private static void buildMenuFile() {
        menuFile.removeAll();
        final JMenu importMenu = new JMenu(Messages.getString("WeasisWin.import")); //$NON-NLS-1$
        JPopupMenu menuImport = importMenu.getPopupMenu();
        menuImport.addPopupMenuListener(new PopupMenuListener() {

            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                synchronized (UIManager.EXPLORER_PLUGINS) {
                    List<DataExplorerView> explorers = UIManager.EXPLORER_PLUGINS;
                    for (final DataExplorerView dataExplorerView : explorers) {
                        Action action = dataExplorerView.getOpenImportDialogAction();
                        if (action != null) {
                            JMenuItem item = new JMenuItem(action);
                            importMenu.add(item);
                        }
                    }
                }
            }

            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                importMenu.removeAll();
            }

            @Override
            public void popupMenuCanceled(PopupMenuEvent e) {
            }
        });
        menuFile.add(importMenu);

        final JMenu exportMenu = new JMenu(Messages.getString("WeasisWin.export")); //$NON-NLS-1$
        JPopupMenu menuExport = exportMenu.getPopupMenu();
        menuExport.addPopupMenuListener(new PopupMenuListener() {

            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                // TODO export workspace in as preference

                //                final AbstractAction saveAction = new AbstractAction("Save workspace layout") { //$NON-NLS-1$
                //
                // @Override
                // public void actionPerformed(ActionEvent e) {
                // // Handle workspace ui persistence
                // PersistenceDelegate pstDelegate = UIManager.toolWindowManager.getPersistenceDelegate();
                // try {
                //                                pstDelegate.save(new FileOutputStream(new File("/home/nicolas/Documents/test.xml"))); //$NON-NLS-1$
                // } catch (FileNotFoundException e1) {
                // e1.printStackTrace();
                // }
                // }
                // };
                // exportMenu.add(saveAction);

                synchronized (UIManager.EXPLORER_PLUGINS) {
                    if (selectedPlugin != null) {
                        Action[] actions = selectedPlugin.getExportActions();
                        if (actions != null) {
                            for (Action action : actions) {
                                JMenuItem item = new JMenuItem(action);
                                exportMenu.add(item);
                            }
                        }
                    }

                    List<DataExplorerView> explorers = UIManager.EXPLORER_PLUGINS;
                    for (final DataExplorerView dataExplorerView : explorers) {
                        Action action = dataExplorerView.getOpenExportDialogAction();
                        if (action != null) {
                            JMenuItem item = new JMenuItem(action);
                            exportMenu.add(item);
                        }
                    }
                }
            }

            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                exportMenu.removeAll();
            }

            @Override
            public void popupMenuCanceled(PopupMenuEvent e) {
            }
        });
        menuFile.add(exportMenu);
        menuFile.add(new JSeparator());
        menuFile.add(new JMenuItem(OpenPreferencesAction.getInstance()));
        menuFile.add(new JSeparator());
        menuFile.add(new JMenuItem(ExitAction.getInstance()));
    }

}
