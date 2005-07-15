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

abstract class AbstractAttributeSet implements AttributeSet {
		
	protected Object writeReplace() throws ObjectStreamException {
		return new AttributeSetSerializer(this);
	}

	public void serializeAttributes(ObjectOutputStream oos)
			throws IOException {
		oos.writeObject(new AttributeSerializer(this));
	}
	
	public void copyTo(final AttributeSet dest) {
		accept(new Visitor(){
			public boolean visit(Attribute attr) {
				dest.addAttribute(attr);
				return true;
			}});		
	}

	public boolean matches(final AttributeSet keys, final boolean ignoreCaseOfPN) {
		return keys.accept(new Visitor(){
			public boolean visit(Attribute test) {
				if (test.isNull()) // Universal Matching
					return true;
				final int tag = test.tag();
				Attribute attr = getAttribute(tag);
				if (attr == null || attr.isNull())
					return true;	// Missing Attribute (Value) match always
				
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

	private boolean matchSQ(Attribute sq, AttributeSet keys, boolean ignoreCaseOfPN) {
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
			public boolean visit(Attribute attr) {
				return false;
			}});
	}
	
	public int size() {
		final int[] count = { 0 };
		accept(new Visitor(){
			public boolean visit(Attribute attr) {
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
	
	public AttributeSet command() {
		return subSet(0x00000000, 0x0000ffff);
	}
	
	public AttributeSet dataset() {
		return subSet(0x00030000, 0xffffffff);		
	}
	
	public AttributeSet fileMetaInfo() {
		return subSet(0x00020000, 0x0002ffff);
	}
	
	public AttributeSet exclude(int[] tags) {
		return tags != null && tags.length > 0 ?
				new FilteredAttributeSet.Exclude(this, tags) : this;
	}

	public AttributeSet excludePrivate() {
		return new FilteredAttributeSet.ExcludePrivate(this);
	}

	public AttributeSet subSet(AttributeSet filter) {
		return filter != null ?
				new FilteredAttributeSet.FilterSet(this, filter) : null;
	}

	public AttributeSet subSet(int fromTag, int toTag) {
		return new FilteredAttributeSet.Range(this, fromTag, toTag);
	}

	public AttributeSet subSet(int[] tags) {
		return tags != null && tags.length > 0 ?
				new FilteredAttributeSet.Include(this, tags) : this;
	}

	public boolean containsValue(int tag) {
		Attribute attr = getAttribute(tag);
		return attr != null && !attr.isNull();
	}

	public byte[] getBytes(int tag, boolean bigEndian) {
		return toBytes(getAttribute(tag), bigEndian);
	}

    private byte[] toBytes(Attribute a, boolean bigEndian) {
        return a == null ? null : a.bigEndian(bigEndian).getBytes();
    }

	public AttributeSet getItem(int tag) {
		Attribute a = getAttribute(tag);
		return a == null && !a.isNull() ? null : a.getItem();
	}

	public int getInt(int tag) {
		return toInt(getAttribute(tag));
	}

    private int toInt(Attribute a) {
        return a == null ? 0 : a.getInt(cacheGet());
    }

	public int[] getInts(int tag) {
		return toInts(getAttribute(tag));
	}

    private int[] toInts(Attribute a) {
        return a == null ? null : a.getInts(cacheGet());
    }

	public float getFloat(int tag) {
		return toFloat(getAttribute(tag));
	}

    private float toFloat(Attribute a) {
        return a == null ? 0f : a.getFloat(cacheGet());
    }

	public float[] getFloats(int tag) {
		return toFloats(getAttribute(tag));
	}

    private float[] toFloats(Attribute a) {
        return a == null ? null : a.getFloats(cacheGet());
    }

	public double getDouble(int tag) {
		return toDouble(getAttribute(tag));
	}

    private double toDouble(Attribute a) {
        return a == null ? 0. : a.getDouble(cacheGet());
    }

	public double[] getDoubles(int tag) {
		return toDoubles(getAttribute(tag));
	}

    private double[] toDoubles(Attribute a) {
        return a == null ? null : a.getDoubles(cacheGet());
    }

	public String getString(int tag) {
		return toString(getAttribute(tag));
	}

    private String toString(Attribute a) {
        return a == null ? null 
                : a.getString(getSpecificCharacterSet(), cacheGet());
    }

	public String[] getStrings(int tag) {
		return toStrings(getAttribute(tag));
	}

    private String[] toStrings(Attribute a) {
        return a == null ? null
                : a.getStrings(getSpecificCharacterSet(), cacheGet());
    }

	public Date getDate(int tag) {
		return toDate(getAttribute(tag));
	}

    private Date toDate(Attribute a) {
        return a == null ? null : a.getDate(cacheGet());
    }

	public Date[] getDates(int tag) {
		return toDates(getAttribute(tag));
	}

    private Date[] toDates(Attribute a) {
        return a == null ? null : a.getDates(cacheGet());
    }

	public DateRange getDateRange(int tag) {
		Attribute a = getAttribute(tag);
		return toDateRange(a);
	}

    private DateRange toDateRange(Attribute a) {
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

    public Attribute getAttribute(int[] tagPath) {
        if ((tagPath.length & 1) == 0) {
            throw new IllegalArgumentException("tagPath.length: "
                    + tagPath.length);
        }
        final int last = tagPath.length - 1;
        final AttributeSet item = getItem(tagPath, last);
        return item != null ? item.getAttribute(tagPath[last]) : null;        
    }
    
    public Attribute getAttribute(String tagPath) {
        int endItemPath = tagPath.lastIndexOf('/');
        final AttributeSet item = 
                endItemPath == -1 ? this :
                endItemPath == 0 ? getRoot() : 
                getItem(tagPath.substring(0, endItemPath));
        return item != null 
                ? item.getAttribute(parseTag(tagPath.substring(endItemPath+1)))
                : null;
    }

    private int parseTag(String tag) {
        return (int) Long.parseLong(tag, 16);
    }
    
	public AttributeSet getItem(int[] itemPath) {
        if ((itemPath.length & 1) != 0) {
            throw new IllegalArgumentException("itemPath.length: "
                    + itemPath.length);
        }
        return getItem(itemPath, itemPath.length);
    }
    
    private AttributeSet getItem(int[] itemPath, int pathLen) {
		AttributeSet item = this;
		for (int i = 0; i < pathLen; ++i, ++i) {
			Attribute sq = item.getAttribute(itemPath[i]);
			if (sq == null || !sq.hasItems() || sq.countItems() < itemPath[i+1])
				return null;
			item = sq.getItem(itemPath[i+1]);
		}
		return item;
	}

    public AttributeSet getItem(String itemPath) {
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
        Attribute sq = getAttribute(tagPath);
        if (sq == null)
            return null;
        if (sq.countItems() <= index)
            return null;
        return sq.getItem(index);
    }
            
    public byte[] getBytes(int[] tagPath, boolean bigEndian) {
        return toBytes(getAttribute(tagPath), bigEndian);
	}

    public byte[] getBytes(String tagPath, boolean bigEndian) {
        return toBytes(getAttribute(tagPath), bigEndian);
    }
    
	public int getInt(int[] tagPath) {
        return toInt(getAttribute(tagPath));
	}
	
    public int getInt(String tagPath) {
        return toInt(getAttribute(tagPath));
    }
    
	public int[] getInts(int[] tagPath) {
        return toInts(getAttribute(tagPath));
	}
	
    public int[] getInts(String tagPath) {
        return toInts(getAttribute(tagPath));
    }
    
	public float getFloat(int[] tagPath) {
        return toFloat(getAttribute(tagPath));
	}
	
    public float getFloat(String tagPath) {
        return toFloat(getAttribute(tagPath));
    }
    
	public float[] getFloats(int[] tagPath) {
        return toFloats(getAttribute(tagPath));
	}
	
    public float[] getFloats(String tagPath) {
        return toFloats(getAttribute(tagPath));
    }
    
	public double getDouble(int[] tagPath) {
        return toDouble(getAttribute(tagPath));
	}
	
    public double getDouble(String tagPath) {
        return toDouble(getAttribute(tagPath));
    }
    
	public double[] getDoubles(int[] tagPath) {
        return toDoubles(getAttribute(tagPath));
	}
	
    public double[] getDoubles(String tagPath) {
        return toDoubles(getAttribute(tagPath));
    }
    
	public String getString(int[] tagPath) {
        return toString(getAttribute(tagPath));
	}
	
    public String getString(String tagPath) {
        return toString(getAttribute(tagPath));
    }
    
	public String[] getStrings(int[] tagPath) {
        return toStrings(getAttribute(tagPath));
	}
	
    public String[] getStrings(String tagPath) {
        return toStrings(getAttribute(tagPath));
    }
    
	public Date getDate(int[] tagPath) {
        return toDate(getAttribute(tagPath));
	}
	
    public Date getDate(String tagPath) {
        return toDate(getAttribute(tagPath));
    }
    
	public Date[] getDates(int[] tagPath) {
        return toDates(getAttribute(tagPath));
	}
	
    public Date[] getDates(String tagPath) {
        return toDates(getAttribute(tagPath));
    }
    
	public DateRange getDateRange(int[] tagPath) {
        return toDateRange(getAttribute(tagPath));
	}
	
    public DateRange getDateRange(String tagPath) {
        return toDateRange(getAttribute(tagPath));
    }
    
	public Date getDate(int[] itemPath, int daTag, int tmTag) {
        final AttributeSet item = getItem(itemPath);
		return item != null ? item.getDate(daTag, tmTag) : null;		
	}
	
    public Date getDate(String itemPath, int daTag, int tmTag) {
        final AttributeSet item = getItem(itemPath);
        return item != null ? item.getDate(daTag, tmTag) : null;        
    }
    
	public Date[] getDates(int[] itemPath, int daTag, int tmTag) {
        final AttributeSet item = getItem(itemPath);
		return item != null ? item.getDates(daTag, tmTag) : null;				
	}
	
    public Date[] getDates(String itemPath, int daTag, int tmTag) {
        final AttributeSet item = getItem(itemPath);
        return item != null ? item.getDates(daTag, tmTag) : null;               
    }
    
	public DateRange getDateRange(int[] itemPath, int daTag, int tmTag) {
        final AttributeSet item = getItem(itemPath);
		return item != null ? item.getDateRange(daTag, tmTag) : null;				
	}

    public DateRange getDateRange(String itemPath, int daTag, int tmTag) {
        final AttributeSet item = getItem(itemPath);
        return item != null ? item.getDateRange(daTag, tmTag) : null;               
    }
}
