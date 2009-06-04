package org.dcm4chee.xero.search;

import org.testng.annotations.Test;

/** Tests the basic study info cache methods */
public class StudyInfoCacheTest {

	/** Test the singleton method */
	@Test
	public void singletonTest() {
		assert StudyInfoCache.getSingleton()!=null;
		assert StudyInfoCache.getSingleton()==StudyInfoCache.getSingleton();
	}
	
	/** Tests that a study UID can be retrieved */
	@Test
	public void retrieveTest() {
		assert StudyInfoCache.getSingleton().get("1")==null;
		StudyInfo si = StudyInfoCache.getSingleton().get("1.2.3");
		assert si!=null;
		StudyInfo si2 = StudyInfoCache.getSingleton().get("1.2.3");
		assert si2==si;
	}
}
