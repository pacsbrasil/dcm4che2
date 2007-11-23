package org.dcm4che2.data;

import junit.framework.TestCase;

public class TagTest extends TestCase {
    public void testToTagPathOneTag() {
        int[] tagPath = Tag.toTagPath("0020000D");
        assertEquals(1, tagPath.length);
        assertEquals(0x0020000D, tagPath[0]);
    }

    public void testToTagPathHierarchy() {
        int[] tagPath = Tag.toTagPath("000100020/00100030/00100040");
        assertEquals(5, tagPath.length);
        assertEquals(0x00100020, tagPath[0]);
        assertEquals(0, tagPath[1]);
        assertEquals(0x00100030, tagPath[2]);
        assertEquals(0, tagPath[3]);
        assertEquals(0x00100040, tagPath[4]);
    }

    public void testToTagPathArray() {
        int[] tagPath = Tag.toTagPath("00100020[9]");
        assertEquals(2, tagPath.length);
        assertEquals(0x00100020, tagPath[0]);
        assertEquals(8, tagPath[1]);
    }

    public void testToTagPathTagName() {
        int[] tagPath = Tag.toTagPath("PatientID");
        assertEquals(1, tagPath.length);
        assertEquals(0x00100020, tagPath[0]);
    }

    public void testToTagPathFull() {
        int[] tagPath = Tag.toTagPath("00100020/PatientBirthDate[9]/0020000D");
        assertEquals(5, tagPath.length);
        assertEquals(0x00100020, tagPath[0]);
        assertEquals(0, tagPath[1]);
        assertEquals(0x00100030, tagPath[2]);
        assertEquals(8, tagPath[3]);
        assertEquals(0x0020000D, tagPath[4]);
        // TODO 2007-11-23 gunter.zeilinger Slashes cause a 0 to be emitted
        // unless preceded by an array index or when at the end of a string.
        // Should we add a trailing 0 to be more consistent? (rick.riemer)
    }

    public void testToTagPathNoSlashes() {
        // TODO 2007-11-23 gunter.zeilinger should this behavior remain
        // supported? (rick.riemer)
        int[] tagPath = Tag.toTagPath("00100020[9]0020000E");
        assertEquals(3, tagPath.length);
        assertEquals(0x00100020, tagPath[0]);
        assertEquals(8, tagPath[1]);
        assertEquals(0x0020000E, tagPath[2]);
    }

    public void testIncorrectTagName() {
        try {
            Tag.toTagPath("wrong");
            fail("expected " + IllegalArgumentException.class);
        } catch (IllegalArgumentException e) {
            assertEquals("Unknown Tag Name: wrong", e.getMessage());
        }
    }

    public void testInvalidArrayIndex() {
        try {
            Tag.toTagPath("0020000D[a]");
            fail("expected " + IllegalArgumentException.class);
        } catch (IllegalArgumentException e) {
            assertEquals("For input string: \"a\"", e.getMessage());
        }
    }
}
