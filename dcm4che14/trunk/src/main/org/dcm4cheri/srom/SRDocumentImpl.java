/*$Id$*/
/*****************************************************************************
 *                                                                           *
 *  Copyright (c) 2001,2002 by TIANI MEDGRAPH AG <gunter.zeilinger@tiani.com>*
 *                                                                           *
 *  This file is part of dcm4che.                                            *
 *                                                                           *
 *  This library is free software; you can redistribute it and/or modify it  *
 *  under the terms of the GNU Lesser General Public License as published    *
 *  by the Free Software Foundation; either version 2 of the License, or     *
 *  (at your option) any later version.                                      *
 *                                                                           *
 *  This library is distributed in the hope that it will be useful, but      *
 *  WITHOUT ANY WARRANTY; without even the implied warranty of               *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU        *
 *  Lesser General Public License for more details.                          *
 *                                                                           *
 *  You should have received a copy of the GNU Lesser General Public         *
 *  License along with this library; if not, write to the Free Software      *
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA  *
 *                                                                           *
 *****************************************************************************/

package org.dcm4cheri.srom;

import org.dcm4che.srom.*;
import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmElement;
import org.dcm4che.data.DcmValueException;
import org.dcm4che.dict.DictionaryFactory;
import org.dcm4che.dict.Tags;
import org.dcm4che.dict.UIDDictionary;

import java.util.*;

/**
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0
 */
class SRDocumentImpl extends KeyObjectImpl
        implements org.dcm4che.srom.SRDocument {
    // Constants -----------------------------------------------------
    private static UIDDictionary uidDict =
            DictionaryFactory.getInstance().getDefaultUIDDictionary( );   
    
    // Attributes ----------------------------------------------------
    private boolean complete = false;
    private String completionFlagDescription = null;
    private boolean verified = false;
    private final List verifications = new LinkedList();

    private final List predecessorDocuments = new LinkedList();
    private final List procedureCodes = new LinkedList();
    private final List otherEvidence = new LinkedList();

        
    // Constructors --------------------------------------------------
    SRDocumentImpl(Patient patient, Study study, Series series,
            Equipment equipment, String sopClassUID, String sopInstanceUID,
            int instanceNumber, Date obsDateTime, Template template,
            Code title, boolean separate) {
        super(patient, study, series, equipment, sopClassUID, sopInstanceUID,
                instanceNumber, obsDateTime, template, title, separate, "SR");
    }

    static SRDocument newSRDocument(Dataset ds) throws DcmValueException {
        if (!"CONTAINER".equals(ds.getString(Tags.ValueType))) {
            throw new IllegalArgumentException(ds.getString(Tags.ValueType));
        }
        SRDocumentImpl sr = new SRDocumentImpl(new PatientImpl(ds),
                new StudyImpl(ds),  new SeriesImpl(ds),  new EquipmentImpl(ds),
                ds.getString(Tags.SOPClassUID),
                ds.getString(Tags.SOPInstanceUID),
                ds.getInt(Tags.InstanceNumber, -1),
                ds.getDate(Tags.ObservationDateTime),
                new TemplateImpl(ds.getNestedDataset(Tags.ContentTemplateSeq)),
                new CodeImpl(ds.getNestedDataset(Tags.ConceptNameCodeSeq)),
                "SEPARATE".equals(ds.getString(Tags.ContinuityOfContent)));
        sr.init(ds);
        return sr;
    }

    protected void init(Dataset ds) throws DcmValueException {
        super.init(ds);
        complete = "COMPLETE".equals(ds.getString(Tags.CompletionFlag));
        completionFlagDescription = ds.getString(Tags.CompletionFlagDescription);
        verified = "VERIFIED".equals(ds.getString(Tags.VerificationFlag));
        initVerifications(ds.get(Tags.VerifyingObserverSeq));
        initSOPInstanceRefList(predecessorDocuments,
                ds.get(Tags.PredecessorDocumentsSeq));
        setProcedureCodes(
                CodeImpl.newCodes(ds.get(Tags.PerformedProcedureCodeSeq)));
        initSOPInstanceRefList(this.otherEvidence,
                ds.get(Tags.PertinentOtherEvidenceSeq));
    }
    
    private void initVerifications(DcmElement sq) throws DcmValueException {
        if (sq == null)
            return;
        
        for (int i = 0, n = sq.vm(); i < n; ++i) {
            addVerification(new VerificationImpl(sq.getDataset(i)));
        }
    }
    
    // Methodes --------------------------------------------------------
    public String toString() {
        return uidDict.toString(sopClassUID) 
                + "[" + getName().getCodeMeaning()
                 + "," + sopInstanceUID
                 + ",#" + instanceNumber
                 + "," + getContentDateTime()
                 + (complete ? ",complete" : ",partial")
                 + (verified ? ",verified" : ",unverified")
                 + "]";
    }

    public boolean isComplete() {
        return complete;
    }

    public void setComplete(boolean complete) {
        this.complete = complete;
    }
    
    public String getCompletionFlagDescription() {
        return completionFlagDescription;
    }
    
    public void setCompletionFlagDescription(String desc) {
        this.completionFlagDescription = desc;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }       
    
    public Verification[] getVerifications() {
        return (Verification[])verifications.toArray(
                VerificationImpl.EMPTY_ARRAY);
    }
    
    public void setVerifications(Verification[] newVerifs) {
        List list = checkList(newVerifs);
        verifications.clear();
        verifications.addAll(list);
    }
    
    public boolean addVerification(Verification verification) {
        if (verification == null)
            throw new NullPointerException();
        
        return verifications.add(verification);
    }
    
    public boolean removeVerification(Verification verification) {
        return verifications.remove(verification);
    }

    public SOPInstanceRef[] getPredecessorDocuments() {
        return (SOPInstanceRef[])predecessorDocuments.toArray(
                SOPInstanceRefImpl.EMPTY_ARRAY);
    }
    
    public void setPredecessorDocuments(SOPInstanceRef[] refs) {
        List list = checkList(refs);
        predecessorDocuments.clear();
        predecessorDocuments.addAll(list);
    }
    
    public boolean addPredecessorDocument(SOPInstanceRef ref) {
        if (ref == null)
            throw new NullPointerException();

        if (predecessorDocuments.indexOf(ref) != -1)
            return false;

        return predecessorDocuments.add(ref);
    }
    
    public boolean removePredecessorDocument(RefSOP ref) {
        return predecessorDocuments.remove(ref);
    }

    public Code[] getProcedureCodes() {
        return (Code[])procedureCodes.toArray(CodeImpl.EMPTY_ARRAY);
    }
    
    public void setProcedureCodes(Code[] newCodes) {
        List list = checkList(newCodes);
        procedureCodes.clear();
        procedureCodes.addAll(list);
    }
    
    public boolean addProcedureCode(Code code) {
        if (code == null)
            throw new NullPointerException();
        
        if (procedureCodes.indexOf(code) != -1)
            return false;

        return procedureCodes.add(code);
    }
    
    public boolean removeProcedureCode(Code code) {
        return procedureCodes.remove(code);
    }

    public SOPInstanceRef[] getOtherEvidence() {
        return (SOPInstanceRef[])otherEvidence.toArray(
                SOPInstanceRefImpl.EMPTY_ARRAY);

    }
    
    public SOPInstanceRef findOtherEvidence(RefSOP ref) {
        return findSOPInstanceRef(otherEvidence, ref);
    }

    public void setOtherEvidence(SOPInstanceRef[] refs) {
        List list = checkList(refs);
        otherEvidence.clear();
        otherEvidence.addAll(list);
    }
    
    public boolean addOtherEvidence(SOPInstanceRef ref) {
        if (ref == null)
            throw new NullPointerException();

        if (otherEvidence.indexOf(ref) != -1)
            return false;

        return otherEvidence.add(ref);
    }
    
    public boolean removeOtherEvidence(RefSOP ref) {
        return otherEvidence.remove(ref);
    }

    public void toDataset(Dataset ds) {
        super.toDataset(ds);
        ds.setCS(Tags.CompletionFlag, complete ? "COMPLETE" : "PARTIAL");
        ds.setLO(Tags.CompletionFlagDescription, completionFlagDescription);
        ds.setCS(Tags.VerificationFlag, verified ? "VERIFIED" : "UNVERIFIED");
        if (!verifications.isEmpty()) {
            DcmElement sq = ds.setSQ(Tags.VerifyingObserverSeq);
            for (Iterator it =verifications.iterator(); it.hasNext();) {
                ((Verification)it.next()).toDataset(sq.addNewDataset());
            }
        }        
        if (!predecessorDocuments.isEmpty()) {
            sopInstanceRefListToSQ(predecessorDocuments,
                    ds.setSQ(Tags.PredecessorDocumentsSeq));
        }
        if (!procedureCodes.isEmpty()) {
            DcmElement sq = ds.setSQ(Tags.PerformedProcedureCodeSeq);
            for (Iterator it = procedureCodes.iterator(); it.hasNext();) {
                ((Code)it.next()).toDataset(sq.addNewDataset());
            }
        }        
        if (!otherEvidence.isEmpty()) {
            sopInstanceRefListToSQ(otherEvidence,
                    ds.setSQ(Tags.PertinentOtherEvidenceSeq));
        }
    }
}
