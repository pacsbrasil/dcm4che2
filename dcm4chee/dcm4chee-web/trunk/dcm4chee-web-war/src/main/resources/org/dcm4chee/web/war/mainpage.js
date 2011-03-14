window.onload = setupMainPage;
 
function setupMainPage() {
  //Wicket.Ajax.registerPreCallHandler(showPre);
  //Wicket.Ajax.registerPostCallHandler(showPost);
  Wicket.Ajax.registerFailureHandler(reloadPage);
}

function showPre() {
  alert("AJAX PreCall!");
}
function showPost() {
  alert("AJAX PostCall!");
}
 
function showFailure() {
  alert("AJAX Failure!");
}
 
function reloadPage() {
  window.location.reload(true);
}