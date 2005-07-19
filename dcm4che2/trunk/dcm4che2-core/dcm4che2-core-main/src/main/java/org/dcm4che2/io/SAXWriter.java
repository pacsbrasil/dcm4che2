package org.dcm4che2.io;

import java.io.IOException;
import java.util.Iterator;

import org.dcm4che2.data.DicomElement;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.VR;
import org.dcm4che2.util.StringUtils;
import org.dcm4che2.util.TagUtils;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.AttributesImpl;

public class SAXWriter implements DicomInputHandler {
    
    private static final int CBUF_LENGTH = 512;
    private final char[] cbuf = new char[CBUF_LENGTH];
    private ContentHandler ch;
    private LexicalHandler lh;
    private boolean seenFirst = false;

    public SAXWriter(ContentHandler ch, LexicalHandler lh) {
        this.ch = ch;
        this.lh = lh;
    }
    
    public void write(DicomObject attrs)
            throws SAXException {
        ch.startDocument();
        writeContent(attrs, attrs.isRoot() ? "dicom" : "item");
        ch.endDocument();
    }

    private void writeContent(DicomObject attrs, String qName)
            throws SAXException {
        AttributesImpl atts = new AttributesImpl();
        if (!attrs.isRoot()) {
            atts.addAttribute("", "", "off", "",
                    Long.toString(attrs.getItemOffset()));
        }
        ch.startElement("", "", qName, atts);
        for (Iterator it = attrs.iterator(); it.hasNext();) {
            DicomElement a = (DicomElement) it.next();
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
                        writeContent(a.getItem(i), "item");
                    } else {
                        final byte[] bytes = a.getBytes(i);
                        atts.clear();
                        atts.addAttribute("", "", "len", "", Integer
                                .toString((bytes.length + 1) & ~1));
                        ch.startElement("", "", "item", atts);
                        vr.formatXMLValue(bytes, a.bigEndian(), null, cbuf, ch);
                        ch.endElement("", "", "item");
                    }
                }
            } else {
                vr.formatXMLValue(a.getBytes(), a.bigEndian(), attrs
                        .getSpecificCharacterSet(), cbuf, ch);
            }
            ch.endElement("", "", "attr");

        }
        ch.endElement("", "", qName);
    }    
    
    public boolean readValue(DicomInputStream in) throws IOException {
        AttributesImpl atts = new AttributesImpl();
        final int tag = in.tag();
        final VR vr = in.vr();
        final int vallen = in.valueLength();
        final boolean bigEndian = in.getTransferSyntax().bigEndian();
        try {
            switch (tag) {
            case Tag.Item:
                boolean isRoot = !seenFirst;
                if (isRoot) {
                    seenFirst = true;
                    ch.startDocument();
                }
                atts.addAttribute("", "", "off", "", Long.toString(in
                        .getStreamPosition() - 8));
                atts.addAttribute("", "", "len", "", Integer.toString(in
                        .valueLength()));
                ch.startElement("", "", "item", atts);
                in.readValue(in);
                DicomElement sq = in.sq();
                VR sqvr = sq.vr();
                if (sqvr != VR.SQ) {
                    sqvr.formatXMLValue(sq.removeBytes(0), bigEndian, null, cbuf, ch);
                }
                ch.endElement("", "", "item");
                if (isRoot)
                    ch.endDocument();
                break;
            case Tag.ItemDelimitationItem:
                in.readValue(in);
                if (in.level() == 0) {
                    ch.endElement("", "", "dicom");
                    ch.endDocument();
                }
                break;
            case Tag.SequenceDelimitationItem:
                in.readValue(in);
                break;
            default:
                if (!seenFirst) {
                    seenFirst = true;
                    ch.startDocument();
                    ch.startElement("", "", "dicom", atts);
                }
                final DicomObject attrs = in.getDicomObject();
                if (lh != null) {
                    String name = attrs.nameOf(tag);
                    lh.comment(name.toCharArray(), 0, name.length());
                }
                atts.addAttribute("", "", "tag", "", StringUtils.intToHex(tag));
                atts.addAttribute("", "", "vr", "", vr.toString());
                atts.addAttribute("", "", "len", "", Integer.toString(vallen));
                ch.startElement("", "", "attr", atts);
                if (vallen == -1 || vr == VR.SQ) {
                    in.readValue(in);
                    attrs.remove(tag);
                } else {
                    byte[] val = in.readBytes(vallen);
                    vr.formatXMLValue(val, bigEndian,
                            attrs.getSpecificCharacterSet(), cbuf, ch);
                    if (tag == Tag.SpecificCharacterSet
                            || TagUtils.isPrivateCreatorDataElement(tag)) {
                        attrs.putBytes(tag, vr, bigEndian, val);
                    }
                }
                ch.endElement("", "", "attr");
            }
        } catch (SAXException e) {
            throw (IOException) new IOException().initCause(e);
        }
        return true;
    }

}
