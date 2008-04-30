$if(layouts)$
$layouts:{layout|$(layout.format)()$}$
$elseif(layouts)$

<!-- List templates here that are required but are listed indirectly. -->
$html/displayIt()$
$html/displayFirst()$

$image/patientNameSel()$
$image/studyDescriptionSel()$
$image/menu()$
$image/image()$
$image/studyTray()$
$image/seriesToolbar()$

$html/nestedRow()$
$html/layout()$

$query/query()$
$search/studyTable()$

$else$
No layouts found.
$endif$