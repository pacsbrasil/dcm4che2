$layout.image.use:{
$if(it.isDISPLAYREL)$
$it._parent._parent._parent.svg.(it.href).text:{
<v:line type="#$it.id$"  from="$it.xc$,$it.yc$" to="$it.xc2$,$it.yc2$" filled='t' stroked='t' fillcolor='white' strokecolor='white'>
     <v:path textpathok="True"/>
     <v:textpath on="True" string="$it.TEXT;format="xml"$" style="font-size: $it.iefontsize$;"/>
</v:line>
}$
$it._parent._parent._parent.svg.(it.href).path:{
<v:shape type="#$it.id$" style="width:100; height:100;" coordsize="100 100"/>
}$
$endif$
}$
