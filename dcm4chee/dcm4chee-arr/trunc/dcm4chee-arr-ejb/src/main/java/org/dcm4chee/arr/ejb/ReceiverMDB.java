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
 * The Initial Developer of the Original Code is Agfa HealthCare.
 * Portions created by the Initial Developer are Copyright (C) 2006
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Gunter Zeilinger <gunterze@gmail.com>
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

package org.dcm4chee.arr.ejb;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.jms.BytesMessage;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.stream.StreamSource;

import org.dcm4chee.arr.util.AuditMessageUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

/**
 * @author gunter zeilinger(gunterze@gmail.com)
 * @version $Revision$ $Date$
 * @since May 24, 2006
 * 
 */
@MessageDriven(activationConfig = {
        @ActivationConfigProperty(
                propertyName = "destinationType",
                propertyValue = "javax.jms.Queue"),
        @ActivationConfigProperty(
                propertyName = "destination",
                propertyValue = "queue/ARRReceiver") })
public class ReceiverMDB implements MessageListener {

    private Logger log = LoggerFactory.getLogger(ReceiverMDB.class);
    
    private static final String IHEYR4_XSL_URL =
        "resource:org/dcm4chee/arr/ejb/iheyr4.xsl";

    @PersistenceContext(unitName="dcm4chee-arr")
    private EntityManager em;
    private SAXParser parser;
    private Transformer transformer;
    private AuditRecordHandler handler;

    public void onMessage(Message msg) {
        byte[] xmldata;
        try {
            BytesMessage bytesMessage = (BytesMessage) msg;
            xmldata = new byte[(int) bytesMessage.getBodyLength()];
            bytesMessage.readBytes(xmldata);
        } catch (Throwable e) {
            log.error("Failed processing " + msg, e);
            return;
        }
        try {
            if (log.isDebugEnabled()) {
                log.debug("Start processing {}", 
                        AuditMessageUtils.promptMsg(xmldata));
            }            
            AuditRecord rec = new AuditRecord();
            rec.setXmldata(xmldata);
            parse(xmldata, rec);
            em.persist(rec);
            if (log.isDebugEnabled()) {
                log.debug("Finished processing {}", 
                        AuditMessageUtils.promptMsg(xmldata));
            }            
        } catch (Throwable e) {
            log.error("Failed processing " +
                    AuditMessageUtils.promptMsg(xmldata), e);
        }
     }

    private void parse(byte[] xmldata, AuditRecord rec) 
            throws TransformerException, ParserConfigurationException, 
            SAXException, IOException {
        if (handler == null) {
            handler = new AuditRecordHandler(em);
        }
        try {
            handler.setAuditRecord(rec);
            ByteArrayInputStream is = new ByteArrayInputStream(xmldata);
            if (AuditMessageUtils.isIHEYr4(xmldata)) {
                xslt(is);
            } else {
                parse(is);
            }
        } finally {
            handler.reset();
        }
    }

    private void parse(ByteArrayInputStream is) 
            throws ParserConfigurationException, SAXException, IOException {
        if (parser == null) {
            parser = SAXParserFactory.newInstance().newSAXParser();
        }
        try {
            parser.parse(is, handler);
        } finally {
            parser.reset();
        }        
    }

    private void xslt(ByteArrayInputStream is) throws TransformerException {
        if (transformer == null) {
            StreamSource iheyr4xsl = new StreamSource(IHEYR4_XSL_URL);
            transformer = SAXTransformerFactory.newInstance().newTransformer(iheyr4xsl);
        }
        try {
            transformer.transform(new StreamSource(is), new SAXResult(handler));
        } finally {
            transformer.reset();
        }        
    }

}
