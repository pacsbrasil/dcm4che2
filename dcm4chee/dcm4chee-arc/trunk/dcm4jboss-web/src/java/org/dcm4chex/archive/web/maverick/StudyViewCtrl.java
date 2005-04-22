/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
package org.dcm4chex.archive.web.maverick;

import java.util.ArrayList;
import java.util.List;

import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmElement;
import org.dcm4che.dict.Tags;
import org.dcm4chex.archive.ejb.interfaces.ContentManager;
import org.dcm4chex.archive.ejb.interfaces.ContentManagerHome;
import org.dcm4chex.archive.util.EJBHomeFactory;
import org.dcm4chex.archive.web.maverick.model.PatientModel;

/**
 * @author gunter.zeilinger@tiani.com
 * @version $Revision$ $Date$
 * @since 5.10.2004
 *
 */
public class StudyViewCtrl extends Dcm4JbossController {
    private int patPk;

    private int studyPk;
    
    private PatientModel patient;
    
    private StudyContainer study;

    private int selectedSeries = 1;

    public PatientModel getPatient() {
    	return patient;
    }
	/**
	 * @return Returns the sopIUIDs.
	 */
	public StudyContainer getStudyContainer() {
		return study;
	}
    public final int getPatPk() {
        return patPk;
    }

    public final void setPatPk(int pk) {
        this.patPk = pk;
    }

    public final int getStudyPk() {
        return studyPk;
    }

    public final void setStudyPk(int pk) {
        this.studyPk = pk;
    }
    
    public int getSelectedSeries() {
    	return this.selectedSeries;
    }

    protected String perform() throws Exception {
    	patient = new PatientModel( lookupContentManager().getPatientForStudy( studyPk ) );
    	Dataset sopIUIDs = lookupContentManager().getSOPInstanceRefMacro( studyPk, true );
    	study = new StudyContainer( sopIUIDs );
        return SUCCESS;
    }
    
    private ContentManager lookupContentManager() throws Exception {
        ContentManagerHome home = (ContentManagerHome) EJBHomeFactory.getFactory()
                .lookup(ContentManagerHome.class, ContentManagerHome.JNDI_NAME);
        return home.create();
    }
    
    
    public class StudyContainer {
    	private String studyIUID;
    	
    	private List series = new ArrayList();
    	
    	public StudyContainer( Dataset ds ) {
    		studyIUID = ds.getString( Tags.StudyInstanceUID );
    		DcmElement serSeq = ds.get( Tags.RefSeriesSeq );
    		for ( int i = 0, len = serSeq.vm() ; i < len ; i++ ) {
    			series.add( new SeriesContainer( serSeq.getItem(i)) );
    		}
    	}
    	
    	public String getStudyIUID() {
    		return studyIUID;
    	}
    	public List getSeries() {
    		return series;
    	}
    }

    public class SeriesContainer {
    	private String seriesIUID;
    	private String modality;
    	private String seriesNumber;
    	
    	private List instanceUIDs = new ArrayList();
    	
    	public SeriesContainer( Dataset ds ) {
    		seriesIUID = ds.getString( Tags.SeriesInstanceUID );
    		modality = ds.getString( Tags.Modality );
    		seriesNumber = ds.getString( Tags.SeriesNumber );
			DcmElement refSopSq = ds.get(Tags.RefSOPSeq);
    		for ( int i = 0, len = refSopSq.vm() ; i < len ; i++ ) {
    			instanceUIDs.add( refSopSq.getItem(i).getString( Tags.RefSOPInstanceUID ) );
    		}
    	}
    	
    	public String getSeriesIUID() {
    		return seriesIUID;
    	}
    	public String getSeriesNumber() {
    		return seriesNumber;
    	}
    	public String getModality() {
    		return modality;
    	}
    	public List getInstanceUIDs() {
    		return instanceUIDs;
    	}
    }

}