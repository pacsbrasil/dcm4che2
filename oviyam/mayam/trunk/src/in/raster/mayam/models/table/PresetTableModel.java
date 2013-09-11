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
package in.raster.mayam.models.table;

import in.raster.mayam.context.ApplicationContext;
import in.raster.mayam.models.PresetModel;
import java.util.ArrayList;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author Devishree
 * @version 2.0
 */
public class PresetTableModel extends AbstractTableModel {

    String[] columnNames = {"Preset Name", "Window Level", "Window Width"};
    Class[] columnTypes = {String.class, String.class, String.class};
    ArrayList presetList;

    public PresetTableModel() {
    }

    public void setData(ArrayList presets) {
        presetList = presets;
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public int getRowCount() {
        if (presetList != null) {
            return presetList.size();
        } else {
            return 0;
        }
    }

    @Override
    public Object getValueAt(int nRow, int nCol) {
        if (nRow < 0 || nRow >= getRowCount()) {
            return "";
        }
        PresetModel row = (PresetModel) presetList.get(nRow);
        switch (nCol) {
            case 0:
                return row.getPresetName();
            case 1:
                return row.getWindowLevel();
            case 2:
                return row.getWindowWidth();

        }
        return "";
    }

    @Override
    public String getColumnName(int column) {
        switch (column) {
            case 0:
                return ApplicationContext.currentBundle.getString("Preferences.presets.presetNameColumn.text");
            case 1:
                return ApplicationContext.currentBundle.getString("Preferences.presets.windowLevelColumn.text");
            case 2:
                return ApplicationContext.currentBundle.getString("Preferences.presets.windowWidthColumn.text");
        }
        return columnNames[column];
    }

    @Override
    public Class getColumnClass(int c) {
        return columnTypes[c];
    }

    @Override
    public boolean isCellEditable(int r, int c) {
        return true;
    }

    public PresetModel getRow(int r) {
        return (PresetModel) presetList.get(r);
    }

    @Override
    public void setValueAt(Object aValue, int r, int c) {
        PresetModel row = (PresetModel) presetList.get(r);
        switch (c) {
            case 0:
                row.setPresetName((String) aValue);
                ApplicationContext.databaseRef.updatePreset(row);
                break;
            case 1:
                try {
                    int level = Integer.parseInt((String) aValue);
                    row.setWindowLevel(Integer.toString(level));
                    ApplicationContext.databaseRef.updatePreset(row);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case 2:
                try {
                    int width = Integer.parseInt((String) aValue);
                    row.setWindowWidth(Integer.toString(width));
                    ApplicationContext.databaseRef.updatePreset(row);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
        }
    }
}
