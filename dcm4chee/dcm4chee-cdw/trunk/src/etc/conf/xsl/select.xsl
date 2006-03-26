<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:redirect="http://xml.apache.org/xalan/redirect"
  extension-element-prefixes="redirect" version="1.0">
  <xsl:template match="dicomdir" mode="select">
    <redirect:write select="'SELECT.HTM'">
      <html>
        <head>
          <title></title>
          <link rel="stylesheet" type="text/css">
            <xsl:call-template name="href">
              <xsl:with-param name="value" select="'STYLE.CSS'"/>
            </xsl:call-template>
          </link>
        </head>
        <body><p>select item above</p></body>
      </html>
    </redirect:write>
  </xsl:template>
</xsl:stylesheet>
