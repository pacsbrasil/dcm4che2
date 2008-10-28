/** Children added from static metadata */
var child;
$children:{child|
	$if(child.template)$
	child = new Layout(this.id+"-$child.name$","$child.template$");
	$elseif(child.layout)$
	child = new $child.layout$(suffix);
	$elseif(child.i18n)$
	child = new LabelLayout(this.id+"-$child.i18n$","$child.i18n$","$child.i18n$", "$child.icon$", "$child.action$", "$child.href$");
	$elseif(child.label)$
	child = new LabelLayout(this.id+"-$child.label;format="js"$","$child.label;format="js"$","", "$child.icon$", "$child.action$", "$child.href$");
	$elseif(child.codeTemplate)$
	child = $(child.codeTemplate)()$;
	$else$
	/* Unknown child type $child$ */
	child = null;
	$endif$
	if( child ) {
		$if(child.showLabel)$child.showLabel=true;$endif$
		$if(child.target)$child.target="$child.target;format="js"$";$endif$
		this.add(child);
	};
}$