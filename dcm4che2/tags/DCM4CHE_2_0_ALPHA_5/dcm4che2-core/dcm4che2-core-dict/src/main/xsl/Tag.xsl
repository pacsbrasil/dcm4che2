<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:output method="text"/>
    <xsl:param name="package">org.dcm4che2.data</xsl:param>
    <xsl:param name="class">Tag</xsl:param>
    
    <xsl:template match="/">
    <xsl:text>/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/

package </xsl:text>
   <xsl:value-of select="$package"/>
   <xsl:text>;

/** Provides tag constants.*/
public class </xsl:text>
   <xsl:value-of select="$class"/>
   <xsl:text> {

    /** Private constructor */
    private </xsl:text>
   <xsl:value-of select="$class"/>
   <xsl:text>() {
    }
    
    public static final int forName(String name) {
       try {
          return </xsl:text>
   <xsl:value-of select="$class"/>
   <xsl:text>.class.getField(name).getInt(null);
       } catch (IllegalAccessException e) {
          throw new Error(e);
       } catch (NoSuchFieldException e) {
          throw new IllegalArgumentException("Unknown Tag Name: " + name);
       }
    }

</xsl:text>
        <xsl:apply-templates select="dictionary/element"/>
        <xsl:text>}</xsl:text>
    </xsl:template>    
    
    <xsl:template match="element">
        <xsl:text>    /** (</xsl:text>
        <xsl:value-of select="substring(@tag,1,4)"/>
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
        <xsl:text>;

</xsl:text>
    </xsl:template>
</xsl:stylesheet>