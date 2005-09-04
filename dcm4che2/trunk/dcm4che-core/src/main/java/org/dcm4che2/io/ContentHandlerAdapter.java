/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/

package org.dcm4che2.io;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Stack;

import org.dcm4che2.data.BasicDicomObject;
import org.dcm4che2.data.DicomElement;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.SpecificCharacterSet;
import org.dcm4che2.data.VR;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * @author gunter zeilinger(gunterze@gmail.com)
 * @version $Revision$ $Date$
 * @since Jul 12, 2005
 */
public class ContentHandlerAdapter extends DefaultHandler {

    private static final int EXPECT_ELM = 0;
    private static final int EXPECT_VAL_OR_FIRST_ITEM = 1;
    private static final int EXPECT_FRAG = 2;
    private static final int EXPECT_NEXT_ITEM = 3;
    private int state = EXPECT_ELM;
    private final ByteArrayOutputStream out = new ByteArrayOutputStream();
    private final StringBuffer sb = new StringBuffer();    
    private final Stack sqStack = new Stack();
    private DicomObject attrs;
    private int tag;
    private VR vr;
    private String src;
    private Locator locator;
    private static final byte[] EMPTY_VALUE = {};

    public ContentHandlerAdapter(DicomObject attrs) {
        this.attrs = attrs;
    }

    public void startElement(String namespaceURI, String localName,
            String qName, Attributes atts) throws SAXException {
        if ("attr".equals(qName)) {
            onStartElement(atts.getValue("tag"), atts.getValue("vr"),
                    atts.getValue("src"));
        } else if ("item".equals(qName)) {
            onStartItem(atts.getValue("off"), atts.getValue("src"));
        }
    }

    public void endElement(String namespaceURI, String localName, String qName)
            throws SAXException {
        if ("attr".equals(qName)) {
            onEndElement();
        } else if ("item".equals(qName)) {
            onEndItem();
        }
    }

    public void characters(char[] ch, int start, int length)
            throws SAXException {
        switch (state) {
        case EXPECT_VAL_OR_FIRST_ITEM:
            if (vr == VR.SQ) return;
        case EXPECT_FRAG:
            sb.append(ch, start, length);
            vr.parseXMLValue(sb, out, false, attrs.getSpecificCharacterSet());
            break;
        }
    }

    private void onStartElement(String tagStr, String vrStr, String src) {
        if (state != EXPECT_ELM)
            throw new IllegalStateException("state:" + state);
        this.tag = (int) Long.parseLong(tagStr,16);
        this.vr = vrStr == null ? attrs.vrOf(tag)
                : VR.valueOf(vrStr.charAt(0) << 8 | vrStr.charAt(1));
        state = EXPECT_VAL_OR_FIRST_ITEM;
        this.src = src;
    }

    private void onStartItem(String offStr, String src) {
        this.src = src;
        switch (state) {
        case EXPECT_VAL_OR_FIRST_ITEM:
            sqStack.push(vr == VR.SQ ? attrs.putSequence(tag) 
                    : attrs.putFragments(tag, vr, false));
        case EXPECT_NEXT_ITEM:
            DicomElement sq = (DicomElement) sqStack.peek();
            if (sq.vr() == VR.SQ) {
                DicomObject parent = attrs;
                attrs = new BasicDicomObject();
                attrs.setParent(parent);
                if (offStr != null) {
                    attrs.setItemOffset(Long.parseLong(offStr));
                }
                sq.addDicomObject(attrs);
                state = EXPECT_ELM;
            } else {
                state = EXPECT_FRAG;
            }
            break;
        default:
            throw new IllegalStateException("state:" + state);
        }
    }
 
    private void onEndItem() throws SAXException {
        switch (state) {
        case EXPECT_ELM:
            attrs = attrs.getParent();
            break;
        case EXPECT_FRAG:
            DicomElement sq = (DicomElement) sqStack.peek();
            byte[] data =  getValue(sq.vr(), null);
            sq.addBytes(data);
            sb.setLength(0);
            out.reset();
            break;
        default:
            throw new IllegalStateException("state:" + state);
        }
        state = EXPECT_NEXT_ITEM;
    }

    private void onEndElement() throws SAXException {
        switch (state) {
        case EXPECT_VAL_OR_FIRST_ITEM:
            if (vr == VR.SQ) {
                attrs.putNull(tag, VR.SQ);
            } else {
                attrs.putBytes(tag, vr, false, 
                        getValue(vr, attrs.getSpecificCharacterSet()));
                sb.setLength(0);
                out.reset();
            }
            break;
        case EXPECT_NEXT_ITEM:
            sqStack.pop();
            break;
        default:
            throw new IllegalStateException("state:" + state);
        }
        state = EXPECT_ELM;
    }

    private byte[] getValue(VR vr, SpecificCharacterSet cs)
            throws SAXException {
        if (src == null)
            return vr.parseXMLValue(sb, out, true, cs);
        if (src.length() == 0)
            return EMPTY_VALUE;
        return readFromSrc();
    }    
    
    private byte[] readFromSrc() throws SAXException {
        URL url;
        try {
            url = new URL(src);
        } catch (MalformedURLException e) {
            String systemId = locator.getSystemId();
            if (systemId == null) {
                throw new SAXException("Missing systemId which is needed " +
                        "for resolving relative src: " + src);
            }
            try {
                url = new URL(
                        systemId.substring(0, systemId.lastIndexOf('/')+1) + src);
            } catch (MalformedURLException e1) {
                throw new SAXException(
                        "Invalid reference to external value src: " + src);
            }
        }
        try {
            URLConnection con = url.openConnection();
            DataInputStream in = new DataInputStream(con.getInputStream());
            try {
                int len = (int) con.getContentLength();
                byte[] data = new byte[(len + 1) & ~1];
                in.readFully(data, 0, len);
                return data;
            } finally {
                in.close();
            }
        } catch (IOException e) {
            throw new SAXException(
                    "Failed to read value from external src: " + url, e);
        }
    }

    public void endDocument() throws SAXException {
    }

    public void startDocument() throws SAXException {
    }

    public void ignorableWhitespace(char[] ch, int start, int length)
            throws SAXException {
    }

    public void endPrefixMapping(String prefix) throws SAXException {
    }

    public void skippedEntity(String name) throws SAXException {
    }

    public void setDocumentLocator(Locator locator) {
        this.locator = locator;
    }

    public void processingInstruction(String target, String data)
            throws SAXException {
    }

    public void startPrefixMapping(String prefix, String uri)
            throws SAXException {
    }
}
