/*
 *  Copyright (c) 2003 by TIANI MEDGRAPH AG                                  *
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
package com.tiani.prnscp.client;

import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.DocFlavor;

/**
 *  Description of the Class
 *
 * @author  <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
 * @created  January 22, 2003
 * @version  $Revision$
 */
public class GetAvailablePrinters
{
    /**
     *  The main program for the GetAvailablePrinters class
     *
     * @param  args The command line arguments
     * @exception  Exception Description of the Exception
     */
    public static void main(String[] args)
        throws Exception
    {
        PrintService[] services = PrintServiceLookup.lookupPrintServices(
                DocFlavor.SERVICE_FORMATTED.PAGEABLE, null);
        System.out.println("Available Printers:");
        for (int i = 0; i < services.length; ++i) {
            System.out.println(services[i].getName());
        }
    }
}

