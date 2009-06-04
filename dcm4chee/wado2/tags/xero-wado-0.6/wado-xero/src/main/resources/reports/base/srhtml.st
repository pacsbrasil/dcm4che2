$! This template allows rendering of the HTML version of a report. !$
<html xmlns="http://www.w3.org/1999/xhtml" xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:svg="http://www.w3.org/2000/svg" xmlns:v="urn:schemas-microsoft-com:vml">
<head><title>$ds.ConceptNameCodeSequence.dicomObjects:{$it.CodeMeaning.value$} $</title></head>
<body>
<font size="-1">
By $ds.("0x80080").value$, Ref. Phys. $ds.("0x80090").value$</font><br />
<table border="0">
  <tr><td>Patient Name:</td><td>$ds.PatientName$</td></tr>
  <tr><td>Patient Id:</td><td>$ds.("0x00100020")$</td></tr>
  <tr><td>Patient Birthdate:</td><td>$ds.("0x100030")$</td></tr>
  <tr><td>Patient Sex:</td><td>$ds.("0x100040")$</td></tr>
</table>
<hr />
$ds.ContentSequence.dicomObjects:{
  $srcontentitem()$
  <br />
}$
</body>
</html>