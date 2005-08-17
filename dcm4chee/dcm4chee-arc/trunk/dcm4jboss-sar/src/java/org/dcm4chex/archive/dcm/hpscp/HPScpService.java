/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/

package org.dcm4chex.archive.dcm.hpscp;

import org.dcm4che.dict.UIDs;
import org.dcm4che.net.AcceptorPolicy;
import org.dcm4che.net.DcmServiceRegistry;
import org.dcm4chex.archive.dcm.AbstractScpService;
import org.dcm4chex.archive.util.EJBHomeFactory;

/**
 * @author gunter.zeilinger@tiani.com
 * @version $Revision$ $Date$
 * @since Aug 17, 2005
 */
public class HPScpService extends AbstractScpService {

    private final HPStoreScp hpStoreScp = new HPStoreScp(this);
    private final HPFindScp hpFindScp = new HPFindScp(this);
    private final HPMoveScp hpMoveScp = new HPMoveScp(this);
	
    public String getEjbProviderURL() {
        return EJBHomeFactory.getEjbProviderURL();
    }

    public void setEjbProviderURL(String ejbProviderURL) {
        EJBHomeFactory.setEjbProviderURL(ejbProviderURL);
    }

    protected void bindDcmServices(DcmServiceRegistry services) {
        services.bind(UIDs.HangingProtocolStorage, hpStoreScp);
        services.bind(UIDs.HangingProtocolInformationModelFIND, hpFindScp);
        services.bind(UIDs.HangingProtocolInformationModelMOVE, hpFindScp);
    }

    protected void unbindDcmServices(DcmServiceRegistry services) {
        services.unbind(UIDs.HangingProtocolStorage);
        services.unbind(UIDs.HangingProtocolInformationModelFIND);
        services.unbind(UIDs.HangingProtocolInformationModelMOVE);
    }

    protected void updatePresContexts(AcceptorPolicy policy, boolean enable) {
		String[] tsuids = enable ? getTransferSyntaxUIDs() : null;
        policy.putPresContext(UIDs.HangingProtocolStorage, tsuids);
        policy.putPresContext(UIDs.HangingProtocolInformationModelFIND, tsuids);
        policy.putPresContext(UIDs.HangingProtocolInformationModelMOVE, tsuids);
     }

}
