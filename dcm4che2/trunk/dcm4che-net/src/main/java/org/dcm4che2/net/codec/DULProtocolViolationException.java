/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/

package org.dcm4che2.net.codec;

import org.apache.mina.protocol.ProtocolViolationException;

/**
 * @author gunter zeilinger(gunterze@gmail.com)
 * @version $Reversion$ $Date$
 * @since Sep 19, 2005
 *
 */
public class DULProtocolViolationException extends ProtocolViolationException {

    private static final long serialVersionUID = 3763095254395401009L;
    
    private final int reason;
    
    public DULProtocolViolationException(int reason) {
        this.reason = reason;
    }

    public DULProtocolViolationException(int reason, String message) {
        super(message);
        this.reason = reason;
    }

    public DULProtocolViolationException(int reason, Throwable cause) {
        super(cause);
        this.reason = reason;
    }

    public DULProtocolViolationException(int reason, String message, 
             Throwable cause) {
        super(message, cause);
        this.reason = reason;
    }
    
    public final int getReason() {
        return reason;
    }

}
