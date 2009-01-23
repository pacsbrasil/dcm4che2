$! Defines an equals (==) assertion that is inline and can record the assertion results. !$
if(!($expected$==$actual$)) {
	var message = "";
	
	$if(msg)$
		message += "$msg;format="js"$  ";
	$endif$
	
	message += "Expected <" + $expected$ + "> but was <" + $actual$ + ">.";
	
	throw new java.lang.AssertionError(message);
}