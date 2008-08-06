/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is part of dcm4che, an implementation of DICOM(TM) in
 * Java(TM), hosted at http://sourceforge.net/projects/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * Gunter Zeilinger, Huetteldorferstr. 24/10, 1150 Vienna/Austria/Europe.
 * Portions created by the Initial Developer are Copyright (C) 2008
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * See listed authors below.
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 *
 * ***** END LICENSE BLOCK ***** */
package org.dcm4che2.data;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Date;
import java.util.Iterator;
import java.util.regex.Pattern;

import org.dcm4che2.data.DicomObject.Visitor;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * 
 * @version $Revision$ $Date$
 * @since Aug 6, 2008
 */
public class DicomObjects {

    // Suppresses default constructor, ensuring non-instantiability.
    private DicomObjects() {
    }

    /**
     * Returns an unmodifiable view of the specified dicom object. This method
     * allows modules to provide users with "read-only" access to internal dicom
     * objects. Query operations on the returned dicom object "read through" to
     * the specified dicom object, and attempts to modify the returned dicom
     * object result in an <tt>UnsupportedOperationException</tt>.
     * <p>
     * 
     * @param dcmobj
     *            the dicom object for which an unmodifiable view is to be
     *            returned.
     * @return an unmodifiable view of the specified dicom object.
     */
    public static DicomObject unmodifiableDicomObject(DicomObject dcmobj) {
        return new UnmodifiableDicomObject(dcmobj);
    }

    static class UnmodifiableDicomObject implements DicomObject {

        private static final long serialVersionUID = 4384087053472506817L;

        private final DicomObject dcmobj;

        public UnmodifiableDicomObject(DicomObject dcmobj) {
            if (dcmobj == null)
                throw new NullPointerException();

            this.dcmobj = dcmobj;
        }

        @Override
        public boolean accept(Visitor visitor) {
            return dcmobj.accept(new UnmodifiableVisitor(visitor));
        }

        @Override
        public void add(DicomElement attr) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean bigEndian() {
            return dcmobj.bigEndian();
        }

        @Override
        public void bigEndian(boolean bigEndian) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean cacheGet() {
            return dcmobj.cacheGet();
        }

        @Override
        public void cacheGet(boolean cacheGet) {
            dcmobj.cacheGet(cacheGet);
        }

        @Override
        public boolean cachePut() {
            return dcmobj.cachePut();
        }

        @Override
        public void cachePut(boolean cachePut) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void clear() {
            throw new UnsupportedOperationException();
        }

        @Override
        public DicomObject command() {
            return new UnmodifiableDicomObject(dcmobj.command());
        }

        @Override
        public Iterator<DicomElement> commandIterator() {
            return new UnmodifiabledIterator(dcmobj.commandIterator());
        }

        @Override
        public boolean contains(int tag) {
            return dcmobj.contains(tag);
        }

        @Override
        public boolean containsAll(DicomObject keys) {
            return dcmobj.containsAll(keys);
        }

        @Override
        public boolean containsValue(int tag) {
            return dcmobj.containsValue(tag);
        }

        @Override
        public void copyTo(DicomObject destination) {
            dcmobj.copyTo(destination);
        }

        @Override
        public DicomObject dataset() {
            return new UnmodifiableDicomObject(dcmobj.dataset());
        }

        @Override
        public Iterator<DicomElement> datasetIterator() {
            return new UnmodifiabledIterator(dcmobj.datasetIterator());
        }

        @Override
        public DicomObject exclude(int[] tags) {
            return new UnmodifiableDicomObject(dcmobj.exclude(tags));
        }

        @Override
        public DicomObject excludePrivate() {
            return new UnmodifiableDicomObject(dcmobj.excludePrivate());
        }

        @Override
        public DicomObject fileMetaInfo() {
            return new UnmodifiableDicomObject(dcmobj.fileMetaInfo());
        }

        @Override
        public Iterator<DicomElement> fileMetaInfoIterator() {
            return new UnmodifiabledIterator(dcmobj.fileMetaInfoIterator());
        }

        @Override
        public DicomElement get(int tag) {
            DicomElement e = dcmobj.get(tag);
            return e != null ? new UnmodifiableDicomElement(e) : null;
        }

        @Override
        public DicomElement get(int[] tagPath) {
            DicomElement e = dcmobj.get(tagPath);
            return e != null ? new UnmodifiableDicomElement(e) : null;
        }

        @Override
        public byte[] getBytes(int tag, boolean bigEndian) {
            return dcmobj.getBytes(tag, bigEndian);
        }

        @Override
        public byte[] getBytes(int tag) {
            return dcmobj.getBytes(tag);
        }

        @Override
        public byte[] getBytes(int[] tagPath, boolean bigEndian) {
            return dcmobj.getBytes(tagPath, bigEndian);
        }

        @Override
        public byte[] getBytes(int[] tagPath) {
            return dcmobj.getBytes(tagPath);
        }

        @Override
        public Date getDate(int tag, Date defVal) {
            return dcmobj.getDate(tag, defVal);
        }

        @Override
        public Date getDate(int daTag, int tmTag, Date defVal) {
            return dcmobj.getDate(daTag, tmTag, defVal);
        }

        @Override
        public Date getDate(int daTag, int tmTag) {
            return dcmobj.getDate(daTag, tmTag);
        }

        @Override
        public Date getDate(int tag) {
            return dcmobj.getDate(tag);
        }

        @Override
        public Date getDate(int[] tagPath, Date defVal) {
            return dcmobj.getDate(tagPath, defVal);
        }

        @Override
        public Date getDate(int[] itemPath, int daTag, int tmTag, Date defVal) {
            return dcmobj.getDate(itemPath, daTag, tmTag, defVal);
        }

        @Override
        public Date getDate(int[] itemPath, int daTag, int tmTag) {
            return dcmobj.getDate(itemPath, daTag, tmTag);
        }

        @Override
        public Date getDate(int[] tagPath) {
            return dcmobj.getDate(tagPath);
        }

        @Override
        public DateRange getDateRange(int tag, DateRange defVal) {
            return dcmobj.getDateRange(tag, defVal);
        }

        @Override
        public DateRange getDateRange(int daTag, int tmTag, DateRange defVal) {
            return dcmobj.getDateRange(daTag, tmTag, defVal);
        }

        @Override
        public DateRange getDateRange(int daTag, int tmTag) {
            return dcmobj.getDateRange(daTag, tmTag);
        }

        @Override
        public DateRange getDateRange(int tag) {
            return dcmobj.getDateRange(tag);
        }

        @Override
        public DateRange getDateRange(int[] tagPath, DateRange defVal) {
            return dcmobj.getDateRange(tagPath, defVal);
        }

        @Override
        public DateRange getDateRange(int[] itemPath, int daTag, int tmTag,
                DateRange defVal) {
            return dcmobj.getDateRange(itemPath, daTag, tmTag, defVal);
        }

        @Override
        public DateRange getDateRange(int[] itemPath, int daTag, int tmTag) {
            return dcmobj.getDateRange(itemPath, daTag, tmTag);
        }

        @Override
        public DateRange getDateRange(int[] tagPath) {
            return dcmobj.getDateRange(tagPath);
        }

        @Override
        public Date[] getDates(int tag, Date[] defVal) {
            return dcmobj.getDates(tag, defVal);
        }

        @Override
        public Date[] getDates(int daTag, int tmTag, Date[] defVal) {
            return dcmobj.getDates(daTag, tmTag, defVal);
        }

        @Override
        public Date[] getDates(int daTag, int tmTag) {
            return dcmobj.getDates(daTag, tmTag);
        }

        @Override
        public Date[] getDates(int tag) {
            return dcmobj.getDates(tag);
        }

        @Override
        public Date[] getDates(int[] tagPath, Date[] defVal) {
            return dcmobj.getDates(tagPath, defVal);
        }

        @Override
        public Date[] getDates(int[] itemPath, int daTag, int tmTag,
                Date[] defVal) {
            return dcmobj.getDates(itemPath, daTag, tmTag, defVal);
        }

        @Override
        public Date[] getDates(int[] itemPath, int daTag, int tmTag) {
            return dcmobj.getDates(itemPath, daTag, tmTag);
        }

        @Override
        public Date[] getDates(int[] tagPath) {
            return dcmobj.getDates(tagPath);
        }

        @Override
        public double getDouble(int tag, double defVal) {
            return dcmobj.getDouble(tag, defVal);
        }

        @Override
        public double getDouble(int tag) {
            return dcmobj.getDouble(tag);
        }

        @Override
        public double getDouble(int[] tagPath, double defVal) {
            return dcmobj.getDouble(tagPath, defVal);
        }

        @Override
        public double getDouble(int[] tagPath) {
            return dcmobj.getDouble(tagPath);
        }

        @Override
        public double[] getDoubles(int tag, double[] defVal) {
            return dcmobj.getDoubles(tag, defVal);
        }

        @Override
        public double[] getDoubles(int tag) {
            return dcmobj.getDoubles(tag);
        }

        @Override
        public double[] getDoubles(int[] tagPath, double[] defVal) {
            return dcmobj.getDoubles(tagPath, defVal);
        }

        @Override
        public double[] getDoubles(int[] tagPath) {
            return dcmobj.getDoubles(tagPath);
        }

        @Override
        public float getFloat(int tag, float defVal) {
            return dcmobj.getFloat(tag, defVal);
        }

        @Override
        public float getFloat(int tag) {
            return dcmobj.getFloat(tag);
        }

        @Override
        public float getFloat(int[] tagPath, float defVal) {
            return dcmobj.getFloat(tagPath, defVal);
        }

        @Override
        public float getFloat(int[] tagPath) {
            return dcmobj.getFloat(tagPath);
        }

        @Override
        public float[] getFloats(int tag, float[] defVal) {
            return dcmobj.getFloats(tag, defVal);
        }

        @Override
        public float[] getFloats(int tag) {
            return dcmobj.getFloats(tag);
        }

        @Override
        public float[] getFloats(int[] tagPath, float[] defVal) {
            return dcmobj.getFloats(tagPath, defVal);
        }

        @Override
        public float[] getFloats(int[] tagPath) {
            return dcmobj.getFloats(tagPath);
        }

        @Override
        public int getInt(int tag, int defVal) {
            return dcmobj.getInt(tag, defVal);
        }

        @Override
        public int getInt(int tag) {
            return dcmobj.getInt(tag);
        }

        @Override
        public int getInt(int[] tagPath, int defVal) {
            return dcmobj.getInt(tagPath, defVal);
        }

        @Override
        public int getInt(int[] tagPath) {
            return dcmobj.getInt(tagPath);
        }

        @Override
        public int[] getInts(int tag, int[] defVal) {
            return dcmobj.getInts(tag, defVal);
        }

        @Override
        public int[] getInts(int tag) {
            return dcmobj.getInts(tag);
        }

        @Override
        public int[] getInts(int[] tagPath, int[] defVal) {
            return dcmobj.getInts(tagPath, defVal);
        }

        @Override
        public int[] getInts(int[] tagPath) {
            return dcmobj.getInts(tagPath);
        }

        @Override
        public long getItemOffset() {
            return dcmobj.getItemOffset();
        }

        @Override
        public int getItemPosition() {
            return dcmobj.getItemPosition();
        }

        @Override
        public DicomObject getNestedDicomObject(int tag) {
            DicomObject item = dcmobj.getNestedDicomObject(tag);
            return item != null ? new UnmodifiableDicomObject(item) : null;
        }

        @Override
        public DicomObject getNestedDicomObject(int[] itemPath) {
            DicomObject item = dcmobj.getNestedDicomObject(itemPath);
            return item != null ? new UnmodifiableDicomObject(item) : null;
        }

        @Override
        public DicomObject getParent() {
            DicomObject parent = dcmobj.getParent();
            return parent != null ? new UnmodifiableDicomObject(parent) : null;
        }

        @Override
        public String getPrivateCreator(int tag) {
            return dcmobj.getPrivateCreator(tag);
        }

        @Override
        public DicomObject getRoot() {
            return dcmobj.isRoot() ? this
                    : new UnmodifiableDicomObject(dcmobj.getRoot());
        }

        @Override
        public short[] getShorts(int tag, short[] defVal) {
            return dcmobj.getShorts(tag, defVal);
        }

        @Override
        public short[] getShorts(int tag) {
            return dcmobj.getShorts(tag);
        }

        @Override
        public short[] getShorts(int[] tagPath, short[] defVal) {
            return dcmobj.getShorts(tagPath, defVal);
        }

        @Override
        public short[] getShorts(int[] tagPath) {
            return dcmobj.getShorts(tagPath);
        }

        @Override
        public SpecificCharacterSet getSpecificCharacterSet() {
            return dcmobj.getSpecificCharacterSet();
        }

        @Override
        public String getString(int tag, String defVal) {
            return dcmobj.getString(tag, defVal);
        }

        @Override
        public String getString(int tag) {
            return dcmobj.getString(tag);
        }

        @Override
        public String getString(int[] tagPath, String defVal) {
            return dcmobj.getString(tagPath, defVal);
        }

        @Override
        public String getString(int[] tagPath) {
            return dcmobj.getString(tagPath);
        }

        @Override
        public String[] getStrings(int tag, String[] defVal) {
            return dcmobj.getStrings(tag, defVal);
        }

        @Override
        public String[] getStrings(int tag) {
            return dcmobj.getStrings(tag);
        }

        @Override
        public String[] getStrings(int[] tagPath, String[] defVal) {
            return dcmobj.getStrings(tagPath, defVal);
        }

        @Override
        public String[] getStrings(int[] tagPath) {
            return dcmobj.getStrings(tagPath);
        }

        @Override
        public void initFileMetaInformation(String cuid, String iuid,
                String tsuid) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void initFileMetaInformation(String tsuid) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isEmpty() {
            return dcmobj.isEmpty();
        }

        @Override
        public boolean isRoot() {
            return dcmobj.isRoot();
        }

        @Override
        public Iterator<DicomElement> iterator() {
            return new UnmodifiabledIterator(dcmobj.iterator());
        }

        @Override
        public Iterator<DicomElement> iterator(int fromTag, int toTag) {
            return new UnmodifiabledIterator(dcmobj.iterator(fromTag, toTag));
        }

        @Override
        public boolean matches(DicomObject keys, boolean ignoreCaseOfPN) {
            return dcmobj.matches(keys, ignoreCaseOfPN);
        }

        @Override
        public String nameOf(int tag) {
            return dcmobj.nameOf(tag);
        }

        @Override
        public DicomElement putBytes(int tag, VR vr, byte[] val,
                boolean bigEndian) {
            throw new UnsupportedOperationException();
        }

        @Override
        public DicomElement putBytes(int tag, VR vr, byte[] val) {
            throw new UnsupportedOperationException();
        }

        @Override
        public DicomElement putBytes(int[] tagPath, VR vr, byte[] val,
                boolean bigEndian) {
            throw new UnsupportedOperationException();
        }

        @Override
        public DicomElement putBytes(int[] tagPath, VR vr, byte[] val) {
            throw new UnsupportedOperationException();
        }

        @Override
        public DicomElement putDate(int tag, VR vr, Date val) {
            throw new UnsupportedOperationException();
        }

        @Override
        public DicomElement putDate(int[] tagPath, VR vr, Date val) {
            throw new UnsupportedOperationException();
        }

        @Override
        public DicomElement putDateRange(int tag, VR vr, DateRange val) {
            throw new UnsupportedOperationException();
        }

        @Override
        public DicomElement putDateRange(int[] tagPath, VR vr, DateRange val) {
            throw new UnsupportedOperationException();
        }

        @Override
        public DicomElement putDates(int tag, VR vr, Date[] val) {
            throw new UnsupportedOperationException();
        }

        @Override
        public DicomElement putDates(int[] tagPath, VR vr, Date[] val) {
            throw new UnsupportedOperationException();
        }

        @Override
        public DicomElement putDouble(int tag, VR vr, double val) {
            throw new UnsupportedOperationException();
        }

        @Override
        public DicomElement putDouble(int[] tagPath, VR vr, double val) {
            throw new UnsupportedOperationException();
        }

        @Override
        public DicomElement putDoubles(int tag, VR vr, double[] val) {
            throw new UnsupportedOperationException();
        }

        @Override
        public DicomElement putDoubles(int[] tagPath, VR vr, double[] val) {
            throw new UnsupportedOperationException();
        }

        @Override
        public DicomElement putFloat(int tag, VR vr, float val) {
            throw new UnsupportedOperationException();
        }

        @Override
        public DicomElement putFloat(int[] tagPath, VR vr, float val) {
            throw new UnsupportedOperationException();
        }

        @Override
        public DicomElement putFloats(int tag, VR vr, float[] val) {
            throw new UnsupportedOperationException();
        }

        @Override
        public DicomElement putFloats(int[] tagPath, VR vr, float[] val) {
            throw new UnsupportedOperationException();
        }

        @Override
        public DicomElement putFragments(int tag, VR vr, boolean bigEndian,
                int capacity) {
            throw new UnsupportedOperationException();
        }

        @Override
        public DicomElement putFragments(int tag, VR vr, boolean bigEndian) {
            throw new UnsupportedOperationException();
        }

        @Override
        public DicomElement putFragments(int[] tagPath, VR vr,
                boolean bigEndian, int capacity) {
            throw new UnsupportedOperationException();
        }

        @Override
        public DicomElement putFragments(int[] tagPath, VR vr, boolean bigEndian) {
            throw new UnsupportedOperationException();
        }

        @Override
        public DicomElement putInt(int tag, VR vr, int val) {
            throw new UnsupportedOperationException();
        }

        @Override
        public DicomElement putInt(int[] tagPath, VR vr, int val) {
            throw new UnsupportedOperationException();
        }

        @Override
        public DicomElement putInts(int tag, VR vr, int[] val) {
            throw new UnsupportedOperationException();
        }

        @Override
        public DicomElement putInts(int[] tagPath, VR vr, int[] val) {
            throw new UnsupportedOperationException();
        }

        @Override
        public DicomElement putNestedDicomObject(int tag, DicomObject item) {
            throw new UnsupportedOperationException();
        }

        @Override
        public DicomElement putNestedDicomObject(int[] tagPath, DicomObject item) {
            throw new UnsupportedOperationException();
        }

        @Override
        public DicomElement putNull(int tag, VR vr) {
            throw new UnsupportedOperationException();
        }

        @Override
        public DicomElement putNull(int[] tagPath, VR vr) {
            throw new UnsupportedOperationException();
        }

        @Override
        public DicomElement putSequence(int tag, int capacity) {
            throw new UnsupportedOperationException();
        }

        @Override
        public DicomElement putSequence(int tag) {
            throw new UnsupportedOperationException();
        }

        @Override
        public DicomElement putSequence(int[] tagPath, int capacity) {
            throw new UnsupportedOperationException();
        }

        @Override
        public DicomElement putSequence(int[] tagPath) {
            throw new UnsupportedOperationException();
        }

        @Override
        public DicomElement putShorts(int tag, VR vr, short[] val) {
            throw new UnsupportedOperationException();
        }

        @Override
        public DicomElement putShorts(int[] tagPath, VR vr, short[] val) {
            throw new UnsupportedOperationException();
        }

        @Override
        public DicomElement putString(int tag, VR vr, String val) {
            throw new UnsupportedOperationException();
        }

        @Override
        public DicomElement putString(int[] tagPath, VR vr, String val) {
            throw new UnsupportedOperationException();
        }

        @Override
        public DicomElement putStrings(int tag, VR vr, String[] val) {
            throw new UnsupportedOperationException();
        }

        @Override
        public DicomElement putStrings(int[] tagPath, VR vr, String[] val) {
            throw new UnsupportedOperationException();
        }

        @Override
        public DicomElement remove(int tag) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int resolveTag(int tag, String privateCreator, boolean reserve) {
            if (reserve) {
                throw new UnsupportedOperationException();
            }
            return dcmobj.resolveTag(tag, privateCreator, reserve);
        }

        @Override
       public int resolveTag(int tag, String privateCreator) {
            return dcmobj.resolveTag(tag, privateCreator);
        }

        @Override
        public void serializeElements(ObjectOutputStream oos)
                throws IOException {
            dcmobj.serializeElements(oos);
        }

        @Override
        public void setItemOffset(long offset) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setItemPosition(int pos) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setParent(DicomObject parent) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void shareElements() {
            dcmobj.shareElements();
        }

        @Override
        public int size() {
            return dcmobj.size();
        }

        @Override
        public DicomObject subSet(DicomObject filter) {
            return new UnmodifiableDicomObject(dcmobj.subSet(filter));
        }

        @Override
        public DicomObject subSet(int fromTag, int toTag) {
            return new UnmodifiableDicomObject(dcmobj.subSet(fromTag, toTag));
        }

        @Override
        public DicomObject subSet(int[] tags) {
            return new UnmodifiableDicomObject(dcmobj.subSet(tags));
        }

        @Override
        public int toStringBuffer(StringBuffer sb,
                DicomObjectToStringParam param) {
            return dcmobj.toStringBuffer(sb, param);
        }

        @Override
       public int vm(int tag) {
            return dcmobj.vm(tag);
        }

        @Override
        public VR vrOf(int tag) {
            return dcmobj.vrOf(tag);
        }

    }

    static class UnmodifiableVisitor implements Visitor {

        private final Visitor visitor;

        public UnmodifiableVisitor(Visitor visitor) {
            if (visitor == null)
                throw new NullPointerException();
            
            this.visitor = visitor;
        }

        @Override
        public boolean visit(DicomElement e) {
            return visitor.visit(new UnmodifiableDicomElement(e));
        }

    }

    static class UnmodifiableDicomElement implements DicomElement {
        
        private static final long serialVersionUID = -5205393560442114448L;

        private final DicomElement e;
        
        public UnmodifiableDicomElement(DicomElement e) {
            if (e == null)
                throw new NullPointerException();
            
            this.e = e;
        }

        @Override
        public DicomObject addDicomObject(DicomObject item) {
            throw new UnsupportedOperationException();
        }

        @Override
       public DicomObject addDicomObject(int index, DicomObject item) {
            throw new UnsupportedOperationException();
        }

        @Override
        public byte[] addFragment(byte[] b) {
            throw new UnsupportedOperationException();
        }

        @Override
        public byte[] addFragment(int index, byte[] b) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean bigEndian() {
            return e.bigEndian();
        }

        @Override
        public DicomElement bigEndian(boolean bigEndian) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int countItems() {
            return e.countItems();
        }

        @Override
        public DicomElement filterItems(DicomObject filter) {
            DicomElement filteredItems = e.filterItems(filter);
            return filteredItems != this
                    ? new UnmodifiableDicomElement(filteredItems) : this;
        }

        @Override
        public byte[] getBytes() {
            return e.getBytes();
        }

        @Override
        public Date getDate(boolean cache) {
            return e.getDate(cache);
        }

        @Override
        public DateRange getDateRange(boolean cache) {
            return e.getDateRange(cache);
        }

        @Override
        public Date[] getDates(boolean cache) {
            return e.getDates(cache);
        }

        @Override
        public DicomObject getDicomObject() {
            DicomObject item = e.getDicomObject();
            return item != null ? new UnmodifiableDicomObject(item) : null;
        }

        @Override
        public DicomObject getDicomObject(int index) {
            return new UnmodifiableDicomObject(e.getDicomObject(index));
        }

        @Override
        public double getDouble(boolean cache) {
            return e.getDouble(cache);
        }

        @Override
        public double[] getDoubles(boolean cache) {
            return e.getDoubles(cache);
        }

        @Override
        public float getFloat(boolean cache) {
            return e.getFloat(cache);
        }

        @Override
        public float[] getFloats(boolean cache) {
            return e.getFloats(cache);
        }

        @Override
       public byte[] getFragment(int index) {
            return e.getFragment(index);
        }

        @Override
        public int getInt(boolean cache) {
            return e.getInt(cache);
        }

        @Override
        public int[] getInts(boolean cache) {
            return e.getInts(cache);
        }

        @Override
        public Pattern getPattern(SpecificCharacterSet cs, boolean ignoreCase,
                boolean cache) {
            return e.getPattern(cs, ignoreCase, cache);
        }

        @Override
        public short[] getShorts(boolean cache) {
            return e.getShorts(cache);
        }

        @Override
        public String getString(SpecificCharacterSet cs, boolean cache) {
            return e.getString(cs, cache);
        }

        @Override
        public String[] getStrings(SpecificCharacterSet cs, boolean cache) {
            return e.getStrings(cs, cache);
        }

        @Override
        public boolean hasDicomObjects() {
            return e.hasDicomObjects();
        }

        @Override
        public boolean hasFragments() {
            return e.hasFragments();
        }

        @Override
        public boolean hasItems() {
            return e.hasItems();
        }

        @Override
        public boolean isEmpty() {
            return e.isEmpty();
        }

        @Override
        public int length() {
            return e.length();
        }

        @Override
        public boolean removeDicomObject(DicomObject item) {
            throw new UnsupportedOperationException();
        }

        @Override
        public DicomObject removeDicomObject(int index) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean removeFragment(byte[] b) {
            throw new UnsupportedOperationException();
        }

        @Override
        public byte[] removeFragment(int index) {
            throw new UnsupportedOperationException();
        }

        @Override
        public DicomObject setDicomObject(int index, DicomObject item) {
            throw new UnsupportedOperationException();
        }

        @Override
        public byte[] setFragment(int index, byte[] b) {
            throw new UnsupportedOperationException();
        }

        @Override
        public DicomElement share() {
            return e.share();
        }

        @Override
        public int tag() {
            return e.tag();
        }

        @Override
        public StringBuffer toStringBuffer(StringBuffer sb, int maxValLen) {
            return e.toStringBuffer(sb, maxValLen);
        }

        @Override
        public int vm(SpecificCharacterSet cs) {
            return e.vm(cs);
        }

        @Override
        public VR vr() {
            return e.vr();
        }

    }

    static class UnmodifiabledIterator implements Iterator<DicomElement> {
        private final Iterator<DicomElement> itr;
        public UnmodifiabledIterator(Iterator<DicomElement> itr) {
            if (itr == null)
                throw new NullPointerException();
            
            this.itr = itr;
        }

        @Override
        public boolean hasNext() {
            return itr.hasNext();
        }

        @Override
        public DicomElement next() {
            return new UnmodifiableDicomElement(itr.next());
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

    }

}
