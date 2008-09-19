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
package org.dcm4chee.xdsb.repository.mbean;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.activation.DataHandler;
import javax.management.ObjectName;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;
import org.dcm4che2.audit.message.AuditEvent;
import org.dcm4che2.audit.message.AuditMessage;
import org.dcm4chee.xds.common.XDSConstants;
import org.dcm4chee.xds.common.XDSPerformanceLogger;
import org.dcm4chee.xds.common.audit.HttpUserInfo;
import org.dcm4chee.xds.common.audit.XDSRetrieveMessage;
import org.dcm4chee.xds.common.exception.XDSException;
import org.dcm4chee.xds.common.infoset.ObjectFactory;
import org.dcm4chee.xds.common.infoset.RegistryResponseType;
import org.dcm4chee.xds.common.infoset.RetrieveDocumentSetRequestType;
import org.dcm4chee.xds.common.infoset.RetrieveDocumentSetResponseType;
import org.dcm4chee.xds.common.infoset.RetrieveDocumentSetRequestType.DocumentRequest;
import org.dcm4chee.xds.common.infoset.RetrieveDocumentSetResponseType.DocumentResponse;
import org.dcm4chee.xds.common.store.DocumentStoreDelegate;
import org.dcm4chee.xds.common.store.XDSDocument;
import org.dcm4chee.xds.common.store.XDSDocumentWriter;
import org.dcm4chee.xds.common.utils.InfoSetUtil;
import org.dcm4chee.xds.common.ws.DocumentRepositoryPortType;
import org.dcm4chee.xds.common.ws.DocumentRepositoryPortTypeFactory;
import org.jboss.system.ServiceMBeanSupport;
import org.jboss.system.server.ServerConfigLocator;


/**
 * @author franz.willer@gmail.com
 * @version $Revision: 5476 $ $Date: 2007-11-21 09:45:36 +0100 (Mi, 21 Nov 2007) $
 * @since Mar 08, 2006
 */
public class XDSbRetrieveService extends ServiceMBeanSupport {


    private static final String CERT = "CERT";
    private static final String NONE = "NONE";
    private String proxyHost;
    private int proxyPort;

    private String keystoreURL = "resource:identity.p12";
    private String keystorePassword;
    private String trustStoreURL = "resource:cacerts.jks";
    private String trustStorePassword;
    private HostnameVerifier origHostnameVerifier = null;
    private String allowedUrlHost = null;

    private String repositoryUniqueId;
    
    private boolean logRequestMessage;
    private boolean logResponseMessage;
    private boolean logRemoteRequestMessages;
    private boolean logRemoteResponseMessages;
    private boolean indentXmlLog;
    private boolean saveRequestAsFile;
    
    private Map<String, String> mapExternalRepositories = new HashMap<String, String>();


    private DocumentStoreDelegate docStoreDelegate = new DocumentStoreDelegate();
    private ObjectFactory objFac = new ObjectFactory();

    public String getRepositoryUniqueId() {
        return repositoryUniqueId;
    }
    public void setRepositoryUniqueId(String repositoryUniqueId) {
        this.repositoryUniqueId = repositoryUniqueId;
    }

    public String getExternalRepositories() {
        if (mapExternalRepositories == null) return NONE;
        StringBuffer sb = new StringBuffer();
        for (Map.Entry e : mapExternalRepositories.entrySet()) {
            sb.append(e.getKey()).append('=').append(e.getValue()).append(System.getProperty("line.separator", "\n"));
        }
        return sb.toString();
    }

    public void setExternalRepositories(String s) {
        if (s == null || s.trim().equalsIgnoreCase(NONE) ) {
            mapExternalRepositories = null;
        } else {
            mapExternalRepositories = new HashMap<String,String>();
            StringTokenizer st = new StringTokenizer(s, "\r\n;=");
            while ( st.hasMoreElements() ) {
                mapExternalRepositories.put( st.nextToken(), st.nextToken());
            }
        }
    }

    /**
     * @return Returns the proxyHost.
     */
    public String getProxyHost() {
        return proxyHost == null ? NONE : proxyHost;
    }
    /**
     * @param proxyHost The proxyHost to set.
     */
    public void setProxyHost(String proxyHost) {
        if ( NONE.equals(proxyHost) ) 
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
    public void setKeyStorePassword(String keyStorePassword) {
        if ( NONE.equals(keyStorePassword)) keyStorePassword = null;
        this.keystorePassword = keyStorePassword;
    }
    /**
     * @return Returns the keyStoreURL.
     */
    public String getKeyStoreURL() {
        return keystoreURL;
    }
    /**
     * @param keyStoreURL The keyStoreURL to set.
     */
    public void setKeyStoreURL(String keyStoreURL) {
        this.keystoreURL = keyStoreURL;
    }
    /**
     * @return Returns the trustStore.
     */
    public String getTrustStoreURL() {
        return trustStoreURL == null ? NONE : trustStoreURL;
    }
    /**
     * @param trustStore The trustStore to set.
     */
    public void setTrustStoreURL(String trustStoreURL) {
        if ( NONE.equals(trustStoreURL ) ) {
            this.trustStoreURL = null;
        } else {
            this.trustStoreURL = trustStoreURL;
        }
    }
    /**
     * @param trustStorePassword The trustStorePassword to set.
     */
    public void setTrustStorePassword(String trustStorePassword) {
        if ( NONE.equals(trustStorePassword)) trustStorePassword = null;
        this.trustStorePassword = trustStorePassword;
    }
    /**
     * @return Returns the allowedUrlHost.
     */
    public String getAllowedUrlHost() {
        return allowedUrlHost == null ? CERT : allowedUrlHost;
    }
    /**
     * @param allowedUrlHost The allowedUrlHost to set.
     */
    public void setAllowedUrlHost(String allowedUrlHost) {
        this.allowedUrlHost = CERT.equals(allowedUrlHost) ? null : allowedUrlHost;
    }

    public boolean isLogRequestMessage() {
        return logRequestMessage;
    }
    public void setLogRequestMessage(boolean b) {
        this.logRequestMessage = b;
    }
    public boolean isLogResponseMessage() {
        return logResponseMessage;
    }
    public void setLogResponseMessage(boolean b) {
        this.logResponseMessage = b;
    }
    public boolean isLogRemoteRequestMessages() {
        return logRemoteRequestMessages;
    }
    public void setLogRemoteRequestMessages(boolean logRemoteRequestMessages) {
        this.logRemoteRequestMessages = logRemoteRequestMessages;
    }
    public boolean isLogRemoteResponseMessages() {
        return logRemoteResponseMessages;
    }
    public void setLogRemoteResponseMessages(boolean logRemoteResponseMessages) {
        this.logRemoteResponseMessages = logRemoteResponseMessages;
    }
    public boolean isIndentXmlLog() {
        return indentXmlLog;
    }
    public void setIndentXmlLog(boolean b) {
        this.indentXmlLog = b;
    }


    public boolean isSaveRequestAsFile() {
        return saveRequestAsFile;
    }
    public void setSaveRequestAsFile(boolean saveRequestAsFile) {
        this.saveRequestAsFile = saveRequestAsFile;
    }

    public ObjectName getDocumentStoreService() {
        return docStoreDelegate.getDocumentStoreService();
    }

    public void setDocumentStoreService(ObjectName name) {
        docStoreDelegate.setDocumentStoreService(name);
    }

    public RetrieveDocumentSetResponseType retrieveDocumentSet(RetrieveDocumentSetRequestType req) throws XDSException {
        if ( logRequestMessage) {
            try {
                log.info("RetrieveDocumentSetRequest:"+InfoSetUtil.marshallObject(
                    objFac.createRetrieveDocumentSetRequest(req), indentXmlLog) );
            } catch (JAXBException ignore) {
                log.debug("Failed to log RetrieveDocumentSetRequest! Ignored", ignore);
            }
        }
        XDSPerformanceLogger perfLogger = new XDSPerformanceLogger("XDSb", "RetrieveDocumentSet");
        String docUid, reqRepoUid;
        XDSDocument doc;
        RetrieveDocumentSetResponseType rsp = objFac.createRetrieveDocumentSetResponseType();
        RetrieveDocumentSetResponseType.DocumentResponse docRsp;
        List<String> localDocUids = new ArrayList<String>();
        List<RetrieveDocumentSetRequestType.DocumentRequest> reqDocList;
        Map<String, List<RetrieveDocumentSetRequestType.DocumentRequest>> remoteDocRequests = 
            new HashMap<String, List<RetrieveDocumentSetRequestType.DocumentRequest>>();
        for ( RetrieveDocumentSetRequestType.DocumentRequest docReq : req.getDocumentRequest() ) {
            reqRepoUid = docReq.getRepositoryUniqueId();
            docUid = docReq.getDocumentUniqueId();
            if ( reqRepoUid.equals(this.repositoryUniqueId) ) {
                perfLogger.startSubEvent("RetrieveDocument");
                perfLogger.setSubEventProperty("DocumentUUID", docUid);
                doc = docStoreDelegate.retrieveDocument(docUid, null);
                if ( doc != null ) {
                    perfLogger.setSubEventProperty("DocumentSize", String.valueOf(doc.getXdsDocWriter().size()));
                    localDocUids.add(docUid);
                    try {
                        docRsp = getDocumentResponse(doc);
                        rsp.getDocumentResponse().add(docRsp);
                    } catch (IOException e) {
                        log.error("Error in building DocumentResponse for document:"+doc);
                    }
                } else {
                    log.warn("Document not found! document UID:"+docUid);
                }
                perfLogger.endSubEvent();
            } else {
                log.info("Retrieve Document Request for other Repository!("+docReq.getRepositoryUniqueId()+
                        ") docUid:"+docUid);
                reqDocList = remoteDocRequests.get(reqRepoUid);
                if (reqDocList == null) {
                    reqDocList = new ArrayList<DocumentRequest>();
                    remoteDocRequests.put(reqRepoUid, reqDocList);
                }
                reqDocList.add(docReq);
            }
            if (!remoteDocRequests.isEmpty()) {
                if ( mapExternalRepositories != null ) {
                    try {
                        addRemoteRetrieveRequests(rsp, remoteDocRequests, perfLogger);
                    } catch (Exception e) {
                        log.error("Remote Document Retrieve failed!",e);
                    }
                } else {
                    log.info("Inclusion of retrieves from remote XDS.b Repositories is disabled!");
                    log.info("Retrieve Request contains request for foreign repositories! requests:"+remoteDocRequests);
                }
            }
        }
        perfLogger.startSubEvent("AuditAndCreateResponse");
        logRetrieve(localDocUids,true);
        RegistryResponseType regRsp = objFac.createRegistryResponseType();
        regRsp.setStatus(XDSConstants.XDS_B_STATUS_SUCCESS);
        rsp.setRegistryResponse(regRsp);
        if ( logResponseMessage) {
            try {
                log.info("RetrieveDocumentSetResponse:"+InfoSetUtil.marshallObject(
                        objFac.createRetrieveDocumentSetResponse(rsp), indentXmlLog) );
            } catch (JAXBException ignore) {
                log.debug("Failed to log RetrieveDocumentSetResponse! Ignored", ignore);
            }
        }
        perfLogger.endSubEvent();
        perfLogger.flush();
        return rsp;
    }

    public RetrieveDocumentSetResponseType retrieveDocumentSet(String docUid, String repositoryUID, String homeUid) throws XDSException {
        RetrieveDocumentSetRequestType rq = objFac.createRetrieveDocumentSetRequestType();
        rq.getDocumentRequest().add( createDocRequest(docUid, repositoryUID, homeUid) );
        return retrieveDocumentSet(rq);
    }
    
    public DataHandler retrieveDocument(String docUid, String repositoryUID, String homeUid) throws XDSException {
        RetrieveDocumentSetResponseType rsp = retrieveDocumentSet(docUid, repositoryUID, homeUid);
        try {
            if ( checkResponse(rsp.getRegistryResponse()) ) {
                List<DocumentResponse> l = rsp.getDocumentResponse();
                if ( l.size() == 1) {
                    DocumentResponse docRsp = l.get(0);
                    return docRsp.getDocument();
                } else if ( l.size() == 0 ) {
                    log.info("XDSDocument "+docUid+" not found on this Repository! repositoryUniqueId:"+repositoryUniqueId);
                } else {
                    log.warn("More than one document found for documentUID:"+docUid);
                }
            }
        } catch ( Exception x) {
            throw new XDSException(XDSConstants.XDS_ERR_REPOSITORY_ERROR, "Error Checking response!",x);
        }
        return null;
    }
    
    public String retrieveDocumentAsString(String docUid, String repositoryUID, String homeUid) throws XDSException, IOException {
        DataHandler dh = retrieveDocument(docUid, repositoryUID, homeUid);
        BufferedInputStream is = null;
        StringWriter w = null;
        try {
            if ( dh == null ) {
                return ("XDSDocument "+docUid+" not found on this Repository!");
            } else {
                w = new StringWriter();
                is = new BufferedInputStream(dh.getInputStream());
                int b;
                while ( (b = is.read()) != -1 ) {
                    if ( b > 31) {
                        w.write(b);
                    }
                }
                return w.toString();
            }
        } finally {
            if (is != null) 
                is.close();
            if (w != null)
                w.close();
        }
    }
    
    private DocumentRequest createDocRequest(String docUid,
            String repositoryUID, String homeUid) {
        DocumentRequest docRq = objFac.createRetrieveDocumentSetRequestTypeDocumentRequest();
        docRq.setDocumentUniqueId(docUid);
        docRq.setRepositoryUniqueId( (repositoryUID == null || repositoryUID.trim().length() == 0)
                ? this.repositoryUniqueId : repositoryUID);
        if ( homeUid != null && homeUid.trim().length() > 0 )
            docRq.setHomeCommunityId(homeUid);
        return docRq;
    }
    
    private DocumentResponse getDocumentResponse(XDSDocument doc) throws IOException {
        RetrieveDocumentSetResponseType.DocumentResponse docRsp;
        docRsp = objFac.createRetrieveDocumentSetResponseTypeDocumentResponse();
        docRsp.setDocumentUniqueId(doc.getDocumentUID());
        docRsp.setMimeType(doc.getMimeType());
        docRsp.setRepositoryUniqueId(this.repositoryUniqueId);
        docRsp.setDocument(doc.getXdsDocWriter().getDataHandler());
        return docRsp;
    }

    public DataHandler retrieveLocalDocument(String docUid, String mime) throws XDSException {
        log.info("Retrieve local Document "+docUid+" with mime type:"+mime);
        XDSDocument doc = docStoreDelegate.retrieveDocument(docUid, mime);
        if ( doc != null ) {
            if ( mime == null || mime.equals( doc.getMimeType() ) ) {
                XDSDocumentWriter wr = doc.getXdsDocWriter();
                return new XdsDataHandler(wr, mime);
            }
            log.info("Requested mime type ("+mime+
                    ") doesn't match with mime type of stored document ("+doc.getMimeType()+")!");
        } else {
            log.info("Requested document not found:"+docUid);
        }
        return null;
    }


    private boolean checkResponse(RegistryResponseType rsp) throws JAXBException {
        if ( rsp == null ){
            log.error("No RegistryResponse from repository (retrieve)!");
            return false;
        }
        log.debug("Check RegistryResponse:"+InfoSetUtil.marshallObject(objFac.createRegistryResponse(rsp), indentXmlLog) );
        String status = rsp.getStatus();
        log.debug("Rsp status:"+status );
        return status == null ? false : XDSConstants.XDS_B_STATUS_SUCCESS.equalsIgnoreCase(rsp.getStatus());
    }


    private RetrieveDocumentSetResponseType addRemoteRetrieveRequests(
            RetrieveDocumentSetResponseType mainResp, Map<String, List<DocumentRequest>> remoteDocRequests, XDSPerformanceLogger perfLogger) throws MalformedURLException,
            JAXBException, XDSException {
        perfLogger.startSubEvent("RetrieveRemoteDocuments");
        perfLogger.setSubEventProperty("NumberOfRemoteRepositories", 
                String.valueOf(remoteDocRequests.size()));
        DocumentRepositoryPortType port;
        String extRepositoryURI;
        List<DocumentRequest> unknownRepositoryRequests = new ArrayList<DocumentRequest>();
        for ( Map.Entry entry : remoteDocRequests.entrySet()) {
            extRepositoryURI = mapExternalRepositories.get( entry.getKey());
            if ( extRepositoryURI != null ) {
                configProxyAndTLS(extRepositoryURI);
                port = DocumentRepositoryPortTypeFactory.getDocumentRepositoryPortSoap12(
                        extRepositoryURI, 
                        XDSConstants.URN_IHE_ITI_2007_RETRIEVE_DOCUMENT_SET, 
                        java.util.UUID.randomUUID().toString());
                log.info("####################################################");
                log.info("####################################################");
                log.info("XDS.b Retrieve Service: Send retrieve document set-b request to repository:"+extRepositoryURI);
                log.info("####################################################");
                log.info("####################################################");
                RetrieveDocumentSetRequestType req = objFac.createRetrieveDocumentSetRequestType();
                req.getDocumentRequest().addAll((List<DocumentRequest>) entry.getValue());
                if ( logRemoteRequestMessages) {
                    try {
                        log.info("Remote RetrieveDocumentSetRequest:"+InfoSetUtil.marshallObject(
                            objFac.createRetrieveDocumentSetRequest(req), indentXmlLog) );
                    } catch (JAXBException ignore) {
                        log.debug("Failed to log RetrieveDocumentSetRequest! Ignored", ignore);
                    }
                }
                RetrieveDocumentSetResponseType rsp = port.documentRepositoryRetrieveDocumentSet(req);
                if ( logRemoteResponseMessages) {
                    try {
                        log.info("Remote RetrieveDocumentSetResponse:"+InfoSetUtil.marshallObject(
                                objFac.createRetrieveDocumentSetResponse(rsp), indentXmlLog) );
                    } catch (JAXBException ignore) {
                        log.debug("Failed to log RetrieveDocumentSetResponse! Ignored", ignore);
                    }
                }
                if ( checkResponse(rsp.getRegistryResponse()) ) {
                    log.info("Add "+rsp.getDocumentResponse().size()+" documents from remote repository!");
                    mainResp.getDocumentResponse().addAll(rsp.getDocumentResponse());
                }
            } else {
                log.info("Unknown remote XDS.b Repository:"+extRepositoryURI);
                unknownRepositoryRequests.addAll((List<DocumentRequest>) entry.getValue());
            }
        }
        perfLogger.endSubEvent();
        return mainResp;
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
                                log.warn("Warning: URL Host: "+urlHostName+" vs. "+session.getPeerHost());
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


    private void logRetrieve(List<String> docUids, boolean success) {
        try {
            HttpUserInfo userInfo = new HttpUserInfo(AuditMessage.isEnableDNSLookups());
            String user = userInfo.getUserId();
            XDSRetrieveMessage msg = XDSRetrieveMessage.createDocumentRepositoryRetrieveMessage(docUids);
            msg.setOutcomeIndicator(success ? AuditEvent.OutcomeIndicator.SUCCESS:
                AuditEvent.OutcomeIndicator.MINOR_FAILURE);
            msg.setSource(AuditMessage.getProcessID(), 
                    AuditMessage.getLocalAETitles(),
                    AuditMessage.getProcessName(),
                    AuditMessage.getLocalHostName());
            msg.setHumanRequestor(user != null ? user : "unknown", null, null);
            msg.setDestination(userInfo.getRequestURI(), null, userInfo.getHostName(), userInfo.getIP() );
            msg.validate();
            Logger.getLogger("auditlog").info(msg);
        } catch ( Throwable t) {
            log.error("Cant send Audit Log for Retrieve Document Set! Ignored");
        }
    }

    public class XdsDataHandler extends DataHandler {
        private XDSDocumentWriter writer;
        public XdsDataHandler(XDSDocumentWriter wr, String mime) {
            super(null, mime);
            writer = wr;
        }

        public void writeTo(OutputStream out) throws IOException {
            writer.writeTo(out);
        }
    }

}

