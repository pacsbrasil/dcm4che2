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

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import org.dcm4che.data.Command;
import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.dict.Status;
import org.dcm4che.dict.Tags;
import org.dcm4che.dict.UIDs;
import org.dcm4che.net.AAssociateRQ;
import org.dcm4che.net.ActiveAssociation;
import org.dcm4che.net.Association;
import org.dcm4che.net.DcmServiceBase;
import org.dcm4che.net.DcmServiceException;
import org.dcm4che.net.Dimse;
import org.dcm4che.net.PresContext;

/**
 *  <description>
 *
 * @author  <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
 * @created  November 9, 2002
 * @version  $Revision$
 */
class FilmBoxService extends DcmServiceBase
{
    // Constants -----------------------------------------------------

    // Attributes ----------------------------------------------------
    private final DcmObjectFactory dof = DcmObjectFactory.getInstance();
    private final PrintScpService scp;


    // Static --------------------------------------------------------

    // Constructors --------------------------------------------------
    /**
     *  Constructor for the FilmBoxService object
     *
     * @param  scp Description of the Parameter
     */
    public FilmBoxService(PrintScpService scp)
    {
        this.scp = scp;
    }


    // Public --------------------------------------------------------

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
    protected Dataset doNCreate(ActiveAssociation as, Dimse rq, Command rspCmd)
        throws IOException, DcmServiceException
    {
        try {
            String aet = as.getAssociation().getCalledAET();
            String uid = rspCmd.getAffectedSOPInstanceUID();
            Dataset ds = rq.getDataset();
            scp.logDataset("Create Film Box:\n", ds);
            // read out dataset
            if (ds == null) {
                scp.getLog().warn("Create Film Box without attributes");
                ds = dof.newDataset();
            }
            scp.getLog().info("Creating Film Box[uid=" + uid + "]");
            FilmSession session = scp.getFilmSession(as);
            HashMap pluts = scp.getPresentationLUTs(as);
            checkRefFilmSession(ds, session);
            if (session.containsFilmBox(uid)) {
                throw new DcmServiceException(Status.DuplicateSOPInstance);
            }
            session.addFilmBox(uid, new FilmBox(scp, aet, uid, ds, pluts, session, rspCmd));
            scp.getLog().info("Created Film Box[uid=" + uid + "]");
            return ds;
        } catch (DcmServiceException e) {
            scp.getLog().warn("Failed to create Basic Film Box SOP Instance", e);
            throw e;
        }
    }


    private void checkRefFilmSession(Dataset ds, FilmSession session)
        throws DcmServiceException
    {
        if (session == null) {
            throw new DcmServiceException(Status.ProcessingFailure,
                    "No Film Session");
        }
        try {
            Dataset ref = ds.getItem(Tags.RefFilmSessionSeq);
            if (!ref.getString(Tags.RefSOPClassUID).equals(UIDs.BasicFilmSession)) {
                throw new DcmServiceException(Status.InvalidAttributeValue);
            }
            if (!ref.getString(Tags.RefSOPInstanceUID).equals(session.uid())) {
                throw new DcmServiceException(Status.InvalidAttributeValue);
            }
        } catch (NullPointerException e) {
            throw new DcmServiceException(Status.MissingAttribute);
        }
    }


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
            scp.logDataset("Set Film Box:\n", ds);
            // read out dataset
            String uid = rq.getCommand().getRequestedSOPInstanceUID();
            FilmSession session = scp.getFilmSession(as);
            HashMap pluts = scp.getPresentationLUTs(as);
            if (session == null || !uid.equals(session.getCurrentFilmBoxUID())) {
                throw new DcmServiceException(Status.NoSuchObjectInstance);
            }
            FilmBox film = session.getCurrentFilmBox();
            film.updateAttributes(ds, pluts, session.getAttributes(), rspCmd);
            return null;
        } catch (DcmServiceException e) {
            scp.getLog().warn("Failed to update Basic Film Box SOP Instance", e);
            throw e;
        }
    }


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
    protected Dataset doNAction(ActiveAssociation as, Dimse rq, Command rspCmd)
        throws IOException, DcmServiceException
    {
        try {
            String uid = rq.getCommand().getRequestedSOPInstanceUID();
            FilmSession session = scp.getFilmSession(as);
            if (session == null || !uid.equals(session.getCurrentFilmBoxUID())) {
                throw new DcmServiceException(Status.NoSuchObjectInstance);
            }
            String aet = as.getAssociation().getCalledAET();
            scp.createPrintJob(aet, session, false);
            return null;
        } catch (DcmServiceException e) {
            scp.getLog().warn("Failed to print Basic Film Box SOP Instance", e);
            throw e;
        }
    }


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
    protected Dataset doNDelete(ActiveAssociation as, Dimse rq, Command rspCmd)
        throws IOException, DcmServiceException
    {
        try {
            String uid = rq.getCommand().getRequestedSOPInstanceUID();
            FilmSession session = scp.getFilmSession(as);
            if (session == null || !uid.equals(session.getCurrentFilmBoxUID())) {
                throw new DcmServiceException(Status.NoSuchObjectInstance);
            }
            session.deleteFilmBox();
            return null;
        } catch (DcmServiceException e) {
            scp.getLog().warn("Failed to delete Basic Film Box SOP Instance", e);
            throw e;
        }

    }

    // Package protected ---------------------------------------------

    // Protected -----------------------------------------------------

    // Private -------------------------------------------------------

    // Inner classes -------------------------------------------------
}

