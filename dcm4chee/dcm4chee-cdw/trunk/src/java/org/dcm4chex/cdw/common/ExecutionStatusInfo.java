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
 * @since 23.06.2004
 *
 */
public class ExecutionStatusInfo {

    public static final String NORMAL = "NORMAL";
    
    public static final String QUEUED = "QUEUED";

    public static final String PROC_FAILURE = "PROC_FAILURE";    

    private ExecutionStatusInfo() {}

}
