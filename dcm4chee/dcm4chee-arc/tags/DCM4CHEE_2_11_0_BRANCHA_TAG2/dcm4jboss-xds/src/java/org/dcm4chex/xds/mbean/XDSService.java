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
 * TIANI Medgraph AG.
 * Portions created by the Initial Developer are Copyright (C) 2003-2005
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Gunter Zeilinger <gunter.zeilinger@tiani.com>
 * Franz Willer <franz.willer@gwi-ag.com>
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
package org.dcm4chex.xds.mbean;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.management.ObjectName;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.AttachmentPart;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Logger;
import org.dcm4cheri.util.UIDGeneratorImpl;
import org.dcm4chex.xds.XDSDocumentMetadata;
import org.dcm4chex.xds.common.SoapBodyProvider;
import org.dcm4chex.xds.common.XDSResponseObject;
import org.dcm4chex.xds.mbean.store.RIDStorageImpl;
import org.dcm4chex.xds.mbean.store.XDSFile;
import org.dcm4chex.xds.query.SQLQueryObject;
import org.dcm4chex.xds.query.XDSQueryObject;
import org.dcm4chex.xds.query.XDSQueryObjectFatory;
import org.jboss.system.ServiceMBeanSupport;
import org.jboss.system.server.ServerConfigLocator;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.sun.xml.messaging.saaj.util.JAXMStreamSource;

/**
 * @author franz.willer@gwi-ag.com
 * @version $Revision$ $Date$
 * @since Mar 08, 2006
 */
public class XDSService extends ServiceMBeanSupport {

    private static final String NONE = "NONE";
    
	private static Logger log = Logger.getLogger(XDSService.class.getName());

    private DocumentBuilderFactory dbFactory;

// http attributes to document repository actor (synchron) 	
	private String xdsRegistryURI;
	private String proxyHost;
	private int proxyPort;

    private String keystoreURL = "resource:identity.p12";
	private String keystorePassword;
    private String trustStoreURL = "resource:cacerts.jks";
	private String trustStorePassword;
	private HostnameVerifier origHostnameVerifier = null;
	private String allowedUrlHost = null;
	
	private boolean reassignDocumentUID = false;
	private boolean reassignSubmissionUID = false;
	
	private String fetchNewPatIDURL;
	
	private RIDStorageImpl store = RIDStorageImpl.getInstance();

	private String retrieveURI;

	private String testPatient;

    private List filteredSlots;
    
	private boolean useLongURI = false;
	private boolean logSOAPMessage = true;

    private String xdsQueryURI;
    private boolean forceSQLQuery = false;
    
	public XDSService() {
        dbFactory = DocumentBuilderFactory.newInstance();
        dbFactory.setNamespaceAware(true);
	}
//http
	/**
	 * @return Returns the docRepositoryURI.
	 */
	public String getXDSRegistryURI() {
		return this.xdsRegistryURI;
	}
	/**
	 * @param docRepositoryURI The docRepositoryURI to set.
	 */
	public void setXDSRegistryURI(String xdsRegistryURI) {
		this.xdsRegistryURI = xdsRegistryURI;
	}

	/**
     * @return the xdsQueryURI
     */
    public String getXDSQueryURI() {
        return xdsQueryURI;
    }
    /**
     * @param xdsQueryURI the xdsQueryURI to set
     */
    public void setXDSQueryURI(String xdsQueryURI) {
        this.xdsQueryURI = xdsQueryURI;
    }
    /**
     * @return the forceSQLQuery
     */
    public boolean isForceSQLQuery() {
        return forceSQLQuery;
    }
    /**
     * @param forceSQLQuery the forceSQLQuery to set
     */
    public void setForceSQLQuery(boolean forceSQLQuery) {
        this.forceSQLQuery = forceSQLQuery;
    }
    /**
	 * @return Returns the proxyHost.
	 */
	public String getProxyHost() {
		return proxyHost == null ? "NONE" : proxyHost;
	}
	/**
	 * @param proxyHost The proxyHost to set.
	 */
	public void setProxyHost(String proxyHost) {
		if ( "NONE".equals(proxyHost) ) 
			this.proxyHost = null;
		else
			this.proxyHost = proxyHost;
	}
	/**
	 * @return Returns the proxyPort.
	 */
	public int getProxyPort() {
		return proxyPort;
	}
	/**
	 * @param proxyPort The proxyPort to set.
	 */
	public void setProxyPort(int proxyPort) {
		this.proxyPort = proxyPort;
	}

	/**
	 * @param keyStorePassword The keyStorePassword to set.
	 */
	public void setKeystorePassword(String keyStorePassword) {
        if ( "NONE".equals(keyStorePassword)) keyStorePassword = null;
		this.keystorePassword = keyStorePassword;
	}
	/**
	 * @return Returns the keyStoreURL.
	 */
	public String getKeystoreURL() {
		return keystoreURL;
	}
	/**
	 * @param keyStoreURL The keyStoreURL to set.
	 */
	public void setKeystoreURL(String keyStoreURL) {
		this.keystoreURL = keyStoreURL;
	}
	/**
	 * @return Returns the trustStore.
	 */
	public String getTrustStoreURL() {
		return trustStoreURL == null ? "NONE" : trustStoreURL;
	}
	/**
	 * @param trustStore The trustStore to set.
	 */
	public void setTrustStoreURL(String trustStoreURL) {
		if ( "NONE".equals(trustStoreURL ) ) {
			this.trustStoreURL = null;
		} else {
			this.trustStoreURL = trustStoreURL;
		}
	}
	/**
	 * @param trustStorePassword The trustStorePassword to set.
	 */
	public void setTrustStorePassword(String trustStorePassword) {
        if ( "NONE".equals(trustStorePassword)) trustStorePassword = null;
		this.trustStorePassword = trustStorePassword;
	}
	/**
	 * @return Returns the allowedUrlHost.
	 */
	public String getAllowedUrlHost() {
		return allowedUrlHost == null ? "CERT" : allowedUrlHost;
	}
	/**
	 * @param allowedUrlHost The allowedUrlHost to set.
	 */
	public void setAllowedUrlHost(String allowedUrlHost) {
		this.allowedUrlHost = "CERT".equals(allowedUrlHost) ? null : allowedUrlHost;
	}
	
	/**
	 * @return Returns the ridServiceName.
	 */
	public ObjectName getRidServiceName() {
		return store.getRidServiceName();
	}
	/**
	 * @param ridServiceName The ridServiceName to set.
	 */
	public void setRidServiceName(ObjectName ridServiceName) {
		store.setRidServiceName(ridServiceName);
	}
	
	/**
	 * @return
	 */
	public String getRetrieveURI() {
		return retrieveURI;
	}
	/**
	 * @param retrieveURI The retrieveURI to set.
	 */
	public void setRetrieveURI(String retrieveURI) {
		this.retrieveURI = retrieveURI;
	}
	
	/**
	 * @return Returns the useLongURI.
	 */
	public boolean isUseLongURI() {
		return useLongURI;
	}
	/**
	 * @param useLongURI The useLongURI to set.
	 */
	public void setUseLongURI(boolean useLongURI) {
		this.useLongURI = useLongURI;
	}
	/**
	 * @return Returns the reassignUID.
	 */
	public boolean isReassignDocumentUID() {
		return reassignDocumentUID;
	}
	/**
	 * @param reassignUID The reassignUID to set.
	 */
	public void setReassignDocumentUID(boolean reassignUID) {
		this.reassignDocumentUID = reassignUID;
	}
	/**
	 * @return Returns the reassignUID.
	 */
	public boolean isReassignSubmissionUID() {
		return reassignSubmissionUID;
	}
	/**
	 * @param reassignUID The reassignUID to set.
	 */
	public void setReassignSubmissionUID(boolean reassignUID) {
		this.reassignSubmissionUID = reassignUID;
	}
    /**
     * @return Returns the logSOAPMessage.
     */
    public boolean isLogSOAPMessage() {
        return logSOAPMessage;
    }
    /**
     * @param logSOAPMessage The logSOAPMessage to set.
     */
    public void setLogSOAPMessage(boolean logSOAPMessage) {
        this.logSOAPMessage = logSOAPMessage;
    }
	/**
	 * @return Returns the testPatient.
	 */
	public String getTestPatient() {
		return testPatient == null ? "NONE":testPatient;
	}
	/**
	 * @param testPatient The testPatient to set.
	 */
	public void setTestPatient(String testPatient) {
		testPatient = replace(testPatient, "&amp;","&");
		this.testPatient = testPatient.equalsIgnoreCase("none") ? null : testPatient;
	}
	
	/**
     * @return the filterSlots
     */
    public String getFilteredSlots() {
        return getListString(filteredSlots);
    }
    /**
     * @param filterSlots the filterSlots to set
     */
    public void setFilteredSlots(String filterSlots) {
        this.filteredSlots = setListString(filterSlots);
    }
    private String replace(String s, String pattern, String replace) {
		int pos = s.indexOf(pattern);
		if ( pos == -1 ) return s;
		String s1 = s.substring(0,pos);
		String s2 = s.substring(pos+pattern.length());
		s = s1+replace+s2;
		return replace(s,pattern,replace);
	}
	/**
	 * @return Returns the fetchNewPatIDURL.
	 */
	public String getFetchNewPatIDURL() {
		return fetchNewPatIDURL;
	}
	/**
	 * @param fetchNewPatIDURL The fetchNewPatIDURL to set.
	 */
	public void setFetchNewPatIDURL(String fetchNewPatIDURL) {
		this.fetchNewPatIDURL = fetchNewPatIDURL;
	}

    private List setListString(String s) {
        List l = new ArrayList();
        if ( NONE.equals(s) ) return l;
        StringTokenizer st = new StringTokenizer( s, ";\n\r");
        while ( st.hasMoreTokens() ) {
            l.add(st.nextToken());
        }
        return l;
    }

    private String getListString(List l) {
        if ( l == null || l.isEmpty() ) return NONE;
        StringBuffer sb = new StringBuffer();
        for ( Iterator iter = l.iterator() ; iter.hasNext() ; ) {
            sb.append(iter.next()).append( System.getProperty("line.separator", "\n"));
        }
        return sb.toString();
    }

	public XDSResponseObject exportDocument( SOAPMessage message ) throws SOAPException, IOException, ParserConfigurationException, SAXException {
		List storedFiles = new ArrayList();
		if ( log.isDebugEnabled()) {
			log.debug("SOAP message:");
			this.dumpSOAPMessage(message);
		}
		String submissionUID = null;
		try {
			File file;
			Map attachments = getAttachments(message);
			Document d = getDocumentFromMessage(message);
			NodeList nl = d.getElementsByTagNameNS("urn:oasis:names:tc:ebxml-regrep:rim:xsd:2.1","ExtrinsicObject");
	        if(nl.getLength() < 1 ) {
                if ( attachments.isEmpty() ) {
                    log.debug("No ExtrinsicObject found! But we have nothing to store (no Attachment) -> forward message to registry!");
                    return new SOAPMessageResponse( sendSOAP(message, getXDSRegistryURI()));
                }
	        	log.error("No XDSDocumentEntry metadata (ExtrinsicObject) found.");
	            throw new Exception("No XDSDocumentEntry metadata (ExtrinsicObject) found.");
	        }
            if ( attachments.isEmpty() ) {
                log.debug("No Attachments found -> forward message to registry!");
                return new SOAPMessageResponse( sendSOAP(message, getXDSRegistryURI()));
            }
	        XDSDocumentMetadata metadata;
	        for(int i = 0, len = nl.getLength(); i < len; i++) {
	        	metadata = new XDSDocumentMetadata((Element)nl.item(i));
	        	if (reassignDocumentUID) {
	        		metadata.setUniqueID( UIDGeneratorImpl.getInstance().createUID() );
	        	}
	        	filterMetadata(metadata);
	        	file = saveDocumentEntry(metadata, attachments);
	        	metadata.setURI(getDocumentURI(metadata.getUniqueID(), metadata.getMimeType()));
	        	if ( file != null) {
	         		storedFiles.add(file);
	        	}
	        }
	        if ( this.reassignSubmissionUID ) {
	        	this.updateExternalIdentifier(d.getDocumentElement(),"urn:uuid:96fdda7c-d067-4183-912e-bf5ee74998a8", 
	        			UIDGeneratorImpl.getInstance().createUID());
	        }
	        if ( testPatient!= null) {
	            log.warn("Change patientID in metadata (urn:uuid:6b5aea1a-874d-4603-a4bc-96a0a7b38446) to testPatient! new patientID:"+testPatient);
	        	this.updateExternalIdentifier(d.getDocumentElement(),"urn:uuid:6b5aea1a-874d-4603-a4bc-96a0a7b38446",testPatient);
	        }
			log.info(storedFiles.size()+" Documents saved!");
		    MessageFactory messageFactory = MessageFactory.newInstance();
		    SOAPMessage msg = messageFactory.createMessage();
		    SOAPEnvelope envelope = msg.getSOAPPart().getEnvelope();
		    SOAPBody soapBody = envelope.getBody();
		    SOAPElement bodyElement = soapBody.addBodyElement(envelope.createName("SubmitObjectsRequest","rs","urn:oasis:names:tc:ebxml-regrep:registry:xsd:2.1"));
			Node leafRegistryObjectList = d.getElementsByTagNameNS("urn:oasis:names:tc:ebxml-regrep:rim:xsd:2.1","LeafRegistryObjectList").item(0);
			bodyElement.appendChild(bodyElement.getOwnerDocument().importNode(leafRegistryObjectList,true));
			SOAPMessage response = sendSOAP(msg, getXDSRegistryURI());
            if ( ! checkResponse( response, "RegistryResponse" ) ) {
            	deleteFiles(storedFiles);
            	log.error("Export document(s) failed! see prior messages for reason. SubmissionSet uid:"+submissionUID);
            	return new SOAPMessageResponse(response);
            }
			log.info("Register document finished.");
			return new SOAPMessageResponse(response);
		} catch ( Exception x ) {
			log.error("Export document(s) failed! SubmissionSet uid:"+submissionUID,x);
			deleteFiles(storedFiles);
			return new XDSRegistryResponse( false, "Export document(s) failed! SubmissionSet uid:"+submissionUID,x);
		}
		
	}
    
    public List xdsQuery(XDSQueryObject query) throws SOAPException {
        SOAPMessage response = sendSOAP(getSOAPMessage(query), this.xdsQueryURI);
        if ( !checkResponse( response, query.getResponseTag() ) ) return null;
        return getRegistryObjects(response, query.getResponseTag() );
    }

    private SOAPMessage getSOAPMessage(SoapBodyProvider docProvider) throws SOAPException {
        MessageFactory messageFactory = MessageFactory.newInstance();
        SOAPMessage msg = messageFactory.createMessage();
        SOAPEnvelope envelope = msg.getSOAPPart().getEnvelope();
        SOAPBody soapBody = envelope.getBody();
        soapBody.addDocument(docProvider.getDocument());
        return msg;
    }
    
    public List sqlQuery(String sql) throws SOAPException {
        log.info("make SQL Query:"+sql);
        SQLQueryObject qry = new SQLQueryObject(sql,SQLQueryObject.RETURN_TYPE_LEAF,true);
        return xdsQuery(new SQLQueryObject(sql));
    }
    
    public List findDocuments(String patId) throws SOAPException {
        if ( patId == null || patId.trim().length() < 1 ) patId = this.testPatient;
        XDSQueryObject query = XDSQueryObjectFatory.getInstance( forceSQLQuery ).newFindDocumentQuery(patId, null);
        log.info("findDocument Stored Query:"+query);
        return xdsQuery(query);
    }

    public List getDocuments(String uuids) throws SOAPException {
        StringTokenizer st = new StringTokenizer(uuids,"|");
        String[] sa = new String[st.countTokens()];
        for ( int i = 0 ; i < sa.length ; i++) {
            sa[i] = st.nextToken();
        }
        return getDocuments(sa);
    }
    public List getDocuments(String[] uuids) throws SOAPException {
        XDSQueryObject query = XDSQueryObjectFatory.getInstance( forceSQLQuery ).newGetDocumentQuery(uuids);
        log.info("getDocument Stored Query:"+query);
        return xdsQuery(query);
    }
    
	/**
	 * @param metadata
	 */
	private void filterMetadata(XDSDocumentMetadata metadata) {
        if ( ! filteredSlots.isEmpty() ) {
            for ( Iterator iter = filteredSlots.iterator() ; iter.hasNext() ; ) {
                metadata.removeSlot((String) iter.next());
            }
        }
        if ( testPatient!= null) {
            log.warn("Change patientID in metadata (urn:uuid:58a6f841-87b3-4a3e-92fd-a8ffeff98427) to testPatient! new patientID:"+testPatient);
        	this.updateExternalIdentifier((Element)metadata.getMetadata(),"urn:uuid:58a6f841-87b3-4a3e-92fd-a8ffeff98427",testPatient);
        }
		
	}
	
	private void updateExternalIdentifier(Element element, String scheme, String value) { 
		NodeList nl = element.getElementsByTagNameNS("urn:oasis:names:tc:ebxml-regrep:rim:xsd:2.1","ExternalIdentifier");
		NamedNodeMap attributes;
		for ( int i=0,l=nl.getLength() ; i<l ; i++ ) {
			attributes = nl.item(i).getAttributes();
			if ( attributes.getNamedItem("identificationScheme").getNodeValue().equals(scheme) ) {
				attributes.getNamedItem("value").setNodeValue(value);
			}
		}
	}
	/**
	 * @param uuid
	 * @param contentType
	 * @return
	 */
	private String[] getDocumentURI(String uuid, String contentType) {
		if ( useLongURI ) {
			String uidPart = "?requestType=DOCUMENT&documentUID="+uuid;
			String pctPart = "&preferredContentType="+contentType;
			int len = retrieveURI.length()+uidPart.length()+pctPart.length(); 
			if ( len < 129 ) { 
				return new String[]{retrieveURI+uidPart+pctPart};
			} else if ( uidPart.length()+pctPart.length() < 127) {//Slot max len = 128, we need 2 for <idx>|
				return new String[]{"1|"+retrieveURI, "2|"+uidPart+pctPart};
			} else {
				return new String[]{"1|"+retrieveURI, "2|"+uidPart, "3|"+pctPart};
			}
		} else {
			String s = retrieveURI+"?RT=DOCUMENT&UID="+uuid+"&PCT="+getShortContentType(contentType);
			return new String[]{s};
		}
	}
	
	/**
	 * @param contentType
	 * @return
	 */
	private String getShortContentType(String contentType) {
		if ( "application/pdf".equals(contentType) ) return "pdf";
		if ( "text/xml".equals(contentType) ) return "xml";
		if ( "application/dicom".equals(contentType) ) return "dcm";
		return contentType;
	}
	private Map getAttachments(SOAPMessage message) {
		Map map = new HashMap();
		AttachmentPart part;
		String id;
        for ( Iterator iter = message.getAttachments(); iter.hasNext() ; ) {
        	part = (AttachmentPart)iter.next();
        	id = part.getContentId();
        	if ( id.charAt(0) == '<') id = id.substring(1,id.length()-1); //remove < and >
        	map.put( id, part);
        }
		return map;
	}
	
	private SOAPMessage getRegistryMessage( SOAPMessage request, Document metadata ) throws SOAPException {
	    MessageFactory messageFactory = MessageFactory.newInstance();
	    SOAPMessage message = messageFactory.createMessage();
	    SOAPEnvelope envelope = message.getSOAPPart().getEnvelope();
	    SOAPBody soapBody = envelope.getBody();
	    soapBody.addDocument(metadata);
	    return message;
	}
	
    /**
	 * @param metadata
     * @throws IOException
	 */
	private File saveDocumentEntry(XDSDocumentMetadata metadata, Map attachments) throws IOException {
		log.info("Store attachment:"+metadata);
		XDSFile xdsFile;
		String id = metadata.getContentID();
		String uid = metadata.getUniqueID();
    	xdsFile = store.store(uid, (AttachmentPart)attachments.get(id));
    	metadata.setHash(xdsFile.getHash());
    	metadata.setSize(xdsFile.getFileSize());
		log.info("Attachment ("+uid+") stored in file "+xdsFile+" (size:"+xdsFile.getFileSize()+" hash:"+xdsFile.getHash());
		return xdsFile.getFile();
	}
	/**
	 * @param storedFiles
	 */
	private void deleteFiles(List storedFiles) {
		File file;
		for ( Iterator iter = storedFiles.iterator() ; iter.hasNext() ; ) {
			file = (File) iter.next();
			file.delete();
			deleteEmptyDir(file.getParentFile());
		}
	}
	/**
	 * @param parentFile
	 */
	private void deleteEmptyDir(File dir) {
		if ( dir == null ) return; 
		if ( dir.isDirectory() ) {
			File[] files = dir.listFiles(); 
			if ( files == null || files.length < 1 ) {
				dir.delete();
				deleteEmptyDir(dir.getParentFile());
			}
		}
		
	}
	
	
	public SOAPMessage sendSOAP( SOAPMessage message, String url ) {
		SOAPConnection conn = null;
		try {
	        log.debug("Send request to "+url+" (proxy:"+proxyHost+":"+proxyPort+")");
			configProxyAndTLS(url);
            SOAPConnectionFactory connFactory = SOAPConnectionFactory.newInstance();
            
            conn = connFactory.createConnection();
    		if ( log.isDebugEnabled()){
	            log.debug("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
	            log.debug("send registry request to "+url+" (proxy:"+proxyHost+":"+proxyPort+")");
	            log.debug("-------------------------------- request  ----------------------------------");
	            dumpSOAPMessage(message);
    		}           
            SOAPMessage response = conn.call(message, url);
            log.debug("-------------------------------- response ----------------------------------");
            dumpSOAPMessage(response);
            return response;
		} catch ( Throwable x ) {
			log.error("Cant send SOAP message! Reason:", x);
			return null;
		} finally {
			if ( conn != null ) try {
					conn.close();
				} catch (SOAPException ignore) {}
		}
		
	}
    public boolean soapQueryTest( String xmlFileName ) throws SOAPException {
        return soapTest(xmlFileName, this.xdsQueryURI);
    }
    public boolean soapRegistryTest( String xmlFileName ) throws SOAPException {
        return soapTest(xmlFileName, this.xdsRegistryURI);
    }
	public boolean soapTest( String xmlFileName, String soapURL ) throws SOAPException {
		log.info("\n\nPerform SOAP Test with SOAP Body from file:"+xmlFileName);
        File xmlFile = new File( xmlFileName );
        if ( !xmlFile.exists() ) {
        	log.warn("XML File for SOAP Body does not exist:"+xmlFile);
        	return false;
        }
        Document sb = readXMLFile(xmlFile);
	    MessageFactory messageFactory = MessageFactory.newInstance();
	    SOAPMessage message = messageFactory.createMessage();
	    SOAPEnvelope envelope = message.getSOAPPart().getEnvelope();
	    SOAPBody soapBody = envelope.getBody();
	    soapBody.addDocument(sb);
        SOAPMessage response = sendSOAP(message, soapURL);
		return checkResponse(response, "RegistryResponse");
	}
	
	private Document readXMLFile(File xmlFile){
        Document document = null;
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        try {
            dbFactory.setNamespaceAware(true);
            DocumentBuilder builder = dbFactory.newDocumentBuilder();
            document = builder.parse(xmlFile);
        } catch (Exception x) {
        	log.error("Cant read xml file:"+xmlFile, x);
        }
        return document;
    }
	
	
	/**
	 * @param response
	 * @return
     * @throws SOAPException
	 */
	private boolean checkResponse(SOAPMessage response, String responseTag) throws SOAPException {
		log.info("check RegistryResponse:"+response);
		try {
			NodeList nl;
	        Document d = getDocumentFromMessage( response );
			nl = d.getElementsByTagName(responseTag);
			log.debug("RegistryResponse NodeList:"+nl);
			if ( nl.getLength() != 0  ) {
				Node n = nl.item(0);
				String status = n.getAttributes().getNamedItem("status").getNodeValue();
				log.info("XDS: SOAP response status:"+status);
				if ( "Failure".equals(status) ) {
					NodeList errors = d.getElementsByTagName("RegistryError");
					StringBuffer sb = new StringBuffer();
					Node errNode;
					for ( int i = 0, len=errors.getLength(); i < len ; i++ ) {
					    sb.setLength(0); 
					    sb.append("Error (").append(i).append("):");
					    if ( (errNode = errors.item(i)) != null && errNode.getFirstChild() != null ) {
					        sb.append( errNode.getFirstChild().getNodeValue());
					    }
						log.info(sb.toString());
					}
					return false;
				}
			} else {
				log.warn("XDS: SOAP response without RegistryResponse!");
			}
			return true;
		} catch ( Exception x ) {
			log.error("Cant check response!", x);
			return false;
		}
	}
	
	private Document getDocumentFromMessage( SOAPMessage message ) throws SOAPException, ParserConfigurationException, SAXException, IOException {
		JAXMStreamSource src = (JAXMStreamSource) message.getSOAPPart().getContent();
        DocumentBuilder builder = dbFactory.newDocumentBuilder();
        Document d = builder.parse( src.getInputStream() );
        return d;
	}
	/**
	 * @param message
	 * @return
     * @throws IOException
     * @throws SOAPException
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 * @throws IOException
	 * @throws SOAPException
	 */
	private void dumpSOAPMessage(SOAPMessage message) throws ParserConfigurationException, SAXException, SOAPException, IOException {
	    if ( ! logSOAPMessage ) return;
		Source s = message.getSOAPPart().getContent();
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            out.write("SOAP message:".getBytes());
            Transformer t = TransformerFactory.newInstance().newTransformer();
            t.transform(s, new StreamResult(out));
            log.debug(out.toString());
        } catch (Exception e) {
            log.warn("Failed to log SOAP message", e);
        }
/*_*/        
	}
	
	public String fetchNewPatientIDfromNIST() throws IOException {
		URL url = new URL(fetchNewPatIDURL);
		configProxyAndTLS(url.getProtocol());
		InputStream is = url.openStream();
		byte[] buffer = new byte[4096];
		int len;
		StringBuffer sb = new StringBuffer();
		while ( (len = is.read(buffer)) > 0 ) {
			sb.append(new String(buffer,0,len));
		}
		log.debug("response of fetching new patientID:"+sb);
		String response = sb.toString().trim();
		int pos = response.indexOf("ISO");
		response = response.substring(0,pos+3);
		pos = response.lastIndexOf(">");
		if ( pos == -1 ) pos = response.lastIndexOf("="); //in Attribute?
		response = response.substring(pos+1);
		this.setTestPatient(response);
		return response;
	}
	/**
	 * 
	 */
	private void configProxyAndTLS(String url) {
		String protocol = url.startsWith("https") ? "https" : "http";
		if ( proxyHost != null && proxyHost.trim().length() > 1 ) {
			System.setProperty( protocol+".proxyHost", proxyHost);
			System.setProperty(protocol+".proxyPort", String.valueOf(proxyPort));
		} else {
			System.setProperty(protocol+".proxyHost", "");
			System.setProperty(protocol+".proxyPort", "");
		}
		if ( "https".equals(protocol) && trustStoreURL != null ) {
			String keyStorePath = resolvePath(keystoreURL);
			String trustStorePath = resolvePath(trustStoreURL);
			System.setProperty("javax.net.ssl.keyStore", keyStorePath);
			if ( keystorePassword != null ) 
                System.setProperty("javax.net.ssl.keyStorePassword", keystorePassword);
			System.setProperty("javax.net.ssl.keyStoreType","PKCS12");
			System.setProperty("javax.net.ssl.trustStore", trustStorePath);
			if ( trustStorePassword != null )
                System.setProperty("javax.net.ssl.trustStorePassword", trustStorePassword);
			if ( origHostnameVerifier == null) {
				origHostnameVerifier = HttpsURLConnection.getDefaultHostnameVerifier();
				HostnameVerifier hv = new HostnameVerifier() {
				    public boolean verify(String urlHostName, SSLSession session) {
				    	if ( !origHostnameVerifier.verify ( urlHostName, session)) {
				    		if ( isAllowedUrlHost(urlHostName)) {
				    			System.out.println("Warning: URL Host: "+urlHostName+" vs. "+session.getPeerHost());
				    		} else {
				    			return false;
				    		}
				    	}
			    		return true;
				    }

					private boolean isAllowedUrlHost(String urlHostName) {
						if (allowedUrlHost == null) return false;
						if ( allowedUrlHost.equals("*")) return true;
						return allowedUrlHost.equals(urlHostName);
					}

				};
				 
				HttpsURLConnection.setDefaultHostnameVerifier(hv);
			}
		}			
		
	}
    public static String resolvePath(String fn) {
    	File f = new File(fn);
        if (f.isAbsolute()) return f.getAbsolutePath();
        File serverHomeDir = ServerConfigLocator.locate().getServerHomeDir();
        return new File(serverHomeDir, f.getPath()).getAbsolutePath();
    }

    public List getRegistryObjects(SOAPMessage response, String responseTag) {
        ArrayList l = new ArrayList();
        try {
            NodeList nl;
            Document d = getDocumentFromMessage( response );
            nl = d.getElementsByTagName(responseTag);
            if ( nl.getLength() != 0  ) {
                Element e = (Element) nl.item(0);
                NodeList nlChilds = e.getElementsByTagName("ExtrinsicObject");
                log.info("ExtrtinsicObjects:"+nlChilds);
                if ( nlChilds.getLength() < 1 ) {
                    nlChilds = e.getElementsByTagName("ObjectRef");
                    for ( int i = 0, len=nlChilds.getLength() ; i < len ; i++) {
                        String id = nlChilds.item(i).getAttributes().getNamedItem("id").getNodeValue();
                        l.add( id );
                    }
                }
            }
        } catch ( Exception x ) {
            log.error("Cant get RegistryObjects from response!", x);
            return null;
        }
        log.info("return RegistryObjects:"+l);
        return l;
    }
	/*_*/
}
