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
package org.dcm4chee.xdsb.registry.mbean;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

import javax.management.Notification;
import javax.xml.soap.SOAPMessage;

import org.dcm4chee.xds.common.UUID;
import org.dcm4chee.xds.common.XDSConstants;
import org.dcm4chee.xds.common.XDSPerformanceLogger;
import org.dcm4chee.xds.common.exception.XDSException;
import org.dcm4chee.xds.common.infoset.ObjectFactory;
import org.dcm4chee.xds.common.infoset.RegistryError;
import org.dcm4chee.xds.common.infoset.RegistryErrorList;
import org.dcm4chee.xds.common.infoset.RegistryPackageType;
import org.dcm4chee.xds.common.infoset.RegistryResponseType;
import org.dcm4chee.xds.common.infoset.SubmitObjectsRequest;
import org.dcm4chee.xds.common.utils.InfoSetUtil;
import org.jboss.system.ServiceMBeanSupport;
import org.jboss.system.server.ServerConfigLocator;
import org.jboss.ws.core.CommonMessageContext;
import org.jboss.ws.core.soap.MessageContextAssociation;
import org.jboss.ws.core.soap.SOAPElementImpl;
import org.jboss.ws.core.soap.SOAPElementWriter;


/**
 * @author franz.willer@gmail.com
 * @version $Revision: 5476 $ $Date: 2007-11-21 09:45:36 +0100 (Mi, 21 Nov 2007) $
 * @since Mar 08, 2006
 */
public class XDSbRegistryService extends ServiceMBeanSupport {


    private static final String NONE = "NONE";
    private boolean logReceivedMessage;
    private boolean logResponseMessage;
    private boolean indentXmlLog;
    private boolean saveRequestAsFile;
    private boolean sendJmxNotification;
    private String savePath;
    private String mockError;


    private ObjectFactory objFac = new ObjectFactory();

    public boolean isLogReceivedMessage() {
        return logReceivedMessage;
    }
    public void setLogReceivedMessage(boolean b) {
        this.logReceivedMessage = b;
    }
    public boolean isLogResponseMessage() {
        return logResponseMessage;
    }
    public void setLogResponseMessage(boolean b) {
        this.logResponseMessage = b;
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

    public String getSavePath() {
        return savePath;
    }
    public void setSavePath(String savePath) {
        this.savePath = savePath;
    }
    public String getMockError() {
        return mockError == null ? NONE : mockError;
    }
    public void setMockError(String mockError) {
        this.mockError = NONE.equals(mockError) ? null : mockError;
    }

    public boolean isSendJmxNotification() {
        return sendJmxNotification;
    }
    public void setSendJmxNotification(boolean sendJmxNotification) {
        this.sendJmxNotification = sendJmxNotification;
    }
    public RegistryResponseType registerDocuments(SubmitObjectsRequest req) throws XDSException {
        boolean success = false;
        XDSPerformanceLogger perfLogger = new XDSPerformanceLogger("XDSb", "RegisterDocuments");
        try {
            log.debug("------------RegisterDocuments request:"+req);
            perfLogger.startSubEvent("LogAndVerify");
            if ( logReceivedMessage ) {
                log.info(" Received RegisterDocuments SubmitObjectsRequest:"+InfoSetUtil.marshallObject( req, indentXmlLog));
            }
            RegistryPackageType registryPackage = InfoSetUtil.getRegistryPackage(req);
            if ( registryPackage == null ) {
                log.error("No RegistryPackage found!");
                throw new XDSException( XDSConstants.XDS_ERR_REGISTRY_ERROR, 
                        XDSConstants.XDS_ERR_MISSING_REGISTRY_PACKAGE, null);
            }
            String submissionUID = InfoSetUtil.getExternalIdentifierValue(UUID.XDSSubmissionSet_uniqueId,registryPackage);
            if ( this.sendJmxNotification ) {
                sendJMXNotification(req);
            }
            perfLogger.setEventProperty("SubmissionSetUID", submissionUID);
            if ( saveRequestAsFile ) {
                File f = new File(resolvePath(savePath),submissionUID+".xml");
                f.getParentFile().mkdirs();
                FileOutputStream fos = new FileOutputStream(f);
                CommonMessageContext msgContext = MessageContextAssociation.peekMessageContext();
                if ( msgContext != null ) {
                    SOAPMessage msg = msgContext.getSOAPMessage();
                    SOAPElementWriter writer = new SOAPElementWriter(fos, "UTF-8");
                    writer.writeElement((SOAPElementImpl) msg.getSOAPPart().getEnvelope());
                } else {
                    log.info("No Message Context found (Request is not part of a webservice request)! Save SubmitObjectsRequest instead of SOAP message!");
                    InfoSetUtil.writeObject(req, fos, true);
                }
                fos.close();
            }
            perfLogger.endSubEvent();
            log.info("Received 'Register Document Set'! SubmissionUID:"+submissionUID);
            RegistryResponseType rsp = getMockResponse(perfLogger);
            if ( logResponseMessage ) {
                log.info("RegistryResponse:"+InfoSetUtil.marshallObject(objFac.createRegistryResponse(rsp), indentXmlLog));
            }
            return rsp;
        } catch (XDSException x) {
            throw x;
        } catch (Throwable t) {
            throw new XDSException(XDSConstants.XDS_ERR_REGISTRY_ERROR,"Register Document Set failed!",t);
       } finally {
           perfLogger.startSubEvent("PostProcess");
           perfLogger.setSubEventProperty("Success", String.valueOf(success));
           perfLogger.endSubEvent();
           perfLogger.flush();
       }
    }

    private RegistryResponseType getMockResponse(XDSPerformanceLogger perfLogger) {
        RegistryResponseType rsp = objFac.createRegistryResponseType();
        log.info("MockError:"+mockError);
        perfLogger.startSubEvent("RegisterDocuments");
        perfLogger.setSubEventProperty("RegistryURI", "none - mocked");
        if ( mockError == null ) {
            rsp.setStatus(XDSConstants.XDS_B_STATUS_SUCCESS);
        } else {
            rsp.setStatus(XDSConstants.XDS_B_STATUS_FAILURE);
            RegistryErrorList errList = objFac.createRegistryErrorList();
            rsp.setRegistryErrorList(errList);
            List<RegistryError> errors = errList.getRegistryError();
            RegistryError err = objFac.createRegistryError();
            int pos = mockError.indexOf('|');
            String errCode = pos == -1 ? XDSConstants.XDS_ERR_REGISTRY_ERROR : mockError.substring(0,pos);
            String errMsg = pos == -1 ? mockError : mockError.substring(++pos);
            err.setErrorCode(errCode);
            err.setCodeContext(errMsg);
            errors.add(err);
        }
        perfLogger.endSubEvent();
        return rsp;
    }

    public static String resolvePath(String fn) {
        File f = new File(fn);
        if (f.isAbsolute()) return f.getAbsolutePath();
        File serverHomeDir = ServerConfigLocator.locate().getServerHomeDir();
        return new File(serverHomeDir, f.getPath()).getAbsolutePath();
    }
    
    public void sendJMXNotification(Object o) {
        long eventID = super.getNextNotificationSequenceNumber();
        Notification notif = new Notification(o.getClass().getName(), this,
                eventID);
        notif.setUserData(o);
        super.sendNotification(notif);
    }
    
}

