package org.dcm4chee.xero.wado;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.dcm4che2.data.DicomElement;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.SpecificCharacterSet;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.VR;

/**
 * This class provides a map view onto a DicomObject instance - for use by
 * Stringtemplate or other places where a simple map is easier to use than a
 * full DicomObject.
 * 
 * @author bwallace
 */
public class DicomObjectMap extends AbstractMap<String, DicomElementWrap> {

	DicomObject ds;
	SpecificCharacterSet scs;

	public DicomObjectMap(DicomObject ds) {
		assert ds!=null;
		this.ds = ds;
		this.scs = ds.getSpecificCharacterSet();
	}

	@Override
	public boolean containsKey(Object key) {
		DicomElementWrap de = this.get(key);
		return de != null;
	}

	/** Gets the dicom element information for the given key object. */
	@Override
	public DicomElementWrap get(Object key) {
		String skey = (String) key;
		int ikey;
		try {
			if (skey.startsWith("0x")) {
				ikey = Integer.parseInt(skey.substring(2), 16);
			} else {
				ikey = Tag.forName(skey);
			}
		} catch (Exception e) {
			return null;
		}
		if (ikey <= 0)
			return null;
		DicomElement de = ds.get(ikey);
		if( de==null ) return null;
		return new DicomElementWrap(de,scs);
	}

	/**
	 * This should only be used if all elements are actually going to be iterated over - it returns a new
	 * list every time.
	 */
	@Override
	public Set<java.util.Map.Entry<String, DicomElementWrap>> entrySet() {
		ListEntrySet les = new ListEntrySet();
		Iterator<DicomElement> it = ds.datasetIterator();
		while(it.hasNext() ) {
			DicomElement de = (DicomElement) it.next();
			les.add( new AbstractMap.SimpleEntry<String, DicomElementWrap>("0x"+Integer.toHexString(de.tag()),new DicomElementWrap(de,scs)) );
		}
		return les;
	}

	/** A simple entry set implemented as a list. */
	static class ListEntrySet extends
			ArrayList<Map.Entry<String, DicomElementWrap>> implements
			Set<Map.Entry<String, DicomElementWrap>> {

		/**
		 * Ignore this.
		 */
		private static final long serialVersionUID = 3030948695030150661L;
	}
	
}

/** A simple wrapper on a dicom element */
class DicomElementWrap {
	DicomElement de;
	SpecificCharacterSet scs;
	
	public DicomElementWrap(DicomElement de, SpecificCharacterSet scs) {
		this.de = de;
		this.scs = scs;
	}
	
	public String getTag() {
		String ret = Integer.toHexString(de.tag()).toUpperCase();
		if(ret.length() < 8 ) ret = "00000000".substring(ret.length())+ret;
		return ret;
	}
	
	public String getVr() {
		return de.vr().toString();
	}

	public int getPos() {
		return -1;
	}
	
	public int getVm() {
		return de.vm(scs);
	}
	
	public int getLen() {
		return de.length();
	}
	
	public String getValue() {
		String val = de.getString(scs, false);
		// If you want it to match WADO 1, then the following line could be included.
		//if( val!=null && de.vr()==VR.CS && (val.length() % 2)==1) return val+" ";
		return val; 
	}
	
	public boolean getHasDicomObjects() {
		return de.hasDicomObjects();
	}
	
	public boolean getIsEmpty() {
		return de.isEmpty();
	}
	
	public List<DicomObjectMap> getDicomObjects() {
		int len = de.countItems();
		List<DicomObjectMap> ret = new ArrayList<DicomObjectMap>(len);
		for(int i=0; i<len; i++) {
			DicomObject childDs = de.getDicomObject(i);
			ret.add(new DicomObjectMap(childDs));
		}
		return ret;
	}
	
	public String toString() {
		return getValue();
	}
}
