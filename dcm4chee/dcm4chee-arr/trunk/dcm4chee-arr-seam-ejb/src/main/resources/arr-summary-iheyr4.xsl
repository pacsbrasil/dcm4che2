<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
  <xsl:template match="IHEYr4">
    <xsl:apply-templates mode="ActiveParticipant" select="*"/>
    <xsl:apply-templates mode="ParticipantObjectIdentification" select="*"/>
  </xsl:template>
  <!-- =========================================== -->
  <!-- Active Participant                          -->
  <!-- =========================================== -->
  <xsl:template mode="ActiveParticipant" match="ActorConfig">
    <xsl:apply-templates mode="ActiveParticipant" select="*"/>
  </xsl:template>
  <xsl:template mode="ActiveParticipant" match="ActorName">
    <strong>User:&#160;</strong>
    <xsl:value-of select="normalize-space(.)"/>
    <xsl:text>,&#32;</xsl:text>
  </xsl:template>
  <xsl:template mode="ActiveParticipant" match="ActorStartStop">
    <xsl:apply-templates mode="ActiveParticipant" select="*"/>
  </xsl:template>
  <xsl:template mode="ActiveParticipant" match="AET">
    <xsl:text>, AETITLE=</xsl:text>
    <xsl:value-of select="normalize-space(.)"/>
  </xsl:template>
  <xsl:template mode="ActiveParticipant" match="AuditLogUsed">
    <xsl:apply-templates mode="ActiveParticipant" select="*"/>
  </xsl:template>
  <xsl:template mode="ActiveParticipant" match="BeginStoringInstances">
    <xsl:apply-templates mode="ActiveParticipant" select="*"/>
  </xsl:template>
  <xsl:template mode="ActiveParticipant" match="Destination">
    <xsl:apply-templates mode="ActiveParticipant" select="*"/>
  </xsl:template>
  <xsl:template mode="ActiveParticipant" match="DICOMInstancesDeleted">
    <xsl:apply-templates mode="ActiveParticipant" select="*"/>
  </xsl:template>
  <xsl:template mode="ActiveParticipant" match="DICOMInstancesUsed">
    <xsl:apply-templates mode="ActiveParticipant" select="*"/>
  </xsl:template>
  <xsl:template mode="ActiveParticipant" match="DicomQuery">
    <xsl:apply-templates mode="ActiveParticipant" select="*"/>
  </xsl:template>
  <xsl:template mode="ActiveParticipant" match="Export">
    <xsl:apply-templates mode="ActiveParticipant" select="*"/>
  </xsl:template>
  <xsl:template mode="ActiveParticipant" match="Hname">
    <xsl:value-of select="normalize-space(.)"/>
    <xsl:text>,&#32;</xsl:text>
  </xsl:template>
  <xsl:template mode="ActiveParticipant" match="Import">
    <xsl:apply-templates mode="ActiveParticipant" select="*"/>
  </xsl:template>
  <xsl:template mode="ActiveParticipant" match="InstanceActionDescription">
    <xsl:apply-templates mode="ActiveParticipant" select="*"/>
  </xsl:template>
  <xsl:template mode="ActiveParticipant" match="InstancesSent">
    <xsl:apply-templates mode="ActiveParticipant" select="*"/>
  </xsl:template>
  <xsl:template mode="ActiveParticipant" match="InstancesStored">
    <strong>Req.User:&#160;</strong>
    <xsl:apply-templates mode="ActiveParticipant" select="RemoteNode"/>
    <xsl:text>,&#32;</xsl:text>
  </xsl:template>
  <xsl:template mode="ActiveParticipant" match="IP">
    <xsl:text>node=</xsl:text>
    <xsl:value-of select="normalize-space(.)"/>
  </xsl:template>
  <xsl:template mode="ActiveParticipant" match="LocalPrinter">
    <strong>User</strong>
    <xsl:text>(Destination Media):&#160;</xsl:text>
    <xsl:value-of select="normalize-space(.)"/>
    <xsl:text>,&#32;</xsl:text>
  </xsl:template>
  <xsl:template mode="ActiveParticipant" match="LocalUser">
    <xsl:value-of select="normalize-space(.)"/>
  </xsl:template>
  <xsl:template mode="ActiveParticipant" match="LocalUsername">
    <strong>Req.User:&#160;</strong>
    <xsl:value-of select="normalize-space(.)"/>
    <xsl:text>,&#32;</xsl:text>
  </xsl:template>
  <xsl:template mode="ActiveParticipant" match="MediaID">
    <strong>User:&#160;</strong>
    <xsl:value-of select="normalize-space(.)"/>
    <xsl:text>,&#32;</xsl:text>
  </xsl:template>
  <xsl:template mode="ActiveParticipant" match="NetworkEntry">
    <strong>User:&#160;</strong>
    <xsl:value-of select="normalize-space(../Host)"/>
    <xsl:text>,&#32;</xsl:text>
  </xsl:template>
  <xsl:template mode="ActiveParticipant" match="Node">
    <strong>User:&#160;</strong>
    <xsl:apply-templates mode="ActiveParticipant" select="*"/>
    <xsl:text>,&#32;</xsl:text>
  </xsl:template>
  <xsl:template mode="ActiveParticipant" match="OrderRecord">
    <xsl:apply-templates mode="ActiveParticipant" select="*"/>
  </xsl:template>
  <xsl:template mode="ActiveParticipant" match="PatientRecord">
    <xsl:apply-templates mode="ActiveParticipant" select="*"/>
  </xsl:template>
  <xsl:template mode="ActiveParticipant" match="ProcedureRecord">
    <xsl:apply-templates mode="ActiveParticipant" select="*"/>
  </xsl:template>
  <xsl:template mode="ActiveParticipant" match="RemoteNode">
    <xsl:apply-templates mode="ActiveParticipant" select="*"/>
  </xsl:template>
  <xsl:template mode="ActiveParticipant" match="Requestor">
    <strong>Req.User:&#160;</strong>
    <xsl:apply-templates mode="ActiveParticipant" select="*"/>
    <xsl:text>,&#32;</xsl:text>
  </xsl:template>
  <xsl:template mode="ActiveParticipant" match="RNode">
    <strong>User:&#160;</strong>
    <xsl:apply-templates mode="ActiveParticipant" select="*"/>
    <xsl:text>,&#32;</xsl:text>
  </xsl:template>
  <xsl:template mode="ActiveParticipant" match="SecurityAlert">
    <xsl:apply-templates mode="ActiveParticipant" select="*"/>
    <strong>User:&#160;</strong>
    <xsl:value-of select="normalize-space(../Host)"/>
    <xsl:text>,&#32;</xsl:text>
  </xsl:template>
  <xsl:template mode="ActiveParticipant" match="StudyDeleted">
    <xsl:apply-templates mode="ActiveParticipant" select="*"/>
  </xsl:template>
  <xsl:template mode="ActiveParticipant" match="User">
    <strong>Req.User:&#160;</strong>
    <xsl:apply-templates mode="ActiveParticipant" select="*"/>
    <xsl:text>,&#32;</xsl:text>
  </xsl:template>
  <xsl:template mode="ActiveParticipant" match="UserAuthenticated">
    <xsl:apply-templates mode="ActiveParticipant" select="*"/>
  </xsl:template>
  <xsl:template mode="ActiveParticipant" match="*"/>
  <!-- =========================================== -->
  <!-- Active Participant / RoleIDCode             -->
  <!-- =========================================== -->
  <!-- =========================================== -->
  <!-- ParticipantObjectIdentification             -->
  <!-- =========================================== -->
  <xsl:template mode="ParticipantObjectIdentification"
    match="BeginStoringInstances">
    <xsl:apply-templates mode="ParticipantObjectIdentification" select="*"/>
  </xsl:template>
  <xsl:template mode="ParticipantObjectIdentification"
    match="DICOMInstancesDeleted">
    <xsl:apply-templates mode="ParticipantObjectIdentification" select="*"/>
  </xsl:template>
  <xsl:template mode="ParticipantObjectIdentification"
    match="DICOMInstancesUsed">
    <xsl:apply-templates mode="ParticipantObjectIdentification" select="*"/>
  </xsl:template>
  <!--
  <xsl:template mode="ParticipantObjectIdentification" match="DicomQuery">
    <xsl:text>,&#32;</xsl:text>
    <strong>Report:&#160;</strong>
    <span title="SOP Class UID"><xsl:value-of select="CUID"/> </span>
  </xsl:template>
  -->
  <xsl:template mode="ParticipantObjectIdentification" match="Export">
    <xsl:apply-templates mode="ParticipantObjectIdentification" select="*"/>
  </xsl:template>
  <xsl:template mode="ParticipantObjectIdentification" match="Import">
    <xsl:apply-templates mode="ParticipantObjectIdentification" select="*"/>
  </xsl:template>
  <xsl:template mode="ParticipantObjectIdentification"
    match="InstanceActionDescription">
    <xsl:apply-templates mode="ParticipantObjectIdentification" select="*"/>
  </xsl:template>
  <xsl:template mode="ParticipantObjectIdentification" match="InstancesSent">
    <xsl:apply-templates mode="ParticipantObjectIdentification" select="*"/>
  </xsl:template>
  <xsl:template mode="ParticipantObjectIdentification" match="InstancesStored">
    <xsl:apply-templates mode="ParticipantObjectIdentification" select="*"/>
  </xsl:template>
  <xsl:template mode="ParticipantObjectIdentification" match="OrderRecord">
    <xsl:apply-templates mode="ParticipantObjectIdentification" select="*"/>
  </xsl:template>
  <xsl:template mode="ParticipantObjectIdentification" match="Patient">
    <strong>Patient:&#160;</strong>
    <xsl:apply-templates mode="ParticipantObjectIdentification"
      select="PatientID"/>
    <xsl:apply-templates mode="ParticipantObjectIdentification"
      select="PatientName"/>
    <xsl:apply-templates mode="ParticipantObjectIdentification" select="SUID"/>
    <xsl:text>,&#32;</xsl:text>
  </xsl:template>
  <xsl:template mode="ParticipantObjectIdentification" match="PatientID">
    <xsl:text>id=</xsl:text>
    <xsl:value-of select="normalize-space(.)"/>
  </xsl:template>
  <xsl:template mode="ParticipantObjectIdentification" match="PatientName">
    <xsl:value-of select="normalize-space(.)"/>
    <xsl:text>,&#32;name=</xsl:text>
  </xsl:template>
  <xsl:template mode="ParticipantObjectIdentification" match="PatientRecord">
    <xsl:apply-templates mode="ParticipantObjectIdentification" select="*"/>
  </xsl:template>
  <xsl:template mode="ParticipantObjectIdentification" match="ProcedureRecord">
    <xsl:apply-templates mode="ParticipantObjectIdentification" select="*"/>
  </xsl:template>
  <xsl:template mode="ParticipantObjectIdentification" match="SecurityAlert">
    <strong>Security&#160;Resource:&#160;</strong>
    <xsl:value-of select="normalize-space(../Host)"/>
    <xsl:text>,&#32;</xsl:text>
  </xsl:template>
  <xsl:template mode="ParticipantObjectIdentification" match="StudyDeleted">
    <xsl:apply-templates mode="ParticipantObjectIdentification" select="*"/>
  </xsl:template>
  <xsl:template mode="ParticipantObjectIdentification" match="AccessionNumber">
    <strong title="Accession Number">Acc.No:&#160;</strong>
    <xsl:value-of select="normalize-space(.)"/>
    <xsl:text>,&#32;</xsl:text>
  </xsl:template>
  <xsl:template mode="ParticipantObjectIdentification" match="SUID">
    <strong>Study:&#160;</strong>
    <xsl:text>uid=</xsl:text>
    <xsl:value-of select="normalize-space(.)"/>
    <xsl:text>,&#32;</xsl:text>
  </xsl:template>
  <xsl:template mode="ParticipantObjectIdentification" match="NumberOfInstances">
    <strong title="Number of Instances">NoI:&#160;</strong>
    <xsl:value-of select="normalize-space(.)"/>
    <xsl:text>,&#32;</xsl:text>
  </xsl:template>
  <xsl:template mode="ParticipantObjectIdentification" match="*"/>
  <!-- =========================================== -->
  <!-- Discard everything else                     -->
  <!-- =========================================== -->
  <xsl:template match="*"/>
</xsl:stylesheet>
