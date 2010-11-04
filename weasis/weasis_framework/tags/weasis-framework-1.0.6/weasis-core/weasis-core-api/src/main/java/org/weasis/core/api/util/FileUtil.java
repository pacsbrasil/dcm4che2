/*******************************************************************************
 * Copyright (c) 2010 Nicolas Roduit.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Nicolas Roduit - initial API and implementation
 ******************************************************************************/
package org.weasis.core.api.util;

import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import javax.imageio.stream.ImageInputStream;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

public class FileUtil {

    public static void safeClose(final Closeable object) {
        try {
            if (object != null) {
                object.close();
            }
        } catch (IOException e) {
            // Do nothing
        }
    }

    public static void safeClose(ImageInputStream stream) {
        try {
            if (stream != null) {
                stream.flush();
                stream.close();
            }
        } catch (IOException e) {
            // Do nothing
        }
    }

    public final static void deleteDirectoryContents(final File dir) {
        if ((dir == null) || !dir.isDirectory()) {
            return;
        }
        final File[] files = dir.listFiles();
        if (files != null) {
            for (final File f : files) {
                if (f.isDirectory()) {
                    deleteDirectoryContents(f);
                } else {
                    try {
                        f.delete();
                    } catch (Exception e) {
                        // Do nothing, wait next start to delete it
                    }
                }
            }
        }
    }

    public static void safeClose(XMLStreamWriter writer) {
        try {
            if (writer != null) {
                writer.close();
            }
        } catch (XMLStreamException e) {
            // Do nothing
        }
    }

    public static void safeClose(XMLStreamReader xmler) {
        try {
            if (xmler != null) {
                xmler.close();
            }
        } catch (XMLStreamException e) {
            // Do nothing
        }
    }

    public static boolean isWriteable(File file) {
        if (file.exists()) {
            // Check the existing file.
            if (!file.canWrite()) {
                return false;
            }
        } else {
            // Check the file that doesn't exist yet.
            // Create a new file. The file is writeable if
            // the creation succeeds.
            try {
                String parentDir = file.getParent();
                if (parentDir != null) {
                    File outputDir = new File(file.getParent());
                    if (outputDir.exists() == false) {
                        // Output directory doesn't exist, so create it.
                        outputDir.mkdirs();
                    } else {
                        if (outputDir.isDirectory() == false) {
                            // File, which have a same name as the output directory, exists.
                            // Create output directory.
                            outputDir.mkdirs();
                        }
                    }
                }

                file.createNewFile();
            } catch (IOException ioe) {
                return false;
            }
        }
        return true;
    }

    public static String nameWithoutExtension(String fn) {
        if (fn == null) {
            return null;
        }
        int i = fn.lastIndexOf('.');
        if (i > 0) {
            return fn.substring(0, i);
        }
        return fn;
    }

    public static boolean writeFile(URL url, File outFilename) {
        InputStream input;
        try {
            input = url.openStream();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        FileOutputStream outputStream;
        try {
            outputStream = new FileOutputStream(outFilename);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        }
        return writFile(input, outputStream);
    }

    public static boolean writFile(InputStream inputStream, OutputStream out) {
        if (inputStream == null && out == null) {
            return false;
        }
        try {
            byte[] buf = new byte[4096];
            int offset;
            while ((offset = inputStream.read(buf)) > 0) {
                out.write(buf, 0, offset);
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            FileUtil.safeClose(inputStream);
            FileUtil.safeClose(out);
        }
    }

}
