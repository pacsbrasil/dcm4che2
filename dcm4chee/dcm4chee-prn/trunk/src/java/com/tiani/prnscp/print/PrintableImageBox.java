/*                                                                           *
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
 */
package com.tiani.prnscp.print;

import java.awt.Color;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Rectangle2D;
import java.awt.Graphics2D;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import java.awt.print.PrinterException;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.print.attribute.standard.Chromaticity;
import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmElement;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.data.FileFormat;
import org.dcm4che.dict.Tags;
import org.dcm4che.imageio.plugins.DcmImageReadParam;
import org.jboss.logging.Logger;

/**
 *  <description>
 *
 * @author  <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
 * @created  January 29, 2003
 * @version  $Revision$
 */
class PrintableImageBox
{

    // Constants -----------------------------------------------------
    private final static String REVERSE = "REVERSE";

    private final static int MEGA_BYTE = 1024 * 1024;
    private final static String[] MAGNIFICATION_TYPES = {
            PrinterService.NONE,
            PrinterService.REPLICATE,
            PrinterService.BILINEAR,
            PrinterService.CUBIC
            };
    private final static Object[] RENDERING_HINTS = {
            RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR,
            RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR,
            RenderingHints.VALUE_INTERPOLATION_BILINEAR,
            RenderingHints.VALUE_INTERPOLATION_BICUBIC,
            };
    private final static int MAGNIFICATION_NONE = 0;

    // Attributes ----------------------------------------------------
    private final static DcmObjectFactory dof = DcmObjectFactory.getInstance();
    private final PrinterService service;
    private final Logger log;
    private final boolean debug;

    private final File hcFile;
    private final Color borderDensityColor;
    private final Color trimColor;
    private final int pos;
    private final boolean crop;
    private final int magnificationType;
    private final double reqImageSize;
    private final byte[] pValToDDL;

    private final ImageReader reader;
    private final DcmImageReadParam readParam;


    /**
     *  Gets the imagePosition attribute of the PrintableImageBox object
     *
     * @return  The imagePosition value
     */
    public int getImagePosition()
    {
        return pos;
    }

    // Static --------------------------------------------------------
    // workaround for ImageIO.scanForPlugins() do not find DICOM Plugin!
    static {
        javax.imageio.spi.IIORegistry.getDefaultInstance().registerServiceProvider(
                new org.dcm4cheri.imageio.plugins.DcmImageReaderSpi());
    }


    // Constructors --------------------------------------------------
    /**
     *  Constructor for the PrintableImageBox object
     *
     * @param  service Description of the Parameter
     * @param  filmbox Description of the Parameter
     * @param  imageBox Description of the Parameter
     * @param  storedPrint Description of the Parameter
     * @param  hcFile Description of the Parameter
     * @exception  IOException Description of the Exception
     */
    public PrintableImageBox(PrinterService service, Dataset filmbox,
            Dataset imageBox, Dataset storedPrint, File hcFile,
            Chromaticity chromaticity)
        throws IOException
    {
        this.log = service.getLog();
        this.debug = log.isDebugEnabled();
        this.service = service;
        this.hcFile = hcFile;
        this.pos = imageBox.getInt(Tags.ImagePositionOnFilm, 1);
        this.crop = PrinterService.CROP.equals(
                imageBox.getString(Tags.RequestedDecimateCropBehavior,
                service.getDecimateCropBehavior()));
        this.magnificationType = Arrays.asList(MAGNIFICATION_TYPES).indexOf(
                imageBox.getString(Tags.MagnificationType,
                filmbox.getString(Tags.MagnificationType,
                service.getDefaultMagnificationType())));
        this.reqImageSize = imageBox.getFloat(Tags.RequestedImageSize, 0.f)
                 * PrinterService.PTS_PER_MM;
        this.borderDensityColor = PrinterService.BLACK.equals(
                filmbox.getString(Tags.BorderDensity,
                service.getBorderDensity()))
                ? Color.BLACK
                : Color.WHITE;
        this.trimColor = PrinterService.YES.equals(
                imageBox.getString(Tags.Trim,
                filmbox.getString(Tags.Trim,
                service.getTrim())))
                ? (borderDensityColor == Color.BLACK ? Color.WHITE : Color.BLACK)
                : null;

        Iterator iter = ImageIO.getImageReadersByFormatName("DICOM");
        this.reader = (ImageReader) iter.next();
        this.readParam = (DcmImageReadParam) reader.getDefaultReadParam();
        
        final int minDensity =
                filmbox.getInt(Tags.MinDensity, service.getMinDensity(chromaticity));
        final int maxDensity =
                filmbox.getInt(Tags.MaxDensity, service.getMaxDensity(chromaticity));
        final int illumination =
                filmbox.getInt(Tags.Illumination, service.getIllumination());
        final int reflectedAmbientLight =
                filmbox.getInt(Tags.ReflectedAmbientLight, service.getReflectedAmbientLight());

        this.pValToDDL = service.getPValToDDL(
                chromaticity,
                12,
                minDensity / 100.f,
                maxDensity / 100.f,
                illumination,
                reflectedAmbientLight,
                getPLUT(filmbox, imageBox, storedPrint));
        if (REVERSE.equals(imageBox.getString(Tags.Polarity))) {
            byte tmp;
            for (int i = 0, j = pValToDDL.length - 1; i < j; ++i, --j) {
                tmp = pValToDDL[i];
                pValToDDL[i] = pValToDDL[j];
                pValToDDL[j] = tmp;
            }
        }
        readParam.setPValToDDL(pValToDDL);
        if (debug) {
            log.debug("ImageBox #" + pos
                     + ": Init\n\thcFile: " + hcFile
                     + "\n\tcrop: " + crop
                     + "\n\tmagnificationType: " + MAGNIFICATION_TYPES[magnificationType]
                     + "\n\treqImageSize: " + reqImageSize
                     + "\n\tborderDensityColor: " + borderDensityColor
                     + "\n\ttrimColor: " + trimColor
                     + "\n\tminDensity: " + minDensity
                     + "\n\tmaxDensity: " + maxDensity
                     + "\n\tillumination: " + illumination
                     + "\n\treflectedAmbientLight: " + reflectedAmbientLight);
/*
             StringBuffer sb = new StringBuffer("pValToDDL:");
             for (int i = 0; i < pValToDDL.length; ++i) {
                 sb.append("\n\t").append(pValToDDL[i] & 0xff);
             }
             log.debug(sb.toString());
*/
        }
    }


    private Dataset getPLUT(Dataset filmbox, Dataset imageBox, Dataset storedPrint)
        throws IOException
    {
        Dataset pLUT = null;
        if (storedPrint != null) {
            DcmElement pLutSeq = storedPrint.get(Tags.PresentationLUTContentSeq);
            Dataset refPLUT = imageBox.getItem(Tags.RefPresentationLUTSeq);
            if (refPLUT == null) {
                refPLUT = filmbox.getItem(Tags.RefPresentationLUTSeq);
            }
            if (refPLUT != null) {
                String uid = refPLUT.getString(Tags.RefSOPInstanceUID);
                if (debug) {
                    log.debug("ImageBox #" + pos
                             + ": Take PLUT[" + uid + "] from Stored Print");
                }
                for (int i = 0; i < pLutSeq.vm(); ++i) {
                    pLUT = pLutSeq.getItem(i);
                    if (uid.equals(pLUT.getString(Tags.SOPInstanceUID))) {
                        return pLUT;
                    }
                }
                throw new RuntimeException(
                        "Could not find PLUT[" + uid + "] in Stored Print");
            }
        }
        String configInfo = imageBox.getString(Tags.ConfigurationInformation,
                filmbox.getString(Tags.ConfigurationInformation));        
        if (configInfo == null) {
            log.debug("ImageBox #" + pos
                             + ": Use Pixel Values as DDLs");
            return null;
        }
        PLutBuilder plutBuilder = new PLutBuilder(configInfo, service.getLUTDir());
        if (debug) {
            log.debug("ImageBox #" + pos
                     + ": Apply PLUT[" + plutBuilder + "]");
        }
        return plutBuilder.createOrLoadPLUT(service.getLUTDir());
    }


    // Public --------------------------------------------------------
    private String toString(Rectangle2D rect)
    {
        return "[x=" + (float) rect.getX()
                 + ",y=" + (float) rect.getY()
                 + ",w=" + (float) rect.getWidth()
                 + ",h=" + (float) rect.getHeight()
                 + "]";
    }


    private Rectangle2D transform(AffineTransform tx, Rectangle2D rect)
    {
        double[] srcPts = {
                rect.getX(),
                rect.getY(),
                rect.getX() + rect.getWidth(),
                rect.getY() + rect.getHeight()
                };
        double[] dstPts = new double[4];
        tx.transform(srcPts, 0, dstPts, 0, 2);
        return new Rectangle2D.Double(
                dstPts[0],
                dstPts[1],
                dstPts[2] - dstPts[0],
                dstPts[3] - dstPts[1]);
    }


    /**
     *  Description of the Method
     *
     * @param  g2 Description of the Parameter
     * @param  boxRect Description of the Parameter
     * @exception  PrinterException Description of the Exception
     */
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
                final double reqImageWidth = getReqImageWidth(g2, boxRect, imgRect);
                final double reqImageHeight = reqImageWidth / reader.getAspectRatio(0);
                final double destWidth = Math.min(reqImageWidth, boxRect.getWidth());
                final double destHeight = Math.min(reqImageHeight, boxRect.getHeight());
                final int srcWidth = (int) Math.round(
                        imgRect.getWidth() * destWidth / reqImageWidth);
                final int srcHeight = (int) Math.round(
                        imgRect.getHeight() * destHeight / reqImageHeight);
                Rectangle srcRect = new Rectangle(
                        (int) Math.round(imgRect.getCenterX() - srcWidth / 2),
                        (int) Math.round(imgRect.getCenterY() - srcHeight / 2),
                        srcWidth,
                        srcHeight);
                Rectangle2D destRect = new Rectangle2D.Double(
                        boxRect.getCenterX() - destWidth / 2,
                        boxRect.getCenterY() - destHeight / 2,
                        destWidth,
                        destHeight);
                final double scaleX = destRect.getWidth() / srcRect.getWidth();
                final double scaleY = destRect.getHeight() / srcRect.getHeight();
                AffineTransform tx = g2.getTransform();
                if (debug) {
                    log.debug("ImageBox #" + pos
                             + " print:\n\tclip" + toString(g2.getClipBounds())
                             + "pts\n\tbox" + toString(boxRect)
                             + "pts\n\t ->" + toString(transform(tx, boxRect))
                             + "ppx\n\timg" + toString(imgRect)
                             + "px\n\tsrc" + toString(srcRect)
                             + "px\n\tdst" + toString(destRect)
                             + "pts\n\t ->" + toString(transform(tx, destRect))
                             + "ppx\n\t" + tx);
                }
                g2.translate(destRect.getX(), destRect.getY());
                g2.scale(scaleX, scaleY);
                drawImage(g2, srcRect);
                g2.setTransform(tx);
                if (trimColor != null) {
                    g2.setColor(trimColor);
                    g2.draw(destRect);
                }
            } finally {
                try {
                    iis.close();
                } catch (IOException e) {}
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
                 || crop && srcWidthPts > boxWidth) {
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


    private double deviceToPts(Graphics2D g2, double width)
    {
        double[] d4 = {0, 0, width, width};
        try {
            g2.getTransform().inverseTransform(d4, 0, d4, 0, 2);
        } catch (NoninvertibleTransformException e) {
            throw new RuntimeException("Failed to inverse Transform", e);
        }
        return Math.abs(d4[2] - d4[0]);
    }


    private void drawImage(Graphics2D g2, Rectangle srcRect)
        throws IOException
    {
        Rectangle clip = g2.getClipBounds();
        clip.translate(srcRect.x, srcRect.y);
        if (!srcRect.intersects(clip)) {
            return;
            // nothing to draw inside clip bounds;
        }
        Rectangle clippedSrcRect = srcRect.intersection(clip);

        AffineTransform tx = g2.getTransform();
        long srcWxH = (long) clippedSrcRect.width * clippedSrcRect.height;
        long dstWxH = toDestWxH(tx, clippedSrcRect);
        final boolean decimate = srcWxH > dstWxH;
        final Object hint = decimate && service.isDecimateByNearestNeighbor()
                 ? RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR
                 : RENDERING_HINTS[magnificationType];
        final boolean scaleBi = decimate && service.isMinimizeJobsize()
                 || hint != RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR;

        final int chunks = 1 + (int) ((srcWxH * 3 + dstWxH * 4)
                 / (service.getChunkSize() * MEGA_BYTE));
        final int chunkOffset = clippedSrcRect.height / chunks;
        Rectangle chunkRect = new Rectangle(
                clippedSrcRect.x,
                clippedSrcRect.y,
                clippedSrcRect.width,
                chunkOffset + 1);
        RenderingHints hints = new RenderingHints(
                RenderingHints.KEY_INTERPOLATION, hint);
        g2.setRenderingHints(hints);
        g2.translate(clippedSrcRect.x - srcRect.x, clippedSrcRect.y - srcRect.y);
        if (debug) {
            log.debug("ImageBox #" + pos
                     + " render:\n\tsrc" + toString(clippedSrcRect)
                     + "px\n\t ->" + toString(transform(tx,
                    new Rectangle(0, 0, clippedSrcRect.width, clippedSrcRect.height)))
                     + "ppx\n\tchunks: " + chunks
                     + "\n\toffset: " + chunkOffset
                     + "px\n\thint: " + hint
                     + "\n\tscaleBi: " + scaleBi);
        }

        double[] mm = null;
        AffineTransformOp scaleOp = null;
        if (scaleBi) {
            mm = new double[6];
            tx.getMatrix(mm);
            mm[4] = -Math.min(0, mm[0] * chunkRect.width + mm[2] * chunkRect.height);
            mm[5] = -Math.min(0, mm[1] * chunkRect.width + mm[3] * chunkRect.height);
            tx.setTransform(mm[0], mm[1], mm[2], mm[3], mm[4], mm[5]);
            g2.transform(createInverse(tx));
            scaleOp = new AffineTransformOp(tx, hints);
        }

        BufferedImage dest = null;
        for (int i = 0, y = 0; i < chunks; ++i, chunkRect.y += chunkOffset, y += chunkOffset) {
            if (i == chunks - 1) {
                // Adjust last chunk to fit clippedSrcRect
                chunkRect.height = clippedSrcRect.height - i * chunkOffset;
                if (chunkRect.height != chunkOffset + 1) {
                    dest = null;
                }
            }
            if (debug) {
                log.debug("ImageBox #" + pos + " render chunk #" + (i + 1)
                         + "\n\tsrc" + toString(chunkRect)
                         + "px\n\t ->" + toString(transform(tx,
                        new Rectangle(0, y, chunkRect.width, chunkRect.height)))
                         + "ppx");
            }
            readParam.setSourceRegion(chunkRect);
            BufferedImage bi = reader.read(0, readParam);
            if (!(bi.getColorModel() instanceof IndexColorModel)) {
                bi = rgbPVtoDDL(bi);
            }
            if (scaleBi) {
                dest = scaleOp.filter(bi, dest);
                final int x1 = (int) (mm[2] * y);
                final int y1 = (int) (mm[3] * y);
                g2.drawImage(dest, x1, y1, null);
            } else {
                g2.drawImage(bi, 0, y, null);
            }
            logMemoryUsage();
        }
    }
    
    private BufferedImage rgbPVtoDDL(BufferedImage bi) {
        final int w = bi.getWidth();
        final int h = bi.getHeight();
        final int[] data = bi.getRGB(0, 0, w, h, null, 0, w);
        if (service.isPrintColorWithPLUT()) {
            int count = 0;
            int shift = pValToDDL.length == 4096 ? 4 : 0;
            for (int rgb, b, i = 0; i < data.length; ++i) {
                b = (rgb = data[i]) & 0xff;
                if (((rgb >> 8) & 0xff) == b && ((rgb >> 16) & 0xff) == b) {
                    b = (pValToDDL[b << shift] & 0xff);
                    data[i] = b | (b << 8) | (b << 16);
                    ++count;
                }
            }
            if (debug) {
                log.debug("Apply P-LUT to " + (count*100.f/(w*h)) + "% [= "
                    + count + " px] of RGB image");
            }
        }
        BufferedImage newbi = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        newbi.setRGB(0, 0, w, h, data, 0, w);
        return newbi;
    }
    

    private long toDestWxH(AffineTransform tx, Rectangle clippedSrcRect)
    {
        double[] srcPts = {
                clippedSrcRect.width,
                clippedSrcRect.height
                };
        double[] dstPts = new double[2];
        tx.deltaTransform(srcPts, 0, dstPts, 0, 1);
        return Math.round(Math.abs(dstPts[0] * dstPts[1]));
    }


    private AffineTransform createInverse(AffineTransform tx)
    {
        try {
            return tx.createInverse();
        } catch (NoninvertibleTransformException e) {
            throw new RuntimeException("Failed to inverse Transform", e);
        }
    }

    private void logMemoryUsage()
    {
        if (debug) {
            Runtime rt = Runtime.getRuntime();
            log.debug("Memory: max=" + (rt.maxMemory() / (float) MEGA_BYTE)
                     + "MB, total=" + (rt.totalMemory() / (float) MEGA_BYTE)
                     + "MB, free=" + (rt.freeMemory() / (float) MEGA_BYTE)
                     + "MB");
        }
    }
}

