package org.weasis.launcher.wado.xml;

import java.io.Closeable;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class FileUtil {
    private static final String DELIM_START = "${";
    private static final String DELIM_STOP = "}";

    public static void safeClose(final Closeable object) {
        try {
            if (object != null) {
                object.close();
            }
        } catch (IOException e) {
            // log.error(e.getMessage());
        }
    }

    public static String substVars(String val, String currentKey, Map<String, String> cycleMap, Properties configProps)
        throws IllegalArgumentException {
        if (cycleMap == null) {
            cycleMap = new HashMap<String, String>();
        }
        cycleMap.put(currentKey, currentKey);

        int stopDelim = -1;
        int startDelim = -1;

        do {
            stopDelim = val.indexOf(DELIM_STOP, stopDelim + 1);
            if (stopDelim < 0) {
                return val;
            }
            startDelim = val.indexOf(DELIM_START);
            if (startDelim < 0) {
                return val;
            }
            while (stopDelim >= 0) {
                int idx = val.indexOf(DELIM_START, startDelim + DELIM_START.length());
                if ((idx < 0) || (idx > stopDelim)) {
                    break;
                } else if (idx < stopDelim) {
                    startDelim = idx;
                }
            }
        } while ((startDelim > stopDelim) && (stopDelim >= 0));

        String variable = val.substring(startDelim + DELIM_START.length(), stopDelim);

        if (cycleMap.get(variable) != null) {
            throw new IllegalArgumentException("recursive variable reference: " + variable);
        }
        String substValue = (configProps != null) ? configProps.getProperty(variable, null) : null;
        if (substValue == null) {
            substValue = System.getProperty(variable, "");
        }

        cycleMap.remove(variable);
        val = val.substring(0, startDelim) + substValue + val.substring(stopDelim + DELIM_STOP.length(), val.length());
        val = substVars(val, currentKey, cycleMap, configProps);
        return val;
    }
}
