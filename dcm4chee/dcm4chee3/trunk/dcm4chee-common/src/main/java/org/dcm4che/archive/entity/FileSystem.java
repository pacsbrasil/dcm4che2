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

import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

/**
 * org.dcm4che.archive.entity.FileSystem
 * 
 * @author <a href="mailto:damien.daddy@gmail.com">Damien Evans</a>
 */
@Entity
@Table(name = "filesystem")
public class FileSystem extends EntityBase {

    private static final long serialVersionUID = 3499959281721365249L;

    @Column(name = "dirpath", nullable = false)
    private String directoryPath;

    @Column(name = "fs_group_id", nullable = false)
    private String groupId;

    @Column(name = "availability", nullable = false)
    private Integer availability;

    @Column(name = "fs_status", nullable = false)
    private int status;

    @Column(name = "user_info")
    private String userInfo;

    @Column(name = "retrieve_aet", nullable = false)
    private String retrieveAET;

    @OneToOne
    @JoinColumn(name = "next_fk")
    private FileSystem nextFileSystem;

    // FIXME
    @Transient
    private Set<FileSystem> previousFileSystems;

    public FileSystem() {
        super();
    }

    public FileSystem(FileSystemDTO dto) {
        fromDTO(dto);
    }

    /**
     * @return the availability
     */
    public Integer getAvailability() {
        return availability;
    }

    /**
     * @param availability
     *            the availability to set
     */
    public void setAvailability(Integer availability) {
        this.availability = availability;
    }

    /**
     * @return the directoryPath
     */
    public String getDirectoryPath() {
        return directoryPath;
    }

    /**
     * @param directoryPath
     *            the directoryPath to set
     */
    public void setDirectoryPath(String directoryPath) {
        this.directoryPath = directoryPath;
    }

    /**
     * @return the nextFileSystem
     */
    public FileSystem getNextFileSystem() {
        return nextFileSystem;
    }

    /**
     * @param nextFileSystem
     *            the nextFileSystem to set
     */
    public void setNextFileSystem(FileSystem nextFileSystem) {
        this.nextFileSystem = nextFileSystem;
    }

    /**
     * @return the status
     */
    public int getStatus() {
        return status;
    }

    /**
     * @param status
     *            the status to set
     */
    public void setStatus(int status) {
        this.status = status;
    }

    /**
     * @return the userInfo
     */
    public String getUserInfo() {
        return userInfo;
    }

    /**
     * @param userInfo
     *            the userInfo to set
     */
    public void setUserInfo(String userInfo) {
        this.userInfo = userInfo;
    }

    /**
     * @return the retrieveAET
     */
    public String getRetrieveAET() {
        return retrieveAET;
    }

    /**
     * @param retrieveAET
     *            the retrieveAET to set
     */
    public void setRetrieveAET(String retrieveAET) {
        this.retrieveAET = retrieveAET;
    }

    /**
     * @return the previousFileSystems
     */
    public Set<FileSystem> getPreviousFileSystems() {
        return previousFileSystems;
    }

    /**
     * @param previousFileSystems
     *            the previousFileSystems to set
     */
    public void setPreviousFileSystems(Set<FileSystem> previousFileSystems) {
        this.previousFileSystems = previousFileSystems;
    }

    /**
     * @generated by CodeSugar http://sourceforge.net/projects/codesugar
     */
    public String toString() {
        StringBuilder buffer = new StringBuilder();
        buffer.append("[FileSystem:");
        buffer.append(" pk: ");
        buffer.append(getPk());
        buffer.append(" directoryPath: ");
        buffer.append(", groupId=");
        buffer.append(getGroupId());
        buffer.append(directoryPath);
        buffer.append(" retrieveAET: ");
        buffer.append(retrieveAET);
        buffer.append(" availability: ");
        buffer.append(availability);
        buffer.append(" status: ");
        buffer.append(status);
        buffer.append(" userInfo: ");
        buffer.append(userInfo);
        buffer.append(" nextFileSystem: ");
        buffer.append(nextFileSystem);
        buffer.append("]");
        return buffer.toString();
    }

    public void fromDTO(FileSystemDTO dto) {
        setDirectoryPath(dto.getDirectoryPath());
        setGroupId(dto.getGroupId());
        setRetrieveAET(dto.getRetrieveAET());
        setAvailability(dto.getAvailability());
        setStatus(dto.getStatus());
        setUserInfo(dto.getUserInfo());
    }

    public FileSystemDTO toDTO() {
        FileSystemDTO dto = new FileSystemDTO();
        dto.setPk(getPk().longValue());
        dto.setDirectoryPath(getDirectoryPath());
        dto.setGroupId(getGroupId());
        dto.setRetrieveAET(getRetrieveAET());
        dto.setAvailability(getAvailability());
        dto.setStatus(getStatus());
        dto.setUserInfo(getUserInfo());
        FileSystem next = getNextFileSystem();
        if (next != null) {
            // prevent reentry in case of next == this
            String nextPath = next.equals(this) ? getDirectoryPath() : next
                    .getDirectoryPath();
            dto.setNext(nextPath);
        }
        return dto;
    }

    /**
     * @return the groupId
     */
    public String getGroupId() {
        return groupId;
    }

    /**
     * @param groupId
     *            the groupId to set
     */
    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }
}