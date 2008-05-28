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
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.dcm4chee.xero.metadata.filter.Filter;
import org.dcm4chee.xero.metadata.filter.FilterItem;
import org.dcm4chee.xero.metadata.filter.MemoryCacheFilter;
import org.dcm4chee.xero.search.SearchFilterUtils;
import org.dcm4chee.xero.search.macro.AspectMacro;
import org.dcm4chee.xero.search.macro.FlipRotateMacro;
import org.dcm4chee.xero.search.macro.RegionMacro;
import org.dcm4chee.xero.search.study.GspsBean;
import org.dcm4chee.xero.search.study.ImageBean;
import org.dcm4chee.xero.search.study.ResultsBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3.svg.GType;
import org.w3.svg.SvgType;
import org.w3.svg.Use;

import static org.dcm4chee.xero.metadata.filter.MemoryCacheFilter.removeFromQuery;

import static org.dcm4chee.xero.metadata.filter.FilterUtil.splitFloat;
import static org.dcm4chee.xero.metadata.filter.FilterUtil.getInt;

/**
 * This class burns in GSPS information into the image. This may cause the image
 * to be converted to RGB if any overlay isn't grayscale, and may return larger
 * image than the specified one. This will NOT apply any window levelling or LUT
 * changes - those are applied by a separate filter that manages all that part
 * of the transform.
 * 
 * @author bwallace
 * 
 */
public class GspsBurnIn implements Filter<WadoImage> {
   private static final Logger log = LoggerFactory.getLogger(GspsBurnIn.class);

   /**
     * Burn in GSPS elements if any are found and relevant to the given image.
     */
   public WadoImage filter(FilterItem<WadoImage> filterItem, Map<String, Object> params) {
	  String presentationUID = (String) params.get("presentationUID");
	  if (presentationUID == null) {
		 log.info("Not burning in presentation state, as none specified.");
		 return filterItem.callNextFilter(params);
	  }

	  log.info("Burning in presentation state " + presentationUID);
	  String uid = (String) params.get("objectUID");
	  if (uid == null)
		 return null;
	  ResultsBean results = SearchFilterUtils.filterImage(filterItem, params, uid, presentationUID);
	  if (results == null) {
		 return null;
	  }

	  ImageBean image = (ImageBean) results.getChildren().get(uid);
	  GspsBean gsps = (GspsBean) results.getChildren().get(presentationUID);
	  if (gsps == null) {
		 log.info("Not burning in presentation state, not applicable to this image.");
		 return (WadoImage) filterItem.callNextFilter(params);
	  }

	  WadoImage wi = filterWadoImage(filterItem, params, image);
	  String svg = generateSvg(image, gsps, wi);
	  if (svg == null) {
		 log.info("No overlay data - returning raw image.");
		 return wi;
	  }
	  BufferedImage biSrc = wi.getValue();

	  BufferedImage biBurn = transcodeImage(biSrc, svg);
	  if (biBurn == null) {
		 log.warn("Returned value from SVG image is null.");
		 return wi;
	  }
	  WadoImage ret = wi.clone();
	  ret.setValue(biBurn);
	  return ret;
   }

   /**
     * Transcode the image on top of the existing image, and return a new
     * buffered image containing the combination of the two.
     * 
     * @param biSrc
     * @param svg
     * @return
     */
   private BufferedImage transcodeImage(BufferedImage biSrc, String svg) {
	  try {
		 ByteArrayInputStream bais = new ByteArrayInputStream(svg.getBytes("UTF-8"));
		 TranscoderInput input = new TranscoderInput(bais);
		 ByteArrayOutputStream baos = new ByteArrayOutputStream();
		 TranscoderOutput output = new TranscoderOutput(baos);
		 BurnInTranscoder transcoder = new BurnInTranscoder(biSrc);
		 transcoder.transcode(input, output);
		 return transcoder.getImage();
	  } catch (TranscoderException e) {
		 e.printStackTrace();
		 throw new RuntimeException(e);
	  } catch (UnsupportedEncodingException e) {
		 throw new RuntimeException(e);
	  }
   }

   /**
     * Find the WADO image relavant for the given GSPS area/region.
     * 
     * @param filterItem
     * @param params
     * @param image
     * @return
     */
   private WadoImage filterWadoImage(FilterItem<WadoImage> filterItem, Map<String, Object> params, ImageBean image) {
	  int imgRows = image.getRows();
	  int imgCols = image.getColumns();
	  float left = 0;
	  float right = 1;
	  float top = 0;
	  float bottom = 1;

	  int destRows = getInt(params, "rows");
	  int destCols = getInt(params, "cols");
	  float aspect = 1;
	  AspectMacro aspectMacro = (AspectMacro) image.getMacroItems().findMacro(AspectMacro.class);
	  if (aspectMacro != null) {
		 aspect = aspectMacro.getAspect();
		 log.info("Aspect=" + aspect);
	  }

	  int cols = imgCols;
	  int rows = (int) (imgRows * aspect);

	  boolean flip = getFlip(image);
	  int rot = getRotation(image);

	  // The rows and columns are the destination rows and columns for the
	  // provided region.
	  RegionMacro regionMacro = (RegionMacro) image.getMacroItems().findMacro(RegionMacro.class);
	  if (regionMacro != null) {
		 log.info("topLeft=" + regionMacro.getTopLeft() + " bottomRight=" + regionMacro.getBottomRight() + " imgCols,imgRows="
			   + imgCols + "," + imgRows);
		 float[] topLeft = splitFloat(regionMacro.getTopLeft(), 2);
		 float[] bottomRight = splitFloat(regionMacro.getBottomRight(), 2);
		 // Lots of systems encode topLeft as 1,1, even when they mean 0,0
		 if (topLeft[0] == 1)
			topLeft[0] = 0;
		 if (topLeft[1] == 1)
			topLeft[1] = 0;
		 left = topLeft[0] / imgCols;
		 top = topLeft[1] / imgRows;
		 right = bottomRight[0] / imgCols;
		 bottom = bottomRight[1] / imgRows;
		 if( left<0 ) left = 0;
		 if( top<0 ) top = 0;
		 if( right>1 ) right = 1;
		 if( bottom>1 ) bottom = 1;
		 cols = (int) (imgCols * (right - left));
		 rows = (int) (imgRows * (bottom - top) * aspect);
	  }
	  if (destRows > 0 && destRows < rows) {
		 cols = (int) (destRows * cols / rows);
		 rows = destRows;
		 log.info("Cutting number of columns/rows to " + cols + "," + rows);
	  }
	  // The second one can also be done if both values are provided, and
	  // it makes the destination area smaller.
	  if (destCols > 0 && destCols < cols) {
		 rows = (int) (destCols * rows / cols);
		 cols = destCols;
		 log.info("Cutting number of columns/rows to " + cols + "," + rows);
	  }
	  log.info("Final cols,rows = " + cols + "," + rows);

	  String region = "" + left + "," + top + "," + right + "," + bottom;
	  log.info("Burning in region " + region);
	  removeFromQuery(params, "region", "fip", "rotation", "rows", "cols");
	  StringBuffer queryStr = new StringBuffer((String) params.get(MemoryCacheFilter.KEY_NAME));
	  params.put("region", region);
	  params.put("rows", rows);
	  params.put("cols", cols);
	  queryStr.append("&region=").append(region).append("&rows=").append(rows).append("&cols=").append(cols);
	  if (flip) {
		 params.put("flip", true);
		 queryStr.append("&flip=true");
		 log.info("Flipping returned image.");
	  }
	  if (rot != 0) {
		 params.put("rotation", rot);
		 queryStr.append("&rotation=").append(rot);
		 log.info("Rotating returned image " + rot);
	  }
	  params.put(MemoryCacheFilter.KEY_NAME, queryStr.toString());

	  WadoImage ret = filterItem.callNextFilter(params);
	  return ret;
   }

   /**
     * Generate the overall SVG object from the image and GSPS information. If
     * the gsps contains no annotations, return null as it is not necessary to
     * run the SVG conversion in that case.
     * 
     * @param image
     * @param gsps
     * @return
     */
   private String generateSvg(ImageBean image, GspsBean gsps, WadoImage wadoImage) {
	  List<Object> elems = image.getOtherElements();
	  if (elems == null || elems.size() == 0)
		 return null;
	  SvgType gspsSvg = gsps.getSvg();
	  Map<String, GType> used = selectG(gspsSvg);
	  if (used.isEmpty())
		 return null;

	  GType svgImage = createSvgFromImport(elems, used, null);
	  GType svgDisplay = createSvgFromImport(elems, used, "DISPLAY");

	  if ((svgImage == null || svgImage.getChildren().size() == 0) && (svgDisplay == null || svgDisplay.getChildren().size() == 0))
		 return null;
	  SvgType svg = new SvgType();
	  svg.getChildren().add(svgImage);
	  svg.getChildren().add(svgDisplay);

	  int width = wadoImage.getValue().getWidth();
	  int height = wadoImage.getValue().getHeight();
	  svg.setWidth(Integer.toString(width) + "px");
	  svg.setHeight(Integer.toString(height) + "px");
	  svg.setViewBox("0,0," + width + "," + height);

	  svgDisplay.setTransform("scale(" + (width / 1000.0) + "," + (height / 1000.0) + ")");

	  String transform = (String) wadoImage.getParameter(ScaleFilter.SVG_TRANSFORM);
	  if (transform != null) {
		 svgImage.setTransform(transform);
	  }

	  try {
		 JAXBContext context = JAXBContext.newInstance(SvgType.class);
		 Marshaller marshaller = context.createMarshaller();
		 DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		 dbf.setNamespaceAware(true);
		 StringWriter sow = new StringWriter();
		 marshaller.marshal(new JAXBElement<SvgType>(new QName("http://www.w3.org/2000/svg", "svg"), SvgType.class, svg), sow);
		 String strSvg = sow.toString();
		 log.info("SVG=" + strSvg);
		 // Would prefer to return this as a DOMDocument, but Batik barfs on
		 // it if you try.
		 return strSvg;
	  } catch (JAXBException e) {
		 e.printStackTrace();
		 return null;
	  }
   }

   /** Get the rotation from the image bean object */
   public static int getRotation(ImageBean image) {
	  FlipRotateMacro flipRotateMacro = (FlipRotateMacro) image.getMacroItems().findMacro(FlipRotateMacro.class);
	  if (flipRotateMacro == null)
		 return 0;
	  return flipRotateMacro.getRotation();
   }

   /** Get the flip from the image bean object */
   public static boolean getFlip(ImageBean image) {
	  FlipRotateMacro flipRotateMacro = (FlipRotateMacro) image.getMacroItems().findMacro(FlipRotateMacro.class);
	  if (flipRotateMacro == null)
		 return false;
	  return flipRotateMacro.getFlip();
   }

   /**
     * Create the SVG from the use/importted GSPS objects.
     * 
     * @param elems
     * @param used
     * @return
     */
   private GType createSvgFromImport(List<Object> elems, Map<String, GType> used, String clazz) {
	  GType svg = new GType();
	  for (Object elem : elems) {
		 if (elem instanceof Use) {
			Use use = (Use) elem;
			GType g = used.get(use.getHref().substring(1));
			if (g == null) {
			   log.warn("Use key not found:" + use.getHref());
			   continue;
			}
			if (!(g.getClazz() == clazz || clazz != null && clazz.equalsIgnoreCase(g.getClazz()))) {
			   continue;
			}
			svg.getChildren().add(g);
		 }
	  }
	  return svg;
   }

   /**
     * Select the G children elements from the GSPS object, putting them into
     * teh return map by id.
     * 
     * @param gspsSvg
     * @return
     */
   private Map<String, GType> selectG(SvgType gspsSvg) {
	  Map<String, GType> used = new HashMap<String, GType>();
	  for (Object child : gspsSvg.getChildren()) {
		 if (child instanceof GType) {
			GType g = (GType) child;
			used.put(g.getId(), g);
		 } else {
			log.warn("Unknown child of SVG type " + child.getClass());
		 }
	  }
	  return used;
   }

}
