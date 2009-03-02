$if(layout.image.keyObjects)$
<div class="menu" style="position: absolute; width: 22px; height: 22px; top: 5px; right: 5px;"><ul class="rightmenu">
    <li><a href="#" onclick="return cancelEvent();"><img class="icon" style="background:none;" src="$i18n.KeyObjectsIcon$?v=$version$" />
    $if(IS_IE)$<table cellspacing="0" cellpadding="0" border="0"><tr><td>$else$</a>$endif$
	<ul>
	$layout.image.keyObjects:{
		<li><a class="koTitle">$it.contentLabel$</a></li>
		$it.keyText:{<li class="koText">$it;format="xml"$</li>}$
	}$
	</ul>
	$if(IS_IE)$</td></tr></table></a>$endif$
	</li></ul>
</div>
$endif$