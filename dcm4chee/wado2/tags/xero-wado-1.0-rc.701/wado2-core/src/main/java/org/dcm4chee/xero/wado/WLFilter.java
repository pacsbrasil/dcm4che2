/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is part of dcm4che, an implementation of DICOM(TM) in
 * Java(TM), hosted at http://sourceforge.net/projects/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * Bill Wallace, Agfa HealthCare Inc., 
 * Portions created by the Initial Developer are Copyright (C) 2007
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Bill Wallace <bill.wallace@agfa.com>
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 *
 * ***** END LICENSE BLOCK ***** */
package org.dcm4chee.xero.wado;

import static org.dcm4chee.xero.metadata.servlet.MetaDataServlet.nanoTimeToString;
import static org.dcm4chee.xero.wado.WadoImage.WINDOW_CENTER;
import static org.dcm4chee.xero.wado.WadoImage.WINDOW_WIDTH;
import static org.dcm4chee.xero.wado.WadoParams.FRAME_NUMBER;

import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.util.Map;

import org.dcm4che2.data.BasicDicomObject;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.image.LookupTable;
import org.dcm4che2.image.VOIUtils;
import org.dcm4chee.xero.metadata.MetaData;
import org.dcm4chee.xero.metadata.filter.Filter;
import org.dcm4chee.xero.metadata.filter.FilterItem;
import org.dcm4chee.xero.metadata.filter.FilterUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class applies the DICOM window level operation directly to the pixel
 * data transform, so that no new copy of the data is required.
 * 
 * @author bwallace
 * 
 */
public class WLFilter implements Filter<WadoImage> {
   private static Logger log = LoggerFactory.getLogger(WLFilter.class);

   ColorSpace csGray = ColorSpace.getInstance(ColorSpace.CS_GRAY);

   public static final double MIN_WIDTH = 0.0001;

   /**
    * Used to specify a presentation UID to apply to an image
    */
   public static String PRESENTATION_UID = "presentationUID";
   
   /** Used to flag inverting the image display to true/false.  Modifes the value
    * in the GSPS or image.
    */
   public static String INVERT = "invert";
   
   private static short[] pval2out_inverse = new short[256];
   static {
	 for(int i=0; i<256; i++ )pval2out_inverse[i] = (short) ((255-i) << 8); 
   };

   public WadoImage filter(FilterItem<WadoImage> filterItem, Map<String, Object> params) {
	  Object[] values = FilterUtil.removeFromQuery(params, WINDOW_WIDTH, WINDOW_CENTER, PRESENTATION_UID, INVERT, WadoParams.WINDOW_FUNCTION);

	  WadoImage wi = filterItem.callNextFilter(params);
	  if (wi == null) {
		 log.debug("No wado image found.");
		 return null;
	  }
	  if (wi.getDicomObject() == null) {
		 log.debug("Skipping window levelling as no dicom object header found.");
		 return wi;
	  }
	  if( wi.hasError() ) return wi;
	  
	  if( FilterUtil.getInt(params,EncodeImage.MAX_BITS)!=0 ) {
		 log.info("Skipping window level as specific bit depth is being requested - see ReduceBitsFilter");
		 return wi;
	  }
	  
	  long start = System.nanoTime();
	  BufferedImage bi = wi.getValue();

	  short[] pval2out = null;
	  if( "true".equalsIgnoreCase((String) values[3]) ) {
		 pval2out = pval2out_inverse;
	  }
	  
	  // The lookup op, either computed by hand, or retrieved from the colour
	  // model.
	  float windowWidth = 0;
	  float windowCenter = 0;
	  String func = null;
	  try {
		 if (values[0] != null && values[1] != null) {
			windowWidth = Float.parseFloat((String) values[0]);
			windowCenter = Float.parseFloat((String) values[1]);
			func = LookupTable.LINEAR;
			if( values[4]!=null ) {
			    func = (String) values[4];
			    if( func.equalsIgnoreCase("LINEAR")) func = LookupTable.LINEAR;
			    else if( func.equalsIgnoreCase("SIGMOID")) func = LookupTable.SIGMOID;
			    else {
			        log.warn("Unknown window level type:"+func);
			    }
			}
		 }
	  } catch (NumberFormatException nfe) {
		 log.warn("Caught number format exception, ignoring and using default values:" + nfe);
	  }
	  log.debug("WL  W:" + windowWidth + " C:" + windowCenter + ")");
	  
	  DicomObject img = wi.getDicomObject();
	  if( img==null ) {
		 log.error("Dicom object must not be null for window-level to work.");
		 return wi;
	  }
	  DicomObject pr = null;
	  if( values[2]!=null ) {
		 // Only image type attributes are required, so filter the image dicomobject.
		 pr = DicomFilter.callInstanceFilter(dicomImageHeader, params, (String) values[2]);
		 if( pr==null ) log.warn("Coudn't find presentation state "+values[2]);
	  }

	  LookupTable lut;

	  int frame = FilterUtil.getInt(params,FRAME_NUMBER,1);
	  BufferedImage dest = createCompatible8BitImage(bi);
	  // Can't window level if a custom type is being used.
	  if( dest==null ) return wi;

	  DicomObject voiObj = VOIUtils.selectVoiObject(img,pr,frame);
	  // Figure out the WL to use - if the width isn't at least 2 or isn't found,
	  // then compute the value as a width of under 2 doesn't make sense.
	  if( func==null && (voiObj==null || (voiObj.getFloat(Tag.WindowWidth)<2 && 
	          !voiObj.contains(Tag.VOILUTSequence)))) {
		 float[] cw = VOIUtils.getMinMaxWindowCenterWidth(img, pr, frame, bi.getRaster());
		 windowCenter = cw[0]+0.5f;
		 windowWidth = cw[1];
		 //log.info("Original c/w={},{}", windowCenter, windowWidth);
		 if( windowWidth < 2 ) windowWidth = 2;
         //log.info("Final c/w={},{}", windowCenter, windowWidth);
		 func = LookupTable.LINEAR;
	  }

	  lut = localCreateLutForImageWithPR(wi, pr, frame, windowCenter, windowWidth, func, 8, pval2out);
	
	  //if (lut != null) {
		// for (int i = 0; i < lut.length(); i += 1 + (lut.length() / 10)) {
		//	int v = i + lut.getOffset();
		//	System.out.println("LUT maps " + v + " to " + lut.lookup(v));
		// }
	  //} else {
		// System.out.println("Null LUT.");
	  //}

	  if (lut != null){
		 lut.lookup( bi.getRaster(), dest.getRaster() );
	  }

	  WadoImage ret = wi.clone();
	  ret.setValue(dest);
	  long dur = System.nanoTime() - start;
	  log.info("Window levelling took " + nanoTimeToString(dur));
	  return ret;
   }
   
   private static boolean isInverse(DicomObject img) {
       String shape = img.getString(Tag.PresentationLUTShape);
       return shape != null ? "INVERSE".equals(shape) : "MONOCHROME1"
               .equals(img.getString(Tag.PhotometricInterpretation));
   }
   
   public static LookupTable localCreateLutForImageWithPR(
		   WadoImage wi, DicomObject pr, int frame, float center, float width, String vlutFct, int outBits, short[] pval2out) {
	   
	   int reducedBits = ReduceBitsFilter.getPreviousReducedBits(wi);
	   int smallestPixelValue = ReduceBitsFilter.getPreviousSmallestPixelValue(wi);
	   int largestPixelValue = ReduceBitsFilter.getPreviousLargestPixelValue(wi);
	   
	   DicomObject img = wi.getDicomObject();
	   DicomObject mlutObj = VOIUtils.selectModalityLUTObject(img, pr, frame);
	   DicomObject voiObj = VOIUtils.selectVoiObject(img, pr, frame);

	   if ( reducedBits >0 ) {
	        float slope = mlutObj.getFloat(Tag.RescaleSlope, 1.f);
	        float intercept = mlutObj.getFloat(Tag.RescaleIntercept, 0.f);
	    	
	        DicomObject mLut = mlutObj.getNestedDicomObject(Tag.ModalityLUTSequence);
	        if ( mLut != null ) {
	        	log.warn("Data has reduced bits, as well as a ModalityLutSequence. This isn't supported yet.  Image pixel values may be corrupted.");
	        } else {
	        	double sourceRange = ( largestPixelValue - smallestPixelValue );
	        	double mult = sourceRange / (double)((1<<reducedBits)-1);
	        	double newSlope = (mult * slope);
	        	double newIntercept = slope * smallestPixelValue + intercept;
	        	
	        	mlutObj = new BasicDicomObject();
	        	mlutObj.putFloat(Tag.RescaleSlope, null, (float)newSlope);
	        	mlutObj.putFloat(Tag.RescaleIntercept, null, (float)newIntercept);
	        }
	        img = new BasicDicomObject(img);
	        //TODO: should we figure out BitsAllocated from the BufferedImage in the WadoImage?
	        img.putInt(Tag.BitsStored, null, reducedBits);
	        img.putInt(Tag.HighBit, null, reducedBits-1);
	        img.putInt(Tag.PixelRepresentation, null, 0);
	   }
	   
	   boolean inverse;
	   if( pr!=null ) {
		   inverse = "INVERSE".equals(pr.getString(Tag.PresentationLUTShape));
	   }
	   else {
		   inverse = isInverse(img);
	   }
	   DicomObject pLut = pr!=null ? pr.getNestedDicomObject(Tag.PresentationLUTSequence) : null;
	   return LookupTable.createLutForImage(img, mlutObj, voiObj, pLut, center, width, vlutFct, inverse, outBits, pval2out);
   }


   /**
     * Returns the number of bits per pixel, assuming the values are constant
     * and uniform. WARNING: If more standard types are defined that are
     * indexed, or anything where luminance isn't evenly spread across channels,
     * then this will need to be updated.
     * 
     * @param bi
     *            to get bits per pixel from.
     * @return number of bits per pixel, assumign they are all the same
     *         (excluding alpha), or 0 otherwise.
     */
   public static int getBitsPerPixel(BufferedImage bi) {
	  if (bi == null)
		 throw new NullPointerException("Buffered image must not be null.");
	  ColorModel cm = bi.getColorModel();
	  int type = bi.getType();
	  if (type == BufferedImage.TYPE_CUSTOM || type == BufferedImage.TYPE_BYTE_INDEXED || type == BufferedImage.TYPE_USHORT_565_RGB)
		 return 0;
	  if (cm.getNumComponents() == 1) {
		 return cm.getComponentSize(0);
	  }
	  // The second component is never the alpha, so it should be safe to use.
	  return cm.getComponentSize(1);
   }

   /**
     * This method creates an 8 bit compatible, default buffered image. Only
     * gray and RGB images are currently supported.
     * 
     * @param src
     *            is the image data to start with and to create the window
     *            levelled image from.
     */
   protected BufferedImage createCompatible8BitImage(BufferedImage src) {
	  int type = src.getType();
	  if (src.getColorModel().getNumComponents() == 1) {
		 return new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
	  } else if( type==BufferedImage.TYPE_INT_RGB || type==BufferedImage.TYPE_3BYTE_BGR ) {
		 return new BufferedImage(src.getWidth(), src.getHeight(), src.getType());
	  }
	  
	  log.warn("Unsupported buffered image type {}",createImageDescription(src));
	  return null;
   }

   /**
    * Create a description of the BufferedImage type so that we can identify it.
    */
   private Object createImageDescription(BufferedImage src)
   {
      StringBuilder sb = new StringBuilder();
      
      sb.append("ImageType=");
      sb.append(src.getType() == 0 ? "CustomImage" : "KnownImage");
      sb.append(", PixelSize=");
      sb.append(src.getColorModel().getPixelSize());
      sb.append(", ComponentSizes=(");
      for(int i : src.getColorModel().getComponentSize())
      {
         sb.append(i);
         sb.append(' ');
      }
      sb.append(")");
      
      return sb.toString();
   }

   
   private Filter<DicomObject> dicomImageHeader;

   /** Gets the filter that returns the dicom object image header */
	public Filter<DicomObject> getDicomImageHeader() {
   	return dicomImageHeader;
   }

	@MetaData(out="${ref:dicomImageHeader}")
	public void setDicomImageHeader(Filter<DicomObject> dicomImageHeader) {
   	this.dicomImageHeader = dicomImageHeader;
   }
}
