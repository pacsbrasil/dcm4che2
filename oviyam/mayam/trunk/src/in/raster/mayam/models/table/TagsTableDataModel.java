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
package in.raster.mayam.models.table;

import in.raster.mayam.context.ApplicationContext;
import in.raster.mayam.dicomtags.DicomTags;
import java.util.ArrayList;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author BabuHussain
 * @version 0.5
 *
 */
public class TagsTableDataModel extends AbstractTableModel {

    String columnName[] = {"Tag", "Name", "VR", "Length", "VM", "Value"};
    Class columnType[] = {String.class, String.class, String.class, String.class, String.class, String.class};
    ArrayList tagsList;

    public TagsTableDataModel() {
    }

    public void setData(ArrayList v) {
        tagsList = v;
    }

    public ArrayList getTagsList() {
        return tagsList;
    }

    @Override
    public int getRowCount() {
        if (tagsList != null) {
            return tagsList.size();
        } else {
            return 0;
        }

    }

    @Override
    public int getColumnCount() {
        return columnName.length;
    }

    @Override
    public String getValueAt(int rowIndex, int columnIndex) {

        if (rowIndex < 0 || rowIndex >= getRowCount()) {
            return "";
        }
        DicomTags row = (DicomTags) tagsList.get(rowIndex);

        switch (columnIndex) {
            case 0:
                return row.getTag();

            case 1:
                return row.getTagName();

            case 2:
                return row.getVR();

            case 3:
                return row.getTagLength();

            case 4:
                return row.getVM();

            case 5:
                return row.getTagValue();

        }

        return "";
    }

    @Override
    public String getColumnName(int column) {
        switch (column) {
            case 0:
                return ApplicationContext.currentBundle.getString("MetaData.tagColumn.text");
            case 1:
                return ApplicationContext.currentBundle.getString("MetaData.nameColumn.text");
            case 2:
                return ApplicationContext.currentBundle.getString("MetaData.vrColumn.text");
            case 3:
                return ApplicationContext.currentBundle.getString("MetaData.lengthcolumn.text");
            case 4:
                return ApplicationContext.currentBundle.getString("MetaData.vmColumn.text");
            case 5:
                return ApplicationContext.currentBundle.getString("MetaData.valueColumn.text");
        }
        return columnName[column];
    }
}