<div class="image" $html/style()$ xmlns="http://www.w3.org/1999/xhtml">
 $if(IS_IE)$
   <v:group id="vml$layout.id$" class="imageDisplay"
   	 onmousedown="controller.mouseDown('$layout.id$')"
$if(layout.sameSize)$
	style="overflow: visible; left: $layout.negHalfRealWidth$; margin-left: 50%; top: 50%; margin-top: $layout.negHalfRealHeight$; width: $layout.realWidth$; height: $layout.realHeight$;"
$else$
     style="overflow: visible; left: $layout.translateXPercent$%; top: $layout.translateYPercent$%; width: expression(Math.min(document.getElementById('$layout.id$').clientWidth,document.getElementById('$layout.id$').clientHeight*$layout.coordWidth$/$layout.coordHeight$));height: expression(Math.min(document.getElementById('$layout.id$').clientWidth*$layout.coordHeight$/$layout.coordWidth$,document.getElementById('$layout.id$').clientHeight));"
$endif$
     coordorigin="$layout.coordX$,$layout.coordY$" coordsize="$layout.coordWidth$,$layout.coordHeight$">
$if(layout.flipRotate)$
   <v:group style="top: -1; left: -1; width: 2; height: 2; overflow: visible;$if(layout.rotation)$rotation: $layout.rotation$;$endif$ $if(layout.flip)$flip:x;$endif$" coordorigin="-1,-1" coordsize="2,2">
$endif$
$if(layout.aspectPercent)$
   <v:group style="overflow: visible; width: 100; height: $layout.aspectPercent$;" coordsize="100,100">
$endif$
$layout.imageUrls:{
		<v:image id="$it.imageId$" src="$it.imageUrl$" style="width: $it.cols$px; height:$it.rows$px; left: $it.left$px;top:$it.top$;" />
}$
		<v:line from="0,0" to="$layout.image.Columns$,0" strokecolor="#202020" strokeweight="2"/>
		<v:line from="0,0" to="0,$layout.image.Rows$"  strokecolor="#202020" strokeweight="2"/>
		<v:line from="$layout.image.Columns$,0" to="$layout.image.Columns$,$layout.image.Rows$" strokecolor="#202020" strokeweight="2"/>
		<v:line from="0,$layout.image.Rows$" to="$layout.image.Columns$,$layout.image.Rows$" strokecolor="#202020" strokeweight="2" />
		
		$image/imageRelativeVml()$
	</v:group>
$if(layout.aspectPercent)$
	</v:group>
$endif$
$if(layout.flipRotate)$
	</v:group>
$endif$
   </v:group>

   <v:group class="imageDisplayRel" style="width: 100%; height: 100%; top: 0px; left: 0px;" coordsize="1000,1000" coordorigin="0,0">
	   $image/displayRelativeVml()$
   </v:group>
$else$
$if(layout.image)$
<svg id="svg$layout.id$" class="imageDisplay" onmousedown="controller.mouseDown('$layout.id$',evt)" xmlns:xlink="http://www.w3.org/1999/xlink" xmlns="http://www.w3.org/2000/svg">

<svg id="gim$layout.id$" 
	class="imageGroup" 
$if(layout.sameSize)$
	preserveAspectRatio="xMidYMid slice"
	x="$layout.negHalfRealWidth$" y="$layout.negHalfRealHeight$"
	style="width:100%; overflow: visible;">
	<svg x="50%" y="50%" width="$layout.realWidth$" height="$layout.realHeight$"
$else$
	preserveAspectRatio="xMidYMid meet" 
$endif$
	viewBox="$layout.coordX$ $layout.coordY$ $layout.coordWidth$ $layout.coordHeight$"
>
<g transform="$if(layout.flip)$scale(-1,1)$endif$ $if(layout.rotation)$rotate($layout.rotation$,0,0)$endif$ $if(layout.aspect)$ scale(1,$layout.aspect$)$endif$">
$layout.imageUrls:{
<image id="$it.imageId$" preserveAspectRatio="none" xlink:href="$it.imageUrl$" width="$it.cols$" height="$it.rows$" transform="translate($it.left$,$it.top$)"/>
}$

<line x1="0" y1="0" x2="$layout.image.Columns$" y2="0" style="stroke: #202020; stroke-width: 4;" />
<line x1="0" y1="0" x2="0" y2="$layout.image.Rows$" style="stroke: #202020; stroke-width: 4;" />
<line x1="$layout.image.Columns$" y1="0" x2="$layout.image.Columns$" y2="$layout.image.Rows$" style="stroke: #202020; stroke-width: 4;" />
<line x1="0" y1="$layout.image.Rows$" x2="$layout.image.Columns$" y2="$layout.image.Rows$" style="stroke: #202020; stroke-width: 4;" />

$image/imageRelativeSvg()$
</g>
</svg>
$if(layout.sameSize)$</svg>$endif$
<svg class="imageDisplayRel" style="width: 100%; height: 100%;" preserveAspectRatio="none" viewBox="0,0,1000,1000">
$image/displayRelativeSvg()$
</svg>
</svg>
$else$
No Image
$endif$
$endif$
</div>
