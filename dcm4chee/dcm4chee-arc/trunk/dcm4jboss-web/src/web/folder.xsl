<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE xsl:stylesheet [
<!ENTITY nbsp "&#160;">
]>
<!--
 $Id$
 -->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:output method="html" indent="yes"
 encoding="ISO-8859-1"
/>
<xsl:variable name="page_title">Patient List</xsl:variable>
<xsl:include href="page.xsl"/>

<xsl:template match="model">
	<form action="foldersubmit.m" method="post">
		<table border="0" cellspacing="0" cellpadding="0" width="100%">
			<td valign="top">
				<table border="0" height="30" cellspacing="0" cellpadding="0" width="100%">
					<td bgcolor="#eeeeee" align="center">Displaying studies
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
							</b>matching studies.
 					</td>

					<td width="150" bgcolor="#eeeeee">
					</td>
					<td width="40" bgcolor="#eeeeee">
						<input type="image" value="Search" name="filter" src="images/search.gif" border="0"/>
					</td>
					<td width="40" bgcolor="#eeeeee">
						<input type="image" value="Prev" name="prev" src="images/preview.gif" alt="preview" border="0">
							<xsl:if test="offset = 0"><xsl:attribute name="disabled">disabled</xsl:attribute>
							</xsl:if>
						</input>
					</td>
					<td width="40" bgcolor="#eeeeee">
						<input type="image" value="Next" name="next" src="images/next.gif" alt="next" border="0" >
							<xsl:if test="offset + limit &gt;= total">
								<xsl:attribute name="disabled">disabled</xsl:attribute>
							</xsl:if>
						</input>
					</td>
					<td width="40" bgcolor="#eeeeee">
						<input type="image" value="Merge" name="merge" src="images/merge.gif" alt="merge" border="0" onclick="return validateChecks(this.form.stickyPat, 'Patient', 2)">
							<xsl:if test="total &lt;= 0">
								<xsl:attribute name="disabled">disabled</xsl:attribute>
							</xsl:if>
						</input>
					</td>
					<td width="42" bgcolor="#eeeeee">
						<input type="image" value="Del" name="del" src="images/loeschen.gif" alt="delete" border="0" onclick="return confirm('Are you sure you want to delete?')">
							<xsl:if test="total &lt;= 0">
								<xsl:attribute name="disabled">disabled</xsl:attribute>
							</xsl:if>
						</input>
					</td>
				</table>
				<table border="0" width="100%" cellpadding="0" cellspacing="0">
					<td bgcolor="#eeeeee" color="#eeeeee" class="label">Patient ID:
					</td>
					<td bgcolor="#eeeeee" color="#eeeeee">
						<input size="10" name="patientID" type="text" value="{patientID}"/>
					</td>
					<td bgcolor="#eeeeee" color="#eeeeee" class="label">Patient Name:
					</td>
					<td bgcolor="#eeeeee" color="#eeeeee">
						<input size="10" name="patientName" type="text" value="{patientName}"/>
      		</td>
      		<td bgcolor="#eeeeee" color="#eeeeee" class="label">Study ID:</td>
      		<td bgcolor="#eeeeee" color="#eeeeee">
        		<input size="10" name="studyID" type="text" value="{studyID}"/>
      		</td>
      		<td bgcolor="#eeeeee" color="#eeeeee" class="label">Study Date:</td>
      		<td bgcolor="#eeeeee" color="#eeeeee"> 
        		<input size="10" name="studyDateRange" type="text" value="{studyDateRange}"/>
      		</td>
      		<td bgcolor="#eeeeee" color="#eeeeee" class="label">Accession No.:</td>
      		<td bgcolor="#eeeeee" color="#eeeeee">
        		<input size="10" name="accessionNumber" type="text" value="{accessionNumber}"/>
      		</td>
      		<td bgcolor="#eeeeee" color="#eeeeee" class="label">Modality:</td>
      		<td bgcolor="#eeeeee" color="#eeeeee">
        		<input size="10" name="modality" type="text" value="{modality}"/>
      		</td>
				</table>
					<xsl:call-template name="overview"/>
   			<table bgcolor="#bbbbbb" border="0" cellpadding="0" cellspacing="0" width="100%">
					<tr>
						<td>
							<table border="0" cellpadding="0" cellspacing="0" width="100%">
								<tbody valign="top">
									<xsl:apply-templates select="patients/item"/>
								</tbody>
							</table>
						</td>
					</tr>
				</table>
			</td>
	</table>
	</form>
</xsl:template>

<xsl:template name="overview">
<table border="0" cellpadding="0" cellspacing="0" width="100%">
	<table border="0" cellpadding="0" cellspacing="0" width="100%">
		<tr bgcolor="#eeeeee">
			<td width="5%" bgcolor="ccee00">
				<font size="1">
					Patient:</font>
			</td>
			<td width="25%">
				<font size="1" color="ff0000">
					&nbsp;Name:</font>
			</td>
    	<td width="10%">
				<font size="1" color="ff0000">
					ID:</font>
    	</td>
    	<td width="5%">
				<font size="1" color="ff0000">
					Sex:</font>
    	</td>
			<td width="10%">
				<font size="1" color="ff0000">
					Birthdate:</font>
			</td>
			<td width="40%">
			</td>
			<td width="5%" bgcolor="ccee00">
			</td>
			</tr>
	</table>	
	
	<table border="0" cellspacing="0" cellpadding="0" width="100%">
		<tr bgcolor="#eeeeee">
			<td width="5%" bgcolor="00eeee">
				<font size="1">
					Study:</font>
			</td>
			<td width="10%">
				<font size="1" color="ff0000">
					&nbsp;Date/Time:</font>
			</td>
			<td width="45%">
				<font size="1" color="ff0000">
					Description:</font>
			</td>
			<td width="8%">
				<font size="1" color="ff0000">
					Modalities:</font>
			</td>
			<td width="8%">
				<font size="1" color="ff0000">
					Se./Inst.:</font>
			</td>
			<td width="8%">
				<font size="1" color="ff0000">
					Acc.Nr.:</font>
			</td>
			<td width="8%">
				<font size="1" color="ff0000">
					ID:</font>
			</td>
			<td width="3%"> 
			</td>
			<td width="5%" bgcolor="00eeee"> 
			</td>
		</tr>
	</table>
	
	<table border="0" cellspacing="0" cellpadding="0" width="100%">
		<tr bgcolor="#eeeeee">
			<td width="5%" bgcolor="#ccccee">
				<font size="1" >
					Serie:</font>
			</td>
 		 	<td width="8%">
				<font size="1" color="ff0000">
					&nbsp;Mod.:</font>			
			</td>
			<td width="42%">
				<font size="1" color="ff0000">
					Description / Body Part Examined:</font>
			</td>
			<td width="8%">
				<font size="1" color="ff0000">
					# Inst.:</font>
			</td>
			<td width="7%">
				<font size="1" color="ff0000">
					Number:</font>
			</td>
			<td width="10%">
				<font size="1" color="ff0000">
					Date/Time:</font>
			</td>
			<td width="15%">
				<font size="1" color="ff0000">
					Mod.Vendor / Modelname:</font>
			</td>
			<td width="5%" bgcolor="ccccee"> 
			</td>
		</tr>
	</table>
</table>
</xsl:template>


<xsl:template match="item[@type='org.dcm4chex.archive.ejb.interfaces.PatientDTO']">
	<tr><table width="100%" callpadding="0" callspacing="0" border="0">
		<xsl:variable name="rowspan" select="1+count(descendant::item)"/>
			<td width="1%" align="right" bgcolor="ccee00" rowspan="{$rowspan}">
				<xsl:choose>
					<xsl:when test="$rowspan=1">
						<a title="Show Studies" href="expandPat.m?patPk={pk}">
						<img src="images/plus.gif" border="0" alt="+"/></a>				
					</xsl:when>
					<xsl:otherwise>
							<a title="Hide Studies" href="collapsePat.m?patPk={pk}">							
							<img src="images/minus.gif" border="0" alt="-"/></a>				
					</xsl:otherwise>
				</xsl:choose>
			</td>
      <td width="29%" title="Patient Name" >
				<strong><xsl:value-of select="patientName"/>
				</strong>
      </td>
      <td width="10%" title="Patient ID" >
				<strong><xsl:value-of select="patientID"/>
				</strong>
			</td>
      <td width="5%" title="Patient Sex">
				<strong><xsl:value-of select="patientSex"/>
				</strong>
      </td>
      <td width="10%" colspan="3" title="Birth Date">
				<strong><xsl:value-of select="patientBirthDate"/>
				</strong>
      </td>
      <td width="41%">
      </td>
			<td width="3%" align="right">
			  <a href="patientEdit.m?pk={pk}"><font size="1">Edit</font></a> 				  
			</td>
			<td width="2%" bgcolor="ccee00">
				<input type="checkbox" name="stickyPat" value="{pk}">
					<xsl:if test="/model/stickyPatients/item = pk">
						<xsl:attribute name="checked"/>
					</xsl:if>
				</input>
			</td>
	</table>
</tr>
			<xsl:apply-templates select="studies/item">
				<xsl:sort data-type="text" order="ascending" select="studyDateTime"/>
			</xsl:apply-templates>
</xsl:template>

<xsl:template match="item[@type='org.dcm4chex.archive.ejb.interfaces.StudyDTO']">
<tr>
	<table width="100%" callpadding="0" callspacing="0" border="0">
		<xsl:variable name="rowspan" select="1+count(descendant::item)"/>
		  <td width="2%" align="right" bgcolor="00eeee" rowspan="{$rowspan}">
				<xsl:choose>
					<xsl:when test="$rowspan=1">
						<a title="Show Series" href="expandStudy.m?patPk={../../pk}&amp;studyPk={pk}">
						<img src="images/plus.gif" border="0" alt="+"/></a>				
					</xsl:when>
					<xsl:otherwise>
						<a title="Hide Series" href="collapseStudy.m?patPk={../../pk}&amp;studyPk={pk}">							
						<img src="images/minus.gif" border="0" alt="-"/></a>				
					</xsl:otherwise>
				</xsl:choose>
			</td>
      <td width="13%" title="Study Date">
				<xsl:value-of select="studyDateTime"/>
			</td>
      <td width="45%" title="Study Description">
				<xsl:value-of select="studyDescription"/>
			</td>
		 	<td width="8%"  title="Modalities">
				<xsl:value-of select="modalitiesInStudy"/>
			</td>
      <td width="8%" title="Number of Series\Instances">
				<xsl:value-of select="numberOfSeries"/>\<xsl:value-of select="numberOfInstances"/>
			</td>
			<td width="8%" title="Accession Number">
				<xsl:value-of select="accessionNumber"/>
			</td>
			<td width="8%" title="Study ID">
				<xsl:value-of select="studyID"/>
			</td>
			
			<td width="6%">
			</td>
			
			<td width="2%" align="right" bgcolor="00eeee">
				<input type="checkbox" name="stickyStudy" value="{pk}">
					<xsl:if test="/model/stickyStudies/item = pk">
						<xsl:attribute name="checked"/>
					</xsl:if>
				</input>
			</td>
	</table>
</tr>
	<xsl:apply-templates select="series/item">
		<xsl:sort data-type="number" order="ascending" select="seriesNumber"/>
	</xsl:apply-templates>
</xsl:template>

<xsl:template match="item[@type='org.dcm4chex.archive.ejb.interfaces.SeriesDTO']">
	<tr>
<table width="100%" callpadding="0" callspacing="0" border="0">	  
		<xsl:variable name="rowspan" select="1+count(descendant::item)"/>
		  <td width="3%" align="right" bgcolor="ccccee" rowspan="{$rowspan}">
				<xsl:choose>
					<xsl:when test="$rowspan=1">
		  			<a title="Show Instances" href="expandSeries.m?patPk={../../../../pk}&amp;studyPk={../../pk}&amp;seriesPk={pk}">
						<img src="images/plus.gif" border="0" alt="+"/></a>				
					</xsl:when>
					<xsl:otherwise>
		  			<a title="Hide Instances" href="collapseSeries.m?patPk={../../../../pk}&amp;studyPk={../../pk}&amp;seriesPk={pk}">
						<img src="images/minus.gif" border="0" alt="-"/></a>				
					</xsl:otherwise>
				</xsl:choose>
			</td>
      <td width="10%" title="Modality">
				<xsl:value-of select="modality"/>
			</td>
      <td colspan="42%" title="Series Description / Body Part">
    		<xsl:value-of select="seriesDescription"/>
\<xsl:value-of select="bodyPartExamined"/>
      </td>
			<td width="8%" title="Number of Instances">
			<xsl:value-of select="numberOfInstances"/>
			</td>
			<td width="7%" title="Series Number">
			<xsl:value-of select="numberOfSeries"/>
			</td>
			<td width="10%" title="Series Date">
				<xsl:value-of select="seriesDateTime"/>
			</td>
			<td width="15%" title="Modality Vendors / Modelname">
    		<xsl:value-of select="manufacturer"/>
\<xsl:value-of select="manufacturerModelName"/>
      </td>
			<td width="3%">
			</td>
			<td width="2%" align="right" bgcolor="ccccee">
				<input type="checkbox" name="stickySeries" value="{pk}">
					<xsl:if test="/model/stickySeries/item = pk">
						<xsl:attribute name="checked"/>
					</xsl:if>
				</input>
			</td></table>
	</tr>
		<xsl:apply-templates select="instances/item">
			<xsl:sort data-type="number" order="ascending" select="instanceNumber"/>
		</xsl:apply-templates>
</xsl:template>

<xsl:template match="item[@type='org.dcm4chex.archive.ejb.interfaces.ImageDTO']">
	<tr>
<table width="100%" callpadding="0" callspacing="0" border="0">		  
		<td width="4%" bgcolor="#eeeeee">
		</td>
		<td width="6%" title="Instance Number">
	   		<xsl:value-of select="instanceNumber"/>
    </td>
		<td width="15%" title="Content Datetime">
			<xsl:value-of select="contentDateTime"/>
		</td>
    <td width="15%" title="ImageType">
			<xsl:value-of select="imageType"/>
		</td>
    <td width="20%" title="Pixel Matrix">
	    	<xsl:value-of select="photometricInterpretation"/>
				&nbsp;
    	<xsl:value-of select="rows"/>x<xsl:value-of select="columns"/>x<xsl:value-of select="numberOfFrames"/>
				&nbsp;
    	<xsl:value-of select="bitsAllocated"/>bits
    </td>
		<td width="10%" title="Number of Files">
			<xsl:value-of select="numberOfFiles"/>
		</td>
	  <td width="28%" title="Retrieve AETs">
			<xsl:value-of select="retrieveAETs"/>
    </td>
		<td width="2%" align="right" bgcolor="#eeeeee">
			<input type="checkbox" name="stickyInst" value="{pk}">
				<xsl:if test="/model/stickyInstances/item = pk">
					<xsl:attribute name="checked"/>
				</xsl:if>
			</input>
		</td></table>
	</tr>
</xsl:template>

<xsl:template match="item[@type='org.dcm4chex.archive.ejb.interfaces.PresentationStateDTO']">
	<tr>
<table width="100%" callpadding="0" callspacing="0" border="0">		  
		<td width="4%" bgcolor="#eeeeee">
		</td>
		<td width="6%" title="Instance Number">
			<xsl:value-of select="instanceNumber"/>
    </td>
		<td width="15%" title="Creation Datetime">
      <xsl:value-of select="presentationCreationDateTime"/>
		</td>
    <td width="20%" title="Presentation Description">
      <xsl:value-of select="presentationDescription"/>
		</td>
		<td width="5%" title="Presentation Label">
    	<xsl:value-of select="presentationLabel"/>
		</td>
		<td width="5%" title="Number of Referenced Images">
      -&gt;<xsl:value-of select="numberOfReferencedImages"/>
		</td>
		<td width="5%" title="Number of Files">
      <xsl:value-of select="numberOfFiles"/>
		</td>
		<td width="38%" title="Retrieve AETs">
      <xsl:value-of select="retrieveAETs"/>
    </td>
		<td width="2%" align="right" bgcolor="#eeeeee">
			<input type="checkbox" name="stickyInst" value="{pk}">
				<xsl:if test="/model/stickyInstances/item = pk">
					<xsl:attribute name="checked"/>
				</xsl:if>
			</input>
		</td></table>
	</tr>
</xsl:template>

<xsl:template match="item[@type='org.dcm4chex.archive.ejb.interfaces.StructuredReportDTO']">
	<tr>
<table width="100%" callpadding="1" callspacing="0" border="0">		  
		<td width="4%" bgcolor="#eeeeee">
		</td>
		<td width="6%" title="Instance Number">
  		<xsl:value-of select="instanceNumber"/>
    </td>
		<td width="15%" title="Content Datetime">
    	<xsl:value-of select="contentDateTime"/>
		</td>
    <td width="15%" title="Document Title">
  		<xsl:value-of select="documentTitle"/>
		</td>
		<td width="15%" colspan="2" title="Document Status">
      <xsl:value-of select="completionFlag"/>/<xsl:value-of select="verificationFlag"/>
    </td>
		<td width="5%" title="Number of Files">
			<xsl:value-of select="numberOfFiles"/>
		</td>
	  <td width="38%" title="Retrieve AETs">
			<xsl:value-of select="retrieveAETs"/>
    </td>
		<td width="2%" align="right" bgcolor="#eeeeee">
			<input type="checkbox" name="stickyInst" value="{pk}">
				<xsl:if test="/model/stickyInstances/item = pk">
					<xsl:attribute name="checked"/>
				</xsl:if>
			</input>
		</td></table>
	</tr>
</xsl:template>
</xsl:stylesheet>
