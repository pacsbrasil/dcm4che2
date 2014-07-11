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
import in.raster.mayam.form.dialogs.WadoInformation;
import in.raster.mayam.models.ServerModel;
import java.util.ArrayList;
import java.util.logging.Level;
import javax.swing.JOptionPane;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author BabuHussain
 * @version 0.6
 *
 */
public class ServerTableModel extends AbstractTableModel {

    String columnNames[] = new String[]{"Server Name", "AE Title", "Host Name", "Port", "Retrieve", "Previews"};
    Class columnTypes[] = new Class[]{String.class, String.class, String.class, String.class, String.class, Boolean.class};
    ArrayList<ServerModel> serverList = new ArrayList<ServerModel>();
    boolean editable = true;

    public void setData(ArrayList<ServerModel> rowdata) {
        this.serverList = rowdata;
    }

    @Override
    public int getRowCount() {
        if (serverList != null) {
            return serverList.size();
        } else {
            return 0;
        }
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public String getColumnName(int column) {
        switch (column) {
            case 0:
                return ApplicationContext.currentBundle.getString("Preferences.servers.serverNameColumn.text");
            case 1:
                return ApplicationContext.currentBundle.getString("Preferences.servers.aeTitleColumn.text");
            case 2:
                return ApplicationContext.currentBundle.getString("Preferences.servers.hostnameColumn.text");
            case 3:
                return ApplicationContext.currentBundle.getString("Preferences.servers.portColumn.text");
            case 4:
                return ApplicationContext.currentBundle.getString("Preferences.servers.retrieveColumn.text");
            case 5:
                return ApplicationContext.currentBundle.getString("Preferences.servers.previewsColumn.text");
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

    public ServerModel getRow(int r) {
        return (ServerModel) serverList.get(r);
    }

    @Override
    public Object getValueAt(int row, int col) {
        if (row < 0 || row >= getRowCount()) {
            return "";
        }
        ServerModel server = (ServerModel) serverList.get(row);
        switch (col) {
            case 0:
                return server.getDescription();
            case 1:
                return server.getAeTitle();
            case 2:
                return server.getHostName();
            case 3:
                return Integer.toString(server.getPort());
            case 4:
                if (server.getRetrieveType().equalsIgnoreCase("WADO")) {
                    return server.getRetrieveType() + " - " + server.getWadoPort();
                } else {
                    return server.getRetrieveType();
                }
            case 5:
                return server.isPreviewEnabled();
        }
        return "";
    }

    @Override
    public void setValueAt(Object aValue, int r, int c) {
        ServerModel row = serverList.get(r);
        switch (c) {
            case 0:
                String prevName = row.getDescription();
                row.setDescription(aValue.toString());
                boolean isDuplicate = ApplicationContext.databaseRef.updateServer(row);
                if (!isDuplicate && !"Description".equals(row.getDescription())) {
                    ApplicationContext.mainScreenObj.addOrEditServer(prevName, row.getDescription());
                } else {
                    row.setDescription(prevName);
                    JOptionPane.showOptionDialog(null, "'" + aValue + "' " + ApplicationContext.currentBundle.getString("SettingsForm.addServerFailiure.text"), ApplicationContext.currentBundle.getString("ErrorTitles.text"), JOptionPane.OK_OPTION, JOptionPane.ERROR_MESSAGE, null, new String[]{ApplicationContext.currentBundle.getString("OkButtons.text")}, "default");
                }
                break;
            case 1:
                row.setAeTitle(aValue.toString());
                ApplicationContext.databaseRef.updateServer(row);
                break;
            case 2:
                row.setHostName(aValue.toString());
                ApplicationContext.databaseRef.updateServer(row);
                break;
            case 3:
                try {
                    row.setPort(Integer.parseInt(aValue.toString()));
                    ApplicationContext.databaseRef.updateServer(row);
                } catch (Exception e) {
                    ApplicationContext.logger.log(Level.INFO, "ServerTableModel - Not a valid port", e);
                }
                break;
            case 4:
                try {
                    row.setRetrieveType(aValue.toString());
                    if (row.getRetrieveType().equalsIgnoreCase("WADO")) {
                        WadoInformation wadoInformation = new WadoInformation(null, true, row);
                        wadoInformation.setLocationRelativeTo(ApplicationContext.mainScreenObj);
                        wadoInformation.setVisible(true);
                    }
                    ApplicationContext.databaseRef.updateServer(row);
                } catch (Exception e) {
                    ApplicationContext.logger.log(Level.INFO, "ServerTableModel - Unable to update retrieve type information", e);
                }
                break;
            case 5:
                row.setPreviewEnabled((Boolean) aValue);
                WadoInformation wadoInformation = null;
                if (row.isPreviewEnabled() && !row.getRetrieveType().equalsIgnoreCase("WADO")) {
                    wadoInformation = new WadoInformation(null, true, row);
                    wadoInformation.setLocationRelativeTo(ApplicationContext.mainScreenObj);
                    wadoInformation.setVisible(true);
                } else if (!row.getRetrieveType().equalsIgnoreCase("WADO") && !row.isPreviewEnabled()) {
                    row.setWadoContextPath("wado");
                    row.setWadoPort(8080);
                    row.setWadoProtocol("http");
                    row.setRetrieveTransferSyntax("Explicit VR Little Endian");
                }
                if (wadoInformation != null && wadoInformation.getReturnStatus() == 0) {
                    row.setPreviewEnabled(false);
                }
                ApplicationContext.databaseRef.updateServer(row);
                ApplicationContext.mainScreenObj.removeTab(row.getDescription());
                if (!row.getDescription().equalsIgnoreCase("description")) {
                    ApplicationContext.mainScreenObj.addOrEditServer("Description", row.getDescription());
                }
                break;
        }
    }
}