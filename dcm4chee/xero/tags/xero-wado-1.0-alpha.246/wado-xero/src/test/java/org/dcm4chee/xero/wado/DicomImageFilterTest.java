/**
 * 
 */
package org.dcm4chee.xero.wado;

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
	public void calculateFinalSizeFromSubsampling_InsertSaneParameters_ReturnNewSize(){
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
	public void calculateFinalSizeFromSubsampling_InsertZeroSubSample_ReturnStartSize(){
		Assert.assertEquals(calculateFinalSizeFromSubsampling(256, 0), 256);		
	}

	@Test
	public void isPowerOfTwo_GivenPowersOfTwo_ReturnTrue() {
		for ( int i = 0; i < 31; i++ ) {
			assertTrue(isPowerOfTwo(1<<i));
		}
	}

	@Test
	public void isPowerOfTwo_GivenNotPowersOfTwo_ReturnFalse() {
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
}
