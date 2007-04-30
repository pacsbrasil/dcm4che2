<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
  <xsl:output method="xml"/>
  <xsl:template match="/dataset">
    <destinations>
      <!-- Forward all Series to LONG_TERM outside business hours (7-19) after one week -->
      <destination aet="LONG_TERM" delay="1w!7-19"/>

      <!-- Forward Series with specified Referring Phyisican with low priority
        to PHYSICAN_DOE  after 3 days -->
      <xsl:if test="attr[@tag='00080090']='Doe^John'">
        <destination aet="PHYSICAN_DOE" priority="low" delay="3d"/>
      </xsl:if>
      
      <!-- Forward Magnetic Resonance Series with high priority 
        to MR_WORKSTATION immediately -->
      <xsl:if test="attr[@tag='00080060']='MR'">
        <destination aet="MR_WORKSTATION" priority="high"/>
      </xsl:if>
      
      <!-- Forward Series requested by Neuro Surgery to NEURO_SURGERY immediately -->
      <xsl:if test="attr[tag='00400275']/item/attr[@tag='00321033']='Neuro Surgery'">
        <destination aet="NEURO_SURGERY"/>
      </xsl:if>
      
    </destinations>
  </xsl:template>

</xsl:stylesheet>