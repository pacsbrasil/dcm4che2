<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:output method="xml" indent="yes"/>
    <xsl:param name="package">org.dcm4che2.data</xsl:param>
    <xsl:param name="class">Tag</xsl:param>
    <xsl:template match="/">
        <dictionary tagclass="{$package}.{$class}">
            <xsl:for-each  select="elements/element">
                <xsl:sort select="@tag"/>
                <xsl:copy-of select="."/>
            </xsl:for-each>
        </dictionary>
    </xsl:template>
</xsl:stylesheet>
