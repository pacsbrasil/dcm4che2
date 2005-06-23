/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
package org.dcm4chex.archive.hl7;

import javax.xml.transform.Transformer;
import javax.xml.transform.sax.SAXResult;

import org.dcm4che.data.Dataset;
import org.dcm4che.dict.Tags;
import org.dcm4chex.archive.ejb.interfaces.PatientUpdate;
import org.dcm4chex.archive.ejb.interfaces.PatientUpdateHome;
import org.dcm4chex.archive.util.EJBHomeFactory;
import org.dcm4chex.archive.util.HomeFactoryException;
import org.dom4j.Document;
import org.dom4j.io.DocumentSource;
import org.xml.sax.ContentHandler;

/**
 * @author gunter.zeilinger@tiani.com
 * @version Revision $Date$
 * @since 26.12.2004
 */

public class ADTService extends AbstractHL7Service {

    private static final String ADT_A08 = "A08";
	private String pidStylesheetURL = "resource:xsl/hl7/pid2dcm.xsl";

    private String mrgStylesheetURL = "resource:xsl/hl7/mrg2dcm.xsl";
    
    private String patientUpdateMessages = ADT_A08+",A01,A04,A05";//A01,A04,A05 'create' patient (via update)
    
    public String getPatientUpdateMessages() {
    	return patientUpdateMessages;
    }
    
    public void setPatientUpdateMessages( String msgs ) {
    	if (msgs == null || msgs.trim().length() < 1) {
    		patientUpdateMessages = ADT_A08;
    	} else {
    		if ( msgs.indexOf( ADT_A08 ) == -1 ) {
    			patientUpdateMessages = ADT_A08+","+msgs;
    		}
    	}
    }

    public final String getMrgStylesheetURL() {
		return mrgStylesheetURL;
	}

	public final void setMrgStylesheetURL(String mrgStylesheetURL) {
		this.mrgStylesheetURL = mrgStylesheetURL;
		reloadStylesheets();
	}

	public final String getPidStylesheetURL() {
		return pidStylesheetURL;
	}

	public final void setPidStylesheetURL(String pidStylesheetURL) {
		reloadStylesheets();
		this.pidStylesheetURL = pidStylesheetURL;
	}

	public boolean process(MSH msh, Document msg, ContentHandler hl7out)
            throws HL7Exception {
        Dataset pat = dof.newDataset();
        try {
            Transformer t = getTemplates(pidStylesheetURL).newTransformer();
            t.transform(new DocumentSource(msg), new SAXResult(pat
                    .getSAXHandler2(null)));
            PatientUpdate update = getPatientUpdateHome().create();
            try {
                if (isUpdate(msh)) {
                    log.info("Update Patient Info of " 
                            + pat.getString(Tags.PatientName)
                            + ", PID:" + pat.getString(Tags.PatientID));
                    update.updatePatient(pat);

                } else if (isMerge(msh)) {
                    Dataset mrg = dof.newDataset();
                    Transformer t2 = getTemplates(mrgStylesheetURL)
                            .newTransformer();
                    t2.transform(new DocumentSource(msg), new SAXResult(mrg
                            .getSAXHandler2(null)));
                    log.info("Merge Patient " 
                            + mrg.getString(Tags.PatientName)
                            + ", PID:" + mrg.getString(Tags.PatientID)
                            + " with "+ pat.getString(Tags.PatientName)
                            + ", PID:" + pat.getString(Tags.PatientID));
                    update.mergePatient(pat, mrg);
                } else if ( isDelete(msh) ) {
                	log.info("Delete Patient "+pat.getString( Tags.PatientName )+", PID:"+pat.getString( Tags.PatientID ));
                	update.deletePatient(pat);
                }
            } finally {
                try {
                    update.remove();
	            } catch (Exception ignore) {
	            }
            }
        } catch (Exception e) {
            throw new HL7Exception("AE", e.getMessage(), e);
        }
        return true;
    }

    private boolean isUpdate(MSH msh) {
        return patientUpdateMessages.indexOf(msh.triggerEvent) != -1;
    }

    private boolean isMerge(MSH msh) {
        return "A40".equals(msh.triggerEvent);
    }

    private boolean isDelete(MSH msh) {
        return "A23".equals(msh.triggerEvent);
    }

    private PatientUpdateHome getPatientUpdateHome()
            throws HomeFactoryException {
        return (PatientUpdateHome) EJBHomeFactory.getFactory().lookup(
                PatientUpdateHome.class, PatientUpdateHome.JNDI_NAME);
    }
}