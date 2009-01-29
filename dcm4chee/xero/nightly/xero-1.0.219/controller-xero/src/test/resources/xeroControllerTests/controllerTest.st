$unittest()$

function XeroModel() {
};

var xeroView = new Object();

$controller()$

var controllerTest = new UnitTest();

controllerTest.init = function() {
	this.origDocument = document;
	document = {
		"getElementById":function(id) {
			return this[id];
       }
	};
	
};

controllerTest.uninit = function() {
	document = this.origDocument;
};

XeroController.prototype.viewRenderedListener = function(lay) {
	lay.fired++;
};

function ViewRenderedLayout(children) {
	this.viewRenderedListeners = ["viewRenderedListener"];
	this.children = children;
	this.fired = 0;
};

controllerTest.test_fireViewRendered_unused = function() {
	var lay = new ViewRenderedLayout();
	controller.fireViewRendered(lay);
	$assertTrue(condition="!lay.fired",msg="The view rendered should not be fired when the layout isn't used.")$;
};

controllerTest.test_fireViewRendered_called = function() {
	var layChild = new ViewRenderedLayout();
	var layChild2 = {"used":true};
	var lay = new ViewRenderedLayout([layChild, layChild2]);
	lay.used = true;
	layChild.used = true;
	controller.fireViewRendered(lay);
	$assertTrue(condition="lay.fired==1",msg="The top view should have been fired")$;
	$assertTrue(condition="layChild.fired==1",msg="The first child should have been fired")$;
	$assertTrue(condition="!layChild2.fired",msg="The second child should not have been fired")$;
};

controllerTest.test_storeScrollPosition = function() {
    this.init();
	var lay = { "id" : "storeItem" };
	document[lay.id] = { "scrollTop": 34 };
	
	controller.storeScrollPosition(lay);

	this.uninit();
	
	$assertTrue(condition="lay.scrollTop==34",msg="Should have stored the scroll top.")$;
	
};

controllerTest.test_fixScrollPosition = function() {
    this.init();
	var lay = { "id" : "storeItem" };
	
	document[lay.id] = { "scrollTop": 25 };
	
	controller.fixScrollPosition(lay);

	$assertTrue(condition="document[lay.id].scrollTop==25",msg="Should have tried to fix the scroll top")$;

	lay.scrollTop = 50;
	controller.fixScrollPosition(lay);
	$assertTrue(condition="document[lay.id].scrollTop==50",msg="Should have fixed the scroll top.")$;

	this.uninit();	
	
};

