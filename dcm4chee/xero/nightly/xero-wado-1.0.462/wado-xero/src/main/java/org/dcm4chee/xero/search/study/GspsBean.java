package org.dcm4chee.xero.search.study;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.annotation.XmlAnyAttribute;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.namespace.QName;

import static org.dcm4chee.xero.search.study.PatientBean.sanitizeString;

import org.dcm4che2.data.DicomElement;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4chee.xero.search.LocalModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a GSPS (Presentation State) DICOM Object, potentially including the child markups and text annotations.
 * @author bwallace
 */
public class GspsBean extends GspsType implements LocalModel<String>, DicomObjectInterface {
	static Logger log = LoggerFactory.getLogger(GspsBean.class.getName());

	Set<String> referencedSOPInstance;
	
	
	@XmlTransient
	SeriesBean series;
	
	@XmlTransient
	MacroItems macroItems;
	
	/** Constructor for JAXB 
	 * @deprecated
	 */
	public GspsBean() {
	   
	}
	
	/** Create a GSPS Bean object */
	public GspsBean(SeriesBean series, DicomObject dobj) {
	    this.series = series;
		addResult(dobj);
	}

	/**
	 * Initialize the image level attributes by copying the DicomObject's image
	 * level data for Columns, Rows, SOP Instance UID and Instance Number.
	 * 
	 * @param dcmobj
	 *            to copy image level data into this from.
	 */
	public void addResult(DicomObject dcmobj) {
		setObjectUID(dcmobj.getString(Tag.SOPInstanceUID));
		setContentLabel(sanitizeString(dcmobj.getString(Tag.ContentLabel)));
		Date date = null;
		try {
			date = dcmobj.getDate(Tag.PresentationCreationDate,
					Tag.PresentationCreationTime);
		} catch (NumberFormatException nfe) {
			log.warn("Illegal presentation date or time:" + nfe);
		}
		if (date == null) {
			try {
				date = dcmobj.getDate(Tag.ContentDate, Tag.ContentTime);
			} catch (NumberFormatException nfe) {
				log.warn("Illegal presentation date or time:" + nfe);
			}
		}
		if (date == null) {
			log.warn("GSPS presentation and content dates both null for "
					+ getObjectUID());
			date = new Date(0);
		}
		GregorianCalendar cal = new GregorianCalendar();
		cal.setTime(date);
		setPresentationDateTime(PatientBean.datatypeFactory
				.newXMLGregorianCalendar(cal));
		initReferencedImages(dcmobj);
	}

	private void initReferencedImages(DicomObject dcmobj) {
		// Initialize the referenced instances.
		DicomElement sqSeries = dcmobj.get(Tag.ReferencedSeriesSequence);
		if (sqSeries != null && sqSeries.hasItems()) {
			referencedSOPInstance = new HashSet<String>();
			int serCount = sqSeries.countItems();
			for (int i = 0; i < serCount; i++) {
				DicomObject seriesDcm = sqSeries.getDicomObject(i);
				DicomElement sqImage = seriesDcm
						.get(Tag.ReferencedImageSequence);
				if (sqImage == null || !sqImage.hasItems()) {
					log
							.warn("GSPS object "
									+ getObjectUID()
									+ " has no referenced images for one of the series.");
					continue;
				}
				int imgCount = sqImage.countItems();
				for (int j = 0; j < imgCount; j++) {
					DicomObject imageDcm = sqImage.getDicomObject(j);
					String uid = imageDcm
							.getString(Tag.ReferencedSOPInstanceUID);
					if (uid == null) {
						log.warn("GSPS object " + getObjectUID()
								+ " has a null SOP instance reference.");
						continue;
					}
					referencedSOPInstance.add(uid);
				}
			}
		} else {
			log.warn("GSPS object " + getObjectUID()
					+ " has no referenced series.");
		}
	}

	/**
	 * GspsBean's are always considered empty - they don't have any
	 * customizations yet.
	 */
	public boolean clearEmpty() {
		return true;
	}

	/** The SOP Instance is the ID for a GSPS object */
	public String getId() {
		return getObjectUID();
	}

	/** Get the set of object uid's that this GSPS references */
	public Set<String> getReferencedSOPInstance() {
		return referencedSOPInstance;
	}

   public SeriesBean getSeriesBean() {
      return series;
   }

   /** Gets additional attributes and child elements defined in other objects */
   public MacroItems getMacroItems() {
      if (macroItems == null)
         macroItems = new MacroItems();
      return macroItems;
   }
   
   /** Get the attributes from the macro items that are included in this object. */
   @XmlAnyAttribute
   public Map<QName, String> getOtherAttributes() {
      Map<QName, String> ret = null;
      if (macroItems != null)
         ret = macroItems.getAnyAttributes();
      if( log.isDebugEnabled() ) log.debug("Getting other attributes="+ret);
      return ret;
   }
   
   @XmlAnyElement(lax=true)
   public List<Object> getOtherElements() {
      if( this.macroItems==null ) return null;
      return this.macroItems.getOtherElements();
   }

   /** Adds the given macro to this objects set of macro items */
   public void addMacro(Macro m) {
      clearMacro(m.getClass());
      getMacroItems().addMacro(m);
   }
   
   /** Clears the macro from this class, and any children of this class.
    * This uses getMacroItems rather than direct access to the variable in order to work with both
    * frame and non-framed images.  It still needs extra work for the multi-frame single object, however. */
   public void clearMacro(Class<? extends Macro> clazz) {
      Macro item = getMacroItems().findMacro(clazz);
      if (item != null)
         getMacroItems().removeMacro(item);
   }
}
