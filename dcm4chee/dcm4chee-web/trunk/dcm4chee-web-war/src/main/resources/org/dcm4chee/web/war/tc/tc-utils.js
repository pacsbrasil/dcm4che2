
function setPositionRelativeToParent(parentElementId, elementId)
{
  var obj = document.getElementById(elementId);

  if (obj)
  {
	  var parent = document.getElementById(parentElementId);
	  
	  var x=y=0;  
	  if (parent.offsetParent) 
	  {
	     x=parent.offsetLeft;
	     y=parent.offsetTop+parent.offsetHeight+1;
	     while (parent=parent.offsetParent) 
	     {
	         x+=parent.offsetLeft;
	         y+=parent.offsetTop;
	     }
	  }
	  
	  obj.style.top = new String(y+"px");
	  obj.style.left = new String(x+"px");
  }
}
