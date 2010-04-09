// $Id$

package com.agfa.db.tools;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

public class Jpdbi {
    public final static String VERSION = "2.0";

    public final static String ID = "$Id$";

    public final static String REVISION = "$Revision$";

    final static int PATIENT = 1;

    final static int STUDY = 2;

    final static int SERIE = 3;

    final static int INSTANCE = 4;

    final static int PATH = 5;

    //

    final static int QUERY_SELECT = 0;

    final static int QUERY_FROM = 1;

    final static int QUERY_JOIN = 2;

    final static int QUERY_LINKS = 3;

    final static int QUERY_WHERE = 4;

    final static int QUERY_GROUP = 5;

    final static int QUERY_ORDER = 6;

    private static void ParseQuery(Connection conn, String[] query, String[][] update, CommandLine cfg)
            throws SQLException, IOException {
        Statement stmt = conn.createStatement();
        ResultSet rs = null;

        String SQLStatement = "";

        if (cfg.nonempty) {
            SQLStatement += "from " + query[QUERY_FROM] + " ";
            SQLStatement += "where " + query[QUERY_LINKS] + " " + query[QUERY_WHERE];
        } else {
            SQLStatement += "from " + query[QUERY_JOIN] + " ";
            SQLStatement += "where " + query[QUERY_WHERE];
        }

        // Construct Count Statement
        String CountStatement = "select COUNT(*) CNT " + SQLStatement;
        // Construct Select Statement
        String QueryStatement = "select " + query[QUERY_SELECT] + " " + SQLStatement;

        if (cfg.debug) {
            System.err.println("DEBUG: Count: < " + CountStatement + " >");
            System.err.println("DEBUG: Query: < " + QueryStatement + " >");
        }

        boolean multi = false;
        String UpdateStatement = null;
        String UpdateLevel = null;
        boolean UpdateAttrs = false;
        boolean DoUpdate = false;
        PreparedStatement UpdStmt = null;

        if (update != null) {
            UpdateLevel = update[0][3].toUpperCase();
            if (update[0][4].equals("t"))
                multi = true;

            for (int loop = 0; loop < update.length; loop++) {
                if (update[loop][0] != null) {
                    if (UpdateStatement == null)
                        UpdateStatement = "";
                    else if (UpdateStatement.length() > 0)
                        UpdateStatement += ",";
                    UpdateStatement += update[loop][0].toUpperCase();
                    UpdateStatement += (update[loop][2] == null) ? "=null" : "='" + update[loop][2] + "'";
                }
                if (update[loop][1] != null)
                    UpdateAttrs = true;
            }
            if (UpdateAttrs) {
                if (UpdateStatement == null)
                    UpdateStatement = "";
                else if (UpdateStatement.length() > 0)
                    UpdateStatement += ",";
                if (UpdateLevel.equals("PATIENT"))
                    UpdateStatement += "PAT_ATTRS";
                if (UpdateLevel.equals("STUDY"))
                    UpdateStatement += "STUDY_ATTRS";
                if (UpdateLevel.equals("SERIES"))
                    UpdateStatement += "SERIES_ATTRS";
                UpdateStatement += "=?";
            }

            UpdateStatement += " where PK=?";
            DoUpdate = true;

            if (cfg.debug) {
                System.err.println("DEBUG: Update: < " + "update " + UpdateLevel + " set " + UpdateStatement + " >");
            }
        }

        rs = stmt.executeQuery(CountStatement);
        rs.next();
        long rows = rs.getLong(1);
        rs.close();

        if (DoUpdate && !multi && rows != 1) {
            _System.exit(1, "Multiple Updates not allowed on this Configuration.");
        }

        if (DoUpdate) {
            UpdStmt = conn.prepareStatement("update " + UpdateLevel + " set " + UpdateStatement);
        }

        rs = stmt.executeQuery(QueryStatement);
        ResultSetMetaData md = rs.getMetaData();

        long PK;

        while (rs.next()) {
            boolean ExecUpdate = false;
            PK = Display.Patient(rs, md, cfg);
            if (DoUpdate && UpdateLevel.equals("PATIENT") && PK > -1) {
                if (!UpdateAttrs)
                    UpdStmt.setLong(1, PK);
                ExecUpdate = true;
            }

            if (cfg.levels.get(STUDY)) {
                PK = Display.Study(rs, md, cfg);
                if (DoUpdate && UpdateLevel.equals("STUDY") && PK > -1) {
                    if (!UpdateAttrs)
                        UpdStmt.setLong(1, PK);
                    ExecUpdate = true;
                }
            }
            if (cfg.levels.get(SERIE)) {
                PK = Display.Serie(rs, md, cfg);
                if (DoUpdate && UpdateLevel.equals("SERIES") && PK > -1) {
                    if (!UpdateAttrs)
                        UpdStmt.setLong(1, PK);
                    ExecUpdate = true;
                }
            }
            if (cfg.levels.get(INSTANCE)) {
                Display.Instance(rs, md, cfg);
            }
            if (cfg.levels.get(PATH)) {
                Display.Path(rs, md, cfg);
            }
            if (ExecUpdate) {
                if (cfg.debug) {
                    System.err.println("DEBUG: Update: < " + UpdateLevel + " PK=" + PK + " >");
                }
            }
        }

        rs.close();
        stmt.close();
    }

    public static void main(String[] argv) {
        Connection conn = null;
        String query[] = null;
        String update[][] = null;

        CommandLine cfg = new CommandLine(argv);

        if (cfg.debug) {
            System.err.println("DEBUG: Connect Url: < " + cfg.jdbcUrl + " >");
        }

        try {
            conn = DriverManager.getConnection(cfg.jdbcUrl);
            cfg.setDatabase(conn);
            // setPreparedStatements(conn);

            update = Query.BuildUpdate(cfg);
            query = Query.Build(cfg);
            /*
             * if (UpdDB.equalsIgnoreCase("MODALITY") &&
             * UpdLevel.equalsIgnoreCase("SERIES")) { UpdateModality = true; }
             * 
             * // System.out.println(loop + ":" + TmpUpdDicom + "|" + UpdDB +
             * "|" + UpdMulti + "|" + UpdValue);
             */

            ParseQuery(conn, query, update, cfg);

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