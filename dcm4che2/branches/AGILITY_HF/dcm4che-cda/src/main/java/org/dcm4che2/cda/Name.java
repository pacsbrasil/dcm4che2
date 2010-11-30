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
import java.io.Writer;

/**
 * @author Gunter Zeilinger<gunterze@gmail.com>
 * @version $Revision$ $Date$
 * @since Mar 11, 2008
 */
public class Name extends BaseElement {

    private Prefix prefix;
    private Given given;
    private Family family;
    private Suffix suffix;

    public Name() {
        super("name");
    }

    public String getPrefix() {
        return prefix != null ? prefix.getText() : null;
    }

    public Name setPrefix(String text) {
        this.prefix = text != null ? new Prefix(text) : null;
        return this;
    }

    public String getGiven() {
        return given != null ? given.getText() : null;
    }

    public Name setGiven(String text) {
        this.given = text != null ? new Given(text) : null;
        return this;
    }

    public String getFamily() {
        return family != null ? family.getText() : null;
    }

    public Name setFamily(String text) {
        this.family = text != null ? new Family(text) : null;
        return this;
    }

    public String getSuffix() {
        return suffix != null ? suffix.getText() : null;
    }

    public Name setSuffix(String text) {
        this.suffix = text != null ? new Suffix(text) : null;
        return this;
    }

    @Override
    protected boolean isEmpty() {
        return false;
    }

    @Override
    protected void writeContentTo(Writer out) throws IOException {
        writeTo(prefix, out);
        writeTo(given, out);
        writeTo(family, out);
        writeTo(suffix, out);
    }


    private static class Prefix extends TextElement {
        public Prefix(String text) {
            super("prefix", text);
        }
    }


    private static class Given extends TextElement {
        public Given(String text) {
            super("given", text);
        }
    }


    private static class Family extends TextElement {
        public Family(String text) {
            super("family", text);
        }
    }


    private static class Suffix extends TextElement {
        public Suffix(String text) {
            super("suffix", text);
        }
    }

}
