// $Id$

package com.agfa.db.tools;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Enumeration;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.cli.*;

class Config {
    String FieldSeperator = ";";

    String RecordSeperator = "";

    String TextDelimitor = "";

    String tableName = null;

    boolean debug = false;

    boolean header = false;

    boolean csv = false;

    boolean label = false;

    boolean dump = false;

    boolean displaylobs = false;

    boolean inserts = false;

    boolean sqlFile = false;

    boolean ignoresqlerror = false;

    String sqlFilename = null;

    String jdbcUrl = null;

    String sql = null;

    long commitInterval = -1;

    public int Database = Jdbexp.DB_TYPES_UNKNOWN;

    //
    String nl = System.getProperty("line.separator");

    static SimpleDateFormat fTimeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    static SimpleDateFormat fTime = new SimpleDateFormat("HH:mm:ss");

    static SimpleDateFormat fDate = new SimpleDateFormat("yyyy-MM-dd");

    public static void LongHelp() {
        System.out.println("Usage: java -jar jdbexp [options] <sql> | --sqlfile <file> | -M <macro>");
        System.out.println("       java -jar jdbexp -h | --help");
        System.out.println("       java -jar jdbexp -v | --version");
        System.out.println("       java -jar jdbexp --jdbcurlhelp");
        System.out.println();
        System.out.println("        -v | --verson                    display version information");
        System.out.println();
        System.out.println("        -U | --url <jdbcURL>             DB jdbcURL to connect");
        System.out.println("             --db <alias>                DB alias to connect");
        System.out.println();
        System.out.println("        -d | --delimiter <delimiter>     set <delimiter> (')");
        System.out.println("        -H | --header                    display columnnames as header");
        System.out.println("        -L | --label                     display columnnames as labels (linewise)");
        System.out.println();
        System.out.println("        --csv                            export as CSV");
        System.out.println("        --insert                         export with insert statements");
        System.out.println("        --dump                           export as (nonportable) dump");
        System.out.println();
        System.out.println("        --sqlfile <file>                 use <file> for SQL statements");
        System.out.println("        --commit <interval>              commit after <interval> statements");
        System.out.println();
        System.out.println("        -M <macro> [arguments]           execute predefined macro");
        System.out.println();
        System.out.println("        -O | --out <file> [-z | -gzip]   write output to (gziped) <file>");
        System.out.println("        -E | --err <file>                write error to <file>");
        System.out.println("");
        System.out.println("        --jdbcurlhelp                    jdbcURL Syntax");
        System.out.println();
        System.out.println("        --debug                          debug mode");
        System.out.println();
    }

    public static void UrlHelp() {
        System.out.println("jdbc URL Examples:");
        System.out.println();
        System.out.println("ORACLE: jdbc:oracle:<drivertype>:<username/password>@<database>");
        System.out.println("");
        System.out.println("        <drivertype>: * thin");
        System.out.println("                      * oci");
        System.out.println("                      * kprb");
        System.out.println();
        System.out.println(" <username/password>: * is either empty or of the form <username>/<password>");
        System.out.println();
        System.out.println("          <database>: * <host>:<port>:<SID>");
        System.out.println("                      * //<host>:<port>/<service>");
        System.out.println("                      * <TNSName>");
        System.out.println();
        System.out.println("             example: * jdbc:oracle:thin:dbuser/dbpw@dbhost:1521:MYSID");
        System.out.println("                      * jdbc:oracle:thin:@MYTSNNAME");
        System.out.println();
    }

    public static void ShortHelp() {
        System.out.println("Usage: java -jar jdbexp <sql>");
        System.out.println("       java -jar jdbexp -h | --help");
        System.out.println("       java -jar jdbexp -v | --version");
        System.out.println();
        System.out.println("        -v | --version     display version information");
        System.out.println("        -h | --help        display extended help");
        System.out.println();
        System.out.println("        --debug            debug mode");
        System.out.println();
    }

    void setDatabase(Connection conn) throws SQLException {
        DatabaseMetaData dmd = conn.getMetaData();

        if (dmd.getDatabaseProductName().equalsIgnoreCase("ORACLE")) {
            Database = Jdbexp.DB_TYPES_ORACLE;
        } else if (dmd.getDatabaseProductName().equalsIgnoreCase("MYSQL")) {
            Database = Jdbexp.DB_TYPES_MYSQL;
        } else {
            Database = Jdbexp.DB_TYPES_UNKNOWN;
        }
    }

    Config(String[] argv) {
        Properties defaultProps = new Properties();

        try {
            defaultProps.load(ClassLoader.getSystemResourceAsStream("com/agfa/db/tools/jdbexp.properties"));
        } catch (Exception e) {
            Jdbexp.exit(1, "I/O failed.");
        }

        Properties applicationProps = new Properties(defaultProps);

        File tmp = null;

        tmp = new File("/etc/jdbexp.properties");
        if (tmp.exists() && tmp.isFile() && tmp.canRead()) {
            try {
                applicationProps.load(new FileInputStream(tmp));
            } catch (Exception e) {
                Jdbexp.exit(1, "Can't find " + tmp);
            }
        }
        tmp = new File("/etc/jdb.properties");
        if (tmp.exists() && tmp.isFile() && tmp.canRead()) {
            try {
                applicationProps.load(new FileInputStream(tmp));
            } catch (Exception e) {
                Jdbexp.exit(1, "Can't find " + tmp);
            }
        }

        String dbalias = null;
        String sqlMacro = null;
        String outFilename = null;
        String errFilename = null;

        boolean gzip = false;

        CommandLineParser parser = new PosixParser();

        // create the Options
        Options options = new Options();

        options.addOption("h", "help", false, "print this message");
        options.addOption("v", "version", false, "version information");
        options.addOption("L", "label", false, "display labels");
        options.addOption("H", "header", false, "display header");
        options.addOption(OptionBuilder.withLongOpt("url").withDescription("jdbc connection url").hasArg().withArgName(
                "JDBCURL").create("U"));
        options.addOption(OptionBuilder.withLongOpt("db").withDescription("DB alias").hasArg().withArgName("ALIAS").create());
        options.addOption(OptionBuilder.withLongOpt("delimiter").withDescription("field delimiter").hasArg()
                .withArgName("DELIMITER").create("d"));
        options.addOption(OptionBuilder.withLongOpt("cvs").withDescription("display as cvs").create());
        options.addOption(OptionBuilder.withLongOpt("insert").withDescription("display with insert statements").create());
        options.addOption(OptionBuilder.withLongOpt("dump").withDescription("display as dump").create());
        options.addOption(OptionBuilder.withLongOpt("sqlfile").withDescription("read statemtents from sqlfile").hasArg().withArgName("FILE")
                .create());
        options.addOption(OptionBuilder.withLongOpt("commit").withDescription("commit after <COMMIT> statements").hasArg().withArgName(
                "COMMIT").create());
        options.addOption(OptionBuilder.withLongOpt("macro").withDescription("exec macro").hasArg()
                .withArgName("MACRO").create("M"));
        options.addOption(OptionBuilder.withLongOpt("out").withDescription("output stdout to FILE").hasArg()
                .withArgName("FILE").create("O"));
        options.addOption(OptionBuilder.withLongOpt("gzip").withDescription("compress output").create("z"));
        options.addOption(OptionBuilder.withLongOpt("err").withDescription("output stderr to FILE").hasArg()
                .withArgName("FILE").create("E"));
        options.addOption(OptionBuilder.withLongOpt("debug").withDescription("output debug").create());
        options.addOption(OptionBuilder.withLongOpt("jdbcurlhelp").withDescription("print URL help").create());
        options.addOption(OptionBuilder.withLongOpt("displaylobs").withDescription("display LOB's as base64").create());
        options.addOption(OptionBuilder.withLongOpt("ignoresqlerrors").withDescription("ignore SQL errors").create());

        try {
            // parse the command line arguments
            CommandLine line = parser.parse(options, argv);

            if (line.hasOption("debug"))
                debug = true;
            if (line.hasOption("displaylobs"))
                displaylobs = true;
            if (line.hasOption("commit")) {
                commitInterval = Long.parseLong(line.getOptionValue("commit").trim());
            }
            if (line.hasOption("insert")) {
                inserts = true;
                TextDelimitor = "\'";
                FieldSeperator = ",";
            }
            if (line.hasOption("dump")) {
                dump = true;
                TextDelimitor = "\'";
                FieldSeperator = ",";
                RecordSeperator = ";";
            }
            if (line.hasOption("M"))
                sqlMacro = line.getOptionValue("M");
            if (line.hasOption("cvs")) {
                csv = true;
                header = true;
                TextDelimitor = "\"";
                FieldSeperator = ";";
            }
            if (line.hasOption("H"))
                header = true;
            if (line.hasOption("L")) {
                label = true;
                FieldSeperator = nl;
                RecordSeperator = nl;
            }
            if (line.hasOption("d"))
                FieldSeperator = line.getOptionValue("d");
            if (line.hasOption("h")) {
                // automatically generate the help statement
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("java -jar jdbexp.jar [options] <\"sql statement\">", options);
                System.exit(0);
            }
            if (line.hasOption("jdbcurlhelp")) {
                UrlHelp();
                System.exit(0);
            }
            if (line.hasOption("U"))
                jdbcUrl = line.getOptionValue("U");

            if (line.hasOption("sqlfile")) {
                sqlFilename = line.getOptionValue("sqlfile");
                sqlFile = true;
            }

            if (line.hasOption("db")) {
                dbalias = line.getOptionValue("db");
            }
            if (line.hasOption("v")) {
                System.out.println(Jdbexp.ID);
                System.out.println(Jdbexp.REVISION);
                System.exit(0);
            }
            if (line.hasOption("E")) {
                errFilename = line.getOptionValue("E");
            }
            if (line.hasOption("O")) {
                outFilename = line.getOptionValue("O");
                if (line.hasOption("z"))
                    gzip = true;
            }

            //
            argv = line.getArgs();
            int i = 0;
            if (errFilename != null) {
                try {
                    System.setErr(new PrintStream(new BufferedOutputStream(new FileOutputStream(errFilename))));
                } catch (Exception e) {
                    Jdbexp.exit(1, e.toString());
                }
            }

            if (outFilename != null) {
                try {
                    if (gzip == true)
                        System.setOut(new PrintStream(new BufferedOutputStream(new GZIPOutputStream(
                                new FileOutputStream(outFilename)))));
                    else
                        System.setOut(new PrintStream(new BufferedOutputStream(new FileOutputStream(outFilename))));

                } catch (Exception e) {
                    Jdbexp.exit(1, e.toString());
                }
            }
            if (sqlMacro != null) {
                if (sqlMacro.equalsIgnoreCase("list")) {
                    System.out.println("MACROS:");
                    Enumeration e = applicationProps.propertyNames();
                    while (e.hasMoreElements()) {
                        String key = (String) e.nextElement();
                        if (key.startsWith("macro."))
                            System.out.println(key + ": " + applicationProps.getProperty(key));
                    }
                    System.exit(0);
                } else {
                    sql = applicationProps.getProperty("macro." + sqlMacro, "").trim();
                    int j = 1;
                    while (i < argv.length)
                        sql = sql.replaceAll("\\$" + j++ + "\\$", argv[i++].trim());
                }
            } else {
                if (i < argv.length)
                    sql = argv[i++].trim();
            }
            if (i < argv.length) {
                System.err.println("Error: Too many arguments.");
            }

            if (gzip && outFilename == null) {
                Jdbexp.exit(1, "Error: gzip option only makes sense with out option.");
            }

            if (sqlFile && sql != null) {
                System.err.println("Error: Either SQLFILE or SQL Statement must be provided.");
            }

            if ((!sqlFile && sql == null) || (sqlFile && sql != null) || (i < argv.length)) {
                ShortHelp();
                System.exit(1);
            }

            if (dump || inserts) {
                Pattern pattern = Pattern.compile("^select\\s+.*\\sfrom\\s+(\\S+)", Pattern.CASE_INSENSITIVE);
                Matcher matcher = pattern.matcher(sql.replaceAll("\\s*,\\s*", ","));
                if (matcher.find()) {
                    tableName = matcher.group(1);
                    if (tableName.split("\\W").length != 1) {
                        Jdbexp.exit(1, "Error: SQL Error - Multiple tables (" + tableName + ") specified?");
                    }
                } else {
                    Jdbexp.exit(1, "Error: SQL Error - No table specified?");
                }
            }

            if (csv && label) {
                Jdbexp.exit(1, "Error: Use either label or csv option.");
            }

            if (header && label) {
                Jdbexp.exit(1, "Error: Use either label or header option.");
            }

            String jdbcDriverClass = System.getProperty("jdbc.driver");

            if (jdbcDriverClass == null)
                jdbcDriverClass = applicationProps.getProperty("jdbc.driver", "oracle.jdbc.driver.OracleDriver");

            try {
                Class.forName(jdbcDriverClass);
            } catch (ClassNotFoundException e) {
                Jdbexp.exit(-1, e.toString());
            }

            if (jdbcUrl == null) {
                if (dbalias == null)
                    dbalias = applicationProps.getProperty("jdbc.url.DEFAULT");
                if (dbalias != null) {
                    jdbcUrl = applicationProps.getProperty("jdbc.url." + dbalias);
                    if (jdbcUrl == null) {
                        Jdbexp.exit(1, "ERROR: DB Alias: < " + dbalias + " > not found!");

                    }
                }
                if (jdbcUrl == null)
                    jdbcUrl = System.getProperty("jdbc.url");

                if (jdbcUrl == null)
                    jdbcUrl = applicationProps.getProperty("jdbc.url");
            }
        }

        catch (ParseException exp) {
            System.out.println("Unexpected exception:" + exp.getMessage());
        }
    }
}
