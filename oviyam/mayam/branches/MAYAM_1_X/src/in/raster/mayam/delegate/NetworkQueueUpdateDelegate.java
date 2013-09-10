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
package in.raster.mayam.delegate;

import in.raster.mayam.context.ApplicationContext;
import in.raster.mayam.form.MainScreen;
import in.raster.mayam.form.SendReceivePanel;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import org.dcm4che2.data.BasicDicomObject;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.io.DicomInputStream;

/**
 *
 * @author  BabuHussain
 * @version 0.5
 *
 */
public class NetworkQueueUpdateDelegate {

    public DefaultTableModel receiveTableModel;
    public DefaultTableModel sendTableModel;
    private boolean rowAlreadyPresent;

    public NetworkQueueUpdateDelegate() {
        receiveTableModel = SendReceivePanel.receiveTableModel;
        sendTableModel = SendReceivePanel.sendTableModel;
    }

    /**
     * This routine used to update the receiver table in the network queue.
     * @param fileObj
     * @param calledAET
     */
    public void updateReceiveTable(File fileObj, String calledAET) {
        DicomInputStream dis = null;
        try {
            File parseFile = fileObj;
            try {
                dis = new DicomInputStream(parseFile);
            } catch (IOException ex) {
                Logger.getLogger(NetworkQueueUpdateDelegate.class.getName()).log(Level.SEVERE, null, ex);
            }
            DicomObject data = new BasicDicomObject();
            try {
                data = dis.readDicomObject();
            } catch (EOFException e) {
                e.printStackTrace();
            }
            if (data != null) {
                ApplicationContext.databaseRef.writeDataToDatabase(data);
                rowAlreadyPresent = false;
                if (SendReceivePanel.receiveTableModel.getRowCount() > 0) {
                    for (int i = 0; i < SendReceivePanel.receiveTableModel.getRowCount(); i++) {
                        if (SendReceivePanel.receiveTableModel.getValueAt(i, 3).toString().equalsIgnoreCase(data.getString(Tag.StudyDate)) && SendReceivePanel.receiveTableModel.getValueAt(i, 1).toString().equalsIgnoreCase(data.getString(Tag.PatientID).toString())) {
                            DateFormat df = new SimpleDateFormat("kk:mm:ss");
                            Date d = new Date();
                            int receivedCount = ApplicationContext.databaseRef.getReceiveCount(data.getString(Tag.StudyInstanceUID));
                            //receivedCount = receivedCount;
                            SendReceivePanel.receiveTableModel.setValueAt(df.format(d), i, 8);
                            SendReceivePanel.receiveTableModel.setValueAt(String.valueOf(receivedCount), i, 5);
                            SendReceivePanel.receiveTableModel.setValueAt(String.valueOf(receivedCount), i, 6);
                            rowAlreadyPresent = true;
                        }
                    }
                }
                if (!rowAlreadyPresent) {
                    SwingUtilities.invokeLater(new Runnable() {

                        public void run() {
                            MainScreen.refreshLocalDBStorage();
                        }
                    });
                    SendReceivePanel.receiveTableModel.insertRow(SendReceivePanel.receiveTableModel.getRowCount(), new Object[]{calledAET, data.getString(Tag.PatientID), data.getString(Tag.PatientName), data.getString(Tag.StudyDate), data.getString(Tag.StudyDescription), "1", "1", "", getCurrentTime(), ""});
                }
            }           
        } catch (IOException ex) {
            ex.printStackTrace();
            //  Logger.getLogger(NetworkQueueUpdate.clas   s.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (dis != null) {
                try {
                    dis.close();
                } catch (Exception e) {
                    e.printStackTrace();
                    // ignore
                }
            }
        }

    }

    public static String getCurrentTime() {
        DateFormat df = new SimpleDateFormat("kk:mm:ss");
        Date d = new Date();
        return df.format(d);
    }
}
