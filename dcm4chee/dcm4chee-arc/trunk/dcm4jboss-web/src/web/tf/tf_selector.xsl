<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" 
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:output method="html" indent="yes" encoding="ISO-8859-1" />
<xsl:variable name="page_title">Teaching File Selector</xsl:variable>
<xsl:include href  = "../page.xsl" />

<xsl:template match="model">
	<form action="tfSelector.m" method="post">
		<table border="0" cellspacing="0" cellpadding="0" width="90%">
			<tr>
				<td colspan="2" align="center">
					Export <xsl:value-of select="numberOfInstances" /> selected Instances:
		   		</td>
            </tr>
			<tr>
		   		<td>&#160;</td>
        	</tr>
			<tr>
		   		<td align="right">Document Title:&#160;</td>
		        <td title="Document Title">
					<select name="selectedTitle" >
						<xsl:for-each select="docTitles/item">
							<option>
								<xsl:attribute name="value"><xsl:value-of select="position()-1"/></xsl:attribute>
								<xsl:value-of select="."/>
							</option>
						</xsl:for-each>
					</select>
		        </td>
        	</tr>
			<tr>
		   		<td align="right">Delay Reason:&#160;</td>
		        <td title="Delay Reason">
					<select name="selectedDelayReason" >
						<option value="-1" selected="">-</option>
						<xsl:for-each select="delayReasons/item">
							<option>
								<xsl:attribute name="value"><xsl:value-of select="position()-1"/></xsl:attribute>
								<xsl:value-of select="."/>
							</option>
						</xsl:for-each>
					</select>
		        </td>
        	</tr>
			<tr>
		   		<td align="right">Disposition:&#160;</td>
		        <td title="Disposition">
					<select name="disposition" >
						<option value="" selected="">-</option>
						<xsl:for-each select="dispositions/item">
							<option>
								<xsl:attribute name="value"><xsl:value-of select="."/></xsl:attribute>
								<xsl:value-of select="."/>
							</option>
						</xsl:for-each>
					</select>
		        </td>
        	</tr>
			<tr>
		   		<td>&#160;</td>
        	</tr>
			<tr>
				<td colspan="2" align="center">
        			<b>Teaching File Structured Report Manifest</b>
					&#160;&#160;Create<input type="radio" name="useManifest" value="yes" checked="true" ></input>
					&#160;Ignore<input type="radio" name="useManifest" value="no" checked="true" ></input>
		   		</td>
            </tr>
			<tr>
		   		<td>&#160;</td>
        	</tr>
			<tr>
		   		<td align="right">Author:&#160;</td>
		        <td title="Author">
					<input size="60" name="author" type="text" />
		        </td>
        	</tr>
			<tr>
		   		<td align="right">Abstract:&#160;</td>
		        <td title="Abstract">
					<input size="60" name="abstract" type="text" />
		        </td>
        	</tr>
			<tr>
		   		<td align="right">Keywords:&#160;</td>
		        <td title="Keywords (separated with '|')">
					<input size="60" name="keywords" type="text" />
		        </td>
        	</tr>
			<tr>
		   		<td align="right">History:&#160;</td>
		        <td title="History (separated with '|')">
					<input size="60" name="history" type="text" />
		        </td>
        	</tr>
			<tr>
		   		<td align="right">Findings:&#160;</td>
		        <td title="Findings (separated with '|')">
					<input size="60" name="findings" type="text" />
		        </td>
        	</tr>
			<tr>
		   		<td align="right">Discussions:&#160;</td>
		        <td title="Discussions (separated with '|')">
					<input size="60" name="discussions" type="text" />
		        </td>
        	</tr>
			<tr>
		   		<td align="right">Impressions:&#160;</td>
		        <td title="Differential Diagnosis/Impressions (separated with '|')">
					<input size="60" name="impressions" type="text" />
		        </td>
        	</tr>
			<tr>
		   		<td align="right">Diagnosis:&#160;</td>
		        <td title="Diagnosis (separated with '|')">
					<input size="60" name="diagnosis" type="text" />
		        </td>
        	</tr>
			<tr>
		   		<td align="right">Radiographic anatomy:&#160;</td>
		        <td title="Radiographic anatomy (separated with '|')">
					<input size="60" name="anatomy" type="text" />
		        </td>
        	</tr>
			<tr>
		   		<td align="right">Pathology:&#160;</td>
		        <td title="Pathology (separated with '|')">
					<input size="60" name="pathology" type="text" />
		        </td>
        	</tr>
			<tr>
		   		<td>&#160;</td>
        	</tr>
			<tr>
		   		<td align="right">Category:&#160;</td>
		        <td title="Category of teaching file">
					<select name="category" >
						<xsl:for-each select="manifestModel/categories/item">
							<option>
								<xsl:attribute name="value"><xsl:value-of select="position()-1"/></xsl:attribute>
								<xsl:value-of select="."/>
							</option>
						</xsl:for-each>
					</select>
		        </td>
        	</tr>
			<tr>
		   		<td align="right">Level:&#160;</td>
		        <td title="Level of teaching file">
					<select name="level" >
						<option value="-1" selected="true">-</option>
						<xsl:for-each select="manifestModel/levels/item">
							<option>
								<xsl:attribute name="value"><xsl:value-of select="position()-1"/></xsl:attribute>
								<xsl:value-of select="."/>
							</option>
						</xsl:for-each>
					</select>
		        </td>
        	</tr>
			<tr>
		   		<td>&#160;</td>
        	</tr>
			<tr>
		   		<td align="right">Diagnosis confirmed:&#160;</td>
		        <td title="Diagnosis confirmed">
					&#160;&#160;Yes<input type="radio" name="confirmed" value="yes" ></input>
					&#160;No<input type="radio" name="confirmed" value="no" checked="true" ></input>
		        </td>
        	</tr>
			<tr>
		   		<td>&#160;</td>
        	</tr>
			<tr>
				<td colspan="2" align="center">
						<input type="submit" name="export" value="Export"/>
						<input type="submit" name="cancel" value="Cancel" />
    			</td>
			</tr>
	    </table>
	</form>
</xsl:template>
<!--
   <xsl:template name="textField" >
   		<xsl:param name="key" />
   		<xsl:param name="value" />
   		
		<tr>
	   		<td align="right"><xsl:value-of select="$key"/>:&#160;</td>
	        <td title="$key">
				<input size="60" name="$key" type="text" />
	        </td>
    	</tr>
   </xsl:template>
   -->

</xsl:stylesheet>

