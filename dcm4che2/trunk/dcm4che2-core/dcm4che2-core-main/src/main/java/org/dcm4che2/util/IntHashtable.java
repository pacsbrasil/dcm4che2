/*
 * @(#)IntHashtable.java	1.11 03/12/19
 *
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * 
 * Modified by gunter zeilinger to use objects as values and enable
 * usage of Integer.MIN_VALUE and Integer.MIN_VALUE+1 as key values.
 */

/*
 * (C) Copyright Taligent, Inc. 1996,1997 - All Rights Reserved
 * (C) Copyright IBM Corp. 1996, 1997 - All Rights Reserved
 * (C) Copyright Sun Microsystems, Inc. 2004 - All Rights Reserved
 */

package org.dcm4che2.util;

/** Simple internal class for doing hash mapping. Much, much faster than the
 * standard Hashtable for integer to integer mappings,
 * and doesn't require object creation.<br>
 */
public class IntHashtable {
	
	public IntHashtable () {
        initialize(3);
    }

    public IntHashtable (int initialSize) {
        initialize(leastGreaterPrimeIndex((int)(initialSize/HIGH_WATER_FACTOR)));
    }

    public int size() {
        return count;
    }

    public boolean isEmpty() {
        return count == 0;
    }

    public void put(int key, Object value) {
		// Make sure the value is not null
		if (value == null) {
		    throw new NullPointerException();
		}
		if (key == EMPTY) {
			valueEMPTY = updateCount(valueEMPTY, value);
			return;
		}
		if (key == DELETED) {
			valueDELETED = updateCount(valueDELETED, value);
			return;
		}
        if (count > highWaterMark) {
            rehash();
        }
		putInternal(key, value);
    }

    private Object updateCount(Object oldValue, Object value) {
		if (oldValue == null)
			++count;
		if (value == null)
			--count;
		return value;
	}

	public Object get(int key) {
		return key == EMPTY ? valueEMPTY : key == DELETED ? valueDELETED
				: values[find(key)];
	}

    public void remove(int key) {
		if (key == EMPTY) {
			valueEMPTY = updateCount(valueEMPTY, null);
			return;
		}
		if (key == DELETED) {
			valueDELETED = updateCount(valueDELETED, null);
			return;
		}
        int index = find(key);
		keyList[index] = DELETED;
        if (values[index] != null) {
            values[index] = null;
            --count;
            if (count < lowWaterMark) {
                rehash();
            }
        }
    }

	public interface Visitor {
		
		void visit(int key, Object value) throws Exception;

	}

	public void accept(Visitor visitor) throws Exception {
		if (valueEMPTY != null) {
			visitor.visit(EMPTY, valueEMPTY);
		}
		if (valueDELETED != null) {
			visitor.visit(DELETED, valueDELETED);
		}
		for (int i = 0; i < keyList.length; i++) {
			Object v = values[i];
			if (v != null) {
				visitor.visit(keyList[i], v);
			}
		}
	}
		
    public boolean equals (Object that) {
        if (that.getClass() != this.getClass()) return false;

        IntHashtable other = (IntHashtable) that;
        if (other.size() != count) {
                return false;
        }
        for (int i = 0; i < values.length; ++i) {
            Object v = values[i];
            if (v != null && !v.equals(other.get(keyList[i])))
                return false;
        }
        return equals(valueEMPTY, other.valueEMPTY)
        	&& equals(valueDELETED, other.valueDELETED);
    }

    private boolean equals(Object o1, Object o2) {
		return o1 == null ? o2 == null : o1.equals(o1);
	}

	public int hashCode() {
         if (count == 0)
            return 0;  // Returns zero

       int h = 0;
       for (int i = 0; i < values.length; i++) {
			Object v = values[i];
			if (v != null)
               h += keyList[i] ^ v.hashCode();
       }
	   if (valueEMPTY != null) {
		   h = 37 * h + valueEMPTY.hashCode();
	   }
	   if (valueDELETED != null) {
		   h = 37 * h + valueDELETED.hashCode();
	   }
	   return h;
    }

    public Object clone ()
                    throws CloneNotSupportedException {
        IntHashtable result = (IntHashtable) super.clone();
        values = (Object[]) values.clone();
        keyList = (int[])keyList.clone();
        return result;
    }

    // =======================PRIVATES============================
    // the tables have to have prime-number lengths. Rather than compute
    // primes, we just keep a table, with the current index we are using.
    private int primeIndex;

    // highWaterFactor determines the maximum number of elements before
    // a rehash. Can be tuned for different performance/storage characteristics.
    private static final float HIGH_WATER_FACTOR = 0.4F;
    private int highWaterMark;

    // lowWaterFactor determines the minimum number of elements before
    // a rehash. Can be tuned for different performance/storage characteristics.
    private static final float LOW_WATER_FACTOR = 0.0F;
    private int lowWaterMark;

    private int count;
	
    // we use two arrays to minimize allocations
    private Object[] values;
    private int[] keyList;
	private Object valueEMPTY;
	private Object valueDELETED;

    private static final int EMPTY = 0;
    private static final int DELETED = -1;

    private void initialize (int primeIndex) {
        if (primeIndex < 0) {
            primeIndex = 0;
        } else if (primeIndex >= PRIMES.length) {
            System.out.println("TOO BIG");
            primeIndex = PRIMES.length - 1;
            // throw new java.util.IllegalArgumentError();
        }
        this.primeIndex = primeIndex;
        int initialSize = PRIMES[primeIndex];
        values = new Object[initialSize];
        keyList = new int[initialSize];
        count = 0;
		if (valueEMPTY != null)
			++count;
		if (valueDELETED != null)
			++count;
        lowWaterMark = (int)(initialSize * LOW_WATER_FACTOR);
        highWaterMark = (int)(initialSize * HIGH_WATER_FACTOR);
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

    private void putInternal (int key, Object value) {
	        int index = find(key);
			keyList[index] = key;
	        values[index] = updateCount(values[index], value);
    }

    private int find (int key) {
		if (key == EMPTY || key == DELETED)
			throw new IllegalArgumentException("key: " + key);
        int firstDeleted = -1;  // assume invalid index
        int index = (key ^ 0x4000000) % keyList.length;
        if (index < 0) index = -index; // positive only
        int jump = 0; // lazy evaluate
        while (true) {
            int tableHash = keyList[index];
            if (tableHash == key) {                 // quick check
                return index;
            } else if (values[index] != null) {    // neither correct nor unused
                // ignore
            } else if (tableHash == EMPTY) {        // empty, end o' the line
                if (firstDeleted >= 0) {
		    index = firstDeleted;           // reset if had deleted slot
                }
                return index;
            } else if (firstDeleted < 0) {	    // remember first deleted
                    firstDeleted = index;
            }
            if (jump == 0) {                        // lazy compute jump
                jump = (key % (keyList.length - 1));
                if (jump < 0) jump = -jump;
                ++jump;
            }

            index = (index + jump) % keyList.length;
	    if (index == firstDeleted) {
		// We've searched all entries for the given key.
		return index;
	    }
        }
    }

    private static int leastGreaterPrimeIndex(int source) {
        int i;
        for (i = 0; i < PRIMES.length; ++i) {
            if (source < PRIMES[i]) {
                break;
            }
        }
        return (i == 0) ? 0 : (i - 1);
    }

    // This list is the result of buildList below. Can be tuned for different
    // performance/storage characteristics.
    private static final int[] PRIMES = {
        17, 37, 67, 131, 257,
        521, 1031, 2053, 4099, 8209, 16411, 32771, 65537,
        131101, 262147, 524309, 1048583, 2097169, 4194319, 8388617, 16777259,
        33554467, 67108879, 134217757, 268435459, 536870923, 1073741827, 2147483647
    };
}

