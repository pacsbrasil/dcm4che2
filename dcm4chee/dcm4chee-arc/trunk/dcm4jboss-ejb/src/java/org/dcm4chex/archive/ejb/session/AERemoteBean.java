/*
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

package org.dcm4chex.archive.ejb.session;

import javax.ejb.EJBException;
import javax.ejb.FinderException;
import javax.ejb.ObjectNotFoundException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.dcm4chex.archive.ejb.interfaces.AEData;
import org.dcm4chex.archive.ejb.interfaces.AELocalHome;

/**
 * @author Gunter.Zeilinger@tiani.com
 * @version $Revision$
 * @since 21.11.2003
 * 
 * @ejb.bean
 *  name="AERemote"
 *  type="Stateless"
 *  view-type="remote"
 *  jndi-name="ejb/AERemote"
 * 
 * @ejb.transaction-type 
 *  type="Container"
 * 
 * @ejb.transaction 
 *  type="NotSupported"
 * 
 * @ejb.ejb-ref
 *  ejb-name="AE" 
 *  view-type="local"
 *  ref-name="ejb/AE" 
 */
public abstract class AERemoteBean implements SessionBean
{
    private AELocalHome aeHome;

    public void setSessionContext(SessionContext ctx)
    {
        Context jndiCtx = null;
        try
        {
            jndiCtx = new InitialContext();
            aeHome = (AELocalHome) jndiCtx.lookup("java:comp/env/ejb/AE");
        } catch (NamingException e)
        {
            throw new EJBException(e);
        } finally
        {
            if (jndiCtx != null)
            {
                try
                {
                    jndiCtx.close();
                } catch (NamingException ignore)
                {}
            }
        }
    }

    public void unsetSessionContext()
    {
        aeHome = null;
    }

    /**
     * @ejb.interface-method
     */
    public AEData getAEData(String aet)
    {
        try
        {
            return aeHome.findByAET(aet).getAEData();
        } catch (ObjectNotFoundException e)
        {
            return null;
        } catch (FinderException e)
        {
            throw new EJBException("Failed to access AE info for AET: " + aet, e);
        }
    }
}
