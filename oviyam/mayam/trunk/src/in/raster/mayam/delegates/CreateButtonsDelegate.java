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
package in.raster.mayam.delegates;

import in.raster.mayam.context.ApplicationContext;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import javax.swing.*;

/**
 *
 * @author Devishree
 * @version 2.0
 */
public class CreateButtonsDelegate {

    JPanel buttonsToolbar;
    ArrayList<String> buttonLabels = new ArrayList<String>();
    JPopupMenu buttonsPopup;
    ActionListener actionListener;

    public CreateButtonsDelegate(JPanel buttonsToolbar, ActionListener queryButtonListener) {
        this.buttonsToolbar = buttonsToolbar;
        this.actionListener = queryButtonListener;
    }

    /*
     * Creates and positions the Quick Search Buttons
     */
    private void createButtons() {
        final int panelWidth = buttonsToolbar.getWidth();
        final int defaultbutonwidth = 120, loopvar;
        final int remaining;
        int intialPos = 120, yPos = 5;
        int totalsize = (buttonLabels.size() * defaultbutonwidth) + 120 + 40; //120 was added to adapt the Appname label width, 40 was added to adapt the More button size        
        if (totalsize > panelWidth - 120) {
            double value = totalsize - panelWidth;
            remaining = (int) Math.ceil(value / defaultbutonwidth);
            loopvar = buttonLabels.size() - remaining;
        } else {
            loopvar = buttonLabels.size();
            intialPos = ((panelWidth - 120 - totalsize) / 2) + 120;
        }
        buttonsToolbar.setLayout(null);
        ApplicationContext.appNameLabel = new JLabel(ApplicationContext.currentBundle.getString("MainScreen.appNameLabel.text"));
        ApplicationContext.appNameLabel.setToolTipText("06/08/2014");
        ApplicationContext.appNameLabel.setFont(new Font("Lucida Grande", Font.BOLD, 16));
        ApplicationContext.appNameLabel.setBounds(10, 5, 120, 30);
        buttonsToolbar.add(ApplicationContext.appNameLabel);

        for (int i = 0; i < loopvar; i++) {
            JButton jb = new JButton(buttonLabels.get(i));
            jb.setBounds(intialPos, yPos, 120, 28);
            intialPos += 120;
            jb.setFocusPainted(false);
            jb.addActionListener(actionListener);
            ApplicationContext.searchButtons.add(jb);
            buttonsToolbar.add(jb);
        }
        if (buttonLabels.size() != loopvar) {
            ApplicationContext.moreButton = new JButton(ApplicationContext.currentBundle.getString("MainScreen.moreButton.text"));
            ApplicationContext.moreButton.setBounds(ApplicationContext.searchButtons.get(loopvar - 1).getX() + 125, 5, panelWidth - (ApplicationContext.searchButtons.get(loopvar - 1).getX() + 125), 30);
            ApplicationContext.moreButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    buttonsPopup = new JPopupMenu();
                    for (int i = loopvar; i < buttonLabels.size(); i++) {
                        JMenuItem menuitem = new JMenuItem(buttonLabels.get(i));
                        buttonsPopup.add(menuitem);
                        menuitem.addActionListener(actionListener);
                    }
                    buttonsPopup.show(buttonsToolbar, panelWidth - 150, 30);
                }
            });
            buttonsToolbar.add(ApplicationContext.moreButton);
            ApplicationContext.searchButtons.add(ApplicationContext.moreButton);
        }
        buttonsToolbar.validate();
        buttonsToolbar.repaint();
    }

    public void loadButtons() {
        buttonsToolbar.removeAll();
        ApplicationContext.searchButtons = new ArrayList<JButton>();
        buttonLabels = ApplicationContext.databaseRef.getAllButtonNames();
        createButtons();
        buttonsToolbar.repaint();
        if (ApplicationContext.isLocal) {
            ApplicationContext.setButtonsDisabled();
        }
    }
}