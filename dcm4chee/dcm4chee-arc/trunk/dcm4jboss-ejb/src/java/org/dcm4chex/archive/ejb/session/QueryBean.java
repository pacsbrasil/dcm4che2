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
 * Date: 20.07.2003
 * Time: 15:54:52
 * CVS Revision: $Revision$
 * Last CVS Commit: $Date$
 * Author of last CVS Commit: $Author$
 */
package org.dcm4chex.archive.ejb.session;

import javax.ejb.CreateException;
import javax.ejb.SessionBean;

import org.apache.log4j.Logger;
import org.dcm4che.data.Dataset;
import org.dcm4che.net.DcmServiceException;
import org.dcm4chex.archive.ejb.jdbc.QueryCmd;

/**
 * Query Bean
 * 
 * @ejb:bean
 *  name="Query"
 *  type="Stateful"
 *  view-type="remote"
 *  jndi-name="ejb/Query"
 * 
 * @ejb:transaction-type 
 *  type="Container"
 * 
 * @ejb:transaction 
 *  type="NotSupported"
 * @ejb:resource-ref
 *  res-name="jdbc/DefaultDS"
 *  res-type="javax.sql.DataSource"
 *  res-auth="Container"
 * @jboss:resource-ref
 *  res-ref-name="jdbc/DefaultDS"
 *  resource-name="java:/PostgresDS"
 * 
 * @author <a href="mailto:gunter@tiani.com">Gunter Zeilinger</a>
 *
 */
public abstract class QueryBean implements SessionBean
{
    private Logger log = Logger.getLogger(QueryBean.class);

    private transient QueryCmd cmd = null;
    
    // Constructors --------------------------------------------------    

    /**
     * @ejb:create-method
     */
    public void ejbCreate(Dataset keys, String principal)
    throws CreateException, DcmServiceException {
        cmd = QueryCmd.create(keys, principal);
        cmd.execute();
    }

        
    public void ejbRemove() {
        if (cmd != null) {
           cmd.close();
           cmd = null;
        }
    }
    
    public void ejbPassivate() {
        if (cmd != null) {
            log.warn("cancel query by EJB passivation");
            cmd.close();
            cmd = null;
        }           
    }

   // Methods -------------------------------------------------------
   /**
    * @ejb:interface-method 
    */
   public Dataset next() {
       if (cmd == null) {
           return null;
       }
       if (cmd.next()) {
           return cmd.getDataset();
       }
       cmd.close();
       cmd = null;
       return null;
   }
}
