/*
 *   @(#) $Id$
 *
 *   Copyright 2004 The Apache Software Foundation
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */
package org.dcm4che2.net;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;

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

/**
 * (<strong>Entry Point</strong>) Starts SumUp client.
 * 
 * @author The Apache Directory Project
 * @version $Rev: 264677 $, $Date$
 */
public class Client
{

    private static final int PCID = 1;
    
    private static AAssociateRQ makeAARQ()
    {
        AAssociateRQ aarq = new AAssociateRQ();
        PresentationContext pc = new PresentationContext();
        pc.setPCID(PCID);
        pc.setAbstractSyntax(UID.VerificationSOPClass);
        pc.addTransferSyntax(UID.ImplicitVRLittleEndian);
        aarq.addPresentationContext(pc);
        return aarq;
    }

    private static DicomObject makeEchoRQ()
    {
        BasicDicomObject cmd = new BasicDicomObject();
        cmd.putString(Tag.AffectedSOPClassUID, VR.UI, UID.VerificationSOPClass);
        cmd.putInt(Tag.CommandField, VR.US, 0x30);
        cmd.putInt(Tag.MessageID, VR.US, 1);
        cmd.putInt(Tag.DataSetType, VR.US, 0x101);
        return cmd;
    }

    
    private static AssociationHandler handler = new AssociationHandler()
    {

        public void onOpened(Association a)
        {
            a.write(makeAARQ());
        }

        public void onAAssociateRQ(Association a, AAssociateRQ rq)
        {
        }

        public void onAAssociateRJ(Association a, AAssociateRJ rj)
        {
        }

        public void onAAssociateAC(Association a, AAssociateAC ac)
        {
            try
            {
                a.write(PCID, makeEchoRQ(), null);
            } catch (IOException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
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
            a.write(new AReleaseRQ());
        }

        public void onClosed(Association association)
        {         
        }
    };

    public static void main(String[] args) throws Throwable
    {
        IoThreadPoolFilter ioThreadPoolFilter = new IoThreadPoolFilter();
        ProtocolThreadPoolFilter protocolThreadPoolFilter = new ProtocolThreadPoolFilter();

//        assocThreadPool.start();
//        ioThreadPoolFilter.start();
//        protocolThreadPoolFilter.start();

        AssociationRequestor connector = new AssociationRequestor();
        // connector.setIoThreadPoolFilter(ioThreadPoolFilter);
        // connector.setProtocolThreadPoolFilter(protocolThreadPoolFilter);
        Association service = connector.connect(handler, makeSocketAddress());
        System.out.println("Wait for STA1");
        synchronized (service)
        {
            while (service.getState() != Association.STA1)
                service.wait();
        }
//        System.out.println("Stop Threads");
//        ioThreadPoolFilter.stop();
//        protocolThreadPoolFilter.stop();
        System.out.println("Exit main");
    }

    private static InetSocketAddress makeSocketAddress()
    {
        return new InetSocketAddress("localhost", 11112);
    }
}
