/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
package org.dcm4chex.archive.ejb.session;

import java.util.Collection;
import java.util.Iterator;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.FinderException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.log4j.Logger;
import org.dcm4che.data.Dataset;
import org.dcm4che.dict.Tags;
import org.dcm4che.util.UIDGenerator;
import org.dcm4chex.archive.ejb.interfaces.GPSPSLocal;
import org.dcm4chex.archive.ejb.interfaces.GPSPSLocalHome;
import org.dcm4chex.archive.ejb.interfaces.PatientLocal;
import org.dcm4chex.archive.ejb.interfaces.PatientLocalHome;

/**
 * @author gunter.zeilinger@tiani.com
 * @version Revision $Date$
 * @since 28.03.2005
 * 
 * @ejb.bean name="GPWLManager" type="Stateless" view-type="remote"
 *           jndi-name="ejb/GPWLManager"
 * @ejb.transaction-type type="Container"
 * @ejb.transaction type="Required"
 * @ejb.ejb-ref ejb-name="Patient" view-type="local" ref-name="ejb/Patient"
 * @ejb.ejb-ref ejb-name="GPSPS" view-type="local" ref-name="ejb/GPSPS"
 */

public abstract class GPWLManagerBean implements SessionBean {

    private static final int[] PATIENT_ATTRS = { Tags.PatientName,
            Tags.PatientID, Tags.PatientBirthDate, Tags.PatientSex, };

    private static Logger log = Logger.getLogger(GPWLManagerBean.class);

    private PatientLocalHome patHome;

    private GPSPSLocalHome gpspsHome;

    public void setSessionContext(SessionContext ctx) {
        Context jndiCtx = null;
        try {
            jndiCtx = new InitialContext();
            patHome = (PatientLocalHome) jndiCtx
                    .lookup("java:comp/env/ejb/Patient");
            gpspsHome = (GPSPSLocalHome) jndiCtx
                    .lookup("java:comp/env/ejb/GPSPS");
        } catch (NamingException e) {
            throw new EJBException(e);
        } finally {
            if (jndiCtx != null) {
                try {
                    jndiCtx.close();
                } catch (NamingException ignore) {
                }
            }
        }
    }

    public void unsetSessionContext() {
        gpspsHome = null;
        patHome = null;
    }

    /**
     * @ejb.interface-method
     */
    public Dataset removeWorklistItem(String uid) {
        try {
            GPSPSLocal gpsps = gpspsHome.findBySopIuid(uid);
            Dataset ds = gpsps.getAttributes();
            gpsps.remove();
            return ds;
        } catch (FinderException e) {
            return null;
        } catch (Exception e) {
            throw new EJBException(e);
        }
    }

    private PatientLocal getPatient(Dataset ds) throws FinderException,
            CreateException {
        final String id = ds.getString(Tags.PatientID);
        Collection c = patHome.findByPatientId(id);
        for (Iterator it = c.iterator(); it.hasNext();) {
            PatientLocal patient = (PatientLocal) it.next();
            if (equals(patient, ds)) {
                PatientLocal mergedWith = patient.getMergedWith();
                if (mergedWith != null) {
                    patient = mergedWith;
                }
                return patient;
            }
        }
        PatientLocal patient = patHome.create(ds.subSet(PATIENT_ATTRS));
        return patient;
    }

    private boolean equals(PatientLocal patient, Dataset ds) {
        // TODO Auto-generated method stub
        return true;
    }

    /**
     * @ejb.interface-method
     */
    public String addWorklistItem(Dataset ds) {
        try {
            String iuid = ds.getString(Tags.SOPInstanceUID);
            if (iuid == null) {
                iuid = UIDGenerator.getInstance().createUID();
                ds.putUI(Tags.SOPInstanceUID, iuid);
            }
            gpspsHome.create(ds.subSet(PATIENT_ATTRS, true, true),
                    getPatient(ds));
            return iuid;
        } catch (Exception e) {
            throw new EJBException(e);
        }
    }

    /**
     * @ejb.interface-method
     */
    public void updateWorklistItem(Dataset ds) {
        try {
            final String iuid = ds.getString(Tags.SOPInstanceUID);
            GPSPSLocal gpsps = gpspsHome.findBySopIuid(iuid);
            Dataset attrs = gpsps.getAttributes();
            attrs.putAll(ds.subSet(PATIENT_ATTRS, true, true));
            gpsps.setAttributes(attrs);
        } catch (Exception e) {
            throw new EJBException(e);
        }
    }
}
