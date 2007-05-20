package org.dcm4chex.archive.hsm.spi;

/**
 * @author Fuad Ibrahimov
 * @version $Id$
 * @since May 4, 2007
 */

interface TimeProvider {
    public long now();

    public static final TimeProvider SYSTEM_TIME_PROVIDER = SystemTimeProvider.INSTANCE;

    static class SystemTimeProvider implements TimeProvider {
        private SystemTimeProvider(){}

        public static final TimeProvider INSTANCE = new SystemTimeProvider(); 

        public long now() {
            return System.currentTimeMillis();
        }
    }

}
