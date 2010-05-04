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
package in.raster.mayam.model.table;

import in.raster.mayam.context.ApplicationContext;
import in.raster.mayam.model.PresetModel;
import java.util.ArrayList;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author  BabuHussain
 * @version 0.5
 *
 */
public class PresetTableModel extends AbstractTableModel {

    String columnName[] = {"Preset Name", "Window Level", "Window Width"};
    Class columnType[] = {String.class, String.class, String.class};
    ArrayList presetList;

    public PresetTableModel() {
    }

    public void setData(ArrayList v) {
        presetList = v;
    }

    public int getColumnCount() {
        return columnName.length;
    }

    public int getRowCount() {
        if (presetList != null) {
            return presetList.size();
        } else {
            return 0;
        }
    }

    public ArrayList getStudyList() {
        return presetList;
    }

    public String getValueAt(int nRow, int nCol) {
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

    public String getColumnName(int column) {
        return columnName[column];
    }

    public Class getColumnClass(int c) {
        return columnType[c];
    }

    public boolean isCellEditable(int r, int c) {
        return true;
    }

    public PresetModel getRow(int r) {
        return (PresetModel) presetList.get(r);
    }

    public void setValueAt(Object aValue, int r, int c) {
        PresetModel row = (PresetModel) presetList.get(r);
        switch (c) {
            case 0:
                row.setPresetName(aValue.toString());
                ApplicationContext.databaseRef.updatePresetValues(row);
                break;
            case 1:
                try {
                    int level = Integer.parseInt(aValue.toString());

                    row.setWindowLevel("" + level);
                    ApplicationContext.databaseRef.updatePresetValues(row);
                } catch (Exception e) {
                }
                break;
            case 2:
                try {
                    int width = Integer.parseInt(aValue.toString());
                    row.setWindowWidth("" + width);
                    ApplicationContext.databaseRef.updatePresetValues(row);
                } catch (Exception e) {
                }
                break;
        }
    }
}
