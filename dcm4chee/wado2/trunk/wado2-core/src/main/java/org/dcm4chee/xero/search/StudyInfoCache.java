package org.dcm4chee.xero.search;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.dcm4chee.xero.metadata.filter.MemoryCache;
import org.dcm4chee.xero.metadata.filter.SizeableFuture;

/**
 * The study info cache is a place to store temporary shared study information.
 * Thus, common information that this object can contain includes file locations/urls, 
 * default series split/breakdowns, whether a study has been audited etc.
 * 
 * @author bwallace
 *
 */
public class StudyInfoCache extends MemoryCache<String,StudyInfo> {

	private static StudyInfoCache singleton = new StudyInfoCache();
	
	public StudyInfoCache() {
		cacheName = "StudyInfoCache";
	}
	
	/** Gets the default, singleton instance of the study information cache */
	public static final StudyInfoCache getSingleton() {
		return singleton;
	}

	/** Get a study info object for the specified study UID, or create one if none exists 
	 * and return it.
	 * @param studyUid
	 * @return
	 */
	public StudyInfo get(String studyUid) {
		if( studyUid.equals("1") ) return null;
		return this.get(studyUid,new StudyInfoFuture(studyUid));
	}
	
	/** Just create a study info object as required */
	static class StudyInfoFuture implements SizeableFuture<StudyInfo> {

		StudyInfo ret;
		
		StudyInfoFuture(String studyUid) {
			this.ret = new StudyInfo(studyUid);
		}
				
		public boolean cancel(boolean mayInterruptIfRunning) {
			return false;
		}
		
		public StudyInfo get() throws InterruptedException, ExecutionException {
			return ret;
		}

		public StudyInfo get(long timeout, TimeUnit unit)
				throws InterruptedException, ExecutionException,
				TimeoutException {
			return ret;
		}

				public boolean isCancelled() {
			return false;
		}
		
		public boolean isDone() {
			return true;
		}

		/** Default size of all study infos is 1024 - however, longer term we might
		 * want to get a better estimate based on study size etc.
		 */
		public long getSize() {
			return 1024;
		}
		
	};
}
