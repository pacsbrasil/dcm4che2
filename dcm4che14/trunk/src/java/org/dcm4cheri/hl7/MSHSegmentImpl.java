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

import org.dcm4che.hl7.HL7;
import org.dcm4che.hl7.HL7Exception;
import org.dcm4che.hl7.MSHSegment;

import java.io.ByteArrayOutputStream;

/**
 * <description> 
 *
 * @see <related>
 * @author  <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
 * @version $Revision$ $Date$
 *   
 * <p><b>Revisions:</b>
 *
 * <p><b>yyyymmdd author:</b>
 * <ul>
 * <li> explicit fix description (no line numbers but methods) go 
 *            beyond the cvs commit message
 * </ul>
 */
public class MSHSegmentImpl extends HL7SegmentImpl implements MSHSegment
{
   // Constants -----------------------------------------------------
   private static final byte[] START_WITH = {
      (byte)'M', (byte)'S', (byte)'H', (byte)'|',
      (byte)'^', (byte)'~', (byte)'\\', (byte)'&', (byte)'|',
   };
   private static final byte[] ACK = {
      (byte)'|', (byte)'|', (byte)'|',
      (byte)'A', (byte)'C', (byte)'K', (byte)'|', (byte)'A'
   };
   private static final byte[] MSA = {
      (byte)'\r', (byte)'M', (byte)'S', (byte)'A', (byte)'|'
   };
   private static final byte[] ERR = {
      (byte)'\r', (byte)'E', (byte)'R', (byte)'R', (byte)'|'
   };
   private static final byte[] AA = { 
       (byte)'A', (byte)'A' 
   };
   private static final byte[] AE = {
       (byte)'A', (byte)'E' 
   };
   private static final byte[] AR = {
       (byte)'A', (byte)'R' 
   };
      
   // Attributes ----------------------------------------------------

   // Static --------------------------------------------------------
   
   // Constructors --------------------------------------------------
   MSHSegmentImpl(byte[] data)
   throws HL7Exception {
       this(data, 0, HL7MessageImpl.indexOfNextCRorLF(data, 0));
   }
   
   MSHSegmentImpl(byte[] data, int off, int len)
   throws HL7Exception {
      super(data, off, len);
      for (int i = 0; i < START_WITH.length; ++i) {
         if (data[off + i] != START_WITH[i]) {
            throw new IllegalArgumentException(toString());
         }
      }
   }
   
   // Public --------------------------------------------------------   
   public int size() {
      return super.size() + 1;
   }
   
   public String get(int seq) {
      switch (seq) {
         case 0:
            return "MSH";
         case 1:
            return "|";
         default:
            return super.get(seq-1);
      }
   }

   public String get(int seq, int rep) {
      if (seq < 2) {
         throw new IllegalArgumentException("seq: " + seq);
      }
      return super.get(seq-1, rep);
   }
   
   public String get(int seq, int rep, int comp) {
      if (seq < 2) {
         throw new IllegalArgumentException("seq: " + seq);
      }
      return super.get(seq-1, rep, comp);
   }
   
   public String get(int seq, int rep, int comp, int sub) {
      if (seq < 2) {
         throw new IllegalArgumentException("seq: " + seq);
      }
      return super.get(seq-1, rep, comp, sub);
   }
   
   public void writeTo(int seq, ByteArrayOutputStream out) {
      if (seq < 2) {
         throw new IllegalArgumentException("seq: " + seq);
      }
      super.writeTo(seq-1, out);
   }

   public String getMessageControlID() {
       return get(HL7.MSHMessageControlID);
   }
   
   public String getReceivingApplication() {
       return get(HL7.MSHReceivingApplication);
   }
   
   public String getReceivingFacility() {
       return get(HL7.MSHReceivingFacility);
   }
   
   public String getSendingApplication() {
       return get(HL7.MSHSendingApplication);
   }
   
   public String getSendingFacility() {
       return get(HL7.MSHSendingFacility);
   }
   
   public String getMessageType() {
       return get(HL7.MSHMessageType,1,1);
   }
   
   public String getTriggerEvent() {
       return get(HL7.MSHMessageType,1,2);
   }
   
   public String getCharacterSet() {
       return get(HL7.MSHCharacterSet);
   }
   
   private void writeTo(byte[] b, ByteArrayOutputStream out) {
      out.write(b, 0, b.length);
   }
   
   public byte[] makeACK_AA() {
      return ack(AA, null, null, null);
   }
    
   public byte[] makeACK_AR(String errText, String errCode, String errComment) {
      return ack(AR, errText, errCode, errComment);
   }
    
   public byte[] makeACK_AE(String errText, String errCode, String errComment) {
      return ack(AE, errText, errCode, errComment);
   }
   
   byte[] ack(byte[] ackCode, String errText, String errCode, String errComment) {
      ByteArrayOutputStream out = new ByteArrayOutputStream(64);
      writeTo(START_WITH, out);
      writeTo(HL7.MSHReceivingApplication, out);
      out.write('|');
      writeTo(HL7.MSHReceivingFacility, out);
      out.write('|');
      writeTo(HL7.MSHSendingApplication, out);
      out.write('|');
      writeTo(HL7.MSHSendingFacility, out);
      writeTo(ACK, out);
      writeTo(HL7.MSHMessageControlID, out);
      out.write('|');
      writeTo(HL7.MSHProcessingID, out);
      out.write('|');
      writeTo(HL7.MSHVersionID, out);
      writeTo(MSA, out);
      writeTo(ackCode, out);
      out.write('|');
      writeTo(HL7.MSHMessageControlID, out);
      if (errText != null || errCode != null) {
         out.write('|');
         if (errText != null) {
            writeTo(errText.getBytes(), out);
         }
         if (errCode != null) {
            out.write('|');
            out.write('|');
            out.write('|');
            writeTo(errCode.getBytes(), out);
         }
      }
      if (errComment != null) {
         writeTo(ERR, out);
         writeTo(errComment.getBytes(), out);
      }
      out.write('\r');
      return out.toByteArray();
   }
}
