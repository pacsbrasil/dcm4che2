/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 *
 * The Initial Developer of the Original Code is
 * Raster Images
 * Portions created by the Initial Developer are Copyright (C) 2009-2010
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Babu Hussain A
 * Devishree V
 * Meer Asgar Hussain B
 * Prakash J
 * Suresh V
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 *
 * ***** END LICENSE BLOCK ***** */
package in.raster.mayam.form.tab.component;

import in.raster.mayam.context.ApplicationContext;
import in.raster.mayam.form.LayeredCanvas;
import in.raster.mayam.form.VideoPanel;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.plaf.basic.BasicButtonUI;

/**
 *
 * @author BabuHussain
 * @version 0.5
 *
 */
public class ButtonTabComp extends JPanel {

    private final JTabbedPane pane;

    public ButtonTabComp(final JTabbedPane pane) {
        this.pane = pane;
        setOpaque(false);
        JLabel label = new JLabel() {
            @Override
            public String getText() {
                int i = pane.indexOfTabComponent(ButtonTabComp.this);
                if (i != -1) {
                    return pane.getTitleAt(i);
                }
                return null;
            }
        };

        label.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        add(label);
        //add more space between the label and the button
        label.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 2));
        label.setVerticalTextPosition(JLabel.TOP);

        JButton button = new TabButton();
        add(button);
        //add more space to the top of the component now it is -1 for mac computer
        setBorder(BorderFactory.createEmptyBorder(-1, 0, 0, 0));
    }

    public Component getTabbedComponent() {
        return pane;
    }

    private class TabButton extends JButton implements ActionListener {

        public TabButton() {
            int size = 17;
            setPreferredSize(new Dimension(size, size));
            setToolTipText("close this tab");
            //Make the button looks the same for all Laf's
            setUI(new BasicButtonUI());
            //Make it transparent
            setContentAreaFilled(false);
            //No need to be focusable
            setFocusable(false);
            setBorder(BorderFactory.createEtchedBorder());
            setBorderPainted(false);
            //Making nice rollover effect
            //we use the same listener for all buttons
            addMouseListener(buttonMouseListener);
            setRolloverEnabled(true);
            //Close the proper tab by clicking the button
            addActionListener(this);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            int i = pane.indexOfTabComponent(ButtonTabComp.this);
            int x = i;
            if (i != -1) {
                JPanel panel = ((JPanel) ((JSplitPane) ApplicationContext.tabbedPane.getComponentAt(i)).getRightComponent());
                if (!(panel instanceof VideoPanel)) {
                    for (int j = 0; j < panel.getComponentCount(); j++) {
                        JPanel seriesLevelPanel = (JPanel) panel.getComponent(j);
                        for (int k = 0; k < seriesLevelPanel.getComponentCount(); k++) {
                            if (seriesLevelPanel.getComponent(k) instanceof LayeredCanvas) {
                                LayeredCanvas tempCanvas = (LayeredCanvas) seriesLevelPanel.getComponent(k);
                                try {
                                    tempCanvas.imgpanel.storeAnnotation();
                                    tempCanvas.imgpanel.storeMultiframeAnnotation();
                                } catch (NullPointerException ex) {
                                    //Null pointer exception occurs when there is no image panel
                                }
                            }
                        }
                    }
                }

                for (int j = 0; j < ApplicationContext.imgView.selectedSeriesDisplays.size(); j++) {
                    if (ApplicationContext.imgView.selectedSeriesDisplays.get(j).getStudyUid().equals(panel.getComponent(0).getName())) {
                        ApplicationContext.imgView.writeToFile(ApplicationContext.imgView.selectedSeriesDisplays.get(j));
                        ApplicationContext.imgView.selectedSeriesDisplays.remove(j);
                    }
                }

                pane.remove(i);
                if (i == 0 && pane.getComponentCount() == 1) {
                    ApplicationContext.imgView.dispose();
                }
            }
        }
        //we don't want to update UI for this button

        @Override
        public void updateUI() {
        }

        //paint the cross
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            //shift the image for pressed buttons
            if (getModel().isPressed()) {
                g2.translate(1, 1);
            }
            g2.setStroke(new BasicStroke(2));
            g2.setColor(Color.BLACK);
            if (getModel().isRollover()) {
                g2.setColor(Color.MAGENTA);
            }
            int delta = 6;
            g2.drawLine(delta, delta, getWidth() - delta - 1, getHeight() - delta - 1);
            g2.drawLine(getWidth() - delta - 1, delta, delta, getHeight() - delta - 1);
            g2.dispose();
        }
    }
    private final static MouseListener buttonMouseListener = new MouseAdapter() {
        @Override
        public void mouseEntered(MouseEvent e) {
            Component component = e.getComponent();
            if (component instanceof AbstractButton) {
                AbstractButton button = (AbstractButton) component;
                button.setBorderPainted(true);
            }
        }

        @Override
        public void mouseExited(MouseEvent e) {
            Component component = e.getComponent();
            if (component instanceof AbstractButton) {
                AbstractButton button = (AbstractButton) component;
                button.setBorderPainted(false);
            }
        }
    };
}
