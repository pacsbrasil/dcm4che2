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

import java.text.SimpleDateFormat;
import java.util.Date;

import org.dcm4che.data.Dataset;
import org.dcm4che.dict.Tags;

/**
 * 
 * @author gunter.zeilinger@tiani.com
 * @version $Revision$ $Date$
 * @since 21.01.2004
 */
public final class DTOFactory {

    private DTOFactory() {
    }

    public static PatientDTO newPatientDTO(int pk, Dataset ds) {
        PatientDTO pat = new PatientDTO();
        pat.setPk(pk);
        pat.setPatientID(ds.getString(Tags.PatientID, ""));
        pat.setPatientName(ds.getString(Tags.PatientName, ""));
        pat.setPatientBirthDate(
            format(ds.getDate(Tags.PatientBirthDate), PatientDTO.DATE_FORMAT));
        pat.setPatientSex(ds.getString(Tags.PatientSex, ""));
        return pat;
    }

    public static StudyDTO newStudyDTO(
        int pk,
        Dataset ds,
        int numSeries,
        int numInst) {
        StudyDTO study = new StudyDTO();
        study.setPk(pk);
        study.setAccessionNumber(ds.getString(Tags.AccessionNumber, ""));
        study.setStudyID(ds.getString(Tags.StudyID, ""));
        study.setStudyIUID(ds.getString(Tags.StudyInstanceUID, ""));
        study.setStudyDateTime(
            format(
                ds.getDateTime(Tags.StudyDate, Tags.StudyTime),
                StudyDTO.DATETIME_FORMAT));
        study.setStudyDescription(ds.getString(Tags.StudyDescription, ""));
        study.setNumberOfSeries(numSeries);
        study.setNumberOfInstances(numInst);
        return study;
    }

    public static SeriesDTO newSeriesDTO(int pk, Dataset ds, int numInst) {
        SeriesDTO series = new SeriesDTO();
        series.setPk(pk);
        series.setModality(ds.getString(Tags.Modality, ""));
        series.setSeriesNumber(ds.getString(Tags.SeriesNumber, ""));
        series.setSeriesIUID(ds.getString(Tags.SeriesInstanceUID, ""));
        series.setSeriesDescription(ds.getString(Tags.SeriesDescription, ""));
        series.setNumberOfInstances(numInst);
        return series;
    }

    public static InstanceDTO newInstanceDTO(int pk, Dataset ds) {
        InstanceDTO inst = new InstanceDTO();
        inst.setPk(pk);
        inst.setInstanceNumber(ds.getString(Tags.InstanceNumber, ""));
        inst.setSopCUID(ds.getString(Tags.SOPClassUID, ""));
        inst.setSopIUID(ds.getString(Tags.SOPInstanceUID, ""));
        return inst;
    }

    private static String format(Date date, String pattern) {
        return date != null ? new SimpleDateFormat(pattern).format(date) : "";
    }
}
