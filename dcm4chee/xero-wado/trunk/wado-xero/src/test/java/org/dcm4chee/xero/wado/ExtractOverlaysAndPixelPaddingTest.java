package org.dcm4chee.xero.wado;

import static org.testng.Assert.assertEquals;

import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferShort;
import java.awt.image.DataBufferUShort;
import java.util.Random;

import org.dcm4che2.data.BasicDicomObject;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.VR;
import org.testng.annotations.Test;


public class ExtractOverlaysAndPixelPaddingTest {
	
	@Test
	public void testFixSignOfShortData() {
		assertEquals(ExtractOverlaysAndPixelPadding.fixSignOfShortData(true,-5,0xffff),-5);
		int value = (-5)&0xffff;
		assertEquals(ExtractOverlaysAndPixelPadding.fixSignOfShortData(true,value,0xffff),-5);
		value = -3000;
		assertEquals(ExtractOverlaysAndPixelPadding.fixSignOfShortData(false,value,0xffff),(-3000)&0xffff);
		value = 3000;
		assertEquals(ExtractOverlaysAndPixelPadding.fixSignOfShortData(false,value,0xffff),3000);
		value = 0x0fff;
		assertEquals(ExtractOverlaysAndPixelPadding.fixSignOfShortData(true,value,0x0fff),-1);
	}
	
	
	
	@Test
	public void testcalcMinMaxWithoutPixelPadding()
	{
		int minTest = -4;
		int minUTest = 77;
		int maxTest = 8999;
		DataBuffer bufferShort = createDataBuffer( DataBuffer.TYPE_SHORT, 4000000, minTest, maxTest);
		DataBuffer bufferUShort = createDataBuffer( DataBuffer.TYPE_USHORT, 4000000, minUTest, maxTest);
		
		DicomObject dcm = new BasicDicomObject();
		dcm.putInt( Tag.BitsStored, VR.IS, 16 );
		dcm.putInt( Tag.PixelRepresentation, VR.IS, 1);

		DicomObject dcmUnsigned = new BasicDicomObject();
		dcmUnsigned.putInt( Tag.BitsStored, VR.IS, 16 );
		dcmUnsigned.putInt( Tag.PixelRepresentation, VR.IS, 0);
		
		MinMaxResults out;

		dcm.putInt( Tag.PixelPaddingValue, VR.SS, (minTest-1));
		out = ExtractOverlaysAndPixelPadding.calcMinMax(dcm, bufferShort);
		assertEquals( out.min, minTest);
		assertEquals( out.max, maxTest);
		dcm.remove(Tag.PixelPaddingValue);

		dcm.putInt( Tag.PixelPaddingValue, VR.US, (maxTest+1));
		out = ExtractOverlaysAndPixelPadding.calcMinMax(dcm, bufferUShort);
		assertEquals( out.min, minUTest);
		assertEquals( out.max, maxTest);
		dcm.remove(Tag.PixelPaddingValue);

		out = ExtractOverlaysAndPixelPadding.calcMinMax(dcm, bufferShort);		
		assertEquals( out.min, minTest);
		assertEquals( out.max, maxTest);
		
		dcm.putInt( Tag.PixelPaddingValue, VR.SS, (minTest-1));
		ExtractOverlaysAndPixelPadding.calcMinMax(dcm, bufferShort);
		dcm.remove(Tag.PixelPaddingValue);
		out = ExtractOverlaysAndPixelPadding.calcMinMax(dcm, bufferShort);		
		
		//
		// DO TIMINGS.
		//
		dcm.putInt( Tag.PixelPaddingValue, VR.SS, (minTest-1));
		long start = System.nanoTime();
		ExtractOverlaysAndPixelPadding.calcMinMax(dcm, bufferShort);
		long end = System.nanoTime();
		dcm.remove(Tag.PixelPaddingValue);
		System.out.println("Total time Signed: " + (end - start)/1000L + " us" );
		
		dcmUnsigned.putInt( Tag.PixelPaddingValue, VR.US, (minUTest-1));
		start = System.nanoTime();
		ExtractOverlaysAndPixelPadding.calcMinMax(dcmUnsigned, bufferUShort);
		end = System.nanoTime();
		dcmUnsigned.remove(Tag.PixelPaddingValue);
		System.out.println("Total time Unsigned: " + (end - start)/1000L + " us" );
		
		start = System.nanoTime();
		ExtractOverlaysAndPixelPadding.calcMinMax(dcm, bufferShort);
		end = System.nanoTime();
		System.out.println("Total time Signed without pixel padding: " + (end - start)/1000L + " us" );

		start = System.nanoTime();
		ExtractOverlaysAndPixelPadding.calcMinMax(dcmUnsigned, bufferUShort);
		end = System.nanoTime();
		System.out.println("Total time Unsigned without pixel padding: " + (end - start)/1000L + " us" );
	
	}
	
	
	protected DataBuffer createDataBuffer( int type, int size, int min, int max )
	{
		DataBuffer buffer = null;
		
		switch( type )
		{
		case( DataBuffer.TYPE_USHORT ):
			buffer = new DataBufferUShort(size);
			break;
		case( DataBuffer.TYPE_SHORT ) :
			buffer = new DataBufferShort(size);
			break;
		case( DataBuffer.TYPE_BYTE ) :
			buffer = new DataBufferByte(size);
			break;
		}

		Random r = new Random(1000);
		
		int diff = max-min+1;
		for( int i=0; i < size; i++ ) {
			buffer.setElem(i, r.nextInt(diff)+min );
		}
		
		int minPos = r.nextInt(size);
		buffer.setElem( minPos, min);
		int maxPos = r.nextInt(size);
		if ( maxPos == minPos ) {
			maxPos = ( minPos + 1 ) % size;
		}
		buffer.setElem( maxPos, max);
	
		return buffer;
	}
	

}
