/*****************************************************************************
 *                                                                           *
 *  Copyright (c) 2002 by TIANI MEDGRAPH AG                                  *
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

import org.dcm4che.data.DcmElement;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.data.Dataset;
import org.dcm4che.data.FileFormat;
import org.dcm4che.dict.Tags;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.print.Printable;
import java.awt.print.PageFormat;
import java.awt.print.PrinterException;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.jboss.logging.Logger;

/**
 * <description>
 *
 * @see <related>
 * @author  <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
 * @version $Revision$
 * @since January 28, 2003
 *
 * <p><b>Revisions:</b>
 *
 * <p><b>yyyymmdd author:</b>
 * <ul>
 * <li> explicit fix description (no line numbers but methods) go
 *            beyond the cvs commit message
 * </ul>
 */
class PrintableFilmBox implements Printable {
   
   // Constants -----------------------------------------------------
   
   // Attributes ----------------------------------------------------
   private static final DcmObjectFactory dof = DcmObjectFactory.getInstance();
   private final PrinterService service;
   private final Logger log;
   private final Dataset storedPrint;
   private final Dataset filmbox;
   private final Annotation annotation;
   private final int totPages;
   private final File hcDir;
   private final PrintableImageBox[] imageBoxes;
   private final Color emptyImageBoxDensityColor;
   private final int rows;
   private final int columns;
   
   // Static --------------------------------------------------------
   
   // Constructors --------------------------------------------------
   public PrintableFilmBox(PrinterService service, File hcDir, File spFile,
      int totPages, Dataset session) throws IOException
   {
      this.service = service;
      this.log = service.getLog();
      this.hcDir = hcDir;
      this.totPages = totPages;
      this.storedPrint = dof.newDataset();
      InputStream in = new BufferedInputStream(new FileInputStream(spFile));
      try {
         storedPrint.readFile(in, FileFormat.DICOM_FILE, -1);
      } finally {
         try { in.close(); } catch (IOException ignore) {}
      }
      this.filmbox = storedPrint.getItem(Tags.FilmBoxContentSeq);
      
      // parse ImageDisplayFormat
      String displayFormat = filmbox.getString(Tags.ImageDisplayFormat);
      int pos = displayFormat.lastIndexOf(',');
      this.rows = Integer.parseInt(displayFormat.substring(pos+1));
      this.columns = Integer.parseInt(displayFormat.substring(9,pos));
      
      this.emptyImageBoxDensityColor = service.toColor(
         filmbox.getString(Tags.EmptyImageDensity, 
            service.getEmptyImageDensity()));

      DcmElement imageBoxContentSeq = storedPrint.get(Tags.ImageBoxContentSeq);
      this.imageBoxes = new PrintableImageBox[imageBoxContentSeq.vm()];
      for (int i = 0; i < imageBoxes.length; ++i) {
         imageBoxes[i] = new PrintableImageBox(service,
            filmbox,
            imageBoxContentSeq.getItem(i),
            storedPrint.get(Tags.PresentationLUTContentSeq),
            hcDir);
      }      
      
      String adfID = filmbox.getString(Tags.AnnotationDisplayFormatID,
         service.getDefaultAnnotation());
      annotation = new Annotation(service, adfID, totPages);
      annotation.setSession(session);
      annotation.setAnnotationContentSeq(storedPrint.get(Tags.AnnotationContentSeq));
      
   }
      
   // Public --------------------------------------------------------
   public PageFormat getPageFormat() {
      String sizeID = filmbox.getString(Tags.FilmSizeID,
         service.getDefaultFilmSizeID());
      String orient = filmbox.getString(Tags.FilmOrientation,
         service.getDefaultFilmOrientation());
      PageFormat pf = new PageFormat();
      pf.setPaper(service.getPaper(sizeID));
      pf.setOrientation(
         "PORTRAIT".equals(orient)
            ? PageFormat.PORTRAIT
            : service.isReverseLandscape()
               ? PageFormat.REVERSE_LANDSCAPE
               : PageFormat.LANDSCAPE);
      return pf;
   }

   private Color getBorderDensityColor() {
      return null;
   }
   
   // Printable implementation ----------------------------------------------
   public int print(Graphics g, PageFormat pf, int pageIndex)
      throws PrinterException
   {
      if (pageIndex >= totPages) {
         return Printable.NO_SUCH_PAGE;
      }
      Graphics2D g2 = (Graphics2D) g;
      if (log.isDebugEnabled()) {
         log.debug("Enter print: pageIndex= " + pageIndex
            + ", transform=" + g2.getTransform()
            + ", clipBounds=" + g2.getClipBounds());
      }
      // Skip first invocation of print with identity tranformation
      // Unknown, why it's done, but painting nothing in this case seems be ok.
      if (g2.getTransform().isIdentity()) {
         log.debug("Exit print: Skip print with identity transform");
         return Printable.PAGE_EXISTS;
      }
      annotation.print(g2, pf, pageIndex);
      
      g2.translate(annotation.getImageableX(pf), annotation.getImageableY(pf));
      g2.setColor(emptyImageBoxDensityColor);
      
      double w = annotation.getImageableWidth(pf);
      double h = annotation.getImageableHeight(pf);
      Rectangle2D filmboxRect = new Rectangle2D.Double(0, 0, w, h);
      g2.fill(filmboxRect);
      for (int i = 0; i < imageBoxes.length; ++i) {
         imageBoxes[i].print(g2, getImageBoxRect(
            imageBoxes[i].getImagePosition(), filmboxRect));
      }
//      new Page(annotation.getImageableWidth(pf), annotation.getImageableHeight(pf),
//         g2, log, storedPrint, hcDir, service);
      log.debug("Exit print");
      return Printable.PAGE_EXISTS;
   }
   
   private Rectangle2D getImageBoxRect(int imagePos, Rectangle2D filmboxRect) {
      double border = service.getBorderThickness() * PrinterService.PTS_PER_MM;
      double w = filmboxRect.getWidth() / columns - border * (columns - 1);
      double h = filmboxRect.getHeight() / rows - border * (rows - 1);
      int xPos = (imagePos - 1) % columns;
      int yPos = (imagePos - 1) / columns;
      double x = w * xPos + border * xPos;
      double y = h * yPos + border * yPos;
      return new Rectangle2D.Double(x, y, w, h);
   }
      
   // Y overrides ---------------------------------------------------
   
   // Package protected ---------------------------------------------
   // make visible only for testdriver 
   PrintableImageBox[] imageBoxes() { 
      return imageBoxes;
   }
   // Protected -----------------------------------------------------
   
   // Private -------------------------------------------------------
   
   // Inner classes -------------------------------------------------
}
