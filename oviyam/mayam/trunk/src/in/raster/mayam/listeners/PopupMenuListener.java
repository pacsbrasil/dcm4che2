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
 * Portions created by the Initial Developer are Copyright (C) 2014
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
package in.raster.mayam.listeners;

import in.raster.mayam.context.ApplicationContext;
import in.raster.mayam.form.ViewerJPanel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import javax.swing.*;

/**
 *
 * @author BabuHussain
 * @version 0.5
 *
 */
public class PopupMenuListener implements ActionListener {

    private String resourcePackage = "/in/raster/mayam/form/images/";

    public PopupMenuListener() {
    }

    public void createPopupMenu(JPopupMenu popup) {
        String state = ((ViewerJPanel) ApplicationContext.tabbedPane.getSelectedComponent()).getTool();

        JCheckBoxMenuItem contrastItem = new JCheckBoxMenuItem(ApplicationContext.currentBundle.getString("ImageView.windowingButton.toolTipText"), getImageIcon("windowing"));
        contrastItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.getKeyText(KeyEvent.VK_W)));
        contrastItem.setSelected(state.equalsIgnoreCase("windowing"));
        contrastItem.addActionListener(this);
        popup.add(contrastItem);

        JCheckBoxMenuItem moveItem = new JCheckBoxMenuItem(ApplicationContext.currentBundle.getString("ImageView.panButton.toolTipText"), getImageIcon("pan"));
        moveItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.getKeyText(KeyEvent.VK_T)));
        moveItem.setSelected(state.equalsIgnoreCase("panning"));
        moveItem.addActionListener(this);
        popup.add(moveItem);

        JCheckBoxMenuItem scrollItem = new JCheckBoxMenuItem(ApplicationContext.currentBundle.getString("ImageView.stackButton.toolTipText"), getImageIcon("stack"));
        scrollItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.getKeyText(KeyEvent.VK_S)));
        scrollItem.setSelected(state.equalsIgnoreCase("stack"));
        scrollItem.addActionListener(this);
        popup.add(scrollItem);

        JMenu menu = new JMenu("ROI");
        popup.add(menu);

        JCheckBoxMenuItem rulerItem = new JCheckBoxMenuItem(ApplicationContext.currentBundle.getString("ImageView.rulerButton.toolTipText"), getImageIcon("ruler"));
        rulerItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.getKeyText(KeyEvent.VK_D)));
        rulerItem.setSelected(state.equalsIgnoreCase("ruler"));
        rulerItem.addActionListener(this);
        menu.add(rulerItem);

        JCheckBoxMenuItem rectangleItem = new JCheckBoxMenuItem(ApplicationContext.currentBundle.getString("ImageView.rectangleButton.toolTipText"), getImageIcon("rectangle"));
        rectangleItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.getKeyText(KeyEvent.VK_R)));
        rectangleItem.setSelected(state.equalsIgnoreCase("rectangle"));
        rectangleItem.addActionListener(this);
        menu.add(rectangleItem);

        JCheckBoxMenuItem ellipseItem = new JCheckBoxMenuItem(ApplicationContext.currentBundle.getString("ImageView.ellipseButton.toolTipText"), getImageIcon("ellipse"));
        ellipseItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.getKeyText(KeyEvent.VK_E)));
        ellipseItem.setSelected(state.equalsIgnoreCase("ellipse"));
        ellipseItem.addActionListener(this);
        menu.add(ellipseItem);

        JCheckBoxMenuItem arrowItem = new JCheckBoxMenuItem(ApplicationContext.currentBundle.getString("ImageView.arrowButton.toolTipText"), getImageIcon("arrow"));
        arrowItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.getKeyText(KeyEvent.VK_A)));
        arrowItem.setSelected(state.equalsIgnoreCase("arrow"));
        arrowItem.addActionListener(this);
        menu.add(arrowItem);

//        if (!ApplicationContext.imgView.imageToolbar.getAnnotationStatus()) {
//            menu.setEnabled(false);
//        }

        if (state.length() == 0) {
            state = "windowing";
        }
//        menu.setIcon(getImageIcon(state));
    }

    public void actionPerformed(ActionEvent e) {
        JMenuItem source = (JMenuItem) (e.getSource());
        String currentProcess = source.getText();
        ((ViewerJPanel) ApplicationContext.tabbedPane.getSelectedComponent()).handleContextSelection(currentProcess);
//        if(currentProcess.equalsIgnoreCase(ApplicationContext.currentBundle.getString("ImageView.windowingButton.toolTipText"))) {
//            ((ViewerJPanel) ApplicationContext.tabbedPane.getSelectedComponent()).doWindowing();
//        } else if(currentProcess.equalsIgnoreCase(ApplicationContext.currentBundle.getString("ImageView.panButton.toolTipText"))) {
//            ((ViewerJPanel) ApplicationContext.tabbedPane.getSelectedComponent()).doPan();
//        } else if(currentProcess.equalsIgnoreCase(ApplicationContext.currentBundle.getString("ImageView.stackButton.toolTipText"))) {
//            ((ViewerJPanel) ApplicationContext.tabbedPane.getSelectedComponent()).doStack();
//        } else if(currentProcess.equalsIgnoreCase(ApplicationContext.currentBundle.getString("ImageView.rulerButton.toolTipText"))) {
//            ((ViewerJPanel) ApplicationContext.tabbedPane.getSelectedComponent()).doRuler(false);
//        } else if(currentProcess.equalsIgnoreCase(ApplicationContext.currentBundle.getString("ImageView.rectangleButton.toolTipText"))) {
//            ((ViewerJPanel) ApplicationContext.tabbedPane.getSelectedComponent()).doRectangle();
//        } else if(currentProcess.equalsIgnoreCase(ApplicationContext.currentBundle.getString("ImageView.ellipseButton.toolTipText"))) {
//            ((ViewerJPanel) ApplicationContext.tabbedPane.getSelectedComponent()).doEllipse();
//        } else if(currentProcess.equalsIgnoreCase(ApplicationContext.currentBundle.getString("ImageView.arrowButton.toolTipText"))) {
//             ((ViewerJPanel) ApplicationContext.tabbedPane.getSelectedComponent()).doRuler(true);
//         } 

//        if (currentProcess.equalsIgnoreCase("Contrast")) {
//            ApplicationContext.imgView.imageToolbar.setWindowingTool();
//        } else if (currentProcess.equalsIgnoreCase("Move")) {
//            ApplicationContext.imgView.imageToolbar.doPan();
//        } else if (currentProcess.equalsIgnoreCase("Scroll")) {
//            ApplicationContext.imgView.imageToolbar.doStack();
//        } else if (currentProcess.equalsIgnoreCase("Ruler")) {
//            ApplicationContext.imgView.imageToolbar.doRuler(false);
//        } else if (currentProcess.equalsIgnoreCase("Rectangle")) {
//            ApplicationContext.imgView.imageToolbar.doRectangle();
//        } else if (currentProcess.equalsIgnoreCase("Ellipse")) {
//            ApplicationContext.imgView.imageToolbar.doEllipse();
//        } else if (currentProcess.equalsIgnoreCase("Arrow")) {
//            ApplicationContext.imgView.imageToolbar.doRuler(true);
//        }
        //selectedAnnotation = currentProcess;
    }

    private ImageIcon getImageIcon(String tool) {
        String iconName = resourcePackage + tool + ".png";
        ImageIcon icon = new ImageIcon(getClass().getResource(iconName));
        return icon;
    }

    public static String getCurrentState() {
        String state = ((ViewerJPanel) ApplicationContext.tabbedPane.getSelectedComponent()).getTool();

//        String tool = ((ViewerJPanel) ApplicationContext.tabbedPane.getSelectedComponent()).getTool();
//        if (tool.equalsIgnoreCase("windowing")) {
//            state = "windowing";
//        } else if (tool.equalsIgnoreCase("panning")) {
//            state = "pan";
//        } else if (tool.equalsIgnoreCase("stack")) {
//            state = "stack";
//        } else if (ApplicationContext.layeredCanvas.annotationPanel.isAddLine()) {
//            state = "ruler";
//        } else if (ApplicationContext.layeredCanvas.annotationPanel.isAddRect()) {
//            state = "rectangle";
//        } else if (ApplicationContext.layeredCanvas.annotationPanel.isAddEllipse()) {
//            state = "ellipse";
//        } else if (ApplicationContext.layeredCanvas.annotationPanel.isAddArrow()) {
//            state = "arrow";
//        }
        return state;
    }
}