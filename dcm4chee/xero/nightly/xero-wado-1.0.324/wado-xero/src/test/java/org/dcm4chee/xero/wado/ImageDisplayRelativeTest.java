package org.dcm4chee.xero.wado;

import static org.dcm4chee.xero.wado.DicomFileLocationFilterTest.callFilter;

import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import org.dcm4chee.imagetest.ImageDiff;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

/**
 * Tests that retrieving the image relative and display relative objects.
 * This includes pixel padding, embedded overlays, other overlays and display relative
 * markup for image relative (and none of course)
 * 
 * For display relative, it tests annotation and display relative markup (and none)
 * 
 * @author bwallace
 */
public class ImageDisplayRelativeTest {
	private static Logger log = LoggerFactory.getLogger(ImageDisplayRelativeTest.class);
	static ClassLoader cl = Thread.currentThread().getContextClassLoader();
	static File fdir = new File("target/surefire-reports/imageDiff");
	static {
		fdir.mkdirs();
		log.info("Save directory is {}", fdir);
	};
	static String dir = fdir.getAbsolutePath();
	
	/** Tests that null response is provided when no image is expected. */
	@Test
	public void emptyImageRelativeTest() throws Exception {
		Map<String,Object> params =new HashMap<String,Object>();
		params.put("relative", "image");
		params.put("pixelPadding", "true");
		params.put("overlay", "true");
		WadoImage wi = (WadoImage) callFilter("relative", "imgconsistency/disa_p01.dcm", params);
		assert wi==null;
	}

	/** Tests that null response is provided when no image is expected. */
	@Test
	public void gspsOverlayTest() throws Exception {
		Map<String,Object> params =new HashMap<String,Object>();
		params.put("relative", "image");
		params.put("pixelPadding", "true");
		params.put("overlay", ":7F7F7F");
		WadoImage wi = (WadoImage) callFilter("relative", "imgconsistency/ovly_p01.pre", params);
		assert wi!=null;
		BufferedImage bi = wi.getValue();
		
		// Check some things about the returned value first.
		assert bi!=null;
		assert bi.getWidth()==512;
		assert bi.getHeight()==512;
		IndexColorModel icm = (IndexColorModel) bi.getColorModel();
		assert icm.getMapSize()==3;
		
		BufferedImage biBase = ImageIO.read(cl.getResource("imgconsistency/ovly8A.png"));
		ImageDiff id = new ImageDiff(biBase,bi,dir+"/ovly8A",0);
		assert id.getMaxDiff()==0;
		

	}

	/** Tests extraction of the pixel padding, in red. */
	@Test
	public void extractPaddingTest() throws Exception {
		Map<String,Object> params =new HashMap<String,Object>();
		params.put("relative", "image");
		params.put("padding", ":FF0000");
		WadoImage wi = (WadoImage) callFilter("relative", "misc/pixelPadding.dcm", params);
		assert wi!=null;
		BufferedImage bi = wi.getValue();
		
		// Check some things about the returned value first.
		assert bi!=null;
		assert bi.getWidth()==256;
		assert bi.getHeight()==256;
		assert bi.getColorModel() instanceof IndexColorModel;
		
		BufferedImage biBase = ImageIO.read(cl.getResource("misc/redPixelPadding.png"));
		ImageDiff id = new ImageDiff(biBase,bi,dir+"/redPixelPadding",0);
		assert id.getMaxDiff()==0;
	}
	
	/** Tests extraction of the specified overlays - every other overlay in green and blue. */
	@Test
	public void extractOverlayTest() throws Exception {
		Map<String,Object> params =new HashMap<String,Object>();
		params.put("relative", "image");
		// Ask for pixel padding in red, but in fact there isn't any.
		params.put("padding", ":FF0000");
		// Ask for 1 embedded and 1 header based overlay.
		params.put("overlay", "0:FF00,4:FF");
		WadoImage wi = (WadoImage) callFilter("relative", "imgconsistency/ovly_p01.dcm", params);
		assert wi!=null;
		BufferedImage bi = wi.getValue();
		
		// Check some things about the returned value first.
		assert bi!=null;
		assert bi.getWidth()==512;
		assert bi.getHeight()==512;
		IndexColorModel icm = (IndexColorModel) bi.getColorModel();
		assert icm.getMapSize()==3;
		
		BufferedImage biBase = ImageIO.read(cl.getResource("imgconsistency/ovly0g4b.png"));
		ImageDiff id = new ImageDiff(biBase,bi,dir+"/ovly0g4b",0);
		assert id.getMaxDiff()==0;
		
	}
}
