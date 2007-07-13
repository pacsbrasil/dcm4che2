/*
 * org.dcm4che.archive.entity.User.java
 * Created on Jun 22, 2007 by jfalk
 * Copyright 2007, QNH, Inc. info@qualitynighthawk.com, All rights reserved
 */
package org.dcm4che.archive.entity;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.Table;

import org.hibernate.annotations.CollectionOfElements;

@Entity
@Table(name = "users")
public class User extends EntityBase {

    private static final long serialVersionUID = 8535630016393206232L;

    @Id
    @Column(name="user_id", nullable=false)
    private String userId;
    
    @Column(name="passwd", nullable=false)
    private String password;
    
    @CollectionOfElements
    @JoinTable(
            name="roles",
            joinColumns = @JoinColumn(name="user_id")
    )
    @Column(name="roles", nullable=false)
    private List<String> roles;

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }    
}