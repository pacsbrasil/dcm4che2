// $Id$

package com.agfa.db.tools;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;

public class Display {
    public static long LastPatient = -1;

    public static long LastStudy = -1;

    public static long LastSerie = -1;

    public static long LastInstance = -1;

    public static long LastFile = -1;

    static String Patient(ResultSet rs, ResultSetMetaData md, CommandLine cfg) throws SQLException, IOException {
        String out = "";
        long tmpKey = rs.getLong("A");

        if (LastPatient != tmpKey) {
            LastPatient = tmpKey;

            if (cfg.displayFields)
                out = out.concat("NAME:");
            out = out.concat(("" + rs.getString("PAT_NAME")).replace('^', ' ').trim() + " ");

            if (cfg.displayFields)
                out = out.concat("SEX:");
            out = out.concat(rs.getString("PAT_SEX") + " ");

            if (cfg.displayFields)
                out = out.concat("BIRTHDATE:");
            if (cfg.pre214) {
                Timestamp tmpBD = rs.getTimestamp("BD");
                if (tmpBD != null) {
                    out = out.concat(CommandLine.fDate.format(tmpBD) + " ");
                } else {
                    out = out.concat("- ");
                }
            } else {
                String tmpBD = rs.getString("BD");
                if (tmpBD != null && tmpBD.length() == 8) {
                    out = out.concat(tmpBD.substring(0, 4) + "-" + tmpBD.substring(4, 6) + "-" + tmpBD.substring(6)
                            + " ");
                } else {
                    out = out.concat("- ");
                }
            }

            if (cfg.displayFields)
                out = out.concat("ISSUER/ID:");
            out = out.concat("<");
            out = out.concat(rs.getString("PAT_ID_ISSUER") + "");
            out = out.concat(":");
            out = out.concat(rs.getString("PAT_ID") + "");
            out = out.concat(">");

            if (cfg.displayPKS) {
                out = out.concat(" ");
                if (cfg.displayFields)
                    out = out.concat("PK:");
                out = out.concat("[" + tmpKey + "]");
            }
            System.out.println(out);
            return out;
        }
        return null;
    }

    static String Study(ResultSet rs, ResultSetMetaData md, CommandLine cfg) throws SQLException, IOException {
        String out = "";
        long tmpKey = rs.getLong("B");

        if (LastStudy != tmpKey) {
            LastStudy = tmpKey;
            if (cfg.displayFields)
                out = out.concat("IUID:");
            out = out.concat(rs.getString("STYIUID") + " ");
            if (cfg.displayFields)
                out = out.concat("DATE:");
            Timestamp tmpSD = rs.getTimestamp("STYD");
            if (tmpSD != null) {
                out = out.concat(CommandLine.fTimeStamp.format(tmpSD) + " ");
            } else {
                out = out.concat("- ");
            }
            if (cfg.displayFields)
                out = out.concat("MODS:");
            String tmpMOD = rs.getString("STYMODS");
            if (tmpMOD != null) {
                out = out.concat("(" + tmpMOD.replace('\\', ',') + ") ");
            } else {
                out = out.concat("( - ) ");
            }

            if (cfg.displayFields)
                out = out.concat("#SER:");
            out = out.concat(rs.getLong("STYNUMSER") + " ");
            if (cfg.displayFields)
                out = out.concat("#INST:");
            out = out.concat(rs.getLong("STYNUMINST") + " ");

            if (cfg.displayFields)
                out = out.concat("AVAIL:");
            out = out.concat(rs.getLong("STYAVAIL") + "");

            if (cfg.displayAET) {
                out = out.concat(" ");
                if (cfg.displayFields)
                    out = out.concat("RETAET:");
                String tmpAET = null;
                String tmpAET1 = rs.getString("STYRETAET");
                String tmpAET2 = rs.getString("STYEXTRETAET");
                tmpAET = tmpAET1;
                if (tmpAET2 != null) {
                    if (tmpAET != null) {
                        tmpAET = tmpAET + "," + tmpAET2;
                    } else {
                        tmpAET = tmpAET2;
                    }
                }
                out = out.concat("{" + tmpAET + "}");
            }

            if (cfg.displayPKS) {
                out = out.concat(" ");
                if (cfg.displayFields)
                    out = out.concat("PK:");
                out = out.concat("[" + tmpKey + "]");
            }

            System.out.println(" " + out);
            return out;
        }
        return null;

    }

    static String Serie(ResultSet rs, ResultSetMetaData md, CommandLine cfg) throws SQLException, IOException {
        String out = "";
        long tmpKey = rs.getLong("C");

        if (LastSerie != tmpKey) {
            LastSerie = tmpKey;
            if (cfg.displayFields)
                out = out.concat("IUID:");
            out = out.concat(rs.getString("SERIUID") + " ");
            // out=out.concat(result.getString("SD") + " ");
            if (cfg.displayFields)
                out = out.concat("MOD:");
            out = out.concat(rs.getString("SERMOD") + " ");
            if (cfg.displayFields)
                out = out.concat("#INST:");
            out = out.concat(rs.getLong("SERNUMINST") + " ");
            if (cfg.displayFields)
                out = out.concat("AVAIL:");
            out = out.concat(rs.getLong("SERAVAIL") + " ");
            if (cfg.displayFields)
                out = out.concat("STAT:");
            out = out.concat(rs.getLong("SERSTATUS") + "");
            if (cfg.displayAET) {
                out = out.concat(" ");
                if (cfg.displayFields)
                    out = out.concat("SRC::RETAET:");
                String tmpAET = null;
                String tmpAET1 = rs.getString("SERRETAET");
                String tmpAET2 = rs.getString("SEREXTRETAET");
                String srcAET = rs.getString("SERSRCAET");
                tmpAET = tmpAET1;
                if (tmpAET2 != null) {
                    if (tmpAET != null) {
                        tmpAET = tmpAET + "," + tmpAET2;
                    } else {
                        tmpAET = tmpAET2;
                    }
                }
                out = out.concat("{" + srcAET + "::" + tmpAET + "}");
            }
            if (cfg.displayPKS) {
                out = out.concat(" ");
                if (cfg.displayFields)
                    out = out.concat("PK:");
                out = out.concat("[" + tmpKey + "]");
            }
            System.out.println("  " + out);
            return out;
        }
        return null;
    }

    static String Instance(ResultSet rs, ResultSetMetaData md, CommandLine cfg) throws SQLException, IOException {
        String out = "";
        long tmpKey = rs.getLong("D");

        if (LastInstance != tmpKey) {
            LastInstance = tmpKey;
            if (cfg.displayFields)
                out = out.concat("IUID:");
            out = out.concat(rs.getString("SOPIUID") + " ");
            if (cfg.displayFields)
                out = out.concat("AVAIL:");
            out = out.concat(rs.getString("INSTAVAIL") + " ");
            if (cfg.displayFields)
                out = out.concat("STAT:");
            out = out.concat(rs.getString("INSTSTATUS") + "");
            if (cfg.displayAET) {
                out = out.concat(" ");
                if (cfg.displayFields)
                    out = out.concat("RETAET:");
                String tmpAET = null;
                String tmpAET1 = rs.getString("INSTRETAET");
                String tmpAET2 = rs.getString("INSTEXTRETAET");
                tmpAET = tmpAET1;
                if (tmpAET2 != null) {
                    if (tmpAET != null) {
                        tmpAET = tmpAET + "," + tmpAET2;
                    } else {
                        tmpAET = tmpAET2;
                    }
                }
                out = out.concat("{" + tmpAET + "}");
            }
            if (cfg.displayPKS) {
                out = out.concat(" ");
                if (cfg.displayFields)
                    out = out.concat("PK:");
                out = out.concat("[" + tmpKey + "]");
            }
            System.out.println("   " + out);
            return out;
        }
        return null;
    }

    static String Path(ResultSet rs, ResultSetMetaData md, CommandLine cfg) throws SQLException, IOException {
        String out = "";
        if (cfg.displayFields)
            out = out.concat("PATH:");
        out = out.concat(rs.getString("DIRPATH") + "/" + rs.getString("FILEPATH") + " ");
        if (cfg.displayFields)
            out = out.concat("SIZE:");
        out = out.concat(rs.getString("FILESIZE") + " ");
        if (cfg.displayFields)
            out = out.concat("AVAIL:");
        out = out.concat(rs.getString("FSAVAIL") + "");

        if (cfg.displayFSInfo) {
            out = out.concat(" ");
            if (cfg.displayFields)
                out = out.concat("FSPK:");
            out = out.concat("["+rs.getLong("F") + "] ");
            if (cfg.displayFields)
                out = out.concat("GROUP:");
            out = out.concat(rs.getString("FSGRP") + "");
        }

        if (cfg.displayPKS) {
            out = out.concat(" ");
            if (cfg.displayFields)
                out = out.concat("PK:");
            out = out.concat("[" + rs.getLong("E") + "]");
        }
        System.out.println("    " + out);
        return out;
    }
}
