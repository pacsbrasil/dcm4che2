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

public class VRMap implements Serializable {

	private static final long serialVersionUID = 6581801202183118918L;

	private static final String USAGE = 
		"Create VRMap resource from XML source:\n" +
		"\n" +
		"  java VRMap <xml-file> <resource-file>\n" +
		"\n" +
		"    Store serialized VRMap in <resource-file>.\n" +
		"\n" +
		"  java VRMap <xml-file> <resource-name> <zip-file>\n" +
		"\n" +
		"    Create <zip-file> with serialized VRMap under <resource-name>\n" +
		"    and appendant META-INF/org.dcm4che2.data.VRMap.";


	private static final VRMap DEFAULT = new VRMap();
	
	private static VRMap vrMap;

	private static Hashtable privVRMaps;

	static {
		VRMap.reloadVRMaps();
	}

	public static void main(String args[]) {
		if (args.length < 2) {
			System.out.println(USAGE);
			System.exit(1);
		}
		VRMap vrMap = new VRMap(2300);
		try {
			vrMap.loadXML(new File(args[0]));
			if (args.length > 2) {
				ResourceLocator.createResource(args[1], vrMap, new File(args[2]));
				System.out.println("Create VRMap Resource  - " + args[2]);
			} else {
				ResourceLocator.serializeTo(vrMap, new File(args[1]));
				System.out.println("Serialize VRMap to - " + args[1]);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void reloadVRMaps() {
		VRMap newVRMap = null;
		Hashtable newPrivVRMaps = new Hashtable();
		List list = ResourceLocator.findResources(VRMap.class);
		for (int i = 0, n = list.size(); i < n; ++i) {
			VRMap m = (VRMap) ResourceLocator.loadResource((String) list
					.get(i));
			if (m.getPrivateCreator() == null) {
				newVRMap = m;
			} else {
				newPrivVRMaps.put(m.getPrivateCreator(), m);
			}
		}
		VRMap.vrMap = newVRMap;
		VRMap.privVRMaps = newPrivVRMaps;
	}

	public static VRMap getVRMap() {
		return maskNull(vrMap);
	}

	public static VRMap getPrivateVRMap(String creatorID) {
		return maskNull((VRMap) privVRMaps.get(creatorID));
	}

	private static VRMap maskNull(VRMap vrMap) {
		return vrMap != null ? vrMap : DEFAULT;
	}

	private transient IntHashtable table;

	private transient String privateCreator;

	private VRMap() {		
	}
	
	private VRMap(int initialCapacity) {
		this.table = new IntHashtable(initialCapacity);
	}

	public final String getPrivateCreator() {
		return privateCreator;
	}

	private void writeObject(final ObjectOutputStream os) throws IOException {
		os.defaultWriteObject();
		os.writeObject(privateCreator);
		os.writeInt(table.size());
		try {
			table.accept(new IntHashtable.Visitor() {
				public boolean visit(int key, Object value) {
					try {
						os.writeInt(key);
						os.writeShort(((VR) value).code);
						return true;
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
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
		for (int i = 0, tag, code; i < size; ++i) {
			tag = is.readInt();
			code = is.readUnsignedShort();
			table.put(tag, VR.valueOf(code));
		}
	}

	public VR vrOf(int tag) {
		if ((tag & 0x0000ffff) == 0) // Group Length
			return VR.UL;
		if ((tag & 0xffff0000) == 0) // Command Element
			return vrOfCommand(tag);
		if ((tag & 0x00010000) != 0) { // Private Element
			if ((tag & 0x0000ff00) == 0)
				return VR.LO; // Private Creator
			else
				tag &= 0xffff00ff;
		} else {
			final int ggg00000 = tag & 0xffe00000;
			if (ggg00000 == 0x50000000 || ggg00000 == 0x60000000)
				tag &= 0xff00ffff; // (50xx,eeee), (60xx,eeee)
		}
		if (table == null)
			return VR.UN;
		VR vr = (VR) table.get(tag);
		return vr != null ? vr : VR.UN;
	}

	private VR vrOfCommand(int tag) {
		switch (tag) {
		case 0x00000600: // MoveDestination
		case 0x00001030: // MoveOriginatorAET
			return VR.AE;
		case 0x00000901: // OffendingElement:
		case 0x00001005: // AttributeIdentifierList:
			return VR.AT;
		case 0x00000902: // ErrorComment:
			return VR.LO;
		case 0x00000002: // AffectedSOPClassUID:
		case 0x00000003: // RequestedSOPClassUID:
		case 0x00001000: // AffectedSOPInstanceUID:
		case 0x00001001: // RequestedSOPInstanceUID:
			return VR.UI;
		case 0x00000100: // CommandField:
		case 0x00000110: // MessageID:
		case 0x00000120: // MessageIDToBeingRespondedTo:
		case 0x00000700: // Priority:
		case 0x00000800: // DataSetType:
		case 0x00000900: // Status:
		case 0x00000903: // ErrorID:
		case 0x00001002: // EventTypeID:
		case 0x00001008: // ActionTypeID:
		case 0x00001020: // NumberOfRemainingSubOperations:
		case 0x00001021: // NumberOfCompletedSubOperations:
		case 0x00001022: // NumberOfFailedSubOperations:
		case 0x00001023: // NumberOfWarningSubOperations:
		case 0x00001031: // MoveOriginatorMessageID:
			return VR.US;
		}
		return VR.UN;
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

		public void startElement(String uri, String localName, String qName,
				Attributes attributes) throws SAXException {
			if ("element".equals(qName)) {
				int tag = (int) Long.parseLong(attributes.getValue("tag")
						.replace('x', '0'), 16);
				String vrstr = attributes.getValue("vr");
				if ((tag & 0x0000ffff) != 0 && (tag & 0xffff0000) != 0
						&& vrstr != null && vrstr.length() != 0) {
					VR vr = VR.valueOf(vrstr.charAt(0) << 8 | vrstr.charAt(1));
					table.put(tag, vr);
				}
			} else if ("dictionary".equals(qName)) {
				privateCreator = attributes.getValue("creator");
			}
		}
	}
}
