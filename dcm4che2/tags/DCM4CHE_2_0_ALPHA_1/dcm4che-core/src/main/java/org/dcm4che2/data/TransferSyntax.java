/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/

package org.dcm4che2.data;

public class TransferSyntax {

	public static final TransferSyntax ImplicitVRLittleEndian 
			= new TransferSyntax("1.2.840.10008.1.2", false, false, false);
	
	public static final TransferSyntax ImplicitVRBigEndian 
			= new TransferSyntax(null, false, true, false);

	public static final TransferSyntax ExplicitVRLittleEndian 
			= new TransferSyntax("1.2.840.10008.1.2.1", true, false, false);
	
	public static final TransferSyntax ExplicitVRBigEndian 
			= new TransferSyntax("1.2.840.10008.1.2.2", true, true, false);
	
	public static final TransferSyntax DeflatedExplicitVRLittleEndian 
			= new TransferSyntax("1.2.840.10008.1.2.1.99", true, false, true);
	
	public static TransferSyntax valueOf(String uid) {
		if (uid.equals(ImplicitVRLittleEndian.uid))
			return ImplicitVRLittleEndian;
		if (uid.equals(ExplicitVRLittleEndian.uid))
			return ExplicitVRLittleEndian;
		if (uid.equals(ExplicitVRBigEndian.uid))
			return ExplicitVRBigEndian;
		if (uid.equals(DeflatedExplicitVRLittleEndian.uid))
			return DeflatedExplicitVRLittleEndian;
		return new TransferSyntax(uid, true, false, false);
	}

	final String uid;

	final boolean bigEndian;

	final boolean explicitVR;

	final boolean deflated;

	private TransferSyntax(String uid, boolean explicitVR, boolean bigEndian,
			boolean deflated) {
		this.uid = uid;
		this.explicitVR = explicitVR;
		this.bigEndian = bigEndian;
		this.deflated = deflated;
	}

	public final String uid() {
		return uid;
	}

	public final boolean bigEndian() {
		return bigEndian;
	}

	public final boolean explicitVR() {
		return explicitVR;
	}

	public final boolean isDeflated() {
		return deflated;
	}
}
