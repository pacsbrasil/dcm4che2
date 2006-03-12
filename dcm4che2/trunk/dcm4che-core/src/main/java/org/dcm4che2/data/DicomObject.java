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
 * Portions created by the Initial Developer are Copyright (C) 2002-2005
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Gunter Zeilinger <gunterze@gmail.com>
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
import java.io.Serializable;
import java.util.Date;
import java.util.Iterator;

/**
 * @author gunter zeilinger(gunterze@gmail.com)
 * @version $Revision$ $Date$
 * @since Aug, 2005
 *
 */
public interface DicomObject extends Serializable
{
    /**
     * Returns number of elements in this Dicom Object.
     * 
     * @return number of elements in this Dicom Object.
     */
    int size();

    /**
     * Returns <code>true</code> if this Dicom Object contains no elements.
     * 
     * @return <code>true</code> if this Dicom Object contains no elements.
     */
    boolean isEmpty();

    /**
     * Removes all elements from this Dicom Object.
     */
    void clear();

    /**
     * @return the root Data Set, if this Data Set is contained within a
     *  Sequence Element of another Data Set, otherwise <code>this</code>. 
     */
    DicomObject getRoot();

    /** 
     * Returns <code>true</code> if this is not a nested Data Set.
     * 
     * @return <code>true</code> if this is not a nested Data Set.
     */
    boolean isRoot();

    /**
     * Returns the Data Set containing this Data Set in a Sequence Element,
     *  or <code>null</code> if this is not a nested Data Set.
     *  
     * @return the Data Set containing this Data Set in a Sequence Element,
     *  or <code>null</code> if this is not a nested Data Set.
     */
    DicomObject getParent();

    /**
     * @return
     */
    SpecificCharacterSet getSpecificCharacterSet();

    /**
     * @return
     */
    Iterator iterator();

    /**
     * @param fromTag
     * @param toTag
     * @return
     */
    Iterator iterator(int fromTag, int toTag);

    /**
     * @return
     */
    Iterator commandIterator();

    /**
     * @return
     */
    Iterator fileMetaInfoIterator();

    /**
     * @return
     */
    Iterator datasetIterator();

    /**
     * @return
     */
    int getItemPosition();

    /**
     * @param pos
     */
    void setItemPosition(int pos);

    /**
     * @return
     */
    long getItemOffset();

    /**
     * @param offset
     */
    void setItemOffset(long offset);

    /**
     * @param tag (group, element) as 8 byte integer: ggggeeee.
     * @return
     */
    VR vrOf(int tag);

    /**
     * @param tag (group, element) as 8 byte integer: ggggeeee.
     * @return
     */
    String nameOf(int tag);

    /**
     * Resolve existing private tag. Invokes 
     * @link{resolveTag(int, String, boolean) resolveTag}(tag, privateCreator,
     * <code>false</code>).
     * 
     * @param tag (group, element) as 8 byte integer: ggggeeee. 
     * @param privateCreator private creator identifier
     * @return resolved tag or <code>-1</code>, if no tags are reserved for
     *         privateCreator.
     */
    int resolveTag(int tag, String privateCreator);

    /**
     * Resolves private tag. If the group number of the specified tag is odd
     * (= private tag) and privateCreator != null, searches for the first private
     * creator data element in (gggg,0010-00FF) which matches privateCreator,
     * and returns <i>ggggEEee</i> with <i>EE</i> the element number
     * of the matching private creator data element and <i>ee</i> the two
     * lower bytes of the element number of the specified tag.>
     * If no matching private creator data element in (gggg,0010-00FF) is found,
     * and reserve=<code>true</code>, the specified privateCreator is inserted
     * in the first unused private creator data element, and <i>ggggEEee</i>
     * with <i>EE</i> the element number the new inserted private creator
     * data element is returned. If reserve=<code>false</code>, <code>-1</code>
     * is returned.<br>
     * If the group number of the specified tag is even (= standard tag) or
     * privateCreator == null, tag is returned unmodified.
     * 
     * @param tag (group, element) as 8 byte integer: ggggeeee. 
     * @param privateCreator private creator identifier
     * @return resolved tag or <code>-1</code>, if no tags are reserved for
     *         privateCreator and reserve=<code>false</code>.
     */
    int resolveTag(int tag, String privateCreator, boolean reserve);

    /**
     * Returns private creator identifier, for given private tag. 
     * 
     * @param tag (group, element) of private tag as 8 byte integer: ggggeeee
     * @return Returns private creator identifier, for given private tag.
     * @throws IllegalArgumentExcepion if tag is not a private tag or if itself
     * a Private Creator Data Element (gggg,00EE).
     */
    String getPrivateCreator(int tag);

    /**
     * @param tag (group, element) as 8 byte integer: ggggeeee.
     * @return
     */
    int vm(int tag);

    /**
     * Returns true, if this DicomObject contains a DicomElement with the
     * specified tag.
     * 
     * @param tag (group, element) as 8 byte integer: ggggeeee.
     * @return
     */
    boolean contains(int tag);

    /**
     * @param tag (group, element) as 8 byte integer: ggggeeee.
     * @return
     */
    boolean containsValue(int tag);

    /**
     * Calls @link{DicomObject.Visitor#vist} for each element in this
     * Dataset. Returns <code>false</code>, if @link{DicomObject.Visitor#visit}
     * returns <code>false</code> for any element.
     * 
     * @param visitor <i>DicomObject.Visitor</i> object, which method
     * @link{DicomObject.Visitor#vist} is called for each element in this
     * Dataset.
     * @return <code>true</code> if @link{DicomObject.Visitor#visit} returns
     * <code>true</code> for all elements of this dataset, <code>false</code>
     * if @link{DicomObject.Visitor#visit} returns <code>false</code> for any element.
     */
    boolean accept(Visitor visitor);

    /** 
     * Visitor object passed to @link{#accept}.
     */
    public interface Visitor
    {
        /** 
         * Called for each element in the visited DicomObject. If it returns
         * <code>false</code>, no further element is visited and
         * @link{DicomObject#accept} returns also <code>false</code>.
         * 
         *  
         * @param e Dicom Element to visit
         * @return <code>true</code> to continue, <code>false</code> to 
         * terminate traversal by @link{DicomObject#accept}. 
         */
        boolean visit(DicomElement e);
    }

    /**
     * @param attr
     */
    void add(DicomElement attr);

    /**
     * @param tag (group, element) as 8 byte integer: ggggeeee.
     * @return
     */
    DicomElement remove(int tag);

    /**
     * @param tag (group, element) as 8 byte integer: ggggeeee.
     * @return
     */
    DicomElement get(int tag);

    /**
     * @param tagPath
     * @return
     */
    DicomElement get(int[] tagPath);

    /**
     * @param tagPath
     * @return
     */
    DicomElement get(String tagPath);

    /**
     * @param tag (group, element) as 8 byte integer: ggggeeee.
     * @param bigEndian
     * @return
     */
    byte[] getBytes(int tag, boolean bigEndian);

    /**
     * @param tagPath
     * @param bigEndian
     * @return
     */
    byte[] getBytes(int[] tagPath, boolean bigEndian);

    /**
     * @param tagPath
     * @param bigEndian
     * @return
     */
    byte[] getBytes(String tagPath, boolean bigEndian);

    /**
     * @param tag (group, element) as 8 byte integer: ggggeeee.
     * @return
     */
    DicomObject getNestedDicomObject(int tag);

    /**
     * @param itemPath
     * @return
     */
    DicomObject getNestedDicomObject(int[] itemPath);

    /**
     * @param itemPath
     * @return
     */
    DicomObject getNestedDicomObject(String itemPath);

    /**
     * @param tag (group, element) as 8 byte integer: ggggeeee.
     * @return
     */
    int getInt(int tag);

    /**
     * @param tag (group, element) as 8 byte integer: ggggeeee.
     * @param defVal TODO
     * @return
     */
    int getInt(int tag, int defVal);
    
    /**
     * @param tagPath
     * @return
     */
    int getInt(int[] tagPath);

    /**
     * @param tagPath
     * @return
     */
    int getInt(int[] tagPath, int defVal);
    
    /**
     * @param tagPath
     * @return
     */
    int getInt(String tagPath);

    /**
     * @param tagPath
     * @return
     */
    int getInt(String tagPath, int defVal);

    /**
     * @param tag (group, element) as 8 byte integer: ggggeeee.
     * @return
     */
    int[] getInts(int tag);


    /**
     * @param tag (group, element) as 8 byte integer: ggggeeee.
     * @return
     */
    int[] getInts(int tag, int[] defVal);

    /**
     * @param tagPath
     * @return
     */
    int[] getInts(int[] tagPath);

    /**
     * @param tagPath
     * @return
     */
    int[] getInts(int[] tagPath, int[] defVal);

    /**
     * @param tagPath
     * @return
     */
    int[] getInts(String tagPath);

    /**
     * @param tagPath
     * @return
     */
    int[] getInts(String tagPath, int[] defVal);

    /**
     * @param tag (group, element) as 8 byte integer: ggggeeee.
     * @return
     */
    float getFloat(int tag);

    /**
     * @param tag (group, element) as 8 byte integer: ggggeeee.
     * @return
     */
    float getFloat(int tag, float defVal);

    /**
     * @param tagPath
     * @return
     */
    float getFloat(int[] tagPath);

    /**
     * @param tagPath
     * @return
     */
    float getFloat(int[] tagPath, float defVal);

    /**
     * @param tagPath
     * @return
     */
    float getFloat(String tagPath);

    /**
     * @param tagPath
     * @return
     */
    float getFloat(String tagPath, float defVal);

    /**
     * @param tag (group, element) as 8 byte integer: ggggeeee.
     * @return
     */
    float[] getFloats(int tag);

    /**
     * @param tag (group, element) as 8 byte integer: ggggeeee.
     * @return
     */
    float[] getFloats(int tag, float[] defVal);

    /**
     * @param tagPath
     * @return
     */
    float[] getFloats(int[] tagPath);

    /**
     * @param tagPath
     * @return
     */
    float[] getFloats(int[] tagPath, float[] defVal);

    /**
     * @param tagPath
     * @return
     */
    float[] getFloats(String tagPath);

    /**
     * @param tagPath
     * @return
     */
    float[] getFloats(String tagPath, float[] defVal);

    /**
     * @param tag (group, element) as 8 byte integer: ggggeeee.
     * @return
     */
    double getDouble(int tag);

    /**
     * @param tag (group, element) as 8 byte integer: ggggeeee.
     * @return
     */
    double getDouble(int tag, double defVal);

    /**
     * @param tagPath
     * @return
     */
    double getDouble(int[] tagPath);

    /**
     * @param tagPath
     * @return
     */
    double getDouble(int[] tagPath, double defVal);

    /**
     * @param tagPath
     * @return
     */
    double getDouble(String tagPath);

    /**
     * @param tagPath
     * @return
     */
    double getDouble(String tagPath, double defVal);

    /**
     * @param tag (group, element) as 8 byte integer: ggggeeee.
     * @return
     */
    double[] getDoubles(int tag);

    /**
     * @param tag (group, element) as 8 byte integer: ggggeeee.
     * @return
     */
    double[] getDoubles(int tag, double[] defVal);

    /**
     * @param tagPath
     * @return
     */
    double[] getDoubles(int[] tagPath);

    /**
     * @param tagPath
     * @return
     */
    double[] getDoubles(int[] tagPath, double[] defVal);

    /**
     * @param tagPath
     * @return
     */
    double[] getDoubles(String tagPath);

    /**
     * @param tagPath
     * @return
     */
    double[] getDoubles(String tagPath, double[] defVal);

    /**
     * @param tag (group, element) as 8 byte integer: ggggeeee.
     * @return
     */
    String getString(int tag);

    /**
     * @param tag (group, element) as 8 byte integer: ggggeeee.
     * @return
     */
    String getString(int tag, String defVal);

    /**
     * @param tagPath
     * @return
     */
    String getString(int[] tagPath);

    /**
     * @param tagPath
     * @return
     */
    String getString(int[] tagPath, String defVal);

    /**
     * @param tagPath
     * @return
     */
    String getString(String tagPath);

    /**
     * @param tagPath
     * @return
     */
    String getString(String tagPath, String defVal);

    /**
     * @param tag (group, element) as 8 byte integer: ggggeeee.
     * @return
     */
    String[] getStrings(int tag);

    /**
     * @param tag (group, element) as 8 byte integer: ggggeeee.
     * @return
     */
    String[] getStrings(int tag, String[] defVal);

    /**
     * @param tagPath
     * @return
     */
    String[] getStrings(int[] tagPath);

    /**
     * @param tagPath
     * @return
     */
    String[] getStrings(int[] tagPath, String[] defVal);

    /**
     * @param tagPath
     * @return
     */
    String[] getStrings(String tagPath);

    /**
     * @param tagPath
     * @return
     */
    String[] getStrings(String tagPath, String[] defVal);

    /**
     * @param tag (group, element) as 8 byte integer: ggggeeee.
     * @return
     */
    Date getDate(int tag);

    /**
     * @param tag (group, element) as 8 byte integer: ggggeeee.
     * @return
     */
    Date getDate(int tag, Date defVal);

    /**
     * @param daTag
     * @param tmTag
     * @return
     */
    Date getDate(int daTag, int tmTag);

    /**
     * @param daTag
     * @param tmTag
     * @return
     */
    Date getDate(int daTag, int tmTag, Date defVal);

    /**
     * @param tagPath
     * @return
     */
    Date getDate(int[] tagPath);

    /**
     * @param tagPath
     * @return
     */
    Date getDate(int[] tagPath, Date defVal);

    /**
     * @param itemPath
     * @param daTag
     * @param tmTag
     * @return
     */
    Date getDate(int[] itemPath, int daTag, int tmTag);

    /**
     * @param itemPath
     * @param daTag
     * @param tmTag
     * @return
     */
    Date getDate(int[] itemPath, int daTag, int tmTag, Date defVal);

    /**
     * @param tagPath
     * @return
     */
    Date getDate(String tagPath);

    /**
     * @param tagPath
     * @return
     */
    Date getDate(String tagPath, Date defVal);

    /**
     * @param itemPath
     * @param daTag
     * @param tmTag
     * @return
     */
    Date getDate(String itemPath, int daTag, int tmTag);

    /**
     * @param itemPath
     * @param daTag
     * @param tmTag
     * @return
     */
    Date getDate(String itemPath, int daTag, int tmTag, Date defVal);

    /**
     * @param tag (group, element) as 8 byte integer: ggggeeee.
     * @return
     */
    Date[] getDates(int tag);

    /**
     * @param tag (group, element) as 8 byte integer: ggggeeee.
     * @return
     */
    Date[] getDates(int tag, Date[] defVal);

    /**
     * @param daTag
     * @param tmTag
     * @return
     */
    Date[] getDates(int daTag, int tmTag);

    /**
     * @param daTag
     * @param tmTag
     * @return
     */
    Date[] getDates(int daTag, int tmTag, Date[] defVal);

    /**
     * @param tagPath
     * @return
     */
    Date[] getDates(int[] tagPath);

    /**
     * @param tagPath
     * @return
     */
    Date[] getDates(int[] tagPath, Date[] defVal);

    /**
     * @param itemPath
     * @param daTag
     * @param tmTag
     * @return
     */
    Date[] getDates(int[] itemPath, int daTag, int tmTag);

    /**
     * @param itemPath
     * @param daTag
     * @param tmTag
     * @return
     */
    Date[] getDates(int[] itemPath, int daTag, int tmTag, Date[] defVal);

    /**
     * @param tagPath
     * @return
     */
    Date[] getDates(String tagPath);

    /**
     * @param tagPath
     * @return
     */
    Date[] getDates(String tagPath, Date[] defVal);

    /**
     * @param itemPath
     * @param daTag
     * @param tmTag
     * @return
     */
    Date[] getDates(String itemPath, int daTag, int tmTag);

    /**
     * @param itemPath
     * @param daTag
     * @param tmTag
     * @return
     */
    Date[] getDates(String itemPath, int daTag, int tmTag, Date[] defVal);

    /**
     * @param tag (group, element) as 8 byte integer: ggggeeee.
     * @return
     */
    DateRange getDateRange(int tag);

    /**
     * @param tag (group, element) as 8 byte integer: ggggeeee.
     * @return
     */
    DateRange getDateRange(int tag, DateRange defVal);

    /**
     * @param daTag
     * @param tmTag
     * @return
     */
    DateRange getDateRange(int daTag, int tmTag);

    /**
     * @param daTag
     * @param tmTag
     * @return
     */
    DateRange getDateRange(int daTag, int tmTag, DateRange defVal);

    /**
     * @param tagPath
     * @return
     */
    DateRange getDateRange(int[] tagPath);

    /**
     * @param tagPath
     * @return
     */
    DateRange getDateRange(int[] tagPath, DateRange defVal);

    /**
     * @param itemPath
     * @param daTag
     * @param tmTag
     * @return
     */
    DateRange getDateRange(int[] itemPath, int daTag, int tmTag);

    /**
     * @param itemPath
     * @param daTag
     * @param tmTag
     * @return
     */
    DateRange getDateRange(int[] itemPath, int daTag, int tmTag, DateRange defVal);

    /**
     * @param tagPath
     * @return
     */
    DateRange getDateRange(String tagPath);

    /**
     * @param tagPath
     * @return
     */
    DateRange getDateRange(String tagPath, DateRange defVal);

    /**
     * @param itemPath
     * @param daTag
     * @param tmTag
     * @return
     */
    DateRange getDateRange(String itemPath, int daTag, int tmTag);

    /**
     * @param itemPath
     * @param daTag
     * @param tmTag
     * @return
     */
    DateRange getDateRange(String itemPath, int daTag, int tmTag, DateRange defVal);

    /**
     * @param tag (group, element) as 8 byte integer: ggggeeee.
     * @param vr
     * @return
     */
    DicomElement putNull(int tag, VR vr);

    /**
     * @param tag (group, element) as 8 byte integer: ggggeeee.
     * @param vr
     * @param bigEndian
     * @param val
     * @return
     */
    DicomElement putBytes(int tag, VR vr, boolean bigEndian, byte[] val);

    /**
     * @param tag (group, element) as 8 byte integer: ggggeeee.
     * @param item
     * @return
     */
    DicomElement putNestedDicomObject(int tag, DicomObject item);

    /**
     * @param tag (group, element) as 8 byte integer: ggggeeee.
     * @param vr
     * @param val
     * @return
     */
    DicomElement putInt(int tag, VR vr, int val);

    /**
     * @param tag (group, element) as 8 byte integer: ggggeeee.
     * @param vr
     * @param val
     * @return
     */
    DicomElement putInts(int tag, VR vr, int[] val);

    /**
     * @param tag (group, element) as 8 byte integer: ggggeeee.
     * @param vr
     * @param val
     * @return
     */
    DicomElement putFloat(int tag, VR vr, float val);

    /**
     * @param tag (group, element) as 8 byte integer: ggggeeee.
     * @param vr
     * @param val
     * @return
     */
    DicomElement putFloats(int tag, VR vr, float[] val);

    /**
     * @param tag (group, element) as 8 byte integer: ggggeeee.
     * @param vr
     * @param val
     * @return
     */
    DicomElement putDouble(int tag, VR vr, double val);

    /**
     * @param tag (group, element) as 8 byte integer: ggggeeee.
     * @param vr
     * @param val
     * @return
     */
    DicomElement putDoubles(int tag, VR vr, double[] val);

    /**
     * @param tag (group, element) as 8 byte integer: ggggeeee.
     * @param vr
     * @param val
     * @return
     */
    DicomElement putString(int tag, VR vr, String val);

    /**
     * @param tag (group, element) as 8 byte integer: ggggeeee.
     * @param vr
     * @param val
     * @return
     */
    DicomElement putStrings(int tag, VR vr, String[] val);

    /**
     * @param tag (group, element) as 8 byte integer: ggggeeee.
     * @param vr
     * @param val
     * @return
     */
    DicomElement putDate(int tag, VR vr, Date val);

    /**
     * @param tag (group, element) as 8 byte integer: ggggeeee.
     * @param vr
     * @param val
     * @return
     */
    DicomElement putDates(int tag, VR vr, Date[] val);

    /**
     * @param tag (group, element) as 8 byte integer: ggggeeee.
     * @param vr
     * @param val
     * @return
     */
    DicomElement putDateRange(int tag, VR vr, DateRange val);

    /**
     * @param tag (group, element) as 8 byte integer: ggggeeee.
     * @return
     */
    DicomElement putSequence(int tag);

    /**
     * @param tag (group, element) as 8 byte integer: ggggeeee.
     * @param capacity
     * @return
     */
    DicomElement putSequence(int tag, int capacity);

    /**
     * @param tag (group, element) as 8 byte integer: ggggeeee.
     * @param vr
     * @param bigEndian
     * @return
     */
    DicomElement putFragments(int tag, VR vr, boolean bigEndian);

    /**
     * @param tag (group, element) as 8 byte integer: ggggeeee.
     * @param vr
     * @param bigEndian
     * @param capacity
     * @return
     */
    DicomElement putFragments(int tag, VR vr, boolean bigEndian, int capacity);

    /**
     * 
     */
    void shareElements();

    /**
     * @param oos
     * @throws IOException
     */
    void serializeElements(ObjectOutputStream oos) throws IOException;

    /**
     * @param destination
     */
    void copyTo(DicomObject destination);

    /**
     * @param keys
     * @param ignoreCaseOfPN
     * @return
     */
    boolean matches(DicomObject keys, boolean ignoreCaseOfPN);

    /**
     * @return
     */
    boolean cacheGet();

    /**
     * @param cacheGet
     */
    void cacheGet(boolean cacheGet);

    /**
     * @return
     */
    boolean cachePut();

    /**
     * @param cachePut
     */
    void cachePut(boolean cachePut);

    /**
     * @return
     */
    boolean bigEndian();

    /**
     * @param bigEndian
     */
    void bigEndian(boolean bigEndian);
    
    /**
     * @return
     */
    DicomObject command();

    /**
     * @return
     */
    DicomObject dataset();

    /**
     * @return
     */
    DicomObject fileMetaInfo();

    /**
     * @param filter
     * @return
     */
    DicomObject subSet(DicomObject filter);

    /**
     * @param fromTag
     * @param toTag
     * @return
     */
    DicomObject subSet(int fromTag, int toTag);

    /**
     * @param tags
     * @return
     */
    DicomObject subSet(int[] tags);

    /**
     * @param tags
     * @return
     */
    DicomObject exclude(int[] tags);

    /**
     * @return
     */
    DicomObject excludePrivate();

    /**
     * @param tsuid
     */
    void initFileMetaInformation(String tsuid);

    /**
     * @param cuid
     * @param iuid
     * @param tsuid
     */
    void initFileMetaInformation(String cuid, String iuid, String tsuid);
    
    /** 
     * @param sb
     * @param param
     * @return
     */
    int toStringBuffer(StringBuffer sb, DicomObjectToStringParam param);
}
