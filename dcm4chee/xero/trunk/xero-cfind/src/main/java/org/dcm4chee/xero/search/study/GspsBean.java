package org.dcm4chee.xero.search.study;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Set;

import org.dcm4che2.data.DicomElement;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4chee.xero.search.LocalModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GspsBean extends GspsType implements LocalModel<String> {
	static Logger log = LoggerFactory.getLogger(GspsBean.class.getName());

	Set<String> referencedSOPInstance;

	/** Create a GSPS Bean object */
	public GspsBean(DicomObject dobj) {
		initAttributes(dobj);
	}

	/**
	 * Initialize the image level attributes by copying the DicomObject's image
	 * level data for Columns, Rows, SOP Instance UID and Instance Number.
	 * 
	 * @param dcmobj
	 *            to copy image level data into this from.
	 */
	protected void initAttributes(DicomObject dcmobj) {
		setSOPInstanceUID(dcmobj.getString(Tag.SOPInstanceUID));
		setContentLabel(sanitizeXmlString(dcmobj.getString(Tag.ContentLabel)));
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
					+ getSOPInstanceUID());
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
									+ getSOPInstanceUID()
									+ " has no referenced images for one of the series.");
					continue;
				}
				int imgCount = sqImage.countItems();
				for (int j = 0; j < imgCount; j++) {
					DicomObject imageDcm = sqImage.getDicomObject(j);
					String uid = imageDcm
							.getString(Tag.ReferencedSOPInstanceUID);
					if (uid == null) {
						log.warn("GSPS object " + getSOPInstanceUID()
								+ " has a null SOP instance reference.");
						continue;
					}
					referencedSOPInstance.add(uid);
				}
			}
		} else {
			log.warn("GSPS object " + getSOPInstanceUID()
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
		return getSOPInstanceUID();
	}

	/** This method replaces illegal charaters in a string with spaces. */
	public static String sanitizeXmlString(String str) {
		if (str.indexOf(0x1B) >= 0) {
			str = str.replace((char) 0x1B, ' ');
		}
		return str;
	}

	/** Get the set of object uid's that this GSPS references */
	public Set<String> getReferencedSOPInstance() {
		return referencedSOPInstance;
	}
}
