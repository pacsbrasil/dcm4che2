/*$Id$*/
/*****************************************************************************
 *                                                                           *
 *  Copyright (c) 2001,2001 by TIANI MEDGRAPH AG <gunter.zeilinger@tiani.com>*
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

package org.dcm4che.srom;

import org.dcm4che.data.Dataset;
import java.util.Date;

/**
 * The <code>KeyObject</code> interface represents a
 * <i>DICOM Key Object Selection Document</i>.
 * <br>
 * The <i>Key Object Selection Document</i> is intended for 
 * flagging one or more significant images, waveforms, or other 
 * composite SOP Instances.
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 0.9.9
 *
 * @see "DICOM Part 3: Information Object Definitions,
 * Annex A.35.4 Key Object Selection Document Information Object Definition"
 *
 * @see "DICOM Part 3: Information Object Definitions,
 * Annex C.17.6 Key Object Selection Modules"
 */
public interface KeyObject extends ContainerContent {
    // Constants -----------------------------------------------------
    
    // Public --------------------------------------------------------

    /** 
     * Returns <code>Patient</code> contained in this document.
     * @return <code>Patient</code> contained in this document.
     */
    public Patient getPatient();

    /** 
     * Replaces <code>Patient</code> contained in this document
     * by <code>newPatient</code>.
     *
     * @param newPatient new <code>Patient</code>.
     *
     * @see SRDocumentFactory#createPatient
     */
    public void setPatient(Patient newPatient);
    
    /** 
     * Returns <code>Study</code> contained in this document.
     *
     * @return <code>Study</code> contained in this document.
     */
    public Study getStudy();

    /** 
     * Replaces <code>Study</code> contained in this document
     * by <code>newStudy</code>.
     *
     * @param newStudy new <code>Study</code>.
     *
     * @see SRDocumentFactory#createStudy
     */
    public void setStudy(Study newStudy);
    
    /** 
     * Returns <code>Series</code> contained in this document.
     * <p>According <i>DICOM</i>, you shall set a new {@link #setSeries Series}
     * and {@link #setSOPInstanceUID SOP Instance UID} of the
     * document, when changing the <code>Study</code>.
     *
     * @return <code>Series</code> contained in this document.
     */
    public Series getSeries();

    /** 
     * Replaces <code>Series</code> contained in this document
     * by <code>newSeries</code>.
     * <p>According <i>DICOM</i>, you shall set a new {@link #setSOPInstanceUID
     * SOP Instance UID} of the document, when changing the
     * <code>Series</code>.
     *
     * @param newSeries new <code>Series</code>.
     * @throws IllegalArgumentException if <code>Modality</code> is not
     * <code>"SR"</code>.
     *
     * @see SRDocumentFactory#createSeries
     */
    public void setSeries(Series newSeries);
    
    /** 
     * Returns <code>Equipment</code> contained in this
     * document. The <code>Equipment</code> is backed by this
     * document, so changes to this <code>Equipment</code> are
     * reflected in the document.
     *
     * @return <code>Equipment</code> contained in this document.
     */
    public Equipment getEquipment();

    /** 
     * Replaces <code>Equipment</code> contained in this
     * document by <code>newEquipment</code>.
     * <p>Clones <code>newEquipment</code>, so following changes to
     * <code>newEquipment</code> are NOT reflected in the
     * document.
     *
     * @param newEquipment new <code>Equipment</code>.
     *
     * @see SRDocumentFactory#createEquipment
     */
    public void setEquipment(Equipment newEquipment);

    /** 
     * Returns <i>SOP Class UID</i> of this document.
     * <i>SOP Class UID</i> is a readonly attribute of document.
     *
     * @return <i>SOP Class UID</i> of this document.
     */
    public String getSOPClassUID();
    
    /** 
     * Returns <i>SOP Instance UID</i> of this document.
     *
     * @return <i>SOP Class UID</i> of this document.
     */
    public String getSOPInstanceUID();

    /** 
     * Sets new <i>SOP Instance UID</i> of this document.
     *
     * @param newSOPInstanceUID new <i>SOP Instance UID</i>.
     */
    public void setSOPInstanceUID(String newSOPInstanceUID);

    /**
     * Returns <i>Specific Character Set</i> for data values of this document.
     *
     * @return <i>Specific Character Set</i> code string.
     */
    public String getSpecificCharacterSet();
    
    /** 
     * Sets <i>Specific Character Set</i> for data values of this document.
     *
     * @parm charset <i>Specific Character Set</i> code string.
     */
    public void setSpecificCharacterSet(String charset);

    /** 
     * Returns <i>Instance Creation DateTime</i> of this document.
     *
     * @return <i>Instance Creation DateTime</i>.
     */
    public Date getInstanceCreationDateTime();
    
    /** 
     * Sets <i>Instance Creation DateTime</i> of this document.
     *
     * @parm dateTime new <i>Instance Creation DateTime</i>.
     */
    public void setInstanceCreationDateTime(Date dateTime);
    
    /**
     * Returns <i>Instance Creator UID</i> of this document.
     *
     * @return <i>Instance Creation DateTime</i>.
     */
    public String getInstanceCreatorUID();

    /** 
     * Sets <i>Instance Creator UID</i> of this document.
     *
     * @parm uid new <i>Instance Creator UID</i>.
     */
    public void setInstanceCreatorUID(String uid);

    /** 
     * Returns <i>Instance Number</i> of this document.
     *
     * @return <i>Instance Number</i>.
     */
    public int getInstanceNumber();

    /** 
     * Sets <i>Instance Number</i> of this document.
     *
     * @parm no new <i>Instance Number</i>.
     */
    public void setInstanceNumber(int no);

    /** 
     * Returns <i>Content DateTime</i> of this document.
     *
     * @return <i>Content DateTime</i>.
     */
    public Date getContentDateTime();

    /** 
     * Sets <i>Content DateTime</i> of this document.
     *
     * @parm dateTime new <i>Content DateTime</i>.
     */
    public void setContentDateTime(Date dateTime);

    /**
     * Returns array of <code>Request</code> objects, identifing
     * <i>Requested Procedures</i> which are being fulfilled by creation of
     * this document.
     *
     * @return <i>Requested Procedures</i> references. 
     * @throws NullPointerException if <code>ref</code> is <code>null</code>.
     */
    public Request[] getRequests();
    
    /**
     * Sets array of <code>Request</code> objects, identifing
     * <i>Requested Procedures</i> which are being fulfilled by creation of
     * this document.
     *
     * @param requests new array of <code>Request</code> objects. 
     */
    public void setRequests(Request[] requests);

    /**
     * Adds a new <code>Request</code> object, identifing a
     * <i>Requested Procedure</i> which is being fulfilled by creation of
     * this document. Ignores the new <code>Request</code> object, if the
     * document already refers an equal <i>Requested Procedure</i>.
     *
     * @param request identify one <i>Requested Procedure</i>.
     * @return <code>true</code> if <code>request</code> was added,
     *   <code>false</code> if the document already contained the reference.
     *
     * @see SRDocumentFactory#createRequest
     */
    public boolean addRequest(Request request);
    
    /**
     * Removes the specified <code>Request</code> object, from the list
     * of <i>Requested Procedures</i> of this document.
     *
     * @return <code>true</code> if the <i>Requested Procedures</i> list of this
     *   document contained <code>request</code>; <code>false</code> otherwise.
     * @throws NullPointerException if <code>request</code> is
     *   <code>null</code>.
     *
     * @see SRDocumentFactory#createRequest
     */
    public boolean removeRequest(Request request);    

    /**
     * Returns array of <code>SOPInstanceRef</code> objects, refering all
     * <i>Current Requested Procedure Evidences</i> of this document.
     *
     * @return references to <i>Current Requested Procedure Evidences</i>. 
     * @throws NullPointerException if <code>code</code> is <code>null</code>.
     */
    public SOPInstanceRef[] getCurrentEvidence();
    
    /**
     * Returns <code>SOPInstanceRef</code> objects, refering the specified
     * <i>Current Requested Procedure Evidence</i>. 
     *
     * @return reference to <i>Current Requested Procedure Evidences</i>,
     *   <code>null</code> if no reference to <code>refSOP</code> was found.
     * @throws NullPointerException if <code>ref</code> is <code>null</code>.
     */
    public SOPInstanceRef findCurrentEvidence(RefSOP refSOP);
    
    /**
     * Sets array of <code>SOPInstanceRef</code> objects, refering all
     * <i>Current Requested Procedure Evidences</i> of this document.
     *
     * @param refs new <i>Current Requested Procedure Evidence</i> references. 
     *
     * @see SRDocumentFactory#createSOPInstanceRef
     */
    public void setCurrentEvidence(SOPInstanceRef[] refs);
    
    /**
     * Adds a new <code>SOPInstanceRef</code> object, refering a
     * <i>Current Requested Procedure Evidence</i> of this document. 
     * Ignores the new <code>SOPInstanceRef</code> object, if the 
     * document already refers an equal 
     * <i>Current Requested Procedure Evidence</i>.
     *
     * @param ref reference to <i>Current Requested Procedure Evidence</i>.
     * @return <code>true</code> if <code>ref</code> was added,
     *   <code>false</code> if the document already contained the reference.
     *
     * @see SRDocumentFactory#createSOPInstanceRef
     */
    public boolean addCurrentEvidence(SOPInstanceRef ref);
    
    /** 
     * Removes the specified <code>SOPInstanceRef</code> object, from the list
     * of <i>Current Requested Procedure Evidences</i> of this document.
     *
     * @return <code>true</code> if the document contained the reference,
     *   <code>false</code> otherwise.
     * @throws NullPointerException if <code>ref</code> is <code>null</code>.
     */
    public boolean removeCurrentEvidence(RefSOP ref);

    /**
     * Returns array of <code>SOPInstanceRef</code> objects, refering all
     * <i>Identical Documents</i> of this document.
     *
     * @return references to <i>Identical Documents</i>. 
     * @throws NullPointerException if <code>ref</code> is <code>null</code>.
     */
    public SOPInstanceRef[] getIdenticalDocuments();
    
    /**
     * Sets array of <code>SOPInstanceRef</code> objects, refering all
     * <i>Identical Documents</i> of this document.
     *
     * @param refs new <i>Identical Documents</i> references. 
     */
    public void setIdenticalDocuments(SOPInstanceRef[] refs);

    /**
     * Adds a new <code>SOPInstanceRef</code> object, refering a
     * <i>Identical Document</i> of this document. Ignores the new
     * <code>SOPInstanceRef</code> object, if the document already
     * refers an equal <i>Identical Document</i>.
     *
     * @param ref reference to <i>Identical Document</i>.
     * @return <code>true</code> if <code>ref</code> was added,
     *   <code>false</code> if the document already contained the reference.
     *
     * @see SRDocumentFactory#createSOPInstanceRef
     */
    public boolean addIdenticalDocument(SOPInstanceRef ref);
    
    /**
     * Removes the specified <code>SOPInstanceRef</code> object, from the list
     * of <i>Predecessor Document</i> of this document.
     *
     * @return <code>true</code> if the document contained the reference,
     *   <code>false</code> otherwise.
     * @throws NullPointerException if <code>ref</code> is <code>null</code>.
     *
     * @see SRDocumentFactory#createSOPInstanceRef
     */
    public boolean removeIdenticalDocument(RefSOP ref);

    /**
     * Returns <code>Content</code> with specified
     * <i>Content Item Identifier</i>. Returns <code>null</code>, if this
     * document does not contain a <i>Content Item</i> with the specified
     * <i>Content Item Identifier</i>.
     *
     * @param id <i>Content Item Identifier</i>.
     * @return <code>Content</code> with specified
     *   <i>Content Item Identifier</i>,
     *   <code>null</code> if there is no such <code>Content</code>.
     * @throws NullPointerException if <code>id</code> is <code>null</code>.
     * @throws IllegalArgumentException if <code>id</code> is not a valid
     *   <i>Content Item Identifier</i>.
     */    
    public Content getContent(int[] id);
    
//    public RelationConstraints getRelationConstraints();
    
    /**
     * Creates new <code>ContainerContent</code> with specified properties.
     *
     * @param obsDateTime <i>Observation DateTime</i> or <code>null</code>.
     * @param template <i>Content Template</i> or <code>null</code>.
     * @param name <i>Section Heading</i> or <code>null</code>.
     * @param separate specifies <i>Continuity of Content</i>.
     * @return created <code>ContainerContent</code> object.
     */
    public ContainerContent createContainerContent(
            Date obsDateTime, Template template, Code name, boolean separate);
    
    /**
     * Creates new <code>TextContent</code> with specified properties.
     *
     * @param obsDateTime <i>Observation DateTime</i> or <code>null</code>.
     * @param template <i>Content Template</i> or <code>null</code>.
     * @param name <i>Concept Name Code</i>.
     * @param text <i>Text Value</i> string.
     * @return created <code>TextContent</code> object.
     * @throws NullPointerException if <code>name</code> or <code>text</code>
     *   are <code>null</code>.
     * @throws IllegalArgumentException if <code>text</code> is an empty
     *   string.
     *
     * @see SRDocumentFactory#createCode
     * @see SRDocumentFactory#createTemplate
     */    
    public TextContent createTextContent(
            Date obsDateTime, Template template, Code name, String text);
    
    /**
     * Creates new <code>PNameContent</code> with specified properties.
     *
     * @param obsDateTime <i>Observation DateTime</i> or <code>null</code>.
     * @param template <i>Content Template</i> or <code>null</code>.
     * @param name <i>Concept Name Code</i>.
     * @param pname <i>Person Name</i> string.
     * @return created <code>PNameContent</code> object.
     * @throws NullPointerException if <code>name</code> or <code>pname</code>
     *   are <code>null</code>.
     * @throws IllegalArgumentException if <code>pname</code> is an empty
     *   string.
     *
     * @see SRDocumentFactory#createCode
     * @see SRDocumentFactory#createTemplate
     */    
    public PNameContent createPNameContent(
            Date obsDateTime, Template template, Code name, String pname);

    /**
     * Creates new <code>UIDRefContent</code> with specified properties.
     *
     * @param obsDateTime <i>Observation DateTime</i> or <code>null</code>.
     * @param template <i>Content Template</i> or <code>null</code>.
     * @param name <i>Concept Name Code</i>.
     * @param uid <i>UID</i> string.
     * @return created <code>UIDRefContent</code> object.
     * @throws NullPointerException if <code>name</code> or <code>uid</code>
     *   are <code>null</code>.
     * @throws IllegalArgumentException if <code>uid</code> is an empty
     *   string.
     */    
    public UIDRefContent createUIDRefContent(
            Date obsDateTime, Template template, Code name, String uid);

    /**
     * Creates new <code>CodeContent</code> with specified properties.
     *
     * @param obsDateTime <i>Observation DateTime</i> or <code>null</code>.
     * @param template <i>Content Template</i> or <code>null</code>.
     * @param name <i>Concept Name Code</i>.
     * @param code <i>Concept Code</i>.
     * @return created <code>CodeContent</code> object.
     * @throws NullPointerException if <code>name</code> or <code>code</code>
     *   are <code>null</code>.
     *
     * @see SRDocumentFactory#createCode
     * @see SRDocumentFactory#createTemplate
     */    
    public CodeContent createCodeContent(
            Date obsDateTime, Template template, Code name, Code code);

    /**
     * Creates new <code>NumContent</code> with specified properties.
     *
     * @param obsDateTime <i>Observation DateTime</i> or <code>null</code>.
     * @param template <i>Content Template</i> or <code>null</code>.
     * @param name <i>Concept Name Code</i>.
     * @param value <i>Numeric Value</i> float.
     * @param unit <i>Measurement Unit Code</i>.
     * @return created <code>NumContent</code> object.
     * @throws NullPointerException if <code>name</code> or <code>unit</code>
     *   are <code>null</code>.
     *
     * @see SRDocumentFactory#createCode
     * @see SRDocumentFactory#createTemplate
     */    
    public NumContent createNumContent(
            Date obsDateTime, Template template, Code name,
            float value, Code unit);

    /**
     * Creates new <code>DateContent</code> with specified properties.
     *
     * @param obsDateTime <i>Observation DateTime</i> or <code>null</code>.
     * @param template <i>Content Template</i> or <code>null</code>.
     * @param name <i>Concept Name Code</i>.
     * @param date <i>Date</i> value.
     * @return created <code>DateContent</code> object.
     * @throws NullPointerException if <code>name</code> or <code>date</code>
     *   are <code>null</code>.
     *
     * @see SRDocumentFactory#createCode
     * @see SRDocumentFactory#createTemplate
     */    
    public DateContent createDateContent(
            Date obsDateTime, Template template, Code name, Date date);

    /**
     * Creates new <code>TimeContent</code> with specified properties.
     *
     * @param obsDateTime <i>Observation DateTime</i> or <code>null</code>.
     * @param template <i>Content Template</i> or <code>null</code>.
     * @param name <i>Concept Name Code</i>.
     * @param time <i>Time</i> value.
     * @return created <code>TimeContent</code> object.
     * @throws NullPointerException if <code>name</code> or <code>time</code>
     *   are <code>null</code>.
     *
     * @see SRDocumentFactory#createCode
     * @see SRDocumentFactory#createTemplate
     */    
    public TimeContent createTimeContent(
            Date obsDateTime, Template template, Code name, Date time);

    /**
     * Creates new <code>TimeContent</code> with specified properties.
     *
     * @param obsDateTime <i>Observation DateTime</i> or <code>null</code>.
     * @param template <i>Content Template</i> or <code>null</code>.
     * @param name <i>Concept Name Code</i>.
     * @param dateTime <i>DateTime</i> value.
     * @return created <code>TimeContent</code> object.
     * @throws NullPointerException if <code>name</code> or
     *   <code>dateTime</code> are <code>null</code>.
     *
     * @see SRDocumentFactory#createCode
     * @see SRDocumentFactory#createTemplate
     */    
    public DateTimeContent createDateTimeContent(
            Date obsDateTime, Template template, Code name, Date dateTime);

    /**
     * Creates new <code>CompositeContent</code> with specified properties.
     *
     * @param obsDateTime <i>Observation DateTime</i> or <code>null</code>.
     * @param template <i>Content Template</i> or <code>null</code>.
     * @param name <i>Concept Name Code</i> or <code>null</code>.
     * @param refSOP <i>Composite Object Reference</i>.
     * @return created <code>CompositeContent</code> object.
     * @throws NullPointerException if <code>refSOP</code> is <code>null</code>.
     *
     * @see SRDocumentFactory#createCode
     * @see SRDocumentFactory#createTemplate
     * @see SRDocumentFactory#createRefSOP
     */    
    public CompositeContent createCompositeContent(
            Date obsDateTime, Template template, Code name, RefSOP refSOP);

    /**
     * Creates new <code>ImageContent</code> with specified properties.
     *
     * @param obsDateTime <i>Observation DateTime</i> or <code>null</code>.
     * @param template <i>Content Template</i> or <code>null</code>.
     * @param name <i>Concept Name Code</i> or <code>null</code>.
     * @param refSOP <i>Composite Object Reference</i>.
     * @param frameNumbers <i>Referenced Frame Number(s)</i> or
     *   <code>null</code>.
     * @param refPresentationSOP <i>Softcopy Presentation State Reference</i>
     *   or <code>null</code>.
     * @return created <code>ImageContent</code> object.
     * @throws NullPointerException if <code>refSOP</code> is <code>null</code>.
     *
     * @see SRDocumentFactory#createCode
     * @see SRDocumentFactory#createTemplate
     * @see SRDocumentFactory#createRefSOP
     */    
    public ImageContent createImageContent(
            Date obsDateTime, Template template, Code name,
            RefSOP refSOP, int[] frameNumbers, RefSOP refPresentationSOP);

    /**
     * Creates new <code>WaveformContent</code> with specified properties.
     *
     * @param obsDateTime <i>Observation DateTime</i> or <code>null</code>.
     * @param template <i>Content Template</i> or <code>null</code>.
     * @param name <i>Concept Name Code</i> or <code>null</code>.
     * @param refSOP <i>Composite Object Reference</i>.
     * @param channelNumbers <i>Referenced Channel Number(s)</i> or
     *   <code>null</code>.
     * @return created <code>WaveformContent</code> object.
     * @throws NullPointerException if <code>refSOP</code> is <code>null</code>.
     * @throws IllegalArgumentException if <code>channelNumbers</code> contains
     *   an odd number of ints.
     *
     * @see SRDocumentFactory#createCode
     * @see SRDocumentFactory#createTemplate
     * @see SRDocumentFactory#createRefSOP
     */    
    public WaveformContent createWaveformContent(
            Date obsDateTime, Template template, Code name,
            RefSOP refSOP, int[] channelNumbers);

    /**
     * Creates new <code>SCoordContent.Point</code> with specified properties.
     *
     * @param obsDateTime <i>Observation DateTime</i> or <code>null</code>.
     * @param template <i>Content Template</i> or <code>null</code>.
     * @param name <i>Concept Name Code</i> or <code>null</code>.
     * @param graphicData <code>float[2]</code> with (column,row) value.
     * @return created <code>SCoordContent.Point</code> object.
     * @throws NullPointerException if <code>graphicData</code> is
     *   <code>null</code>.
     * @throws IllegalArgumentException if <code>graphicData</code> contains
     *   more or less than 2 floats.
     */    
    public SCoordContent.Point createPointSCoordContent(
            Date obsDateTime, Template template, Code name,
            float[] graphicData);

    /**
     * Creates new <code>SCoordContent.MultiPoint</code> with specified
     * properties.
     *
     * @param obsDateTime <i>Observation DateTime</i> or <code>null</code>.
     * @param template <i>Content Template</i> or <code>null</code>.
     * @param name <i>Concept Name Code</i> or <code>null</code>.
     * @param graphicData <code>float[2N]</code> with N (column,row) values.
     * @return created <code>SCoordContent.MultiPoint</code> object.
     * @throws NullPointerException if <code>graphicData</code> is
     *   <code>null</code>.
     * @throws IllegalArgumentException if <code>graphicData</code> contains
     *   an odd number of floats.
     *
     * @see SRDocumentFactory#createCode
     * @see SRDocumentFactory#createTemplate
     */    
    public SCoordContent.MultiPoint createMultiPointSCoordContent(
            Date obsDateTime, Template template, Code name,
            float[] graphicData);

    /**
     * Creates new <code>SCoordContent.Polyline</code> with specified
     * properties.
     *
     * @param obsDateTime <i>Observation DateTime</i> or <code>null</code>.
     * @param template <i>Content Template</i> or <code>null</code>.
     * @param name <i>Concept Name Code</i> or <code>null</code>.
     * @param graphicData <code>float[2N]</code> with N (column,row) values.
     * @return created <code>SCoordContent.Polyline</code> object.
     * @throws NullPointerException if <code>graphicData</code> is
     *   <code>null</code>.
     * @throws IllegalArgumentException if <code>graphicData</code> contains
     *   an odd number of floats.
     *
     * @see SRDocumentFactory#createCode
     * @see SRDocumentFactory#createTemplate
     */    
    public SCoordContent.Polyline createPolylineSCoordContent(
            Date obsDateTime, Template template, Code name,
            float[] graphicData);

    /**
     * Creates new <code>SCoordContent.Circle</code> with specified
     * properties.
     *
     * @param obsDateTime <i>Observation DateTime</i> or <code>null</code>.
     * @param template <i>Content Template</i> or <code>null</code>.
     * @param name <i>Concept Name Code</i> or <code>null</code>.
     * @param graphicData <code>float[4]</code> with (column,row) of central
     *   pixel, followed by (column,row) of pixel on the perimeter of the circle.
     * @return created <code>SCoordContent.Circle</code> object.
     * @throws NullPointerException if <code>graphicData</code> is
     *   <code>null</code>.
     * @throws IllegalArgumentException if <code>graphicData</code> contains
     *   more or less than 4 floats.
     *
     * @see SRDocumentFactory#createCode
     * @see SRDocumentFactory#createTemplate
     */    
    public SCoordContent.Circle createCircleSCoordContent(
            Date obsDateTime, Template template, Code name,
            float[] graphicData);

    /**
     * Creates new <code>SCoordContent.Ellipse</code> with specified
     * properties.
     *
     * @param obsDateTime <i>Observation DateTime</i> or <code>null</code>.
     * @param template <i>Content Template</i> or <code>null</code>.
     * @param name <i>Concept Name Code</i> or <code>null</code>.
     * @param graphicData <code>float[8]</code> with 4 (column,row) values,
     *   specifying the endpoints of the major axis followed by the endpoints
     *   of the minor axis of the ellipse.
     * @return created <code>SCoordContent.Ellipse</code> object.
     * @throws NullPointerException if <code>graphicData</code> is
     *   <code>null</code>.
     * @throws IllegalArgumentException if <code>graphicData</code> contains
     *   more or less than 8 floats.
     *
     * @see SRDocumentFactory#createCode
     * @see SRDocumentFactory#createTemplate
     */    
    public SCoordContent.Ellipse createEllipseSCoordContent(
            Date obsDateTime, Template template, Code name,
            float[] graphicData);

    /**
     * Creates new <code>TCoordContent.Point</code> with specified properties.
     *
     * @param obsDateTime <i>Observation DateTime</i> or <code>null</code>.
     * @param template <i>Content Template</i> or <code>null</code>.
     * @param name <i>Concept Name Code</i> or <code>null</code>.
     * @param positions <code>TCoordContent.Positions</code> with one entry.
     * @return created <code>TCoordContent.Point</code> object.
     * @throws NullPointerException if <code>positions</code> is
     *   <code>null</code>.
     * @throws IllegalArgumentException if <code>positions</code> contains
     *   more or less than 1 entry.
     *
     * @see SRDocumentFactory#createCode
     * @see SRDocumentFactory#createTemplate
     * @see SRDocumentFactory#createSamplePositions
     * @see SRDocumentFactory#createRelativePositions
     * @see SRDocumentFactory#createAbsolutePositions
     */    
    public TCoordContent.Point createPointTCoordContent(
            Date obsDateTime, Template template, Code name,
            TCoordContent.Positions positions);

    /**
     * Creates new <code>TCoordContent.MultiPoint</code> with specified
     * properties.
     *
     * @param obsDateTime <i>Observation DateTime</i> or <code>null</code>.
     * @param template <i>Content Template</i> or <code>null</code>.
     * @param name <i>Concept Name Code</i> or <code>null</code>.
     * @param positions <code>TCoordContent.Positions</code> with one entry.
     * @return created <code>TCoordContent.MultiPoint</code> object.
     * @throws NullPointerException if <code>positions</code> is
     *   <code>null</code>.
     *
     * @see SRDocumentFactory#createCode
     * @see SRDocumentFactory#createTemplate
     * @see SRDocumentFactory#createSamplePositions
     * @see SRDocumentFactory#createRelativePositions
     * @see SRDocumentFactory#createAbsolutePositions
     */    
    public TCoordContent.MultiPoint createMultiPointTCoordContent(
            Date obsDateTime, Template template, Code name,
            TCoordContent.Positions positions);

    /**
     * Creates new <code>TCoordContent.Segment</code> with specified
     * properties.
     *
     * @param obsDateTime <i>Observation DateTime</i> or <code>null</code>.
     * @param template <i>Content Template</i> or <code>null</code>.
     * @param name <i>Concept Name Code</i> or <code>null</code>.
     * @param positions <code>TCoordContent.Positions</code> with two entries.
     * @return created <code>TCoordContent.Segment</code> object.
     * @throws NullPointerException if <code>positions</code> is
     *   <code>null</code>.
     * @throws IllegalArgumentException if <code>positions</code> contains
     *   more or less than two entries.
     *
     * @see SRDocumentFactory#createCode
     * @see SRDocumentFactory#createTemplate
     * @see SRDocumentFactory#createSamplePositions
     * @see SRDocumentFactory#createRelativePositions
     * @see SRDocumentFactory#createAbsolutePositions
     */    
    public TCoordContent.Segment createSegmentTCoordContent(
            Date obsDateTime, Template template, Code name,
            TCoordContent.Positions positions);

    /**
     * Creates new <code>TCoordContent.MultiSegment</code> with specified
     * properties.
     *
     * @param obsDateTime <i>Observation DateTime</i> or <code>null</code>.
     * @param template <i>Content Template</i> or <code>null</code>.
     * @param name <i>Concept Name Code</i> or <code>null</code>.
     * @param positions <code>TCoordContent.Positions</code> with 2N entries.
     * @return created <code>TCoordContent.MultiSegment</code> object.
     * @throws NullPointerException if <code>positions</code> is
     *   <code>null</code>.
     * @throws IllegalArgumentException if <code>positions</code> contains
     *   an odd number of entries.
     *
     * @see SRDocumentFactory#createCode
     * @see SRDocumentFactory#createTemplate
     * @see SRDocumentFactory#createSamplePositions
     * @see SRDocumentFactory#createRelativePositions
     * @see SRDocumentFactory#createAbsolutePositions
     */    
    public TCoordContent.MultiSegment createMultiSegmentTCoordContent(
            Date obsDateTime, Template template, Code name,
            TCoordContent.Positions positions);

    /**
     * Creates new <code>TCoordContent.Begin</code> with specified
     * properties.
     *
     * @param obsDateTime <i>Observation DateTime</i> or <code>null</code>.
     * @param template <i>Content Template</i> or <code>null</code>.
     * @param name <i>Concept Name Code</i> or <code>null</code>.
     * @param positions <code>TCoordContent.Positions</code> with one entry.
     * @return created <code>TCoordContent.Begin</code> object.
     * @throws NullPointerException if <code>positions</code> is
     *   <code>null</code>.
     * @throws IllegalArgumentException if <code>positions</code> contains
     *   more or less than one entry.
     *
     * @see SRDocumentFactory#createCode
     * @see SRDocumentFactory#createTemplate
     * @see SRDocumentFactory#createSamplePositions
     * @see SRDocumentFactory#createRelativePositions
     * @see SRDocumentFactory#createAbsolutePositions
     */    
    public TCoordContent.Begin createBeginTCoordContent(
            Date obsDateTime, Template template, Code name,
            TCoordContent.Positions positions);

    /**
     * Creates new <code>TCoordContent.End</code> with specified
     * properties.
     *
     * @param obsDateTime <i>Observation DateTime</i> or <code>null</code>.
     * @param template <i>Content Template</i> or <code>null</code>.
     * @param name <i>Concept Name Code</i> or <code>null</code>.
     * @param positions <code>TCoordContent.Positions</code> with one entry.
     * @return created <code>TCoordContent.End</code> object.
     * @throws NullPointerException if <code>positions</code> is
     *   <code>null</code>.
     * @throws IllegalArgumentException if <code>positions</code> contains
     *   more or less than one entry.
     *
     * @see SRDocumentFactory#createCode
     * @see SRDocumentFactory#createTemplate
     * @see SRDocumentFactory#createSamplePositions
     * @see SRDocumentFactory#createRelativePositions
     * @see SRDocumentFactory#createAbsolutePositions
     */    
    public TCoordContent.End createEndTCoordContent(
            Date obsDateTime, Template template, Code name,
            TCoordContent.Positions positions);

    /**
     * Creates new <code>ReferencedContent</code> which refers the specified
     * <code>Content</code>.
     *
     * @param refContent referenced <code>Content</code>.
     * @return created <code>ReferencedContent</code> object.
     * @throws NullPointerException if <code>refContent</code> is
     *   <code>null</code>.
     * @throws IllegalArgumentException if <code>refContent</code> is not
     *   in the content tree of this document, or if <code>refContent</code> is
     *   a <code>ReferencedContent</code>.
     */    
    public ReferencedContent createReferencedContent(Content refContent);

    /**
     * Creates new <code>ReferencedContent</code> which refers the specified
     * <code>Content</code>. The referenced <code>Content</code> may not yet be
     * inserted into the content tree of this document at creation time. But
     * {@link ReferencedContent#getRefContent} will fail, throwing a
     * <code>NoSuchElementException</code>, as long the document does not
     * contain a <code>Content</code> with the specified <i>Content Item
     * Identifier</i>.
     *
     * @param refContentId <i>Content Item Identifier</i> of referenced
     *   <code>Content</code>.
     * @return created <code>ReferencedContent</code> object.
     * @throws NullPointerException if <code>refContentId</code> is
     *   <code>null</code>.
     * @throws IllegalArgumentException if <code>refContentId</code> is not a
     *   valid <i>Content Item Identifier</i>.
     */    
    public ReferencedContent createReferencedContent(int[] refContentId);    
    
    /**
     * Imports content from another document to this document. The returned 
     * content has no parent.
     * <p>The <i>Observation DateTime</i> of the imported content is explicitly
     * set to {@link Content#getObservationDateTime
     * importedContent.getObservationDateTime(true)}.
     *
     * @param importedContent The content to import.
     * @param deep If <code>true</code>, recursively imports the subtree under 
     *   the specified content; if <code>false</code>, imports only the content 
     *   itself.
     * @return The imported content.
     * @throws NullPointerException if <code>importedContent</code> is
     *   <code>null</code>.
     *
     * @see Content#clone
     */
    public Content importContent(Content importedContent, boolean deep);
    
    public Dataset toDataset();
}
