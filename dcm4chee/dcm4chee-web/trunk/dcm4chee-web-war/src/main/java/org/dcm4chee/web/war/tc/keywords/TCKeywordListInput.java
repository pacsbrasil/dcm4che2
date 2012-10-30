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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.extensions.ajax.markup.html.autocomplete.AutoCompleteSettings;
import org.apache.wicket.extensions.ajax.markup.html.autocomplete.AutoCompleteTextField;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.ListChoice;
import org.apache.wicket.markup.html.form.ListMultipleChoice;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.util.ListModel;
import org.dcm4chee.icons.ImageManager;
import org.dcm4chee.web.dao.tc.TCQueryFilterKey;
import org.dcm4chee.web.war.common.AutoSelectInputTextBehaviour;
import org.dcm4chee.web.war.tc.TCPopupManager.AbstractTCPopup;
import org.dcm4chee.web.war.tc.TCPopupManager.TCPopupPosition;
import org.dcm4chee.web.war.tc.TCPopupManager.TCPopupPosition.PopupAlign;
import org.dcm4chee.web.war.tc.TCUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Bernhard Ableitinger <bernhard.ableitinger@agfa.com>
 * @version $Revision$ $Date$
 * @since June 20, 2011
 */
public class TCKeywordListInput extends AbstractTCKeywordInput {

    private static final long serialVersionUID = 1L;
    private final static Logger log = LoggerFactory
            .getLogger(TCKeywordListInput.class);
	private Map<String, TCKeyword> keywordMap;
    private AutoCompleteTextField<String> text;

    public TCKeywordListInput(final String id, TCQueryFilterKey filterKey, boolean usedForSearch, TCKeyword selectedKeyword,
            final List<TCKeyword> keywords) {
    	this(id, filterKey, usedForSearch, selectedKeyword!=null ?
    			Collections.singletonList(selectedKeyword):null, keywords);
    }
    
    public TCKeywordListInput(final String id, TCQueryFilterKey filterKey, boolean usedForSearch, List<TCKeyword> selectedKeywords,
            final List<TCKeyword> keywords) {
        super(id, filterKey, usedForSearch);
        
		if (keywords!=null && !keywords.isEmpty())
		{
			keywordMap = new HashMap<String, TCKeyword>(keywords.size());
			for (TCKeyword keyword : keywords)
			{
				keywordMap.put(keyword.toString(), keyword);
			}
		}
		
        setDefaultModel(new ListModel<TCKeyword>(selectedKeywords!=null ?
        		new ArrayList<TCKeyword>(selectedKeywords):new ArrayList<TCKeyword>()) {
			private static final long serialVersionUID = 1L;
			@Override
            public void setObject(List<TCKeyword> keywords)
            {
				List<TCKeyword> cur = getObject();
                if (!TCUtilities.equals(cur,keywords))
                {
                    super.setObject(keywords);                    
                    fireValueChanged();
                }
            }
        });

        final MultipleKeywordsTextModel textModel = new MultipleKeywordsTextModel(
        		selectedKeywords);
        text = new AutoCompleteTextField<String>(
                "text", textModel, String.class, new AutoCompleteSettings()) {
            private static final long serialVersionUID = 1L;
            @Override
            protected Iterator<String> getChoices(String s) {
                List<String> match = new ArrayList<String>();
                if (s.length() >= 3) {
                	String ks = null;
                    for (TCKeyword keyword : keywords) {
                    	ks = keyword.toString();
                        if (ks.toUpperCase()
                                .contains(s.toUpperCase())) {
                            match.add(ks);
                        }
                    }
                }
                return match.iterator();
            }
        };
        
        text.setOutputMarkupId(true);
        text.add(new AutoSelectInputTextBehaviour());
        text.add(new AjaxFormComponentUpdatingBehavior("onchange") {
			private static final long serialVersionUID = 1L;
			@Override
            public void onUpdate(AjaxRequestTarget target)
            {
                text.updateModel();
                getModel().setObject(textModel.getKeywordItems());
            }
        });

        final Button chooserBtn = new Button("chooser-button", new Model<String>("..."));
        chooserBtn.add(new Image("chooser-button-img", ImageManager.IMAGE_TC_ARROW_DOWN)
        .setOutputMarkupId(true));
        
        final KeywordListPopup popup = new KeywordListPopup(chooserBtn, keywords);

        add(text);
        add(popup);
        add(chooserBtn);
    }

    @Override
    public TCKeyword[] getKeywords() {
        List<TCKeyword> keywords = getKeywordsAsList();
        if (keywords!=null && !keywords.isEmpty())
        {
        	List<TCKeyword> list = new ArrayList<TCKeyword>(keywords);
        	for (Iterator<TCKeyword> it=list.iterator();it.hasNext();)
        	{
        		TCKeyword keyword = it.next();
        		if (keyword==null || keyword.isAllKeywordsPlaceholder())
        		{
        			it.remove();
        		}
        	}
        	if (!list.isEmpty())
        	{
        		return list.toArray(new TCKeyword[0]);
        	}
        }
        return null;
    }
    
    public List<TCKeyword> getKeywordsAsList() {
    	return getModel().getObject();
    }
    
    public void setKeywords(List<TCKeyword> keywords)
    {
    	getModel().setObject(keywords);
    	((MultipleKeywordsTextModel)text.getModel()).setKeywordItems(keywords);
    }

    @Override
    public void resetKeywords() {
        getModel().setObject(null);
        text.setModelObject(null);
    }
    
    @Override
    public boolean isExclusive()
    {
        return text.isEnabled();
    }
    
    @Override
    public void setExclusive(boolean exclusive)
    {
        text.setEnabled(!exclusive);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private ListModel<TCKeyword> getModel() {
        return (ListModel) getDefaultModel();
    }

    private class MultipleKeywordsTextModel extends MultipleItemsTextModel
    {
		private static final long serialVersionUID = 4098272902061698377L;
		
		public MultipleKeywordsTextModel(List<TCKeyword> selectedKeywords)
    	{
    		setKeywordItems(selectedKeywords);
    	}
    	
    	public void setKeywordItems(List<TCKeyword> keywords)
    	{
    		setObject(toString(keywords));
    	}
    	
    	public List<TCKeyword> getKeywordItems()
    	{
    		List<String> items = getStringItems();
    		if (items!=null && !items.isEmpty())
    		{
    			List<TCKeyword> keywords = new ArrayList<TCKeyword>();
    			for (String item : items)
    			{
                    TCKeyword keyword = keywordMap!=null ? keywordMap.get(item) : null;
                    if (keyword == null) {
                        keyword = new TCKeyword(item, null, false);
                    }
                    keywords.add(keyword);
    			}
    			return keywords;
    		}
    		return null;
    	}
    }
    
    private class KeywordListPopup extends AbstractTCPopup 
    {
		private static final long serialVersionUID = -6621878309773247538L;

		private IListCreator listCreator;
		private Component list;

		public KeywordListPopup(Component trigger, final List<TCKeyword> availableKeywords)
        {
        	super("list-keyword-popup", true, true, true, true);
        	
        	this.listCreator = new IListCreator() {
				private static final long serialVersionUID = 8173577296342482236L;
				@Override
        		public boolean isListInvalid() {
                    if (isMultipleKeywordSearchEnabled())
                    {
                    	if (!(list instanceof ListMultipleChoice))
                    	{
                    		return true;
                    	}
                    }
                    else
                    {
                    	if (!(list instanceof ListChoice))
                    	{
                    		return true;
                    	}
                    }
                    return false;
        		}
        		@SuppressWarnings({ "unchecked", "rawtypes" })
				@Override
        		public void updateSelection()
        		{
        			if (list instanceof ListMultipleChoice)
        			{
        				((ListMultipleChoice)list).setModelObject(getKeywordsAsList());
        			}
        			else if (list instanceof ListChoice)
        			{
        				((ListChoice)list).setModelObject(getKeyword());
        			}
        		}
        		@SuppressWarnings({ "unchecked", "rawtypes" })
				@Override
        		public List<TCKeyword> getSelection() {
        			if (list instanceof ListMultipleChoice)
        			{
        				return (List) ((ListMultipleChoice<?>)list).getModelObject();
        			}
        			else if (list instanceof ListChoice)
        			{
        				TCKeyword keyword = (TCKeyword) ((ListChoice<?>)list).getModelObject();
        				if (keyword!=null)
        				{
        					return new ArrayList<TCKeyword>(
        							Collections.singletonList(keyword));
        				}
        			}
        			return new ArrayList<TCKeyword>(0);
        		}
                @Override
            	public Component createList()
                {
                    if (isMultipleKeywordSearchEnabled())
                    {
            	        ListMultipleChoice<TCKeyword> choice = new ListMultipleChoice<TCKeyword>(
            	                "keyword-list", availableKeywords);
            	        choice.setDefaultModel(new ListModel<TCKeyword>(getKeywordsAsList()));
            	        choice.setOutputMarkupId(true);
                        choice.add(new AjaxFormComponentUpdatingBehavior("onchange") {
                        	private static final long serialVersionUID = 1L;
                        	@Override
                        	public void onUpdate(AjaxRequestTarget target) {
                        	}
                        });
            	        return choice;
                    }
                    else
                    {
                    	TCKeyword selectedKeyword = getKeyword();
            	        ListChoice<TCKeyword> choice = new ListChoice<TCKeyword>(
            	                "keyword-list", selectedKeyword!=null?
            	                		new Model<TCKeyword>(selectedKeyword) : new Model<TCKeyword>(), availableKeywords) {
            		        private static final long serialVersionUID = 1L;
            	            @Override
            	            protected String getNullValidKey() {
            	                return "tc.search.null.text";
            	            }
            	        };
            	        choice.setNullValid(true);
            	        choice.setOutputMarkupId(true);
                        choice.add(new AjaxFormComponentUpdatingBehavior("onchange") {
                        	private static final long serialVersionUID = 1L;
                        	@Override
                        	public void onUpdate(AjaxRequestTarget target) {
               					hide(target);
                        	}
                        });
            	        return choice;
                    }
                }
            };
        	
        	installPopupTrigger(trigger, new TCPopupPosition(
	                trigger.getMarkupId(),
	                getMarkupId(), 
	                PopupAlign.BottomLeft, PopupAlign.TopLeft));
        	
        	add(this.list=listCreator.createList());
        }
        
        @Override
        public void beforeShowing(AjaxRequestTarget target)
        {
        	if (listCreator.isListInvalid())
        	{
        		remove(list);
        		add(list=listCreator.createList());
        		//target.addComponent(this);
        	}
        }
        
        @Override
        public void afterShowing(AjaxRequestTarget target) 
        {
        	listCreator.updateSelection();

            if (target!=null)
            {
                target.addComponent(list);
            }
        }

        @Override
        public void beforeHiding(AjaxRequestTarget target) 
        {
            setKeywords(listCreator.getSelection());
            
            if (target!=null)
            {
                target.addComponent(text);
            }
        }
    }
    
    private interface IListCreator extends Serializable
    {
    	boolean isListInvalid();
    	void updateSelection();
    	List<TCKeyword> getSelection();
    	Component createList();
    }
}
