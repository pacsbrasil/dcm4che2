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

package org.dcm4chex.archive.ejb.jdbc;

import java.io.InputStream;
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

    private class SAXHandler extends DefaultHandler {
        private char[] ch;
        private String ejb;
        private String field;
        private boolean inEntity = false;
        private int length;
        private int start;
        private String table;
        public void characters(char[] ch, int start, int length)
            throws SAXException {
            this.ch = ch;
            this.start = start;
            this.length = length;
        }

        public void endElement(String uri, String localName, String qName)
            throws SAXException {
            if (!inEntity) {
                if (qName.equals(DATASOURCE))
                    put(DATASOURCE, text());
                return;
            }
            if (qName.equals(EJB_NAME))
                ejb = text();
            else if (qName.equals(TABLE_NAME))
                put(ejb, table = text());
            else if (qName.equals(FIELD_NAME))
                field = text();
            else if (qName.equals(COLUMN_NAME))
                put(ejb + '.' + field, table + '.' + text());
            else if (qName.equals(ENTITY))
                inEntity = false;
        }

        public void startElement(
            String uri,
            String localName,
            String qName,
            Attributes attributes)
            throws SAXException {
            if (qName.equals(ENTITY))
                inEntity = true;
        }

        private String text() {
            return new String(ch, start, length);
        }
    }
    private static final String COLUMN_NAME = "column-name";
    private static final String EJB_NAME = "ejb-name";
    private static final String ENTITY = "entity";
    private static final String FIELD_NAME = "field-name";
    private static final String DATASOURCE = "datasource";

    private static final String[] FK_FIELDS =
        {
            "Study", "patient_fk",
            "Series", "study_fk",
            "Instance", "series_fk",
            "Instance", "srcode_fk",
            "File", "instance_fk",
            "File", "media_fk",
        };
    private static final JdbcProperties instance = new JdbcProperties();
    private static final String JBOSSCMP_JDBC_XML =
        "META-INF/jbosscmp-jdbc.xml";
    private static final String TABLE_NAME = "table-name";
    
    public static JdbcProperties getInstance() {
        return instance;
    }
    
    public String[] getProperties(String[] keys, int length) {
        String[] values = new String[length];
        for (int i = 0; i < length; i++)
            values[i] = getProperty(keys[i]);
        return values;
    }

    public String[] getPk(String[] entity) {
        String[] values = new String[entity.length];
        for (int i = 0; i < values.length; i++)
            values[i] = getProperty(entity[i] + ".pk");
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
