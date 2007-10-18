<?xml version="1.0" encoding="UTF-8"?>
<!-- Sample configuration for grant/revoke Study Permissions on received HL7 messages -->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
  <xsl:output method="xml"/>
  <xsl:template match="/hl7">
    <permissions>
      <xsl:apply-templates select="MSH/field[7]" mode="MessageType"/>
    </permissions>
  </xsl:template>
  
  <!-- on Procedure Scheduled (ORM^O01) -->
  <xsl:template match="field[text()='ORM'][component='O01']" mode="MessageType">
    <!-- grant Query and Read permission on all exisiting Studies of this
    Patient to Doctor -->   
    <grant role="Doctor" action="Q,R">
      <xsl:apply-templates select="/hl7/PID"/>
    </grant>
    <!-- grant Query, Read and Append permission on scheduled Study to Doctor -->   
    <grant role="Doctor" action="Q,R,A">
      <xsl:apply-templates select="/hl7/ZDS"/>
    </grant>
  </xsl:template>
  
  <!-- on Patient Discharge (ADT^A03) -->
  <xsl:template match="field[text()='ADT'][component='A03']" mode="MessageType">
    <!-- revoke Query, Read and Append permission on all exisiting Studies of this
    Patient to Doctor -->   
    <revoke role="Doctor" action="Q,R,A">
      <xsl:apply-templates select="/hl7/PID"/>
    </revoke>
  </xsl:template>

  <xsl:template match="*" mode="MessageType"/>
  
  <xsl:template match="PID">
    <xsl:attribute name="pid">
      <xsl:value-of select="field[3]/text()"/>
    </xsl:attribute>
    <xsl:attribute name="issuer">
      <xsl:value-of select="field[3]/component[3]"/>
    </xsl:attribute>
  </xsl:template>
  
  <xsl:template match="ZDS">
    <xsl:attribute name="suid">
      <xsl:value-of select="field[1]/text()"/>
    </xsl:attribute>
  </xsl:template>
  
</xsl:stylesheet>
