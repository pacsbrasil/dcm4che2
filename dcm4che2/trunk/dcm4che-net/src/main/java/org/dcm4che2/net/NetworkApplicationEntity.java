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
public class NetworkApplicationEntity
{
    private boolean associationAcceptor;
    private boolean associationInitiator;
    private String aeTitle;
    private String description;
    private Object[] vendorData = {};
    private String[] applicationCluster = {};
    private String[] preferredCallingAETitle = {};
    private String[] preferredCalledAETitle = {};
    private String[] supportedCharacterSet = {};
    private Boolean installed;

    private int maxOpsInvoked = Integer.MAX_VALUE;
    private int maxOpsPerformed = Integer.MAX_VALUE;
    private int maxPDULengthReceive = 0x4000; //=16384
    private int maxPDULengthSend = 0x4000;
    private boolean packPDV;
    private int dimseRspTimeout = 60000;
    private int moveRspTimeout = 600000;
    private int idleTimeout = 60000;
    private String[] reuseAssocationToAETitle = {};
    private String[] reuseAssocationFromAETitle = {};
    
    private NetworkConnection[] networkConnection = {};
    private TransferCapability[] transferCapability = {};

    private final DicomServiceRegistry serviceRegistry = new DicomServiceRegistry();
    private final ArrayList pool = new ArrayList();
    private Device device;

    public final Device getDevice()
    {
        return device;
    }

    final void setDevice(Device device)
    {
        this.device = device;
    }
        
    public final String getAETitle()
    {
        return aeTitle;
    }

    public final void setAETitle(String aetitle)
    {
        this.aeTitle = aetitle;
    }

    public final String[] getApplicationCluster()
    {
        return applicationCluster;
    }

    public final void setApplicationCluster(String[] cluster)
    {
        this.applicationCluster = cluster;
    }

    public final boolean isAssociationAcceptor()
    {
        return associationAcceptor;
    }

    public final void setAssociationAcceptor(boolean acceptor)
    {
        this.associationAcceptor = acceptor;
    }

    public final boolean isAssociationInitiator()
    {
        return associationInitiator;
    }

    public final void setAssociationInitiator(boolean initiator)
    {
        this.associationInitiator = initiator;
    }

    public final String getDescription()
    {
        return description;
    }

    public final void setDescription(String description)
    {
        this.description = description;
    }

    public final boolean isInstalled()
    {
        return installed != null ? installed.booleanValue() 
                                 : device == null || device.isInstalled();
    }

    public final void setInstalled(boolean installed)
    {
        this.installed = Boolean.valueOf(installed);
    }

    public final NetworkConnection[] getNetworkConnection()
    {
        return networkConnection;
    }

    public final void setNetworkConnection(NetworkConnection nc)
    {
        setNetworkConnection(new NetworkConnection[]{nc});
    }
    
    public final void setNetworkConnection(NetworkConnection[] nc)
    {
        this.networkConnection = nc;
    }

    public final String[] getPreferredCalledAETitle()
    {
        return preferredCalledAETitle;
    }

    public final boolean hasPreferredCalledAETitle()
    {
        return preferredCalledAETitle != null
                && preferredCalledAETitle.length > 0;
    }
    
    public boolean isPreferredCalledAETitle(String aet)
    {
        return contains(preferredCalledAETitle, aet);
    }
    
    private static boolean contains(String[] a, String s)
    {
        for (int i = 0; i < a.length; i++)
            if (s.equals(a[i]))
                return true;
        return false;
    }

    public final void setPreferredCalledAETitle(String[] aets)
    {
        this.preferredCalledAETitle = aets;
    }

    public final String[] getPreferredCallingAETitle()
    {
        return preferredCallingAETitle;
    }

    public final boolean hasPreferredCallingAETitle()
    {
        return preferredCallingAETitle != null
                && preferredCallingAETitle.length > 0;
    }
    
    public boolean isPreferredCallingAETitle(String aet)
    {
        return contains(preferredCallingAETitle, aet);
    }
    
    public final void setPreferredCallingAETitle(String[] aets)
    {
        this.preferredCallingAETitle = aets;
    }

    public final String[] getSupportedCharacterSet()
    {
        return supportedCharacterSet;
    }

    public final void setSupportedCharacterSet(String[] characterSets)
    {
        this.supportedCharacterSet = characterSets;
    }

    public final TransferCapability[] getTransferCapability()
    {
        return transferCapability;
    }

    public final void setTransferCapability(TransferCapability[] transferCapability)
    {
        this.transferCapability = transferCapability;
    }

    public final Object[] getVendorData()
    {
        return vendorData;
    }

    public final void setVendorData(Object[] vendorData)
    {
        this.vendorData = vendorData;
    }

    public final int getMaxOpsInvoked()
    {
        return maxOpsInvoked;
    }
    
    public final void setMaxOpsInvoked(int maxOpsInvoked)
    {
        this.maxOpsInvoked = maxOpsInvoked;
    }
    
    public final int getMaxOpsPerformed()
    {
        return maxOpsPerformed;
    }
    
    public final void setMaxOpsPerformed(int maxOpsPerformed)
    {
        this.maxOpsPerformed = maxOpsPerformed;
    }
    
    public final boolean isAsyncOps()
    {
        return maxOpsInvoked != 1 || maxOpsPerformed != 1;
    }
    
    public final int getMaxPDULengthReceive()
    {
        return maxPDULengthReceive;
    }
    
    public final void setMaxPDULengthReceive(int maxPDULengthReceive)
    {
        this.maxPDULengthReceive = maxPDULengthReceive;
    }
    
    public final int getMaxPDULengthSend()
    {
        return maxPDULengthSend;
    }
    
    public final void setMaxPDULengthSend(int maxPDULengthSend)
    {
        this.maxPDULengthSend = maxPDULengthSend;
    }

    public final boolean isPackPDV()
    {
        return packPDV;
    }

    public final void setPackPDV(boolean packPDV)
    {
        this.packPDV = packPDV;
    }

    public final int getDimseRspTimeout()
    {
        return dimseRspTimeout ;
    }

    public final void setDimseRspTimeout(int dimseRspTimeout)
    {
        this.dimseRspTimeout = dimseRspTimeout;
    }

    public final int getIdleTimeout()
    {
        return idleTimeout;
    }

    public final void setIdleTimeout(int idleTimeout)
    {
        this.idleTimeout = idleTimeout;
    }

    public final int getMoveRspTimeout()
    {
        return moveRspTimeout;
    }

    public final void setMoveRspTimeout(int moveRspTimeout)
    {
        this.moveRspTimeout = moveRspTimeout;
    }

    public final String[] getReuseAssocationFromAETitle()
    {
        return reuseAssocationFromAETitle;
    }

    public final void setReuseAssocationFromAETitle(
            String[] reuseAssocationFromAETitle)
    {
        this.reuseAssocationFromAETitle = reuseAssocationFromAETitle;
    }

    public final String[] getReuseAssocationToAETitle()
    {
        return reuseAssocationToAETitle;
    }

    public final void setReuseAssocationToAETitle(String[] reuseAssocationToAETitle)
    {
        this.reuseAssocationToAETitle = reuseAssocationToAETitle;
    }

    public Association connect(NetworkApplicationEntity remoteAE, 
            Executor executor)
    throws ConfigurationException, IOException, InterruptedException
    {
        return connect(remoteAE, executor, false);
    }
    
    public Association connect(NetworkApplicationEntity remoteAE, 
            Executor executor, boolean forceNew)
    throws ConfigurationException, IOException, InterruptedException
    {
        final String remoteAET = remoteAE.getAETitle();
        if (!forceNew && !pool.isEmpty() &&
                (reuseAssocationToAETitle.length > 0 || 
                 reuseAssocationFromAETitle.length > 0))
        {
            final boolean reuseAssocationTo = 
                Arrays.asList(reuseAssocationToAETitle).indexOf(remoteAET) != -1;
            final boolean reuseAssocationFrom = 
                Arrays.asList(reuseAssocationFromAETitle).indexOf(remoteAET) != -1;
            synchronized (pool)
            {
                for (Iterator iter = pool.iterator(); iter.hasNext();)
                {
                    Association as = (Association) iter.next();
                    if (!remoteAET.equals(as.getRemoteAET()))
                        continue;
                    if (as.isReadyForDataTransfer() && (as.isRequestor() 
                            ? reuseAssocationTo : reuseAssocationFrom))
                        return as;
                }
            }
        }
        NetworkConnection[] remoteConns = remoteAE.getNetworkConnection();
        for (int i = 0; i < networkConnection.length; i++)
        {
            NetworkConnection c = networkConnection[i];
            if (!networkConnection[i].isInstalled())
                continue;
            for (int j = 0; j < remoteConns.length; j++)
            {
                NetworkConnection nc = remoteConns[j];
                if (nc.isInstalled() && nc.isListening() 
                        && c.isTLS() == nc.isTLS())
                {
                    AAssociateRQ rq = makeAAssociateRQ(remoteAE);
                    Socket s = c.connect(nc);
                    Association a = Association.request(s, c, this);
                    executor.execute(a);
                    a.negotiate(rq);
                    addToPool(a);
                    return a;                    
                }
            }
        }
        throw new ConfigurationException(
                "No compatible Network Connection between local AE "
                    + aeTitle + " and remote AE " + remoteAET);
    }
    
    private AAssociateRQ makeAAssociateRQ(NetworkApplicationEntity remoteAE)
    throws ConfigurationException
    {
        AAssociateRQ aarq = new AAssociateRQ();
        aarq.setCallingAET(aeTitle);
        aarq.setCalledAET(remoteAE.getAETitle());
        aarq.setMaxPDULength(maxPDULengthReceive);
        aarq.setMaxOpsInvoked(maxOpsInvoked);
        aarq.setMaxOpsPerformed(maxOpsPerformed);
        
        LinkedHashMap as2ts = new LinkedHashMap();
        LinkedHashSet asscp = new LinkedHashSet();
        TransferCapability[] remoteTC = remoteAE.getTransferCapability();
        for (int i = 0; i < transferCapability.length; i++)
        {
            TransferCapability localTC = transferCapability[i]; 
            String cuid = localTC.getSopClass();
            // consider Transfer Capabilities of Remote AE if available
            if (remoteTC.length != 0)
            {
                for (int j = 0; j < remoteTC.length; j++)
                {
                    TransferCapability rtc = remoteTC[j];
                    if (localTC.isSCU() == rtc.isSCP()
                            && cuid.equals(rtc.getSopClass()))
                    {
                        LinkedHashSet ts0 = new LinkedHashSet(
                                Arrays.asList(localTC.getTransferSyntax()));
                        ts0.retainAll(Arrays.asList(rtc.getTransferSyntax()));
                        if (ts0.isEmpty())
                            continue;
                        LinkedHashSet ts = (LinkedHashSet) as2ts.get(cuid);
                        if (ts == null)
                            as2ts.put(cuid, ts0);
                        else
                            ts.addAll(ts0);
                        if (localTC.isSCP())
                            asscp.add(cuid);
                    }
                }
            }
            else
            {
                LinkedHashSet ts0 = new LinkedHashSet(
                        Arrays.asList(localTC.getTransferSyntax()));
                LinkedHashSet ts = (LinkedHashSet) as2ts.get(cuid);
                if (ts == null)
                    as2ts.put(cuid, ts0);
                else
                    ts.addAll(ts0);
                if (localTC.isSCP())
                    asscp.add(cuid);
            }
        }
        if (as2ts.isEmpty())
            throw new ConfigurationException(
                    "No common Transfer Capability between local AE "
                    + getAETitle() + " and remote AE "
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

    public void unregister(DicomService service)
    {
        serviceRegistry.unregister(service);        
    }
    
    void addToPool(Association a)
    {
        synchronized (pool)
        {
            pool.add(a);
        }        
    }    

    void removeFromPool(Association a)
    {
        synchronized (pool)
        {
            pool.remove(a);
        }        
    }
        
    void perform(Association as, int pcid, DicomObject cmd, 
            PDVInputStream dataStream, String tsuid)
    {
        serviceRegistry.process(as, pcid, cmd, dataStream, tsuid);        
    }

    AAssociateAC negotiate(Association a, AAssociateRQ rq)
    throws AAssociateRJException
    {
        if (!isAssociationAcceptor())
            throw new AAssociateRJException(
                    AAssociateRJException.RESULT_REJECTED_PERMANENT,
                    AAssociateRJException.SOURCE_SERVICE_USER,
                    AAssociateRJException.REASON_NO_REASON_GIVEN);
        String[] calling = getPreferredCallingAETitle();
        if (calling.length != 0 
                && Arrays.asList(calling).indexOf(rq.getCallingAET()) == -1)
            throw new AAssociateRJException(
                    AAssociateRJException.RESULT_REJECTED_PERMANENT,
                    AAssociateRJException.SOURCE_SERVICE_USER,
                    AAssociateRJException.REASON_CALLING_AET_NOT_RECOGNIZED);        
        if (!isInstalled())
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
