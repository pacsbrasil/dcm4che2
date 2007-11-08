<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" 
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:output method="html" indent="yes" encoding="UTF-8" />
<xsl:variable name="page_title">Study Permissions Overview</xsl:variable>
<xsl:include href  = "../page.xsl" />

<xsl:template match="model">
	<p>
	<div align="center" style="font-size : 22px;" >
		<xsl:text>Permission list for </xsl:text>
		<xsl:choose>
			<xsl:when test="patient/patientID">
				<xsl:text>all studies of patient </xsl:text><xsl:value-of select="patient/patientName" /><xsl:text>:</xsl:text>
			</xsl:when>
			<xsl:otherwise>
				<xsl:text>Study IUID:</xsl:text><xsl:value-of select="studyIUID" />
				<xsl:text>(patient: </xsl:text><xsl:value-of select="patient/patientName" />):
			</xsl:otherwise>
		</xsl:choose>
	</div>
	</p>
	<p>
	<div align="center" style="background : green">
		<table width="80%" border="4" align="center">
			<xsl:call-template name="add_colgroup" />
				<tr>
					<td>&#160;</td>
					<xsl:apply-templates select="rolesConfig/actions/item" mode="action_header"/>
				</tr>
			<xsl:apply-templates select="rolesConfig/roles/item" mode="role_line">
				<xsl:sort data-type="text" order="ascending" select="displayName"/>
			</xsl:apply-templates>
   		</table>
	</div>
	</p>
	<div align="center" style="background-color: #eeeeee;" >
		<form action="studyPermissionUpdate.m" name="permForm" method="post" accept-charset="UTF-8" >
			<br/>&#160;<br/>
			role:&#160;<input size="10" name="role" type="text" value="" />
			&#160;
			action:&#160;<input size="10" name="action" type="text" value="" />
			&#160;
			<input type="submit" name="cmd" value="Add"/>
			<br/>&#160;<br/>
			<input type="submit" name="cmd" value="Cancel"/>
			<br/>&#160;<br/>
		</form>
	</div>
</xsl:template>

<xsl:template match="item" mode="action_header">
	<th title="{.}" >
   		<xsl:value-of select="@key"/> (<xsl:value-of select="."/>)&#160;
	</th>
</xsl:template>

<xsl:template match="item" mode="role_line">
	<tr>
		<th title="Role:{name}({descr})"><xsl:value-of select="displayName" /></th>
		<xsl:apply-templates select="/model/rolesConfig/actions/item" mode="action_line">
			<xsl:with-param name="role" select="name"/>
		</xsl:apply-templates>	
	</tr>
</xsl:template>

<xsl:template match="item" mode="action_line">
   	<xsl:param name="role" />
   	<xsl:variable name="action" select="@key" />
   	<xsl:variable name="countPermissions" select="count(/model/rolesWithActions/item[@key=$role]/item[@key=$action]/item)" />
	<td title="{$role}:{.}" align="center">
		<xsl:choose>
			<xsl:when test="$countPermissions = /model/countStudies">
				<a title="remove permission for {$role}:{.}" 
					href="studyPermissionUpdate.m?cmd=remove&amp;role={$role}&amp;action={$action}" >
	   				<img src="images/granted.gif" alt="granted" border="0" />
	   			</a>
	   		</xsl:when>
			<xsl:when test="$countPermissions > 0">
				<a title="remove permission for {$role}:{.}" 
					href="studyPermissionUpdate.m?cmd=remove&amp;role={$role}&amp;action={$action}">
	   				<img src="images/granted_part.gif" alt="some studies granted" border="0" />
	   			</a>
	   			<xsl:value-of select="$countPermissions" />/<xsl:value-of select="/model/countStudies"/>
	   		</xsl:when>
			<xsl:otherwise>
				<a title="add permission for {$role}:{.}" 
					href="studyPermissionUpdate.m?cmd=add&amp;role={$role}&amp;action={$action}" >
		   			<img src="images/denied.gif" alt="denied" border="0" />
	   			</a>
	   		</xsl:otherwise>
	   	</xsl:choose>&#160;
	</td>
</xsl:template>

<xsl:template name="add_colgroup">
	<xsl:variable name="firstColumn" select="10"/>
	<xsl:variable name="actionCount" select="count(/model/rolesConfig/actions/item)"/>
	<xsl:variable name="colWidth" select="(100-$firstColumn) div $actionCount"/>
	<colgroup>
		<col width="{$firstColumn}%"/>
		<xsl:for-each select="/model/rolesConfig/actions/item" >
			<col width="{$colWidth}"/>
		</xsl:for-each>
	</colgroup>
</xsl:template>

</xsl:stylesheet>

