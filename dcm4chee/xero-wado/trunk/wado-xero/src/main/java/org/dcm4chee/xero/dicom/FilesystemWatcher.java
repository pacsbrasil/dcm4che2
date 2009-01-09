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

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class that watches for a particular file on the file system and will create an input stream to it
 * when it is available.
 * 
 * @author Andrew Cowan (amidx)
 */
public class FilesystemWatcher // Disposable
{
   private static final Logger log = LoggerFactory.getLogger(FilesystemWatcher.class);
   
   private ExecutorService executor = Executors.newCachedThreadPool();

   /**
    * Open the input stream when it is available. The {@link Future} interface is used to allow the
    * caller maximum flexibility on how to wait for the incoming file to be ready.
    */
   public Future<InputStream> openWhenAvailable(File file)
   {
      if (file == null)
         throw new IllegalArgumentException("Cannot watch for a NULL file");

      log.debug("Watching for {} to be created.",file);
      IncrementalDelay delay = new IncrementalDelay(50,50);
      OpenFileWhenAvailable openFileTask = new OpenFileWhenAvailable(file,delay);
      return executor.submit(openFileTask);
   }

   // TODO: Create a decaying file check...
   private static class OpenFileWhenAvailable implements Callable<InputStream>
   {
      private final File file;
      private final IncrementalDelay delay;

      OpenFileWhenAvailable(File file, IncrementalDelay delay)
      {
         this.file = file;
         this.delay = delay;
      }

      public InputStream call() throws Exception
      {
         while (!file.exists())
         {
            synchronized (file)
            {
               file.wait(delay.getDelay());
            }
         }

         // Throws FileNotFoundException if the file isn't available.
         return new FileInputStream(file);
      }

   }

   public void dispose()
   {
      executor.shutdownNow();
   }
   
   /**
    * Fun little class that tracks a decaying delay time for the client.
    */
   public static class IncrementalDelay
   {
      private int delay;
      private final int increment;

      public IncrementalDelay(int initialDelay,int increment)
      {
         this.delay = Math.max(0,initialDelay - increment);
         this.increment = increment;
      }
      
      public int getDelay()
      {
         return this.delay += this.increment;
      }
   }
}
