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

public class TimeFilter  implements Filter{
	static Logger log = LoggerFactory.getLogger(TimeFilter.class);

	public void destroy() {
	}

	public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
		HttpServletRequest req = (HttpServletRequest) request;
		long start = System.currentTimeMillis();
		filterChain.doFilter(request,response);
		long dur = System.currentTimeMillis() - start;
		String msg = "The request "+ req.getRequestURI()
		+ " with parameters " + req.getQueryString() + " took "+dur+" ms.";
		if( dur < 10 ) {
			log.debug(msg);
		}
		else if( dur < 400 ) {
			log.info(msg);
		}
		else if( dur < 1000 ) {
			log.warn(msg);
		}
		else {
			log.error(msg);
		}
	}

	public void init(FilterConfig arg0) throws ServletException {
	}

}
