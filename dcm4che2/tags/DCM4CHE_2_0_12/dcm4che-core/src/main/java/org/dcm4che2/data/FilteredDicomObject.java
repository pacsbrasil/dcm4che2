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

import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.dcm4che2.util.TagUtils;

abstract class FilteredDicomObject extends AbstractDicomObject
{

    static final class Include extends FilteredDicomObject
    {
        private static final long serialVersionUID = 1L;
        final int[] tags;

        public Include(DicomObject attrs, int[] tags)
        {
            super(attrs);
            this.tags = (int[]) tags.clone();
            Arrays.sort(this.tags);
        }

        protected boolean filter(int tag)
        {
            return Arrays.binarySearch(tags, tag) >= 0;
        }
    }

    static final class Exclude extends FilteredDicomObject
    {
        private static final long serialVersionUID = 1L;
        final int[] tags;

        public Exclude(DicomObject attrs, int[] tags)
        {
            super(attrs);
            this.tags = (int[]) tags.clone();
            Arrays.sort(this.tags);
        }

        protected boolean filter(int tag)
        {
            return Arrays.binarySearch(tags, tag) < 0;
        }
    }

    static final class Range extends FilteredDicomObject
    {
        private static final long serialVersionUID = 1L;
        final long fromTag;
        final long toTag;

        public Range(DicomObject attrs, int fromTag, int toTag)
        {
            super(attrs);
            if ((fromTag & 0xffffffffL) > (toTag & 0xffffffffL))
            {
                throw new IllegalArgumentException("fromTag:"
                        + TagUtils.toString(fromTag) + " > toTag:"
                        + TagUtils.toString(toTag));
            }
            this.fromTag = fromTag & 0xffffffffL;
            this.toTag = toTag & 0xffffffffL;
            if (this.fromTag > this.toTag)
            {
                throw new IllegalArgumentException("fromTag:"
                        + TagUtils.toString(fromTag) + " > toTag:"
                        + TagUtils.toString(toTag));
            }
        }

        protected boolean filter(int tag)
        {
            long ltag = tag & 0xffffffffL;
            return fromTag <= ltag && ltag <= toTag;
        }

        public Iterator iterator()
        {
            return new Itr(attrs.iterator((int) fromTag, (int) toTag));
        }

        public Iterator iterator(int fromTag, int toTag)
        {
            final long maxFromTag = Math.max(fromTag & 0xffffffff, this.fromTag);
            final long minToTag = Math.min(toTag & 0xffffffff, this.toTag);
            return new Itr(attrs.iterator((int) maxFromTag, (int) minToTag));
        }
    }

    static final class ExcludePrivate extends FilteredDicomObject
    {
        private static final long serialVersionUID = 1L;

        public ExcludePrivate(DicomObject attrs)
        {
            super(attrs);
        }

        protected boolean filter(int tag)
        {
            return !TagUtils.isPrivateDataElement(tag);
        }

    }

    static final class FilterSet extends FilteredDicomObject
    {
        private static final long serialVersionUID = 1L;

        final class FilterItr extends Itr implements Iterator
        {

            public FilterItr(Iterator itr)
            {
                super(itr);
            }

            public Object next()
            {
                DicomElement attr = (DicomElement) super.next();
                if (attr.vr() == VR.SQ && attr.hasItems())
                {
                    return attr.filterItems(filter.getNestedDicomObject(attr.tag()));
                }
                return attr;
            }

        }

        final DicomObject filter;

        public FilterSet(DicomObject attrs, DicomObject filter)
        {
            super(attrs);
            this.filter = filter;
        }

        protected boolean filter(int tag)
        {
            return filter.contains(tag);
        }

        public DicomObject getNestedDicomObject(int tag)
        {
            DicomObject item = super.getNestedDicomObject(tag);
            if (item == null)
                return null;

            return item.subSet(filter.getNestedDicomObject(tag));
        }

        public Iterator iterator()
        {
            return new FilterItr(attrs.iterator());
        }

        public Iterator iterator(int fromTag, int toTag)
        {
            return new FilterItr(attrs.iterator(fromTag, toTag));
        }
    }

    protected final DicomObject attrs;

    public FilteredDicomObject(DicomObject attrs)
    {
        this.attrs = attrs;
    }

    protected abstract boolean filter(int tag);

    public int getItemPosition()
    {
        return attrs.getItemPosition();
    }

    public void setItemPosition(int pos)
    {
        attrs.setItemPosition(pos);
    }

    public long getItemOffset()
    {
        return attrs.getItemOffset();
    }

    public void setItemOffset(long offset)
    {
        //NO OP
    }

    public boolean accept(final Visitor visitor)
    {
        return attrs.accept(new Visitor()
        {
            public boolean visit(DicomElement attr)
            {
                return !filter(attr.tag()) || visitor.visit(attr);
            }
        });
    }

    public Iterator iterator()
    {
        return new Itr(attrs.iterator());
    }

    public Iterator iterator(int fromTag, int toTag)
    {
        return new Itr(attrs.iterator(fromTag, toTag));
    }

    protected class Itr implements Iterator
    {
        final Iterator itr;
        DicomElement next;

        public Itr(Iterator itr)
        {
            this.itr = itr;
            findNext();
        }

        public void remove()
        {
            throw new UnsupportedOperationException();
        }

        public boolean hasNext()
        {
            return next != null;
        }

        public Object next()
        {
            if (next == null)
                return new NoSuchElementException();
            Object tmp = next;
            findNext();
            return tmp;
        }

        private void findNext()
        {
            while (itr.hasNext())
            {
                next = (DicomElement) itr.next();
                if (filter(next.tag()))
                    return;
            }
            next = null;
        }

    }

    public boolean contains(int tag)
    {
        return filter(tag) && attrs.contains(tag);
    }

    public DicomElement get(int tag)
    {
        return filter(tag) ? attrs.get(tag) : null;
    }

    public DicomObject getParent()
    {
        return attrs.getParent();
    }

    public void setParent(DicomObject parent)
    {
        throw new UnsupportedOperationException();
    }

    public String getPrivateCreator(int privateTag)
    {
        return filter(privateTag) ? attrs.getPrivateCreator(privateTag) : null;
    }

    public DicomObject getRoot()
    {
        return attrs.getRoot();
    }

    public SpecificCharacterSet getSpecificCharacterSet()
    {
        return attrs.getSpecificCharacterSet();
    }

    public boolean cacheGet()
    {
        return attrs.cacheGet();
    }

    public boolean cachePut()
    {
        return attrs.cachePut();
    }

    public boolean bigEndian()
    {
        return attrs.bigEndian();
    }

    public void add(DicomElement attr)
    {
        throw new UnsupportedOperationException();
    }

    public DicomElement putBytes(int tag, VR vr, byte[] val, boolean bigEndian)
    {
        throw new UnsupportedOperationException();
    }

    public DicomElement putDouble(int tag, VR vr, double val)
    {
        throw new UnsupportedOperationException();
    }

    public DicomElement putDoubles(int tag, VR vr, double[] val)
    {
        throw new UnsupportedOperationException();
    }

    public DicomElement putNull(int tag, VR vr)
    {
        throw new UnsupportedOperationException();
    }

    public DicomElement putFloat(int tag, VR vr, float val)
    {
        throw new UnsupportedOperationException();
    }

    public DicomElement putFloats(int tag, VR vr, float[] val)
    {
        throw new UnsupportedOperationException();
    }

    public DicomElement putInt(int tag, VR vr, int val)
    {
        throw new UnsupportedOperationException();
    }

    public DicomElement putInts(int tag, VR vr, int[] val)
    {
        throw new UnsupportedOperationException();
    }

    public DicomElement putShorts(int tag, VR vr, short[] val)
    {
        throw new UnsupportedOperationException();
    }

    public DicomElement putNestedDicomObject(int tag, DicomObject item)
    {
        throw new IllegalArgumentException(TagUtils.toString(tag));
    }

    public DicomElement putString(int tag, VR vr, String val)
    {
        throw new UnsupportedOperationException();
    }

    public DicomElement putStrings(int tag, VR vr, String[] val)
    {
        throw new UnsupportedOperationException();
    }

    public DicomElement putDate(int tag, VR vr, Date val)
    {
        throw new UnsupportedOperationException();
    }

    public DicomElement putDates(int tag, VR vr, Date[] val)
    {
        throw new UnsupportedOperationException();
    }

    public DicomElement putDateRange(int tag, VR vr, DateRange val)
    {
        throw new UnsupportedOperationException();
    }

    public void clear()
    {
        throw new UnsupportedOperationException();
    }

    public DicomElement remove(int tag)
    {
        throw new UnsupportedOperationException();
    }

    public int resolveTag(int privateTag, String privateCreator)
    {
        return attrs.resolveTag(privateTag, privateCreator);
    }

    public int resolveTag(int privateTag, String privateCreator, boolean reserve)
    {
        return attrs.resolveTag(privateTag, privateCreator, false);
    }

    public void cacheGet(boolean cached)
    {
        attrs.cacheGet(cached);
    }

    public void cachePut(boolean cached)
    {
        attrs.cachePut(cached);
    }

    public void bigEndian(boolean bigEndian)
    {
        attrs.bigEndian(bigEndian);
    }

    public void shareElements()
    {
        throw new UnsupportedOperationException();
    }

    public VR vrOf(int tag)
    {
        return attrs.vrOf(tag);
    }

    public String nameOf(int tag)
    {
        return attrs.nameOf(tag);
    }

    public DicomElement putFragments(int tag, VR vr, boolean bigEndian,
            int capacity)
    {
        throw new UnsupportedOperationException();
    }

    public DicomElement putFragments(int tag, VR vr, boolean bigEndian)
    {
        throw new UnsupportedOperationException();
    }

    public DicomElement putSequence(int tag, int capacity)
    {
        throw new UnsupportedOperationException();
    }

    public DicomElement putSequence(int tag)
    {
        throw new UnsupportedOperationException();
    }

    public void initFileMetaInformation(String tsuid)
    {
        throw new UnsupportedOperationException();
    }

    public void initFileMetaInformation(String cuid, String iuid, String tsuid)
    {
        throw new UnsupportedOperationException();
    }
}
