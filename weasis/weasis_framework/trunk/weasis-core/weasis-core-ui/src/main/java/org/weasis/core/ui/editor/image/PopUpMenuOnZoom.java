/*******************************************************************************
 * Copyright (c) 2011 Nicolas Roduit.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Nicolas Roduit - initial API and implementation
 ******************************************************************************/
package org.weasis.core.ui.editor.image;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;

import org.weasis.core.api.gui.util.ActionState;
import org.weasis.core.api.gui.util.ActionW;
import org.weasis.core.api.gui.util.SliderChangeListener;
import org.weasis.core.ui.Messages;
import org.weasis.core.ui.editor.image.ZoomWin.SYNCH_TYPE;

/**
 * The Class PopUpMenuOnZoom.
 * 
 * @author Nicolas Roduit
 */
public class PopUpMenuOnZoom extends JPopupMenu {

    /** The display image zone */
    private final ZoomWin zoomWin;
    private final JMenuItem jMenuItemZoom = new JMenuItem();
    private final ButtonGroup buttonMagnify = new ButtonGroup();
    private final int[] magnify = { 1, 2, 3, 4, 6 };
    private JRadioButtonMenuItem[] jRadioButtonMenuItemMagnify;
    private final ActionListener magnifyListener = new java.awt.event.ActionListener() {

        @Override
        public void actionPerformed(ActionEvent e) {
            magnifyActionPerformed(e);
        }
    };
    private final JMenu jMenuMagnify = new JMenu();
    private final JMenu jMenuImage = new JMenu();
    private final JRadioButtonMenuItem jMenuItemMagnifyOther = new JRadioButtonMenuItem();
    private final JCheckBoxMenuItem jCheckBoxMenuItemDraw = new JCheckBoxMenuItem();
    private final JCheckBoxMenuItem jCheckBoxMenutemSychronize = new JCheckBoxMenuItem();
    private final JMenuItem resetFreeze = new JMenuItem(Messages.getString("PopUpMenuOnZoom.reset")); //$NON-NLS-1$

    public PopUpMenuOnZoom(ZoomWin zoomWin) {
        if (zoomWin == null) {
            throw new IllegalArgumentException("ZoomWin cannot be null"); //$NON-NLS-1$
        }
        this.zoomWin = zoomWin;
        try {
            jbInit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void jbInit() throws Exception {
        jMenuItemZoom.setText(Messages.getString("PopUpMenuOnZoom.hide")); //$NON-NLS-1$
        jMenuItemZoom.addActionListener(new java.awt.event.ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                zoomWin.hideZoom();
            }
        });
        jCheckBoxMenuItemDraw.setText(Messages.getString("PopUpMenuOnZoom.showDraw")); //$NON-NLS-1$
        jCheckBoxMenuItemDraw.addActionListener(new java.awt.event.ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                zoomWin.setActionInView(ActionW.DRAW.cmd(), jCheckBoxMenuItemDraw.isSelected());
                zoomWin.repaint();
            }
        });
        jMenuImage.setText(Messages.getString("PopUpMenuOnZoom.image")); //$NON-NLS-1$
        final JMenuItem freezParams = new JMenuItem(Messages.getString("PopUpMenuOnZoom.freeze")); //$NON-NLS-1$
        freezParams.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                zoomWin.setFreezeImage(zoomWin.freezeParentParameters(), SYNCH_TYPE.ParentParameters);
            }
        });
        jMenuImage.add(freezParams);
        final JMenuItem freeze = new JMenuItem(Messages.getString("PopUpMenuOnZoom.freezeImg")); //$NON-NLS-1$
        freeze.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                zoomWin.setFreezeImage(zoomWin.freezeParentImage(), SYNCH_TYPE.ParentImage);
            }
        });
        jMenuImage.add(freeze);
        jMenuImage.addSeparator();
        resetFreeze.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                zoomWin.setFreezeImage(null, SYNCH_TYPE.None);
            }
        });
        jMenuImage.add(resetFreeze);

        jMenuMagnify.setText(Messages.getString("PopUpMenuOnZoom.magnify")); //$NON-NLS-1$
        jCheckBoxMenutemSychronize.setText(Messages.getString("PopUpMenuOnZoom.synch")); //$NON-NLS-1$
        jCheckBoxMenutemSychronize.addActionListener(new java.awt.event.ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                zoomWin.setActionInView(ZoomWin.SYNCH_CMD, jCheckBoxMenutemSychronize.isSelected());
                zoomWin.updateZoom();
            }
        });
        this.add(jMenuItemZoom);
        this.addSeparator();
        this.add(jCheckBoxMenuItemDraw);
        this.add(jCheckBoxMenutemSychronize);
        this.add(jMenuMagnify);
        iniMenuItemZoomMagnify();
        this.add(jMenuImage);
    }

    public void iniMenuItemZoomMagnify() {
        jRadioButtonMenuItemMagnify = new JRadioButtonMenuItem[magnify.length];
        for (int i = 0; i < jRadioButtonMenuItemMagnify.length; i++) {
            JRadioButtonMenuItem item = new JRadioButtonMenuItem();
            item.setText(magnify[i] + "X"); //$NON-NLS-1$
            buttonMagnify.add(item);
            item.addActionListener(magnifyListener);
            jMenuMagnify.add(item);
            jRadioButtonMenuItemMagnify[i] = item;
        }
    }

    public void enableMenuItem() {
        // Do not trigger actionLinstener
        jCheckBoxMenutemSychronize.setSelected((Boolean) zoomWin.getActionValue(ZoomWin.SYNCH_CMD));
        jCheckBoxMenuItemDraw.setSelected((Boolean) zoomWin.getActionValue(ActionW.DRAW.cmd()));
        Object img = zoomWin.getActionValue(ZoomWin.FREEZE_CMD);
        resetFreeze.setEnabled(img != null);

        // essaie de récupérer le magnitude du zoom, si ne correspond pas aux
        // magnitude possible : pas de sélection
        boolean noselection = true;
        if (jRadioButtonMenuItemMagnify.length < jMenuMagnify.getItemCount()) {
            jMenuMagnify.remove(jMenuItemMagnifyOther);
        }
        Double ratio = (Double) zoomWin.getActionValue(ActionW.ZOOM.cmd());
        if (ratio == null) {
            ratio = 1.0;
        }
        int currentZoomRatio = (int) (ratio * 100.0);
        for (int i = 0; i < jRadioButtonMenuItemMagnify.length; i++) {
            if ((magnify[i] * 100) == currentZoomRatio) {
                JRadioButtonMenuItem item3 = jRadioButtonMenuItemMagnify[i];
                // action setSelected ne déclenche pas l'actionLinstener
                item3.setSelected(true);
                noselection = false;
                break;
            }
        }
        if (noselection) {
            ratio = Math.abs(ratio);
            jMenuItemMagnifyOther.setText(ratio + "X"); //$NON-NLS-1$
            buttonMagnify.add(jMenuItemMagnifyOther);
            if ((magnify[magnify.length - 1]) < ratio) {
                jMenuMagnify.add(jMenuItemMagnifyOther);
            } else {
                int k = 0;
                for (int i = 0; i < magnify.length; i++) {
                    if ((magnify[i]) > ratio) {
                        k = i;
                        break;
                    }
                }
                jMenuMagnify.add(jMenuItemMagnifyOther, k);
            }
            jMenuItemMagnifyOther.setSelected(true);
        }
    }

    private void magnifyActionPerformed(ActionEvent e) {
        if (e.getSource() instanceof JRadioButtonMenuItem) {
            JRadioButtonMenuItem item = (JRadioButtonMenuItem) e.getSource();
            for (int i = 0; i < jRadioButtonMenuItemMagnify.length; i++) {
                if (item.equals(jRadioButtonMenuItemMagnify[i])) {
                    ImageViewerEventManager manager = zoomWin.getView2d().getEventManager();
                    ActionState zoomAction = manager.getAction(ActionW.LENSZOOM);
                    if (zoomAction instanceof SliderChangeListener) {
                        ((SliderChangeListener) zoomAction).setValue(manager.viewScaleToSliderValue(magnify[i]));
                    }
                    break;
                }
            }
        }
    }

}
