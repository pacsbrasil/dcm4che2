/*                                                                           *
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
 */
package com.tiani.prnscp.print;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.dcm4che.data.Dataset;

import org.dcm4che.data.DcmElement;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.data.FileFormat;
import org.dcm4che.dict.Tags;

import org.jboss.logging.Logger;

/**
 *  <description>
 *
 * @author     <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
 * @since      March 30, 2003
 * @created    January 28, 2003
 * @version    $Revision$
 */
class PrintableFilmBox implements Printable
{

    // Constants -----------------------------------------------------

    // Attributes ----------------------------------------------------
    private final static DcmObjectFactory dof = DcmObjectFactory.getInstance();
    private final PrinterService service;
    private final Logger log;
    private final PageFormat pageFormat;
    private final Annotation annotation;
    private final int totPages;
    private final PrintableImageBox[] imageBoxes;
    private final int rows;
    private final int columns;


    // Static --------------------------------------------------------

    // Constructors --------------------------------------------------
    /**
     *  Constructor for the PrintableFilmBox object
     *
     * @param  service          Description of the Parameter
     * @param  hcDir            Description of the Parameter
     * @param  spFile           Description of the Parameter
     * @param  totPages         Description of the Parameter
     * @param  session          Description of the Parameter
     * @param  callingAET       Description of the Parameter
     * @exception  IOException  Description of the Exception
     */
    public PrintableFilmBox(PrinterService service, File hcDir, File spFile,
            int totPages, Dataset session, String callingAET)
        throws IOException
    {
        this.service = service;
        this.log = service.getLog();
        this.totPages = totPages;
        Dataset storedPrint = dof.newDataset();
        InputStream in = new BufferedInputStream(new FileInputStream(spFile));
        try {
            storedPrint.readFile(in, FileFormat.DICOM_FILE, -1);
        } finally {
            try {
                in.close();
            } catch (IOException ignore) {}
        }
        Dataset filmbox = storedPrint.getItem(Tags.FilmBoxContentSeq);
        // set Filmbox Configuration Information if not provided.
        if (filmbox.getString(Tags.ConfigurationInformation) == null) {
            filmbox.putLO(Tags.ConfigurationInformation,
                    service.getLUTForCallingAET(callingAET));
        }
        this.pageFormat = toPageFormat(filmbox);
        // parse ImageDisplayFormat
        String displayFormat = filmbox.getString(Tags.ImageDisplayFormat);
        int pos = displayFormat.lastIndexOf(',');
        this.rows = Integer.parseInt(displayFormat.substring(pos + 1));
        this.columns = Integer.parseInt(displayFormat.substring(9, pos));

        DcmElement imageBoxContentSeq = storedPrint.get(Tags.ImageBoxContentSeq);
        this.imageBoxes = new PrintableImageBox[imageBoxContentSeq.vm()];
        for (int i = 0; i < imageBoxes.length; ++i) {
            Dataset imageBox = imageBoxContentSeq.getItem(i);
            Dataset refImage = imageBox.getItem(Tags.RefImageSeq);
            File hcFile = new File(hcDir, refImage.getString(Tags.RefSOPInstanceUID));
            imageBoxes[i] = new PrintableImageBox(service,
                    filmbox,
                    imageBox,
                    storedPrint,
                    hcFile);
        }

        String adfID = filmbox.getString(Tags.AnnotationDisplayFormatID,
                service.getAnnotationForCallingAET(callingAET));
        annotation = new Annotation(service, adfID, totPages);
        annotation.setSession(session);
        annotation.setCallingAET(callingAET);
        annotation.setAnnotationContentSeq(storedPrint.get(Tags.AnnotationContentSeq));

    }


    /**
     *Constructor for the PrintableFilmBox object
     *
     * @param  service          Description of the Parameter
     * @param  hcFile           Description of the Parameter
     * @param  config           Description of the Parameter
     * @param  session          Description of the Parameter
     * @exception  IOException  Description of the Exception
     */
    public PrintableFilmBox(PrinterService service, File hcFile, String config)
        throws IOException
    {
        this.service = service;
        this.log = service.getLog();
        this.totPages = 1;
        Dataset session = dof.newDataset();
        session.putLO(Tags.FilmSessionLabel, hcFile.getName() + "[" + config + "]");
        Dataset filmbox = dof.newDataset();
        filmbox.putLO(Tags.ConfigurationInformation, config);
        this.pageFormat = toPageFormat(filmbox);
        this.rows = 1;
        this.columns = 1;

        this.imageBoxes = new PrintableImageBox[1];
        this.imageBoxes[0] = new PrintableImageBox(service,
                filmbox,
                filmbox,
                null,
                hcFile);

        String adfID = service.getAnnotationForPrintImage();
        annotation = new Annotation(service, adfID, totPages);
        annotation.setSession(session);
    }

    // Public --------------------------------------------------------
    private PageFormat toPageFormat(Dataset filmbox)
    {
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


    /**
     *  Gets the pageFormat attribute of the PrintableFilmBox object
     *
     * @return    The pageFormat value
     */
    public PageFormat getPageFormat()
    {
        return pageFormat;
    }


    private Color getBorderDensityColor()
    {
        return null;
    }


    // Printable implementation ----------------------------------------------
    /**
     *  Description of the Method
     *
     * @param  g                     Description of the Parameter
     * @param  pf                    Description of the Parameter
     * @param  pageIndex             Description of the Parameter
     * @return                       Description of the Return Value
     * @exception  PrinterException  Description of the Exception
     */
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
            return Printable.PAGE_EXISTS;
        }
        annotation.print(g2, pf, pageIndex);

        g2.translate(annotation.getImageableX(pf), annotation.getImageableY(pf));

        double w = annotation.getImageableWidth(pf);
        double h = annotation.getImageableHeight(pf);
        Rectangle2D filmboxRect = new Rectangle2D.Double(0, 0, w, h);
        for (int i = 0; i < imageBoxes.length; ++i) {
            imageBoxes[i].print(g2, getImageBoxRect(
                    imageBoxes[i].getImagePosition(), filmboxRect));
        }
        return Printable.PAGE_EXISTS;
    }


    private Rectangle2D getImageBoxRect(int imagePos, Rectangle2D filmboxRect)
    {
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
    PrintableImageBox[] imageBoxes()
    {
        return imageBoxes;
    }
    // Protected -----------------------------------------------------

    // Private -------------------------------------------------------

    // Inner classes -------------------------------------------------
}

