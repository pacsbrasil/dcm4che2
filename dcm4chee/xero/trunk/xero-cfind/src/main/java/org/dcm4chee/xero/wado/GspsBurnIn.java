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
import org.dcm4chee.xero.search.SearchFilterUtils;
import org.dcm4chee.xero.search.study.GspsBean;
import org.dcm4chee.xero.search.study.ImageBean;
import org.dcm4chee.xero.search.study.ResultsBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3.svg.GType;
import org.w3.svg.SvgType;
import org.w3.svg.Use;

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
   public WadoImage filter(FilterItem filterItem, Map<String, Object> params) {
	  String presentationUID = (String) params.get("presentationUID");
	  if (presentationUID == null) {
		 log.info("Not burning in presentation state, as none specified.");
		 return (WadoImage) filterItem.callNextFilter(params);
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
	  String svg = generateSvg(image, gsps);
	  if( svg==null ) {
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
    * Transcode the image on top of the existing image, and return a new buffered image
    * containing the combination of the two.
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

   /** Find the WADO image relavant for the given GSPS area/region.
    * 
    * @param filterItem
    * @param params
    * @param image
    * @return
    */
   private WadoImage filterWadoImage(FilterItem filterItem, Map<String, Object> params, ImageBean image) {
	  return (WadoImage) filterItem.callNextFilter(params);
   }

   /** Generate the overall SVG object from the image and GSPS information.  If the gsps contains 
    * no annotations, return null as it is not necessary to run the SVG conversion in that case.
    * @param image
    * @param gsps
    * @return
    */
   private String generateSvg(ImageBean image, GspsBean gsps) {
	  List<Object> elems = image.getOtherElements();
	  if( elems==null || elems.size()==0 ) return null;
	  SvgType gspsSvg = gsps.getSvg();
	  Map<String, GType> used = selectG(gspsSvg);
	  if( used.isEmpty() ) return null;
	  
	  SvgType svg = createSvgFromImport(elems, used);
	  
	  if( svg==null || svg.getChildren().size()==0 ) return null;
	  
	  svg.setHeight(Integer.toString(image.getRows())+"px");
	  svg.setWidth(Integer.toString(image.getColumns())+"px");
	  svg.setViewBox("0 0 " + image.getColumns() + " " + image.getRows());
	  try {
		 JAXBContext context = JAXBContext.newInstance(SvgType.class);
		 Marshaller marshaller = context.createMarshaller();
		 DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		 dbf.setNamespaceAware(true);
		 StringWriter sow = new StringWriter();
		 marshaller.marshal(new JAXBElement<SvgType>(new QName("http://www.w3.org/2000/svg", "svg"), SvgType.class, svg), sow);
		 String strSvg = sow.toString();
		 log.info("SVG=" + strSvg);
		 // Would prefer to return this as a DOMDocument, but Batik barfs on it if you try.
		 return strSvg;
	  } catch (JAXBException e) {
		 e.printStackTrace();
		 return null;
	  }
   }

   /**
    * Create the SVG from the use/importted GSPS objects.
    * @param elems
    * @param used
    * @return
    */
   private SvgType createSvgFromImport(List<Object> elems, Map<String, GType> used) {
	  SvgType svg = new SvgType();
	  for(Object elem : elems) {
		 if( elem instanceof Use ) {
			Use use = (Use) elem;
			GType g = used.get(use.getHref().substring(1));
			if( g==null ) {
			   log.warn("Use key not found:"+use.getHref() );
			   continue;
			}
			if( "DISPLAY".equalsIgnoreCase(g.getClazz())) {
			   log.warn("DISPLAY values not handled yet.");
			   continue;
			}
			svg.getChildren().add(g);
		 }
	  }
	  return svg;
   }

   /**
    * Select the G children elements from the GSPS object, putting them into teh return map
    * by id.
    * @param gspsSvg
    * @return
    */
   private Map<String, GType> selectG(SvgType gspsSvg) {
	  Map<String, GType> used = new HashMap<String,GType>();
	  for(Object child : gspsSvg.getChildren()) {
		 if(child instanceof GType) {
			GType g = (GType) child;
			used.put(g.getId(), g);
		 }
		 else {
			log.warn("Unknown child of SVG type "+child.getClass());
		 }
	  }
	  return used;
   }

}
