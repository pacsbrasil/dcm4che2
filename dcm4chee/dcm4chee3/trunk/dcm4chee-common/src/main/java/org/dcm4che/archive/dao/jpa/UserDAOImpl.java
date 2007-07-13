/*
 * org.dcm4che.archive.dao.jpa.UserDAOImpl.java
 * Created on Jun 22, 2007 by jfalk
 * Copyright 2007, QNH, Inc. info@qualitynighthawk.com, All rights reserved
 */
package org.dcm4che.archive.dao.jpa;

import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;

import org.dcm4che.archive.dao.UserDAO;
import org.dcm4che.archive.entity.User;

@Stateless
@TransactionManagement(value = TransactionManagementType.CONTAINER)
public class UserDAOImpl extends BaseDAOImpl<User> implements UserDAO {

    @Override
    public Class getPersistentClass() {
        return User.class;
    }
}