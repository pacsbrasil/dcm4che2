package org.dcm4che.auditlog;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Date;

import org.xml.sax.SAXException;

/**
 * @author joseph foraci
 *
 * This is the interface for a Audit Record Repository service that may be used
 * to parse a IHE audit message.
 */
public interface ArrService {
	void parse(File file)
		throws ArrInputException;
	void parse(String xmlData)
		throws ArrInputException;
	void parse(InputStream is)
		throws ArrInputException;

	/**
	 * Get the audit type from parsed data (ie the name of the audit event
	 * element that was generated for the triggering event).
	 *
	 * @return A <code>String</code> representing the audit type or
	 *   <code>null</code> if it was not included.
	 */
	String getType();
	/** Get parsed Hostname from Host element
	 *
	 * @return A <code>String</code> representing the host mentioned or
	 *   <code>null</code> if it was not included.
	 */
	String getHost();
	/** Get parsed TimeStamp from Timestamp element
	 *
	 * @return A <code>Date</code> representing the time stamp mentioned or
	 *   <code>null</code> if it was not included.
	 */
	Date getTimeStamp();
}
