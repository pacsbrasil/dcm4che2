/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
package org.dcm4chex.archive.dcm.gpwlscp;

import org.dcm4che.dict.UIDs;
import org.dcm4che.net.AcceptorPolicy;
import org.dcm4che.net.DcmServiceRegistry;
import org.dcm4chex.archive.dcm.AbstractScpService;
import org.dcm4chex.archive.ejb.interfaces.GPWLManagerHome;
import org.dcm4chex.archive.util.EJBHomeFactory;
import org.dcm4chex.archive.util.HomeFactoryException;

/**
 * @author gunter.zeilinger@tiani.com
 * @version Revision $Date$
 * @since 03.04.2005
 */

public class GPWLScpService extends AbstractScpService {

    private GPWLFindScp gpwlFindScp = new GPWLFindScp(this);

    public String getEjbProviderURL() {
        return EJBHomeFactory.getEjbProviderURL();
    }        

    public void setEjbProviderURL(String ejbProviderURL) {
        EJBHomeFactory.setEjbProviderURL(ejbProviderURL);
    }

    protected void startService() throws Exception {
        super.startService();
    }

    protected void stopService() throws Exception {
        super.stopService();
    }
    
    protected void bindDcmServices(DcmServiceRegistry services) {
        services.bind(UIDs.GeneralPurposeWorklistInformationModelFIND, gpwlFindScp);
    }

    protected void unbindDcmServices(DcmServiceRegistry services) {
        services.unbind(UIDs.GeneralPurposeWorklistInformationModelFIND);
    }

    protected void updatePresContexts(AcceptorPolicy policy, boolean enable) {
        policy.putPresContext(UIDs.ModalityWorklistInformationModelFIND,
                enable ? getTransferSyntaxUIDs() : null);
    }

    private GPWLManagerHome getGPWLManagerHome() throws HomeFactoryException {
        return (GPWLManagerHome) EJBHomeFactory.getFactory().lookup(
                GPWLManagerHome.class, GPWLManagerHome.JNDI_NAME);
    }
}
