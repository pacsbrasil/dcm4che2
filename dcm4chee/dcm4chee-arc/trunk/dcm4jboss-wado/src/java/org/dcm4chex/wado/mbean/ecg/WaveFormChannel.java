/*
 * Created on 10.02.2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.dcm4chex.wado.mbean.ecg;

import org.dcm4che.data.Dataset;
import org.dcm4che.dict.Tags;

/**
 * @author franz.willer
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class WaveFormChannel {

	private int chNr;
	private int bitsStored;
	private Integer minValue;
	private Integer maxValue;
	private String label;
	private String status;
	private WaveFormBuffer buffer;
	/**
	 * @param buffer
	 * @param item
	 */
	public WaveFormChannel(Dataset ch, WaveFormBuffer buffer) {
		chNr = ch.getInt( Tags.WaveformChannelNumber, -1);
		bitsStored = ch.getInt( Tags.WaveformBitsStored, -1 );
		minValue = ch.getInteger( Tags.ChannelMinimumValue, -1 );
		maxValue = ch.getInteger( Tags.ChannelMaximumValue, -1 );
		label = ch.getString( Tags.ChannelLabel );
		status = ch.getString( Tags.ChannelStatus );
		this.buffer = buffer;
	}
	
	public int getValue() {
		return buffer.getValue();
	}
	
	public int getValue( int sampleNr ) {
		return buffer.getValue( sampleNr );
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("WaveFormChannel: chNr").append(chNr).append(" label:").append(label);
		sb.append(" bitsStored:").append( bitsStored).append(" min:").append(minValue).append(" max:").append(maxValue);
		sb.append(" status:").append(status);
		return sb.toString();
	}

}
