package org.dcm4chee.xero.controller;

import static org.easymock.classextension.EasyMock.*;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.dcm4chee.xero.metadata.filter.FilterItem;
import org.dcm4chee.xero.metadata.filter.FilterUtil;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class LanguageThemeControllerTest {

	LanguageThemeController<Object> ltc = new LanguageThemeController<Object>();
	FilterItem<Object> filterItem;
	Map<String,Object> params, model;
	
	@SuppressWarnings("unchecked")
	@BeforeMethod
	public void init() {
		filterItem = createMock(FilterItem.class);
		params = new HashMap<String,Object>();
		model = new HashMap<String,Object>();
		params.put("model", model);
		expect(filterItem.callNextFilter(params)).andReturn(null);
		replay(filterItem);
	}
	
	@Test
	public void test_theme_and_language_default() {
		params.put("locale", Locale.CANADA);
		ltc.filter(filterItem, params);
		verify(filterItem);
		Map<String,Object> i18n = FilterUtil.getMap(params,"model.i18n");
		assert i18n!=null;
		assert i18n.get("themeName").equals("Default Theme");
		assert i18n.get("Language").equals("English");
	}
	
	@Test
	public void test_theme_and_language_custom() {
		params.put("theme", "custom");
		params.put("locale", Locale.CANADA_FRENCH);
		ltc.filter(filterItem, params);
		verify(filterItem);
		Map<String,Object> i18n = FilterUtil.getMap(params,"model.i18n");
		assert i18n!=null;
		assert i18n.get("themeName").equals("Custom Theme");
		assert i18n.get("Language").equals("Français");
	}
}
