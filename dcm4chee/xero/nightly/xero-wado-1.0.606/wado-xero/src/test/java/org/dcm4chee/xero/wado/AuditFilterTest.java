package org.dcm4chee.xero.wado;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.stream.MemoryCacheImageInputStream;

import org.apache.log4j.Logger;
import org.dcm4che2.audit.message.ActiveParticipant;
import org.dcm4che2.audit.message.AuditMessage;
import org.dcm4che2.audit.message.ParticipantObject;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.imageio.plugins.dcm.DicomStreamMetaData;
import org.dcm4che2.imageioimpl.plugins.dcm.DicomImageReader;
import org.dcm4che2.imageioimpl.plugins.dcm.DicomImageReaderSpi;
import org.dcm4chee.xero.metadata.MetaDataBean;
import org.dcm4chee.xero.metadata.StaticMetaData;
import org.dcm4chee.xero.metadata.filter.FilterList;
import org.dcm4chee.xero.metadata.filter.MemoryCacheFilter;
import org.dcm4chee.xero.metadata.filter.ParamReturnFilter;
import org.dcm4chee.xero.metadata.servlet.HttpServletRequestImpl;
import org.dcm4chee.xero.metadata.servlet.MetaDataServlet;
import org.dcm4chee.xero.search.filter.AuditFilter;
import org.dcm4chee.xero.search.study.ResultsBean;
import org.easymock.Capture;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.easymock.classextension.EasyMock.*;

/**
 * Tests that the audit filter audits the correct messages, doesn't repeat messages,
 * audits against the correct user etc.
 * 
 * @author bwallace
 *
 */
public class AuditFilterTest {
	static MetaDataBean mdb = StaticMetaData.getMetaData("dicom.metadata");
	static MetaDataBean auditMdb = mdb.getChild("audit");
	static MetaDataBean vauditMdb = mdb.getChild("vaudit");
	static FilterList<?> fl = (FilterList<?>) auditMdb.getValue();
	static FilterList<?> vfl = (FilterList<?>) vauditMdb.getValue();
	static AuditFilter<?> af = (AuditFilter<?>) auditMdb.getValue("message");
	static AuditFilter<?> vaf = (AuditFilter<?>) vauditMdb.getValue("message");
   static DicomImageReaderSpi dicomImageReaderSpi = new DicomImageReaderSpi();

	/**
	 * A pair of studies in the same STUDY instance UID
	 */
	static String[] STUDY_FILES = new String[]{"sr/601/sr_601cr.dcm", "sr/601/sr_601cr_v.dcm"};
	static String[] QUERY_FILES = new String[]{"sr/601/sr_601cr.dcm","sr/1402/sr_1402_1.dcm"};
	
	Capture<AuditMessage> auditCapture;
	Map<String,Object> params;
	Logger log;

	/** Setup the log and audit filter, and setup to expect 1 call to the log.info method. */
	@BeforeMethod
	public void init() {
		auditCapture = new Capture<AuditMessage>();
		params = new HashMap<String,Object>();
		log = createMock( Logger.class );
		log.info(capture(auditCapture));
		expectLastCall();
		af.setAuditLogger(log);
		vaf.setAuditLogger(log);
		replay(log);
	}

	/** Tests that simple audit messages are created - null value, exception and
	 * returned value.
	 */
	@Test
	public void simpleViewAuditTest() {
		vfl.filter(null,params);
		verify(log);
		assert auditCapture.hasCaptured();
		AuditMessage am =  auditCapture.getValue();
		assert am!=null;
	}
	
	/** Tests that simple audit messages are created.
	 */
	@Test
	public void simpleQueryAuditTest() throws Exception {
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		ResultsBean rb = new ResultsBean();
		StringBuffer sb = new StringBuffer("studyUID=");
		boolean first = true;
		for(String file : QUERY_FILES) {
			DicomImageReader dir = (DicomImageReader) dicomImageReaderSpi.createReaderInstance();
			InputStream is = cl.getResourceAsStream(file);
			dir.setInput(new MemoryCacheImageInputStream(is));
			DicomObject ds = ((DicomStreamMetaData)dir.getStreamMetadata()).getDicomObject();
			rb.addResult(ds);
			if( first ) first = false;
			else sb.append("\\");
			sb.append(ds.getString(Tag.StudyInstanceUID));
			dir.dispose();
		}
		params.put(MemoryCacheFilter.KEY_NAME,sb.toString());
		params.put(ParamReturnFilter.RETURN_KEY, rb);
		params.put(MetaDataServlet.USER_KEY, "testuser");
		
		fl.filter(null,params);
		verify(log);
		assert auditCapture.hasCaptured();
		AuditMessage am =  auditCapture.getValue();
		assert am!=null;

		ParticipantObject poQuery =  am.getParticipantObjects().get(0);
		assert poQuery.getParticipantObjectID().equals(sb.toString());
		assert poQuery.getParticipantObjectIDTypeCode()==ParticipantObject.IDTypeCode.SEARCH_CRITERIA;
		
		ActiveParticipant poSrc = am.getActiveParticipants().get(0);
		assert poSrc!=null;
		ActiveParticipant poDest = am.getActiveParticipants().get(1);
		assert poDest!=null;
	}
	
	/** Tests that a single audit message is generated per audit user/study/type for the
	 * instance viewed audit types
	 */
	@Test
	public void singleViewAuditMessageTest() throws Exception {
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		assert STUDY_FILES.length>1;
		DicomObject ds = null;
		for(String file : STUDY_FILES) {
			DicomImageReader dir = (DicomImageReader) dicomImageReaderSpi.createReaderInstance();
			InputStream is = cl.getResourceAsStream(file);
			dir.setInput(new MemoryCacheImageInputStream(is));
			params.put(ParamReturnFilter.RETURN_KEY, dir);
			ds = ((DicomStreamMetaData)dir.getStreamMetadata()).getDicomObject();
			vfl.filter(null,params);
		}
		// Also check it works with DicomObject directly
		params.put(ParamReturnFilter.RETURN_KEY,ds);
		vfl.filter(null,params);
		verify(log);
		assert auditCapture.hasCaptured();
		AuditMessage am =  auditCapture.getValue();
		assert am!=null;
		// Now check that patient ID, name and study UID are correctly audited.
		String pid = ds.getString(Tag.PatientID);
		String pname = ds.getString(Tag.PatientName);
		String studyUid = ds.getString(Tag.StudyInstanceUID);
		List<ParticipantObject> lpo = am.getParticipantObjects();
		assert lpo.size()>0;
		// First the patient, then the study are the participant objects - if the remote
		// system was specified, it would be first.  This would be better to search for in terms
		// of robustness, but for now this is good enough.
		ParticipantObject poPat = lpo.get(0);
		ParticipantObject poStd = lpo.get(1);
		assert poPat.getParticipantObjectID().equals(pid);
		assert poPat.getParticipantObjectName().equals(pname);
		assert poStd.getParticipantObjectID().equals(studyUid);
	}
	
	/**
	 * Tests that the user name and remote service are correctly audited.
	 * @throws Exception
	 */
	@Test
	public void userAuditTest() throws Exception {
		HttpServletRequestImpl request = new HttpServletRequestImpl();
		request.setQueryString("requestType=WADO&studyUID=2&seriesUID=3&objectUID=4");
		request.setRequestURL("http://localhost/wado2/wado");
		params.put(MetaDataServlet.REQUEST, request);
		params.put(MetaDataServlet.USER_KEY, "testuser");
		vfl.filter(null,params);
		verify(log);
		assert auditCapture.hasCaptured();
		AuditMessage am =  auditCapture.getValue();
		assert am!=null;
		List<ActiveParticipant> lap = am.getActiveParticipants();
		assert lap!=null;
		assert lap.size()>=2;
		// User name is the second instance.
		ActiveParticipant ap = lap.get(1);
		assert "testuser".equals(ap.getUserID());
		
		List<ParticipantObject> lpo = am.getParticipantObjects();
		assert lpo!=null;
		assert lpo.size()>=1;
		ParticipantObject po = lpo.get(0);
		assert po!=null;
		assert po.getParticipantObjectID().equals(request.getRequestURL().toString());
	}
	
}
