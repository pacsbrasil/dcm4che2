package org.dcm4chee.xero.wado;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.dcm4che2.data.DicomElement;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.VR;
import org.dcm4chee.xero.metadata.servlet.ServletResponseItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Encodes the dicom object as XML.
 * 
 * @author bwallace
 * 
 */
public class XmlServletResponseItem implements ServletResponseItem {
   private static final Logger log = LoggerFactory.getLogger(XmlServletResponseItem.class);

   protected DicomObject ds;

   public XmlServletResponseItem(DicomObject ds) {
	  this.ds = ds;
   }

   public void writeResponse(HttpServletRequest request, HttpServletResponse response) throws IOException {
	  response.setContentType("application/xml");
	  response.setCharacterEncoding("UTF-8");
	  OutputStream os = response.getOutputStream();
	  Writer w = new OutputStreamWriter(os, "UTF-8");
	  w.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
	  w.write("<?xml-stylesheet href=\"/xsl/sr_html.xsl\" type=\"text/xsl\" ?>\n");
	  w.write("<dataset>\n");
	  writeDicomObject(w, ds);
	  w.write("</dataset>\n");
	  w.close();
   }

   protected void writeDicomObject(Writer w, DicomObject ds2) throws IOException {
	  for (Iterator it = ds2.iterator(); it.hasNext();) {
		 DicomElement de = (DicomElement) it.next();
		 w.write("<attr tag=\"");
		 String hexTag = Integer.toString(de.tag(), 16).toUpperCase();
		 for (int i = 0, n = 8 - hexTag.length(); i < n; i++)
			w.write("0");
		 w.write(hexTag);
		 w.write("\">");
		 if (de.hasDicomObjects()) {
			w.write("\n");
			for (int i = 0, n = de.countItems(); i < n; i++) {
			   DicomObject child = de.getDicomObject(i);
			   w.write("<item>\n");
			   writeDicomObject(w, child);
			   w.write("</item>\n");
			}
		 } else {
			String val = de.getString(ds.getSpecificCharacterSet(), false);
			if (val != null) {
			   int len = val.length();
			   val = val.replaceAll("&", "&amp;");
			   val = val.replaceAll("<", "&lt;");
			   w.write(val);
			   if( de.vr()==VR.CS && de.length()>len ) {
				  for(int i=0, n=de.length()-len; i<n; i++) {
					 w.write(" ");
				  }
			   }
			} else {
			   log.debug("Value is null for tag " + hexTag);
			}
		 }
		 w.write("</attr>\n");
	  }
   }

}
