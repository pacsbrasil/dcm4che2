<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
  <xsl:template match="IHEYr4">
    <xsl:apply-templates select="*" mode="EventIdentification"/>
    <br/>
    <xsl:apply-templates select="*" mode="ActiveParticipant"/>
    <br/>
    <xsl:apply-templates select="*" mode="AuditSourceIdentification"/>
    <br/>
    <xsl:apply-templates select="*" mode="ParticipantObjectIdentification"/>
  </xsl:template>
  <!-- =========================================== -->
  <!-- EventIdentification                         -->
  <!-- =========================================== -->
  <xsl:template mode="EventIdentification" match="ActorConfig">
    <strong>Application Activity: </strong>
    <xsl:apply-templates select="*" mode="EventTypeCode"/>
  </xsl:template>
  <xsl:template mode="EventIdentification" match="ActorStartStop">
    <strong>Application Activity: </strong>
    <xsl:apply-templates select="*" mode="EventTypeCode"/>
  </xsl:template>
  <xsl:template mode="EventIdentification" match="AuditLogUsed">
    <strong>Audit Log Used: </strong>
  </xsl:template>
  <xsl:template mode="EventIdentification" match="BeginStoringInstances">
    <strong>Begin Transferring DICOM Instances: </strong>
  </xsl:template>
  <xsl:template mode="EventIdentification" match="DICOMInstancesDeleted">
    <strong>DICOM Instances Accessed: </strong>
  </xsl:template>
  <xsl:template mode="EventIdentification" match="DICOMInstancesUsed">
    <strong>DICOM Instances Accessed: </strong>
  </xsl:template>
  <xsl:template mode="EventIdentification" match="DicomQuery">
    <strong>Query: </strong>
  </xsl:template>
  <xsl:template mode="EventIdentification" match="Export">
    <strong>Export: </strong>
  </xsl:template>
  <xsl:template mode="EventIdentification" match="Import">
    <strong>Import: </strong>
  </xsl:template>
  <xsl:template mode="EventIdentification" match="InstancesSent">
    <strong>DICOM Instances Transferred: </strong>
    <xsl:apply-templates select="*" mode="EventTypeCode"/>
  </xsl:template>
  <xsl:template mode="EventIdentification" match="InstancesStored">
    <strong>DICOM Instances Transferred: </strong>
  </xsl:template>
  <xsl:template mode="EventIdentification" match="NetworkEntry">
    <strong>NetworkEntry: </strong>
    <xsl:apply-templates select="*" mode="EventTypeCode"/>
  </xsl:template>
  <xsl:template mode="EventIdentification" match="OrderRecord">
    <strong>Order Record: </strong>
  </xsl:template>
  <xsl:template mode="EventIdentification" match="PatientRecord">
    <strong>Patient Record: </strong>
  </xsl:template>
  <xsl:template mode="EventIdentification" match="ProcedureRecord">
    <strong>Procedure Record: </strong>
  </xsl:template>
  <xsl:template mode="EventIdentification" match="SecurityAlert">
    <strong>Security Alert: </strong>
    <xsl:apply-templates select="*" mode="EventTypeCode"/>
  </xsl:template>
  <xsl:template mode="EventIdentification" match="StudyDeleted">
    <strong>DICOM Study Deleted </strong>
  </xsl:template>
  <xsl:template mode="EventIdentification" match="UserAuthenticated">
    <strong>User Authentication: </strong>
    <xsl:apply-templates select="*" mode="EventTypeCode"/>
  </xsl:template>
  <xsl:template mode="EventIdentification" match="*"/>
  <!-- =========================================== -->
  <!-- EventIdentification / EventTypeCode         -->
  <!-- =========================================== -->
  <xsl:template mode="EventTypeCode" match="Action">
    <xsl:choose>
      <xsl:when test=". = 'Login'">Login </xsl:when>
      <xsl:when test=". = 'Logout'">Logout </xsl:when>
      <xsl:when test=". = 'Failure'">Login </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="."/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  <xsl:template mode="EventTypeCode" match="AlertType">
    <xsl:choose>
      <xsl:when test=". = 'NodeAuthenticationFailure'">Node Authentication </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="."/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  <xsl:template mode="EventTypeCode" match="ApplicationAction">
    <xsl:choose>
      <xsl:when test=". = 'Start'">Application Start </xsl:when>
      <xsl:when test=". = 'Stop'">Application Stop </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="."/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  <xsl:template mode="EventTypeCode" match="ConfigType">
    <xsl:choose>
      <xsl:when test=". = 'Networking'">Network Configuration </xsl:when>
      <xsl:when test=". = 'Security'">Security Configuration </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="."/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  <xsl:template mode="EventTypeCode" match="MachineAction">
    <xsl:choose>
      <xsl:when test=". = 'Attach'">Attach </xsl:when>
      <xsl:when test=". = 'Detach'">Detach </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="."/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  <xsl:template mode="EventTypeCode" match="*"/>
  <!-- =========================================== -->
  <!-- Active Participant                          -->
  <!-- =========================================== -->
  <xsl:template mode="ActiveParticipant" match="ActorConfig">
    <xsl:apply-templates select="*" mode="ActiveParticipant"/>
  </xsl:template>
  <xsl:template mode="ActiveParticipant" match="ActorName">
    <strong>User:&#160;</strong>
    <xsl:value-of select="."/>
    <xsl:text>&#32;</xsl:text>
  </xsl:template>
  <xsl:template mode="ActiveParticipant" match="ActorStartStop">
    <xsl:apply-templates select="*" mode="ActiveParticipant"/>
  </xsl:template>
  <xsl:template mode="ActiveParticipant" match="AET">
    <xsl:text>AETITLE=</xsl:text>
    <xsl:value-of select="."/>
    <xsl:text>&#32;</xsl:text>
  </xsl:template>
  <xsl:template mode="ActiveParticipant" match="AuditLogUsed">
    <xsl:apply-templates select="*" mode="ActiveParticipant"/>
  </xsl:template>
  <xsl:template mode="ActiveParticipant" match="BeginStoringInstances">
    <xsl:apply-templates select="*" mode="ActiveParticipant"/>
  </xsl:template>
  <xsl:template mode="ActiveParticipant" match="Destination">
    <xsl:apply-templates select="*" mode="ActiveParticipant"/>
  </xsl:template>
  <xsl:template mode="ActiveParticipant" match="DICOMInstancesDeleted">
    <xsl:apply-templates select="*" mode="ActiveParticipant"/>
  </xsl:template>
  <xsl:template mode="ActiveParticipant" match="DICOMInstancesUsed">
    <xsl:apply-templates select="*" mode="ActiveParticipant"/>
  </xsl:template>
  <xsl:template mode="ActiveParticipant" match="DicomQuery">
    <xsl:apply-templates select="*" mode="ActiveParticipant"/>
  </xsl:template>
  <xsl:template mode="ActiveParticipant" match="Export">
    <xsl:apply-templates select="*" mode="ActiveParticipant"/>
  </xsl:template>
  <xsl:template mode="ActiveParticipant" match="Hname">
    <xsl:value-of select="."/>
    <xsl:text>&#32;</xsl:text>
  </xsl:template>
  <xsl:template mode="ActiveParticipant" match="Import">
    <xsl:apply-templates select="*" mode="ActiveParticipant"/>
  </xsl:template>
  <xsl:template mode="ActiveParticipant" match="InstanceActionDescription">
    <xsl:apply-templates select="*" mode="ActiveParticipant"/>
  </xsl:template>
  <xsl:template mode="ActiveParticipant" match="InstancesSent">
    <xsl:apply-templates select="*" mode="ActiveParticipant"/>
  </xsl:template>
  <xsl:template mode="ActiveParticipant" match="InstancesStored">
    <strong>Requestor:&#160;</strong>
    <xsl:apply-templates mode="ActiveParticipant" select="RemoteNode"/>
  </xsl:template>
  <xsl:template mode="ActiveParticipant" match="IP">
    <xsl:text>node=</xsl:text>
    <xsl:value-of select="."/>
    <xsl:text>&#32;</xsl:text>
  </xsl:template>
  <xsl:template mode="ActiveParticipant" match="LocalPrinter">
    <strong>Destination Media:&#160;</strong>
    <xsl:value-of select="."/>
    <xsl:text>&#32;</xsl:text>
  </xsl:template>
  <xsl:template mode="ActiveParticipant" match="LocalUser">
    <xsl:value-of select="."/>
    <xsl:text>&#32;</xsl:text>
  </xsl:template>
  <xsl:template mode="ActiveParticipant" match="LocalUsername">
    <strong>Requestor:&#160;</strong>
    <xsl:value-of select="."/>
    <xsl:text>&#32;</xsl:text>
  </xsl:template>
  <xsl:template mode="ActiveParticipant" match="MediaID">
    <strong>User:&#160;</strong>
    <xsl:value-of select="."/>
    <xsl:text>&#32;</xsl:text>
  </xsl:template>
  <xsl:template mode="ActiveParticipant" match="NetworkEntry">
    <strong>User:&#160;</strong>
    <xsl:value-of select="../Host"/>
    <xsl:text>&#32;</xsl:text>
  </xsl:template>
  <xsl:template mode="ActiveParticipant" match="Node">
    <strong>User:&#160;</strong>
    <xsl:apply-templates mode="ActiveParticipant" select="*"/>
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
    <strong>Requestor:&#160;</strong>
    <xsl:apply-templates mode="ActiveParticipant" select="*"/>
  </xsl:template>
  <xsl:template mode="ActiveParticipant" match="RNode">
    <strong>User:&#160;</strong>
    <xsl:apply-templates mode="ActiveParticipant" select="*"/>
  </xsl:template>
  <xsl:template mode="ActiveParticipant" match="SecurityAlert">
    <xsl:apply-templates mode="ActiveParticipant" select="*"/>
    <strong>User:&#160;</strong>
    <xsl:value-of select="../Host"/>
    <xsl:text>&#32;</xsl:text>
  </xsl:template>
  <xsl:template mode="ActiveParticipant" match="StudyDeleted">
    <xsl:apply-templates mode="ActiveParticipant" select="*"/>
  </xsl:template>
  <xsl:template mode="ActiveParticipant" match="User">
    <strong>Requestor:&#160;</strong>
    <xsl:apply-templates mode="ActiveParticipant" select="*"/>
  </xsl:template>
  <xsl:template mode="ActiveParticipant" match="UserAuthenticated">
    <xsl:apply-templates mode="ActiveParticipant" select="*"/>
  </xsl:template>
  <xsl:template mode="ActiveParticipant" match="*"/>
  <!-- =========================================== -->
  <!-- Active Participant / RoleIDCode             -->
  <!-- =========================================== -->
  <!-- =========================================== -->
  <!-- AuditSourceIdentification                   -->
  <!-- =========================================== -->
  <xsl:template mode="AuditSourceIdentification" match="Host">
    <strong>Audit Source:&#160;</strong>
    <xsl:value-of select="../Host"/>
    <xsl:text>&#32;</xsl:text>
  </xsl:template>
  <xsl:template mode="AuditSourceIdentification" match="*"/>
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
    <xsl:apply-templates mode="ParticipantObjectIdentification"/>
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
  </xsl:template>
  <xsl:template mode="ParticipantObjectIdentification" match="PatientID">
    <xsl:text>ID=</xsl:text>
    <xsl:value-of select="."/>
    <xsl:text>&#32;</xsl:text>
  </xsl:template>
  <xsl:template mode="ParticipantObjectIdentification" match="PatientName">
    <xsl:value-of select="."/>
    <xsl:text>&#32;</xsl:text>
  </xsl:template>
  <xsl:template mode="ParticipantObjectIdentification" match="PatientRecord">
    <xsl:apply-templates mode="ParticipantObjectIdentification" select="*"/>
  </xsl:template>
  <xsl:template mode="ParticipantObjectIdentification" match="ProcedureRecord">
    <xsl:apply-templates mode="ParticipantObjectIdentification" select="*"/>
  </xsl:template>
  <xsl:template mode="ParticipantObjectIdentification" match="SecurityAlert">
    <strong>Security Resource:&#160;</strong>
    <xsl:value-of select="../Host"/>
    <xsl:text>&#32;</xsl:text>
  </xsl:template>
  <xsl:template mode="ParticipantObjectIdentification" match="StudyDeleted">
    <xsl:apply-templates mode="ParticipantObjectIdentification" select="*"/>
  </xsl:template>
  <xsl:template mode="ParticipantObjectIdentification" match="SUID">
    <strong>Study:&#160;</strong>
    <xsl:text>UID=</xsl:text>
    <xsl:value-of select="."/>
    <xsl:text>&#32;</xsl:text>
  </xsl:template>
  <xsl:template mode="ParticipantObjectIdentification" match="*"/>
  <!-- =========================================== -->
  <!-- Discard everything else                     -->
  <!-- =========================================== -->
  <xsl:template match="*"/>
</xsl:stylesheet>
