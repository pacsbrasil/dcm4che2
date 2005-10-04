/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
package org.dcm4chex.archive.common;


/**
 * @author gunter.zeilinger@tiani.com
 * @version $Revision$ $Date$
 * @since 06.10.2004
 *
 */
public class PrivateTags {
    public static final String CreatorID = "dcm4che/archive";
    public static final int PatientPk = 0x00430010;
    public static final int StudyPk = 0x00430011;
    public static final int SeriesPk = 0x00430012;
    public static final int InstancePk = 0x00430013;
    public static final int CallingAET = 0x00430014;
    public static final int CalledAET = 0x00430015;
    public static final int HiddenInstance = 0x00430016;
    public static final int HiddenSeries = 0x00430017;
    public static final int HiddenStudy = 0x00430018;
    public static final int HiddenPatient = 0x00430019;

}
