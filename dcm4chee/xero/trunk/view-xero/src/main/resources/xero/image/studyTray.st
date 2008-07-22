<!-- This is the per-study control containing the series thumbnails and controls within this study. -->
<div class="studyTray" $html/style()$>
$if(layout.study)$
	$layout.study.StudyDescription$ <br />
	Study ID: $layout.study.StudyID$ <br />
	Acc#: $layout.study.AccessionNumber$ <br />
	$html/link(it="Prev",onclick="xeroController.navSer('"+layout.id+"',-1)")$ $html/link(it="Next",onclick="xeroController.navSer('"+layout.id+"',1)")$
	<br />
	<a target="easyviz" href="http://192.190.75.101/easyviz/easyviz.php?patient_id=$layout.study.PatientID$">EasyViz</a>
	<a target="impax" href="AgfaHC://impax-client-epr?user=admin&amp;password=Open,ses&amp;domain=agfa%20healthcare&amp;patientid=$layout.study.PatientID$&amp;accession=$layout.study.AccessionNumber$">IMPAX 6</a><br />
	$series/reports()$
	$series/keyObjects()$
	$series/presentation()$
	$series/thumbnails()$
$else$
No Study
$endif$
</div>