/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
package org.dcm4chex.archive.ejb.interfaces;

import java.io.Serializable;
import java.util.Date;

/**
 * @author gunter.zeilinger@tiani.com
 * @version Revision $Date$
 * @since 14.12.2004
 */

public class MediaDTO implements Serializable {

    private static final long serialVersionUID = 3545516192564721461L;

    public static final int COLLECTING = 0;
    public static final int TRANSFERING = 1;
    public static final int CREATING = 2;
    public static final int COMPLETED = 3;
    public static final int TRANSFER_ERROR = -1;
    public static final int CREATE_ERROR = -2;

    private int pk;
    private String filesetId;
    private String filesetIuid;
    private String mediaCreationRequestIuid;
    private int mediaStatus;
    private long mediaUsage;
    private long createdTime;
    private long updatedTime;

    public final int getPk() {
        return pk;
    }

    public final void setPk(int pk) {
        this.pk = pk;
    }

    public final Date getCreatedTime() {
        return new Date(createdTime);
    }

    public final void setCreatedTime(Date time) {
        this.createdTime = time.getTime();
    }

    public final Date getUpdatedTime() {
        return new Date(updatedTime);
    }

    public final void setUpdatedTime(Date time) {
        this.updatedTime = time.getTime();
    }

    public final String getFilesetId() {
        return filesetId;
    }

    public final void setFilesetId(String filesetId) {
        this.filesetId = filesetId;
    }

    public final String getFilesetIuid() {
        return filesetIuid;
    }

    public final void setFilesetIuid(String filesetIuid) {
        this.filesetIuid = filesetIuid;
    }

    public final String getMediaCreationRequestIuid() {
        return mediaCreationRequestIuid;
    }
    
    public final void setMediaCreationRequestIuid(
            String mediaCreationRequestIuid) {
        this.mediaCreationRequestIuid = mediaCreationRequestIuid;
    }
    
    public final long getMediaUsage() {
        return mediaUsage;
    }

    public final void setMediaUsage(long mediaUsage) {
        this.mediaUsage = mediaUsage;
    }
    
    public final int getMediaStatus() {
        return mediaStatus;
    }

    public final void setMediaStatus(int mediaStatus) {
        this.mediaStatus = mediaStatus;
    }
}
