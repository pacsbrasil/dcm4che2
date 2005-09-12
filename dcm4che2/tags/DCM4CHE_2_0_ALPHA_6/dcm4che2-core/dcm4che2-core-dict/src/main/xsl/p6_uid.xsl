<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
    xmlns:office="http://openoffice.org/2000/office" xmlns:table="http://openoffice.org/2000/table"
    exclude-result-prefixes="office table">
    <xsl:output method="xml" indent="yes"/>
    <xsl:template match="/">
        <xsl:apply-templates
            select="office:document-content/office:body/table:table/table:table-row"/>
    </xsl:template>
    <xsl:template match="table:table-row">
        <xsl:variable name="uid" select="normalize-space(table:table-cell[1])"/>
        <xsl:if test="starts-with($uid,'1.2.840') and count(table:table-cell)=4">
            <uid>
                <xsl:attribute name="value">
                    <xsl:value-of select="$uid"/>
                </xsl:attribute>
                <xsl:attribute name="type">
                    <xsl:value-of select="normalize-space(table:table-cell[3])"/>
                </xsl:attribute>
                 <xsl:value-of select="normalize-space(table:table-cell[2])"/>
            </uid>
        </xsl:if>
    </xsl:template>
</xsl:stylesheet>
