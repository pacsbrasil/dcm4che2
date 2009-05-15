$sharedStatic()$
/** Utilities contains methods that are used client side only */
$utilities()$
$model()$
$xeroView()$
$controller(IS_SERVER=false)$

// Define short versions of the MVC components.
var ua = navigator.userAgent;
var model = new XeroModel(_SARISSA_IS_IE);
var controller = new XeroController(model, ua, xeroView);
