/**
 * A base class for image events, where it is expected that the user of this class will
 * be listening to mouse drag events, middle click and mouse wheel events.
 * This class encapsulates the differences between IE and standard browsers such as 
 * Firefox.
 * Also, up to 3 different event handlers can be active provided they don't capture
 * the same events (left mouse, context menu or mouse wheel events).
 */
 
function updateImage(url) 
{
  this.imageNode.src = url;
}

// An empty method to prevent not defined responses.
function imageEventEmpty() { }
 
/**
 * Create an ImageEvent base object
 */
function ImageEvent() {
  this.imageNode = document.getElementById("image");
  this.imageDiv = document.getElementById("imageDiv");
  this.body = document.getElementById("body");
  this.updateImage = updateImage;
  this.mouseDown = imageEventEmpty;
  this.mouseMove = imageEventEmpty;
  this.mouseUp = imageEventEmpty;
}

// Sometimes the page likes seeing this well defined early on - so create them, even though they
// might be invalid. 
var imageEvent = new ImageEvent();
var imageHandler = imageEvent;

function init() {
  imageEvent = new ImageEvent;
  windowLevelBase.prototype = imageEvent;
  imageHandler = new windowLevelBase(0, 65535);
}
