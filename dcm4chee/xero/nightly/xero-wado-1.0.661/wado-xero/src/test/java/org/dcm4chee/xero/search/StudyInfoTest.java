package org.dcm4chee.xero.search;

import org.testng.annotations.Test;

/**
 * Tests the basic study information methods.
 * @author bwallace
 *
 */
public class StudyInfoTest {

	/** Test audit actions */
	@Test
	public void auditTest() {
		StudyInfo si = new StudyInfo("1.2.3");
		assert si.isAudited("userkey", "neverAudited")==false;
		si.putAudited("userkey", "auditact");
		assert si.isAudited("userkey", "auditact");
		assert !si.isAudited("otherkey", "auditact");
		assert !si.isAudited("userkey", "otheract");
		assert si.isAudited("userkey", "auditact");
	}
	
}
