/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
package org.dcm4chex.archive.common;

/**
 * @author gunter.zeilinger@tiani.com
 * @version $Revision$ $Date$
 * @since 06.10.2004
 *
 */
public class Availability {

    private static final String[] AVAILABILITY = { "ONLINE", "NEARLINE",
            "OFFLINE"};

    public static final String toString(int value) {
        return AVAILABILITY[value];
    }

    private final int value;

    public Availability(int value) {
        if (value < 0 || value >= AVAILABILITY.length)
                throw new IllegalArgumentException("value: " + value);
        this.value = value;
    }

    public final String toString() {
        return AVAILABILITY[value];
    }

    public final int getValue() {
        return value;
    }
}