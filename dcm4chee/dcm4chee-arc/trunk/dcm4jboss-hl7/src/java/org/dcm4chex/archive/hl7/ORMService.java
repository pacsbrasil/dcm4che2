/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
package org.dcm4chex.archive.hl7;

import javax.xml.transform.Transformer;
import javax.xml.transform.sax.SAXResult;

import org.dcm4che.data.Dataset;
import org.dom4j.Document;
import org.dom4j.io.DocumentSource;
import org.xml.sax.ContentHandler;

/**
 * @author gunter.zeilinger@tiani.com
 * @version $Revision$ $Date$
 * @since 17.02.2005
 *
 */

public class ORMService extends AbstractHL7Service {

    private static final String ORM2DCM_XSL_URL = "resource:xsl/hl7/orm2dcm.xsl";

    public boolean process(MSH msh, Document msg, ContentHandler hl7out)
			throws HL7Exception {
        Dataset orm = dof.newDataset();
        try {
            Transformer t = getTemplates(ORM2DCM_XSL_URL).newTransformer();
            t.transform(new DocumentSource(msg),
            		new SAXResult(orm.getSAXHandler2(null)));
        } catch (Exception e) {
            throw new HL7Exception.AE(e.getMessage(), e);
        }
        return true;
	}

}
