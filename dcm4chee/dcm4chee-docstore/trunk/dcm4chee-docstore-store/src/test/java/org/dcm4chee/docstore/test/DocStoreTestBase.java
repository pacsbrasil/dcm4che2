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
 * Java(TM), available at http://sourceforge.net/projects/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * TIANI Medgraph AG.
 * Portions created by the Initial Developer are Copyright (C) 2003-2005
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Gunter Zeilinger <gunter.zeilinger@tiani.com>
 * Franz Willer <franz.willer@gwi-ag.com>
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
package org.dcm4chee.docstore.test;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.dcm4chee.docstore.DocumentStorageRegistry;
import org.dcm4chee.docstore.DocumentStore;

public class DocStoreTestBase extends TestCase{

    protected static DocumentStorageRegistry registry;
    protected static DocumentStore docStore;

    private static final String testStorageBaseDir="/docstore/test";
    
    private static Logger log = Logger.getLogger( DocStoreTestBase.class );
    private static Set<File> storeDirs;

    protected DocStoreTestBase() {
        init();
    }

    private static void init() {
        if ( registry == null ) {
            MBeanServer mbServer = MBeanServerFactory.createMBeanServer();
            try {
                mbServer.createMBean("org.dcm4chee.docstore.test.DummyDFCommandMBean", new ObjectName("dcm4chee.archive:service=dfcmd"));
            } catch (Exception ignore) {log.error("Can't create TestDFCommandMBean!",ignore);}
            registry = new DocumentStorageRegistry();
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            URL url = cl.getResource("test_docstore_cfg.xml");
            log.info("################## Test docstore cfg file:"+url);
            registry.config(url.toExternalForm());
        }
    }

    protected void initDocStore() throws IOException {
        if ( docStore == null ) {
            DocumentStore.setDocumentStorageRegistry(registry);
            docStore = DocumentStore.getInstance("TestDocStore_TEST", TestUtil.DOMAIN_TEST);
        }
    }

    protected void clearDocumentStorages() throws Exception {
        log.info("Clear document storages in base:"+testStorageBaseDir);
        File f = new File(testStorageBaseDir);
        markStorageSubdirs(f);
        TestUtil.deleteDir(f, false);
    }
    
    private void markStorageSubdirs(File dir) {
        File[] files = dir.listFiles();
        if ( files == null ) return;
        if ( storeDirs == null ) {
            storeDirs = new HashSet<File>();
        }
        Set<File> dirs = new HashSet<File>();
        for ( File f : files ) {
            if ( f.isDirectory()) {
                storeDirs.add(f);
            }
        }
        log.debug("Marked directories:"+storeDirs);
    }

    protected void initDocumentStoragePath() {
        log.info("Create document storages base dir:"+testStorageBaseDir);
        File f = new File(testStorageBaseDir);
        if ( storeDirs != null ) {
            f.mkdirs();
            for ( File subdir : storeDirs ) {
                log.debug("initDocumentStoragePath create dir:"+subdir);
                subdir.mkdirs();
            }
        }
    }
    
}
