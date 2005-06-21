package org.dcm4che2.util;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class IntHashtableTest extends TestCase {

	public static void main(String[] args) {
		junit.textui.TestRunner.run(suite());
	}
	
	public IntHashtableTest(String testName) {
		super(testName);
	}

    public static Test suite() {
        return new TestSuite( IntHashtableTest.class );
    }
	
	public void testRehash() {
		IntHashtable table = new IntHashtable();
		for (int i = -100; i < 100; i++) {
			table.put(i, new Integer(i));
		}
		assertEquals(200, table.size());
		for (int i = -100; i < 100; i++) {
			assertEquals(i, ((Integer) table.get(i)).intValue());
		}	
		for (int i = -100; i < 100; i++) {
			table.remove(i);
		}
		assertEquals(true, table.isEmpty());
	}

}
