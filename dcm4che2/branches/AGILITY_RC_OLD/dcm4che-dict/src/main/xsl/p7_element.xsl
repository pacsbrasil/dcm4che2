<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
   <xsl:output method="xml" indent="yes" />
   <xsl:template match="/article">
      <xsl:apply-templates select="//row[starts-with(entry[1]/para,'(0')]">
         <xsl:sort select="entry[1]/para" />
      </xsl:apply-templates>
   </xsl:template>
   <xsl:template match="row">
      <xsl:variable name="tag">
         <xsl:value-of select="substring(entry[1]/para,2,4)" />
         <xsl:value-of select="substring(entry[1]/para,7,4)" />
      </xsl:variable>
      <xsl:variable name="name" select="normalize-space(entry[2]/para)" />
      <xsl:variable name="keyword" select="entry[3]/para" />
      <xsl:variable name="vr" select="entry[4]/para" />
      <xsl:variable name="vm" select="entry[5]/para" />
      <element tag="{$tag}" keyword="{$keyword}" vr="{$vr}" vm="{$vm}">
          <xsl:if test="not(entry[6])">
             <xsl:attribute name="retired">true</xsl:attribute>
          </xsl:if>
          <xsl:value-of select="$name"/>
      </element>
   </xsl:template>
</xsl:stylesheet>
