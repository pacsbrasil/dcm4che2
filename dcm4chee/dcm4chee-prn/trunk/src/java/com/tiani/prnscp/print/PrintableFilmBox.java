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

import java.awt.Graphics;
import java.awt.Graphics2D;
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
   private final Dataset ds;
   private final Dataset filmbox;
   private final Annotation annotation;
   private final int totPages;
   private final File hcDir;
   
   // Static --------------------------------------------------------
   
   // Constructors --------------------------------------------------
   public PrintableFilmBox(PrinterService service, File hcDir, File spFile,
      int totPages, Dataset session) throws IOException
   {
      this.service = service;
      this.log = service.getLog();
      this.hcDir = hcDir;
      this.totPages = totPages;
      this.ds = dof.newDataset();
      InputStream in = new BufferedInputStream(new FileInputStream(spFile));
      try {
         ds.readFile(in, FileFormat.DICOM_FILE, -1);
      } finally {
         try { in.close(); } catch (IOException ignore) {}
      }
      this.filmbox = ds.getItem(Tags.FilmBoxContentSeq);
      
      String adfID = filmbox.getString(Tags.AnnotationDisplayFormatID,
         service.getDefaultAnnotation());
      annotation = new Annotation(service, adfID, totPages);
      annotation.setSession(session);
      annotation.setAnnotationContentSeq(ds.get(Tags.AnnotationContentSeq));
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
      new Page(annotation.getImageableWidth(pf), annotation.getImageableHeight(pf),
         g2, log, ds, hcDir, service);
      log.debug("Exit print");
      return Printable.PAGE_EXISTS;
   }
   
   // Y overrides ---------------------------------------------------
   
   // Package protected ---------------------------------------------
   
   // Protected -----------------------------------------------------
   
   // Private -------------------------------------------------------
   
   // Inner classes -------------------------------------------------
}
