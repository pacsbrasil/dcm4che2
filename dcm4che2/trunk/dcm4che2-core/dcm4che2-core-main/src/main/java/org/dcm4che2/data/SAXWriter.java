package org.dcm4che2.data;

import java.util.Iterator;

import org.dcm4che2.util.StringUtils;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.AttributesImpl;

public class SAXWriter {
    
    private static final int CBUF_LENGTH = 512;
    private final char[] cbuf = new char[CBUF_LENGTH];
    private ContentHandler ch;
    private LexicalHandler lh;

    public SAXWriter(ContentHandler ch, LexicalHandler lh) {
        this.ch = ch;
        this.lh = lh;
    }
    
    public void write(AttributeSet attrs)
            throws SAXException {
        ch.startDocument();
        writeContent(attrs);
        ch.endDocument();
    }

    private void writeContent(AttributeSet attrs)
            throws SAXException {
        AttributesImpl atts = new AttributesImpl();
        String qName = "dicom";
        if (!attrs.isRoot()) {
            qName = "item";
            atts.addAttribute("", "", "off", "",
                    Long.toString(attrs.getItemOffset()));
        }
        ch.startElement("", "", qName, atts);
        for (Iterator it = attrs.iterator(); it.hasNext();) {
            Attribute a = (Attribute) it.next();
            VR vr = a.vr();
            final int tag = a.tag();
            if (lh != null) {
                String name = attrs.nameOf(tag);
                lh.comment(name.toCharArray(), 0, name.length());
            }
            atts.clear();
            atts.addAttribute("", "", "tag", "", StringUtils.intToHex(tag));
            atts.addAttribute("", "", "vr", "", vr.toString());
            atts.addAttribute("", "", "len", "", Integer.toString(a.length()));
            ch.startElement("", "", "attr", atts);
            if (a.hasItems()) {
                for (int i = 0, n = a.countItems(); i < n; ++i) {
                    if (vr == VR.SQ) {
                        writeContent(a.getItem(i));
                    } else {
                        final byte[] bytes = a.getBytes(i);
                        atts.clear();
                        atts.addAttribute("", "", "len", "", Integer
                                .toString((bytes.length + 1) & ~1));
                        ch.startElement("", "", "item", atts);
                        vr.formatXMLValue(bytes, a.bigEndian(), null, ch, cbuf);
                        ch.endElement("", "", "item");
                    }
                }
            } else {
                vr.formatXMLValue(a.getBytes(), a.bigEndian(), attrs
                        .getSpecificCharacterSet(), ch, cbuf);
            }
            ch.endElement("", "", "attr");

        }
        ch.endElement("", "", qName);
    }

}
