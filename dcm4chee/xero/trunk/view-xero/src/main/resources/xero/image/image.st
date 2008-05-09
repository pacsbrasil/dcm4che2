<div class="image" $html/style()$  xmlns="http://www.w3.org/1999/xhtml">
$if(layout.image)$
<svg style="width:$layout.width$px; height: $layout.height$px;" width="$layout.width$" height="$layout.height$" xmlns:xlink="http://www.w3.org/1999/xlink" xmlns="http://www.w3.org/2000/svg">
<g class="imageGroup" transform="translate($layout.translateX$,$layout.translateY$) scale($layout.scale$,$layout.scale$)">
$layout.imageUrls:{
<image preserveAspectRatio="none" xlink:href="$it.imageUrl$" width="$it.cols$" height="$it.rows$" transform="translate($it.left$,$it.top$)"/>
}$

<line x1="0" y1="0" x2="$layout.image.Columns$" y2="0" style="stroke: rgb(255,255,0); stroke-width: 22;" />
<line x1="0" y1="0" x2="0" y2="$layout.image.Rows$" style="stroke: rgb(255,255,0); stroke-width: 22;" />
<line x1="$layout.image.Columns$" y1="0" x2="$layout.image.Columns$" y2="$layout.image.Rows$" style="stroke: rgb(255,255,0); stroke-width: 22;" />
<line x1="0" y1="$layout.image.Rows$" x2="$layout.image.Columns$" y2="$layout.image.Rows$" style="stroke: rgb(255,255,0); stroke-width: 22;" />
</g>
<svg class="imageDisplayRel" style="width: 100%; height: 100%;" preserveAspectRatio="none" viewBox="0,0,1000,1000" coordsize="1000,1000" coordorigin="0,0">
<line x1="0" y1="0" x2="1000" y2="1000" style="stroke:rgb(255,0,0);stroke-width:2"/>
<line x1="0" y1="0" x2="10" y2="0" style="stroke:rgb(255,0,0);stroke-width:2"/>
<line x1="0" y1="0" x2="0" y2="10" style="stroke:rgb(255,0,0);stroke-width:2"/>
<line x1="1000" y1="1000" x2="990" y2="1000" style="stroke:rgb(255,0,0);stroke-width:2"/>
<line x1="1000" y1="1000" y2="990" x2="1000" style="stroke:rgb(255,0,0);stroke-width:2"/>
<line x1="1000" y1="0" x2="0" y2="1000" style="stroke:rgb(255,0,0);stroke-width:2"/>
</svg>
</svg>
$else$
No Image
$endif$
</div>
