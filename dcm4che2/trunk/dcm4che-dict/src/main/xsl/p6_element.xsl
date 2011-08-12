<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:output method="xml" indent="yes" />
  <xsl:template match="/article">
    <xsl:apply-templates select="//row[starts-with(entry/para,'(0002')]" />
    <xsl:apply-templates select="//row[starts-with(entry/para,'(0004')]" />
    <xsl:apply-templates
      select="//row[entry[2]/para != '' and starts-with(entry/para,'(') and not(starts-with(entry/para,'(0002')) and not(starts-with(entry/para,'(0004'))]" />
  </xsl:template>
  <xsl:template match="row">
    <xsl:variable name="name" select="normalize-space(entry[2]/para)" />
    <xsl:variable name="keyword" select="entry[3]/para" />
    <xsl:variable name="tag">
      <xsl:choose>
        <xsl:when test="$keyword='SourceImageIDs'">
          <xsl:text>002031xx</xsl:text>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="substring(entry[1]/para,2,4)" />
          <xsl:value-of select="substring(entry[1]/para,7,4)" />
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:variable name="vr">
      <xsl:if test="entry[4]/para!='see note'">
        <xsl:value-of select="translate(entry[4]/para,'or ','|')" />
      </xsl:if>
    </xsl:variable>
    <xsl:variable name="vm" select="entry[5]/para" />
    <element tag="{$tag}" keyword="{$keyword}" vr="{$vr}" vm="{$vm}">
      <xsl:if test="entry[6]/para = 'RET'">
        <xsl:attribute name="retired">true</xsl:attribute>
      </xsl:if>
      <xsl:value-of select="$name"/>
    </element>
  </xsl:template>
</xsl:stylesheet>
