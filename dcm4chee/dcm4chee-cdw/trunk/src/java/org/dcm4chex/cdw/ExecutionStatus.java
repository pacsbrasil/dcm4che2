/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
package org.dcm4chex.cdw;

/**
 * @author gunter.zeilinter@tiani.com
 * @version $Revision$ $Date$
 * @since 23.06.2004
 *
 */
public class ExecutionStatus {

    public static final String IDLE = "IDLE";

    public static final String PENDING = "PENDING";

    public static final String CREATING = "CREATING";

    public static final String DONE = "DONE";

    public static final String FAILURE = "FAILURE";

    private ExecutionStatus() {
    }
}
