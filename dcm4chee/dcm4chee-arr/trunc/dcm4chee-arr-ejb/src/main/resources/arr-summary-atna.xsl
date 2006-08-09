<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
  <xsl:template match="AuditMessage">
    <xsl:apply-templates select="EventIdentification"/>
    <strong>Audit&#160;Source:&#160;</strong>
    <xsl:apply-templates select="AuditSourceIdentification"/>
    <xsl:apply-templates select="ActiveParticipant"/>
    <xsl:apply-templates select="ParticipantObjectIdentification"/>
    <xsl:text>; </xsl:text>
  </xsl:template>
  <xsl:template match="EventIdentification">
    <strong>
      <xsl:value-of select="EventID/@displayName"/>
      <xsl:apply-templates select="EventTypeCode"/>
      <xsl:text>: </xsl:text>
    </strong>
  </xsl:template>
  <xsl:template match="EventTypeCode">
    <xsl:choose>
      <xsl:when test="position()=1">(</xsl:when>
      <xsl:otherwise>, </xsl:otherwise>
    </xsl:choose>
    <xsl:value-of select="@displayName"/>
    <xsl:if test="position()=last()">)</xsl:if>
  </xsl:template>
  <xsl:template match="AuditSourceIdentification">    
    <xsl:if test="position()>1">, </xsl:if>
    <xsl:value-of select="@AuditSourceID"/>
    <xsl:if test="@AuditEnterpriseSiteID">
      <xsl:text>/</xsl:text>
      <xsl:value-of select="@AuditEnterpriseSiteID"/>
    </xsl:if>
  </xsl:template>
  <xsl:template match="ActiveParticipant">
    <xsl:text>; </xsl:text>
    <strong>
      <xsl:choose>
        <xsl:when test="@UserIsRequestor!='false'">Requestor</xsl:when>
        <xsl:otherwise>User</xsl:otherwise>
      </xsl:choose>
      <xsl:apply-templates select="RoleIDCode"/>
      <xsl:text>:&#160;</xsl:text>
    </strong>
    <xsl:choose>
      <xsl:when test="@UserName">
        <xsl:value-of select="@UserName"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:choose>
          <xsl:when test="@AlternativeUserID">
            <xsl:value-of select="@AlternativeUserID"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="@UserID"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  <xsl:template match="RoleIDCode">
    <xsl:choose>
      <xsl:when test="position()=1">(</xsl:when>
      <xsl:otherwise>, </xsl:otherwise>
    </xsl:choose>
    <xsl:value-of select="@displayName"/>
    <xsl:if test="position()=last()">)</xsl:if>
  </xsl:template>
  <xsl:template match="ParticipantObjectIdentification">    
    <xsl:text>; </xsl:text>
    <strong>
      <xsl:choose>
        <xsl:when test="@ParticipantObjectTypeCode=1">Person</xsl:when>
        <xsl:when test="@ParticipantObjectTypeCode=2">System&#160;object</xsl:when>
        <xsl:when test="@ParticipantObjectTypeCode=3">Organization</xsl:when>
        <xsl:when test="@ParticipantObjectTypeCode=4">Other</xsl:when>
        <xsl:otherwise>Object</xsl:otherwise>
      </xsl:choose>
      <xsl:choose>
        <xsl:when test="@ParticipantObjectTypeCodeRole=1">(Patient)</xsl:when>
        <xsl:when test="@ParticipantObjectTypeCodeRole=2">(Location)</xsl:when>
        <xsl:when test="@ParticipantObjectTypeCodeRole=3">(Report)</xsl:when>
        <xsl:when test="@ParticipantObjectTypeCodeRole=4">(Resource)</xsl:when>
        <xsl:when test="@ParticipantObjectTypeCodeRole=5">(Master&#160;file)</xsl:when>
        <xsl:when test="@ParticipantObjectTypeCodeRole=6">(User)</xsl:when>
        <xsl:when test="@ParticipantObjectTypeCodeRole=7">(List)</xsl:when>
        <xsl:when test="@ParticipantObjectTypeCodeRole=8">(Doctor)</xsl:when>
        <xsl:when test="@ParticipantObjectTypeCodeRole=9">(Subscriber)</xsl:when>
        <xsl:when test="@ParticipantObjectTypeCodeRole=10">(Guarantor)</xsl:when>
        <xsl:when test="@ParticipantObjectTypeCodeRole=11">(Security&#160;User&#160;Entity)</xsl:when>
        <xsl:when test="@ParticipantObjectTypeCodeRole=12">(Security&#160;User&#160;Group)</xsl:when>
        <xsl:when test="@ParticipantObjectTypeCodeRole=13">(Security&#160;Resource)</xsl:when>
        <xsl:when test="@ParticipantObjectTypeCodeRole=14">(Security&#160;Granualarity&#160;Definition)</xsl:when>
        <xsl:when test="@ParticipantObjectTypeCodeRole=15">(Provider)</xsl:when>
        <xsl:when test="@ParticipantObjectTypeCodeRole=16">(Report&#160;Destination)</xsl:when>
        <xsl:when test="@ParticipantObjectTypeCodeRole=17">(Report&#160;Library)</xsl:when>
        <xsl:when test="@ParticipantObjectTypeCodeRole=18">(Schedule)</xsl:when>
        <xsl:when test="@ParticipantObjectTypeCodeRole=19">(Customer)</xsl:when>
        <xsl:when test="@ParticipantObjectTypeCodeRole=20">(Job)</xsl:when>
        <xsl:when test="@ParticipantObjectTypeCodeRole=21">(Job&#160;Stream)</xsl:when>
        <xsl:when test="@ParticipantObjectTypeCodeRole=22">(Table)</xsl:when>
        <xsl:when test="@ParticipantObjectTypeCodeRole=23">(Routing&#160;Criteria)</xsl:when>
        <xsl:when test="@ParticipantObjectTypeCodeRole=24">(Query)</xsl:when>
      </xsl:choose>
      <xsl:text>: </xsl:text>
    </strong>    
    <xsl:apply-templates select="ParticipantObjectIDTypeCode"/>
    <xsl:text>=</xsl:text>
    <xsl:value-of select="@ParticipantObjectID"/>
  </xsl:template>
  <xsl:template match="ParticipantObjectIDTypeCode">
    <xsl:choose>
      <xsl:when test="@displayName"><xsl:value-of select="@displayName"/></xsl:when>
      <xsl:when test="@code=1">Medical&#160;Record&#160;Number</xsl:when>
      <xsl:when test="@code=2">Patient&#160;Number</xsl:when>
      <xsl:when test="@code=3">Encounter&#160;Number</xsl:when>
      <xsl:when test="@code=4">Enrollee&#160;Number</xsl:when>
      <xsl:when test="@code=5">Social&#160;Security&#160;Number</xsl:when>
      <xsl:when test="@code=6">Account&#160;Number</xsl:when>
      <xsl:when test="@code=7">Guarantor&#160;Number</xsl:when>
      <xsl:when test="@code=8">Report&#160;Name</xsl:when>
      <xsl:when test="@code=9">Report&#160;Number</xsl:when>
      <xsl:when test="@code=10">Search&#160;Criteria</xsl:when>
      <xsl:when test="@code=11">User&#160;Identifier</xsl:when>
      <xsl:when test="@code=12">URI</xsl:when>
      <xsl:otherwise>id</xsl:otherwise>
    </xsl:choose>
  </xsl:template>
</xsl:stylesheet>
