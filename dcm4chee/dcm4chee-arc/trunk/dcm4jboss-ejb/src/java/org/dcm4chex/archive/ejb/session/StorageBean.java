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
 * Java(TM), available at http://sourceforge.net/projects/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * TIANI Medgraph AG.
 * Portions created by the Initial Developer are Copyright (C) 2003-2005
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Gunter Zeilinger <gunter.zeilinger@tiani.com>
 * Franz Willer <franz.willer@gwi-ag.com>
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

package org.dcm4chex.archive.ejb.session;

import java.nio.charset.Charset;
import java.util.Collection;
import java.util.HashSet;
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
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.data.DcmValueException;
import org.dcm4che.data.FileMetaInfo;
import org.dcm4che.dict.Status;
import org.dcm4che.dict.Tags;
import org.dcm4che.dict.VRs;
import org.dcm4che.net.DcmServiceException;
import org.dcm4chex.archive.common.Availability;
import org.dcm4chex.archive.ejb.conf.AttributeCoercions;
import org.dcm4chex.archive.ejb.conf.AttributeFilter;
import org.dcm4chex.archive.ejb.conf.ConfigurationException;
import org.dcm4chex.archive.ejb.interfaces.FileLocal;
import org.dcm4chex.archive.ejb.interfaces.FileLocalHome;
import org.dcm4chex.archive.ejb.interfaces.FileSystemLocal;
import org.dcm4chex.archive.ejb.interfaces.FileSystemLocalHome;
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
 * @ejb.bean name="Storage" type="Stateless" view-type="remote" 
 * 			 jndi-name="ejb/Storage"
 * @ejb.transaction-type type="Container"
 * @ejb.transaction type="Required"
 * 
 * @ejb.ejb-ref ejb-name="Patient" view-type="local" ref-name="ejb/Patient"
 * @ejb.ejb-ref ejb-name="Study"  view-type="local" ref-name="ejb/Study"
 * @ejb.ejb-ref ejb-name="Series" view-type="local" ref-name="ejb/Series"
 * @ejb.ejb-ref ejb-name="Instance" view-type="local" ref-name="ejb/Instance"
 * @ejb.ejb-ref ejb-name="File" view-type="local" ref-name="ejb/File"
 * @ejb.ejb-ref ejb-name="FileSystem" view-type="local" ref-name="ejb/FileSystem"
 * 
 * @ejb.env-entry name="AttributeFilterConfigURL" type="java.lang.String"
 *                value="resource:dcm4jboss-attribute-filter.xml"
 * @ejb.env-entry name="AttributeCoercionConfigURL" type="java.lang.String"
 *                value="resource:dcm4jboss-attribute-coercion.xml"
 * 
 * @author <a href="mailto:gunter@tiani.com">Gunter Zeilinger </a>
 * @version $Revision$ $Date$
 *  
 */
public abstract class StorageBean implements SessionBean {

    private static final int ForbiddenAttributeCoercion = 0xCB00;
    
	private static Logger log = Logger.getLogger(StorageBean.class);

    private static final DcmObjectFactory dof = DcmObjectFactory.getInstance();

    private PatientLocalHome patHome;

    private StudyLocalHome studyHome;

    private SeriesLocalHome seriesHome;

    private InstanceLocalHome instHome;

    private FileLocalHome fileHome;

    private FileSystemLocalHome fileSystemHome;

    private AttributeFilter attrFilter;

    private AttributeCoercions attrCoercions;

    private SessionContext sessionCtx;

    public void setSessionContext(SessionContext ctx) {
        sessionCtx = ctx;
        Context jndiCtx = null;
        try {
            jndiCtx = new InitialContext();
            patHome = (PatientLocalHome) jndiCtx
                    .lookup("java:comp/env/ejb/Patient");
            studyHome = (StudyLocalHome) jndiCtx
                    .lookup("java:comp/env/ejb/Study");
            seriesHome = (SeriesLocalHome) jndiCtx
                    .lookup("java:comp/env/ejb/Series");
            instHome = (InstanceLocalHome) jndiCtx
                    .lookup("java:comp/env/ejb/Instance");
            fileHome = (FileLocalHome) jndiCtx.lookup("java:comp/env/ejb/File");
            fileSystemHome = (FileSystemLocalHome) jndiCtx
                    .lookup("java:comp/env/ejb/FileSystem");
            attrFilter = new AttributeFilter((String) jndiCtx
                    .lookup("java:comp/env/AttributeFilterConfigURL"));
            attrCoercions = new AttributeCoercions((String) jndiCtx
                    .lookup("java:comp/env/AttributeCoercionConfigURL"));
            try {
            } catch ( Throwable t ) {
            	t.printStackTrace();
            }
        } catch (NamingException e) {
            throw new EJBException(e);
        } catch (ConfigurationException e) {
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
        sessionCtx = null;
        patHome = null;
        studyHome = null;
        seriesHome = null;
        instHome = null;
        fileHome = null;
        fileSystemHome = null;
    }

    /**
     * @ejb.interface-method
     */
    public org.dcm4che.data.Dataset store(org.dcm4che.data.Dataset ds,
            java.lang.String dirpath, java.lang.String fileid, int size,
            byte[] md5) throws DcmServiceException {
        FileMetaInfo fmi = ds.getFileMetaInfo();
        final String iuid = fmi.getMediaStorageSOPInstanceUID();
        final String cuid = fmi.getMediaStorageSOPClassUID();
        final String tsuid = fmi.getTransferSyntaxUID();
        log.info("inserting instance " + fmi);
        try {
            Dataset coercedElements = dof.newDataset();
            InstanceLocal instance = null;
            try {
                instance = instHome.findBySopIuid(iuid);
                coerceInstanceIdentity(instance, ds, coercedElements);
            } catch (ObjectNotFoundException onfe) {
                attrCoercions.coerce(ds, coercedElements);
                final int[] filter = attrFilter.getInstanceFilter();
                instance = instHome.create(ds.subSet(filter),
                        getSeries(ds, coercedElements));
            }
            final String retrieveAET = ds.getString(Tags.RetrieveAET);
			FileSystemLocal fs = getFileSystem(dirpath, retrieveAET, 
					Availability.ONLINE);
            FileLocal file = fileHome.create(fileid, tsuid, size, md5,
                    0, instance, fs);
            instance.setAvailability(Availability.ONLINE);
            instance.addRetrieveAET(fs.getRetrieveAET());
            log.info("inserted records for instance[uid=" + iuid + "]");
            return coercedElements;
        } catch (Exception e) {
            log.error("inserting records for instance[uid=" + iuid
                    + "] failed:", e);
            sessionCtx.setRollbackOnly();
            throw new DcmServiceException(Status.ProcessingFailure);
        }
    }
    
    /**
     * @ejb.interface-method
     */
    public void storeFile(java.lang.String iuid, java.lang.String tsuid,
    		java.lang.String retrieveAET, int availability,
    		java.lang.String dirpath, java.lang.String fileid,
    		int size, byte[] md5, int status)
    throws CreateException, FinderException
    {
		FileSystemLocal fs = getFileSystem(dirpath, retrieveAET, availability);
		InstanceLocal instance = instHome.findBySopIuid(iuid);
        fileHome.create(fileid, tsuid, size, md5, status, instance, fs);    	
    }

	private FileSystemLocal getFileSystem(String dirpath, String retrieveAET,
			int availability)
	throws FinderException {
		try {
		    return fileSystemHome.findByDirectoryPath(dirpath);
		} catch (ObjectNotFoundException onfe) {
		    try {
				return fileSystemHome.create(dirpath, retrieveAET, availability);
			} catch (CreateException e) {
				// try to find again, in case it was concurrently created by another thread
				return fileSystemHome.findByDirectoryPath(dirpath);
			}
		}
	}

    private SeriesLocal getSeries(Dataset ds, Dataset coercedElements)
            throws FinderException, CreateException, DcmServiceException {
        final String uid = ds.getString(Tags.SeriesInstanceUID);
        SeriesLocal series;
        try {
            series = seriesHome.findBySeriesIuid(uid);
            coerceSeriesIdentity(series, ds, coercedElements);
        } catch (ObjectNotFoundException onfe) {
            final int[] filter = attrFilter.getSeriesFilter();
            series = seriesHome.create(ds.subSet(filter),
                    getStudy(ds, coercedElements));
        }
        return series;
    }

    /**
     * @param ds
     * @return
     * @throws DcmServiceException 
     * @throws IllegalAttributeCoercionException 
     */
    private StudyLocal getStudy(Dataset ds, Dataset coercedElements)
            throws CreateException, FinderException, DcmServiceException {
        final String uid = ds.getString(Tags.StudyInstanceUID);
        StudyLocal study;
        try {
            study = studyHome.findByStudyIuid(uid);
            coerceStudyIdentity(study, ds, coercedElements);
        } catch (ObjectNotFoundException onfe) {
            final int[] filter = attrFilter.getStudyFilter();
            study = studyHome.create(ds.subSet(filter),
                    getPatient(ds, coercedElements));
        }

        return study;
    }

    /**
     * @param ds
     * @return
     * @throws DcmServiceException 
     * @throws IllegalAttributeCoercionException 
     */
    private PatientLocal getPatient(Dataset ds, Dataset coercedElements)
            throws CreateException, FinderException, DcmServiceException {
		String pid = ds.getString(Tags.PatientID);
		String issuer = ds.getString(Tags.IssuerOfPatientID);
		try {
			Collection c = issuer != null 
					? patHome.findByPatientIdWithIssuer(pid, issuer)
					: patHome.findByPatientId(pid);
			final int n = c.size();
			switch (n) {
			case 0:
				return patHome.create(ds.subSet(attrFilter.getPatientFilter()));
			case 1:
				return checkIfMerged((PatientLocal) c.iterator().next());
				
			default:
				throw new DcmServiceException(Status.ProcessingFailure,
						"Found " + n + " Patients with id=" + pid
						+ ", issuer=" + issuer);					
			}
		} catch (FinderException e) {
			throw new EJBException(e);
		} catch (CreateException e) {
			throw new EJBException(e);
		}
    }
    
 	private PatientLocal checkIfMerged(PatientLocal pat) {
		PatientLocal merged;
		if ((merged = pat.getMergedWith()) != null)
			pat = merged;
		return pat;
	}

	private boolean equals(PatientLocal patient, Dataset ds) {
        // TODO Auto-generated method stub
        return true;
    }

    private void coercePatientIdentity(PatientLocal patient, Dataset ds,
            Dataset coercedElements) throws DcmServiceException {
        Dataset patAttrs = patient.getAttributes(false);
        Dataset filtered = patAttrs.subSet(attrFilter.getPatientFilter());
        int excludeAttrs = patAttrs.size() - filtered.size();
        if (excludeAttrs > 0) {
            log.warn("Detect " + excludeAttrs + " attributes in record of " +
                    "patient " + patAttrs.getString(Tags.PatientName) + "[" +
                    patAttrs.getString(Tags.PatientID) + "] which does not " +
                    "match current configured patient attribute filter -> " +
                    "removed attributes from patient record");
            patient.setAttributes(filtered);
        }
        coerceIdentity(filtered, ds, coercedElements);
    }

    private void coerceStudyIdentity(StudyLocal study, Dataset ds,
            Dataset coercedElements) throws DcmServiceException {
        coercePatientIdentity(study.getPatient(), ds, coercedElements);
        coerceIdentity(study.getAttributes(false), ds, coercedElements);
    }

    private void coerceSeriesIdentity(SeriesLocal series, Dataset ds,
            Dataset coercedElements) throws DcmServiceException {
        coerceStudyIdentity(series.getStudy(), ds, coercedElements);
        coerceIdentity(series.getAttributes(false), ds, coercedElements);
    }

    private void coerceInstanceIdentity(InstanceLocal instance, Dataset ds,
            Dataset coercedElements) throws DcmServiceException {
        coerceSeriesIdentity(instance.getSeries(), ds, coercedElements);
        coerceIdentity(instance.getAttributes(false), ds, coercedElements);
    }

    private boolean coerceIdentity(Dataset ref, Dataset ds,
            Dataset coercedElements) throws DcmServiceException {
        boolean coercedIdentity = false;
        for (Iterator it = ref.iterator(); it.hasNext();) {
            DcmElement refEl = (DcmElement) it.next();
            final int tag = refEl.tag();
			DcmElement el = ds.get(tag);
            if (!equals(el,
                    ds.getCharset(),
                    refEl,
                    ref.getCharset(),
                    coercedElements)) {
            	if (attrFilter.isCoercionForbidden(tag)) {
            		throw new DcmServiceException(ForbiddenAttributeCoercion,
            				"Storage would require forbidden Coercion of " + el + " to " + refEl);
            	}
                log.warn("Coerce " + el + " to " + refEl);
                if (coercedElements != null) {
                    if (VRs.isLengthField16Bit(refEl.vr())) {
                        coercedElements.putXX(tag, refEl
                                .getByteBuffer());
                    } else {
                        coercedElements.putXX(tag);
                    }
                }
                coercedIdentity = true;
            }
        }
        return coercedIdentity;
    }

    private boolean equals(DcmElement el, Charset cs, DcmElement refEl,
            Charset refCS, Dataset coercedElements)
    		throws DcmServiceException {
        final int vm = refEl.vm();
        if (el == null || el.vm() != vm) { return false; }
        final int vr = refEl.vr();
        if (vr == VRs.OW || vr == VRs.OB || vr == VRs.UN) {
            // no check implemented!
            return true;
        }
        for (int i = 0; i < vm; ++i) {
            if (vr == VRs.SQ) {
                if (coerceIdentity(refEl.getItem(i), el.getItem(i), null)) {
                    if (coercedElements != null) {
                        coercedElements.putSQ(el.tag());
                    }
                }
            } else {
                try {
                    if (!(vr == VRs.PN ? refEl.getPersonName(i, refCS)
                            .equals(el.getPersonName(i, cs)) : refEl
                            .getString(i, refCS).equals(el.getString(i, cs)))) { return false; }
                } catch (DcmValueException e) {
                    log.warn("Failure during coercion of " + el, e);
                }
            }
        }
        return true;
    }
    /**
     * @ejb.interface-method
     */
    public boolean unhide(String iuid) throws FinderException {
        InstanceLocal il = instHome.findBySopIuid(iuid);
        boolean wasHidden = il.getHiddenSafe();
        il.setHidden(false);
        SeriesLocal sl = il.getSeries();
        sl.setHidden(false);
        StudyLocal stl = sl.getStudy();
        stl.setHidden(false);
		stl.getPatient().setHidden(false);
		return wasHidden;
    }

    /**
     * @ejb.interface-method
     */
    public void commit(String iuid) throws FinderException {
        instHome.findBySopIuid(iuid).setCommitment(true);
    }
    
    /**
     * @ejb.interface-method
     */
    public void commited(Dataset stgCmtResult) throws FinderException {
        DcmElement refSOPSeq = stgCmtResult.get(Tags.RefSOPSeq);
        if (refSOPSeq == null) return;
        HashSet seriesSet = new HashSet();
        HashSet studySet = new HashSet();
        final String aet0 = stgCmtResult.getString(Tags.RetrieveAET);
        for (int i = 0, n = refSOPSeq.vm(); i < n; ++i) {
            final Dataset refSOP = refSOPSeq.getItem(i);
            final String iuid = refSOP.getString(Tags.RefSOPInstanceUID);
            final String aet = refSOP.getString(Tags.RetrieveAET, aet0);
            if (iuid != null && aet != null)
            	commited(seriesSet, studySet, iuid, aet);
        }
        for (Iterator series = seriesSet.iterator(); series.hasNext();) {
            final SeriesLocal ser = seriesHome.findBySeriesIuid((String) series.next());
			ser.updateDerivedFields(false, false, true, false, false, true);
        }
        for (Iterator studies = studySet.iterator(); studies.hasNext();) {
            final StudyLocal study = studyHome.findByStudyIuid((String) studies.next());
			study.updateDerivedFields(false, false, true, false, false, false, true);
        }
    }

    private void commited(HashSet seriesSet, HashSet studySet, final String iuid, final String aet) throws FinderException {
		InstanceLocal inst = instHome.findBySopIuid(iuid);
		inst.setExternalRetrieveAET(aet);
		SeriesLocal series = inst.getSeries();
		seriesSet.add(series.getSeriesIuid());
		StudyLocal study = series.getStudy();
		studySet.add(study.getStudyIuid());
	}
    
    /**
     * @ejb.interface-method
     */
    public void updateStudy(String iuid) throws FinderException {
        final StudyLocal study = studyHome.findByStudyIuid(iuid);
		study.updateDerivedFields(true, true, false, true, true, true, false);
		if ( study.updateDerivedFields(false, false, false, false, false, false, true) )
			study.getPatient().updateDerivedFields();
		
    }
    
    /**
     * @ejb.interface-method
     */
    public void updateSeries(String iuid) throws FinderException {
        final SeriesLocal series = seriesHome.findBySeriesIuid(iuid);
        series.updateDerivedFields(true, true, false, true, true, true);
    }
}