$! Defines an assertion that is inline and can record the assertion results etc !$
if(!($test$)) {
	$if(msg)$
	java.lang.System.out.println("$msg;format="js"$");	throw new Error("$msg;format="js"$");
	$else$
	java.lang.System.out.println("Assertion '$test;format="js"$' failed.");throw new Error("Assertion '$test;format="js"$' failed.");
	$endif$
}
