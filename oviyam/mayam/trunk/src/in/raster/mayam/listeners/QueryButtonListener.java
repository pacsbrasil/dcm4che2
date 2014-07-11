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
import in.raster.mayam.models.ButtonsModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import javax.swing.JTabbedPane;

/**
 *
 * @author Devishree
 * @version 2.0
 */
public class QueryButtonListener implements ActionListener {

    JTabbedPane serverTab;
    DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
    DateFormat timeFormat = new SimpleDateFormat("hhmmss");

    public QueryButtonListener(JTabbedPane serverTab) {
        this.serverTab = serverTab;
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        performButtonAction(ae.getActionCommand());
    }

    private void performButtonAction(String buttonLabel) {
        ApplicationContext.mainScreenObj.removeAllPreviewsOfImagePreviewPanel();
        ButtonsModel buttonDetails = ApplicationContext.databaseRef.getButtonDetails(buttonLabel);
        String searchdate = buttonDetails.getStudyDate();
        if (searchdate == null) {
            searchdate = "";
        }
        String searchtime = buttonDetails.getStudyTime();
        if (searchtime == null) {
            searchtime = "";
        }
        int i = 0;
        if (!searchdate.equals("")) {
            if (buttonDetails.getStudyDate().contains("-") && (!buttonDetails.isCustomDate())) {
                Calendar calendar = Calendar.getInstance();
                calendar.add(Calendar.DATE, i - (Integer.parseInt(buttonDetails.getStudyDate().split("-")[1].toString())));
                searchdate = dateFormat.format(calendar.getTime()) + "-" + dateFormat.format(new Date());
            } else if (buttonDetails.isCustomDate()) {
                searchdate = buttonDetails.getStudyDate();
            } else {
                searchdate = dateFormat.format(new Date());
            }
        }
        if (!searchtime.equals("")) {
            i = 0;

            if (searchtime.contains("-") && (!buttonDetails.isCustomTime())) {
                Calendar calendar = Calendar.getInstance();
                if (buttonDetails.getStudyTime().split("-")[0].equalsIgnoreCase("m")) {
                    calendar.add(Calendar.MINUTE, i - (Integer.parseInt(buttonDetails.getStudyTime().split("-")[1].toString())));
                    searchtime = timeFormat.format(calendar.getTime()) + "-" + timeFormat.format(new Date());
                } else {
                    calendar.add(Calendar.HOUR, i - (Integer.parseInt(searchtime.split("-")[1].toString())));
                    searchtime = timeFormat.format(calendar.getTime()) + "-" + timeFormat.format(new Date());
                }
            }
        }
        ApplicationContext.communicationDelegate.setQueryParam(buttonDetails.getModality(), searchdate, searchtime);
        ApplicationContext.communicationDelegate.doQuery(buttonLabel);
    }
}