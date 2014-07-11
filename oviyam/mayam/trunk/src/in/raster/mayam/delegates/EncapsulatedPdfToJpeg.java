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

import com.sun.pdfview.PDFFile;
import com.sun.pdfview.PDFPage;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import org.dcm4che.dict.Tags;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.io.DicomInputStream;

/**
 *
 * @author BabuHussain
 * @version 0.9
 *
 */
public class EncapsulatedPdfToJpeg {

    PDFFile curFile;
    int totalInstance;
    int curpage;
    ImageIcon imageIcon;
    Image loadedImage;
    BufferedImage currentbufferedimage;

    public EncapsulatedPdfToJpeg() {
    }

    public void readDicom(File file) {
        try {
            DicomInputStream din = new DicomInputStream(new File(file.getAbsolutePath()));
            DicomObject dcmObject = din.readDicomObject();
            byte[] buf = dcmObject.getBytes(Tags.EncapsulatedDocument);
            ByteBuffer byteBuffer = ByteBuffer.wrap(buf);
            openPDFByteBuffer(byteBuffer, null, null);
        } catch (IOException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void openPDFByteBuffer(ByteBuffer buf, String path, String name) {
        PDFFile newfile = null;
        try {
            newfile = new PDFFile(buf);
        } catch (IOException ioe) {
            return;
        }
        this.curFile = newfile;
    }
    PDFPage pg = null;

    public ArrayList createPDFArray() {
        ArrayList<BufferedImage> temp = new ArrayList<BufferedImage>();
        for (int pagenum = 0; pagenum < curFile.getNumPages(); pagenum++) {
            PDFPage pdfPage = curFile.getPage(pagenum + 1);
            Rectangle rect = new Rectangle(0, 0,
                    (int) pdfPage.getBBox().getWidth(),
                    (int) pdfPage.getBBox().getHeight());

            //generate the image
            Image current = pdfPage.getImage(
                    rect.width, rect.height, //width & height
                    rect, // clip rect
                    null, // null for the ImageObserver
                    true, // fill background with white
                    true // block until drawing is done
                    );
            imageIcon = new ImageIcon();
            imageIcon.setImage(current);
            Image tempImage = imageIcon.getImage();
            BufferedImage tempBufferedImage = new BufferedImage(tempImage.getWidth(null), tempImage.getHeight(null), BufferedImage.TYPE_INT_RGB);
            Graphics2D g2 = tempBufferedImage.createGraphics();
            g2.drawImage(tempImage, 0, 0, null);
            temp.add(tempBufferedImage);
        }
        return temp;
    }
}
