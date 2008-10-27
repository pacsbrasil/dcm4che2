$if(layout.action)$
$html/actionItem()$
$elseif(layout.href)$
$html/aItem()$
$else$
$html/labelItem()$
$endif$