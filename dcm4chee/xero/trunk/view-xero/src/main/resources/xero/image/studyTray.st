<!-- This is the per-study control containing the series thumbnails and controls within this study. -->
<div class="studyTray" $html/style()$>
$if(layout.study)$
	$layout.study.StudyDescription$ <br />
	Study ID: $layout.study.StudyID$ <br />
	Acc#: $layout.study.AccessionNumber$ <br />
	$html/link(it="Prev",onclick="xeroController.navSer('"+layout.id+"',-1)")$ $html/link(it="Next",onclick="xeroController.navSer('"+layout.id+"',1)")$ Series<br />
	<br />
	Reports <br />
	Key Image Sets <br />
	$series/presentation()$ <br />
	$series/thumbnails()$<br />
$else$
No Study
$endif$
</div>