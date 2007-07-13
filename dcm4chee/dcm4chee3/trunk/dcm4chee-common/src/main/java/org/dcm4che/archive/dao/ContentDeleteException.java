/*
 * com.asd.qnh.exceptions.ContentDeleteException.java
 * Created on May 20, 2007 by damien
 * Copyright 2007, QNH, Inc. info@qualitynighthawk.com, All rights reserved
 */
package org.dcm4che.archive.dao;

import javax.persistence.PersistenceException;

/**
 * com.asd.qnh.exceptions.ContentDeleteException
 * 
 * @author <a href="mailto:damien.daddy@gmail.com">Damien Evans</a>
 */
public class ContentDeleteException extends PersistenceException {

    private static final long serialVersionUID = 142766923163104412L;

    /**
     * 
     */
    public ContentDeleteException() {
    }

    /**
     * @param message
     */
    public ContentDeleteException(String message) {
        super(message);
    }

    /**
     * @param cause
     */
    public ContentDeleteException(Throwable cause) {
        super(cause);
    }

    /**
     * @param message
     * @param cause
     */
    public ContentDeleteException(String message, Throwable cause) {
        super(message, cause);
    }

}
