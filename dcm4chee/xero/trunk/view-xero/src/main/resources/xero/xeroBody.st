<div id="innerBody" style="width:100%; height: 100%;"  xmlns="http://www.w3.org/1999/xhtml" xmlns:xlink="http://www.w3.org/1999/xlink">
$if(layout.template)$
$(layout.template)()$
$elseif(layout.template)$

<!-- List templates here that are required but are listed indirectly - temporary until restart. -->

$else$
No layout found.
$endif$
</div>