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

import org.dcm4che.data.DcmParserFactory;
import org.dcm4che.data.DcmParser;
import org.dcm4che.dict.DictionaryFactory;
import org.dcm4che.dict.Tags;
import org.dcm4che.dict.TagDictionary;

import java.io.*;
import java.net.*;
import java.util.*;
import javax.xml.transform.*;
import javax.xml.transform.sax.*;
import javax.xml.transform.stream.*;
import gnu.getopt.*;

/**
 *
 * @author  gunter.zeilinger@tiani.com
 */
public class Dcm2Xml {

    private OutputStream out = System.out;
    private URL xslt = null;
    private LinkedList xsltParams = new LinkedList();
    private boolean xsltInc = false;
    private int stopTag = Tags.PixelData;
    private TagDictionary dict =
            DictionaryFactory.getInstance().getDefaultTagDictionary();
    
    /** Creates a new instance of Dcm2xml */
    public Dcm2Xml() {
    }
    
    public void setStopTag(int stopTag) {
        this.stopTag = stopTag;
    }
    
    public void setTagDictionary(TagDictionary dict) {
        this.dict = dict;
    }
    
    public void setXslt(URL xslt) {
        this.xslt = xslt;
    }
    
    public void setXsltInc(boolean xsltInc) {
        this.xsltInc = xsltInc;
    }
    
    public void addXsltParam(String expr) {
        if (expr.indexOf('=') <= 0) {
            throw new IllegalArgumentException(expr);
        }
        this.xsltParams.add(expr);
    }
    
    public void setOut(OutputStream out) {
        this.out = out;
    }
    
    private TransformerHandler getTransformerHandler()
            throws TransformerConfigurationException, IOException {
        SAXTransformerFactory tf =
            (SAXTransformerFactory)TransformerFactory.newInstance();
        TransformerHandler th = null;
        if (xslt != null) {
            if (xsltInc) {
                tf.setAttribute(
                        "http://xml.apache.org/xalan/features/incremental", 
                        Boolean.TRUE);
            }
            th = tf.newTransformerHandler(
                    new StreamSource(xslt.openStream(),xslt.toExternalForm()));
            Transformer t = th.getTransformer();
            for (Iterator it = xsltParams.iterator(); it.hasNext();) {
                String s = (String)it.next();
                int eqPos = s.indexOf('=');
                t.setParameter(s.substring(0,eqPos), s.substring(eqPos+1));
            }
        } else {
            th = tf.newTransformerHandler();            
            th.getTransformer().setOutputProperty(OutputKeys.INDENT,"yes");
        }
        th.setResult(new StreamResult(out));
        return th;
    }
    
    public void process(String file)
            throws IOException, TransformerConfigurationException {
        DcmParser parser = DcmParserFactory.getInstance().newDcmParser();
        DataInputStream in = new DataInputStream(new BufferedInputStream(
                new FileInputStream(file)));
        try {
            parser.setInput(in);
            parser.setSAXHandler(getTransformerHandler(), dict);
            parser.parseDcmFile(null, stopTag);
        } finally {
            try { out.close(); } catch (IOException ignore) {}
            try { in.close(); } catch (IOException ignore) {}
        }        
    }        
    
    /**
    * @param args the command line arguments
    */
    public static void main (String args[]) throws Exception {
        LongOpt[] longopts = new LongOpt[2];
        longopts[0] = new LongOpt("TXT", LongOpt.NO_ARGUMENT, null, 'T');
        longopts[1] = new LongOpt("XSL", LongOpt.REQUIRED_ARGUMENT, null, 'X');

        Getopt g = new Getopt("dcm2xml.jar", args, "bxo:ID:", longopts, true);
        
        Dcm2Xml dcm2xml = new Dcm2Xml();
        int c;
        while ((c = g.getopt()) != -1) {
            switch (c) {                
                case 'b':
                    dcm2xml.setTagDictionary(null);
                    break;
                case 'x':
                    dcm2xml.setStopTag(-1);
                    break;
                case 'o':
                    dcm2xml.setOut(new BufferedOutputStream(
                            new FileOutputStream(g.getOptarg())));
                    break;
                case 'T':
                    dcm2xml.setXslt(Dcm2Xml.class.getResource(
                            "/resources/Xml2Txt.xsl"));
                    break;
                case 'X':
                    dcm2xml.setXslt(new File(g.getOptarg()).toURL());
                    break;
                case 'I':
                    dcm2xml.setXsltInc(true);
                    break;
                case 'D':
                    dcm2xml.addXsltParam(g.getOptarg());
                    break;
                case '?':
                    exit("");
                    break;
                }
        }
        int optind = g.getOptind();
        int argc = args.length - optind;
        if (argc == 0) {
            exit("dcm2xml.jar: Missing argument\n");
        }

        if (argc > 1) {
            exit("dcm2xml.jar: To many arguments\n");
        }
        dcm2xml.process(args[optind]);
    }
    
    private static void exit(String prompt) {
        System.err.println(prompt);
        System.err.println(USAGE);
        System.exit(1);
    }

    private static final String USAGE =
"Usage: java -jar dcm2xml.jar <dcm_file> [-bx] [--TXT | --XSL <xsl_file>]\n" +
"                    [-I] [-D<param>=<value> ...][-o <xml_file>] \n\n" +
"Transform the specified DICOM file <dcm_file> into XML and optionally apply\n" +
"XSLT with the specified XSL stylesheet <xsl_file> to the XML presentation.\n\n" +
"Options:\n" +
" -b                 Brief format: exclude attribute names from XML output.\n" +
" -x                 Include pixel data into the XML output. At default, only\n" +
"                    elements before the pixel data element will be included.\n" +             
" --TXT              Apply default XSLT to produce text output:\n" +
"                     -Dmaxlen=<maximal line length> default=80\n" +
"                     -Dvallen=<displayed value length> default=64\n" +
" --XSL <xsl_file>   Apply XSLT with specified XSL stylesheet <xsl_file>.\n" +
" -I                 Enable incremental XSLT (only usable with XALAN)\n" +
" -D<param>=<value>  Set XSL parameter to specified value.\n" +
" -o <xml_file>      Place output in <xml_file> instead in standard output.\n";
}
