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

package org.dcm4chex.archive.dcm.hpscp;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

import javax.management.JMException;
import javax.management.ObjectName;

import org.dcm4che.dict.Status;
import org.dcm4che.dict.UIDs;
import org.dcm4che.net.AcceptorPolicy;
import org.dcm4che.net.DcmServiceException;
import org.dcm4che.net.DcmServiceRegistry;
import org.dcm4chex.archive.dcm.AbstractScpService;
import org.dcm4chex.archive.ejb.jdbc.AEData;
import org.dcm4chex.archive.mbean.TLSConfigDelegate;
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

    private TLSConfigDelegate tlsConfig = new TLSConfigDelegate(this);

    private ObjectName aeServiceName;

    private boolean sendPendingMoveRSP = true;

    private int acTimeout = 5000;

    private int dimseTimeout = 0;

    private int soCloseDelay = 500;

    public String getEjbProviderURL() {
        return EJBHomeFactory.getEjbProviderURL();
    }

    public void setEjbProviderURL(String ejbProviderURL) {
        EJBHomeFactory.setEjbProviderURL(ejbProviderURL);
    }

    public final ObjectName getTLSConfigName() {
        return tlsConfig.getTLSConfigName();
    }

    public final void setTLSConfigName(ObjectName tlsConfigName) {
        tlsConfig.setTLSConfigName(tlsConfigName);
    }

    public final int getReceiveBufferSize() {
        return tlsConfig.getReceiveBufferSize();
    }

    public final void setReceiveBufferSize(int size) {
        tlsConfig.setReceiveBufferSize(size);
    }

    public final int getSendBufferSize() {
        return tlsConfig.getSendBufferSize();
    }

    public final void setSendBufferSize(int size) {
        tlsConfig.setSendBufferSize(size);
    }

    public final boolean isTcpNoDelay() {
        return tlsConfig.isTcpNoDelay();
    }

    public final void setTcpNoDelay(boolean on) {
        tlsConfig.setTcpNoDelay(on);
    }

    /**
     * @return Returns the aeService.
     */
    public ObjectName getAEServiceName() {
        return aeServiceName;
    }

    /**
     * @param aeService
     *            The aeService to set.
     */
    public void setAEServiceName(ObjectName aeServiceName) {
        this.aeServiceName = aeServiceName;
    }

    public final int getAcTimeout() {
        return acTimeout;
    }

    public final void setAcTimeout(int acTimeout) {
        this.acTimeout = acTimeout;
    }

    public final int getDimseTimeout() {
        return dimseTimeout;
    }

    public final void setDimseTimeout(int dimseTimeout) {
        this.dimseTimeout = dimseTimeout;
    }

    public final int getSoCloseDelay() {
        return soCloseDelay;
    }

    public final void setSoCloseDelay(int soCloseDelay) {
        this.soCloseDelay = soCloseDelay;
    }

    public final boolean isSendPendingMoveRSP() {
        return sendPendingMoveRSP;
    }

    public final void setSendPendingMoveRSP(boolean sendPendingMoveRSP) {
        this.sendPendingMoveRSP = sendPendingMoveRSP;
    }

    public AEData queryAEData(String aet, InetAddress addr)
            throws DcmServiceException {
        try {
            Object o = server.invoke(aeServiceName, "getAE", new Object[] {
                    aet, addr }, new String[] { String.class.getName(),
                    InetAddress.class.getName() });
            if (o == null) {
                throw new DcmServiceException(Status.MoveDestinationUnknown,
                        aet);
            }
            return (AEData) o;
        } catch (JMException e) {
            log.error("Failed to query AEData", e);
            throw new DcmServiceException(Status.ProcessingFailure, e);
        }
    }

    Socket createSocket(AEData aeData) throws IOException {
        return tlsConfig.createSocket(aeData);
    }

    protected void bindDcmServices(DcmServiceRegistry services) {
        services.bind(UIDs.HangingProtocolStorage, hpStoreScp);
        services.bind(UIDs.HangingProtocolInformationModelFIND, hpFindScp);
        services.bind(UIDs.HangingProtocolInformationModelMOVE, hpMoveScp);
    }

    protected void unbindDcmServices(DcmServiceRegistry services) {
        services.unbind(UIDs.HangingProtocolStorage);
        services.unbind(UIDs.HangingProtocolInformationModelFIND);
        services.unbind(UIDs.HangingProtocolInformationModelMOVE);
    }

    protected void updatePresContexts(AcceptorPolicy policy, boolean enable) {
        String[] tsuids = enable ? valuesToStringArray(tsuidMap) : null;
        policy.putPresContext(UIDs.HangingProtocolStorage, tsuids);
        policy.putPresContext(UIDs.HangingProtocolInformationModelFIND, tsuids);
        policy.putPresContext(UIDs.HangingProtocolInformationModelMOVE, tsuids);
    }

}
