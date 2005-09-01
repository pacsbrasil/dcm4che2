package org.dcm4che2.hp;

import java.util.List;

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
        HangingProtocol hp = new HangingProtocol(loadXML("NeurosurgeryPlan.xml"));
        List list = hp.getImageSets();
        assertEquals(3, list.size());
        HPImageSet is1 = (HPImageSet) list.get(0);
        HPImageSet is2 = (HPImageSet) list.get(1);
        HPImageSet is3 = (HPImageSet) list.get(2);
        DicomObject o = new BasicDicomObject();
        assertEquals(false, is1.contains(o, 0));
        assertEquals(false, is2.contains(o, 0));
        assertEquals(false, is3.contains(o, 0));
        o.putString(Tag.BodyPartExamined, VR.CS, "HEAD");
        assertEquals(false, is1.contains(o, 0));
        assertEquals(false, is2.contains(o, 0));
        assertEquals(false, is3.contains(o, 0));
        o.putString(Tag.Modality, VR.CS, "CT");
        assertEquals(false, is1.contains(o, 0));
        assertEquals(true, is2.contains(o, 0));
        assertEquals(true, is3.contains(o, 0));
        o.putString(Tag.Modality, VR.CS, "MR");
        assertEquals(true, is1.contains(o, 0));
        assertEquals(false, is2.contains(o, 0));
        assertEquals(false, is3.contains(o, 0));
    }

}
