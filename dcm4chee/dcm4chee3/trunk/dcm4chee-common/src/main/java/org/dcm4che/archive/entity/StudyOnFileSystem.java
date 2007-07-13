/*
 * StudyOnFileSystem.java 
 * Created on May 20, 2007 by damien 
 * Copyright 2007, QNH, Inc. info@qualitynighthawk.com, All rights reserved
 */
package org.dcm4che.archive.entity;

import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

/**
 * org.dcm4che.archive.entity.StudyOnFileSystem
 * 
 * @author <a href="mailto:damien.daddy@gmail.com">Damien Evans</a>
 */
@Entity
@Table(name = "study_on_fs")
@SequenceGenerator(name = "SEQ_STORE", sequenceName = "study_on_fs_s")
public class StudyOnFileSystem extends EntityBase {

    private static final long serialVersionUID = 1716183638389803719L;

    @Column(name = "access_time", nullable = false)
    private Timestamp accessTime;

    @ManyToOne(targetEntity = org.dcm4che.archive.entity.Study.class)
    @JoinColumn(name = "study_fk")
    private Study study;

    @ManyToOne(targetEntity = org.dcm4che.archive.entity.FileSystem.class)
    @JoinColumn(name = "filesystem_fk")
    private FileSystem fileSystem;

    /**
     * @return the accessTime
     */
    public Timestamp getAccessTime() {
        return accessTime;
    }

    /**
     * @param accessTime
     *            the accessTime to set
     */
    public void setAccessTime(Timestamp accessTime) {
        this.accessTime = accessTime;
    }

    /**
     * @return the fileSystem
     */
    public FileSystem getFileSystem() {
        return fileSystem;
    }

    /**
     * @param fileSystem
     *            the fileSystem to set
     */
    public void setFileSystem(FileSystem fileSystem) {
        this.fileSystem = fileSystem;
    }

    /**
     * @return the study
     */
    public Study getStudy() {
        return study;
    }

    /**
     * @param study
     *            the study to set
     */
    public void setStudy(Study study) {
        this.study = study;
    }

    public String toString() {
        return "StudyOnFileSystem[" + (study == null ? "null" : study.toString()) + "@"
                + (fileSystem == null ? "null" : fileSystem.toString()) + "]";
    }

    /**
     * Update the access time of this object.
     */
    public void touch() {
        setAccessTime(new Timestamp(System.currentTimeMillis()));
    }
}
