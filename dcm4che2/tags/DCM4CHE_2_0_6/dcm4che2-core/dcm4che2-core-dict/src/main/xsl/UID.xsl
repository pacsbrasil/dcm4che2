<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:output method="text"/>
    <xsl:param name="package">org.dcm4che2.data</xsl:param>
    <xsl:param name="class">UID</xsl:param>
    
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
    
    public static final String forName(String name) {
       try {
          return (String) </xsl:text>
   <xsl:value-of select="$class"/>
   <xsl:text>.class.getField(name).get(null);
       } catch (IllegalAccessException e) {
          throw new Error(e);
       } catch (NoSuchFieldException e) {
          throw new IllegalArgumentException("Unknown UID Name: " + name);
       }
    }

</xsl:text>
        <xsl:apply-templates select="uids/uid"/>
        <xsl:text>}</xsl:text>
    </xsl:template>    
    
    <xsl:template match="uid">
        <xsl:text>    /** </xsl:text>
        <xsl:value-of select="text()"/>
        <xsl:text> - </xsl:text>
        <xsl:value-of select="@type"/>        
        <xsl:text> */
    public static final String </xsl:text>
        <xsl:value-of select="@alias"/>
        <xsl:text> = "</xsl:text>
        <xsl:value-of select="@value"/>
        <xsl:text>";

</xsl:text>
    </xsl:template>
</xsl:stylesheet>