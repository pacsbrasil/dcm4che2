$stringtemplate()$

var $templatesName$ = new StringTemplate({ 
$templates:{template|$\n$'$template.name$':$template(template=template)$}; separator=",  "$
  });
