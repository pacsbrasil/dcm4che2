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
 * Date: 10.07.2003
 * Time: 16:05:24
 * CVS Revision: $Revision$
 * Last CVS Commit: $Date$
 * Author of last CVS Commit: $Author$
 */
package org.dcm4chex.archive.ejb.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.ejb.EJBLocalHome;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 * @author <a href="mailto:gunter@tiani.com">Gunter Zeilinger</a>
 *
 */
public class EJBLocalHomeFactory {
    
    private static String EJB_COMP_JNDI = "java:comp/env/ejb/";

    private static EJBLocalHomeFactory factory;
    
    private Map ejbHomes;

    private Context ctx;

    private EJBLocalHomeFactory() throws NamingException {
        ctx = new InitialContext();
        this.ejbHomes = Collections.synchronizedMap(new HashMap());
    }
    
    public static EJBLocalHomeFactory getInstance() throws EJBHomeFactoryException
    {
        try {
            if (EJBLocalHomeFactory.factory == null) {
                EJBLocalHomeFactory.factory = new EJBLocalHomeFactory();
            }
        } catch (NamingException e) {
            throw new EJBHomeFactoryException(e);
        }
        return EJBLocalHomeFactory.factory;
    }
    
    public EJBLocalHome lookup(Class homeClass) throws EJBHomeFactoryException {
        EJBLocalHome home = (EJBLocalHome) ejbHomes.get(homeClass);
        if (home == null) {
            try {
                home = (EJBLocalHome) ctx.lookup(EJB_COMP_JNDI + homeClass.getName());
                ejbHomes.put(homeClass, home);
            } catch (NamingException e) {
                throw new EJBHomeFactoryException(e);
            }
        }
        return home;
    }

}
