<?xml version='1.0'?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                version='1.0'
                xmlns="http://www.w3.org/TR/xhtml1/transitional"
                exclude-result-prefixes="#default">

<xsl:import href="docbook.xsl"/>

<xsl:variable name="toc.section.depth">10</xsl:variable>
<xsl:variable name="toc.indent.width">10</xsl:variable>
<xsl:variable name="section.autolabel">1</xsl:variable>
<xsl:variable name="paper.type">A4</xsl:variable>
<xsl:param name="hyphenate">false</xsl:param>
<xsl:param name="draft.watermark.image" select="'@DRAFT_IMG_URI@'"/>
</xsl:stylesheet>

