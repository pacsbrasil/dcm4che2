/*
 * $Id$ Copyright (c) 2002,2003 by TIANI MEDGRAPH AG
 * 
 * This file is part of dcm4che.
 * 
 * This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General
 * Public License as published by the Free Software Foundation; either version 2 of the License, or (at your option)
 * any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with this library; if not, write to
 * the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.dcm4chex.archive.web.maverick;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.dcm4chex.archive.ejb.interfaces.ContentEdit;
import org.dcm4chex.archive.ejb.interfaces.ContentEditHome;
import org.dcm4chex.archive.ejb.interfaces.PatientDTO;
import org.dcm4chex.archive.util.EJBHomeFactory;

/**
 * @author umberto.cappellini@tiani.com
 */
public class PatientUpdateCtrl extends Dcm4JbossController {
    private int pk;

    private String patientID = null;

    private String issuerOfPatientID = null;

    private String patientName = null;

    private String patientSex = null;

    private String patientBirthDay;

    private String patientBirthMonth;

    private String patientBirthYear;

    private String submit = null;

    private String cancel = null;

    protected String perform() throws Exception {
        if (submit != null)
            if (pk == -1)
                executeCreate();
            else
                executeUpdate();
        return SUCCESS;
    }

    private void executeCreate() {
        try {
	        PatientDTO pat = new PatientDTO();
	        pat.setPk(-1);
	        pat.setSpecificCharacterSet("ISO_IR 100");        
	        pat.setPatientID(patientID);
	        pat.setIssuerOfPatientID(issuerOfPatientID);
	        pat.setPatientSex(patientSex);
	        pat.setPatientName(patientName);
	        setPatientBirthDate(pat);
	        ContentEdit ce = lookupContentEdit();
	        ce.createPatient(pat);        
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private ContentEdit lookupContentEdit() throws Exception {
        ContentEditHome home = (ContentEditHome) EJBHomeFactory.getFactory()
                .lookup(ContentEditHome.class, ContentEditHome.JNDI_NAME);
        return home.create();
    }

    private void executeUpdate() {
        try {
            PatientDTO to_update = FolderForm.getFolderForm(
                    getCtx().getRequest()).getPatientByPk(pk);
            to_update.setPatientSex(patientSex);
            to_update.setPatientName(patientName);

            setPatientBirthDate(to_update);
            //updating data model
            ContentEdit ce = lookupContentEdit();
            ce.updatePatient(to_update);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void setPatientBirthDate(PatientDTO to_update) {
        if ((patientBirthDay == null || patientBirthDay.length() == 0)
                && (patientBirthMonth == null || patientBirthMonth.length() == 0)
                && (patientBirthYear == null || patientBirthYear.length() == 0)) {
            to_update.setPatientBirthDate(null);
        } else {
            try {
                Calendar c = Calendar.getInstance();
                c.set(Calendar.DAY_OF_MONTH, Integer
                        .parseInt(patientBirthDay));
                c.set(Calendar.MONTH,
                        Integer.parseInt(patientBirthMonth) - 1);
                c.set(Calendar.YEAR, Integer.parseInt(patientBirthYear));
                to_update.setPatientBirthDate(new SimpleDateFormat(
                        PatientDTO.DATE_FORMAT).format(c.getTime()));
            } catch (Throwable e1) {
                //do nothing
            }
        }
    }

    public final void setPatientBirthDay(String patientBirthDay) {
        this.patientBirthDay = patientBirthDay;
    }

    public final void setPatientBirthMonth(String patientBirthMonth) {
        this.patientBirthMonth = patientBirthMonth;
    }

    public final void setPatientBirthYear(String patientBirthYear) {
        this.patientBirthYear = patientBirthYear;
    }

    public final void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public final void setPatientSex(String patientSex) {
        this.patientSex = patientSex;
    }

    public final void setPk(int pk) {
        this.pk = pk;
    }

    public final void setSubmit(String update) {
        this.submit = update;
    }

    public final void setCancel(String cancel) {
        this.cancel = cancel;
    }

    public final void setIssuerOfPatientID(String issuerOfPatientID) {
        this.issuerOfPatientID = issuerOfPatientID;
    }

    public final void setPatientID(String patientID) {
        this.patientID = patientID;
    }
}