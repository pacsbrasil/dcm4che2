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
 * Portions created by the Initial Developer are Copyright (C) 2008
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

package org.dcm4chee.xero.wado.multi;

import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.dcm4chee.xero.metadata.MetaData;
import org.dcm4chee.xero.metadata.filter.Filter;
import org.dcm4chee.xero.metadata.filter.FilterItem;
import org.dcm4chee.xero.metadata.filter.FilterUtil;
import org.dcm4chee.xero.metadata.servlet.ErrorResponseItem;
import org.dcm4chee.xero.metadata.servlet.ServletResponseItem;
import org.dcm4chee.xero.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.dcm4chee.xero.wado.WadoParams.*;

/**
 * When the response content-type is to be multi-part/mixed, coordinates
 * multiple responses and assembles them into a single response object.
 */
public class MultiPartContentTypeFilter implements Filter<ServletResponseItem> {
	private static final Logger log = LoggerFactory.getLogger(MultiPartContentTypeFilter.class);

	public static final String MULTIPART_MIXED = "multipart/mixed";
	public static final String APPLICATION_ZIP = "application/zip";

	private Filter<Iterator<ServletResponseItem>> iteratorFilter;

	/**
	 * Responds with a multi-part response item appropriate to the given
	 * multi-part content type request type
	 *
	 * Game plan: Use the iteratorFilter to figure out what response to encode, and then create
	 * an iterator that actually encodes with that type.  This allows multiple levels of nesting/
	 * iteration to be used - object UID, per-object, per-frame etc.
	 */
	public ServletResponseItem filter(FilterItem<ServletResponseItem> filterItem, Map<String, Object> params) {
		String contentTypes = (String) params.get(CONTENT_TYPE);

		if (contentTypes == null) {
			log.warn("No content type found, expected to see multipart/mixed");
			return null;
		}
	   FilterUtil.removeFromQuery(params, CONTENT_TYPE);

		MultiPartHandler ri = createMultiPartResponseItem(contentTypes);
		if (ri==null) {
			log.warn("Actual content types were not specified, expected to see multipart/mixed or application/zip");
			return null;
		}
		
		Iterator<ServletResponseItem> sri = iteratorFilter.filter(null,params);
		
		if( sri==null || !sri.hasNext() ) {
			log.warn("No results for contentType: " + contentTypes);
			return new ErrorResponseItem(HttpServletResponse.SC_NO_CONTENT);
		}

		ri.setResponseIterator(sri);

		return ri;
	}

	protected MultiPartHandler createMultiPartResponseItem(String contentTypes) {
		if(MULTIPART_MIXED.equals(contentTypes)) return new MultiPartContentTypeResponseItem();
		if(APPLICATION_ZIP.equals(contentTypes)) return new ZipContentTypeResponseItem();
		return null;
   }

	protected String[] parseString(String stringToParse, char delimiter) {
		return StringUtil.split(stringToParse, delimiter, true);
	}

	/** Gets the filter that knows how to generate an iterator on the current request type.
	 * 
	 * @return
	 */
	public Filter<Iterator<ServletResponseItem>> getIteratorFilter() {
   	return iteratorFilter;
   }

	/** Sets the iterator - defaults to an ObjectUidIterator */
	@MetaData(out="${ref:multipart}")
	public void setIteratorFilter(Filter<Iterator<ServletResponseItem>> iteratorFilter) {
   	this.iteratorFilter = iteratorFilter;
   }

}
