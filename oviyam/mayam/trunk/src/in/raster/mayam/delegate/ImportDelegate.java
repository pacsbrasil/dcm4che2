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
import in.raster.mayam.exception.CompressedDcmOnMacException;
import in.raster.mayam.facade.Platform;
import in.raster.mayam.form.display.Display;
import in.raster.mayam.form.ImportingProgress;
import in.raster.mayam.form.MainScreen;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import javax.swing.JOptionPane;
import org.dcm4che.dict.Tags;
import org.dcm4che2.data.BasicDicomObject;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.TransferSyntax;
import org.dcm4che2.io.DicomInputStream;

/**
 *
 * @author  BabuHussain
 * @version 0.5
 *
 */
public class ImportDelegate extends Thread {

    private File importFolder;
    private File importFile;
    private File file;
    private boolean isDirectory;
    private MainScreen parent;
    ArrayList<String> absolutePathList = new ArrayList();
    ImportingProgress importingProgress = null;

    public ImportDelegate() {
    }

    public ImportDelegate(File file, boolean isDirectory, MainScreen parent) {
        this.parent = parent;
        this.isDirectory = isDirectory;
        this.file = file;
        this.start();
    }

    public void run() {
        importingProgress = new ImportingProgress();
        Display.alignScreen(importingProgress);
        importingProgress.setVisible(true);
        importingProgress.updateBar(0);

        if (isDirectory) {
            setImportFolder(file);
            readAndUpdateDB();
        } else {
            setImportFile(file);
            readFileAndUpdateDB();

        }
        importingProgress.updateBar(importingProgress.getProgressMaximum());
        importingProgress.setVisible(false);
        MainScreen.showLocalDBStorage();
    }

    public ImportDelegate(File importFolder) {
        this.importFolder = importFolder;
    }

    private ArrayList getAbsolutePathArray() {
        addPath(importFolder.getAbsolutePath());
        return absolutePathList;
    }

    private void addPath(String directoryPath) {
        File directory = new File(directoryPath);
        String[] listOfFiles = directory.list();
        for (int i = 0; i < listOfFiles.length; i++) {
            if (new File(directory.getAbsolutePath() + File.separator + listOfFiles[i]).isDirectory()) {
                //  System.out.println(""+listOfFiles[i]+"is directory");
                addPath(new String(directory.getAbsolutePath() + File.separator + listOfFiles[i]));
            } else {
                absolutePathList.add(new String(directory.getAbsolutePath() + File.separator + listOfFiles[i]));
            }
        }
    }

    public void readAndUpdateDB() {
        getAbsolutePathArray();
        importingProgress.setProgressMaximum(absolutePathList.size());
        for (int i = 0; i < absolutePathList.size(); i++) {
            try {
                String s = absolutePathList.get(i);
                readAndImportDicomFile(s);
                importingProgress.updateBar(i + 1);
            } catch (CompressedDcmOnMacException e) {
                importingProgress.setVisible(false);
                JOptionPane.showMessageDialog(ApplicationContext.mainScreen, e.getMessage());
                break;
            }

        }
    }

    private void readAndImportDicomFile(String dicomFilePath) throws CompressedDcmOnMacException {
        DicomInputStream dis = null;
        File parseFile = new File(dicomFilePath);
        try {
            dis = new DicomInputStream(parseFile);
            DicomObject data = new BasicDicomObject();
            data = dis.readDicomObject();
            if (data != null) {
                if (Platform.getCurrentPlatform().equals(Platform.MAC)) {
                    if (data.getString(Tags.TransferSyntaxUID).equalsIgnoreCase(TransferSyntax.ExplicitVRLittleEndian.uid()) || data.getString(Tags.TransferSyntaxUID).equalsIgnoreCase(TransferSyntax.ImplicitVRLittleEndian.uid())) {
                        ApplicationContext.databaseRef.importDataToDatabase(data, parseFile, false);
                    } else {
                        throw new CompressedDcmOnMacException();
                    }
                } else {
                    ApplicationContext.databaseRef.importDataToDatabase(data, parseFile, false);
                }

            }
        } catch (IOException ex) {
            ex.printStackTrace();
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

    public void setImportFolder(File importFolder) {
        this.importFolder = importFolder;
    }

    public void setImportFile(File importFile) {
        this.importFile = importFile;
    }

    public void readFileAndUpdateDB() {
        try {
            readAndImportDicomFile(importFile.getAbsolutePath());
        } catch (CompressedDcmOnMacException e) {
            importingProgress.setVisible(false);
            JOptionPane.showMessageDialog(ApplicationContext.mainScreen, e.getMessage());
        }
    }
}
