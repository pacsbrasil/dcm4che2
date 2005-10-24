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
 * Java(TM), available at http://sourceforge.net/projects/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * Gunter Zeilinger, Huetteldorferstr. 24/10, 1150 Vienna/Austria/Europe.
 * Portions created by the Initial Developer are Copyright (C) 2005
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

package org.dcm4che2.net;

import java.io.InputStream;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.net.pdu.AAbort;
import org.dcm4che2.net.pdu.AAssociateAC;
import org.dcm4che2.net.pdu.AAssociateRJ;
import org.dcm4che2.net.pdu.AAssociateRQ;
import org.dcm4che2.net.pdu.AReleaseRP;
import org.dcm4che2.net.pdu.AReleaseRQ;

/**
 * @author gunter zeilinger(gunterze@gmail.com)
 * @version $Reversion$ $Date$
 * @since Oct 9, 2005
 *
 */
public class AssociationHandlerFilter implements AssociationHandler
{
    private final AssociationHandler handler;
    
    public AssociationHandlerFilter(AssociationHandler handler)
    {
        if (handler == null)
            throw new NullPointerException();
        
        this.handler = handler;
    }


    public final AssociationHandler getHandler()
    {
        return handler;
    }
    
    public void onAAssociateAC(Association as, AAssociateAC ac)
    {
        handler.onAAssociateAC(as, ac);
    }

    public void onAAssociateRJ(Association as, AAssociateRJ rj)
    {
        handler.onAAssociateRJ(as, rj);
    }

    public void onAAssociateRQ(Association as, AAssociateRQ rq)
    {
        handler.onAAssociateRQ(as, rq);
    }

    public void onAReleaseRP(Association as, AReleaseRP rp)
    {
        handler.onAReleaseRP(as, rp);
    }

    public void onAReleaseRQ(Association as, AReleaseRQ rq)
    {
        handler.onAReleaseRQ(as, rq);
    }

    public void onAbort(Association as, AAbort abort)
    {
        handler.onAbort(as, abort);
    }

    public void onClosed(Association association)
    {
        handler.onClosed(association);
    }

    public void onDIMSE(Association as, int pcid, DicomObject command,
            InputStream dataStream)
    {
        handler.onDIMSE(as, pcid, command, dataStream);
    }

    public void onOpened(Association as)
    {
        handler.onOpened(as);
    }

}
