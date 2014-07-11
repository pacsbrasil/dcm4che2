/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 *
 * The Initial Developer of the Original Code is
 * Raster Images
 * Portions created by the Initial Developer are Copyright (C) 2014
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Babu Hussain A
 * Devishree V
 * Meer Asgar Hussain B
 * Prakash J
 * Suresh V
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 *
 * ***** END LICENSE BLOCK ***** */
package in.raster.mayam.delegates;

import in.raster.mayam.context.ApplicationContext;
import static in.raster.mayam.delegates.InputArgumentsParser.inputArgumentValues;
import static in.raster.mayam.delegates.InputArgumentsParser.opts;
import in.raster.mayam.models.InputArgumentValues;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import org.apache.commons.cli.*;
import org.apache.commons.codec.binary.Base64;

/**
 *
 * @author BabuHussain
 * @version 0.9
 *
 */
public class InputArgumentsParser {

    public static InputArgumentValues inputArgumentValues = null;
    private static final String USAGE = "java -jar Mayam.jar [Options] ";//<aet>[@<host>[:<port>]]
    private static final String EXAMPLE =
            "\nExample: java -jar Mayam.jar -AET <aetitle> -HostName <hostname> -Port <port> [Options] \n"
            + "=> Query the specified Application Entity with the given filters "
            + "and retrieves the matching studies to Application Entity of Mayam."
            + "One of the fields Patient ID or Study Uid or Accession number is mandatory.";
    static Options opts = new Options();

    public static void parse(String[] args) {
        if (args.length > 0) {
            inputArgumentValues = new InputArgumentValues();

            OptionBuilder.withArgName("StudyUID");
            OptionBuilder.hasArg();
            OptionBuilder.withDescription("Set the study instance uid for query/retrieve");
            opts.addOption(OptionBuilder.create("StudyUID"));

            OptionBuilder.withArgName("PatientID");
            OptionBuilder.hasArg();
            OptionBuilder.withDescription("Set the patient id query/retrieve from server");
            opts.addOption(OptionBuilder.create("PatientID"));

            OptionBuilder.withArgName("Modality");
            OptionBuilder.hasArg();
            OptionBuilder.withDescription("Set the modality matching key for query/retrieve from server");
            opts.addOption(OptionBuilder.create("Modality"));

            OptionBuilder.withArgName("AccessionNumber");
            OptionBuilder.hasArg();
            OptionBuilder.withDescription("Set the accession number matching key for query/retrieve");
            opts.addOption(OptionBuilder.create("Accession"));

            OptionBuilder.withArgName("PatientName");
            OptionBuilder.hasArg();
            OptionBuilder.withDescription("Set the patient name matching key for query/retrieve");
            opts.addOption(OptionBuilder.create("PatientName"));

            OptionBuilder.withArgName("StudyDate");
            OptionBuilder.hasArg();
            OptionBuilder.withDescription("Set the study date matching key for query/retrieve. Date Format : yyyy-MM-dd/today/lastweek/lastmonth/between");
            opts.addOption(OptionBuilder.create("StudyDate"));

            OptionBuilder.withArgName("AETitle");
            OptionBuilder.hasArg();
            OptionBuilder.withDescription("Specifies the AE title of DICOM server for query/retrieve");
            opts.addOption(OptionBuilder.create("AET"));

            OptionBuilder.withArgName("Port");
            OptionBuilder.hasArg();
            OptionBuilder.withDescription("Specifies the port of DICOM server");
            opts.addOption(OptionBuilder.create("Port"));

            OptionBuilder.withArgName("HostName");
            OptionBuilder.hasArg();
            OptionBuilder.withDescription("Specifies the hostname of DICOM server");
            opts.addOption(OptionBuilder.create("HostName"));

            OptionBuilder.withArgName("WADOURL");
            OptionBuilder.hasArg();
            OptionBuilder.withDescription("Specifies the wado context path eg.wado\n");
            opts.addOption(OptionBuilder.create("WADOURL"));


            OptionBuilder.withArgName("WADOPort");
            OptionBuilder.hasArg();
            OptionBuilder.withDescription("Specifies the wado port for retrieving the studies via WADO");
            opts.addOption(OptionBuilder.create("WADOPort"));

            OptionBuilder.withArgName("WADOProtocol");
            OptionBuilder.hasArg();
            OptionBuilder.withDescription("Specifies the wado protocol for retrieving the studies via WADO");
            opts.addOption(OptionBuilder.create("WADOProtocol"));


            OptionBuilder.withArgName("From");
            OptionBuilder.hasArg();
            OptionBuilder.withDescription("Specifies the starting date of the studies.Date Format : yyyy-MM-dd");
            opts.addOption(OptionBuilder.create("From"));


            OptionBuilder.withArgName("To");
            OptionBuilder.hasArg();
            OptionBuilder.withDescription("Specifies the ending date of the studies.Date Format : yyyy-MM-dd");
            opts.addOption(OptionBuilder.create("To"));

            OptionBuilder.withArgName("RetrieveType");
            OptionBuilder.hasArg();
            OptionBuilder.withDescription("Specifies the retrieve method C-MOVE/C-GET/WADO");
            opts.addOption(OptionBuilder.create("RetrieveType"));

            OptionBuilder.withArgName("DicomData");
            OptionBuilder.hasArg();
            OptionBuilder.withDescription("Encoded XML file that have entire study information.");
            opts.addOption(OptionBuilder.create("DicomData"));

            opts.addOption("h", "help", false, "Prints help");

            CommandLine cl = null;
            try {
                cl = new GnuParser().parse(opts, args);
            } catch (ParseException e) {
                ApplicationContext.logger.log(Level.SEVERE, "Unreachable", e);
                throw new RuntimeException("unreachable");
            }
            if (cl.hasOption('h')) {
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp(USAGE, "\nOptions:", opts, EXAMPLE);
                System.exit(0);
            }
            getInputArgumentValues(cl);
        }
    }

    public static String[] split(String s, char delim) {
        String[] s2 = {s, null};
        int pos = s.indexOf(delim);
        if (pos != -1) {
            s2[0] = s.substring(0, pos);
            s2[1] = s.substring(pos + 1);
        }
        return s2;
    }

    public static void getInputArgumentValues(CommandLine cl) {
        if (cl.hasOption("DicomData")) {    // If Encoded XML file found
            checkWadoInfo(cl);
            inputArgumentValues.setHostName(cl.hasOption("HostName") ? cl.getOptionValue("HostName") : "localhost");
            inputArgumentValues.setXmlFilePath(decodeXML(cl.getOptionValue("DicomData")));
        } else if (cl.hasOption("StudyUID") || cl.hasOption("PatientID") || cl.hasOption("Accession")) {
            checkDicomServerInfo(cl);
//            if (ApplicationContext.databaseRef.getDynamicRetrieveTypeStatus()) {
//                if (cl.hasOption("RetrieveType")) {
//                    inputArgumentValues.setRetrieveType(cl.getOptionValue("RetrieveType"));
//                } else {
//                    inputArgumentValues.setRetrieveType(ApplicationContext.databaseRef.getJNLPRetrieveType());
//                }
//            } else {
//                inputArgumentValues.setRetrieveType(ApplicationContext.databaseRef.getJNLPRetrieveType());
//            }

            if (ApplicationContext.databaseRef.getDynamicRetrieveTypeStatus() && cl.hasOption("RetrieveType")) {
                inputArgumentValues.setRetrieveType(cl.getOptionValue("RetrieveType"));
            } else {
                inputArgumentValues.setRetrieveType(ApplicationContext.databaseRef.getJNLPRetrieveType());
            }

            if (inputArgumentValues.getRetrieveType().equals("WADO")) {
                checkWadoInfo(cl);
            }
            if (cl.hasOption("Accession")) {
                inputArgumentValues.setAccessionNumber(cl.getOptionValue("Accession"));
            }
            if (cl.hasOption("Modality")) {
                inputArgumentValues.setModality(cl.getOptionValue("Modality"));
            }
            if (cl.hasOption("PatientID")) {
                inputArgumentValues.setPatientID(cl.getOptionValue("PatientID"));
            }
            if (cl.hasOption("PatientName")) {
                inputArgumentValues.setPatientName(cl.getOptionValue("PatientName"));
            }
            if (cl.hasOption("StudyDate")) {
                inputArgumentValues.setStudyDate(cl.getOptionValue("StudyDate"));
            } else {
                inputArgumentValues.setStudyDate("");
            }
            if (cl.hasOption("StudyUID")) {
                inputArgumentValues.setStudyUID(cl.getOptionValue("StudyUID"));
            }
            if (cl.hasOption("From")) {
                inputArgumentValues.setFrom(cl.getOptionValue("From"));
            }
            if (cl.hasOption("To")) {
                inputArgumentValues.setTo(cl.getOptionValue("To"));
            }
        } else if (!cl.hasOption("h")) {
            System.out.println("");
            System.err.println("ERROR : One of Patient ID or Study Uid or Accession Number Should be specified.");
            System.out.println("");
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp(USAGE, "\nOptions:", opts, EXAMPLE);
            System.exit(0);
        }
    }

    private static void checkDicomServerInfo(CommandLine cl) {
        if (cl.hasOption("AET") && cl.hasOption("HostName") && cl.hasOption("Port")) {
            inputArgumentValues.setAeTitle(cl.getOptionValue("AET"));
            inputArgumentValues.setHostName(cl.getOptionValue("HostName"));
            inputArgumentValues.setPort(Integer.parseInt(cl.getOptionValue("Port")));
        } else {
            System.out.println("");
            System.err.println("ERROR : Mandatory Fields AET, Host Name, Port ");
            System.out.println("");
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp(USAGE, "\nOptions:", opts, EXAMPLE);
//            System.exit(0);
        }
    }

    private static void checkWadoInfo(CommandLine cl) {
        if (cl.hasOption("WADOURL") && cl.hasOption("WADOPort") && cl.hasOption("WADOProtocol")) {
            inputArgumentValues.setWadoContext(cl.getOptionValue("WADOURL"));
            inputArgumentValues.setWadoPort(Integer.parseInt(cl.getOptionValue("WADOPort")));
            inputArgumentValues.setWadoProtocol(cl.getOptionValue("WADOProtocol"));
        } else {
            System.out.println("");
            System.err.println("ERROR : Insufficient WADO information.");
            System.out.println("");
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp(USAGE, "\nOptions:", opts, EXAMPLE);
            System.exit(0);
        }
    }

    private static String decodeXML(String encodedXML) {
        byte[] decoded = Base64.decodeBase64(encodedXML);
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(ApplicationContext.getAppDirectory() + File.separator + "MayamInfo.xml");
            fileOutputStream.write(decoded);
            fileOutputStream.close();
            System.out.println("Decoded XML.");
            return ApplicationContext.getAppDirectory() + File.separator + "MayamInfo.xml";
        } catch (FileNotFoundException ex) {
            ApplicationContext.logger.log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            ApplicationContext.logger.log(Level.SEVERE, null, ex);
        }
        return null;
    }
}