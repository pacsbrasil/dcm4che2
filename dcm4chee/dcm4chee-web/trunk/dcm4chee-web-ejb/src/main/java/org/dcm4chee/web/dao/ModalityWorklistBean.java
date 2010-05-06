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

package org.dcm4chee.web.dao;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.StringTokenizer;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TemporalType;

import org.dcm4che2.data.DicomObject;
import org.dcm4chee.archive.common.SPSStatus;
import org.dcm4chee.archive.entity.Instance;
import org.dcm4chee.archive.entity.MPPS;
import org.dcm4chee.archive.entity.MWLItem;
import org.dcm4chee.archive.entity.Patient;
import org.dcm4chee.archive.entity.Series;
import org.dcm4chee.archive.entity.Study;
import org.jboss.annotation.ejb.LocalBinding;

/**
 * @author Robert David <robert.david@agfa.com>
 * @version $Revision$ $Date$
 * @since 20.04.2010
 */
@Stateless
@LocalBinding (jndiBinding=ModalityWorklist.JNDI_NAME)
public class ModalityWorklistBean implements ModalityWorklist {

    @PersistenceContext(unitName="dcm4chee-arc")
    private EntityManager em;

    @SuppressWarnings("unchecked")
    public List<MWLItem> findAll() {
        List<MWLItem> l = em.createQuery("FROM MWLItem mwlItem ORDER BY mwlItem.studyInstanceUID")
                .getResultList();
        em.clear();
        return l;
    }
    
    public int countMWLItems(ModalityWorklistFilter filter) {
        StringBuilder ql = new StringBuilder(64);
        ql.append("SELECT COUNT(*)");
        appendFromClause(ql, filter);
        appendWhereClause(ql, filter);
        Query query = em.createQuery(ql.toString());
        setQueryParameters(query, filter);
        return ((Number) query.getSingleResult()).intValue();
    }

    @SuppressWarnings("unchecked")
    public List<MWLItem> findMWLItems(ModalityWorklistFilter filter, int max, int index) {
        StringBuilder ql = new StringBuilder(64);
        ql.append("SELECT m");
        appendFromClause(ql, filter);
        appendWhereClause(ql, filter);
        ql.append(" ORDER BY p.patientName, m.startDateTime");
        if (filter.isLatestItemsFirst()) {
            ql.append(" DESC");
        }

        Query query = em.createQuery(ql.toString());
        setQueryParameters(query, filter);
        return query.setMaxResults(max).setFirstResult(index).getResultList();
    }

    private static void appendFromClause(StringBuilder ql, ModalityWorklistFilter filter) {
        ql.append(ql.toString().startsWith("SELECT COUNT(*)") ?
            " FROM MWLItem m LEFT JOIN m.patient p " : 
            " FROM MWLItem m LEFT JOIN FETCH m.patient p "
        );
    }

    private static void appendWhereClause(StringBuilder ql, ModalityWorklistFilter filter) {
        ql.append(" WHERE p.mergedWith IS NULL");

        if (!filter.isExtendedQuery()) {            
            if (filter.getPatientName() != null)
                appendPatientNameFilter(ql, filter.getPatientName());
            if (filter.getPatientID() != null)
                appendPatientIDFilter(ql, filter.getPatientID());
            if (filter.getIssuerOfPatientID() != null)
                appendIssuerOfPatientIDFilter(ql, filter.getIssuerOfPatientID());
            if (filter.getAccessionNumber() != null)
                appendAccessionNumberFilter(ql, filter.getAccessionNumber());
            if (filter.getStartDateMin() != null)
                appendStartDateMinFilter(ql, filter.getStartDateMin());
            if (filter.getStartDateMax() != null)
                appendStartDateMaxFilter(ql, filter.getStartDateMax());
            if (filter.getModality() != null)        
                appendModalityFilter(ql, filter.getModality());
            if (filter.getScheduledStationAET() != null)        
                appendScheduledStationAETFilter(ql, filter.getScheduledStationAET());
            if (filter.getScheduledStationName() != null)
                appendScheduledStationNameFilter(ql, filter.getScheduledStationName());
            if (filter.getScheduledProcedureStepStatus() != null)
                appendScheduledProcedureStepStatus(ql, filter.getScheduledProcedureStepStatus());
        } else {
            if (!"*".equals(filter.getStudyInstanceUID()))
                ql.append(" AND m.studyInstanceUID = :studyInstanceUID");
        }        
    }
    
    private static void setQueryParameters(Query query, ModalityWorklistFilter filter) {

        if (!filter.isExtendedQuery()) {
            if (filter.getPatientName() != null)
                setPatientNameQueryParameter(query, filter.getPatientName());
            if (filter.getPatientID() != null)
                setPatientIDQueryParameter(query, filter.getPatientID());
            if (filter.getIssuerOfPatientID() != null)
                setIssuerOfPatientIDQueryParameter(query, filter.getIssuerOfPatientID());
            if (filter.getAccessionNumber() != null)
                setAccessionNumberQueryParameter(query, filter.getAccessionNumber());
            if (filter.getStartDateMin() != null)
                setStartDateMinQueryParameter(query, filter.getStartDateMin());
            if (filter.getStartDateMax() != null)
                setStartDateMaxQueryParameter(query, filter.getStartDateMax());
            if (filter.getModality() != null)
                setModalityQueryParameter(query, filter.getModality());
            if (filter.getScheduledStationAET() != null)
                setScheduledStationAETQueryParameter(query, filter.getScheduledStationAET());
            if (filter.getScheduledStationName() != null)
                setScheduledStationNameQueryParameter(query, filter.getScheduledStationName());
            if (filter.getScheduledProcedureStepStatus() != null)
                setScheduledProcedureStepStatusQueryParameter(query, filter.getScheduledProcedureStepStatus());
        } else {        
            if (!"*".equals(filter.getStudyInstanceUID()))
                setStudyInstanceUIDQueryParameter(query, filter.getStudyInstanceUID());
        }
    }

    private static boolean containsWildcard(String s) {
        return s.indexOf('*') != -1 || s.indexOf('?') != -1;
    }

    private static boolean isMustNotNull(String s) {
        return "?*".equals(s) || "*?".equals(s);
    }

    private static String toLike(String s) {
        StringBuilder param = new StringBuilder();
        StringTokenizer tokens = new StringTokenizer(s, "*?_%", true);
        while (tokens.hasMoreTokens()) {
            String token = tokens.nextToken();
            switch (token.charAt(0)) {
            case '%':
                param.append("\\%");
                break;
            case '*':
                param.append('%');
                break;
            case '?':
                param.append('_');
                break;
            case '_':
                param.append("\\_");
                break;
            default:
                param.append(token);
            }
        }
        return param.toString();
    }

    private static void appendPatientNameFilter(StringBuilder ql,
            String patientName) {
        if (!"*".equals(patientName)) {
            ql.append(" AND p.patientName LIKE :patientName");
        }
    }

    private static void setPatientNameQueryParameter(Query query, String patientName) {

        if (!"*".equals(patientName)) {
            int padcarets = 4;
            StringBuilder param = new StringBuilder();
            StringTokenizer tokens = new StringTokenizer(patientName.toUpperCase(),
                    "^*?_%", true);
            while (tokens.hasMoreTokens()) {
                String token = tokens.nextToken();
                switch (token.charAt(0)) {
                case '%':
                    param.append("\\%");
                    break;
                case '*':
                    param.append('%');
                    break;
                case '?':
                    param.append('_');
                    break;
                case '^':
                    padcarets--;
                    param.append('^');
                    break;
                case '_':
                    param.append("\\_");
                    break;
                default:
                    param.append(token);
                }
            }
            while (padcarets-- > 0) {
                param.append("^%");
            }
            query.setParameter("patientName", param.toString());
        }
    }

    private static void appendPatientIDFilter(StringBuilder ql,
            String patientID) {
        if (!"*".equals(patientID)) {
            ql.append(containsWildcard(patientID)
                    ? " AND p.patientID LIKE :patientID"
                    : " AND p.patientID = :patientID");
        }
    }

    private static void setPatientIDQueryParameter(Query query,
            String patientID) {
        if (!"*".equals(patientID)) {
            query.setParameter("patientID", containsWildcard(patientID) 
                    ? toLike(patientID)
                    : patientID);
        }
    }

    private static void appendIssuerOfPatientIDFilter(StringBuilder ql,
            String issuerOfPatientID) {
        if (!"*".equals(issuerOfPatientID)) {
            ql.append("-".equals(issuerOfPatientID)
                    ? " AND p.issuerOfPatientID IS NULL" 
                    : isMustNotNull(issuerOfPatientID)
                    ? " AND p.issuerOfPatientID IS NOT NULL" 
                    : containsWildcard(issuerOfPatientID)
                    ? " AND p.issuerOfPatientID LIKE :issuerOfPatientID"
                    : " AND p.issuerOfPatientID = :issuerOfPatientID");
        }
    }

    private static void setIssuerOfPatientIDQueryParameter(Query query,
            String issuerOfPatientID) {
        if (!"*".equals(issuerOfPatientID)
                && !"-".equals(issuerOfPatientID)
                && !isMustNotNull(issuerOfPatientID)) {
            query.setParameter("issuerOfPatientID",
                    containsWildcard(issuerOfPatientID)
                            ? toLike(issuerOfPatientID)
                            : issuerOfPatientID);
        }
    }

    private static void appendStartDateMinFilter(StringBuilder ql, Date date) {
        if (date != null) {
            ql.append(" AND m.startDateTime >= :startDateTimeMin");
        }
    }

    private static void appendStartDateMaxFilter(StringBuilder ql, Date date) {
        if (date != null) {
            ql.append(" AND m.startDateTime <= :startDateTimeMax");
        }
    }

    private static void setStartDateMinQueryParameter(Query query, Date date) {
        setStartDateQueryParameter(query, date, "startDateTimeMin", false);
    }

    private static void setStartDateMaxQueryParameter(Query query, Date date) {
        setStartDateQueryParameter(query, date, "startDateTimeMax", true);
    }

    private static void setStartDateQueryParameter(Query query,
            Date startDate, String param, boolean max) {
        if (startDate != null) {
            GregorianCalendar cal = new GregorianCalendar();
            cal.setTime(startDate);
            if (max) {
                cal.set(Calendar.HOUR_OF_DAY, 23);
                cal.set(Calendar.MINUTE, 59);
                cal.set(Calendar.SECOND, 59);
                cal.set(Calendar.MILLISECOND, 999);
            }
            query.setParameter(param, cal, TemporalType.TIMESTAMP);
        }
    }

    private static void appendAccessionNumberFilter(StringBuilder ql,
            String accessionNumber) {
        if (!"*".equals(accessionNumber)) {
            ql.append("-".equals(accessionNumber)
                    ? " AND m.accessionNumber IS NULL"
                    : isMustNotNull(accessionNumber)
                    ? " AND m.accessionNumber IS NOT NULL" 
                    : containsWildcard(accessionNumber)
                    ? " AND m.accessionNumber LIKE :accessionNumber"
                    : " AND m.accessionNumber = :accessionNumber");
        }
    }

    private static void setAccessionNumberQueryParameter(Query query,
            String accessionNumber) {
        if (!"*".equals(accessionNumber)
                && !"-".equals(accessionNumber)
                && !isMustNotNull(accessionNumber)) {
            query.setParameter("accessionNumber",
                    containsWildcard(accessionNumber) 
                            ? toLike(accessionNumber)
                            : accessionNumber);
        }
    }

    private static void setStudyInstanceUIDQueryParameter(Query query,
            String studyInstanceUID) {
        if (!"*".equals(studyInstanceUID)) {
            query.setParameter("studyInstanceUID", studyInstanceUID);
        }
    }

    private static void appendModalityFilter(StringBuilder ql,
            String modality) {
        if (!"*".equals(modality)) {
            ql.append(" AND m.modality = :modality");
        }
    }

    private static void setModalityQueryParameter(Query query,
            String modality) {
        if (!"*".equals(modality)) {
            query.setParameter("modality", modality);
        }
    }

    private static void appendScheduledStationAETFilter(StringBuilder ql,
            String scheduledStationAET) {
        if (!"*".equals(scheduledStationAET)) {
            ql.append(" AND m.scheduledStationAET = :scheduledStationAET");
        }
    }

    private static void setScheduledStationAETQueryParameter(Query query,
            String scheduledStationAET) {
        if (!"*".equals(scheduledStationAET)) {
            query.setParameter("scheduledStationAET", scheduledStationAET);
        }
    }

    private static void appendScheduledStationNameFilter(StringBuilder ql,
            String scheduledStationName) {
        if (!"*".equals(scheduledStationName)) {
            ql.append(" AND m.scheduledStationName = :scheduledStationName");
        }
    }

    private static void setScheduledStationNameQueryParameter(Query query,
            String scheduledStationName) {
        if (!"*".equals(scheduledStationName)) {
            query.setParameter("scheduledStationName", scheduledStationName);
        }
    }

    private static void appendScheduledProcedureStepStatus(StringBuilder ql,
            String scheduledProcedureStepStatus) {
        if (!"*".equals(scheduledProcedureStepStatus)) {
            ql.append(" AND m.status = :scheduledProcedureStepStatus");
        }
    }

    private static void setScheduledProcedureStepStatusQueryParameter(Query query,
            String scheduledProcedureStepStatus) {        
        if (!"*".equals(scheduledProcedureStepStatus)) {
            query.setParameter("scheduledProcedureStepStatus", SPSStatus.valueOf(scheduledProcedureStepStatus));
        }
    }

    @SuppressWarnings("unchecked")
    public List<Study> findMWLItemsOfPatient(long pk, boolean latestStudyFirst) {
        return em.createQuery(latestStudyFirst
                    ? "FROM Study s WHERE s.patient.pk=?1 ORDER BY s.studyDateTime DESC"
                    : "FROM Study s WHERE s.patient.pk=?1 ORDER BY s.studyDateTime")
                .setParameter(1, pk)
                .getResultList();
    }

    @SuppressWarnings("unchecked")
    public List<String> selectDistinctModalities() {
        return em.createQuery("SELECT DISTINCT m.modality FROM MWLItem m WHERE m.modality IS NOT NULL ORDER BY m.modality")
                .getResultList();
    }

    @SuppressWarnings("unchecked")
    public List<String> selectDistinctStationAETs() {
        return em.createQuery("SELECT DISTINCT m.scheduledStationAET FROM MWLItem m WHERE m.scheduledStationAET IS NOT NULL ORDER BY m.scheduledStationAET")
        .getResultList();
    }
    
    @SuppressWarnings("unchecked")
    public List<String> selectDistinctStationNames() {
        return em.createQuery("SELECT DISTINCT m.scheduledStationName FROM MWLItem m WHERE m.scheduledStationName IS NOT NULL ORDER BY m.scheduledStationName")
        .getResultList();
    }

    @SuppressWarnings("unchecked")
    public List<String> selectDistinctStatus() {
        return em.createQuery("SELECT DISTINCT m.status FROM MWLItem m WHERE m.status IS NOT NULL ORDER BY m.status")
                .getResultList();
    }

    public Patient getPatient(long pk) {
        return em.find(Patient.class, pk);
    }

    public Patient updatePatient(long pk, DicomObject attrs) {
        Patient patient = em.find(Patient.class, pk);
        patient.setAttributes(attrs);
        return patient;
    }

    public Study getStudy(long pk) {
        return em.find(Study.class, pk);
    }

    public Study updateStudy(long pk, DicomObject attrs) {
        Study study = em.find(Study.class, pk);
        study.setAttributes(attrs);
        return study;
    }

    public Series getSeries(long pk) {
        return em.find(Series.class, pk);
    }

    public Series updateSeries(long pk, DicomObject attrs) {
        Series series = em.find(Series.class, pk);
        series.setAttributes(attrs);
        return series;
    }

    public Instance getInstance(long pk) {
        return em.find(Instance.class, pk);
    }
    

    public Instance updateInstance(long pk, DicomObject attrs) {
        Instance inst = em.find(Instance.class, pk);
        inst.setAttributes(attrs);
        return inst;
    }

    public MPPS getMPPS(long pk) {
        return em.find(MPPS.class, pk);
    }

    public MPPS updateMPPS(long pk, DicomObject attrs) {
        MPPS mpps = em.find(MPPS.class, pk);
        mpps.setAttributes(attrs);
        return mpps;
    }
}
