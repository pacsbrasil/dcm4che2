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
import java.awt.geom.Rectangle2D;
import java.awt.print.PrinterException;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.util.Iterator;

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
   
   // Attributes ----------------------------------------------------
   private static final DcmObjectFactory dof = DcmObjectFactory.getInstance();
   private final PrinterService service;
   private final File hcFile;
   private final Logger log;
   private final Color borderDensityColor;
   private final int imagePosition;
   private final boolean trim;
   private final boolean crop;
   private final String magnificationType;
   private final Float reqImageSize;

   private final ImageReader reader;
   private final DcmImageReadParam readParam;
   
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
      this.magnificationType = imageBox.getString(Tags.MagnificationType,
         filmbox.getString(Tags.MagnificationType,
            service.getDefaultMagnificationType()));
      this.reqImageSize = imageBox.getFloat(Tags.RequestedImageSize);
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
      
      ImageInputStream iis = null;
      try {
         iis = ImageIO.createImageInputStream(hcFile);
         reader.setInput(iis);
         Rectangle2D imageRect = calcImageRect(g2, boxRect);
         // TODO
         if (trim) {
            g2.setColor(service.toColor(service.getTrimBoxDensity()));
            g2.draw(imageRect);
         }
         
      } catch (IOException e) {
         throw new PrinterException("Failed to read image from " + hcFile);
      } finally {
         if (iis != null) {
            try { iis.close(); } catch (IOException e) {}
         }
      }
//      g2.translate(boxRect.getX(), boxRect.getY());
//      g2.translate(-boxRect.getX(), -boxRect.getY());
   }
   
   private Rectangle2D calcImageRect(Graphics2D g2, Rectangle2D boxRect)
      throws IOException
   {
      int srcWidth = reader.getWidth(0);
      int srcHeight = reader.getHeight(0);
      double aspectRatioSrc = reader.getAspectRatio(0);
      double aspectRatioBox = boxRect.getWidth() / boxRect.getHeight();
      double destWidth = boxRect.getWidth();
      double destHeight = boxRect.getHeight();
      if (aspectRatioSrc > aspectRatioBox) {
         destHeight = destWidth / aspectRatioSrc;
      } else {
         destWidth = destHeight * aspectRatioSrc;
      }
      return new Rectangle2D.Double(
         boxRect.getCenterX() - destWidth / 2,
         boxRect.getCenterY() - destHeight / 2,
         destWidth,
         destHeight);
   }
   
   public int getImagePosition() {
      return imagePosition;
   }
      
   // X overrides ---------------------------------------------------
   
   // Y implementation ----------------------------------------------
}
