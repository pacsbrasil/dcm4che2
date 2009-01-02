/** Handles the spatial transformations - rotate, flip, spatReset */

/** Handle right rotating the image */
XeroController.prototype.RotateRight_clickImage = function(lay,e) {
	this.rotateSeries(lay,90);
};

XeroController.prototype.rotateSeries = function(lay,angle) {
	console.info("Image clicked - trying to rotate the image",angle,"layout",lay.id);
	var nav = this.getNavigate(lay);
	var rot = nav.merged.get("rotation");
	var flip = nav.merged.get("horizontalFlip");
	flip = (flip && flip!=="FALSE" && flip!=="false");
	if(!rot) rot = 0;
	nav=nav.parent;
	if( flip ) rot = (rot+360-angle) % 360;
	else rot = (rot+angle) % 360;
	nav.setMerged("rotation",rot);
	this.waitUpdate("mouseUp",2);
};

/** Handle left rotating the image */
XeroController.prototype.RotateLeft_clickImage = function(lay,e) {
	this.rotateSeries(lay,270);
};

/** Handle horizontal flipping the image */
XeroController.prototype.FlipHorizontal_clickImage = function(lay,e) {
	console.info("Image clicked - trying to flip the image, layout",lay.id);
	var nav = this.getNavigate(lay);
	var flip = nav.merged.get("horizontalFlip");
	nav=nav.parent;
	flip = !flip;
	nav.setMerged("horizontalFlip",flip);
	this.waitUpdate("mouseUp",1);
};

/** Handle the spatial transform reset operation */
XeroController.prototype.SpatReset_clickImage = function(lay,e) {
	console.info("Image clicked - trying to spat reset the image, layout",lay.id);
	var nav = this.getNavigate(lay);
	nav=nav.parent;
	nav.setMerged("horizontalFlip");
	nav.setMerged("rotation");
	this.waitUpdate("mouseUp",2);
};
