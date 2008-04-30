$if(chunk.leaf)$
"$chunk$"
$else$
{ "ast":[$callast(ast=chunk.AST)$] $if(chunk.separatorString)$, "separator":"$chunk.separatorString$" $endif$}
$endif$