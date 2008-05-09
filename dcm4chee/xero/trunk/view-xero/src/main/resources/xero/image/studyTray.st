<!-- This is the per-study control containing the series thumbnails and controls within this study. -->
<div class="studyTray" $html/style()$>
$if(layout.study)$
	$layout.study.StudyDescription$ <br />
	Study ID: $layout.study.StudyID$ <br />
	Acc#: $layout.study.AccessionNumber$ <br />
	Next Prev Series<br />
	<br />
	Reports <br />
	Key Image Sets <br />
	Presentation State <br />
	Series <br />
	  Key Images <br />
	  Other Series <br />
$else$
No Study
$endif$
</div>