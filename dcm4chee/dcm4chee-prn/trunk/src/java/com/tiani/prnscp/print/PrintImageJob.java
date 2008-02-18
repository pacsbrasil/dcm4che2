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
import java.awt.print.Pageable;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import javax.print.attribute.standard.Chromaticity;

/**
 *  <description>
 *
 * @author     <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
 * @since      March 30, 2003
 * @version    $Revision$
 */
class PrintImageJob implements Pageable
{
    // Variables -----------------------------------------------------
    private final PrintableFilmBox filmBox;
    private final String name;

    // Constructors --------------------------------------------------
    /**
     *Constructor for the PrintImageJob object
     *
     * @param  service          Description of the Parameter
     * @param  f                Description of the Parameter
     * @param  config           Description of the Parameter
     * @exception  IOException  Description of the Exception
     */
    public PrintImageJob(PrinterService service, File f, String config,
            Chromaticity chromaticity)
        throws IOException
    {
        if (!f.isFile()) {
            throw new FileNotFoundException("Could not find " + f);
        }
        this.name = f.getName() + "[" + chromaticity + "/" +
            (config.length() == 0 ? "not calibrated" : config) + "]";
        this.filmBox = new PrintableFilmBox(service, f, config, chromaticity, name);
    }


    // Methodes ------------------------------------------------------
    /**
     *  Gets the name attribute of the PrintImageJob object
     *
     * @return    The name value
     */
    public String getName()
    {
        return name;
    }

    // Pageable implementation ---------------------------------------

    /**
     *  Gets the numberOfPages attribute of the PrintImageJob object
     *
     * @return    The numberOfPages value
     */
    public int getNumberOfPages()
    {
        return 1;
    }


    /**
     *  Gets the pageFormat attribute of the PrintImageJob object
     *
     * @param  pageIndex                      Description of the Parameter
     * @return                                The pageFormat value
     * @exception  IndexOutOfBoundsException  Description of the Exception
     */
    public PageFormat getPageFormat(int pageIndex)
        throws IndexOutOfBoundsException
    {
        if (pageIndex != 0) {
            throw new IndexOutOfBoundsException("pageIndex: " + pageIndex);
        }
        return filmBox.getPageFormat();
    }


    /**
     *  Gets the printable attribute of the PrintImageJob object
     *
     * @param  pageIndex                      Description of the Parameter
     * @return                                The printable value
     * @exception  IndexOutOfBoundsException  Description of the Exception
     */
    public Printable getPrintable(int pageIndex)
        throws IndexOutOfBoundsException
    {
        if (pageIndex != 0) {
            throw new IndexOutOfBoundsException("pageIndex: " + pageIndex);
        }
        return filmBox;
    }
}

