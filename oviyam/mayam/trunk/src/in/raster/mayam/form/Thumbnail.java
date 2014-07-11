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
package in.raster.mayam.form;

import in.raster.mayam.context.ApplicationContext;
import in.raster.mayam.delegates.DicomImageReader;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Level;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

/**
 *
 * @author Devishree
 * @version 2.0
 */
public class Thumbnail extends JLabel {

    int width = 75, height = 75;
    String wadoRequest = null;
    String dest = null;
    String src = null;

    public Thumbnail() {
        setFont(new java.awt.Font("Times", 0, 10));
        setHorizontalAlignment(SwingConstants.CENTER);
        setOpaque(true);
        setBackground(new Color(42, 42, 42));
    }

    public Thumbnail(String iuid) {
        setFont(new java.awt.Font("Times", 0, 10));
        setHorizontalAlignment(SwingConstants.CENTER);
        setOpaque(true);
        setBackground(new Color(42, 42, 42));
        setName(iuid);
    }

    public Thumbnail(String wadoRequest, String dest, String iuid) {
        this.wadoRequest = wadoRequest;
        this.dest = dest;
        setName(iuid);
        setFont(new java.awt.Font("Times", 0, 10));
        setHorizontalAlignment(SwingConstants.CENTER);
        setOpaque(true);
        setBackground(new Color(42, 42, 42));
    }

    public Thumbnail(String source, String dest) {
        this.src = source.split(",")[0];
        this.dest = dest;
        setName(source.split(",")[1]);
        setOpaque(true);
        setBackground(new Color(42, 42, 42));
        setHorizontalAlignment(SwingConstants.CENTER);
    }

    public void read() {
        if (src != null) {
            try {
                BufferedImage image = ImageIO.read(new File(dest + File.separator + getName()));
                width = image.getWidth();
                height = image.getHeight();
                setIcon(new ImageIcon(image));
            } catch (Exception ex) {
                try {
                    BufferedImage image = shrink(DicomImageReader.readDicomFile(new File(src)));
                    setIcon(new ImageIcon(image));
                    File destination = new File(dest + File.separator + getName());
                    destination.getParentFile().mkdirs();
                    ImageIO.write(image, "jpg", destination);
                } catch (Exception ex1) {
                    setDefaultImage();
                }
            }
        } else {
            load();
        }
    }

    public void setImage(BufferedImage image) {
        try {
            width = image.getWidth();
            height = image.getHeight();
            setIcon(new ImageIcon(image));
        } catch (NullPointerException ex) {
            setDefaultImage();
        }
    }

    public void readImage(String dest) {
        try {
            BufferedImage image = ImageIO.read(new File(dest));
            width = image.getWidth();
            height = image.getHeight();
            setIcon(new ImageIcon(image));
        } catch (Exception ex) {
            setDefaultImage();
        }

    }

    public void setVideoImage() {
        setIcon(new ImageIcon(getClass().getResource("/in/raster/mayam/form/images/video.png")));
    }

    public void setDefaultImage() {
        setIcon(new ImageIcon(getClass().getResource("/in/raster/mayam/form/images/blank.jpg")));
    }

    @Override
    public int getWidth() {
        return this.width;
    }

    @Override
    public int getHeight() {
        return this.height;
    }

    public void load() {
        try {
            HttpURLConnection httpURLConnection = (HttpURLConnection) new URL(wadoRequest).openConnection();
            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.setInstanceFollowRedirects(false);
            httpURLConnection.setRequestProperty("Connection", "Keep-Alive");
            httpURLConnection.setRequestProperty("Content-Type", "application/x-java-serialized-object");
            try {
                httpURLConnection.connect();
            } catch (RuntimeException e) {
                ApplicationContext.logger.log(Level.INFO, "Thumbnail - Error while querying ", e);
            }
            if (httpURLConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                InputStream inputStream = httpURLConnection.getInputStream();
                File destination = new File(dest);
                destination.getParentFile().mkdirs();
                OutputStream outputStream = new FileOutputStream(destination);
                byte[] buffer = new byte[4 * 1024];
                int read;
                while ((read = inputStream.read(buffer)) != -1) {
                    if (outputStream != null) {
                        outputStream.write(buffer, 0, read);
                    }
                }
                setOpaque(false);
                setIcon(new ImageIcon(buffer));
                inputStream.close();
                outputStream.close();
            } else {
                ApplicationContext.logger.log(Level.INFO, "Thumbnail - Response Error : " + httpURLConnection.getResponseMessage());
            }
        } catch (IOException ex) {
            ApplicationContext.logger.log(Level.INFO, "Thumbnail", ex);
        }
    }

    private BufferedImage shrink(BufferedImage image) {
        int maxThumbWidth = 75;
        if (image.getWidth() > image.getHeight()) {
            height = maxThumbWidth / Math.round(image.getWidth() / image.getHeight());
        } else {
            width = maxThumbWidth / Math.round(image.getHeight() / image.getWidth());
        }
        BufferedImage imageToReturn = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration().createCompatibleImage(width, height);
        Graphics2D g2 = imageToReturn.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.drawImage(image, 0, 0, imageToReturn.getWidth(), imageToReturn.getHeight(), null);
        g2.dispose();
        return imageToReturn;
    }
}
