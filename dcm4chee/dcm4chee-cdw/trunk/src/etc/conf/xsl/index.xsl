<?xml version="1.0" encoding="ISO-8859-1"?>
<!DOCTYPE xsl:stylesheet [
<!ENTITY nbsp "&#160;">
]>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:exsl="http://exslt.org/common" xmlns="http://www.w3.org/1999/xhtml" exclude-result-prefixes="exsl" version="1.0">
  <xsl:output method="ht" encoding="ISO-8859-1" indent="yes" doctype-public="-//W3C//DTD XHTML 1.0 Transitional//EN" doctype-system="http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd"/>
  <xsl:template match="/">
    <html xmlns="http://www.w3.org/1999/xhtml">
      <head>
        <title>[Title of INDEX.HTM]</title>
      </head>
      <body>
        <p>Edit <i>dcmcdw-x.y.z/server/dcmcdw/conf/xsl/index.xsl</i> to generate this page (INDEX.HTM)
			  compliant to the IHE Portable Data for Imaging Integration Profile, specified in 
				<a href="http://www.rsna.org/IHE/tf/IHE_TF_Suppl_TI_2004-06-04.pdf">
			  	IHE Radiology Technical Framework 2004-2005 Supplements for Trial Implementation
				</a>, Page 99f:</p>
        <p><b>INDEX.HTM</b> file located in the root directory, which shall portray the exact content of
				the interchange media. The file shall present:
			</p>
        <ul>
          <li>
            <p>An informative header containing:</p>
            <ul>
              <li>
                <p>Identification of the institution that created the interchange media</p>
              </li>
              <li>
                <p>Optionally, a disclaimer statement about privacy/security from the institution
							that created the interchange media.</p>
              </li>
              <li>
                <p>a <a href="IHE_PDI/INDEX.HTM">link</a> to an entry point for accessing the
							web content of the IHE_PDI directory.</p>
              </li>
              <li>
                <p>a <a href="README.TXT">link</a> to the README.TXT file.</p>
              </li>
              <li>
                <p>a link to additional non-constrained data (if it exists)</p>
              </li>
              <li>
                <p>a manifest that lists the data that can be imported by a Portable Media Importer Actor.
						  If there is web-viewable content that is not importable by a Portable Media Importer,
						  a note describing this situation shall be included as part of the manifest.</p>
              </li>
              <li>
                <p>a link to a launch point for a DICOM viewer, if present on the interchange media.</p>
              </li>
            </ul>
          </li>
        </ul>
        <xsl:apply-templates select="dicomdir"/>
      </body>
    </html>
  </xsl:template>
  <xsl:template match="dicomdir">
    <h3>Manifest of File-Set <xsl:value-of select="attr[@tag='(0004,1130)']"/></h3>
    <table cellpadding="0" cellspacing="0" border="0" bgcolor="#C0C0C0" width="100%">
      <tr>
        <td>
          <table cellpadding="3" cellspacing="1" border="0" width="100%">
            <colgroup>
              <col width="1%"/>
              <col width="1%"/>
              <col width="10%"/>
              <col width="73%"/>
              <col width="10%"/>
              <col width="5%"/>
            </colgroup>
            <thead align="left">
              <tr>
                <th nowrap="nowrap" bgcolor="#FFE4EE" colspan="4">Patient Name</th>
                <th nowrap="nowrap" bgcolor="#FFE4EE">Birth Date</th>
                <th nowrap="nowrap" bgcolor="#FFE4EE">Sex</th>
              </tr>
              <tr>
                <th nowrap="nowrap" bgcolor="#FFE4EE" rowspan="2">&nbsp;</th>
                <th nowrap="nowrap" bgcolor="#E4EEFF" colspan="2">Study Date</th>
                <th nowrap="nowrap" bgcolor="#E4EEFF" colspan="2">Study Description</th>
                <th nowrap="nowrap" bgcolor="#E4EEFF">Files</th>
              </tr>
              <tr>
                <th nowrap="nowrap" bgcolor="#E4EEFF">&nbsp;</th>
                <th nowrap="nowrap" bgcolor="#EEFFE4">Modality</th>
                <th nowrap="nowrap" bgcolor="#EEFFE4" colspan="2">Directory Path</th>
                <th nowrap="nowrap" bgcolor="#EEFFE4">Files</th>
              </tr>
            </thead>
            <tbody>
              <xsl:apply-templates select="record[@type='PATIENT']">
                <xsl:sort data-type="text" order="ascending" select="attr[@tag='(0010,0010)']"/>
              </xsl:apply-templates>
            </tbody>
          </table>
        </td>
      </tr>
    </table>
  </xsl:template>
  <xsl:template match="record[@type='PATIENT']">
    <tr>
      <td bgcolor="#FFE4EE" colspan="4">
        <xsl:call-template name="formatPN">
          <xsl:with-param name="pn" select="attr[@tag='(0010,0010)']"/>
        </xsl:call-template>
      </td>
      <td bgcolor="#FFE4EE">
        <xsl:call-template name="formatDate">
          <xsl:with-param name="date" select="attr[@tag='(0010,0030)']"/>
        </xsl:call-template>
      </td>
      <td bgcolor="#FFE4EE">
        <xsl:value-of select="attr[@tag='(0010,0040)']"/>
      </td>
    </tr>
    <xsl:apply-templates select="record[@type='STUDY']">
      <xsl:sort data-type="text" order="ascending" select="attr[@tag='(0008,0020)']"/>
    </xsl:apply-templates>
  </xsl:template>
  <xsl:template match="record[@type='STUDY']">
    <tr>
      <xsl:if test="position()=1">
        <td bgcolor="#FFE4EE" rowspan="{last()+count(../record/record)}">&nbsp;</td>
      </xsl:if>
      <td bgcolor="#E4EEFF" colspan="2">
        <xsl:call-template name="formatDate">
          <xsl:with-param name="date" select="attr[@tag='(0008,0020)']"/>
        </xsl:call-template>
      </td>
      <td bgcolor="#E4EEFF" colspan="2">
        <xsl:value-of select="attr[@tag='(0008,1030)']"/>
      </td>
      <td bgcolor="#E4EEFF">
        <xsl:value-of select="count(record/record)"/>
      </td>
    </tr>
    <xsl:apply-templates select="record[@type='SERIES']">
      <xsl:sort data-type="text" order="ascending" select="attr[@tag='(0008,0060)']"/>
    </xsl:apply-templates>
  </xsl:template>
  <xsl:template match="record[@type='SERIES']">
    <tr>
      <xsl:if test="position()=1">
        <td bgcolor="#E4EEFF" rowspan="{last()}">&nbsp;</td>
      </xsl:if>
      <td bgcolor="#EEFFE4">
        <xsl:value-of select="attr[@tag='(0008,0060)']"/>
      </td>
      <td bgcolor="#EEFFE4" colspan="2">
        <xsl:variable name="dir">
          <xsl:call-template name="parentDir">
            <xsl:with-param name="file" select="record/attr[@tag='(0004,1500)']"/>
          </xsl:call-template>
        </xsl:variable>
        <a href="{$dir}">
          <xsl:value-of select="$dir"/>
        </a>
      </td>
      <td bgcolor="#EEFFE4">
        <xsl:value-of select="count(record)"/>
      </td>
    </tr>
  </xsl:template>
  <xsl:template name="parentDir">
    <xsl:param name="file"/>
    <xsl:value-of select="substring($file,0,string-length($file)-8)"/>
  </xsl:template>
  <xsl:template name="formatDate">
    <xsl:param name="date"/>
    <xsl:value-of select="substring($date,1,4)"/>
    <xsl:text>-</xsl:text>
    <xsl:value-of select="substring($date,5,2)"/>
    <xsl:text>-</xsl:text>
    <xsl:value-of select="substring($date,7)"/>
  </xsl:template>
  <xsl:template name="formatPN">
    <xsl:param name="pn"/>
    <xsl:value-of select="translate($pn,'^',',')"/>
  </xsl:template>
</xsl:stylesheet>
