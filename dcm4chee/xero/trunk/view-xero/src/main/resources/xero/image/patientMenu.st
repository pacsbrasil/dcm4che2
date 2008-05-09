<div class="menu" $html/style()$>
	$if(layout.patient)$
	$layout.patient.PatientName$ PID $layout.id$
	Current:    Prior:    
	WL Zoom Markup CINE
	$html/link(onclick="window.xeroController.displayQuery(this)",href="nojs?action=displayQuery",it="Query")$
	$else$
	No Patient
	$endif$
	
</div>