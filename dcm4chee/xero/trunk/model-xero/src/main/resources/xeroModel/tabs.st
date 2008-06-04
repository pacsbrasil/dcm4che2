/**
 * Define the model information for a Layout that implements a tab.
 */
function TabsLayout(id) {
	if( !id ) id = "tab"+Layout.prototype.idCnt++;
	this.id = id;
};
 
TabsLayout.prototype = new Layout(null,"html/tabs");

/** Adds the given tab/Layout  to the display */
TabsLayout.prototype.addTab = function TV_addTab(name,layout) {
	if(layout==null) {
		layout = new Layout();
		layout.tabInactive = true;
	}
	layout.tabName = name;
	if( this.activeTab ) {
		layout.used = false;
	} else {
		this.activeTab = layout;
		layout.used = true;
	}
	this.add(layout);
};

/** Removes a given tab */
TabsLayout.prototype.removeTab = function TV_removeTab(name) {
	var tab,i,n=this.layouts.length;
	var newArr = new Array();
	for(i=0; i<n; i++) {
		tab = this.layouts[i];
		if( tab.tabName!==name ) {
			newArr.push(tab);
		}
	}
	this.layouts = newArr;
};

/** Finds a tab */
TabsLayout.prototype.findTab = function TV_findTab(name) {
	var tab,i,n=this.layouts.length;
	for(i=0; i<n; i++) {
		tab = this.layouts[i];
		if( tab.tabName===name ) {
			return tab
		}
	}
};

/** Selects the given tab.  Return true if a change is made. */
TabsLayout.prototype.selectTab = function TV_selectTab(name) {
	var tab = this.findTab(name);
	if( !tab ) return false;
	if( this.activeTab===tab ) return false;
	if( tab.tabInactive ) return false;
	if( this.activeTab ) {
		this.activeTab.used = false;
	}
	this.activeTab = tab;
	tab.used = true;
	return true;
};