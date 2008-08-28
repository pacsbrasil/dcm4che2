<!-- Parameters rows, cols, nest where nest is the child nesting to use -->
$if(layout.search.patient)$
$html/nestedTable(rows=layout.search.patient,cols=layout.columns,nested="study",id="searchTable")$
$endif$