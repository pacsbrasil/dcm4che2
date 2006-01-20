package org.dicom.data;

public interface DicomObject
{
    /* VR codes */
    int AE = 0x4145;
    int AS = 0x4153;
    int AT = 0x4154;
    int CS = 0x4353;
    int DA = 0x4441;
    int DS = 0x4453;
    int DT = 0x4454;
    int FD = 0x4644;
    int FL = 0x464c;
    int IS = 0x4953;
    int LO = 0x4c4f;
    int LT = 0x4c54;
    int OB = 0x4f42;
    int OF = 0x4f46;
    int OW = 0x4f57;
    int PN = 0x504e;
    int SH = 0x5348;
    int SL = 0x534c;
    int SQ = 0x5351;
    int SS = 0x5353;
    int ST = 0x5354;
    int TM = 0x544d;
    int UI = 0x5549;
    int UL = 0x554c;
    int UN = 0x554E;
    int US = 0x5553;
    int UT = 0x5554;
    int AUTO = -1;

    /**
     * Resolves private tag. If the group number of the specified tag is odd
     * (= private tag) and privateCreator != null, searches for the first private
     * creator data element in (gggg,0010-00FF) which matches privateCreator,
     * and returns <code>ggggEEee</code> with <code>EE</code> the element number
     * of the matching private creator data element and <code>ee</code> the two
     * lower bytes of the element number of the specified tag.>
     * If no matching private creator data element in (gggg,0010-00FF) is found,
     * and reserve=<code>true</code>, the specified privateCreator is inserted
     * in the first unused private creator data element, and <code>ggggEEee</code>
     * with <code>EE</code> the element number the new inserted private creator
     * data element is returned. If reserve=<code>false</code>, <code>-1</code>
     * is returned.<br>
     * If the group number of the specified tag is even (= standard tag) or
     * privateCreator == null, tag is returned unmodified.
     * 
     * @param tag (group, element) as 8 byte integer: ggggeeee. 
     * @param privateCreator private creator identifier
     * @return resolved tag or <code>-1</code>, if no tags are reserved for
     *         privateCreator and reserve=<code>false</code>.
     */
    int resolveTag(int tag, String privateCreator, boolean reserve);

    /**
     * Resolve existing private tag. Invokes 
     * @link{resolveTag(int, String, boolean) resolveTag}(tag, privateCreator,
     * <code>false</code>).
     * 
     * @param tag (group, element) as 8 byte integer: ggggeeee. 
     * @param privateCreator private creator identifier
     * @return resolved tag or <code>-1</code>, if no tags are reserved for
     *         privateCreator.
     */
    int resolveTag(int tag, String privateCreator);

}
