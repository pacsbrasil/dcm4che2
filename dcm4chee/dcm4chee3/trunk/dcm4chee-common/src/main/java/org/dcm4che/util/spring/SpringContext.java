/*
 * org.dcm4che.util.spring.SpringContext.java
 * Created on May 28, 2007 by damien
 * Copyright 2007, QNH, Inc. info@qualitynighthawk.com, All rights reserved
 */
package org.dcm4che.util.spring;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Simple utility to get a handle to the Spring context.
 * 
 * @author <a href="mailto:damien.daddy@gmail.com">Damien Evans</a>
 */
public final class SpringContext {
    private static ApplicationContext context;

    // Default for testing
    private static String serviceDefinitions = "dcm4chee-archive.xml";

    public static ApplicationContext getApplicationContext() {
        if (context == null) context = new ClassPathXmlApplicationContext(serviceDefinitions);
        return context;
    }

}
