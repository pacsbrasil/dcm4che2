package org.dcm4chee.xero.wado;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.dcm4che2.data.DicomElement;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.SpecificCharacterSet;
import org.dcm4che2.data.Tag;
import org.dcm4chee.xero.util.StringUtil;

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
      assert ds != null;
      this.ds = ds;
      this.scs = ds.getSpecificCharacterSet();
   }

   /** Formats a DICOM name */
   public static String formatDicomName(String name) {
      if (name == null)
         return null;
      if (name.indexOf('=') >= 0) {
         String[] i18n = StringUtil.split(name, '=', true);
         StringBuffer ret = new StringBuffer();
         for (int i = 0; i < i18n.length; i++) {
            if (i18n[i].equals(""))
               continue;
            i18n[i] = formatDicomName(i18n[i]);
            if (ret.length() > 0)
               ret.append(", ");
            ret.append(i18n[i]);
         }
         return ret.toString();
      }
      String[] parts = StringUtil.split(name, '^', true);
      if (parts.length == 1)
         return parts[0];
      StringBuffer ret = new StringBuffer();
      if (parts.length >= 4)
         ret.append(parts[3]).append(" ");
      ret.append(parts[1]).append(" ");
      if (parts.length >= 3)
         ret.append(parts[2]).append(" ");
      ret.append(parts[0]).append(" ");
      if (parts.length >= 5)
         ret.append(parts[4]);
      return ret.toString();
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
      if (de == null)
         return null;
      return new DicomElementWrap(de, scs);
   }

   /**
    * This should only be used if all elements are actually going to be iterated
    * over - it returns a new list every time.
    */
   @Override
   public Set<java.util.Map.Entry<String, DicomElementWrap>> entrySet() {
      ListEntrySet les = new ListEntrySet();
      Iterator<DicomElement> it = ds.datasetIterator();
      while (it.hasNext()) {
         DicomElement de = (DicomElement) it.next();
         les.add(new SimpleEntry<String, DicomElementWrap>("0x" + Integer.toHexString(de.tag()), new DicomElementWrap(de, scs)));
      }
      return les;
   }

   /** A simple entry set implemented as a list. */
   static class ListEntrySet extends ArrayList<Map.Entry<String, DicomElementWrap>> implements
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
      if (ret.length() < 8)
         ret = "00000000".substring(ret.length()) + ret;
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

   /** Returns the simple value of the object */
   public Object getValue() {
      int vm = getVm();
      if (vm == 0)
         return null;
      if (vm == 1) {
         String val = de.getString(scs, false);
         val = val.trim();
         if (val.equals(""))
            return null;
         // If you want it to match WADO 1, then the following line could be
         // included.
         // if( val!=null && de.vr()==VR.CS && (val.length() % 2)==1) return
         // val+" ";
         return val;
      }
      List<String> ret = Arrays.asList(de.getStrings(scs, false));
      return ret;
   }

   /** Returns the patient name value of the object as a formatted string */
   public String getName() {
      String ret = (String) getValue();
      if (ret == null)
         return null;
      return DicomObjectMap.formatDicomName(ret);
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
      for (int i = 0; i < len; i++) {
         DicomObject childDs = de.getDicomObject(i);
         ret.add(new DicomObjectMap(childDs));
      }
      return ret;
   }

   public String toString() {
      Object v = getValue();
      if (v == null)
         return "";
      return v.toString();
   }
}

// This class is not needed anymore when using jdk/jre 1.6
class SimpleEntry<K, V> implements Entry<K, V>, java.io.Serializable {
   private static final long serialVersionUID = -8499721149061103585L;

   private final K key;
   private V value;

   /**
    * Creates an entry representing a mapping from the specified key to the
    * specified value.
    * 
    * @param key
    *           the key represented by this entry
    * @param value
    *           the value represented by this entry
    */
   public SimpleEntry(K key, V value) {
      this.key = key;
      this.value = value;
   }

   /**
    * Creates an entry representing the same mapping as the specified entry.
    * 
    * @param entry
    *           the entry to copy
    */
   public SimpleEntry(Entry<? extends K, ? extends V> entry) {
      this.key = entry.getKey();
      this.value = entry.getValue();
   }

   public K getKey() {
      return key;
   }

   public V getValue() {
      return value;
   }

   public V setValue(V value) {
      V oldValue = this.value;
      this.value = value;
      return oldValue;
   }
}
