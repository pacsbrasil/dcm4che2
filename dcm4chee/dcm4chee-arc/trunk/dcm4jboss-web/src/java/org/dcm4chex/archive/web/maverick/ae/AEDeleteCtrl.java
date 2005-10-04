/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
package org.dcm4chex.archive.web.maverick.ae;

import org.dcm4chex.archive.web.maverick.Errable;

/**
 * @author umberto.cappellini@tiani.com
 * @author gunter.zeilinger@tiani.com
 * @version $Revision$ $Date$
 */
public class AEDeleteCtrl extends Errable
{
	private String title;

	/**
	 * @param oldPk The oldPk to set.
	 */
	public final void setTitle(String title)
	{
		this.title = title;
	}

	protected String perform() throws Exception
	{
		try
		{
			lookupAEDelegate().delAE(title);
			return "success";
		} catch (Throwable e)
		{
			this.errorType = e.getClass().getName();
			this.message = e.getMessage();
			this.backURL = "aedelete.m?title=" + this.title;
			return ERROR_VIEW;				
		}
	}	
}
