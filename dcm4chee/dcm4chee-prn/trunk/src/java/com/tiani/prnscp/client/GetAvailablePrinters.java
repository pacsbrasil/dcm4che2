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

