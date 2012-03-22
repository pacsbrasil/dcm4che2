package org.dcm4chee.web.war.tc;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.Component;
import org.dcm4che2.data.BasicDicomObject;
import org.dcm4che2.data.DicomElement;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.VR;
import org.dcm4che2.io.DicomInputStream;
import org.dcm4chee.archive.entity.Code;
import org.dcm4chee.web.dao.tc.TCQueryFilterKey;
import org.dcm4chee.web.dao.tc.TCQueryFilterValue.AcquisitionModality;
import org.dcm4chee.web.dao.tc.TCQueryFilterValue.Category;
import org.dcm4chee.web.dao.tc.TCQueryFilterValue.Level;
import org.dcm4chee.web.dao.tc.TCQueryFilterValue.PatientSex;
import org.dcm4chee.web.dao.tc.TCQueryFilterValue.YesNo;
import org.dcm4chee.web.war.folder.delegate.TarRetrieveDelegate;
import org.dcm4chee.web.war.tc.keywords.TCKeyword;
import org.dcm4chee.web.war.tc.keywords.TCKeywordCatalogueProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Bernhard Ableitinger <bernhard.ableitinger@agfa.com>
 * @version $Revision$ $Date$
 * @since May 04, 2011
 */
public class TCObject implements Serializable {

    private static final long serialVersionUID = 1L;

    private final static Logger log = LoggerFactory.getLogger(TCObject.class);
    
    private String iuid;

    protected String abstr;

    protected List<AcquisitionModality> acquisitionModalities;

    protected String anatomy;

    protected String authorAffiliation;

    protected String authorContact;

    protected String authorName;

    protected Category category;

    protected String diagnosis;

    protected YesNo diagnosisConfirmed;

    protected String diffDiagnosis;

    protected String discussion;

    protected String finding;

    protected String history;

    protected List<String> keywords;

    protected Level level;

    protected String organSystem;

    protected String pathology;
    
    protected Integer patientAge;

    protected PatientSex patientSex;

    protected String patientSpecies;

    protected List<String> bibliographicReferences;

    protected String title;

    protected DicomCode anatomyCode;

    protected DicomCode diagnosisCode;

    protected DicomCode diffDiagnosisCode;

    protected DicomCode findingCode;

    protected DicomCode keywordCode;

    protected DicomCode pathologyCode;

    private List<TCReferencedStudy> studyRefs;
    
    private List<TCReferencedInstance> instanceRefs;
    
    private List<TCReferencedInstance> imageRefs;
    
    protected TCObject(DicomObject object) {
        parse(object);
    }

    public static TCObject create(TCModel model) throws IOException {
        String fsID = model.getFileSystemId();
        String fileID = model.getFileId();

        DicomInputStream dis = null;

        try {
            dis = new DicomInputStream(
                    fsID.startsWith("tar:") ? TarRetrieveDelegate.getInstance()
                            .retrieveFileFromTar(fsID, fileID)
                            : new java.io.File(fsID, fileID));

            return new TCObject(dis.readDicomObject());
        } finally {
            if (dis != null) {
                dis.close();
            }
        }
    }
    
    public String getUID()
    {
        return iuid;
    }
    
    public String getAbstr() {
        return abstr;
    }

    public List<AcquisitionModality> getAcquisitionModalities() {
        return acquisitionModalities;
    }

    public String getAnatomy() {
        return anatomy;
    }

    public String getAuthorAffiliation() {
        return authorAffiliation;
    }

    public String getAuthorContact() {
        return authorContact;
    }

    public String getAuthorName() {
        return authorName;
    }

    public Category getCategory() {
        return category;
    }

    public String getDiagnosis() {
        return diagnosis;
    }

    public YesNo getDiagnosisConfirmed() {
        return diagnosisConfirmed;
    }

    public String getDiffDiagnosis() {
        return diffDiagnosis;
    }

    public String getDiscussion() {
        return discussion;
    }

    public String getFinding() {
        return finding;
    }

    public String getHistory() {
        return history;
    }

    public List<String> getKeywords() {
        return keywords;
    }

    public Level getLevel() {
        return level;
    }

    public String getOrganSystem() {
        return organSystem;
    }

    public String getPathology() {
        return pathology;
    }
    
    public Integer getPatientAge() {
        return patientAge;
    }

    public PatientSex getPatientSex() {
        return patientSex;
    }

    public String getPatientSpecies() {
        return patientSpecies;
    }

    public List<String> getBibliographicReferences() {
        if (bibliographicReferences != null) {
            return Collections.unmodifiableList(bibliographicReferences);
        } else {
            return Collections.emptyList();
        }
    }

    public String getTitle() {
        return title;
    }

    public DicomCode getAnatomyCode() {
        return anatomyCode;
    }

    public DicomCode getDiagnosisCode() {
        return diagnosisCode;
    }

    public DicomCode getDiffDiagnosisCode() {
        return diffDiagnosisCode;
    }

    public DicomCode getFindingCode() {
        return findingCode;
    }

    public DicomCode getKeywordCode() {
        return keywordCode;
    }

    public DicomCode getPathologyCode() {
        return pathologyCode;
    }

    public List<TCReferencedStudy> getReferencedStudies() {
        if (studyRefs != null) {
            return Collections.unmodifiableList(studyRefs);
        }

        return Collections.emptyList();
    }
    
    public List<TCReferencedInstance> getReferencedInstances()
    {
        if (instanceRefs==null)
        {
            instanceRefs = new ArrayList<TCReferencedInstance>();
            
            List<TCReferencedStudy> studies = getReferencedStudies();
            for (TCReferencedStudy study : studies)
            {
                for (TCReferencedSeries series : study.getSeries())
                {
                    for (TCReferencedInstance instance : series.getInstances())
                    {
                        if (!instanceRefs.contains(instance))
                        {
                            instanceRefs.add(instance);
                        }
                    }
                }
            }
        }
        
        return instanceRefs;
    }
    
    public List<TCReferencedInstance> getReferencedImages()
    {
        if (imageRefs==null)
        {
            imageRefs = new ArrayList<TCReferencedInstance>();
            
            List<TCReferencedStudy> studies = getReferencedStudies();

            for (TCReferencedStudy study : studies)
            {
                for (TCReferencedSeries series : study.getSeries())
                {
                    for (TCReferencedInstance image : series.getImages())
                    {
                        if (!imageRefs.contains(image))
                        {
                            imageRefs.add(image);
                        }
                    }
                }
            }
        }
        
        return imageRefs;
    }
    
    public Object getValue(TCQueryFilterKey key) {
        Object value = null;
        
        TCKeywordCatalogueProvider catProv = TCKeywordCatalogueProvider.getInstance();
        
        if (TCQueryFilterKey.Abstract.equals(key)) {
            value = getAbstr();
        } else if (TCQueryFilterKey.AcquisitionModality.equals(key)) {
            value = concatStrings(getAcquisitionModalities());
        } else if (TCQueryFilterKey.Anatomy.equals(key)) {
            if (catProv == null || !catProv.hasCatalogue(key)) {
                if (getAnatomy() != null) {
                    value = getAnatomy();
                }
            }

            if (value == null) {
                value = getAnatomyCode();
            }
        } else if (TCQueryFilterKey.AuthorAffiliation.equals(key)) {
            value = getAuthorAffiliation();
        } else if (TCQueryFilterKey.AuthorContact.equals(key)) {
            value = getAuthorContact();
        } else if (TCQueryFilterKey.AuthorName.equals(key)) {
            value = getAuthorName();
        } else if (TCQueryFilterKey.BibliographicReference.equals(key)) {
            value = getBibliographicReferences();
        } else if (TCQueryFilterKey.Category.equals(key)) {
            value = getCategory() != null ? getCategory() : null;
        } else if (TCQueryFilterKey.DiagnosisConfirmed.equals(key)) {
            value = getDiagnosisConfirmed() != null ? getDiagnosisConfirmed() : null;
        } else if (TCQueryFilterKey.Diagnosis.equals(key)) {
            if (catProv == null || !catProv.hasCatalogue(key)) {
                if (getDiagnosis() != null) {
                    value = getDiagnosis();
                }
            }

            if (value == null) {
                value = getDiagnosisCode();
            }
        } else if (TCQueryFilterKey.DifferentialDiagnosis.equals(key)) {
            if (catProv == null || !catProv.hasCatalogue(key)) {
                if (getDiffDiagnosis() != null) {
                    value = getDiffDiagnosis();
                }
            }

            if (value == null) {
                value = getDiffDiagnosisCode();
            }
        } else if (TCQueryFilterKey.Discussion.equals(key)) {
            value = getDiscussion();
        } else if (TCQueryFilterKey.Finding.equals(key)) {
            if (catProv == null || !catProv.hasCatalogue(key)) {
                if (getFinding() != null) {
                    value = getFinding();
                }
            }

            if (value == null) {
                value = getFindingCode();
            }
        } else if (TCQueryFilterKey.History.equals(key)) {
            value = getHistory();
        } else if (TCQueryFilterKey.Keyword.equals(key)) {
            if (catProv == null || !catProv.hasCatalogue(key)) {
                if (getKeywords() != null) {
                    value = concatStrings(getKeywords());
                }
            }

            if (value == null) {
                value = getKeywordCode();
            }
        } else if (TCQueryFilterKey.Level.equals(key)) {
            value = getLevel() != null ? getLevel() : null;
        } else if (TCQueryFilterKey.OrganSystem.equals(key)) {
            value = getOrganSystem();

        } else if (TCQueryFilterKey.Pathology.equals(key)) {
            if (catProv == null || !catProv.hasCatalogue(key)) {
                if (getPathology() != null) {
                    value = getPathology();
                }
            }

            if (value == null) {
                value = getPathologyCode();
            }
        } else if (TCQueryFilterKey.PatientAge.equals(key)) {
            value = getPatientAge() != null ? getPatientAge() : null;
        } else if (TCQueryFilterKey.PatientSex.equals(key)) {
            value = getPatientSex() != null ? getPatientSex() : null;
        } else if (TCQueryFilterKey.PatientSpecies.equals(key)) {
            value = getPatientSpecies();
        } else if (TCQueryFilterKey.Title.equals(key)) {
            value = getTitle();
        }

        return value;
    }


    public String getLocalizedStringValue(TCQueryFilterKey key, Component c) {
        if (TCQueryFilterKey.Category.equals(key)) {
            return getCategory() != null ? c.getString("tc.category."
                    + getCategory().name().toLowerCase()) : null;
        } else if (TCQueryFilterKey.DiagnosisConfirmed.equals(key)) {
            return getDiagnosisConfirmed() != null ? c.getString("tc.yesno."
                    + getDiagnosisConfirmed().name().toLowerCase()) : null;
        } else if (TCQueryFilterKey.Level.equals(key)) {
            return getLevel() != null ? c.getString("tc.level."
                    + getLevel().name().toLowerCase()) : null;
        } else if (TCQueryFilterKey.PatientSex.equals(key)) {
            return getPatientSex() != null ? c.getString("tc.patientsex."
                    + getPatientSex().name().toLowerCase()) : null;
        } else {
            return getStringValue(key);
        }
    }

    public String getStringValue(TCQueryFilterKey key) {
        String s = null;
        
        TCKeywordCatalogueProvider catProv = TCKeywordCatalogueProvider.getInstance();
        
        if (TCQueryFilterKey.Abstract.equals(key)) {
            s = getAbstr();
        } else if (TCQueryFilterKey.AcquisitionModality.equals(key)) {
            s = concatStrings(getAcquisitionModalities());
        } else if (TCQueryFilterKey.Anatomy.equals(key)) {
            if (catProv == null || !catProv.hasCatalogue(key)) {
                if (getAnatomy() != null) {
                    s = getAnatomy();
                }
            }

            if (s == null) {
                s = getCodeAsString(getAnatomyCode());
            }
        } else if (TCQueryFilterKey.AuthorAffiliation.equals(key)) {
            s = getAuthorAffiliation();
        } else if (TCQueryFilterKey.AuthorContact.equals(key)) {
            s = getAuthorContact();
        } else if (TCQueryFilterKey.AuthorName.equals(key)) {
            s = getAuthorName();
        } else if (TCQueryFilterKey.BibliographicReference.equals(key)) {
            s = concatStrings(this.getBibliographicReferences());
        } else if (TCQueryFilterKey.Category.equals(key)) {
            s = getCategory() != null ? getCategory().toString() : null;
        } else if (TCQueryFilterKey.DiagnosisConfirmed.equals(key)) {
            s = getDiagnosisConfirmed() != null ? getDiagnosisConfirmed()
                    .toString() : null;
        } else if (TCQueryFilterKey.Diagnosis.equals(key)) {
            if (catProv == null || !catProv.hasCatalogue(key)) {
                if (getDiagnosis() != null) {
                    s = getDiagnosis();
                }
            }

            if (s == null) {
                s = getCodeAsString(getDiagnosisCode());
            }
        } else if (TCQueryFilterKey.DifferentialDiagnosis.equals(key)) {
            if (catProv == null || !catProv.hasCatalogue(key)) {
                if (getDiffDiagnosis() != null) {
                    s = getDiffDiagnosis();
                }
            }

            if (s == null) {
                s = getCodeAsString(getDiffDiagnosisCode());
            }
        } else if (TCQueryFilterKey.Discussion.equals(key)) {
            s = getDiscussion();
        } else if (TCQueryFilterKey.Finding.equals(key)) {
            if (catProv == null || !catProv.hasCatalogue(key)) {
                if (getFinding() != null) {
                    s = getFinding();
                }
            }

            if (s == null) {
                s = getCodeAsString(getFindingCode());
            }
        } else if (TCQueryFilterKey.History.equals(key)) {
            s = getHistory();
        } else if (TCQueryFilterKey.Keyword.equals(key)) {
            if (catProv == null || !catProv.hasCatalogue(key)) {
                if (getKeywords() != null) {
                    s = concatStrings(getKeywords());
                }
            }

            if (s == null) {
                s = getCodeAsString(getKeywordCode());
            }
        } else if (TCQueryFilterKey.Level.equals(key)) {
            s = getLevel() != null ? getLevel().toString() : null;
        } else if (TCQueryFilterKey.OrganSystem.equals(key)) {
            s = getOrganSystem();

        } else if (TCQueryFilterKey.Pathology.equals(key)) {
            if (catProv == null || !catProv.hasCatalogue(key)) {
                if (getPathology() != null) {
                    s = getPathology();
                }
            }

            if (s == null) {
                s = getCodeAsString(getPathologyCode());
            }
        } else if (TCQueryFilterKey.PatientAge.equals(key)) {
            s = getPatientAge() != null ? getPatientAge().toString() : null;
        } else if (TCQueryFilterKey.PatientSex.equals(key)) {
            s = getPatientSex() != null ? getPatientSex().toString() : null;
        } else if (TCQueryFilterKey.PatientSpecies.equals(key)) {
            s = getPatientSpecies();
        } else if (TCQueryFilterKey.Title.equals(key)) {
            s = getTitle();
        }

        return s;
    }


    
    private String getCodeAsString(DicomCode code) {
        return code != null ? code.toString() : null;
    }

    private String concatStrings(List<?> list) {
        if (list != null) {
            Iterator<?> it = list.iterator();
            StringBuilder sbuilder = new StringBuilder();
            if (it.hasNext())
                sbuilder.append(it.next());
            while (it.hasNext()) {
                sbuilder.append(", ");
                sbuilder.append(it.next().toString());
            }
            return sbuilder.toString();
        }

        return null;
    }

    private void parse(DicomObject o) {
        iuid = o.getString(Tag.SOPInstanceUID);
        
        DicomElement content = o != null ? o.get(Tag.ContentSequence) : null;
        DicomElement ref = o != null ? o
                .get(Tag.CurrentRequestedProcedureEvidenceSequence) : null;

        int refCount = ref != null ? ref.countItems() : -1;
        if (refCount > 0) {
            for (int i = 0; i < refCount; i++) {
                DicomObject studyRef = ref.getDicomObject(i);
                String stuid = studyRef != null ? studyRef
                        .getString(Tag.StudyInstanceUID) : null;

                if (stuid != null) {
                    TCReferencedStudy study = new TCReferencedStudy(stuid);
                    
                    DicomElement seriesSeq = studyRef
                            .get(Tag.ReferencedSeriesSequence);
                    int seriesCount = seriesSeq != null ? seriesSeq
                            .countItems() : -1;
                    int instanceCount = 0;
                    
                    if (seriesCount > 0) {
                        for (int j = 0; j < seriesCount; j++) {
                            DicomObject seriesRef = seriesSeq.getDicomObject(j);
                            String suid = seriesRef != null ? seriesRef
                                    .getString(Tag.SeriesInstanceUID) : null;

                            if (suid != null) {
                                TCReferencedSeries series = new TCReferencedSeries(suid, study);
                                
                                DicomElement instanceSeq = seriesRef
                                        .get(Tag.ReferencedSOPSequence);
                                instanceCount = instanceSeq != null ? instanceSeq
                                        .countItems() : -1;

                                if (instanceCount > 0) {
                                    study.addSeries(series);
                                    
                                    for (int k = 0; k < instanceCount; k++) {
                                        DicomObject instanceRef = instanceSeq
                                                .getDicomObject(k);
                                        String iuid = instanceRef
                                                .getString(Tag.ReferencedSOPInstanceUID);
                                        String cuid = instanceRef
                                                .getString(Tag.ReferencedSOPClassUID);

                                        series.addInstance(
                                                new TCReferencedInstance(series, iuid, cuid));
                                    }
                                }
                            }
                        }
                    }
                    
                    if (instanceCount>0)
                    {
                        if (studyRefs==null)
                        {
                            studyRefs = new ArrayList<TCReferencedStudy>();
                        }
                        studyRefs.add(study);
                    }
                }
            }
        }

        if (content != null) {
            int count = content.countItems();
            for (int i = 0; i < count; i++) {
                try {
                    DicomObject item = content.getDicomObject(i);

                    if (item != null) {
                        String valueType = item.getString(Tag.ValueType);
                        DicomCode conceptName = new DicomCode(
                                item.getNestedDicomObject(Tag.ConceptNameCodeSequence));

                        if ("TEXT".equalsIgnoreCase(valueType)) {
                            if (conceptName.equals(TCQueryFilterKey.Abstract
                                    .getCode())) {
                                abstr = getTextValue(item);
                            } else if (conceptName
                                    .equals(TCQueryFilterKey.Anatomy.getCode())) {
                                anatomy = getTextValue(item);
                            } else if (conceptName
                                    .equals(TCQueryFilterKey.AuthorName
                                            .getCode())) {
                                authorName = getTextValue(item);
                            } else if (conceptName
                                    .equals(TCQueryFilterKey.AuthorContact
                                            .getCode())) {
                                authorContact = getTextValue(item);
                            } else if (conceptName
                                    .equals(TCQueryFilterKey.AuthorAffiliation
                                            .getCode())) {
                                authorAffiliation = getTextValue(item);
                            } else if (conceptName
                                    .equals(TCQueryFilterKey.BibliographicReference
                                            .getCode())) {
                                String s = getTextValue(item);
                                if (s != null) {
                                    if (bibliographicReferences == null) {
                                        bibliographicReferences = new ArrayList<String>();
                                    }

                                    bibliographicReferences.add(s);
                                }
                            } else if (conceptName
                                    .equals(TCQueryFilterKey.Diagnosis
                                            .getCode())) {
                                diagnosis = getTextValue(item);
                            } else if (conceptName
                                    .equals(TCQueryFilterKey.DifferentialDiagnosis
                                            .getCode())) {
                                diffDiagnosis = getTextValue(item);
                            } else if (conceptName
                                    .equals(TCQueryFilterKey.Discussion
                                            .getCode())) {
                                discussion = getTextValue(item);
                            } else if (conceptName
                                    .equals(TCQueryFilterKey.Finding.getCode())) {
                                finding = getTextValue(item);
                            } else if (conceptName
                                    .equals(TCQueryFilterKey.History.getCode())) {
                                history = getTextValue(item);
                            } else if (conceptName
                                    .equals(TCQueryFilterKey.Keyword.getCode())) {
                                String keyword = getTextValue(item);

                                if (keyword != null) {
                                    if (keywords == null) {
                                        keywords = new ArrayList<String>();
                                    }

                                    if (!keywords.contains(keyword)) {
                                        keywords.add(keyword);
                                    }
                                }
                            } else if (conceptName
                                    .equals(TCQueryFilterKey.OrganSystem
                                            .getCode())) {
                                organSystem = getTextValue(item);
                            } else if (conceptName
                                    .equals(TCQueryFilterKey.Pathology
                                            .getCode())) {
                                pathology = getTextValue(item);
                            } else if (conceptName
                                    .equals(TCQueryFilterKey.PatientAge
                                            .getCode())) {
                                try
                                {
                                    patientAge = Integer.valueOf(getTextValue(item));
                                }
                                catch (Exception e)
                                {
                                    log.warn("Parsing patient age failed! Skipped..");
                                }
                            } else if (conceptName
                                    .equals(TCQueryFilterKey.PatientSex
                                            .getCode())) {
                                patientSex = PatientSex.get(getTextValue(item));
                            } else if (conceptName
                                    .equals(TCQueryFilterKey.PatientSpecies
                                            .getCode())) {
                                patientSpecies = getTextValue(item);
                            } else if (conceptName
                                    .equals(TCQueryFilterKey.Title.getCode())) {
                                title = getTextValue(item);
                            }
                        } else if ("CODE".equalsIgnoreCase(valueType)) {
                            if (conceptName.equals(TCQueryFilterKey.Category
                                    .getCode())) {
                                category = Category.get(getCodeValue(item)
                                        .toCode());
                            } else if (conceptName
                                    .equals(TCQueryFilterKey.AcquisitionModality
                                            .getCode())) {
                                AcquisitionModality m = AcquisitionModality
                                        .get(getCodeValue(item).toCode());

                                if (m != null) {
                                    if (acquisitionModalities == null) {
                                        acquisitionModalities = new ArrayList<AcquisitionModality>();
                                    }

                                    if (!acquisitionModalities.contains(m)) {
                                        acquisitionModalities.add(m);
                                    }
                                }
                            } else if (conceptName
                                    .equals(TCQueryFilterKey.DiagnosisConfirmed
                                            .getCode())) {
                                diagnosisConfirmed = YesNo.get(getCodeValue(
                                        item).toCode());
                            } else if (conceptName
                                    .equals(TCQueryFilterKey.Level.getCode())) {
                                level = Level.get(getCodeValue(item).toCode());
                            } else if (conceptName
                                    .equals(TCQueryFilterKey.Anatomy.getCode())) {
                                anatomyCode = getCodeValue(item);
                            } else if (conceptName
                                    .equals(TCQueryFilterKey.Diagnosis
                                            .getCode())) {
                                diagnosisCode = getCodeValue(item);
                            } else if (conceptName
                                    .equals(TCQueryFilterKey.DifferentialDiagnosis
                                            .getCode())) {
                                diffDiagnosisCode = getCodeValue(item);
                            } else if (conceptName
                                    .equals(TCQueryFilterKey.Finding.getCode())) {
                                findingCode = getCodeValue(item);
                            } else if (conceptName
                                    .equals(TCQueryFilterKey.Keyword.getCode())) {
                                keywordCode = getCodeValue(item);
                            } else if (conceptName
                                    .equals(TCQueryFilterKey.Pathology
                                            .getCode())) {
                                pathologyCode = getCodeValue(item);
                            }
                        }
                    }
                } catch (Exception e) {
                    log.warn("Parsing TC attribute failed! Skipped...", e);
                }
            }
        }
    }

    private String getTextValue(DicomObject object) {
        return object != null ? object.getString(Tag.TextValue) : null;
    }

    private DicomCode getCodeValue(DicomObject object) {
        return object != null ? new DicomCode(
                object.getNestedDicomObject(Tag.ConceptCodeSequence)) : null;
    }
        
    @SuppressWarnings("unchecked")
    protected static <T extends Object> T convertValue(Object v, Class<T> valueClass) throws IllegalArgumentException
    {
        if (v==null)
        {
            return null;
        }
        else if (String.class.isAssignableFrom(valueClass))
        {
            if (DicomCode.class.isAssignableFrom(v.getClass()))
            {
                return (T) ((DicomCode)v).getMeaning();
            }
            else
            {
                return (T) v.toString();
            }
        }
        else if (DicomCode.class.isAssignableFrom(valueClass))
        {
            if (DicomCode.class.isAssignableFrom(v.getClass()))
            {
                return (T) v;
            }
            else if (TCKeyword.class.isAssignableFrom(v.getClass()))
            {
                return (T) ((TCKeyword)v).getCode();
            }
        }
        else if (Enum.class.isAssignableFrom(valueClass))
        {
            if (valueClass.isAssignableFrom(v.getClass()))
            {
                return (T) v;
            }
            else
            {
                for (Object o : valueClass.getEnumConstants())
                {
                    if (((Enum<?>)o).name().equals(v.toString().trim()))
                    {
                        return (T) o;
                    }
                }
            }

            throw new IllegalArgumentException("Unable to convert enum (" + valueClass + "): No enum constant found for name '" + v.toString() + "'!"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }
        
        throw new IllegalArgumentException("Unable to convert from class " + v.getClass() + " to class " + valueClass); //$NON-NLS-1$ //$NON-NLS-2$
    }
    
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    protected <T extends Object> List<T> convertValues(Object v, Class<T> valueClass) throws IllegalArgumentException
    {
        if (v==null)
        {
            return null;
        }
        else
        {
            Object[] values = null;
            
            if (v instanceof List)
            {
                values = new Object[((List<?>)v).size()];
                for (int i=0; i<values.length; i++)
                {
                    values[i] = convertValue(((List<?>)v).get(i), valueClass);
                }
            }
            else if (v.getClass().isAssignableFrom(valueClass))
            {
                values = new Object[] {convertValue(v, valueClass)};
            }
            
            if (values!=null)
            {
                return (List) Arrays.asList(values);
            }
        }
        
        throw new IllegalArgumentException("Unable to convert from class " + v.getClass() + " to list of class " + valueClass); //$NON-NLS-1$ //$NON-NLS-2$
    }
    
    
    public static class DicomCode implements Serializable {

        private static final long serialVersionUID = 1L;

        private String value;

        private String designator;

        private String meaning;

        private String version;

        public DicomCode(String designator, String value, String meaning,
                String version) {
            this.value = value;
            this.designator = designator;
            this.meaning = meaning;
            this.version = version;
        }

        public DicomCode(String designator, String value, String meaning) {
            this(designator, value, meaning, null);
        }

        public DicomCode(DicomObject dataset) {
            this.value = dataset.getString(Tag.CodeValue);
            this.designator = dataset.getString(Tag.CodingSchemeDesignator);
            this.meaning = dataset.getString(Tag.CodeMeaning);
            this.version = dataset.getString(Tag.CodingSchemeVersion);
        }

        public static DicomCode fromString(String designator, String s) {
            if (designator != null && s != null) {
                if (s.startsWith("(")) {
                    String value = s.substring(1, s.indexOf(")"));
                    String meaning = s.substring(s.indexOf(")") + 2);

                    return new DicomCode(designator, value, meaning);
                } else {
                    return new DicomCode(designator, s, null);
                }
            }

            return null;
        }

        public String getValue() {
            return value;
        }

        public String getDesignator() {
            return designator;
        }

        public String getMeaning() {
            return meaning;
        }

        public String getVersion() {
            return version;
        }

        @Override
        public String toString() {
            StringBuffer sbuf = new StringBuffer();

            if (meaning != null) {
                sbuf.append("(");
            }

            sbuf.append(value);

            if (meaning != null) {
                sbuf.append(") ");
                sbuf.append(meaning);
            }

            return sbuf.toString();
        }

        public Code toCode() {
            DicomObject dataset = new BasicDicomObject();
            dataset.putString(Tag.CodingSchemeDesignator, VR.SH, designator);
            dataset.putString(Tag.CodeValue, VR.SH, value);
            dataset.putString(Tag.CodeMeaning, VR.LO, meaning == null ? ""
                    : meaning);

            if (version != null) {
                dataset.putString(Tag.CodingSchemeVersion, null, version);
            }

            return new Code(dataset);
        }

        public boolean equals(Code code) {
            if (code != null) {
                return value.equals(code.getCodeValue())
                        && designator.equals(code.getCodingSchemeDesignator())
                        && (version == null || version.equals(code
                                .getCodingSchemeVersion()));
            }

            return false;
        }
    }
}
