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
package org.dcm4chee.web.war.tc.keywords;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.extensions.ajax.markup.html.autocomplete.AutoCompleteSettings;
import org.apache.wicket.extensions.ajax.markup.html.autocomplete.AutoCompleteTextField;
import org.apache.wicket.markup.html.JavascriptPackageResource;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.ListChoice;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.convert.IConverter;
import org.dcm4chee.web.war.tc.TCPanel;
import org.dcm4chee.web.war.tc.keywords.TCKeywordCatalogue.TCKeywordInput;

/**
 * @author Bernhard Ableitinger <bernhard.ableitinger@agfa.com>
 * @version $Revision$ $Date$
 * @since June 20, 2011
 */
public class TCKeywordListInput extends Panel implements TCKeywordInput {
    public TCKeywordListInput(final String id, List<TCKeyword> keywords) {
        this(id, null, keywords);
    }

    public TCKeywordListInput(final String id, TCKeyword selectedKeyword,
            final List<TCKeyword> keywords) {
        super(id, selectedKeyword != null ? new Model<TCKeyword>(
                selectedKeyword) : new Model<TCKeyword>());

        final AutoCompleteTextField<TCKeyword> text = new AutoCompleteTextField<TCKeyword>(
                "text", getModel(), TCKeyword.class, new AutoCompleteSettings()) {
            final Map<String, TCKeyword> keywordMap = new HashMap<String, TCKeyword>();

            @Override
            public IConverter getConverter(Class<?> type) {
                if (TCKeyword.class.equals(type)) {
                    if (keywordMap.isEmpty() && keywords != null
                            && !keywords.isEmpty()) {
                        for (TCKeyword keyword : keywords) {
                            keywordMap.put(keyword.toString(), keyword);
                        }
                    }
                    return new IConverter() {
                        @Override
                        public String convertToString(Object o, Locale locale) {
                            return o != null ? o.toString() : null;
                        }

                        @Override
                        public TCKeyword convertToObject(String s, Locale locale) {
                            if (s!=null)
                            {
                                TCKeyword keyword = keywordMap.get(s);
                                if (keyword==null)
                                {
                                    keyword = new TCKeyword(s, null, false);
                                }
                                return keyword;
                            }
                            return null;
                        }
                    };
                }
                return getConverter(type);
            }

            @Override
            protected Iterator<TCKeyword> getChoices(String s) {
                List<TCKeyword> match = new ArrayList<TCKeyword>();
                if (s.length() >= 3) {
                    for (TCKeyword keyword : keywords) {
                        if (keyword.toString().toUpperCase()
                                .contains(s.toUpperCase())) {
                            match.add(keyword);
                        }
                    }
                }
                return match.iterator();
            }
        };
        text.setOutputMarkupId(true);

        final WebMarkupContainer popup = new WebMarkupContainer(
                "popup-keyword-list");
        popup.setOutputMarkupId(true);
        popup.setOutputMarkupPlaceholderTag(true);
        popup.setVisible(false);

        final ListChoice<TCKeyword> keywordList = new ListChoice<TCKeyword>(
                "keyword-list", new Model<TCKeyword>(selectedKeyword), keywords) {
            @Override
            protected String getNullValidKey() {
                return "tc.search.null.text";
            }
        };
        keywordList.setOutputMarkupId(true);
        keywordList.setNullValid(true);
        keywordList.add(new AjaxFormComponentUpdatingBehavior("onchange") {
            @Override
            public void onUpdate(AjaxRequestTarget target) {
                TCKeyword choosenKeyword = keywordList.getModelObject();

                TCKeywordListInput.this.getModel().setObject(choosenKeyword!=null && 
                        choosenKeyword.isAllKeywordsPlaceholder()?null:choosenKeyword);

                MarkupContainer parent = TCKeywordListInput.this.getParent();
                
                popup.setVisible(false);
                target.addComponent(popup);
                if (parent!=null)
                {
                    target.addComponent(parent);
                }
                else
                {
                    target.addComponent(text);
                }
            }
        });

        popup.add(keywordList);

        popup.add(new AjaxButton("popup-close-button") {
            @Override
            public void onSubmit(AjaxRequestTarget target, Form<?> form) {
                MarkupContainer parent = TCKeywordListInput.this.getParent();
                
                popup.setVisible(false);
                target.addComponent(popup);
                if (parent!=null)
                {
                    target.addComponent(parent);
                }
                else
                {
                    target.addComponent(text);
                }
            }
        });

        add(JavascriptPackageResource.getHeaderContribution(TCPanel.class,
                "tc-utils.js"));

        add(text);
        add(new AjaxButton("chooser-button", new Model<String>("...")) {
            @Override
            public void onSubmit(AjaxRequestTarget target, Form<?> form) {
                target.appendJavascript("setPositionRelativeToParent('"
                        + getMarkupId() + "','" + popup.getMarkupId() + "')");
                
                keywordList.setModelObject(getSelectedKeyword());
                
                popup.setVisible(true);
                target.addComponent(keywordList);
                target.addComponent(popup);
            }
        });
        add(popup);
    }

    @Override
    public TCKeyword getSelectedKeyword() {
        TCKeyword keyword = getModel().getObject();

        return keyword == null || keyword.isAllKeywordsPlaceholder() ? null
                : keyword;
    }

    @Override
    public void resetSelectedKeyword() {
        getModel().setObject(null);
    }

    @Override
    public Component getComponent() {
        return this;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private Model<TCKeyword> getModel() {
        return (Model) getDefaultModel();
    }
}
