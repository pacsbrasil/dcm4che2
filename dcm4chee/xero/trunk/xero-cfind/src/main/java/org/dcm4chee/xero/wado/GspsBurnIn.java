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
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

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
import org.w3.svg.PathType;
import org.w3.svg.SvgType;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

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

   private BufferedImage transcodeImage(BufferedImage biSrc, String svg) {
	  try {
		 DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		 DocumentBuilder db = dbf.newDocumentBuilder();
		 ByteArrayInputStream bais = new ByteArrayInputStream(svg.getBytes("UTF-8"));
		 //InputSource is = new InputSource( bais );
		 //Document doc = db.parse(is);
		 TranscoderInput input = new TranscoderInput(bais);
		 ByteArrayOutputStream baos = new ByteArrayOutputStream();
		 TranscoderOutput output = new TranscoderOutput(baos);
		 BurnInTranscoder transcoder = new BurnInTranscoder(biSrc);
		 transcoder.transcode(input, output);
		 return transcoder.getImage();
	  } catch (TranscoderException e) {
		e.printStackTrace();
		throw new RuntimeException(e);
	  } catch (ParserConfigurationException e) {
		throw new RuntimeException(e);
	  } catch (UnsupportedEncodingException e) {
		throw new RuntimeException(e);
	  } catch (IOException e) {
		e.printStackTrace();
		throw new RuntimeException(e);
	  }
   }

   private WadoImage filterWadoImage(FilterItem filterItem, Map<String, Object> params, ImageBean image) {
	  return (WadoImage) filterItem.callNextFilter(params);
   }

   private String generateSvg(ImageBean image, GspsBean gsps) {
	  SvgType svg = new SvgType();
	  svg.setHeight(Integer.toString(image.getRows())+"px");
	  svg.setWidth(Integer.toString(image.getColumns())+"px");
	  svg.setViewBox("0 0 " + image.getColumns() + " " + image.getRows());
	  PathType path = new PathType();
	  path.setD("M 100 100 L 300 100 L 200 300 z");
	  path.setFill("red");
	  path.setStroke("blue");
	  path.setStrokeWidth("3");
	  svg.getChildren().add(path);
	  try {
		 JAXBContext context = JAXBContext.newInstance(SvgType.class);
		 Marshaller marshaller = context.createMarshaller();
		 DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		 dbf.setNamespaceAware(true);
		 Document doc = dbf.newDocumentBuilder().newDocument();
		 StringWriter sow = new StringWriter();
		 marshaller.marshal(new JAXBElement<SvgType>(new QName("http://www.w3.org/2000/svg", "svg"), SvgType.class, svg), sow);
		 String strSvg = sow.toString();
		 log.info("SVG=" + strSvg);
		 return strSvg;
	  } catch (JAXBException e) {
		 e.printStackTrace();
		 return null;
	  } catch (ParserConfigurationException e) {
		 e.printStackTrace();
		 return null;
	  }
   }

}
