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
 * Java(TM), hosted at http://sourceforge.net/projects/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * Gunter Zeilinger, Huetteldorferstr. 24/10, 1150 Vienna/Austria/Europe.
 * Portions created by the Initial Developer are Copyright (C) 2002-2005
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
    { UNDEFINED, "rejected-permanent", "rejected-transient" };

    private static final String[] SOURCE =
    { UNDEFINED, "DICOM UL service-user",
            "DICOM UL service-provider (ACSE related function)",
            "DICOM UL service-provider (Presentation related function)" };

    private static final String[][] REASON =
    {
            { UNDEFINED, "no-reason-given",
                    "application-context-name-not-supported",
                    "calling-AE-title-not-recognized", RESERVED, RESERVED,
                    RESERVED, "called-AE-title-not-recognized", RESERVED,
                    RESERVED, RESERVED },
            { UNDEFINED, "no-reason-given", "protocol-version-not-supported" },
            { RESERVED, "temporary-congestion", "local-limit-exceeded",
                    RESERVED, RESERVED, RESERVED, RESERVED, RESERVED } };

    private int result;
    private int source;
    private int reason;

    public AAssociateRJ()
    {
    }

    public AAssociateRJ(int result, int source, int reason)
    {
        this.result = result;
        this.source = source;
        this.reason = reason;
    }

    public final int type()
    {
         return PDUType.A_ASSOCIATE_RJ;
    }

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
        }
        catch (IndexOutOfBoundsException e)
        {
            return UNDEFINED;
        }
    }

    private String reason2str()
    {
        try
        {
            return code2str(reason, REASON[source - 1]);
        }
        catch (IndexOutOfBoundsException e)
        {
            return UNDEFINED;
        }
    }

}
