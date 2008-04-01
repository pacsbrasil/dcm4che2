package org.dcm4chee.xds.repo.ws;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.dcm4chee.xds.common.XDSConstants;
import org.jboss.ws.core.jaxws.handler.GenericSOAPHandler;
import org.jboss.ws.core.soap.SOAPMessageImpl;
import org.jboss.ws.core.soap.attachment.MimeConstants;
import org.jboss.ws.extensions.addressing.AddressingConstantsImpl;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeaderElement;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.addressing.AddressingBuilder;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPMessageContext;

public class SOAPHeaderHandler extends GenericSOAPHandler
{
	private Logger log = LoggerFactory.getLogger(SOAPHeaderHandler.class);
	private static Set<QName> HEADERS = new HashSet<QName>();

	static {
		HEADERS.add( new AddressingConstantsImpl().getActionQName());
	}
	
	public Set getHeaders()
	{
		return Collections.unmodifiableSet(HEADERS);
	}
	
	protected boolean handleOutbound(MessageContext msgContext) {
		log.debug("--------------------------------handleOutbound");
		try {
			SOAPMessage msg = ((SOAPMessageContext)msgContext).getMessage();
			if (log.isDebugEnabled()) {
				log.debug("--------------------------------handleOutbound --SOAP Message end.");
				msg.writeTo(System.out);
				log.debug("--------------------------------handleOutbound --SOAP Message end.");
			}
			SOAPHeaderElement hdr = msg.getSOAPHeader().addHeaderElement(
					new QName(XDSConstants.NS_WS_ADDRESSING, XDSConstants.SOAP_HEADER_ACTION, "wsa"));
			hdr.setMustUnderstand(true);
			hdr.setValue(getActionForMessage(msg));
			log.info("--------------------------------handleOutbound --SOAP Message with added Action header.");
			msg.writeTo(System.out);
			log.info("--------------------------------handleOutbound --SOAP Message end.");
		} catch (Exception e) {
			log.error("Error:",e);
		}
		log.debug("--------------------------------handleOutbound end.");
		return true;
	}
	private String getActionForMessage(SOAPMessage msg) throws SOAPException {
		String root = msg.getSOAPBody().getFirstChild().getLocalName();
		log.debug("SOAPBody root elem:"+root);
		if ( XDSConstants.TAG_XDSB_RETRIEVE_DOC_SET_REPSONSE.equals(root)) {
			return XDSConstants.URN_IHE_ITI_2007_RETRIEVE_DOCUMENT_SET_RESPONSE;
		} else {
			return XDSConstants.URN_IHE_ITI_2007_PROVIDE_AND_REGISTER_DOCUMENT_SET_B_RESPONSE;
		}
	}
	protected boolean handleInbound(MessageContext msgContext) {
		log.debug("--------------------------------handleInbound");
		if (log.isDebugEnabled())
			try {
				SOAPMessage msg = ((SOAPMessageContext)msgContext).getMessage();
				msg.writeTo(System.out);
				SOAPMessageImpl msgi = (SOAPMessageImpl)msg;
				log.debug("***********************************************");
				log.debug("****** isFaultMessage:"+msgi.isFaultMessage());
				log.debug("****** isSWARefMessage:"+msgi.isSWARefMessage());
				log.debug("****** isXOPMessage:"+msgi.isXOPMessage());
				log.debug("****** countAttachments:"+msgi.countAttachments());
				log.debug("***********************************************");
			} catch (Exception e) {
				log.error("Error:",e);
			}
		log.debug("--------------------------------handleInbound end.");
		return true;
	}
}