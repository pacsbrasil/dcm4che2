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

package org.dcm4cheri.imageio.plugins;

import org.dcm4cheri.util.StringUtils;

import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmElement;
import org.dcm4che.data.DcmValueException;
import org.dcm4che.dict.DictionaryFactory;
import org.dcm4che.dict.TagDictionary;
import org.dcm4che.dict.Tags;

import java.nio.ByteBuffer;
import java.util.Arrays;
import javax.imageio.metadata.IIOMetadataFormatImpl;
import javax.imageio.metadata.IIOMetadataNode;

import org.w3c.dom.Node;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0.0
 */
final class DcmMetadataImpl extends org.dcm4che.imageio.plugins.DcmMetadata {

    private final Dataset ds;
    private TagDictionary dict = 
            DictionaryFactory.getInstance().getDefaultTagDictionary();
            
    /** Creates a new instance of DcmMetadata */
    public DcmMetadataImpl(Dataset ds) {
        super(false, null, null);
        this.ds = ds;
    }
    
    public final Dataset getDataset() {
        return ds;
    }

    public final void setDictionary(TagDictionary dict) {
        this.dict = dict;
    }

    public final boolean isReadOnly() {
        return true;
    }
    
    public Node getAsTree(String formatName) {
        if (formatName.equals(nativeMetadataFormatName)) {
            return getNativeTree();
        } else if (formatName.equals
                   (IIOMetadataFormatImpl.standardMetadataFormatName)) {
            throw new IllegalArgumentException(
                    IIOMetadataFormatImpl.standardMetadataFormatName
                    + " not supported!");
        } else {
            throw new IllegalArgumentException("Not a recognized format!");
        }
    }
    
    private Node getNativeTree() {
        final IIOMetadataNode root =
                new IIOMetadataNode(nativeMetadataFormatName);
        
        DefaultHandler ch = new DefaultHandler() {
            Node curNode = root;
            public void startElement (String uri, String localName,
                                      String qName, Attributes attr) {
                if ("dicomfile".equals(qName)) {
                    return;
                }
                IIOMetadataNode newNode = new IIOMetadataNode(qName);
                for (int i = 0, n = attr.getLength(); i < n; ++i) {
                    String attrName = attr.getQName(i);
                    if (!"pos".equals(attrName)) {
                        newNode.setAttribute(attrName, attr.getValue(i));
                    }
                }
                curNode.appendChild(newNode);
                curNode = newNode;
            }

            public void endElement (String uri, String localName, String qName)
            {
                if ("dicomfile".equals(qName)) {
                    return;
                }
                curNode = curNode.getParentNode();
            }
        };
               	
        try {
            ds.writeFile(ch, dict);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException("Exception in getNativeTree", ex);
        }
        return root;
    }
    
    public void setFromTree(String formatName, Node root) {
        throw new IllegalStateException("Metadata is read-only!");
    }

    public void mergeTree(String formatName, Node root) {
        throw new IllegalStateException("Metadata is read-only!");
    }

    public void reset() {
        throw new IllegalStateException("Metadata is read-only!");
    }
}
