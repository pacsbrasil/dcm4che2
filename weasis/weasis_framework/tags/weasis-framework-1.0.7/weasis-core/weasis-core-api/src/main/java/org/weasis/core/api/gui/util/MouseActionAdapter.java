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
package org.weasis.core.api.gui.util;

import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

public abstract class MouseActionAdapter implements MouseListener, MouseWheelListener, MouseMotionListener {

    // Define in java.awt.event.InputEvent, could change if extra modifier bits are added
    static final int JDK_1_3_MODIFIERS = (1 << 6) - 1;
    static final int HIGH_MODIFIERS = ~((1 << 14) - 1);

    private int buttonMaskEx = 0;
    protected int lastPosition = 0;
    private boolean inverse = false;
    private boolean moveOnX = false;
    private double mouseSensivity = 1.0;
    protected double dragAccumulator = Double.MAX_VALUE;

    public void mouseClicked(MouseEvent e) {
    }

    public void mousePressed(MouseEvent e) {
    }

    public void mouseReleased(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mouseWheelMoved(MouseWheelEvent e) {
    }

    public void mouseDragged(MouseEvent e) {
    }

    public void mouseMoved(MouseEvent e) {
    }

    public double getMouseSensivity() {
        return mouseSensivity;
    }

    public void setMouseSensivity(double mouseSensivity) {
        this.mouseSensivity = mouseSensivity;
    }

    public int getButtonMaskEx() {
        return buttonMaskEx;
    }

    public int getButtonMask() {
        int buttonMask = (buttonMaskEx & InputEvent.BUTTON1_DOWN_MASK) != 0 ? InputEvent.BUTTON1_MASK : 0;
        if ((buttonMaskEx & InputEvent.BUTTON2_DOWN_MASK) != 0) {
            buttonMask |= InputEvent.BUTTON2_MASK;
        }
        if ((buttonMaskEx & InputEvent.BUTTON3_DOWN_MASK) != 0) {
            buttonMask |= InputEvent.BUTTON3_MASK;
        }
        return buttonMask;
    }

    public void setButtonMaskEx(int buttonMask) {
        this.buttonMaskEx = buttonMask;
        // Zero is used to disable the mouse adapter
        if (buttonMask == 0) {
            // assign an invalid value to the accumulator.
            dragAccumulator = Double.MAX_VALUE;
        }
    }

    public boolean isMoveOnX() {
        return moveOnX;
    }

    public void setMoveOnX(boolean moveOnX) {
        this.moveOnX = moveOnX;
    }

    public boolean isInverse() {
        return inverse;
    }

    public void setInverse(boolean inverse) {
        this.inverse = inverse;
    }

}
