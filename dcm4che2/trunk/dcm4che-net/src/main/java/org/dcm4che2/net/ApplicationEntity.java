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
import org.dcm4che2.net.service.BasicDicomServiceRegistry;
import org.dcm4che2.net.service.DicomService;

/**
 * @author gunter zeilinger(gunterze@gmail.com)
 * @version $Revision$ $Date$
 * @since Nov 25, 2005
 *
 */
public class ApplicationEntity
{
    private final Device device;
    private final NetworkApplicationEntity config;
    private final BasicDicomServiceRegistry serviceRegistry;
    private NetworkConnection conn1;
    private NetworkConnection conn2;

    public ApplicationEntity(Device device, NetworkApplicationEntity config)
    {
        if (device == null)
            throw new NullPointerException("device");
        if (config == null)
            throw new NullPointerException("config");
        this.device = device;
        this.config = config;
        this.serviceRegistry = new BasicDicomServiceRegistry(device.getServiceRegistry());
        NetworkConnection[] conns = config.getNetworkConnection();
        for (int i = 0; i < conns.length; i++)
        {
            if (!conns[i].isInstalled())
                continue;
            if (conn1 == null)
            {
                conn1 = conns[i];
            }
            else if (conn1.isTLS() != conns[i].isTLS())
            {
                conn2 = conns[i];
                break;
            }
        }
    }


    public final NetworkApplicationEntity getConfiguration()
    {
        return config;
    }
    
    public final String getAETitle()
    {
        return config.getAETitle();
    }

    public final int getMaxPDULengthSend()
    {
        return config.getMaxPDULengthSend();
    }

    public final int getMaxPDULengthReceive()
    {
        return config.getMaxPDULengthReceive();
    }

    public final int getMaxOpsPerformed()
    {
        return config.getMaxOpsPerformed();
    }

    public final int getMaxOpsInvoked()
    {
        return config.getMaxOpsInvoked();
    }
    
    public final boolean isPackPDV()
    {
        return config.isPackPDV();
    }

    public Association connect(NetworkApplicationEntity remoteAE)
    throws ConfigurationException, IOException, InterruptedException
    {
        return connect(remoteAE, getTransferCapability());
    }


    public TransferCapability[] getTransferCapability()
    {
        return config.getTransferCapability();
    }
    
    public Association connect(NetworkApplicationEntity remoteAE,
            TransferCapability[] tc)
    throws ConfigurationException, IOException, InterruptedException
    {
        if (conn1 == null)
            throw new ConfigurationException(
                    "No Network Connection configured at local AE " 
                    + config.getAETitle());
        if (!config.isAssociationInitiator())
            throw new ConfigurationException(
                    "Local AE " + config.getAETitle() + " does not initiate Associations");
        if (!remoteAE.isInstalled())
            throw new ConfigurationException(
                    "Remote AE " + remoteAE.getAETitle() + " not installed");
        if (!remoteAE.isAssociationAcceptor())
            throw new ConfigurationException(
                    "Remote AE " + remoteAE.getAETitle() + " does not accept Associations");
        NetworkConnection localConn = null;        
        NetworkConnection remoteConn = null;       
        NetworkConnection[] conns = remoteAE.getNetworkConnection();
        for (int i = 0; i < conns.length; i++)
        {
            NetworkConnection conn = conns[i];
            if (!conn.isInstalled() || !conn.isListening())
                continue;
            if (conn1.isTLS() == conn.isTLS())
            {
                localConn = conn1;
                remoteConn = conn;
                break;
            }
            if (localConn == null && conn2 != null
                    && conn2.isTLS() == conn.isTLS())
            {
                localConn = conn2;
                remoteConn = conn;
            }
         }
        if (localConn == null)
            throw new ConfigurationException(
                    "No compatible Network Connection between local AE "
                        + config.getAETitle() + " and remote AE "
                        + remoteAE.getAETitle());
        AAssociateRQ rq = makeAAssociateRQ(remoteAE, tc);
        Connector connector = device.getConnector(localConn);
        Socket s = connector.connect(remoteConn);
        Association a = Association.request(s, connector, this);
        device.getExecutor().execute(a);
        a.negotiate(rq);
        return a;
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
            if (rtcs != null)
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

    public void process(Association as, int pcid, DicomObject cmd, 
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
        ac.setMaxOpsInvoked(getMaxOpsPerformed());
        ac.setMaxOpsPerformed(getMaxOpsInvoked());
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
