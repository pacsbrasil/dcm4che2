<div style="float: left;"><table>

<tr><td>Patient Name</td><td><input type="text" name="PatientName" value="$query.PatientName$"/></td></tr>
<tr><td>Patient ID</td><td><input type="text" name="PatientID" value="$query.PatientID$"/></td></tr>

</table></div>

<div style="float: left;"><table>

<tr><td>Accession Number</td><td><input type="text" name="AccessionNumber" value="$query.AccessionNumber$"/></td></tr>

<tr><td>Study Date</td><td>
  <select name="StudyDateTime">
	$if(query.StudyDateTime)$<option selected="true" value="$query.StudyDateTime$">$query.StudyDateTime$</option>$endif$
	<option value=""> </option>
  	<option value="Today">Today</option>
  	<option value="SinceYesterday">Since Yesterday</option>
  	<option value="Yesterday">Yesterday</option>
  	<option value="Last7days">Last 7 Days</option>
  	<option value="Last4weeks">Last 4 Weeks</option>
  	<option value="ThisMonth">This Month</option>
  	<option value="LastMonth">Last Month</option>
  	<option value="ThisYear">This Year</option>
  	<option value="LastYear">Last Year</option>
  	<option value="SinceLastYear">Since Last Year</option>
  	<option value="Last10Years">Last 10 Years</option>
  </select>
</td></tr>

<tr><td>Modality</td><td>
  <select name="ModalitiesInStudy">
	$if(query.ModalitiesInStudy)$<option selected="true" value="$query.ModalitiesInStudy$">$query.ModalitiesInStudy$</option>$endif$
	<option value=""> </option>
	<option value="CR">CR</option>
	<option value="DX">DX</option>
	<option value="CT">CT</option>
	<option value="MG">Mammo</option>
	<option value="MR">MR</option>
	<option value="US">Ultrasound</option>
	<option value="NM">NM</option>

	<option value="XC">XC</option>
	<option value="XA">Angio</option>
	
	<option value="PX">Pano</option>
	<option value="IO">Dental</option>
	
	<option value="KO">Key</option>
	<option value="SR">Report</option>
	<option value="OT">Test</option>
	<option value="PR">Presentation/Markup</option>
  </select></td>
</tr>

</table></div>