package org.dcm4che2.hp;

import java.util.List;

import org.dcm4che2.data.BasicDicomObject;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.VR;
import org.dcm4che2.junit.BaseTestCase;

public class HPDisplaySetTest extends BaseTestCase {

    private static final String CORONAL = 
            "1.000000\\0.000000\\0.000000\\0.000000\\0.000000\\-1.000000";
    private static final String SAGITAL = 
            "0.000000\\-1.000000\\0.000000\\0.000000\\0.000000\\-1.000000";
    private static final String AXIAL =
            "1.000000\\0.000000\\0.000000\\0.000000\\1.000000\\0.000000";

    private final DicomObject CT_CORONAL = 
            image("ORIGINAL\\PRIMARY\\LOCALIZER", "CT", "HEAD", 
                    "-248.187592\\0.000000\\30.000000", CORONAL);
    private final DicomObject CT_SAGITAL = 
            image("ORIGINAL\\PRIMARY\\LOCALIZER", "CT", "HEAD", 
                    "0.000000\\248.187592\\30.000000", SAGITAL);
    private final DicomObject CT_AXIAL1 = 
            image("ORIGINAL\\PRIMARY\\AXIAL", "CT", "HEAD", 
                    "-158.135818\\-179.035812\\-59.200001", AXIAL);
    private final DicomObject CT_AXIAL2 =
            image("ORIGINAL\\PRIMARY\\AXIAL", "CT", "HEAD", 
                    "-158.135818\\-179.035812\\-29.200001", AXIAL);
    private final DicomObject MR_AXIAL1 =
            image("ORIGINAL\\PRIMARY", "MR", "HEAD", 
                    "-120.000000\\-116.699997\\-19.799999", AXIAL);
    private final DicomObject MR_AXIAL2 = 
            image("ORIGINAL\\PRIMARY", "MR", "HEAD", 
                    "-120.000000\\-116.699997\\-5.800000", AXIAL);

    private static DicomObject image(String type, String modality, String bodyPart,
            String position, String orientation) {
        DicomObject o = new BasicDicomObject();
        o.putString(Tag.ImageType, VR.CS, type);
        o.putString(Tag.Modality, VR.CS, modality);
        o.putString(Tag.BodyPartExamined, VR.CS, bodyPart);
        o.putString(Tag.ImagePositionPatient, VR.DS, position);
        o.putString(Tag.ImageOrientationPatient, VR.DS, orientation);
        return o;
    }

    
    public static void main(String[] args) {
        junit.textui.TestRunner.run(HPDisplaySetTest.class);
    }

    public HPDisplaySetTest(String name) {
        super(name);
    }

    public final void testNeurosurgeryPlan() throws Exception {
        HangingProtocol neurosurgeryPlan = 
                new HangingProtocol(loadXML("NeurosurgeryPlan.xml"));
        
        assertEquals(4, neurosurgeryPlan.countPresentationGroups());
        List ctOnlyDisplay = 
                neurosurgeryPlan.getDisplaySetsOfPresentationGroup(1);
        assertEquals(5, ctOnlyDisplay.size());
        List mrOnlyDisplay = 
            neurosurgeryPlan.getDisplaySetsOfPresentationGroup(2);
        assertEquals(5, mrOnlyDisplay.size());
        List mrctCombined = 
            neurosurgeryPlan.getDisplaySetsOfPresentationGroup(3);
        assertEquals(6, mrctCombined.size());
        List ctNewctOldCombined = 
            neurosurgeryPlan.getDisplaySetsOfPresentationGroup(4);
        assertEquals(6, ctNewctOldCombined.size());
        
        HPDisplaySet ds5 = (HPDisplaySet) ctOnlyDisplay.get(4);
        HPImageSet is2 = ds5.getImageSet();
        assertEquals(true, is2.contains(CT_CORONAL, 0));
        assertEquals(true, is2.contains(CT_SAGITAL, 0));
        assertEquals(true, is2.contains(CT_AXIAL1, 0));
        assertEquals(true, is2.contains(CT_AXIAL2, 0));
        assertEquals(false, is2.contains(MR_AXIAL1, 0));
        assertEquals(false, is2.contains(MR_AXIAL2, 0));
        assertEquals(false, ds5.contains(CT_CORONAL, 0));
        assertEquals(false, ds5.contains(CT_SAGITAL, 0));
        assertEquals(true, ds5.contains(CT_AXIAL1, 0));
        assertEquals(true, ds5.contains(CT_AXIAL2, 0));
        assertEquals(true, ds5.compare(CT_AXIAL1, 1, CT_AXIAL2, 1) > 0);

        HPDisplaySet ds10 = (HPDisplaySet) mrOnlyDisplay.get(4);
        HPImageSet is1 = ds10.getImageSet();
        assertEquals(false, is1.contains(CT_CORONAL, 0));
        assertEquals(false, is1.contains(CT_SAGITAL, 0));
        assertEquals(false, is1.contains(CT_AXIAL1, 0));
        assertEquals(false, is1.contains(CT_AXIAL2, 0));
        assertEquals(true, is1.contains(MR_AXIAL1, 0));
        assertEquals(true, is1.contains(MR_AXIAL2, 0));
        assertEquals(true, ds10.contains(MR_AXIAL1, 0));
        assertEquals(true, ds10.contains(MR_AXIAL2, 0));
        assertEquals(true, ds10.compare(MR_AXIAL1, 1, MR_AXIAL2, 1) > 0);
     }
}
