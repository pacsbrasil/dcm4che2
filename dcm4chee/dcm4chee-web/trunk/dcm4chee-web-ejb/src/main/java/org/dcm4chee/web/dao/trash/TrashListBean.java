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

package org.dcm4chee.web.dao.trash;

import java.util.List;
import java.util.StringTokenizer;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.dcm4che2.data.DicomObject;
import org.dcm4chee.archive.entity.BaseEntity;
import org.dcm4chee.archive.entity.PrivateFile;
import org.dcm4chee.archive.entity.PrivateInstance;
import org.dcm4chee.archive.entity.PrivatePatient;
import org.dcm4chee.archive.entity.PrivateSeries;
import org.dcm4chee.archive.entity.PrivateStudy;
import org.dcm4chee.web.dao.util.QueryUtil;
import org.jboss.annotation.ejb.LocalBinding;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @version $Revision$ $Date$
 * @since Dec 17, 2008
 */
@Stateless
@LocalBinding (jndiBinding=TrashListLocal.JNDI_NAME)
public class TrashListBean implements TrashListLocal {

    @PersistenceContext(unitName="dcm4chee-arc")
    private EntityManager em;

    public int countStudies(TrashListFilter filter) {
        StringBuilder ql = new StringBuilder(64);
        ql.append("SELECT COUNT(*)");
        appendFromClause(ql, filter);
        appendWhereClause(ql, filter);
        Query query = em.createQuery(ql.toString());
        setQueryParameters(query, filter);
        return ((Number) query.getSingleResult()).intValue();
    }

    @SuppressWarnings("unchecked")
    public List<Object[]> findStudies(TrashListFilter filter, int max, int index) {
        StringBuilder ql = new StringBuilder(64);
        ql.append("SELECT p, s");
        appendFromClause(ql, filter);
        appendWhereClause(ql, filter);
        ql.append(" ORDER BY p.patientName");
        Query query = em.createQuery(ql.toString());
        setQueryParameters(query, filter);
        return query.setMaxResults(max).setFirstResult(index).getResultList();
    }

    private static void appendFromClause(StringBuilder ql, TrashListFilter filter) {
        ql.append(filter.isPatientsWithoutStudies()
                ? " FROM PrivatePatient p LEFT JOIN p.studies s "
                : " FROM PrivatePatient p INNER JOIN p.studies s");
    }

    private static void appendWhereClause(StringBuilder ql,
            TrashListFilter filter) {
        ql.append(" WHERE p.privateType = 1");
        if ( QueryUtil.isUniversalMatch(filter.getStudyInstanceUID()) ) {
            appendPatientNameFilter(ql, QueryUtil.checkAutoWildcard(filter.getPatientName()));
            appendPatientIDFilter(ql, QueryUtil.checkAutoWildcard(filter.getPatientID()));
            appendIssuerOfPatientIDFilter(ql, QueryUtil.checkAutoWildcard(filter.getIssuerOfPatientID()));
            appendAccessionNumberFilter(ql, QueryUtil.checkAutoWildcard(filter.getAccessionNumber()));
        } else {
            ql.append(" AND s.studyInstanceUID = :studyInstanceUID");
        }
        appendSourceAETFilter(ql, filter.getSourceAET());
    }

    private static void setQueryParameters(Query query, TrashListFilter filter) {
        if ( QueryUtil.isUniversalMatch(filter.getStudyInstanceUID()) ) {
            setPatientNameQueryParameter(query, QueryUtil.checkAutoWildcard(filter.getPatientName()));
            setPatientIDQueryParameter(query, QueryUtil.checkAutoWildcard(filter.getPatientID()));
            setIssuerOfPatientIDQueryParameter(query, QueryUtil.checkAutoWildcard(filter.getIssuerOfPatientID()));
            setAccessionNumberQueryParameter(query, QueryUtil.checkAutoWildcard(filter.getAccessionNumber()));
        } else {
            setStudyInstanceUIDQueryParameter(query, filter.getStudyInstanceUID());
        }
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
        if (patientName!=null) {
            ql.append(" AND p.patientName LIKE :patientName");
        }
    }

    private static void setPatientNameQueryParameter(Query query,
            String patientName) {
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

    private static void appendAccessionNumberFilter(StringBuilder ql,
            String accessionNumber) {
        if (accessionNumber!=null) {
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


    private static void appendSourceAETFilter(StringBuilder ql,
            String sourceAET) {
        if (!QueryUtil.isUniversalMatch(sourceAET)) {
            ql.append(" AND EXISTS (SELECT ser FROM s.series ser WHERE ser.sourceAET = :sourceAET)");
        }
    }

    private static void setSourceAETQueryParameter(Query query,
            String sourceAET) {
        if (!QueryUtil.isUniversalMatch(sourceAET)) {
            query.setParameter("sourceAET", sourceAET);
        }
    }

    @SuppressWarnings("unchecked")
    public List<PrivateStudy> findStudiesOfPatient(long pk) {
        return em.createQuery("FROM PrivateStudy s WHERE s.patient.pk=?1")
                .setParameter(1, pk)
                .getResultList();
    }

    @SuppressWarnings("unchecked")
    public List<PrivateSeries> findSeriesOfStudy(long pk) {
        return em.createQuery("FROM PrivateSeries s WHERE s.study.pk=?1 ORDER BY s.pk")
                .setParameter(1, pk)
                .getResultList();
    }

    @SuppressWarnings("unchecked")
    public List<PrivateInstance> findInstancesOfSeries(long pk) {
        return em.createQuery("FROM PrivateInstance i WHERE i.series.pk=?1 ORDER BY i.pk")
                .setParameter(1, pk)
                .getResultList();
   }

    @SuppressWarnings("unchecked")
    public List<String> selectDistinctSourceAETs() {
        return em.createQuery("SELECT DISTINCT s.sourceAET FROM Series s WHERE s.sourceAET IS NOT NULL ORDER BY s.sourceAET")
                .getResultList();
    }

    public PrivatePatient getPatient(long pk) {
        return em.find(PrivatePatient.class, pk);
    }

    public PrivateStudy getStudy(long pk) {
        return em.find(PrivateStudy.class, pk);
    }

    public PrivateSeries getSeries(long pk) {
        return em.find(PrivateSeries.class, pk);
    }

    public PrivateInstance getInstance(long pk) {
        return em.find(PrivateInstance.class, pk);
    }
    
    public void removeTrashPatients(List<Long> pks) {
        for (Long pk : pks)
            removeTrashEntity(getPatient(pk));
    }

    public void removeTrashStudies(List<Long> pks) {
        for (Long pk : pks)
            removeTrashEntity(getStudy(pk));
    }

    public void removeTrashSeries(List<Long> pks) {
        for (Long pk : pks)
            removeTrashEntity(getSeries(pk));
    }

    public void removeTrashInstances(List<Long> pks) {
        for (Long pk : pks)
            removeTrashEntity(getInstance(pk));
    }
    
    private void removeTrashEntity(BaseEntity entity) {
        if (entity == null) return;
        else {
            if (entity instanceof PrivatePatient) {
                PrivatePatient pp = (PrivatePatient) entity;
                for (PrivateStudy pst : pp.getStudies())
                    removeTrashEntity(pst);
                em.remove(pp);
            } else if (entity instanceof PrivateStudy) {
                PrivateStudy pst = (PrivateStudy) entity;
                for (PrivateSeries pse : pst.getSeries())
                    removeTrashEntity(pse);
                PrivatePatient p = pst.getPatient();
                em.remove(pst);
                if (p.getStudies().size() <= 1)
                    em.remove(p);
            } else if (entity instanceof PrivateSeries) {
                PrivateSeries pse = (PrivateSeries) entity;
                for (PrivateInstance pi : pse.getInstances())
                    removeTrashEntity(pi);
                PrivateStudy pst = pse.getStudy();
                em.remove(pse);
                if (pst.getSeries().size() <= 1)
                    em.remove(pst);
            } else if (entity instanceof PrivateInstance) {
                PrivateInstance pi = (PrivateInstance) entity;
                for (PrivateFile pf : pi.getFiles()) {
                    pf.setInstance(null);
                    em.merge(pf);
                }
                PrivateSeries pse = pi.getSeries();
                em.remove(pi);
                if (pse.getInstances().size() <= 1)
                    em.remove(pse);
            } else return;
        }
    }
    
    public void removeTrashAll() {
        em.createQuery("UPDATE PrivateFile p SET p.instance = Null").executeUpdate();
        em.createQuery("DELETE FROM PrivateInstance pi").executeUpdate();
        em.createQuery("DELETE FROM PrivateSeries pse").executeUpdate();
        em.createQuery("DELETE FROM PrivateStudy pst").executeUpdate();
        em.createQuery("DELETE FROM PrivatePatient pp").executeUpdate();
    }

    @SuppressWarnings("unchecked")
    public List<PrivateFile> getFilesForEntity(long pk, Class<? extends BaseEntity> clazz) {
        
        String query = "SELECT DISTINCT f FROM PrivateFile f LEFT JOIN FETCH f.fileSystem fs LEFT JOIN FETCH f.instance i ";

        if (clazz.equals(PrivateInstance.class))
            query += "WHERE i.pk = :pk";
        else {
            query += "LEFT JOIN i.series se ";
            
            if (clazz.equals(PrivateSeries.class))
                query += "WHERE se.pk = :pk";
            else {
                query += "LEFT JOIN se.study st";
                if (clazz.equals(PrivateStudy.class))
                    query += " WHERE st.pk = :pk";
                else if (clazz.equals(PrivatePatient.class))
                    query += "LEFT JOIN st.patient p WHERE p.pk = :pk";
                else return null;
            }
        }
        
        return em.createQuery(query)
            .setParameter("pk", pk)
            .getResultList();
    }

    public DicomObject getDicomAttributes(long filePk) {
        PrivateFile pf = em.find(PrivateFile.class, filePk);
        DicomObject dio = pf.getInstance().getAttributes();
        pf.getInstance().getSeries().getAttributes().copyTo(dio);
        pf.getInstance().getSeries().getStudy().getAttributes().copyTo(dio);
        pf.getInstance().getSeries().getStudy().getPatient().getAttributes().copyTo(dio);
        return dio;
    }
}
