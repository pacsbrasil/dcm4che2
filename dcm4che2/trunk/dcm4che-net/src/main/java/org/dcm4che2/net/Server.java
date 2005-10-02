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
