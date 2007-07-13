/*
 * org.dcm4che.archive.dao.UserDAO.java
 * Created on Jun 22, 2007 by jfalk
 * Copyright 2007, QNH, Inc. info@qualitynighthawk.com, All rights reserved
 */
package org.dcm4che.archive.dao;

import javax.ejb.Local;

import org.dcm4che.archive.entity.User;

@Local
public interface UserDAO extends DAO<User> {

}