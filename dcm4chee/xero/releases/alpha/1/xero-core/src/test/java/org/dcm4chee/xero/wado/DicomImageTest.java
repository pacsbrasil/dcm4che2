package org.dcm4chee.xero.wado;

import static org.dcm4chee.xero.wado.DicomFileLocationFilterTest.callFilter;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;

import org.dcm4chee.xero.metadata.MetaDataBean;
import org.dcm4chee.xero.metadata.StaticMetaData;
import org.testng.annotations.Test;

public class DicomImageTest {
	static MetaDataBean mdb = StaticMetaData.getMetaData("dicom.metadata");

	static final boolean debug = false;

	@Test
	public void imageHeaderReadTest() {
		Object dobj = callFilter("image", "org/dcm4chee/xero/wado/CR3S1IM1.dcm");
		assert dobj != null;
		WadoImage wi = (WadoImage) dobj;
		BufferedImage bi = wi.getValue();
		assert bi != null;
		WritableRaster wr = bi.getRaster();
		assert wr.getNumBands() == 1;
	}

	/** Returns the percentage value of the pixel at x,y */
	protected static double getPercentage(BufferedImage bi, double x, double y) {
		int xi = (int) (51.2 * x);
		int yi = (int) (51.2 * y);
		int rgb = bi.getRGB((int) (51.2 * x), (int) (51.2 * y));
		int b = (rgb & 0xFF);
		if (debug) {
			Object orig = bi.getRaster().getDataElements(xi, yi, null);
			int v = 0;
			int uv = 0;
			if (orig instanceof short[]) {
				v = ((short[]) orig)[0];
				uv = (v & 0xFFFF);
			} else if (orig instanceof byte[]) {
				v = ((byte[]) orig)[0];
				uv = (v & 0xFF);
			}
			System.out.println("Orig(" + xi + "," + yi + ")=" + v + "," + uv
					+ " new " + b);
		}
		return b / 255.0;
	}

	protected void smpteTest(String img) {
		if (debug)
			System.out.println("SMPTE Test on " + img);
		Object dobj = callFilter("image", img);
		assert dobj != null;
		WadoImage wi = (WadoImage) dobj;
		BufferedImage bi = wi.getValue();
		WritableRaster wr = bi.getRaster();
		assert wr.getNumBands() == 1;
		double gray = getPercentage(bi, 2.5, 6.5);
		assert Math.abs(gray) <= 0.01;
		gray = getPercentage(bi, 2.5, 5.5);
		assert Math.abs(gray - 0.1) < 0.01;
		gray = getPercentage(bi, 2.5, 4.5);
		assert Math.abs(gray - 0.2) < 0.01;
		gray = getPercentage(bi, 2.5, 3.5);
		assert Math.abs(gray - 0.3) < 0.01;
		gray = getPercentage(bi, 3.5, 3.5);
		assert Math.abs(gray - 0.4) < 0.01;
		gray = getPercentage(bi, 4.5, 3.5);
		assert Math.abs(gray - 0.5) < 0.01;
		gray = getPercentage(bi, 6.5, 3.5);
		assert Math.abs(gray - 0.6) < 0.01;
		gray = getPercentage(bi, 7.5, 3.5);
		assert Math.abs(gray - 0.7) < 0.01;
		gray = getPercentage(bi, 7.5, 4.5);
		assert Math.abs(gray - 0.8) < 0.01;
		gray = getPercentage(bi, 7.5, 5.5);
		assert Math.abs(gray - 0.9) < 0.01;
		gray = getPercentage(bi, 7.5, 6.5);
		assert Math.abs(gray - 1.0) ==0.0;
	}

	/** Tests mlut from IHE */
	@Test
	public void mlut1Test() {
		smpteTest("org/dcm4chee/xero/wado/mlut_01.dcm");
	}

	/** Tests mlut from IHE */
	@Test
	public void mlut2Test() {
		smpteTest("org/dcm4chee/xero/wado/mlut_02.dcm");
	}

	/** Tests mlut from IHE */
	@Test
	public void mlut3Test() {
		smpteTest("org/dcm4chee/xero/wado/mlut_03.dcm");
	}

	/** Tests mlut from IHE */
	@Test
	public void mlut4Test() {
		smpteTest("org/dcm4chee/xero/wado/mlut_04.dcm");
	}

	/** Tests mlut from IHE */
	@Test
	public void mlut5Test() {
		smpteTest("org/dcm4chee/xero/wado/mlut_05.dcm");
	}

	/** Tests mlut from IHE */
	@Test
	public void mlut6Test() {
		smpteTest("org/dcm4chee/xero/wado/mlut_06.dcm");
	}

	/** Tests mlut from IHE */
	@Test
	public void mlut7Test() {
		smpteTest("org/dcm4chee/xero/wado/mlut_07.dcm");
	}

	/** Tests mlut from IHE */
	@Test
	public void mlut8Test() {
		smpteTest("org/dcm4chee/xero/wado/mlut_08.dcm");
	}

	/** Tests mlut from IHE */
	@Test
	public void mlut9Test() {
		smpteTest("org/dcm4chee/xero/wado/mlut_09.dcm");
	}

	/** Tests mlut from IHE */
	@Test
	public void mlut10Test() {
		smpteTest("org/dcm4chee/xero/wado/mlut_10.dcm");
	}

	/** Tests mlut from IHE */
	@Test
	public void mlut11Test() {
		smpteTest("org/dcm4chee/xero/wado/mlut_11.dcm");
	}

	/** Tests mlut from IHE */
	@Test
	public void mlut12Test() {
		smpteTest("org/dcm4chee/xero/wado/mlut_12.dcm");
	}

	/** Tests mlut from IHE */
	@Test
	public void mlut13Test() {
		smpteTest("org/dcm4chee/xero/wado/mlut_13.dcm");
	}

	/** Tests mlut from IHE */
	@Test
	public void mlut14Test() {
		smpteTest("org/dcm4chee/xero/wado/mlut_14.dcm");
	}

	/** Tests mlut from IHE */
	@Test
	public void mlut15Test() {
		smpteTest("org/dcm4chee/xero/wado/mlut_15.dcm");
	}

	/** Tests mlut from IHE */
	@Test
	public void mlut16Test() {
		smpteTest("org/dcm4chee/xero/wado/mlut_16.dcm");
	}

	/** Tests mlut from IHE */
	@Test
	public void mlut17Test() {
		smpteTest("org/dcm4chee/xero/wado/mlut_17.dcm");
	}

	/** Tests mlut from IHE */
	@Test
	public void mlut18Test() {
		smpteTest("org/dcm4chee/xero/wado/mlut_18.dcm");
	}

	/** Tests mlut from IHE */
	@Test
	public void mlut19Test() {
		smpteTest("org/dcm4chee/xero/wado/mlut_19.dcm");
	}

	/** Tests colour US read */
	@Test
	public void colourTest() {
		Object dobj = callFilter("image", "org/dcm4chee/xero/wado/usColour.dcm");
		assert dobj != null;
		WadoImage wi = (WadoImage) dobj;
		BufferedImage bi = wi.getValue();
		WritableRaster wr = bi.getRaster();
		assert wr.getNumBands() == 3;
	}
}
