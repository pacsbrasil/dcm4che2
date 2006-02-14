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

package org.dcm4chex.archive.config;

import java.util.StringTokenizer;
import java.util.regex.Pattern;

/**
 * @author gunter.zeilinger@tiani.com
 * @version $Revision$ $Date$
 * @since Jun 8, 2005
 */
public class IssuerOfPatientIDRules {
	
	private final Pattern[] patterns;
	private final String[] issuers;

	public IssuerOfPatientIDRules(String rules) {
		StringTokenizer stk = new StringTokenizer(rules, " ;\t\r\n");
		patterns = new Pattern[stk.countTokens()];
		issuers = new String[patterns.length];
		for (int i = 0; i < issuers.length; i++) {
			issuers[i] = stk.nextToken();
			final int beginIssuer = issuers[i].indexOf(':') + 1;
			if (beginIssuer > 0) {
				patterns[i] = Pattern.compile(issuers[i].substring(0,
						beginIssuer - 1));
				issuers[i] = issuers[i].substring(beginIssuer);
			}
		}
	}
	
	public String issuerOf(String pid) {
		for (int i = 0; i < patterns.length; i++) {
			if (patterns[i] == null || patterns[i].matcher(pid).matches()) {
				return issuers[i];
			}
		}
		return null;
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < issuers.length; i++) {
			if (patterns[i] != null) {
				sb.append(patterns[i].pattern()).append(':');
			}
			sb.append(issuers[i]).append("\r\n");
		}
		return sb.toString();
	}

}
