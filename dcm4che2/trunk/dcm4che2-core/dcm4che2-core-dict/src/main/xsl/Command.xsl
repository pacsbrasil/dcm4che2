<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
    xmlns:office="http://openoffice.org/2000/office" xmlns:table="http://openoffice.org/2000/table"
    exclude-result-prefixes="office table">
    <xsl:output method="xml" indent="yes"/>
    <xsl:template match="/">
        <xsl:apply-templates
            select="office:document-content/office:body/table:table[position()=last()-1]/table:table-row[position()!=1]"/>
        <xsl:apply-templates
            select="office:document-content/office:body/table:table[position()=last()]/table:table-row[position()!=1]">
            <xsl:with-param name="ret">RET</xsl:with-param>
        </xsl:apply-templates>
    </xsl:template>
    <xsl:template match="table:table-row">
        <xsl:param name="ret"/>
        <xsl:variable name="tag" select="normalize-space(table:table-cell[2])"/>
        <element>
            <xsl:attribute name="tag">
                <xsl:value-of select="$tag"/>
            </xsl:attribute>
            <xsl:attribute name="vr">
                <xsl:value-of select="normalize-space(table:table-cell[3])"/>
            </xsl:attribute>
            <xsl:attribute name="vm">
                <xsl:value-of select="normalize-space(table:table-cell[4])"/>
            </xsl:attribute>
            <xsl:attribute name="ret">
                <xsl:value-of select="$ret"/>
            </xsl:attribute>
            <xsl:value-of select="normalize-space(table:table-cell[1])"/>
        </element>
    </xsl:template>
</xsl:stylesheet>
