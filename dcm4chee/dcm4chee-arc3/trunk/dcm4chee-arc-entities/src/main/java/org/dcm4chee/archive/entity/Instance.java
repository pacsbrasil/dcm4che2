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
 * Portions created by the Initial Developer are Copyright (C) 2006-2008
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

import java.io.IOException;
import java.io.Serializable;
import java.util.Date;
import java.util.Set;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4chee.archive.common.Availability;
import org.dcm4chee.archive.common.StorageStatus;
import org.dcm4chee.archive.conf.AttributeFilter;
import org.dcm4chee.archive.exceptions.ConfigurationException;
import org.dcm4chee.archive.util.DicomObjectUtils;

/**
 * @author Damien Evans <damien.daddy@gmail.com>
 * @author Justin Falk <jfalkmu@gmail.com>
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @version $Revision$ $Date$
 * @since Feb 25, 2008
 */
public class Instance implements Serializable {

    private static final long serialVersionUID = -924140016923828861L;

    private long pk;

    private Date createdTime;

    private Date updatedTime;

    private String sopInstanceUID;

    private String sopClassUID;

    private String instanceNumber;

    private Date contentDateTime;

    private String completionFlag;

    private String verificationFlag;

    private String instanceCustomAttribute1;

    private String instanceCustomAttribute2;

    private String instanceCustomAttribute3;

    private byte[] encodedAttributes;

    private String retrieveAETs;

    private String externalRetrieveAET;

    private Availability availability;

    private StorageStatus storageStatus;

    private boolean allAttributes;

    private boolean storageComitted;

    private Code conceptNameCode;

    private Media media;

    private Set<VerifyingObserver> verifyingObservers;

    private Series series;

    private Set<FileInfo> fileInfos;

    public final long getPk() {
        return pk;
    }

    public Date getCreatedTime() {
        return createdTime;
    }

    public Date getUpdatedTime() {
        return updatedTime;
    }

    public String getSOPInstanceUID() {
        return sopInstanceUID;
    }

    public String getSOPClassUID() {
        return sopClassUID;
    }

    public String getInstanceNumber() {
        return instanceNumber;
    }

    public Date getContentDateTime() {
        return contentDateTime;
    }

    public String getCompletionFlag() {
        return completionFlag;
    }

    public String getVerificationFlag() {
        return verificationFlag;
    }

    public String getInstanceCustomAttribute1() {
        return instanceCustomAttribute1;
    }

    public String getInstanceCustomAttribute2() {
        return instanceCustomAttribute2;
    }

    public String getInstanceCustomAttribute3() {
        return instanceCustomAttribute3;
    }

    public byte[] getEncodedAttributes() {
        return encodedAttributes;
    }

    public String getRetrieveAETs() {
        return retrieveAETs;
    }

    public void setRetrieveAETs(String retrieveAETs) {
        this.retrieveAETs = retrieveAETs;
    }

    public String getExternalRetrieveAET() {
        return externalRetrieveAET;
    }

    public void setExternalRetrieveAET(String externalRetrieveAET) {
        this.externalRetrieveAET = externalRetrieveAET;
    }

    public Availability getAvailability() {
        return availability;
    }

    public void setAvailability(Availability availability) {
        this.availability = availability;
    }

    public StorageStatus getStorageStatus() {
        return storageStatus;
    }

    public void setStorageStatus(StorageStatus storageStatus) {
        this.storageStatus = storageStatus;
    }

    public boolean isAllAttributes() {
        return allAttributes;
    }

    public void setAllAttributes(boolean allAttributes) {
        this.allAttributes = allAttributes;
    }

    public boolean isStorageComitted() {
        return storageComitted;
    }

    public void setStorageComitted(boolean storageComitted) {
        this.storageComitted = storageComitted;
    }

    public Code getConceptNameCode() {
        return conceptNameCode;
    }

    public void setConceptNameCode(Code conceptNameCode) {
        this.conceptNameCode = conceptNameCode;
    }

    public Media getMedia() {
        return media;
    }

    public void setMedia(Media media) {
        this.media = media;
    }

    public Set<VerifyingObserver> getVerifyingObservers() {
        return verifyingObservers;
    }

    public void setVerifyingObservers(Set<VerifyingObserver> verifyingObservers) {
        this.verifyingObservers = verifyingObservers;
    }

    public Series getSeries() {
        return series;
    }

    public void setSeries(Series series) {
        this.series = series;
    }

    public Set<FileInfo> getFileInfos() {
        return fileInfos;
    }

    @Override
    public String toString() {
        return "Instance[pk=" + pk
                + ", iuid=" + sopInstanceUID
                + ", cuid=" + sopClassUID
                + ", instno=" + instanceNumber
                + ", time=" + contentDateTime
                + (completionFlag != null
                        ? ", completion=" + completionFlag
                        : "")
                + (verificationFlag != null
                        ? ", verification=" + verificationFlag
                        : "")
                + ", allattrs=" + allAttributes
                + ", comitted=" + storageComitted
                + ", status=" + storageStatus
                + ", avail=" + availability
                + ", aets=" + retrieveAETs
                + ", extaet=" + externalRetrieveAET
                 + "]";
    }

    public void onPrePersist() {
        createdTime = new Date();
    }

    public void onPreUpdate() {
        updatedTime = new Date();
    }

    public DicomObject getAttributes() throws IOException {
        return DicomObjectUtils.decode(encodedAttributes);
    }

    public void setAttributes(DicomObject attrs) {
        this.sopInstanceUID = attrs.getString(Tag.SOPInstanceUID);
        this.sopClassUID = attrs.getString(Tag.SOPClassUID);
        this.instanceNumber = attrs.getString(Tag.InstanceNumber, "");
        this.contentDateTime = attrs.getDate(Tag.ContentDate, Tag.ContentTime);
        this.completionFlag = attrs.getString(Tag.CompletionFlag, "");
        this.verificationFlag = attrs.getString(Tag.VerificationFlag, "");
        AttributeFilter filter = AttributeFilter
                .getInstanceAttributeFilter(sopClassUID);
        int[] fieldTags = filter.getFieldTags();
        for (int i = 0; i < fieldTags.length; i++) {
            try {
                Study.class.getField(filter.getField(fieldTags[i])).set(this,
                        attrs.getString(fieldTags[i], ""));
            } catch (Exception e) {
                throw new ConfigurationException(e);
            }
        }
        this.encodedAttributes = DicomObjectUtils.encode(filter.filter(attrs),
                filter.getTransferSyntaxUID());
    }

}
