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

package org.dcm4chee.xds.common.delegate;

import java.util.List;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.xml.bind.JAXBException;
import javax.xml.ws.handler.MessageContext;

import org.dcm4chee.xds.common.XDSConstants;
import org.dcm4chee.xds.common.exception.XDSException;
import org.dcm4chee.xds.infoset.v30.ObjectFactory;
import org.dcm4chee.xds.infoset.v30.ProvideAndRegisterDocumentSetRequestType;
import org.dcm4chee.xds.infoset.v30.RegistryError;
import org.dcm4chee.xds.infoset.v30.RegistryErrorList;
import org.dcm4chee.xds.infoset.v30.RegistryResponseType;
import org.dcm4chee.xds.infoset.v30.RetrieveDocumentSetRequestType;
import org.dcm4chee.xds.infoset.v30.RetrieveDocumentSetResponseType;
import org.jboss.mx.util.MBeanServerLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author franz.willer@gwi-ag.com
 * @version $Revision: 2389 $ $Date: 2006-04-05 11:55:23 +0200 (Mi, 05 Apr 2006) $
 * @since Mar 08, 2006
 */
public  class XDSbServiceDelegate {

    private static final String DEFAULT_XDSB_REPOSITORY_SERVICE_NAME = "dcm4chee.xds:service=XDSbRepositoryService";
    private static final String DEFAULT_XDSB_RETRIEVE_SERVICE_NAME = "dcm4chee.xds:service=XDSbRetrieveService";
    private static ObjectName xdsRepositoryServiceName = null;
    private static ObjectName xdsRetrieveServiceName = null;
    private static MBeanServer server;
    private ObjectFactory objFac = new ObjectFactory();

    private Logger log = LoggerFactory.getLogger(XDSbServiceDelegate.class);

    public XDSbServiceDelegate() {
        init();
    }
    public void init() {
        if (server != null) return;
        server = MBeanServerLocator.locate();
        String s = DEFAULT_XDSB_REPOSITORY_SERVICE_NAME;
        try {
            xdsRepositoryServiceName = new ObjectName(s);
            s = DEFAULT_XDSB_RETRIEVE_SERVICE_NAME;
            xdsRetrieveServiceName = new ObjectName(s);
        } catch (Exception e) {
            log.error( "Exception in init! Cant create ObjectName for "+s,e );
        }
    }

    public static ObjectName getXdsRepositoryServiceName() {
        return xdsRepositoryServiceName;
    }
    public static void setXdsRepositoryServiceName(
            ObjectName xdsRepositoryServiceName) {
        XDSbServiceDelegate.xdsRepositoryServiceName = xdsRepositoryServiceName;
    }
    public static ObjectName getXdsRetrieveServiceName() {
        return xdsRetrieveServiceName;
    }
    public static void setXdsRetrieveServiceName(ObjectName xdsRetrieveServiceName) {
        XDSbServiceDelegate.xdsRetrieveServiceName = xdsRetrieveServiceName;
    }
    /**
     * Makes the MBean call to export the document packed in a SOAP message.
     * @param submitRequest 
     * 
     * @param msg The document(s) packed in a SOAP message.
     * 
     * @return List of StoredDocument objects.
     */
    public RegistryResponseType storeAndRegisterDocuments( ProvideAndRegisterDocumentSetRequestType req ) throws XDSException {
        try {
            return (RegistryResponseType) server.invoke(xdsRepositoryServiceName,
                    "storeAndRegisterDocuments",
                    new Object[] { req },
                    new String[] { ProvideAndRegisterDocumentSetRequestType.class.getName() } );
        } catch ( Exception x ) {
            if ( x instanceof XDSException ) {
                throw (XDSException)x;
            } else if ( x.getCause() instanceof XDSException ){
                throw (XDSException) x.getCause();
            }
            log.error( "Exception occured in exportDocument: "+x.getMessage(), x );
            throw new XDSException( XDSConstants.XDS_ERR_REPOSITORY_ERROR, "Unexpected error in XDS service !: "+x.getMessage(),x);
        }
    }

    public RetrieveDocumentSetResponseType retrieveDocumentSetFromXDSbRepositoryService(
            RetrieveDocumentSetRequestType req, MessageContext msgCtx) throws XDSException {
        try {
            return (RetrieveDocumentSetResponseType) server.invoke(xdsRepositoryServiceName,
                    "retrieveDocumentSet",
                    new Object[] { req, msgCtx },
                    new String[] { RetrieveDocumentSetRequestType.class.getName(), MessageContext.class.getName() } );
        } catch ( Exception x ) {
            if ( x instanceof XDSException ) {
                throw (XDSException)x;
            } else if ( x.getCause() instanceof XDSException ){
                throw (XDSException) x.getCause();
            }
            log.error( "Exception occured in retrieveDocumentSet: "+x.getMessage(), x );
            throw new XDSException( XDSConstants.XDS_ERR_REPOSITORY_ERROR, "Unexpected error in XDS service !: "+x.getMessage(),x);
        }
    }

    public RetrieveDocumentSetResponseType retrieveDocumentSetFromXDSbRetrieveService(
            RetrieveDocumentSetRequestType req, String repoUID, MessageContext msgCtx) throws XDSException {
        try {
            return (RetrieveDocumentSetResponseType) server.invoke(xdsRetrieveServiceName,
                    "retrieveDocumentSet",
                    new Object[] { req, repoUID, msgCtx },
                    new String[] { RetrieveDocumentSetRequestType.class.getName(), String.class.getName(), MessageContext.class.getName() } );
        } catch ( Exception x ) {
            if ( x instanceof XDSException ) {
                throw (XDSException)x;
            } else if ( x.getCause() instanceof XDSException ){
                throw (XDSException) x.getCause();
            }
            log.error( "Exception occured in retrieveDocumentSet: "+x.getMessage(), x );
            throw new XDSException( XDSConstants.XDS_ERR_REPOSITORY_ERROR, "Unexpected error in XDS service !: "+x.getMessage(),x);
        }
    }
    
    public RegistryResponseType getErrorRegistryResponse( XDSException x) throws JAXBException {
        RegistryResponseType rsp = objFac.createRegistryResponseType();
        rsp.setStatus(XDSConstants.XDS_B_STATUS_FAILURE);
        RegistryErrorList errList = objFac.createRegistryErrorList();
        List<RegistryError> errors = errList.getRegistryError();
        rsp.setRegistryErrorList( errList );
        RegistryError error = getRegistryError(x);
        errors.add(error);
        return rsp;
    }
    private RegistryError getRegistryError(XDSException xdsException)
    throws JAXBException {
        RegistryError error = objFac.createRegistryError();
        error.setErrorCode(xdsException.getErrorCode());
        error.setCodeContext(xdsException.getMsg());
        error.setSeverity(xdsException.getSeverity());
        error.setLocation(xdsException.getLocation());
        error.setValue(xdsException.getMsg());
        return error;
    }
    public RetrieveDocumentSetResponseType getErrorRetrieveDocumentSetResponse(XDSException x) throws JAXBException {
        RetrieveDocumentSetResponseType rsp = objFac.createRetrieveDocumentSetResponseType();
        rsp.setRegistryResponse(getErrorRegistryResponse(x));
        return rsp;
    }

}
