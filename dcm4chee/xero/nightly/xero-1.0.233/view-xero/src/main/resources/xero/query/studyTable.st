<!-- Parameters rows, cols, nest where nest is the child nesting to use -->
$if(layout.search.patient)$
<div id="$layout.id$" class="studyTable">
$if(layout.search.tooManyResults)$
<span class="warning">Too many results found.</span>
$endif$
$html/nestedTable(rows=layout.search.patient,cols=layout.columns,nested="study",id=layout.id)$
</div>
$endif$