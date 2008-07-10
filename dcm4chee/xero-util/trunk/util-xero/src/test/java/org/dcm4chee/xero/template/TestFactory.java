package org.dcm4chee.xero.template;

import java.util.Map;

/** A simple factory that returns a fixed object */
public class TestFactory implements MapFactory {
	public static Object staticValue = new Object();
	
	@Override
	public Object create(Map<String, Object> src) {
		return staticValue;
	}

}
