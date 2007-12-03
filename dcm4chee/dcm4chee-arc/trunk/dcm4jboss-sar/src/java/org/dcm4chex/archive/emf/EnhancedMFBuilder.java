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
 * The Original Code is part of dcm4che, an implementation of DICOM(TM) in
 * Java(TM), available at http://sourceforge.net/projects/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * Agfa-Gevaert Group.
 * Portions created by the Initial Developer are Copyright (C) 2003-2005
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * See @authors listed below.
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

package org.dcm4chex.archive.emf;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Iterator;

import javax.xml.transform.Templates;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;

import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmElement;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.data.DcmParser;
import org.dcm4che.data.DcmParserFactory;
import org.dcm4che.data.DcmValueException;
import org.dcm4che.data.FileFormat;
import org.dcm4che.dict.Tags;
import org.dcm4che.dict.UIDs;
import org.dcm4che.util.UIDGenerator;
import org.dcm4chex.archive.exceptions.ConfigurationException;
import org.jboss.logging.Logger;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @version $Revision$ $Date$
 * @since Nov 27, 2007
 */
class EnhancedMFBuilder {

    private static final int[] FG_SEQ_TAGS = new int[] {
            Tags.SharedFunctionalGroupsSeq, Tags.PerFrameFunctionalGroupsSeq };

    private final SAXTransformerFactory tf = (SAXTransformerFactory) 
            TransformerFactory.newInstance();
    private final Logger log;
    private final Templates tpl;
    private final int frameTypeTag;
    private final boolean noPixelData;
    private final boolean deflate;
    private File f0;
    private Dataset dataset;
    private int pixelDataVR;
    private int pixelDataLength;
    private int curFrame = 0;
    private final long[] pixelDataOffsets;
    private final int[] pixelDataLengths;


    public EnhancedMFBuilder(UpgradeToEnhancedMFService service,
            Templates tpl, int frameTypeTag, int numFrames) {
        this.log = service.getLog();
        this.noPixelData = service.isNoPixelData();
        this.deflate = service.isDeflate();
        this.tpl = tpl;
        this.frameTypeTag = frameTypeTag;
        this.pixelDataOffsets = new long[numFrames];
        this.pixelDataLengths = new int[numFrames];
     }

    public final int getPixelDataVR() {
        return pixelDataVR;
    }

    public int getPixelDataLength() {
        return pixelDataLength == -1 ? -1 
                : pixelDataLength * pixelDataOffsets.length;
    }

    public long getPixelDataOffset(int i) {
        return pixelDataOffsets[i];
    }

    public int getPixelDataLength(int i) {
        return pixelDataLengths[i];
    }

    public void add(File f) throws UpgradeToEnhancedMFException, IOException {
        if (curFrame == pixelDataOffsets.length) {
            throw new IllegalStateException("curFrame: " + curFrame
                    + " == numFrame: " + pixelDataOffsets.length);
        }
        if (log.isDebugEnabled()) {
            log.debug("M-READ " + f);
        }
        Dataset newFrame = DcmObjectFactory.getInstance().newDataset();
        BufferedInputStream bis = new BufferedInputStream(
                new FileInputStream(f));
        DcmParser parser = DcmParserFactory.getInstance().newDcmParser(bis);
        try {
            TransformerHandler th = tf.newTransformerHandler(tpl);
            parser.setSAXHandler2(th, null, null, Integer.MAX_VALUE, null);
            th.setResult(new SAXResult(newFrame.getSAXHandler2(null)));
            parser.parseDcmFile(FileFormat.DICOM_FILE, Tags.PixelData);
            if (parser.getReadTag() != Tags.PixelData) {
                throw new UpgradeToEnhancedMFException("No Pixel Data in " + f);
            }
            if (curFrame == 0) {
                pixelDataVR = parser.getReadVR();
                pixelDataLength = parser.getReadLength();
            } else {
                if (pixelDataVR != parser.getReadVR()) {
                    throw new UpgradeToEnhancedMFException("VR of Pixel Data in "
                            + f + " differs from " + f0);
                }
                if (pixelDataLength != parser.getReadLength()) {
                    throw new UpgradeToEnhancedMFException("Pixel Data Length in "
                            + f + " differs from " + f0);
                }
            }
            if (pixelDataLength == -1) {
                // skip frame offset table
                parser.parseHeader();
                if (parser.getReadLength() != 0) {
                    throw new UpgradeToEnhancedMFException(
                            "Non-empty Frame Offset table in " + f);
                }
                parser.parseHeader();
            }
            pixelDataOffsets[curFrame] = parser.getStreamPosition();
            pixelDataLengths[curFrame] = parser.getReadLength();
        } catch (TransformerConfigurationException e) {
            throw new ConfigurationException(e);
        } finally {
            try { bis.close(); } catch (IOException ignore) {}
        }
        purgeEmptyItems(newFrame.get(Tags.SharedFunctionalGroupsSeq).getItem());
        if (dataset == null) {
            if (log.isDebugEnabled()) {
                log.debug("Create new Enhanced MF from " + f);
            }
            dataset = newFrame;
            f0 = f;
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Merge Functional Groups to Enhanced MF from " + f);
            }
            if (!noPixelData
                    && !dataset.getFileMetaInfo().getTransferSyntaxUID()
                            .equals(newFrame.getFileMetaInfo()
                                            .getTransferSyntaxUID())) {
                throw new UpgradeToEnhancedMFException("Transfer Syntax of "
                        + f + " differs from " + f0);
            }
            if (!dataset.exclude(FG_SEQ_TAGS).equals(
                    newFrame.exclude(FG_SEQ_TAGS))) {
                throw new UpgradeToEnhancedMFException(
                        "Non Functional Groups elements of "  + f 
                        + " differs from " + f0);
            }
            mergeFunctionalGroups(dataset, newFrame);
        }
        ++curFrame;
    }

    private void purgeEmptyItems(Dataset ds) {
        for (Iterator iterator = ds.iterator(); iterator.hasNext();) {
            DcmElement el = (DcmElement) iterator.next();
            if (el.isEmpty() || el.countItems() == 1 && el.getItem().isEmpty()) {
                iterator.remove();
            }
        }
    }

    private void mergeFunctionalGroups(Dataset dataset, Dataset newframe) {
        Dataset sharedFGs =
                dataset.get(Tags.SharedFunctionalGroupsSeq).getItem();
        Dataset newSharedFGs =
                newframe.get(Tags.SharedFunctionalGroupsSeq).getItem();
        Dataset newPerFrameFGs =
                newframe.get(Tags.PerFrameFunctionalGroupsSeq).getItem();
        int[] noLongerSharedFGTags = {};
        for (Iterator sharedFGsIter = sharedFGs.iterator();
                sharedFGsIter.hasNext();) {
            DcmElement sharedFGSeq = (DcmElement) sharedFGsIter.next();
            int fgTag = sharedFGSeq.tag();
            if (sharedFGSeq.equals(newSharedFGs.get(fgTag))) {
                newSharedFGs.remove(fgTag);
            } else {
                noLongerSharedFGTags = append(noLongerSharedFGTags, fgTag);
            } 
        }
        DcmElement perFrameFGSeq =
            dataset.get(Tags.PerFrameFunctionalGroupsSeq);
        if (noLongerSharedFGTags.length != 0) {
            Dataset noLongerSharedFGs = sharedFGs.subSet(noLongerSharedFGTags);
            for (int i = 0, n = perFrameFGSeq.countItems(); i < n; i++) {
                perFrameFGSeq.getItem(i).putAll(noLongerSharedFGs);
            }
            noLongerSharedFGs.clear();
        }
        newPerFrameFGs.putAll(newSharedFGs);
        perFrameFGSeq.addItem(newPerFrameFGs);
    }

    private int[] append(int[] a, int i) {
        int[] tmp = new int[a.length + 1];
        System.arraycopy(a, 0, tmp, 0, a.length);
        tmp[a.length] = i;
        return tmp;
    }

    public Dataset build() throws UpgradeToEnhancedMFException {
        if (curFrame < pixelDataOffsets.length) {
            throw new IllegalStateException("curFrame: " + curFrame
                    + " < numFrame: " + pixelDataOffsets.length);
        }
        initImageTypeAndAcquisitionDatetime();
        dataset.putIS(Tags.NumberOfFrames, pixelDataOffsets.length);
        UIDGenerator uidGen = UIDGenerator.getInstance();
        if (!dataset.containsValue(Tags.SeriesInstanceUID)) {
            dataset.putUI(Tags.SeriesInstanceUID, uidGen.createUID());
        }
        dataset.putUI(Tags.SOPInstanceUID, uidGen.createUID());
        try {
            dataset.setFileMetaInfo(
                    DcmObjectFactory.getInstance().newFileMetaInfo(dataset,
                            noPixelData
                                    ? (deflate ? UIDs.NoPixelDataDeflate
                                               : UIDs.NoPixelData)
                                    : dataset.getFileMetaInfo()
                                             .getTransferSyntaxUID()));
        } catch (DcmValueException e) {
            throw new UpgradeToEnhancedMFException(e);
        }
        return dataset;
    }

    private void initImageTypeAndAcquisitionDatetime() {
        Dataset sharedFGs = dataset.getItem(Tags.SharedFunctionalGroupsSeq);
        Dataset sharedFrameTypeFG = sharedFGs.getItem(frameTypeTag);
        DcmElement perFrameFGsSeq = dataset.get(Tags.PerFrameFunctionalGroupsSeq);
        Dataset perFrameFGs = perFrameFGsSeq.getItem(0);
        Dataset frameContentFG = perFrameFGs.getItem(Tags.FrameContentSeq);
        String[] imageType = (sharedFrameTypeFG != null 
                ? sharedFrameTypeFG : perFrameFGs.getItem(frameTypeTag))
                .getStrings(Tags.FrameType);
        String instNo = frameContentFG.getString(Tags.InstanceNumber);
        Date contentDateTime = frameContentFG.getDateTime(Tags.ContentDate,
                Tags.ContentTime);
        Date acquistionDateTime = frameContentFG.getDate(
                Tags.FrameAcquisitionDatetime);
        Date d;
        for (int i = 1, n = perFrameFGsSeq.countItems(); i < n; i++) {
            perFrameFGs = perFrameFGsSeq.getItem(i);
            frameContentFG = perFrameFGs.getItem(Tags.FrameContentSeq);
            if (sharedFrameTypeFG == null) {
                imageType = mergeImageType(imageType, perFrameFGs
                        .getItem(frameTypeTag).getStrings(Tags.FrameType));
            }
            d = frameContentFG.getDateTime(Tags.ContentDate,
                    Tags.ContentTime);
            if (d != null && (contentDateTime == null 
                    || d.compareTo(contentDateTime) < 0)) {
                contentDateTime = d;
                instNo = frameContentFG.getString(Tags.InstanceNumber);
            }
            d = frameContentFG.getDate(Tags.FrameAcquisitionDatetime);
            if (d != null && (acquistionDateTime == null 
                    || d.compareTo(acquistionDateTime) < 0)) {
                acquistionDateTime = d;
            }
        }
        dataset.putCS(Tags.ImageType, imageType);
        dataset.putCS(Tags.InstanceNumber, instNo);
        dataset.putDA(Tags.ContentDate, contentDateTime);
        dataset.putTM(Tags.ContentTime, contentDateTime);
        dataset.putDT(Tags.AcquisitionDatetime, acquistionDateTime);
    }

    private String[] mergeImageType(String[] t1, String[] t2) {
        if (t1.length < t2.length) {
            String[] tmp = t1;
            t1 = t2;
            t2 = tmp;
        }
        for (int i = 0; i < t2.length; i++) {
            if (!t1[i].equals(t2[i])) {
                t1[i] = "MIXED";
            }
        }
        return t1;
    }
}
