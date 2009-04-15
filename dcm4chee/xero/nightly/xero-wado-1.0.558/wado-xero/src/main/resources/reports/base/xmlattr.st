<attr tag="$it.tag$" vr="$it.vr$" vm="$it.vm$" len="$it.len$"$if(it.isEmpty)$/>$else$>$if(it.hasDicomObjects)$
$it.dicomObjects:{
<item>
$it:xmlattr()$</item>$\n$}$
$else$$it.value$$endif$</attr>$endif$$\n$
