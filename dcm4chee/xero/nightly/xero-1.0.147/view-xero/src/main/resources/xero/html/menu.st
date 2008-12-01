<div class="$if(layout.displayClass)$$layout.displayClass$ $endif$menu" $html/style()$>
	<ul>
$layout.layouts:{layout|$if(layout.used)$$(layout.template)()$$endif$}$
	</ul>
</div>