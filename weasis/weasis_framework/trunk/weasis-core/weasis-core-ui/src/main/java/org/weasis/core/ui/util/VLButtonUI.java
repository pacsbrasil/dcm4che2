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
package org.weasis.core.ui.util;

import java.awt.Graphics;

import javax.swing.AbstractButton;
import javax.swing.JComponent;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.plaf.basic.BasicToggleButtonUI;

public class VLButtonUI extends BasicToggleButtonUI {

    public VLButtonUI() {
    }

    @Override
    public void paint(Graphics g, JComponent comp) {
        AbstractButton btn = (AbstractButton) comp;
        boolean rollover = btn.getModel().isRollover();
        boolean selected = btn.getModel().isSelected();
        boolean armed = btn.getModel().isArmed();
        btn.setBorderPainted(selected || rollover);
        if (rollover || selected) {
            if (armed) {
                g.translate(1, 1);
            } else {
                if (!selected) {
                    g.setColor(UIManager.getColor("controlHighlight"));
                    g.fillRect(1, 1, btn.getWidth() - 2, btn.getHeight() - 2);
                }
            }
        }

        Border b = comp.getBorder();
        if (b instanceof ToolBarButtonBorder) {
            ((ToolBarButtonBorder) b).setPressed(selected || armed);
        }

        super.paint(g, comp);
    }

}
