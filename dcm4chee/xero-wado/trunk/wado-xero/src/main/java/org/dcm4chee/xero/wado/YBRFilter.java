package org.dcm4chee.xero.wado;

import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.util.Map;

import org.dcm4che2.image.SimpleYBRColorSpace;
import org.dcm4chee.xero.metadata.filter.Filter;
import org.dcm4chee.xero.metadata.filter.FilterItem;
import org.dcm4chee.xero.metadata.filter.FilterUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Fixes the YBR color Space Images, assuming that "raw" images never need to be
 * fixed as they are being requested already in the correct space.
 */
public class YBRFilter implements Filter<WadoImage> {
    private static final Logger log = LoggerFactory.getLogger(YBRFilter.class);

    public WadoImage filter(FilterItem<WadoImage> filterItem, Map<String, Object> params) {
        int bits = FilterUtil.getInt(params, EncodeImage.MAX_BITS);
        if (bits != 0) {
            log.debug("Not applying YBR filter as an original image is required");
            return filterItem.callNextFilter(params);
        }
        WadoImage wi = filterItem.callNextFilter(params);
        BufferedImage bi = wi.getValue();
        if (bi == null)
            return wi;
        ColorModel cm = bi.getColorModel();
        if (cm.getNumComponents() != 3)
            return wi;
        ColorSpace cs = cm.getColorSpace();
        if (!(cs instanceof SimpleYBRColorSpace))
            return wi;
        log.info("Converting YBR type image to RGB type image.");
        WadoImage ret = wi.clone();
        BufferedImage biRet = new BufferedImage(bi.getWidth(), bi.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
        convertColorSpaceToRGB(bi, biRet);
        ret.setValue(biRet);
        return ret;
    }

    /**
     * This method uses the source colour space to allow conversion to RGB from
     * YBR CS
     */
    private void convertColorSpaceToRGB(BufferedImage src, BufferedImage dest) {
        int w = src.getWidth();
        int h = src.getHeight();

        int[] srcRgb = new int[w];
        log.info("Converting image " + src.getColorModel().getColorSpace() + " to " + dest.getColorModel().getColorSpace());
        for (int iy = 0; iy < h; iy++) {
            int[] destRgb = src.getRGB(0, iy, w, 1, srcRgb, 0, w);
            dest.setRGB(0, iy, w, 1, destRgb, 0, w);
        }
    }

}
