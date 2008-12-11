$! This template allows rendering of the HTML version of a report. !$
<html xmlns="http://www.w3.org/1999/xhtml" xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:svg="http://www.w3.org/2000/svg" xmlns:v="urn:schemas-microsoft-com:vml">
<head>
  <title>$ds.ConceptNameCodeSequence.dicomObjects:{$it.CodeMeaning.value;format="xml"$}$</title>
<style type="text/css">
.style1 {
	font-size: 14px;
	font-weight: bold;
}
body,td,th {
	font-family: Verdana, Arial, Helvetica, sans-serif;
	font-size: 14px;
	color: #000000;
}
body {
	background-color: #CCCCCC;
	margin: 0;
	padding: 10px;
}

.contentItem {
	font-size: 15px;
	font-weight: bold;
}

</style>
  
</head>

<body>

<table border="0" cellpadding="0" cellspacing="0" width="100%">
    <tr>
    <td width="60%" valign="top"><p class="style1">
      $if(ds.("0x80080").value)$By $ds.("0x80080").name;format="xml"$,  <br />$endif$
      $if(ds.("0x80090").value)$Ref. Phys. $ds.("0x80090").name;format="xml"$ <br />$endif$
      Patient Name:$ds.PatientName.name;format="xml"$<br />
      Patient ID:$ds.("0x00100020")$<br />
      Patient Birthdate:$ds.("0x100030")$<br />
      Patient Sex:$ds.("0x100040")$<br />
      Accession Number:$ds.AccessionNumber$
    </p></td>
    <td width="40%" valign="top">
    	<img src="/agfa/icon/agfaLogo.gif" width="1388" height="511" style="width:138px; height:51px" />
    	<h2>McCormick Hospital</h2>
        <p>2301 S. Lake Shore Drive<br />
          Chicago, Illinois, USA, 60616<br />

    Ph. 312-791-7000</p></td>
  </tr>
</table>
  
<hr />
<h2>Findings:</h2>
$ds.ContentSequence.dicomObjects:{
  $srcontentitem()$
  <br />
}$
</body>
</html>