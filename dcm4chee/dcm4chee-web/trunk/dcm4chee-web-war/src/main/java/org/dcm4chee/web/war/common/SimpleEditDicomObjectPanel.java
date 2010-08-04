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

package org.dcm4chee.web.war.common;

import org.apache.wicket.ResourceReference;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxFallbackButton;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow.CloseButtonCallback;
import org.apache.wicket.markup.html.CSSPackageResource;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.FormComponentPanel;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.html.resources.CompressedResourceReference;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.dcm4che2.data.BasicDicomObject;
import org.dcm4che2.data.DicomElement;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.ElementDictionary;
import org.dcm4che2.data.SpecificCharacterSet;
import org.dcm4che2.util.TagUtils;
import org.dcm4chee.web.common.base.BaseWicketPage;
import org.dcm4chee.web.common.behaviours.TooltipBehaviour;
import org.dcm4chee.web.common.markup.BaseForm;

/**
 * @author Franz Willer <franz.willer@gmail.com>
 * @version $Revision$ $Date$
 * @since Jan 15, 2009
 */
public class SimpleEditDicomObjectPanel extends Panel {

    private static final long serialVersionUID = 1L;
    
    private static final ResourceReference BaseCSS = new CompressedResourceReference(BaseWicketPage.class, "base-style.css");
    
    private static ElementDictionary dict = ElementDictionary.getDictionary();
    private final DicomObject dcmObj;
    private final BaseForm form;
    private TooltipBehaviour tooltipBehaviour = new TooltipBehaviour("dicom.");
    private Model<String> resultMessage;
    
    public SimpleEditDicomObjectPanel(String id, final ModalWindow window, DicomObject dcmObj, String title, final int[][] tagPaths, final boolean close) {
        super(id);
    
        if (SimpleEditDicomObjectPanel.BaseCSS != null)
            add(CSSPackageResource.getHeaderContribution(SimpleEditDicomObjectPanel.BaseCSS));
        
        window.setCloseButtonCallback(new CloseButtonCallback() {

            private static final long serialVersionUID = 1L;

            public boolean onCloseButtonClicked(AjaxRequestTarget target) {
                onClose();
                return true;
            }
        });
        
        this.dcmObj = new BasicDicomObject();
        dcmObj.copyTo(this.dcmObj);
        add(new Label("title", new Model<String>(title)));
        add(form = new BaseForm("form"));
        form.setOutputMarkupId(true);
        addHdrLabels(form);
        RepeatingView rv = new RepeatingView("elements") {

            private static final long serialVersionUID = 1L;

            @Override
            protected void onPopulate() {
                removeAll();
                addDicomObject(this, tagPaths);
            }
        };
        form.add(rv);
        form.add(new AjaxFallbackButton("submit", new ResourceModel("saveBtn"), form) {

            private static final long serialVersionUID = 1L;

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                try {
                    SimpleEditDicomObjectPanel.this.onSubmit();
                    getPage().setOutputMarkupId(true);
                    target.addComponent(getPage());
                    if (close)
                        window.close(target);
                } catch (Exception e) {
                    resultMessage.setObject(e.getLocalizedMessage());
                    SimpleEditDicomObjectPanel.this.setOutputMarkupId(true);
                    target.addComponent(SimpleEditDicomObjectPanel.this);
                }
            }

            @Override
            protected void onError(AjaxRequestTarget target, Form<?> form) {
                BaseForm.addInvalidComponentsToAjaxRequestTarget(target, form);
            }
        });
        form.add(new AjaxFallbackButton("cancel", new ResourceModel("cancelBtn"), form) {

            private static final long serialVersionUID = 1L;

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                SimpleEditDicomObjectPanel.this.onCancel();
                window.close(target);
            }
        }
        .setDefaultFormProcessing(false));
        form.add(new Label("result-message", (resultMessage = new Model<String>(""))));
    }

    private void addHdrLabels(WebMarkupContainer table) {
        table.add(new Label("nameHdr", new ResourceModel("dicom.nameHdr")).add(tooltipBehaviour));
        table.add(new Label("valueHdr", new ResourceModel("dicom.valueHdr")).add(tooltipBehaviour));
    }

    protected DicomObject getDicomObject() {
        return dcmObj;
    }

    protected void onSubmit() {}

    protected void onCancel() {}

    protected void onClose() {}
    
    private void addDicomObject(RepeatingView rv, int[][] tagPaths) {
        final SpecificCharacterSet cs = dcmObj.getSpecificCharacterSet();
        int[] tagPath;
        for (int i=0 ; i < tagPaths.length ; i++) {
            tagPath = tagPaths[i];
            if ((tagPath.length & 0x1) == 1) {
                DicomElement el = dcmObj.get(tagPath);
                if (el != null && el.hasDicomObjects())
                    continue;
            }
            WebMarkupContainer elrow = new WebMarkupContainer(rv.newChildId());
            rv.add(elrow);
            elrow.add(new ElementFragment("fragment", cs, tagPath));
        }
    }

    public class ElementFragment extends Fragment {

        private static final long serialVersionUID = 1L;

        public ElementFragment(String id, SpecificCharacterSet cs, final int[] tagPath) {
            super(id, "element", SimpleEditDicomObjectPanel.this);
            final int tag = tagPath[tagPath.length-1];
            add(new Label("name", new AbstractReadOnlyModel<String>(){
                
                private static final long serialVersionUID = 1L;

                @Override
                public String getObject() {
                    String s = getString(TagUtils.toString(tag),null,"");
                    if (s == "") {
                        s = dict.nameOf(tag);
                        if (s == null || s.equals(ElementDictionary.getUnkown()) 
                                || ElementDictionary.PRIVATE_CREATOR.equals(s)) {
                            s = TagUtils.toString(tag);
                        }
                    }
                    return s;
                }
            }));
            FormComponent<?> c = form.getDicomObjectField("value", dcmObj, tagPath);
            add(c);
            if (c instanceof FormComponentPanel<?>) {
                setMarkupTagReferenceId("date_element");
            }
        }
    }
}
