/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/

package org.dcm4che2.data;

import java.io.ByteArrayOutputStream;
import java.util.Stack;

import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * @author gunter zeilinger(gunterze@gmail.com)
 * @version $Reversion$ $Date$
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
    private AttributeSet attrs;
    private int tag;
    private VR vr;

    public ContentHandlerAdapter(AttributeSet attrs) {
        this.attrs = attrs;
    }

    public void startElement(String namespaceURI, String localName,
            String qName, Attributes atts) throws SAXException {
        if ("attr".equals(qName)) {
            onStartElement(atts.getValue("tag"), atts.getValue("vr"));
        } else if ("item".equals(qName)) {
            onStartItem(atts.getValue("off"));
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

    private void onStartElement(String tagStr, String vrStr) {
        if (state != EXPECT_ELM)
            throw new IllegalStateException("state:" + state);
        this.tag = (int) Long.parseLong(tagStr,16);
        this.vr = vrStr == null ? attrs.vrOf(tag)
                : VR.valueOf(vrStr.charAt(0) << 8 | vrStr.charAt(1));
        state = EXPECT_VAL_OR_FIRST_ITEM;
    }

    private void onStartItem(String offStr) {
        switch (state) {
        case EXPECT_VAL_OR_FIRST_ITEM:
            sqStack.push(vr == VR.SQ ? attrs.putSequence(tag) 
                    : attrs.putFragments(tag, vr, false));
        case EXPECT_NEXT_ITEM:
            Attribute sq = (Attribute) sqStack.peek();
            if (sq.vr() == VR.SQ) {
                AttributeSet parent = attrs;
                attrs = new BasicAttributeSet();
                attrs.setParent(parent);
                if (offStr != null) {
                    attrs.setItemOffset(Long.parseLong(offStr));
                }
                sq.addItem(attrs);
                state = EXPECT_ELM;
            } else {
                state = EXPECT_FRAG;
            }
            break;
        default:
            throw new IllegalStateException("state:" + state);
        }
    }
 
    private void onEndItem() {
        switch (state) {
        case EXPECT_ELM:
            attrs = attrs.getParent();
            break;
        case EXPECT_FRAG:
            Attribute sq = (Attribute) sqStack.peek();
            byte[] data =  sq.vr().parseXMLValue(sb, out, true, null);
            sq.addBytes(data);
            sb.setLength(0);
            out.reset();
            break;
        default:
            throw new IllegalStateException("state:" + state);
        }
        state = EXPECT_NEXT_ITEM;
    }

    private void onEndElement() {
        switch (state) {
        case EXPECT_VAL_OR_FIRST_ITEM:
            if (vr == VR.SQ) {
                attrs.putNull(tag, VR.SQ);
            } else {
                byte[] value = vr.parseXMLValue(sb, out, true, 
                        attrs.getSpecificCharacterSet());
                attrs.putBytes(tag, vr, false, value);
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
    }

    public void processingInstruction(String target, String data)
            throws SAXException {
    }

    public void startPrefixMapping(String prefix, String uri)
            throws SAXException {
    }
}
