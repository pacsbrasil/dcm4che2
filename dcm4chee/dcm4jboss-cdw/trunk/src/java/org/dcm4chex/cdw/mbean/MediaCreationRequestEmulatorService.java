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
 * Java(TM), available at http://sourceforge.net/projects/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * Agfa HealthCare.
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
package org.dcm4chex.cdw.mbean;

import java.text.DecimalFormat;

import javax.management.Attribute;
import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.ObjectName;

import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmElement;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.dict.Tags;
import org.dcm4chex.cdw.common.Flag;
import org.dcm4chex.cdw.common.Priority;
import org.dcm4chex.cdw.common.SpoolDirDelegate;
import org.jboss.system.ServiceMBeanSupport;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @version $Revision$ $Date$
 * @since Feb 13, 2009
 */
public class MediaCreationRequestEmulatorService extends ServiceMBeanSupport
    implements NotificationListener {

    private static final String AET = "aet";

    private String mediaComposerQueueName;

    private String mediaWriter;

    private String mediaApplicationProfile = "STD-GEN-CD";

    private String requestPriority = Priority.LOW;

    private String filesetID;
    
    private DecimalFormat filesetIDFormat;
    
    private int nextFilesetIDSeqno = 1;
    
    private int numberOfCopies = 1;

    private boolean labelUsingInformationExtractedFromInstances = true;

    private String labelText = "";

    private String labelStyleSelection = "";

    private boolean allowMediaSplitting = false;

    private boolean allowLossyCompression = false;

    private String includeNonDICOMObjects = "NO";

    private boolean includeDisplayApplication = false;

    private boolean preserveInstances = false;

    private long pollInterval = SpoolDirService.MS_PER_MINUTE;

    private long createDelay = SpoolDirService.MS_PER_MINUTE;

    private Integer schedulerID;

    private final SpoolDirDelegate spoolDir = new SpoolDirDelegate(this);

    private final SchedulerDelegate scheduler = new SchedulerDelegate(this);

    public String getSourceAET() {
        return serviceName.getKeyProperty(AET);
    }

    public ObjectName getSpoolDirName() {
        return spoolDir.getSpoolDirName();
    }

    public void setSpoolDirName(ObjectName spoolDirName) {
        spoolDir.setSpoolDirName(spoolDirName);
    }

    public ObjectName getSchedulerServiceName() {
        return scheduler.getSchedulerServiceName();
    }

    public void setSchedulerServiceName(ObjectName schedulerServiceName) {
        scheduler.setSchedulerServiceName(schedulerServiceName);
    }

    public final String getMediaComposerQueueName() {
        return mediaComposerQueueName;
    }

    public final void setMediaComposerQueueName(String mediaComposerQueueName) {
        this.mediaComposerQueueName = mediaComposerQueueName;
    }

    public void setMediaWriter(String mediaWriter) {
        this.mediaWriter = mediaWriter;
    }

    public String getMediaWriter() {
        return mediaWriter;
    }

    public final String getDefaultMediaApplicationProfile() {
        return mediaApplicationProfile;
    }

    public final void setDefaultMediaApplicationProfile(String profile) {
        this.mediaApplicationProfile = profile;
    }

    public final String getFilesetID() {
        return filesetID;
    }

    public final void setFilesetID(String filesetID) {
        if (filesetID.indexOf('0') != -1
                || filesetID.indexOf('#') != -1) {
            filesetIDFormat = new DecimalFormat(filesetID);
        }
        this.filesetID = filesetID;
    }

    public final int getNextFilesetIDSeqno() {
        return nextFilesetIDSeqno;
    }

    public final void setNextFilesetIDSeqno(int seqno) {
        this.nextFilesetIDSeqno = seqno;
    }   

    private String nextFilesetID() {
        if (filesetIDFormat == null) {
            return filesetID;
        }
        
        synchronized (filesetIDFormat) {
            try {
                server.setAttribute(serviceName, 
                        new Attribute("NextFilesetIDSeqno", 
                                new Integer(nextFilesetIDSeqno+1)) );
            } catch (Exception e) {
                log.warn("Failed to store incremented NextFilesetSeqno - " +
                            "will be reset by next reboot! ", e);
                ++nextFilesetIDSeqno;
            }
            return filesetIDFormat.format(nextFilesetIDSeqno-1);
        }
    }

    public final boolean isAllowLossyCompression() {
        return allowLossyCompression;
    }

    public final void setAllowLossyCompression(
            boolean allowLossyCompression) {
        this.allowLossyCompression = allowLossyCompression;
    }

    public final boolean isAllowMediaSplitting() {
        return allowMediaSplitting;
    }

    public final void setAllowMediaSplitting(
            boolean allowMediaSplitting) {
        this.allowMediaSplitting = allowMediaSplitting;
    }

    public final boolean isIncludeDisplayApplication() {
        return includeDisplayApplication;
    }

    public final void setIncludeDisplayApplication(
            boolean includeDisplayApplication) {
        this.includeDisplayApplication = includeDisplayApplication;
    }

    public final String getIncludeNonDICOMObjects() {
        return includeNonDICOMObjects;
    }

    public final void setIncludeNonDICOMObjects(
            String includeNonDICOMObjects) {
        this.includeNonDICOMObjects = includeNonDICOMObjects;
    }

    public final String getLabelStyleSelection() {
        return labelStyleSelection;
    }

    public final void setLabelStyleSelection(
            String labelStyleSelection) {
        this.labelStyleSelection = labelStyleSelection;
    }

    public final String getLabelText() {
        return labelText;
    }

    public final void setLabelText(String labelText) {
        this.labelText = labelText;
    }

    public final boolean isLabelUsingInformationExtractedFromInstances() {
        return labelUsingInformationExtractedFromInstances;
    }

    public final void setLabelUsingInformationExtractedFromInstances(
            boolean labelUsingInformationExtractedFromInstances) {
        this.labelUsingInformationExtractedFromInstances =
                labelUsingInformationExtractedFromInstances;
    }

    public final boolean isPreserveInstances() {
        return preserveInstances;
    }

    public final void setPreserveInstances(
            boolean preserveInstances) {
        this.preserveInstances = preserveInstances;
    }

    public final String getRequestPriority() {
        return requestPriority;
    }

    public final void setRequestPriority(String priority) {
        if (!Priority.isValid(priority))
                throw new IllegalArgumentException("priority:" + priority);
        this.requestPriority = priority;
    }

    public final int getNumberOfCopies() {
        return numberOfCopies;
    }

    public final void setNumberOfCopies(int numberOfCopies) {
        if (numberOfCopies < 1)
                throw new IllegalArgumentException("numberOfCopies:"
                        + numberOfCopies);
        this.numberOfCopies = numberOfCopies;
    }

    private Dataset createRequest() {
        Dataset ds = DcmObjectFactory.getInstance().newDataset();
        ds.putCS(Tags.LabelUsingInformationExtractedFromInstances,
                Flag.valueOf(labelUsingInformationExtractedFromInstances));
        ds.putCS(Tags.AllowMediaSplitting, Flag.valueOf(allowMediaSplitting));
        ds.putCS(Tags.AllowLossyCompression,
                Flag.valueOf(allowLossyCompression));
        ds.putCS(Tags.IncludeDisplayApplication,
                Flag.valueOf(includeDisplayApplication));
        ds.putCS(Tags.PreserveCompositeInstancesAfterMediaCreation,
                Flag.valueOf(preserveInstances));
        ds.putUT(Tags.LabelText, labelText);
        ds.putCS(Tags.LabelStyleSelection, labelStyleSelection);
        ds.putCS(Tags.IncludeNonDICOMObjects,
                        includeNonDICOMObjects);
        DcmElement refSOPs = ds.putSQ(Tags.RefSOPSeq);
        return ds;
    }

    public final String getCreateDelay() {
        return SpoolDirService.timeAsString(createDelay);
    }

    public void setCreateDelay(String createDelay) throws Exception {
        this.createDelay = SpoolDirService.timeFromString(createDelay);
    }

    public final String getPollInterval() {
        return SpoolDirService.timeAsString(pollInterval);
    }

    public void setPollInterval(String interval) throws Exception {
        this.pollInterval = SpoolDirService.timeFromString(interval);
        if (getState() == STARTED) {
            scheduler.stopScheduler(getSchedulerName(), schedulerID, this);
            schedulerID = scheduler.startScheduler("PurgeSpoolDir",
                        pollInterval, this);
        }
    }

    protected void startService() throws Exception {
        schedulerID = scheduler.startScheduler(getSchedulerName(),
                pollInterval, this);
    }

    protected void stopService() throws Exception {
        scheduler.stopScheduler(getSchedulerName(), schedulerID, this);
    }

    private String getSchedulerName() {
        return "EmulateRequestsFor" + getSourceAET();
    }

    public void handleNotification(Notification notification, Object handback) {
        // TODO Auto-generated method stub
        
    }

}
