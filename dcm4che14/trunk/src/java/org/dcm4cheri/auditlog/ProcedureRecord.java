/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
package org.dcm4cheri.auditlog;

import org.dcm4che.auditlog.Patient;
import org.dcm4che.auditlog.User;

/**
 * @author gunter.zeilinger@tiani.com
 * @version $Revision$ $Date$
 * @since 12.10.2004
 *
 */
public class ProcedureRecord implements IHEYr4.Message {

    private final String action;

    private final String placerOrderNumber;

    private final String fillerOrderNumber;

    private final String suid;

    private final String accessionNumber;

    private final Patient patient;

    private final User user;

    public ProcedureRecord(String action, String placerOrderNumber,
            String fillerOrderNumber, String suid, String accessionNumber,
            Patient patient, User user) {
        this.action = action;
        this.placerOrderNumber = placerOrderNumber;
        this.fillerOrderNumber = fillerOrderNumber;
        this.suid = suid;
        this.accessionNumber = accessionNumber;
        this.patient = patient;
        this.user = user;
    }

    public void writeTo(StringBuffer sb) {
        sb.append("<ProcedureRecord><ObjectAction>").append(action)
                .append("</ObjectAction>").append("<PlacerOrderNumber>")
                .append(placerOrderNumber).append("</PlacerOrderNumber>")
                .append("<FillerOrderNumber>").append(fillerOrderNumber)
                .append("</FillerOrderNumber>").append("<SUID>").append(suid)
                .append("</SUID>");
        if (accessionNumber != null)
                sb.append("<AccessionNumber>").append(accessionNumber)
                        .append("</AccessionNumber>");
        patient.writeTo(sb);
        user.writeTo(sb);
        sb.append("</ProcedureRecord>");
    }

}