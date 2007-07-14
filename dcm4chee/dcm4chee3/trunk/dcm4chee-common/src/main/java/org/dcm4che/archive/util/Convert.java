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
 * Accurate Software Design, LLC.
 * Portions created by the Initial Developer are Copyright (C) 2006
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Gunter Zeilinger <gunter.zeilinger@tiani.com>
 * Franz Willer <franz.willer@gwi-ag.com>
 * Damien Evans <damien.daddy@gmail.com>
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
package org.dcm4che.archive.util;

public class Convert {

	public static byte[] toBytes(long n) {
		byte[] b = new byte[8];
		b[0] = (byte) (n >>> 56);
		b[1] = (byte) (n >>> 48);
		b[2] = (byte) (n >>> 40);
		b[3] = (byte) (n >>> 32);
		b[4] = (byte) (n >>> 24);
		b[5] = (byte) (n >>> 16);
		b[6] = (byte) (n >>> 8);
		b[7] = (byte) (n);

		return b;
	}

	public static long toLong(byte[] b) {
		long n = (long) (b[0] & 0xff) << 56 | (long) (b[1] & 0xff) << 48
				| (long) (b[2] & 0xff) << 40 | (long) (b[3] & 0xff) << 32
				| (long) (b[4] & 0xff) << 24 | (long) (b[5] & 0xff) << 16
				| (long) (b[6] & 0xff) << 8 | (b[7] & 0xff);

		return n;
	}
}
