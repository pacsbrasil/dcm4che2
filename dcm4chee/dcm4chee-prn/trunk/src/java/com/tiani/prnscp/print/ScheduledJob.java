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

package com.tiani.prnscp.print;
import java.awt.print.PageFormat;

import java.awt.print.Pageable;
import java.awt.print.Printable;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;

import org.dcm4che.data.Dataset;
import org.dcm4che.dict.Tags;

/**
 *  <description>
 *
 * @author     <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
 * @since      March 30, 2003
 * @version    $Revision$
 */
class ScheduledJob implements Pageable
{

    // Constants -----------------------------------------------------

    // Attributes ----------------------------------------------------
    private final String path;
    private final String jobID;
    private final Dataset session;
    private final Boolean color;
    private String callingAET;
    private File hcDir;
    private File[] spFiles;
    private PrintableFilmBox[] filmBoxes;


    // Static --------------------------------------------------------

    // Constructors --------------------------------------------------
    /**
     *  Constructor for the ScheduledJob object
     *
     * @param  path     Description of the Parameter
     * @param  session  Description of the Parameter
     * @param  color    Description of the Parameter
     */
    public ScheduledJob(String path, Dataset session, Boolean color)
    {
        this.path = path;
        this.session = session;
        this.color = color;
        File jobDir = new File(path);
        this.jobID = jobDir.getName();
        File rootDir = jobDir.getParentFile().getParentFile();
        this.hcDir = new File(rootDir, "HC");
        this.callingAET = hcDir.getParentFile().getParentFile().getName();
        this.spFiles = jobDir.listFiles();
        Arrays.sort(spFiles,
            new Comparator()
            {
                public int compare(Object o1, Object o2)
                {
                    return (int) (((File) o1).lastModified()
                             - ((File) o2).lastModified());
                }
            });

    }


    // Public --------------------------------------------------------
    /**
     *  Gets the color attribute of the ScheduledJob object
     *
     * @return    The color value
     */
    public Boolean isColor()
    {
        return color;
    }


    /**
     *  Gets the callingAET attribute of the ScheduledJob object
     *
     * @return    The callingAET value
     */
    public String getCallingAET()
    {
        return callingAET;
    }


    /**
     *  Gets the session attribute of the ScheduledJob object
     *
     * @return    The session value
     */
    public Dataset getSession()
    {
        return session;
    }


    /**
     *  Gets the path attribute of the ScheduledJob object
     *
     * @return    The path value
     */
    public String getPath()
    {
        return path;
    }


    /**
     *  Gets the jobID attribute of the ScheduledJob object
     *
     * @return    The jobID value
     */
    public String getJobID()
    {
        return jobID;
    }


    /**
     *  Description of the Method
     *
     * @param  service          Description of the Parameter
     * @exception  IOException  Description of the Exception
     */
    public void initFilmBoxes(PrinterService service)
        throws IOException
    {
        String defConfig = service.getConfigurationInformationForCallingAET(
                callingAET, color);
        String defADF = service.getAnnotationForCallingAET(callingAET);
        this.filmBoxes = new PrintableFilmBox[spFiles.length];
        for (int i = 0; i < filmBoxes.length; ++i) {
            filmBoxes[i] = new PrintableFilmBox(service, hcDir, spFiles[i],
                    spFiles.length, session, callingAET, defConfig, defADF,
                    service.toChromaticity(color));
        }
    }


    /**
     *  Gets the requestedResolutionID attribute of the ScheduledJob object
     *
     * @return    The requestedResolutionID value
     */
    public String getRequestedResolutionID()
    {
        if (filmBoxes == null || filmBoxes.length == 0) {
            throw new IllegalStateException("filmbox not  yet initalized");
        }
        return filmBoxes[0].getAttributes().getString(Tags.RequestedResolutionID);
    }


    /**
     *  Gets the copies attribute of the ScheduledJob object
     *
     * @return    The copies value
     */
    public int getCopies()
    {
        return session != null
                 ? session.getInt(Tags.NumberOfCopies, 1)
                 : 1;
    }


    /**
     *  Gets the name attribute of the ScheduledJob object
     *
     * @return    The jobName value
     */
    public String getName()
    {
        return session != null
                 ? session.getString(Tags.FilmSessionLabel)
                 : callingAET;
    }

    // Pageable implementation ---------------------------------------

    /**
     *  Gets the numberOfPages attribute of the ScheduledJob object
     *
     * @return    The numberOfPages value
     */
    public int getNumberOfPages()
    {
        if (filmBoxes == null) {
            throw new IllegalStateException("filmBoxes not yet initialized!");
        }
        return filmBoxes.length;
    }


    /**
     *  Gets the pageFormat attribute of the ScheduledJob object
     *
     * @param  pageIndex                      Description of the Parameter
     * @return                                The pageFormat value
     * @exception  IndexOutOfBoundsException  Description of the Exception
     */
    public PageFormat getPageFormat(int pageIndex)
        throws IndexOutOfBoundsException
    {
        if (filmBoxes == null) {
            throw new IllegalStateException("filmBoxes not yet initialized!");
        }
        return filmBoxes[pageIndex].getPageFormat();
    }


    /**
     *  Gets the printable attribute of the ScheduledJob object
     *
     * @param  pageIndex                      Description of the Parameter
     * @return                                The printable value
     * @exception  IndexOutOfBoundsException  Description of the Exception
     */
    public Printable getPrintable(int pageIndex)
        throws IndexOutOfBoundsException
    {
        return filmBoxes[pageIndex];
    }
}

