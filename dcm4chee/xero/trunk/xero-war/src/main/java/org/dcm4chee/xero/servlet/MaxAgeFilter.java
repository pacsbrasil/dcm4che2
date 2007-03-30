package org.dcm4chee.xero.servlet;
import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MaxAgeFilter implements Filter
{
	static Logger log = LoggerFactory.getLogger(MaxAgeFilter.class.getName());

	/** The maximum age to declare an item, in seconds. 1 minute by default. */
	private long maxAge = 60;

	public void destroy() {
	}

	/**
	 * Add the cache control private and max-age= parameters to allow the page
	 * to be cached by the client browser.
	 */
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		HttpServletResponse httpResponse = (HttpServletResponse) response;
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		httpResponse.addHeader("Cache-Control", "max-age="+maxAge);
		httpResponse.addHeader("Cache-Control", "private");
		log.debug("For request "+httpRequest.getRequestURI()+" set max-age="+maxAge);
		chain.doFilter(request,response);
	}

	/**
	 * Read the maximum age from the maxAge filter configuration parameter in web.xml.
	 * This value is in seconds, and defaults to 1 minute if not otherwise specified.
	 */
	public void init(FilterConfig config) throws ServletException {
		String sMaxAge = config.getInitParameter("maxAge");
		if( sMaxAge!=null ) {
			this.maxAge = Long.parseLong(sMaxAge);
			log.info("Set max-age to "+maxAge+" for "+config.getFilterName());
		}
	}

}
