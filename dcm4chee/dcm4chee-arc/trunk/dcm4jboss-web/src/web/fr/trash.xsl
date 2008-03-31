<?xml version="1.0" encoding="UTF-8"?>
<!--
 $Id: trash.xsl 5390 2007-11-05 11:54:36Z javawilli $
 -->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:param name="trash.remove" select="'false'" />

<xsl:output method="html" indent="yes" encoding="UTF-8"/>
<xsl:variable name="page_title">Liste des patients</xsl:variable>
<xsl:include href="page.xsl"/>

<xsl:template match="model">
	<form action="trashfolder.m" method="post" name="myForm" accept-charset="UTF-8">
		<table class="folder_header" border="0" cellspacing="0" cellpadding="0" width="100%">
			<td class="folder_header" valign="top">
				<table class="folder_header" border="0" height="30" cellspacing="0" cellpadding="0" width="100%">
					<td class="folder_header" width="5">
						<input type="checkbox" name="showWithoutStudies" value="true" title="Afficher les patients sans examens">
							<xsl:if test="/model/showWithoutStudies = 'true'">
								<xsl:attribute name="checked"/>
							</xsl:if>
						</input>
					</td>
					<td class="folder_header" width="5" title="Afficher les patients sans examens">0 examens</td>
					<td class="folder_header" align="center">
					<xsl:choose>
						<xsl:when test="total &lt; 1">
							Aucun examen correspondant trouvé!
						</xsl:when>
						<xsl:otherwise>
							Affichage des examens
							<b>
								<xsl:value-of select="offset + 1"/>
							</b>
								de
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
								à
							<b>
								<xsl:value-of select="total"/>
							</b> examens trouvés.
						</xsl:otherwise>
					</xsl:choose>
 					</td>

					<td class="folder_header" width="150">
					</td>
					<td class="folder_header" width="40">
						<input type="image" value="Search" name="filter" src="images/search.gif" border="0"
						 	title="Nouvelle Recherche"/>
					</td>
					<td class="folder_header" width="40">
						<input type="image" value="Prev" name="prev" src="images/prev.gif" alt="prev" border="0"
						 	title="Résultats précédents">
							<xsl:if test="offset = 0">
                  <xsl:attribute name="disabled">disabled</xsl:attribute>
							</xsl:if>
						</input>
					</td>
					<td class="folder_header" width="40">
						<input type="image" value="Next" name="next" src="images/next.gif" alt="next" border="0"
						 	title="Résultats Suivants">
							<xsl:if test="offset + limit &gt;= total">
								<xsl:attribute name="disabled">disabled</xsl:attribute>
							</xsl:if>
						</input>
					</td>
					<td class="folder_header" width="40">
						&#160;
					</td>
					<td class="folder_header" width="40">
						<input type="image" value="Undel" name="undel" src="images/undel.gif" alt="undelete" border="0"
							title="Restaurer les Entités sélectionnées"
							onclick="return confirm('Restaurer les Entités sélectionnées?')">
							<xsl:if test="total &lt;= 0">
								<xsl:attribute name="disabled">disabled</xsl:attribute>
							</xsl:if>
						</input>
					</td>
					<xsl:if test="$trash.remove='true'">	
						<td class="folder_header" width="40">
							<input type="image" value="Del" name="del" src="images/loeschen.gif" alt="delete" border="0"
								title="Supprimer les Entités sélectionnées"
								onclick="return confirm('Supprimer les Entités sélectionnées?')">
								<xsl:if test="total &lt;= 0">
									<xsl:attribute name="disabled">disabled</xsl:attribute>
								</xsl:if>
							</input>
						</td>
						<td class="folder_header" width="40">
							<input type="image" value="EmptyTrash" name="emptyTrash" src="images/deltrash.gif" alt="emptyTrash" border="0"
								title="Supprimer toutes les entités de la corbeille"
								onclick="return confirm('Voulez-vous vraiment vider la corbeille?')">
								<xsl:if test="total &lt;= 0">
									<xsl:attribute name="disabled">disabled</xsl:attribute>
								</xsl:if>
							</input>
						</td>
					</xsl:if>
				</table>
				<table class="folder_search" border="0" width="100%" cellpadding="0" cellspacing="0">
				  <tr>
					<td class="folder_search" >Nom du patient:
					</td>
					<td>
						<input size="10" name="patientName" type="text" value="{patientName}"/>
      				</td>
					<td class="folder_search" >Patient ID:
					</td>
					<td>
						<input size="10" name="patientID" type="text" value="{patientID}"/>
					</td>
		      		<td class="label">Examen IUID:</td>
		      		<td>
		        		<input size="45" name="studyUID" type="text" value="{studyUID}"/>
		      		</td>
		      		<td class="label">Matricule:
							</td>
		      		<td>
		        		<input size="10" name="accessionNumber" type="text" value="{accessionNumber}"/>
		      		</td>
		      	  </tr>
				</table>
					<xsl:call-template name="overview"/>
   			<table border="0" cellpadding="0" cellspacing="0" width="100%">
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
<table class="folder_overview" border="0" cellpadding="0" cellspacing="0" width="100%">
	<table class="folder_overview" border="0" cellpadding="0" cellspacing="0" width="100%">
		<colgroup>
			<col width="5%"/>
			<col width="22%"/>
			<col width="10%"/>
			<col width="12%"/>
			<col width="47%"/>
			<col width="4%"/>
		</colgroup>
		<tr>
			<td class="patient_mark" >
				<font size="1">
					Patient</font>
			</td>
			<td>
				<font size="1">
					Nom:</font>
			</td>
    	<td>
				<font size="1">
					Patient ID:</font>
    	</td>
			<td>
				<font size="1">
					Date de naissance:</font>
			</td>
    	<td>
				<font size="1">
					Sexe:</font>
    	</td>
			<td>
			</td>
			</tr>
	</table>	
	
	<table class="folder_overview" border="0" cellspacing="0" cellpadding="0" width="100%">
		<colgroup>
			<col width="5%"/><!-- margin -->
			<col width="11%"/><!-- Date/time -->
			<col width="22%"/><!-- StudyID -->
			<col width="26%"/><!-- Study Instance UID -->
			<col width="9%"/><!-- Acc No --><!-- 73 -->
    		<col width="13%"/><!-- Ref. Physician -->
		    <col width="8%"/><!-- Study Status ID -->
			<col width="2%"/><!-- add -->
			<col width="2%"/><!-- edit -->
			<col width="2%"/><!-- sticky -->
		</colgroup>
		<tr>
			<td class="study_mark" >
				<font size="1">
					Examen</font>
			</td>
			<td>
				<font size="1">
					Date/Heure:</font>
			</td>
			<td>
				<font size="1">
					Examen ID (@Media):</font>
			</td>
			<td>
				<font size="1">
				Examen IUID:
				</font>
			</td>
			<td>
				<font size="1">
					Matricule:</font>
			</td>
			<td>
				<font size="1">
					Ref. Médecin:</font>
			</td>
			<td>
				<font size="1">
					Status:</font>
			</td>
			<td>&#160;</td>
			<td>&#160;</td>
			<td>&#160;</td>
		</tr>
	</table>
	
	<table class="folder_overview" border="0" cellspacing="0" cellpadding="0" width="100%">
		<colgroup>
			<col width="5%"/><!-- margin -->
			<col width="12%"/><!-- date/time -->
			<col width="12%"/><!-- Series No -->
			<col width="10%"/><!-- Modality -->
			<col width="35%"/><!-- Series Instance UID. -->
			<col width="10%"/><!-- Vendor/Model -->
			
    		<col width="12%"/><!-- PPS Status -->
			<col width="2%"/><!-- edit -->
			<col width="2%"/><!-- sticky -->
		</colgroup>
		<tr>
			<td class="series_mark" >
				<font size="1">
					Séries</font>
			</td>
			<td>
				<font size="1">
					Date/Heure:</font>
			</td>
			<td>
				<font size="1">
					Séries No (@media):</font>
			</td>
			<td>
				<font size="1">
					Mod:</font>			
			</td>
			<td>
				<font size="1">
				Instance Séries UID:
				</font>
			</td>
			<td>
				<font size="1">
					Distributeur/Modèle:</font>
			</td>
			<td>
				<font size="1">
					PPS Status:</font>
			</td>
			<td>&#160;</td>
			<td>&#160;</td>
			<td align="right" valign="bottom">&#160;
				<img src="images/plus.gif" alt="Select all Studies" onclick="selectAll( document.myForm,'stickyStudy', true)" />
				<img src="images/minus.gif" alt="Deselect all" onclick="selectAll( document.myForm,'sticky', false)" />
			</td>
		</tr>
	</table>
</table>
</xsl:template>


<xsl:template match="item[@type='org.dcm4chex.archive.web.maverick.model.PatientModel']">
	<tr>
      <table class="patient_line" width="100%" cellpadding="0" cellspacing="0" border="0">
		<colgroup>
			<col width="1%"/>
			<col width="26%"/>
			<col width="10%"/>
			<col width="12%"/>
		    <col width="45%"/>
		    <col width="2%"/>
		    <col width="2%"/>
			<col width="2%"/>
		</colgroup>
		<xsl:variable name="rowspan" select="1+count(descendant::studies/item)"/>
			<td class="patient_mark" align="right" rowspan="{$rowspan}">
				<xsl:choose>
					<xsl:when test="$rowspan=1">
						<a title="Afficher Examens" href="expandTrashPatient.m?patPk={pk}&amp;expand=true">
							<img src="images/plus.gif" border="0" alt="+"/>
              			</a>				
					</xsl:when>
					<xsl:otherwise>
						<a title="Cacher Examens" href="expandTrashPatient.m?patPk={pk}&amp;expand=false">							
							<img src="images/minus.gif" border="0" alt="-"/>
              			</a>				
					</xsl:otherwise>
				</xsl:choose>
			</td>
			<td title="Nom du Patient">
				<strong><xsl:value-of select="patientName"/>&#160;</strong>
  			</td>
			<td title="ID Patient">
				<strong><xsl:value-of select="patientID"/>&#160;</strong>
			</td>
			<td title="Date de naissance">
				<strong><xsl:value-of select="patientBirthDate"/>&#160;</strong>
			</td>
			<td title="Sexe">
				<strong><xsl:value-of select="patientSex"/>&#160;</strong>
			</td>
            <td>&#160;</td>
			<td class="patient_mark" align="right">
				<a href="trashfolder.m?undel=patient&amp;patPk={pk}"
					onclick="return confirm('Restaurer ce patient ?')">
					<img src="images/undel.gif" alt="Undelete Patient" border="0" title="Restaurer Patient"/>		
				</a>
			</td>
			<td class="patient_mark" align="right">
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

<xsl:template match="item[@type='org.dcm4chex.archive.web.maverick.model.StudyModel']">
	<tr>
		<table class="study_line" width="100%" cellpadding="0" cellspacing="0" border="0">
			<xsl:variable name="rowspan" select="1+count(descendant::series/item)"/>
			<colgroup>
				<col width="2%"/><!-- margin -->
				<col width="14%"/><!-- Date/time -->
				<col width="22%"/><!-- StudyID -->
				<col width="26%"/><!-- Study Instance UID -->
				<col width="9%"/><!-- Acc No -->
	    		<col width="13%"/><!-- Ref. Physician -->
			    <col width="8%"/><!-- Study Status ID -->
				<col width="2%"/><!-- add -->
				<col width="2%"/><!-- edit -->
				<col width="2%"/><!-- sticky -->
			</colgroup>
			<td class="study_mark" align="right" rowspan="{$rowspan}">
				<xsl:choose>
					<xsl:when test="$rowspan=1">
						<a title="Afficher Séries" href="expandTrashStudy.m?patPk={../../pk}&amp;studyPk={pk}&amp;expand=true">
						    <img src="images/plus.gif" border="0" alt="+"/>
	                                                                                    </a>				
					</xsl:when>
					<xsl:otherwise>
						<a title="Cacher Séries" href="expandTrashStudy.m?patPk={../../pk}&amp;studyPk={pk}&amp;expand=false">							
						    <img src="images/minus.gif" border="0" alt="-"/>
	                                                                                    </a>				
					</xsl:otherwise>
				</xsl:choose>
			</td>
	  		<td title="Date de l'examen">
				<xsl:value-of select="studyDateTime"/>&#160;
			</td>
			<td title="ID Examen (@Media)" >
				<xsl:value-of select="studyID"/>
				<xsl:if test="filesetId != '_NA_'"> @<xsl:value-of select="filesetId"/> </xsl:if>
				&#160;
			</td>
	  		<td title="Study IUID">
				<xsl:value-of select="studyIUID"/>&#160;
			</td>
			<td title="Matricule">
				&#160;<xsl:value-of select="accessionNumber"/>&#160;
			</td>
	  		<td title="Médecin référant">
				<xsl:value-of select="referringPhysician"/>&#160;
			</td>
	  		<td title="Status ID Examen" align="center">
	  			<xsl:choose>
	  				<xsl:when test="studyStatusImage!=''">
		      			<img src="{studyStatusImage}" border="0" alt="{studyStatusId}"/>
		      		</xsl:when>
	  				<xsl:when test="studyStatusId!=''">
	  					<xsl:value-of select="studyStatusId"/>
	  				</xsl:when>
	  				<xsl:otherwise>&#160;</xsl:otherwise>
	      		</xsl:choose>
			</td>
            <td>&#160;</td>
		    <td class="study_mark" align="right">
				<a href="trashfolder.m?undel=study&amp;studyPk={pk}"
					onclick="return confirm('Restaurer cet examen ?')">
					<img src="images/undel.gif" alt="Undelete Study" border="0" title="Restaurer Examen"/>		
				</a>
		    </td>
			<td class="study_mark" align="right">
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

<xsl:template match="item[@type='org.dcm4chex.archive.web.maverick.model.SeriesModel']">
	<tr>
		<table class="series_line" width="100%" cellpadding="0" cellspacing="0" border="0" >	  
			<colgroup>
				<col width="3%"/><!-- left margin -->
				<col width="2%"/><!-- spacer -->
				<col width="14%"/><!-- Date/Time -->
				<col width="12%"/><!-- Series No -->
				<col width="10%"/><!-- Modality -->
				<col width="35%"/><!-- Series Instance UID. -->
				<col width="10%"/><!-- Vendor/Model -->
	    		<col width="12%"/><!-- PPS Status -->
				<col width="2%"/><!-- edit -->
				<col width="2%"/><!-- sticky -->
			</colgroup>
			<xsl:variable name="rowspan" select="1+count(descendant::instances/item)"/>
			<td class="series_mark" align="right" rowspan="{$rowspan}">
				<xsl:choose>
					<xsl:when test="$rowspan=1">
		  				<a title="Afficher Instances" href="expandTrashSeries.m?patPk={../../../../pk}&amp;studyPk={../../pk}&amp;seriesPk={pk}&amp;expand=true">
							<img src="images/plus.gif" border="0" alt="+"/>
              			</a>				
					</xsl:when>
					<xsl:otherwise>
			  			<a title="Cacher Instances" href="expandTrashSeries.m?patPk={../../../../pk}&amp;studyPk={../../pk}&amp;seriesPk={pk}&amp;expand=false">
							<img src="images/minus.gif" border="0" alt="-"/>
	              		</a>				
					</xsl:otherwise>
				</xsl:choose>
			</td>
			<td>&#160;</td>
			<td title="Date Séries">
				<xsl:value-of select="seriesDateTime"/>&#160;
			</td>
			<td title="Numéro Séries (@media)">
				<xsl:value-of select="seriesNumber"/>
				<xsl:if test="filesetId != '_NA_'"> @<xsl:value-of select="filesetId"/> </xsl:if>
				&#160;
			</td>
		    <td title="Modalité">
				<xsl:value-of select="modality"/>&#160;
			</td>
      		<td title="UID Instance Séries">
						<xsl:value-of select="seriesIUID"/>&#160;
	      	</td>
			<td title="Modalité Distributeur / Modèle">
    			<xsl:value-of select="manufacturer"/>
					\ <xsl:value-of select="manufacturerModelName"/>&#160;
      		</td>
			<td>
				<xsl:choose>
					<xsl:when test="PPSStatus='DISCONTINUED' or incorrectWLEntry='true'">
						<xsl:attribute name="style">color: red</xsl:attribute>
						<xsl:attribute name="title">PPS Status:<xsl:value-of select="DRCodeMeaning"/></xsl:attribute>
						<xsl:text>ABANDONNEES</xsl:text>
					</xsl:when>
					<xsl:when test="PPSStatus!=''">
						<xsl:attribute name="style">color: black</xsl:attribute>
						<xsl:attribute name="title">PPS Status</xsl:attribute>
						<xsl:value-of select="PPSStatus"/>	
					</xsl:when>
				</xsl:choose>
	    		&#160;
    	  	</td>
			<td>&#160;</td>
            <td class="series_mark" align="right">
				<a href="trashfolder.m?undel=series&amp;seriesPk={pk}"
					onclick="return confirm('Restaurer ces séries ?')">
					<img src="images/undel.gif" alt="Undelete Series" border="0" title="Restaurer Séries"/>		
				</a>
	        </td>
            <td class="series_mark" align="right">
				<input type="checkbox" name="stickySeries" value="{pk}">
					<xsl:if test="/model/stickySeries/item = pk">
						<xsl:attribute name="checked"/>
					</xsl:if>
				</input>
			</td>
      </table>
	</tr>
	<xsl:apply-templates select="instances/item">
		<xsl:sort data-type="number" order="ascending" select="instanceNumber"/>
	</xsl:apply-templates>
</xsl:template>

<xsl:template match="item[@type='org.dcm4chex.archive.web.maverick.model.ImageModel']">
	<tr>
<table class="instance_line" width="100%" cellpadding="0" cellspacing="0" border="0">
		<colgroup>
			<col width="4%"/>
			<col width="12%"/>
			<col width="3%"/>
			<col width="6%"/>
			<col width="25%"/>
			<col width="5%"/>
			<col width="10%"/>
			<col width="31%"/>
			<col width="2%"/>
			<col width="2%"/>
		</colgroup>
		<xsl:variable name="rowspan" select="1+count(descendant::files/item)"/>
		  <td align="right" rowspan="{$rowspan}">
			<xsl:choose>
				<xsl:when test="$rowspan=1">
	  				<a title="Afficher fichiers" href="expandTrashInstance.m?expand=true&amp;patPk={../../../../../../pk}&amp;studyPk={../../../../pk}&amp;seriesPk={../../pk}&amp;instancePk={pk}">
						<img src="images/plus.gif" border="0" alt="+"/>
  		            </a>				
				</xsl:when>
				<xsl:otherwise>
  			        <a title="Cacher Instances" href="expandTrashInstance.m?expand=false&amp;patPk={../../../../../../pk}&amp;studyPk={../../../../pk}&amp;seriesPk={../../pk}&amp;instancePk={pk}">
						<img src="images/minus.gif" border="0" alt="-"/>
                    </a>				
				</xsl:otherwise>
			</xsl:choose>
                                </td>

		<td title="Date/Heure Contenu" >
			<xsl:value-of select="contentDateTime"/>&#160;
		</td>
		<td title="Numéro d'Instance" >
	   		<xsl:value-of select="instanceNumber"/>&#160;
    </td>
    <td title="Type d'Image" >
			<xsl:value-of select="imageType"/>&#160;
		</td>
    <td title="Matrice de Pixels" >
	    	<xsl:value-of select="photometricInterpretation"/>
				??
    		<xsl:value-of select="rows"/>x<xsl:value-of select="columns"/>x<xsl:value-of select="numberOfFrames"/>
				??
    		<xsl:value-of select="bitsAllocated"/>bits&#160;
    </td>
		<td title="Nombre de Fichiers" >
			<xsl:value-of select="numberOfFiles"/>&#160;
		</td>
	  	<td title="Retrieve AETs" >
			<xsl:value-of select="retrieveAETs"/>&#160;
    	</td>
	  	<td title="SopIUID" >
			<xsl:value-of select="sopIUID"/>&#160;
    	</td>
        <td class="instance_mark" align="right">
			<a href="trashfolder.m?undel=instance&amp;instancePk={pk}"
				onclick="return confirm('Restaurer cette instance ?')">
				<img src="images/undel.gif" alt="Undelete Instance" border="0" title="Restaurer Instance"/>		
			</a>
        </td>
		
		<td class="instance_mark" align="right">
			<input type="checkbox" name="stickyInst" value="{pk}">
				<xsl:if test="/model/stickyInstances/item = pk">
					<xsl:attribute name="checked"/>
				</xsl:if>
			</input>
		</td>
      </table>
	</tr>
		<xsl:apply-templates select="files/item">
			<xsl:sort data-type="number" order="descending" select="pk"/>
		</xsl:apply-templates>
</xsl:template>

<xsl:template match="item[@type='org.dcm4chex.archive.web.maverick.model.PresentationStateModel']">
	<tr>
<table class="instance_line" width="100%" cellpadding="0" cellspacing="0" border="0">	
		<colgroup>
			<col width="4%"/>
			<col width="15%"/>
			<col width="6%"/>
			<col width="20%"/>
			<col width="5%"/>
			<col width="5%"/>
			<col width="5%"/>
			<col width="12%"/>
			<col width="24%"/>
			<col width="2%"/>
			<col width="2%"/>
		</colgroup>
		<xsl:variable name="rowspan" select="1+count(descendant::files/item)"/>
		  <td align="right" rowspan="{$rowspan}">
			<xsl:choose>
				<xsl:when test="$rowspan=1">
	  				<a title="Afficher fichiers" href="expandTrashInstance.m?expand=true&amp;patPk={../../../../../../pk}&amp;studyPk={../../../../pk}&amp;seriesPk={../../pk}&amp;instancePk={pk}">
						<img src="images/plus.gif" border="0" alt="+"/>
              		                                            </a>				
				</xsl:when>
				<xsl:otherwise>
	  			        <a title="Cacher les instances" href="expandTrashInstance.m?expand=false&amp;patPk={../../../../../../pk}&amp;studyPk={../../../../pk}&amp;seriesPk={../../pk}&amp;instancePk={pk}">
					<img src="images/minus.gif" border="0" alt="-"/>
                                                                              </a>				
				</xsl:otherwise>
			</xsl:choose>
                                </td>
		<td title="Date/Heure Création" >
      		<xsl:value-of select="presentationCreationDateTime"/>&#160;
		</td>
		<td title="Numéro d'Instance" >
			<xsl:value-of select="instanceNumber"/>&#160;
    	</td>
    	<td title="Description de la Présentation" >
      		<xsl:value-of select="presentationDescription"/>&#160;
		</td>
		<td title="Presentation Label" >
    		<xsl:value-of select="presentationLabel"/>&#160;
		</td>
		<td title="Nombre d'Images Référencées" >
      		-&gt;<xsl:value-of select="numberOfReferencedImages"/>&#160;
		</td>
		<td title="Nombre de Fichiers" >
      		<xsl:value-of select="numberOfFiles"/>&#160;
		</td>
		<td title="Retrieve AETs" >
      		<xsl:value-of select="retrieveAETs"/>&#160;
    	</td>
	  	<td title="SopIUID" >
			<xsl:value-of select="sopIUID"/>&#160;
    	</td>
        <td class="instance_mark" align="right">
			<a href="trashfolder.m?undel=instance&amp;patPk={../../../../../../pk}&amp;studyPk={../../../../pk}&amp;seriesPk={../../pk}&amp;instancePk={pk}"
				onclick="return confirm('Restaurer cette instance ?')">
				<img src="images/undel.gif" alt="Undelete Instance" border="0" title="Restaurer l'Instance"/>		
			</a>
        </td>
		<td class="instance_mark" align="right">
			<input type="checkbox" name="stickyInst" value="{pk}">
				<xsl:if test="/model/stickyInstances/item = pk">
					<xsl:attribute name="checked"/>
				</xsl:if>
			</input>
		</td>
      </table>
	</tr>
		<xsl:apply-templates select="files/item">
			<xsl:sort data-type="number" order="descending" select="pk"/>
		</xsl:apply-templates>
</xsl:template>

<xsl:template match="item[@type='org.dcm4chex.archive.web.maverick.model.StructuredReportModel']">
	<tr>
<table class="instance_line" width="100%" cellpadding="1" cellspacing="0" border="0">		 
		<colgroup>
			<col width="4%"/>
			<col width="15%"/>
			<col width="6%"/>
			<col width="15%"/>
			<col width="15%"/>
			<col width="5%"/>
			<col width="13%"/>
			<col width="23"/>
			<col width="2%"/>
			<col width="2%"/>
		</colgroup>
 
		<xsl:variable name="rowspan" select="1+count(descendant::files/item)"/>
		  <td align="right" rowspan="{$rowspan}">
			<xsl:choose>
				<xsl:when test="$rowspan=1">
	  				<a title="Afficher Fichiers" href="expandTrashInstance.m?expand=true&amp;patPk={../../../../../../pk}&amp;studyPk={../../../../pk}&amp;seriesPk={../../pk}&amp;instancePk={pk}">
						<img src="images/plus.gif" border="0" alt="+"/>
              		                                            </a>				
				</xsl:when>
				<xsl:otherwise>
	  			        <a title="Cacher Instances" href="expandTrashInstance.m?expand=false&amp;patPk={../../../../../../pk}&amp;studyPk={../../../../pk}&amp;seriesPk={../../pk}&amp;instancePk={pk}">
					<img src="images/minus.gif" border="0" alt="-"/>
                                                                              </a>				
				</xsl:otherwise>
			</xsl:choose>
                                </td>
		<td title="Date/Heure Contenu" >
    	                    <xsl:value-of select="contentDateTime"/>&#160;
		</td>
		<td title="Numéro d'Instance" >
  		    <xsl:value-of select="instanceNumber"/>&#160;
    	</td>
    	<td title="Titre du Document" >
  			<xsl:value-of select="documentTitle"/>&#160;
		</td>
		<td title="Status du Document" >
      		<xsl:value-of select="completionFlag"/>/<xsl:value-of select="verificationFlag"/>&#160;
    	</td>
		<td title="Nombre de Fichiers" >
			<xsl:value-of select="numberOfFiles"/>&#160;
		</td>
	  	<td title="Retrieve AETs" >
			<xsl:value-of select="retrieveAETs"/>&#160;
    	</td>
	  	<td title="SopIUID" >
			<xsl:value-of select="sopIUID"/>&#160;
    	</td>
        <td class="instance_mark" align="right">
			<a href="trashfolder.m?undel=instance&amp;patPk={../../../../../../pk}&amp;studyPk={../../../../pk}&amp;seriesPk={../../pk}&amp;instancePk={pk}"
				onclick="return confirm('Restaurer cette instance ?')">
				<img src="images/undel.gif" alt="Undelete Instance" border="0" title="Restaurer Instance"/>		
			</a>
        </td>
		<td class="instance_mark" align="right">
			<input type="checkbox" name="stickyInst" value="{pk}">
				<xsl:if test="/model/stickyInstances/item = pk">
					<xsl:attribute name="checked"/>
				</xsl:if>
			</input>
		</td>
</table>
	</tr>
		<xsl:apply-templates select="files/item">
			<xsl:sort data-type="number" order="descending" select="pk"/>
		</xsl:apply-templates>
</xsl:template>

<xsl:template match="item[@type='org.dcm4chex.archive.web.maverick.model.WaveformModel']">
	<tr>
<table class="instance_line" width="100%" cellpadding="0" cellspacing="0" border="0">
		<colgroup>
			<col width="4%"/>
			<col width="10%"/>
			<col width="3%"/>
			<col width="21%"/>
			<col width="25%"/>
			<col width="10%"/>
			<col width="23%"/>
			<col width="2%"/>
			<col width="2%"/>
		</colgroup>
		<xsl:variable name="rowspan" select="1+count(descendant::files/item)"/>
		  <td align="right" rowspan="{$rowspan}">
			<xsl:choose>
				<xsl:when test="$rowspan=1">
	  				<a title="Afficher fichiers" href="expandTrashInstance.m?expand=true&amp;patPk={../../../../../../pk}&amp;studyPk={../../../../pk}&amp;seriesPk={../../pk}&amp;instancePk={pk}">
						<img src="images/plus.gif" border="0" alt="+"/>
              		                                            </a>				
				</xsl:when>
				<xsl:otherwise>
	  			        <a title="Cacher Instances" href="expandTrashInstance.m?expand=false&amp;patPk={../../../../../../pk}&amp;studyPk={../../../../pk}&amp;seriesPk={../../pk}&amp;instancePk={pk}">
					<img src="images/minus.gif" border="0" alt="-"/>
                                                                              </a>				
				</xsl:otherwise>
			</xsl:choose>
                                </td>
		<td title="Date/Heure Contenu" >
			<xsl:value-of select="contentDateTime"/>&#160;
		</td>
		<td title="Numéro d'Instance" >
	   		<xsl:value-of select="instanceNumber"/>&#160;
	    </td>
	    <td title="Type Wave" >
			<xsl:value-of select="waveformType"/>&#160;
		</td>
	    <td title="dummy" >&#160;&#160;</td>
 	  	<td title="Retrieve AETs" >
			<xsl:value-of select="retrieveAETs"/>&#160;
    	</td>
	  	<td title="SopIUID" >
			<xsl:value-of select="sopIUID"/>&#160;
    	</td>
        <td class="instance_mark" align="right">
			<a href="trashfolder.m?undel=instance&amp;patPk={../../../../../../pk}&amp;studyPk={../../../../pk}&amp;seriesPk={../../pk}&amp;instancePk={pk}"
				onclick="return confirm('Restaurer cette instance ?')">
				<img src="images/undel.gif" alt="Undelete Instance" border="0" title="Restaurer Instance"/>		
			</a>
        </td>
		<td class="instance_mark" align="right" >
			<input type="checkbox" name="stickyInst" value="{pk}">
				<xsl:if test="/model/stickyInstances/item = pk">
					<xsl:attribute name="checked"/>
				</xsl:if>
			</input>
		</td>
      </table>
	</tr>
		<xsl:apply-templates select="files/item">
			<xsl:sort data-type="number" order="descending" select="pk"/>
		</xsl:apply-templates>
</xsl:template>

<xsl:template match="item[@type='org.dcm4chex.archive.ejb.interfaces.FileDTO']">
<xsl:variable name="line_name">
	<xsl:choose>
		<xsl:when test="fileStatus &lt; 0">error_line</xsl:when>
		<xsl:otherwise>file_line</xsl:otherwise>
	</xsl:choose>
</xsl:variable>
	<tr>
<table class="{$line_name}" width="100%" cellpadding="0" cellspacing="0" border="0">
		<colgroup>
			<col width="5%"/>
			<col width="10%"/>
			<col width="10%"/>
			<col width="10%"/>
			<col width="10%"/>
			<col width="35%"/>
			<col width="20%"/>
		</colgroup>
                             <td>&#160;</td>
		<td title="fileTSUID">
			<xsl:value-of select="fileTsuid"/>&#160;
		</td>
		<td title="retrieveAET">
	   		<xsl:value-of select="retrieveAET"/>&#160;
                            </td>
                            <td title="Status">
                                        <xsl:choose>
                                            <xsl:when test="fileStatus=0">OK</xsl:when>
                                            <xsl:when test="fileStatus=1">à archiver</xsl:when>
                                            <xsl:when test="fileStatus=2">archivé</xsl:when>
                                            <xsl:when test="fileStatus=-1">erreur compression</xsl:when>
                                            <xsl:when test="fileStatus=-2">erreur vérification compression</xsl:when>
                                            <xsl:when test="fileStatus=-3">erreur MD5 check</xsl:when>
                                            <xsl:when test="fileStatus=-3">erreur requête HSM</xsl:when>
                                            <xsl:otherwise>inconnu(<xsl:value-of select="fileStatus"/>)</xsl:otherwise>
                                        </xsl:choose>&#160;
                            </td>
                            <td title="Taille">
	    	               <xsl:value-of select="fileSize"/> octets&#160;
                            </td>
		<td title="Chemin">
			<xsl:value-of select="directoryPath"/>/<xsl:value-of select="filePath"/>&#160;
		</td>
	  	<td title="MD5">
			<xsl:value-of select="md5String"/>
    	                </td>
      </table>
	</tr>
</xsl:template>
    
</xsl:stylesheet>
