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
package in.raster.mayam.delegates;

import in.raster.mayam.context.ApplicationContext;
import in.raster.mayam.facade.Platform;
import in.raster.mayam.util.core.TranscoderMain;
import java.io.File;

/**
 *
 * @author BabuHussain
 * @version 0.5
 *
 */
public class DestinationFinder {

    public DestinationFinder() {
    }

    public String getFileDestination(File file) {
        String fileDest = null;
        if (!Platform.getCurrentPlatform().equals(Platform.MAC)) {
            try {
                String archiveDir = "archieve";
                if (file.getAbsolutePath().startsWith(archiveDir)) {
                    fileDest = transcodedDestination(file);
                } else {
                    fileDest = importFileTranscodedDestination(file);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            fileDest = file.getAbsolutePath();
        }
        return fileDest;
    }

    public String getFileDestination(String filePath) {
        String fileDest = filePath;
        try {
            String archiveDir = "archieve";
            if (filePath.startsWith(archiveDir)) {
                fileDest = ApplicationContext.getAppDirectory() + File.separator + filePath;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return fileDest;
    }

    /**
     *
     * @param file -
     * @return
     */
    public String transcodedDestination(File file) throws Exception {
        String dest = file.getParent() + File.separator + "UnCompressed" + File.separator + file.getName();
        String transParam[] = {"--ivle", file.getAbsolutePath(), dest};
        if (file.isFile()) {
            synchronized (this) {
                TranscoderMain.main(transParam);
            }
        }
        return dest;
    }

    /**
     *
     * @param file
     * @return
     */
    private String importFileTranscodedDestination(File file) throws Exception {
        String userDir = ApplicationContext.getAppDirectory();
        String archiveDir = userDir + File.separator + "archieve";
        //In order to avoid duplication it has been changed.
        //Calendar today = Calendar.getInstance();
        // String parent = archiveDir + File.separator + today.get(Calendar.YEAR) + File.separator + today.get(Calendar.MONTH) + File.separator + today.get(Calendar.DATE) + File.separator + "UnCompressed";
        String parent = archiveDir + File.separator + "UnCompressed";
        File parentFile = new File(parent);
        if (!parentFile.exists()) {
            parentFile.mkdirs();
        }
        String dest = parent + File.separator + file.getName();
        String transParam[] = {"--ivle", file.getAbsolutePath(), dest};
        if (file.isFile()) {
            synchronized (this) {
                TranscoderMain.main(transParam);
            }
        }
        return dest;
    }
}
