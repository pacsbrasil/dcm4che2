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

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;

class OverlayInfo {
	
	OverlayInfo(){};
	
	OverlayInfo( DicomObject ds, int overlayNumber ) {
		number = overlayNumber;
		int base = ( 0x6000 | (overlayNumber & 0xFF ) ) << 16;
		clr = OverlayUtils.WHITE;
		bit = ds.getInt(base | Tag.OverlayBitPosition);
		embedded = (bit > 0);
		width = ds.getInt(base | Tag.OverlayColumns);
		height = ds.getInt(base | Tag.OverlayRows);
		if (!embedded) {
			data = ds.getBytes(base | Tag.OverlayData);
			if( data != null ) multiframe = (data.length >= 2 * ((width + 7) / 8) * height);
		}
		
	}
	
	public boolean hasValidSeparateOverlayBytes() {
		if ( data == null ) {
			return false;
		}
		int frames = numberOfFramesInOverlay;
		int expectedDataLength = (((width * height * frames) + 7)/8);
		if ( data.length >= expectedDataLength ) {
			return true;
		}
		return false;
	}
	public boolean hasOverlayForFrameFromOne( int frameNumberFromOne, int totalImageFrames ) {
		if ( isEmpty() ) {
			return false;
		}
		int numFrames = getNumberOfOverlayFrames( totalImageFrames );
		int lastFrame = imageFrameOrigin + numFrames - 1;
		if ( ( frameNumberFromOne >= imageFrameOrigin ) && ( frameNumberFromOne <= lastFrame ) ) {
			return true;
		}
		return false;
	}
	
	public int getNumberOfOverlayFrames( int totalImageFrames ) {
		if ( isEmpty() ) {
			return 0;
		}
		int potentialFrames = numberOfFramesInOverlay;
		if ( ( data != null ) && ( (width * height) > 8 ) ) {
			int dataFrames = (int)( (long)data.length * 8 / (long)(width*height));
			if ( (dataFrames > potentialFrames) && (potentialFrames == 1) ) {
				potentialFrames = dataFrames;
			}
		}
		
		if ( (potentialFrames + imageFrameOrigin) >= totalImageFrames ) {
			potentialFrames = totalImageFrames - imageFrameOrigin + 1;
		}
		return potentialFrames;
	}
	
	public boolean isEmpty() {
		return ( ( width * height ) <= 0 );
	}
	
	boolean multiframe = false;
	boolean embedded = false;
	int number;
	int width, height;
	byte[] clr;
	int useClr;
	int bit;
	byte[] data;
	String description;
	String subtype;
	String label;
	int numberOfFramesInOverlay = 1;
	int imageFrameOrigin = 1;
}
