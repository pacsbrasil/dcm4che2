<?xml version="1.0" encoding="UTF-8" ?>
<!-- $Id -->
<!--**************************************************************************
 *                                                                           *
 *  Copyright (c) 2001,2002 by TIANI MEDGRAPH AG                             *
 *                                                                           *
 *  This file is part of dcm4che.                                            *
 *                                                                           *
 *  This library is free software; you can redistribute it and/or modify it  *
 *  under the terms of the GNU Lesser General Public License as published    *
 *  by the Free Software Foundation; either version 2 of the License, or     *
 *  (at your option) any later version.                                      *
 *                                                                           *
 *  This library is distributed in the hope that it will be useful, but      *
 *  WITHOUT ANY WARRANTY; without even the implied warranty of               *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU        *
 *  Lesser General Public License for more details.                          *
 *                                                                           *
 *  You should have received a copy of the GNU Lesser General Public         *
 *  License along with this library; if not, write to the Free Software      *
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA  *
 *                                                                           *
 ***************************************************************************-->

<xsl:stylesheet version="1.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:output method="text"/>

<xsl:param name="maxlen" select="79"/>
<xsl:param name="vallen" select="64"/>

<xsl:template match="/">
    <xsl:apply-templates select="dataset"/>
</xsl:template>

<xsl:template match="dataset">
<xsl:text>=== Dataset ===
</xsl:text>
<xsl:apply-templates select="elm">
    <xsl:with-param name="level" select="''"/>
</xsl:apply-templates>
</xsl:template>

<xsl:template match="elm">
        <xsl:param name='level'/>
    <xsl:variable name="prefix">
        <xsl:value-of select="format-number(@pos,'0000 ')"/>
        <xsl:value-of select="$level"/>
        <xsl:text>(</xsl:text>
        <xsl:value-of select="substring(@tag,1,4)"/>
        <xsl:text>,</xsl:text>
        <xsl:value-of select="substring(@tag,5,4)"/>
        <xsl:text>) </xsl:text>
        <xsl:value-of select="@vr"/>
    </xsl:variable>
    <xsl:apply-templates select="val">
        <xsl:with-param name="prefix" select="$prefix"/>
        <xsl:with-param name="name" select="@name"/>
    </xsl:apply-templates>
    <xsl:apply-templates select="seq">
        <xsl:with-param name="level" select="$level"/>
        <xsl:with-param name="prefix" select="$prefix"/>
        <xsl:with-param name="name" select="@name"/>
    </xsl:apply-templates>
</xsl:template>

<xsl:template match="val">
        <xsl:param name='prefix'/>
        <xsl:param name='name'/>
    <xsl:variable name="dataLen" select="string-length(@data)"/>
    <xsl:variable name="line">
        <xsl:value-of select="$prefix"/>
        <xsl:text> #</xsl:text>
        <xsl:value-of select="@len"/>
        <xsl:text> *</xsl:text>
        <xsl:value-of select="@vm"/>
        <xsl:text> [</xsl:text>
        <xsl:choose>
            <xsl:when test="$dataLen &gt; $vallen">        
                <xsl:value-of select="substring(@data,1,$vallen - 2)"/>
                <xsl:value-of select="'..'"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="@data"/>
            </xsl:otherwise>
        </xsl:choose>
        <xsl:text>]</xsl:text>
    </xsl:variable>
    <xsl:call-template name="promptLine">
        <xsl:with-param name="line" select="$line"/>
        <xsl:with-param name="name" select="$name"/>
    </xsl:call-template>
</xsl:template>

<xsl:template name="promptLine">
        <xsl:param name="line"/>
        <xsl:param name="name"/>
    <xsl:variable name="prompt" select="concat($line,' //',$name)"/>
    <xsl:choose>
        <xsl:when test="string-length($prompt) &gt; $maxlen">        
            <xsl:value-of select="substring($prompt,1,$maxlen)"/>
        </xsl:when>
        <xsl:otherwise>
            <xsl:value-of select="$prompt"/>
        </xsl:otherwise>
    </xsl:choose>
    <xsl:text>
</xsl:text>
</xsl:template>

<xsl:template match="seq">
        <xsl:param name='level'/>
        <xsl:param name='prefix'/>
        <xsl:param name='name'/>
    <xsl:call-template name="promptLine">
        <xsl:with-param name="line">
            <xsl:value-of select="$prefix"/>
            <xsl:text> #</xsl:text>
            <xsl:value-of select="@len"/>
        </xsl:with-param>
        <xsl:with-param name="name" select="$name"/>
    </xsl:call-template>
    <xsl:apply-templates select="item">
        <xsl:with-param name="level" select="$level"/>
    </xsl:apply-templates>
    <xsl:apply-templates select="frag">
        <xsl:with-param name="level" select="$level"/>
    </xsl:apply-templates>
    <xsl:if test="@len = -1">
        <xsl:call-template name="promptLine">
            <xsl:with-param name="line" 
                          select="concat('     ',$level,'(fffe,e0dd)    #0')"/>
            <xsl:with-param name="name" select="'Sequence Delimitation Item'"/>
        </xsl:call-template>
    </xsl:if>
</xsl:template>

<xsl:template match="item">
        <xsl:param name='level'/>
    <xsl:call-template name="promptLine">
        <xsl:with-param name="line">
            <xsl:value-of select="format-number(@pos,'0000 ')"/>
            <xsl:value-of select="$level"/>
            <xsl:text>(fffe,e000) #</xsl:text>
            <xsl:value-of select="@len"/>
        </xsl:with-param>
        <xsl:with-param name="name" select="concat('Item-',@id)"/>
    </xsl:call-template>
    <xsl:apply-templates select="elm">
        <xsl:with-param name="level" select="concat($level,'&gt;')"/>
    </xsl:apply-templates>
    <xsl:if test="@len = -1">
        <xsl:call-template name="promptLine">
            <xsl:with-param name="line" 
                          select="concat('     ',$level,'(fffe,e00d)    #0')"/>
            <xsl:with-param name="name" select="'Item Delimitation Item'"/>
        </xsl:call-template>
    </xsl:if>
</xsl:template>

<xsl:template match="frag">
        <xsl:param name='level'/>
    <xsl:call-template name="promptLine">
        <xsl:with-param name="line">
            <xsl:value-of select="format-number(@pos,'0000 ')"/>
            <xsl:value-of select="$level"/>
            <xsl:text>(fffe,e000)    #</xsl:text>
            <xsl:value-of select="@len"/>
        </xsl:with-param>
        <xsl:with-param name="name" select="concat('Item-',@id)"/>
    </xsl:call-template>
</xsl:template>

</xsl:stylesheet>