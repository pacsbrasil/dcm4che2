/*
 * org.dcm4che.archive.dao.PrivateFileDAO.java
 * Created on May 28, 2007 by damien
 * Copyright 2007, QNH, Inc. info@qualitynighthawk.com, All rights reserved
 */
package org.dcm4che.archive.dao;

import java.util.List;

import javax.ejb.Local;
import javax.persistence.PersistenceException;

import org.dcm4che.archive.entity.FileSystem;
import org.dcm4che.archive.entity.PrivateFile;
import org.dcm4che.archive.entity.PrivateInstance;

/**
 * org.dcm4che.archive.dao.PrivateFileDAO
 * 
 * @author <a href="mailto:damien.daddy@gmail.com">Damien Evans</a>
 */
@Local
public interface PrivateFileDAO extends DAO<PrivateFile> {

    public PrivateFile create(String path, String tsuid, long size, byte[] md5,
            int status, PrivateInstance instance, FileSystem filesystem)
            throws ContentCreateException;

    /**
     * @param dirPath
     * @param limit
     * @return
     */
    public List<PrivateFile> findDereferencedInFileSystem(String dirPath,
            int limit) throws PersistenceException;

    /**
     * @param pfPk
     */
    public void remove(Long pfPk) throws ContentDeleteException;

}
