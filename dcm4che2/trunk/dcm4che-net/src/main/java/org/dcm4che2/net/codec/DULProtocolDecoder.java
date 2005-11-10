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

package org.dcm4che2.net.codec;

import org.apache.mina.common.ByteBuffer;
import org.apache.mina.protocol.ProtocolDecoderOutput;
import org.apache.mina.protocol.ProtocolSession;
import org.apache.mina.protocol.ProtocolViolationException;
import org.apache.mina.protocol.codec.CumulativeProtocolDecoder;
import org.dcm4che2.net.pdu.AAbort;
import org.dcm4che2.net.pdu.AAssociateRQAC;

/**
 * @author gunter zeilinger(gunterze@gmail.com)
 * @version $Reversion$ $Date$
 * @since Sep 19, 2005
 */
public class DULProtocolDecoder extends CumulativeProtocolDecoder
{

    private static final int HEADER_LEN = 6;

    private final PDUDecoder[] decoders =
    { new AAssociateRQDecoder(), new AAssociateACDecoder(),
            new AAssociateRJDecoder(), new PDataTFDecoder(),
            new AReleaseRQDecoder(), new AReleaseRPDecoder(),
            new AAbortDecoder() };

    private PDUDecoder decoder;
    
    private int length;

    public DULProtocolDecoder()
    {
        super(AAssociateRQAC.DEF_MAX_PDU_LENGTH + 6);
    }

    protected boolean doDecode(ProtocolSession session, ByteBuffer in,
            ProtocolDecoderOutput out) throws ProtocolViolationException
    {

        if (in.remaining() < HEADER_LEN)
            return false;
        if (decoder == null)
        {
            int type = in.get() & 0xff;
            try
            {
                decoder = decoders[type - 1];
            }
            catch (IndexOutOfBoundsException e)
            {
                throw new DULProtocolViolationException(
                        AAbort.UNRECOGNIZED_PDU, "Unkown PDU type: " + type);
            }
            in.get(); // reserved byte
            length = in.getInt();
        }

        if (in.remaining() < length)
            return false;
        
        int prevLimit = in.limit();
        in.limit(in.position() + length);
        out.write(decoder.decodePDU(session, in));
        in.limit(prevLimit);
        decoder = null;
        return true;
    }

}
