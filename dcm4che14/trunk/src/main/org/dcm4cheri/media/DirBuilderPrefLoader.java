/*$Id$*/
/*****************************************************************************
 *                                                                           *
 *  Copyright (c) 2002 by TIANI MEDGRAPH AG                                  *
 *                                                                           *
 *  This file is part of dcm4che.                                            *
 *                                                                           *
 *  This library is free software; you can redistribute it and/or modify it  *
 *  under the terms of the GNU Lesser General Public License as published    *
 *  by the Free Software Foundation; either version 2 of the License, or     *
 *  (at your option) any later version.                                      *
 *                                                                           *
 *  This library is distributed in the hope that it will be useful, but      *
 *  WITHOUT ANY WARRANTY; without even the implied warranty of               *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU        *
 *  Lesser General Public License for more details.                          *
 *                                                                           *
 *  You should have received a copy of the GNU Lesser General Public         *
 *  License along with this library; if not, write to the Free Software      *
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA  *
 *                                                                           *
 *****************************************************************************/

package org.dcm4cheri.media;

import org.dcm4che.media.DirBuilderPref;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0.0
 */
final class DirBuilderPrefLoader extends DefaultHandler {

    private final HashMap map;
    private final ArrayList list = new ArrayList(16);
    private String type;
    
    /** Creates a new instance of DirBuilderPrefLoader */
    public DirBuilderPrefLoader(HashMap map) {
        this.map = map;
    }
    
    public void startElement (String uri, String localName, String qName,
            Attributes attr) throws SAXException
    {
        if ("record".equals(qName)) {
            type = attr.getValue("type");
            list.clear();
        } else if ("element".equals(qName)) {
            list.add(attr.getValue("tag"));
        }
    }

    public void endElement (String uri, String localName, String qName)
            throws SAXException
    {
        if ("record".equals(qName)) {
            int[] tags = new int[list.size()];
            for (int i = 0; i < tags.length; ++i) {
                String str = (String)list.get(i);
                try {
                    tags[i] = (Integer.parseInt(str.substring(1,5), 16) << 16)
                            | Integer.parseInt(str.substring(6,10), 16);
                } catch (Exception ex) {
                    throw new SAXException("tag=" + str);
                }
            }
            map.put(type, tags);
        }
    }

    public void parse(InputStream in) throws IOException {
        try {
            SAXParserFactory f = SAXParserFactory.newInstance();
            SAXParser p  = f.newSAXParser();
            p.parse(in, this);
        } catch (SAXException ex) {
            throw new IOException(ex.getMessage());
        } catch (ParserConfigurationException ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex.getMessage());
        }
    }

    public void parse(File file) throws IOException {
        try {
            SAXParserFactory f = SAXParserFactory.newInstance();
            SAXParser p  = f.newSAXParser();
            p.parse(file, this);
        } catch (SAXException ex) {
            throw new IOException(ex.getMessage());
        } catch (ParserConfigurationException ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex.getMessage());
        }
    }
}
