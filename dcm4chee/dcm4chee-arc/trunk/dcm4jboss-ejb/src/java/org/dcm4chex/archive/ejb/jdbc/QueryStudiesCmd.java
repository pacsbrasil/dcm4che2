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
package org.dcm4chex.archive.ejb.jdbc;

import java.sql.SQLException;

import javax.sql.DataSource;

import org.dcm4chex.archive.ejb.interfaces.StudyFilterDTO;

/**
 * 
 * @author gunter.zeilinger@tiani.com
 * @version $Revision$ $Date$
 * @since 14.01.2004
 */
public class QueryStudiesCmd extends BaseCmd {

    private static final String[] SELECT_ATTRIBUTE =
        {
            "Patient.pk",
            "Patient.encodedAttributes",
            "Study.pk",
            "Study.encodedAttributes",
            "Study.numberOfStudyRelatedSeries", 
            "Study.numberOfStudyRelatedInstances", 
};
    /**
     * @param ds
     * @throws SQLException
     */
    public QueryStudiesCmd(DataSource ds, StudyFilterDTO filter) throws SQLException {
        super(ds);
        // TODO Auto-generated constructor stub
    }

}
