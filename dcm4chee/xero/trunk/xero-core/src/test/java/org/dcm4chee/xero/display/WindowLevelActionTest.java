package org.dcm4chee.xero.display;

import org.testng.annotations.Test;

public class WindowLevelActionTest extends DisplayVars
{
	float windowCenter = 15.23f;
	float windowWidth = 76.82f;
	WindowLevelAction wl = new WindowLevelAction();
	
	public WindowLevelActionTest() {
		wl.setLocalStudyModel(model);		
		wl.setWindowCenter(windowCenter);
		wl.setWindowWidth(windowWidth);
		wl.setMode(mode);
	}
	
	@Test
	public void seriesLevelWLTest() throws Exception {
		mode.setApplyLevelStr("series");
		String result = wl.action();
		assert result.equals("success");
		Number num = getXpathNum("/study/series/@windowCenter");
		assert num.floatValue() - windowCenter == 0.0f;
		num = getXpathNum("/study/series/image/@windowCenter");
		assert num==null || Float.isNaN(num.floatValue());
	}

	@Test
	public void imageLevelWLTest() throws Exception {
		mode.setApplyLevelStr("image");
		String result = wl.action();
		assert result.equals("success");
		Number num = getXpathNum("/study/series/image/@windowCenter");
		assert num.floatValue() - windowCenter == 0.0f;
	}
}
