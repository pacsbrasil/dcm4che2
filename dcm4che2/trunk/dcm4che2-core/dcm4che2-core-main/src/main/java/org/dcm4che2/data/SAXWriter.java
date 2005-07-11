package org.dcm4che2.data;

import java.util.Iterator;

import org.dcm4che2.util.StringUtils;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

public class SAXWriter {
    
    private static final int CBUF_LENGTH = 512;

    private ContentHandler handler;

    public SAXWriter(ContentHandler handler) {
        this.handler = handler;
    }
    
    public void write(AttributeSet attrs)
            throws SAXException {
        handler.startDocument();
        writeContent(attrs, new char[CBUF_LENGTH]);
        handler.endDocument();
    }

    private void writeContent(AttributeSet attrs, char[] cbuf)
            throws SAXException {
        AttributesImpl atts = new AttributesImpl();
        String qName = "dicom";
        if (!attrs.isRoot()) {
            qName = "item";
            atts.addAttribute("", "", "pos", "",
                    Integer.toString(attrs.getItemPosition()));
            atts.addAttribute("", "", "off", "",
                    Long.toString(attrs.getItemOffset()));
        }
        handler.startElement("", "", qName, atts);
        for (Iterator it = attrs.iterator(); it.hasNext();) {
            Attribute a = (Attribute) it.next();
            VR vr = a.vr();
            atts.clear();
            atts.addAttribute("", "", "tag", "", StringUtils.intToHex(a.tag()));
            atts.addAttribute("", "", "vr", "", vr.toString());
            atts.addAttribute("", "", "len", "", Integer.toString(a.length()));
            handler.startElement("", "", "attr", atts);
            if (a.hasItems()) {
                for (int i = 0, n = a.countItems(); i < n; ++i) {
                    if (vr == VR.SQ) {
                        writeContent(a.getItem(i), cbuf);
                    } else {
                        final byte[] bytes = a.getBytes(i);
                        atts.clear();
                        atts.addAttribute("", "", "pos", "", Integer
                                .toString(i + 1));
                        atts.addAttribute("", "", "len", "", Integer
                                .toString((bytes.length + 1) & ~1));
                        handler.startElement("", "", "item", atts);
                        vr.toContentHandler(bytes, a.bigEndian(), null, handler, cbuf);
                        handler.endElement("", "", "item");
                    }
                }
            } else {
                vr.toContentHandler(a.getBytes(), a.bigEndian(), attrs
                        .getSpecificCharacterSet(), handler, cbuf);
            }
            handler.endElement("", "", "attr");

        }
        handler.endElement("", "", qName);
    }

}
