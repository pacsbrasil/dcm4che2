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
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Collection;
import java.util.Iterator;

import org.apache.mina.io.filter.IoThreadPoolFilter;
import org.apache.mina.protocol.filter.ProtocolThreadPoolFilter;
import org.dcm4che2.data.BasicDicomObject;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.UID;
import org.dcm4che2.data.VR;
import org.dcm4che2.net.pdu.AAbort;
import org.dcm4che2.net.pdu.AAssociateAC;
import org.dcm4che2.net.pdu.AAssociateRJ;
import org.dcm4che2.net.pdu.AAssociateRQ;
import org.dcm4che2.net.pdu.AReleaseRP;
import org.dcm4che2.net.pdu.AReleaseRQ;
import org.dcm4che2.net.pdu.PresentationContext;

public class Server
{
    private static SocketAddress makeSocketAddress()
    {
        return new InetSocketAddress(11112);
    }

    private static AAssociateAC makeAAAC(AAssociateRQ rq)
    {
        AAssociateAC ac = new AAssociateAC();
        ac.setCallingAET(rq.getCallingAET());
        ac.setCalledAET(rq.getCalledAET());
        Collection pcs = rq.getPresentationContexts();
        for (Iterator iter = pcs.iterator(); iter.hasNext();)
        {
            PresentationContext rqpc = (PresentationContext) iter.next();
            PresentationContext acpc = new PresentationContext();
            acpc.setPCID(rqpc.getPCID());
            acpc.setResult(
                    UID.VerificationSOPClass.equals(rqpc.getAbstractSyntax())
                            ? PresentationContext.ACCEPTANCE
                            : PresentationContext.ABSTRACT_SYNTAX_NOT_SUPPORTED);
            acpc.addTransferSyntax(rqpc.getTransferSyntax());
            ac.addPresentationContext(acpc);
        }
        return ac ;
    }

    private static DicomObject makeEchoRSP(DicomObject rq)
    {
        BasicDicomObject rsp = new BasicDicomObject();
        rsp.putString(Tag.AffectedSOPClassUID, VR.UI, UID.VerificationSOPClass);
        rsp.putInt(Tag.CommandField, VR.US, 0x8030);
        rsp.putInt(Tag.MessageIDBeingRespondedTo, VR.US, rq.getInt(Tag.MessageID));
        rsp.putInt(Tag.DataSetType, VR.US, 0x101);
        rsp.putInt(Tag.Status, VR.US, 0);
        return rsp;
    }
    
    static AssociationHandler listener = new AssociationHandler()
    {

        public void onOpened(Association a)
        {
        }

        public void onAAssociateRQ(Association a, AAssociateRQ rq)
        {
            a.write(makeAAAC(rq));
        }

        public void onAAssociateAC(Association a, AAssociateAC ac)
        {
        }

        public void onAAssociateRJ(Association a, AAssociateRJ rj)
        {
        }

        public void onAReleaseRQ(Association a, AReleaseRQ rq)
        {
            a.write(new AReleaseRP());
        }

        public void onAReleaseRP(Association a, AReleaseRP rp)
        {
        }

        public void onAbort(Association a, AAbort abort)
        {
        }

        public void onDIMSE(Association a, int pcid, DicomObject command, 
                InputStream dataStream)
        {
            try
            {
                a.write(pcid, makeEchoRSP(command), null);
            } catch (IOException e)
            {
                e.printStackTrace();
            }
        }

        public void onClosed(Association association)
        {
        }
    };

    /**
     * @param args
     */
    public static void main(String[] args)
    {
        IoThreadPoolFilter ioThreadPoolFilter = new IoThreadPoolFilter();
        ProtocolThreadPoolFilter protocolThreadPoolFilter = new ProtocolThreadPoolFilter();

        ioThreadPoolFilter.start();
        protocolThreadPoolFilter.start();

        AssociationAcceptor acceptor = new AssociationAcceptor();
        // acceptor.setIoThreadPoolFilter(ioThreadPoolFilter);
        // acceptor.setProtocolThreadPoolFilter(protocolThreadPoolFilter);
        try
        {
            acceptor.bind(listener, makeSocketAddress());
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

}
