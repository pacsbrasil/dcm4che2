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
package org.weasis.core.ui.task;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.JProgressBar;

import org.weasis.core.api.util.FontTools;
import org.weasis.core.ui.Messages;

public class CircularProgressBar extends JProgressBar {
    private final static Color BACK_COLOR = new Color(82, 152, 219);
    private final Vector<InterruptionListener> interruptionListeners = new Vector<InterruptionListener>();

    public CircularProgressBar() {
        super();
        init();
    }

    public CircularProgressBar(int min, int max) {
        super(min, max);
        init();
    }

    private void init() {
        this.setOpaque(false);

        this.setToolTipText(Messages.getString("CircularProgressBar.tip")); //$NON-NLS-1$
        this.addMouseListener(new MouseClickMonitor());
    }

    @Override
    public void paint(Graphics g) {
        if (g instanceof Graphics2D) {
            draw((Graphics2D) g);
        }
    }

    private void draw(Graphics2D g2) {
        int h = this.getHeight();
        int w = this.getWidth();
        int range = this.getMaximum() - this.getMinimum();
        if (range < 1) {
            range = 1;
        }
        int a = 360 - this.getValue() * 360 / range;
        String str = (this.getValue() * 100 / range) + "%"; //$NON-NLS-1$

        float x = w / 2.0f - g2.getFontMetrics().stringWidth(str) / 2.0f;

        final float fontHeight = FontTools.getAccurateFontHeight(g2);
        final float midfontHeight = fontHeight * FontTools.getMidFontHeightFactor();

        float y = h / 2.0f + midfontHeight;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setPaint(Color.WHITE);
        g2.fillArc(0, 0, w, h, 0, 360);

        g2.setPaint(BACK_COLOR);
        g2.fillArc(0, 0, w, h, a, 360 - a);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_DEFAULT);

        g2.setPaint(Color.BLACK);
        g2.drawString(str, x, y);
    }

    public void addInterruptionListener(InterruptionListener interruptionListener) {
        interruptionListeners.add(interruptionListener);
    }

    public boolean removeInterruptionListener(InterruptionListener interruptionListener) {
        return interruptionListeners.remove(interruptionListener);
    }

    private class MouseClickMonitor extends MouseAdapter {

        @Override
        public void mousePressed(MouseEvent e) {
            // TODO mettre dans Thumbnail
            Iterator<InterruptionListener> i = interruptionListeners.iterator();
            while (i.hasNext()) {
                try {
                    i.next().interruptionRequested();
                    continue;
                } catch (ConcurrentModificationException modEx) {
                    System.err.println("Process completed before interruption could be requested."); //$NON-NLS-1$
                }
                break;
            }
        }
    }

    public void setMessage(String string) {
        // TODO Auto-generated method stub

    }

}
