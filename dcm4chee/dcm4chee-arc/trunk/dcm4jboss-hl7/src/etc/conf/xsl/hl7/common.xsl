<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:template name="patID">
        <xsl:param name="cx"/>
        <attr tag="00100020" vr="LO">
            <xsl:value-of select="$cx/text()"/>
        </attr>
        <attr tag="00100021" vr="LO">
            <xsl:value-of select="$cx/component[3]"/>
        </attr>
    </xsl:template>
    <xsl:template name="patName">
        <xsl:param name="xpn"/>
        <xsl:if test="$xpn/text()">
            <attr tag="00100010" vr="PN">
                <xsl:if test="$xpn != '&quot;&quot;'">
                    <xsl:call-template name="xpn2pn">
                        <xsl:with-param name="xpn" select="$xpn"/>
                    </xsl:call-template>
                </xsl:if>
            </attr>
        </xsl:if>
    </xsl:template>
    <xsl:template name="patBirthDate">
        <xsl:param name="ts"/>
        <xsl:if test="$ts/text()">
            <attr tag="00100030" vr="DA">
                <xsl:if test="$ts != '&quot;&quot;'">
                    <xsl:value-of select="substring($ts,1,8)"/>
                </xsl:if>
            </attr>
        </xsl:if>
    </xsl:template>
    <xsl:template name="patSex">
        <xsl:param name="is"/>
        <xsl:if test="$is/text()">
            <attr tag="00100040" vr="CS">
                <xsl:if test="$is != '&quot;&quot;'">
                    <xsl:value-of select="$is"/>
                </xsl:if>
            </attr>
        </xsl:if>
    </xsl:template>
    <xsl:template name="cx2attrs">
        <xsl:param name="idtag"/>
        <xsl:param name="istag"/>
        <xsl:param name="cx"/>
        <attr tag="{$idtag}" vr="LO">
            <xsl:value-of select="$cx/text()"/>
        </attr>
        <attr tag="{$istag}" vr="LO">
            <xsl:value-of select="$cx/component[3]"/>
        </attr>
    </xsl:template>
    <xsl:template name="ei2attr">
        <xsl:param name="tag"/>
        <xsl:param name="ei"/>
        <attr tag="{$tag}" vr="LO">
            <xsl:value-of select="$ei/text()"/>
            <xsl:text>^</xsl:text>
            <xsl:value-of select="$ei/component[1]"/>
        </attr>
    </xsl:template>
    <xsl:template name="dcmAttrDA">
        <xsl:param name="tag"/>
        <xsl:param name="val"/>
        <xsl:if test="$val">
            <attr tag="{$tag}" vr="DA">
                <xsl:if test="$val != '&quot;&quot;'">
                    <xsl:value-of select="substring($val,1,8)"/>
                </xsl:if>
            </attr>
        </xsl:if>
    </xsl:template>
    <xsl:template name="dcmAttrDATM">
        <xsl:param name="datag"/>
        <xsl:param name="tmtag"/>
        <xsl:param name="val"/>
        <xsl:if test="$val">
            <attr tag="{$datag}" vr="DA">
                <xsl:if test="$val != '&quot;&quot;'">
                    <xsl:value-of select="substring($val,1,8)"/>
                </xsl:if>
            </attr>
            <attr tag="{$tmtag}" vr="TM">
                <xsl:if test="$val != '&quot;&quot;'">
                    <xsl:value-of select="substring($val,9)"/>
                </xsl:if>
            </attr>
        </xsl:if>
    </xsl:template>
    <xsl:template name="dcmAttr">
        <xsl:param name="tag"/>
        <xsl:param name="vr"/>
        <xsl:param name="val"/>
        <xsl:if test="$val">
            <attr tag="{$tag}" vr="{$vr}">
                <xsl:if test="$val != '&quot;&quot;'">
                    <xsl:value-of select="$val"/>
                </xsl:if>
            </attr>
        </xsl:if>
    </xsl:template>
    <xsl:template name="xpn2pnAttr">
        <xsl:param name="tag"/>
        <xsl:param name="xpn"/>
        <xsl:if test="$xpn/text()">
            <attr tag="{$tag}" vr="PN">
                <xsl:if test="$xpn != '&quot;&quot;'">
                    <xsl:value-of select="$xpn/text()"/>
                    <xsl:variable name="compCount" select="count($xpn/component)"/>
                    <xsl:if test="$compCount &gt; 0">
                        <xsl:text>^</xsl:text>
                        <xsl:value-of select="$xpn/component[1]"/>
                        <xsl:if test="$compCount &gt; 1">
                            <xsl:text>^</xsl:text>
                            <xsl:value-of select="$xpn/component[2]"/>
                            <xsl:if test="$compCount &gt; 2">
                                <xsl:text>^</xsl:text>
                                <xsl:value-of select="$xpn/component[4]"/>
                                <xsl:text>^</xsl:text>
                                <xsl:value-of select="$xpn/component[3]"/>
                            </xsl:if>
                        </xsl:if>
                    </xsl:if>
                </xsl:if>
            </attr>
        </xsl:if>
    </xsl:template>
    <xsl:template name="xcn2pnAttr">
        <xsl:param name="tag"/>
        <xsl:param name="xcn"/>
        <xsl:if test="$xcn/text()">
            <attr tag="{$tag}" vr="PN">
                <xsl:if test="$xcn != '&quot;&quot;'">
                    <xsl:value-of select="$xcn/component[1]"/>
                    <xsl:variable name="compCount" select="count($xcn/component)"/>
                    <xsl:if test="$compCount &gt; 0">
                        <xsl:text>^</xsl:text>
                        <xsl:value-of select="$xcn/component[2]"/>
                        <xsl:if test="$compCount &gt; 1">
                            <xsl:text>^</xsl:text>
                            <xsl:value-of select="$xcn/component[3]"/>
                            <xsl:if test="$compCount &gt; 2">
                                <xsl:text>^</xsl:text>
                                <xsl:value-of select="$xcn/component[5]"/>
                                <xsl:text>^</xsl:text>
                                <xsl:value-of select="$xcn/component[4]"/>
                            </xsl:if>
                        </xsl:if>
                    </xsl:if>
                </xsl:if>
            </attr>
        </xsl:if>
    </xsl:template>
    <xsl:template name="xpn2pn">
        <xsl:param name="xpn"/>
        <xsl:value-of select="$xpn/text()"/>
        <xsl:variable name="compCount" select="count($xpn/component)"/>
        <xsl:if test="$compCount &gt; 0">
            <xsl:text>^</xsl:text>
            <xsl:value-of select="$xpn/component[1]"/>
            <xsl:if test="$compCount &gt; 1">
                <xsl:text>^</xsl:text>
                <xsl:value-of select="$xpn/component[2]"/>
                <xsl:if test="$compCount &gt; 2">
                    <xsl:text>^</xsl:text>
                    <xsl:value-of select="$xpn/component[4]"/>
                    <xsl:text>^</xsl:text>
                    <xsl:value-of select="$xpn/component[3]"/>
                </xsl:if>
            </xsl:if>
        </xsl:if>
    </xsl:template>
    <xsl:template name="codeItem">
        <xsl:param name="sqtag"/>
        <xsl:param name="code"/>
        <xsl:param name="scheme"/>
        <xsl:param name="meaning"/>
        <attr tag="{$sqtag}" vr="SQ">
            <item>
                <!-- Code Value -->
                <xsl:call-template name="dcmAttr">
                    <xsl:with-param name="tag" select="'00080100'"/>
                    <xsl:with-param name="vr" select="'SH'"/>
                    <xsl:with-param name="val" select="$code"/>
                </xsl:call-template>
                <!-- Coding Scheme Designator -->
                <xsl:call-template name="dcmAttr">
                    <xsl:with-param name="tag" select="'00080102'"/>
                    <xsl:with-param name="vr" select="'SH'"/>
                    <xsl:with-param name="val" select="$scheme"/>
                </xsl:call-template>
                <!-- Code Meaning -->
                <xsl:call-template name="dcmAttr">
                    <xsl:with-param name="tag" select="'00080104'"/>
                    <xsl:with-param name="vr" select="'LO'"/>
                    <xsl:with-param name="val" select="$meaning"/>
                </xsl:call-template>
            </item>
        </attr>
    </xsl:template>
</xsl:stylesheet>
