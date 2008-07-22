package org.dcm4chee.xero.wado;

import static org.dcm4chee.xero.wado.DicomFileLocationFilterTest.callFilter;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

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
   protected static double getPercentage(BufferedImage bi, double x, double y, WadoImage origData) {
	  if (debug)
		 System.out.println("Size of image is " + bi.getWidth() + "," + bi.getHeight());
	  int xi = (int) (51.2 * x);
	  int yi = (int) (51.2 * y);
	  Object rawB = bi.getRaster().getDataElements(xi, yi, null);
	  int b;
	  double ret;
	  int bits;
	  if( rawB instanceof short[] ) {
		 b = ((short[]) rawB)[0] & 0xFFFF;
		 bits = 16;
	  }
	  else {
		 b = ((byte[]) rawB)[0] & 0xFF;
		 bits = 8;
	  }
	  double div = ((1 << bits)-1);
	  ret = b/div;
	  if (origData!=null ) {
		 Object rawOrig = origData.getValue().getRaster().getDataElements(xi,yi,null); 
		 int v = 0;
		 int uv = 0;
		 if (rawOrig instanceof short[]) {
			v = ((short[]) rawOrig)[0];
			uv = (v & 0xFFFF);
		 } else if (rawOrig instanceof byte[]) {
			v = ((byte[]) rawOrig)[0];
			uv = (v & 0xFF);
		 }
		 System.out.println("Orig(" + xi + "," + yi + ")=" + v + "," + uv + " new " + b + " div "+div);
	  }
	  return ret;
   }

   void testPercentage(BufferedImage bi, double x, double y, double percentage, WadoImage origData) {
	  double gray = getPercentage(bi, x, y, origData);
	  if (Math.abs(gray - percentage) >= 0.03) {
		 System.err.println("Value at " + x + "," + y + " is " + gray + " but is supposed to be " + percentage);
		 if (!debug)
			assert false;
	  }
   }

   protected void smpteTest(String img) {
	  String fname = "org/dcm4chee/xero/wado/" + img + ".dcm";
	  if (debug)
		 System.out.println("SMPTE Test on " + fname);
	  Object dobj = callFilter("wlimage", fname);
	  WadoImage origData = null;
	  if( debug ) {
		 origData =  (WadoImage) callFilter("image", fname);
	  }
	  assert dobj != null;
	  WadoImage wi = (WadoImage) dobj;
	  BufferedImage bi = wi.getValue();
	  WritableRaster wr = bi.getRaster();
	  assert wr.getNumBands() == 1;
	  testPercentage(bi, 2.5, 6.5, 0, origData);
	  testPercentage(bi, 2.5, 5.5, 0.1, origData);
	  testPercentage(bi, 2.5, 4.5, 0.2, origData);
	  testPercentage(bi, 2.5, 3.5, 0.3, origData);
	  testPercentage(bi, 3.5, 3.5, 0.4, origData);
	  testPercentage(bi, 4.5, 3.5, 0.5, origData);
	  testPercentage(bi, 6.5, 3.5, 0.6, origData);
	  testPercentage(bi, 7.5, 3.5, 0.7, origData);
	  testPercentage(bi, 7.5, 4.5, 0.8, origData);
	  testPercentage(bi, 7.5, 5.5, 0.9, origData);
	  testPercentage(bi, 7.5, 6.5, 1.0, origData);

	  try {
		 if( debug ) ImageIO.write(bi, "jpeg", new File("C:/temp/" + img + ".jpg"));
	  } catch (IOException e) {
		 e.printStackTrace();
	  }
   }

   /** Tests mlut from IHE */
   @Test
   public void mlut1Test() {
	  smpteTest("mlut_01");
   }

   /** Tests mlut from IHE */
   @Test
   public void mlut2Test() {
	  smpteTest("mlut_02");
   }

   /** Tests mlut from IHE */
   @Test
   public void mlut3Test() {
	  smpteTest("mlut_03");
   }

   /** Tests mlut from IHE */
   @Test
   public void mlut4Test() {
	  smpteTest("mlut_04");
   }

   /** Tests mlut from IHE */
   @Test
   public void mlut5Test() {
	  smpteTest("mlut_05");
   }

   /** Tests mlut from IHE */
   @Test
   public void mlut6Test() {
	  smpteTest("mlut_06");
   }

   /** Tests mlut from IHE */
   @Test
   public void mlut7Test() {
	  smpteTest("mlut_07");
   }

   /** Tests mlut from IHE */
   @Test
   public void mlut8Test() {
	  smpteTest("mlut_08");
   }

   /** Tests mlut from IHE */
   @Test
   public void mlut9Test() {
	  smpteTest("mlut_09");
   }

   /** Tests mlut from IHE */
   //@Test
   public void mlut10Test() {
	  smpteTest("mlut_10");
   }

   /** Tests mlut from IHE */
   @Test
   public void mlut11Test() {
	  smpteTest("mlut_11");
   }

   /** Tests mlut from IHE */
   @Test
   public void mlut12Test() {
	  smpteTest("mlut_12");
   }

   /** Tests mlut from IHE */
   @Test
   public void mlut13Test() {
	  smpteTest("mlut_13");
   }

   /** Tests mlut from IHE */
   @Test
   public void mlut14Test() {
	  smpteTest("mlut_14");
   }

   /** Tests mlut from IHE */
   @Test
   public void mlut15Test() {
	  smpteTest("mlut_15");
   }

   /** Tests mlut from IHE */
   @Test
   public void mlut16Test() {
	  smpteTest("mlut_16");
   }

   /** Tests mlut from IHE */
   @Test
   public void mlut17Test() {
	  smpteTest("mlut_17");
   }

   /** Tests mlut from IHE */
   @Test
   public void mlut18Test() {
	  smpteTest("mlut_18");
   }

   /** Tests mlut from IHE */
   @Test
   public void mlut19Test() {
	  smpteTest("mlut_19");
   }
   
   /** Tests vlut 10 from IHE */
   @Test
   public void vlut10Test() {
	  smpteTest("vlut_10");
   }

   
   /** Tests vlut 3 from IHE */
   @Test
   public void vlut03Test() {
	  smpteTest("vlut_03");
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
