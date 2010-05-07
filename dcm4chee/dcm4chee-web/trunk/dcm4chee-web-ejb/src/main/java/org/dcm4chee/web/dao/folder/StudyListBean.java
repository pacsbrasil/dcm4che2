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

package org.dcm4chee.web.dao.folder;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.StringTokenizer;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TemporalType;

import org.dcm4che2.data.DicomObject;
import org.dcm4chee.archive.entity.File;
import org.dcm4chee.archive.entity.Instance;
import org.dcm4chee.archive.entity.MPPS;
import org.dcm4chee.archive.entity.Patient;
import org.dcm4chee.archive.entity.Series;
import org.dcm4chee.archive.entity.Study;
import org.jboss.annotation.ejb.LocalBinding;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @version $Revision$ $Date$
 * @since Dec 17, 2008
 */
@Stateless
@LocalBinding (jndiBinding=StudyListLocal.JNDI_NAME)
public class StudyListBean implements StudyListLocal {

    @PersistenceContext(unitName="dcm4chee-arc")
    private EntityManager em;

    public int countStudies(StudyListFilter filter) {
        StringBuilder ql = new StringBuilder(64);
        ql.append("SELECT COUNT(*)");
        appendFromClause(ql, filter);
        appendWhereClause(ql, filter);
        Query query = em.createQuery(ql.toString());
        setQueryParameters(query, filter);
        return ((Number) query.getSingleResult()).intValue();
    }

    @SuppressWarnings("unchecked")
    public List<Object[]> findStudies(StudyListFilter filter, int max, int index) {
        StringBuilder ql = new StringBuilder(64);
        ql.append("SELECT p, s");
        appendFromClause(ql, filter);
        appendWhereClause(ql, filter);
        ql.append(" ORDER BY p.patientName, s.studyDateTime");
        if (filter.isLatestStudiesFirst()) {
            ql.append(" DESC");
        }
        Query query = em.createQuery(ql.toString());
        setQueryParameters(query, filter);
        return query.setMaxResults(max).setFirstResult(index).getResultList();
    }

    private static void appendFromClause(StringBuilder ql, StudyListFilter filter) {
        ql.append(filter.isPatientsWithoutStudies()
                ? " FROM Patient p LEFT JOIN p.studies s "
                : " FROM Patient p INNER JOIN p.studies s");
    }

    private static void appendWhereClause(StringBuilder ql,
            StudyListFilter filter) {
        ql.append(" WHERE p.mergedWith IS NULL");
        if ( !filter.isExtendedStudyQuery() || "*".equals(filter.getStudyInstanceUID()) ) {
            appendPatientNameFilter(ql, filter.getPatientName());
            appendPatientIDFilter(ql, filter.getPatientID());
            appendIssuerOfPatientIDFilter(ql, filter.getIssuerOfPatientID());
            if ( filter.isExtendedPatQuery()) {
                appendPatientBirthDateFilter(ql, filter.getBirthDateMin(), filter.getBirthDateMax());
            }
            appendAccessionNumberFilter(ql, filter.getAccessionNumber());
            appendStudyDateMinFilter(ql, filter.getStudyDateMin());
            appendStudyDateMaxFilter(ql, filter.getStudyDateMax());
        } else {
            ql.append(" AND s.studyInstanceUID = :studyInstanceUID");
        }
        appendModalityFilter(ql, filter.getModality());
        appendSourceAETFilter(ql, filter.getSourceAET());
    }

    private static void setQueryParameters(Query query, StudyListFilter filter) {
        if ( !filter.isExtendedStudyQuery() || "*".equals(filter.getStudyInstanceUID()) ) {
            setPatientNameQueryParameter(query, filter.getPatientName());
            setPatientIDQueryParameter(query, filter.getPatientID());
            setIssuerOfPatientIDQueryParameter(query, filter.getIssuerOfPatientID());
            if ( filter.isExtendedPatQuery()) {
                setPatientBirthDateQueryParameter(query, filter.getBirthDateMin(), filter.getBirthDateMax());
            }
            setAccessionNumberQueryParameter(query, filter.getAccessionNumber());
            setStudyDateMinQueryParameter(query, filter.getStudyDateMin());
            setStudyDateMaxQueryParameter(query, filter.getStudyDateMax());
        } else {
            setStudyInstanceUIDQueryParameter(query, filter.getStudyInstanceUID());
        }
        setModalityQueryParameter(query, filter.getModality());
        setSourceAETQueryParameter(query, filter.getSourceAET());
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

    private static void setPatientNameQueryParameter(Query query,
            String patientName) {
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

    private static void appendPatientBirthDateFilter(StringBuilder ql,
            String minDate, String maxDate) {
        if (!"*".equals(minDate)) {
            if ("*".equals(maxDate)) {
                ql.append(" AND p.patientBirthDate >= :birthdateMin");
            } else {
                ql.append(" AND p.patientBirthDate BETWEEN :birthdateMin AND :birthdateMax");
                
            }
        } else if ( !"*".equals(maxDate)) {
            ql.append(" AND p.patientBirthDate <= :birthdateMax");
        }
    }
    private static void setPatientBirthDateQueryParameter(Query query,
            String minDate, String maxDate) {
        if ( !"*".equals(minDate))
            query.setParameter("birthdateMin", normalizeDate(minDate));
        if ( !"*".equals(maxDate))
            query.setParameter("birthdateMax", normalizeDate(maxDate));
    }

    private static String normalizeDate(String date) {
        return date.substring(0,4)+date.substring(5,7)+date.substring(8);
    }

    private static void appendStudyDateMinFilter(StringBuilder ql,
            String studyDate) {
        if (!"*".equals(studyDate)) {
            ql.append(" AND s.studyDateTime >= :studyDateTimeMin");
        }
    }

    private static void appendStudyDateMaxFilter(StringBuilder ql,
            String studyDate) {
        if (!"*".equals(studyDate)) {
            ql.append(" AND s.studyDateTime <= :studyDateTimeMax");
        }
    }

    private static void setStudyDateMinQueryParameter(Query query,
            String studyDate) {
        setStudyDateQueryParameter(query, studyDate, "studyDateTimeMin", false);
    }

    private static void setStudyDateMaxQueryParameter(Query query,
            String studyDate) {
        setStudyDateQueryParameter(query, studyDate, "studyDateTimeMax", true);
    }

    private static void setStudyDateQueryParameter(Query query,
            String studyDate, String param, boolean max) {
        if (!"*".equals(studyDate)) {
            int year = Integer.parseInt(studyDate.substring(0,4));
            int month = Integer.parseInt(studyDate.substring(5,7))-1;
            int day = Integer.parseInt(studyDate.substring(8,10));
            GregorianCalendar cal = new GregorianCalendar(year, month, day);
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
                    ? " AND s.accessionNumber IS NULL"
                    : isMustNotNull(accessionNumber)
                    ? " AND s.accessionNumber IS NOT NULL" 
                    : containsWildcard(accessionNumber)
                    ? " AND s.accessionNumber LIKE :accessionNumber"
                    : " AND s.accessionNumber = :accessionNumber");
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
            ql.append(" AND EXISTS (SELECT ser FROM s.series ser WHERE ser.modality = :modality)");
        }
    }

    private static void setModalityQueryParameter(Query query,
            String modality) {
        if (!"*".equals(modality)) {
            query.setParameter("modality", modality);
        }
    }

    private static void appendSourceAETFilter(StringBuilder ql,
            String sourceAET) {
        if (!"*".equals(sourceAET)) {
            ql.append(" AND EXISTS (SELECT ser FROM s.series ser WHERE ser.sourceAET = :sourceAET)");
        }
    }

    private static void setSourceAETQueryParameter(Query query,
            String sourceAET) {
        if (!"*".equals(sourceAET)) {
            query.setParameter("sourceAET", sourceAET);
        }
    }

    @SuppressWarnings("unchecked")
    public List<Study> findStudiesOfPatient(long pk, boolean latestStudyFirst) {
        return em.createQuery(latestStudyFirst
                    ? "FROM Study s WHERE s.patient.pk=?1 ORDER BY s.studyDateTime DESC"
                    : "FROM Study s WHERE s.patient.pk=?1 ORDER BY s.studyDateTime")
                .setParameter(1, pk)
                .getResultList();
    }

    @SuppressWarnings("unchecked")
    public List<Series> findSeriesOfStudy(long pk) {
        return em.createQuery("FROM Series s LEFT JOIN FETCH s.modalityPerformedProcedureStep WHERE s.study.pk=?1 ORDER BY s.pk")
                .setParameter(1, pk)
                .getResultList();
    }

    @SuppressWarnings("unchecked")
    public List<Series> findSeriesOfMpps(String uid) {
        return em.createQuery("FROM Series s WHERE s.performedProcedureStepInstanceUID=?1 ORDER BY s.pk")
                .setParameter(1, uid)
                .getResultList();
    }

    @SuppressWarnings("unchecked")
    public List<Instance> findInstancesOfSeries(long pk) {
        return em.createQuery("FROM Instance i LEFT JOIN FETCH i.media WHERE i.series.pk=?1 ORDER BY i.pk")
                .setParameter(1, pk)
                .getResultList();
   }

    @SuppressWarnings("unchecked")
    public List<File> findFilesOfInstance(long pk) {
        return em.createQuery("FROM File f JOIN FETCH f.fileSystem WHERE f.instance.pk=?1 ORDER BY f.pk")
                .setParameter(1, pk)
                .getResultList();
    }

    @SuppressWarnings("unchecked")
    public List<String> selectDistinctSourceAETs() {
        return em.createQuery("SELECT DISTINCT s.sourceAET FROM Series s WHERE s.sourceAET IS NOT NULL ORDER BY s.sourceAET")
                .getResultList();
    }

    @SuppressWarnings("unchecked")
    public List<String> selectDistinctModalities() {
        return em.createQuery("SELECT DISTINCT s.modality FROM Series s WHERE s.modality IS NOT NULL ORDER BY s.modality")
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
