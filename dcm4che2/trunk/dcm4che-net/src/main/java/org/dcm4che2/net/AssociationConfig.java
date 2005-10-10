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
class AssociationConfig
{

    protected final Executor executor;
    protected long associationRequestTimeout = 10000L;
    protected long associationAcceptTimeout = 10000L;
    protected long releaseResponseTimeout = 10000L;
    protected long socketCloseDelay = 100L;
    protected int pdvPipeBufferSize = 1024;
    protected boolean packPDV = true;
    protected int idleTime = 0;
    protected int writeTimeout = 0;
    protected int receiveBufferSize;
    protected int sendBufferSize;
    protected int sessionReceiveBufferSize;
    protected int soLinger = -1;
    protected int trafficClass;
    protected boolean oobInline;
    protected boolean keepAlive;
    protected boolean tcpNoDelay;
    protected boolean reuseAddress = true;

    public AssociationConfig(Executor executor)
    {
        if (executor == null)
            throw new NullPointerException();

        this.executor = executor;
    }

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

    public final boolean isPackPDV()
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

    public final int getIdleTime()
    {
        return idleTime;
    }

    public final void setIdleTime(int idleTime)
    {
        this.idleTime = idleTime;
    }

    public final boolean isKeepAlive()
    {
        return keepAlive;
    }

    public final void setKeepAlive(boolean keepAlive)
    {
        this.keepAlive = keepAlive;
    }

    public final boolean isOobInline()
    {
        return oobInline;
    }

    public final void setOobInline(boolean oobInline)
    {
        this.oobInline = oobInline;
    }

    public final int getReceiveBufferSize()
    {
        return receiveBufferSize;
    }

    public final void setReceiveBufferSize(int receiveBufferSize)
    {
        this.receiveBufferSize = receiveBufferSize;
    }

    public final boolean isReuseAddress()
    {
        return reuseAddress;
    }

    public final void setReuseAddress(boolean reuseAddress)
    {
        this.reuseAddress = reuseAddress;
    }

    public final int getSendBufferSize()
    {
        return sendBufferSize;
    }

    public final void setSendBufferSize(int sendBufferSize)
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

    public final boolean isTcpNoDelay()
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

    public final int getWriteTimeout()
    {
        return writeTimeout;
    }

    public final void setWriteTimeout(int writeTimeout)
    {
        this.writeTimeout = writeTimeout;
    }

    protected void configure(DULProtocolProvider provider)
    {
        provider.setAssociationRequestTimeout(associationRequestTimeout);
        provider.setAssociationAcceptTimeout(associationAcceptTimeout);
        provider.setReleaseResponseTimeout(releaseResponseTimeout);
        provider.setIdleTime(idleTime);
        provider.setPDVPipeBufferSize(pdvPipeBufferSize);
        provider.setPackPDV(packPDV);
        provider.setWriteTimeout(writeTimeout);
        provider.setReceiveBufferSize(receiveBufferSize);
        provider.setSendBufferSize(sendBufferSize);
        provider.setSessionReceiveBufferSize(sessionReceiveBufferSize);
        provider.setSoLinger(soLinger);
        provider.setTrafficClass(trafficClass);
        provider.setOOBInline(oobInline);
        provider.setKeepAlive(keepAlive);
        provider.setTcpNoDelay(tcpNoDelay);
        provider.setReuseAddress(reuseAddress);
    }

}
