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
package org.dcm4chex.archive.xdsi;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.TreeMap;

import javax.activation.DataHandler;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.soap.AttachmentPart;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

import org.apache.log4j.Logger;
import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.dict.Tags;
import org.dcm4che.dict.UIDs;
import org.dcm4chex.archive.ejb.interfaces.FileDTO;
import org.dcm4chex.archive.ejb.jdbc.QueryCmd;
import org.dcm4chex.archive.ejb.jdbc.QueryFilesCmd;
import org.dcm4chex.archive.util.FileUtils;
import org.jboss.system.ServiceMBeanSupport;
import org.jboss.system.server.ServerConfig;
import org.jboss.system.server.ServerConfigLocator;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sun.xml.messaging.saaj.util.JAXMStreamSource;

/**
 * @author franz.willer@gwi-ag.com
 * @version $Revision$ $Date$
 * @since Feb 15, 2006
 */
public class XDSIService extends ServiceMBeanSupport {

	public static final String DOCUMENT_ID = "doc_1";
	public static final String AUTHOR_SPECIALITY = "authorSpeciality";
	public static final String AUTHOR_PERSON = "authorPerson";
	public static final String AUTHOR_ROLE = "authorRole";
	public static final String AUTHOR_ROLE_DIPLAYNAME = "authorRoleDisplayName";
	public  static final String AUTHOR_INSTITUTION = "authorInstitution";
	private static final String HEADER_VALUE = " ";
	
	private static Logger log = Logger.getLogger(XDSIService.class.getName());

    private static ServerConfig config = ServerConfigLocator.locate();

	private String testPath;
	
	
	private Map usr2author = new TreeMap();
	
	
// http attributes to document repository actor (synchron) 	
	private String docRepositoryURI;
	private String proxyHost;
	private int proxyPort;
	private boolean useHttp;
	
// SMTP attributes to document repository actor (asynchron)	
	private String docRepositoryMailAddr;//TODO
	private String smtpHost;
	private int smtpPort;
	private boolean useSmtp;
	
// Metadata attributes
	private File propertyFile;
	private File docTitleCodeListFile;
	private File classCodeListFile;
	private File contentTypeCodeListFile;
	private File eventCodeListFile;
	private File healthCareFacilityCodeListFile;

	private List authorRoles = new ArrayList();
	private List confidentialityCodes;
	
	private Properties metadataProps = new Properties();

	private Map mapCodeLists = new HashMap();
	/**
	 * @return Returns the property file path.
	 */
	public String getPropertyFile() {
			return propertyFile.getPath();
	}
	public void setPropertyFile(String file) throws IOException {
		if ( file == null || file.trim().length() < 1) return;
		propertyFile = new File(file.replace('/', File.separatorChar));
		try {
			readPropertyFile();
		} catch ( Throwable ignore ) {
			log.warn("Property file "+file+" cant be read!");
		}
	}

	public String getDocTitleCodeListFile() {
		return docTitleCodeListFile == null ? null : docTitleCodeListFile.getPath();
	}
	public void setDocTitleCodeListFile(String file) throws IOException {
		if ( file == null || file.trim().length() < 1) {
			docTitleCodeListFile = null;
		} else {
			docTitleCodeListFile = new File(file.replace('/', File.separatorChar));
		}
	}
	
	public String getClassCodeListFile() {
		return classCodeListFile == null ? null : classCodeListFile.getPath();
	}
	public void setClassCodeListFile(String file) throws IOException {
		if ( file == null || file.trim().length() < 1) {
			classCodeListFile = null;
		} else {
			classCodeListFile = new File(file.replace('/', File.separatorChar));
		}
	}

	public String getContentTypeCodeListFile() {
		return contentTypeCodeListFile == null ? null : contentTypeCodeListFile.getPath();
	}
	public void setContentTypeCodeListFile(String file) throws IOException {
		if ( file == null || file.trim().length() < 1) {
			contentTypeCodeListFile = null;
		} else {
			contentTypeCodeListFile = new File(file.replace('/', File.separatorChar));
		}
	}
	
	public String getHealthCareFacilityCodeListFile() {
		return healthCareFacilityCodeListFile == null ? null : healthCareFacilityCodeListFile.getPath();
	}
	public void setHealthCareFacilityCodeListFile(String file) throws IOException {
		if ( file == null || file.trim().length() < 1) {
			healthCareFacilityCodeListFile = null;
		} else {
			healthCareFacilityCodeListFile = new File(file.replace('/', File.separatorChar));
		}
	}
	
	public String getEventCodeListFile() {
		return eventCodeListFile == null ? null : eventCodeListFile.getPath();
	}
	public void setEventCodeListFile(String file) throws IOException {
		if ( file == null || file.trim().length() < 1) {
			eventCodeListFile = null;
		} else {
			eventCodeListFile = new File(file.replace('/', File.separatorChar));
		}
	}
	
	public String getConfidentialityCodes() {
		return getListString(confidentialityCodes);
	}
	public void setConfidentialityCodes(String codes) throws IOException {
		confidentialityCodes = setListString( codes );;
	}

	/**
	 * @return Returns the authorPerson or a user to authorPerson mapping.
	 */
	public String getAuthorPersonMapping() {
		if ( usr2author.isEmpty() ) {
			return metadataProps.getProperty(AUTHOR_PERSON);
		} else {
			return this.getMappingString(usr2author);
		}
	}
	
	/**
	 * Set either a fix authorPerson or a mapping user to authorPerson.
	 * <p>
	 * Mapping format: &lt;user&gt;=&lt;authorPerson&gt;<br>
	 * Use either newline or semicolon to seperate mappings.
	 * <p>
	 * If '=' is ommited, a fixed autorPerson is set in <code>metadataProps</code>
	 * 
	 * @param s The authorPerson(-mapping) to set.
	 */
	public void setAuthorPersonMapping(String s) {
		if ( s == null || s.trim().length() < 1) return;
		usr2author.clear();
		if ( s.indexOf('=') == -1) {
			metadataProps.setProperty(AUTHOR_PERSON, s); //NO mapping user -> authorPerson; use fix authorPerson instead
		} else {
			this.addMappingString(s, usr2author);
		}
	}

	/**
	 * get the authorPerson value for given user.
	 * 
	 * @param user
	 * @return
	 */
	public String getAuthorPerson( String user ) {
		String person = (String)usr2author.get(user);
		if ( person == null ) {
			person = metadataProps.getProperty(AUTHOR_PERSON);
		}
		return person;
	}

	/**
	 * @return Returns a list of authorRoles (with displayName) as String.
	 */
	public String getAuthorRoles() {
		return getListString(authorRoles);
	}
	
	/**
	 * Set authorRoles (with displayName).
	 * <p>
	 * Format: &lt;role&gt;^&lt;displayName&gt;<br>
	 * Use either newline or semicolon to seperate roles.
	 * <p>
	 * @param s The roles to set.
	 */
	public void setAuthorRoles(String s) {
		if ( s == null || s.trim().length() < 1) return;
		authorRoles = setListString(s);
	}

	public Properties joinMetadataProperties(Properties props) {
		Properties p = new Properties();//we should not change metadataProps!
		p.putAll(metadataProps);
		if ( props == null )
			p.putAll(props);
		return p;
	}
	
//http
	/**
	 * @return Returns the docRepositoryURI.
	 */
	public String getDocRepositoryURI() {
		return docRepositoryURI;
	}
	/**
	 * @param docRepositoryURI The docRepositoryURI to set.
	 */
	public void setDocRepositoryURI(String docRepositoryURI) {
		this.docRepositoryURI = docRepositoryURI;
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
	 * @return Returns the useHttp.
	 */
	public boolean isUseHttp() {
		return useHttp;
	}
	/**
	 * @param useHttp The useHttp to set.
	 */
	public void setUseHttp(boolean useHttp) {
		this.useHttp = useHttp;
	}
	
	/**
	 * @return Returns the testPath.
	 */
	public String getTestPath() {
		return testPath == null ? "NONE":testPath;
	}
	/**
	 * @param testPath The testPath to set.
	 */
	public void setTestPath(String testPath) {
		if ( "NONE".equals(testPath) ) 
			this.testPath = null;
		else
			this.testPath = testPath;
	}

	/**
	 * Adds a 'mappingString' (format:&lt;key&gt;=&lt;value&gt;...) to a map.
	 * 
	 * @param s
	 */
	private void addMappingString(String s, Map map) {
		StringTokenizer st = new StringTokenizer( s, ",;\n\r\t ");
		String t;
		int pos;
		while ( st.hasMoreTokens() ) {
			t = st.nextToken();
			pos = t.indexOf('=');
			if ( pos == -1) {
				map.put(t,t);
			} else {
				map.put(t.substring(0,pos), t.substring(++pos));
			}
		}
	}
	/**
	 * Returns the String representation of a map
	 * @return
	 */
	private String getMappingString(Map map) {
		if ( map == null || map.isEmpty() ) return null;
		StringBuffer sb = new StringBuffer();
		String key;
		for ( Iterator iter = map.keySet().iterator() ; iter.hasNext() ; ) {
			key = iter.next().toString();
			sb.append(key).append('=').append(map.get(key)).append( System.getProperty("line.separator", "\n"));
		}
		return sb.toString();
	}

	private List setListString(String s) {
		StringTokenizer st = new StringTokenizer( s, ";\n\r");
		String t;
		int pos;
		List l = new ArrayList();
		while ( st.hasMoreTokens() ) {
			t = st.nextToken();
			l.add(t);
		}
		return l;
	}

	private String getListString(List l) {
		if ( l == null || l.isEmpty() ) return null;
		StringBuffer sb = new StringBuffer();
		for ( Iterator iter = l.iterator() ; iter.hasNext() ; ) {
			sb.append(iter.next()).append( System.getProperty("line.separator", "\n"));
		}
		return sb.toString();
	}

// Operations	
	
	/**
	 * @throws IOException
	 */
	public void readPropertyFile() throws IOException {
		File propFile = FileUtils.resolve(this.propertyFile);
		BufferedInputStream bis= new BufferedInputStream( new FileInputStream( propFile ));
		try {
			metadataProps.load(bis);
		} finally {
			bis.close();
		}
	}
	
	public List listAuthorRoles() throws IOException {
		return this.authorRoles;
	}
	public List listDocTitleCodes() throws IOException {
		return readCodeFile(docTitleCodeListFile);
	}
	public List listEventCodes() throws IOException {
		return readCodeFile(eventCodeListFile);
	}
	public List listClassCodes() throws IOException {
		return readCodeFile(classCodeListFile);
	}
	public List listContentTypeCodes() throws IOException {
		return readCodeFile(contentTypeCodeListFile);
	}
	public List listHealthCareFacilityTypeCodes() throws IOException {
		return readCodeFile(healthCareFacilityCodeListFile);
	}
	public List listConfidentialityCodes() throws IOException {
		return confidentialityCodes;
	}
	
	/**
	 * @throws IOException
	 * 
	 */
	public List readCodeFile(File codeFile) throws IOException {
		if ( codeFile == null ) return new ArrayList();
		List l = (List) mapCodeLists.get(codeFile);
		if ( l == null ) {
			l = new ArrayList();
			File file = FileUtils.resolve(codeFile);
			if ( file.exists() ) {
				BufferedReader r = new BufferedReader( new FileReader(file));
				String line;
				while ( (line = r.readLine()) != null ) {
					if ( ! (line.charAt(0) == '#') ) {
						l.add( line );
					}
				}
				log.debug("Codes read from code file "+codeFile);
				log.debug("Codes:"+l);
				mapCodeLists.put(codeFile,l);
			} else {
				log.warn("Code File "+file+" does not exist! return empty code list!");
			}
		}
		return l;
	}
	
	public void clearCodeFileCache() {
		mapCodeLists.clear();
	}
		
	
	
	public boolean nistTest(String testID){
		if ( testID == null || testID.trim().length() < 1) {
			File dir = new File( testPath );
			File[] testDirs = dir.listFiles();
			boolean result = true;
			for ( int i = 0; i< testDirs.length; i++) {
				if ( testDirs[i].isDirectory())
					if ( !nistTest(testDirs[i].getName()) ) result = false;
			}
			return result;
		}
		log.info("\n\nPerform NIST test:"+testID);
        File propFile = new File( new File( testPath, testID), "test.properties");
    	Properties props = new Properties();
        try {
        	props.load(new FileInputStream(propFile));
        } catch(FileNotFoundException e) {
            log.warn(" Not a test Directory");
            return false;
        } catch(IOException e) {
            log.error("Error reading test.properties file!",e);
        }
		log.info(" Test with URI:"+props.getProperty("url"));
        String docs = null;
        if ( !props.getProperty("NumOfDoc","0").equals("0") ) {
	        StringBuffer sb = new StringBuffer();
	        int idx=1;
	        String doc, docPropName;
	        while ( (doc = props.getProperty( docPropName="doc"+idx++) ) != null) {
	        	sb.append(',').append(doc).append('|').append(props.get(docPropName+"mimeType"));
	        	sb.append('|').append(props.getProperty(docPropName+"UUID"));
	        }
	        if ( sb.length() > 1 ) docs = sb.toString().substring(1);
        }
		return sendSOAP(propFile.getParentFile().getAbsolutePath()+"/",props.getProperty("metadatafile"),docs, props.getProperty("url")  );
	}
	public boolean sendSOAP( String metaDataFilename, String docNames, String url ) {
		return sendSOAP(null,metaDataFilename,docNames, url);
	}
	public boolean sendSOAP( String baseDir, String metaDataFilename, String docNames, String url ) {
		if ( baseDir == null ) baseDir = testPath == null ? "": testPath+"/";
		File metaDataFile = new File( baseDir+metaDataFilename);
		
		String testID=metaDataFile.getParentFile().getName();
		XDSIDocument[] docFiles = null;
		if ( docNames != null && docNames.trim().length() > 0) {
			StringTokenizer st = new StringTokenizer( docNames, "," );
			docFiles = new XDSIDocument[ st.countTokens() ];
			for ( int i=0; st.hasMoreTokens(); i++ ) {
				docFiles[i] = XDSIFileDocument.valueOf( baseDir+st.nextToken() );
			}
		}
		return sendSOAP( readXMLFile(metaDataFile), docFiles, url );
	}
	
	public boolean sendSOAP( Document metaData, XDSIDocument[] docs, String url ) {
		SOAPConnection conn = null;
		try {
			if ( proxyHost != null && proxyHost.trim().length() > 1 ) {
				System.setProperty("http.proxyHost", proxyHost);
				System.setProperty("http.proxyPort", String.valueOf(proxyPort));
			}
			if ( url == null ) url = this.docRepositoryURI;
		    MessageFactory messageFactory = MessageFactory.newInstance();
		    SOAPMessage message = messageFactory.createMessage();
		    SOAPEnvelope envelope = message.getSOAPPart().getEnvelope();
		    SOAPBody soapBody = envelope.getBody();
		    SOAPElement bodyElement = soapBody.addDocument(metaData);
   			if ( docs != null ) {
		   		for (int i = 0; i < docs.length; i++) {
		   			DataHandler dhAttachment = docs[i].getDataHandler();
		   			AttachmentPart part = message.createAttachmentPart(dhAttachment);
		   			part.setMimeHeader("Content-Type", docs[i].getMimeType());
		   			part.setContentId(docs[i].getDocumentID());
		   			if ( log.isDebugEnabled()){
		   				log.debug("Add Attachment Part ("+(i+1)+"/"+docs.length+")! Document ID:"+part.getContentId()+" mime:"+docs[i].getMimeType());
		   			}
		   			message.addAttachmentPart(part);
		   		}
   			}
            SOAPConnectionFactory connFactory = SOAPConnectionFactory.newInstance();
           
            conn = connFactory.createConnection();
    		if ( log.isDebugEnabled()){
	            log.debug("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
	            log.debug("send request to "+url+" (proxy:"+proxyHost+":"+proxyPort+")");
	            log.debug("-------------------------------- request  ----------------------------------");
	            dumpSOAPMessage(message);
    		}           
            SOAPMessage response = conn.call(message, url);
       		if ( log.isDebugEnabled()){
	            log.debug("-------------------------------- response ----------------------------------");
	            dumpSOAPMessage(response);
	            log.debug("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
       		}
            return checkResponse( response );
		} catch ( Throwable x ) {
			log.error("Cant send SOAP message! Reason:", x);
			return false;
		} finally {
			if ( conn != null ) try {
					conn.close();
				} catch (SOAPException ignore) {}
		}
		
	}
	public boolean sendSOAP(String kosIuid, Properties mdProps) throws SQLException {
		Dataset kos = queryKOS( kosIuid );
		if ( kos == null ) return false;
		if ( mdProps == null ) mdProps = this.metadataProps;
		XDSMetadata md = new XDSMetadata(kos, mdProps);
		Document metadata = md.getMetadata();
        List files = new QueryFilesCmd(kosIuid).getFileDTOs();
        if ( files == null || files.size() == 0 ) {
        	
        }
        FileDTO fileDTO = (FileDTO) files.iterator().next();
		File file = FileUtils.toFile(fileDTO.getDirectoryPath(), fileDTO.getFilePath());
		XDSIDocument doc = new XDSIFileDocument(file,"application/dicom",DOCUMENT_ID);
		return sendSOAP(metadata,new XDSIDocument[]{doc} , null);
	}
	
	
	private Dataset queryKOS(String kosIuid) {
		Dataset keys = DcmObjectFactory.getInstance().newDataset();
		keys.putUI(Tags.SOPInstanceUID, kosIuid);
		keys.putUI(Tags.SOPClassUID, UIDs.KeyObjectSelectionDocument);
		QueryCmd query = null;
		try {
			query = QueryCmd.createInstanceQuery(keys, false);
			query.execute();
			if (query.next())
				return query.getDataset();			
		} catch (SQLException e) {
			log.error("Query for Key Object Selection iuid:" + kosIuid + " failed!", e);
		} finally {
			if (query != null)
				query.close();
		}
		return null;
	}

	public boolean sendSOAP(String kosIuid) throws SQLException {
		Dataset kos = queryKOS( kosIuid );
		return sendSOAP(kos,null);
	}
	public boolean sendSOAP(Dataset kos, Properties mdProps) throws SQLException {
		if ( log.isDebugEnabled()) {
			log.debug("Key Selection Object:");log.debug(kos);
		}
		if ( kos == null ) return false;
		if ( mdProps == null ) mdProps = this.metadataProps;
		XDSMetadata md = new XDSMetadata(kos, mdProps);
		Document metadata = md.getMetadata();
		XDSIDocument doc = new XDSIDatasetDocument(kos,"application/dicom",DOCUMENT_ID);
		return sendSOAP(metadata,new XDSIDocument[]{doc} , null);
	}
	
    /**
	 * @param response
	 * @return
     * @throws SOAPException
	 */
	private boolean checkResponse(SOAPMessage response) throws SOAPException {
		log.info("checkResponse:"+response);
		try {
//OK i give up! response.getSOAPBody() doesnt work (can not get/create Envelope due to an namespace	trouble ?!)
//So i read the DOM from SOAPPart content! 
//TODO somehow who know the right xalan, xerces, jaxp, ssaj,.. version mix to get it work 			
			JAXMStreamSource src = (JAXMStreamSource) response.getSOAPPart().getContent();
			NodeList nl;
	        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            dbFactory.setNamespaceAware(true);
            DocumentBuilder builder = dbFactory.newDocumentBuilder();
	        Document d = builder.parse( src.getInputStream() );
			nl = d.getElementsByTagName("RegistryResponse");
			log.debug("RegistryResponse NodeList:"+nl);
			if ( nl.getLength() != 0  ) {
				Node n = nl.item(0);
				String status = n.getAttributes().getNamedItem("status").getNodeValue();
				log.info("XDSI: SOAP response status."+status);
				if ( "Failure".equals(status) ) {
					NodeList errors = d.getElementsByTagName("RegistryError");
					for ( int i = 0, len=errors.getLength(); i < len ; i++ ) {
						log.info("Error ("+i+"):"+errors.item(i).getChildNodes().item(0).getNodeValue());
					}
					return false;
				}
			} else {
				log.warn("XDSI: SOAP response without RegistryResponse!");
			}
			return true;
		} catch ( Exception x ) {
			log.error("Cant check response!", x);
			return false;
		}
	}
	/**
	 * @param message
	 * @return
     * @throws IOException
     * @throws SOAPException
	 */
	private void dumpSOAPMessage(SOAPMessage message) throws SOAPException, IOException {
		message.writeTo(System.out);
	}
	
	private Document readXMLFile(File xmlFile){
        Document document = null;
        PrintStream orig = System.out;
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
	
}
