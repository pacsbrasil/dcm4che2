/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/

package org.dcm4che2.data;

import java.io.IOException;

/**
 * @author gunter zeilinger(gunterze@gmail.com)
 * @version $Reversion$ $Date$
 * @since Jun 26, 2005
 *
 */
public class StopTagInputHandler implements DicomInputHandler {

	private final long stopTag;

	public StopTagInputHandler(int stopTag) {
		this.stopTag = stopTag & 0xffffffffL;		
	}
	
	public boolean readValue(DicomInputStream in) throws IOException {
		if ((in.tag() & 0xffffffffL) >= stopTag 
				&& in.level() == 0)
			return false;
		return in.readValue(in);
	}

}
