/** Children added from static metadata */
var child;
$children:{child|
	$if(child.template)$
	child = new Layout("$child.name$"+suffix,"$child.template$");
	$elseif(child.layout)$
	child = new $child.layout$(suffix$if(child.i18n)$,"$child.i18n;format="xml"$"$endif$);
	$elseif(child.i18n)$
	child = new LabelLayout("$child.i18n$"+suffix,"$child.i18n$","$child.i18n$", "$child.icon$", "$child.action$", "$child.href$");
	$elseif(child.label)$
	child = new LabelLayout("$child.label;format="js"$"+suffix,"$child.label;format="js"$","", "$child.icon$", "$child.action$", "$child.href$");
	$elseif(child.codeTemplate)$
	child = $(child.codeTemplate)()$;
	$else$
	/* Unknown child type $child$ */
	child = null;
	$endif$
	if( child ) {
		$if(child.showLabel)$child.showLabel=true;$endif$
		$if(child.isActive)$child.isActive=$child.isActive$;$endif$
		$if(child.target)$child.target="$child.target;format="js"$";$endif$
		this.add(child);
	};
}$