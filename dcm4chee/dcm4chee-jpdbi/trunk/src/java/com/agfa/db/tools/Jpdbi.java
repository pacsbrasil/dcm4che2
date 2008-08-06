/*
$Id: Jpdbi.java,v 1.18.2.12 2008-06-20 23:06:21 kianusch Exp $
$Log: Jpdbi.java,v $
Revision 1.18.2.12  2008-06-20 23:06:21  kianusch
Multi Update ist supportet (no Syntax Checking)

Revision 1.18.2.11  2008-06-19 22:12:01  kianusch
 *** empty log message ***

Revision 1.18.2.10  2008-06-19 17:55:26  kianusch
 * Update Modality
 * Append "^" to Patient Name if less than 4 Provided

Revision 1.18.2.9  2008-06-19 09:29:45  kianusch
 *** empty log message ***

Revision 1.18.2.8  2008-06-18 18:47:48  kianusch
UPDATE :)

Revision 1.18.2.7  2008-06-18 18:34:49  kianusch
 *** empty log message ***

Revision 1.18.2.6  2008-06-18 00:04:00  kianusch
 *** empty log message ***

Revision 1.18.2.5  2008-06-17 16:55:54  kianusch
 * Starting DataSet Update implementation

Does not work yet!

Revision 1.18.2.4  2008-06-17 15:16:49  kianusch
nix geht

Revision 1.18.2.3  2008-06-17 12:55:36  kurt
...

Revision 1.18.2.2  2008-06-17 10:23:03  thomas
change properties of dataset

Revision 1.18.2.1  2008-06-17 07:10:58  kianusch
 * XML Output

Revision 1.18  2008-05-13 00:14:48  kianusch
Code Cleanup

Revision 1.17  2008-05-02 14:16:28  kianusch
 * --dataset
 * --patds, --styds, --serds
 * Compiles clean

Revision 1.16  2008-05-02 12:23:19  willi
debug logs added

Revision 1.15  2008-05-02 11:34:15  kianusch
Still Broken!

Revision 1.14  2008-05-01 17:48:57  kianusch
Broken!

Revision 1.13  2007-10-25 16:13:25  kianusch
Fixed missing [SPACE] character, when displaying the STUDY-Path.

Revision 1.12  2007-06-06 14:00:34  kianusch
--port option

Revision 1.11  2007-06-06 13:15:43  kianusch
Cosmetic changes

Revision 1.10  2007-06-06 13:07:04  kianusch
Cosmetic changes

Revision 1.9  2007-06-06 13:02:31  kianusch
Initial Release

Revision 1.8  2007-06-06 11:11:44  kianusch
Code Cleanup

Revision 1.7  2007-06-06 08:37:07  kurt
uncompileable state (just some structural changes for demonstation issues)

Revision 1.6  2007-06-06 08:06:12  kianusch
Ignore Case when using "-m"

Revision 1.5  2007-06-06 01:53:28  kianusch
First working code

Revision 1.4  2007-06-05 22:47:31  kianusch
Study Level

Revision 1.3  2007-06-05 16:44:13  kianusch
Patient Level

Revision 1.2  2007-06-05 15:06:24  kianusch
First Code

Revision 0.1  2007/06/05 10:16:49  kianusch
Initial Release

 */

package com.agfa.db.tools;

/* GNU.GetOpt */
import gnu.getopt.Getopt;
import gnu.getopt.LongOpt;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URL;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Properties;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmEncodeParam;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.data.DcmParser;
import org.dcm4che.data.DcmParserFactory;
import org.dcm4che.dict.DictionaryFactory;
import org.dcm4che.dict.TagDictionary;
import org.dcm4che.dict.Tags;

// import sun.util.calendar.BaseCalendar.Date;

public class Jpdbi {
	public static final int OPT_DEBUG = 1;
	public static final int OPT_HELP = 2;
	public static final int OPT_SID = 3;
	public static final int OPT_PATH = 4;
	public static final int OPT_PATID = 5;
	public static final int OPT_SOP = 6;
	public static final int OPT_DATASET = 7;
	public static final int OPT_PATDS = 8;
	public static final int OPT_SERDS = 9;
	public static final int OPT_STYDS = 10;
	public static final int OPT_UPDATE = 11;
	public static final int OPT_UPDATE_CNT = 12;

	public static final int MODE_DISPLAY = 1;
	public static final int MODE_UPDATE = 2;

	private static final DcmParserFactory pfact = DcmParserFactory
			.getInstance();

	private URL xslt = null;
	private LinkedList xsltParams = new LinkedList();
	private boolean xsltInc = false;
	private OutputStream out = System.out;
	private int[] excludeTags = {};
	private int excludeValueLengthLimit = Integer.MAX_VALUE;
	private File baseDir;

	private static String newline = System.getProperty("line.separator");

	private static boolean debug = false;

	// private static boolean force=false;

	private static long lastPat = -1L;

	private static long lastSty = -1L;

	private static long lastSeries = -1L;

	private static String db_port = null;

	private static String db_un = null;

	private static String db_pw = null;

	private static String jdbcUrl = null;

	private static String series = null;

	private static String study = null;

	private static String stydate = null;

	private static String modality = null;

	private static String patid = null;

	private static String anyid = null;

	private static boolean displaypath = false;

	private static boolean displaysopid = false;

	private static boolean patds = false;
	private static boolean styds = false;
	private static boolean serds = false;

	private static String lastname = null;

	private static String firstname = null;

	private static String birthdate = null;

	private static String upd = null;
	private static String upd_level = null;
	private static long upd_cnt = 1;
	private static boolean upd_mod = false;
    private static boolean upd_name = false;
    private static boolean upd_multi = true;

	private static String[] upd_dicom;

	private static SimpleDateFormat bdf = new SimpleDateFormat("yyyy-MM-dd");
	private static SimpleDateFormat sdf = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss");

	public final void setExcludeValueLengthLimit(int excludeValueLengthLimit) {
		this.excludeValueLengthLimit = excludeValueLengthLimit;
	}

	public final void setBaseDir(File baseDir) {
		this.baseDir = baseDir;
	}

	private TagDictionary dict = DictionaryFactory.getInstance()
			.getDefaultTagDictionary();

	public Jpdbi(int Mode) {
		switch (Mode) {
		case MODE_DISPLAY:
			display();
			break;
		case MODE_UPDATE:
			update();
			break;
		default:
			break;
		}
	}

	public String makeWhere(String level) {
		String where = "";
		boolean patient_table = false;
		boolean study_table = false;
		boolean series_table = false;
		boolean instance_table = false;

		if (lastname != null || firstname != null) {
			if (lastname == null)
				lastname = "%";
			if (firstname == null)
				firstname = "%";

			where = where.concat(whereClause(where, "upper(pat_name)",
					"upper('" + lastname + "^" + firstname + "^%^%')", false));
			patient_table = true;
		}

		if (birthdate != null) {
			where = where.concat(whereClause(where, "PAT_BIRTHDATE", "{d'"
					+ birthdate + "'}", false));
			patient_table = true;
		}

		if (series != null) {
			where = where
					.concat(whereClause(where, "SERIES_IUID", series, true));
			series_table = true;
		}

		if (patid != null) {
			String issuer = null;
			int split = patid.indexOf(':');
			if (split == -1) {
				issuer = null;
			} else {
				issuer = patid.substring(0, split);
				patid = patid.substring(split + 1);
			}

			/*
			 * System.out.print(patid+"\n"); System.out.print(issuer+"\n");
			 */

			where = where.concat(whereClause(where, "PAT_ID", patid, true));
			where = where.concat(whereClause(where, "PAT_ID_ISSUER", issuer,
					true));
			patient_table = true;
		}

		if (modality != null) {
			/*
			 * where = where.concat(whereClause(where, "upper(MODS_IN_STUDY)",
			 * "upper('%" + modality + "%')", false));
			 */
			where = where.concat(whereClause(where, "upper(MODALITY)",
					"upper('" + modality + "')", false));
			series_table = true;
		}

		if (study != null) {
			where = where.concat(whereClause(where, "STUDY_IUID", study, true));
			study_table = true;
		}

		if (stydate != null) {
			if (where != null && where != "")
				where = where.concat(" and ");
			long dummy = Long.parseLong(stydate);
			if (dummy <= 999) {
				Calendar cal = Calendar.getInstance();
				cal.add(Calendar.DATE, (int) -dummy);
				String mydate = "" + cal.get(Calendar.YEAR) + "-"
						+ (cal.get(Calendar.MONTH) + 1) + "-"
						+ (cal.get(Calendar.DATE));
				where = where + " STUDY_DATETIME" + ">= {ts'" + mydate
						+ " 00:00:00'}";
			} else {
				where = where + " (STUDY_DATETIME" + ">= {ts'" + stydate
						+ " 00:00:00'} and";
				where = where + " STUDY_DATETIME" + "<= {ts'" + stydate
						+ " 23:59:59'})";
			}
			series_table = true;
			study_table = true;
		}

		if (anyid != null) {
			String operator = null;
			if (where != null && where != "")
				where = where.concat(" and ");

			if (anyid.indexOf('%') != -1)
				operator = " like ";
			else
				operator = " = ";

			where = where.concat("( STUDY_IUID " + operator + "'" + anyid
					+ "' or SERIES_IUID" + operator + "'" + anyid + "') ");
			study_table = true;
			series_table = true;
		}

		/*
		if (level != null) {
			boolean error = false;
			if (patient_table && !level.equalsIgnoreCase("PATIENT"))
				error = true;
			if (study_table && !level.equalsIgnoreCase("STUDY"))
				error = true;
			if (series_table && !level.equalsIgnoreCase("SERIES"))
				error = true;
			if (instance_table && !level.equalsIgnoreCase("INSTANCE"))
				error = true;
			if (error)
				return null;
		}
		*/
		return where;
	}

	public void update_modality(Connection connection, long fk) {

		String sql = "select distinct MODALITY from " + upd_level
				+ " where STUDY_FK=" + fk;
		Statement statement;
		try {
			statement = connection.createStatement();
			ResultSet rs1 = statement.executeQuery(sql);
			String MODALITIES = "";
			String MOD = null;
			while (rs1.next()) {
				MOD = rs1.getString(1);
				if (MODALITIES.length() > 0) {
					MODALITIES = MODALITIES + "\\";
				}
				MODALITIES = MODALITIES + MOD;
			}
			sql = "update STUDY set MODS_IN_STUDY='" + MODALITIES
					+ "' where PK=" + fk;
			if (debug)
				System.out.println(sql);
			else
				statement.executeUpdate(sql);
			statement.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void update() {
		String where = makeWhere(upd_level);
		if (where != null && where != "") {
			try {
				Connection connection = DriverManager.getConnection(jdbcUrl,
						db_un, db_pw);
				Statement statement = connection.createStatement();

				String sql = null;
				String attr = null;
				String fk_field = null;

				if (upd != null && upd != "") {
					if (upd_level.equalsIgnoreCase("PATIENT")) {
						attr = "PAT_ATTRS";
						fk_field = "";
					}
					if (upd_level.equalsIgnoreCase("STUDY")) {
						attr = "STUDY_ATTRS";
						fk_field = "PATIENT_FK FK,";
					}
					if (upd_level.equalsIgnoreCase("SERIES")) {
						attr = "SERIES_ATTRS";
						fk_field = "STUDY_FK FK,";
					}

					String from = " patient,study,series ";
					where = where + " and (PATIENT.PK=PATIENT_FK and STUDY.PK=STUDY_FK)";
					// sql = "select count(*) from " + upd_level + " where " + where;
					sql = "select count(*) from " + from + " where " + where;
					if (debug)
						System.out.println(sql);
					ResultSet rs = statement.executeQuery(sql);
					rs.next();
					long last_fk = -1;
					long rowsRead = 0;
					long rows = rs.getLong(1);
					if (debug)
						System.out.println("Res: "+rows);
					if (rows > 0 && rows == upd_cnt && ( upd_multi || rows == 1)) {
						sql = "select "+upd_level+".PK," + fk_field + attr + " from " + from 
								+ " where " + where;
						if (debug)
							System.out.println(sql);
						rs = statement.executeQuery(sql);
						while (rs.next()) {
							rowsRead++;
							long pk = rs.getLong("PK");
							Dataset ds = updateDataSet(rs, attr, upd_dicom);
							if (ds != null) {
								sql = "update " + upd_level + " set " + attr
										+ "=?," + upd + " where PK=" + pk;
								if (debug)
									System.out.println(sql);
								else {
									PreparedStatement pstmt = connection
											.prepareStatement(sql);
									int len = ds
											.calcLength(DcmEncodeParam.EVR_LE);
									ByteArrayOutputStream bos = new ByteArrayOutputStream(
											len);
									ds.writeDataset(bos, DcmEncodeParam.EVR_LE);
									pstmt.setBinaryStream(1,
											new ByteArrayInputStream(bos
													.toByteArray()), len);
									pstmt.execute();
								}

								if (upd_mod) {
									long fk = rs.getLong("FK");
									if (last_fk == -1)
										last_fk=fk;
									if (fk != last_fk) {
										update_modality(connection, last_fk);
									} else if (rowsRead == rows ) {
										update_modality(connection, fk);
									}
									last_fk = fk;
								}
							}
						}
					} else {
						if (upd_multi)
                            System.err.println("Update Count Mismatch. [" + rows + "]");
						else
                            System.err.println("Multiple Updates not allowed on this Field.");
						System.exit(1);
					}
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			System.err.println("Where clause empty.");
			System.exit(1);
		}
	}

	public void display() {
		String where = makeWhere(null);
		if (where != null && where != "") {
			try {
				Connection connection = DriverManager.getConnection(jdbcUrl,
						db_un, db_pw);
				Statement statement = connection.createStatement();

				String sql = null;
				String attr = null;

				// Update DataSet

				sql = "select PATIENT.PK A,STUDY.PK B, SERIES.PK C,"
						+ "PAT_NAME,PAT_SEX,PAT_BIRTHDATE BD, "
						+ "PAT_ID, PAT_ID_ISSUER, "
						+ "STUDY_IUID,STUDY_DATETIME SD,MODS_IN_STUDY,num_series, study.num_instances STYNI, study.availability STYA, "
						+ "series_iuid,modality,series.num_instances SERNI,series.availability SERA,series_status ";

				if (patds) {
					sql = sql + ",PAT_ATTRS";
				}
				if (styds) {
					sql = sql + ",STUDY_ATTRS";
				}
				if (serds) {
					sql = sql + ",SERIES_ATTRS";
				}

				if (displaypath || displaysopid) {
					sql = sql
							+ ",sop_iuid,inst_no Z,instance.availability INSTA,inst_status,";
					sql = sql
							+ "DIRPATH,FILEPATH,FILE_SIZE,filesystem.availability FSA";
				}

				sql = sql + " from patient,study,series ";
				if (displaypath || displaysopid) {
					sql = sql + ",instance,files,filesystem ";
				}
				if (where != null && where != "")
					where = where.concat(" and ");
				sql = sql + " where " + where
						+ "(PATIENT.PK=PATIENT_FK and STUDY.PK=STUDY_FK";
				if (displaypath || displaysopid) {
					sql = sql
							+ " and SERIES.PK=SERIES_FK and INSTANCE.PK=INSTANCE_FK and FILESYSTEM.PK=FILESYSTEM_FK";
				}
				sql = sql + ") order by PAT_NAME,A,B,C";

				if (debug)
					System.err.println(sql);

				ResultSet rs = statement.executeQuery(sql);

				while (rs.next()) {
					displayPatient(rs);
					displayStudy(rs);
					displaySeries(rs);

					if (displaysopid)
						displaySOP(rs);

					if (displaypath)
						displayPath(rs);
				}

				System.out.print(newline);
				connection.close();
				System.out.flush();
				System.out.close();

				connection.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			System.err.println("No filter criteria given...");
			System.err
					.println("Use at least % if you know what you are doing,");
			System.err.println("or use --help for help.");
			System.exit(1);
		}
	}

	private static void displayHelp() {
		System.out
				.println("Usage: java -jar jpdbi [options] [<Lastname> [<Firstname> [<Birthdate>]]]");
		System.out.println("       java -jar jpdbi -v");
		System.out.print(newline);
		System.out
				.println("        -v                display version information");
		System.out.print(newline);
		System.out
				.println("        -h db             DB host to connect to (localhost)");
		System.out
				.println("        -c config         Use specified config properties");
		System.out
				.println("        -u username       username to connect to DB");
		System.out
				.println("        -p password       password to connect to DB");
		System.out.println("        --sid sid         DB SID");
		System.out.println("        --port port       DB port");

		System.out.print(newline);
		System.out
				.println("        -s StudyUID             Search for Dicom Study UID");
		System.out
				.println("        -S SeriesUID            Search for Dicom Series UID");
		System.out
				.println("        -i UID                  Search for Dicom Study or Series UID");
		System.out
				.println("        -m Modality             Search for Modality");
		System.out
				.println("        --patid [ISSUER:]PATID  Search for (ISSUER and) PATID");

		System.out.print(newline);
		System.out
				.println("        -d YYYYMMDD             Search for Study Date");
		System.out
				.println("        -d <0-999>              Search last N days");

		System.out.print(newline);
		System.out.println("        --sop             Display SOP UID");
		System.out.println("        --path            Display Path");
		System.out.println("        --dataset         Display all DCM DataSet");
		System.out
				.println("        --patds               Display DataSet on Patient Level");
		System.out
				.println("        --styds               Display DataSet on Study Level");
		System.out
				.println("        --serds               Display DataSet on Series Level");
		/*
		 * System.out.print(newline); System.out.println(" --force Display even
		 * if result would return more that 500 entries");
		 */

		System.out.print(newline);
		System.out.println("        --debug           Display SQL Statement");

		System.out.print(newline);
		System.out.println("    Use % as wildcard statement");

		System.out.print(newline);
	}

	private void displayPatient(ResultSet result) throws SQLException {
		long tmpKey = result.getLong("A");

		if (lastPat != tmpKey) {
			lastPat = tmpKey;
			System.out.print(result.getString("PAT_NAME").replace('^', ' ')
					.trim()
					+ " ");
			System.out.print(result.getString("PAT_SEX") + " ");
			System.out.print(bdf.format(result.getTimestamp("BD")) + " ");
			System.out.print("<");
			System.out.print(result.getString("PAT_ID_ISSUER"));
			System.out.print(":");
			System.out.print(result.getString("PAT_ID"));
			System.out.print("> ");
			System.out.print("[" + tmpKey + "]" + newline);
			if (patds)
				try {
					displayDataSet(result, "PAT_ATTRS");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

		}
	}

	private void displayStudy(ResultSet result) throws SQLException {
		long tmpKey = result.getLong("B");

		if (lastSty != tmpKey) {
			lastSty = tmpKey;
			System.out.print(" ");
			System.out.print(result.getString("STUDY_IUID") + " ");
			System.out.print(sdf.format(result.getTimestamp("SD")) + " ");
			System.out.print("("
					+ result.getString("MODS_IN_STUDY").replace('\\', ',')
					+ ") ");
			System.out.print(result.getLong("NUM_SERIES") + " ");
			System.out.print(result.getLong("STYNI") + " ");
			System.out.print(result.getLong("STYA") + newline);

			if (styds)
				try {
					displayDataSet(result, "STUDY_ATTRS");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

		}
	}

	private static String whereClause(String where, String field, String value,
			boolean delimiter) {
		String statement = "";

		if (value != null) {
			if (where != null && where != "")
				statement = " and ";

			statement = statement.concat(" " + field + " ");
			if (value.indexOf('%') != -1)
				statement = statement.concat(" like ");
			else
				statement = statement.concat(" = ");

			if (delimiter)
				statement = statement.concat("'" + value + "'");
			else
				statement = statement.concat(value);

			if (debug)
				System.err.println(statement);
		}

		return statement;
	}

	private void displaySeries(ResultSet result) throws SQLException {
		long tmpKey = result.getLong("C");

		if (lastSeries != tmpKey) {
			lastSeries = tmpKey;

			System.out.print("  ");
			System.out.print(result.getString("SERIES_IUID") + " ");
			// System.out.print(result.getString("SD") + " ");
			System.out.print(result.getString("MODALITY") + " ");
			System.out.print(result.getLong("SERNI") + " ");
			System.out.print(result.getLong("SERA") + " ");
			System.out.print(result.getLong("SERIES_STATUS") + newline);
			if (serds)
				try {
					displayDataSet(result, "SERIES_ATTRS");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
	}

	private static void displaySOP(ResultSet result) throws SQLException {
		System.out.print("   ");
		System.out.print(result.getString("SOP_IUID") + " ");
		System.out.print(result.getString("INSTA") + " ");
		System.out.print(result.getString("INST_STATUS") + newline);
	}

	private static void displayPath(ResultSet result) throws SQLException {
		System.out.print("   ");
		System.out.print(result.getString("DIRPATH") + "/");
		System.out.print(result.getString("FILEPATH") + " ");
		System.out.print(result.getString("FILE_SIZE") + " ");
		System.out.print(result.getString("FSA") + newline);
	}

	/*
	 * private static void displayDataSet(ResultSet result, String ATTR) throws
	 * SQLException, IOException {
	 * 
	 * Blob blob=result.getBlob(ATTR);
	 * 
	 * if (blob != null) { InputStream bis = blob.getBinaryStream(); Dataset ds =
	 * DcmObjectFactory.getInstance().newDataset();
	 * 
	 * try { ds.readFile(bis, null, -1);
	 * 
	 * ds.setFileMetaInfo(null); } catch (IOException e) { throw new
	 * IllegalArgumentException("" + e); } System.out.println("<"+ATTR+">");
	 * ds.dumpDataset(System.out, null); System.out.println("</"+ATTR+">"); } }
	 */

	private TransformerHandler getTransformerHandler()
			throws TransformerConfigurationException, IOException {
		SAXTransformerFactory tf = (SAXTransformerFactory) TransformerFactory
				.newInstance();
		TransformerHandler th = null;
		if (xslt != null) {
			if (xsltInc) {
				tf.setAttribute(
						"http://xml.apache.org/xalan/features/incremental",
						Boolean.TRUE);
			}
			th = tf.newTransformerHandler(new StreamSource(xslt.openStream(),
					xslt.toExternalForm()));
			Transformer t = th.getTransformer();
			for (Iterator it = xsltParams.iterator(); it.hasNext();) {
				String s = (String) it.next();
				int eqPos = s.indexOf('=');
				t.setParameter(s.substring(0, eqPos), s.substring(eqPos + 1));
			}
		} else {
			th = tf.newTransformerHandler();
			th.getTransformer().setOutputProperty(OutputKeys.INDENT, "yes");
		}
		th.setResult(new StreamResult(out));
		return th;
	}

	private Dataset updateDataSet(ResultSet result, String ATTR, String[] UPD)
			throws SQLException, IOException {

		String[] Line = new String[2];

		Blob blob = result.getBlob(ATTR);

		if (blob != null) {
			InputStream bis = blob.getBinaryStream();
			Dataset ds = DcmObjectFactory.getInstance().newDataset();
			ds.readFile(bis, null, -1);
			bis.close();

			if (debug) {
				System.out.println("<" + ATTR + " OLD>");
				ds.dumpDataset(System.out, null);
				System.out.println("</" + ATTR + "OLD>");
			}

			for (int loop = 0; loop < UPD.length; loop++) {
				Line = UPD[loop].split("=");
				ds.putXX(Tags.forName(Line[0]), Line[1]);
			}

			if (debug) {
				System.out.println("<" + ATTR + " NEW>");
				ds.dumpDataset(System.out, null);
				System.out.println("</" + ATTR + " NEW>");
			}
			return ds;
		}
		return null;
	}

	/*
	 * private static void displayDataSet(ResultSet result, String ATTR) throws
	 * SQLException, IOException {
	 * 
	 * Blob blob=result.getBlob(ATTR);
	 * 
	 * if (blob != null) { InputStream bis = blob.getBinaryStream(); Dataset ds =
	 * DcmObjectFactory.getInstance().newDataset(); try { ds.readFile(bis, null,
	 * -1);
	 * 
	 * ds.setFileMetaInfo(null); } catch (IOException e) { throw new
	 * IllegalArgumentException("" + e); } System.out.println("<"+ATTR+">");
	 * ds.dumpDataset(System.out, null); System.out.println("</"+ATTR+">"); } }
	 */

	private void displayDataSet(ResultSet result, String ATTR)
			throws SQLException, IOException {

		Blob blob = result.getBlob(ATTR);

		if (blob != null) {
			InputStream bis = blob.getBinaryStream();

			DcmParser parser = pfact.newDcmParser(bis);
			try {
				parser.setSAXHandler2(getTransformerHandler(), dict,
						excludeTags, excludeValueLengthLimit, baseDir);
			} catch (TransformerConfigurationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// parser.setDcmHandler(ds.getDcmHandler());
			parser.parseDcmFile(parser.detectFileFormat(), -1);

			/*
			 * System.out.println("<"+ATTR+">"); ds.dumpDataset(System.out,
			 * null); System.out.println("</"+ATTR+">");
			 */
		}
	}

	public static void main(String[] argv) {
		Properties defaultProps = new Properties();

		String ID = "$Id: Jpdbi.java,v 1.18.2.12 2008-06-20 23:06:21 kianusch Exp $";
		String REVISION = "$Revision: 1.18.2.12 $";

		try {
			defaultProps
					.load(ClassLoader
							.getSystemResourceAsStream("com/agfa/db/tools/jpdbi.properties"));
		} catch (Exception e) {
			System.err.println("I/O failed.");
		}

		Properties applicationProps = new Properties(defaultProps);

		File tmp = new File("/etc/jpdbi.properties");
		if (tmp.exists() && tmp.isFile() && tmp.canRead()) {
			try {
				applicationProps.load(new FileInputStream(tmp));
			} catch (Exception e) {
				System.err.println("Can't find " + tmp);
			}
		}
		// get system newline
		if (newline == null || newline.length() <= 0)
			newline = "\n";

		// Parse Command Line
		boolean help = false;
		boolean version = false;
		String configFile = null;
		String outFile = null;
		String db_host = null;
		String db_sid = null;
		String updates = null;

		LongOpt[] longopts = new LongOpt[24];
		// 
		longopts[0] = new LongOpt("help", LongOpt.NO_ARGUMENT, null, OPT_HELP);
		longopts[1] = new LongOpt("debug", LongOpt.NO_ARGUMENT, null, OPT_DEBUG);
		longopts[2] = new LongOpt("sid", LongOpt.REQUIRED_ARGUMENT, null,
				OPT_SID);
		longopts[3] = new LongOpt("port", LongOpt.REQUIRED_ARGUMENT, null, 'P');
		longopts[4] = new LongOpt("username", LongOpt.REQUIRED_ARGUMENT, null,
				'u');
		longopts[5] = new LongOpt("password", LongOpt.REQUIRED_ARGUMENT, null,
				'p');
		longopts[6] = new LongOpt("cfg", LongOpt.REQUIRED_ARGUMENT, null, 'c');
		longopts[7] = new LongOpt("host", LongOpt.REQUIRED_ARGUMENT, null, 'a');
		longopts[8] = new LongOpt("version", LongOpt.NO_ARGUMENT, null, 'v');
		longopts[9] = new LongOpt("outfile", LongOpt.REQUIRED_ARGUMENT, null,
				'O');

		longopts[10] = new LongOpt("series", LongOpt.REQUIRED_ARGUMENT, null,
				'S');
		longopts[11] = new LongOpt("study", LongOpt.REQUIRED_ARGUMENT, null,
				's');
		longopts[12] = new LongOpt("patid", LongOpt.REQUIRED_ARGUMENT, null,
				OPT_PATID);
		longopts[13] = new LongOpt("any", LongOpt.REQUIRED_ARGUMENT, null, 'i');
		longopts[14] = new LongOpt("modality", LongOpt.REQUIRED_ARGUMENT, null,
				'm');
		longopts[15] = new LongOpt("stydate", LongOpt.REQUIRED_ARGUMENT, null,
				'd');

		longopts[16] = new LongOpt("path", LongOpt.NO_ARGUMENT, null, OPT_PATH);
		longopts[17] = new LongOpt("sop", LongOpt.NO_ARGUMENT, null, OPT_SOP);

		longopts[18] = new LongOpt("dataset", LongOpt.NO_ARGUMENT, null,
				OPT_DATASET);
		longopts[19] = new LongOpt("patds", LongOpt.NO_ARGUMENT, null,
				OPT_PATDS);
		longopts[20] = new LongOpt("serds", LongOpt.NO_ARGUMENT, null,
				OPT_SERDS);
		longopts[21] = new LongOpt("styds", LongOpt.NO_ARGUMENT, null,
				OPT_STYDS);

		longopts[22] = new LongOpt("update", LongOpt.REQUIRED_ARGUMENT, null,
				OPT_UPDATE);
		longopts[23] = new LongOpt("count", LongOpt.REQUIRED_ARGUMENT, null,
				OPT_UPDATE_CNT);

		int MODE = MODE_DISPLAY;

		Getopt g = new Getopt(Jpdbi.class.getName(), argv,
				":h:a:P:u:p:c:o:O:S:s:i:m:d:v;", longopts);
		g.setOpterr(true);

		int c;
		while ((c = g.getopt()) != -1)
			switch (c) {
			case OPT_DEBUG:
				debug = true;
				break;
			//
			case OPT_HELP:
				help = true;
				break;
			// host
			case 'a':
			case 'h':
				db_host = g.getOptarg();
				break;
			// port
			case 'P':
				db_port = g.getOptarg();
				break;
			// username
			case 'u':
				db_un = g.getOptarg();
				break;
			// password
			case 'p':
				db_pw = g.getOptarg();
				break;
			// sid
			case OPT_SID:
				db_sid = g.getOptarg();
				break;
			// cfg
			case 'c':
				configFile = g.getOptarg();
				break;
			// version
			case 'v':
				version = true;
				break;
			// OutFile
			case 'O':
			case 'o':
				outFile = g.getOptarg();
				break;

			// series
			case 'S':
				series = g.getOptarg();
				break;
			// study
			case 's':
				study = g.getOptarg();
				break;
			// any (study or series)
			case 'i':
				anyid = g.getOptarg();
				break;
			// modality
			case 'm':
				modality = g.getOptarg();
				break;
			// stydate
			case 'd':
				stydate = g.getOptarg();
				break;
			// PatID
			case OPT_PATID:
				patid = g.getOptarg();
				break;
			//
			case OPT_SOP:
				displaysopid = true;
				break;
			//
			case OPT_PATH:
				displaypath = true;
				break;
			//
			case OPT_DATASET:
				patds = true;
				styds = true;
				serds = true;
				break;
			case OPT_PATDS:
				patds = true;
				break;
			case OPT_STYDS:
				styds = true;
				break;
			case OPT_SERDS:
				serds = true;
				break;
			//
			case OPT_UPDATE:
				updates = g.getOptarg();
				MODE = MODE_UPDATE;
				break;
			case OPT_UPDATE_CNT:
				upd_cnt = Long.parseLong(g.getOptarg());
				break;
			default:
				System.err
						.println("Error encountered during Argument Parsing.");
				System.err.println("Use -h for help.");
				System.exit(1);
				break;
			}

		int i = g.getOptind();

		if (i < argv.length)
			lastname = argv[i++].trim();
		if (i < argv.length)
			firstname = argv[i++].trim();
		if (i < argv.length)
			birthdate = argv[i++].trim();

		if (version) {
			System.out.println(REVISION);
			System.out.println(ID);
			return;
		}

		if (help) {
			displayHelp();
			return;
		}

		if (configFile != null) {
			try {
				applicationProps.load(new FileInputStream(configFile));
			} catch (Exception e) {
				System.out
						.println("Couldn't load config file! using default config file");
			}
		}

		if (updates != null) {
			String old_level = null;
			String upd_dcm = null;
			String upd_db = null;
			String upd_field = null;
            String upd_value = null;
            String upd_m = null;
			String Updates[] = updates.split(",");
			upd = "";
			upd_dicom = new String[Updates.length];

			for (int loop = 0; loop < Updates.length; loop++) {
				int split = Updates[loop].indexOf('=');
				if (split != -1) {
					upd_field = Updates[loop].substring(0, split).toLowerCase();
					upd_value = Updates[loop].substring(split + 1);

					upd_level = applicationProps.getProperty(
							"update." + upd_field + ".level", "").trim();
					if (old_level == null) {
						old_level = upd_level;
					}
					if (upd_level.length() > 0) {
						if (old_level.equalsIgnoreCase(upd_level)) {
							upd_dcm = applicationProps.getProperty(
									"update." + upd_field + ".dcm", "").trim();
                            upd_db = applicationProps.getProperty(
                                    "update." + upd_field + ".dbfield", "")
                                    .trim();
                            upd_m = applicationProps.getProperty(
                                    "update." + upd_field + ".multi", "")
                                    .trim();
                            
                            if ( upd_m.equalsIgnoreCase("yes") || upd_m.equalsIgnoreCase("true")) {
                                upd_multi = upd_multi && true;
                            } else {
                                upd_multi = false;
                            }

							if (upd_db.equalsIgnoreCase("PAT_NAME")
									&& upd_level.equalsIgnoreCase("PATIENT")) {
								int cnt = 0;
								for (int pos = 0; pos < upd_value.length(); pos++)
									if (upd_value.charAt(pos) == '^')
										cnt++;
								for (; cnt < 4; cnt++)
									upd_value = upd_value + "^";
							}

							if (upd.length() > 0) {
								upd = upd + ",";
							}

							if (upd_dcm.length() > 0) {
								upd_dicom[loop] = upd_dcm + "=" + upd_value;
							} else {
								upd_dicom[loop] = null;
							}

							if (upd_db.equalsIgnoreCase("MODALITY")
									&& upd_level.equalsIgnoreCase("SERIES")) {
								upd_mod = true;
							}

							upd = upd + upd_db + "='" + upd_value + "'";
						} else {
							System.err
									.println("Multilevel updates not supported.");
							System.exit(1);
						}
					} else {
						System.err.println("Update not defined.");
						System.exit(1);
					}
				} else {
					System.err.println("Update syntax error.");
					System.exit(1);
				}
			}
		}

		String jdbcDriverClass = applicationProps.getProperty("jdbc.driver",
				"oracle.jdbc.driver.OracleDriver");

		try {
			Class.forName(jdbcDriverClass);
		} catch (ClassNotFoundException e) {
			System.err.println(e);
			System.exit(-1);
		}
		jdbcUrl = applicationProps.getProperty("jdbc.url");

		if (db_host == null)
			db_host = applicationProps.getProperty("jdbc.host");
		if (db_un == null)
			db_un = applicationProps.getProperty("jdbc.username");
		if (db_port == null)
			db_port = applicationProps.getProperty("jdbc.port");
		if (db_pw == null)
			db_pw = applicationProps.getProperty("jdbc.password");
		if (db_sid == null)
			db_sid = applicationProps.getProperty("jdbc.sid");

		if (db_host != null)
			jdbcUrl = jdbcUrl.replaceAll("\\$HOST\\$", db_host);

		if (db_sid != null)
			jdbcUrl = jdbcUrl.replaceAll("\\$SID\\$", db_sid);

		if (db_sid != null)
			jdbcUrl = jdbcUrl.replaceAll("\\$PORT\\$", db_port);

		// applicationProps.list(System.out);

		if (outFile != null) {
			try {
				System.setOut(new PrintStream(new BufferedOutputStream(
						new FileOutputStream(outFile))));
			} catch (Exception e) {
				System.err.println(e);
			}
		}

		Jpdbi instance = new Jpdbi(MODE);
	}
}