/*
 * org.dcm4che.archive.entity.PrivateFile.java
 * Created on May 28, 2007 by damien
 * Copyright 2007, QNH, Inc. info@qualitynighthawk.com, All rights reserved
 */
package org.dcm4che.archive.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;


/**
 * org.dcm4che.archive.entity.PrivateFile
 * 
 * @author <a href="mailto:damien.daddy@gmail.com">Damien Evans</a>
 */
@Entity
@Table(name = "priv_file")
public class PrivateFile extends EntityBase {

    private static final long serialVersionUID = 2019619506707680219L;
    
    @Column(name = "filepath", nullable = false)
    private String filePath;
    
    @Column(name = "file_tsuid", nullable = false)
    private String fileTsuid;
    
    @Column(name = "file_md5")
    private String fileMD5Field;
    
    @Column(name = "file_status")
    private Integer fileStatus;
    
    @Column(name = "file_size")
    private Long fileSize;
    
    @ManyToOne(targetEntity = org.dcm4che.archive.entity.PrivateInstance.class)
    @JoinColumn(name = "instance_fk")
    private Instance instance;
    
    @ManyToOne(targetEntity = org.dcm4che.archive.entity.FileSystem.class)
    @JoinColumn(name = "filesystem_fk")
    private FileSystem fileSystem;

    /**
     * 
     */
    public PrivateFile() {
    }
    
    /**
     * @return the fileMD5Field
     */
    public String getFileMD5Field() {
        return fileMD5Field;
    }

    /**
     * @param fileMD5Field the fileMD5Field to set
     */
    public void setFileMD5Field(String fileMD5Field) {
        this.fileMD5Field = fileMD5Field;
    }

    /**
     * @return the filePath
     */
    public String getFilePath() {
        return filePath;
    }

    /**
     * @param filePath the filePath to set
     */
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    /**
     * @return the fileSize
     */
    public Long getFileSize() {
        return fileSize;
    }

    /**
     * @param fileSize the fileSize to set
     */
    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    /**
     * @return the fileStatus
     */
    public Integer getFileStatus() {
        return fileStatus;
    }

    /**
     * @param fileStatus the fileStatus to set
     */
    public void setFileStatus(Integer fileStatus) {
        this.fileStatus = fileStatus;
    }

    /**
     * @return the fileSystem
     */
    public FileSystem getFileSystem() {
        return fileSystem;
    }

    /**
     * @param fileSystem the fileSystem to set
     */
    public void setFileSystem(FileSystem fileSystem) {
        this.fileSystem = fileSystem;
    }

    /**
     * @return the fileTsuid
     */
    public String getFileTsuid() {
        return fileTsuid;
    }

    /**
     * @param fileTsuid the fileTsuid to set
     */
    public void setFileTsuid(String fileTSUID) {
        this.fileTsuid = fileTSUID;
    }

    /**
     * @return the instance
     */
    public Instance getInstance() {
        return instance;
    }

    /**
     * @param instance the instance to set
     */
    public void setInstance(Instance instance) {
        this.instance = instance;
    }

    /**
     * MD5 checksum in binary format
     * 
     * @ejb.interface-method
     */
    public byte[] getFileMd5() {
        return MD5.toBytes(getFileMD5Field());
    }

    public void setFileMd5(byte[] md5) {
        setFileMD5Field(MD5.toString(md5));
    }
    
    public FileDTO getFileDTO() {
        FileSystem fs = getFileSystem();
        FileDTO retval = new FileDTO();
        retval.setPk(getPk());
        retval.setRetrieveAET(fs.getRetrieveAET());
        retval.setFileSystemPk(fs.getPk().longValue());
        retval.setDirectoryPath(fs.getDirectoryPath());
        retval.setAvailability(fs.getAvailability());
        retval.setUserInfo(fs.getUserInfo());
        retval.setFilePath(getFilePath());
        retval.setFileTsuid(getFileTsuid());
        retval.setFileSize(getFileSize());
        retval.setFileMd5(getFileMd5());
        retval.setFileStatus(getFileStatus());
        return retval;
    }

}
