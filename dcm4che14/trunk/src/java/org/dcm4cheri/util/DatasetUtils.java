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

package org.dcm4cheri.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmDecodeParam;
import org.dcm4che.data.DcmEncodeParam;
import org.dcm4che.data.DcmObjectFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * @author Gunter.Zeilinger@tiani.com
 * @version $Revision$
 * @since 05.11.2003
 */
public class DatasetUtils {

    private static final DcmObjectFactory dof = DcmObjectFactory.getInstance();

    public static Dataset fromByteArray(
        byte[] data,
        DcmDecodeParam decodeParam) {
        ByteArrayInputStream bin = new ByteArrayInputStream(data);
        Dataset ds = dof.newDataset();
        try {
            ds.readDataset(bin, decodeParam, -1);
        } catch (IOException e) {
            throw new IllegalArgumentException("" + e);
        }
        return ds;
    }

    public static byte[] toByteArray(Dataset ds, DcmEncodeParam encodeParam) {
        ByteArrayOutputStream bos =
            new ByteArrayOutputStream(ds.calcLength(encodeParam));
        try {
            ds.writeDataset(bos, encodeParam);
        } catch (IOException e) {
            throw new IllegalArgumentException("" + e);
        }
        return bos.toByteArray();
    }

    private static SAXParser getSAXParser() {
        try {
            return SAXParserFactory.newInstance().newSAXParser();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Dataset fromXML(InputSource is)
        throws SAXException, IOException {
        Dataset ds = dof.newDataset();
        getSAXParser().parse(is, new XML2DatasetHandler(ds));
        return ds;
    }

    public static Dataset fromXML(InputStream is)
        throws SAXException, IOException {
        return fromXML(new InputSource(is));
    }

    public static Dataset fromXML(Reader r) throws SAXException, IOException {
        return fromXML(new InputSource(r));
    }

    public static Dataset fromXML(String s) throws SAXException, IOException {
        return fromXML(new StringReader(s));
    }

    private DatasetUtils() {} // no instance
}
