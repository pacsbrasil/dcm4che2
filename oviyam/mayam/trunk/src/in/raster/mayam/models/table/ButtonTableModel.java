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
 * ***** END LICENSE BLOCK ***** */package in.raster.mayam.models.table;

import in.raster.mayam.context.ApplicationContext;
import in.raster.mayam.models.ButtonsModel;
import java.util.ArrayList;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author Devishree
 * @version 2.0
 */
public class ButtonTableModel extends AbstractTableModel {

    String columnNames[] = new String[]{"Button Name", "Modality", "Study Date", "Study Time"};
    Class columnTypes[] = new Class[]{String.class, String.class, String.class, String.class};
    ArrayList<ButtonsModel> buttonList = new ArrayList<ButtonsModel>();

    public ButtonTableModel() {
    }

    public void setData(ArrayList<ButtonsModel> rowdata) {
        this.buttonList = rowdata;
    }

    @Override
    public int getRowCount() {
        return buttonList.size();
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public String getColumnName(int column) {
        switch (column) {
            case 0:
                return ApplicationContext.currentBundle.getString("Preferences.queryParameters.buttonNameColumn.text");
            case 1:
                return ApplicationContext.currentBundle.getString("Preferences.queryParameters.modalityColumn.text");
            case 2:
                return ApplicationContext.currentBundle.getString("Preferences.queryParameters.studyDateColumn.text");
            case 3:
                return ApplicationContext.currentBundle.getString("Preferences.queryParameters.studyTimeColumn.text");
        }
        return columnNames[column];
    }

    @Override
    public Class getColumnClass(int c) {
        return columnTypes[c];
    }

    @Override
    public boolean isCellEditable(int r, int c) {
        return false;
    }

    public ButtonsModel getRow(int r) {
        return (ButtonsModel) buttonList.get(r);
    }

    @Override
    public Object getValueAt(int row, int col) {
        if (row < 0 || row >= getRowCount()) {
            return "";
        }
        ButtonsModel button = (ButtonsModel) buttonList.get(row);
        switch (col) {
            case 0:
                return button.getButtonlable();
            case 1:
                return button.getModality();
            case 2:
                return button.getStudyDate();
            case 3:
                return button.getStudyTime();
        }
        return "";
    }
}