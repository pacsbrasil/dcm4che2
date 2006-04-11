<xsl:stylesheet version="1.0" 
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:output method="html" indent="yes" encoding="ISO-8859-1"/>
<xsl:variable name="page_title">General Purpose Performed Procedure Step - GPPPS Console</xsl:variable>
<xsl:include href="../page.xsl"/>
<xsl:template match="model">
<!-- Filter -->
	<form action="gppps_console.m" method="get" name="myForm">
		<table border="0" cellspacing="0" cellpadding="0" width="100%" bgcolor="eeeeee">
			<td valign="top">
				<table border="0" height="30" cellspacing="0" cellpadding="0" width="100%">
					<td bgcolor="eeeeee" align="center">
						<xsl:if test="total &gt; 0">
							<b>GPPPS Worklist:</b> Displaying procedure step 
							<b>
								<xsl:value-of select="offset + 1"/>
							</b>
								to
							<b>
								<xsl:choose>
									<xsl:when test="offset + limit &lt; total">
										<xsl:value-of select="offset + limit"/>
									</xsl:when>
									<xsl:otherwise>
										<xsl:value-of select="total"/>												
									</xsl:otherwise>
								</xsl:choose>
							</b>
								of
							<b>
								<xsl:value-of select="total"/>
							</b>matching procedure steps.
						</xsl:if>
						<xsl:if test="total = 0">
							<b>GPPPS Worklist:</b> No matching procedure steps found!
						</xsl:if>
 					</td>

					<td width="150" bgcolor="eeeeee">
					</td>
					<td width="40" bgcolor="eeeeee">
						<input type="image" value="Search" name="filter" src="images/search.gif" border="0"
						 	title="New Search"/>
					</td>
					<td width="40" bgcolor="eeeeee">
						<xsl:if test="offset &gt; 0">
							<input type="image" value="Search" name="prev" src="images/prev.gif" border="0"
						 		title="Previous Search Results"/>
						</xsl:if>
					</td>
					<td width="40" bgcolor="eeeeee">
						<xsl:if test="offset + limit &lt; total">
							<input type="image" value="Search" name="next" src="images/next.gif" border="0"
						 		title="Next Search Results"/>
						</xsl:if>
					</td>
					<td bgcolor="eeeeee">&#160;</td>
					<td width="40" bgcolor="eeeeee">	
						<input type="image" name="del" value="del" src="images/loeschen.gif" 
								border="0" title="Delete selected GPPPS entries"
								onclick="return confirm('Delete selected GPPPS entries?')">
							<xsl:if test="total &lt;= 0">
								<xsl:attribute name="disabled">disabled</xsl:attribute>
							</xsl:if>
						</input>
					</td>
				</table>
				<table border="0" cellpadding="0" cellspacing="0" bgcolor="eeeeee">
					<tr>
						<td bgcolor="eeeeee">&#160;&#160;</td>
						<td bgcolor="eeeeee" class="label">Patient:</td>
						<td bgcolor="eeeeee">
							<input size="10" name="patientName" type="text" value="{filter/patientName}"/>
						</td>
						<td bgcolor="eeeeee">&#160;&#160;</td>
						<td bgcolor="eeeeee" colspan="2" title="Query Start date. format:yyyy/mm/dd hh:mm">Date: </td>
						<td bgcolor="eeeeee">
							<input size="14" name="startDate" 
									title="Query Start date. format:yyyy/mm/dd hh:mm" 
									type="text" value="{filter/startDate}"/>
						</td>
						<td bgcolor="eeeeee" title="Query End date. format:yyyy/mm/dd hh:mm">to: </td>
						<td bgcolor="eeeeee">
							<input size="14" name="endDate" title="Query End date. format:yyyy/mm/dd hh:mm" 
									type="text" value="{filter/endDate}"/>
						</td>
						<td bgcolor="eeeeee">&#160;&#160;</td>
						<td bgcolor="eeeeee" nowrap="nowrap" >Status: </td>
						<td bgcolor="eeeeee">
							<select name="status" >
								<option value="">
									<xsl:if test="filter/status=''"><xsl:attribute name="selected" /></xsl:if>
									[ANY]
								</option>
								<option value="0">
									<xsl:if test="filter/status=0"><xsl:attribute name="selected" /></xsl:if>
									IN PROGRESS
								</option>
								<option value="1">
									<xsl:if test="filter/status=1"><xsl:attribute name="selected" /></xsl:if>
									COMPLETED
								</option>
								<option value="2">
									<xsl:if test="filter/status=2"><xsl:attribute name="selected" /></xsl:if>
									DISCONTINUED
								</option>
							</select>
					 						</td>
						<td width="100%" bgcolor="eeeeee">&#160;</td>
						
					</tr>
				</table>
			</td>
		</table>
		<xsl:call-template name="tableheader"/>
		<xsl:apply-templates select="gpppsEntries/item">
		</xsl:apply-templates>

	</form>
</xsl:template>

<xsl:template name="tableheader">
		
<!-- Header of working list entries -->
<table width="100%" border="0" cellspacing="0" cellpadding="0">

	<table border="0" cellpadding="0" cellspacing="0" width="100%">
		<colgroup>
			<col width="17%"/>
			<col width="11%"/>
			<col width="30%"/>
			<col width="25%"/>
			<col width="12%"/>
			<col width="5%"/>
		</colgroup>
		<tr >
			<td bgcolor="eeeeee" style="height:7px" colspan="7"></td> <!-- spacer -->
		</tr>
		<tr bgcolor="eeeeee">
			<th title="PatientName: " align="left">Patient</th>
			<th title="Performed Step Start Date" align="left">Start Date</th>
			<th title="Performed Procedure Description" align="left">Proc. Desc.</th>
			<th title="Performed Station Name: (&lt;Name&gt;-&lt;AET&gt;[&lt;Mod.&gt;]" align="left">Station</th>
			<th title="Procedure Step Status" align="left">Status</th>
			<th nowrap="nowrap">Function</th>
		</tr>
	</table>
	<table border="0" cellpadding="0" cellspacing="0" width="100%">
		<colgroup>
			<col width="5%"/>
			<col width="35%"/>
			<col width="35%"/>
			<col width="13%"/>
			<col width="12%"/>
		</colgroup>
		<tr bgcolor="eeeeee">
			<th >&#160;</th>
			<th title="Performed Procedure Step ID" align="left">Performed Procedure Step ID</th>
		</tr>
	</table>
	
</table>

</xsl:template>

<!-- List of GPPPS entries ( General Purpose performed procedure steps ) -->

<xsl:template match="item[@type='org.dcm4chex.archive.web.maverick.gppps.model.GPPPSEntry']">

<table width="100%" border="0" cellspacing="0" cellpadding="0">
	
	<table border="0" cellpadding="0" cellspacing="0" width="100%">
		<colgroup>
			<col width="17%"/>
			<col width="11%"/>
			<col width="30%"/>
			<col width="25%"/>
			<col width="12%"/>
			<col width="5%"/>
		</colgroup>
		<tr >
			<td bgcolor="999999" style="height:2px" colspan="9"></td> <!-- spacer -->
		</tr>
		<tr>
	        <td align="left" title="Patient" >
				<a href="foldersubmit.m?destination=LOCAL&amp;patientID={patientID}&amp;accessionNumber=&amp;patientName=&amp;studyID=&amp;studyDateRange=&amp;modality=&amp;filter.x=1&amp;trashFolder=false">
					<xsl:value-of select="patientName"/>
				</a>
			</td>
	        <td align="left" title="Start Date" >
				<xsl:value-of select="ppsStartDateTime"/>
	        </td>
	        <td align="left" title="PPS Desc.">
				<xsl:value-of select="PPSDescription"/>
		 	</td>
	        <td align="left" title="Station" >
				<xsl:if test="string-length(stationName) > 0">
					<xsl:value-of select="stationName"/> -
				</xsl:if>
				<xsl:value-of select="stationAET"/>[<xsl:value-of select="modality"/>]
	        </td>
	        <td align="left" title="PPSStatus" >
				<xsl:value-of select="PPSStatus"/>
		 	</td>
			<xsl:choose> 
				<xsl:when test="accNumbers=''">
					<td title="Function">
						<a href="mwl_console.m?action=link&amp;patientName={patientName}&amp;gpppsIUID={gpppsIUID}&amp;">
							<img src="images/link.gif" alt="link" border="0" title="Link this GPPPS entry with a MWL entry"/>		
						</a>
					</td>	
				</xsl:when>
				<xsl:otherwise>
					<td>&#160;&#160;</td>
				</xsl:otherwise>
			</xsl:choose>
			<td title="Function">
				<input type="checkbox" name="gpppsIUID" value="{gpppsIUID}">
					<xsl:if test="/model/gpppsIUIDs/item = gpppsIUID">
						<xsl:attribute name="checked"/>
					</xsl:if>
				</input>
			</td>	
		</tr>
	</table>

</table>

</xsl:template>
	   
</xsl:stylesheet>

