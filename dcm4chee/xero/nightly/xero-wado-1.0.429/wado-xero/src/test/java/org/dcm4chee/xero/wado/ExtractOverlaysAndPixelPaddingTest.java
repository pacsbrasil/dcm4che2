package org.dcm4chee.xero.wado;

import static org.testng.Assert.*;

import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferShort;
import java.awt.image.DataBufferUShort;
import java.util.Random;

import org.dcm4che2.data.BasicDicomObject;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.VR;
import org.testng.annotations.*;


public class ExtractOverlaysAndPixelPaddingTest {
	
	private DataBuffer bufferShort14;
	private DataBuffer bufferUShort;
	private DataBuffer bufferShort;
	private DataBuffer bufferUShort14;
	private int minTest;
	private int minUTest;
	private int maxTest;
	private DicomObject dcm;
	private DicomObject dcmUnsigned;
	private DicomObject dcm14;
	private DicomObject dcmUnsigned14;

	@BeforeTest
	public void setup() {
		minTest = -4;
		minUTest = 77;
		maxTest = 8099;
		bufferShort = createDataBuffer( DataBuffer.TYPE_SHORT, 400000, minTest, maxTest, 16);
		bufferUShort = createDataBuffer( DataBuffer.TYPE_USHORT, 400000, minUTest, maxTest, 16);
		bufferShort14 = createDataBuffer( DataBuffer.TYPE_SHORT, 400000, minTest, maxTest, 14);
		bufferUShort14 = createDataBuffer( DataBuffer.TYPE_USHORT, 400000, minUTest, maxTest, 14);
	}

	@BeforeMethod
	public void setupMethod() {
		dcm = new BasicDicomObject();
		dcm.putInt( Tag.BitsStored, VR.IS, 16 );
		dcm.putInt( Tag.PixelRepresentation, VR.IS, 1);

		dcmUnsigned = new BasicDicomObject();
		dcmUnsigned.putInt( Tag.BitsStored, VR.IS, 16 );
		dcmUnsigned.putInt( Tag.PixelRepresentation, VR.IS, 0);
		
		dcm14 = new BasicDicomObject();
		dcm14.putInt( Tag.BitsStored, VR.IS, 14 );
		dcm14.putInt( Tag.PixelRepresentation, VR.IS, 1);

		dcmUnsigned14 = new BasicDicomObject();
		dcmUnsigned14.putInt( Tag.BitsStored, VR.IS, 14 );
		dcmUnsigned14.putInt( Tag.PixelRepresentation, VR.IS, 0);
	}

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
	public void testCalcMinMax_givenShortDataWithoutPixelPadding_expectCorrectMinMaxValues() {
		MinMaxResults out = ExtractOverlaysAndPixelPadding.calcMinMax(dcm, bufferShort);		
		assertEquals( out.min, minTest);
		assertEquals( out.max, maxTest);
		assertFalse( out.pixelPaddingFound);

		MinMaxResults out14 = ExtractOverlaysAndPixelPadding.calcMinMax(dcm14, bufferShort14);		
		assertEquals( out14.min, minTest);
		assertEquals( out14.max, maxTest);
		assertFalse( out14.pixelPaddingFound);
	}
	
	@Test
	public void testCalcMinMax_givenUnsignedShortDataWithoutPixelPadding_expectCorrectMinMaxValues() {
		MinMaxResults out = ExtractOverlaysAndPixelPadding.calcMinMax(dcmUnsigned, bufferUShort);		
		assertEquals( out.min, minUTest);
		assertEquals( out.max, maxTest);
		assertFalse( out.pixelPaddingFound);

		MinMaxResults out14 = ExtractOverlaysAndPixelPadding.calcMinMax(dcmUnsigned14, bufferUShort14);		
		assertEquals( out14.min, minUTest);
		assertEquals( out14.max, maxTest);
		assertFalse( out14.pixelPaddingFound);
	}
	
	@Test
	public void testCalcMinMax_givenShortDataWithPixelPadding_expectCorrectMinMaxValues() {
		dcm.putInt( Tag.PixelPaddingValue, VR.SS, (minTest-1));
		MinMaxResults out = ExtractOverlaysAndPixelPadding.calcMinMax(dcm, bufferShort);		
		assertEquals( out.min, minTest);
		assertEquals( out.max, maxTest);
		assertFalse( out.pixelPaddingFound);
		
		dcm.putInt( Tag.PixelPaddingValue, VR.SS, (minTest));
		out = ExtractOverlaysAndPixelPadding.calcMinMax(dcm, bufferShort);
		assertTrue( out.min > minTest);
		assertEquals( out.max, maxTest);
		assertTrue( out.pixelPaddingFound);

		dcm14.putInt( Tag.PixelPaddingValue, VR.SS, (minTest-1));
		MinMaxResults out14 = ExtractOverlaysAndPixelPadding.calcMinMax(dcm14, bufferShort14);		
		assertEquals( out14.min, minTest);
		assertEquals( out14.max, maxTest);
		assertFalse( out14.pixelPaddingFound);

		dcm14.putInt( Tag.PixelPaddingValue, VR.SS, (minTest));
		out14 = ExtractOverlaysAndPixelPadding.calcMinMax(dcm14, bufferShort14);		
		assertTrue( out14.min > minTest);
		assertEquals( out14.max, maxTest);
		assertTrue( out14.pixelPaddingFound);
	}
	
	@Test
	public void testCalcMinMax_givenUnsignedShortDataWithPixelPadding_expectCorrectMinMaxValues() {
		dcmUnsigned.putInt( Tag.PixelPaddingValue, VR.US, (maxTest+1));
		MinMaxResults out = ExtractOverlaysAndPixelPadding.calcMinMax(dcmUnsigned, bufferUShort);		
		assertEquals( out.min, minUTest);
		assertEquals( out.max, maxTest);
		assertFalse( out.pixelPaddingFound);

		dcmUnsigned.putInt( Tag.PixelPaddingValue, VR.US, (maxTest));
		out = ExtractOverlaysAndPixelPadding.calcMinMax(dcmUnsigned, bufferUShort);		
		assertEquals( out.min, minUTest);
		assertTrue( out.max < maxTest);
		assertTrue( out.pixelPaddingFound);

		dcmUnsigned14.putInt( Tag.PixelPaddingValue, VR.US, (maxTest+1));
		MinMaxResults out14 = ExtractOverlaysAndPixelPadding.calcMinMax(dcmUnsigned14, bufferUShort14);		
		assertEquals( out14.min, minUTest);
		assertEquals( out14.max, maxTest);
		assertFalse( out14.pixelPaddingFound);

		dcmUnsigned14.putInt( Tag.PixelPaddingValue, VR.US, (maxTest));
		out14 = ExtractOverlaysAndPixelPadding.calcMinMax(dcmUnsigned14, bufferUShort14);		
		assertEquals( out14.min, minUTest);
		assertTrue( out14.max < maxTest);
		assertTrue( out14.pixelPaddingFound);
	}
	
	@Test
	public void testZCalcMinMax_doTimings()
	{
		//
		// DO TIMINGS.
		//
		dcm.putInt( Tag.PixelPaddingValue, VR.SS, (minTest-1));
		long start = System.nanoTime();
		ExtractOverlaysAndPixelPadding.calcMinMax(dcm, bufferShort);
		long end = System.nanoTime();
		dcm.remove(Tag.PixelPaddingValue);
		System.out.println("Total time 16-bit Signed: " + (end - start)/1000L + " us" );
		
		dcmUnsigned.putInt( Tag.PixelPaddingValue, VR.US, (minUTest-1));
		start = System.nanoTime();
		ExtractOverlaysAndPixelPadding.calcMinMax(dcmUnsigned, bufferUShort);
		end = System.nanoTime();
		dcmUnsigned.remove(Tag.PixelPaddingValue);
		System.out.println("Total time 16-bit Unsigned: " + (end - start)/1000L + " us" );
		
		dcm14.putInt( Tag.PixelPaddingValue, VR.SS, (minTest-1));
		start = System.nanoTime();
		ExtractOverlaysAndPixelPadding.calcMinMax(dcm14, bufferShort14);
		end = System.nanoTime();
		dcm14.remove(Tag.PixelPaddingValue);
		System.out.println("Total time 14-bit Signed: " + (end - start)/1000L + " us" );
		
		dcmUnsigned14.putInt( Tag.PixelPaddingValue, VR.US, (minUTest-1));
		start = System.nanoTime();
		ExtractOverlaysAndPixelPadding.calcMinMax(dcmUnsigned14, bufferUShort14);
		end = System.nanoTime();
		dcmUnsigned14.remove(Tag.PixelPaddingValue);
		System.out.println("Total time 14-bit Unsigned: " + (end - start)/1000L + " us" );
		
		start = System.nanoTime();
		ExtractOverlaysAndPixelPadding.calcMinMax(dcm, bufferShort);
		end = System.nanoTime();
		System.out.println("Total time 16-bit Signed without pixel padding: " + (end - start)/1000L + " us" );

		start = System.nanoTime();
		ExtractOverlaysAndPixelPadding.calcMinMax(dcmUnsigned, bufferUShort);
		end = System.nanoTime();
		System.out.println("Total time 16-bit Unsigned without pixel padding: " + (end - start)/1000L + " us" );

		start = System.nanoTime();
		ExtractOverlaysAndPixelPadding.calcMinMax(dcm14, bufferShort14);
		end = System.nanoTime();
		System.out.println("Total time 14-bit Signed without pixel padding: " + (end - start)/1000L + " us" );

		start = System.nanoTime();
		ExtractOverlaysAndPixelPadding.calcMinMax(dcmUnsigned14, bufferUShort14);
		end = System.nanoTime();
		System.out.println("Total time 14-bit Unsigned without pixel padding: " + (end - start)/1000L + " us" );
	
	}



	protected DataBuffer createDataBuffer( int type, int size, int min, int max, int bits )
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
			int val = r.nextInt(diff)+min;
			if ( bits < 16 ) {
				val ^= 0x8000;
			}
			buffer.setElem(i, val );
		}
		
		int minPos = r.nextInt(size);
		int setVal = min;
		if ( bits < 16 ) {
			setVal ^= 0x8000;
		}
		buffer.setElem( minPos, setVal);
		int maxPos = r.nextInt(size);
		if ( maxPos == minPos ) {
			maxPos = ( minPos + 1 ) % size;
		}
		setVal = max;
		if ( bits < 16 ) {
			setVal ^= 0x8000;
		}
		buffer.setElem( maxPos, setVal);
	
		return buffer;
	}
	

}
