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
	static Logger log = LoggerFactory.getLogger(MaxAgeFilter.class);

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
		httpResponse.setHeader("Cache-Control", "max-age="+maxAge);
		// Seems to break IE httpResponse.addHeader("Cache-Control", "private");
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
