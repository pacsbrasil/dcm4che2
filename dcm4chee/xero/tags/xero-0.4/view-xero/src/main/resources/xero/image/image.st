<div class="image" $html/style()$  xmlns="http://www.w3.org/1999/xhtml">
$if(layout.image)$
<svg id="svg$layout.id$" class="imageDisplay" onmousedown="xeroController.imageMouseDown(evt)" style="width:$layout.width$px; height: $layout.height$px;" width="$layout.width$" height="$layout.height$" xmlns:xlink="http://www.w3.org/1999/xlink" xmlns="http://www.w3.org/2000/svg">
<g id="gim$layout.id$" class="imageGroup" transform=" translate($layout.translateX$,$layout.translateY$) $if(layout.flip)$scale(-1,1)$endif$ $if(layout.rotation)$rotate($layout.rotation$)$endif$ scale($layout.scale$,$layout.scaleY$)">
$layout.imageUrls:{
<image id="im$layout.id$" preserveAspectRatio="none" xlink:href="$it.imageUrl$" width="$it.cols$" height="$it.rows$" transform="translate($it.left$,$it.top$)"/>
}$

<line x1="0" y1="0" x2="$layout.image.Columns$" y2="0" style="stroke: #202020; stroke-width: 4;" />
<line x1="0" y1="0" x2="0" y2="$layout.image.Rows$" style="stroke: #202020; stroke-width: 4;" />
<line x1="$layout.image.Columns$" y1="0" x2="$layout.image.Columns$" y2="$layout.image.Rows$" style="stroke: #202020; stroke-width: 4;" />
<line x1="0" y1="$layout.image.Rows$" x2="$layout.image.Columns$" y2="$layout.image.Rows$" style="stroke: #202020; stroke-width: 4;" />
</g>
<svg class="imageDisplayRel" style="width: 100%; height: 100%;" preserveAspectRatio="none" viewBox="0,0,1000,1000">
</svg>
</svg>
$else$
No Image
$endif$
</div>
