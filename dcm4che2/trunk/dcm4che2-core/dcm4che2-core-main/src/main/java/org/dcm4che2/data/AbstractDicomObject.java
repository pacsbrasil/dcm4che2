/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/

package org.dcm4che2.data;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.util.Date;
import java.util.Iterator;
import java.util.regex.Pattern;

import org.dcm4che2.util.DateUtils;

abstract class AbstractDicomObject implements DicomObject {
		
	protected Object writeReplace() throws ObjectStreamException {
		return new DicomObjectSerializer(this);
	}

	public void serializeElements(ObjectOutputStream oos)
			throws IOException {
		oos.writeObject(new ElementSerializer(this));
	}
	
	public void copyTo(final DicomObject dest) {
		accept(new Visitor(){
			public boolean visit(DicomElement attr) {
				dest.add(attr);
				return true;
			}});		
	}

	public boolean matches(final DicomObject keys, final boolean ignoreCaseOfPN) {
		return keys.accept(new Visitor(){
			public boolean visit(DicomElement test) {
				if (test.isNull()) // Universal Matching
					return true;
				final int tag = test.tag();
				DicomElement attr = get(tag);
				if (attr == null || attr.isNull())
					return true;	// Missing DicomElement (Value) match always
				
				final VR vr = test.vr();
				if (vr instanceof VR.Fragment)
					return true; 	// ignore OB,OW,OF,UN filter attrs

				if (vr == VR.SQ)
					return matchSQ(attr, test.getItem(), ignoreCaseOfPN);
				
				if (vr == VR.DA) {
					int tmTag = DA_TM.getTMTag(tag);
					return tmTag != 0 
							? matchRange(getDates(tag, tmTag), 
									keys.getDateRange(tag, tmTag)) 
							: matchRange(attr.getDates(cacheGet()), 
									test.getDateRange(keys.cacheGet()));
				}
				if (vr == VR.TM) {
					int daTag = DA_TM.getDATag(tag);
					return daTag != 0 && containsValue(daTag) 
							? true // considered by visit of daTag
							: matchRange(attr.getDates(cacheGet()), 
									test.getDateRange(keys.cacheGet()));
				}
				if (vr == VR.DT) {
					return matchRange(attr.getDates(cacheGet()), 
							test.getDateRange(keys.cacheGet()));
				}
				return matchValue(
						attr.getStrings(getSpecificCharacterSet(), cacheGet()),
						test.getPattern(keys.getSpecificCharacterSet(), 
								vr == VR.PN ? ignoreCaseOfPN : false,
								keys.cacheGet()));				
			}});		
		
	}
	
	private boolean matchValue(String[] value, Pattern pattern) {
		for (int i = 0; i < value.length; i++) {
			if (pattern.matcher(value[i]).matches())
				return true;
		}
		return false;
	}

	private boolean matchRange(Date[] dates, DateRange dateRange) {
		for (int i = 0; i < dates.length; i++) {
			if (matchRange(dates[i], dateRange.getStart(), dateRange.getEnd()))
				return true;
		}
		return false;
	}

	private boolean matchRange(Date date, Date start, Date end) {
		if (start != null && start.after(date))
			return false;
		if (end != null && end.before(date))
			return false;
		return true;
	}

	private boolean matchSQ(DicomElement sq, DicomObject keys, boolean ignoreCaseOfPN) {
		if (keys.isEmpty())
			return true;
		for (int i = 0, n = sq.countItems(); i < n; i++) {
			if (!sq.getItem(i).matches(keys, ignoreCaseOfPN))
				return false;
		}
		return true;
	}

    public boolean isRoot() {
        return getParent() == null;
    }
    
	public boolean isEmpty() {
		return accept(new Visitor(){
			public boolean visit(DicomElement attr) {
				return false;
			}});
	}
	
	public int size() {
		final int[] count = { 0 };
		accept(new Visitor(){
			public boolean visit(DicomElement attr) {
				++count[0];
				return true;
			}});
		return count[0];
	}

	public Iterator commandIterator() {
		return iterator(0x00000000, 0x0000ffff);
	}
	
	public Iterator fileMetaInfoIterator() {
		return iterator(0x00020000, 0x0002ffff);
	}
	
	public Iterator datasetIterator() {
		return iterator(0x00030000, 0xffffffff);
	}
	
	public DicomObject command() {
		return subSet(0x00000000, 0x0000ffff);
	}
	
	public DicomObject dataset() {
		return subSet(0x00030000, 0xffffffff);		
	}
	
	public DicomObject fileMetaInfo() {
		return subSet(0x00020000, 0x0002ffff);
	}
	
	public DicomObject exclude(int[] tags) {
		return tags != null && tags.length > 0 ?
				new FilteredDicomObject.Exclude(this, tags) : this;
	}

	public DicomObject excludePrivate() {
		return new FilteredDicomObject.ExcludePrivate(this);
	}

	public DicomObject subSet(DicomObject filter) {
		return filter != null ?
				new FilteredDicomObject.FilterSet(this, filter) : null;
	}

	public DicomObject subSet(int fromTag, int toTag) {
		return new FilteredDicomObject.Range(this, fromTag, toTag);
	}

	public DicomObject subSet(int[] tags) {
		return tags != null && tags.length > 0 ?
				new FilteredDicomObject.Include(this, tags) : this;
	}
    
    public int vm(int tag) {
        DicomElement attr = get(tag);
        return attr != null ? attr.vm(getSpecificCharacterSet()) : -1;
    }

	public boolean containsValue(int tag) {
		DicomElement attr = get(tag);
		return attr != null && !attr.isNull();
	}

	public byte[] getBytes(int tag, boolean bigEndian) {
		return toBytes(get(tag), bigEndian);
	}

    private byte[] toBytes(DicomElement a, boolean bigEndian) {
        return a == null ? null : a.bigEndian(bigEndian).getBytes();
    }

	public DicomObject getItem(int tag) {
		DicomElement a = get(tag);
		return a == null && !a.isNull() ? null : a.getItem();
	}

	public int getInt(int tag) {
		return toInt(get(tag));
	}

    private int toInt(DicomElement a) {
        return a == null ? 0 : a.getInt(cacheGet());
    }

	public int[] getInts(int tag) {
		return toInts(get(tag));
	}

    private int[] toInts(DicomElement a) {
        return a == null ? null : a.getInts(cacheGet());
    }

	public float getFloat(int tag) {
		return toFloat(get(tag));
	}

    private float toFloat(DicomElement a) {
        return a == null ? 0f : a.getFloat(cacheGet());
    }

	public float[] getFloats(int tag) {
		return toFloats(get(tag));
	}

    private float[] toFloats(DicomElement a) {
        return a == null ? null : a.getFloats(cacheGet());
    }

	public double getDouble(int tag) {
		return toDouble(get(tag));
	}

    private double toDouble(DicomElement a) {
        return a == null ? 0. : a.getDouble(cacheGet());
    }

	public double[] getDoubles(int tag) {
		return toDoubles(get(tag));
	}

    private double[] toDoubles(DicomElement a) {
        return a == null ? null : a.getDoubles(cacheGet());
    }

	public String getString(int tag) {
		return toString(get(tag));
	}

    private String toString(DicomElement a) {
        return a == null ? null 
                : a.getString(getSpecificCharacterSet(), cacheGet());
    }

	public String[] getStrings(int tag) {
		return toStrings(get(tag));
	}

    private String[] toStrings(DicomElement a) {
        return a == null ? null
                : a.getStrings(getSpecificCharacterSet(), cacheGet());
    }

	public Date getDate(int tag) {
		return toDate(get(tag));
	}

    private Date toDate(DicomElement a) {
        return a == null ? null : a.getDate(cacheGet());
    }

	public Date[] getDates(int tag) {
		return toDates(get(tag));
	}

    private Date[] toDates(DicomElement a) {
        return a == null ? null : a.getDates(cacheGet());
    }

	public DateRange getDateRange(int tag) {
		DicomElement a = get(tag);
		return toDateRange(a);
	}

    private DateRange toDateRange(DicomElement a) {
        return a == null ? null : a.getDateRange(cacheGet());
    }

	public Date getDate(int daTag, int tmTag) {
		return DateUtils.toDateTime(getDate(daTag), getDate(tmTag));
	}

	public Date[] getDates(int daTag, int tmTag) {
		Date[] da = getDates(daTag);
		Date[] tm = getDates(tmTag);
		if (da == null)
			return null;
		if (tm != null) {
			for (int i = 0, n = Math.min(da.length, tm.length); i < n; ++i) {
				da[i] = DateUtils.toDateTime(da[i], tm[i]);
			}
		}
		return da;
	}
	
	public DateRange getDateRange(int daTag, int tmTag) {
		DateRange da = getDateRange(daTag);
		if (da == null) return null;
		DateRange tm = getDateRange(tmTag);
		if (tm == null) return da;
		return new DateRange(
				DateUtils.toDateTime(da.getStart(), tm.getStart()),
				DateUtils.toDateTime(da.getEnd(), tm.getEnd()));
	}

    public DicomElement get(int[] tagPath) {
        if ((tagPath.length & 1) == 0) {
            throw new IllegalArgumentException("tagPath.length: "
                    + tagPath.length);
        }
        final int last = tagPath.length - 1;
        final DicomObject item = getItem(tagPath, last);
        return item != null ? item.get(tagPath[last]) : null;        
    }
    
    public DicomElement get(String tagPath) {
        int endItemPath = tagPath.lastIndexOf('/');
        final DicomObject item = 
                endItemPath == -1 ? this :
                endItemPath == 0 ? getRoot() : 
                getItem(tagPath.substring(0, endItemPath));
        return item != null 
                ? item.get(parseTag(tagPath.substring(endItemPath+1)))
                : null;
    }

    private int parseTag(String tag) {
        return (int) Long.parseLong(tag, 16);
    }
    
	public DicomObject getItem(int[] itemPath) {
        if ((itemPath.length & 1) != 0) {
            throw new IllegalArgumentException("itemPath.length: "
                    + itemPath.length);
        }
        return getItem(itemPath, itemPath.length);
    }
    
    private DicomObject getItem(int[] itemPath, int pathLen) {
		DicomObject item = this;
		for (int i = 0; i < pathLen; ++i, ++i) {
			DicomElement sq = item.get(itemPath[i]);
			if (sq == null || !sq.hasItems() || sq.countItems() < itemPath[i+1])
				return null;
			item = sq.getItem(itemPath[i+1]);
		}
		return item;
	}

    public DicomObject getItem(String itemPath) {
        if (itemPath.length() == 0)
            return this;
        if (itemPath.equals("/"))
            return getRoot();
        String tagPath = itemPath;
        int index = 0;
        if (itemPath.endsWith("]")) {
            int endTagPath = tagPath.lastIndexOf('[');
            tagPath = itemPath.substring(0, endTagPath);
            index = Integer.parseInt(
                    itemPath.substring(endTagPath+1, itemPath.length() -1)) - 1;
        }
        DicomElement sq = get(tagPath);
        if (sq == null)
            return null;
        if (sq.countItems() <= index)
            return null;
        return sq.getItem(index);
    }
            
    public byte[] getBytes(int[] tagPath, boolean bigEndian) {
        return toBytes(get(tagPath), bigEndian);
	}

    public byte[] getBytes(String tagPath, boolean bigEndian) {
        return toBytes(get(tagPath), bigEndian);
    }
    
	public int getInt(int[] tagPath) {
        return toInt(get(tagPath));
	}
	
    public int getInt(String tagPath) {
        return toInt(get(tagPath));
    }
    
	public int[] getInts(int[] tagPath) {
        return toInts(get(tagPath));
	}
	
    public int[] getInts(String tagPath) {
        return toInts(get(tagPath));
    }
    
	public float getFloat(int[] tagPath) {
        return toFloat(get(tagPath));
	}
	
    public float getFloat(String tagPath) {
        return toFloat(get(tagPath));
    }
    
	public float[] getFloats(int[] tagPath) {
        return toFloats(get(tagPath));
	}
	
    public float[] getFloats(String tagPath) {
        return toFloats(get(tagPath));
    }
    
	public double getDouble(int[] tagPath) {
        return toDouble(get(tagPath));
	}
	
    public double getDouble(String tagPath) {
        return toDouble(get(tagPath));
    }
    
	public double[] getDoubles(int[] tagPath) {
        return toDoubles(get(tagPath));
	}
	
    public double[] getDoubles(String tagPath) {
        return toDoubles(get(tagPath));
    }
    
	public String getString(int[] tagPath) {
        return toString(get(tagPath));
	}
	
    public String getString(String tagPath) {
        return toString(get(tagPath));
    }
    
	public String[] getStrings(int[] tagPath) {
        return toStrings(get(tagPath));
	}
	
    public String[] getStrings(String tagPath) {
        return toStrings(get(tagPath));
    }
    
	public Date getDate(int[] tagPath) {
        return toDate(get(tagPath));
	}
	
    public Date getDate(String tagPath) {
        return toDate(get(tagPath));
    }
    
	public Date[] getDates(int[] tagPath) {
        return toDates(get(tagPath));
	}
	
    public Date[] getDates(String tagPath) {
        return toDates(get(tagPath));
    }
    
	public DateRange getDateRange(int[] tagPath) {
        return toDateRange(get(tagPath));
	}
	
    public DateRange getDateRange(String tagPath) {
        return toDateRange(get(tagPath));
    }
    
	public Date getDate(int[] itemPath, int daTag, int tmTag) {
        final DicomObject item = getItem(itemPath);
		return item != null ? item.getDate(daTag, tmTag) : null;		
	}
	
    public Date getDate(String itemPath, int daTag, int tmTag) {
        final DicomObject item = getItem(itemPath);
        return item != null ? item.getDate(daTag, tmTag) : null;        
    }
    
	public Date[] getDates(int[] itemPath, int daTag, int tmTag) {
        final DicomObject item = getItem(itemPath);
		return item != null ? item.getDates(daTag, tmTag) : null;				
	}
	
    public Date[] getDates(String itemPath, int daTag, int tmTag) {
        final DicomObject item = getItem(itemPath);
        return item != null ? item.getDates(daTag, tmTag) : null;               
    }
    
	public DateRange getDateRange(int[] itemPath, int daTag, int tmTag) {
        final DicomObject item = getItem(itemPath);
		return item != null ? item.getDateRange(daTag, tmTag) : null;				
	}

    public DateRange getDateRange(String itemPath, int daTag, int tmTag) {
        final DicomObject item = getItem(itemPath);
        return item != null ? item.getDateRange(daTag, tmTag) : null;               
    }
}
