package org.dcm4chee.xero.template;

import org.dcm4chee.xero.metadata.MetaDataBean;
import org.dcm4chee.xero.metadata.StaticMetaData;
import org.dcm4chee.xero.metadata.access.MapWithDefaults;
import org.testng.annotations.Test;

/** Tests that the map with defaults works as expected. */
public class MapWithDefaultsTest {

	@Test
	public void testMap() {
		  MetaDataBean root = StaticMetaData.getMetaData("util-test.metadata");
		  MetaDataBean mdb = root.get("model");
		  MapWithDefaults mwd = new MapWithDefaults(mdb);
		  assert mwd.get("testKey").equals("testKeyValue");
		  assert mwd.get("doesNotExist")==null;
		  assert mwd.get("testFactory").equals(TestFactory.staticValue);
	}
}
