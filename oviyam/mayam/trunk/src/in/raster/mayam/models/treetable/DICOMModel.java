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
package in.raster.mayam.models.treetable;

import in.raster.mayam.context.ApplicationContext;

/**
 *
 * @author Devishree
 * @version 2.1
 */
public class DICOMModel extends AbstractTreeTableModel {

    static protected String[] columnNames = {"", "", "Patient Id", "Patient Name", "DOB", "Acc no", "Study Date", "Study Desc", "Modality", "Images"};
    static protected Class<?>[] columnTypes = {TreeTableModel.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class};

    public DICOMModel(Object rootnode) {
        super(rootnode);
    }

    @Override
    public Object getChild(Object parent, int index) {
        return parent instanceof StudyNode ? ((StudyNode) parent).getChild(index) : null;
    }

    @Override
    public int getChildCount(Object parent) {
        return parent instanceof StudyNode ? ((StudyNode) parent).getChildCount() : 0;
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
        if (node instanceof SeriesNode) {
            switch (column) {
                case 2:
                    return ((SeriesNode) node).getSeriesNo();
                case 3:
                    return ((SeriesNode) node).getSeriesDesc();
                case 4:
                    return ((SeriesNode) node).getSeriesDate();
                case 5:
                    return ((SeriesNode) node).getSeriesTime();
                case 6:
                    return ((SeriesNode) node).getBodyPartExamined();
                case 7:
                    return ((SeriesNode) node).getModality();
                case 8:
                    return ((SeriesNode) node).getSeriesRelatedInstance();
                case 9:
                    return "";
                case 13:
                    return ((SeriesNode) node).isHeader;
            }
        } else {
            switch (column) {
                case 1:
                    return "";
                case 2:
                    return ((StudyNode) node).getPatientId();
                case 3:
                    return ((StudyNode) node).getPatientName();
                case 4:
                    return ((StudyNode) node).getDob();
                case 5:
                    return ((StudyNode) node).getAccessionNo();
                case 6:
                    return ((StudyNode) node).getStudyDate();
                case 7:
                    return ((StudyNode) node).getStudyDescription();
                case 8:
                    return ((StudyNode) node).getModalitiesInStudy();
                case 9:
                    return ((StudyNode) node).getStudyReleatedInstances();
                case 10:
                    return ((StudyNode) node).getStudyUID();
                case 11:
                    return ((StudyNode) node).getStudyTime();
                case 12:
                    return ((StudyNode) node);
                case 13:
                    return false;
            }
        }
        return null;
    }

    @Override
    public boolean isCellEditable(Object node, int column) {
        if (node instanceof StudyNode) {
            switch (((StudyNode) node).isRoot()) {
                case 1: //Important to activate tree expand listener
                    if (column == 0) {
                        return true;
                    }
                    break;
                case 0:
                    if (column != 0 && column != -1 && column != 9) {
                        return true;
                    }
                    break;
            }
        }
        return false;
    }

    @Override
    public void setValueAt(Object aValue, Object node, int column) {
        switch (column) {
            case 2:
                ((StudyNode) node).setPatientId(String.valueOf(aValue));
                break;
            case 3:
                ((StudyNode) node).setPatientName(String.valueOf(aValue));
                break;
            case 4:
                ((StudyNode) node).setDob(String.valueOf(aValue));
                break;
            case 5:
                ((StudyNode) node).setAccessionNo(String.valueOf(aValue));
                break;
            case 6:
                ((StudyNode) node).setStudyDate(String.valueOf(aValue));
                break;
            case 7:
                ((StudyNode) node).setStudyDescription(String.valueOf(aValue));
                break;
            case 8:
                ((StudyNode) node).setModalitiesInStudy(String.valueOf(aValue));
                break;
            case 9:
                ((StudyNode) node).setStudyReleatedInstances(String.valueOf(aValue));
                break;
        }
    }
}