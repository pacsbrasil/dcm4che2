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

package org.dcm4cheri.util;

import org.dcm4che.Dcm4che;
import org.dcm4che.util.UIDGenerator;

import java.io.DataOutput;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.server.UID;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0.0
 */
public final class UIDGeneratorImpl extends UIDGenerator {
    
    private static final SimpleDateFormat FORMAT = 
            new SimpleDateFormat("yyyyMMddHHmmssSSS");

    /** Creates a new instance of UIDGeneratorImpl */
    public UIDGeneratorImpl() {
    }

    public String createUID() {
        return createUID(Dcm4che.getImplementationClassUID());
    }
    
    public String createUID(String root) {
        final StringBuffer sb = new StringBuffer(64).append(root).append('.');
        String ip = "127.0.0.1";
        try {
            ip = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException ex) { }
        final String finalIP = ip;
        try {
            new UID().write(new DataOutput() {
                public void write(int b) {}
                public void write(byte b[]) {}
                public void write(byte b[], int off, int len) {}
                public void writeBoolean(boolean v) {}
                public void writeByte(int v) {}
                public void writeShort(int v) {
                    sb.append('.').append(v & 0xffff);
                }
                public void writeChar(int v) {}
                public void writeInt(int v)  {
                    if ("127.0.0.1".equals(finalIP)) {
                        sb.append(v & 0xffffffffL);
                    } else {
                        sb.append(finalIP);
                    }
                }
                public void writeLong(long v) {
                    sb.append('.').append(FORMAT.format(new Date(v)));
                }
                public void writeFloat(float v) {}
                public void writeDouble(double v) {}
                public void writeBytes(String s) {}
                public void writeChars(String s) {}
                public void writeUTF(String str) {}             
            });
        }
        catch (IOException ex) {
            throw new RuntimeException(ex.toString());
        }
        if (sb.length() > 64) {
            throw new IllegalArgumentException("Too long root prefix");
        }
        return sb.toString();
    }
}
