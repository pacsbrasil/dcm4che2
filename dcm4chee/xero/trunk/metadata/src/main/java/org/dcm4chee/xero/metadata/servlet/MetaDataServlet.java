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
package org.dcm4chee.xero.metadata.servlet;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.dcm4chee.xero.metadata.MetaDataBean;
import org.dcm4chee.xero.metadata.StaticMetaData;
import org.dcm4chee.xero.metadata.filter.Filter;
import org.dcm4chee.xero.metadata.filter.FilterItem;

/**
 * This servlet uses either JndiMetaData or SeamMetaData to get a list of
 * filters that can provide get/post responses based on query parameters, and
 * perhaps based on the content types accepted. The intent of this type of
 * servlet is that any two requests with the same parameters retrieve the same
 * data - except for changes to the underlying data made between requests. There
 * might be per-user filters that allow/deny the request based on the
 * parameters, but excluding that, the same results should be returned all the
 * time. If filtering per user/role/group is required, then the user/role/group
 * should be a parameter to this servlet and understood by the underlying
 * filters.
 */
public class MetaDataServlet extends HttpServlet {
	private static Logger log = Logger.getLogger(MetaDataServlet.class.getName());
	/** The meta data needs to be read from the appropriate location. */
	MetaDataBean metaData, metaDataForThis;

	/**
	 * The filterItem contains the fixed information about how to handle this
	 * request
	 */
	FilterItem filterItem;

	/**
	 * The filter needs to supply a type and a stream/byte array/data object to
	 * send
	 */
	Filter<ServletResponseItem> filter;

	private long modifiedTimeAllowed = 60 * 60 * 1000; // 1 hour default.  Refresh screen refreshes list.

	/** Filter the items to get a return response by calling the first filter in the list.
	 * 
	 * @param request
	 * @param response
	 * @throws ServletException
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	protected void doFilter(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		Map<String, Object> params = new HashMap<String, Object>(request
				.getParameterMap());
		ServletResponseItem sri = filter.filter(filterItem, params);
		response.setCharacterEncoding("UTF-8");
		if (sri == null) {
			response.sendError(HttpServletResponse.SC_NO_CONTENT,
					"No content found for this request.");
			return;
		}
		sri.writeResponse(request, response);
	}

	/**
	 * The initialization needs to read the meta data object in, from one of 3
	 * places: JndiMetaData SeamMetaData PropertiesMetaData TODO - see if we can
	 * figure out how to make this just kind of work by inheriting from
	 * something else...
	 * 
	 * @throws MalformedURLException
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void init() throws ServletException {
		String name = this.getInitParameter("metaData");
		try {
			URL url = this.getServletContext().getResource(name);
			if (url == null) {
				log.fine("URL for metadata resource "+name+" from servlet context was nullm trying class resource.");
				url = getClass().getResource(name);
				if( url==null ) {
					log.warning("URL from class is still null - missing resource:"+name);
				}
			}
			log.fine("Using url " + url + " on name " + name);
			MetaDataBean root = StaticMetaData.getMetaDataByUrl(url);
			String filterName = getInitParameter("filter");
			if( filterName==null ) throw new NullPointerException("Filter name not provided to meta-data servlet.");
			metaData = root.getForPath(filterName);
			if( metaData==null ) throw new NullPointerException("Filter/meta-data information not found for "+filterName);
			filter = (Filter<ServletResponseItem>) metaData.getValue();
			filterItem = new FilterItem(metaData);
		} catch (MalformedURLException e) {
			throw new ServletException(e);
		}
	}

	/** Get requests can return last modified information */
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		resp.addHeader("Cache-Control", "max-age="+Long.toString(getModifiedTimeAllowed()/1000));
		resp.addHeader("Cache-Control", "private");
		doFilter(req, resp);
	}

	/** Returns the amount of time allowed for modifications. */
	protected long getModifiedTimeAllowed() {
		return modifiedTimeAllowed;
	}

	/** Post responses always return new data */
	@Override
	protected void doPost(HttpServletRequest arg0, HttpServletResponse arg1)
			throws ServletException, IOException {
		doFilter(arg0, arg1);
	}

}
