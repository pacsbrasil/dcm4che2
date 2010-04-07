// $Id$

package com.agfa.db.tools;

import java.sql.*;
import java.io.*;

public class Jpdbi {
    public final static String VERSION = "2.0";
    
    public final static String ID = "$Id$";

    public final static String REVISION = "$Revision$";

	final static int PATIENT = 1;
	final static int STUDY = 2;
	final static int SERIE = 3;
	final static int INSTANCE = 4;
	final static int PATH = 5;

	private static void ParseQuery(Connection conn, String query, CommandLine cfg) throws SQLException, IOException {
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery(query);
		ResultSetMetaData md = rs.getMetaData();

		while (rs.next()) {
			Display.Patient(rs, md, cfg);

			if (cfg.levels.get(STUDY))
				Display.Study(rs, md, cfg);
				if (cfg.levels.get(SERIE))
					Display.Serie(rs, md, cfg);
					if (cfg.levels.get(INSTANCE))
						Display.Instance(rs, md, cfg);
						if (cfg.levels.get(PATH)) 
							Display.Path(rs, md, cfg);
		}

		rs.close();
		stmt.close();
	}

	public static void main(String[] argv) {
		Connection conn = null;
		String query = null;

		CommandLine cfg = new CommandLine(argv);

		if (cfg.debug) {
			System.err.println("DEBUG: Connect Url: < " + cfg.jdbcUrl + " >");
		}

		try {
			conn = DriverManager.getConnection(cfg.jdbcUrl);
			cfg.setDatabase(conn);
			// setPreparedStatements(conn);

			query = Query.Build(cfg);

			if (cfg.debug) {
				System.err.println("DEBUG: Query: < " + query + " >");
			}

			ParseQuery(conn, query, cfg);

			conn.close();
		} catch (Exception e) {
			if (cfg.debug)
				e.printStackTrace();
			else
				_System.exit(1, e.toString());
		}
		_System.exit(0);
	}
}