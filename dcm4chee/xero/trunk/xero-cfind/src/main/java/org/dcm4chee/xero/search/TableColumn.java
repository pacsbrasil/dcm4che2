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
 * Bill Wallace, Agfa HealthCare Inc., 
 * Portions created by the Initial Developer are Copyright (C) 2007
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Bill Wallace <bill.wallace@agfa.com>
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
package org.dcm4chee.xero.search;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.xml.bind.annotation.XmlTransient;

/**
 * The table columns represent the columns displayed in a table, along with the
 * search values for that column, and any ordering information.
 * 
 * @author bwallace
 * 
 */
public class TableColumn extends AttributeType {
	private static Logger log = Logger.getLogger(TableColumn.class.getName());
	
	@XmlTransient
	String i18NName;

	@XmlTransient
	PropertyDescriptor descriptor;

	@XmlTransient
	boolean hidden;

	public TableColumn(PropertyDescriptor pd) {
		this.descriptor = pd;
		this.i18NName = pd.getDisplayName().intern();
		setName(pd.getName().intern());
		Method meth = pd.getReadMethod();
		Column col = meth.getAnnotation(Column.class);
		searchable = Boolean.FALSE;
		if( col!=null ) {
			searchable = col.searchable();
			i18NName = col.i18n();
			if( i18NName==null || i18NName.length()==0 ) i18NName = getName();
			type = col.type();
		}
		label = i18NName;
		hidden = pd.isHidden();
	}

	public TableColumn(TableColumn prototype) {
		this.i18NName = prototype.i18NName;
		this.descriptor = prototype.descriptor;
		this.hidden = prototype.hidden;
		this.name = prototype.name;
		this.searchable = prototype.searchable;
		this.column = prototype.column;
		this.type = prototype.type;
		this.label = prototype.label;
	}

	private static Object[] emptyArray = new Object[0];

	/**
	 * This looks up the value in the object for this table column.
	 */
	public Object readValue(Object object) {
		try {
			return descriptor.getReadMethod().invoke(object, emptyArray);
		} catch (IllegalArgumentException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Indicate if the given value is correctly matched.
	 */
	public boolean matches(Object value) {
		throw new UnsupportedOperationException("Not implemented yet.");
	}

	/**
	 * Gets the name used to lookup the localized label. Usually the same as the
	 * object name.
	 */
	public String getI18NName() {
		return i18NName;
	}

	/**
	 * Generates a list of TableColumn objects, one for every property in the
	 * given class/object type.
	 * 
	 * @param type
	 *            to generate the columns for.
	 * @return List of table columns, one for each property that is readable.
	 */
	public static List<TableColumn> generateColumnsForClass(Class type) {
		List<TableColumn> cols;
		try {
			BeanInfo bi = Introspector.getBeanInfo(type);
			PropertyDescriptor descriptors[] = bi.getPropertyDescriptors();
			cols = new ArrayList<TableColumn>(descriptors.length);
			for (PropertyDescriptor pd : descriptors) {
				TableColumn tc = new TableColumn(pd);
				// Skip the class variable - it isn't interesting.
				if (tc.getName().equals("class"))
					continue;
				cols.add(tc);
			}
		} catch (IntrospectionException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		return cols;
	}

	public static List<TableColumn> generateSearchableColumnsForClass(Class type) {
		List<TableColumn> ret = generateColumnsForClass(type);
		for (Iterator<TableColumn> it = ret.iterator(); it.hasNext();) {
			TableColumn tc = it.next();
			if (!tc.isSearchable())
				it.remove();
		}
		return ret;
	}

	/**
	 * Convert the columns into a map format for fast lookup by name. Includes
	 * both java-attribute case names and initial uppercase names.
	 * 
	 * @param columns
	 * @return Map of the varName to the column data.
	 */
	public static Map<String, TableColumn> toMap(List<TableColumn> columns) {
		Map<String, TableColumn> ret = new HashMap<String, TableColumn>(columns
				.size() * 2);
		for (TableColumn tc : columns) {
			ret.put(tc.getName(), tc);
			String upperVarName = tc.getName().substring(0, 1).toUpperCase()
					+ tc.getName().substring(1);
			ret.put(upperVarName, tc);
		}
		return ret;
	}

	/** Sets a simple string value as the sole content. */
	public void setValue(String value) {
		getContent().clear();
		getContent().add(value);
	}

	/** Sets an array of stirngs as the only content - any of these matching */
	public void setValue(String[] value) {
		getContent().clear();
		if (value.length == 1) {
			setValue(value[0]);
			return;
		}
		for (String str : value) {
			getContent().add(str);
		}
	}

	/**
	 * Indicate if this table column is empty, ie contains no search criteria.
	 * @return
	 */
	public boolean isEmpty() {
		if( getContent().size()==0 ) return true;
		Object value = getContent().get(0);
		if( value instanceof String )
			return ((String) value).length()==0;
		// As more sub-types are added, they need to have their own
		// implementations of isEmpty.
		log.warning("TableColumn has a non-String sub-type for which no isEmpty method has been written, and thus isEmpty might be invalid.");
		return false;
	}
	
}
