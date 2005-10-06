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
 * Java(TM), available at http://sourceforge.net/projects/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * TIANI Medgraph AG.
 * Portions created by the Initial Developer are Copyright (C) 2003-2005
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Gunter Zeilinger <gunter.zeilinger@tiani.com>
 * Franz Willer <franz.willer@gwi-ag.com>
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

package org.dcm4chex.archive.ejb.jdbc;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * @author Gunter.Zeilinger@tiani.com
 * @version $Revision$ $Date$
 * @since 25.08.2003
 */
abstract class Match
{

    protected String column;
    protected final boolean type2;

    /**
     * Default empty constructor.
     * <p>
     * Only used for class Node! 
     *
     */
    private Match(){
    	type2 = false;
    }
    
    protected Match(String alias, String field, boolean type2)
    {
        this.column = JdbcProperties.getInstance().getProperty(field);
        if (column == null)
            throw new IllegalArgumentException("field: " + field);
        if (alias != null) {
            this.column = alias + column.substring(column.indexOf('.'));
        }
        this.type2 = type2;
    }

    public boolean appendTo(StringBuffer sb)
    {
        if (isUniveralMatch())
            return false;
        sb.append('(');
        if (type2)
        {
            sb.append(column);
            sb.append(" IS NULL OR ");
        }
        appendBodyTo(sb);
        sb.append(')');
        return true;
    }

    public abstract boolean isUniveralMatch();
    protected abstract void appendBodyTo(StringBuffer sb);

    static class NULLValue extends Match
    {
    	private boolean inverter;
        public NULLValue(String alias, String field, boolean inverter)
        {
            super(alias, field, false);
            this.inverter = inverter;
        }

        public boolean isUniveralMatch()
        {
            return false;
        }

        protected void appendBodyTo(StringBuffer sb)
        {
            sb.append(column);
            sb.append(" IS");
            if ( inverter )
            	sb.append( " NOT" );
            sb.append(" NULL");
        }
    }
    
    static class SingleValue extends Match
    {
        private final String value;
        public SingleValue(String alias, String field, boolean type2, String value)
        {
            super(alias, field, type2);
            this.value = value;
        }

        public boolean isUniveralMatch()
        {
            return value == null || value.length() == 0;
        }

        protected void appendBodyTo(StringBuffer sb)
        {
            sb.append(column);
            sb.append(" = \'");
            sb.append(value);
            sb.append('\'');
        }
    }

    static class IntValue extends Match
    {
        private final int value;
        public IntValue(String alias, String field, boolean type2, int value)
        {
            super(alias, field, type2);
            this.value = value;
        }

        public boolean isUniveralMatch()
        {
            return false;
        }

        protected void appendBodyTo(StringBuffer sb)
        {
            sb.append(column);
            sb.append(" = ");
            sb.append(value);
        }
    }

    static class ListOfInt extends Match
    {
        private final int[] ints;
        public ListOfInt(String alias, String field, boolean type2, int[] ints)
        {
            super(alias, field, type2);
            this.ints = ints != null ? (int[]) ints.clone() : new int[0];
        }

        public boolean isUniveralMatch()
        {
            return ints.length == 0;
        }

        protected void appendBodyTo(StringBuffer sb)
        {
            sb.append(column);
            if (ints.length == 1) {
                sb.append(" = ").append(ints[0]);
            } else {
                sb.append(" IN (").append(ints[0]);
                for (int i = 1; i < ints.length; i++) {
                    sb.append(", ").append(ints[i]);
                }
                sb.append(")");
            }
        }
    }
    
    static class AppendLiteral extends Match
    {
        private final String literal;
        public AppendLiteral(String alias, String field, boolean type2, String literal)
        {
            super(alias, field, type2);
            this.literal = literal;
        }

        public boolean isUniveralMatch()
        {
            return false;
        }

        protected void appendBodyTo(StringBuffer sb)
        {
            sb.append(column);
            sb.append(" ");
            sb.append(literal);
        }
    }
    
    static class ListOfUID extends Match
    {
        private final String[] uids;
        public ListOfUID(String alias, String field, boolean type2, String[] uids)
        {
            super(alias, field, type2);
            this.uids = uids != null ? (String[]) uids.clone() : new String[0];
        }

        public boolean isUniveralMatch()
        {
            return uids.length == 0;
        }

        protected void appendBodyTo(StringBuffer sb)
        {
            sb.append(column);
            if (uids.length == 1) {
                sb.append(" = \'").append(uids[0]).append('\'');
            } else {
                sb.append(" IN ('").append(uids[0]);
                for (int i = 1; i < uids.length; i++) {
                    sb.append("\', \'").append(uids[i]);
                }
                sb.append("\')");
            }
        }
    }

    static class WildCard extends Match
    {
        private final char[] wc;
        private final boolean ignoreCase;
        public WildCard(String alias, String field, boolean type2, String wc,
            boolean ignoreCase)
        {
            super(alias, field, type2);
            this.wc = wc != null ? wc.toCharArray() : new char[0];
            this.ignoreCase = ignoreCase;
        }

        public boolean isUniveralMatch()
        {
            for (int i = wc.length; --i >= 0;)
                if (wc[i] != '*')
                    return false;
            return true;
        }

        public boolean isLike()
        {
            for (int i = wc.length; --i >= 0;)
                if (wc[i] == '*' || wc[i] == '?')
                    return true;
            return false;
        }

        protected void appendBodyTo(StringBuffer sb)
        {
            if (ignoreCase)
                sb.append(" UPPER(");
            sb.append(column);
            if (ignoreCase)
                sb.append(')');
            final boolean like = isLike();
            sb.append(like ? " LIKE " : " = ");
            if (ignoreCase)
                sb.append(" UPPER(");

            sb.append('\'');
            char c;
            for (int i = 0; i < wc.length; i++)
            {
                switch (c = wc[i])
                {
                    case '?' :
                        c = '_';
                        break;
                    case '*' :
                        c = '%';
                        break;
                    case '\'' :
                        sb.append('\'');
                        break;
                    case '_' :
                    case '%' :
                        if (like) {
                            sb.append('\\');
                        }
                        break;
                }
                sb.append(c);
            }
            sb.append('\'');

            if (ignoreCase)
                sb.append(')');
        }

    }

    static class Range extends Match
    {
        private final Date[] range;
        private final String format;
        public Range(String alias, String field, boolean type2, Date[] range,
        		String format)
        {
            super(alias, field, type2);
            this.range = range != null ? (Date[]) range.clone() : null;
            this.format = format;
        }

        public boolean isUniveralMatch()
        {
            return range == null;
        }

        protected void appendBodyTo(StringBuffer sb)
        {
            SimpleDateFormat df = new SimpleDateFormat(format);
            sb.append(column);
            if (range[0] == null)
            {
                sb.append(" <= ");
                sb.append(df.format(range[1]));
            } else if (range[1] == null)
            {
                sb.append(" >= ");
                sb.append(df.format(range[0]));
            } else
            {
                sb.append(" BETWEEN ");
                sb.append(df.format(range[0]));
                sb.append(" AND ");
                sb.append(df.format(range[1]));
            }

        }

    }

    static class ModalitiesInStudy extends Match
    {
        private final char[] wc;
        public ModalitiesInStudy(String alias, String md)
        {
            super(alias, "Series.modality", false);
            this.wc = md != null ? md.toCharArray() : new char[0];
        }

        public boolean isUniveralMatch()
        {
            for (int i = wc.length; --i >= 0;)
                if (wc[i] != '*')
                    return false;
            return true;
        }

        public boolean isLike()
        {
            for (int i = wc.length; --i >= 0;)
                if (wc[i] == '*' || wc[i] == '?')
                    return true;
            return false;
        }
        
        protected void appendBodyTo(StringBuffer sb)
        {
            JdbcProperties jp = JdbcProperties.getInstance();
            sb.append("(SELECT count(*) FROM ");
            sb.append(jp.getProperty("Series"));
            sb.append(" WHERE ");
            sb.append(jp.getProperty("Series.study_fk"));
            sb.append(" = ");
            sb.append(jp.getProperty("Study.pk"));
            sb.append(" AND ");
            sb.append(column);
            final boolean like = isLike();
            sb.append(like ? " LIKE '" : " = '");
            char c;
            for (int i = 0; i < wc.length; i++)
            {
                switch (c = wc[i])
                {
                    case '?' :
                        c = '_';
                        break;
                    case '*' :
                        c = '%';
                        break;
                    case '\'' :
                        sb.append('\'');
                        break;
                    case '_' :
                    case '%' :
                        if (like) {
                            sb.append('\\');
                        }
                        break;
                }
                sb.append(c);
            }
            sb.append("') > 0");
        }
    }

    static class Node extends Match
    {
        private List matches = new ArrayList();
        private final String orORand;
        private final boolean invert;
        
        public Node(String orORand, boolean invert){
        	this.orORand = orORand;
        	this.invert = invert;
        }
        
        public void addMatch( Match match ) {
        	matches.add( match );
        }

        public boolean isUniveralMatch()
        {
            return false;
        }

        protected void appendBodyTo(StringBuffer sb) {
        	if ( invert ) sb.append(" NOT");
            sb.append(" ( ");
            Iterator iter = matches.iterator();
            ((Match) iter.next()).appendTo(sb);
            while ( iter.hasNext() ) {
            	sb.append(orORand);
            	( (Match) iter.next()).appendTo(sb);
            }
            sb.append(" )");
        }
        public boolean appendTo(StringBuffer sb)
        {
        	if ( matches.isEmpty() ) return false;
        	appendBodyTo(sb);
            return true;
        }
    }

}
