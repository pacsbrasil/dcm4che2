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

import org.dcm4che.imageio.plugins.DcmMetadata;
import org.dcm4che.data.DcmObjectFactory;
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
import org.xml.sax.ContentHandler;
import org.xml.sax.helpers.DefaultHandler;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.TransformerHandler;

/**
 * Change history:<br>
 * 2002.06.13, Thomas Hacklaender: Method setDataset(Dataset ds) added.<br>
 * 2002.06.16, Thomas Hacklaender: Methods reset(), setFromTree(String formatName, 
 * Node root) and mergeTree(String formatName, Node root) added. Method 
 * isReadOnly() modified.<br>
 * <br>
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0.0
 */
final class DcmMetadataImpl extends DcmMetadata {

    static final DcmImageReaderConf conf = DcmImageReaderConf.getInstance();
    
    // 2002.06.13, Thomas Hacklaender: Modifier "final" removed.
    // private final Dataset ds;
    private Dataset ds;
    
    private TagDictionary dict = 
            DictionaryFactory.getInstance().getDefaultTagDictionary();
            
    /** Creates a new instance of DcmMetadata */
    public DcmMetadataImpl(Dataset ds) {
        super(false,
            conf.getExtraStreamMetadataFormatNames(),
            conf.getExtraStreamMetadataFormatClassNames());
        this.ds = ds;
    }
    
    public final Dataset getDataset() {
        return ds;
    }
    
    /**
     * Sets a new Dataset as a base for the metadata.
     * @param ds the new Dataset.
     * @author Thomas Hacklaender
     * @version 2002.06.13
     * @since 1.0.0
     */
    public final void setDataset(Dataset ds) {
        this.ds = ds;
    }

    public final void setDictionary(TagDictionary dict) {
        this.dict = dict;
    }
    
    /**
     * This object supports the mergeTree, setFromTree, and reset methods.
     * @return allways false.
     * @author Thomas Hacklaender
     * @version 2002.06.16
     * @since 1.0.0
     */
    public final boolean isReadOnly() {
        return false;
    }
    
    public Node getAsTree(String formatName) {
        if (formatName.equals(nativeMetadataFormatName)) {
            return getTree(formatName, null, null);
        } else if (formatName.equals
                   (IIOMetadataFormatImpl.standardMetadataFormatName)) {
            throw new IllegalArgumentException(
                    IIOMetadataFormatImpl.standardMetadataFormatName
                    + " not supported!");
        } else if (conf.contains(formatName)) {
            return getTree(formatName, conf.getFilterDataset(formatName),
                    conf.getTransformerHandler(formatName));
        } else {
            throw new IllegalArgumentException("Not a recognized format: "
                    + formatName);
        }
    }
    
    private Node getTree(String formatName,
            Dataset filter, TransformerHandler th) {
        final IIOMetadataNode root = new IIOMetadataNode(formatName);
        
        ContentHandler ch = new DefaultHandler() {
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
            if (th != null) {
                th.setResult(new SAXResult(ch));
                ch = th;
            }
            ds.subset(filter).writeFile(ch, dict);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException("Exception in getTree", ex);
        }
        return root;
    }
    
    /**
     * Alters the internal state of this IIOMetadata object from a tree of 
     * XML DOM Nodes whose syntax is defined by the given metadata format. The 
     * previous state is altered only as necessary to accomodate the nodes that 
     * are present in the given tree. If the tree structure or contents are 
     * invalid, an IIOInvalidTreeException will be thrown.
     * @param formatName the desired metadata format.
     * @param root an XML DOM Node object forming the root of a tree.
     * @throws IllegalStateException if this object is read-only.
     * @throws IllegalArgumentException if formatName is null or is not one of 
     *         the names returned by getMetadataFormatNames.
     * @throws IIOInvalidTreeException if the tree cannot be parsed successfully 
     *         using the rules of the given format.
     * @author Thomas Hacklaender
     * @version 2002.06.16
     * @since 1.0.0
     */
    public void mergeTree(String formatName, Node root) {
        throw new IllegalStateException("Metadata is read-only!");
    }
    
    /**
     * Resets all the data stored in this object to default values to the state 
     * this object was in immediately after construction.
     * @author Thomas Hacklaender
     * @version 2002.06.16
     * @since 1.0.0
     */
    public void reset() {
        ds = DcmObjectFactory.getInstance().newDataset();
    }
    
    /**
     * Sets the internal state of this IIOMetadata object from a tree of XML 
     * DOM Nodes whose syntax is defined by the given metadata format. The 
     * previous state is discarded. If the tree's structure or contents are 
     * invalid, an IIOInvalidTreeException will be thrown.
     * @param formatName the desired metadata format.
     * @param root an XML DOM Node object forming the root of a tree.
     * @throws IllegalStateException if this object is read-only.
     * @throws IllegalArgumentException if formatName is null or is not one of 
     *         the names returned by getMetadataFormatNames.
     * @throws IIOInvalidTreeException if the tree cannot be parsed successfully 
     *         using the rules of the given format.
     * @author Thomas Hacklaender
     * @version 2002.06.16
     * @since 1.0.0
     */
    public void setFromTree(String formatName, Node root) {
        reset();
        mergeTree(formatName, root);
    }

}
