package org.dcm4chee.xds.repo.ws;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.xml.soap.AttachmentPart;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.dcm4chee.xds.common.XDSConstants;
import org.dcm4chee.xds.common.store.XDSDocumentWriterFactory;
import org.jboss.ws.core.jaxws.handler.GenericSOAPHandler;
import org.jboss.ws.core.soap.SOAPMessageImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

public class XDSXOPHandler extends GenericSOAPHandler
{
	private Logger log = LoggerFactory.getLogger(XDSXOPHandler.class);

	
	protected boolean handleOutbound(MessageContext msgContext) {
		return true;
	}
	protected boolean handleInbound(MessageContext msgContext) {
		log.debug("@@@@@@@@@@@@@@@@@@@@@@@@@@handleInbound");
		try {
			SOAPMessage msg = ((SOAPMessageContext)msgContext).getMessage();
			SOAPMessageImpl msgi = (SOAPMessageImpl)msg;
			HttpServletRequest servlet = (HttpServletRequest) msgContext.get(MessageContext.SERVLET_REQUEST);
			if ( servlet != null) {
				Map docDatas = getDocumentsData(msgi);
				servlet.getSession().setAttribute(XDSConstants.WORKAROUND_DOC_DATA_SESSION_KEY, docDatas);
			}
		} catch (Exception e) {
			log.error("Error:",e);
		}
		log.debug("@@@@@@@@@@@@@@@@@@@@@@@@@@handleInbound end.");
		return true;
	}
	/**
	 * @throws SOAPException *************************************************************************************/	
    private Map getDocumentsData(SOAPMessageImpl msg) throws SOAPException {
    	log.debug("@@@ Get Document Data from SOAP as workaround @@@");
    	Map docDatas = new HashMap();
        Map attachments = this.getAttachments(msg);
        Map documentElements = this.getDocumentElements(msg);
        AttachmentPart attachment;
        String id;
        Map.Entry entry;
        for ( Iterator iter = documentElements.entrySet().iterator() ; iter.hasNext() ; ) {
        	entry = (Map.Entry) iter.next();
        	log.debug("doc:"+entry.getKey());
       		try {
				attachment = getAttachmentforDoc((SOAPElement)entry.getValue(), attachments);
				log.debug("Attachment for document:"+attachment);
				if ( attachment != null) {
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					XDSDocumentWriterFactory.getInstance().getDocumentWriter(attachment).writeTo(baos);
					docDatas.put(entry.getKey(), baos.toByteArray());
					log.info("XOP-WORKAROUND: Add Document data! id:"+entry.getKey()+" size:"+baos.toByteArray().length);
				}
			} catch (Exception e) {
				log.error("XOP-WORKAROUND:Cant get Attachment for Document:"+entry.getKey());
			}
        }
        return docDatas;
	}
    private AttachmentPart getAttachmentforDoc(SOAPElement docElem, Map attachments) {
		SOAPElement n = getFirstChildNode(docElem, XDSConstants.NS_XOP, XDSConstants.XOP_INCLUDE);
		if ( n != null ) {
			String href = n.getAttributes().getNamedItem("href").getNodeValue();
			if ( href.startsWith("cid:")) 
				href = href.substring(4);
			AttachmentPart part = (AttachmentPart)attachments.get(href);
			return part;
		}    	
		return null;
    }
	private Map getDocumentElements(SOAPMessageImpl msg) throws SOAPException {
		Map map = new HashMap();
		List docNodes = getDocumentNodes(msg);
		SOAPElement docElem;
		String id;
		for ( Iterator iter = docNodes.iterator(); iter.hasNext() ; ) {
			docElem = (SOAPElement)iter.next();
			id = docElem.getAttribute("id");
			map.put(id, docElem);
		}
		return map;
	}
	private Map getAttachments(SOAPMessageImpl msg) throws SOAPException {
		Map map = new HashMap();
		String id;
		AttachmentPart part;
		for ( Iterator iter = msg.getAttachments(); iter.hasNext() ; ) {
			part = (AttachmentPart)iter.next();
			id = part.getContentId();
			if ( id.charAt(0)=='<')
				id = id.substring(1,id.length()-1);
			map.put(id, part);
		}
		return map;
	}
	private List getChildNodes(SOAPElement node, String namespaceURI, String localName) {
		List nodes = new ArrayList();
		if ( node != null ) {
			Node child;
	    	for ( Iterator iter = node.getChildElements(); iter.hasNext(); ) {
	    		child = (Node)iter.next();
	    		if ( child.getNodeType() == Node.ELEMENT_NODE ) {
	    			if ( (namespaceURI == null || namespaceURI.equals( child.getNamespaceURI() ) ) &&
	    					localName.equals( child.getLocalName() ) ) {
	    				nodes.add(child);
	    			}
	    		}    		
	    	}
		}
    	return nodes;
	}
	private SOAPElement getFirstChildNode(SOAPElement docElem, String namespaceURI, String localName) {
		if ( docElem == null ) return null;
    	SOAPElement child;
    	for ( Iterator iter = docElem.getChildElements() ; iter.hasNext() ; ) {
    		child = (SOAPElement) iter.next();
    		if ( child.getNodeType() == Node.ELEMENT_NODE ) {
    			if ( (namespaceURI == null || namespaceURI.equals( child.getNamespaceURI() ) ) &&
    					localName.equals( child.getLocalName() ) ) {
    				return child;
    			}
    		}		
    	}
    	return null;
	}
	
	private List getDocumentNodes(SOAPMessageImpl msg) throws SOAPException {
		if ( msg != null ) {
			SOAPElement elem = (SOAPElement) msg.getSOAPBody().getChildElements().next();
		   	return getChildNodes( elem, XDSConstants.NS_URN_IHE_ITI_XDS_B_2007, XDSConstants.TAG_XDSB_DOCUMENT);
		} else {
			return new ArrayList();
		}
	}

}