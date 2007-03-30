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
 * Bill Wallace, Agfa HealthCare Inc., 
 * Portions created by the Initial Developer are Copyright (C) 2007
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Bill Wallace <bill.wallace@agfa.com>
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
package org.dcm4chee.xero.search.study;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.net.CommandUtils;
import org.dcm4che2.net.DimseRSP;
import org.dcm4chee.xero.search.ResultFromDicom;

/**
 * This class handles creating or extending results from DICOM query results.
 * 
 * @author bwallace
 * 
 */
@XmlRootElement(name="results")
public class ResultsBean extends ResultsType implements ResultFromDicom {

	@XmlTransient
	private Map<PatientIdentifier, PatientBean> patients = new HashMap<PatientIdentifier, PatientBean>();

	/** Create an empty results */
	public ResultsBean() {
	}

	/**
	 * Create a results based on the provided query. This will have various
	 * types of objects in it, to whatever depth is required to represent the
	 * provided data. That is, if the data only includes down to series level
	 * data, then only series level will be provided. At each level, the UID for
	 * that level must be available for lookup/usage.
	 * @throws InterruptedException 
	 * @throws IOException 
	 */
	public ResultsBean(DimseRSP rsp) throws IOException, InterruptedException {
		addResults(rsp);
	}

	/* (non-Javadoc)
	 * @see org.dcm4chee.xero.search.study.ResultsFromDicom#addResults(org.dcm4che2.net.DimseRSP)
	 */
	public void addResults(DimseRSP rsp) throws IOException, InterruptedException {
		while (rsp.next()) {
			DicomObject cmd = rsp.getCommand();
			if (CommandUtils.isPending(cmd)) {
				DicomObject data = rsp.getDataset();
				addResult(data);
			}
		}
	}
	
	/** Add a single result to the results list */
	public void addResult(DicomObject data) {
		PatientIdentifier pi = new PatientIdentifier(data);
		if( patients.containsKey(pi) ) {
			ResultFromDicom pb = patients.get(pi);
			pb.addResult(data);
		}
		else {
			PatientBean pb = new PatientBean(data);
			patients.put(pi,pb);
			pb.setPatientIdentifier(pi.toString());
			getPatient().add(pb);
		}
	}
}
