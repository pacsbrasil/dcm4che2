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
import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.dcm4chee.xero.metadata.MetaDataBean;
import org.dcm4chee.xero.metadata.MetaDataUser;
import org.dcm4chee.xero.metadata.filter.Filter;
import org.dcm4chee.xero.metadata.filter.FilterItem;

/** This class knows how to encode a single item as XML, based on the pre-condition
 * that the underlying item is configured to allow Jaxb.  This class doesn't do any
 * creation or filtering of the item, it merely serializes it as a response, with the
 * correct XML content type etc.
 * This filter always runs - there isn't any check on content type etc to cause it to
 * run or not, unless the provided value is null, or it isn't serializeable.
 * @author bwallace
 *
 */
public class JaxbFilter implements Filter<ServletResponseItem>, MetaDataUser
{
	JAXBContext context;
	static Logger log = Logger.getLogger(JaxbFilter.class.getName());
	
	/**
	 * This class holds the filtered response item until it is time to be serialized
	 * 
	 * @author bwallace
	 *
	 */
	static class JaxbServletResponseItem implements ServletResponseItem {
		Object data;
		JAXBContext context;
		
		/** Hold the given data item, and JAXB context until serialization occurs. */
		JaxbServletResponseItem(Object data, JAXBContext context)
		{
			this.data = data;
			this.context = context;
		}

		/** Actually write the XML out to the response.
		 * @param response to write teh XML to.
		 * @param request is ignored.
		 */
		public void writeResponse(HttpServletRequest request, HttpServletResponse response) {
			try {
				if( context==null ) {
					context = JAXBContext.newInstance(data.getClass());
				}
				Marshaller m = context.createMarshaller();
				response.setContentType("text/xml");
				m.marshal(data, response.getOutputStream());			
			} catch (JAXBException e) {
				throw new RuntimeException(e);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

	/** Convert the object (if any) into a servlet response item that serializes it as XML
	 * @param nextFilter is called to get the item to convert to XML.
	 * @param params are just passed down the filter chain.
	 * @return a servlet response that can be used to actually write the XML data.
	 */
	public ServletResponseItem filter(FilterItem nextFilter, Map<String, Object> params) {
		Object data = nextFilter.callNextFilter(params);
		if( data==null ) return null;
		return new JaxbServletResponseItem(data,context);
	}

	/** Read the context path from the meta-data.
	 * @param metadatabean to read the JAXBContext name from.
	 */
	public void setMetaData(MetaDataBean metaDataBean) {
		String contextPath = (String) metaDataBean.getValue("contextPath");
		log.warning("Found contextPath="+contextPath);
		try {
			if( contextPath!=null ) context = JAXBContext.newInstance(contextPath);
		}
		catch(JAXBException e) {
			log.warning("Could not find context "+contextPath+" caught exception "+e);
		}
	}

}
