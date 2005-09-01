package org.dcm4che2.util;

import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class IntHashtable {

	private static final float HIGH_WATER_FACTOR = 0.4F;
	private static final float LOW_WATER_FACTOR = 0.0F;
	private static final int[] PRIMES = { 7, 17, 37, 67, 131, 257, 521, 1031,
		2053, 4099, 8209, 16411, 32771, 65537, 131101, 262147, 524309,
		1048583, 2097169, 4194319, 8388617, 16777259, 33554467, 67108879,
		134217757, 268435459, 536870923, 1073741827, 2147483647 };
	
	private static int primeIndex(int size) {
		for (int i = 0; i < PRIMES.length; ++i) {
			if (size < PRIMES[i]) {
				return i;
			}
		}
		return PRIMES.length - 1;
	}	

	public interface Visitor {
		boolean visit(int key, Object value);
	}

	private int primeIndex;
	private int highWaterMark;
	private int lowWaterMark;
	private int count;
	private int[] keyList;
	private Object[] values;
	private Object value0;
	private int[] sortedKeys;
	private boolean sorted;

	
	public IntHashtable() {
		initialize(3);
	}

	public IntHashtable(int initialSize) {
		initialize(primeIndex((int) (initialSize / HIGH_WATER_FACTOR)));
	}

	public int size() {
		return count;
	}

	public boolean isEmpty() {
		return count == 0;
	}
	
	public void clear() {
		count = 0;
		Arrays.fill(keyList, 0);
		Arrays.fill(values, null);
		value0 = null;
		sorted = false;
	}

	public void put(int key, Object value) {
		if (value == null) {
			throw new NullPointerException();
		}
		if (key == 0) {
			if (value0 == null)
				++count;
			value0 = value;
			return;
		}
		sorted = false;
		if (count > highWaterMark)
			rehash();
		putInternal(key, value);
	}

	public Object get(int key) {
		return key == 0 ? value0 : values[find(key)];
	}

	public Object remove(int key) {
		Object retval = null;
		if (key == 0) {
			if (value0 != null) {
				retval = value0;
				value0 = null;
				--count;
			}
		} else {
			final int index = find(key);
			if (values[index] != null) {
				retval = values[index];
				sorted = false;
				values[index] = null;
				--count;
				if (count < lowWaterMark) {
					rehash();
				}
			}
		}
		return retval;
	}

	public boolean equals(Object that) {
		if (that.getClass() != this.getClass())
			return false;

		IntHashtable other = (IntHashtable) that;
		if (other.size() != count) {
			return false;
		}
		for (int i = 0; i < values.length; ++i) {
			Object v = values[i];
			if (v != null && !v.equals(other.get(keyList[i])))
				return false;
		}
		return equals(value0, other.value0);// && equals(value_1, other.value_1);
	}

	private boolean equals(Object o1, Object o2) {
		return o1 == null ? o2 == null : o1.equals(o1);
	}

	public int hashCode() {
		if (count == 0)
			return 0;
		
		int h = 0;
		for (int i = 0; i < values.length; i++) {
			Object v = values[i];
			if (v != null)
				h += keyList[i] ^ v.hashCode();
		}
		if (value0 != null) {
			h = 37 * h + value0.hashCode();
		}
		return h;
	}

	public Object clone() throws CloneNotSupportedException {
		IntHashtable result = (IntHashtable) super.clone();
		values = (Object[]) values.clone();
		keyList = (int[]) keyList.clone();
		sortedKeys = sortedKeys != null ? (int[]) sortedKeys.clone() : null;
		return result;
	}

	public boolean accept(Visitor visitor) {
		if (value0 != null) {
			if (!visitor.visit(0, value0))
				return false;
		}
		for (int i = 0; i < keyList.length; i++) {
			Object v = values[i];
			if (v != null) {
				if (!visitor.visit(keyList[i], v))
					return false;
			}
		}
		return true;
	}

	public Iterator iterator(int start, int end) {
		return new Itr(start, end);
	}

	private void initialize(int primeIndex) {
		this.primeIndex = Math.min(Math.max(0, primeIndex), PRIMES.length - 1);
		int initialSize = PRIMES[primeIndex];
		values = new Object[initialSize];
		keyList = new int[initialSize];
		sortedKeys = null;
		count = 0;
		if (value0 != null)
			++count;
		lowWaterMark = (int) (initialSize * LOW_WATER_FACTOR);
		highWaterMark = (int) (initialSize * HIGH_WATER_FACTOR);
	}

	private void rehash() {
		Object[] oldValues = values;
		int[] oldkeyList = keyList;
		int newPrimeIndex = primeIndex;
		if (count > highWaterMark) {
			++newPrimeIndex;
		} else if (count < lowWaterMark) {
			newPrimeIndex -= 2;
		}
		initialize(newPrimeIndex);
		for (int i = oldkeyList.length - 1; i >= 0; --i) {
			Object value = oldValues[i];
			if (value != null) {
				putInternal(oldkeyList[i], value);
			}
		}
	}

	private void putInternal(int key, Object value) {
		int index = find(key);
		keyList[index] = key;
		if (values[index] == null)
			++count;
		values[index] = value;
	}

	private int find(int key) {
		int firstDeleted = -1;
		int i = (key ^ 0x4000000) % keyList.length;
		if (i < 0)
			i = -i;
		int d = 0;
		do {
			int hash = keyList[i];
			if (hash == key) {
				return i;
			} else {
				if (values[i] == null) {
					if (hash == 0) {
						return firstDeleted >= 0 ? firstDeleted : i;
					} else if (firstDeleted < 0) {
						firstDeleted = i;
					}
				}
			}
			if (d == 0) {
				d = (key % (keyList.length - 1));
				if (d < 0)
					d = -d;
				++d;
			}

			i = (i + d) % keyList.length;
		} while (i != firstDeleted);
		return i;
	}

	private final class Itr implements Iterator {
		int endIndex;
		int index;
		Object next;

		private Itr(int start, int end) {
			if ((start & 0xffffffffL) > (end & 0xffffffffL))
				throw new IllegalArgumentException(
						"start:" + start + ", end:" + end);
			if (isEmpty())
				return;
			if (end == 0) {
				if (start == 0)
					next = value0;
				return;
			}
			if (!sorted) {
				if (sortedKeys == null) {
					sortedKeys = new int[keyList.length];
				}
				System.arraycopy(keyList, 0, sortedKeys, 0, keyList.length);
				Arrays.sort(sortedKeys);
				sorted = true;
			}
			endIndex = Arrays.binarySearch(sortedKeys, end);
			if (endIndex < 0) {
				if (endIndex == -1)
					endIndex = sortedKeys.length - 1;
				else
					endIndex = -(endIndex + 1) - 1;
			}
			index = Arrays.binarySearch(sortedKeys, start != 0 ? start : 1);
			if (index < 0) {
				index = -(index + 1) % sortedKeys.length;				
			}
			if (start == 0 && value0 != null) {
				next = value0;
				--index;
			} else {
				if (index != incIndex(endIndex)) {
					next = get(sortedKeys[index]);
				}
			}
		}

		private int incIndex(int index) {
			return (index + 1) % sortedKeys.length;
		}

		public boolean hasNext() {
			return next != null;
		}

		public Object next() {
			if (next == null)
				throw new NoSuchElementException();
			Object v = next;
			if (index == endIndex) {
				next = null;
			} else {
				index = incIndex(index);
				next = get(sortedKeys[index]);
			}
			return v;
		}

		public void remove() {
			throw new UnsupportedOperationException();
		}

	}
}
