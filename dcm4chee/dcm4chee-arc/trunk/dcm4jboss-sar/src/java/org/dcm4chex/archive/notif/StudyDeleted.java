/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/

package org.dcm4chex.archive.notif;

import java.io.Serializable;

import org.dcm4che.data.Dataset;

/**
 * @author gunter.zeilinger@tiani.com
 * @version $Revision$ $Date$
 * @since Nov 8, 2005
 */
public class StudyDeleted implements Serializable {

	private static final long serialVersionUID = 3256722883605704752L;
	
	private final Dataset ian;

	public StudyDeleted(Dataset ian) {
		this.ian = ian;
	}

	public final Dataset getInstanceAvailabilityNotification() {
		return ian;
	}
}
