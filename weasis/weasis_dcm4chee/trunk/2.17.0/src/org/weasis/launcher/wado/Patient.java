package org.weasis.launcher.wado;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.launcher.wado.xml.DateUtil;
import org.weasis.launcher.wado.xml.TagUtil;
import org.weasis.launcher.wado.xml.XmlDescription;

public class Patient implements XmlDescription {

    private static Logger logger = LoggerFactory.getLogger(Patient.class);

    private final String patientID;
    private String patientName = null;
    private String patientBirthDate = null;
    private String patientBirthTime = null;
    private String patientSex = null;
    private final List<Study> studiesList;

    public Patient(String patientID) {
        if (patientID == null) {
            throw new IllegalArgumentException("PaientID cannot be null!");
        }
        this.patientID = patientID;
        studiesList = new ArrayList<Study>();
    }

    public String getPatientID() {
        return patientID;
    }

    public String getPatientName() {
        return patientName;
    }

    public List<Study> getStudies() {
        return studiesList;
    }

    public boolean isEmpty() {
        for (Study s : studiesList) {
            if (!s.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    public String getPatientBirthTime() {
        return patientBirthTime;
    }

    public void setPatientBirthTime(String patientBirthTime) {
        this.patientBirthTime = patientBirthTime;
    }

    public String getPatientBirthDate() {
        return patientBirthDate;
    }

    public void setPatientBirthDate(String patientBirthDate) {
        this.patientBirthDate = patientBirthDate;
    }

    public String getPatientSex() {
        return patientSex;
    }

    public void setPatientSex(String patientSex) {
        if (patientSex != null) {
            patientSex = patientSex.toUpperCase();
            patientSex = patientSex.startsWith("M") ? "M" : patientSex.startsWith("F") ? "F" : "O";
        }
        this.patientSex = patientSex;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName == null ? "" : patientName.replace("^", " ").trim();
    }

    public void addStudy(Study study) {
        if (!studiesList.contains(study)) {
            studiesList.add(study);
        }
    }

    /**
     * 
     * @return
     */
    public String toXml() {
        StringBuffer result = new StringBuffer();
        if (patientID != null && patientName != null) {
            result.append("\n<" + TagElement.DICOM_LEVEL.Patient.name() + " ");

            TagUtil.addXmlAttribute(TagElement.PatientID, patientID, result);
            TagUtil.addXmlAttribute(TagElement.PatientName, patientName, result);
            TagUtil.addXmlAttribute(TagElement.PatientBirthDate, patientBirthDate, result);
            TagUtil.addXmlAttribute(TagElement.PatientBirthTime, patientBirthTime, result);
            TagUtil.addXmlAttribute(TagElement.PatientSex, patientSex, result);
            result.append(">");

            Collections.sort(studiesList, new Comparator<Study>() {

                public int compare(Study o1, Study o2) {
                    Date date1 = DateUtil.getDate(o1.getStudyDate());
                    Date date2 = DateUtil.getDate(o2.getStudyDate());
                    if (date1 != null && date2 != null) {
                        // inverse time
                        int rep = date2.compareTo(date1);
                        if (rep == 0) {
                            Date time1 = DateUtil.getTime(o1.getStudyTime());
                            Date time2 = DateUtil.getTime(o2.getStudyTime());
                            if (time1 != null && time2 != null) {
                                // inverse time
                                return time2.compareTo(time1);
                            }
                        } else {
                            return rep;
                        }
                    }
                    if (date1 == null && date2 == null) {
                        return o1.getStudyInstanceUID().compareTo(o2.getStudyInstanceUID());
                    } else {
                        if (date1 == null) {
                            return 1;
                        }
                        if (date2 == null) {
                            return -1;
                        }
                    }
                    return 0;
                }
            });
            for (Study s : studiesList) {
                result.append(s.toXml());
            }
            result.append("\n</Patient>");
        }
        logger.debug("Patient toXml [{}]", result.toString());
        return result.toString();
    }

    public Study getStudy(String uid) {
        for (Study s : studiesList) {
            if (s.getStudyInstanceUID().equals(uid)) {
                return s;
            }
        }
        return null;
    }

}
