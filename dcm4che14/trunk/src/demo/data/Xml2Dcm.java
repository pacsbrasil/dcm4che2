/*$Id$*/
/*****************************************************************************
 *                                                                           *
 *  Copyright (c) 2002 by TIANI MEDGRAPH AG                                  *
 *                                                                           *
 *  This file is part of dcm4che.                                            *
 *                                                                           *
 *  This library is free software; you can redistribute it and/or modify it  *
 *  under the terms of the GNU Lesser General Public License as published    *
 *  by the Free Software Foundation; either version 2 of the License, or     *
 *  (at your option) any later version.                                      *
 *                                                                           *
 *  This library is distributed in the hope that it will be useful, but      *
 *  WITHOUT ANY WARRANTY; without even the implied warranty of               *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU        *
 *  Lesser General Public License for more details.                          *
 *                                                                           *
 *  You should have received a copy of the GNU Lesser General Public         *
 *  License along with this library; if not, write to the Free Software      *
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA  *
 *                                                                           *
 *****************************************************************************/

import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.data.DcmValueException;
import org.dcm4che.data.Dataset;

import org.xml.sax.SAXException;
import java.io.*;
import javax.xml.parsers.*;
import gnu.getopt.*;

/**
 *
 * @author  gunter.zeilinger@tiani.com
 */
public class Xml2Dcm {

    private Dataset ds = DcmObjectFactory.getInstance().newDataset();
    
    /** Creates a new instance of Xml2Dcm */
    public Xml2Dcm() {
    }
    
    public void read(DataInputStream in) throws IOException, DcmValueException {
        ds.clear();
        try {
            ds.read(in, null, -1);
        } finally {
            try { in.close(); } catch (IOException ignore) {}
        }
    }
            
    public void process(String xml_file, DataOutputStream out)
            throws IOException, DcmValueException,
                ParserConfigurationException, SAXException {
        try {
            SAXParserFactory f = SAXParserFactory.newInstance();
            SAXParser p = f.newSAXParser();
            p.parse(new File(xml_file), ds.getSAXHandler());
            ds.writeFile(out, null);
        } finally {
            try { out.close(); } catch (IOException ignore) {}
        }        
    }        
    
    /**
    * @param args the command line arguments
    */
    public static void main (String args[]) throws Exception {
        Getopt g = new Getopt("xml2dcm.jar", args, "i:");
        
        Xml2Dcm xml2dcm = new Xml2Dcm();
        int c;
        while ((c = g.getopt()) != -1) {
            switch (c) {                
                case 'i':
                    xml2dcm.read(new DataInputStream(
                            new BufferedInputStream(
                            new FileInputStream(g.getOptarg()))));
                    break;
                case '?':
                    exit("");
                    break;
                }
        }
        int optind = g.getOptind();
        int argc = args.length - optind;
        if (argc < 2) {
            exit("xml2dcm.jar: Missing argument\n");
        }

        if (argc > 2) {
            exit("xml2dcm.jar: To many arguments\n");
        }
        xml2dcm.process(args[optind], new DataOutputStream(
                            new BufferedOutputStream(
                            new FileOutputStream(args[optind+1]))));
    }
    
    private static void exit(String prompt) {
        System.err.println(prompt);
        System.err.println(USAGE);
        System.exit(1);
    }

    private static final String USAGE =
"Usage: java -jar xml2dcm.jar [-i <dcm_file>] <xml_file> <dcm_file>\n\n" +
"Create or update DICOM file <dcm_file> according XML specification <xml_file>.\n\n" +
"Options:\n" +
" -i <dcm_file>  Update specified DICOM file but store it as new one.\n";
}
