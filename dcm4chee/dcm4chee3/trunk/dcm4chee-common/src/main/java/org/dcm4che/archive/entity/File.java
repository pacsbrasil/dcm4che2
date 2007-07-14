/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is part of dcm4che, an implementation of DICOM(TM) in
 * Java(TM), hosted at http://sourceforge.net/projects/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * Accurate Software Design, LLC.
 * Portions created by the Initial Developer are Copyright (C) 2006
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Gunter Zeilinger <gunterze@gmail.com>
 * Franz Willer <franz.willer@gwi-ag.com>
 * Justin Falk <jfalkmu@gmail.com>
 * Damien Evans <damien.daddy@gmail.com>
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 *
 * ***** END LICENSE BLOCK ***** */
package org.dcm4che.archive.entity;

import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * org.dcm4che.archive.entity.File
 * 
 * @author <a href="mailto:damien.daddy@gmail.com">Damien Evans</a>
 */
@Entity
@Table(name = "files")
public final class File extends EntityBase {

    private static final long serialVersionUID = 1913267650031774173L;

    @ManyToOne
    @JoinColumn(name = "filesystem_fk")
    private FileSystem fileSystem;

    @ManyToOne
    @JoinColumn(name = "instance_fk")
    private Instance instance;

    @Column(name = "created_time")
    private Timestamp createdTime;

    @Column(name = "filepath")
    private String filePath;

    @Column(name = "md5_check_time")
    private Timestamp timeOfLastMd5Check;

    @Column(name = "file_tsuid")
    private String fileTsuid;

    @Column(name = "file_status")
    private Integer fileStatus;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "file_md5")
    private String fileMd5Field;

    public boolean isRedundant() {
        Instance inst = getInstance();
        return inst == null || inst.getFiles().size() > 1;
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
        retval.setFilePath(filePath);
        retval.setFileTsuid(fileTsuid);
        retval.setFileSize(fileSize);
        retval.setFileMd5(getFileMd5());
        retval.setFileStatus(fileStatus);

        Instance inst = getInstance();
        if (inst != null)
            retval.setSopClassUID(inst.getSopCuid());
        return retval;
    }

    /**
     * @return the createdTime
     */
    public Timestamp getCreatedTime() {
        return createdTime;
    }

    /**
     * @param createdTime
     *            the createdTime to set
     */
    public void setCreatedTime(Timestamp createdTime) {
        this.createdTime = createdTime;
    }

    /**
     * @return the fileMD5
     */
    public String getFileMd5Field() {
        return fileMd5Field;
    }

    /**
     * @param fileMD5
     *            the fileMD5 to set
     */
    public void setFileMd5Field(String fileMD5) {
        this.fileMd5Field = fileMD5;
    }

    public byte[] getFileMd5() {
        return MD5.toBytes(getFileMd5Field());
    }

    /**
     * @ejb.interface-method
     */
    public void setFileMd5(byte[] md5) {
        setFileMd5Field(MD5.toString(md5));
    }

    /**
     * @return the filePath
     */
    public String getFilePath() {
        return filePath;
    }

    /**
     * @param filePath
     *            the filePath to set
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
     * @param fileSize
     *            the fileSize to set
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
     * @param fileStatus
     *            the fileStatus to set
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
     * @param fileSystem
     *            the fileSystem to set
     */
    public void setFileSystem(FileSystem fileSystem) {
        this.fileSystem = fileSystem;
    }

    /**
     * @return the instance
     */
    public Instance getInstance() {
        return instance;
    }

    /**
     * @param instance
     *            the instance to set
     */
    public void setInstance(Instance instance) {
        this.instance = instance;
    }

    /**
     * @return the timeOfLastMd5Check
     */
    public Timestamp getTimeOfLastMd5Check() {
        return timeOfLastMd5Check;
    }

    /**
     * @param timeOfLastMd5Check
     *            the timeOfLastMd5Check to set
     */
    public void setTimeOfLastMd5Check(Timestamp timeOfLastMd5Check) {
        this.timeOfLastMd5Check = timeOfLastMd5Check;
    }

    /**
     * @return the fileTsuid
     */
    public String getFileTsuid() {
        return fileTsuid;
    }

    /**
     * @param fileTsuid
     *            the fileTsuid to set
     */
    public void setFileTsuid(String tsUID) {
        this.fileTsuid = tsUID;
    }

    public String toString() {
        return "File[pk=" + getPk() + ", filepath=" + getFilePath()
                + ", tsuid=" + getFileTsuid() + ", filesystem->"
                + getFileSystem() + ", inst->" + getInstance() + "]";
    }
}