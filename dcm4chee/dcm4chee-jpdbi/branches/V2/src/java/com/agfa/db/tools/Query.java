// $Id$

package com.agfa.db.tools;

import java.sql.Types;
import java.util.HashMap;
import java.util.Hashtable;

public class Query {

	private static int Count(char c, String s) {
		int cnt = 0;
		for (int pos = 0; pos < s.length(); pos++)
			if (s.charAt(pos) == c)
				cnt++;
		return cnt;
	}

	private static String addWhere(String w, String f, String v, int t) {
		return addWhere(w, f, v, t, false);
	}

	private static String addWhere(String w, String f, String v, int t, boolean u) {
		if (w.length() > 0)
			w = w.concat(" and ");

		if (u)
			w = w.concat("upper(" + f + ")");
		else
			w = w.concat(f);

		switch (t) {
		case Types.VARCHAR:
			if (v.indexOf('%') > -1 || v.indexOf('_') > -1)
				w = w.concat(" like ");
			else
				w = w.concat("=");

			if (u)
				w = w.concat("upper('" + v + "')");
			else
				w = w.concat("'" + v + "'");
			break;
		case Types.DATE:
			w = w.concat("={d '" + v + "'}");
			break;
		}

		return w;
	}

	private static String PrependSql(String s, String c) {
		if (c.length() > 0)
			c = "," + c;
		c = s + c;
		return c;
	}

	static String[][] BuildUpdate(CommandLine cfg) {
		String[][] UpdDicom = null;
		if (!cfg.update.isEmpty()) {
			Hashtable<String, Boolean> ht=new Hashtable<String, Boolean>();
			String TmpLevel = null;
			String TmpUpdDicom = null;
			String UpdDB = null;
			boolean UpdMulti = true;

			String UpdMultiTmp = null;

			int CntUpdates = cfg.update.size();

			if (CntUpdates > 0) {
				UpdDicom = new String[CntUpdates][5];

				for (int loop = 0; loop < CntUpdates; loop++) {
					String Update = cfg.update.get(loop);

					int split = Update.indexOf('=');
					if (split != -1) {
						String UpdField = Update.substring(0, split).toLowerCase();
						if ( ! ht.containsKey(UpdField)) { 

							String UpdValue = Update.substring(split + 1);
							String UpdPrefix = "update." + UpdField + ".";
							String UpdLevel = cfg.applicationProps.getProperty(UpdPrefix.concat("level"), "").trim();
							if (UpdLevel.length() > 0) {
								if (TmpLevel == null)
									TmpLevel = UpdLevel;
								if (TmpLevel.equalsIgnoreCase(UpdLevel)) {
									TmpUpdDicom = cfg.applicationProps.getProperty(UpdPrefix.concat("dcm"), "").trim();
									UpdDB = cfg.applicationProps.getProperty(UpdPrefix.concat("dbfield"), "").trim();
									UpdMultiTmp = cfg.applicationProps.getProperty(UpdPrefix.concat("multi"), "").trim();
									if (UpdMultiTmp.equalsIgnoreCase("true") || UpdMultiTmp.equalsIgnoreCase("yes"))
										UpdMulti &= true;
									else
										UpdMulti = false;
									// Special Cases
									// Patient Name
									if (UpdDB.equalsIgnoreCase("PAT_NAME") && UpdLevel.equalsIgnoreCase("PATIENT")) {
										int cnt = Count('^', UpdValue);
										while (cnt++ < 4)
											UpdValue += "^";
									}
								} else {
									_System.exit(1, "Multilevel updates not supported ["+TmpLevel+" & "+UpdLevel+"].");
								}

								if (UpdDB.equalsIgnoreCase("NONE")) {
									UpdDB = null;
								}
								if (UpdValue.equalsIgnoreCase("_REMOVE_")) {
									UpdValue = null;
								}

								if (TmpUpdDicom.length() > 0) {
									ht.put(UpdField, true);
									UpdDicom[loop][0] = UpdDB;
									UpdDicom[loop][1] = TmpUpdDicom;
									UpdDicom[loop][2] = UpdValue;
								}
							} else {
								_System.exit(1, "Update not defined ["+UpdField+"].");
							}
						} else {
							_System.exit(1, "Duplicate Update ["+UpdField+"].");
						}
					} else {
						_System.exit(1, "Update syntax error ["+Update+"].");
					}
				}
				UpdDicom[0][3] = TmpLevel;
				UpdDicom[0][4] = (UpdMulti ? "t" : "f");
				if (TmpLevel.equals("PATIENT"))
					cfg.levels.set(Jpdbi.PATIENT);
				if (TmpLevel.equals("STUDY"))
					cfg.levels.set(Jpdbi.STUDY);
				if (TmpLevel.equals("SERIES"))
					cfg.levels.set(Jpdbi.SERIE);
			}
		}
		return UpdDicom;
	}

	static String[] Build(CommandLine cfg) {
		String select = "";
		String where = "";
		String order = "";
		String from = "";
		String links = "";
		String group = "";

		String FirstName = cfg.FirstName;
		String LastName = cfg.LastName;
		String BirthDate = cfg.BirthDate;
		String Issuer = cfg.Issuer;
		String PatID = cfg.PatID;
		String StudyIUID = cfg.StudyIUID;
		String SeriesIUID = cfg.SeriesIUID;
		String AddQuery = cfg.AddQuery;

		boolean PatientLink = false;
		boolean StudyLink = false;
		boolean SeriesLink = false;

		cfg.levels.set(Jpdbi.PATIENT);

		// QUERY
		if (AddQuery != null && AddQuery.length() > 0) {
			int split = AddQuery.indexOf(".");
			if (split > -1) {
				String field = AddQuery.substring(0, split);
				String value = AddQuery.substring(split + 1);

				if (field.equalsIgnoreCase("PATIENT")) {
					PatientLink = true;
				} else if (field.equalsIgnoreCase("STUDY")) {
					StudyLink = true;
				} else if (field.equalsIgnoreCase("SERIES")) {
					SeriesLink = true;
				}
				if (where.length() > 0)
					where = where.concat(" and " + AddQuery);
				else
					where = AddQuery;
			}
		}

		// Patient
		{
			String myWhere = "";

			if (LastName != null || FirstName != null) {
				if (LastName == null)
					LastName = "%";
				if (FirstName == null)
					FirstName = "%";

				myWhere = addWhere(myWhere, "pat_name", LastName + "^" + FirstName + "^%^%", Types.VARCHAR, true);
			}

			if (BirthDate != null) {
				if (cfg.pre214)
					myWhere = addWhere(myWhere, "PAT_BIRTHDATE", BirthDate, Types.DATE);
				else
					myWhere = addWhere(myWhere, "PAT_BIRTHDATE", BirthDate, Types.VARCHAR);
			}

			if (Issuer != null) {
				myWhere = addWhere(myWhere, "PAT_ID_ISSUER", Issuer, Types.VARCHAR);
			}

			if (PatID != null) {
				int split = PatID.indexOf(':');
				if (split == -1) {
					Issuer = null;
				} else {
					Issuer = PatID.substring(0, split);
					PatID = PatID.substring(split + 1);
				}

				if (PatID.length() > 0)
					myWhere = addWhere(myWhere, "PAT_ID", PatID, Types.VARCHAR);

				myWhere = addWhere(myWhere, "PAT_ID_ISSUER", Issuer, Types.VARCHAR);
			}

			if (myWhere.length() > 0) {
				PatientLink = true;

				if (where.length() > 0)
					where = where.concat(" and " + myWhere);
				else
					where = myWhere;
			}
		}

		// Study
		{
			String myWhere = "";
			if (StudyIUID != null) {
				myWhere = addWhere(myWhere, "STUDY.STUDY_IUID", StudyIUID, Types.VARCHAR);
			}
			if (myWhere.length() > 0) {
				StudyLink = true;

				if (where.length() > 0)
					where = where.concat(" and " + myWhere);
				else
					where = myWhere;
			}
		}

		// Series
		{
			String myWhere = "";
			if (SeriesIUID != null) {
				myWhere = addWhere(myWhere, "SERIES.SERIES_IUID", SeriesIUID, Types.VARCHAR);
			}
			if (myWhere.length() > 0) {
				SeriesLink = true;

				if (where.length() > 0)
					where = where.concat(" and " + myWhere);
				else
					where = myWhere;
			}
		}

		if (cfg.levels.get(Jpdbi.PATH)) {
			from = from.concat("FILESYSTEM,FILES,");
			links = links.concat("INSTANCE.PK=INSTANCE_FK and FILESYSTEM.PK=FILES.FILESYSTEM_FK and ");
			cfg.levels.set(Jpdbi.INSTANCE);
			if (cfg.displayFSInfo)
				select = PrependSql("FILESYSTEM.PK F, FS_GROUP_ID FSGRP", select);
			select = PrependSql("FILES.PK E, DIRPATH, FILEPATH, FILE_SIZE FILESIZE, filesystem.availability FSAVAIL",
					select);
			order = PrependSql("E", order);
		}

		if (cfg.levels.get(Jpdbi.INSTANCE)) {
			from = from.concat("INSTANCE,");
			links = links.concat("SERIES.PK=SERIES_FK and ");
			cfg.levels.set(Jpdbi.SERIE);
			if (cfg.displayAET)
				select = PrependSql("INSTANCE.EXT_RETR_AET INSTEXTRETAET, INSTANCE.RETRIEVE_AETS INSTRETAET", select);

			select = PrependSql(
					"INSTANCE.PK D, SOP_IUID SOPIUID, inst_no INSTNUM, instance.availability INSTAVAIL, inst_status INSTSTATUS",
					select);
			order = PrependSql("D", order);
		}

		if (SeriesLink || cfg.levels.get(Jpdbi.SERIE)) {
			StudyLink = true;
			from = from.concat("SERIES,");
			links = links.concat("STUDY.PK=STUDY_FK and ");
			if (cfg.levels.get(Jpdbi.SERIE)) {
				cfg.levels.set(Jpdbi.STUDY);
				if (cfg.displayAET)
					select = PrependSql(
							"SERIES.EXT_RETR_AET SEREXTRETAET, SERIES.RETRIEVE_AETS SERRETAET, SERIES.SRC_AET SERSRCAET",
							select);

				select = PrependSql(
						"SERIES.PK C, SERIES_IUID SERIUID, SERIES.MODALITY SERMOD, SERIES.NUM_INSTANCES SERNUMINST, SERIES.AVAILABILITY SERAVAIL, series_status SERSTATUS",
						select);
				order = PrependSql("C", order);
			}
		}

		if (StudyLink || cfg.levels.get(Jpdbi.STUDY)) {
			PatientLink = true;
			from = from.concat("STUDY,");
			links = links.concat("PATIENT.PK=STUDY.PATIENT_FK");
			if (cfg.levels.get(Jpdbi.STUDY)) {
				cfg.levels.set(Jpdbi.PATIENT);
				if (cfg.displayAET)
					select = PrependSql("STUDY.EXT_RETR_AET STYEXTRETAET, STUDY.RETRIEVE_AETS STYRETAET", select);
				select = PrependSql(
						"STUDY.PK B, STUDY.STUDY_IUID STYIUID, STUDY_DATETIME STYD, MODS_IN_STUDY STYMODS, num_series STYNUMSER, study.num_instances STYNUMINST, study.availability STYAVAIL",
						select);

				order = PrependSql("B", order);
			}
		}

		if (PatientLink || cfg.levels.get(Jpdbi.PATIENT)) {
			from = from.concat("PATIENT");
			if (cfg.levels.get(Jpdbi.PATIENT)) {
				select = PrependSql("PATIENT.PK A, PAT_NAME, PAT_SEX, PAT_BIRTHDATE BD, PAT_ID, PAT_ID_ISSUER ", select);
				order = PrependSql("PAT_NAME,A", order);
			}
		}

		if (links.length() > 0 && where.length() > 0) {
			links = links.concat(" and ");
		}

		if (where.length() > 0)
			return new String[] { select,from,links,where,group,order };

		System.err.println("No filter criteria given...");
		System.err.println("Use at least % if you know what you are doing,");
		System.err.println("or use --help for help.");
		System.exit(1);

		return null;
	}
}