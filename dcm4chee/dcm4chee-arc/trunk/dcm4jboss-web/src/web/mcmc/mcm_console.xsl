<xsl:stylesheet version="1.0" 
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:internal="urn:my-internal-data">

<xsl:output method="html">
</xsl:output>
<xsl:variable name="page_title">Media Creation Managment Console</xsl:variable>
<xsl:include href="../page.xsl"/>
<xsl:template match="model">
<!-- Filter -->
	<form action="mcm_console.m" method="get" name="myForm">
		<table border="0" cellspacing="0" cellpadding="0" width="100%" bgcolor="eeeeee">
			<td valign="top">
				<table border="0" height="30" cellspacing="0" cellpadding="0" width="100%">
					<td bgcolor="eeeeee" align="center">Displaying media
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
						</b>matching media.
 					</td>

					<td width="150" bgcolor="eeeeee">
					</td>
					<td width="40" bgcolor="eeeeee">
						<input type="image" value="Search" name="filter" src="images/search.gif" border="0"
						 	title="New Search"/>
					</td>
					<td width="40" bgcolor="eeeeee">
						<xsl:if test="offset &gt; 0">
							<a href="mcm_console.m?nav=prev">
								<img src="images/prev.gif" alt="prev" border="0" title="Previous Search Results"/>		
							</a>
						</xsl:if>
					</td>
					<td width="40" bgcolor="eeeeee">
						<xsl:if test="offset + limit &lt; total">
							<a href="mcm_console.m?nav=next">
								<img src="images/next.gif" alt="next" border="0" title="Next Search Results"/>		
							</a>
						</xsl:if>
					</td>
				</table>
				<table border="0" width="100%" cellpadding="0" cellspacing="0" bgcolor="eeeeee">
					<tr>
						<td width="20" bgcolor="eeeeee"></td>
						<td width="130" bgcolor="eeeeee" class="label">Media Status</td>
						<td width="20" bgcolor="eeeeee" >
							<input type="radio" name="createOrUpdateDate" value="create">
								<xsl:if test="filter/createOrUpdateDate = 'create'">
	                  				<xsl:attribute name="checked">true</xsl:attribute>
								</xsl:if>
							</input>
						</td> 
						<td width="130" bgcolor="eeeeee" class="label">Creation date:</td>
						<td width="90" bgcolor="eeeeee">
							<input size="10" name="startCreationDate" type="text" value="{filter/startCreationDate}"/>
			      		</td>
						<td width="10" bgcolor="eeeeee" class="label">-</td>
						<td width="90" bgcolor="eeeeee">
							<input size="10" name="endCreationDate" type="text" value="{filter/endCreationDate}"/>
						</td>
			      		<xsl:choose>
							<xsl:when test="/model/mcmNotAvail = 'true'">
								<td width="60%" bgcolor="eeeeee" rowspan="2" align="center">
									<table border="1" cellpadding="4" cellspacing="4" bgcolor="eeeeee">
										<tr>										
											<td nowrap="" valign="middle" align="center" bgcolor="ee8888"><font color="000000">&#160;Media Creation Managment service not available!&#160;</font>
												<a href="mcm_console.m?action=check_mcm_avail">
													<img src="images/checkmcm.gif" alt="Retry" border="0" title="Retry"/>		
												</a>
											</td>
										</tr>
									</table>
								</td>
								<td width="100%" bgcolor="eeeeee" />
							</xsl:when>
							<xsl:otherwise>
								<td width="60%" bgcolor="eeeeee"></td>
							</xsl:otherwise>
						</xsl:choose>
					</tr>
					<tr>
						<td width="10" bgcolor="eeeeee" ></td>
						<td width="130" bgcolor="eeeeee">
							<select size="1" name="mediaStatus" title="Media Status:">
								<xsl:for-each select="filter/mediaStatusList/item">
									<xsl:sort data-type="number" order="ascending" select="status"/>
									<option>
										<xsl:attribute name="value"><xsl:value-of select="status"/></xsl:attribute>
										<xsl:if test="/model/filter/selectedStatus = status">
											<xsl:attribute name="selected"/>
										</xsl:if>
										<xsl:value-of select="description"/>
									</option>
								</xsl:for-each>
								<!-- additional 'all media types' option -->
								<option value="-all-" >
									<xsl:if test="/model/filter/selectedStatus = '-all-'">
										<xsl:attribute name="selected"/>
									</xsl:if>
									all
								</option>
							</select>
						</td>
						<td width="20" bgcolor="eeeeee" >
							<input type="radio" name="createOrUpdateDate" value="update">
								<xsl:if test="filter/createOrUpdateDate = 'update'">
	                  				<xsl:attribute name="checked">true</xsl:attribute>
								</xsl:if>
							</input>
						</td> 
						<td width="130" bgcolor="eeeeee" class="label">Update date:
						</td>
			      		<td width="90" bgcolor="eeeeee">
			        		<input size="10" name="startUpdateDate" type="text" value="{filter/startUpdateDate}"/>
			      		</td>
						<td width="10" bgcolor="eeeeee" class="label">-</td>
			      		<td width="90" bgcolor="eeeeee"> 
			        		<input size="10" name="endUpdateDate" type="text" value="{filter/endUpdateDate}"/>
			      		</td>
						<td width="60%" bgcolor="eeeeee"></td>
			      	</tr>
			      	<tr height="10">
						<td width="10" bgcolor="eeeeee" />
						<td width="130" bgcolor="eeeeee" />
						<td width="20" bgcolor="eeeeee" />
						<td width="130" bgcolor="eeeeee" />
			      		<td width="90" bgcolor="eeeeee" />
						<td width="10" bgcolor="eeeeee" />
			      		<td width="90" bgcolor="eeeeee" /> 
						<td width="60%" bgcolor="eeeeee" />
						<td width="100%" bgcolor="eeeeee" />
			      	</tr>
				</table>
			</td>
		</table>
	</form>
<!-- List of media -->
		<table width="70%" border="0" bordercolor="#ffffff" cellspacing="5" cellpadding="0">
		<tr>	<center>
			<td>
				<tr>
					<td width="20%"><h2>Fileset ID</h2></td>
					<td width="15%"><h2>Created</h2></td>
					<td width="15%"><h2>Updated</h2></td>
					<td width="15%"><h2>Usage</h2></td>
					<td width="10%"><h2>Status</h2></td>	
					<td width="10"><h2>Action</h2></td>	
				</tr>
					<xsl:apply-templates select="mediaList/item">
						<xsl:sort data-type="text" order="ascending" select="title"/>
					</xsl:apply-templates>
			</td>	</center>
		</tr>
		</table>


</xsl:template>

	<xsl:template match="item[@type='org.dcm4chex.archive.web.maverick.mcmc.model.MediaData']">
		<tr>
	        <td title="Media Fileset ID" >
				<xsl:value-of select="filesetID"/>
			</td>
	        <td title="Creation date" >
				<xsl:value-of select="createdTime"/>
	        </td>
	        <td title="Date of last update" >
				<xsl:value-of select="updatedTime"/>
	        </td>
	        <td title="Media usage: {mediaUsage} Bytes" >
				<xsl:value-of select="mediaUsageWithUnit"/>
	        </td>
	        <td title="Status info: {mediaStatusString}">
				<xsl:choose>
					<xsl:when test="mediaStatus = 0">
						<img src="images/cdrom-open.gif" alt="Media status: open" border="0" title="Status: open - info: {mediaStatusInfo}"/>		
					</xsl:when>
					<xsl:when test="mediaStatus = 1">
						<img src="images/cdrom-queued.gif" alt="Media status: queued" border="0" title="Status: queued - info: {mediaStatusInfo}"/>		
					</xsl:when>
					<xsl:when test="mediaStatus = 2">
						<img src="images/cdrom-creating.gif" alt="Media status: creating" border="0" title="Status: creating - info: {mediaStatusInfo}"/>		
					</xsl:when>
					<xsl:when test="mediaStatus = 3">
						<img src="images/cdrom-completed.gif" alt="Media status: completed" border="0" title="Status: completed - info: {mediaStatusInfo}"/>		
					</xsl:when>
					<xsl:when test="mediaStatus = -1">
						<img src="images/cdrom-failed.gif" alt="Media status: failed" border="0" title="Status: failed - info: {mediaStatusInfo}"/>		
					</xsl:when>
					<xsl:otherwise>
						unknown	(<xsl:value-of select="mediaStatus"/>)											
					</xsl:otherwise>
				</xsl:choose>
	        </td>
			<xsl:if test="mediaStatus = /model/statiForQueue and /model/mcmNotAvail = 'false'">
	        	<td title="Status info">
					<a href="mcm_console.m?action=queue&amp;mediaPk={mediaPk}">
						<img src="images/burn.gif" width="20" height="20" alt="Create media" border="0" title="Create media"/>		
					</a>
	        	</td>
 			</xsl:if>
			<xsl:if test="mediaStatus &lt; 0 and /model/mcmNotAvail = 'false'"><!-- error stati are lower than 0! -->
	        	<td title="Status info">
					<a href="mcm_console.m?action=queue&amp;mediaPk={mediaPk}">
						<img src="images/burn.gif" width="20" height="20" alt="Retry" border="0" title="Retry"/>		
					</a>
	        	</td>
 			</xsl:if>
		</tr>
	</xsl:template>
	   
</xsl:stylesheet>

