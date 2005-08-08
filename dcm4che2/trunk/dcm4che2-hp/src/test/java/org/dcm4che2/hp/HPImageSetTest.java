package org.dcm4che2.hp;

import org.dcm4che2.data.BasicDicomObject;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.VR;
import org.dcm4che2.junit.BaseTestCase;

public class HPImageSetTest extends BaseTestCase {

    public static void main(String[] args) {
        junit.textui.TestRunner.run(HPImageSetTest.class);
    }

    public HPImageSetTest(String name) {
        super(name);
    }

    public final void testContains1() throws Exception {
        HangingProtocol hp = new HangingProtocol(loadXML("hp1.xml"));
        assertEquals(2, hp.countImageSets());
        HPImageSet is1 = hp.getImageSet(1);
        HPImageSet is2 = hp.getImageSet(2);
        DicomObject o = new BasicDicomObject();
        assertEquals(false, is1.contains(o, 0));
        o.putString(Tag.Modality, VR.CS, "CT");
        assertEquals(true, is1.contains(o, 0));
        assertEquals(true, is2.contains(o, 0));
        o.putString(Tag.Modality, VR.CS, "MR");
        assertEquals(false, is2.contains(o, 0));
    }

}
