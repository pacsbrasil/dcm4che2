package org.dcm4chex.xds.query.match;

import java.util.List;

public class ClassificationMatch {

    private String name;
    private StringBuffer sbMatch = new StringBuffer();
    private ClassificationMatch(List codes, String name, String urn) {
        this.name = name;
        if ( codes == null || codes.size() < 1) return;
        sbMatch.append("( ").append(name).append(".classifiedobject = doc.id AND ").append(name);
        sbMatch.append(".classificationScheme ='").append(urn).append("' AND ").append(name);
        sbMatch.append(".nodeRepresentation IN ('").append(codes.get(0));
        for (int i = 1; i < codes.size(); i++) {
            sbMatch.append("', '").append(codes.get(i));
        }
        sbMatch.append("')");
    }
    
    public static ClassificationMatch getClassCodeMatch(List l) {
        return getCodeMatch(l, "clCode", "urn:uuid:41a5887f-8865-4c09-adf7-e362475b143a");
    }
    public static ClassificationMatch getPSCodeMatch(List l) {
        return getCodeMatch(l, "psc", "urn:uuid:cccf5598-8b07-4b77-a05e-ae952c785ead");
    }
    public static ClassificationMatch getHFTCodeMatch(List l) {
        return getCodeMatch(l, "hftc", "urn:uuid:f33fb8ac-18af-42cc-ae0e-ed0b0bdb91e1");
    }
    public static ClassificationMatch getEVCodeMatch(List l) {
        return getCodeMatch(l, "ecl", "urn:uuid:2c6b8cb7-8b2a-4051-b291-b1ae6a575ef4");
    }
    
    private static ClassificationMatch getCodeMatch(List l, String name, String urn) {
        if ( l == null || l.size() < 1 ) return null;
        return new ClassificationMatch(l, name, urn);
    }
    
    public String getName() {
        return name;
    }
    public String toString() {
        return sbMatch.toString();
    }

}
