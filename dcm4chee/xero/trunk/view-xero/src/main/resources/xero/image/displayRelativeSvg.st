$layout.image.use:{
$if(it.isDISPLAYREL)$<svg:use id="$it.id$" xlink:href="$it.href$" />$endif$
}$
$if(layout.image.keyObjects)$
	<svg:image id="ko$layout.id$" xlink:href="$i18n.KeyObjectsIcon$" width="35" height="35" x="965" y="7"/>
	$layout.image.keyObjects:{
		$it.contentLabel$
		$it.keyText:{<p>$it;format="xml"$</p>}$
	}$
$endif$