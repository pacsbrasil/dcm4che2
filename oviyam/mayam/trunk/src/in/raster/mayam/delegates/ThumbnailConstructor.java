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
package in.raster.mayam.delegates;

import in.raster.mayam.context.ApplicationContext;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import javax.imageio.ImageIO;

/**
 *
 * @author Devishree
 * @version 2.1
 */
public class ThumbnailConstructor implements Runnable {

    String filePath;
    String dest = null;

    public ThumbnailConstructor(String filePath, String storeLoc) {
        this.filePath = filePath;
        this.dest = storeLoc;
    }

    public ThumbnailConstructor() {
    }

    public void constructThumbnail(String filePath, String storeLoc) {
        this.filePath = filePath;
        this.dest = storeLoc;
        run();
    }

    @Override
    public void run() {
        File fileToStore = new File(dest);
        fileToStore.getParentFile().mkdirs();
        try {
//            ImageIO.write(shrinkImage(DicomImageReader.readDicomFile(new File(filePath))), "jpeg", fileToStore);
            ImageIO.write(shrinkImage(ImageIO.read(new File(filePath))), "jpeg", fileToStore);
        } catch (IOException ex) {
            ApplicationContext.logger.log(Level.SEVERE, null, ex);
        }
    }

    private BufferedImage shrinkImage(BufferedImage image) {
        int width = 0, height = 0, maxThumbwidth = 75;
        if (image.getWidth() > image.getHeight()) {
            width = maxThumbwidth;
            height = maxThumbwidth / Math.round(image.getWidth() / image.getHeight());
        } else {
            height = maxThumbwidth;
            width = maxThumbwidth / Math.round(image.getHeight() / image.getWidth());
        }
        GraphicsConfiguration gc = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
        BufferedImage imageToReturn = gc.createCompatibleImage(width, height);
        Graphics2D g2 = imageToReturn.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.drawImage(image, 0, 0, imageToReturn.getWidth(), imageToReturn.getHeight(), null);
        g2.dispose();
        return imageToReturn;
    }
}