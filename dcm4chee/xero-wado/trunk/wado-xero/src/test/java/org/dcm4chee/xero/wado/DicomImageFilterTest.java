/**
 * 
 */
package org.dcm4chee.xero.wado;

import java.awt.Rectangle;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageReadParam;

import org.testng.Assert;
import org.testng.annotations.Test;

import static org.dcm4chee.xero.wado.DicomImageFilter.*;
import static org.testng.Assert.*;

/**
 * @author lpeters
 *
 */
public class DicomImageFilterTest {

	@Test
	public void calculateFinalSizeFromSubsamplingTest_InsertSaneParameters_ReturnNewSize(){
		int[] startSizes = new int[]{512, 511, 513, 133, 2};
		int[] subSampleIndices = new int[]{2, 1, 200, 13, 5};
		int[][] finalSizes = new int[][]{ 
				{256,512,3,40,103}, // startSize 512
				{256,511,3,40,103},  // startSize 511
				{257,513,3,40,103},  // startSize 513
				{67,133,1,11,27},  // startSize 133
				{1,2,1,1,1},  // startSize 2
		};

		for (int i = 0; i < startSizes.length; i++){
			for (int j = 0; j < subSampleIndices.length; j++){
				assertEquals(calculateFinalSizeFromSubsampling(startSizes[i], subSampleIndices[j]), 
						finalSizes[i][j]);
			}
		}
	}

	@Test
	public void calculateFinalSizeFromSubsamplingTest_InsertZeroSubSample_ReturnStartSize(){
		Assert.assertEquals(calculateFinalSizeFromSubsampling(256, 0), 256);		
	}

	@Test
	public void isPowerOfTwoTest_GivenPowersOfTwo_ReturnTrue() {
		for ( int i = 0; i < 31; i++ ) {
			assertTrue(isPowerOfTwo(1<<i));
		}
	}

	@Test
	public void isPowerOfTwoTest_GivenNotPowersOfTwo_ReturnFalse() {
		assertFalse(isPowerOfTwo(7));
		assertFalse(isPowerOfTwo(5));
		assertFalse(isPowerOfTwo(3));
		assertFalse(isPowerOfTwo(0));
		assertFalse(isPowerOfTwo(-2));
		for ( int i = 3; i < 31; i++ ) {
			assertFalse(isPowerOfTwo((1<<i) + 1));
			assertFalse(isPowerOfTwo((1<<i) - 1));
			assertFalse(isPowerOfTwo((1<<i) + (1<<(i-1))));
		}
	}
	
	@Test
	public void calculateDesiredSubsamplingFactorForOneDimensionTest_GivenData_OutputSubsampleFactorGivesSizeGreaterOrEqualToDesiredSize() {
		for ( int startSize = 16; startSize < 69; startSize++) {
			for ( int desiredSize = 1; desiredSize < 70; desiredSize++ ) {
				int subsampleFactor = calculateDesiredSubsamplingFactorForOneDimension( startSize, desiredSize );
				int finalSize = calculateFinalSizeFromSubsampling( startSize, subsampleFactor );
				int smallerThanDesiredSize = calculateFinalSizeFromSubsampling( startSize, subsampleFactor+1 );
				String msg = "startSize= "+startSize+", desiredSize= "+desiredSize;
				if( desiredSize > startSize){
					assertTrue(finalSize == startSize, msg);
					assertTrue((smallerThanDesiredSize < desiredSize)||((smallerThanDesiredSize==desiredSize)&&(smallerThanDesiredSize==finalSize)),
							msg );
				}else{
					assertTrue(finalSize >= desiredSize, msg);
					assertTrue((smallerThanDesiredSize < desiredSize)||((smallerThanDesiredSize==desiredSize)&&(smallerThanDesiredSize==finalSize)),
							msg );
				}				
			}
		}
	}
	
	@Test
	public void calculateSubregionFromRegionFloatArrayTest() {
		float[] regionFull = {0.0f,0.0f,1.0f,1.0f};
		for ( int fullWidth = 500; fullWidth < 533; fullWidth ++ ) {
			int fullHeight = fullWidth + 4979;
			Rectangle rect = calculateSubregionFromRegionFloatArray( regionFull, fullWidth, fullHeight );
			Rectangle expectedRect = new Rectangle(fullWidth,fullHeight);
			assertEquals(rect,expectedRect,"fullWidth= "+fullWidth);
		}
		float[] region = new float[4];
		int fullWidth = 511;
		int fullHeight = 2542;
		// Test first element.
		System.arraycopy(regionFull, 0, region, 0, 4);
		for ( float x0 = -0.1f; x0 <= 1.1; x0+=0.003f ) {
			region[0] = x0;
			Rectangle rect = calculateSubregionFromRegionFloatArray( region, fullWidth, fullHeight );
			if ( x0 < 0.0f ) {
				assertEquals(rect.x,0);
			} else if ( x0 >= 1.0f ) {
				assertTrue(rect.isEmpty());
			} else {
				assertEquals((int)Math.round(x0*fullWidth),rect.x);
			}
		}
		// Test second element.
		System.arraycopy(regionFull, 0, region, 0, 4);
		final float y1Test = 0.8f;
		region[3] = y1Test;
		for ( float y0 = -0.1f; y0 <= 1.1; y0+=0.003f ) {
			region[1] = y0;
			Rectangle rect = calculateSubregionFromRegionFloatArray( region, fullWidth, fullHeight );
			if ( y0 < 0.0f ) {
				assertEquals(rect.y,0);
			} else if ( y0 >= y1Test ) {
				assertTrue(rect.isEmpty());
			} else {
				assertEquals((int)Math.round(y0*fullHeight),rect.y);
			}
		}
		// Test third element.
		System.arraycopy(regionFull, 0, region, 0, 4);
		for ( float x1 = -0.1f; x1 <= 1.1; x1+=0.003f ) {
			region[2] = x1;
			Rectangle rect = calculateSubregionFromRegionFloatArray( region, fullWidth, fullHeight );
			if ( x1 < region[0] ) {
				assertTrue(rect.isEmpty());
			} else if ( x1 >= 1.0f ) {
				assertEquals(rect.width,fullWidth);
			} else {
				assertEquals((int)Math.round(x1*fullWidth),rect.width);
			}
		}
		// Test fourth element.
		System.arraycopy(regionFull, 0, region, 0, 4);
		final float y0Test = 0.11f;
		region[1] = y0Test;
		for ( float y1 = -0.1f; y1 <= 1.1; y1+=0.003f ) {
			region[3] = y1;
			Rectangle rect = calculateSubregionFromRegionFloatArray( region, fullWidth, fullHeight );
			if ( y1 < region[1] ) {
				assertTrue(rect.isEmpty());
			} else if ( y1 >= 1.0f ) {
				assertEquals(rect.height,(int)Math.round((1.0f-y0Test)*fullHeight));
			} else {
				assertEquals((int)Math.round((y1-y0Test)*fullHeight),rect.height,"here.");
			}
		}
	}
	
	@Test
	public void updateParamFromRegionTest_GivenRowsColumnsAndRegion_ReturnStringWithSubsampleAndCropRect() {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put(WadoParams.ROWS, 512);
		params.put(WadoParams.COLUMNS, 64);
		params.put(WadoParams.REGION, new float[]{0.25f,0,0.75f,1.0f});
		int width = 1024;
		int height = 2048;
		String ret = updateParamFromRegion(new ImageReadParam(), params, width, height);
		assertEquals(ret, "-s8,4-r256,0,512,2048");

	}
}