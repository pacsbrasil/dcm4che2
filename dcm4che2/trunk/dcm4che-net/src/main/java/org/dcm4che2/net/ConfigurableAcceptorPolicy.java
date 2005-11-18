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

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import org.dcm4che2.config.NetworkApplicationEntity;
import org.dcm4che2.config.NetworkApplicationEntityExt;
import org.dcm4che2.config.TransferCapability;
import org.dcm4che2.data.UID;
import org.dcm4che2.net.pdu.AAssociateAC;
import org.dcm4che2.net.pdu.AAssociateRJ;
import org.dcm4che2.net.pdu.AAssociateRQ;
import org.dcm4che2.net.pdu.PDU;
import org.dcm4che2.net.pdu.PresentationContext;
import org.dcm4che2.net.pdu.RoleSelection;

/**
 * @author gunter zeilinger(gunterze@gmail.com)
 * @version $Revision$ $Date$
 * @since Oct 26, 2005
 *
 */
public class ConfigurableAcceptorPolicy implements AcceptorPolicy
{
    private static class Entry {
        final NetworkApplicationEntity ae;
        final NetworkApplicationEntityExt ext;
        Entry(NetworkApplicationEntity ae,
                NetworkApplicationEntityExt ext)
        {
            this.ae = ae;
            this.ext = ext;
        }
    }
    private static NetworkApplicationEntityExt defExt =
            new NetworkApplicationEntityExt();
    
    private Entry unkownAE;
    private HashMap map = new HashMap();

    public void addNetworkAE(NetworkApplicationEntity networkAE,
            NetworkApplicationEntityExt aeExt)
    {
        String aet = networkAE.getAEtitle();
        Entry entry = new Entry(networkAE, aeExt != null ? aeExt : defExt);
        if (aet != null && aet.length() != 0)
            map.put(aet, entry);
        else
            unkownAE = entry;
    }
    
    public PDU negotiate(Association as, AAssociateRQ rq)
    {
        if (!UID.DICOMApplicationContextName.equals(rq.getApplicationContext()))
            return new AAssociateRJ(
                    AAssociateRJ.RESULT_REJECTED_PERMANENT,
                    AAssociateRJ.SOURCE_SERVICE_USER,
                    AAssociateRJ.REASON_APP_CTX_NAME_NOT_SUPPORTED);

        String calledAET = rq.getCalledAET();
        Entry e = (Entry) map.get(calledAET);
        if (e == null)
            e = unkownAE;
        if (e == null || !e.ae.isAssociationAcceptor())
            return new AAssociateRJ(
                    AAssociateRJ.RESULT_REJECTED_PERMANENT,
                    AAssociateRJ.SOURCE_SERVICE_USER,
                    AAssociateRJ.REASON_CALLED_AET_NOT_RECOGNIZED);
        if (e.ae.hasPreferredCallingAETitle() 
                && !e.ae.isPreferredCallingAETitle(rq.getCallingAET()))
            return new AAssociateRJ(
                    AAssociateRJ.RESULT_REJECTED_PERMANENT,
                    AAssociateRJ.SOURCE_SERVICE_USER,
                    AAssociateRJ.REASON_CALLING_AET_NOT_RECOGNIZED);
        return negotiate(as, rq, e.ae, e.ext);
    }

    private PDU negotiate(Association as, AAssociateRQ rq,
            NetworkApplicationEntity ae, NetworkApplicationEntityExt ext)
    {        
        AAssociateAC ac = new AAssociateAC();
        ac.setApplicationContext(UID.DICOMApplicationContextName);
        ac.setCalledAET(rq.getCalledAET());
        ac.setCallingAET(rq.getCallingAET());
        Collection c = rq.getPresentationContexts();
        for (Iterator iter = c.iterator(); iter.hasNext();)
        {
            PresentationContext rqpc = (PresentationContext) iter.next();
            String asuid = rqpc.getAbstractSyntax();
            RoleSelection rqRoleSel = rq.getRoleSelectionFor(asuid);
            TransferCapability scu = null;
            TransferCapability scp = null;
            TransferCapability[] tcs = ae.getTransferCapability();
            for (int i = 0; i < tcs.length; ++i)
            {
                final TransferCapability tc = tcs[i];
                if (asuid.equals(tc.getSopClass()))
                {
                    if (tc.isSCP())
                        scp = tc;
                    else if (rqRoleSel != null)
                        scu = tc;
                }                
            }
            PresentationContext acpc = new PresentationContext();
            acpc.setPCID(rqpc.getPCID());
            if (scp == null && scu == null)
            {
                acpc.setResult(PresentationContext.ABSTRACT_SYNTAX_NOT_SUPPORTED);
                acpc.addTransferSyntax(rqpc.getTransferSyntax());
            }
            else
            {
                TransferCapability tc = scu == null 
                        || (scp != null && rqRoleSel.isSCP()) ? scp : scu;
                String tsuid = selectTransferSyntax(tc.getTransferSyntax(), 
                        rqpc.getTransferSyntaxes());
                if (tsuid == null)
                {
                    acpc.setResult(PresentationContext.TRANSFER_SYNTAX_NOT_SUPPORTED);
                    acpc.addTransferSyntax(rqpc.getTransferSyntax());
                }
                else
                {
                    acpc.setResult(PresentationContext.ACCEPTANCE);
                    acpc.addTransferSyntax(tsuid);
                    if (rqRoleSel != null)
                    {
                        ac.addRoleSelection(
                                new RoleSelection(asuid, 
                                        rqRoleSel.isSCU() && scu != null,
                                        rqRoleSel.isSCP() && scp != null));
                    }
                }
            }
            ac.addPresentationContext(acpc);
        }
        ac.setMaxPDULength(ext.getMaxPDULengthReceive());
        if (rq.isAsyncOps() && ext.isAsyncOps())
        {
            ac.setMaxOpsInvoked(maxOps(rq.getMaxOpsInvoked(),
                    ext.getMaxOpsPerformed()));
            ac.setMaxOpsPerformed(maxOps(rq.getMaxOpsPerformed(),
                    ext.getMaxOpsInvoked()));
        }
        return ac;
    }

    private static int maxOps(int a, int b)
    {
        return a == 0 ? b : b == 0 ? a : Math.min(a, b);
    }

    private String selectTransferSyntax(String[] supported, Set offered)
    {
        for (int i = 0; i < supported.length; i++)
        {
            String tsuid = supported[i];
            if (offered.contains(tsuid))
                return tsuid;
        }
        return null;
    }

}
