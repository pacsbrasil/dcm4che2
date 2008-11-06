$! Child list creates a list of child items from a list contained in a layout object !$
$layout.items:{
<li><a href="#" onclick="return $layout.action$('$layout.id$','$it.(layout.itemId);format="xml"$');">
	$if(layout.listTemplate)$$(layout.listTemplate)()$
	$else$$it.(layout.childItem)$
	$endif$
</a>
</li>
}$