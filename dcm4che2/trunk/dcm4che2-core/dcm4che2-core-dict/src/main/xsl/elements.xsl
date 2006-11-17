<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:output method="xml" indent="yes"/>
    <xsl:variable name="noascii">&#xF06D;&#x2019;&#x2013;</xsl:variable>
    <xsl:variable name="ascii">u'-</xsl:variable>
    <xsl:variable name="lower">abcdefghijklmnopqrstuvwxyz/-,'()[]@:&amp;</xsl:variable>
    <xsl:variable name="upper">ABCDEFGHIJKLMNOPQRSTUVWXYZ  </xsl:variable>
    <xsl:variable name="digits">0123456789</xsl:variable>
    <xsl:template match="/">
        <dictionary>
            <xsl:apply-templates select="elements/element">
                <xsl:sort select="@tag"/>
            </xsl:apply-templates>
        </dictionary>
    </xsl:template>
    <xsl:template match="element">
        <xsl:variable name="tag" select="@tag"/>        
        <xsl:variable name="text" select="text()"/>        
        <xsl:if test="$text and not(following-sibling::*[@tag=$tag])">
            <xsl:variable name="hex">
                <xsl:choose>
                    <xsl:when test="@tag='(0020,3100 to 31FF)'">002031xx</xsl:when>
                    <xsl:otherwise>
                        <xsl:value-of select="translate(@tag,'(,)','')"/>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:variable>    
            <xsl:variable name="name" select="translate($text,$noascii,$ascii)"/>        
            <xsl:variable name="ret" select="@ret"/>
            <element>
                <xsl:attribute name="tag">
                    <xsl:value-of select="$hex"/>
                </xsl:attribute>
                <xsl:attribute name="alias">
                    <!-- if first char is digit, add _ as prefix -->
                    <xsl:if test="not(translate(substring($name,1,1),$digits,''))">_</xsl:if>
                    <xsl:value-of select="translate(normalize-space(translate($name,$lower,$upper)),' ','_')"/>                    
                    <!-- if different attributes with equal names, add _gggg_eeee suffix -->
                    <xsl:if test="../*[text()=$text][@ret=$ret][@tag!=$tag]">
                      <xsl:text>_</xsl:text>
                      <xsl:value-of select="$hex"/>
                    </xsl:if>
                    <xsl:if test="string($ret)">
                      <xsl:text>_RET</xsl:text>
                    </xsl:if>
                </xsl:attribute>
                <xsl:attribute name="vr">
                    <xsl:if test="not(@vr='see note')">
                        <xsl:value-of select="translate(@vr,'or ','|')"/>
                    </xsl:if>
                </xsl:attribute>
                <xsl:attribute name="vm">
                     <xsl:value-of select="@vm"/>
                </xsl:attribute>
                <xsl:attribute name="ret">
                    <xsl:value-of select="@ret"/>
                </xsl:attribute>
                <xsl:value-of select="$name"/>                
            </element>
        </xsl:if>
    </xsl:template>
</xsl:stylesheet>
