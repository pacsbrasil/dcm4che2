/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/

package org.dcm4che2.data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ResourceLocator {

	private static final String PREFIX = "META-INF/dcm4che/";

	public static List findResources(Class c) {
		ArrayList list = new ArrayList();
		try {
			for (Enumeration configs = enumResources(PREFIX + c.getName()); 
					configs.hasMoreElements();) {
				URL u = (URL) configs.nextElement();
				InputStream in = u.openStream();
				try {
					BufferedReader r = new BufferedReader(
							new InputStreamReader(in, "utf-8"));
					String ln;
					while ((ln = r.readLine()) != null) {
						int end = ln.indexOf('#');
						if (end >= 0)
							ln = ln.substring(0, end);
						ln = ln.trim();
						if (ln.length() > 0)
							list.add(ln);
					}
				} finally {
					in.close();
				}
			}
			return list;
		} catch (IOException e) {
			throw new ConfigurationError("Failed to find Resources for " + c, e);
		}
	}

    private static Enumeration enumResources(String name) throws IOException {
        Enumeration e = Thread.currentThread().getContextClassLoader()
                .getResources(name);        
        return e.hasMoreElements() ? e 
                : ResourceLocator.class.getClassLoader().getResources(name);
    }

    public static Object createInstance(String name) {
        try {
            return loadClass(name).newInstance();
        } catch (ClassNotFoundException ex) {
            throw new ConfigurationError("Class not found: " + name, ex); 
        } catch (InstantiationException ex) {
            throw new ConfigurationError("Could not instantiate: " + name, ex); 
        } catch (IllegalAccessException ex) {
            throw new ConfigurationError("could not instantiate: " + name, ex); 
        }        
    }
    
	private static Class loadClass(String name) throws ClassNotFoundException {
        try {
            return Thread.currentThread().getContextClassLoader()
                    .loadClass(name);
        } catch (ClassNotFoundException ex) {
            return ResourceLocator.class.getClassLoader().loadClass(name);
        }
    }

    public static Object loadResource(String name) {
		InputStream is = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream(name);
        if (is == null) {
            is = ResourceLocator.class.getClassLoader()
                    .getResourceAsStream(name);
    		if (is == null) {
    			throw new ConfigurationError("Missing Resource: " + name);
    		}
        }
		try {
			ObjectInputStream ois = new ObjectInputStream(is);
			return ois.readObject();
		} catch (Exception e) {
			throw new ConfigurationError("Failed to load Resource " + name, e);
		} finally {
			try {
				is.close();
			} catch (IOException ignore) {
			}
		}
	}
	
	public static void createResource(String name, Object o, File out)
			throws IOException {
		ZipOutputStream zip = new ZipOutputStream(new FileOutputStream(out));
		try {
			zip.putNextEntry(new ZipEntry(PREFIX + o.getClass().getName()));
			zip.write(name.getBytes("utf-8"));
			zip.putNextEntry(new ZipEntry(name));
			ObjectOutputStream oos = new ObjectOutputStream(zip);
			oos.writeObject(o);
			oos.close();
		} finally {
			zip.close();
		}					
	}

	public static void serializeTo(Object o, File out)
			throws IOException {
		ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(out));		
		try {
			oos.writeObject(o);
		} finally {
			oos.close();
		}					
	}
}
