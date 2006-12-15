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
 * Gunter Zeilinger, Huetteldorferstr. 24/10, 1150 Vienna/Austria/Europe.
 * Portions created by the Initial Developer are Copyright (C) 2002-2005
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * See listed authors below.
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
 
package org.dcm4che2.audit.message;

import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @version $Revision$ $Date$
 * @since Nov 17, 2006
 */
class BaseElement {
    
    private static String dateTimeFormat = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
    protected static String encoding = 
        System.getProperty("file.encoding", "ISO-8859-1");

    protected final String name;
    protected LinkedHashMap atts;
    private boolean readOnly = false;
    
    public BaseElement(String name) {
        this.name = name;
    }
    
    public BaseElement(String name, String attr, String val) {
        this.name = name;
        addAttribute(attr, val);
    }

    public static final String getEncoding() {
        return encoding;
    }

    public static final void setEncoding(String encoding) 
            throws UnsupportedEncodingException {
        "".getBytes(encoding);
        BaseElement.encoding = encoding;
    }

    public static final String getDateTimeFormat() {
        return dateTimeFormat;
    }

    public static final void setDateTimeFormat(String pattern) {
        if (pattern == null) {
            throw new NullPointerException();
        }
        BaseElement.dateTimeFormat = pattern;
    }

    protected void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }
    
    protected void addAttribute(String name, Object val) {
        if (readOnly) {
            throw new IllegalArgumentException("read only");
        }
        if (val == null) {
            throw new NullPointerException(name);
        }
        if (val instanceof String && ((String) val).length() == 0) {
            throw new IllegalArgumentException("empty value");
        }
        if (atts == null) {
            atts = new LinkedHashMap();
        }
        atts.put(name, val);
    }
    
    protected Object getAttribute(String name) {
        return atts.get(name);
    }

    public void output(Writer out) throws IOException {
        out.write('<');
        out.write(name);
        if (atts != null) {
            outputAtts(out);
        }
        if (isEmpty()) {
            out.write('/');
        } else {
            out.write('>');
            outputContent(out);
            out.write('<');
            out.write('/');
            out.write(name);
        }
        out.write('>');
    }
    

    public String toString() {
        return toString(64);
    }
    
    public String toString(int initialSize) {
         StringWriter sw = new StringWriter(initialSize);
         try {
            output(sw);
         } catch (IOException e) {
            // Should never happens
            throw new Error(e);
        }
        return sw.toString();
    }
    
    private void outputAtts(Writer out) throws IOException {
        for (Iterator iter = atts.entrySet().iterator(); iter.hasNext();) {
            Map.Entry entry = (Map.Entry) iter.next();
            out.write(' ');
            out.write(entry.getKey().toString());
            out.write('=');
            outputAttValue(out, entry.getValue());
        }
    }

    protected boolean isEmpty() {
        return true;
    }

    protected void outputContent(Writer out) throws IOException {
    }
    
    protected void outputChilds(Writer out, List childs) throws IOException {
        for (Iterator iter = childs.iterator(); iter.hasNext();) {
            ((BaseElement) iter.next()).output(out);            
        }
    }
    
    private void outputAttValue(Writer out, Object val) throws IOException {
        if (val instanceof Date) {
            out.write('"');
            out.write(toDateTimeStr((Date) val));
            out.write('"');
        } else if (val instanceof byte[]) {
            out.write('"');
            out.write(Base64Encoder.encode((byte[]) val));
            out.write('"');
        } else {
            String str = val.toString();
            int quote = '"';
            String apos = "'";
            if (str.indexOf('"') != -1) {
                quote = '\'';
                apos = "&apos;";
            }
            out.write(quote);
            outputEscaped(out, str, apos);
            out.write(quote);
        }
    }

    protected void outputEscaped(Writer out, String val, String apos)
    throws IOException {
        char[] cs = val.toCharArray();
        for (int i = 0; i < cs.length; i++) {
            switch (cs[i]) {
            case '&':
                out.write("&amp;");
                break;
            case '\'':
                out.write(apos);
                break;
            case '<':
                out.write("&lt;");
                break;
            case '>':
                out.write("&gt;");
                break;
            default:
                out.write(cs[i]); 
            }
        }
    }

    private static String toDateTimeStr(Date date) {
        String str = new SimpleDateFormat(dateTimeFormat).format(date);
        if (dateTimeFormat.endsWith("Z")) {
            int len = str.length();
            StringBuffer sb = new StringBuffer(len+1);
            sb.append(str);
            sb.insert(len - 2, ':');
            str = sb.toString();
        }
        return str;
    }

}
