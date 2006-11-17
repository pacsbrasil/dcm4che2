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
 * Java(TM), hosted at http://sourceforge.net/projects/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * Gunter Zeilinger, Huetteldorferstr. 24/10, 1150 Vienna/Austria/Europe.
 * Portions created by the Initial Developer are Copyright (C) 2002-2005
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Gunter Zeilinger <gunterze@gmail.com>
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

package org.dcm4che2.iod.module.composite;

import org.dcm4che2.data.BasicDicomObject;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.VR;
import org.dcm4che2.iod.module.Module;
import org.dcm4che2.iod.value.PixelRepresentation;

/**
 * @author gunter zeilinger(gunterze@gmail.com)
 * @version $Revision$ $Date$
 * @since Jun 11, 2006
 *
 */
public class ImagePixel extends Module {

    public ImagePixel(DicomObject dcmobj) {
        super(dcmobj);
    }

    public ImagePixel() {
        super(new BasicDicomObject());
    }

    public int getSamplesPerPixel() {
        return dcmobj.getInt(Tag.SAMPLES_PER_PIXEL);
    }
    
    public void setSamplesPerPixel(int i) {
        dcmobj.putInt(Tag.SAMPLES_PER_PIXEL, VR.US, i);
    }
    
    public String getPhotometricInterpretation() {
        return dcmobj.getString(Tag.PHOTOMETRIC_INTERPRETATION);
    }
    
    public void setPhotometricInterpretation(String s) {
        dcmobj.putString(Tag.PHOTOMETRIC_INTERPRETATION, VR.CS, s);
    }
    
    public int getRows() {
        return dcmobj.getInt(Tag.ROWS);
    }
    
    public void setRows(int i) {
        dcmobj.putInt(Tag.ROWS, VR.US, i);
    }
    
    public int getColumns() {
        return dcmobj.getInt(Tag.COLUMNS);
    }
    
    public void setColumns(int i) {
        dcmobj.putInt(Tag.COLUMNS, VR.US, i);
    }
    
    public int getBitsAllocated() {
        return dcmobj.getInt(Tag.BITS_ALLOCATED);
    }
    
    public void setBitsAllocated(int i) {
        dcmobj.putInt(Tag.BITS_ALLOCATED, VR.US, i);
    }
    
    public int getBitsStored() {
        return dcmobj.getInt(Tag.BITS_STORED);
    }
    
    public void setBitsStored(int i) {
        dcmobj.putInt(Tag.BITS_STORED, VR.US, i);
    }
    
    public int getHighBit() {
        return dcmobj.getInt(Tag.HIGH_BIT);
    }
    
    public void setHighBit(int i) {
        dcmobj.putInt(Tag.HIGH_BIT, VR.US, i);
    }
    
    public int getPixelRepresentation() {
        return dcmobj.getInt(Tag.PIXEL_REPRESENTATION);
    }
    
    public void setPixelRepresentation(int i) {
        dcmobj.putInt(Tag.PIXEL_REPRESENTATION, VR.US, i);
    }
    
    public int getPlanarConfiguration() {
        return dcmobj.getInt(Tag.PLANAR_CONFIGURATION);
    }
    
    public void setPlanarConfiguration(int i) {
        dcmobj.putInt(Tag.PLANAR_CONFIGURATION, VR.US, i);
    }
    
    public int[] getPixelAspectRatio() {
        return dcmobj.getInts(Tag.PIXEL_ASPECT_RATIO);
    }
    
    public void setPixelAspectRatio(int[] ints) {
        dcmobj.putInts(Tag.PIXEL_ASPECT_RATIO, VR.IS, ints);
    }
    
    public byte[] getPixelData() {
        return dcmobj.getBytes(Tag.PIXEL_DATA, false);
    }
    
    public void setPixelData(byte[] b) {
        dcmobj.putBytes(Tag.PIXEL_DATA, VR.OW, b, false);
    }
    
    public int getSmallestImagePixelValue() {
        return dcmobj.getInt(Tag.SMALLEST_IMAGE_PIXEL_VALUE);
    }

    public void setSmallestImagePixelValue(int s) {
        dcmobj.putInt(Tag.SMALLEST_IMAGE_PIXEL_VALUE,
                PixelRepresentation.isSigned(dcmobj) ? VR.SS : VR.US, s);
    }

    public int getLargestImagePixelValue() {
        return dcmobj.getInt(Tag.LARGEST_IMAGE_PIXEL_VALUE);
    }

    public void setLargestImagePixelValue(int s) {
        dcmobj.putInt(Tag.LARGEST_IMAGE_PIXEL_VALUE,
                PixelRepresentation.isSigned(dcmobj) ? VR.SS : VR.US, s);
    }
    
    public int[] getRedPaletteColorLookupTableDescriptor() {
        return dcmobj.getInts(Tag.RED_PALETTE_COLOR_LOOKUP_TABLE_DESCRIPTOR);
    }
    
    public void setRedPaletteColorLookupTableDescriptor(int[] ints) {
        dcmobj.putInts(Tag.RED_PALETTE_COLOR_LOOKUP_TABLE_DESCRIPTOR, VR.US, ints);
    }
    
    public int[] getGreenPaletteColorLookupTableDescriptor() {
        return dcmobj.getInts(Tag.GREEN_PALETTE_COLOR_LOOKUP_TABLE_DESCRIPTOR);
    }
    
    public void setGreenPaletteColorLookupTableDescriptor(int[] ints) {
        dcmobj.putInts(Tag.GREEN_PALETTE_COLOR_LOOKUP_TABLE_DESCRIPTOR, VR.US, ints);
    }
    
    public int[] getBluePaletteColorLookupTableDescriptor() {
        return dcmobj.getInts(Tag.BLUE_PALETTE_COLOR_LOOKUP_TABLE_DESCRIPTOR);
    }
    
    public void setBluePaletteColorLookupTableDescriptor(int[] ints) {
        dcmobj.putInts(Tag.BLUE_PALETTE_COLOR_LOOKUP_TABLE_DESCRIPTOR, VR.US, ints);
    }
    
    public byte[] getRedPaletteColorLookupTableData() {
        return dcmobj.getBytes(Tag.RED_PALETTE_COLOR_LOOKUP_TABLE_DATA, false);
    }
    
    public void setRedPaletteColorLookupTableData(byte[] b) {
        dcmobj.putBytes(Tag.RED_PALETTE_COLOR_LOOKUP_TABLE_DATA, VR.OW, b, false);
    }
    
    public byte[] getGreenPaletteColorLookupTableData() {
        return dcmobj.getBytes(Tag.GREEN_PALETTE_COLOR_LOOKUP_TABLE_DATA, false);
    }
    
    public void setGreenPaletteColorLookupTableData(byte[] b) {
        dcmobj.putBytes(Tag.GREEN_PALETTE_COLOR_LOOKUP_TABLE_DATA, VR.OW, b, false);
    }
    
    public byte[] getBluePaletteColorLookupTableData() {
        return dcmobj.getBytes(Tag.BLUE_PALETTE_COLOR_LOOKUP_TABLE_DATA, false);
    }
    
    public void setBluePaletteColorLookupTableData(byte[] b) {
        dcmobj.putBytes(Tag.BLUE_PALETTE_COLOR_LOOKUP_TABLE_DATA, VR.OW, b, false);
    }
    
    public byte[] getICCProfile() {
        return dcmobj.getBytes(Tag.ICC_PROFILE, false);
    }
    
    public void setICCProfile(byte[] b) {
        dcmobj.putBytes(Tag.ICC_PROFILE, VR.OB, b, false);
    }
}
