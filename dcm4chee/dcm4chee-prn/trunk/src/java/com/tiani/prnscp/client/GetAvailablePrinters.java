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

import javax.print.DocFlavor;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;

/**
 * Description of the Class
 * 
 * @author <a href="mailto:gunter@tiani.com">gunter zeilinger </a>
 * @created January 22, 2003
 * @version $Revision$
 */
public class GetAvailablePrinters {
	final PrintService service;

	public static void main(String[] args) throws Exception {
		PrintService[] services = PrintServiceLookup.lookupPrintServices(
				DocFlavor.SERVICE_FORMATTED.PAGEABLE, null);
		if (args.length == 0)
			System.out.println("Available Printers:");
		for (int i = 0; i < services.length; ++i) {
			String name = services[i].getName();
			if (args.length == 0)
				System.out.println(name);
			else if (name.equals(args[0]))
				System.out.println(new GetAvailablePrinters(services[i]));
		}
	}

	public GetAvailablePrinters(final PrintService service) {
		this.service = service;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer("Printer: ");
		sb.append(service.getName());
		Class[] cats = service.getSupportedAttributeCategories();
		for (int i = 0; i < cats.length; i++) {
			appendSupportedValues(sb, cats[i]);
		}
		return sb.toString();
	}

	private void appendSupportedValues(StringBuffer sb, Class category) {
		sb.append("\r\n\t").append(category.getName()).append(": ");
		Object values = service.getSupportedAttributeValues(
				category, null, null);
		
		try {
			Object d = service.getDefaultAttributeValue(category);
			Object[] a = (Object[]) values;
			for (int i = 0; i < a.length; i++) {
				sb.append("\r\n\t\t");
				if (a[i].equals(d))
					sb.append('[').append(a[i]).append(']');
				else
					sb.append(a[i]);
			}
		} catch (Exception e) {
			sb.append(values);
		}
	}
}

