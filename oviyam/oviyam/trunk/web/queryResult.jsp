<!--
/* ***** BEGIN LICENSE BLOCK *****
* Version: MPL 1.1/GPL 2.0/LGPL 2.1
*
* The contents of this file are subject to the Mozilla Public License Version
* 1.1 (the "License"); you may not use this file except in compliance with
* the License. You may obtain a copy of the License at
* http://www.mozilla.org/MPL/
*
* Software distributed under the License is distributed on an "AS IS" basis,
* WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
* for the specific language governing rights and limitations under the
* License.
*
* The Original Code is part of Oviyam, an web viewer for DICOM(TM) images
* hosted at http://skshospital.net/pacs/webviewer/oviyam_0.6-src.zip
*
* The Initial Developer of the Original Code is
* Raster Images
* Portions created by the Initial Developer are Copyright (C) 2014
* the Initial Developer. All Rights Reserved.
*
* Contributor(s):
* Babu Hussain A
* Devishree V
* Meer Asgar Hussain B
* Prakash J
* Suresh V
*
* Alternatively, the contents of this file may be used under the terms of
* either the GNU General Public License Version 2 or later (the "GPL"), or
* the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
* in which case the provisions of the GPL or the LGPL are applicable instead
* of those above. If you wish to allow use of your version of this file only
* under the terms of either the GPL or the LGPL, and not to allow others to
* use your version of this file under the terms of the MPL, indicate your
* decision by deleting the provisions above and replace them with the notice
* and other provisions required by the GPL or the LGPL. If you do not delete
* the provisions above, a recipient may use your version of this file under
* the terms of any one of the MPL, the GPL or the LGPL.
*
* ***** END LICENSE BLOCK ***** */
-->

<%@page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@page isELIgnored="false"%>
<%@taglib prefix="pat" uri="/WEB-INF/tags/PatientInfo.tld"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>


<%
    String patName = request.getParameter("patientName");
    String tabName = request.getParameter("tabName");

    if(patName != null) {
        patName = new String(patName.getBytes("ISO-8859-1"), "UTF-8");
    }
%>

<fmt:setBundle basename="resources.i18n.Messages" var="lang" />

<html>
<head>

<script type="text/javascript" src="js/lib/jquery.dataTables.min.js"></script>

<script type="text/javascript">
            var dTable;
            $(document).ready(function() {
                var tableName = '#<%=tabName%>_table';                
                dTable = $(tableName).dataTable({
                    "bJQueryUI": true,
                    //"sPaginationType": "full_numbers",
                    "bPaginate": false,
                    //"bFilter": false,
                    "oLanguage": {
                        "sSearch": "Filter:"
                    },
                    "sScrollY": "87%",
                    "bScrollCollapse": true,
                    "bAutoWidth": true,
                    "sScrollX": "100%",
                    //"sScrollXInner": "100%",
                    "aaSorting": [[ 5, "desc" ]],
                    "aoColumnDefs": [ {
                            "aTargets": [0],
                            "fnCreatedCell": function (nTd, sData, oData, iRow, iCol) {
                                if ( sData.indexOf('img') >= 0) {
                                    $(nTd).css('padding', '0px');
                                    $(nTd).css('text-align', 'center');
                                }
                            }
                        }],
                    "aoColumns": [ null, null, null, null, null, null, null, null, null, {"bVisible": false}, {"bVisible": false}, {"bVisible": false}, {"bVisible": false}]
                });

                $.fn.dataTableInstances[<%=request.getParameter("tabIndex")%>] = dTable;               

                if(<%=request.getParameter("search")%>!=null && !<%=request.getParameter("search")%>) { //For Direct launch
                	$('#searchToggler').hide();
                	$('#Toggler').css('top','0.5%');
                }                 
            });
            
            function toggleDivider(divider) {
                var westPane = $('#<%=tabName%>_westPane');
            	
                if($(westPane).is(":visible")) {
                	$(westPane).html('');
                    $(westPane).hide();
                    $(divider).next().css('left','26px');
                   // $(divider).css('left','2px');
                    $('#Toggler').next().css('width', '100%');
                   $('#Toggler').css('left','0%');
                    $(divider).attr('title', 'Show Preview');
                    $(divider).css('left','2px');   
					if($('#<%=tabName%>_search').is(":visible")) {
						$(divider).css('background','url("images/showall.png")');
						$(divider).next().css('background','url("images/hideall.png")');
					} else {
						$(divider).css('background','url("images/hidesearch.png")');
						$(divider).next().css('background','url("images/hidewest.png")');
					}
                } else {
	                loadWest();
                    $(westPane).show();
                    $('#Toggler').next().css('width', '82%');
                    $(divider).attr('title', 'Hide Preview');
                    $('#Toggler').css('left','19%');
                    if($('#<%=tabName%>_search').is(":visible")) {
						$(divider).css('background','url("images/hidewest.png")');
						$(divider).next().css('background','url("images/hidesearch.png")');
					} else {
						$(divider).css('background','url("images/hideall.png")');
						$(divider).next().css('background','url("images/showall.png")');
					}                   
                }
                dTable.fnAdjustColumnSizing();
            }
            
            function toggleSearch(divider) {            	
            	var searchPane = $('#<%=tabName%>_search');
            	var tabContent = $('#<%=tabName%>_content');

            	
            	if($(searchPane).is(":visible")) {
            		$(searchPane).hide();
            		$(tabContent).css('height','100%');
            		//$(divider).css('top','0.5%');
            		//$(divider).prev().css('top','0.5%');
            		 $('#Toggler').css('top','1%');
            		$(divider).attr('title','Show Search');

            		if($('#<%=tabName%>_westPane').is(":visible")) {
						$(divider).css('background','url("images/showall.png")');
						$(divider).prev().css('background','url("images/hideall.png")');
					} else {
						$(divider).css('background','url("images/hidewest.png")');
						$(divider).prev().css('background','url("images/hidesearch.png")');
					}
            	} else {
            		$(searchPane).show();
            		$(divider).attr('title','Hide Search');
            		$(tabContent).css('height','85%');
            		 $('#Toggler').css('top','13.5%');
            		//$(divider).css('top','13.5%');
            		//$(divider).prev().css('top','13.5%');

            		if($('#<%=tabName%>_westPane').is(":visible")) {
						$(divider).css('background','url("images/hidesearch.png")');
						$(divider).prev().css('background','url("images/hidewest.png")');
					} else {
						$(divider).css('background','url("images/hideall.png")');
						$(divider).prev().css('background','url("images/showall.png")');
					}
            	}            	            	
            }        
            
            function positionDividers(westDivider) {
            	if($('#<%=tabName%>_search').is(":visible")) {
					$(westDivider).css('top','13.5%');
					$(westDivider).next().css('top','13.5%');
            	} else {
					$(westDivider).css('top','0%');
					$(westDivider).next().css('top','0%');
            	}
            	
            	if(!showWest) {
	            	$('#<%=tabName%>_westPane').hide();
					$(westDivider).next().css('left','42px');
                    $(westDivider).next().next().css('width', '100%');
                    $(westDivider).attr('title', 'Open');
                    $(westDivider).css('left','2px'); 
                    dTable.fnAdjustColumnSizing();
            	}           	
            }
            
            function loadWest() {
            	var selected = $(dTable.find('.row_selected'));
            	if(selected.length>0) {
		        	var iPos = dTable.fnGetData($(dTable.find('.row_selected')).get(0));
				    if( iPos == null ) {
						return;
				    }  
				    if(document.getElementById(iPos[9]).style.visibility == 'hidden') {
		            	showWestPane(iPos);
			        } else {
			            if(!!(window.requestFileSystem || window.webkitRequestFileSystem)) {
			                viewWPSeries(this);
			            } else {
			                showWestPane(iPos);
			            }
    	        	}    
		        }            
           	}
        </script>
</head>
<body>
	<c:choose>
	<c:when test="${param.preview=='true'}">
		<div id="<%=tabName%>_westPane"
			style="width: 18%; height: 97%; visibility: visible; display: block; z-index: 0; float: left;"></div>	

		<div id="Toggler" style="position: absolute; top: 13.5%; left: 19%; z-index: 3;">

		<div id="westToggler" title="Hide Preview" class="ui-state-default"
			onmouseover="this.className='ui-state-hover'"
			onmouseout="this.className='ui-state-default'"
			style="width: 24px; height: 24px; cursor: pointer; float: left; z-index: 3; background: url('images/hidewest.png'); border: none;"
			onclick="this.className='ui-state-default';toggleDivider(this);"></div>

		<div id ="searchToggler" title="Hide Search" class="ui-state-default toggler"
			onmouseover="this.className='ui-state-hover'"
			onmouseout="this.className='ui-state-default'"
			style="width: 24px; height: 24px; cursor: pointer; float: left; z-index: 3; background: url('images/hidesearch.png'); border: none;"
			onclick="this.className='ui-state-default'; toggleSearch(this);"></div>
		</div>	

		<div style="float: left; width: 82%; height: 97%; padding: 0px'">
	</c:when>
	
	<c:otherwise>
		<div style="float: left; width: 100%; height: 97%; padding: 0px'">
	</c:otherwise>
	</c:choose>

		<table class="display" id="<%=tabName%>_table" style="font-size: 12px;">

			<thead>
				<tr>
					<th></th>
					<th><fmt:message key='patientID' bundle="${lang}" /></th>
					<th><fmt:message key='patientName' bundle="${lang}" /></th>
					<th><fmt:message key='dateOfBirth' bundle="${lang}" /></th>
					<th><fmt:message key='accessionNumber' bundle="${lang}" /></th>
					<th><fmt:message key='studyDate' bundle="${lang}" /></th>
					<th><fmt:message key='studyDescription' bundle="${lang}" /></th>
					<th>Modality</th>
					<th><fmt:message key="instanceCount" bundle="${lang}" /></th>
					<th>Study Instance UID</th>
					<th>Refer Physician</th>
					<th>Series Count</th>
					<th>Gender</th>
				</tr>
			</thead>
			<tbody>
				<pat:Patient patientId="${param.patientId}"
					patientName="<%=patName%>" birthDate="${param.birthDate}"
					modality="${param.modality}" from="${param.from}" to="${param.to}"
					searchDays="${param.searchDays}"
					accessionNumber="${param.accessionNumber}"
					referPhysician="${param.referPhysician}"
					studyDescription="${param.studyDesc}" dcmURL="${param.dcmURL}"
					fromTime="${param.fromTime}" toTime="${param.toTime}">
					<tr>
						<td><img src="images/details_open.png" alt="" /> <img
							src="images/green.png" style="visibility: hidden"
							id="${studyIUID}" alt="" /></td>
						<td>${patientId}</td>
						<td>${patientName}</td>
						<td>${birthDate}</td>
						<td>${accessionNumber}</td>
						<td>${studyDate}</td>
						<td>${studyDescription}</td>
						<td>${modality}</td>
						<td>${totalInstances}</td>
						<td>${studyIUID}</td>
						<td>${referPhysician}</td>
						<td>${totalSeries}</td>
						<td>${patientGender}</td>
					</tr>
				</pat:Patient>
			</tbody>
		</table>
	</div>
</body>
</html>