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
 * Agfa-Gevaert AG.
 * Portions created by the Initial Developer are Copyright (C) 2008
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

package org.dcm4che2.cda;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.List;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @version $Revision$ $Date$
 * @since Mar 10, 2008
 */
class BaseElement {

    protected final String name;
    private Attr firstAttr;

    protected BaseElement(String name) {
        this.name = name;
    }

    protected BaseElement(String name, String attr, Object val) {
        this.name = name;
        addAttribute(attr, val);
    }

    protected BaseElement(String name, String[] attrs, Object[] vals) {
        this.name = name;
        for (int i = 0; i < attrs.length; i++) {
            addAttribute(attrs[i], vals[i]);
        }
    }

    private static class Attr {
        String name;
        Object val;
        Attr next;

        Attr(String name, Object val) {
            this.name = name;
            this.val = val;
        }

        void writeTo(Writer out) throws IOException {
            out.write(' ');
            out.write(name);
            out.write('=');
            writeValTo(out);
        }

        public void writeValTo(Writer out) throws IOException {
            String str = val.toString();
            int quote = '"';
            String apos = "'";
            if (str.indexOf('"') != -1) {
                quote = '\'';
                apos = "&apos;";
            }
            out.write(quote);
            writeEscapedTo(str, apos, out);
            out.write(quote);
        }
    }

    protected void addAttribute(String name, Object val) {
        addAttribute(new Attr(name, val));
    }

    private void addAttribute(Attr attr) {
        if (firstAttr == null) {
            firstAttr = attr;
            return;
        }
        Attr cur = firstAttr;
        while (!attr.name.equals(cur.name)) {
            if (cur.next == null) {
                cur.next = attr;
                return;
            }
            cur = cur.next;
        }
        cur.val = attr.val;
    }

    protected void removeAttribute(String name) {
        if (firstAttr == null) {
            return;
        }
        Attr cur = firstAttr;
        Attr prev = null;
        while (!name.equals(cur.name)) {
            if (cur.next == null) {
                return;
            }
            cur = (prev = cur).next;
        }
        if (prev != null) {
            prev.next = cur.next;
        } else {
            firstAttr = cur.next;
        }
    }

    protected Object getAttribute(String name) {
        for (Attr attr = firstAttr; attr != null; attr = attr.next) {
            if (name.equals(attr.name)) {
                return attr.val;
            }
        }
        return null;
    }

    public void writeTo(Writer out) throws IOException {
        out.write('<');
        out.write(name);
        if (firstAttr != null) {
            for (Attr attr = firstAttr; attr != null; attr = attr.next) {
                attr.writeTo(out);
            }
        }
        if (isEmpty()) {
            out.write('/');
        } else {
            out.write('>');
            writeContentTo(out);
            out.write('<');
            out.write('/');
            out.write(name);
        }
        out.write('>');
    }

    @Override
    public String toString() {
        return toXML();
    }

    public String toXML() {
        return toXML(64);
    }

    public String toXML(int initialSize) {
        StringWriter sw = new StringWriter(initialSize);
        try {
            writeTo(sw);
        } catch (IOException e) {
            // Should never happens
            throw new Error(e);
        }
        return sw.toString();
    }

    protected boolean isEmpty() {
        return true;
    }

    /**
     * Allows subclasses to write content to the output.
     * <em>Note to implementers: {@link #isEmpty()} should also be overridden
     *  to return <code>false</code>, otherwise this method will not be called</em>.
     * 
     * @param out
     *                the writer to write the output to.
     * @throws IOException
     *                 Can be thrown by one of the derived classes if an error occurs.
     */
    @SuppressWarnings("unused")
    protected void writeContentTo(Writer out) throws IOException {
        // empty
    }

    static void writeTo(List<? extends BaseElement> childs, Writer out)
            throws IOException {
        if (childs != null)
            for (BaseElement child : childs) {
                writeTo(child, out);
            }
    }

    static void writeTo(BaseElement element, Writer out) throws IOException {
        if (element != null) {
            element.writeTo(out);
        }
    }

    static void writeEscapedTo(String val, String apos, Writer out)
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
}
