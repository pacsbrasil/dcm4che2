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
package org.dcm4chex.archive.ejb.conf;

import java.util.ArrayList;
import java.util.Properties;

import javax.xml.parsers.SAXParserFactory;

import org.dcm4che.data.Dataset;
import org.dcm4che.dict.Tags;
import org.dcm4cheri.util.StringUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * 
 * @author gunter.zeilinger@tiani.com
 * @version $Revision$ $Date$
 * @since 28.12.2003
 */
public class AttributeCoercions {

    private ArrayList list = new ArrayList();
    private static class Entry {
        CoercionCondition condition;
        AttributeCoercion coercion;
    }

    private class MyHandler extends DefaultHandler {
        private CoercionCondition cc;
        private ArrayList fromTo;
        private Dataset ds;
        private Properties props;

        public void startElement(
            String uri,
            String localName,
            String qName,
            Attributes attributes)
            throws SAXException {
            if (qName.equals("if")) {
                String clazz = attributes.getValue("class");
                cc =
                    (clazz == null)
                        ? new CoercionCondition()
                        : (CoercionCondition) newInstance(clazz);
                fromTo = null;
                ds = cc.condition;
                props = cc.props;
            } else if (qName.equals("then")) {
                Entry entry = new Entry();
                entry.condition = cc;
                String clazz = attributes.getValue("class");
                entry.coercion =
                    (clazz == null)
                        ? new AttributeCoercion()
                        : (AttributeCoercion) newInstance(clazz);
                ds = entry.coercion.values;
                props = entry.coercion.props;
                list.add(entry);
            } else if (qName.equals("attr")) {
                int tag = Tags.valueOf(attributes.getValue("tag"));
                String value = attributes.getValue("value");
                if (value != null) {
                    ds.putXX(tag, StringUtils.split(value, '\\'));
                } else if (fromTo != null) {
                    fromTo.add(
                        new int[] {
                            Tags.valueOf(attributes.getValue("from")),
                            tag });
                }
            } else if (qName.equals("property")) {
                props.setProperty(
                    attributes.getValue("name"),
                    attributes.getValue("value"));
            }
        }
    }

    private static Object newInstance(String clazz) throws SAXException {
        try {
            return Class.forName(clazz).newInstance();
        } catch (Exception e) {
            throw new SAXException(clazz, e);
        }
    }

    public AttributeCoercions(String uri) throws ConfigurationException {
        try {
            SAXParserFactory.newInstance().newSAXParser().parse(
                uri,
                new MyHandler());
        } catch (Exception e) {
            throw new ConfigurationException(
                "Failed to load attribute coercions from " + uri,
                e);
        }
    }

    public void coerce(
        String callingAET,
        String calledAET,
        Dataset ds,
        Dataset coercedElements) {
        for (int i = 0, n = list.size(); i < n; ++i) {
            Entry entry = (Entry) list.get(i);
            if (entry.condition.match(callingAET, calledAET, ds)) {
                entry.coercion.coerce(ds, coercedElements);
            }
        }
    }

}
