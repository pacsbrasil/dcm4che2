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

import java.io.InputStream;
import java.util.Properties;

/**
 * @author Gunter.Zeilinger@tiani.com
 * @version $Revision$ $Date$
 * @since 25.08.2003
 */
public class JdbcProperties extends Properties {

    private static final String[] FK_FIELDS =
        {
            "Study",
            "patient_fk",
            "Series",
            "study_fk",
            "Instance",
            "series_fk",
            "Instance",
            "srcode_fk",
            "File",
            "instance_fk",
            };
    private static final JdbcProperties instance = new JdbcProperties();

    public static JdbcProperties getInstance() {
        return instance;
    }

    public String[] getProperties(String[] keys) {
        String[] values = new String[keys.length];
        for (int i = 0; i < keys.length; i++)
            values[i] = getProperty(keys[i]);
        return values;
    }

    public String getProperty(String key) {
        String value = super.getProperty(key);
        if (value == null)
            throw new IllegalArgumentException("key: " + key);
        return value;
    }

    private JdbcProperties() {
        try {
            InputStream in =
                JdbcProperties.class.getResourceAsStream("Jdbc.properties");
            load(in);
            in.close();
            for (int i = 0; i < FK_FIELDS.length; ++i, ++i)
                put(
                    FK_FIELDS[i] + '.' + FK_FIELDS[i + 1],
                    getProperty(FK_FIELDS[i]) + '.' + FK_FIELDS[i + 1]);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load jdbc properties", e);
        }
    }

}
