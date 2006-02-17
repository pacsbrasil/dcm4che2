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


package org.dcm4chex.archive.dcm.qrscp;

import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.dict.Tags;
import org.dcm4che.dict.UIDs;
import org.dcm4che.net.DcmServiceException;

/**
 * @author gunter.zeilinger@tiani.com
 * @version $Revision$ $Date$
 * @since Feb 1, 2006
 */
class VMFBuilder {
	private static final int[] COMMON = {
		Tags.SOPClassUID,
		Tags.Rows,
		Tags.Columns,
		Tags.BitsAllocated,
		Tags.BitsStored,
		Tags.HighBit
	};
	private final QueryRetrieveScpService service;
	private final Dataset result;
	private final Dataset common;
	private int frames = 0;
	
	private VMFBuilder(QueryRetrieveScpService service, Dataset firstFrame,
			Dataset cfg) {
		this.service = service;
		this.result = DcmObjectFactory.getInstance().newDataset();
		this.result.putAll(cfg);
		this.result.putAll(firstFrame.subSet(cfg));
		this.common = result.subSet(COMMON);
	}

	public static VMFBuilder newVMFBuilder(QueryRetrieveScpService service, Dataset dataset) 
	throws DcmServiceException {
		String cuid = dataset.getString(Tags.SOPClassUID);		
		if (UIDs.MRImageStorage.equals(cuid))
			return new MR(service, dataset);
		if (UIDs.CTImageStorage.equals(cuid))
			return new CT(service, dataset);
		throw new DcmServiceException(0xC001, 
				"Series contains instance(s) of different SOP Classes than MR or CT - " + cuid);
	}

	private static class MR extends VMFBuilder {

		public MR(QueryRetrieveScpService service, Dataset dataset) {
			super(service, dataset, service.getVirtualEnhancedMRConfig());			
		}

	}

	private static class CT extends VMFBuilder {

		public CT(QueryRetrieveScpService service, Dataset dataset) {
			super(service, dataset, service.getVirtualEnhancedCTConfig());
		}

	}

	
	public void addFrame(Dataset frame) throws DcmServiceException {
		if (!frame.subSet(COMMON).equals(common))
			throw new DcmServiceException(0xC002, 
					"Series contains instance(s) which cannot be put into one MF image");
		
		frames++;		
	}

	public Dataset getResult() {
		result.putIS(Tags.NumberOfFrames, frames);
		return result;
	}

}
