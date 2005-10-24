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

    private int source;
    private int reason;

    public AAbort()
    {
        this(UL_SERIVE_USER, REASON_NOT_SPECIFIED);
    }
    
    public AAbort(int reason)
    {
        this(UL_SERIVE_PROVIDER, reason);
    }

    public AAbort(int source, int reason)
    {
        setSource(source);
        setReason(reason);
    }

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
