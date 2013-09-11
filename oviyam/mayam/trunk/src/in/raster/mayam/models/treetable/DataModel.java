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
package in.raster.mayam.models.treetable;

import in.raster.mayam.context.ApplicationContext;

/**
 *
 * @author Devishree
 * @version 2.0
 */
public class DataModel extends AbstractTreeTableModel {

    static protected String[] columnNames = {"", "", "Patient Id", "Patient Name", "DOB", "Accession no", "Study Date", "Study Desc", "Modality", "Images"};
    static protected Class<?>[] columnTypes = {TreeTableModel.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class};

    public DataModel(DataNode rootnode) {
        super(rootnode);
//        root = rootnode;
    }

    @Override
    public Object getChild(Object parent, int index) {
        return ((DataNode) parent).getChildren().get(index);
    }

    @Override
    public int getChildCount(Object parent) {
        return ((DataNode) parent).getChildren().size();
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public String getColumnName(int column) {
        switch (column) {
            case 2:
                return ApplicationContext.currentBundle.getString("MainScreen.patientIdColumn.text");
            case 3:
                return ApplicationContext.currentBundle.getString("MainScreen.patientNameColumn.text");
            case 4:
                return ApplicationContext.currentBundle.getString("MainScreen.dobColumn.text");
            case 5:
                return ApplicationContext.currentBundle.getString("MainScreen.accessionNoColumn.text");
            case 6:
                return ApplicationContext.currentBundle.getString("MainScreen.studyDateColumn.text");
            case 7:
                return ApplicationContext.currentBundle.getString("MainScreen.studyDescColumn.text");
            case 8:
                return ApplicationContext.currentBundle.getString("MainScreen.modalityColumn.text");
            case 9:
                return ApplicationContext.currentBundle.getString("MainScreen.imagesColumn.text");
        }
        return columnNames[column];
    }

    @Override
    public Class<?> getColumnClass(int column) {
        return columnTypes[column];
    }

    @Override
    public Object getValueAt(Object node, int column) {
        if (((DataNode) node).isSeries) {
            switch (column) {
                case 2:
                    return ((DataNode) node).getSeriesNumber();
                case 3:
                    return ((DataNode) node).getSeriesDescription();
                case 4:
                    return ((DataNode) node).getSeriesDate();
                case 5:
                    return ((DataNode) node).getSeriesTime();
                case 6:
                    return ((DataNode) node).getBodyPart();
                case 7:
                    return ((DataNode) node).getModality();
                case 8:
                    return !((DataNode) node).getSeriesRelatedInstances().equals("0") ? ((DataNode) node).getSeriesRelatedInstances() : "";
                case 9:
                    return "";
                case 13:
                    return ((DataNode) node).getHeader();
                case 14:
                    return ((DataNode) node).isSeries;
            }
        } else {
            switch (column) {
                case 1:
                    return "";
                case 2:
                    return ((DataNode) node).getPatientid();
                case 3:
                    return ((DataNode) node).getPatientname();
                case 4:
                    return ((DataNode) node).getDob();
                case 5:
                    return ((DataNode) node).getAccno();
                case 6:
                    return ((DataNode) node).getStudydate();
                case 7:
                    return ((DataNode) node).getStudydesc();
                case 8:
                    return ((DataNode) node).getModalitiesInStudy();
                case 9:
                    return !((DataNode) node).getInstances().equals("0") ? ((DataNode) node).getInstances() : "";
                case 10:
                    return ((DataNode) node).getStudyInstanceUid();
                case 11:
                    return ((DataNode) node).getStudyDetails();
                case 12:
                    return ((DataNode) node).getSeriesList();
                case 13:
                    return ((DataNode) node).getHeader();
                case 14:
                    return ((DataNode) node).isSeries;
            }
        }
        return null;
    }

    @Override
    public boolean isCellEditable(Object node, int column) {
        if (column == 0 && !((DataNode) node).isRoot) {
            return true; // Important to activate TreeExpandListener
        }
        if (((DataNode) node).isRoot && column != 0 && column != 1 && column != 9) {
            return true;
        }
        return false;
    }

    @Override
    public void setValueAt(Object aValue, Object node, int column) {
        switch (column) {
            case 2:
                ((DataNode) node).setPatientid(String.valueOf(aValue));
                break;
            case 3:
                ((DataNode) node).setPatientname(String.valueOf(aValue));
                break;
            case 4:
                ((DataNode) node).setDob(String.valueOf(aValue));
                break;
            case 5:
                ((DataNode) node).setAccno(String.valueOf(aValue));
                break;
            case 6:
                ((DataNode) node).setStudydate(String.valueOf(aValue));
                break;
            case 7:
                ((DataNode) node).setStudydesc(String.valueOf(aValue));
                break;
            case 8:
                ((DataNode) node).setModalitiesInStudy(String.valueOf(aValue));
                break;
            case 9:
                ((DataNode) node).setInstances(String.valueOf(aValue));
                break;
        }
    }
}