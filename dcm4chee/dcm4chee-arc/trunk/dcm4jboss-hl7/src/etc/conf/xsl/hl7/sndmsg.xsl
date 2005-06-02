<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:output method="xml"/>
    <xsl:param name="SendingApplication" select="'na'"/>
    <xsl:param name="SendingFacility" select="'na'"/>
    <xsl:param name="ReceivingApplication" select="'na'"/>
    <xsl:param name="ReceivingFacility" select="'na'"/>
    <xsl:param name="DateTimeOfMessage" select="'200506020007'"/>
    <xsl:param name="MessageControlID" select="12345"/>
    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
         </xsl:copy>
    </xsl:template>
    <xsl:template match="/hl7/MSH/field[1]/text()">
        <xsl:value-of select="$SendingApplication"/>
    </xsl:template>
    <xsl:template match="/hl7/MSH/field[2]/text()">
        <xsl:value-of select="$SendingFacility"/>
    </xsl:template>
    <xsl:template match="/hl7/MSH/field[3]/text()">
        <xsl:value-of select="$ReceivingApplication"/>
    </xsl:template>
    <xsl:template match="/hl7/MSH/field[4]/text()">
        <xsl:value-of select="$ReceivingFacility"/>
    </xsl:template>
    <xsl:template match="/hl7/MSH/field[5]/text()">
        <xsl:value-of select="$DateTimeOfMessage"/>
    </xsl:template>
    <xsl:template match="/hl7/MSH/field[8]/text()">
        <xsl:value-of select="$MessageControlID"/>
    </xsl:template>
</xsl:stylesheet>
