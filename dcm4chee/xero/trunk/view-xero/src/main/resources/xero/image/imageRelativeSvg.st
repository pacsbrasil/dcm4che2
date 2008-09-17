$layout.image.image:{
<svg:image id="$it.id$" xlink:href="$it.href;format="xml"$" width="$it.width$" height="$it.height$" />
}$
$layout.image.use:{
$if(it.isIMAGEREL)$<svg:use id="$it.id$" xlink:href="$it.href$" />$endif$
}$
