/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is part of dcm4che, an implementation of DICOM(TM) in
 * Java(TM), hosted at http://sourceforge.net/projects/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * Bill Wallace, Agfa HealthCare Inc., 
 * Portions created by the Initial Developer are Copyright (C) 2007
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Bill Wallace <bill.wallace@agfa.com>
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 *
 * ***** END LICENSE BLOCK ***** */
package org.dcm4chee.xero.performance;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.dcm4chee.xero.search.study.DicomObjectType;
import org.dcm4chee.xero.search.study.ImageType;
import org.dcm4chee.xero.search.study.PatientType;
import org.dcm4chee.xero.search.study.SeriesType;
import org.dcm4chee.xero.search.study.StudyType;
import org.dcm4chee.xero.search.study.ResultsType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static org.dcm4chee.xero.metadata.servlet.MetaDataServlet.nanoTimeToString;

/**
 * This class supports testing URL retrieval, in a varying number of threads.
 * URL's can have child URL's that are all retrieved after the primary URL is
 * retrieved. They can even be dependent on the primary URL. This can be nested
 * however deep is required to get all the necessary data. Each type/level of
 * URL is reported independently.
 * 
 * @author bwallace
 * 
 */
public class TestUrlRetrieve {
   public static String BASE_URL = "http://localhost/";
   public static String WADO2_URL = BASE_URL+"wado2/";
   public static String XERO_URL = BASE_URL+"xero/";

   private static Logger log = LoggerFactory.getLogger(TestUrlRetrieve.class);

   enum UrlLevel {
	  STUDY, SERIES, SERIES_IMAGE, IMAGE, IMAGE_PAGE, WADO, THUMBNAIL, SEAM_IMAGE, ACTION, ORIG, VIEW,
   };

   static JAXBContext context;
   static {
	  try {
		 context = JAXBContext.newInstance("org.dcm4chee.xero.search.study");
	  } catch (JAXBException e) {
		 // TODO Auto-generated catch block
		 e.printStackTrace();
	  }
   }

   int numberOfOperations = 0;

   long totalTime;

   int threadCount;

   int rate;

   UrlLevel level;

   List<TimeRunnable> runnables = new ArrayList<TimeRunnable>();

   public TestUrlRetrieve(UrlLevel level, int threadCount, int rate) {
	  this.threadCount = threadCount;
	  this.rate = rate;
	  this.level = level;
   }

   /**
     * Runs the test and prints out the test results.
     */
   public void testAndPrintResults() throws InterruptedException {
	  if (runnables.size() == 0) {
		 System.out.println("For level " + level + " there were no items to retrieve.");
		 return;
	  }
	  CountDownLatch latch = new CountDownLatch(runnables.size());
	  long totalStart = System.nanoTime();
	  if (rate == 0) {
		 Executor executor = Executors.newFixedThreadPool(threadCount);
		 for (TimeRunnable r : runnables) {
			r.latch = latch;
			executor.execute(r);
		 }
	  } else {
		 ScheduledExecutorService executor = Executors.newScheduledThreadPool(12);
		 int i = 0;
		 long startTimeMicro = 1000000l / this.rate;
		 for (TimeRunnable r : runnables) {
			r.latch = latch;
			executor.schedule(r, i * startTimeMicro, TimeUnit.MICROSECONDS);
			i++;
		 }
	  }
	  long subItems = 0;
	  latch.await();
	  long totalDur = System.nanoTime() - totalStart;
	  long totalSize = 0;
	  for (TimeRunnable tru : runnables) {
		 totalTime += tru.dur;
		 subItems += tru.subItems;
		 totalSize += tru.size;
	  }
	  System.out.println("Level " + level + " " + nanoTimeToString(totalTime / runnables.size()) + " avg " + " for " + runnables.size()
			+ " overall items, overall total average=" + nanoTimeToString(totalDur / runnables.size()) + " avg size="+(totalSize/runnables.size())+" total size="+totalSize);
   }

   static class TimeRunnable implements Runnable {
	  long dur;

	  long subItems;
	  
	  long size;

	  CountDownLatch latch;
	  
	  String surl;
	  public TimeRunnable() {};
	  public TimeRunnable(String surl) {
		 this.surl =surl;
	  }

	  public void run() {
		 long start = System.nanoTime();
		 try {
			subItems = runLoadUrl();
		 } catch (Exception e) {
			e.printStackTrace();
		 }
		 dur = System.nanoTime() - start;
		 latch.countDown();
	  }

	  public int runLoadUrl() throws Exception {
		 readFully(surl);
		 return 1;
	  }

	  public void readFully(String urlS) throws Exception {
		 URL url = new URL(urlS);
		 URLConnection conn = url.openConnection();
		 InputStream is = conn.getInputStream();
		 byte[] data = new byte[10240];
		 int b = is.read(data);
		 while (b>0) {
			size += b;
			b = is.read(data);
		 }
		 is.close();
	  }

   };

   static TestUrlRetrieve studyRet = new TestUrlRetrieve(UrlLevel.STUDY, 3, 0);

   static TestUrlRetrieve seriesRet = new TestUrlRetrieve(UrlLevel.SERIES, 3, 0);
   
   static TestUrlRetrieve seriesImageRet = new TestUrlRetrieve(UrlLevel.SERIES_IMAGE, 1, 0);

   static TestUrlRetrieve imageRet = new TestUrlRetrieve(UrlLevel.IMAGE, 3, 0);

   static TestUrlRetrieve imagePageRet = new TestUrlRetrieve(UrlLevel.IMAGE_PAGE, 2, 0);

   static TestUrlRetrieve wadoRet = new TestUrlRetrieve(UrlLevel.WADO, 3, 0);
   

   static TestUrlRetrieve thumbRet = new TestUrlRetrieve(UrlLevel.THUMBNAIL, 2, 0);
   
   static TestUrlRetrieve seamRet = new TestUrlRetrieve(UrlLevel.SEAM_IMAGE, 2, 0);
   
   static TestUrlRetrieve actionRet = new TestUrlRetrieve(UrlLevel.ACTION, 8, 0);
   
   static TestUrlRetrieve origRet = new TestUrlRetrieve(UrlLevel.ORIG, 2, 0);
   
   static TestUrlRetrieve viewRet = new TestUrlRetrieve(UrlLevel.VIEW,2,0);

   static int count = 6;

   /***************************************************************************
     * Run the test retrieves. Format is LEVEL? {studyUID} where level is one of
     * -s retrieves study.xml only - on call for all UID's provided) -e add
     * series.xml -i add image.xml for first set of images -g add grouped
     * image.xml for the remaining images, in sets of size count -w add wado
     * retrieve for the images. All retrieves of a given type/level are
     * retrieved in separate groups. -t # Number of concurrent retrieves -r RATE
     * Attempted rate to retrieve at (makes threads sleep until the next start
     * time is applicable, uses a lot of threads to allow overlapping start
     * times) -c CNT Retrieve images in groups of the given size.
     * 
     * @param args
     */
   public static void main(String[] args) throws Exception {

	  for (int i = 0; i < args.length; i++) {
		 String a = args[i];
		 if (!a.startsWith("-")) {
			studyRet.runnables.add(new StudyRun(a));
			seriesRet.runnables.add(new SeriesRun(a));
		 } else if (a.equals("-t")) {
			studyRet.threadCount = Integer.parseInt(args[i + 1]);
			seriesRet.threadCount = studyRet.threadCount;
			i++;
		 } else if (a.equals("-r")) {
			studyRet.rate = Integer.parseInt(args[i + 1]);
			seriesRet.rate = studyRet.rate;
			i++;
		 } else {
			log.warn("Unknown option " + a);
		 }
	  }
	  if (studyRet.runnables.size() == 0) {
		 // 14 image CR String uid = "1.2.124.113532.128.5.1.74.20020108.75343.563790";
		 // String uid="1.2.124.113532.132.183.36.32.20040429.112406.11120792";
		 // 451 image single series 
		 String uid="1.2.124.113532.193.190.36.23.20030729.152309.4144348";
		 // 400 image study		 String uid="1.2.124.113532.128.5.1.74.20020131.162815.589479"; 
		 studyRet.runnables.add(new StudyRun(uid));
		 seriesRet.runnables.add(new SeriesRun(uid));

		 seriesImageRet.runnables.add(new TimeRunnable(WADO2_URL+"cfind?level=image&studyUID="+uid));

	  }
	  
	  for(int i=0; i<100; i++) viewRet.runnables.add(new TimeRunnable(BASE_URL+"xview/xero"));


	  long overallStart = System.nanoTime();
	  studyRet.testAndPrintResults();
	  seriesRet.testAndPrintResults();
	  seriesImageRet.testAndPrintResults();
	  imageRet.testAndPrintResults();
	  imagePageRet.testAndPrintResults();
	  viewRet.testAndPrintResults();
	  //seamRet.testAndPrintResults();
	  //actionRet.testAndPrintResults();
	  //thumbRet.testAndPrintResults();
	  origRet.testAndPrintResults();
	  wadoRet.testAndPrintResults();
	  System.out.println("Total time for all operations "+nanoTimeToString(System.nanoTime()-overallStart));
	  System.exit(0);
   }

   static class StudyRun extends TimeRunnable {
	  String uid;

	  public StudyRun(String uid) {
		 this.uid = uid;
	  }

	  @Override
	  public int runLoadUrl() throws Exception {
		 String url = WADO2_URL+"study.xml?StudyInstanceUID=" + uid;
		 readFully(url);
		 return 1;
	  }

   };

   static class SeriesRun extends TimeRunnable {
	  String uid;

	  Unmarshaller unmarshaller;

	  public SeriesRun(String uid) throws JAXBException {
		 this.uid = uid;
		 unmarshaller = context.createUnmarshaller();
	  }

	  @Override
	  public int runLoadUrl() throws Exception {
		 String urlS = WADO2_URL+"series.xml?studyUID=" + uid;
		 URL url = new URL(urlS);
		 System.out.println("Series URL="+urlS);
		 ResultsType rt = (ResultsType) ((JAXBElement) unmarshaller.unmarshal(url)).getValue();
		 int ret = 0;
		 for (PatientType pt : rt.getPatient()) {
			for (StudyType st : pt.getStudy()) {
			   for (SeriesType se : st.getSeries()) {
				  imageRet.runnables.add(new ImageRun(se.getSeriesInstanceUID()));
				  if( se.getDicomObject().size()>0 && se.getDicomObject().get(0) instanceof ImageType ) {
					 ImageType image = (ImageType) se.getDicomObject().get(0);
					 WadoRun wado = new WadoRun(image.getObjectUID(),null);
					 wado.thumb = true;
					 thumbRet.runnables.add(wado);
					 wado = new WadoRun(image.getObjectUID(), null);
					 wado.useOrig = true;
					 origRet.runnables.add(wado);
				  }
				  seamRet.runnables.add(new TimeRunnable(XERO_URL+"image/image.seam?studyUID="+st.getStudyInstanceUID()+"&seriesUID="+se.getSeriesInstanceUID()));
				  actionRet.runnables.add(new TimeRunnable(XERO_URL+"image/action/WindowLevel.seam?windowWidth=32767&windowCenter=65536&studyUID="+st.getStudyInstanceUID()+"&seriesUID="+se.getSeriesInstanceUID()));
				  ret++;
			   }
			}
		 }
		 return ret;
	  }

   };

   static class ImageRun extends TimeRunnable {
	  String uid;

	  Unmarshaller unmarshaller;

	  static Map<String, Integer> frameNumber = new HashMap<String, Integer>();

	  int pos = 0;

	  public ImageRun(String uid) throws JAXBException {
		 this.uid = uid;
		 unmarshaller = context.createUnmarshaller();
	  }

	  public ImageRun(String uid, int pos) throws JAXBException {
		 this(uid);
		 this.pos = pos;
	  }

	  @Override
	  public int runLoadUrl() throws Exception {
		 String urlS = WADO2_URL+"image.xml?Position=" + pos + "&Count=" + count + "&SeriesInstanceUID=" + uid;
		 URL url = new URL(urlS);
		 ResultsType rt = (ResultsType) ((JAXBElement) unmarshaller.unmarshal(url)).getValue();
		 int ret = 0;
		 for (PatientType pt : rt.getPatient()) {
			for (StudyType st : pt.getStudy()) {
			   for (SeriesType se : st.getSeries()) {
				  if (this.pos == 0) {
					 int viewable = se.getViewable();
					 for (int addPos = count; addPos < viewable; addPos += count) {
						imagePageRet.runnables.add(new ImageRun(uid, addPos));
					 }
				  }
				  for (DicomObjectType dot : se.getDicomObject()) {
					 ret++;
					 if (dot instanceof ImageType) {
						synchronized (wadoRet) {
						   Integer frame = null;
						   frame = frameNumber.get(dot.getObjectUID());
						   WadoRun wr = new WadoRun(dot.getObjectUID(), frame);
						   //wr.useMime = true;
						   wadoRet.runnables.add(wr);
						   if (frame == null)
							  frame = 2;
						   else
							  frame = frame + 1;
						   frameNumber.put(dot.getObjectUID(), frame);
						}
					 }
				  }
			   }
			}
		 }
		 return ret;
	  }

   };

   static class WadoRun extends TimeRunnable {
	  String uid;

	  Integer frame;
	  
	  boolean thumb=false;
	  
	  boolean useOrig=false;
	  boolean useMime=false;

	  public WadoRun(String uid, Integer frame) {
		 this.uid = uid;
		 this.frame = frame;
	  }

	  @Override
	  public int runLoadUrl() throws Exception {
		 String url = WADO2_URL+"wado?objectUID=" + uid;
		 if( thumb ) {
			url = url+"&rows=128";
		 }
		 if (frame != null)
			url = url + "&frameNumber=" + frame;
		 if( useOrig ) {
			url = url + "&contentType=application/dicom&transferSyntax=1.2.840.10008.1.2.4.51";
			log.info("Original URL="+url);
		 }
		 else if( useMime ) {
			url = url + "&contentType=image/jpeg,image/jpll";
		 }
		 else {
			url = url + "&contentType=image/jp12";
		 }
		 readFully(url);
		 return 1;
	  }

   };
      
}
