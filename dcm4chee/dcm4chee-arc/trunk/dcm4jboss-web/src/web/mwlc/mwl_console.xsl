<xsl:stylesheet version="1.0" 
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:output method="html">
</xsl:output>
<xsl:variable name="page_title">Modality Worklist Console</xsl:variable>
<xsl:include href="../page.xsl"/>
<xsl:template match="model">
<!-- Filter -->
	<form action="mwl_console.m" method="get" name="myForm">
		<table border="0" cellspacing="0" cellpadding="0" width="100%" bgcolor="eeeeee">
			<td valign="top">
				<table border="0" height="30" cellspacing="0" cellpadding="0" width="100%">
					<td bgcolor="eeeeee" align="center">
						<xsl:if test="total &gt; 0">
							Displaying procedure step 
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
							No matching procedure steps found!
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
							<a href="mwl_console.m?nav=prev">
								<img src="images/prev.gif" alt="prev" border="0" title="Previous Search Results"/>		
							</a>
						</xsl:if>
					</td>
					<td width="40" bgcolor="eeeeee">
						<xsl:if test="offset + limit &lt; total">
							<a href="mwl_console.m?nav=next">
								<img src="images/next.gif" alt="next" border="0" title="Next Search Results"/>		
							</a>
						</xsl:if>
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
						<td bgcolor="eeeeee" colspan="2">Date: </td>
						<td bgcolor="eeeeee">
							<input size="15" name="startDate" type="text" value="{filter/startDate}"/>
						</td>
						<td bgcolor="eeeeee">to: </td>
						<td bgcolor="eeeeee">
							<input size="15" name="endDate" type="text" value="{filter/endDate}"/>
						</td>
						<td bgcolor="eeeeee">&#160;&#160;</td>
						<td bgcolor="eeeeee" >Modality: </td>
						<td bgcolor="eeeeee">
							<input size="10" name="modality" type="text" value="{filter/modality}"/>
						</td>
						<td bgcolor="eeeeee">&#160;&#160;</td>
						<td bgcolor="eeeeee" nowrap="nowrap" >Station name: </td>
						<td bgcolor="eeeeee">
							<input size="10" name="stationName" type="text" value="{filter/stationName}"/>
						</td>
						<td bgcolor="eeeeee">&#160;&#160;</td>
						<td bgcolor="eeeeee" nowrap="nowrap" >Station AET: </td>
						<td bgcolor="eeeeee">
							<input size="10" name="stationAET" type="text" value="{filter/stationAET}"/>
						</td>
						<td bgcolor="eeeeee">&#160;&#160;</td>
						<td bgcolor="eeeeee" nowrap="nowrap" >Accession No.: </td>
						<td bgcolor="eeeeee">
							<input size="10" name="accessionNumber" type="text" value="{filter/accessionNumber}"/>
						</td>
						<td width="100%" bgcolor="eeeeee">&#160;</td>
						
					</tr>
				</table>
			</td>
		</table>
<!-- List of working list entries ( scheduled procedur steps ) -->
		<table width="70%" border="0" bordercolor="#ffffff" cellspacing="7" cellpadding="0">
		<tr>	<center>
			<td>
				<tr>
					<td width="100" ><h2>Patient</h2></td>
					<td width="20" ><h2>Pat.ID</h2></td>
					<td width="15" ><h2>ID</h2></td>
					<td width="150" ><h2>Start Date</h2></td>
					<td width="150" ><h2>Station Name</h2></td>
					<td width="30" ><h2 nowrap="nowrap">Station AET</h2></td>
					<td width="30" ><h2>Modality</h2></td>	
					<td width="100" ><h2>Physician</h2></td>	
					<td width="200" ><h2 nowrap="nowrap">Req. Procedure ID</h2></td>	
					<td width="200" ><h2 nowrap="nowrap">Accession No.</h2></td>	
					<xsl:if test="local = 'true'">
						<td width="200" ><h2 nowrap="nowrap">Function</h2></td>	
					</xsl:if>
				</tr>
					<xsl:apply-templates select="mwlEntries/item">
						<xsl:sort data-type="number" order="ascending" select="spsID"/>
					</xsl:apply-templates>
			</td>	</center>
		</tr>
		</table>
</form>

</xsl:template>

	<xsl:template match="item[@type='org.dcm4chex.archive.web.maverick.mwl.model.MWLEntry']">
		<tr>
	        <td title="Patient Name" >
				<xsl:value-of select="patientName"/>
			</td>
	        <td title="Patient ID" >
				<xsl:value-of select="patientID"/>
			</td>
	        <td title="SPS ID" >
				<xsl:value-of select="spsID"/>
	        </td>
	        <td title="Start Date" >
				<xsl:value-of select="spsStartDateTime"/>
	        </td>
	        <td title="Station Name" >
				<xsl:value-of select="stationName"/>
	        </td>
	        <td title="Station AET" >
				<xsl:value-of select="stationAET"/>
	        </td>
	        <td title="Modality" >
				<xsl:value-of select="modality"/>
		 	</td>
	        <td title="Physician" >
				<xsl:value-of select="physiciansName"/>
		 	</td>
	        <td title="Req. Procedure ID" >
				<xsl:value-of select="reqProcedureID"/>
		 	</td>
	        <td title="Accession No." >
				<xsl:value-of select="accessionNumber"/>
		 	</td>
			<xsl:if test="/model/local = 'true'">
				<td title="Function" >
					<a href="mwl_console.m?action=delete&amp;spsid={spsID}">
						<xsl:attribute name="onclick">return confirm('Delete worklist entry <xsl:value-of select="spsID"/> ?')</xsl:attribute>
						<img src="images/loeschen.gif" alt="delete" border="0" title="Delete this worklist entry!"/>		
					</a>
				</td>	
			</xsl:if>
		</tr>
	</xsl:template>
	   
</xsl:stylesheet>

