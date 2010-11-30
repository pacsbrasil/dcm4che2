<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
    xmlns:office="urn:oasis:names:tc:opendocument:xmlns:office:1.0"
    xmlns:table="urn:oasis:names:tc:opendocument:xmlns:table:1.0"
    exclude-result-prefixes="office table">
    <xsl:output method="xml" indent="yes"/>
    <xsl:template match="/">
        <xsl:apply-templates select="//table:table-row"/>
  </xsl:template>
    <xsl:template match="table:table-row">
        <xsl:variable name="tag" select="normalize-space(table:table-cell[1])"/>
        <xsl:variable name="name" select="normalize-space(table:table-cell[2])"/>
        <xsl:variable name="vr" select="normalize-space(table:table-cell[4])"/>
        <xsl:if test="$name and starts-with($tag,'(') and not(starts-with($tag,'(R'))">
            <element>
                <xsl:attribute name="tag">
                    <xsl:choose>
                        <xsl:when test="$tag='(0020,3100 to 31FF)'">002031xx</xsl:when>
                        <xsl:otherwise>
                            <xsl:value-of select="translate($tag,'(,)','')"/>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:attribute>
                <xsl:attribute name="keyword">
                    <xsl:value-of select="normalize-space(table:table-cell[3])"/>
                </xsl:attribute>
                <xsl:attribute name="vr">
                    <xsl:if test="not($vr='see note')">
                        <xsl:value-of select="translate($vr,'or ','|')"/>
                    </xsl:if>
                </xsl:attribute>
                <xsl:attribute name="vm">
                    <xsl:value-of select="normalize-space(table:table-cell[5])"/>
                </xsl:attribute>
                <xsl:attribute name="ret">
                    <xsl:value-of select="normalize-space(table:table-cell[6])"/>
                </xsl:attribute>
                <xsl:value-of select="$name"/>
            </element>
        </xsl:if>
    </xsl:template>
</xsl:stylesheet>
