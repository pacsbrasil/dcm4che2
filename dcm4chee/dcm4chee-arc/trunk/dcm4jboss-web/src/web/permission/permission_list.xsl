<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" 
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:output method="html" indent="yes" encoding="UTF-8" />
<xsl:variable name="page_title">Study Permissions Overview</xsl:variable>
<xsl:include href  = "../page.xsl" />

<xsl:template match="model">
	<xsl:variable name="actionCount" select="count(permissionConfig/actions/item)"/>
	<p>
	<div align="center" style="font-size : 22px;" >
		<xsl:text>Permission list for </xsl:text>
		<xsl:if test="string-length(studyIUID) &gt; 0">
			<xsl:text>Study IUID:</xsl:text><xsl:value-of select="studyIUID" />
			<xsl:text>(patient: </xsl:text><xsl:value-of select="patName" />):
		</xsl:if>
		<xsl:if test="string-length(patPk) &gt; 0">
			<xsl:text>all studies of patient </xsl:text><xsl:value-of select="patName" /><xsl:text>:</xsl:text>
		</xsl:if>
	</div>
	</p>
	<p>
	<div align="center" style="background : green">
		<table width="80%" border="4" align="center">
			<xsl:call-template name="add_colgroup" />
				<tr>
					<td>&#160;</td>
					<xsl:apply-templates select="permissionConfig/actions/item" mode="action_header"/>
				</tr>
			<xsl:apply-templates select="permissionConfig/roles/item" mode="role_line"/>
   		</table>
	</div>
	</p>
	<div align="center" style="background-color: #eeeeee;" >
		<xsl:text>Custom roles/actions:</xsl:text><br/>
		<table border="4" cellspacing="0" cellpadding="0" width="50%">
			<colgroup>
				<col width="45%"/>
				<col width="45%"/>
				<col width="10%"/>
			</colgroup>
			<tr>
				<th>Role</th>
			    <th>Action</th>
			    <th>&#160;</th>
			</tr>
			<xsl:apply-templates select="/model/rolesWithActions/item" mode="custom_roles">
				<xsl:sort select="/model/rolesWithActions/item" />
			</xsl:apply-templates>
		</table>
		<form action="studyPermission.m?studyIUID={studyIUID}&amp;patPk={patPk}" method="post" accept-charset="UTF-8" >
			<input size="10" name="role" type="text" value="" />
			&#160;
			<input size="10" name="action" type="text" value="" />
			&#160;
			<input type="submit" name="add" value="Add"/>
			&#160;
			<input type="submit" name="remove" value="Del"/>
			&#160;&#160;&#160;
			<input type="submit" name="cancel" value="Cancel" />
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
		<th title="{@key}"><xsl:value-of select="@key" /></th>
		<xsl:apply-templates select="/model/permissionConfig/actions/item" mode="action_line">
			<xsl:with-param name="role" select="@key"/>
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
					href="studyPermission.m?studyIUID={/model/studyIUID}&amp;remove=true&amp;role={$role}&amp;action={$action}&amp;patPk={/model/patPk}&amp;patName={/model/patName}">
	   				<img src="images/granted.gif" alt="granted" border="0" />
	   			</a>
	   		</xsl:when>
			<xsl:when test="$countPermissions > 0">
				<a title="remove permission for {$role}:{.}" 
					href="studyPermission.m?studyIUID={/model/studyIUID}&amp;add=true&amp;role={$role}&amp;action={$action}&amp;patPk={/model/patPk}&amp;patName={/model/patName}">
	   				<img src="images/granted_part.gif" alt="some studies granted" border="0" />
	   			</a>
	   			<xsl:value-of select="$countPermissions" />/<xsl:value-of select="/model/countStudies"/>
	   		</xsl:when>
			<xsl:otherwise>
				<a title="add permission for {$role}:{.}" 
					href="studyPermission.m?studyIUID={/model/studyIUID}&amp;add=true&amp;role={$role}&amp;action={$action}&amp;patPk={/model/patPk}&amp;patName={/model/patName}">
		   			<img src="images/denied.gif" alt="denied" border="0" />
	   			</a>
	   		</xsl:otherwise>
	   	</xsl:choose>&#160;
	</td>
</xsl:template>

<xsl:template match="item" mode="custom_roles">
   	<xsl:variable name="role" select="@key" />
	<xsl:apply-templates select="item" mode="custom_action_line">
		<xsl:with-param name="role" select="@key"/>
	</xsl:apply-templates>	
</xsl:template>

<xsl:template match="item" mode="custom_action_line">
   	<xsl:param name="role" />
   	<xsl:variable name="action" select="@key" />
   	<xsl:variable name="countPermissions" select="count(/model/rolesWithActions/item[@key=$role]/item[@key=$action]/item)" />
	<xsl:if test="not(/model/permissionConfig/roles/item[@key=$role]) or not(/model/permissionConfig/actions/item[@key=$action])" >
		<tr>
	   		<td><xsl:value-of select="$role" /></td>
	   		<td><xsl:value-of select="$action" /></td>
	   		<td titel="Remove this grant">
				<a title="remove this permission" 
					href="studyPermission.m?studyIUID={/model/studyIUID}&amp;remove=true&amp;role={$role}&amp;action={$action}&amp;patPk={/model/patPk}&amp;patName={/model/patName}">
	   				<img src="images/delete.gif" alt="del" border="0" />
	   			</a>
	   			<xsl:if test="$countPermissions &lt; /model/countStudies">
					<a title="add this permission to all studies of this patient" 
						href="studyPermission.m?studyIUID={/model/studyIUID}&amp;add=true&amp;role={$role}&amp;action={$action}&amp;patPk={/model/patPk}&amp;patName={/model/patName}">
		   				<img src="images/expand_grant.gif" alt="add" border="0" />
		   			</a>
	   			</xsl:if>
		   	</td>
		</tr>
	</xsl:if>
</xsl:template>

<xsl:template name="add_colgroup">
	<colgroup>
		<xsl:for-each select="/model/columnWidths/item" >
			<col width="{.}"/>
		</xsl:for-each>
	</colgroup>
</xsl:template>

</xsl:stylesheet>

