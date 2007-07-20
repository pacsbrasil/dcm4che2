package org.dcm4che.archive.perf;

import java.util.Arrays;

/**
 * Performance property enums for counters. It is couter's responsibility
 * to retrieve appropriate data for performance measurement purpose.
 * 
 * @author Fang Yang (fang.yang@agfa.com)
 */
public class PerfPropertyEnum {
	private static final String[] ENUM = { 
		"REQ_DIMSE",
    	"REQ_DATASET",
    	"DICOM_FILE",
    	"RSP_DATASET",
    	"NUM_OF_RESULTS",
    	"STUDY_IUID"
    };

    public static final int REQ_DIMSE = 1;
    public static final int REQ_DATASET = 2;
    public static final int DICOM_FILE = 3;
    public static final int RSP_DATASET = 4;
    public static final int NUM_OF_RESULTS = 5;
    public static final int STUDY_IUID = 6;
    
    public static final String toString(int value) {
       return ENUM[++value];
    }

    public static final int toInt(String s) {
    	final int index = Arrays.asList(ENUM).indexOf(s);
        if (index == -1)
            throw new IllegalArgumentException(s);
        return index;
    }
}
