/*
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
/* 
 * File: $Source$
 * Author: gunter
 * Date: 15.07.2003
 * Time: 10:33:59
 * CVS Revision: $Revision$
 * Last CVS Commit: $Date$
 * Author of last CVS Commit: $Author$
 */
package org.dcm4chex.archive.ejb.session;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.FinderException;
import javax.ejb.ObjectNotFoundException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.log4j.Logger;
import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmElement;
import org.dcm4che.data.DcmValueException;
import org.dcm4che.data.FileMetaInfo;
import org.dcm4che.dict.Status;
import org.dcm4che.dict.Tags;
import org.dcm4che.dict.VRs;
import org.dcm4che.net.DcmServiceException;
import org.dcm4chex.archive.ejb.interfaces.FileLocal;
import org.dcm4chex.archive.ejb.interfaces.FileLocalHome;
import org.dcm4chex.archive.ejb.interfaces.InstanceLocal;
import org.dcm4chex.archive.ejb.interfaces.InstanceLocalHome;
import org.dcm4chex.archive.ejb.interfaces.PatientLocal;
import org.dcm4chex.archive.ejb.interfaces.PatientLocalHome;
import org.dcm4chex.archive.ejb.interfaces.SeriesLocal;
import org.dcm4chex.archive.ejb.interfaces.SeriesLocalHome;
import org.dcm4chex.archive.ejb.interfaces.StudyLocal;
import org.dcm4chex.archive.ejb.interfaces.StudyLocalHome;

/**
 * Storage Bean
 * 
 * @ejb:bean
 *  name="Storage"
 *  type="Stateless"
 *  view-type="remote"
 *  jndi-name="ejb/Storage"
 * 
 * @ejb:transaction-type 
 *  type="Container"
 * 
 * @ejb:transaction 
 *  type="Required"
 * 
 * @ejb.ejb-ref
 *  ejb-name="Patient" 
 *  view-type="local"
 *  ref-name="ejb/Patient" 
 * 
 * @ejb.ejb-ref
 *  ejb-name="Study" 
 *  view-type="local"
 *  ref-name="ejb/Study" 
 * 
 * @ejb.ejb-ref
 *  ejb-name="Series" 
 *  view-type="local"
 *  ref-name="ejb/Series" 
 * 
 * @ejb.ejb-ref
 *  ejb-name="Instance" 
 *  view-type="local"
 *  ref-name="ejb/Instance" 
 * 
 * @ejb.ejb-ref
 *  ejb-name="File" 
 *  view-type="local"
 *  ref-name="ejb/File" 
 *
 * @author <a href="mailto:gunter@tiani.com">Gunter Zeilinger</a>
 *
 */
public abstract class StorageBean implements SessionBean
{
    private Logger log = Logger.getLogger(StorageBean.class);

    private PatientLocalHome patHome;
    private StudyLocalHome studyHome;
    private SeriesLocalHome seriesHome;
    private InstanceLocalHome instHome;
    private FileLocalHome fileHome;

    public void setSessionContext(SessionContext ctx)
    {
        Context jndiCtx = null;
        try {
            jndiCtx = new InitialContext();
            patHome = (PatientLocalHome) jndiCtx.lookup("java:comp/env/ejb/Patient");
            studyHome = (StudyLocalHome) jndiCtx.lookup("java:comp/env/ejb/Study");
            seriesHome = (SeriesLocalHome) jndiCtx.lookup("java:comp/env/ejb/Series");
            instHome = (InstanceLocalHome) jndiCtx.lookup("java:comp/env/ejb/Instance");
            fileHome = (FileLocalHome) jndiCtx.lookup("java:comp/env/ejb/File");
        } catch (NamingException e) {
            throw new EJBException(e);
        } finally {
            if (jndiCtx != null) {
                try {
                    jndiCtx.close();
                } catch (NamingException ignore) {}
            }
        }
    }

    public void unsetSessionContext()
    {
        patHome = null;
        studyHome = null;
        seriesHome = null;
        instHome = null;
        fileHome = null;
    }

    /**
     * @ejb:interface-method
     */
    public int store(
        Dataset ds,
        String host,
        String mnt,
        String path,
        long size,
        byte[] md5)
        throws DcmServiceException
    {
        try
        {
            ArrayList modified = new ArrayList();
            FileMetaInfo fmi = ds.getFileMetaInfo();
            final String iuid = fmi.getMediaStorageSOPInstanceUID();
            final String cuid = fmi.getMediaStorageSOPClassUID();
            final String tsuid = fmi.getTransferSyntaxUID();
            InstanceLocal instance = null;
            try
            {
                instance = instHome.findBySopIuid(iuid);
                coerceInstanceIdentity(instance, ds, modified);
            }
            catch (ObjectNotFoundException onfe)
            {
                instance = instHome.create(ds, getSeries(ds, modified));
            }
            FileLocal file =
                fileHome.create(host, mnt, path, tsuid, size, md5, instance);
            return modified.isEmpty()
                ? Status.Success
                : Status.CoercionOfDataElements;
        }
        catch (Exception e)
        {
            throw new DcmServiceException(Status.ProcessingFailure);
        }
    }

    /**
     * @param ds
     * @return
     */
    private SeriesLocal getSeries(Dataset ds, ArrayList modified)
        throws FinderException, CreateException
    {
        final String uid = ds.getString(Tags.SeriesInstanceUID);
        SeriesLocal series;
        try
        {
            series = seriesHome.findBySeriesIuid(uid);
            coerceSeriesIdentity(series, ds, modified);
        }
        catch (ObjectNotFoundException onfe)
        {
            series = seriesHome.create(ds, getStudy(ds, modified));
        }

        return series;
    }

    /**
     * @param ds
     * @return
     */
    private StudyLocal getStudy(Dataset ds, ArrayList modified)
        throws CreateException, FinderException
    {
        final String uid = ds.getString(Tags.StudyInstanceUID);
        StudyLocal study;
        try
        {
            study = studyHome.findByStudyIuid(uid);
            coerceStudyIdentity(study, ds, modified);
        }
        catch (ObjectNotFoundException onfe)
        {
            study = studyHome.create(ds, getPatient(ds, modified));
        }

        return study;
    }

    /**
     * @param ds
     * @return
     */
    private PatientLocal getPatient(Dataset ds, ArrayList modified)
        throws CreateException, FinderException
    {
        final String id = ds.getString(Tags.PatientID);
        Collection c = patHome.findByPatientId(id);
        for (Iterator it = c.iterator(); it.hasNext();)
        {
            PatientLocal patient = (PatientLocal) it.next();
            if (equals(patient, ds))
            {
                coercePatientIdentity(patient, ds, modified);
                return patient;
            }
        }
        PatientLocal patient = patHome.create(ds);
        return patient;
    }

    private boolean equals(PatientLocal patient, Dataset ds)
    {
        // TODO Auto-generated method stub
        return true;
    }

    private void coercePatientIdentity(
        PatientLocal patient,
        Dataset ds,
        ArrayList modified)
    {
        coerceIdentity(patient.getAttributes(), ds, modified);
    }

    private void coerceStudyIdentity(
        StudyLocal study,
        Dataset ds,
        ArrayList modified)
    {
        coercePatientIdentity(study.getPatient(), ds, modified);
        coerceIdentity(study.getAttributes(), ds, modified);
    }

    private void coerceSeriesIdentity(
        SeriesLocal series,
        Dataset ds,
        ArrayList modified)
    {
        coerceStudyIdentity(series.getStudy(), ds, modified);
        coerceIdentity(series.getAttributes(), ds, modified);
    }

    private void coerceInstanceIdentity(
        InstanceLocal instance,
        Dataset ds,
        ArrayList modified)
    {
        coerceSeriesIdentity(instance.getSeries(), ds, modified);
        coerceIdentity(instance.getAttributes(), ds, modified);
    }

    private void coerceIdentity(Dataset ref, Dataset ds, ArrayList modified)
    {
        for (Iterator it = ref.iterator(); it.hasNext();)
        {
            DcmElement refEl = (DcmElement) it.next();
            DcmElement el = ds.get(refEl.tag());
            if (!equals(el, ds.getCharset(), refEl, ref.getCharset(), modified))
            {
                log.warn("Coerce " + el + " to " + refEl);
                modified.add(new Integer(refEl.tag()));
            }
        }
    }

    private boolean equals(
        DcmElement el,
        Charset cs,
        DcmElement refEl,
        Charset refCS,
        ArrayList modified)
    {
        final int vm = refEl.vm();
        if (el == null || el.vm() != vm)
        {
            return false;
        }
        final int vr = refEl.vr();
        if (vr == VRs.OW || vr == VRs.OB || vr == VRs.UN)
        {
            // no check implemented!
            return true;
        }
        for (int i = 0; i < vm; ++i)
        {
            if (vr == VRs.SQ)
            {
                coerceIdentity(refEl.getItem(i), el.getItem(i), modified);
            }
            else
            {
                try
                {
                    if (!(vr == VRs.PN
                        ? refEl.getPersonName(i, refCS).equals(el.getPersonName(i, cs))
                        : refEl.getString(i, refCS).equals(el.getString(i, cs))))
                    {
                        return false;
                    }
                }
                catch (DcmValueException e)
                {
                    log.warn(e,e);
                }
            }
        }
        return true;
    }
}
