<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:output method="xml" indent="yes"/>
    <xsl:variable name="noascii">&#xF06D;&#x2019;&#x2013;</xsl:variable>
    <xsl:variable name="ascii">u'-</xsl:variable>
    <xsl:variable name="nojavaid"> ,'/-()[]@:&amp;</xsl:variable>
    <xsl:variable name="digits">0123456789</xsl:variable>
    <xsl:template match="/">
        <uids>
            <xsl:apply-templates select="uids/uid">
                <xsl:sort select="@value"/>
            </xsl:apply-templates>
        </uids>
    </xsl:template>
    <xsl:template match="uid">
        <xsl:variable name="name">
            <xsl:call-template name="skipAfterColon">
                <xsl:with-param name="val" select="translate(text(),$noascii,$ascii)"/>
            </xsl:call-template>
        </xsl:variable>
        <uid>
            <xsl:attribute name="value">
                <xsl:value-of select="@value"/>
            </xsl:attribute>
            <xsl:attribute name="alias">
                <xsl:variable name="name1">
                    <xsl:choose>
                        <xsl:when test="@value='1.2.840.10008.1.2.4.70'">JPEGLossless</xsl:when>
                        <xsl:when test="@value='1.2.840.10008.1.2.4.100'">MPEG2</xsl:when>
                        <xsl:when test="@type='Transfer Syntax'">
                            <xsl:call-template name="remove">
                                <xsl:with-param name="tag">Process</xsl:with-param>
                                <xsl:with-param name="val">
                                    <xsl:call-template name="remove">
                                        <xsl:with-param name="tag">Image Compression</xsl:with-param>
                                        <xsl:with-param name="val" select="$name"/>
                                    </xsl:call-template>
                                </xsl:with-param>
                            </xsl:call-template>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:value-of select="$name"/>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:variable>
                <!-- if first char is digit, add _ as prefix -->
                <xsl:if test="not(translate(substring($name1,1,1),$digits,''))">_</xsl:if>
                <xsl:value-of select="translate($name1,$nojavaid,'')"/>
            </xsl:attribute>
            <xsl:attribute name="type">
                <xsl:value-of select="@type"/>
            </xsl:attribute>
            <xsl:value-of select="$name"/>
        </uid>
    </xsl:template>
    <xsl:template name="skipAfterColon">
        <xsl:param name="val"/>
        <xsl:choose>
            <xsl:when test="substring-before($val,':')">
                <xsl:value-of select="substring-before($val,':')"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$val"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    <xsl:template name="remove">
        <xsl:param name="val"/>
        <xsl:param name="tag"/>
        <xsl:choose>
            <xsl:when test="substring-before($val,$tag)">
                <xsl:value-of select="substring-before($val,$tag)"/>
                <xsl:value-of select="substring-after($val,$tag)"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$val"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
</xsl:stylesheet>
