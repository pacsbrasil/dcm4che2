<div class="seriesToolbar" $html/style()$>
$html/link(it="&lt;&lt;",onclick="xeroController.navImg('"+layout.id+"',-16)")$
$html/link(it="&lt;Prev",onclick="xeroController.navImg('"+layout.id+"',-1)")$ 
$layout.imageArea.layouts.("0").image.Position$ of $layout.series.Viewable$ 
$html/link(it="Next&gt;",onclick="xeroController.navImg('"+layout.id+"',1)")$
$html/link(it="&gt;&gt;",onclick="xeroController.navImg('"+layout.id+"',16)")$
<img src="icon/layout1x1.png" class="icon" onclick="xeroController.setImgLay('$layout.id$',1,1)"/>
</div>