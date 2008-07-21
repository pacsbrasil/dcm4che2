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
 * Agfa-Gevaert AG.
 * Portions created by the Initial Developer are Copyright (C) 2008
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * See listed authors below.
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
package org.dcm4chee.archive.entity;

import java.io.Serializable;
import java.util.Date;
import java.util.Set;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @version $Revision$ $Date$
 * @since Feb 29, 2008
 */
public class Media implements Serializable {

    private static final long serialVersionUID = -4054710856453638118L;

    private long pk;

    private Date createdTime;

    private Date updatedTime;

    private String mediaCreationRequestInstanceUID;

    private int mediaStatus;

    private String mediaStatusInfo;

    private long mediaUsage;

    private String fileSetID;

    private String fileSetUID;

    private Set<Instance> instances;

    public final long getPk() {
        return pk;
    }

    public Date getCreatedTime() {
        return createdTime;
    }

    public Date getUpdatedTime() {
        return updatedTime;
    }

    public String getMediaCreationRequestInstanceUID() {
        return mediaCreationRequestInstanceUID;
    }

    public void setMediaCreationRequestInstanceUID(
            String mediaCreationRequestInstanceUID) {
        this.mediaCreationRequestInstanceUID = mediaCreationRequestInstanceUID;
    }

    public int getMediaStatus() {
        return mediaStatus;
    }

    public void setMediaStatus(int mediaStatus) {
        this.mediaStatus = mediaStatus;
    }

    public String getMediaStatusInfo() {
        return mediaStatusInfo;
    }

    public void setMediaStatusInfo(String mediaStatusInfo) {
        this.mediaStatusInfo = mediaStatusInfo;
    }

    public long getMediaUsage() {
        return mediaUsage;
    }

    public void setMediaUsage(long mediaUsage) {
        this.mediaUsage = mediaUsage;
    }

    public String getFileSetID() {
        return fileSetID;
    }

    public void setFileSetID(String fileSetID) {
        this.fileSetID = fileSetID;
    }

    public String getFileSetUID() {
        return fileSetUID;
    }

    public void setFileSetUID(String fileSetUID) {
        this.fileSetUID = fileSetUID;
    }

    public Set<Instance> getInstances() {
        return instances;
    }

    public void setInstances(Set<Instance> instances) {
        this.instances = instances;
    }

    @Override
    public String toString() {
        return "Media[pk=" + pk
                + ", fsid=" + fileSetID
                + ", fsuid=" + fileSetUID
                + ", usage=" + mediaUsage
                + ", status=" + mediaStatus
                + "]";
    }

    public void onPrePersist() {
        createdTime = new Date();
    }

    public void onPreUpdate() {
        updatedTime = new Date();
    }

}
