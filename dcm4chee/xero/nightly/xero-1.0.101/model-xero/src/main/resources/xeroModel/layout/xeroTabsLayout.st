function XeroRootMenu() {
	this.baseConstructor("XeroRootMenu");
$layout/addChildren(children=overallMenuLayout.children)$
};

XeroRootMenu.prototype = new Layout("XeroRootMenuPrototype","html/menu");
XeroRootMenu.prototype.baseConstructor = Layout;
XeroRootMenu.prototype.displayClass="rootMenu";