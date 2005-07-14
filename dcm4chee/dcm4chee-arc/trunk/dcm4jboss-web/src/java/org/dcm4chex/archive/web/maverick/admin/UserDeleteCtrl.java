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
public class UserDeleteCtrl extends Errable
{
	private int userHash;

	/**
	 * @return Returns the userID.
	 */
	public int getUserHash() {
		return userHash;
	}
	/**
	 * @param userID The userID to set.
	 */
	public void setUserHash(int userHash) {
		this.userHash = userHash;
	}
	protected String perform() throws Exception
	{
		UserAdminModel model = UserAdminModel.getModel( getCtx().getRequest() );
		if ( !model.isAdmin() ) return "error";
		model.deleteUser( userHash );//Any failure will be marked in popUpMessage!
		return "success";
	}		
}
