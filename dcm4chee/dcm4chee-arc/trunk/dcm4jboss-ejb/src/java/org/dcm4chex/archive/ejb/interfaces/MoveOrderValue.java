/* $Id$
 * Copyright (c) 2002,2003 by TIANI MEDGRAPH AG
 *
 * This file is part of dcm4che.
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.dcm4chex.archive.ejb.interfaces;

import java.io.Serializable;
import java.util.Date;

/**
 * 
 * @author gunter.zeilinger@tiani.com
 * @version $Revision$ $Date$
 * @since 10.12.2003
 */
public final class MoveOrderValue implements Serializable {

    private Date scheduledTime;
    private int priority;
    private String retrieveAET;
    private String moveDestination;
    private String patientId;
    private String studyIuids;
    private String seriesIuids;
    private String sopIuids;
    private int failureCount;
    private int failureStatus;

    /**
     * @return
     */
    public final int getPriority() {
        return priority;
    }

    /**
     * @param priority
     */
    public final void setPriority(int priority) {
        this.priority = priority;
    }

    /**
     * @return
     */
    public final String getRetrieveAET() {
        return retrieveAET;
    }

    /**
     * @param retrieveAET
     */
    public final void setRetrieveAET(String retrieveAET) {
        this.retrieveAET = retrieveAET;
    }

    /**
     * @return
     */
    public final Date getScheduledTime() {
        return scheduledTime;
    }

    /**
     * @param scheduledTime
     */
    public final void setScheduledTime(Date scheduledTime) {
        this.scheduledTime = scheduledTime;
    }

    /**
     * @return
     */
    public final int getFailureCount() {
        return failureCount;
    }

    /**
     * @param failureCount
     */
    public final void setFailureCount(int failureCount) {
        this.failureCount = failureCount;
    }

    /**
     * @return
     */
    public final int getFailureStatus() {
        return failureStatus;
    }

    /**
     * @param failureStatus
     */
    public final void setFailureStatus(int failureStatus) {
        this.failureStatus = failureStatus;
    }

    /**
     * @param failureStatus
     */
    public final void addFailure(int failureStatus) {
        this.failureCount++;
        this.failureStatus = failureStatus;
    }

    /**
     * @return
     */
    public final String getMoveDestination() {
        return moveDestination;
    }

    /**
     * @param moveDestination
     */
    public final void setMoveDestination(String moveDestination) {
        this.moveDestination = moveDestination;
    }

    /**
     * @return
     */
    public final String getPatientId() {
        return patientId;
    }

    /**
     * @param patientId
     */
    public final void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    /**
     * @return
     */
    public final String getSeriesIuids() {
        return seriesIuids;
    }

    /**
     * @param seriesIuids
     */
    public final void setSeriesIuids(String seriesIuids) {
        this.seriesIuids = seriesIuids;
    }

    /**
     * @return
     */
    public final String getSopIuids() {
        return sopIuids;
    }

    /**
     * @param sopIuids
     */
    public final void setSopIuids(String sopIuids) {
        this.sopIuids = sopIuids;
    }

    /**
     * @return
     */
    public final String getStudyIuids() {
        return studyIuids;
    }

    /**
     * @param studyIuids
     */
    public final void setStudyIuids(String studyIuids) {
        this.studyIuids = studyIuids;
    }

    public String toString() {
        return "MoveOrder[scheduled="
            + getScheduledTime()
            + ", priority="
            + getPriority()
            + ", retrieveAET="
            + getRetrieveAET()
            + ", destination="
            + getMoveDestination()
            + ", patientID="
            + getPatientId()
            + ", studyIUIDs="
            + getStudyIuids()
            + ", seriesIUIDs="
            + getSeriesIuids()
            + ", sopIUIDs="
            + getSopIuids()
            + ", failures="
            + getFailureCount()
            + ", status="
            + getFailureStatus()
            + "]";
    }
    
    public String getQueryRetrieveLevel() {
        if (sopIuids != null && sopIuids.length() > 0) {
            return "IMAGE";
        }
        if (seriesIuids != null && seriesIuids.length() > 0) {
            return "SERIES";
        }
        if (studyIuids != null && studyIuids.length() > 0) {
            return "STUDY";
        }
        if (patientId != null && patientId.length() > 0) {
            return "PATIENT";
        }
        throw new IllegalStateException("No Unique Key");
    }

}
