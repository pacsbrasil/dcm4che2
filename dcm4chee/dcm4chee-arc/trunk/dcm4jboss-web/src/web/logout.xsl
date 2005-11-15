<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" 
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:internal="urn:my-internal-data">
<xsl:output method="html" indent="yes" encoding="ISO-8859-1"/>

   <xsl:template match="/">
	 <html>
	    <head>
		<style type="text/css">
body
{
  background-color: #666666;
}
a:link {
	text-decoration: none;
	color: #99bbaa;
	font-size: 14px;
   line-height: 21px;
   font-family: Verdana,Arial,Helvetica;
}

a:visited {
	text-decoration: none;
	color: #99bbaa;
	font-size: 14px;
   line-height: 21px;
   font-family: Verdana,Arial,Helvetica;
}

a:hover {
	text-decoration: none;
	color: #99bbaa;
	font-size: 14px;
   line-height: 21px;
   font-family: Verdana,Arial,Helvetica;
}

a:active {
	text-decoration: none;
	color: #99bbaa;
	font-size: 14px;
   line-height: 21px;
   font-family: Verdana,Arial,Helvetica;
}


.text
{
  font-size: 14px;
  color: #dddddd;
  line-height: 21px;
  font-family: Verdana,Arial,Helvetica;
}

.head
{
  font-size: 18px;
  color: #dddddd;
  line-height: 21px;
  font-family: Verdana,Arial,Helvetica;
}
		</style>
	    </head>
	    <body>
	    	<center>
	    		<b><div class="text">You have logged out from DCM Folder:</div></b> 
	    		<p>
	    		<a href="foldersubmit.m?filter=">Login</a>
	    		</p>
	    	</center>
		</body>
	 </html>
   </xsl:template>

   
</xsl:stylesheet>

