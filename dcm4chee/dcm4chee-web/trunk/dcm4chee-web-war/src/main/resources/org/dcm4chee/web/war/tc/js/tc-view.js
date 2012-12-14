function createTCViewDialog(settings) {
	try
	{
		var w = Wicket.Window.create(settings);
		w.resizing = function() {
			updateTCViewDialogLayout();
		};
		return w;
	}
	catch (e)
	{
		console.log(e);
	}
}

function updateTCViewDialog() {
	updateTCViewDialogToUseQueryUI();
	updateTCViewDialogLayout();
}

function updateTCViewDialogToUseQueryUI() {
	styleComboBoxes($("#tc-view"));
	styleTextFields($("#tc-view"));
	styleTextAreas($("#tc-view"));
	styleYearSpinners($("#tc-view"));
	styleMonthSpinners($("#tc-view"));
	
	$('#tc-view-content').tabs({
		show: function(event, ui) {
			updateTCViewTabsLayout();
			checkToLoadThumbnails();
		},
		activate: function( event, ui ) {
			tabActivated(event, ui, $(this).attr('activation-callback-url'));
		}
	});
	$('#tc-view-editable-content').tabs({
		show: function(event, ui) {
			updateTCViewTabsLayout();
			checkToLoadThumbnails();
		},
		activate: function( event, ui ) {
			tabActivated(event, ui, $(this).attr('activation-callback-url'));
		}
	});
	$('#tc-view-images-tab-container').parent().addClass("tc-view-images-tab-container-parent");
	$('#tc-view button').each(function() {
		if (!$(this).hasClass('tc-keyword-chooser-btn'))
		{
			$(this).button();
		}
	});
}

function setDisabledTCViewTabs(indices)
{
	$('#tc-view-content').tabs('option','disabled',indices);
	$('#tc-view-editable-content').tabs('option','disabled',indices);
}

function setHiddenTCViewTabs(indices)
{
	$('#tc-view-content .ui-tabs-nav li').each(function(i) {
		if ($.inArray(i,indices)>=0) {
			$(this).css('display','none');
		}
		else {
			$(this).css('display','');
		}
	});
	$('#tc-view-editable-content .ui-tabs-nav li').each(function(i) {
		if ($.inArray(i,indices)>=0) {
			$(this).css('display','none');
		}
		else {
			$(this).css('display','');
		}
	});
}


function updateTCViewDialogLayout() {
	updateTCViewTabsLayout();
}

function updateTCViewTabsLayout() {
	//adapt height of individual tabs
	$('#tc-view-content').find('div[id^="tabs"]').each(function(index) {
		$(this).height(
				$('#tc-view-content').height()-
				$('#tc-view-content > ul').outerHeight()-
				($(this).outerHeight()-$(this).height()));
	});
	$('#tc-view-editable-content').find('div[id^="tabs"]').each(function(index) {
		$(this).height(
				$('#tc-view-editable-content').height()-
				$('#tc-view-editable-content > ul').outerHeight()-
				($(this).outerHeight()-$(this).height()));
	});
	
	//adapt height of individual elements
	$('#tc-view-diagnosis-text').height($('#tc-view-diagnosis-text').parent().innerHeight()-$('#tc-view-diagnosis-confirmed-chkbox').outerHeight()-33);
	$('#tc-view-bibliography-container').outerHeight(
		$('#tc-view-bibliography-container').parent().innerHeight()-
		$('#tc-view-bibliography-header').outerHeight() -
		$('#tc-view-bibliography-hr').outerHeight());
	
	//layout bibliography elements
	/*
	$('#tc-view-bibliography-container').find('.tc-view-bibliography-list').each(function(index) {
		//account for horizontal width of vertical scrollbar
		$(this).outerWidth($(this).parent().innerWidth()-24);
		
		var textarea = $(this).children('textarea');
		var btn = $(this).children('a');
		
		//textarea.outerWidth($(this).innerWidth());
		btn.css('top',textarea.position().top+4);
		btn.css('left',textarea.position().left+textarea.outerWidth()-btn.outerWidth()-4);
	});
	*/
}

function tabActivated(event, ui, callbackURL)
{
	if (callbackURL) {
		var url = callbackURL;				
		if (ui.newPanel) {
			if (url.indexOf('?')==-1) {
				url += '?newTabId=';
			}
			else {
				url += '&newTabId=';
			}
			url += ui.newPanel.attr('id');
		}
		if (ui.oldPanel) {
			if (url.indexOf('?')==-1) {
				url += '?oldTabId=';
			}
			else {
				url += '&oldTabId=';
			}
			url += ui.oldPanel.attr('id');
		}
		wicketAjaxGet(url,function(){},function(){});
	}
}

function checkToLoadThumbnails()
{
	$('.tc-view-images-thumbnail').lazyload({
		event: "scrollstop",
		//effect: "fadeIn",
		threshold: "500", //px
		container: $('#tc-view-images-thumbnail-container')
	});
}

function checkToScrollToThumbnail(img)
{
	var container = $('#tc-view-images-thumbnail-container');
	var viewLeft = 0; //container.scrollTop();
	var viewRight = container.width()+viewLeft;
	var imgLeft = img.position().left;
	var imgRight = img.width() + imgLeft;
	var left = container.scrollLeft();
	
	if (imgLeft<viewLeft)
	{
		container.animate({scrollLeft:left+imgLeft-1});
	}
	else if (imgRight>viewRight)
	{
		container.animate({scrollLeft:left+imgLeft+img.width()+1-container.width()});
	}
}
