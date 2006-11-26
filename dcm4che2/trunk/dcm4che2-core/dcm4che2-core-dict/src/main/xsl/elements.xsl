<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:output method="xml" indent="yes"/>
    <xsl:variable name="noascii">&#xF06D;&#x2019;&#x2013;</xsl:variable>
    <xsl:variable name="ascii">u'-</xsl:variable>
    <xsl:variable name="apos">'</xsl:variable>
    <xsl:variable name="nojavaid">,/-()[]@:&amp;</xsl:variable>
    <xsl:variable name="lower">abcdefghijklmnopqrstuvwxyz</xsl:variable>
    <xsl:variable name="upper">ABCDEFGHIJKLMNOPQRSTUVWXYZ</xsl:variable>
    <xsl:template match="/">
        <dictionary>
            <xsl:apply-templates select="elements/element">
                <xsl:sort select="@tag"/>
            </xsl:apply-templates>
        </dictionary>
    </xsl:template>
    <xsl:template match="element">
        <xsl:variable name="tag" select="@tag"/>        
        <xsl:if test="not(following-sibling::*[@tag=$tag])">
            <xsl:variable name="hex">
                <xsl:choose>
                    <xsl:when test="@tag='(0020,3100 to 31FF)'">002031xx</xsl:when>
                    <xsl:otherwise>
                        <xsl:value-of select="translate(@tag,'(,)','')"/>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:variable>    
            <xsl:variable name="text" select="text()"/>        
            <xsl:variable name="name" select="translate($text,$noascii,$ascii)"/>        
            <xsl:variable name="ret" select="@ret"/>
            <element>
                <xsl:attribute name="tag">
                    <xsl:value-of select="$hex"/>
                </xsl:attribute>
                <xsl:variable name="alias">
                    <xsl:call-template name="skipSpaces">
                      <xsl:with-param name="val">
                        <xsl:call-template name="skipAposS">
                          <xsl:with-param name="val" 
                            select="normalize-space(translate($name,$nojavaid,''))"/>
                        </xsl:call-template>
                      </xsl:with-param>
                    </xsl:call-template>
                    <xsl:value-of select="@ret"/>
                    <!-- if different attributes with equal names, add ggggeeee suffix -->
                    <xsl:if test="../*[text()=$text][@ret=$ret][@tag!=$tag]">
                        <xsl:value-of select="$hex"/>
                    </xsl:if>
                </xsl:variable>
                <xsl:attribute name="alias">
                  <xsl:choose>
                    <xsl:when test="$alias = 'OperatorsName'">OperatorName</xsl:when>
                    <xsl:when test="$alias = '3DRenderingType'">_3DRenderingType</xsl:when>
                    <xsl:otherwise>
                      <xsl:value-of select="$alias"/>
                    </xsl:otherwise>
                  </xsl:choose>
                </xsl:attribute>
                <xsl:attribute name="vr">
                    <xsl:if test="not(@vr='see note')">
                        <xsl:value-of select="translate(@vr,'or ','|')"/>
                    </xsl:if>
                </xsl:attribute>
                <xsl:attribute name="vm">
                     <xsl:value-of select="@vm"/>
                </xsl:attribute>
                <xsl:attribute name="ret">
                    <xsl:value-of select="@ret"/>
                </xsl:attribute>
                <xsl:value-of select="$name"/>                
            </element>
        </xsl:if>
    </xsl:template>
    <xsl:template name="skipAposS">
      <xsl:param name="val"/>
      <xsl:variable name="before" select="substring-before($val, $apos)"/>
      <xsl:choose>
        <xsl:when test="$before">
          <xsl:value-of select="$before"/>
          <xsl:variable name="after" select="substring-after($val, $apos)"/>
          <xsl:call-template name="skipAposS">
              <xsl:with-param name="val" select="substring($after, 2)"/>
          </xsl:call-template>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="$val"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:template>
    <xsl:template name="skipSpaces">
      <xsl:param name="val"/>
      <xsl:variable name="before" select="substring-before($val, ' ')"/>
      <xsl:choose>
        <xsl:when test="$before or starts-with($val, ' ')">
          <xsl:variable name="after" select="substring-after($val, ' ')"/>
          <xsl:value-of select="$before"/>
          <!-- capitalize first character -->
          <xsl:value-of select="translate(substring($after,1,1), $lower, $upper)"/>
          <xsl:call-template name="skipSpaces">
              <xsl:with-param name="val" select="substring($after, 2)"/>
          </xsl:call-template>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="$val"/>
        </xsl:otherwise>
      </xsl:choose>      
    </xsl:template>
</xsl:stylesheet>
