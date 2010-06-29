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
 * Portions created by the Initial Developer are Copyright (C) 2009-2010
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Babu Hussain A
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
package in.raster.mayam.delegate;

import in.raster.mayam.context.ApplicationContext;
import in.raster.mayam.form.MainScreen;
import in.raster.mayam.model.Instance;
import in.raster.mayam.model.Series;
import in.raster.mayam.model.Study;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import org.dcm4che.data.Dataset;
import org.dcm4che.dict.Tags;
import org.dcm4che.imageio.plugins.DcmMetadata;
import org.dcm4che2.data.Tag;

/**
 *
 * @author  BabuHussain
 * @version 0.5
 *
 */
public class SeriesListUpdator extends Thread {

    Study tempStudy = null;
    boolean studyAlreadyPresent = false;
    private String studyUID;
    private String seriesUID;
    private boolean skipStudyParsing;

    public SeriesListUpdator() {
    }

    public SeriesListUpdator(String studyUID, String seriesUID, boolean skipStudyParsing) {
        this.studyUID = studyUID;
        this.seriesUID = seriesUID;
        this.skipStudyParsing = skipStudyParsing;
        this.start();
    }

    @Override
    public void run() {
        addSeriesToStudyList(studyUID, seriesUID, skipStudyParsing);
    }

    /**
     * This routine used to update the series object to the studylist array
     * @param studyUID
     * @param seriesUID
     * @param skipStudyParsing
     */
    public void addSeriesToStudyList(String studyUID, String seriesUID, boolean skipStudyParsing) {

        boolean seriesAlreadyPresent = false;
        if (!skipStudyParsing) {
            findStudyFromStudyList(studyUID);
        }

        if (tempStudy != null) {
            for (Series series : tempStudy.getSeriesList()) {
                if (series.getSeriesInstanceUID().equalsIgnoreCase(seriesUID)) {
                    seriesAlreadyPresent = true;
                }
            }
        }
        if (!seriesAlreadyPresent) {
            Series series = ApplicationContext.databaseRef.getSeries(studyUID, seriesUID);
            if (!studyAlreadyPresent) {
                tempStudy = new Study(studyUID);
                MainScreen.studyList.add(tempStudy);
            }
            tempStudy.addSeries(series);
            try {
                for (Instance img : series.getImageList()) {
                    readDicomFile(img);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * This routine used to parse the study list
     * @param studyUID
     */
    public void findStudyFromStudyList(String studyUID) {
        studyAlreadyPresent = false;
        for (Study study : MainScreen.studyList) {
            if (study.getStudyInstanceUID().equalsIgnoreCase(studyUID)) {
                tempStudy = study;
                studyAlreadyPresent = true;
            }
        }
    }

    private void readDicomFile(Instance img) {
        ImageInputStream iis = null;
        Iterator iter = null;
        ImageReader reader = null;
        Dataset dataset;
        try {
            File selFile = new File(System.getProperty("user.dir") + File.separator + img.getFilepath());
            if (!selFile.isFile()) {
                selFile = new File(img.getFilepath());
            }
            iis = ImageIO.createImageInputStream(selFile);
            iter = ImageIO.getImageReadersByFormatName("DICOM");
            reader = (ImageReader) iter.next();
            reader.setInput(iis, false);
            dataset = ((DcmMetadata) reader.getStreamMetadata()).getDataset();
            try {
                img.setPixelData(reader.read(0));
                img.setInstance_no(dataset.getString(Tags.InstanceNumber));
                String frameOfReferenceUID = dataset.getString(Tags.FrameOfReferenceUID) != null ? dataset.getString(Tags.FrameOfReferenceUID) : "";
                String imagePosition = dataset.getString(Tags.ImagePosition, 0) != null ? dataset.getString(Tags.ImagePosition, 0) + "\\" + dataset.getString(Tags.ImagePosition, 1) + "\\" + dataset.getString(Tags.ImagePosition, 2) : null;
                String imageOrientation = dataset.getString(Tags.ImageOrientation) != null ? dataset.getString(Tags.ImageOrientation, 0) + "\\" + dataset.getString(Tags.ImageOrientation, 1) + "\\" + dataset.getString(Tags.ImageOrientation, 2) + "\\" + dataset.getString(Tags.ImageOrientation, 3) + "\\" + dataset.getString(Tags.ImageOrientation, 4) + "\\" + dataset.getString(Tags.ImageOrientation, 5) : null;
                String[] imageType = dataset.getStrings(Tags.ImageType) != null ? dataset.getStrings(Tags.ImageType) : null;
                String pixelSpacing = dataset.getString(Tags.PixelSpacing) != null ? dataset.getString(Tags.PixelSpacing, 0) + "\\" + dataset.getString(Tags.PixelSpacing, 1) : null;
                int row = dataset.getString(Tags.Rows) != null ? Integer.parseInt(dataset.getString(Tags.Rows)) : 0;
                int column = dataset.getString(Tags.Columns) != null ? Integer.parseInt(dataset.getString(Tags.Columns)) : 0;
                String referencedSOPInstanceUID = "";
                Dataset referencedImageSequence = dataset.getItem(Tag.ReferencedImageSequence);
                if (referencedImageSequence != null) {
                    referencedSOPInstanceUID = referencedImageSequence.getString(Tag.ReferencedSOPInstanceUID);
                }
                img.setImagePosition(imagePosition);
                img.setImageOrientation(imageOrientation);
                img.setImageType(imageType);
                img.setPixelSpacing(pixelSpacing);
                img.setRow(row);
                img.setColumn(column);
                img.setReferenceSOPInstanceUID(referencedSOPInstanceUID);
                img.setFrameOfReferenceUID(frameOfReferenceUID);
            } catch (NullPointerException e) {
            } catch (RuntimeException e) {
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                iis.close();
                iter = null;
                reader.dispose();
            } catch (IOException ex) {
                ex.printStackTrace();
                Logger.getLogger(StudyListUpdator.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
