/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
package org.dcm4chex.cdw.mbean;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.print.Doc;
import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintException;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.SimpleDoc;
import javax.print.attribute.Attribute;
import javax.print.attribute.AttributeSet;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.Chromaticity;
import javax.print.attribute.standard.Media;
import javax.print.attribute.standard.MediaName;
import javax.print.attribute.standard.MediaSizeName;
import javax.print.attribute.standard.MediaTray;
import javax.print.attribute.standard.PrintQuality;

import org.dcm4chex.cdw.common.Executer;
import org.dcm4chex.cdw.common.MediaCreationRequest;

/**
 * @author gunter.zeilinger@tiani.com
 * @version $Revision$ $Date$
 * @since 15.08.2004
 *
 */
public class LabelPrintService extends org.jboss.system.ServiceMBeanSupport {

    private static final String[] chromaticityStringTable = { "monochrome",
            "color"};

    private static final List chromaticityStringList = Arrays.asList(chromaticityStringTable);

    private static final Chromaticity[] chromaticityValueTable = {
            Chromaticity.MONOCHROME, Chromaticity.COLOR};

    private static final String[] mediaStringTable = { "na-letter-white",
            "na-letter-transparent", "iso-a4-white", "iso-a4-transparent",
            "top", "middle", "bottom", "envelope", "manual", "large-capacity",
            "main", "side", "iso-a0", "iso-a1", "iso-a2", "iso-a3", "iso-a4",
            "iso-a5", "iso-a6", "iso-a7", "iso-a8", "iso-a9", "iso-a10",
            "iso-b0", "iso-b1", "iso-b2", "iso-b3", "iso-b4", "iso-b5",
            "iso-b6", "iso-b7", "iso-b8", "iso-b9", "iso-b10", "jis-b0",
            "jis-b1", "jis-b2", "jis-b3", "jis-b4", "jis-b5", "jis-b6",
            "jis-b7", "jis-b8", "jis-b9", "jis-b10", "iso-c0", "iso-c1",
            "iso-c2", "iso-c3", "iso-c4", "iso-c5", "iso-c6", "na-letter",
            "na-legal", "executive", "ledger", "tabloid", "invoice", "folio",
            "quarto", "japanese-postcard", "oufuko-postcard", "a", "b", "c",
            "d", "e", "iso-designated-long", "italian-envelope",
            "monarch-envelope", "personal-envelope", "na-number-9-envelope",
            "na-number-10-envelope", "na-number-11-envelope",
            "na-number-12-envelope", "na-number-14-envelope",
            "na-6x9-envelope", "na-7x9-envelope", "na-9x11-envelope",
            "na-9x12-envelope", "na-10x13-envelope", "na-10x14-envelope",
            "na-10x15-envelope", "na-5x7", "na-8x10",};

    private static final List mediaStringList = Arrays.asList(mediaStringTable);

    private static final Media[] mediaValueTable = { MediaName.NA_LETTER_WHITE,
            MediaName.NA_LETTER_TRANSPARENT, MediaName.ISO_A4_WHITE,
            MediaName.ISO_A4_TRANSPARENT, MediaTray.TOP, MediaTray.MIDDLE,
            MediaTray.BOTTOM, MediaTray.MANUAL, MediaTray.LARGE_CAPACITY,
            MediaTray.MAIN, MediaTray.SIDE, MediaSizeName.ISO_A0,
            MediaSizeName.ISO_A1, MediaSizeName.ISO_A2, MediaSizeName.ISO_A3,
            MediaSizeName.ISO_A4, MediaSizeName.ISO_A5, MediaSizeName.ISO_A6,
            MediaSizeName.ISO_A7, MediaSizeName.ISO_A8, MediaSizeName.ISO_A9,
            MediaSizeName.ISO_A10, MediaSizeName.ISO_B0, MediaSizeName.ISO_B1,
            MediaSizeName.ISO_B2, MediaSizeName.ISO_B3, MediaSizeName.ISO_B4,
            MediaSizeName.ISO_B5, MediaSizeName.ISO_B6, MediaSizeName.ISO_B7,
            MediaSizeName.ISO_B8, MediaSizeName.ISO_B9, MediaSizeName.ISO_B10,
            MediaSizeName.JIS_B0, MediaSizeName.JIS_B1, MediaSizeName.JIS_B2,
            MediaSizeName.JIS_B3, MediaSizeName.JIS_B4, MediaSizeName.JIS_B5,
            MediaSizeName.JIS_B6, MediaSizeName.JIS_B7, MediaSizeName.JIS_B8,
            MediaSizeName.JIS_B9, MediaSizeName.JIS_B10, MediaSizeName.ISO_C0,
            MediaSizeName.ISO_C1, MediaSizeName.ISO_C2, MediaSizeName.ISO_C3,
            MediaSizeName.ISO_C4, MediaSizeName.ISO_C5, MediaSizeName.ISO_C6,
            MediaSizeName.NA_LETTER, MediaSizeName.NA_LEGAL,
            MediaSizeName.EXECUTIVE, MediaSizeName.LEDGER,
            MediaSizeName.TABLOID, MediaSizeName.INVOICE, MediaSizeName.FOLIO,
            MediaSizeName.QUARTO, MediaSizeName.JAPANESE_POSTCARD,
            MediaSizeName.JAPANESE_DOUBLE_POSTCARD, MediaSizeName.A,
            MediaSizeName.B, MediaSizeName.C, MediaSizeName.D, MediaSizeName.E,
            MediaSizeName.ISO_DESIGNATED_LONG, MediaSizeName.ITALY_ENVELOPE,
            MediaSizeName.MONARCH_ENVELOPE, MediaSizeName.PERSONAL_ENVELOPE,
            MediaSizeName.NA_NUMBER_9_ENVELOPE,
            MediaSizeName.NA_NUMBER_10_ENVELOPE,
            MediaSizeName.NA_NUMBER_11_ENVELOPE,
            MediaSizeName.NA_NUMBER_12_ENVELOPE,
            MediaSizeName.NA_NUMBER_14_ENVELOPE, MediaSizeName.NA_6X9_ENVELOPE,
            MediaSizeName.NA_7X9_ENVELOPE, MediaSizeName.NA_9X11_ENVELOPE,
            MediaSizeName.NA_9X12_ENVELOPE, MediaSizeName.NA_10X13_ENVELOPE,
            MediaSizeName.NA_10X14_ENVELOPE, MediaSizeName.NA_10X15_ENVELOPE,
            MediaSizeName.NA_5X7, MediaSizeName.NA_8X10,};

    private static final String[] printQualityStringTable = { "draft",
            "normal", "high"};

    private static final List printQualityStringList = Arrays.asList(printQualityStringTable);
    
    private static final PrintQuality[] printQualityValueTable = {
            PrintQuality.DRAFT, PrintQuality.NORMAL, PrintQuality.HIGH};

    private static String toString(Attribute attr) {
        return attr != null ? attr.toString() : "*";
    }

    private static Attribute toAttribute(String s, List stringList, Attribute[] valueTable) {
        try {
            return "*".equals(s) ? null : valueTable[stringList.indexOf(s)];
        } catch (IndexOutOfBoundsException e) {
            throw new IllegalArgumentException(s);
        }
    }
    
    private String printerName = null;

    private Chromaticity chromaticity = null;

    private Media media = null;

    private PrintQuality printQuality = null;
    
    private boolean useExternalPrintUtility = false;
    
    private String printUtilityCommandLine = "acroread /t/h/p %s";

    private int labelFileAvailabilityTime = 10;
        
    public final String getPrintUtilityCommandLine() {
        return printUtilityCommandLine;
    }
    
    public final void setPrintUtilityCommandLine(String externalCommand) {
        this.printUtilityCommandLine = externalCommand;
    }
    
    public final boolean isUseExternalPrintUtility() {
        return useExternalPrintUtility;
    }
    
    public final void setUseExternalPrintUtility(boolean useExternalCommand) {
        this.useExternalPrintUtility = useExternalCommand;
    }
    
    
    public final int getLabelFileAvailabilityTime() {
        return labelFileAvailabilityTime;
    }
    
    public final void setLabelFileAvailabilityTime(int time) {
        this.labelFileAvailabilityTime = time;
    }

    public final String getChromaticity() {
        return toString(chromaticity);
    }

    public final void setChromaticity(String s) {
        this.chromaticity = (Chromaticity) toAttribute(s, chromaticityStringList, chromaticityValueTable);
    }

    public final String getMedia() {
        return toString(media);
    }

    public final void setMedia(String media) {
        this.media = (Media) toAttribute(media, mediaStringList, mediaValueTable);
    }

    public final String getPrinterName() {
        return printerName != null ? printerName : "*";
    }

    public final void setPrinterName(String printerName) {
        this.printerName = "*".equals(printerName) ? null : printerName;
    }

    public final String getPrintQuality() {
        return toString(printQuality);
    }

    public final void setPrintQuality(String printQuality) {
        this.printQuality = (PrintQuality) toAttribute(printQuality, printQualityStringList, printQualityValueTable);
    }

    private PrintService lookupPrintService(DocFlavor flavor)
            throws PrintException {
        PrintService[] pservices = PrintServiceLookup
                .lookupPrintServices(flavor, null);
        if (pservices.length == 0) { throw new PrintException(
                flavor == null ? "No print service available"
                        : ("No print service available for " + flavor)); }
        if (printerName == null) return pservices[0];
        for (int i = 0; i < pservices.length; i++) {
            if (printerName.equalsIgnoreCase(pservices[i].getName()))
                    return pservices[i];
        }
        log.warn("No such Printer: " + printerName + " - use: "
                + pservices[0].getName());
        return pservices[0];
    }

    public void print(MediaCreationRequest rq) throws IOException, PrintException {
        log.info("Prepare printing Label for " + rq);
        File file = rq.getLabelFile();
        String fpath = file.getPath();
        if (useExternalPrintUtility) {
            String cmd = printUtilityCommandLine.replaceFirst("%s", fpath);
            Executer exe = new Executer(cmd);
            try {
                int exit = exe.waitFor();
                if (exit != 0)
                    throw new PrintException(cmd + " exit with " + exit);
            } catch (InterruptedException e1) {
                throw new PrintException(cmd + " throws " + e1);
            }
            if (labelFileAvailabilityTime > 0)
                try {
                    Thread.sleep(labelFileAvailabilityTime * 1000L);
                } catch (InterruptedException e2) {
                    log.warn("Wait after Invoke of Print Utility was interrupted:", e2);
                }
        }
        DocFlavor flavor = fpath.endsWith(".ps") ? DocFlavor.INPUT_STREAM.POSTSCRIPT
                : fpath.endsWith(".pdf") ? DocFlavor.INPUT_STREAM.PDF : null;
        PrintService printer = lookupPrintService(flavor);
        DocPrintJob pj = printer.createPrintJob();
        BufferedInputStream in = new BufferedInputStream(new FileInputStream(
                file));
        try {
            PrintRequestAttributeSet attrs = new HashPrintRequestAttributeSet();
            if (chromaticity != null) attrs.add(chromaticity);
            if (media != null) attrs.add(media);
            if (printQuality != null) attrs.add(printQuality);
            AttributeSet unsupported = printer.getUnsupportedAttributes(flavor,
                    attrs);
            if (unsupported != null) {
                log.warn("Ignore unsupported attributes " + unsupported
                        + " for printing " + file + " to " + printer);
                Attribute[] a = unsupported.toArray();
                for (int i = 0; i < a.length; i++) {
                    attrs.remove(a[i]);
                }
            }
            Doc doc = new SimpleDoc(in, flavor, null);
            pj.print(doc, attrs);
        } catch (PrintException e) {
            log.error("Failed to print " + file + " to " + printer, e);
            throw e;
        } finally {
            in.close();
        }
    }

    public String listAvailablePrinters() {
        return toString(PrintServiceLookup.lookupPrintServices(null,
                        null));
    }

    public String listSupportedChromaticity() throws PrintException {
        return listSupportedValues(Chromaticity.class);
    }

    public String listSupportedMedia() throws PrintException {
        return listSupportedValues(Media.class);
    }

    public String listSupportedPrintQuality() throws PrintException {
        return listSupportedValues(PrintQuality.class);
    }

    public String listSupportedDocFlavors() throws PrintException {
        return toString(lookupPrintService(null)
                        .getSupportedDocFlavors());
    }

    private String toString(Object[] a) {
        if (a.length == 0)
            return "";
        StringBuffer sb = new StringBuffer(a[0].toString());
        for (int i = 1; i < a.length; i++) {
            sb.append('\n').append(a[i]);
        }
        return sb.toString();
    }

    private String listSupportedValues(Class category) throws PrintException {
        Object values = lookupPrintService(null)
                .getSupportedAttributeValues(category, null, null);
        try {
            return toString((Object[]) values);
        } catch (Exception e) {
            return "" + values;
        }
    }
}