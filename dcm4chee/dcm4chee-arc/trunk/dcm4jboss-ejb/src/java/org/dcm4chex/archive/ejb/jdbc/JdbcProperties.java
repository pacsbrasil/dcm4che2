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
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * @author Gunter.Zeilinger@tiani.com
 * @version $Revision$
 * @since 25.08.2003
 */
public class JdbcProperties extends Properties {

    private static final int NONE = -1;
    private static final int EJB_NAME = 0;
    private static final int TABLE_NAME = 1;
    private static final int FIELD_NAME = 2;
    private static final int COLUMN_NAME = 3;
    private static final String ENTITY = "entity";

    private class SAXHandler extends DefaultHandler {
        private final String[] QNAMES = {
            "ejb-name",        
            "table-name",
            "field-name",
            "column-name",
        };
        private List QNAMES_AS_LIST = Arrays.asList(QNAMES);
        private int curEl = NONE;
        private StringBuffer text = new StringBuffer();
        private String ejb;
        private String field;
        private boolean inEntity = false;
        private String table;

        public void characters(char[] ch, int start, int length)
            throws SAXException {
            if (curEl != NONE) {
                for (int i = 0; i < length; i++) {
                    text.append(ch[start + i]);
                }
            }
        }

        public void endElement(String uri, String localName, String qName)
            throws SAXException {
            if (inEntity) {
                switch (curEl) {
                    case NONE:
                        if (ENTITY.equals(qName)) {
                            inEntity = false;
                        }
                        break;
                    case EJB_NAME :
                        ejb = text.toString();
                        break;
                    case TABLE_NAME :
                        put(ejb, table = text.toString());
                        break;
                    case FIELD_NAME :
                        field = text.toString();
                        break;
                    case COLUMN_NAME :
                        put(ejb + '.' + field, table + '.' + text.toString());
                        break;
                }
            }
            text.setLength(0);
            curEl = NONE;
        }

        public void startElement(
            String uri,
            String localName,
            String qName,
            Attributes attributes)
            throws SAXException {
            if (inEntity) {
                curEl = QNAMES_AS_LIST.indexOf(qName);
            } else {
                inEntity = ENTITY.equals(qName);
            }
        }

    }
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
            "File",
            "directory_fk",
            };
    private static final JdbcProperties instance = new JdbcProperties();
    private static final String JBOSSCMP_JDBC_XML =
        "META-INF/jbosscmp-jdbc.xml";

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
            SAXParserFactory fact = SAXParserFactory.newInstance();
            SAXParser parser = fact.newSAXParser();
            ClassLoader ccl = Thread.currentThread().getContextClassLoader();
            InputStream in = ccl.getResourceAsStream(JBOSSCMP_JDBC_XML);
            parser.parse(in, new SAXHandler());
            for (int i = 0; i < FK_FIELDS.length; ++i, ++i)
                put(
                    FK_FIELDS[i] + '.' + FK_FIELDS[i + 1],
                    getProperty(FK_FIELDS[i]) + '.' + FK_FIELDS[i + 1]);
        } catch (Exception e) {
            throw new RuntimeException(
                "Failed to load jdbc properties from " + JBOSSCMP_JDBC_XML,
                e);
        }
    }

}
