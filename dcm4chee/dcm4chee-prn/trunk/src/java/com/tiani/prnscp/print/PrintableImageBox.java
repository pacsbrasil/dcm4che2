/*****************************************************************************
 *                                                                           *
 *  Copyright (c) 2003 by TIANI MEDGRAPH AG                                  *
 *                                                                           *
 *  This file is part of dcm4che.                                            *
 *                                                                           *
 *  This library is free software; you can redistribute it and/or modify it  *
 *  under the terms of the GNU Lesser General Public License as published    *
 *  by the Free Software Foundation; either version 2 of the License, or     *
 *  (at your option) any later version.                                      *
 *                                                                           *
 *  This library is distributed in the hope that it will be useful, but      *
 *  WITHOUT ANY WARRANTY; without even the implied warranty of               *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU        *
 *  Lesser General Public License for more details.                          *
 *                                                                           *
 *  You should have received a copy of the GNU Lesser General Public         *
 *  License along with this library; if not, write to the Free Software      *
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA  *
 *                                                                           *
 *****************************************************************************/

package com.tiani.prnscp.print;

import org.jboss.logging.Logger;

import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmElement;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.data.FileFormat;
import org.dcm4che.dict.Tags;
import org.dcm4che.imageio.plugins.DcmImageReadParam;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import java.awt.print.PrinterException;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * <description>
 *
 * @see <related>
 * @author  <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
 * @version $Revision$
 * @since January 29, 2003
 *
 * <p><b>Revisions:</b>
 *
 * <p><b>yyyymmdd author:</b>
 * <ul>
 * <li> explicit fix description (no line numbers but methods) go
 *            beyond the cvs commit message
 * </ul>
 */
class PrintableImageBox {
   
   // Constants -----------------------------------------------------
   private static final int MEGA_PX = 1024 * 1024;
   private static final String[] MAGNIFICATION_TYPES = {
         "NONE",
         "REPLICATE",
         "BILINEAR",
         "CUBIC"
   };
   private static final Object[] RENDERING_HINTS = {
         RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR,
         RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR,
         RenderingHints.VALUE_INTERPOLATION_BILINEAR,
         RenderingHints.VALUE_INTERPOLATION_BICUBIC,
   };
   private static final int MAGNIFICATION_NONE = 0;
   
   // Attributes ----------------------------------------------------
   private static final DcmObjectFactory dof = DcmObjectFactory.getInstance();
   private final PrinterService service;
   private final Logger log;
   private final File hcFile;
   private final Color borderDensityColor;
   private final int imagePosition;
   private final boolean trim;
   private final boolean crop;
   private final int magnificationType;
   private final double reqImageSize;

   private final ImageReader reader;
   private final DcmImageReadParam readParam;
   
   public int getImagePosition() {
      return imagePosition;
   }
      
   // Static --------------------------------------------------------
   
   // Constructors --------------------------------------------------
   public PrintableImageBox(PrinterService service, Dataset filmbox,
         Dataset imageBox, DcmElement pLutSeq, File hcDir)
      throws IOException
   {
      this.service = service;
      Dataset refImage =  imageBox.getItem(Tags.RefImageSeq);
      this.hcFile = new File(hcDir, refImage.getString(Tags.RefSOPInstanceUID));

      this.imagePosition = imageBox.getInt(Tags.ImagePositionOnFilm, 1);
      this.trim = "YES".equals(
         imageBox.getString(Tags.Trim, filmbox.getString(Tags.Trim)));
      this.crop = "CROP".equals(
         imageBox.getString(Tags.RequestedDecimateCropBehavior,
            service.getDecimateCropBehavior()));
      this.magnificationType = Arrays.asList(MAGNIFICATION_TYPES).indexOf(
         imageBox.getString(Tags.MagnificationType,
            filmbox.getString(Tags.MagnificationType,
               service.getDefaultMagnificationType())));      
      this.reqImageSize = imageBox.getFloat(Tags.RequestedImageSize, 0.f)
         * PrinterService.PTS_PER_MM;
      this.borderDensityColor = service.toColor(
         filmbox.getString(Tags.BorderDensity, service.getBorderDensity()));
      
      this.log = service.getLog();
      
      Iterator iter = ImageIO.getImageReadersByFormatName("DICOM");
      this.reader = (ImageReader) iter.next();
      this.readParam = (DcmImageReadParam) reader.getDefaultReadParam();
      initReadParam(filmbox, imageBox, pLutSeq);
   }

   private void initReadParam(Dataset filmbox, Dataset imageBox, DcmElement pLutSeq) 
      throws IOException
   {
      int minDensity = filmbox.getInt(Tags.MinDensity, service.getMinDensity());
      int maxDensity = filmbox.getInt(Tags.MaxDensity, service.getMaxDensity());
      int illumination =
         filmbox.getInt(Tags.Illumination, service.getIllumination());      
      int reflectedAmbientLight =
         filmbox.getInt(Tags.ReflectedAmbientLight, service.getReflectedAmbientLight());
      
      byte[] pValToDDL = service.getPValToDDL(
         12, 
         minDensity/100.f,
         maxDensity/100.f,
         illumination,
         reflectedAmbientLight,
         getPLUT(filmbox, imageBox, pLutSeq));
      if ("REVERSE".equals(imageBox.getString(Tags.Polarity))) {
         byte tmp;
         for (int i = 0, j = pValToDDL.length-1; i < j; ++i,--j) {
            tmp = pValToDDL[i];
            pValToDDL[i] = pValToDDL[j];
            pValToDDL[j] = tmp;
         }
      }
      readParam.setPValToDLL(pValToDDL);      
   }

   private Dataset getPLUT(Dataset filmbox, Dataset imageBox, DcmElement pLutSeq) 
      throws IOException
   {
      Dataset pLUT;
      Dataset refPLUT = imageBox.getItem(Tags.RefPresentationLUTSeq);
      if (refPLUT == null) {
         refPLUT = filmbox.getItem(Tags.RefPresentationLUTSeq);
      }
      if (refPLUT != null) {
         String uid = refPLUT.getString(Tags.RefSOPInstanceUID);
         for (int i = 0; i < pLutSeq.vm(); ++i) {
            pLUT = pLutSeq.getItem(i);
            if (uid.equals(pLUT.getString(Tags.SOPInstanceUID))) {
               return pLUT;
            }
         }
         throw new RuntimeException(
            "Could not find ref. PLUT[" + uid + "] in Stored Print"); 
      }
      
      String configInfo = imageBox.getString(Tags.ConfigurationInformation,
         filmbox.getString(Tags.ConfigurationInformation,
            service.getDefaultLUT()));
      pLUT = dof.newDataset();
      File pLutFile = new File(service.getLUTDir(),
         configInfo + PrinterService.LUT_FILE_EXT);
      InputStream in = new BufferedInputStream(new FileInputStream(pLutFile));
      try {
         pLUT.readFile(in, FileFormat.DICOM_FILE, -1);
      } finally {
         try { in.close(); } catch (IOException ignore) {}
      }
      return pLUT;      
   }
      
   // Public --------------------------------------------------------
   public void print(Graphics2D g2, Rectangle2D boxRect)
      throws PrinterException
   {
      g2.setColor(borderDensityColor);
      g2.fill(boxRect);
      try {
         ImageInputStream iis = ImageIO.createImageInputStream(hcFile);
         try {
            reader.setInput(iis);
            Rectangle imgRect = 
               new Rectangle(0, 0, reader.getWidth(0), reader.getHeight(0));
            double reqImageWidth = getReqImageWidth(g2, boxRect, imgRect);
            double reqImageHeight = reqImageWidth / reader.getAspectRatio(0);
            double destWidth = Math.min(reqImageWidth, boxRect.getWidth());
            double destHeight = Math.min(reqImageHeight, boxRect.getHeight());
            int srcWidth = (int) Math.round(
               imgRect.getWidth() * destWidth / reqImageWidth);
            int srcHeight = (int) Math.round(
               imgRect.getHeight() * destHeight / reqImageHeight);
            Rectangle srcRect = new Rectangle (
               (int) Math.round(imgRect.getCenterX() - srcWidth / 2),
               (int) Math.round(imgRect.getCenterY() - srcHeight / 2),
               srcWidth,
               srcHeight);
            Rectangle2D destRect = new Rectangle2D.Double(
               boxRect.getCenterX() - destWidth / 2,
               boxRect.getCenterY() - destHeight / 2,
               destWidth,
               destHeight);
            drawImage(g2, destRect, srcRect);
            if (trim) {
               g2.setColor(service.toColor(service.getTrimBoxDensity()));
               g2.draw(destRect);
            }
         } finally {
            try { iis.close(); } catch (IOException e) {}
         }         
      } catch (IOException e) {
         throw new PrinterException("Failed to read image from " + hcFile);
      }
   }
   
   private double getReqImageWidth(Graphics2D g2,
         Rectangle2D boxRect, Rectangle imgRect)
      throws IOException
   {
      if (reqImageSize != 0.) {
         return reqImageSize;
      }
      final double boxWidth = boxRect.getWidth();
      double srcWidthPts = deviceToPts(g2, imgRect.getWidth());
      if (magnificationType == MAGNIFICATION_NONE
         || crop && srcWidthPts > boxWidth)
      {
         return srcWidthPts;
      }
      final double aspectRatioSrc = reader.getAspectRatio(0);
      final double aspectRatioBox = boxRect.getWidth() / boxRect.getHeight();
      if (aspectRatioSrc > aspectRatioBox) {
         return boxWidth;
      } else {
         return boxRect.getHeight() * aspectRatioSrc;
      }
   }
   
   private double deviceToPts(Graphics2D g2, double width) {
      double[] d4 = { 0, 0, width, width };
      try {
         g2.getTransform().inverseTransform(d4, 0, d4, 0, 2);
      } catch (NoninvertibleTransformException e) {
         throw new RuntimeException("Failed to inverse Transform", e);
      }
      return Math.abs(d4[2] - d4[0]);
   }

   public void drawImage(Graphics2D g2, Rectangle2D destRect, Rectangle srcRect)
      throws IOException
   {
      AffineTransform saveAT = g2.getTransform();
      g2.translate(destRect.getX(), destRect.getY());
      g2.scale(
         destRect.getWidth() / srcRect.getWidth(), 
         destRect.getHeight() / srcRect.getHeight());

      Rectangle clip = g2.getClipBounds();
      if (!srcRect.intersects(clip)) {
         return; // nothing to draw inside clip bounds;
      }
      AffineTransform saveAT1 = g2.getTransform();
      Rectangle clippedSrcRect = srcRect.intersection(clip);
      double[] d2 = { clippedSrcRect.getWidth(), clippedSrcRect.getHeight() };
      saveAT1.deltaTransform(d2, 0, d2, 0, 1);
      double sizeMPx = Math.abs(d2[0] * d2[1])  / MEGA_PX;
      int chunks = (int) (sizeMPx / service.getChunkSize()) + 1;
      Rectangle chunkRect =  new Rectangle(0, 0, 
         clippedSrcRect.width, clippedSrcRect.height / chunks + 1);
      log.info("Rendering Clip: " + clippedSrcRect + " = " + sizeMPx 
                    + " MPx, in " + chunks + " chunks");
      
      Object hint = RENDERING_HINTS[magnificationType];
      RenderingHints hints = new RenderingHints(
         RenderingHints.KEY_INTERPOLATION, hint);
      g2.setRenderingHints(hints);
      
      for (int i = 0; i < chunks; ++i) {
         chunkRect.setLocation(
            clippedSrcRect.x,
            clippedSrcRect.y + i * clippedSrcRect.height / chunks);
         log.info("Rendering Chunk #" + (i+1) + ": " + chunkRect);
         g2.setClip(chunkRect);
         g2.translate(
            chunkRect.getX() - srcRect.getX(),
            chunkRect.getY() - srcRect.getY());
         readParam.setSourceRegion(chunkRect);
         BufferedImage bi = reader.read(0, readParam);
         if(!(bi.getColorModel() instanceof IndexColorModel)){
            int w = bi.getWidth();
            int h = bi.getHeight();
            int[] b = bi.getRGB(0, 0, w, h, null, 0, w);
            bi.flush();
            bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            bi.setRGB(0,0,w,h,b, 0,w);
         }

         if (hint != RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR) {
            AffineTransform at = g2.getTransform();
            AffineTransformOp op = new AffineTransformOp(at, hints);
            try {
               g2.transform(at.createInverse());
            } catch (NoninvertibleTransformException e) {
               throw new RuntimeException("Failed to inverse Transform", e);
            }
            bi = op.filter(bi, null);
         }
         g2.drawImage(bi, new AffineTransform(), null);
         g2.setTransform(saveAT1);
      }

      g2.setTransform(saveAT);
   }
}
