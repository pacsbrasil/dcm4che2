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
	updateTCViewDialog(false);
}

function updateTCViewDialog(verticalTabs) {
	updateTCViewDialogToUseQueryUI(verticalTabs);
	updateTCViewDialogLayout(verticalTabs);
}

function updateTCViewDialogToUseQueryUI(verticalTabs) {
	style($("#tc-view"));
	
	$('#tc-view-content, #tc-view-editable-content').each(function(index) {
		if (verticalTabs) {
			$(this).addClass('ui-tabs-vertical ui-helper-clearfix');
			$(this).find('li').removeClass('ui-corner-top').addClass('ui-corner-left');
		}
		$(this).tabs({
			show: function(event, ui) {
				updateTCViewTabsLayout();
				checkToLoadThumbnails();
			},
			activate: function( event, ui ) {
				tabActivated(event, ui, $(this).attr('activation-callback-url'));
			}
		});
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
	// layout content
	$('#tc-view-content, #tc-view-editable-content').each(function(index) {
		var vertical = $(this).hasClass('ui-tabs-vertical');
		if (!vertical) {
			var nav = $(this).find('.ui-tabs-nav').first();
			var tabHeight = $(this).height() - $(nav).outerHeight(true);
			var tabWidth = $(this).width();

			$(this).find('.ui-tabs-panel').each(function(index) {
				var diffHeight = $(this).outerHeight(true)-$(this).height();
				var diffWidth = $(this).outerWidth(true)-$(this).width();
				
				$(this).height(tabHeight-diffHeight);
				$(this).width(tabWidth-diffWidth);
			});
		}
		
		/*
		var nav = $(this).find('.ui-tabs-nav').first();
		var header = $(this).siblings('#tc-view-header').first();
		var footer = $(this).siblings('#tc-view-footer').first();
		var parentHeight = $(this).parent().height();
		var parentWidth = $(this).parent().width();

		var contentHeight = parentHeight;
		var contentWidth = parentWidth;
		if (header) {
			contentHeight -= header.outerHeight(true);
		}
		if (footer) {
			contentHeight -= footer.outerHeight(true);
		}
		
		contentHeight -= ($(this).outerHeight(true)-$(this).height());
		contentWidth -= ($(this).outerWidth(true)-$(this).width());

		$(this).height(contentHeight);
		$(this).width(contentWidth);
		
		var tabHeight = $(this).height();
		var tabWidth = $(this).width();

		if (vertical) {
			$(nav).height(contentHeight);
		}
		else {
			tabHeight -= $(nav).outerHeight(true);
		}
		
		$(this).find('.ui-tabs-panel').each(function(index) {
			var diffHeight = $(this).outerHeight(true)-$(this).height();
			var diffWidth = $(this).outerWidth(true)-$(this).width();
			
			$(this).height(tabHeight-diffHeight);
			$(this).width(tabWidth-diffWidth);
		});
		*/
	});
	
	//adapt height of individual elements
	$('#tc-view-diagnosis-text').height($('#tc-view-diagnosis-text').parent().innerHeight()-$('#tc-view-diagnosis-confirmed-chkbox').outerHeight()-33);
	$('#tc-view-bibliography-container').outerHeight(
		$('#tc-view-bibliography-container').parent().innerHeight()-
		$('#tc-view-bibliography-header').outerHeight() -
		$('#tc-view-bibliography-hr').outerHeight());
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
