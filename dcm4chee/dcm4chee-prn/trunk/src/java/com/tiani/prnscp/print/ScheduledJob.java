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
 *                                                                           *
 */
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

