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
import in.raster.mayam.delegate.AnnotationDelegate;
import javax.swing.*;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.*;
import java.awt.event.*;

/**
 *
 * @author  BabuHussain
 * @version 0.5
 *
 */
public class ButtonTabComp extends JPanel {

    private final JTabbedPane pane;

    public ButtonTabComp(final JTabbedPane pane) {
        this.pane = pane;
        setOpaque(false);
        JLabel label = new JLabel() {

            public String getText() {
                int i = pane.indexOfTabComponent(ButtonTabComp.this);
                if (i != -1) {
                    return pane.getTitleAt(i);
                }
                return null;
            }
        };

        add(label);
        //add more space between the label and the button
        label.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 2));
        label.setVerticalTextPosition(JLabel.TOP);

        JButton button = new TabButton();
        add(button);
        //add more space to the top of the component now it is -1 for mac computer
        setBorder(BorderFactory.createEmptyBorder(-1, 0, 0, 0));
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

        public void actionPerformed(ActionEvent e) {
            int i = pane.indexOfTabComponent(ButtonTabComp.this);
            int x = i;
            if (i != -1) {
                if (i >= 1) {
                    x++;
                }
                //storeAnnotationHook(x);
                // saveAnnotation(x);
                AnnotationDelegate annotationDelegate = new AnnotationDelegate();
                annotationDelegate.storeAnnotationHook(x);
                annotationDelegate.saveAnnotation(x);
                pane.remove(i);
                if (i == 0 && pane.getComponentCount() == 1) {
                    ApplicationContext.imgView.annotationAlreadyStored = true;
                    ApplicationContext.imgView.dispose();
                }
            }
        }
/*
        private void saveAnnotation(int i) {           
                Study studyTobeDelete = null;
                LayeredCanvas tempCanvas = null;
                StudyAnnotation studyAnnotation = new StudyAnnotation();
                for (int x = 0; x < ((JPanel) ApplicationContext.imgView.jTabbedPane1.getComponent(i)).getComponentCount(); x++) {
                    if (((JPanel) ApplicationContext.imgView.jTabbedPane1.getComponent(i)).getComponent(x) instanceof LayeredCanvas) {
                        tempCanvas = ((LayeredCanvas) ((JPanel) ApplicationContext.imgView.jTabbedPane1.getComponent(i)).getComponent(x));
                        File instanceFile = new File(tempCanvas.imgpanel.getDicomFileUrl());
                        String studyDir = instanceFile.getParent();
                        for (Study study : MainScreen.studyList) {                            
                            if (study.getStudyInstanceUID().equalsIgnoreCase(tempCanvas.imgpanel.getStudyUID())) {
                                studyTobeDelete = study;
                                studyAnnotation.setStudyUID(study.getStudyInstanceUID());
                                for (Series series : study.getSeriesList()) {
                                    SeriesAnnotation seriesAnnotation = new SeriesAnnotation();
                                    seriesAnnotation.setSeriesUID(series.getSeriesInstanceUID());
                                    for (Instance instance : series.getImageList()) {
                                        InstanceAnnotation instanceAnnotation = new InstanceAnnotation(instance.getAnnotation());
                                        instanceAnnotation.setInstanceUID(instance.getSop_iuid());
                                        seriesAnnotation.getInstanceArray().put(instance.getSop_iuid(), instanceAnnotation);
                                    }
                                    studyAnnotation.getSeriesAnnotation().put(series.getSeriesInstanceUID(), seriesAnnotation);
                                }
                            }
                        }
                        writeToFile(studyDir, studyAnnotation);
                        RemoveStudy.removeStudyFromStudylist(studyTobeDelete);
                        break;
                    }
                }          
        }

        private void writeToFile(String studyDir, StudyAnnotation studyAnnotation) {
            ObjectOutputStream oos = null;
            try {
                FileOutputStream fos = new FileOutputStream(new File(studyDir, "info.ser"));
                oos = new ObjectOutputStream(fos);
                oos.writeObject(studyAnnotation);
                oos.close();
            } catch (IOException ex) {
                Logger.getLogger(ButtonTabComp.class.getName()).log(Level.SEVERE, null, ex);
            } catch (Exception ex) {
                Logger.getLogger(ButtonTabComp.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                try {
                    oos.close();
                } catch (IOException ex) {
                    Logger.getLogger(ButtonTabComp.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

        public void storeAnnotationHook(int i) {
            for (int j = 0; j < ((JPanel) ApplicationContext.imgView.jTabbedPane1.getComponent(i)).getComponentCount(); j++) {
                try {
                    if (((JPanel) ApplicationContext.imgView.jTabbedPane1.getComponent(i)).getComponent(j) instanceof LayeredCanvas) {
                        LayeredCanvas tempCanvas = ((LayeredCanvas) ((JPanel) ApplicationContext.imgView.jTabbedPane1.getComponent(i)).getComponent(j));
                        if (tempCanvas.imgpanel != null) {
                            tempCanvas.imgpanel.storeAnnotation();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }*/
        //we don't want to update UI for this button
        public void updateUI() {
        }

        //paint the cross
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

        public void mouseEntered(MouseEvent e) {
            Component component = e.getComponent();
            if (component instanceof AbstractButton) {
                AbstractButton button = (AbstractButton) component;
                button.setBorderPainted(true);
            }
        }
        public void mouseExited(MouseEvent e) {
            Component component = e.getComponent();
            if (component instanceof AbstractButton) {
                AbstractButton button = (AbstractButton) component;
                button.setBorderPainted(false);
            }
        }
    };
}