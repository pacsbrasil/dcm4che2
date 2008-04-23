$model/model()$

/** Creates a XeroController object, typically called xeroController */
function XeroController(model, view) {
	this.model = model;
	this.view = view;
};

XeroController.prototype.getBody = function XC_getBody() {
	if( this.body===undefined ) {
		this.body = document.getElementById("body");
	};
	return this.body;
};

XeroController.prototype.action = function XC_action(src) {
	console.info("Starting to render view from %s",src.value);
	this.model.query.updateUrl();
	console.info("Updated URL to %s",this.model.query.url);
	console.time("SearchQuery");
	this.model.search = new XmlModel(this.model.query);
	this.model.search.request();
	console.timeEnd("SearchQuery");
	console.time("InitialRender");
	var render = this.view.toString("xeroBody",this.model);
	console.timeEnd("InitialRender");
	//console.info("Done rendering the view - now perf testing it, rendered view result %s",render);
	
	console.time("InnerHTML");
	this.getBody().innerHTML = render;
	console.timeEnd("InnerHTML");
};

var xeroController = new XeroController(xeroModel, xeroTemplates);