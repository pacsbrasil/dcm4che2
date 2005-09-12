/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/

package org.dcm4che2.data;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Hashtable;
import java.util.List;

import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.dcm4che2.util.IntHashtable;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class UIDDictionary implements Serializable {

    private static final long serialVersionUID = 3258135738985296181L;

    private static final String RESOURCE_NAME = 
            "org/dcm4che2/data/UIDDictionary.ser";

    private static final String USAGE = 
        "Usage: mkuiddic <xml-file> <resource-file>\n" +
		"         (Store serialized dictionary in <resource-file>).\n";

	public static final String UNKOWN = "?";

	private static UIDDictionary inst;

	public static void main(String args[]) {
		if (args.length < 2) {
			System.out.println(USAGE);
			System.exit(1);
		}
		UIDDictionary dict = new UIDDictionary(250);
		try {
			dict.loadXML(new File(args[0]));
			ResourceLocator.serializeTo(dict, new File(args[1]));
			System.out.println("Serialize Dictionary to - " + args[1]);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void loadDictionary() {
        UIDDictionary.inst = (UIDDictionary) ResourceLocator
				.loadResource(RESOURCE_NAME);
	}
	
	public static UIDDictionary getDictionary() {
        if (inst == null)
            loadDictionary();
		return inst;
	}
    
	private Hashtable table;

    private UIDDictionary() {
        this(11);
    }
    
	private UIDDictionary(int initialCapacity) {
		this.table = new Hashtable(initialCapacity);
	}

	public String nameOf(String tag) {
		if (table == null)
			return UNKOWN;
		String name = (String) table.get(tag);
		return name != null ? name : UNKOWN;
	}

	public void loadXML(File f) throws IOException, SAXException {
		try {
			SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
			parser.parse(f, new SAXAdapter());
		} catch (ParserConfigurationException e) {
			throw new RuntimeException(e);
		} catch (FactoryConfigurationError e) {
			throw new RuntimeException(e);
		}
	}

	private final class SAXAdapter extends DefaultHandler {
		String uid;

		StringBuffer name = new StringBuffer(80);

		public void characters(char[] ch, int start, int length)
				throws SAXException {
			if (uid != null) {
				name.append(ch, start, length);
			}
		}

		public void startElement(String uri, String localName, String qName,
				Attributes attributes) throws SAXException {
			if ("uid".equals(qName)) {
				uid = attributes.getValue("value");
			}
		}

		public void endElement(String uri, String localName, String qName)
				throws SAXException {
			if ("uid".equals(qName)) {
				table.put(uid, name.toString());
				name.setLength(0);
				uid = null;
			}
		}
	}

}
