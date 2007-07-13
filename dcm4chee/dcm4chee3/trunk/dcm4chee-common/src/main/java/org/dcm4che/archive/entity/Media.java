package org.dcm4che.archive.entity;

import java.sql.Timestamp;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;

/**
 * org.dcm4che.archive.entity.Media
 * 
 * @author <a href="mailto:damien.daddy@gmail.com">Damien Evans</a>
 */
@Entity
@Table(name = "media")
public class Media extends EntityBase {

    private static final long serialVersionUID = 3545516192564721461L;

    @Column(name = "media_rq_iuid", length = 64)
    private String mediaCreationRequestIuid;

    @Column(name = "media_status")
    private int mediaStatus;

    @Column(name = "media_status_info")
    private String mediaStatusInfo;

    @Column(name = "media_usage")
    private long mediaUsage;

    @Column(name = "fileset_id")
    private String filesetId;

    @Column(name = "fileset_iuid")
    private String filesetIuid;

    @Column(name = "created_time")
    private Timestamp createdTime;

    @Column(name = "updated_time")
    private Timestamp updatedTime;

    @OneToMany(mappedBy = "media")
    private Set<Instance> instances;

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
     * @return the filesetId
     */
    public String getFilesetId() {
        return filesetId;
    }

    /**
     * @param filesetId
     *            the filesetId to set
     */
    public void setFilesetId(String filesetId) {
        this.filesetId = filesetId;
    }

    /**
     * @return the filesetIuid
     */
    public String getFilesetIuid() {
        return filesetIuid;
    }

    /**
     * @param filesetIuid
     *            the filesetIuid to set
     */
    public void setFilesetIuid(String filesetIuid) {
        this.filesetIuid = filesetIuid;
    }

    /**
     * @return the instances
     */
    public Set<Instance> getInstances() {
        return instances;
    }

    /**
     * @param instances
     *            the instances to set
     */
    public void setInstances(Set<Instance> instances) {
        this.instances = instances;
    }

    /**
     * @return the mediaCreationRequestIuid
     */
    public String getMediaCreationRequestIuid() {
        return mediaCreationRequestIuid;
    }

    /**
     * @param mediaCreationRequestIuid
     *            the mediaCreationRequestIuid to set
     */
    public void setMediaCreationRequestIuid(String mediaCreationRequestIuid) {
        this.mediaCreationRequestIuid = mediaCreationRequestIuid;
    }

    /**
     * @return the mediaStatus
     */
    public int getMediaStatus() {
        return mediaStatus;
    }

    /**
     * @param mediaStatus
     *            the mediaStatus to set
     */
    public void setMediaStatus(int mediaStatus) {
        this.mediaStatus = mediaStatus;
    }

    /**
     * @return the mediaStatusInfo
     */
    public String getMediaStatusInfo() {
        return mediaStatusInfo;
    }

    /**
     * @param mediaStatusInfo
     *            the mediaStatusInfo to set
     */
    public void setMediaStatusInfo(String mediaStatusInfo) {
        this.mediaStatusInfo = mediaStatusInfo;
    }

    /**
     * @return the mediaUsage
     */
    public long getMediaUsage() {
        return mediaUsage;
    }

    /**
     * @param mediaUsage
     *            the mediaUsage to set
     */
    public void setMediaUsage(long mediaUsage) {
        this.mediaUsage = mediaUsage;
    }

    /**
     * @return the updatedTime
     */
    public Timestamp getUpdatedTime() {
        return updatedTime;
    }

    /**
     * @param updatedTime
     *            the updatedTime to set
     */
    public void setUpdatedTime(Timestamp updatedTime) {
        this.updatedTime = updatedTime;
    }

}
