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
 * TIANI Medgraph AG.
 * Portions created by the Initial Developer are Copyright (C) 2002-2005
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Gunter Zeilinger <gunter.zeilinger@tiani.com>
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

package com.tiani.prnscp.scp;

import java.io.IOException;

import org.dcm4che.data.Command;
import org.dcm4che.data.Dataset;
import org.dcm4che.net.ActiveAssociation;
import org.dcm4che.net.DcmServiceBase;
import org.dcm4che.net.DcmServiceException;
import org.dcm4che.net.Dimse;

/**
 *  <description>
 *
 * @author  <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
 * @created  February 5, 2003
 * @version  $Revision$
 */
class AnnotationBoxService extends DcmServiceBase
{

    // Constants -----------------------------------------------------

    // Attributes ----------------------------------------------------
    private final PrintScpService scp;


    // Static --------------------------------------------------------

    // Constructors --------------------------------------------------
    /**
     *  Constructor for the AnnotationBoxService object
     *
     * @param  scp Description of the Parameter
     */
    public AnnotationBoxService(PrintScpService scp)
    {
        this.scp = scp;
    }


    // Public --------------------------------------------------------

    // Z implementation ----------------------------------------------

    // DcmServiceBase overrides --------------------------------------

    /**
     *  Description of the Method
     *
     * @param  as Description of the Parameter
     * @param  rq Description of the Parameter
     * @param  rspCmd Description of the Parameter
     * @return  Description of the Return Value
     * @exception  IOException Description of the Exception
     * @exception  DcmServiceException Description of the Exception
     */
    protected Dataset doNSet(ActiveAssociation as, Dimse rq, Command rspCmd)
        throws IOException, DcmServiceException
    {
        try {
            Dataset ds = rq.getDataset();
            scp.logDataset("Set Annotation Box\n:", ds);
            // read out dataset
            String uid = rq.getCommand().getRequestedSOPInstanceUID();
            FilmSession session = scp.getFilmSession(as);
            session.getCurrentFilmBox().setAnnotationBox(uid, ds);
            return null;
        } catch (DcmServiceException e) {
            scp.getLog().warn("Failed to update Basic Annotation Box SOP Instance", e);
            throw e;
        }
    }

    // Package protected ---------------------------------------------

    // Protected -----------------------------------------------------

    // Private -------------------------------------------------------

    // Inner classes -------------------------------------------------
}

