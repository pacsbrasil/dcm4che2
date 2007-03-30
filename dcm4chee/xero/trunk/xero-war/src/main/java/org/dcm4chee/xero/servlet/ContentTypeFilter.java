package org.dcm4chee.xero.servlet;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Sets the content type - defaults to text/xml. */
public class ContentTypeFilter  implements Filter{
	static private Logger log = LoggerFactory.getLogger(ContentTypeFilter.class);

	private String contentType;
	
	public void destroy() {
	}

	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		HttpServletRequest hsr = (HttpServletRequest) request;
		String specificContent = request.getParameter("contentType");
		if( specificContent==null ) specificContent = contentType;
		log.debug("Setting content type to "+specificContent+" for url "+hsr.getRequestURI());
		response.setContentType(specificContent);
		chain.doFilter(request,response);
	}

	public void init(FilterConfig config) throws ServletException {
		if( config.getInitParameter("contentType")!=null ) {
		  contentType = config.getInitParameter("contentType");
		}
	}

}
