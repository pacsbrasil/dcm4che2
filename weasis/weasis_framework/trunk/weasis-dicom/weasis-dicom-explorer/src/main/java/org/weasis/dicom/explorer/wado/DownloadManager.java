/*******************************************************************************
 * Copyright (c) 2010 Nicolas Roduit.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Nicolas Roduit - initial API and implementation
 ******************************************************************************/
package org.weasis.dicom.explorer.wado;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.zip.GZIPInputStream;

import javax.xml.XMLConstants;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Source;
import javax.xml.transform.stax.StAXSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.core.api.explorer.model.TreeModel;
import org.weasis.core.api.gui.util.AbstractProperties;
import org.weasis.core.api.gui.util.GuiExecutor;
import org.weasis.core.api.media.MimeInspector;
import org.weasis.core.api.media.data.MediaSeriesGroup;
import org.weasis.core.api.media.data.MediaSeriesGroupNode;
import org.weasis.core.api.media.data.Series;
import org.weasis.core.api.media.data.TagElement;
import org.weasis.core.api.util.FileUtil;
import org.weasis.core.api.util.GzipManager;
import org.weasis.core.ui.docking.UIManager;
import org.weasis.core.ui.editor.image.ViewerPlugin;
import org.weasis.dicom.codec.DicomInstance;
import org.weasis.dicom.codec.DicomSeries;
import org.weasis.dicom.codec.DicomVideoSeries;
import org.weasis.dicom.codec.wado.WadoParameters;
import org.weasis.dicom.explorer.DicomModel;
import org.weasis.dicom.explorer.Messages;
import org.xml.sax.SAXException;

public class DownloadManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(DownloadManager.class);

    public DownloadManager() {
    }

    public static ArrayList<LoadSeries> buildDicomSeriesFromXml(URI uri, final DicomModel model) {
        ArrayList<LoadSeries> seriesList = new ArrayList<LoadSeries>();
        XMLStreamReader xmler = null;
        InputStream stream = null;
        try {
            XMLInputFactory xmlif = XMLInputFactory.newInstance();
            URL url = uri.toURL();
            LOGGER.info("Downloading WADO references: {}", url); //$NON-NLS-1$

            String path = uri.getPath();
            if (path.endsWith(".gz")) { //$NON-NLS-1$
                stream = GzipManager.gzipUncompressToStream(url);
            } else if (path.endsWith(".xml")) { //$NON-NLS-1$
                stream = url.openStream();
            } else {
                // In case wado file has no extension
                File outFile = File.createTempFile("wado_", "", AbstractProperties.APP_TEMP_DIR); //$NON-NLS-1$ //$NON-NLS-2$
                if (FileUtil.writeFile(url, outFile)) {
                    String mime = MimeInspector.getMimeType(outFile);
                    if (mime != null && mime.equals("application/x-gzip")) { //$NON-NLS-1$
                        stream = new BufferedInputStream((new GZIPInputStream(new FileInputStream((outFile)))));
                    } else {
                        stream = url.openStream();
                    }
                }
            }
            File tempFile = File.createTempFile("wado_", ".xml", AbstractProperties.APP_TEMP_DIR); //$NON-NLS-1$ //$NON-NLS-2$
            FileUtil.writeFile(stream, new FileOutputStream(tempFile));
            xmler = xmlif.createXMLStreamReader(new FileReader(tempFile));

            Source xmlFile = new StAXSource(xmler);
            SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            try {
                Schema schema = schemaFactory.newSchema(DownloadManager.class.getResource("/config/wado_query.xsd"));//$NON-NLS-1$ 
                Validator validator = schema.newValidator();
                validator.validate(xmlFile);
                LOGGER.info("[Validate with XSD schema] wado_query is valid"); //$NON-NLS-1$
            } catch (SAXException e) {
                LOGGER.error("[Validate with XSD schema] wado_query is NOT valid"); //$NON-NLS-1$
                LOGGER.error("Reason: {}", e.getLocalizedMessage()); //$NON-NLS-1$
            } catch (Exception e) {
                LOGGER.error("Error when validate XSD schema. Try to update JRE"); //$NON-NLS-1$
                e.printStackTrace();
            }
            // Try to read the xml even it is not valid.
            xmler = xmlif.createXMLStreamReader(new FileReader(tempFile));
            int eventType;
            if (xmler.hasNext()) {
                eventType = xmler.next();
                switch (eventType) {
                    case XMLStreamConstants.START_ELEMENT:
                        String key = xmler.getName().getLocalPart();
                        // First Tag must <wado_query>
                        if (WadoParameters.TAG_DOCUMENT_ROOT.equals(key)) {
                            String wadoURL = getTagAttribute(xmler, WadoParameters.TAG_WADO_URL, null);
                            boolean onlySopUID =
                                Boolean.valueOf(getTagAttribute(xmler, WadoParameters.TAG_WADO_ONLY_SOP_UID, "false")); //$NON-NLS-1$
                            String additionnalParameters =
                                getTagAttribute(xmler, WadoParameters.TAG_WADO_ADDITIONNAL_PARAMETERS, ""); //$NON-NLS-1$
                            String overrideList = getTagAttribute(xmler, WadoParameters.TAG_WADO_OVERRIDE_TAGS, null);
                            String webLogin = getTagAttribute(xmler, WadoParameters.TAG_WADO_WEB_LOGIN, null); //$NON-NLS-1$
                            final WadoParameters wadoParameters =
                                new WadoParameters(wadoURL, onlySopUID, additionnalParameters, overrideList, webLogin);
                            int pat = 0;
                            MediaSeriesGroup patient = null;
                            while (xmler.hasNext()) {
                                eventType = xmler.next();
                                switch (eventType) {
                                    case XMLStreamConstants.START_ELEMENT:
                                        // <Patient> Tag
                                        if (TagElement.DICOM_LEVEL.Patient.name()
                                            .equals(xmler.getName().getLocalPart())) {
                                            patient = readPatient(model, seriesList, xmler, wadoParameters);
                                            pat++;
                                        }
                                        break;
                                    default:
                                        break;
                                }
                            }
                            if (pat == 1) {
                                // In case of the patient already exists, select it
                                final MediaSeriesGroup uniquePatient = patient;
                                GuiExecutor.instance().execute(new Runnable() {

                                    public void run() {
                                        synchronized (UIManager.VIEWER_PLUGINS) {
                                            for (final ViewerPlugin p : UIManager.VIEWER_PLUGINS) {
                                                if (uniquePatient.equals(p.getGroupID())) {
                                                    p.setSelectedAndGetFocus();
                                                    break;
                                                }
                                            }
                                        }
                                    }
                                });
                            }
                            for (LoadSeries loadSeries : seriesList) {
                                String modality = (String) loadSeries.getDicomSeries().getTagValue(TagElement.Modality);
                                boolean ps = modality != null && ("PR".equals(modality) || "KO".equals(modality)); //$NON-NLS-1$ //$NON-NLS-2$
                                if (!ps) {
                                    loadSeries.startDownloadImageReference(wadoParameters);
                                }
                            }
                        }
                        break;
                    default:
                        break;
                }
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (XMLStreamException e) {
            e.printStackTrace();
        } finally {
            FileUtil.safeClose(xmler);
            FileUtil.safeClose(stream);
        }
        return seriesList;
    }

    private static MediaSeriesGroup readPatient(DicomModel model, ArrayList<LoadSeries> seriesList,
        XMLStreamReader xmler, WadoParameters wadoParameters) throws XMLStreamException {
        // PatientID, PatientBirthDate, StudyInstanceUID, SeriesInstanceUID and SOPInstanceUID override
        // the tags located in DICOM object (because original DICOM can contain different values after merging
        // patient or study
        String patientID = getTagAttribute(xmler, TagElement.PatientID.getTagName(), ""); //$NON-NLS-1$
        String patientBirthDate = getTagAttribute(xmler, TagElement.PatientBirthDate.getTagName(), ""); //$NON-NLS-1$
        String name =
            getTagAttribute(xmler, TagElement.PatientName.getTagName(), Messages.getString("DownloadManager.unknown")); //$NON-NLS-1$

        // TODO set preferences of building patientPseudoUID
        String patientPseudoUID = patientID + patientBirthDate;
        MediaSeriesGroup patient = model.getHierarchyNode(TreeModel.rootNode, patientPseudoUID);
        if (patient == null) {
            patient = new MediaSeriesGroupNode(TagElement.PatientPseudoUID, patientPseudoUID, TagElement.PatientName);
            patient.setTag(TagElement.PatientID, patientID);
            if (name.trim().equals("")) { //$NON-NLS-1$
                name = Messages.getString("DownloadManager.unknown"); //$NON-NLS-1$
            }
            name = name.replace("^", " "); //$NON-NLS-1$ //$NON-NLS-2$
            patient.setTag(TagElement.PatientName, name);

            patient.setTag(TagElement.PatientSex, getTagAttribute(xmler, TagElement.PatientSex.getTagName(), "O")); //$NON-NLS-1$
            patient.setTag(TagElement.PatientBirthDate, TagElement.getDicomDate(patientBirthDate));
            patient.setTag(TagElement.PatientBirthTime,
                TagElement.getDicomTime(getTagAttribute(xmler, TagElement.PatientBirthTime.getTagName(), null)));
            model.addHierarchyNode(TreeModel.rootNode, patient);
            LOGGER.info("Adding new patient: " + patient); //$NON-NLS-1$
        }

        int eventType;
        boolean state = true;
        while (xmler.hasNext() && state) {
            eventType = xmler.next();
            switch (eventType) {
                case XMLStreamConstants.START_ELEMENT:
                    // <Study> Tag
                    if (TagElement.DICOM_LEVEL.Study.name().equals(xmler.getName().getLocalPart())) {
                        readStudy(model, seriesList, xmler, patient, wadoParameters);
                    }
                    break;
                case XMLStreamConstants.END_ELEMENT:
                    if (TagElement.DICOM_LEVEL.Patient.name().equals(xmler.getName().getLocalPart())) {
                        state = false;
                    }
                    break;
                default:
                    break;
            }
        }
        return patient;
    }

    private static MediaSeriesGroup readStudy(DicomModel model, ArrayList<LoadSeries> seriesList,
        XMLStreamReader xmler, MediaSeriesGroup patient, WadoParameters wadoParameters) throws XMLStreamException {
        String studyUID = getTagAttribute(xmler, TagElement.StudyInstanceUID.getTagName(), ""); //$NON-NLS-1$
        MediaSeriesGroup study = model.getHierarchyNode(patient, studyUID);
        if (study == null) {
            study = new MediaSeriesGroupNode(TagElement.StudyInstanceUID, studyUID, TagElement.StudyDate);
            study.setTag(TagElement.StudyDate,
                TagElement.getDicomDate(getTagAttribute(xmler, TagElement.StudyDate.getTagName(), null)));
            study.setTag(TagElement.StudyTime,
                TagElement.getDicomTime(getTagAttribute(xmler, TagElement.StudyTime.getTagName(), null)));
            study.setTag(TagElement.StudyDescription,
                getTagAttribute(xmler, TagElement.StudyDescription.getTagName(), "")); //$NON-NLS-1$
            study.setTag(TagElement.AccessionNumber,
                getTagAttribute(xmler, TagElement.AccessionNumber.getTagName(), null));
            study.setTag(TagElement.StudyID, getTagAttribute(xmler, TagElement.StudyID.getTagName(), null));

            model.addHierarchyNode(patient, study);
        }

        int eventType;
        boolean state = true;
        while (xmler.hasNext() && state) {
            eventType = xmler.next();
            switch (eventType) {
                case XMLStreamConstants.START_ELEMENT:
                    // <Series> Tag
                    if (TagElement.DICOM_LEVEL.Series.name().equals(xmler.getName().getLocalPart())) {
                        readSeries(model, seriesList, xmler, patient, study, wadoParameters);
                    }
                    break;
                case XMLStreamConstants.END_ELEMENT:
                    if (TagElement.DICOM_LEVEL.Study.name().equals(xmler.getName().getLocalPart())) {
                        state = false;
                    }
                    break;
                default:
                    break;
            }
        }
        return study;
    }

    private static Series readSeries(DicomModel model, ArrayList<LoadSeries> seriesList, XMLStreamReader xmler,
        MediaSeriesGroup patient, MediaSeriesGroup study, WadoParameters wadoParameters) throws XMLStreamException {

        String seriesUID = getTagAttribute(xmler, TagElement.SeriesInstanceUID.getTagName(), ""); //$NON-NLS-1$

        Series dicomSeries = (Series) model.getHierarchyNode(study, seriesUID);
        if (dicomSeries == null) {
            dicomSeries = new DicomSeries(seriesUID);
            dicomSeries.setTag(TagElement.ExplorerModel, model);
            dicomSeries.setTag(TagElement.SeriesInstanceUID, seriesUID);
            dicomSeries.setTag(TagElement.SeriesNumber,
                getIntegerTagAttribute(xmler, TagElement.SeriesNumber.getTagName(), null));
            dicomSeries.setTag(TagElement.Modality, getTagAttribute(xmler, TagElement.Modality.getTagName(), null));
            dicomSeries.setTag(TagElement.SeriesDescription,
                getTagAttribute(xmler, TagElement.SeriesDescription.getTagName(), "")); //$NON-NLS-1$
            dicomSeries.setTag(TagElement.ReferringPhysicianName,
                getTagAttribute(xmler, TagElement.ReferringPhysicianName.getTagName(), null));
            dicomSeries.setTag(TagElement.WadoTransferSyntaxUID,
                getTagAttribute(xmler, TagElement.WadoTransferSyntaxUID.getTagName(), null));
            dicomSeries.setTag(TagElement.WadoCompressionRate,
                getIntegerTagAttribute(xmler, TagElement.WadoCompressionRate.getTagName(), 0));
            dicomSeries.setTag(TagElement.WadoParameters, wadoParameters);
            dicomSeries.setTag(TagElement.WadoInstanceReferenceList, new ArrayList<DicomInstance>());
            model.addHierarchyNode(study, dicomSeries);
        } else {
            WadoParameters wado = (WadoParameters) dicomSeries.getTagValue(TagElement.WadoParameters);
            if (wado == null) {
                // Should not happen
                dicomSeries.setTag(TagElement.WadoParameters, wadoParameters);
            } else if (!wado.getWadoURL().equals(wadoParameters.getWadoURL())) {
                LOGGER.error("Wado parameters must be unique for a DICOM Series: {}", dicomSeries); //$NON-NLS-1$
                // Cannot have multiple wado parameter for a Series
                return dicomSeries;
            }
        }

        List<DicomInstance> dicomInstances =
            (List<DicomInstance>) dicomSeries.getTagValue(TagElement.WadoInstanceReferenceList);
        boolean containsInstance = false;
        if (dicomInstances == null) {
            dicomSeries.setTag(TagElement.WadoInstanceReferenceList, new ArrayList<DicomInstance>());
        } else if (dicomInstances.size() > 0) {
            containsInstance = true;
        }

        int eventType;
        boolean state = true;
        while (xmler.hasNext() && state) {
            eventType = xmler.next();
            switch (eventType) {
                case XMLStreamConstants.START_ELEMENT:
                    // <Instance> Tag
                    if (TagElement.DICOM_LEVEL.Instance.name().equals(xmler.getName().getLocalPart())) {
                        String sopInstanceUID = getTagAttribute(xmler, TagElement.SOPInstanceUID.getTagName(), null);
                        if (sopInstanceUID != null) {
                            if (containsInstance && dicomInstances.contains(sopInstanceUID)) {
                                LOGGER.warn("DICOM instance {} already exists, abort downloading.", sopInstanceUID); //$NON-NLS-1$
                            } else {
                                String tsuid = getTagAttribute(xmler, TagElement.TransferSyntaxUID.getTagName(), null);
                                DicomInstance dcmInstance = new DicomInstance(sopInstanceUID, tsuid);
                                dcmInstance.setInstanceNumber(getIntegerTagAttribute(xmler,
                                    TagElement.InstanceNumber.getTagName(), -1));
                                dicomInstances.add(dcmInstance);
                            }
                        }

                    }
                    break;
                case XMLStreamConstants.END_ELEMENT:
                    if (TagElement.DICOM_LEVEL.Series.name().equals(xmler.getName().getLocalPart())) {
                        state = false;
                    }
                    break;
                default:
                    break;
            }
        }

        if (dicomInstances.size() > 0) {
            // TODO Manage video if tsuid is not present
            if (dicomInstances.size() == 1
                && "1.2.840.10008.1.2.4.100".equals(dicomInstances.get(0).getTransferSyntaxUID())) { //$NON-NLS-1$
                model.removeHierarchyNode(study, dicomSeries);
                dicomSeries = new DicomVideoSeries((DicomSeries) dicomSeries);
                model.addHierarchyNode(study, dicomSeries);
            }

            String modality = (String) dicomSeries.getTagValue(TagElement.Modality);
            boolean ps = modality != null && ("PR".equals(modality) || "KO".equals(modality)); //$NON-NLS-1$ //$NON-NLS-2$
            final LoadSeries loadSeries = new LoadSeries(dicomSeries, model);

            Integer sn = (Integer) (ps ? Integer.MAX_VALUE : dicomSeries.getTagValue(TagElement.SeriesNumber));
            DownloadPriority priority =
                new DownloadPriority((String) patient.getTagValue(TagElement.PatientName),
                    (String) study.getTagValue(TagElement.StudyInstanceUID),
                    (Date) study.getTagValue(TagElement.StudyDate), sn);
            loadSeries.setPriority(priority);
            seriesList.add(loadSeries);
        }
        return dicomSeries;
    }

    private static String getTagAttribute(XMLStreamReader xmler, String attribute, String defaultValue) {
        if (attribute != null) {
            String val = xmler.getAttributeValue(null, attribute);
            if (val != null) {
                return val;
            }
        }
        return defaultValue;
    }

    private static Integer getIntegerTagAttribute(XMLStreamReader xmler, String attribute, Integer defaultValue) {
        if (attribute != null) {
            try {
                String val = xmler.getAttributeValue(null, attribute);
                if (val != null) {
                    return Integer.valueOf(val);
                }
            } catch (NumberFormatException e) {
            }
        }
        return defaultValue;
    }

}
