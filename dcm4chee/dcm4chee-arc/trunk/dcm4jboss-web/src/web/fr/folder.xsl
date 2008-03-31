<?xml version="1.0" encoding="UTF-8"?>
<!--
 $Id: folder.xsl 5658 2008-01-09 11:29:56Z javawilli $
 -->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:output method="html" indent="yes" encoding="UTF-8"/>
<xsl:variable name="page_title">Liste des patients</xsl:variable>
<xsl:include href="page.xsl"/>
<xsl:include href="../modality_sel.xsl"/>

<!--
 	Enable/disable the patient folder operations to match the project requirements
 	TODO: Remove project specific hardcoded values
 -->
<xsl:param name="folder.export_tf" select="'false'"/>
<xsl:param name="folder.export_xds" select="'false'"/>
<xsl:param name="folder.xds_consumer" select="'false'"/>
<xsl:param name="folder.send" select="'false'"/>
<xsl:param name="folder.delete" select="'false'"/>
<xsl:param name="folder.edit" select="'false'"/>
<xsl:param name="folder.move" select="'false'"/>
<xsl:param name="folder.add_worklist" select="'false'"/>
<xsl:param name="folder.mergepat" select="'false'"/>
<xsl:param name="folder.study_permission" select="'false'"/>

<xsl:template match="model">
	<form action="foldersubmit.m" method="post" name="myForm" accept-charset="UTF-8" > 
 			<input type="hidden" name="form" value="true" />
 		  <table class="folder_header" border="0" cellspacing="0" cellpadding="0" width="100%">
			<td class="folder_header" valign="top">
				<table class="folder_header" border="0" height="30" cellspacing="0" cellpadding="0" width="100%">
					<td class="folder_header">
					  <div title="Afficher les patients sans examens">
  						<input type="checkbox" name="showWithoutStudies" value="true">
  							<xsl:if test="showWithoutStudies = 'true'">
  								<xsl:attribute name="checked"/>
  							</xsl:if>
  						</input>
 							<xsl:text>0 examens</xsl:text>
						</div>
					  <div title="Lister les examens d'un patient du plus récent au plus ancien">
  						<input type="checkbox" name="latestStudiesFirst" value="true">
  							<xsl:if test="latestStudiesFirst = 'true'">
  								<xsl:attribute name="checked"/>
  							</xsl:if>
  						</input>
 							<xsl:text>Derniers examens en premier</xsl:text>
						</div>
					</td>
					<td class="folder_header" align="center">
					<xsl:choose>
						<xsl:when test="total &lt; 1">
							Aucun examen trouvé!
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
							</b> examens correspondants.
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
						 	title="Résultats suivants">
							<xsl:if test="offset + limit &gt;= total">
								<xsl:attribute name="disabled">disabled</xsl:attribute>
							</xsl:if>
						</input>
					</td>
					<xsl:if test="$folder.edit='true'">
						<td class="folder_header" width="40">
							<a href="patientEdit.m?pk=-1">
								<img src="images/addpat.gif" alt="Add Patient" border="0" title="Ajouter un nouveau patient"/>		
							</a>
						</td>
					</xsl:if>
					<xsl:if test="$folder.mergepat='true'">
						<td class="folder_header" width="40">
							<input type="image" value="Merge" name="merge" src="images/merge.gif" alt="merge" border="0"
								title="Fusionner les patients sélectionnés" onclick="return validateChecks(this.form.stickyPat, 'Patient', 2)">
								<xsl:if test="total &lt;= 0">
									<xsl:attribute name="disabled">disabled</xsl:attribute>
								</xsl:if>
							</input>
						</td>
					</xsl:if>
					<xsl:if test="$folder.move='true'">
						<td class="folder_header" width="40">
							<input type="image" value="Move" name="move" src="images/move.gif" alt="move" border="0"
								title="Déplacer les entités sélectionnées">
								<xsl:if test="total &lt;= 0">
									<xsl:attribute name="disabled">disabled</xsl:attribute>
								</xsl:if>
							</input>
						</td>
					</xsl:if>
					<xsl:if test="$folder.export_tf='true'">
						<td class="folder_header" width="40">
							<input type="image" value="Export" name="exportTF" src="images/export_tf.gif" alt="TF Export" border="0"
								title="Exporter les instances sélectionnées vers le système de fichiers pédagogique">
								<xsl:if test="total &lt;= 0">
									<xsl:attribute name="disabled">disabled</xsl:attribute>
								</xsl:if>
							</input>
						</td>
					</xsl:if>
					<xsl:if test="$folder.export_xds='true'">
						<td class="folder_header" width="40">
							<input type="image" value="xdsi" name="exportXDSI" src="images/export_xdsi.gif" alt="XDSI Export" border="0"
								title="Exporter les isntances sélectionnées pour XDS-I">
								<xsl:if test="total &lt;= 0">
									<xsl:attribute name="disabled">disabled</xsl:attribute>
								</xsl:if>
							</input>
						</td>
					</xsl:if>
					<xsl:if test="$folder.delete='true'">
						<td class="folder_header" width="40">
							<input type="image" value="Del" name="del" src="images/trash.gif" alt="delete" border="0"
								title="Supprimer les entités sélectionnées"
								onclick="return confirm('Supprimer les entités selectionnées?')">
								<xsl:if test="total &lt;= 0">
									<xsl:attribute name="disabled">disabled</xsl:attribute>
								</xsl:if>
							</input>
						</td>
					</xsl:if>
					<xsl:if test="$folder.send='true'">
						<td class="folder_header" width="40">
							<input type="image" value="Send" name="send" src="images/send.gif" alt="send" border="0"
								title="Envoyer les entités sélectionnées vers la destination indiquée"
								onclick="return confirm(Envoyer les entités sélectionnées' ' + 
										document.myForm.destination.options[document.myForm.destination.selectedIndex ].text + '?')">
								<xsl:if test="total &lt;= 0">
									<xsl:attribute name="disabled">disabled</xsl:attribute>
								</xsl:if>
							</input>
						</td>
					</xsl:if>
					<td class="folder_header" width="50">
						<select size="1" name="destination" title="Destination d'envoi">
							<xsl:for-each select="aets/item">
								<xsl:sort data-type="text" order="ascending" select="title"/>
								<option>
									<xsl:if test="/model/destination = title">
										<xsl:attribute name="selected"/>
									</xsl:if>
									<xsl:value-of select="title"/>
									<xsl:if test="not(description='')">
										<xsl:text> (</xsl:text><xsl:value-of select="description"/><xsl:text>)</xsl:text>
									</xsl:if>
								</option>
							</xsl:for-each>						
						</select>
					</td>
					<td class="folder_header" width="5">
						<input type="checkbox" name="filterAET" value="true" title="Afficher seulement les examens provenant de l'AET sélectionnée">
							<xsl:if test="filterAET = 'true'">
								<xsl:attribute name="checked"/>
							</xsl:if>
						</input>
					</td>
					<td class="folder_header" width="5" title="Afficher seulement les examens provenant de l'AET sélectionnée">Filtre AET</td>
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
					<td title="ID. Patient format:patid[^^^emetteur]">
						<input size="10" name="patientID" type="text" title="ID. Patient format:patid[^^^emetteur]" 
								value="{patientID}"/>
					</td>
	
					<xsl:choose>
						<xsl:when test="showStudyIUID='true'">
				      		<td class="label">Examen IUID:</td>
				      		<td>
				        		<input size="45" name="studyUID" type="text" value="{studyUID}"/>
				      		</td>
						</xsl:when>
						<xsl:when test="showSeriesIUID='true'">
				      		<td class="label">Séries IUID:</td>
				      		<td>
				        		<input size="45" name="seriesUID" type="text" value="{seriesUID}"/>
				      		</td>
						</xsl:when>
						<xsl:otherwise>
							<td class="label">Examen ID:</td>
				      		<td>
				        		<input size="10" name="studyID" type="text" value="{studyID}"/>
				      		</td>
							<td class="label" title="Date d'examen. format:yyyy/mm/dd or intervalle:yyyy/mm/dd-yyyy/mm/dd">Date d'Examen:
							</td>
				      		<td> 
				        		    <input size="10" name="studyDateRange" type="text" value="{studyDateRange}"
				        		    title="Date d'examen. format:yyyy/mm/dd or intervalle:yyyy/mm/dd-yyyy/mm/dd" />
				      		    <input name="studyUID" type="hidden" value=""/>
				      		</td>
						</xsl:otherwise>
					</xsl:choose>

		      		<td class="label">Matricule:
							</td>
		      		<td>
		        		<input size="10" name="accessionNumber" type="text" value="{accessionNumber}"/>
		      		</td>
		      		<td class="label">Modalité:
							</td>
					<td>
						<xsl:call-template name="modalityList">
						    <xsl:with-param name="name">modality</xsl:with-param>
						    <xsl:with-param name="title">Modality</xsl:with-param>
						    <xsl:with-param name="selected" select="modality"/>
						</xsl:call-template>
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
			<col width="22%"/><!-- pat name -->
			<col width="10%"/><!-- pat id -->
			<col width="12%"/><!-- pat birthdate -->
			<col width="5%"/><!--  patient sex  -->
		    <col width="38%"/>
			<col width="8%"/><!-- xds, add, inspect, edit, sticky -->
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
			<col width="12%"/><!-- StudyID -->
			<col width="10%"/><!-- Modalities -->
			<col width="26%"/><!-- Study Desc -->
			<col width="9%"/><!-- Acc No --><!-- 73 -->
    		<col width="11%"/><!-- Ref. Physician -->
		    <col width="4%"/><!-- Study Status ID -->
			<col width="2%"/><!-- No. of Series -->
		    <col width="2%"/><!-- No. of Instances -->
			<col width="8%"/><!-- Webviewer, add, inspect, edit, sticky -->
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
					Mods:</font>
			</td>
			<td>
				<font size="1">
				<xsl:choose>
					<xsl:when test="showStudyIUID='false'">
							<b>Description de l'examen</b> / 
						<a title="Afficher le StudyIUID" href="foldersubmit.m?showStudyIUID=true&amp;studyID=">IUID</a>
					</xsl:when>
					<xsl:otherwise>
						<a title="Afficher les descriptions des examens" href="foldersubmit.m?showStudyIUID=false&amp;studyUID=">Description de l'examen</a>
							/ <b>IUID</b>
					</xsl:otherwise>
				</xsl:choose> :
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
				<font size="1">Status:</font>
			</td>
			<td align="right">
				<font size="1">NoS:</font>
			</td>
			<td align="right">
				<font size="1">NoI:</font>
			</td>
			<td>&#160;</td>
		</tr>
	</table>
	
	<table class="folder_overview" border="0" cellspacing="0" cellpadding="0" width="100%">
		<colgroup>
			<col width="5%"/><!-- left margin -->
			<col width="12%"/><!-- Date/Time -->
			<col width="12%"/><!-- Series No -->
			<col width="10%"/><!-- Modality -->
			<col width="35%"/><!-- Series Desc. -->
			<col width="10%"/><!-- Vendor/Model -->
    		<col width="6%"/><!-- PPS Status -->
    		<col width="2%"/><!-- NOI -->
			<col width="8%"/><!-- web viewer, edit, inspect, sticky -->
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
				<xsl:choose>
					<xsl:when test="showSeriesIUID='false'">
							<b>Description des séries/Partie du corps</b> / 
						<a title="Afficher les SeriesIUID" href="foldersubmit.m?showSeriesIUID=true">IUID</a>
					</xsl:when>
					<xsl:otherwise>
						<a title="Afficher la description" href="foldersubmit.m?showSeriesIUID=false">Description des séries/Partie du corps</a>
							/ <b>IUID</b>
					</xsl:otherwise>
				</xsl:choose> :
				</font>
			</td>
			<td>
				<font size="1">
					Distributeur/Modèle:</font>
			</td>
			<td>
				<font size="1">
					Status PPS:</font>
			</td>
			<td align="right">
				<font size="1">NoI:</font>
			</td>
			<td align="right">
				<img src="images/minus.gif" alt="Tout déselectionner" onclick="selectAll( document.myForm,'sticky', false)" />
				<img src="images/plus.gif" alt="Sélectionner tous les examens" onclick="selectAll( document.myForm,'stickyStudy', true)" />
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
			<col width="26%"/><!-- pat name -->
			<col width="10%"/><!-- pat id -->
			<col width="12%"/><!-- pat birthdate -->
			<col width="5%"/><!--  patient sex  -->
		    <col width="38%"/>
			<col width="8%"/><!-- xds, add, inspect, edit, sticky -->
		</colgroup>
		<xsl:variable name="rowspan" select="1+count(descendant::studies/item)"/>
			<td class="patient_mark" align="right" rowspan="{$rowspan}">
				<xsl:choose>
					<xsl:when test="showStudies='false'">
						<a title="Afficher les Examens" href="expandPat.m?patPk={pk}&amp;expand=true">
						<img src="images/plus.gif" border="0" alt="+"/>
              </a>				
					</xsl:when>
					<xsl:otherwise>
							<a title="Cacher les Examens" href="expandPat.m?patPk={pk}&amp;expand=false">							
							<img src="images/minus.gif" border="0" alt="-"/>
              </a>				
					</xsl:otherwise>
				</xsl:choose>
			</td>
      		<td title="Nom du Patient">
				<strong>
            		<xsl:value-of select="patientName"/>&#160;
				</strong>
      		</td>
      		<td title="ID Patient">
				<strong>
            		<xsl:value-of select="patientID"/>&#160;
				</strong>
			</td>
      		<td title="Date de Naissance">
				<strong>
            		<xsl:value-of select="patientBirthDate"/>&#160;
				</strong>
      		</td>
      		<td title="Sexe">
				<strong>
            		<xsl:value-of select="patientSex"/>&#160;
				</strong>
      		</td>
      		<td>&#160;</td>
			<td class="patient_mark" align="right" >
	      		<xsl:if test="$folder.xds_consumer='true'">
					<xsl:text>XDS</xsl:text>
					<xsl:choose>
						<xsl:when test="showXDS='false'">
							<a title="Afficher les Documents XDS" href="expandXDS.m?patPk={pk}&amp;expand=true">
								<img src="images/plus.gif" border="0" alt="+"/>
	              			</a>				
						</xsl:when>
						<xsl:otherwise>
							<a title="Cacher les Documents XDS" href="expandXDS.m?patPk={pk}&amp;expand=false">
								<img src="images/minus.gif" border="0" alt="+"/>
	              			</a>				
						</xsl:otherwise>
					</xsl:choose>
				</xsl:if>
			    <xsl:if test="$folder.edit='true'">
					<a href="studyEdit.m?patPk={pk}&amp;studyPk=-1">
						<img src="images/add.gif" alt="Add Study" border="0" title="Ajouter un nouvel Examen"/>		
					</a>
					<a href="patientEdit.m?pk={pk}">
						<img src="images/edit.gif" alt="Edit Patient" border="0" title="Editer les attributs Patient"/>		
					</a>
					<a href="inspectDicomHeader.m?patPk={pk}" target="dbAttrs">
						<img src="images/dbattrs.gif" alt="attrs" border="0" title="Afficher les attributs Patients dans la BD"/>		
					</a>
					<xsl:if test="$folder.study_permission='true'">
						<a href="studyPermission.m?patPk={pk}">
							<img src="images/permission.gif" alt="permissions" border="0" title="Afficher les Permissions de l'Examen pour le Patient"/>		
						</a>
					</xsl:if>
				</xsl:if>
				<input type="checkbox" name="stickyPat" value="{pk}">
					<xsl:if test="/model/stickyPatients/item = pk">
						<xsl:attribute name="checked"/>
					</xsl:if>
				</input>
			</td>
	  </table>
  </tr>
  <xsl:if test="showXDS='true'">
  	<xsl:call-template name="xds_documents"/>
  </xsl:if>	
  <xsl:variable name="studyOrder">
    <xsl:choose>
      <xsl:when test="/model/latestStudiesFirst = 'true'">descending</xsl:when>
      <xsl:otherwise>ascending</xsl:otherwise>
    </xsl:choose>
  </xsl:variable>
			<xsl:apply-templates select="studies/item">
				<xsl:sort data-type="text" order="{$studyOrder}" select="studyDateTime"/>
			</xsl:apply-templates>
</xsl:template>

<xsl:template match="item[@type='org.dcm4chex.archive.web.maverick.model.StudyModel']">
<tr>
	<table class="study_line" width="100%" cellpadding="0" cellspacing="0" border="0">
		<xsl:variable name="rowspan" select="1+count(descendant::series/item)"/>
		<colgroup>
			<col width="2%"/><!-- margin -->
			<col width="14%"/><!-- Date/time -->
			<col width="12%"/><!-- StudyID -->
			<col width="10%"/><!-- Modalities -->
			<col width="26%"/><!-- Study Desc -->
			<col width="9%"/><!-- Acc No --><!-- 73 -->
    		<col width="11%"/><!-- Ref. Physician -->
		    <col width="4%"/><!-- Study Status ID -->
			<col width="2%"/><!-- No. of Series -->
		    <col width="2%"/><!-- No. of Instances -->
			<col width="8%"/><!-- Webviewer, add, inspect, edit, sticky -->
		</colgroup>
			<td class="study_mark" align="right" rowspan="{$rowspan}">
				<xsl:choose>
					<xsl:when test="$rowspan=1">
						<a title="Afficher les Séries" href="expandStudy.m?patPk={../../pk}&amp;studyPk={pk}&amp;expand=true">
						    <img src="images/plus.gif" border="0" alt="+"/>
                                                                                        </a>				
					</xsl:when>
					<xsl:otherwise>
						<a title="Cacher les Séries" href="expandStudy.m?patPk={../../pk}&amp;studyPk={pk}&amp;expand=false">							
						    <img src="images/minus.gif" border="0" alt="-"/>
                                                                                        </a>				
					</xsl:otherwise>
				</xsl:choose>
			</td>
      		<td title="Date d'Examen">
				<xsl:value-of select="studyDateTime"/>&#160;
			</td>
			<td title="ID Examen (@Media)" >
				<xsl:value-of select="studyID"/>
				<xsl:if test="filesetId != '_NA_'"> @<xsl:value-of select="filesetId"/> </xsl:if>
				&#160;
			</td>
		 	<td title="Modalités">
				<xsl:value-of select="modalitiesInStudy"/>&#160;
			</td>
      		<td title="Description de l'Examen">
      			<xsl:choose>
					<xsl:when test="/model/showStudyIUID='false'">
						<xsl:value-of select="studyDescription"/>&#160;
					</xsl:when>
					<xsl:otherwise>
						<xsl:value-of select="studyIUID"/>&#160;
					</xsl:otherwise>
				</xsl:choose>
			</td>
			<td title="Matricule">
				&#160;<xsl:value-of select="accessionNumber"/>&#160;
			</td>
      		<td title="Médecin référant">
				<xsl:value-of select="referringPhysician"/>&#160;
			</td>
      		<td title="Status ID de l'Examen" align="center">
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
      		<td title="Nombre de Séries" align="center">
				<xsl:value-of select="numberOfSeries"/>&#160;
			</td>
      		<td title="Nombre d'Instances" align="center">
				<xsl:value-of select="numberOfInstances"/>&#160;
			</td>
		    <td class="study_mark" align="right">
			   <xsl:if test="/model/webViewer='true'">
    			          <xsl:choose>
    			                  <xsl:when test="modalitiesInStudy='SR'"><!-- no webviewer action for SR! -->
    			                  </xsl:when>
    			                  <xsl:when test="modalitiesInStudy='KO'"><!-- no webviewer action if study contains only KO ! -->
    			                  </xsl:when>
    			                  <xsl:otherwise>
    			                      <a href="/dcm4chee-webview/webviewer.jsp?studyUID={studyIUID}" >
										<xsl:attribute name="onclick" >return openWin('<xsl:value-of select="/model/webViewerWindowName" />','/dcm4chee-webview/webviewer.jsp?studyUID=<xsl:value-of select="studyIUID" />')</xsl:attribute>
										<img src="images/webview.gif" alt="View Study" border="0" title="Visualiser l'examen dans le Webviewer"/>
    			                      </a>
    			                  </xsl:otherwise>
    			          </xsl:choose>
			   </xsl:if>
	           <xsl:if test="$folder.edit='true'">    
					<xsl:if test="$folder.add_worklist='true'">
						<a href="addWorklist.m?studyPk={pk}">
							<img src="images/worklist.gif" alt="Add worklist item" border="0" title="Ajouter un élément worklist"/>		
						</a>
					</xsl:if>
					<a href="seriesEdit.m?patPk={../../pk}&amp;studyPk={pk}&amp;seriesPk=-1">
						<img src="images/add.gif" alt="Add Series" border="0" title="Ajouter une nouvelle série"/>		
					</a>
					<a href="studyEdit.m?patPk={../../pk}&amp;studyPk={pk}">
						<img src="images/edit.gif" alt="Edit Study" border="0" title="Editer les attributs de l'examen"/>		
					</a>
					<a href="inspectDicomHeader.m?studyPk={pk}" target="studyAtrrs">
						<img src="images/dbattrs.gif" alt="attrs" border="0" title="Afficher les attributs de l'Examen dans la BD"/>		
					</a>
					<xsl:if test="$folder.study_permission='true'">
						<a href="studyPermission.m?studyIUID={studyIUID}&amp;patPk={../../pk}">
							<img src="images/permission.gif" alt="permissions" border="0" title="Show Study Permissions"/>		
						</a>
					</xsl:if>
    	       	</xsl:if>
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
			<col width="14%"/><!-- Date/Time -->
			<col width="12%"/><!-- Series No -->
			<col width="10%"/><!-- Modality -->
			<col width="35%"/><!-- Series Desc. -->
			<col width="10%"/><!-- Vendor/Model -->
			
    		<col width="6%"/><!-- PPS Status -->
    		<col width="2%"/><!-- NOI -->
			<col width="8%"/><!-- web viewer, edit, inspect, sticky -->
		</colgroup>
		<xsl:variable name="rowspan" select="1+count(descendant::instances/item)"/>
		  <td class="series_mark" align="right" rowspan="{$rowspan}">
				<xsl:choose>
					<xsl:when test="$rowspan=1">
		  				<a title="Afficher les Instances" href="expandSeries.m?patPk={../../../../pk}&amp;studyPk={../../pk}&amp;seriesPk={pk}&amp;expand=true">
							<img src="images/plus.gif" border="0" alt="+"/>
              			</a>				
					</xsl:when>
					<xsl:otherwise>
		  			<a title="Cacher les Instances" href="expandSeries.m?patPk={../../../../pk}&amp;studyPk={../../pk}&amp;seriesPk={pk}&amp;expand=false">
						<img src="images/minus.gif" border="0" alt="-"/>
              </a>				
					</xsl:otherwise>
				</xsl:choose>
			</td>
			<td title="Date des séries">
				<xsl:value-of select="seriesDateTime"/>&#160;
			</td>
			<td title="Nombre de séries (@media)">
				<xsl:value-of select="seriesNumber"/>
				<xsl:if test="filesetId != '_NA_'"> @<xsl:value-of select="filesetId"/> </xsl:if>
				&#160;
			</td>
      <td title="Modalité">
				<xsl:value-of select="modality"/>&#160;
			</td>
      <td title="Description des Séries / Partie du corps">
      			<xsl:choose>
					<xsl:when test="/model/showSeriesIUID='false'">
						<xsl:value-of select="seriesDescription"/>
						\ <xsl:value-of select="bodyPartExamined"/>
					</xsl:when>
					<xsl:otherwise>
						<xsl:value-of select="seriesIUID"/>
					</xsl:otherwise>
				</xsl:choose>&#160;
    		
      	</td>
		<td title="Distributeur Modalité / Modèle">
    		<xsl:value-of select="manufacturer"/>
				\ <xsl:value-of select="manufacturerModelName"/>&#160;
      	</td>
		<td title="Status PPS"  >
			<xsl:choose>
				<xsl:when test="PPSStatus='DISCONTINUED'">
					<xsl:attribute name="style">color: red</xsl:attribute>
				</xsl:when>
				<xsl:when test="PPSStatus!=''">
					<xsl:attribute name="style">color: black</xsl:attribute>
				</xsl:when>
			</xsl:choose>
    		<xsl:value-of select="PPSStatus"/>&#160;
      	</td>
		<td title="Nombre d'instances" align="center">
			<xsl:value-of select="numberOfInstances"/>
		</td>
        <td class="series_mark" align="right">
           <xsl:if test="/model/webViewer='true'">
	            <xsl:choose>
	                <xsl:when test="modality != 'SR' and modality != 'PR' and modality != 'KO' and modality != 'AU' ">
	                      <a href="/dcm4chee-webview/webviewer.jsp?seriesUID={seriesIUID}" >
							<xsl:attribute name="onclick" >return openWin('<xsl:value-of select="/model/webViewerWindowName" />','/dcm4chee-webview/webviewer.jsp?seriesUID=<xsl:value-of select="seriesIUID" />')</xsl:attribute>
							<img src="images/webview.gif" alt="View Series" border="0" title="Afficher les séries dans un Webviewer"/>		
	                      </a>
	                </xsl:when>
	                <xsl:when test="modality = 'KO'">
	                      <a href="/dcm4chee-webview/webviewer.jsp?seriesUID={seriesIUID}" >
							<xsl:attribute name="onclick" >return openWin('<xsl:value-of select="/model/webViewerWindowName" />','/dcm4chee-webview/webviewer.jsp?seriesUID=<xsl:value-of select="seriesIUID" />')</xsl:attribute>
							<img src="images/webview_ko.gif" alt="View key object" border="0" title="Afficher le Key Object dans un Webviewer"/>		
						</a>
					</xsl:when>
	            </xsl:choose>
     	   </xsl:if>
           <xsl:if test="$folder.edit='true'">
					<a href="seriesEdit.m?patPk={../../../../pk}&amp;studyPk={../../pk}&amp;seriesPk={pk}">
						<img src="images/edit.gif" alt="Edit Series" border="0" title="Editer les Attributs de la Série"/>		
					</a>
					<a href="inspectDicomHeader.m?seriesPk={pk}" target="dbAttrs">
						<img src="images/dbattrs.gif" alt="attrs" border="0" title="Afficher les attributs BD de la série"/>		
					</a>
            </xsl:if>
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
	  				<a title="Afficher les fichiers" href="expandInstance.m?expand=true&amp;patPk={../../../../../../pk}&amp;studyPk={../../../../pk}&amp;seriesPk={../../pk}&amp;instancePk={pk}">
						<img src="images/plus.gif" border="0" alt="+"/>
              		                                            </a>				
				</xsl:when>
				<xsl:otherwise>
	  			        <a title="Cacher les Instances" href="expandInstance.m?expand=false&amp;patPk={../../../../../../pk}&amp;studyPk={../../../../pk}&amp;seriesPk={../../pk}&amp;instancePk={pk}">
					<img src="images/minus.gif" border="0" alt="-"/>
                                                                              </a>				
				</xsl:otherwise>
			</xsl:choose>
                                </td>

		<td title="Date du Contenu" >
			<xsl:value-of select="contentDateTime"/>&#160;
		</td>
		<td title="Numéro d'instance" >
	   		<xsl:value-of select="instanceNumber"/>&#160;
    </td>
    <td title="Type d'Image" >
			<xsl:value-of select="imageType"/>&#160;
		</td>
    <td title="Matrice Pixels" >
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
		<td class="instance_mark" align="right" >
			<xsl:choose>
				<xsl:when test="availability='ONLINE'" >			
					<a href="{/model/wadoBaseURL}wado?requestType=WADO&amp;studyUID={../../../../studyIUID}&amp;seriesUID={../../seriesIUID}&amp;objectUID={sopIUID}" target="imageview" >
						<img src="images/image.gif" alt="View image" border="0" title="Afficher l'image"/>		
					</a>
					<a href="inspectDicomHeader.m?instancePk={pk}" target="dbAttrs">
						<img src="images/dbattrs.gif" alt="attrs" border="0" title="Afficher les attributs BD de l'instance"/>		
					</a>
					<a href="{/model/wadoBaseURL}wado?requestType=WADO&amp;contentType=application/dicom%2Bxml&amp;studyUID={../../../../studyIUID}&amp;seriesUID={../../seriesIUID}&amp;objectUID={sopIUID}" target="_blank" >
						<img src="images/attrs.gif" alt="attrs" border="0" title="Afficher les attributs DICOM"/>		
					</a>
					<a href="{/model/wadoBaseURL}wado?requestType=WADO&amp;contentType=application/dicom&amp;studyUID={../../../../studyIUID}&amp;seriesUID={../../seriesIUID}&amp;objectUID={sopIUID}" target="_blank" >
						<img src="images/save.gif" alt="save" border="0" title="Sauvegarder l'objet DICOM"/>		
					</a>
				</xsl:when>
				<xsl:otherwise>
					<img src="images/invalid.gif" alt="Image not online" border="0" title="Image pas en ligne"/>		
				</xsl:otherwise>
			</xsl:choose>				
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
			<col width="13%"/>
			<col width="23%"/>
			<col width="2%"/>
			<col width="2%"/>
		</colgroup>
		<xsl:variable name="rowspan" select="1+count(descendant::files/item)"/>
		  <td align="right" rowspan="{$rowspan}">
			<xsl:choose>
				<xsl:when test="$rowspan=1">
	  				<a title="Afficher les Fichiers" href="expandInstance.m?expand=true&amp;patPk={../../../../../../pk}&amp;studyPk={../../../../pk}&amp;seriesPk={../../pk}&amp;instancePk={pk}">
						<img src="images/plus.gif" border="0" alt="+"/>
              		                                            </a>				
				</xsl:when>
				<xsl:otherwise>
	  			        <a title="Cacher les Instances" href="expandInstance.m?expand=false&amp;patPk={../../../../../../pk}&amp;studyPk={../../../../pk}&amp;seriesPk={../../pk}&amp;instancePk={pk}">
					<img src="images/minus.gif" border="0" alt="-"/>
                                                                              </a>				
				</xsl:otherwise>
			</xsl:choose>
                                </td>
		<td title="Date de Création" >
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
			<xsl:choose>
				<xsl:when test="availability='ONLINE'" >			
					<xsl:if test="/model/webViewer='true' and ../../modality='PR'" >
	                    <a href="/dcm4chee-webview/webviewer.jsp?prUID={sopIUID}" >
							<xsl:attribute name="onclick" >return openWin('<xsl:value-of select="/model/webViewerWindowName" />','/dcm4chee-webview/webviewer.jsp?prUID=<xsl:value-of select="sopIUID" />')</xsl:attribute>
							<img src="images/webview_pr.gif" alt="View Presentation State" border="0" title="Afficher les 'Presentation State' dans un Webviewer"/>		
						</a>
					</xsl:if>
					<a href="inspectDicomHeader.m?instancePk={pk}" target="dbAttrs">
						<img src="images/dbattrs.gif" alt="attrs" border="0" title="Afficher les Attributs BD de l'instance"/>		
					</a>
					<a href="{/model/wadoBaseURL}wado?requestType=WADO&amp;contentType=application/dicom%2Bxml&amp;studyUID={../../../../studyIUID}&amp;seriesUID={../../seriesIUID}&amp;objectUID={sopIUID}" target="_blank" >
						<img src="images/attrs.gif" alt="attrs" border="0" title="Afficher les attributs DICOM"/>		
					</a>
					<a href="{/model/wadoBaseURL}wado?requestType=WADO&amp;contentType=application/dicom&amp;studyUID={../../../../studyIUID}&amp;seriesUID={../../seriesIUID}&amp;objectUID={sopIUID}" target="_blank" >
						<img src="images/save.gif" alt="save" border="0" title="Sauvegarder l'objet DICOM"/>		
					</a>
				</xsl:when>
				<xsl:otherwise>
					<img src="images/invalid.gif" alt="Not online" border="0" title="Hors-Ligne"/>		
				</xsl:otherwise>
			</xsl:choose>
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
			<col width="16%"/>
			<col width="18"/>
			<col width="2%"/>
			<col width="2%"/>
			<col width="2%"/>
		</colgroup>
 
		<xsl:variable name="rowspan" select="1+count(descendant::files/item)"/>
		  <td align="right" rowspan="{$rowspan}">
			<xsl:choose>
				<xsl:when test="$rowspan=1">
	  				<a title="Afficher les fichiers" href="expandInstance.m?expand=true&amp;patPk={../../../../../../pk}&amp;studyPk={../../../../pk}&amp;seriesPk={../../pk}&amp;instancePk={pk}">
						<img src="images/plus.gif" border="0" alt="+"/>
              		                                            </a>				
				</xsl:when>
				<xsl:otherwise>
	  			        <a title="Cacher les Instances" href="expandInstance.m?expand=false&amp;patPk={../../../../../../pk}&amp;studyPk={../../../../pk}&amp;seriesPk={../../pk}&amp;instancePk={pk}">
					<img src="images/minus.gif" border="0" alt="-"/>
                                                                              </a>				
				</xsl:otherwise>
			</xsl:choose>
                                </td>
		<td title="Date du Contenu" >
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
		<xsl:choose>
			<xsl:when test="availability='ONLINE'" >			
				<td class="instance_mark" align="right" >
					<xsl:choose>
						<xsl:when test="/model/webViewer='true' and sopCUID='1.2.840.10008.5.1.4.1.1.88.59'" >
		                    <a href="/dcm4chee-webview/webviewer.jsp?instanceUID={sopIUID}" >
								<xsl:attribute name="onclick" >return openWin('<xsl:value-of select="/model/webViewerWindowName" />','/dcm4chee-webview/webviewer.jsp?instanceUID=<xsl:value-of select="sopIUID" />')</xsl:attribute>
								<img src="images/webview_ko.gif" alt="View Study" border="0" title="Afficher le Key Object dans un Webviewer"/>		
							</a>
						</xsl:when>
						<xsl:otherwise>
							&#160;
						</xsl:otherwise>
					</xsl:choose>				
				</td>
				<td class="instance_mark" align="right" >
					<a href="{/model/wadoBaseURL}IHERetrieveDocument?requestType=DOCUMENT&amp;documentUID={sopIUID}&amp;preferredContentType=application/pdf" target="SRview" >
						<img src="images/sr_pdf.gif" alt="View Report" border="0" title="Afficher le compte-rendu en PDF"/>		
					</a>
					<a href="{/model/wadoBaseURL}wado?requestType=WADO&amp;studyUID=0&amp;seriesUID=0&amp;objectUID={sopIUID}&amp;contentType=text/html" target="SRview" >
						<img src="images/sr.gif" alt="View Report" border="0" title="Afficher le compte-rendu en html"/>		
					</a>
					<a href="xdsiExport.m?docUID={sopIUID}" >
						<img src="images/xds.gif" alt="PDFtoXDS" border="0" title="Exporter PDF vers XDS Repository"/>		
					</a>
					<a href="inspectDicomHeader.m?instancePk={pk}" target="dbAttrs">
						<img src="images/dbattrs.gif" alt="attrs" border="0" title="Afficher les attributs DB de l'Instance"/>		
					</a>
					<a href="{/model/wadoBaseURL}wado?requestType=WADO&amp;contentType=application/dicom%2Bxml&amp;studyUID={../../../../studyIUID}&amp;seriesUID={../../seriesIUID}&amp;objectUID={sopIUID}" target="_blank" >
						<img src="images/attrs.gif" alt="attrs" border="0" title="Afficher les attributs DICOM"/>		
					</a>
					<a href="{/model/wadoBaseURL}wado?requestType=WADO&amp;contentType=application/dicom&amp;studyUID={../../../../studyIUID}&amp;seriesUID={../../seriesIUID}&amp;objectUID={sopIUID}" target="_blank" >
						<img src="images/save.gif" alt="save" border="0" title="Sauvegarder l'objet DICOM"/>		
					</a>
				</td>
			</xsl:when>
			<xsl:otherwise>
				<td class="instance_mark" align="right" >&#160;</td>
				<td class="instance_mark" align="right" >
					<img src="images/invalid.gif" alt="Report not online" border="0" title="Compte-rendu hors-ligne"/>		
				</td>
			</xsl:otherwise>
		</xsl:choose>				
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
			<col width="5%"/>
			<col width="10%"/>
			<col width="20%"/>
			<col width="2%"/>
		</colgroup>
		<xsl:variable name="rowspan" select="1+count(descendant::files/item)"/>
		  <td align="right" rowspan="{$rowspan}">
			<xsl:choose>
				<xsl:when test="$rowspan=1">
	  				<a title="Afficher les fichiers" href="expandInstance.m?expand=true&amp;patPk={../../../../../../pk}&amp;studyPk={../../../../pk}&amp;seriesPk={../../pk}&amp;instancePk={pk}">
						<img src="images/plus.gif" border="0" alt="+"/>
              		                                            </a>				
				</xsl:when>
				<xsl:otherwise>
	  			        <a title="Cacher les instances" href="expandInstance.m?expand=false&amp;patPk={../../../../../../pk}&amp;studyPk={../../../../pk}&amp;seriesPk={../../pk}&amp;instancePk={pk}">
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
	    <td title="Type de Son" >
			<xsl:value-of select="waveformType"/>&#160;
		</td>
	    <td title="dummy" >&#160;&#160;</td>
		<td title="dummy" >&#160;&#160;</td>
 	  	<td title="Retrieve AETs" >
			<xsl:value-of select="retrieveAETs"/>&#160;
    	</td>
	  	<td title="SopIUID" >
			<xsl:value-of select="sopIUID"/>&#160;
    	</td>
		<td class="instance_mark" align="right">
			<xsl:choose>
				<xsl:when test="availability='ONLINE'" >			
					<a href="{/model/wadoBaseURL}IHERetrieveDocument?requestType=DOCUMENT&amp;documentUID={sopIUID}&amp;preferredContentType=application/pdf" target="waveformview" >
						<img src="images/waveform.gif" alt="waveform" border="0" title="Ecouter l'audio"/>		
					</a>
					<a href="inspectDicomHeader.m?instancePk={pk}" target="dbAttrs">
						<img src="images/dbattrs.gif" alt="attrs" border="0" title="Afficher les attributs DB de l'instance"/>		
					</a>
					<a href="{/model/wadoBaseURL}wado?requestType=WADO&amp;contentType=application/dicom%2Bxml&amp;studyUID={../../../../studyIUID}&amp;seriesUID={../../seriesIUID}&amp;objectUID={sopIUID}" target="_blank" >
						<img src="images/attrs.gif" alt="attrs" border="0" title="Afficher les attributs DICOM"/>		
					</a>
					<a href="{/model/wadoBaseURL}wado?requestType=WADO&amp;contentType=application/dicom&amp;studyUID={../../../../studyIUID}&amp;seriesUID={../../seriesIUID}&amp;objectUID={sopIUID}" target="_blank" >
						<img src="images/save.gif" alt="save" border="0" title="Sauvegarder l'objet DICOM"/>		
					</a>
					<a href="xdsiExport.m?docUID={sopIUID}" >
						<img src="images/xds.gif" alt="PDFtoXDS" border="0" title="Exporter de PDF vers système XDS"/>		
					</a>
				</xsl:when>
				<xsl:otherwise>
					<img src="images/invalid.gif" alt="Image not online" border="0" title="Image hors-ligne"/>		
				</xsl:otherwise>
			</xsl:choose>				
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

<xsl:template match="item[@type='org.dcm4chex.archive.web.maverick.model.EncapsulatedModel']">
	<tr>
<table class="instance_line" width="100%" cellpadding="1" cellspacing="0" border="0">		 
		<colgroup>
			<col width="4%"/>
			<col width="15%"/>
			<col width="6%"/>
			<col width="15%"/>
			<col width="15%"/>
			<col width="5%"/>
			<col width="16%"/>
			<col width="18"/>
			<col width="4%"/>
			<col width="2%"/>
		</colgroup>
 
		<xsl:variable name="rowspan" select="1+count(descendant::files/item)"/>
		  <td align="right" rowspan="{$rowspan}">
			<xsl:choose>
				<xsl:when test="$rowspan=1">
	  				<a title="Afficher les fichiers" href="expandInstance.m?expand=true&amp;patPk={../../../../../../pk}&amp;studyPk={../../../../pk}&amp;seriesPk={../../pk}&amp;instancePk={pk}">
						<img src="images/plus.gif" border="0" alt="+"/>
              		                                            </a>				
				</xsl:when>
				<xsl:otherwise>
	  			        <a title="Cacher les Instances" href="expandInstance.m?expand=false&amp;patPk={../../../../../../pk}&amp;studyPk={../../../../pk}&amp;seriesPk={../../pk}&amp;instancePk={pk}">
					<img src="images/minus.gif" border="0" alt="-"/>
                                                                              </a>				
				</xsl:otherwise>
			</xsl:choose>
                                </td>
		<td title="Date/Heure du Contenu" >
    	                    <xsl:value-of select="contentDateTime"/>&#160;
		</td>
		<td title="Numéro d'instance" >
  		    <xsl:value-of select="instanceNumber"/>&#160;
    	</td>
    	<td title="Titre du Document" >
  			<xsl:value-of select="documentTitle"/>&#160;
		</td>
		<td title="Type MIME" >
      		<xsl:value-of select="mimeType"/>&#160;
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
		<xsl:choose>
			<xsl:when test="availability='ONLINE'" >			
				<td class="instance_mark" align="right" >
					<a href="{/model/wadoBaseURL}IHERetrieveDocument?requestType=DOCUMENT&amp;documentUID={sopIUID}&amp;preferredContentType={mimeType}" target="SRview" >
						<img src="images/sr_pdf.gif" alt="View Document" border="0" title="Afficher le Document"/>		
					</a>
					<a href="inspectDicomHeader.m?instancePk={pk}" target="dbAttrs">
						<img src="images/dbattrs.gif" alt="attrs" border="0" title="Afficher les attributs BD de l'Instance"/>		
					</a>
					<a href="{/model/wadoBaseURL}wado?requestType=WADO&amp;contentType=application/dicom%2Bxml&amp;studyUID={../../../../studyIUID}&amp;seriesUID={../../seriesIUID}&amp;objectUID={sopIUID}" target="_blank" >
						<img src="images/attrs.gif" alt="attrs" border="0" title="Afficher les attributs DICOM"/>		
					</a>
					<a href="{/model/wadoBaseURL}wado?requestType=WADO&amp;contentType=application/dicom&amp;studyUID={../../../../studyIUID}&amp;seriesUID={../../seriesIUID}&amp;objectUID={sopIUID}" target="_blank" >
						<img src="images/save.gif" alt="save" border="0" title="Sauvegarder l'objet DICOM"/>		
					</a>
				</td>
			</xsl:when>
			<xsl:otherwise>
				<td class="instance_mark" align="right" >
					<img src="images/invalid.gif" alt="Document not online" border="0" title="Document hors-ligne"/>		
				</td>
			</xsl:otherwise>
		</xsl:choose>				
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
                                            <xsl:when test="fileStatus=-2">verifier erreur compression</xsl:when>
                                            <xsl:when test="fileStatus=-3">erreur MD5</xsl:when>
                                            <xsl:when test="fileStatus=-3">erreur requête HSM</xsl:when>
                                            <xsl:otherwise>inconnu(<xsl:value-of select="fileStatus"/>)</xsl:otherwise>
                                        </xsl:choose>&#160;
                            </td>
                            <td title="Size">
	    	               <xsl:value-of select="fileSize"/> octets&#160;
                            </td>
		<td title="Chemin">
			<xsl:value-of select="directoryPath"/>/<xsl:value-of select="filePath"/>&#160;
		</td>
	  	<td title="MD5">
			<xsl:value-of select="md5String"/>
    	</td>
	  	<td title="XML">
	    	<xsl:if test="position()=1 and ../../availability='ONLINE'">
				<a href="{/model/wadoBaseURL}wado?requestType=WADO&amp;contentType=application/dicom%2Bxml&amp;useOrig=true&amp;studyUID=1&amp;seriesUID=1&amp;objectUID={../../sopIUID}" target="_blank" >
					<img src="images/attrs.gif" alt="XML" border="0" title="Afficher les attributs DICOM d'origine"/>
				</a>
				<a href="{/model/wadoBaseURL}wado?requestType=WADO&amp;contentType=application/dicom&amp;useOrig=true&amp;studyUID={../../../../../../studyIUID}&amp;seriesUID={../../../../seriesIUID}&amp;objectUID={../../sopIUID}" target="_blank" >
					<img src="images/save.gif" alt="save" border="0" title="Sauvegarder l'objet original DICOM (sans les mise-à-jour des entrées BD)"/>		
				</a>
			</xsl:if>
		</td>
      </table>
	</tr>
</xsl:template>



<xsl:template name="xds_documents">
	<tr>
		<table class="xds_docs" width="100%">
			<colgroup>
				<col width="2%"/>
				<col width="30%"/>
				<col width="10%"/>
				<col width="10%"/>
				<col width="13%"/>
				<col width="40%"/>
				<col width="5%"/>
			</colgroup>
			<tr>
		  		<td class="xds_docs_nav" title="Navigation du document XDS" colspan="7">
		  			Documents XDS:&#160;&#160;
					<a href="xdsQuery.m?queryType=findDocuments&amp;patPk={pk}" >
						<img src="images/search.gif" alt="XML" border="0" title="Chercher des documents XDS"/>		
					</a>
	    		</td>
	    	</tr>
			<tr>
		  		<td class="xds_doc_header" title="">
		  			&#160;
	    		</td>
		  		<td class="xds_doc_header" title="Titre du Document">
		  			Titre&#160;
	    		</td>
		  		<td class="xds_doc_header" title="Date de création du document">
		  			Date de création&#160;
	    		</td>
		  		<td class="xds_doc_header" title="Status du document">
		  			Status&#160;
	    		</td>
		  		<td class="xds_doc_header" title="Mime Type">
		  			MimeType&#160;
	    		</td>
		  		<td class="xds_doc_header" title="Document ID">
		  			Document ID&#160;
	    		</td>
		  		<td class="xds_doc_header" title="">
		  			&#160;
	    		</td>
	    	</tr>
			<xsl:apply-templates select="XDSDocuments/item" mode="xds"/>
	    </table>
	</tr>
</xsl:template>
  
<xsl:template match="item[@type='java.lang.String']" mode="xds">
	<tr>
	  	<td title="Document XDS">
			<xsl:value-of select="." />
    	</td>
	</tr>
</xsl:template>
<xsl:template match="item[@type='org.dcm4chex.archive.web.maverick.xdsi.XDSDocumentObject']" mode="xds">
	<tr>
	  	<td title="">
			&#160;
    	</td>
	  	<td title="Titre du Document">
			<xsl:value-of select="name" />
    	</td>
	  	<td title="Créé">
			<xsl:value-of select="creationTime" />
    	</td>
	  	<td title="Status du document">
			<xsl:value-of select="statusAsString" />
    	</td>
	  	<td title="Type MIME du Document">
			<xsl:value-of select="mimeType" />
    	</td>
	  	<td title="Document ID">
			<xsl:value-of select="id" />
    	</td>
    	<td>
    		<xsl:choose>
    			<xsl:when test="mimeType='application/dicom'">
    				<a href="showManifest.m?url={URL}&amp;documentID={id}" target="xdsManifest" >
						<img src="images/image.gif" alt="XML" border="0" title="Ouvrir le Manifeste XDSI"/>		
					</a>
					<xsl:if test="/model/webViewer='true'">
	                    <a href="/dcm4chee-webview/webviewer.jsp?manifestURL={URL}" >
							<xsl:attribute name="onclick" >return openWin('<xsl:value-of select="/model/webViewerWindowName" />','/dcm4chee-webview/webviewer.jsp?manifestURL=<xsl:value-of select="URL" />')</xsl:attribute>
							<img src="images/webview.gif" alt="View Manifest" border="0" title="Afficher le Manifeste dans dans un WebViewer"/>
	                    </a>
					</xsl:if>
    			</xsl:when>
    			<xsl:when test="mimeType='application/pdf'">
    				<a href="{URI}" target="xdsdoc" >
						<img src="images/sr_pdf.gif" alt="XML" border="0" title="Ouvrir le Document PDF"/>		
					</a>
    			</xsl:when>
    			<xsl:otherwise>
    				<a href="{URI}" target="xdsdoc" >
						<img src="images/sr.gif" alt="XML" border="0" title="Ouvrir le Document XDS"/>		
					</a>
    			</xsl:otherwise>
    		</xsl:choose>
    	</td>
	</tr>
</xsl:template>
    
</xsl:stylesheet>
