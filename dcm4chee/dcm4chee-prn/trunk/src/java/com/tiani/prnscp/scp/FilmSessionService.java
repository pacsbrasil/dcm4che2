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

import org.dcm4che.data.Command;
import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.data.DcmValueException;
import org.dcm4che.dict.Status;
import org.dcm4che.dict.Tags;
import org.dcm4che.dict.UIDs;
import org.dcm4che.net.AAssociateRQ;
import org.dcm4che.net.ActiveAssociation;
import org.dcm4che.net.Association;
import org.dcm4che.net.AssociationListener;
import org.dcm4che.net.DcmServiceBase;
import org.dcm4che.net.DcmServiceException;
import org.dcm4che.net.Dimse;
import org.dcm4che.net.PDU;
import org.dcm4che.net.PresContext;

/**
 *  <description>
 *
 * @author  <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
 * @created  November 9, 2002
 * @version  $Revision$
 */
class FilmSessionService
         extends DcmServiceBase
         implements AssociationListener
{
    // Constants -----------------------------------------------------

    // Attributes ----------------------------------------------------
    private final DcmObjectFactory dof = DcmObjectFactory.getInstance();
    private final PrintScpService scp;


    // Static --------------------------------------------------------

    // Constructors --------------------------------------------------
    /**
     *  Constructor for the FilmSessionService object
     *
     * @param  scp Description of the Parameter
     */
    public FilmSessionService(PrintScpService scp)
    {
        this.scp = scp;
    }


    // Public --------------------------------------------------------

    // AssociationListener implementation ----------------------------
    /**
     *  Description of the Method
     *
     * @param  as Description of the Parameter
     */
    public void close(Association as)
    {
        FilmSession session = (FilmSession) as.getProperty("FilmSession");
        if (session != null) {
            scp.purgeSessionSpoolDir(session.dir(), true);
        }
    }


    /**
     *  Description of the Method
     *
     * @param  association Description of the Parameter
     * @param  iOException Description of the Parameter
     */
    public void error(Association association, IOException iOException)
    {
    }


    /**
     *  Description of the Method
     *
     * @param  association Description of the Parameter
     * @param  dimse Description of the Parameter
     */
    public void received(Association association, Dimse dimse)
    {
    }


    /**
     *  Description of the Method
     *
     * @param  association Description of the Parameter
     * @param  pDU Description of the Parameter
     */
    public void received(Association association, PDU pDU)
    {
    }


    /**
     *  Description of the Method
     *
     * @param  association Description of the Parameter
     * @param  dimse Description of the Parameter
     */
    public void write(Association association, Dimse dimse)
    {
    }


    /**
     *  Description of the Method
     *
     * @param  association Description of the Parameter
     * @param  pDU Description of the Parameter
     */
    public void write(Association association, PDU pDU)
    {
    }


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
            String uid = rspCmd.getAffectedSOPInstanceUID();
            Dataset ds = rq.getDataset();
            // read out dataset
            if (ds == null) {
                scp.getLog().warn("Create Film Session without attributes");
                ds = dof.newDataset();
            }
            Association a = as.getAssociation();
            String aet = a.getCalledAET();
            AAssociateRQ aarq = a.getAAssociateRQ();
            PresContext pc = aarq.getPresContext(rq.pcid());
            String asuid = pc.getAbstractSyntaxUID();
            scp.getLog().info("Creating Film Session[uid=" + uid + "]");
            if (scp.getFilmSession(as) != null) {
                throw new DcmServiceException(Status.ProcessingFailure,
                        "Only support one Basic Film Session SOP Instance on an Association.");
            }
            File dir = scp.getSessionSpoolDir(a, uid);
            if (dir.exists()) {
                throw new DcmServiceException(Status.DuplicateSOPInstance);
            }
            boolean color = asuid.equals(UIDs.BasicColorPrintManagement);
            FilmSession session = new FilmSession(scp, aet, color, uid, ds, dir, rspCmd);
            scp.initSessionSpoolDir(dir);
            a.putProperty("FilmSession", session);
            a.addAssociationListener(this);
            scp.getLog().info("Create Film Session[uid=" + uid + "]");
            return null;
        } catch (DcmServiceException e) {
            scp.getLog().warn("Failed to create Basic Film Session SOP Instance", e);
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
    protected Dataset doNSet(ActiveAssociation as, Dimse rq, Command rspCmd)
        throws IOException, DcmServiceException
    {
        try {
            Dataset ds = rq.getDataset();
            // read out dataset
            String uid = rq.getCommand().getRequestedSOPInstanceUID();
            FilmSession session = scp.getFilmSession(as);
            if (session == null || !uid.equals(session.uid())) {
                throw new DcmServiceException(Status.NoSuchObjectInstance);
            }
            session.updateAttributes(ds, rspCmd);
            return null;
        } catch (DcmServiceException e) {
            scp.getLog().warn("Failed to update Basic Film Session SOP Instance", e);
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
            if (session == null || !uid.equals(session.uid())) {
                throw new DcmServiceException(Status.NoSuchObjectInstance);
            }
            String aet = as.getAssociation().getCalledAET();
            scp.createPrintJob(aet, session, true);
            return null;
        } catch (DcmServiceException e) {
            scp.getLog().warn("Failed to print Basic Film Session SOP Instance", e);
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
            if (session == null || !uid.equals(session.uid())) {
                throw new DcmServiceException(Status.NoSuchObjectInstance);
            }
            scp.purgeSessionSpoolDir(session.dir(), true);
            Association a = as.getAssociation();
            a.putProperty("FilmSession", null);
            a.removeAssociationListener(this);
            return null;
        } catch (DcmServiceException e) {
            scp.getLog().warn("Failed to delete Basic Film Session SOP Instance", e);
            throw e;
        }
    }

    // Package protected ---------------------------------------------

    // Protected -----------------------------------------------------

    // Private -------------------------------------------------------

    // Inner classes -------------------------------------------------
}

