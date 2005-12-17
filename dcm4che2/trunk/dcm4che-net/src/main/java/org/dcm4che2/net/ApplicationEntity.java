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
 * Java(TM), hosted at http://sourceforge.net/projects/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * Gunter Zeilinger, Huetteldorferstr. 24/10, 1150 Vienna/Austria/Europe.
 * Portions created by the Initial Developer are Copyright (C) 2002-2005
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Gunter Zeilinger <gunterze@gmail.com>
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

package org.dcm4che2.net;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.dcm4che2.config.NetworkApplicationEntity;
import org.dcm4che2.config.NetworkConnection;
import org.dcm4che2.config.TransferCapability;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.net.pdu.AAssociateAC;
import org.dcm4che2.net.pdu.AAssociateRJException;
import org.dcm4che2.net.pdu.AAssociateRQ;
import org.dcm4che2.net.pdu.PresentationContext;
import org.dcm4che2.net.service.DicomService;

/**
 * @author gunter zeilinger(gunterze@gmail.com)
 * @version $Revision$ $Date$
 * @since Nov 25, 2005
 *
 */
public class ApplicationEntity
{
    private final NetworkApplicationEntity config;
    private final DicomServiceRegistry serviceRegistry;
    private final ArrayList connectors;
    private Device device;

    public ApplicationEntity()
    {
        this(new NetworkApplicationEntity());
    }
    
    public ApplicationEntity(NetworkApplicationEntity config)
    {
        if (config == null)
            throw new NullPointerException("config");
        this.config = config;
        this.serviceRegistry = new DicomServiceRegistry();
        this.connectors = new ArrayList(config.getNetworkConnection().length);
    }

    final ArrayList getConnectors()
    {
        return connectors;
    }

    void setDefaultServiceRegistry(DicomServiceRegistry serviceRegistry)
    {
        serviceRegistry.setDefaultRegistry(serviceRegistry);        
    }

    void initConnectors()
    {
        NetworkConnection[] nc = config.getNetworkConnection();
        for (int i = 0; i < nc.length; i++)
            addConnector(device.getConnector(nc[i]));
    }
    
    public NetworkApplicationEntity getConfiguration()
    {
        config.setNetworkConnection(getNetworkConnection());
        return config;
    }
    
    public final Device getDevice()
    {
        return device;
    }

    final void setDevice(Device device)
    {
        this.device = device;
    }

    public void addConnector(Connector c)
    {
        connectors.add(c);
        if (device != null)
            device.addConnector(c);
    }

    public final String getAETitle()
    {
        return config.getAETitle();
    }

    public final void setAETitle(String aetitle)
    {
        config.setAETitle(aetitle);
    }

    public final String[] getApplicationCluster()
    {
        return config.getApplicationCluster();
    }

    public final void setApplicationCluster(String[] cluster)
    {
        config.setApplicationCluster(cluster);
    }

    public final boolean isAssociationAcceptor()
    {
        return config.isAssociationAcceptor();
    }

    public final void setAssociationAcceptor(boolean acceptor)
    {
        config.setAssociationAcceptor(acceptor);
    }

    public final boolean isAssociationInitiator()
    {
        return config.isAssociationInitiator();
    }

    public final void setAssociationInitiator(boolean initiator)
    {
        config.setAssociationInitiator(initiator);
    }

    public final String getDescription()
    {
        return config.getDescription();
    }

    public final void setDescription(String description)
    {
        config.setDescription(description);
    }

    public final boolean isInstalled()
    {
        return config.isInstalled();
    }

    public final void setInstalled(boolean installed)
    {
        config.setInstalled(installed);
    }

    public NetworkConnection[] getNetworkConnection()
    {
        NetworkConnection[] a = new NetworkConnection[connectors.size()];
        for (int i = 0; i < a.length; i++)
            a[i] = ((Connector) connectors.get(i)).getConfiguration();
        return a;
    }

    public final String[] getPreferredCalledAETitle()
    {
        return config.getPreferredCalledAETitle();
    }

    public final boolean hasPreferredCalledAETitle()
    {
        return config.hasPreferredCalledAETitle();
    }
    
    public boolean isPreferredCalledAETitle(String aet)
    {
        return config.isPreferredCalledAETitle(aet);
    }
    
    public final void setPreferredCalledAETitle(String[] aets)
    {
        config.setPreferredCalledAETitle(aets);
    }

    public final String[] getPreferredCallingAETitle()
    {
        return config.getPreferredCallingAETitle();
    }

    public final boolean hasPreferredCallingAETitle()
    {
        return config.hasPreferredCallingAETitle();
    }
    
    public boolean isPreferredCallingAETitle(String aet)
    {
        return config.isPreferredCallingAETitle(aet);
    }
    
    public final void setPreferredCallingAETitle(String[] aets)
    {
        config.setPreferredCallingAETitle(aets);
    }

    public final String[] getSupportedCharacterSet()
    {
        return config.getSupportedCharacterSet();
    }

    public final void setSupportedCharacterSet(String[] characterSets)
    {
        config.setSupportedCharacterSet(characterSets);
    }

    public final TransferCapability[] getTransferCapability()
    {
        return config.getTransferCapability();
    }

    public final void setTransferCapability(TransferCapability[] transferCapability)
    {
        config.setTransferCapability(transferCapability);
    }

    public final Object[] getVendorData()
    {
        return config.getVendorData();
    }

    public final void setVendorData(Object[] vendorData)
    {
        config.setVendorData(vendorData);
    }

    public final int getMaxOpsInvoked()
    {
        return config.getMaxOpsInvoked();
    }
    
    public final void setMaxOpsInvoked(int maxOpsInvoked)
    {
        config.setMaxOpsInvoked(maxOpsInvoked);
    }
    
    public final int getMaxOpsPerformed()
    {
        return config.getMaxOpsPerformed();
    }
    
    public final void setMaxOpsPerformed(int maxOpsPerformed)
    {
        config.setMaxOpsPerformed(maxOpsPerformed);
    }
    
    public final boolean isAsyncOps()
    {
        return config.isAsyncOps();
    }
    
    public final int getMaxPDULengthReceive()
    {
        return config.getMaxPDULengthReceive();
    }
    
    public final void setMaxPDULengthReceive(int length)
    {
        config.setMaxPDULengthReceive(length);
    }
    
    public final int getMaxPDULengthSend()
    {
        return config.getMaxPDULengthSend();
    }
    
    public final void setMaxPDULengthSend(int length)
    {
        config.setMaxPDULengthSend(length);
    }

    public final boolean isPackPDV()
    {
        return config.isPackPDV();
    }

    public final void setPackPDV(boolean packPDV)
    {
        config.setPackPDV(packPDV);
    }

    public final int getDimseRspTimeout()
    {
        return config.getDimseRspTimeout();
    }

    public final void setDimseRspTimeout(int timeout)
    {
        config.setDimseRspTimeout(timeout);
    }
    

    public final int getIdleTimeout()
    {
        return config.getIdleTimeout();
    }

    public final void setIdleTimeout(int timeout)
    {
        config.setIdleTimeout(timeout);
    }

    public final int getMoveRspTimeout()
    {
        return config.getMoveRspTimeout();
    }

    public final void setMoveRspTimeout(int timeout)
    {
        config.setMoveRspTimeout(timeout);
    }
    
    
    public Association connect(NetworkApplicationEntity remoteAE)
    throws ConfigurationException, IOException, InterruptedException
    {
        return connect(remoteAE, getTransferCapability());
    }

    public Association connect(NetworkApplicationEntity remoteAE,
            TransferCapability[] tc)
    throws ConfigurationException, IOException, InterruptedException
    {
        if (!config.isAssociationInitiator())
            throw new ConfigurationException(
                    "Local AE " + config.getAETitle() + " does not initiate Associations");
        if (!remoteAE.isInstalled())
            throw new ConfigurationException(
                    "Remote AE " + remoteAE.getAETitle() + " not installed");
        if (!remoteAE.isAssociationAcceptor())
            throw new ConfigurationException(
                    "Remote AE " + remoteAE.getAETitle() + " does not accept Associations");
        NetworkConnection[] remoteConns = remoteAE.getNetworkConnection();
        for (Iterator iter = connectors.iterator(); iter.hasNext();)
        {
            Connector c = (Connector) iter.next();
            if (!c.isInstalled())
                continue;
            for (int i = 0; i < remoteConns.length; i++)
            {
                NetworkConnection nc = remoteConns[i];
                if (nc.isInstalled() && nc.isListening() 
                        && c.isTLS() == nc.isTLS())
                {
                    AAssociateRQ rq = makeAAssociateRQ(remoteAE, tc);
                    Socket s = c.connect(nc);
                    Association a = Association.request(s, c, this);
                    device.getExecutor().execute(a);
                    a.negotiate(rq);
                    return a;                    
                }
            }
        }
        throw new ConfigurationException(
                "No compatible Network Connection between local AE "
                    + config.getAETitle() + " and remote AE "
                    + remoteAE.getAETitle());
    }
    
    private AAssociateRQ makeAAssociateRQ(NetworkApplicationEntity remoteAE,
            TransferCapability[] tc) throws ConfigurationException
    {
        AAssociateRQ aarq = new AAssociateRQ();
        aarq.setCallingAET(getAETitle());
        aarq.setCalledAET(remoteAE.getAETitle());
        aarq.setMaxPDULength(getMaxPDULengthReceive());
        aarq.setMaxOpsInvoked(getMaxOpsInvoked());
        aarq.setMaxOpsPerformed(getMaxOpsPerformed());
        
        LinkedHashMap as2ts = new LinkedHashMap();
        LinkedHashSet asscp = new LinkedHashSet();
        TransferCapability[] rtcs = remoteAE.getTransferCapability();
        for (int i = 0; i < tc.length; i++)
        {
            TransferCapability ltc = tc[i]; 
            String cuid = ltc.getSopClass();
            if (rtcs.length != 0)
            {
                for (int j = 0; j < rtcs.length; j++)
                {
                    TransferCapability rtc = rtcs[i];
                    if (ltc.isSCU() == rtc.isSCP()
                            && cuid.equals(rtc.getSopClass()))
                    {
                        LinkedHashSet ts0 = new LinkedHashSet(
                                Arrays.asList(ltc.getTransferSyntax()));
                        ts0.retainAll(Arrays.asList(rtc.getTransferSyntax()));
                        if (ts0.isEmpty())
                            continue;
                        LinkedHashSet ts = (LinkedHashSet) as2ts.get(cuid);
                        if (ts == null)
                            as2ts.put(cuid, ts0);
                        else
                            ts.addAll(ts0);
                        if (ltc.isSCP())
                            asscp.add(cuid);
                    }
                }
            }
            else
            {
                LinkedHashSet ts0 = new LinkedHashSet(
                        Arrays.asList(ltc.getTransferSyntax()));
                LinkedHashSet ts = (LinkedHashSet) as2ts.get(cuid);
                if (ts == null)
                    as2ts.put(cuid, ts0);
                else
                    ts.addAll(ts0);
                if (ltc.isSCP())
                    asscp.add(cuid);
            }
        }
        if (as2ts.isEmpty())
            throw new ConfigurationException(
                    "No common Transfer Capability between local AE "
                    + config.getAETitle() + " and remote AE "
                    + remoteAE.getAETitle());
        int available = 128 - as2ts.size();
        int pcid = 1;
        for (Iterator iter = as2ts.entrySet().iterator(); iter.hasNext();)
        {
            Map.Entry e = (Map.Entry) iter.next();
            String asuid = (String) e.getKey();
            LinkedHashSet ts = (LinkedHashSet) e.getValue();
            int expand = Math.min(available, ts.size()-1);
            PresentationContext pc = new PresentationContext();
            pc.setAbstractSyntax(asuid);
            for (Iterator it = ts.iterator(); it.hasNext(); --expand)
            {
                if (expand > 0)
                {
                    PresentationContext pc1 = new PresentationContext();
                    pc1.setPCID(pcid);
                    pc1.setAbstractSyntax(asuid);
                    pc1.addTransferSyntax((String) it.next());
                    aarq.addPresentationContext(pc1);
                    ++pcid;
                    ++pcid;
                }
                else
                {
                    pc.addTransferSyntax((String) it.next());                    
                }
            }
            pc.setPCID(pcid);
            aarq.addPresentationContext(pc);
            ++pcid;
            ++pcid;
         }
        return aarq;
    }

    public void register(DicomService service)
    {
        serviceRegistry.register(service);        
    }

    public void perform(Association as, int pcid, DicomObject cmd, 
            PDVInputStream dataStream, String tsuid)
    {
        serviceRegistry.process(as, pcid, cmd, dataStream, tsuid);        
    }

    AAssociateAC negotiate(Association a, AAssociateRQ rq)
    throws AAssociateRJException
    {
        if (!config.isAssociationAcceptor())
            throw new AAssociateRJException(
                    AAssociateRJException.RESULT_REJECTED_PERMANENT,
                    AAssociateRJException.SOURCE_SERVICE_USER,
                    AAssociateRJException.REASON_NO_REASON_GIVEN);
        String[] calling = config.getPreferredCallingAETitle();
        if (calling.length != 0 
                && Arrays.asList(calling).indexOf(rq.getCallingAET()) == -1)
            throw new AAssociateRJException(
                    AAssociateRJException.RESULT_REJECTED_PERMANENT,
                    AAssociateRJException.SOURCE_SERVICE_USER,
                    AAssociateRJException.REASON_CALLING_AET_NOT_RECOGNIZED);        
        if (!config.isInstalled())
            throw new AAssociateRJException(
                    AAssociateRJException.RESULT_REJECTED_TRANSIENT,
                    AAssociateRJException.SOURCE_SERVICE_USER,
                    AAssociateRJException.REASON_NO_REASON_GIVEN);
        AAssociateAC ac = new AAssociateAC();
        ac.setCalledAET(rq.getCalledAET());
        ac.setCallingAET(rq.getCallingAET());
        ac.setMaxPDULength(getMaxPDULengthReceive());
        ac.setMaxOpsInvoked(Math.min(rq.getMaxOpsInvoked(), getMaxOpsPerformed()));
        ac.setMaxOpsPerformed(Math.min(rq.getMaxOpsPerformed(), getMaxOpsInvoked()));
        Collection pcs = rq.getPresentationContexts();
        for (Iterator iter = pcs.iterator(); iter.hasNext();)
        {
            PresentationContext pc = (PresentationContext) iter.next();
            ac.addPresentationContext(negotiate(pc));
        }
        return ac;
    }


    private PresentationContext negotiate(PresentationContext rqpc)
    {
        PresentationContext acpc = new PresentationContext();
        acpc.setResult(PresentationContext.ABSTRACT_SYNTAX_NOT_SUPPORTED);
        acpc.setPCID(rqpc.getPCID());
        final String asuid = rqpc.getAbstractSyntax();
        final Set rqts = rqpc.getTransferSyntaxes();
        TransferCapability[] tc = getTransferCapability();        
        for (int i = 0; i < tc.length; i++)
        {
            if (asuid.equals(tc[i].getSopClass()))
            {
                String[] ts = tc[i].getTransferSyntax();
                for (int j = 0; j < ts.length; j++)
                {
                    if (rqts.contains(ts[j]))
                    {                       
                        acpc.setResult(PresentationContext.ACCEPTANCE);
                        acpc.addTransferSyntax(ts[j]);
                        return acpc;
                    }
                }
                acpc.setResult(PresentationContext.TRANSFER_SYNTAX_NOT_SUPPORTED);                
            }
        }
        return acpc;
    }


}
