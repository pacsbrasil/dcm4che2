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
import java.util.StringTokenizer;
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
		Attribute a = getAttribute(tag);
		return a == null ? null : a.bigEndian(bigEndian).getBytes();
	}

	public AttributeSet getItem(int tag) {
		Attribute a = getAttribute(tag);
		return a == null && !a.isNull() ? null : a.getItem();
	}

	public int getInt(int tag) {
		Attribute a = getAttribute(tag);
		return a == null ? 0 : a.getInt(cacheGet());
	}

	public int[] getInts(int tag) {
		Attribute a = getAttribute(tag);
		return a == null ? null : a.getInts(cacheGet());
	}

	public float getFloat(int tag) {
		Attribute a = getAttribute(tag);
		return a == null ? 0 : a.getFloat(cacheGet());
	}

	public float[] getFloats(int tag) {
		Attribute a = getAttribute(tag);
		return a == null ? null : a.getFloats(cacheGet());
	}

	public double getDouble(int tag) {
		Attribute a = getAttribute(tag);
		return a == null ? 0 : a.getDouble(cacheGet());
	}

	public double[] getDoubles(int tag) {
		Attribute a = getAttribute(tag);
		return a == null ? null : a.getDoubles(cacheGet());
	}

	public String getString(int tag) {
		Attribute a = getAttribute(tag);
		return a == null ? null : a.getString(getSpecificCharacterSet(), cacheGet());
	}

	public String[] getStrings(int tag) {
		Attribute a = getAttribute(tag);
		return a == null ? null : a
				.getStrings(getSpecificCharacterSet(), cacheGet());
	}

	public Date getDate(int tag) {
		Attribute a = getAttribute(tag);
		return a == null ? null : a.getDate(cacheGet());
	}

	public Date[] getDates(int tag) {
		Attribute a = getAttribute(tag);
		return a == null ? null : a.getDates(cacheGet());
	}

	public DateRange getDateRange(int tag) {
		Attribute a = getAttribute(tag);
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

    public Attribute getAttribute(int[] itemPath, int tag) {
        final AttributeSet item = getItem(itemPath);
        return item != null ? item.getAttribute(tag) : null;        
    }
    
    public Attribute getAttribute(String itemPath, int tag) {
        final AttributeSet item = getItem(itemPath);
        return item != null ? item.getAttribute(tag) : null;        
    }
    
	public AttributeSet getItem(int[] itemPath) {
		if ((itemPath.length & 1) != 0) {
			throw new IllegalArgumentException("itemPath.length: "
					+ itemPath.length);
		}
		AttributeSet item = this;
		for (int i = 0; i < itemPath.length; ++i, ++i) {
			Attribute sq = item.getAttribute(itemPath[i]);
			if (sq == null || !sq.hasItems() || sq.countItems() < itemPath[i+1])
				return null;
			item = sq.getItem(itemPath[i+1]);
		}
		return item;
	}

    public AttributeSet getItem(String str) {
        if (str.length() == 0)
            return this;
        if (str.startsWith("/"))
            return getRoot().getItem(str.substring(1));
        return getItem(parseItemPath(str));
    }
    
    private int[] parseItemPath(String str) {
        int n = 0;
        for (int i = 0; (i = str.indexOf(']', i) + 1) != 0; ++n);
        int[] itemPath = new int[n];
        int state = 0;
        int i = 0;
        try {
            for (StringTokenizer stk = new StringTokenizer(str, "/[]", true);
                    stk.hasMoreTokens();) {
                String tk = stk.nextToken();
                switch (state) {
                case 0:
                    itemPath[i++] = (int) Long.parseLong(tk, 16);
                    ++state;
                    break;
                case 1:
                    if (!"[".equals(tk))
                        throw new IllegalArgumentException(str);
                    ++state;
                    break;
                case 2:
                    itemPath[i++] = Integer.parseInt(tk);
                    ++state;
                    break;
                case 3:
                    if (!"]".equals(tk))
                        throw new IllegalArgumentException(str);
                    ++state;
                    break;
                case 4:
                    if (!"/".equals(tk))
                        throw new IllegalArgumentException(str);
                    state = 0;
                    break;
                }
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(str);
        } catch (IndexOutOfBoundsException e) {
            throw new IllegalArgumentException(str);
        }
        if (!(state == 4 && i == itemPath.length))
            throw new IllegalArgumentException(str);
        return itemPath;
    }
        
    public byte[] getBytes(int[] itemPath, int tag, boolean bigEndian) {
		final AttributeSet item = getItem(itemPath);
        return item != null ? item.getBytes(tag, bigEndian) : null;
	}

    public byte[] getBytes(String itemPath, int tag, boolean bigEndian) {
        final AttributeSet item = getItem(itemPath);
        return item != null ? item.getBytes(tag, bigEndian) : null;
    }
    
	public int getInt(int[] itemPath, int tag) {
        final AttributeSet item = getItem(itemPath);
		return item != null ? item.getInt(tag) : 0;		
	}
	
    public int getInt(String itemPath, int tag) {
        final AttributeSet item = getItem(itemPath);
        return item != null ? item.getInt(tag) : 0;     
    }
    
	public int[] getInts(int[] itemPath, int tag) {
        final AttributeSet item = getItem(itemPath);
		return item != null ? item.getInts(tag) : null;		
	}
	
    public int[] getInts(String itemPath, int tag) {
        final AttributeSet item = getItem(itemPath);
        return item != null ? item.getInts(tag) : null;     
    }
    
	public float getFloat(int[] itemPath, int tag) {
        final AttributeSet item = getItem(itemPath);
		return item != null ? item.getFloat(tag) : 0;		
	}
	
    public float getFloat(String itemPath, int tag) {
        final AttributeSet item = getItem(itemPath);
        return item != null ? item.getFloat(tag) : 0;       
    }
    
	public float[] getFloats(int[] itemPath, int tag) {
        final AttributeSet item = getItem(itemPath);
		return item != null ? item.getFloats(tag) : null;				
	}
	
    public float[] getFloats(String itemPath, int tag) {
        final AttributeSet item = getItem(itemPath);
        return item != null ? item.getFloats(tag) : null;               
    }
    
	public double getDouble(int[] itemPath, int tag) {
        final AttributeSet item = getItem(itemPath);
		return item != null ? item.getDouble(tag) : 0;				
	}
	
    public double getDouble(String itemPath, int tag) {
        final AttributeSet item = getItem(itemPath);
        return item != null ? item.getDouble(tag) : 0;              
    }
    
	public double[] getDoubles(int[] itemPath, int tag) {
        final AttributeSet item = getItem(itemPath);
		return item != null ? item.getDoubles(tag) : null;				
	}
	
    public double[] getDoubles(String itemPath, int tag) {
        final AttributeSet item = getItem(itemPath);
        return item != null ? item.getDoubles(tag) : null;              
    }
    
	public String getString(int[] itemPath, int tag) {
        final AttributeSet item = getItem(itemPath);
		return item != null ? item.getString(tag) : null;				
	}
	
    public String getString(String itemPath, int tag) {
        final AttributeSet item = getItem(itemPath);
        return item != null ? item.getString(tag) : null;               
    }
    
	public String[] getStrings(int[] itemPath, int tag) {
        final AttributeSet item = getItem(itemPath);
		return item != null ? item.getStrings(tag) : null;				
	}
	
    public String[] getStrings(String itemPath, int tag) {
        final AttributeSet item = getItem(itemPath);
        return item != null ? item.getStrings(tag) : null;              
    }
    
	public Date getDate(int[] itemPath, int tag) {
        final AttributeSet item = getItem(itemPath);
		return item != null ? item.getDate(tag) : null;				
	}
	
    public Date getDate(String itemPath, int tag) {
        final AttributeSet item = getItem(itemPath);
        return item != null ? item.getDate(tag) : null;             
    }
    
	public Date[] getDates(int[] itemPath, int tag) {
        final AttributeSet item = getItem(itemPath);
		return item != null ? item.getDates(tag) : null;				
	}
	
    public Date[] getDates(String itemPath, int tag) {
        final AttributeSet item = getItem(itemPath);
        return item != null ? item.getDates(tag) : null;                
    }
    
	public DateRange getDateRange(int[] itemPath, int tag) {
        final AttributeSet item = getItem(itemPath);
		return item != null ? item.getDateRange(tag) : null;				
	}
	
    public DateRange getDateRange(String itemPath, int tag) {
        final AttributeSet item = getItem(itemPath);
        return item != null ? item.getDateRange(tag) : null;                
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
