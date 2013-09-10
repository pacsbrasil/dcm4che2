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
import in.raster.mayam.form.ServerChangeListener;
import in.raster.mayam.form.dialog.WadoInformation;
import in.raster.mayam.model.ServerModel;
import java.util.ArrayList;
import javax.swing.table.AbstractTableModel;
import org.dcm4che2.data.TransferSyntax;

/**
 *
 * @author  BabuHussain
 * @version 0.5
 *
 */
public class ServerTableModel extends AbstractTableModel {

    private boolean editable = true;
    String columnName[] = {"Description", "AE Title", "Host Name", "Port", "Retrieve Type"};
    Class columnType[] = {String.class, String.class, String.class, String.class, String.class};
    ArrayList serverList;
    ServerChangeListener listener = null;

    public ServerTableModel() {
    }

    public void setData(ArrayList v) {
        serverList = v;
    }

    public int getColumnCount() {
        return columnName.length;
    }

    public int getRowCount() {
        if (serverList != null) {
            return serverList.size();
        } else {
            return 0;
        }
    }

    public ArrayList getStudyList() {
        return serverList;
    }

    public String getValueAt(int nRow, int nCol) {
        if (nRow < 0 || nRow >= getRowCount()) {
            return "";
        }
        ServerModel row = (ServerModel) serverList.get(nRow);
        switch (nCol) {
            case 0:
                return row.getServerName();
            case 1:
                return row.getAeTitle();
            case 2:
                return row.getHostName();
            case 3:
                return Integer.toString(row.getPort());
            case 4:
                return row.getRetrieveType();
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
        return editable;
    }

    public ServerModel getRow(int r) {
        return (ServerModel) serverList.get(r);
    }

    public void setValueAt(Object aValue, int r, int c) {
        ServerModel row = (ServerModel) serverList.get(r);
        switch (c) {
            case 0:
                row.setServerName(aValue.toString());
                ApplicationContext.databaseRef.updateServerListValues(row);
                break;
            case 1:
                try {
                    row.setAeTitle(aValue.toString());
                    ApplicationContext.databaseRef.updateServerListValues(row);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case 2:
                try {
                    row.setHostName(aValue.toString());
                    ApplicationContext.databaseRef.updateServerListValues(row);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case 3:
                try {
                    int port = Integer.parseInt(aValue.toString());
                    row.setPort(port);
                    ApplicationContext.databaseRef.updateServerListValues(row);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case 4:
                try {
                    row.setRetrieveType(aValue.toString());
                    if (row.getRetrieveType().equalsIgnoreCase("WADO")) {
                        WadoInformation wadoInformation = new WadoInformation(null, true, row);
                        wadoInformation.setLocationRelativeTo(ApplicationContext.mainScreen);
                        wadoInformation.setVisible(true);
                    } else {
                        row.setWadoContextPath("wado");
                        row.setWadoPort(0);
                        row.setWadoProtocol("http");
                        row.setRetrieveTransferSyntax("Explicit VR Little Endian");
                    }
                    ApplicationContext.databaseRef.updateServerListValues(row);
                } catch (Exception e) {
                    e.printStackTrace();
                }

        }
        if (listener != null) {
            listener.onServerChange();
        }


    }

    public boolean isEditable() {
        return editable;
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
    }

    public void addChangeListener(ServerChangeListener listener) {
        this.listener = listener;
    }

    public ServerChangeListener getListener() {
        return this.listener;
    }
}
