<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:output method="text"/>

<xsl:template match="/">
<xsl:text>datasource-mapping=</xsl:text>
<xsl:value-of select="jbosscmp-jdbc/defaults/datasource-mapping"/>
<xsl:text>
</xsl:text>
<xsl:apply-templates select="jbosscmp-jdbc/enterprise-beans/entity"/>
</xsl:template>

<xsl:template match="entity">
<xsl:value-of select="ejb-name"/>
<xsl:text>=</xsl:text>
<xsl:value-of select="table-name"/>
<xsl:text>
</xsl:text>
<xsl:apply-templates select="cmp-field"/>
</xsl:template>

<xsl:template match="cmp-field">
<xsl:value-of select="../ejb-name"/>
<xsl:text>.</xsl:text>
<xsl:value-of select="field-name"/>
<xsl:text>=</xsl:text>
<xsl:value-of select="../table-name"/>
<xsl:text>.</xsl:text>
<xsl:value-of select="column-name"/>
<xsl:text>
</xsl:text>
</xsl:template>
</xsl:stylesheet>
