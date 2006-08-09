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
 * Java(TM), available at http://sourceforge.net/projects/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * Agfa HealthCare.
 * Portions created by the Initial Developer are Copyright (C) 2003-2005
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * See listed authors below.
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

package org.dcm4chee.arr.util;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @version $Revision$ $Date$
 * @since Aug 2, 2006
 */
public class XSLTUtils {
    
    private static final String SUMMARY_XSL = "resource:arr-summary.xsl";
    private static final String DETAILS_XSL = "resource:arr-details.xsl";
    private static Templates summaryTpl;
    private static Templates detailsTpl;

    public static String toXML(byte[] xmldata) {
        try {
            Transformer tr = TransformerFactory.newInstance().newTransformer();
            tr.setOutputProperty(OutputKeys.INDENT, "yes");
            return transform(tr, xmldata);
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    public static String toSummary(byte[] xmldata) {
	try {
	    if (summaryTpl == null) {
		summaryTpl = loadTemplates(SUMMARY_XSL);
	    }
	    return transform(detailsTpl.newTransformer(), xmldata);
	} catch (Exception e) {
	    return e.getMessage();
	}
    }

    public static String toDetails(byte[] xmldata) {
	try {
	    if (detailsTpl == null) {
		detailsTpl = loadTemplates(DETAILS_XSL);
	    }
	    return transform(detailsTpl.newTransformer(), xmldata);
	} catch (Exception e) {
	    return e.getMessage();
	}
    }

    private static String transform(Transformer tr, byte[] xmldata)
	    throws TransformerException {
	StringWriter out = new StringWriter(512);
	tr.transform(new StreamSource(new ByteArrayInputStream(xmldata)),
		new StreamResult(out));
	return out.toString();
    }

    private static Templates loadTemplates(String url)
	    throws TransformerConfigurationException,
	    TransformerFactoryConfigurationError {
	return TransformerFactory.newInstance().newTemplates(
		new StreamSource(url));
    }
    
}
