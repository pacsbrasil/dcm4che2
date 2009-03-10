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
 * Dave Smith & Laura Peters, Agfa HealthCare Inc., 
 * Portions created by the Initial Developer are Copyright (C) 2007
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Laura Peters <laura.peters@agfa.com>
 * David Smith <david.smith@agfa.com>
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
package org.dcm4chee.xero.search.filter;

import static org.testng.Assert.*;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.dcm4chex.archive.ejb.interfaces.FileDTO;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class FileLocationURLFilterTest {
	private FileLocationURLFilter filter;
	private Map<String, Object> params;
	private FileDTO inputDTO;
	private static String baseDir = "SomeDir"; 
	private static String path = "SomePath.file"; 
	@BeforeMethod
	public void setup() {
		inputDTO = new FileDTO();
		inputDTO.setDirectoryPath(baseDir);
		inputDTO.setFilePath(path);
		params = new HashMap<String, Object>();
		filter = new FileLocationURLFilter();	
	}
	
	@Test
	public void filterTest_noDTO_expectIllegalArgumentException() {
		try {
			filter.filter(null, params);
			fail("Expected IllegalArgumentException");
		} catch (IllegalArgumentException e) {
			//SUCCESS!!!
		}
		
	}

	@Test
	public void filterTest_OnlineRelativePath_expectFileUrl() throws MalformedURLException {
		params.put(FileLocationURLFilter.FILE_DTO, inputDTO);
		URL fileUrl = filter.filter(null, params);
		assertEquals(fileUrl, new URL("file:../server/default/SomeDir/SomePath.file"));
	}

    @Test
    public void filterTest_OnlineAbsolutePath_expectFileUrl() throws MalformedURLException {
        inputDTO.setDirectoryPath("/"+baseDir);
        params.put(FileLocationURLFilter.FILE_DTO, inputDTO);
        URL fileUrl = filter.filter(null, params);
        assertEquals(fileUrl, new URL("file:///SomeDir/SomePath.file"));

        inputDTO.setDirectoryPath("\\"+baseDir);
        params.put(FileLocationURLFilter.FILE_DTO, inputDTO);
        fileUrl = filter.filter(null, params);
        assertEquals(fileUrl, new URL("file:///SomeDir/SomePath.file"));
    }

    @Test
    public void filterTest_OnlineWindowsDirPath_expectFileUrl() throws MalformedURLException {
        inputDTO.setDirectoryPath("C:\\"+baseDir);
        params.put(FileLocationURLFilter.FILE_DTO, inputDTO);
        URL fileUrl = filter.filter(null, params);
        assertEquals(fileUrl, new URL("file:///C:/SomeDir/SomePath.file"));
    }

	/**
	 * 
	 * This test highlights a problem constructing a URL for NEARLINE locations
	 * @throws MalformedURLException 
	 * 
	 * @throws MalformedURLException
	 */
	@Test
	public void filterTest_NearlineCacheFile_expectTarUrl() throws MalformedURLException {
		inputDTO.setDirectoryPath("tar://SomeDir");
		params.put(FileLocationURLFilter.FILE_DTO, inputDTO);
		URL fileUrl = filter.filter(null, params);
		assertNull(fileUrl);
	}	
}
