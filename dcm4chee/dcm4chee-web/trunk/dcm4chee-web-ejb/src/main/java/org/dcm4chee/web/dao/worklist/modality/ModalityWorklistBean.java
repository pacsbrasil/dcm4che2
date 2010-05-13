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

package org.dcm4chee.web.dao.worklist.modality;

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
import org.dcm4chee.archive.entity.MWLItem;
import org.dcm4chee.archive.entity.Study;
import org.dcm4chee.web.dao.util.QueryUtil;
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
            appendPatientNameFilter(ql, filter.getPatientName());
            appendPatientIDFilter(ql, filter.getPatientID());
            appendIssuerOfPatientIDFilter(ql, filter.getIssuerOfPatientID());
            appendAccessionNumberFilter(ql, filter.getAccessionNumber());
            appendStartDateMinFilter(ql, filter.getStartDateMin());
            appendStartDateMaxFilter(ql, filter.getStartDateMax());
            appendModalityFilter(ql, filter.getModality());
            appendScheduledStationAETFilter(ql, filter.getScheduledStationAET());
            appendScheduledStationNameFilter(ql, filter.getScheduledStationName());
            appendScheduledProcedureStepStatus(ql, filter.getScheduledProcedureStepStatus());
        } else {
            if (!QueryUtil.isUniversalMatch(filter.getStudyInstanceUID()))
                ql.append(" AND m.studyInstanceUID = :studyInstanceUID");
        }        
    }
    
    private static void setQueryParameters(Query query, ModalityWorklistFilter filter) {

        if (!filter.isExtendedQuery()) {
            setPatientNameQueryParameter(query, QueryUtil.checkAutoWildcard(filter.getPatientName()));
            setPatientIDQueryParameter(query, QueryUtil.checkAutoWildcard(filter.getPatientID()));
            setIssuerOfPatientIDQueryParameter(query, QueryUtil.checkAutoWildcard(filter.getIssuerOfPatientID()));
            setAccessionNumberQueryParameter(query, QueryUtil.checkAutoWildcard(filter.getAccessionNumber()));
            setStartDateMinQueryParameter(query, filter.getStartDateMin());
            setStartDateMaxQueryParameter(query, filter.getStartDateMax());
            setModalityQueryParameter(query, filter.getModality());
            setScheduledStationAETQueryParameter(query, filter.getScheduledStationAET());
            setScheduledStationNameQueryParameter(query, filter.getScheduledStationName());
            setScheduledProcedureStepStatusQueryParameter(query, filter.getScheduledProcedureStepStatus());
        } else {        
            if (!QueryUtil.isUniversalMatch(filter.getStudyInstanceUID()))
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
        if (patientName!=null) {
            ql.append(" AND p.patientName LIKE :patientName");
        }
    }

    private static void setPatientNameQueryParameter(Query query, String patientName) {

        if (patientName!=null) {
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
        if (patientID!=null) {
            ql.append(containsWildcard(patientID)
                    ? " AND p.patientID LIKE :patientID"
                    : " AND p.patientID = :patientID");
        }
    }

    private static void setPatientIDQueryParameter(Query query,
            String patientID) {
        if (patientID!=null) {
            query.setParameter("patientID", containsWildcard(patientID) 
                    ? toLike(patientID)
                    : patientID);
        }
    }

    private static void appendIssuerOfPatientIDFilter(StringBuilder ql,
            String issuerOfPatientID) {
        if (issuerOfPatientID!=null) {
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
        if (issuerOfPatientID!=null
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
        setStartDateQueryParameter(query, date, "startDateTimeMin");
    }

    private static void setStartDateMaxQueryParameter(Query query, Date date) {
        setStartDateQueryParameter(query, date, "startDateTimeMax");
    }

    private static void setStartDateQueryParameter(Query query,
            Date startDate, String param) {
        if (startDate != null) {
            query.setParameter(param, startDate, TemporalType.TIMESTAMP);
        }
    }

    private static void appendAccessionNumberFilter(StringBuilder ql,
            String accessionNumber) {
        if (accessionNumber!=null) {
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
        if (accessionNumber!=null
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
        if (!QueryUtil.isUniversalMatch(studyInstanceUID)) {
            query.setParameter("studyInstanceUID", studyInstanceUID);
        }
    }

    private static void appendModalityFilter(StringBuilder ql,
            String modality) {
        if (!QueryUtil.isUniversalMatch(modality)) {
            ql.append(" AND m.modality = :modality");
        }
    }

    private static void setModalityQueryParameter(Query query,
            String modality) {
        if (!QueryUtil.isUniversalMatch(modality)) {
            query.setParameter("modality", modality);
        }
    }

    private static void appendScheduledStationAETFilter(StringBuilder ql,
            String scheduledStationAET) {
        if (!QueryUtil.isUniversalMatch(scheduledStationAET)) {
            ql.append(" AND m.scheduledStationAET = :scheduledStationAET");
        }
    }

    private static void setScheduledStationAETQueryParameter(Query query,
            String scheduledStationAET) {
        if (!QueryUtil.isUniversalMatch(scheduledStationAET)) {
            query.setParameter("scheduledStationAET", scheduledStationAET);
        }
    }

    private static void appendScheduledStationNameFilter(StringBuilder ql,
            String scheduledStationName) {
        if (!QueryUtil.isUniversalMatch(scheduledStationName)) {
            ql.append(" AND m.scheduledStationName = :scheduledStationName");
        }
    }

    private static void setScheduledStationNameQueryParameter(Query query,
            String scheduledStationName) {
        if (!QueryUtil.isUniversalMatch(scheduledStationName)) {
            query.setParameter("scheduledStationName", scheduledStationName);
        }
    }

    private static void appendScheduledProcedureStepStatus(StringBuilder ql,
            String scheduledProcedureStepStatus) {
        if (!QueryUtil.isUniversalMatch(scheduledProcedureStepStatus)) {
            ql.append(" AND m.status = :scheduledProcedureStepStatus");
        }
    }

    private static void setScheduledProcedureStepStatusQueryParameter(Query query,
            String scheduledProcedureStepStatus) {        
        if (!QueryUtil.isUniversalMatch(scheduledProcedureStepStatus)) {
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

    public MWLItem updateMWLItem(long pk, DicomObject attrs) {
        MWLItem mwlItem = em.find(MWLItem.class, pk);
        mwlItem.setAttributes(attrs);
        return mwlItem;
    }
}
