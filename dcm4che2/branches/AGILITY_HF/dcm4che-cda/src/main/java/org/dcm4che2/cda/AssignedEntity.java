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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Gunter Zeilinger<gunterze@gmail.com>
 * @version $Revision$ $Date$
 * @since Mar 11, 2008
 */
public class AssignedEntity extends BaseElement {

    private List<ID> ids;
    private Code code;
    private List<Addr> addrs;
    private AssignedPerson assignedPerson;
    private RepresentedOrganization representedOrganization;

    public AssignedEntity() {
        super("assignedEntity");
    }

    public List<ID> getIDs() {
        return ids;
    }

    public AssignedEntity setIDs(List<ID> ids) {
        this.ids = ids;
        return this;
    }

    public ID getID() {
        return ids != null && !ids.isEmpty() ? ids.get(0) : null;
    }

    public AssignedEntity setID(ID id) {
        this.ids = Collections.singletonList(id);
        return this;
    }

    public AssignedEntity addID(ID id) {
        if (ids == null) {
            return setID(id);
        }
        try {
            ids.add(id);
        } catch (UnsupportedOperationException e) {
            List<ID> tmp = new ArrayList<ID>();
            tmp.addAll(ids);
            tmp.add(id);
            ids = tmp;
        }
        return this;
    }

    public Code getCode() {
        return code;
    }

    public AssignedEntity setCode(Code code) {
        this.code = code;
        return this;
    }

    public Addr getAddr() {
        return addrs != null && !addrs.isEmpty() ? addrs.get(0) : null;
    }

    public AssignedEntity setAddr(Addr addr) {
        this.addrs = Collections.singletonList(addr);
        return this;
    }

    public AssignedEntity addAddr(Addr addr) {
        if (addrs == null) {
            return setAddr(addr);
        }
        try {
            addrs.add(addr);
        } catch (UnsupportedOperationException e) {
            List<Addr> tmp = new ArrayList<Addr>();
            tmp.addAll(addrs);
            tmp.add(addr);
            addrs = tmp;
        }
        return this;
    }

    public AssignedPerson getAssignedPerson() {
        return assignedPerson;
    }

    public AssignedEntity setAssignedPerson(AssignedPerson assignedPerson) {
        this.assignedPerson = assignedPerson;
        return this;
    }

    public RepresentedOrganization getRepresentedOrganization() {
        return representedOrganization;
    }

    public AssignedEntity setRepresentedOrganization(
            RepresentedOrganization representedOrganization) {
        this.representedOrganization = representedOrganization;
        return this;
    }

    @Override
    protected boolean isEmpty() {
        return false;
    }

    @Override
    protected void writeContentTo(Writer out) throws IOException {
        writeTo(ids, out);
        writeTo(code, out);
        writeTo(addrs, out);
        writeTo(assignedPerson, out);
        writeTo(representedOrganization, out);
    }

}
