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
	private Object value_1;
	private int[] skeys;
	private Object[] svalues;

	
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

	public void flush() {
		skeys = null;
		svalues = null;
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
		if (key == -1) {
			if (value_1 == null)
				++count;
			value_1 = value;
			return;
		}
		flush();
		if (count > highWaterMark)
			rehash();
		putInternal(key, value);
	}

	public Object get(int key) {
		return key == 0 ? value0 : key == -1 ? value_1 : values[find(key)];
	}

	public Object remove(int key) {
		Object retval = null;
		if (key == 0) {
			if (value0 != null) {
				retval = value0;
				value0 = null;
				--count;
			}
		} else if (key == -1) {
			if (value_1 != null) {
				retval = value_1;
				value_1 = null;
				--count;
			}
		} else {
			final int index = find(key);
			if (values[index] != null) {
				retval = values[index];
				flush();
				keyList[index] = -1;
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
		return equals(value0, other.value0) && equals(value_1, other.value_1);
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
		if (value_1 != null) {
			h = 37 * h + value_1.hashCode();
		}
		return h;
	}

	public Object clone() throws CloneNotSupportedException {
		IntHashtable result = (IntHashtable) super.clone();
		values = (Object[]) values.clone();
		keyList = (int[]) keyList.clone();
		return result;
	}

	public boolean accept(Visitor visitor) {
		if (value0 != null) {
			if (!visitor.visit(0, value0))
				return false;
		}
		if (value_1 != null) {
			if (!visitor.visit(-1, value_1))
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
		count = 0;
		if (value0 != null)
			++count;
		if (value_1 != null)
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

	private void sort() {
		skeys = (int[]) keyList.clone();
		svalues = (Object[]) values.clone();
		sort(0, skeys.length);

	}

	private void sort(int off, int len) {
		// Insertion sort on smallest arrays
		if (len < 7) {
			for (int i = 0; i < skeys.length; i++)
				for (int j = i; j > off && skeys[j - 1] > skeys[j]; j--)
					swap(j, j - 1);
			return;
		}

		// Choose a partition element, v
		int m = off + (len >> 1); // Small arrays, middle element
		if (len > 7) {
			int l = off;
			int n = off + len - 1;
			if (len > 40) { // Big arrays, pseudomedian of 9
				int s = len / 8;
				l = med3(l, l + s, l + 2 * s);
				m = med3(m - s, m, m + s);
				n = med3(n - 2 * s, n - s, n);
			}
			m = med3(l, m, n); // Mid-size, med of 3
		}
		int v = skeys[m];

		// Establish Invariant: v* (<v)* (>v)* v*
		int a = off, b = a, c = off + len - 1, d = c;
		while (true) {
			while (b <= c && skeys[b] <= v) {
				if (skeys[b] == v)
					swap(a++, b);
				b++;
			}
			while (c >= b && skeys[c] >= v) {
				if (skeys[c] == v)
					swap(c, d--);
				c--;
			}
			if (b > c)
				break;
			swap(b++, c--);
		}

		// Swap partition elements back to middle
		int s, n = off + len;
		s = Math.min(a - off, b - a);
		vecswap(off, b - s, s);
		s = Math.min(d - c, n - d - 1);
		vecswap(b, n - s, s);

		// Recursively sort non-partition-elements
		if ((s = b - a) > 1)
			sort(off, s);
		if ((s = d - c) > 1)
			sort(n - s, s);
	}

	private void swap(int a, int b) {
		int t = skeys[a];
		skeys[a] = skeys[b];
		skeys[b] = t;
		Object v = svalues[a];
		svalues[a] = svalues[b];
		svalues[b] = v;
	}

	private void vecswap(int a, int b, int n) {
		for (int i = 0; i < n; i++, a++, b++)
			swap(a, b);
	}

	private int med3(int a, int b, int c) {
		return (skeys[a] < skeys[b] ? (skeys[b] < skeys[c] ? b
				: skeys[a] < skeys[c] ? c : a) : (skeys[b] > skeys[c] ? b
				: skeys[a] > skeys[c] ? c : a));
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
			if (start == -1) {
				if (end == -1)
					next = value_1;
				return;
			}
			if (skeys == null)
				sort();
			endIndex = Arrays.binarySearch(skeys, end != -1 ? end : -2);
			if (endIndex < 0) {
				endIndex = normalize(-endIndex - 2);				
			}
			if (end == -1 && value_1 != null) {
				endIndex = normalize(endIndex + 1);
			}
			index = Arrays.binarySearch(skeys, start != 0 ? start : 1);
			if (index < 0) {
				index = normalize(-index-1);
			}
			if (start == 0 && value0 != null) {
				next = value0;
				--index;
			} else {
				if (index != normalize(endIndex+1)) {
					next = svalues[index];
				}
			}
		}

		private int normalize(int index) {
			return (index + svalues.length) % svalues.length;
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
				index = normalize(index+1);
				next = svalues[index];
				if (next == null)
					next = value_1;
			}
			return v;
		}

		public void remove() {
			throw new UnsupportedOperationException();
		}

	}
}
