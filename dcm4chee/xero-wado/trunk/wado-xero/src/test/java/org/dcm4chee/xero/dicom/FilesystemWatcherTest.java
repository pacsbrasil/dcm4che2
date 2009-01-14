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
package org.dcm4chee.xero.dicom;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;


public class FilesystemWatcherTest
{
   private File tempFile;
   private FilesystemWatcher watcher;
   private ScheduledExecutorService executor;

   
   @BeforeClass
   public void setupTestingDirectory() throws IOException
   {
      tempFile = File.createTempFile("UnitTest", "FileSystemWatcher");
   }
   
   @AfterClass
   public void deleteTestingFile()
   {
      tempFile.delete();
   }
   
   @BeforeMethod
   public void setup()
   {
      watcher = new FilesystemWatcher();
      executor = Executors.newSingleThreadScheduledExecutor();
   }
   
   @AfterMethod
   public void tearDown()
   {
      watcher = null;
      executor.shutdown();
      executor = null;
   }
   
   /**
    * Test method for {@link org.dcm4chee.xero.dicom.FilesystemWatcher#openStream(java.lang.String)}.
    * @throws FileNotFoundException 
    * @throws ExecutionException 
    * @throws InterruptedException 
    */
   @Test
   public void testOpenStream_ShouldReturnAnInputStream_WhenFileIsAvailable() throws FileNotFoundException, InterruptedException, ExecutionException
   {
      Future<InputStream> futureIS = watcher.openWhenAvailable(tempFile);
      assertNotNull(futureIS.get());
   }
   
   @Test
   public void testOpenStream_ShouldReturnAnInputStream_WhenFileBecomesAvailable() throws IOException, InterruptedException, ExecutionException
   {
      File notYetCreated = new File(tempFile.getCanonicalPath()+"_2");
      try
      {
         assertFalse(notYetCreated.exists());
         CreateFile createCmd = new CreateFile(notYetCreated);
         Future<Boolean> result = executor.schedule(createCmd, 20, TimeUnit.MILLISECONDS);
         Future<InputStream> futureIS = watcher.openWhenAvailable(notYetCreated);
         assertNotNull(futureIS.get());
         assertTrue(result.get(), "File must be created");
      }
      finally
      {
         notYetCreated.delete();
      }
   }

   @Test
   public void testIncrementalDelay_ShouldIncrementDelayWhenInvoked()
   {
      FilesystemWatcher.IncrementalDelay d = new FilesystemWatcher.IncrementalDelay(100,50);
      assertEquals(d.getDelay(),100);
      assertEquals(d.getDelay(),150);
      assertEquals(d.getDelay(),200);
   }
   
   @Test
   public void testOpenStream_WorksForOpenFiles() throws Exception, ExecutionException
   {
      FileOutputStream fos = null;
      InputStream fis = null;
      try
      {
         fos = new FileOutputStream(tempFile);
         fos.write(5);
         Future<InputStream> futureIS = watcher.openWhenAvailable(tempFile);
         fis = futureIS.get();
         assertEquals(fis.read(),5);
      }
      finally
      {
         if(fos != null) fos.close();
         if(fis != null) fis.close();
      }
   }

   public class CreateFile implements Callable<Boolean>
   {
      private final File file;
      public CreateFile(File file)
      {
         this.file = file;
      }

      public Boolean call() throws Exception
      {
         return file.createNewFile();
      }
      
   }
}
