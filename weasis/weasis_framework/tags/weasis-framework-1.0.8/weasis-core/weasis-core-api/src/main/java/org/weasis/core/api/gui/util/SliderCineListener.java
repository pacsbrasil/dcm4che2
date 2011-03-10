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

import java.awt.Color;

import javax.swing.JPanel;
import javax.swing.SpinnerNumberModel;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.weasis.core.api.Messages;

public abstract class SliderCineListener extends SliderChangeListener {
    public enum TIME {
        second, minute, hour
    };

    private final TIME time;
    private final SpinnerNumberModel speedModel;

    public SliderCineListener(ActionW action, int min, int max, int value, int speed, TIME time, double mouseSensivity) {
        this(action, min, max, value, speed, time);
        setMouseSensivity(mouseSensivity);
    }

    public SliderCineListener(ActionW action, int min, int max, int value, int speed, TIME time) {
        super(action, min, max, value);
        this.time = time;
        speedModel = new SpinnerNumberModel(speed, 1, 60, 1);
        speedModel.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                setSpeed((Integer) ((SpinnerNumberModel) e.getSource()).getValue());
            }
        });
    }

    public abstract void start();

    public abstract void stop();

    public int getSpeed() {
        return (Integer) speedModel.getValue();
    }

    @Override
    public void updateSliderProoperties(JSliderW slider) {
        JPanel panel = (JPanel) slider.getParent();
        int rate = getCurrentCineRate();
        StringBuffer buffer = new StringBuffer(Messages.getString("SliderCineListener.img")); //$NON-NLS-1$
        buffer.append(getValueToDisplay());

        if (!slider.isDisplayOnlyValue() && panel != null && panel.getBorder() instanceof TitledBorder) {
            if (rate > 0) {
                buffer.append(Messages.getString("SliderCineListener.cine")); //$NON-NLS-1$
                buffer.append(rate);
                if (TIME.second.equals(time)) {
                    buffer.append(Messages.getString("SliderCineListener.fps")); //$NON-NLS-1$
                } else if (TIME.minute.equals(time)) {
                    buffer.append(Messages.getString("SliderCineListener.fpm")); //$NON-NLS-1$
                } else if (TIME.hour.equals(time)) {
                    buffer.append(Messages.getString("SliderCineListener.fph")); //$NON-NLS-1$
                }
            }
            ((TitledBorder) panel.getBorder()).setTitleColor(rate > 0 && rate < (getSpeed() - 1) ? Color.red
                : UIManager.getColor("TitledBorder.titleColor")); //$NON-NLS-1$
            ((TitledBorder) panel.getBorder()).setTitle(buffer.toString());
            panel.repaint();
        } else {
            slider.setToolTipText(buffer.toString());
        }
    }

    public int getCurrentCineRate() {
        return 0;
    }

    public void setSpeed(int speed) {
        speedModel.setValue(speed);
    }

    public SpinnerNumberModel getSpeedModel() {
        return speedModel;
    }

}
