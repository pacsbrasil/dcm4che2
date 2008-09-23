$! Defines an assertion that is inline and can record the assertion results etc !$
if(!($test$)) {
	$if(msg)$
	print(msg);print("\n");	throw new Error(msg);
	$else$
	print("Assertion '$test;format="js"$' failed.");throw new Error("Assertion '$test;format="js"$' failed.");
	$endif$
}
