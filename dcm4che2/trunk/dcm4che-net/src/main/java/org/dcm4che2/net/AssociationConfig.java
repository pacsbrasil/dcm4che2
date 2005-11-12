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
 * Java(TM), available at http://sourceforge.net/projects/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * Gunter Zeilinger, Huetteldorferstr. 24/10, 1150 Vienna/Austria/Europe.
 * Portions created by the Initial Developer are Copyright (C) 2005
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

/**
 * @author gunter zeilinger(gunterze@gmail.com)
 * @version $Reversion$ $Date$
 * @since Oct 10, 2005
 *
 */
public class AssociationConfig
{

    private long associationRequestTimeout = 0;
    private long associationAcceptTimeout = 0;
    private long releaseResponseTimeout = 0;
    private long socketCloseDelay = 100L;
    private int pdvPipeBufferSize = 1024;
    private boolean packPDV = false;
    private int idleTime = 0;
    private int writeTimeout = 0;
    private int receiveBufferSize;
    private int sendBufferSize;
    private int sessionReceiveBufferSize;
    private int soLinger = -1;
    private int trafficClass;
    private boolean oobInline;
    private boolean keepAlive;
    private boolean tcpNoDelay;
    private boolean reuseAddress = true;
    private int maxSendPDULength;

    public final long getAssociationRequestTimeout()
    {
        return associationRequestTimeout;
    }

    public final void setAssociationRequestTimeout(long timeout)
    {
        this.associationRequestTimeout = timeout;
    }

    public final long getAssociationAcceptTimeout()
    {
        return associationAcceptTimeout;
    }

    public final void setAssociationAcceptTimeout(long timeout)
    {
        this.associationAcceptTimeout = timeout;
    }

    public final int getPDVPipeBufferSize()
    {
        return pdvPipeBufferSize;
    }

    public final void setPDVPipeBufferSize(int bufferSize)
    {
        this.pdvPipeBufferSize = bufferSize;
    }

    public final int getMaxSendPDULength()
    {
        return maxSendPDULength;
    }

    public final void setMaxSendPDULength(int bufferSize)
    {
        this.maxSendPDULength = bufferSize;
    }
    
    public final boolean getPackPDV()
    {
        return packPDV;
    }

    public final void setPackPDV(boolean packPDV)
    {
        this.packPDV = packPDV;
    }

    public final long getReleaseResponseTimeout()
    {
        return releaseResponseTimeout;
    }

    public final void setReleaseResponseTimeout(long timeout)
    {
        this.releaseResponseTimeout = timeout;
    }

    public final long getSocketCloseDelay()
    {
        return socketCloseDelay;
    }

    public final void setSocketCloseDelay(long socketCloseDelay)
    {
        this.socketCloseDelay = socketCloseDelay;
    }

    public final boolean getSocketKeepAlive()
    {
        return keepAlive;
    }

    public final void setSocketKeepAlive(boolean keepAlive)
    {
        this.keepAlive = keepAlive;
    }

    public final boolean getSocketOobInline()
    {
        return oobInline;
    }

    public final void setSocketOobInline(boolean oobInline)
    {
        this.oobInline = oobInline;
    }

    public final int getSocketReceiveBufferSize()
    {
        return receiveBufferSize;
    }

    public final void setSocketReceiveBufferSize(int receiveBufferSize)
    {
        this.receiveBufferSize = receiveBufferSize;
    }

    public final boolean getReuseAddress()
    {
        return reuseAddress;
    }

    public final void setReuseAddress(boolean reuseAddress)
    {
        this.reuseAddress = reuseAddress;
    }

    public final int getSocketSendBufferSize()
    {
        return sendBufferSize;
    }

    public final void setSocketSendBufferSize(int sendBufferSize)
    {
        this.sendBufferSize = sendBufferSize;
    }

    public final int getSessionReceiveBufferSize()
    {
        return sessionReceiveBufferSize;
    }

    public final void setSessionReceiveBufferSize(int sessionReceiveBufferSize)
    {
        this.sessionReceiveBufferSize = sessionReceiveBufferSize;
    }

    public final int getSoLinger()
    {
        return soLinger;
    }

    public final void setSoLinger(int soLinger)
    {
        this.soLinger = soLinger;
    }

    public final boolean getTcpNoDelay()
    {
        return tcpNoDelay;
    }

    public final void setTcpNoDelay(boolean tcpNoDelay)
    {
        this.tcpNoDelay = tcpNoDelay;
    }

    public final int getTrafficClass()
    {
        return trafficClass;
    }

    public final void setTrafficClass(int trafficClass)
    {
        this.trafficClass = trafficClass;
    }

    public final int getIdleTime()
    {
        return idleTime;
    }

    public final void setIdleTime(int idleTime)
    {
        this.idleTime = idleTime;
    }

    public final int getWriteTimeout()
    {
        return writeTimeout;
    }

    public final void setWriteTimeout(int writeTimeout)
    {
        this.writeTimeout = writeTimeout;
    }
}
