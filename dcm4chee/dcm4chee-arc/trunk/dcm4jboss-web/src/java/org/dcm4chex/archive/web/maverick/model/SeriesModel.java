/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
package org.dcm4chex.archive.web.maverick.model;

import java.util.List;

import org.dcm4che.data.Dataset;
import org.dcm4che.dict.Tags;
import org.dcm4cheri.util.StringUtils;
import org.dcm4chex.archive.common.PrivateTags;

/**
 * @author gunter.zeilinger@tiani.com
 * @version $Revision$ $Date$
 * @since 05.10.2004
 *
 */
public class SeriesModel extends AbstractModel {

    private int pk = -1;

    private boolean incorrectWLEntry = false;
    private String drCode = null;
    private String drCodeMeaning = null;
    private String drCodeDesignator = null;

    
    
    public SeriesModel() {
    }

    public SeriesModel(Dataset ds) {
        super(ds);
        ds.setPrivateCreatorID(PrivateTags.CreatorID);
        isHidden = ds.getInt( PrivateTags.HiddenSeries, 0) != 0;
        Dataset item = ds.getItem(Tags.PPSDiscontinuationReasonCodeSeq);
        if ( item != null ) {
	        drCode = item.getString(Tags.CodeValue);
	        drCodeMeaning = item.getString(Tags.CodeMeaning);
	        drCodeDesignator = item.getString(Tags.CodingSchemeDesignator);
	        incorrectWLEntry = "110514".equals(drCode) && "DCM".equals(drCodeDesignator);
    	}
        this.pk = ds.getInt(PrivateTags.SeriesPk, -1);
    }

    public final int getPk() {
        return pk;
    }

    public final void setPk(int pk) {
        ds.setPrivateCreatorID(PrivateTags.CreatorID);
        ds.putUL(PrivateTags.SeriesPk, pk);
        this.pk = pk;
    }

    public int hashCode() {
        return pk;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SeriesModel)) return false;
        SeriesModel other = (SeriesModel) o;
        return pk == other.pk;
    }

    
	/**
	 * @return Returns the incorrectWLEntry.
	 */
	public boolean isIncorrectWLEntry() {
		return incorrectWLEntry;
	}
    public final String getBodyPartExamined() {
        return ds.getString(Tags.BodyPartExamined);
    }

    public final void setBodyPartExamined(String s) {
        ds.putCS(Tags.BodyPartExamined, s);
    }

    public final String getLaterality() {
        return ds.getString(Tags.Laterality);
    }

    public final void setLaterality(String s) {
        ds.putCS(Tags.Laterality, s);
    }

    public final String getManufacturer() {
        return ds.getString(Tags.Manufacturer);
    }

    public final void setManufacturer(String s) {
        ds.putCS(Tags.Manufacturer, s);
    }

    public final String getManufacturerModelName() {
        return ds.getString(Tags.ManufacturerModelName);
    }

    public final void setManufacturerModelName(String s) {
        ds.putCS(Tags.ManufacturerModelName, s);
    }

    public final String getModality() {
        return ds.getString(Tags.Modality);
    }

    public final void setModality(String s) {
        ds.putCS(Tags.Modality, s);
    }

    public final String getSeriesDateTime() {
        return getDateTime(Tags.SeriesDate, Tags.SeriesTime);
    }

    public final void setSeriesDateTime(String s) {
        setDateTime(Tags.SeriesDate, Tags.SeriesTime, s);
    }

    public final String getSeriesDescription() {
        return ds.getString(Tags.SeriesDescription);
    }

    public final void setSeriesDescription(String s) {
        ds.putLO(Tags.SeriesDescription, s);
    }

    public final String getSeriesIUID() {
        return ds.getString(Tags.SeriesInstanceUID);
    }

    public final void setSeriesIUID(String s) {
        ds.putUI(Tags.SeriesInstanceUID, s);
    }

    public final String getSeriesNumber() {
        return ds.getString(Tags.SeriesNumber);
    }

    public final void setSeriesNumber(String s) {
        ds.putIS(Tags.SeriesNumber, s);
    }

    public final int getNumberOfInstances() {
        return ds.getInt(Tags.NumberOfSeriesRelatedInstances, 0);
    }

    public final String getAvailability() {
        return ds.getString(Tags.InstanceAvailability);
    }

    public final String getRetrieveAETs() {
        return StringUtils.toString(ds.getStrings(Tags.RetrieveAET), '\\');
    }

    public final String getFilesetId() {
        String s = ds.getString(Tags.StorageMediaFileSetID);
        if ( s == null || s.trim().length() < 1 ) s = "_NA_";
        return s;
    }
    
    /**
     * Returns the list of Instances.
     * <p>
     * Use the <code>childs</code> from <code>AbstractModel</code> method now.
     * 
     * @return Instances as List.
     */
    public final List getInstances() {
        return listOfChilds();
    }

    /**
     * Set a new list of instances.
     * <p>
     * Use the <code>setChilds</code> from <code>AbstractModel</code> method now.
     * 
     * @param instances List of instances
     */
    public final void setInstances(List instances) {
        setChilds(instances);
    }
    
    public String getPPSID() {
    	return ds.getString(Tags.PPSID);
    }

    public String getPPSDesc() {
    	return ds.getString(Tags.PPSDescription);
    }
    
    public String getPPSStatus() {
    	return ds.getString(Tags.PPSStatus);
    }

    public String getDRCode() { return drCode; }
    public String getDRCodeDesignator() { return drCodeDesignator; }
    public String getDRCodeMeaning() { return drCodeMeaning; }
    
    public String getPPSStartDate() {
    	return getDateTime(Tags.PPSStartDate, Tags.PPSStartTime);
    }
    
    public String getPPSEndDate() {
    	return getDateTime(Tags.PPSEndDate, Tags.PPSEndTime);
    }
    
}