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
package org.dcm4chee.xero.servlet;

import java.io.ByteArrayOutputStream;
import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class sits in front of another page and applies the requested XSLT to
 * the page. The XSLT to be applied may only depend on the base URL (eg
 * /xero/image/image.seam), the browser and the language.
 * 
 * @author bwallace
 */
public class XsltFilter implements Filter {
   private static final Logger log = LoggerFactory.getLogger(XsltFilter.class);
   public String XSLT_PARAMETER = "xslt";

   FilterConfig filterConfig;

   Map<String,Transformer> transformers = new HashMap<String,Transformer>();

   public void destroy() {
   }

   /**
     * Gets the transformer appropriate to transform the given request, as
     * embodied in the responseWrapper.  Currently this replaces the last bit of the
     * URL (.seam) with Xsl.xsl and does a request, checking first to see if it is in the cache.
     * Assume this filter is single-threaded - if not, we need to use a rental model whereby
     * there is a replace transformer call.
     */
   protected Transformer getTransformer(HttpServletRequest request, String xml) throws IOException {
	  String xsl = request.getRequestURI();
	  int lastDot = xsl.lastIndexOf('.');
	  if( lastDot<0 ) throw new IllegalArgumentException("Unknown request - need to be .X for some X");
	  xsl = "http://localhost:8080/"+xsl.substring(0,lastDot)+"Xsl.xsl";
	  Transformer transformer = transformers.get(xsl);
	  if (transformer == null) {
		 URL url = new URL(xsl);
		 Source styleSource = new StreamSource(url.openStream());
		 TransformerFactory transformerFactory = TransformerFactory.newInstance();
		 transformerFactory.setURIResolver(new UrlURIResolver(request, filterConfig.getServletContext()));
		 try {
			transformer = transformerFactory.newTransformer(styleSource);
		 } catch (TransformerConfigurationException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		 }
		 transformers.put(xsl,transformer);
	  }
	  return transformer;
   }

   /**
     * Detect if XSLT should be applied
     */
   protected boolean checkApplyXslt(HttpServletRequest request) {
	  if ( request.getParameter(XSLT_PARAMETER)!=null ) 
		 return "true".equalsIgnoreCase(request.getParameter(XSLT_PARAMETER));
	  String agent = request.getHeader("USER-AGENT");
	  // Note that apple web-kit is NOT included, as pages for that browser are conditionally included
	  // based on need.
	  if (agent.indexOf("Opera") >= 0) {
		 return true;
	  }
	  if (agent.indexOf("Konqueror") >=0 ) {
		 return true;
	  }
	  return false;
   }

   public void doFilter(ServletRequest request, ServletResponse response, FilterChain filters) throws IOException, ServletException {
	  HttpServletRequest hRequest = (HttpServletRequest) request;
	  if (!checkApplyXslt(hRequest)) {
		 log.debug("Not applying XSLT transformation to "+hRequest.getRequestURI());
		 filters.doFilter(request, response);
		 return;
	  }
	  log.debug("Apply XSLT.");
 	  response.setContentType("application/xhtml+xml");
	  PrintWriter out = response.getWriter();
	  CharResponseWrapper responseWrapper = new CharResponseWrapper((HttpServletResponse) response);
	  filters.doFilter(request, responseWrapper);
	  // Get response from servlet
	  String xml = responseWrapper.toString();
	  if( xml.indexOf("<?xml-stylesheet")==-1 ) {
		 log.debug("Not applying XSLT - no xml-stylesheet applied.");
		 response.setContentLength(xml.length());
		 out.write(xml);
		 return;
	  }
	  StringReader sr = new StringReader(xml);
	  Source xmlSource = new StreamSource(sr);

	  try {
		 Transformer useTransform = getTransformer(hRequest, xml);
		 useTransform.setURIResolver(new UrlURIResolver(hRequest,filterConfig.getServletContext()));
		 CharArrayWriter caw = new CharArrayWriter();
		 StreamResult result = new StreamResult(caw);
		 useTransform.transform(xmlSource, result);
		 String html = caw.toString();
		 response.setContentLength(html.length());
		 out.write(html);
	  } catch (Exception ex) {
		 out.println(ex.toString());
		 out.write(responseWrapper.toString());
	  }
   }

   public void init(FilterConfig config) throws ServletException {
	  this.filterConfig = config;
   }

}

/**
 * Figures out what source to use for various URI requests TODO High priority -
 * validate the path being requested to ensure it is locally valid and safe to
 * resolve. TODO Later on, include any security credentials.
 */
class UrlURIResolver implements URIResolver {
   private static Logger log = LoggerFactory.getLogger(UrlURIResolver.class);
   
   HttpServletRequest request;
   ServletContext servletContext;
   
   public UrlURIResolver(HttpServletRequest request, ServletContext servletContext) {
	  this.request = request;
	  this.servletContext = servletContext;
   }

   public Source resolve(String href, String base) throws TransformerException {
	  log.info("Trying to resolve "+href);
	  int firstSlash = href.indexOf('/');
	  RequestDispatcher dispatcher;
	  if( firstSlash!=0) {
		 log.info("Resolving against relative request dispatcher ");
		 dispatcher = request.getRequestDispatcher(href);
	  }
	  else {
		 log.info("Resolving absolute path.");
		 int secondSlash = href.indexOf('/',1);
		 if( secondSlash<0 ) {
			throw new RuntimeException("Not an absolute path within a request module:"+href);
		 }
		 String relative = href.substring(secondSlash);
		 String contextPath = href.substring(0,secondSlash);
		 log.info("Context path="+contextPath+" relative url="+relative);
		 if( contextPath.equals(request.getContextPath()) ) {
		   dispatcher = request.getRequestDispatcher(relative);
		 }
		 else {
		   ServletContext altContext = servletContext.getContext(contextPath);
		   dispatcher = altContext.getRequestDispatcher(relative);
		 }
	  }
	  
	  try {	 
		 CaptureHttpServletResponse response = new CaptureHttpServletResponse();
		 IncludeHttpServletRequest includeRequest = new IncludeHttpServletRequest(request,href);
		 dispatcher.include(includeRequest, response);
		 String resolvedResponse = response.getResponseString();
		 StringReader reader = new StringReader(resolvedResponse);
		 return new StreamSource(reader);
	  } catch (IOException e) {
		 throw new RuntimeException("IO Exception on URL:" + href, e);
	  } catch (ServletException e) {
		 throw new RuntimeException("Caught servlet exception "+e,e);
	  }
   }
}

/** This class removes all request attributes, replacing them with ones parsed from the new URL */
class IncludeHttpServletRequest extends HttpServletRequestWrapper {
   private static final Logger log = LoggerFactory.getLogger(IncludeHttpServletRequest.class);
   private Map<String,String[]> parameterMap = new HashMap<String,String[]>();
   private String queryString = "";
   private String requestURI;
   
   /** Add all the parameters from href into the parameter map, instead of the original values */
   public IncludeHttpServletRequest(HttpServletRequest request, String href) {
	  super(request);
	  int qPos = href.indexOf("?");
	  requestURI = href;
	  if( qPos==-1 ) return;
	  queryString = href.substring(qPos+1);
	  log.info("Include http servlet query string="+queryString);
	  requestURI = href.substring(0,qPos);
	  if( href.length()==0 ) return;
	  String[] args = queryString.split("&");
	  for(String arg : args) {
		 int keyEnd = arg.indexOf('=');
		 if( keyEnd<=0 ) continue;
		 String key = arg.substring(0,keyEnd);
		 String value = arg.substring(keyEnd+1);
		 if( value.isEmpty() ) continue;
		 String[] vals = parameterMap.get(key);
		 if( vals==null ) {
			parameterMap.put(key,new String[]{value});
		 }
		 else {
			String[] newVals = new String[vals.length+1];
			System.arraycopy(vals, 0, newVals, 0, vals.length);
			parameterMap.put(key,newVals);
		 }
	  }
   }
   
   @Override
   public String getQueryString() {
	  return queryString;
   }
   
   @Override
   public String getRequestURI() {
	  return requestURI;
   }

   @Override
   public String getParameter(String arg0) {
	  String[] val = parameterMap.get(arg0);
	  if( val==null || val.length==0 ) return null;
	  return val[0];
   }

   @Override
   public Map getParameterMap() {
	  return parameterMap;
   }

   @Override
   public Enumeration getParameterNames() {
	  throw new UnsupportedOperationException();
   }

   @Override
   public String[] getParameterValues(String arg0) {
	  return parameterMap.get(arg0);
   }
   
   
}

/**
 * Converts a regular output stream into a servlet output stream.
 * @author bwallace
 *
 */
class ServletOutputStreamConverter extends ServletOutputStream {
   private OutputStream outputStream;
   
   public ServletOutputStreamConverter(OutputStream os) {
	 outputStream = os;  
   }

   @Override
   public void write(int b) throws IOException {
	  outputStream.write(b);
   }

   @Override
   public void close() throws IOException {
	  super.close();
	  outputStream.close();
   }

   @Override
   public void flush() throws IOException {
	  super.flush();
	  outputStream.flush();
   }

   @Override
   public void write(byte[] b, int off, int len) throws IOException {
	  outputStream.write(b,off,len);
   }

   @Override
   public void write(byte[] b) throws IOException {
	  outputStream.write(b);
   }
   
}

/** This class just captures the output from the given http request */
class CaptureHttpServletResponse implements HttpServletResponse {
   
   private ByteArrayOutputStream outputStream;
   private ServletOutputStreamConverter servletOutputStream;
   
   private CharArrayWriter writer;
   private PrintWriter printWriter;
   
   public void addCookie(Cookie arg0) {
   }

   /** Return the string response sent from the servlet */
   public String getResponseString() {
	  if( writer!=null ) {
		 return writer.toString();
	  }
	  if( outputStream!=null ) {
		 try {
			return outputStream.toString("UTF-8");
		 } catch (UnsupportedEncodingException e) {
			throw new RuntimeException("Should be able to decode UTF-8");
		 }
	  }
	  return "";
   }

   public void addDateHeader(String arg0, long arg1) {
   }

   public void addHeader(String arg0, String arg1) {
   }

   public void addIntHeader(String arg0, int arg1) {
   }

   public boolean containsHeader(String arg0) {
	  return false;
   }

   public String encodeRedirectURL(String arg0) {
	  return null;
   }

   public String encodeRedirectUrl(String arg0) {
	  return null;
   }

   public String encodeURL(String arg0) {
	  return null;
   }

   public String encodeUrl(String arg0) {
	  return null;
   }

   public void sendError(int arg0) throws IOException {
   }

   public void sendError(int arg0, String arg1) throws IOException {
   }

   public void sendRedirect(String arg0) throws IOException {
   }

   public void setDateHeader(String arg0, long arg1) {
   }

   public void setHeader(String arg0, String arg1) {
   }

   public void setIntHeader(String arg0, int arg1) {
   }

   public void setStatus(int arg0) {
   }

   public void setStatus(int arg0, String arg1) {
   }

   public void flushBuffer() throws IOException {
   }

   public int getBufferSize() {
	  return 0;
   }

   public String getCharacterEncoding() {
	  return "UTF-8";
   }

   public String getContentType() {
	  return null;
   }

   public Locale getLocale() {
	  return null;
   }

   public ServletOutputStream getOutputStream() throws IOException {
	  if( servletOutputStream==null ) {
		 if( printWriter!=null ) 
			throw new IOException("Can't use both output stream and writer.");
		 outputStream = new ByteArrayOutputStream();
		 servletOutputStream = new ServletOutputStreamConverter(outputStream);
	  }
	  return servletOutputStream;
   }

   public PrintWriter getWriter() throws IOException {
	  if( printWriter==null ) {
		 if( servletOutputStream!=null ) throw new IOException("Can't use both writer and output stream.");
		 writer = new CharArrayWriter();
		 printWriter = new PrintWriter(writer);
	  }
	  return printWriter;
   }

   public boolean isCommitted() {
	  return false;
   }

   public void reset() {
	  throw new UnsupportedOperationException();
   }

   public void resetBuffer() {
	  throw new UnsupportedOperationException();
   }

   public void setBufferSize(int arg0) {
   }

   public void setCharacterEncoding(String arg0) {
   }

   public void setContentLength(int arg0) {
   }

   public void setContentType(String arg0) {
   }

   public void setLocale(Locale arg0) {
   }
}