<!-- Defines the series tray items - currently just creates an unordered list of
  class seriesTray, with list items each containing a link to a given series. -->
<ul class="thumbnails">
$layout.study.series:{$if(it.image)$
	<li class="thumbnail">
		<span>$it.SeriesDescription$ | $it.Viewable$</span>
		<img onclick="xeroController.navToSer('$layout.id$', '$it.SeriesInstanceUID$')" height="256" src="/wado2/wado?objectUID=$it.image.("0").SOPInstanceUID$&amp;rows=256" />
	</li>
$endif$
}$
</ul>