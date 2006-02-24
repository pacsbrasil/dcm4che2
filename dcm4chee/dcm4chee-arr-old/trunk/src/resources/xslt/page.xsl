<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:template match="/">
			<html>
			<head>
				<title><xsl:copy-of select="$page_title" /></title>
				<script language = "JavaScript" src= "dcm4che.js"/>
				<link rel="stylesheet" href="stylesheet.css" type="text/css"/>
			</head>
			<body bgcolor="#FFFFFF" leftmargin="0" topmargin="0" marginwidth="0" marginheight="0" link="#FF0000" alink="#FF0000" vlink="#FF0000">
			<table width="100%" border="0" cellpadding="0" cellspacing="0">
  		<tr valign="middle" bgcolor="#eeeeee" style="text-align: center">
    	<td width="50" align="left"><img src="images/logo.gif" alt="DCM4JBOSS" border="0"/></td>
    	<td width="100" align="center"><a href="../dcm4jboss-web/default.jsp">Folder</a></td>
      <td width="120" align="center"><a href="../dcm4jboss-web/ae.m">AE Management</a></td>
      <td width="120" align="center"><a href="index.jsp">Audit Repository</a></td>
			<td width="40%"></td>
			  <xsl:apply-templates select="query"/>
	      </tr>
      </table>
			</body>
		</html>
	</xsl:template>
</xsl:stylesheet>
	
