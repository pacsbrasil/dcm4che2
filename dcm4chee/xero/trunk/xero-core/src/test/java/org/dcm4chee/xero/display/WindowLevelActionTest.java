package org.dcm4chee.xero.display;

import org.dcm4chee.xero.search.study.ImageBean;
import org.dcm4chee.xero.search.study.WindowLevelMacro;
import org.testng.annotations.Test;

public class WindowLevelActionTest extends DisplayVars
{
	float windowCenter = 15.23f;
	float windowWidth = 76.82f;
	WindowLevelAction wl = new WindowLevelAction();
	
	public WindowLevelActionTest() {
		wl.setStudyModel(model);		
		wl.setWindowCenter(windowCenter);
		wl.setWindowWidth(windowWidth);
		wl.setMode(mode);
	}
	
	@Test
	public void seriesLevelWLTest() throws Exception {
		mode.setApplyLevelStr("series");
		String result = wl.action();
		assert result.equals("success");
		Number num = getXpathNum("/patient/study/series/@windowCenter");
		assert num.floatValue() - windowCenter == 0.0f;
		num = getXpathNum("/patient/study/series/image/@windowCenter");
		assert num==null || Float.isNaN(num.floatValue());
	}

	@Test
	public void imageLevelWLTest() throws Exception {
		mode.setApplyLevelStr("image");
		String result = wl.action();
		ImageBean ib = model.getImage();
		assert ib!=null;
		WindowLevelMacro wl = (WindowLevelMacro) ib.getMacroItems().findMacro(WindowLevelMacro.class);
		assert wl!=null;
		assert wl.getCenter()==windowCenter;
		assert result.equals("success");
		Number num = getXpathNum("/patient/study/series/image/@windowCenter");
		assert num.floatValue() - windowCenter == 0.0f;
	}
}
