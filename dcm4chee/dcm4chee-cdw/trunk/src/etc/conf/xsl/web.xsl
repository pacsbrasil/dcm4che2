<?xml version="1.0" encoding="US-ASCII"?>
<!DOCTYPE xsl:stylesheet [
<!ENTITY nbsp "&#160;">
]>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns="http://www.w3.org/1999/xhtml" xmlns:redirect="http://xml.apache.org/xalan/redirect" extension-element-prefixes="redirect" version="1.0">
  <xsl:output method="xml" encoding="UTF-8" indent="yes" doctype-public="-//W3C//DTD XHTML 1.0 Transitional//EN" doctype-system="http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd"/>
  <xsl:template match="/">
    <html>
      <head>
        <title>DICOM CD
			<xsl:value-of select="dicomdir/attr[@tag='(0004,1130)']"/>
		</title>
      </head>
      <frameset cols="20%,80%">
        <frameset rows="35%,35%,30%">
          <frame src="TOC.HTM" name="patlist"/>
          <redirect:write select="'TOC.HTM'">
            <html>
              <head>
                <title>Patients</title>
                <link rel="stylesheet" href="STYLE.CSS" type="text/css"/>
              </head>
              <body background="BACKGRND.JPG" link="#FF0000" alink="#FF0000" vlink="#FF0000">
                <table>
                  <tr>
                    <td nowrap="nowrap">
                      <b>Patients:</b>
                      <br/>
                      <xsl:apply-templates select="dicomdir/record[@type='PATIENT']" mode="toc">
                        <xsl:sort data-type="text" order="ascending" select="attr[@tag='(0010,0010)']"/>
                      </xsl:apply-templates>
                    </td>
                  </tr>
                </table>
              </body>
            </html>
          </redirect:write>
          <frame src="SELECT.HTM" name="stylist"/>
          <frame src="SELECT.HTM" name="serlist"/>
        </frameset>
        <frame src="HOME.HTM" name="view"/>
        <redirect:write select="'HOME.HTM'">
          <html>
            <head>
              <title>DICOM CD
						<xsl:value-of select="dicomdir/attr[@tag='(0004,1130)']"/>
					</title>
              <link rel="stylesheet" href="STYLE.CSS" type="text/css"/>
            </head>
            <body background="BACKGRND.JPG" link="#FF0000" alink="#FF0000" vlink="#FF0000">
              <table width="100%">
                <tr>
                  <td><b>Home</b><br/><a href="INDEX.HTM" target="_top">FRAMES</a>&nbsp;&nbsp;<a href="HOME.HTM" target="_top">NO FRAMES</a></td>
                  <td align="right">
                    <a href="http://www.tiani.com" target="_blank">
                      <img src="TIANI.GIF" border="0"/>
                    </a>
                  </td>
                </tr>
              </table>
              <table border="0" cellspacing="1" cellpadding="1">
                <tr align="left">
                  <th>Patient Name</th>
                  <th>ID</th>
                  <th>Birth Date</th>
                  <th>Sex</th>
                  <th>Studies</th>
                  <th>Files</th>
                </tr>
                <xsl:apply-templates select="dicomdir/record[@type='PATIENT']" mode="home">
                  <xsl:sort data-type="text" order="ascending" select="attr[@tag='(0010,0010)']"/>
                </xsl:apply-templates>
              </table>
            </body>
          </html>
        </redirect:write>
      </frameset>
    </html>
  </xsl:template>
  <xsl:template match="record[@type='PATIENT']" mode="toc">
    <xsl:variable name="patdir">
      <xsl:call-template name="patDir">
        <xsl:with-param name="file" select="record/record/record/attr[@tag='(0004,1500)']"/>
      </xsl:call-template>
    </xsl:variable>
    <a href="{concat($patdir,'TOC.HTM')}" target="stylist">
      <xsl:call-template name="formatPN">
        <xsl:with-param name="pn" select="attr[@tag='(0010,0010)']"/>
      </xsl:call-template>
    </a>
    <br/>
  </xsl:template>
  <xsl:template match="record[@type='PATIENT']" mode="home">
    <xsl:variable name="patdir">
      <xsl:call-template name="patDir">
        <xsl:with-param name="file" select="record/record/record/attr[@tag='(0004,1500)']"/>
      </xsl:call-template>
    </xsl:variable>
    <tr>
      <td>
        <a href="{concat($patdir,'INDEX.HTM')}">
          <xsl:call-template name="formatPN">
            <xsl:with-param name="pn" select="attr[@tag='(0010,0010)']"/>
          </xsl:call-template>
        </a>
      </td>
      <td>
        <xsl:value-of select="attr[@tag='(0010,0020)']"/>
      </td>
      <td>
        <xsl:call-template name="formatDate">
          <xsl:with-param name="date" select="attr[@tag='(0010,0030)']"/>
        </xsl:call-template>
      </td>
      <td align="center">
        <xsl:value-of select="attr[@tag='(0010,0040)']"/>
      </td>
      <td align="center">
        <xsl:value-of select="count(record)"/>
      </td>
      <td align="center">
        <xsl:value-of select="count(record/record/record)"/>
      </td>
    </tr>
  </xsl:template>
  <xsl:template name="formatPN">
    <xsl:param name="pn"/>
    <xsl:value-of select="translate($pn,'^',',')"/>
  </xsl:template>
  <xsl:template name="formatDate">
    <xsl:param name="date"/>
    <xsl:value-of select="substring($date,1,4)"/>
    <xsl:text>-</xsl:text>
    <xsl:value-of select="substring($date,5,2)"/>
    <xsl:text>-</xsl:text>
    <xsl:value-of select="substring($date,7)"/>
  </xsl:template>
  <xsl:template name="patDir">
    <xsl:param name="file"/>
    <xsl:value-of select="substring($file,7,string-length($file)-15)"/>
  </xsl:template>
</xsl:stylesheet>
