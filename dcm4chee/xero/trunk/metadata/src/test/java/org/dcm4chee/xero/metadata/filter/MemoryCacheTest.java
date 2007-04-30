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
package org.dcm4chee.xero.metadata.filter;

import java.util.Map;
import org.easymock.EasyMock;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * This class tests the common WADO access patterns for memory cached
 * data.
 * @author bwallace
 *
 */
public class MemoryCacheTest extends EasyMock {
	
	/** Create a default memory cache - 1 meg stored, 2 accesses required to promote,
	 * and 2 levels L1, and L2.
	 */
	Map<String,CacheItem> memoryCache = new MemoryCache<String,CacheItem>();
	
	static String key1 = "key1";
	static String key2 = "key2";

	CacheItem item1, item2;
	
	/** Initialize some commonly used data */
	@BeforeMethod
	void init() {
		item1 = createMock(CacheItem.class);
		expect(item1.getSize()).andReturn(1024l).anyTimes();
		item2 = createMock(CacheItem.class);
		expect(item2.getSize()).andReturn(20480l).anyTimes();
		replay(item1,item2);
		assert item1!=null;
		assert item2!=null;
	}
	
	/** This tests a simple cached item */
	@Test
	public void testCacheItem() {
		memoryCache.put(key1, item1);
		memoryCache.put(key2, item2);
		assert memoryCache.get(key1)==item1;
		assert memoryCache.get(key2)==item2;
	}
		
	/**
	 * Tests that items get thrown out of cache appropriately at the L1 level.
	 */
	@Test
	public void testCacheSizeL1() {
		memoryCache.put("key0", item1);
		for(int i=1; i<60; i++) {
			if( i==48 ) {
				// Don't cause it to be promoted here - just make it most recent in L1.
				assert memoryCache.get("key0")==item1;
			}
			memoryCache.put("key"+i, item2);
		}
		// Both get and contains key should fail.
		assert memoryCache.get("key1")==null;
		assert memoryCache.containsKey("key1")==false;
		// It had better still have the most recent key.
		assert memoryCache.containsKey("key59");
		// Both get and contains key should succeed since it was more recently referenced.
		assert memoryCache.get("key0")==item1;
		assert memoryCache.containsKey("key0");
	}
	
	/**
	 * Tests that items get promoted to L2 after a subsequent get request.
	 */
	@Test
	public void testCacheSizeL2() {
		memoryCache.put("key0", item1);
		// Promote it to L2 immediately
		assert memoryCache.get("key0")==item1;
		for(int i=1; i<60; i++) {
			memoryCache.put("key"+i, item2);
		}
		// Both get and contains key should fail.
		assert memoryCache.get("key0")==item1;
		assert memoryCache.containsKey("key0");
		// It had better still have the most recent key.
		assert memoryCache.containsKey("key59");
		// But not the oldest l1 keys.
		assert memoryCache.get("key1")==null;
		assert memoryCache.containsKey("key1")==false;
	}

}
