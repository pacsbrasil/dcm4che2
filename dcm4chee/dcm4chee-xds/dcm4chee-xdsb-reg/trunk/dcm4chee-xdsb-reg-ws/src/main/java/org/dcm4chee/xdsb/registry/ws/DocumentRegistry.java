package org.dcm4chee.xdsb.registry.ws;

import javax.annotation.Resource;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.xml.bind.JAXBException;
import javax.xml.ws.WebServiceContext;

import org.dcm4chee.xds.common.XDSConstants;
import org.dcm4chee.xds.common.exception.XDSException;
import org.dcm4chee.xds.common.infoset.AdhocQueryRequest;
import org.dcm4chee.xds.common.infoset.AdhocQueryResponse;
import org.dcm4chee.xds.common.infoset.RegistryResponseType;
import org.dcm4chee.xds.common.infoset.SubmitObjectsRequest;
import org.dcm4chee.xds.common.ws.DocumentRegistryPortType;
import org.jboss.ws.annotation.EndpointConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author me
 *
 */
@WebService(
        name="DocumentRegistry_PortType",
        serviceName="DocumentRegistry_Service",
        portName="DocumentRegistry_Port_Soap12",
        targetNamespace="urn:ihe:iti:xds-b:2007",
        wsdlLocation="/WEB-INF/wsdl/registry.wsdl",
        endpointInterface="org.dcm4chee.xds.common.ws.DocumentRegistryPortType"
)
@SOAPBinding(style = SOAPBinding.Style.DOCUMENT)
@EndpointConfig(configName = "Standard SOAP 1.2 WSAddressing Endpoint")
public class DocumentRegistry implements DocumentRegistryPortType {

    @Resource
    WebServiceContext wsCtx;

    private XDSbRegistryDelegate delegate = new XDSbRegistryDelegate();
    private Logger log = LoggerFactory.getLogger(DocumentRegistry.class);

    public DocumentRegistry() {
        // TODO Auto-generated constructor stub
    }

    /* (non-Javadoc)
     * @see org.dcm4chee.xds.common.ws.DocumentRegistryPortType#registerDocumentSetB(org.dcm4chee.xds.repo.ws.ProvideAndRegisterDocumentSetRequestType)
     */
    public RegistryResponseType documentRegistryRegisterDocumentSetB(SubmitObjectsRequest req) {
        log.info("documentRegistryRegisterDocumentSetB SubmitObjectsRequest:"+req);
        RegistryResponseType resp = null;
        try {
            resp = delegate.registerDocuments(req);
        } catch (Exception x) {
            log.error("RegisterDocumentSet failed!Reason:"+x,x);
            try {
                resp = delegate.getErrorRegistryResponse(new XDSException(XDSConstants.XDS_ERR_REGISTRY_ERROR,"Register Document Set failed!",x));
            } catch (JAXBException e) {
                log.error("Can't create Error RegistryResponse!",e);
            }
        }
        return resp;
    }

    public AdhocQueryResponse documentRegistryRegistryStoredQuery(
            AdhocQueryRequest req) {
        log.info("documentRegistryRegistryStoredQuery AdhocQueryRequest:"+req);
        // TODO Auto-generated method stub
        return null;
    }



}