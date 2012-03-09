<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:output method="xml" indent="yes" />
  <xsl:variable name="LOWER">abcdefghijklmnopqrstuvwxyz</xsl:variable>
  <xsl:variable name="UPPER">ABCDEFGHIJKLMNOPQRSTUVWXYZ</xsl:variable>
  <xsl:template match="/article">
    <xsl:apply-templates select="//row[(count(entry)=4) and starts-with(entry/para,'1.')]" />
    <xsl:apply-templates select="//row[(count(entry)=3) and starts-with(entry/para,'1.2.840.10008.6.1.') and entry[2]/para!='']" >
      <xsl:with-param name="namepos">3</xsl:with-param>
      <xsl:with-param name="type">Context Group Name</xsl:with-param>
    </xsl:apply-templates>
  </xsl:template>
  <xsl:template match="row">
    <xsl:param name="namepos">2</xsl:param>
    <xsl:param name="type" select="entry[3]/para" />
    <xsl:variable name="uid" select="entry[1]/para" />
    <xsl:variable name="name">
      <xsl:call-template name="skipAfterColon">
         <xsl:with-param name="name" select="entry[$namepos]/para" />
      </xsl:call-template>
      <xsl:if test="$namepos = 3">
         <xsl:text> (</xsl:text>
         <xsl:value-of select="entry[2]/para" />
         <xsl:text>)</xsl:text>
      </xsl:if>
    </xsl:variable>
    <xsl:variable name="keyword">
      <xsl:choose>
        <xsl:when test="$uid='1.2.840.10008.1.2.4.50'">JPEGBaseline1</xsl:when>
        <xsl:when test="$uid='1.2.840.10008.1.2.4.51'">JPEGExtended24</xsl:when>
        <xsl:when test="$uid='1.2.840.10008.1.2.4.52'">JPEGExtended35Retired</xsl:when>
        <xsl:when test="$uid='1.2.840.10008.1.2.4.53'">JPEGSpectralSelectionNonHierarchical68Retired</xsl:when>
        <xsl:when test="$uid='1.2.840.10008.1.2.4.54'">JPEGSpectralSelectionNonHierarchical79Retired</xsl:when>
        <xsl:when test="$uid='1.2.840.10008.1.2.4.55'">JPEGFullProgressionNonHierarchical1012Retired</xsl:when>
        <xsl:when test="$uid='1.2.840.10008.1.2.4.56'">JPEGFullProgressionNonHierarchical1113Retired</xsl:when>
        <xsl:when test="$uid='1.2.840.10008.1.2.4.57'">JPEGLosslessNonHierarchical14</xsl:when>
        <xsl:when test="$uid='1.2.840.10008.1.2.4.58'">JPEGLosslessNonHierarchical15Retired</xsl:when>
        <xsl:when test="$uid='1.2.840.10008.1.2.4.59'">JPEGExtendedHierarchical1618Retired</xsl:when>
        <xsl:when test="$uid='1.2.840.10008.1.2.4.60'">JPEGExtendedHierarchical1719Retired</xsl:when>
        <xsl:when test="$uid='1.2.840.10008.1.2.4.61'">JPEGSpectralSelectionHierarchical2022Retired</xsl:when>
        <xsl:when test="$uid='1.2.840.10008.1.2.4.62'">JPEGSpectralSelectionHierarchical2123Retired</xsl:when>
        <xsl:when test="$uid='1.2.840.10008.1.2.4.63'">JPEGFullProgressionHierarchical2426Retired</xsl:when>
        <xsl:when test="$uid='1.2.840.10008.1.2.4.64'">JPEGFullProgressionHierarchical2527Retired</xsl:when>
        <xsl:when test="$uid='1.2.840.10008.1.2.4.65'">JPEGLosslessHierarchical28Retired</xsl:when>
        <xsl:when test="$uid='1.2.840.10008.1.2.4.66'">JPEGLosslessHierarchical29Retired</xsl:when>
        <xsl:when test="$uid='1.2.840.10008.1.2.4.70'">JPEGLossless</xsl:when>
        <xsl:when test="$uid='1.2.840.10008.1.2.4.80'">JPEGLSLossless</xsl:when>
        <xsl:when test="$uid='1.2.840.10008.1.2.4.81'">JPEGLSLossyNearLossless</xsl:when>
        <xsl:when test="$uid='1.2.840.10008.1.2.4.90'">JPEG2000LosslessOnly</xsl:when>
        <xsl:when test="$uid='1.2.840.10008.1.2.4.91'">JPEG2000</xsl:when>
        <xsl:when test="$uid='1.2.840.10008.1.2.4.92'">JPEG2000Part2MultiComponentLosslessOnly</xsl:when>
        <xsl:when test="$uid='1.2.840.10008.1.2.4.93'">JPEG2000Part2MultiComponent</xsl:when>
        <xsl:when test="$uid='1.2.840.10008.1.2.4.100'">MPEG2</xsl:when>
        <xsl:when test="$uid='1.2.840.10008.1.2.4.101'">MPEG2MainProfileHighLevel</xsl:when>
        <xsl:when test="$uid='1.2.840.10008.5.1.4.1.1.9.1.1'">TwelveLeadECGWaveformStorage</xsl:when>
        <xsl:otherwise>
          <xsl:call-template name="removeSpaces">
            <xsl:with-param name="name">
              <xsl:call-template name="replaceNonAlpha">
                 <xsl:with-param name="name" select="$name" />
              </xsl:call-template>
            </xsl:with-param>
          </xsl:call-template>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <uid uid="{$uid}" keyword="{$keyword}" type="{$type}">
      <xsl:value-of select="$name" />
    </uid>
  </xsl:template>
  <xsl:template name="skipAfterColon">
    <xsl:param name="name"/>
    <xsl:variable name="before" select="substring-before($name,':')" />
    <xsl:choose>
      <xsl:when test="$before">
        <xsl:value-of select="normalize-space($before)" />
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="normalize-space($name)" />
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  <xsl:template name="replaceNonAlpha">
    <xsl:param name="name"/>
    <xsl:value-of select="normalize-space(translate($name,'-,.@/()&amp;®','        '))"/>
  </xsl:template>
  <xsl:template name="removeSpaces">
    <xsl:param name="name"/>
    <xsl:variable name="after" select="substring-after($name, ' ')"/>
    <xsl:choose>
      <xsl:when test="$after">
        <xsl:value-of select="substring-before($name, ' ')"/>
        <xsl:value-of select="translate(substring($after,1,1),$LOWER,$UPPER)"/>
        <xsl:call-template name="removeSpaces">
          <xsl:with-param name="name" select="substring($after,2)"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$name"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
</xsl:stylesheet>
