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
 * Time: 16:22:09
 * CVS Revision: $Revision$
 * Last CVS Commit: $Date$
 * Author of last CVS Commit: $Author$
 */
package org.dcm4chex.archive.ejb.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Iterator;

import javax.ejb.EJBException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.log4j.Logger;

/**
 * @author <a href="mailto:gunter@tiani.com">Gunter Zeilinger</a>
 *
 */
abstract class BaseQueryCmd
{
    private static final Logger log = Logger.getLogger(BaseQueryCmd.class);
    
    protected Connection con;
    protected Statement stmt;
    protected ResultSet rs = null;
    protected HashMap prepStmts = new HashMap();
    
    protected BaseQueryCmd() {
        try {
            Context cxt = new InitialContext();
            DataSource ds = 
                (DataSource) cxt.lookup("java:comp/env/jdbc/DefaultDS");
            con = ds.getConnection();
            stmt = con.createStatement();
        } catch (SQLException e) {
            if (con != null) {
                try { con.close(); } catch (SQLException ignore) {}
            }
            throw new EJBException(e);
        } catch (NamingException e) {
            throw new EJBException(e);
        }
    }
    
    public PreparedStatement getPreparedStatement(String sql) {
        PreparedStatement pstmt = (PreparedStatement)prepStmts.get(sql);
        if (pstmt != null) {
            return pstmt;
        }
        try {
            log.debug("SQL: " + sql);
            pstmt = con.prepareStatement(sql);
            prepStmts.put(sql, pstmt);
            return pstmt;
        } catch (SQLException e) {
            close();
            throw new EJBException(e);
        }
    }

    public void execute(String sql) {
        if (stmt == null) {
            throw new IllegalStateException();
        }
        if (rs != null) {
            throw new IllegalStateException();
        }
        try {
            log.debug("SQL: " + sql);
            rs = stmt.executeQuery(sql);
        } catch (SQLException e) {
            close();
            throw new EJBException(e);
        }
     }
    
    public boolean next() {
        if (rs == null) {
            throw new IllegalStateException();
        }
        try {
            return rs.next();
        } catch (SQLException e) {
            close();
            throw new EJBException(e);
        }
    }
    
    public void close() {
        if (rs != null) {
            try { rs.close(); } catch (SQLException ignore) {}
            rs = null;
        }
        if (stmt != null) {
            try { stmt.close(); } catch (SQLException ignore) {}
            stmt = null;
        }
        for (Iterator it = prepStmts.values().iterator(); it.hasNext();) {
            PreparedStatement ps = (PreparedStatement)it.next();
            try { ps.close(); } catch (SQLException ignore) {}
        }
        prepStmts.clear();
        if (con != null) {
            try { con.close(); } catch (SQLException ignore) {}
            con = null;
        }
    }
}
