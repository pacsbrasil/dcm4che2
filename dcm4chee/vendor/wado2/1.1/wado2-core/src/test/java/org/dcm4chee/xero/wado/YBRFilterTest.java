package org.dcm4chee.xero.wado;

import java.awt.Point;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;

import org.dcm4che2.image.SimpleYBRColorSpace;
import org.dcm4chee.xero.metadata.filter.FilterItem;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.awt.image.DataBuffer;
import java.awt.Transparency;
import java.util.HashMap;
import java.util.Map;

import static org.easymock.EasyMock.expect;
import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.replay;
import static org.easymock.classextension.EasyMock.verify;

/**
 * Tests that YBR images are converted to RGB successfully.
 * 
 * @author bwallace
 */
public class YBRFilterTest {

    public static YBRFilter ybrFilter = new YBRFilter();
    public static BufferedImage ybrImage;
    public static BufferedImage rgbImage;
    public static ColorSpace ybrCS = SimpleYBRColorSpace.createYBRFullColorSpace(ColorSpace.getInstance(ColorSpace.CS_sRGB));
    public static ColorModel ybrCM = new ComponentColorModel(ybrCS, new int[]{8,8,8}, false, false, Transparency.OPAQUE, DataBuffer.TYPE_BYTE);
    public FilterItem<WadoImage> filterItem;
    public Map<String,Object> params;
    
    @BeforeTest
    public void initStatic() {
        rgbImage = new BufferedImage(8,8,BufferedImage.TYPE_INT_RGB);
        WritableRaster r = Raster.createInterleavedRaster(DataBuffer.TYPE_BYTE, 8, 8, 3, new Point());
        ybrImage = new BufferedImage(ybrCM, r, false, null);
        for(int y=0; y<8; y++) {
            for(int x=0; x<8; x++) {
               int iArray[] = new int[]{x*32,y*32,(x+y)*16 };
               r.setPixel(x, y, iArray);
            }
        }
    }
    
    @SuppressWarnings("unchecked")
    @BeforeMethod
    public void init() {
        filterItem = createMock(FilterItem.class);
        params = new HashMap<String,Object>();
        params.put(EncodeImage.MAX_BITS,0);
    }
    
    @Test
    public void test_alreadyRGB_noChange() {
        WadoImage wi = new WadoImage(null,8, rgbImage);
        expect(filterItem.callNextFilter(params)).andReturn(wi);
        replay(filterItem);
        WadoImage ret = ybrFilter.filter(filterItem, params);
        assert wi==ret;
        verify(filterItem);
    }

    @Test
    public void test_grayscale_noChange() {
        WadoImage wi = new WadoImage(null,8, new BufferedImage(8,8,BufferedImage.TYPE_BYTE_GRAY));
        expect(filterItem.callNextFilter(params)).andReturn(wi);
        replay(filterItem);
        WadoImage ret = ybrFilter.filter(filterItem, params);
        assert wi==ret;
        verify(filterItem);
    }

    @Test
    public void test_ybr_changed() {
        WadoImage wi = new WadoImage(null,8, ybrImage);
        expect(filterItem.callNextFilter(params)).andReturn(wi);
        replay(filterItem);
        WadoImage ret = ybrFilter.filter(filterItem, params);
        assert wi!=ret;
        assert ret.getValue()!=ybrImage;
        BufferedImage bi = ret.getValue();
        WritableRaster r= bi.getRaster();
        int[] iArray = new int[3];
        iArray = r.getPixel(7,6, iArray);
        assert iArray[0]!=7*32;
        assert iArray[1]!=6*32;
        assert iArray[2]!=13*16;
        verify(filterItem);
    }
}
