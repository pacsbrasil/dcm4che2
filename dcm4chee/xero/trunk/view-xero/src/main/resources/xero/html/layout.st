<!-- Creates a layout box, to nest child elements in as appropriate -->
<div class="layout" $html/style()$>
$layout.layouts:{layout|$if(layout.used)$$(layout.format)()$$endif$}$
</div>