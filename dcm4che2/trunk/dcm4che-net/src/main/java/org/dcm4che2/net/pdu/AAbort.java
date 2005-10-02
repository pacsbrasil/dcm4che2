/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/

package org.dcm4che2.net.pdu;

/**
 * @author gunter zeilinger(gunterze@gmail.com)
 * @version $Reversion$ $Date$
 * @since Sep 15, 2005
 */
public class AAbort implements PDU
{

    public static final int UL_SERIVE_USER = 0;
    public static final int UL_SERIVE_PROVIDER = 2;

    public static final int REASON_NOT_SPECIFIED = 0;
    public static final int UNRECOGNIZED_PDU = 1;
    public static final int UNEXPECTED_PDU = 2;
    public static final int UNRECOGNIZED_PDU_PARAMETER = 4;
    public static final int UNEXPECTED_PDU_PARAMETER = 5;
    public static final int INVALID_PDU_PARAMETER_VALUE = 6;

    private static final String RESERVED = "reserved";
    private static final String UNDEFINED = "undefined";

    private static final String[] SOURCE =
    {
            "DICOM UL service-user (initiated abort)",
            RESERVED,
            "DICOM UL service-provider (initiated abort)"
    };

    private static final String[] REASON =
    {
            "reason-not-specified",
            "unrecognized-PDU",
            "unexpected-PDU",
            RESERVED,
            "unrecognized-PDU parameter",
            "unexpected-PDU parameter",
            "invalid-PDU-parameter value"
    };

    private int source = UL_SERIVE_USER;
    private int reason = REASON_NOT_SPECIFIED;

    public final int length()
    {
        return 4;
    }

    public final int getReason()
    {
        return reason;
    }

    public final void setReason(int reason)
    {
        this.reason = reason;
    }

    public final int getSource()
    {
        return source;
    }

    public final void setSource(int source)
    {
        this.source = source;
    }

    public static AAbort unexpectedPDU()
    {
        return fromServiceProvider(UNEXPECTED_PDU);
    }

    public static AAbort reasonNotSpecified()
    {
        return fromServiceProvider(REASON_NOT_SPECIFIED);
    }

    public static AAbort fromServiceProvider(int reason)
    {
        AAbort aa = new AAbort();
        aa.setSource(UL_SERIVE_PROVIDER);
        aa.setReason(reason);
        return aa;
    }

    public String toString()
    {
        return "A-ABORT[\n  source = " + source + " - "
                + code2str(source, SOURCE) + "\n  reason = " + reason
                + (source == 2 ? (" - " + code2str(reason, REASON)) : "")
                + "\n]";
    }

    private static String code2str(int code, String[] prompts)
    {
        try
        {
            return prompts[code];
        } catch (IndexOutOfBoundsException e)
        {
            return UNDEFINED;
        }
    }

}
