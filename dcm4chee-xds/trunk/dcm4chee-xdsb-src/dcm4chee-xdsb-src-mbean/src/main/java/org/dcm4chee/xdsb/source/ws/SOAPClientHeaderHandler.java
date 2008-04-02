package org.dcm4chee.xdsb.source.ws;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPHeaderElement;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.dcm4chee.xds.common.XDSConstants;
import org.jboss.ws.core.jaxws.handler.GenericSOAPHandler;
import org.jboss.ws.extensions.addressing.AddressingConstantsImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SOAPClientHeaderHandler extends GenericSOAPHandler
{
	private Logger log = LoggerFactory.getLogger(SOAPClientHeaderHandler.class);
	private static Set<QName> HEADERS = new HashSet<QName>();

	static {
		HEADERS.add( new AddressingConstantsImpl().getActionQName());
	}
	
	public Set getHeaders()
	{
		log.info("@@@@@@@@@@@@ CLIENT @@@@@@@@@@@@@@@@ getHeaders :"+HEADERS);
		return Collections.unmodifiableSet(HEADERS);
	}

	protected boolean handleOutbound(MessageContext msgContext) {
		log.debug("@@@@@@@@@@@@ CLIENT @@@@@@@@@@@@@@@@ handleOutbound");
		try {
			SOAPMessage msg = ((SOAPMessageContext)msgContext).getMessage();
			if (log.isDebugEnabled()) {
				log.debug("@@@@@@@@@@@@ CLIENT @@@@@@@@@@@@@@@@ handleOutbound --SOAP Message end.");
				msg.writeTo(System.out);
				log.debug("@@@@@@@@@@@@ CLIENT @@@@@@@@@@@@@@@@ handleOutbound --SOAP Message end.");
			}
			SOAPHeaderElement hdr = msg.getSOAPHeader().addHeaderElement(
					new QName(XDSConstants.NS_WS_ADDRESSING, XDSConstants.SOAP_HEADER_ACTION, "wsa"));
			hdr.setMustUnderstand(true);
			hdr.setValue(XDSConstants.URN_IHE_ITI_2007_PROVIDE_AND_REGISTER_DOCUMENT_SET_B);
			log.info("@@@@@@@@@@@@ CLIENT @@@@@@@@@@@@@@@@ handleOutbound --SOAP Message with added Action header.");
			msg.writeTo(System.out);
			log.info("@@@@@@@@@@@@ CLIENT @@@@@@@@@@@@@@@@ handleOutbound --SOAP Message end.");
		} catch (Exception e) {
			log.error("Error:",e);
		}
		log.debug("@@@@@@@@@@@@ CLIENT @@@@@@@@@@@@@@@@ handleOutbound end.");
		return true;
	}
	protected boolean handleInbound(MessageContext msgContext) {
		log.debug("@@@@@@@@@@@@ CLIENT @@@@@@@@@@@@@@@@ handleInbound");
		return true;
	}
}