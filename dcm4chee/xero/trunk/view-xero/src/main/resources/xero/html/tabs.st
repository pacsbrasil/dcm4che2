<div class="tabHeader">$layout.layouts:{
  <div class="$if(it.used)$activeTab$else$tab$endif$" onclick="xeroController.switchTab(this,'$it.tabName$')">$it.tabName$</div>
}$
$if(layout.activeTab.tabMenu)$ $(layout.activeTab.tabMenu.template)(layout=layout.activeTab.tabMenu)$ $endif$
</div>
<div class="tabBody">
$(layout.activeTab.template)(layout=layout.activeTab)$
</div>