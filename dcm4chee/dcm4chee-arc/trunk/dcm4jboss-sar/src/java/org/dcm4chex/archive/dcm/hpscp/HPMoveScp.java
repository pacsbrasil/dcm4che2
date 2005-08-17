/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/

package org.dcm4chex.archive.dcm.hpscp;

import org.dcm4che.net.DcmServiceBase;
import org.jboss.logging.Logger;

/**
 * @author gunter.zeilinger@tiani.com
 * @version $Revision$ $Date$
 * @since Aug 17, 2005
 */
public class HPMoveScp extends DcmServiceBase {

	private final HPScpService service;

	private final Logger log;


	public HPMoveScp(HPScpService service) {
		this.service = service;
		this.log = service.getLog();
	}
}
