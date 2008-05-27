<div class="menu" $html/style()$>
	<ul>
	$if(layout.patient)$
	<li>$layout.patient.PatientName$</li>
	<li>id:$layout.patient.PatientID$</li>
	
	$image/wlmenu()$
	
	$html/nestedMenu(baseItem="Layouts", childItems=image/layoutMenu())$
	$else$
	No Patient
	$endif$
	<li>$html/link(onclick="window.xeroController.displayQuery(this)",href="nojs?action=displayQuery",it="Query")$</li>
	</ul>
</div>