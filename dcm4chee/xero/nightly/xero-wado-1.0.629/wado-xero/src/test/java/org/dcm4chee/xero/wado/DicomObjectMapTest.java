package org.dcm4chee.xero.wado;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateGroup;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.io.DicomInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

/**
 * Tests that the DicomObjectMap can be used as the model object to correctly encode reports etc.
 * 
 * @author bwallace
 */
public class DicomObjectMapTest {
	static final Logger log = LoggerFactory.getLogger(DicomObjectMapTest.class);

	static final ClassLoader cl = Thread.currentThread().getContextClassLoader();

	static final URL rootRes = cl.getResource("reports/base");
	static final String rootDir = rootRes.getFile();
	
	protected static DicomObject readDicomObject(String resource) throws IOException {
		assert cl!=null;
		URL res = cl.getResource(resource);
		log.info("Found dicom object resource "+res);
		BufferedInputStream bis = new BufferedInputStream(res.openStream());
		DicomInputStream dis = new DicomInputStream(bis);
		return dis.readDicomObject();
	}

	@Test
	public void topLevelEncodeTest() throws Exception {
		log.info("rootRes={}",rootRes);
		DicomObject ds = readDicomObject("sr/601/sr_601cr.dcm");
		assert ds!=null;
		StringTemplateGroup stg = new StringTemplateGroup("reports.base",rootDir);
		Map<String,Object> model = new HashMap<String,Object>();
		model.put("ds",new DicomObjectMap(ds));
		StringTemplate st = stg.getInstanceOf("xml",model);
		String s = st.toString();
		//log.info("Text for sr_601cr.dcm is {}", s);
		assert s.indexOf("<dataset>")>0;
		assert s.indexOf("<attr tag=\"00080016\" vr=\"UI\" vm=\"1\" len=\"30\">1.2.840.10008.5.1.4.1.1.88.11</attr>")>0;
		assert s.indexOf("<attr tag=\"00080030\" vr=\"TM\" vm=\"0\" len=\"0\"/>")>0;
	}
}
