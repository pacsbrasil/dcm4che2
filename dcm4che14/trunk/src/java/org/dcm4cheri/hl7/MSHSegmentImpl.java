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
   
   // Attributes ----------------------------------------------------
   public final String sendingApplication;
   public final String sendingFacility;
   public final String receivingApplication;
   public final String receivingFacility;
   public final String messageType;
   public final String triggerEvent;
   public final String messageControlID;

   // Static --------------------------------------------------------
   
   // Constructors --------------------------------------------------
   MSHSegmentImpl(byte[] data, int off, int len) {
      super(data, off, len);
      for (int i = 0; i < START_WITH.length; ++i) {
         if (data[off + i] != START_WITH[i]) {
            throw new IllegalArgumentException(toString());
         }
      }
      sendingApplication = get(3);
      sendingFacility = get(4);
      receivingApplication = get(5);
      receivingFacility = get(6);
      messageType = get(9, 1, 1);
      triggerEvent = get(9, 1, 2);
      messageControlID = get(10);
   }
   
   // Public --------------------------------------------------------
   public final String getSendingApplication() {
      return sendingApplication;
   }
   
   public final String getSendingFacility() {
      return sendingFacility;
   }
   
   public final String getReceivingApplication() {
      return receivingApplication;
   }
   
   public final String getReceivingFacility() {
      return receivingFacility;
   }
   
   public final String getMessageType() {
      return messageType;
   }
   
   public final String getTriggerEvent() {
      return triggerEvent;
   }
   
   public final String getMessageControlID() {
      return messageControlID;
   }
   
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

   private void writeTo(byte[] b, ByteArrayOutputStream out) {
      out.write(b, 0, b.length);
   }
   
   byte[] ack(byte[] ackCode, String errText, String errCode, String errComment) {
      ByteArrayOutputStream out = new ByteArrayOutputStream(64);
      writeTo(START_WITH, out);
      writeTo(5, out);
      out.write('|');
      writeTo(6, out);
      out.write('|');
      writeTo(3, out);
      out.write('|');
      writeTo(4, out);
      writeTo(ACK, out);
      if (messageControlID.length() < 20) {
         writeTo(10, out);
      } else {
         writeTo(messageControlID.substring(1).getBytes(), out);
      }
      out.write('|');
      writeTo(11, out);
      out.write('|');
      writeTo(12, out);
      writeTo(MSA, out);
      writeTo(ackCode, out);
      out.write('|');
      writeTo(10, out);
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
