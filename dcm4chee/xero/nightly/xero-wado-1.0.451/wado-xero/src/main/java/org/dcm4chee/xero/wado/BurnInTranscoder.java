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

import java.awt.image.BufferedImage;
import java.awt.image.Raster;

import org.apache.batik.transcoder.DefaultErrorHandler;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Takes an existing image, and burns in the SVG on top of the existing image.
 * The selected region specified the area to use - it is assumed that the width/height will
 * be the same as the pre-existing image.
 * @author bwallace
 *
 */
public class BurnInTranscoder extends PNGTranscoder {
   static Logger log = LoggerFactory.getLogger(BurnInTranscoder.class);
   BufferedImage image;
   BufferedImage srcImageRaw;
   
   /** Create a transcoder that transcodes onto a copy of the src iamge */
   public BurnInTranscoder(BufferedImage srcImageRaw) {
	  this.srcImageRaw = srcImageRaw;
	  addTranscodingHint(KEY_WIDTH, new Float(srcImageRaw.getWidth()));
	  addTranscodingHint(KEY_HEIGHT, new Float(srcImageRaw.getHeight()));
	  setErrorHandler(NoErrorHandler.instance);
   }

   /** Create an image with the source being the provided raw src.  Always creates a new image object,
    * as it isn't clear that the srcImageRaw can be safely overwritten.
    */
   @Override
   public BufferedImage createImage(int width, int height) {
	  BufferedImage ret = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
	  int type = srcImageRaw.getType();
	  int[] rgba = new int[width];
	  if( type==BufferedImage.TYPE_BYTE_GRAY ) {
		 log.info("Creating gray to RGB copy.");
		 Raster r = srcImageRaw.getData();
		 byte[] row = new byte[width];
		 for(int y=0; y<height; y++ ) {
			r.getDataElements(0, y, width, 1, row);
			for(int x=0; x<width; x++) {
			   int bits = row[x] & 0xFF;
			   rgba[x] = 0xFF000000 | (bits << 16) | (bits << 8) | bits;
			}
			ret.setRGB(0,y,width,1,rgba, 0, 0);
		 }
	  }
	  else {
		 log.info("Creating existing RGB to ARGB copy.");
		 for(int y=0; y<height; y++) {
			srcImageRaw.getRGB(0,y,width,1,rgba,0,0);
			ret.setRGB(0,y,width,1,rgba,0,0);
		 }
	  }
	  return ret;
   }

   /** Write the image to the output - this does not use the output as the buffered image isn't
    * actually written in that format, but is instead used directly and written out separately.
    */
   @Override
   public void writeImage(BufferedImage image, TranscoderOutput output) throws TranscoderException {
	  // No-op - just store the created image
	  this.image = image;
	  //super.writeImage(image,output);
   }

   /** Returns the destination image */
   public BufferedImage getImage() {
      return image;
   }

}

class NoErrorHandler extends DefaultErrorHandler {
   public static final NoErrorHandler instance = new NoErrorHandler();
   @Override
   public void error(TranscoderException arg0) throws TranscoderException {
	  BurnInTranscoder.log.error(arg0.toString());
   }

   @Override
   public void warning(TranscoderException arg0) throws TranscoderException {
	  BurnInTranscoder.log.warn(arg0.toString());
   }
   
}