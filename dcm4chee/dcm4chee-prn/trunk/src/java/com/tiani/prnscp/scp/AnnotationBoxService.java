/*                                                                           *
 *  Copyright (c) 2002 by TIANI MEDGRAPH AG                                  *
 *                                                                           *
 *  This file is part of dcm4che.                                            *
 *                                                                           *
 *  This library is free software; you can redistribute it and/or modify it  *
 *  under the terms of the GNU Lesser General Public License as published    *
 *  by the Free Software Foundation; either version 2 of the License, or     *
 *  (at your option) any later version.                                      *
 *                                                                           *
 *  This library is distributed in the hope that it will be useful, but      *
 *  WITHOUT ANY WARRANTY; without even the implied warranty of               *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU        *
 *  Lesser General Public License for more details.                          *
 *                                                                           *
 *  You should have received a copy of the GNU Lesser General Public         *
 *  License along with this library; if not, write to the Free Software      *
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA  *
 */
package com.tiani.prnscp.scp;

import java.io.IOException;

import org.dcm4che.data.Command;
import org.dcm4che.data.Dataset;
import org.dcm4che.dict.Tags;
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

