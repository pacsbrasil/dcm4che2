<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:output method="text"/>
    <xsl:param name="package">org.dcm4che2.data</xsl:param>
    <xsl:param name="class">Tag</xsl:param>
    
    <xsl:template match="/">/* ***** BEGIN LICENSE BLOCK *****
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
 * Gunter Zeilinger, Huetteldorferstr. 24/10, 1150 Vienna/Austria/Europe.
 * Portions created by the Initial Developer are Copyright (C) 2002-2005
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
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


package <xsl:value-of select="$package"/>;

import java.util.StringTokenizer;

/** Provides tag constants.*/
public class <xsl:value-of select="$class"/> {

    /** Private constructor */
    private <xsl:value-of select="$class"/>() {
    }
    
    public static final int forName(String name) {
       try {
          return <xsl:value-of select="$class"/>.class.getField(name).getInt(null);
       } catch (IllegalAccessException e) {
          throw new Error(e);
       } catch (NoSuchFieldException e) {
          throw new IllegalArgumentException("Unknown Tag Name: " + name);
       }
    }

    public static int toTag(String s) {
        try {
            return (int) Long.parseLong(s, 16);
        } catch (NumberFormatException e) {
            return Tag.forName(s);
        }
    }
    
    public static int[] toTagPath(String expr) {
        StringTokenizer stk = new StringTokenizer(expr, "/[]", true);
        int[] tagPath = new int[stk.countTokens()];
        int i= 0;
        char delim = '/';
        while (stk.hasMoreTokens()) {
            String s = stk.nextToken();
            char ch0 = s.charAt(0);
            switch (ch0) {
            case '/':
                if (delim == '/') {
                    tagPath[i] = 0;
                    i++;
                }
            case '[':
            case ']':
                delim = ch0;              
                break;
            default:
                tagPath[i] = (delim == '[') ? Integer.parseInt(s)-1 : toTag(s);
                ++i;
                break;
            }
        }
        if (i &lt; tagPath.length) {
            int[] tmp = new int[i];
            System.arraycopy(tagPath, 0, tmp, 0, i);
            tagPath = tmp;
        }
        return tagPath;
    }<xsl:apply-templates select="dictionary/element"/>
}</xsl:template>    
    
    <xsl:template match="element">
        
    /** (<xsl:value-of select="substring(@tag,1,4)"/>
        <xsl:text>,</xsl:text>
        <xsl:value-of select="substring(@tag,5,4)"/>
        <xsl:text>) VR=</xsl:text>
        <xsl:value-of select="@vr"/>
        <xsl:text>, VM=</xsl:text>
        <xsl:value-of select="@vm"/>
        <xsl:text> </xsl:text>
        <xsl:value-of select="text()"/>
        <xsl:text> </xsl:text>
        <xsl:value-of select="@ret"/>
        <xsl:text> */
    public static final int </xsl:text>
        <xsl:value-of select="@alias"/>
        <xsl:text> = 0x</xsl:text>
        <xsl:value-of select="translate(@tag,'x','0')"/>
        <xsl:text>;</xsl:text>
    </xsl:template>
</xsl:stylesheet>