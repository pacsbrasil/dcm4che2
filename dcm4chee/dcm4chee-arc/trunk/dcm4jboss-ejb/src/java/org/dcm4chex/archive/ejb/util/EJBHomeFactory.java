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
/* 
 * File: $Source$
 * Author: gunter
 * Date: 16.07.2003
 * Time: 09:05:24
 * CVS Revision: $Revision$
 * Last CVS Commit: $Date$
 * Author of last CVS Commit: $Author$
 */
package org.dcm4chex.archive.ejb.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.ejb.EJBHome;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.rmi.PortableRemoteObject;

/**
 * @author <a href="mailto:gunter@tiani.com">Gunter Zeilinger</a>
 *
 */
public class EJBHomeFactory
{

    private static final String EJB_COMP_JNDI = "java:comp/env/ejb/";

    private static final String JNDI_PROPERTIES = "jndi.properties";

    private static EJBHomeFactory factory;

    private Map ejbHomes;

    private Context ctx;

    private EJBHomeFactory() throws NamingException, IOException
    {
        ctx = new InitialContext(loadJndiProperties());
        this.ejbHomes = Collections.synchronizedMap(new HashMap());
    }

    private Properties loadJndiProperties() throws IOException
    {
        Properties env = new Properties();
        InputStream is =
            Thread.currentThread().getContextClassLoader().getResourceAsStream(
                JNDI_PROPERTIES);
        try
        {
            env.load(is);
        }
        finally
        {
            try
            {
                is.close();
            }
            catch (Exception ignore)
            {
            }
        }
        return env;
    }

    public static EJBHomeFactory getInstance() throws EJBHomeFactoryException
    {
        try
        {
            if (EJBHomeFactory.factory == null)
            {
                EJBHomeFactory.factory = new EJBHomeFactory();
            }
        }
        catch (Exception e)
        {
            throw new EJBHomeFactoryException(e);
        }
        return EJBHomeFactory.factory;
    }

    public EJBHome lookup(Class homeClass) throws EJBHomeFactoryException
    {
        EJBHome home = (EJBHome) ejbHomes.get(homeClass);
        if (home == null)
        {
            try
            {
                home =
                    (EJBHome) PortableRemoteObject.narrow(
                        ctx.lookup(
                            (String) homeClass.getField("JNDI_NAME").get(null)),
                        homeClass);
                ejbHomes.put(homeClass, home);
            }
            catch (Exception e)
            {
                throw new EJBHomeFactoryException(e);
            }
        }
        return home;
    }

}
