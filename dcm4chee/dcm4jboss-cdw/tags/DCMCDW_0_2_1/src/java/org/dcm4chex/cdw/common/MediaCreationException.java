/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
package org.dcm4chex.cdw.common;

/**
 * @author gunter.zeilinter@tiani.com
 * @version $Revision$ $Date$
 * @since 28.06.2004
 *
 */
public class MediaCreationException extends Exception {

    private final String statusInfo;

    public MediaCreationException(String statusInfo) {
        super();
        this.statusInfo = statusInfo;
    }

    public MediaCreationException(String statusInfo, String message) {
        super(message);
        this.statusInfo = statusInfo;
    }

    public MediaCreationException(String statusInfo, Throwable cause) {
        super(cause);
        this.statusInfo = statusInfo;
    }

    public MediaCreationException(String statusInfo, String message,
            Throwable cause) {
        super(message, cause);
        this.statusInfo = statusInfo;
    }

    public final String getStatusInfo() {
        return statusInfo;
    }
}
