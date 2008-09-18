 $if(IS_IE)$
 <div class="image"  $html/style()$>
   <v:group 
     style="width: $layout.dblColsDisp$px; height:$layout.dblRowsDisp$px; left: $layout.translateXIE$px; top: $layout.translateYIE$px; $if(layout.rotation)$rotation:$layout.rotation$;$endif$ $if(layout.flip)$flip:x;$endif$"
   	 coordorigin="-$layout.image.Columns$,-$layout.image.Rows$" 
   	 coordsize="$layout.dblCols$,$layout.dblRows$" 
   >
$layout.imageUrls:{
		<v:image id="$it.imageId$" src="$it.imageUrl$" style="width: $it.cols$px; height:$it.rows$px; left: $it.left$px;top:$it.top$;" />
}$
		<v:line from="0,0" to="$layout.image.Columns$,0" strokecolor="#202020" strokeweight="2"/>
		<v:line from="0,0" to="0,$layout.image.Rows$"  strokecolor="#202020" strokeweight="2"/>
		<v:line from="$layout.image.Columns$,0" to="$layout.image.Columns$,$layout.image.Rows$" strokecolor="#202020" strokeweight="2"/>
		<v:line from="0,$layout.image.Rows$" to="$layout.image.Columns$,$layout.image.Rows$" strokecolor="#202020" strokeweight="2" />
		
		$image/imageRelativeVml()$
		
   </v:group>

   <v:group class="imageDisplayRel" style="width: 100%; height: 100%; top: 0px; left: 0px;" coordsize="1000,1000" coordorigin="0,0">
	 $image/displayRelativeVml()$
   </v:group>
 </div>
$else$
<div class="image" $html/style()$  xmlns="http://www.w3.org/1999/xhtml">
$if(layout.image)$
<svg id="svg$layout.id$" class="imageDisplay" onmousedown="xeroController.imageMouseDown(evt)" style="width:$layout.width$px; height: $layout.height$px;" width="$layout.width$" height="$layout.height$" xmlns:xlink="http://www.w3.org/1999/xlink" xmlns="http://www.w3.org/2000/svg">
<g id="gim$layout.id$" class="imageGroup" transform=" translate($layout.translateX$,$layout.translateY$) $if(layout.flip)$scale(-1,1)$endif$ $if(layout.rotation)$rotate($layout.rotation$)$endif$ scale($layout.scale$,$layout.scaleY$)">
$layout.imageUrls:{
<image id="$it.imageId$" preserveAspectRatio="none" xlink:href="$it.imageUrl$" width="$it.cols$" height="$it.rows$" transform="translate($it.left$,$it.top$)"/>
}$

<line x1="0" y1="0" x2="$layout.image.Columns$" y2="0" style="stroke: #202020; stroke-width: 4;" />
<line x1="0" y1="0" x2="0" y2="$layout.image.Rows$" style="stroke: #202020; stroke-width: 4;" />
<line x1="$layout.image.Columns$" y1="0" x2="$layout.image.Columns$" y2="$layout.image.Rows$" style="stroke: #202020; stroke-width: 4;" />
<line x1="0" y1="$layout.image.Rows$" x2="$layout.image.Columns$" y2="$layout.image.Rows$" style="stroke: #202020; stroke-width: 4;" />

$image/imageRelativeSvg()$

</g>
<svg class="imageDisplayRel" style="width: 100%; height: 100%;" preserveAspectRatio="none" viewBox="0,0,1000,1000">
$image/displayRelativeSvg()$
</svg>
</svg>
$else$
No Image
$endif$
</div>
$endif$