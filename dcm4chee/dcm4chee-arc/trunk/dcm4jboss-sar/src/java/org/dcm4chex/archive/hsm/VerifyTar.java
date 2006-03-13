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
 * Java(TM), available at http://sourceforge.net/projects/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * TIANI Medgraph AG.
 * Portions created by the Initial Developer are Copyright (C) 2003-2005
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Gunter Zeilinger <gunter.zeilinger@tiani.com>
 * Franz Willer <franz.willer@gwi-ag.com>
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


package org.dcm4chex.archive.hsm;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;

import org.apache.commons.compress.tar.TarEntry;
import org.apache.commons.compress.tar.TarInputStream;

/**
 * @author gunter.zeilinger@tiani.com
 * @version $Revision$ $Date$
 * @since Mar 1, 2006
 */
public class VerifyTar {

	private byte[] buf = new byte[8192];
	
	public void verify(InputStream in, String tarname) throws IOException, VerifyTarException {
		TarInputStream tar = new TarInputStream(in);
		try {
			TarEntry entry = tar.getNextEntry();
			if (entry == null)
				throw new VerifyTarException("No entries in " + tarname);
			String entryName = entry.getName();
			if (!"MD5SUM".equals(entryName))
				throw new VerifyTarException("Missing MD5SUM entry in " + tarname);
			DataInputStream dis = new DataInputStream(tar);
			HashMap md5sums = new HashMap();
			String line;
			while ((line = dis.readLine()) != null) {
				char[] c = line.toCharArray();
				byte[] md5sum = new byte[16];
				for (int i = 0, j = 0; i < md5sum.length; i++,j++,j++) {
					md5sum[i] = (byte) ((fromHexDigit(c[j])<<4) | fromHexDigit(c[j+1]));
				}
				md5sums.put(line.substring(34), md5sum);
			}
			MessageDigest digest;
			try {
				digest = MessageDigest.getInstance("MD5");
			} catch (NoSuchAlgorithmException e) {
				throw new RuntimeException(e);
			}
			while ((entry = tar.getNextEntry()) != null) {
				entryName = entry.getName();
				byte[] md5sum = (byte[]) md5sums.remove(entryName);
				if (md5sum == null)
					throw new VerifyTarException("Unexpected TAR entry: " +
							entryName + " in " + tarname);
				digest.reset();
				in = new DigestInputStream(tar, digest);
				while (in.read(buf) > 0);
				if (!Arrays.equals(digest.digest(),md5sum)) {
					throw new VerifyTarException("Failed MD5 check of TAR entry: " +
							entryName + " in " + tarname);
				}				
			}
			if (!md5sums.isEmpty())
				throw new VerifyTarException("Missing TAR entries: " +
						md5sums.keySet() + " in " + tarname);
		} finally {
			tar.close();
		}
	}

	private static int fromHexDigit(char c) {
		return c - 
			((c <= '9') ? '0' :
				(((c <= 'F') ? 'A' : 'a') - 10));
	}

	public static void main(String[] args) {
		int errors = 0;
		VerifyTar inst = new VerifyTar();
		for (int i = 0; i < args.length; i++) {
			try {
				errors += inst.verify(new File(args[i]));
			} catch (FileNotFoundException e) {
				System.err.println(e.getMessage());
				System.exit(2);
			}
		}
		System.exit(-errors);
	}

	private int verify(File file) throws FileNotFoundException {
		int errors = 0;
		if (file.isDirectory()) {
			String[] ss = file.list();
			for (int i = 0; i < ss.length; i++) {
				errors += verify(new File(file, ss[i]));				
			}
		} else {
			FileInputStream in = new FileInputStream(file);
			String tarname = file.getPath();
			
			try {
				System.out.print(tarname);
				System.out.print(' ');
				verify(in, tarname);
				System.out.println("ok");
			} catch (Exception e) {
				errors = 1;
				System.out.println(e.getMessage());
			}
		}
		return errors;
	}

}
