<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE xsl:stylesheet [
<!ENTITY nbsp "&#160;">
]>
<xsl:stylesheet version="1.0" 
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:internal="urn:my-internal-data">

   <xsl:template match="/">
      <html>
         <head>
            <title>Edit AES</title>
            <link rel="stylesheet" href="stylesheet.css" type="text/css" />
			<script language="JavaScript">window.name = "patient_edit";</script>
         </head>
         <body>
           			<form action="foldersubmit.m" method="post">
					<table border="1" cellspacing="0" cellpadding="0" width="100%"><tr><td>
					<table border="0">
			            <tr> <td class="title">AE Edit</td> </tr>
			            <tr> <td>&nbsp;</td> </tr>
						<xsl:apply-templates select="model/AEs/item"/>
					</table>
					</td></tr></table>
					</form>
         </body>
      </html>
   </xsl:template>

	<xsl:template match="item[@type='org.dcm4chex.archive.ejb.jdbc.AEData']">
		<tr bgcolor="#eeeeee">
	        <td title="AE Title" >
				<xsl:value-of select="title"/>
			</td>
	        <td title="Hostname" >
				<xsl:value-of select="hostName"/>
	        </td>
	        <td title="Port">
					<xsl:value-of select="port"/>
	        </td>
	        <td title="Cipher Suites">
					<xsl:value-of select="cipherSuites"/>
	        </td>
			<td>
					<input type="checkbox" name="AE" value="{title}">
						<xsl:if test="true">
								<xsl:attribute name="checked"/>
						</xsl:if>
					</input>
			</td>
		</tr>
	</xsl:template>

   
</xsl:stylesheet>

