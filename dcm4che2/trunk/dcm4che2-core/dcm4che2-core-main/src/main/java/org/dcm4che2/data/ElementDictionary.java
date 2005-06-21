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

public class ElementDictionary implements Serializable {

	private static final long serialVersionUID = 3834593209899757880L;

	private static final String USAGE = 
		"Create dictionary resource from XML source:\n" +
		"\n" +
		"  java ElementDictionary <xml-file> <resource-file>\n" +
		"\n" +
		"    Store serialized dictionary in <resource-file>.\n" +
		"\n" +
		"  java ElementDictionary <xml-file> <resource-name> <zip-file>\n" +
		"\n" +
		"    Create <zip-file> with serialized dictionary under <resource-name>\n" +
		"    and appendant META-INF/org.dcm4che2.data.ElementDictionary.";

	public static final String UNKOWN = "?";

	public static final String PRIVATE_CREATOR = "Private Creator Data Element";

	public static final String GROUP_LENGTH = "Group Length";

	private static final ElementDictionary EMPTY = new ElementDictionary();

	private static ElementDictionary stdDict;

	private static Hashtable privDicts;

	static {
		ElementDictionary.reloadDictionaries();
	}

	public static void main(String args[]) {
		if (args.length < 2) {
			System.out.println(USAGE);
			System.exit(1);
		}
		ElementDictionary dict = new ElementDictionary(2300);
		try {
			dict.loadXML(new File(args[0]));
			if (args.length > 2) {
				ResourceLocator.createResource(args[1], dict, new File(args[2]));
				System.out.println("Create Dictionary Resource  - " + args[2]);
			} else {
				ResourceLocator.serializeTo(dict, new File(args[1]));
				System.out.println("Serialize Dictionary to - " + args[1]);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void reloadDictionaries() {
		ElementDictionary newStdDict = null;
		Hashtable newPrivDicts = new Hashtable();
		List list = ResourceLocator.findResources(ElementDictionary.class);
		for (int i = 0, n = list.size(); i < n; ++i) {
			ElementDictionary d = (ElementDictionary) ResourceLocator
					.loadResource((String) list.get(i));
			if (d.getPrivateCreator() == null) {
				newStdDict = d;
			} else {
				newPrivDicts.put(d.getPrivateCreator(), d);
			}
		}
		ElementDictionary.stdDict = newStdDict;
		ElementDictionary.privDicts = newPrivDicts;
	}

	public final String getPrivateCreator() {
		return privateCreator;
	}

	public static ElementDictionary getDictionary() {
		return maskNull(stdDict);
	}

	public static ElementDictionary getPrivateDictionary(String creatorID) {
		return maskNull((ElementDictionary) privDicts.get(creatorID));
	}

	private static ElementDictionary maskNull(ElementDictionary dict) {
		return dict != null  ? dict : EMPTY;
	}

	private transient IntHashtable table;

	private transient String privateCreator;

	private ElementDictionary() {
	}

	private ElementDictionary(int initialCapacity) {
		this.table = new IntHashtable(initialCapacity);
	}

	private void writeObject(final ObjectOutputStream os) throws IOException {
		os.defaultWriteObject();
		os.writeObject(privateCreator);
		os.writeInt(table.size());
		try {
			table.accept(new IntHashtable.Visitor() {
				public void visit(int key, Object value) throws IOException {
					os.writeInt(key);
					os.writeUTF((String) value);
				}
			});
		} catch (Exception e) {
			throw (IOException) e;
		}
	}

	private void readObject(ObjectInputStream is) throws IOException,
			ClassNotFoundException {
		is.defaultReadObject();
		privateCreator = (String) is.readObject();
		int size = is.readInt();
		table = new IntHashtable(size);
		for (int i = 0, tag; i < size; ++i) {
			tag = is.readInt();
			table.put(tag, is.readUTF());
		}
	}

	public String nameOf(int tag) {
		if ((tag & 0x0000ffff) == 0)
			return GROUP_LENGTH;
		if ((tag & 0x00010000) != 0) { // Private Element
			if ((tag & 0x0000ff00) == 0)
				return PRIVATE_CREATOR;
			else
				tag &= 0xffff00ff;
		} else if ((tag & 0xffffff00) == 0x00203100)
			tag &= 0xffffff00; // (0020,31xx) Source Image Ids
		else {
			final int ggg00000 = tag & 0xffe00000;
			if (ggg00000 == 0x50000000 || ggg00000 == 0x60000000)
				tag &= 0xff00ffff; // (50xx,eeee), (60xx,eeee)
		}
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
		int tag = -1;

		StringBuffer name = new StringBuffer(80);

		public void characters(char[] ch, int start, int length)
				throws SAXException {
			if (tag != -1) {
				name.append(ch, start, length);
			}
		}

		public void startElement(String uri, String localName, String qName,
				Attributes attributes) throws SAXException {
			if ("element".equals(qName)) {
				tag = (int) Long.parseLong(attributes.getValue("tag").replace(
						'x', '0'), 16);
			} else if ("dictionary".equals(qName)) {
				privateCreator = attributes.getValue("creator");
			}
		}

		public void endElement(String uri, String localName, String qName)
				throws SAXException {
			if ("element".equals(qName)) {
				table.put(tag, name.toString());
				name.setLength(0);
				tag = -1;
			}
		}
	}

}
