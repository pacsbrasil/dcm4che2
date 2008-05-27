<div id="innerBody" style="width:100%; height: 100%;"  xmlns="http://www.w3.org/1999/xhtml" xmlns:xlink="http://www.w3.org/1999/xlink">
$if(layouts)$
$layouts:{layout|$(layout.format)()$}$
$elseif(layouts)$

<!-- List templates here that are required but are listed indirectly. -->
$html/displayIt()$
$html/displayFirst()$

$image/patientNameSel()$
$image/studyDescriptionSel()$
$image/patientMenu()$
$image/image()$
$image/imageArea()$
$image/studyTray()$
$image/seriesToolbar()$
$image/layoutMenu()$
$html/nestedRow()$
$html/layout()$

$query/query()$
$search/studyTable()$

$else$
No layouts found.
$endif$
</div>