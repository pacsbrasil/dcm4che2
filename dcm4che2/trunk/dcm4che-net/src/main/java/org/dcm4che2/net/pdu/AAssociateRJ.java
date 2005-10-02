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
public class AAssociateRJ implements PDU
{

    public static final int RESULT_REJECTED_PERMANENT = 1;
    public static final int RESULT_REJECTED_TRANSIENT = 2;

    public static final int SOURCE_SERVICE_USER = 1;
    public static final int SOURCE_SERVICE_PROVIDER_ACSE = 2;
    public static final int SOURCE_SERVICE_PROVIDER_PRES = 3;

    public static final int REASON_NO_REASON_GIVEN = 1;
    public static final int REASON_APP_CTX_NAME_NOT_SUPPORTED = 2;
    public static final int REASON_CALLING_AET_NOT_RECOGNIZED = 3;
    public static final int REASON_CALLED_AET_NOT_RECOGNIZED = 7;

    public static final int REASON_PROTOCOL_VERSION_NOT_SUPPORTED = 2;

    public static final int REASON_TEMPORARY_CONGESTION = 1;
    public static final int REASON_LOCAL_LIMIT_EXCEEDED = 2;

    private static final String RESERVED = "reserved";
    private static final String UNDEFINED = "undefined";

    private static final String[] RESULT =
    {
            UNDEFINED,
            "rejected-permanent",
            "rejected-transient"
    };

    private static final String[] SOURCE =
    {
            UNDEFINED,
            "DICOM UL service-user",
            "DICOM UL service-provider (ACSE related function)",
            "DICOM UL service-provider (Presentation related function)"
    };

    private static final String[][] REASON =
    {
            {
                    UNDEFINED,
                    "no-reason-given",
                    "application-context-name-not-supported",
                    "calling-AE-title-not-recognized",
                    RESERVED,
                    RESERVED,
                    RESERVED,
                    "called-AE-title-not-recognized",
                    RESERVED,
                    RESERVED,
                    RESERVED
            },
            {
                    UNDEFINED,
                    "no-reason-given",
                    "protocol-version-not-supported"
            },
            {
                    RESERVED,
                    "temporary-congestion",
                    "local-limit-exceeded",
                    RESERVED,
                    RESERVED,
                    RESERVED,
                    RESERVED,
                    RESERVED
            }
    };

    private int result;
    private int source;
    private int reason;

    public final int length()
    {
        return 4;
    }

    public final int getResult()
    {
        return result;
    }

    public final void setResult(int result)
    {
        this.result = result;
    }

    public final int getSource()
    {
        return source;
    }

    public final void setSource(int source)
    {
        this.source = source;
    }

    public final int getReason()
    {
        return reason;
    }

    public final void setReason(int reason)
    {
        this.reason = reason;
    }

    public static AAssociateRJ protocolVersionNotSupported()
    {
        AAssociateRJ rj = new AAssociateRJ();
        rj.setResult(RESULT_REJECTED_PERMANENT);
        rj.setSource(SOURCE_SERVICE_PROVIDER_ACSE);
        rj.setReason(REASON_PROTOCOL_VERSION_NOT_SUPPORTED);
        return rj;
    }

    public String toString()
    {
        return "A-ASSOCIATE_RJ[\n  result = " + result + " - "
                + code2str(result, RESULT) + "\n  source = " + source + " - "
                + code2str(source, SOURCE) + "\n  reason = " + reason + " - "
                + reason2str() + "\n]";
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

    private String reason2str()
    {
        try
        {
            return code2str(reason, REASON[source - 1]);
        } catch (IndexOutOfBoundsException e)
        {
            return UNDEFINED;
        }
    }

}
