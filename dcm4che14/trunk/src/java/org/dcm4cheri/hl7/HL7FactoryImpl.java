/*$Id$*/
/*****************************************************************************
 *                                                                           *
 *  Copyright (c) 2002 by TIANI MEDGRAPH AG                                  *
 *                                                                           *
 *  This file is part of dcm4che.                                            *
 *                                                                           *
 *  This library is free software; you can redistribute it and/or modify it  *
 *  under the terms of the GNU Lesser General Public License as published    *
 *  by the Free Software Foundation; either version 2 of the License, or     *
 *  (at your option) any later version.                                      *
 *                                                                           *
 *  This library is distributed in the hope that it will be useful, but      *
 *  WITHOUT ANY WARRANTY; without even the implied warranty of               *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU        *
 *  Lesser General Public License for more details.                          *
 *                                                                           *
 *  You should have received a copy of the GNU Lesser General Public         *
 *  License along with this library; if not, write to the Free Software      *
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA  *
 *                                                                           *
 *****************************************************************************/

package org.dcm4cheri.hl7;

import org.dcm4che.hl7.HL7Factory;
import org.dcm4che.hl7.HL7Message;


/**
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0.0
 */
public class HL7FactoryImpl extends HL7Factory {
  
   private static final byte[] AA = { (byte)'A', (byte)'A' };
   private static final byte[] AE = { (byte)'A', (byte)'E' };
   private static final byte[] AR = { (byte)'A', (byte)'R' };
   
   public HL7Message toHL7Message(byte[] data) {
      return new HL7MessageImpl(data);
   }
   
   public byte[] accept(HL7Message msg) {
      return ack(msg, AA, null, null, null);
   }
    
   public byte[] reject(HL7Message msg,
         String errText, String errCode, String errComment) {
      return ack(msg, AR, errText, errCode, errComment);
   }
    
   public byte[] error(HL7Message msg,
         String errText, String errCode, String errComment) {
      return ack(msg, AE, errText, errCode, errComment);
   }
   
   private byte[] ack(HL7Message msg, byte[] ackCode,
         String errText, String errCode, String errComment) {
      return ((MSHSegmentImpl)msg.header()).ack(ackCode,
                                             errText, errCode, errComment);
   }
}
