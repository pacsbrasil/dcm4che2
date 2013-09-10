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
package in.raster.mayam.listeners;

import in.raster.mayam.context.ApplicationContext;
import in.raster.mayam.form.SearchFilterForm;
import in.raster.mayam.models.QueryInformation;
import in.raster.mayam.models.treetable.TreeTable;
import java.util.ArrayList;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JViewport;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 *
 * @author Devishree
 * @version 2.0
 */
public class ServerTabChangeListener implements ChangeListener {

    JTabbedPane serverTab;
    ArrayList<QueryInformation> queryInfo = null;
    boolean serverReached = false;

    public ServerTabChangeListener(JTabbedPane serverTab) {
        this.serverTab = serverTab;
        ApplicationContext.setButtonsDisabled();
    }

    @Override
    public void stateChanged(ChangeEvent ce) {
        ApplicationContext.mainScreenObj.setStudiesFound("");
        if (serverTab.getSelectedIndex() == 0) {
            ApplicationContext.isLocal = true;
            ApplicationContext.setButtonsDisabled();
            ApplicationContext.currentTreeTable = ((TreeTable) ((JViewport) ((JScrollPane) ((JSplitPane) serverTab.getSelectedComponent()).getRightComponent()).getComponent(0)).getComponent(0));
            ApplicationContext.mainScreenObj.refreshLocalDB();
            if (ApplicationContext.mainScreenObj.getCurrentProgressValue() == 0 || ApplicationContext.mainScreenObj.getProgressText().equalsIgnoreCase("Downloading")) {
                ApplicationContext.mainScreenObj.setProgressbarVisibility(false);
            } else {
                ApplicationContext.mainScreenObj.setProgressbarVisibility(true);
            }
        } else {
            ApplicationContext.currentQueryUrl = ApplicationContext.communicationDelegate.constructURL(serverTab.getTitleAt(serverTab.getSelectedIndex()));
            ApplicationContext.currentServer = serverTab.getTitleAt(serverTab.getSelectedIndex());
            ApplicationContext.isLocal = false;
            if (((JSplitPane) serverTab.getSelectedComponent()).getRightComponent() instanceof JScrollPane) {
                ApplicationContext.currentTreeTable = ((TreeTable) ((JViewport) ((JScrollPane) ((JSplitPane) serverTab.getSelectedComponent()).getRightComponent()).getComponent(0)).getComponent(0));
            } else {
                ApplicationContext.currentTreeTable = ((TreeTable) ((JViewport) ((JScrollPane) ((JSplitPane) ((JSplitPane) serverTab.getSelectedComponent()).getRightComponent()).getRightComponent()).getComponent(0)).getComponent(0));
            }
            if (ApplicationContext.mainScreenObj.getCurrentProgressValue() == 0) {
                ApplicationContext.mainScreenObj.setProgressbarVisibility(false);
            } else {
                ApplicationContext.mainScreenObj.setProgressbarVisibility(true);
            }
            for (int i = 0; i < ApplicationContext.searchButtons.size(); i++) {
                ApplicationContext.searchButtons.get(i).setFocusPainted(false);
                ApplicationContext.searchButtons.get(i).setVisible(true);

                if (ApplicationContext.currentTreeTable.getRowCount() != 0) {
                    for (int j = 0; j < ApplicationContext.queryInformations.size(); j++) {
                        if (ApplicationContext.queryInformations.get(j).getSelectedButton().equals(ApplicationContext.searchButtons.get(i).getText()) && ApplicationContext.queryInformations.get(j).getServerName().equalsIgnoreCase(serverTab.getTitleAt(serverTab.getSelectedIndex()))) {
                            ApplicationContext.searchButtons.get(i).setFocusPainted(true);
                            ApplicationContext.searchButtons.get(i).requestFocus();
                            if (ApplicationContext.queryInformations.get(j).getTotalStudiesFound() != 0) {
                                ApplicationContext.mainScreenObj.setStudiesFound(ApplicationContext.currentBundle.getString("MainScreen.studiesFoundLabel.text") + ApplicationContext.queryInformations.get(j).getTotalStudiesFound());
                            } else {
                                ApplicationContext.mainScreenObj.setStudiesFound("");
                            }
                            break;
                        } else if (ApplicationContext.queryInformations.get(j).getServerName().equalsIgnoreCase(serverTab.getTitleAt(serverTab.getSelectedIndex()))) {
                            if (ApplicationContext.queryInformations.get(j).getTotalStudiesFound() != 0) {
                                ApplicationContext.mainScreenObj.setStudiesFound(ApplicationContext.currentBundle.getString("MainScreen.studiesFoundLabel.text") + ApplicationContext.queryInformations.get(j).getTotalStudiesFound());
                            } else {
                                ApplicationContext.mainScreenObj.setStudiesFound("");
                            }
                            break;
                        }
                    }
                }
            }
        }
    }
}
