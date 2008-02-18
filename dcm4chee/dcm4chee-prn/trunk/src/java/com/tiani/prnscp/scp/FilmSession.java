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

import java.io.File;
import java.util.HashMap;
import java.util.LinkedHashMap;

import org.dcm4che.data.Command;
import org.dcm4che.data.Dataset;
import org.dcm4che.dict.Status;
import org.dcm4che.dict.Tags;
import org.dcm4che.dict.UIDs;
import org.dcm4che.net.DcmServiceException;

import org.jboss.logging.Logger;

/**
 *  <description>
 *
 * @author  <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
 * @created  November 14, 2002
 * @version  $Revision$
 */
class FilmSession
{

    // Constants -----------------------------------------------------
    private static final String[] PRIORITY = {
        "MED",
        "LOW",
        "HIGH"
    };

    // Attributes ----------------------------------------------------
    private final PrintScpService scp;
    private final Logger log;
    private final File dir;
    private final String aet;
    private final String uid;
    private final Dataset session;
    private final boolean color;
    private final LinkedHashMap filmBoxes = new LinkedHashMap();
    private String curFilmBoxUID = null;
    private FilmBox curFilmBox = null;


    // Static --------------------------------------------------------

    // Constructors --------------------------------------------------
    /**
     *  Constructor for the FilmSession object
     *
     * @param  scp Description of the Parameter
     * @param  aet Description of the Parameter
     * @param  color Description of the Parameter
     * @param  uid Description of the Parameter
     * @param  session Description of the Parameter
     * @param  dir Description of the Parameter
     * @param  rspCmd Description of the Parameter
     * @exception  DcmServiceException Description of the Exception
     */
    public FilmSession(PrintScpService scp, String aet, boolean color, String uid,
            Dataset session, File dir, Command rspCmd)
        throws DcmServiceException
    {
        this.scp = scp;
        this.log = scp.getLog();
        this.aet = aet;
        this.color = color;
        // metaCUID.equals(UIDs.BasicColorPrintManagement);
        this.uid = uid;
        this.session = session;
        this.dir = dir;
        check(session, rspCmd);
    }


    // Public --------------------------------------------------------
    /**
     *  Description of the Method
     *
     * @return  Description of the Return Value
     */
    public String toString()
    {
        return "FilmSession[uid=" + uid + ", " + filmBoxes.size() + " FilmBoxes]";
    }


    /**
     *  Description of the Method
     *
     * @return  Description of the Return Value
     */
    public String uid()
    {
        return uid;
    }


    /**
     *  Description of the Method
     *
     * @return  Description of the Return Value
     */
    public File dir()
    {
        return dir;
    }


    /**
     *  Gets the attributes attribute of the FilmSession object
     *
     * @return  The attributes value
     */
    public Dataset getAttributes()
    {
        return session;
    }


    /**
     *  Gets the color attribute of the FilmSession object
     *
     * @return  The color value
     */
    public boolean isColor()
    {
        return color;
    }


    /**
     *  Gets the imageBoxCUID attribute of the FilmSession object
     *
     * @return  The imageBoxCUID value
     */
    public String getImageBoxCUID()
    {
        return color
                 ? UIDs.BasicColorImageBox
                 : UIDs.BasicGrayscaleImageBox;
    }


    /**
     *  Gets the hardcopyCUID attribute of the FilmSession object
     *
     * @return  The hardcopyCUID value
     */
    public String getHardcopyCUID()
    {
        return color
                 ? UIDs.HardcopyColorImageStorage
                 : UIDs.HardcopyGrayscaleImageStorage;
    }


    /**
     *  Description of the Method
     *
     * @param  modification Description of the Parameter
     * @param  rspCmd Description of the Parameter
     * @exception  DcmServiceException Description of the Exception
     */
    public void updateAttributes(Dataset modification, Command rspCmd)
        throws DcmServiceException
    {
        check(modification, rspCmd);
        session.putAll(modification);
    }


    /**
     *  Adds a feature to the FilmBox attribute of the FilmSession object
     *
     * @param  uid The feature to be added to the FilmBox attribute
     * @param  filmbox The feature to be added to the FilmBox attribute
     */
    public void addFilmBox(String uid, FilmBox filmbox)
    {
        if (filmBoxes.containsKey(uid)) {
            throw new IllegalStateException();
        }
        this.curFilmBoxUID = uid;
        this.curFilmBox = filmbox;
        filmBoxes.put(uid, filmbox);
    }


    /**
     *  Gets the currentFilmBoxUID attribute of the FilmSession object
     *
     * @return  The currentFilmBoxUID value
     */
    public String getCurrentFilmBoxUID()
    {
        return curFilmBoxUID;
    }


    /**
     *  Gets the currentFilmBox attribute of the FilmSession object
     *
     * @return  The currentFilmBox value
     */
    public FilmBox getCurrentFilmBox()
    {
        return curFilmBox;
    }


    /**
     *  Gets the filmBoxes attribute of the FilmSession object
     *
     * @return  The filmBoxes value
     */
    public LinkedHashMap getFilmBoxes()
    {
        return filmBoxes;
    }


    /**
     *  Description of the Method
     *
     * @param  uid Description of the Parameter
     * @return  Description of the Return Value
     */
    public boolean containsFilmBox(String uid)
    {
        return filmBoxes.containsKey(uid);
    }


    /**
     *  Sets the filmBox attribute of the FilmSession object
     *
     * @param  filmbox The new filmBox value
     * @param  rspCmd The new filmBox value
     * @param  pluts The new filmBox value
     * @exception  DcmServiceException Description of the Exception
     */
    public void setFilmBox(Dataset filmbox, Command rspCmd, HashMap pluts)
        throws DcmServiceException
    {
        if (curFilmBox == null) {
            throw new IllegalStateException();
        }
        curFilmBox.updateAttributes(filmbox, pluts, session, rspCmd);
    }


    /**  Description of the Method */
    public void deleteFilmBox()
    {
        if (curFilmBox == null) {
            throw new IllegalStateException();
        }
        filmBoxes.remove(curFilmBoxUID);
        curFilmBoxUID = null;
        curFilmBox = null;
    }


    // Private -------------------------------------------------------
    private void check(Dataset ds, Command rsp)
        throws DcmServiceException
    {
        try {
            scp.checkAttribute(ds, Tags.Priority, PRIORITY, rsp);
            scp.checkAttribute(ds, Tags.MediumType,
                aet, "isSupportsMediumType", rsp);
            scp.checkNumberOfCopies(ds, aet, rsp);
            scp.ignoreAttribute(ds, Tags.MemoryAllocation, rsp,
                Status.MemoryAllocationNotSupported);
            scp.checkAttributeLen(ds, Tags.FilmSessionLabel, 64, rsp);
        } catch (Exception e) {
            log.error("Processing Failure:", e);
            throw new DcmServiceException(Status.ProcessingFailure);
        }
    }
}

