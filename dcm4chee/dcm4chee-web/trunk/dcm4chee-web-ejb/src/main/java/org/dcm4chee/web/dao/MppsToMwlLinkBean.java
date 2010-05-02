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

import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.dcm4che2.data.BasicDicomObject;
import org.dcm4che2.data.DicomElement;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.VR;
import org.dcm4chee.archive.entity.MPPS;
import org.dcm4chee.archive.entity.MWLItem;
import org.dcm4chee.archive.entity.Patient;
import org.dcm4chee.archive.entity.Series;
import org.dcm4chee.archive.entity.Study;
import org.dcm4chee.web.dao.util.QueryUtil;
import org.dcm4chee.web.dao.vo.MppsToMwlLinkResult;
import org.jboss.annotation.ejb.LocalBinding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Franz Willer <fwiller@gmail.com>
 * @version $Revision$ $Date$
 * @since Feb 01, 2010
 */

@Stateless
@LocalBinding (jndiBinding=MppsToMwlLinkLocal.JNDI_NAME)
public class MppsToMwlLinkBean implements MppsToMwlLinkLocal {

    private static Logger log = LoggerFactory.getLogger(MppsToMwlLinkBean.class);

    @PersistenceContext(unitName="dcm4chee-arc")
    private EntityManager em;

    @SuppressWarnings("unchecked")
    public MppsToMwlLinkResult linkMppsToMwl(String mppsIUID, String rpId, String spsId, String modifyingSystem, String reason) {
        Query qMwl = em.createQuery("select object(m) from MWLItem m where requestedProcedureID = :rpId and scheduledProcedureStepID = :spsId");
        qMwl.setParameter("rpId", rpId).setParameter("spsId", spsId);
        MWLItem mwl = (MWLItem) qMwl.getSingleResult();
        Query qMpps = em.createQuery("select object(m) from MPPS m where sopInstanceUID = :mppsIUID");
        qMpps.setParameter("mppsIUID", mppsIUID);
        List<MPPS> mppss = (List<MPPS>) qMpps.getResultList();
        return link(mppss, mwl, modifyingSystem, reason);
    }   
            
    @SuppressWarnings("unchecked")
    public MppsToMwlLinkResult linkMppsToMwl(long[] mppsPks, long mwlPk, String modifyingSystem, String reason) {
        Query qMwl = em.createQuery("select object(m) from MWLItem m where pk = :pk");
        qMwl.setParameter("pk", mwlPk);
        MWLItem mwl = (MWLItem) qMwl.getSingleResult();
        Query qMpps = QueryUtil.getQueryForPks(em,"select object(m) from MPPS m where pk ", mppsPks);
        List<MPPS> mppss = (List<MPPS>) qMpps.getResultList(); 
        return link(mppss, mwl, modifyingSystem, reason);
    }

    private MppsToMwlLinkResult link(List<MPPS> mppss, MWLItem mwl, String modifyingSystem, String reason) {
        Patient patMpps;
        Patient mwlPat = mwl.getPatient();
        MppsToMwlLinkResult result = new MppsToMwlLinkResult();
        result.setMwl(mwl);
        for (MPPS mpps : mppss) {
            patMpps = mpps.getPatient();
            if ( patMpps.getPk() != mwlPat.getPk()) {
                log.warn("Patient of MPPS("+patMpps.getPatientID()+") and MWL("+mwlPat.getPatientName()+") are different!");
                result.addStudyToMove(mpps.getSeries().iterator().next().getStudy());
            }
            link(mpps, mwl, modifyingSystem, reason);
            result.addMppsAttributes(mpps);
        }
        return result;
    }
    
    private void link(MPPS mpps, MWLItem mwl, String modifyingSystem, String reason) {
        DicomObject ssa;
        DicomObject mppsAttrs = mpps.getAttributes();
        log.debug("MPPS attrs:{}", mpps);
        DicomObject mwlAttrs = mwl.getAttributes();
        log.debug("MWL attrs:{}",mwlAttrs);
        String rpid = mwlAttrs.getString(Tag.RequestedProcedureID);
        DicomElement spsSq = mwlAttrs.get(Tag.ScheduledProcedureStepSequence);
        String spsid = spsSq.getDicomObject().getString(Tag.ScheduledProcedureStepID);
        String accNo = mwlAttrs.getString(Tag.AccessionNumber);
        DicomElement ssaSQ = mppsAttrs.get(Tag.ScheduledStepAttributesSequence);
        DicomObject origAttrs = new BasicDicomObject();
        mppsAttrs.subSet(new int[]{Tag.ScheduledStepAttributesSequence}).copyTo(origAttrs);
        updateOriginalAttributeSequence(mppsAttrs, origAttrs, modifyingSystem,
                reason);
        String ssaSpsID, studyIUID = null;
        boolean spsNotInList = true;
        for (int i = 0, len = ssaSQ.countItems(); i < len; i++) {
            ssa = ssaSQ.getDicomObject(i);
            if (ssa != null) {
                if (studyIUID == null) {
                    studyIUID = ssa.getString(Tag.StudyInstanceUID);
                }
                ssaSpsID = ssa.getString(Tag.ScheduledProcedureStepID);
                if (ssaSpsID == null || spsid.equals(ssaSpsID)) {
                    ssa.putString(Tag.AccessionNumber, VR.SH, accNo);
                    ssa.putString(Tag.ScheduledProcedureStepID, VR.SH, spsid);
                    ssa.putString(Tag.RequestedProcedureID, VR.SH, rpid);
                    ssa.putString(Tag.StudyInstanceUID, VR.UI, studyIUID);
                    spsNotInList = false;
                }
            }
        }
        if (spsNotInList) {
            ssa = new BasicDicomObject();
            ssaSQ.addDicomObject(ssa);
            DicomObject spsDS = spsSq.getDicomObject();
            ssa.putString(Tag.StudyInstanceUID, VR.UI, studyIUID);
            ssa.putString(Tag.ScheduledProcedureStepID, VR.SH, spsid);
            ssa.putString(Tag.RequestedProcedureID, VR.SH, rpid);
            ssa.putString(Tag.AccessionNumber, VR.SH, accNo);
            ssa.putSequence(Tag.ReferencedStudySequence);
            ssa.putString(Tag.RequestedProcedureID, VR.SH, rpid);
            ssa.putString(Tag.ScheduledProcedureStepDescription, VR.LO, 
                    spsDS.getString(Tag.ScheduledProcedureStepDescription));
            DicomElement mppsSPCSQ = ssa.putSequence(Tag.ScheduledProtocolCodeSequence);
            DicomElement mwlSPCSQ = spsDS.get(Tag.ScheduledProtocolCodeSequence);
            if (mwlSPCSQ != null) {
                DicomObject codeItem;
                for (int i = 0, len = mwlSPCSQ.countItems(); i < len; i++) {
                    codeItem = new BasicDicomObject();
                    mwlSPCSQ.getDicomObject(i).copyTo(codeItem);
                    mppsSPCSQ.addDicomObject(codeItem);
                }
            }
            log.debug("Add new ScheduledStepAttribute item: {}", ssa);
            log.debug("New mppsAttrs:{}", mppsAttrs);
        }
        mpps.setAttributes(mppsAttrs);
        em.merge(mpps);
    }
    
    @SuppressWarnings("unchecked")
    public void updateSeriesAndStudyAttributes(String[] mppsIuids, DicomObject coerce) {
        StringBuilder sb = new StringBuilder("SELECT object(s) FROM Series s WHERE performedProcedureStepInstanceUID");
        QueryUtil.appendIN(sb, mppsIuids.length);
        Query qS = em.createQuery(sb.toString());
        QueryUtil.setParametersForIN(qS, mppsIuids);
        List<Series> seriess = (List<Series>) qS.getResultList();
        DicomObject seriesAndStudyAttrs = null;
        Study study = null;
        for (Series s : seriess) {
            seriesAndStudyAttrs = s.getAttributes(true);
            study = s.getStudy();
            seriesAndStudyAttrs.setParent(study.getAttributes(true));
            seriesAndStudyAttrs.remove(Tag.RequestAttributesSequence);
            log.info("Coerce SeriesAndStudy: orig:"+seriesAndStudyAttrs);
            log.info("Coerce SeriesAndStudy: coerce:"+coerce);
            coerceAttributes(seriesAndStudyAttrs, coerce, null);
            log.info("Set coerced SeriesAndStudy: "+seriesAndStudyAttrs);
            s.setAttributes(seriesAndStudyAttrs);
            em.merge(s);
        }
        study.setAttributes(seriesAndStudyAttrs);
    }

    private void updateOriginalAttributeSequence(DicomObject attrs,
            DicomObject origAttrs, String modifyingSystem, String reason) {
        DicomElement origAttrsSq = attrs.get(Tag.OriginalAttributesSequence);
        if (origAttrsSq == null)
            origAttrsSq = attrs.putSequence(Tag.OriginalAttributesSequence);
        DicomObject origAttrsItem = new BasicDicomObject();
        origAttrsItem.putString(Tag.SourceOfPreviousValues, VR.LO, null);
        origAttrsItem.putDate(Tag.AttributeModificationDateTime, VR.DT, new Date());
        origAttrsItem.putString(Tag.ModifyingSystem, VR.LO, modifyingSystem);
        origAttrsItem.putString(Tag.ReasonForTheAttributeModification, VR.CS, reason);
        DicomElement modSq = origAttrsItem.putSequence(Tag.ModifiedAttributesSequence);
        modSq.addDicomObject(origAttrs);
        origAttrsSq.addDicomObject(origAttrsItem);
    }

    private void coerceAttributes(DicomObject attrs, DicomObject coerce,
            DicomElement parent) {
        boolean coerced = false;
        DicomElement el;
        DicomElement oldEl;
        for (Iterator<DicomElement> it = coerce.iterator(); it.hasNext();) {
            el = it.next();
            oldEl = attrs.get(el.tag());
            if (el.isEmpty()) {
                coerced = oldEl != null && !oldEl.isEmpty();
                if (oldEl == null || coerced) {
                    if ( el.vr()==VR.SQ ) {
                        attrs.putSequence(el.tag());
                    } else {
                        attrs.putBytes(el.tag(), el.vr(), el.getBytes());
                    }
                }
            } else {
                DicomObject item;
                DicomElement sq = oldEl; 
                if (el.vr() == VR.SQ) {
                    coerced = oldEl != null && sq.vr() != VR.SQ;
                    if (oldEl == null || coerced) {
                        sq = attrs.putSequence(el.tag());
                    }
                    for (int i = 0, n = el.countItems(), sqLen = sq.countItems(); i < n; i++) {
                        if (i < sqLen) {
                            item  = sq.getDicomObject(i);
                        } else {
                            item = new BasicDicomObject();
                            sq.addDicomObject(item);
                        }
                        DicomObject coerceItem = el.getDicomObject(i);
                        coerceAttributes(item, coerceItem, el);
                        if (!coerceItem.isEmpty()) {
                            coerced = true;
                        }
                    }
                } else {
                    coerced = oldEl != null && !oldEl.equals(el);
                    if (oldEl == null || coerced) {
                        attrs.putBytes(el.tag(), el.vr(), el.getBytes());
                    }
                }
            }
            if (coerced) {
                log.info(parent == null ? ("Coerce " + oldEl + " to " + el)
                                : ("Coerce " + oldEl + " to " + el
                                        + " in item of " + parent));
            } else {
                if (oldEl == null && log.isDebugEnabled()) {
                    log.debug(parent == null ? ("Add " + el) : ("Add " + el
                            + " in item of " + parent));
                }
            }
        }
    }

    
}
