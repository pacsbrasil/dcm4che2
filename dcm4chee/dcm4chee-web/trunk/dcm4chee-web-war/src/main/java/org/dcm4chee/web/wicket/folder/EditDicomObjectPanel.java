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

package org.dcm4chee.web.wicket.folder;

import java.util.Iterator;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.Model;
import org.apache.wicket.validation.validator.PatternValidator;
import org.dcm4che2.data.BasicDicomObject;
import org.dcm4che2.data.DicomElement;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.ElementDictionary;
import org.dcm4che2.data.SpecificCharacterSet;
import org.dcm4che2.data.VR;
import org.dcm4che2.util.TagUtils;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @version $Revision$ $Date$
 * @since Jan 15, 2009
 */
public class EditDicomObjectPanel extends Panel {

    private static ElementDictionary dict = ElementDictionary.getDictionary();
    private final DicomObject dcmObj;
    private final WebMarkupContainer table;

    public EditDicomObjectPanel(String id, DicomObject dcmObj) {
        super(id);
        this.dcmObj = dcmObj;
        add(new FeedbackPanel("feedback"));
        Form form = new Form("form");
        add(form);
        table = new WebMarkupContainer("table");
        table.setOutputMarkupId(true);
        form.add(table);
        RepeatingView rv = new RepeatingView("elements") {

            @Override
            protected void onPopulate() {
                removeAll();
                addDicomObject(this, EditDicomObjectPanel.this.dcmObj, "",
                        new int[0]);
            }
            
        };
        table.add(rv);
    }

    private void addDicomObject(RepeatingView rv, DicomObject dcmObj,
            String nesting, int[] itemPath) {
        final SpecificCharacterSet cs = dcmObj.getSpecificCharacterSet();
        final String nesting1 = nesting + '>';
        for (Iterator<DicomElement> it = dcmObj.iterator(); it.hasNext();) {
            DicomElement el = it.next();
            final int[] tagPath = new int[itemPath.length+1];
            tagPath[itemPath.length] = el.tag();
            System.arraycopy(itemPath, 0, tagPath, 0, itemPath.length);
            WebMarkupContainer elrow = new WebMarkupContainer(rv.newChildId());
            rv.add(elrow);
            elrow.add(
                    new ElementFragment("fragment", el, cs, tagPath, nesting));
            if (el.hasDicomObjects()) {
                int numitems = el.countItems();
                for (int i = 0; i < numitems; i++) {
                    final int[] itemPath1 = new int[tagPath.length+1];
                    System.arraycopy(tagPath, 0, itemPath1, 0, tagPath.length);
                    itemPath1[tagPath.length] = i;
                    WebMarkupContainer itemrow =
                        new WebMarkupContainer(rv.newChildId());
                    rv.add(itemrow);
                    DicomObject item = el.getDicomObject(i);
                    itemrow.add(new ItemFragment("fragment", tagPath, i,
                            nesting1));
                    addDicomObject(rv, item, nesting1, itemPath1);
                }
                WebMarkupContainer additemrow =
                    new WebMarkupContainer(rv.newChildId());
                rv.add(additemrow);
                additemrow.add(new AddItemFragment("fragment", tagPath, numitems,
                        nesting1));
            }
        }
        WebMarkupContainer addelrow = new WebMarkupContainer(rv.newChildId());
        rv.add(addelrow);
        addelrow.add(
                new AddElementFragment("fragment", itemPath, nesting));
        
    }

    public class ElementFragment extends Fragment {

        public ElementFragment(String id, DicomElement el,
                SpecificCharacterSet cs, final int[] tagPath, String nesting) {
            super(id, "element", EditDicomObjectPanel.this);
            final int tag = el.tag();
            add(new Label("name", nesting + dict.nameOf(tag)));
            add(new Label("tag", TagUtils.toString(tag)));
            add(new Label("vr", el.vr().toString()));
            add(new Label("length", Integer.toString(el.length())));
            add(new TextField("value", el.hasItems() ? new Model("") 
                                  : new DicomElementModel(el, cs, tagPath))
                .setVisible(!el.hasItems()));
            add(new AjaxFallbackLink("remove"){
                
                @Override
                public void onClick(AjaxRequestTarget target) {
                    EditDicomObjectPanel.this.dcmObj.remove(tagPath);
                    if (target != null) {
                        target.addComponent(table);
                    }
                }

            });
        }

     }

    public class AddElementFragment extends Fragment {

        public AddElementFragment(String id, final int[] itemPath, 
                String nesting) {
            super(id, "addelement", EditDicomObjectPanel.this);
            add(new Label("name", nesting + "New Attribute"));
            Form form = new Form("form");
            add(form);
            final Model tagModel = new Model("(0008,0000)");
            form.add(new TextField("tag", tagModel).add(new PatternValidator(
                    "\\([0-9a-fA-F]{4},[0-9a-fA-F]{4}\\)")));
            add(new AjaxSubmitLink("add", form){

                @Override
                public void onSubmit(AjaxRequestTarget target, Form form) {
                    String s = (String) tagModel.getObject();
                    int tag = parseTag(s);
                    if (!TagUtils.isGroupLengthElement(tag)) {
                        EditDicomObjectPanel.this.dcmObj
                                .getNestedDicomObject(itemPath)
                                .putNull(tag, null);
                    }
                    if (target != null) {
                        target.addComponent(table);
                    }
                }

            });
        }

     }

    private static int parseTag(String s) {
        return (Integer.parseInt(s.substring(1,5), 16) << 16)
                 | Integer.parseInt(s.substring(6,10), 16);
    }

    public class ItemFragment extends Fragment {

        public ItemFragment(String id, final int[] tagPath,
                final int itemIndex, String nesting) {
            super(id, "item", EditDicomObjectPanel.this);
            add(new Label("name", nesting + "Item #" + (itemIndex+1)));
            add(new AjaxFallbackLink("remove"){
                
                @Override
                public void onClick(AjaxRequestTarget target) {
                    EditDicomObjectPanel.this.dcmObj.get(tagPath)
                            .removeDicomObject(itemIndex);
                    if (target != null) {
                        target.addComponent(table);
                    }
                }
                
            });
        }

     }

    public class AddItemFragment extends Fragment {

        public AddItemFragment(String id, final int[] tagPath,
                final int itemIndex, String nesting) {
            super(id, "additem", EditDicomObjectPanel.this);
            add(new Label("name", nesting + "New Item #" + (itemIndex+1)));
            add(new AjaxFallbackLink("add"){
                
                @Override
                public void onClick(AjaxRequestTarget target) {
                    EditDicomObjectPanel.this.dcmObj.get(tagPath)
                            .addDicomObject(new BasicDicomObject());
                    if (target != null) {
                        target.addComponent(table);
                    }
                }
                
            });
        }

     }

    private class DicomElementModel extends Model {

        private final int[] tagPath;
        private final int vr;
 
        public DicomElementModel(DicomElement el, SpecificCharacterSet cs,
                int[] tagPath) {
            super(el.getValueAsString(cs, 64));
            this.vr = el.vr().code();
            this.tagPath = tagPath.clone();
        }

        @Override
        public void setObject(Object object) {
            Object prev = super.getObject();
            if (object == null) {
                if (prev != null) {
                    EditDicomObjectPanel.this.dcmObj.putNull(tagPath, 
                            VR.valueOf(vr));
                }
            } else {
                if (!object.equals(prev)) {
                    EditDicomObjectPanel.this.dcmObj.putString(tagPath,
                            VR.valueOf(vr), (String) object);
                }
            }
            super.setObject(object);
        }
    }

}
