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
 * Bill Wallace, Agfa HealthCare Inc., 
 * Portions created by the Initial Developer are Copyright (C) 2008
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Bill Wallace <bill.wallace@agfa.com>
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
package org.dcm4chee.xero.wado;

import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferShort;
import java.awt.image.DataBufferUShort;
import java.util.List;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExtractOverlaysAndPixelPadding {
	
	@SuppressWarnings("unused")
	private static Logger log = LoggerFactory.getLogger(ExtractOverlaysAndPixelPadding.class);

	DicomObject header;
	
	List<OverlayInfo> getSingleFrameEmbeddedOverlaysAsSeparate( BufferedImage bi ) {
		List<OverlayInfo> list = OverlayUtils.findEmbeddedOverlays(null, header);
		
		
		return list;
	}
	
	/** for monochrome data only */
    public static MinMaxResults calcMinMax(DicomObject img, DataBuffer db)
    {
        int allocated = img.getInt(Tag.BitsAllocated, 8);
        int stored = img.getInt(Tag.BitsStored, allocated);
        boolean signed = img.getInt(Tag.PixelRepresentation) != 0;
        int range = 1 << stored;
        int mask = range - 1;
        int signbit = signed ? 1 << (stored - 1) : 0;

        int pixelPaddingValue = img.getInt(Tag.PixelPaddingValue, Integer.MIN_VALUE);
        boolean havePixelPaddingValue = false;
        int pixelPaddingRange = pixelPaddingValue;
        {
        	if ( pixelPaddingValue != Integer.MIN_VALUE) {
        		havePixelPaddingValue = true;
        		pixelPaddingRange = img.getInt(Tag.PixelPaddingRangeLimit, pixelPaddingValue); 
        	}
        	pixelPaddingValue = fixSignOfShortData(signed, pixelPaddingValue, mask);
        	pixelPaddingRange = fixSignOfShortData(signed, pixelPaddingRange, mask);
        }

        MinMaxResults results = new MinMaxResults();
        if ( havePixelPaddingValue ) {
        	results.setPixelPadding( pixelPaddingValue, pixelPaddingRange );
        }

        switch(db.getDataType())
        {
        case DataBuffer.TYPE_BYTE: // '\0'
        	return calcMinMax(signbit, mask, ((DataBufferByte)db).getData(), results);

        case DataBuffer.TYPE_USHORT: // '\001'
        	return calcMinMaxWithoutPixelPadding(signbit, mask, ((DataBufferUShort)db).getData(), results);

        case DataBuffer.TYPE_SHORT: // '\002'
        	return calcMinMaxWithoutPixelPadding(signbit, mask, ((DataBufferShort)db).getData(), results);
        }
        throw new IllegalArgumentException((new StringBuilder()).append("Illegal Type of DataBuffer: ").append(db).toString());
    }

    
	protected static int fixSignOfShortData( boolean signed,
											 int value,
											 int mask ) {
		if ( signed ) {
        	value = ( value & mask ) | (~(mask));
        }
        else {
        	value = value & mask;
        }
		return value;
	}

    protected static MinMaxResults calcMinMax(int signbit, int mask, short data[], MinMaxResults results )
    {
        int minVal = results.min;
        int maxVal = results.max;
        if ( signbit == 0 ) {
        	for(int i = 0; i < data.length; i++)
        	{
        		int val = data[i] & mask;
        		minVal = Math.min(val, minVal);
        		maxVal = Math.max(val, maxVal);
        	}
        } else {
        	if ( mask == 0xffff ) {
        		for(int i = 0; i < data.length; i++)
        		{
        			int val = data[i];
        			minVal = Math.min(val, minVal);
        			maxVal = Math.max(val, maxVal);
        		}           		
        	} else {
        		for(int i = 0; i < data.length; i++)
        		{
        			int val = data[i] & mask;
        			if((val & signbit) != 0)
        				val |= ~mask;

        			minVal = Math.min(val, minVal);
        			maxVal = Math.max(val, maxVal);
        		}   
        	}
        }

        results.min = minVal;
        results.max = maxVal;
        return results; 
    }

    static MinMaxResults calcMinMaxWithoutPixelPadding(int signbit, int mask, short data[], MinMaxResults results)
    {
        int minVal = results.min;
        int maxVal = results.max;
        
        int minPadding = results.minPixelPadding;
        int maxPadding = results.maxPixelPadding;
        
        if ( (minPadding > maxPadding) ) {
        	return calcMinMax(signbit, mask, data, results);
        }
        
        boolean foundPixelPadding = results.pixelPaddingFound;
        boolean bitMaskingNeeded = results.bitsNeedMasking;
        int complementMask = ( mask ^ 0xffff ) & 0xffff;
        int val = 0;
        short dataVal = 0;
        short accumVal = 0;
        
        if ( signbit == 0 ) {
        	for(int i = 0; i < data.length; i++) {

        		dataVal = data[i];
        		val = dataVal & mask;
        		accumVal |= dataVal;

        		if ( ( val >= minPadding ) && ( val <= maxPadding ) ) {
        			foundPixelPadding = true;
        		}
        		else
        		{
        			minVal = Math.min(val, minVal);
        			maxVal = Math.max(val, maxVal);
        		}
        	}        	
        } else {
        	if ( mask == 0xffff ) {
        		for(int i = 0; i < data.length; i++) {

        			val = data[i];
        			accumVal |= val;

        			if ( ( val >= minPadding ) && ( val <= maxPadding ) ) {
        				foundPixelPadding = true;
        			}
        			else
        			{
        				minVal = Math.min(val, minVal);
        				maxVal = Math.max(val, maxVal);
        			}
        		}        		
        	} else {
        		for(int i = 0; i < data.length; i++) {

        			dataVal = data[i];
        			val = dataVal & mask;
        			accumVal |= dataVal;

        			if((val & signbit) != 0)
        				val |= ~mask;

        			if ( ( val >= minPadding ) && ( val <= maxPadding ) ) {
        				foundPixelPadding = true;
        			}
        			else
        			{
        				minVal = Math.min(val, minVal);
        				maxVal = Math.max(val, maxVal);
        			}
        		}
        	}
        }
        if ( ( accumVal & complementMask ) != 0 ) {
        	bitMaskingNeeded = true;
        }

        results.min = minVal;
        results.max = maxVal;
        results.pixelPaddingFound = foundPixelPadding;
        results.bitsNeedMasking = bitMaskingNeeded;
        
        return results; 
    }

    static MinMaxResults calcMinMax(int signbit, int mask, byte data[], MinMaxResults results)
    {
    	int minVal = results.min;
        int maxVal = results.max;
        if ( signbit == 0 ) {
        	for (int i = 0; i < data.length; i++) {
        		int val = data[i] & mask;
        		minVal = Math.min(val, minVal);
        		maxVal = Math.max(val, maxVal);  
        	}
        } else {
        	for (int i = 0; i < data.length; i++) {
        		int val = data[i] & mask;
        		if((val & signbit) != 0)
        			val |= ~mask;
        		minVal = Math.min(val, minVal);
        		maxVal = Math.max(val, maxVal);
        	}
        }

        results.min = minVal;
        results.max = maxVal;
        return results; 
    }
    
    static MinMaxResults calcMinMaxWithoutPixelPadding(int signbit, int mask, byte data[], MinMaxResults results)
    {
        int minVal = results.min;
        int maxVal = results.max;
        
        int minPadding = results.minPixelPadding;
        int maxPadding = results.maxPixelPadding;
        
        if ( (minPadding > maxPadding) ) {
        	return calcMinMax(signbit, mask, data, results);
        }
        
        boolean foundPixelPadding = results.pixelPaddingFound;
        boolean bitMaskingNeeded = results.bitsNeedMasking;
        int complementMask = ( mask ^ 0xff ) & 0xff;
        int val = 0;
        byte dataVal = 0;
        byte accumVal = 0;
        
        if ( signbit == 0 ) {
        	for(int i = 0; i < data.length; i++) {

        		dataVal = data[i];
        		val = dataVal & mask;
        		accumVal |= dataVal;

        		if ( ( val >= minPadding ) && ( val <= maxPadding ) ) {
        			foundPixelPadding = true;
        		}
        		else
        		{
        			minVal = Math.min(val, minVal);
        			maxVal = Math.max(val, maxVal);
        		}
        	}        	
        } else {
        	for(int i = 0; i < data.length; i++) {

        		dataVal = data[i];
        		val = dataVal & mask;
        		accumVal |= dataVal;

        		if((val & signbit) != 0)
        			val |= ~mask;

        		if ( ( val >= minPadding ) && ( val <= maxPadding ) ) {
        			foundPixelPadding = true;
        		}
        		else
        		{
        			minVal = Math.min(val, minVal);
        			maxVal = Math.max(val, maxVal);
        		}
        	}
        }
        if ( ( accumVal & complementMask ) != 0 ) {
        	bitMaskingNeeded = true;
        }

        results.min = minVal;
        results.max = maxVal;
        results.pixelPaddingFound = foundPixelPadding;
        results.bitsNeedMasking = bitMaskingNeeded;
        
        return results; 
    }
    
}
