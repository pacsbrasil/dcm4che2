/*$Id$*/
/*****************************************************************************
 *                                                                           *
 *  Copyright (c) 2002 by TIANI MEDGRAPH AG                                  *
 *                                                                           *
 *  This file is part of dcm4che.                                            *
 *                                                                           *
 *  This library is free software; you can redistribute it and/or modify it  *
 *  under the terms of the GNU Lesser General Public License as published    *
 *  by the Free Software Foundation; either version 2 of the License, or     *
 *  (at your option) any later version.                                      *
 *                                                                           *
 *  This library is distributed in the hope that it will be useful, but      *
 *  WITHOUT ANY WARRANTY; without even the implied warranty of               *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU        *
 *  Lesser General Public License for more details.                          *
 *                                                                           *
 *  You should have received a copy of the GNU Lesser General Public         *
 *  License along with this library; if not, write to the Free Software      *
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA  *
 *                                                                           *
 *****************************************************************************/

package tiani.dcm4che.data;

import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmElement;
import org.dcm4che.data.DcmHandler;
import org.dcm4che.data.FileFormat;

import java.io.InputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;
import javax.imageio.stream.ImageInputStream;

import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0.0
 */
abstract class DatasetView extends BaseDatasetImpl implements Dataset {
    
    protected final BaseDatasetImpl backend;
    
    /** Creates a new instance of DatasetView */
    public DatasetView(BaseDatasetImpl backend) {
        this.backend = backend;
    }
        
    protected abstract boolean filter(int tag);
    
    public DcmElement get(int tag) {
        return filter(tag) ? backend.get(tag) : null;
    }
    
    protected int getMaxGroupCount() {
        return backend.getMaxGroupCount();
    }        

    public int size() {
        int count = 0;
        for (Iterator iter = iterator(); iter.hasNext(); ++count)
            ; //no op
        return count;
    }
    
    public boolean isEmpty() {
        return size() == 0;
    }

    public Charset getCharset() {
        return backend.getCharset();
    }
    
    public Dataset getParent() {
        return backend.getParent();
    }
    
    public long getItemOffset() {
        return backend.getItemOffset();
    }

    public DcmHandler getDcmHandler() {
        throw new UnsupportedOperationException();
    }

    public DefaultHandler getSAXHandler() {
        throw new UnsupportedOperationException();
    }
    
    protected DcmElement set(DcmElement newElem) {
        throw new UnsupportedOperationException();
    }
    
    public DcmElement remove(int tag) {
        throw new UnsupportedOperationException();
    }
    
    public void clear() {
        throw new UnsupportedOperationException();
    }

    public Dataset setItemOffset(long itemOffset) {
        throw new UnsupportedOperationException();
    }
    
    public void read(InputStream in, FileFormat format, int stopTag)
            throws IOException {
        throw new UnsupportedOperationException();
    }    
    
    public void read(ImageInputStream iin, FileFormat format, int stopTag)
            throws IOException {
        throw new UnsupportedOperationException();
    }

    static final class Selection extends DatasetView {
        private final int[] inclTags;
        Selection(BaseDatasetImpl backend, int[] inclTags) {
            super(backend);
            this.inclTags = (int[])inclTags.clone();
            Arrays.sort(this.inclTags);
        }
        protected boolean filter(int tag) {
            return  Arrays.binarySearch(inclTags, tag) >= 0;
        }
        public Iterator iterator() {
            return new ViewIterator();
        }
        private final class ViewIterator implements Iterator {
            int cur = 0;
            DcmElement next = null;
            ViewIterator() {
                findNext();
            }
            void findNext() {
                while (cur < inclTags.length) {
                    next = Selection.this.backend.get(inclTags[cur++]);
                    if (next != null) {
                        return;
                    }
                }
                next = null;
            }
            public boolean hasNext() {
                return next != null;
            }
            public Object next() {
                if (next == null) {
                    throw new NoSuchElementException();
                }
                DcmElement retval = next;
                findNext();
                return retval;
            }
            public void remove() {
                throw new UnsupportedOperationException();
            }
        }
    }
 
    static final class Exclusion extends DatasetView {
        private int[] exclTags = {};
        private long minTag;
        private long maxTag;
        Exclusion(BaseDatasetImpl backend, int minTag, int maxTag,
                int[] exclTags) {
            super(backend);
            if (exclTags != null) {
                this.exclTags = (int[])exclTags.clone();
                Arrays.sort(this.exclTags);
            }
            this.minTag = minTag & 0xFFFFFFFFL;
            this.maxTag = maxTag & 0xFFFFFFFFL;
        }
        protected boolean filter(int tag) {
            long ltag = tag & 0xFFFFFFFFL;
            return ltag >= minTag && ltag <= maxTag
                    && Arrays.binarySearch(exclTags, tag) < 0;
        }
        public Iterator iterator() {
            return new ViewIterator();
        }
        private final class ViewIterator implements Iterator {
            Iterator backendIter = Exclusion.this.backend.iterator();
            DcmElement next = null;
            ViewIterator() {
                findNext();
            }
            void findNext() {
                while (backendIter.hasNext()) {
                    next = (DcmElement)backendIter.next();
                    if (filter(next.tag())) {
                        return;
                    }
                }
                next = null;
            }
            public boolean hasNext() {
                return next != null;
            }
            public Object next() {
                if (next == null) {
                    throw new NoSuchElementException();
                }
                DcmElement retval = next;
                findNext();
                return retval;
            }
            public void remove() {
                throw new UnsupportedOperationException();
            }
        }
    } 
}
