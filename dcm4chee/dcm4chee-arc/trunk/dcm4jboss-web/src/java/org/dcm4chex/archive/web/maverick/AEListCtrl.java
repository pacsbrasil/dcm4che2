/* $Id$
 * Copyright (c) 2002,2003 by TIANI MEDGRAPH AG
 *
 * This file is part of dcm4che.
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.dcm4chex.archive.web.maverick;


import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import org.dcm4chex.archive.ejb.interfaces.ContentEdit;
import org.dcm4chex.archive.ejb.interfaces.ContentEditHome;
import org.dcm4chex.archive.ejb.interfaces.ContentManager;
import org.dcm4chex.archive.ejb.interfaces.ContentManagerHome;
import org.dcm4chex.archive.util.EJBHomeFactory;
import org.infohazard.maverick.ctl.ThrowawayBean2;

/**
 * @author umberto.cappellini@tiani.com
 */
public class AEListCtrl extends ThrowawayBean2
{
	public List getAEs() 
	{
		try
		{
			return lookupContentManager().getAes();
		} catch (RemoteException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			return new ArrayList();
		} catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			return new ArrayList();			
		}
	}
	
	private ContentManager lookupContentManager() throws Exception
	{
		ContentManagerHome home =
			(ContentManagerHome) EJBHomeFactory.getFactory().lookup(
					ContentManagerHome.class,
					ContentManagerHome.JNDI_NAME);
		return home.create();
	}

}
