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

import org.dcm4che2.net.pdu.AAbortException;
import org.dcm4che2.net.pdu.AAssociateAC;
import org.dcm4che2.net.pdu.AAssociateRJException;
import org.dcm4che2.net.pdu.AAssociateRQ;

/**
 * @author gunter zeilinger(gunterze@gmail.com)
 * @version $Revision$ $Date$
 * @since Nov 25, 2005
 *
 */
public class State
{
    public static final State STA1 = new Sta1();
    public static final State STA2 = new Sta2();
    public static final State STA3 = new Sta3();
    public static final State STA4 = new Sta4();
    public static final State STA5 = new Sta5();
    public static final State STA6 = new Sta6();
    public static final State STA7 = new Sta7();
    public static final State STA8 = new Sta8();
//    public static final State STA9 = new Sta9();
    public static final State STA10 = new Sta10();
    public static final State STA11 = new Sta11();
//    public static final State STA12 = new Sta12();
    public static final State STA13 = new Sta13();
    
    protected final String name;

    private State(String name)
    {
        this.name = name;
    }

    public String toString()
    {
        return name;
    }

    /** Sta1 - Idle */
    private static class Sta1 extends State
    {

        Sta1()
        {
            super("Sta1");
        }

        void abort(Association as, AAbortException aa)
        {
            // NOOP
        }
    }

    /** Sta2 - Transport connection open (Awaiting A-ASSOCIATE-RQ PDU) */
    private static class Sta2 extends State
    {

        Sta2()
        {
            super("Sta2");
        }
        
        void receivedAssociateRQ(Association as, AAssociateRQ rq) throws IOException
        {
            as.onAAssociateRQ(rq);
        }
    }

    /** Sta3 - Awaiting local A-ASSOCIATE response primitive */
    private static class Sta3 extends State
    {

        Sta3()
        {
            super("Sta3");
        }

    }

    /** Sta4 - Awaiting local A-ASSOCIATE request primitive. */
    private static class Sta4 extends State
    {

        Sta4()
        {
            super("Sta4");
        }

        void sendAssociateRQ(Association as, AAssociateRQ rq) throws IOException
        {
            as.writeAssociationRQ(rq);
        }
    }

    /** Sta5 - Awaiting A-ASSOCIATE-AC or A-ASSOCIATE-RJ PDU */
    private static class Sta5 extends State
    {

        Sta5()
        {
            super("Sta5");
        }

        void receivedAssociateAC(Association as, AAssociateAC ac) throws IOException
        {
            as.onAssociateAC(ac);
        }
        
        void receivedAssociateRJ(Association as, AAssociateRJException rj) throws IOException
        {
            as.onAssociateRJ(rj);
        }
    }

    /** Sta6 - Association established and ready for data transfer */
    private static class Sta6 extends State
    {

        Sta6()
        {
            super("Sta6");
        }

        void receivedPDataTF(Association as) throws IOException
        {
            as.onPDataTF();
        }
        
        void sendPDataTF(Association as) throws IOException
        {
            as.writePDataTF();
        }

        void receivedReleaseRQ(Association as) throws IOException
        {
            as.onReleaseRQ();
        }

        void sendReleaseRQ(Association as) throws IOException
        {
            as.writeReleaseRQ();
        }
        
        boolean isReadyForDataTransfer()
        {
            return true;
        }

        boolean isReadyForDataSend()
        {
            return true;
        }

        boolean isReadyForDataReceive()
        {
            return true;
        }
        
    }

    /** Sta7 - Awaiting A-RELEASE-RP PDU */
    private static class Sta7 extends State
    {

        Sta7()
        {
            super("Sta7");
        }

        void receivedPDataTF(Association as) throws IOException
        {
            as.onPDataTF();
        }

        void receivedReleaseRQ(Association as) throws IOException
        {
            as.onCollisionReleaseRQ();
        }

        void receivedReleaseRP(Association as) throws IOException
        {
            as.onReleaseRP();
        }

        boolean isReadyForDataReceive()
        {
            return true;
        }
        
    }

    /** Sta8 - Awaiting local A-RELEASE response primitive */
    private static class Sta8 extends State
    {

        public Sta8()
        {
            super("Sta8");
        }

        void sendPDataTF(Association as) throws IOException
        {
            as.writePDataTF();
        }

        boolean isReadyForDataSend()
        {
            return true;
        }

    }
    /** Sta9 - Release collision requestor side;
     * awaiting A-RELEASE response primitive */
/*
    private static class Sta9 extends State
    {

        public Sta9()
        {
            super("Sta9 - Release collision requestor side; " +
                    "awaiting A-RELEASE response primitive");
        }

    }
*/
    /** Sta10 - Release collision acceptor side;
     * awaiting A-RELEASE-RP PDU */
    private static class Sta10 extends State
    {

        public Sta10()
        {
            super("Sta10");
        }

        void receivedReleaseRP(Association as) throws IOException
        {
            as.onCollisionReleaseRP();
        }
    }

    /** Sta11 - Release collision requestor side;
     * awaiting A-RELEASE-RP PDU */
    private static class Sta11 extends State
    {

        public Sta11()
        {
            super("Sta11");
        }

        void receivedReleaseRP(Association as) throws IOException
        {
            as.onReleaseRP();
        }
    }
    /** Sta12 - Release collision acceptor side; 
     * awaiting A-RELEASE response primitive */
/*
    private static class Sta12 extends State
    {

        public Sta12()
        {
            super("Sta12");
        }

    }
*/
    /** Sta13 - Awaiting Transport Connection Close Indication */
    private static class Sta13 extends State
    {

        public Sta13()
        {
            super("Sta13 ");
        }

        void abort(Association as, AAbortException aa)
        {
            // NOOP
        }

    }

    void receivedAssociateRQ(Association as, AAssociateRQ rq) throws IOException
    {
        as.unexpectedPDU("A-ASSOCIATE-RQ");
    }

    void receivedAssociateAC(Association as, AAssociateAC ac) throws IOException
    {
        as.unexpectedPDU("A-ASSOCIATE-AC");
    }
    
    void receivedAssociateRJ(Association as, AAssociateRJException rj) throws IOException
    {
        as.unexpectedPDU("A-ASSOCIATE-RJ");
    }

    void receivedPDataTF(Association as) throws IOException
    {
        as.unexpectedPDU("P-DATA-TF");
    }

    void receivedReleaseRQ(Association as) throws IOException
    {
        as.unexpectedPDU("A-RELEASE-RQ");
    }

    void receivedReleaseRP(Association as) throws IOException
    {
        as.unexpectedPDU("A-RELEASE-RP");
    }

    void sendAssociateRQ(Association as, AAssociateRQ rq) throws IOException
    {
        //as.illegalStateForSending("A-ASSOCIATE-RQ");
        throw new IllegalStateException(toString());
    }
    
    void sendPDataTF(Association as) throws IOException
    {
        as.illegalStateForSending("P-DATA-TF");
    }
    
    void sendReleaseRQ(Association as) throws IOException
    {
        as.illegalStateForSending("A-RELEASE-RQ");
    }
    
    void abort(Association as, AAbortException aa)
    {
        as.writeAbort(aa);        
    }
    
    boolean isReadyForDataTransfer()
    {
        return false;
    }

    boolean isReadyForDataSend()
    {
        return false;
    }

    boolean isReadyForDataReceive()
    {
        return false;
    }
}
