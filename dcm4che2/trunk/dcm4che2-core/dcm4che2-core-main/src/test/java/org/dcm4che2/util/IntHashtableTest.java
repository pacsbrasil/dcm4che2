package org.dcm4che2.util;

import java.util.Iterator;

import junit.framework.TestCase;

public class IntHashtableTest extends TestCase {

	private IntHashtable table;

	public static void main(String[] args) {
		junit.textui.TestRunner.run(IntHashtableTest.class);
	}

	public IntHashtableTest(String arg0) {
		super(arg0);
	}

	protected void setUp() throws Exception {
		this.table = new IntHashtable();
		for (int i = -10; i < 10; i++) {
			table.put(i, new Integer(i));
		}
	}

	public final void testPut() {
		for (int i = -100; i < 100; i++) {
			table.put(i, new Integer(i));
		}
		assertEquals(200, table.size());
	}

	public final void testGet() {
		for (int i = -10; i < 10; i++) {
			assertEquals(new Integer(i), table.get(i));
		}
		assertNull(table.get(11));
		assertNull(table.get(-11));
	}

	public final void testRemove() {
		for (int i = -10; i < 0; i++) {
			assertEquals(new Integer(i), table.remove(i));
		}
		assertEquals(10, table.size());
		for (int i = -10; i < 0; i++) {
			assertNull(table.get(i));
		}
		for (int i = 0; i < 10; i++) {
			assertEquals(new Integer(i), table.get(i));
		}
		for (int i = 0; i < 10; i++) {
			assertEquals(new Integer(i), table.remove(i));
		}
		assertEquals(true, table.isEmpty());
		assertEquals(0, table.size());
		for (int i = -10; i < 10; i++) {
			assertNull(table.get(i));
		}
	}

	public final void testAccept() {
		table.accept(new IntHashtable.Visitor() {
			public boolean visit(int key, Object value) {
				assertEquals(new Integer(key), value);
				return true;
			}});
	}

	public final void testIterator() {
		doTestIterator(0, -1);
		doTestIterator(1, -2);
		doTestIterator(0, -3);
		doTestIterator(2, -1);
		doTestIterator(9, -10);
		doTestIterator(10, -10);
		doTestIterator(10, -11);
		doTestIterator(0, 1);
		doTestIterator(-2, -1);
		doTestIterator(0, 0);
		doTestIterator(1, 1);
		doTestIterator(-1, -1);
		doTestIterator(10, 10);
	}

	private void doTestIterator(int start, int end) {
		Iterator itr = table.iterator(start, end);
		if (start >= 0) {
			for (int i = start, n = (end >= 0 && end < 10) ? end : 9; i <= n; i++) {
				assertEquals(true, itr.hasNext());
				assertEquals(new Integer(i), itr.next());
			}
		}
		if (end < 0) {
			for (int i = (start > -10 && start < 0) ? start : -10; i <= end; i++) {
				assertEquals(true, itr.hasNext());
				assertEquals(new Integer(i), itr.next());
			}
		}
		assertEquals(false, itr.hasNext());		
	}
}
