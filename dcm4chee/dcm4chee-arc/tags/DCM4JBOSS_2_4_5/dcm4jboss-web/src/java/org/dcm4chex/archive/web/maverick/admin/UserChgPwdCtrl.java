/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
package org.dcm4chex.archive.web.maverick.admin;

import org.dcm4chex.archive.web.maverick.Errable;

/**
 * @author franz.willer@gwi-ag.com
 * @version $Revision$ $Date$
 */
public class UserChgPwdCtrl extends Errable
{
	private String userID;
	private String popupMsg;

	/**
	 * @return Returns the userID.
	 */
	public String getUserID() {
		return userID;
	}
	public String getPopupMsg() {
		return popupMsg;
	}
	
	protected String perform() throws Exception
	{
		UserAdminModel model = UserAdminModel.getModel(this.getCtx().getRequest());
		userID = getCtx().getRequest().getUserPrincipal().getName();
		popupMsg = model.getPopupMsg();
		return "success";
	}		
}
