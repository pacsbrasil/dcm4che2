/*
 * $Id$
 * Copyright (c) 2002,2003 by TIANI MEDGRAPH AG
 * 
 * This file is part of dcm4che.
 * 
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation; either version 2 of the License, or (at your
 * option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.dcm4chex.archive.ejb.conf;

import java.util.ArrayList;
import java.util.Hashtable;

import org.dcm4che.data.Dataset;
import org.dcm4che.dict.Tags;
import org.dcm4cheri.util.StringUtils;

/**
 * @author gunter.zeilinger@tiani.com
 * @version $Revision$ $Date$
 * @since 28.12.2003
 */
public class AttributeCoercion {

    ArrayList list = new ArrayList();
    Hashtable params = new Hashtable();
    private static abstract class Coerce {
        final int tag;
        Coerce(int tag) {
            this.tag = tag;
        }
        abstract void coerce(Dataset ds);
    }

    private static class LiteralCoerce extends Coerce {
        final String[] value;
        LiteralCoerce(int tag, String value) {
            super(tag);
            this.value = value != null ? StringUtils.split(value, '\\') : null;
        }
        void coerce(Dataset ds) {
            ds.putXX(tag, value);
        }
    }

    private static class CopyCoerce extends Coerce {
        final int srcTag;
        CopyCoerce(int tag, String value) {
            super(tag);
            this.srcTag = Tags.valueOf(value.substring(1));
        }
        void coerce(Dataset ds) {
            ds.putXX(tag, ds.getStrings(srcTag));
        }
    }

    private static class GeneralCoerce extends Coerce {
        final String[] literals;
        final int[] srcTags;
        final int[] srcIndex;
        final Hashtable[] luts;
        GeneralCoerce(int tag, String value, Hashtable lutsByName) {
            super(tag);
            ArrayList tokens = new ArrayList();
            int left = 0, pos = -2, srcTag;
            while ((pos = value.indexOf("$(", pos + 2)) != -1
                && pos + 12 <= value.length()
                && (srcTag = Tags.valueOf(value.substring(pos + 1, pos + 12)))
                    != -1) {
                tokens.add(value.substring(left, pos));
                left = pos;
            }
            tokens.add(value.substring(left));
            literals = new String[tokens.size()];
            srcTags = new int[literals.length - 1];
            srcIndex = new int[literals.length - 1];
            luts = new Hashtable[literals.length - 1];
            literals[0] = (String) tokens.get(0);
            for (int i = 0; i < srcTags.length; i++) {
                String tk = (String) tokens.get(i + 1);
                srcTags[i] = Tags.valueOf(tk.substring(1, 12));
				int startLiteral = 12;
				 if (tk.length() > 14 && tk.charAt(12) == '[') {
					 int last = tk.indexOf(']', 14);
					 if (last != -1) {
						 try {
							 int tmp = Integer.parseInt(tk.substring(13, pos));
							 if (tmp >= 0) {
								 srcIndex[i] = tmp;
								 startLiteral = last + 1;
							 }
						 } catch (NumberFormatException e) {}
					 }
				 }
				 if (tk.length() > startLiteral + 3
					 && tk.charAt(startLiteral) == '{') {
					 int last = tk.indexOf('}', startLiteral + 2);
					 if (last != -1) {
						 luts[i] =
							 (Hashtable) lutsByName.get(
						tk.substring(startLiteral + 1, last));
						 startLiteral = last + 1;
					 }
				 }
                 literals[i] = tk.substring(startLiteral);
            }
        }

        void coerce(Dataset ds) {
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < srcTags.length; ++i) {
                sb.append(literals[i]);
                String val = ds.getString(srcTags[i], srcIndex[i]);
                sb.append(luts[i] != null ? (String) luts[i].get(val) : val);
            }
            sb.append(literals[srcTags.length]);
            ds.putXX(tag, StringUtils.split(sb.toString(), '\\'));
        }
    }

    void add(int tag, String val, Hashtable lutsByName) {
        list.add(makeCoerce(tag, val, lutsByName));
    }

    private Coerce makeCoerce(int tag, String val, Hashtable lutsByName) {
        int pos;
        if (val == null || (pos = val.indexOf("$(")) == -1) {
            return new LiteralCoerce(tag, val);
        }
        if (pos == 0 && Tags.valueOf(val.substring(1)) != -1) {
            return new CopyCoerce(tag, val);
        }
        return new GeneralCoerce(tag, val, lutsByName);
    }

    public void coerce(Dataset ds, Dataset coercedElements) {
        for (int i = 0, n = list.size(); i < n; ++i) {
            Coerce coerce = (Coerce) list.get(i);
            coerce.coerce(ds);
            coercedElements.putXX(coerce.tag);
        }
    }
}