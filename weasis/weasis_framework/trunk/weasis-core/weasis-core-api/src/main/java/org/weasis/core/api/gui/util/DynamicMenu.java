package org.weasis.core.api.gui.util;

import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

public abstract class DynamicMenu extends JMenu {

    public DynamicMenu() {
        super();
    }

    public DynamicMenu(Action a) {
        super(a);
    }

    public DynamicMenu(String s, boolean b) {
        super(s, b);
    }

    public DynamicMenu(String s) {
        super(s);
    }

    public abstract void popupMenuWillBecomeVisible();

    public void popupMenuWillBecomeInvisible() {
        removeAll();
    }

    public void popupMenuCanceled() {
    }

    public void addPopupMenuListener() {
        // #WEA-6 - workaround, PopupMenuListener doesn't work on Mac in the top bar with native look and feel
        if (AbstractProperties.isMacNativeLookAndFeel()) {
            this.addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    if (DynamicMenu.this.isSelected()) {
                        DynamicMenu.this.popupMenuWillBecomeVisible();
                    } else {
                        DynamicMenu.this.popupMenuWillBecomeInvisible();
                    }
                }
            });
        } else {
            JPopupMenu menuExport = this.getPopupMenu();
            menuExport.addPopupMenuListener(new PopupMenuListener() {

                @Override
                public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                    DynamicMenu.this.popupMenuWillBecomeVisible();
                }

                @Override
                public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                    DynamicMenu.this.popupMenuWillBecomeInvisible();
                }

                @Override
                public void popupMenuCanceled(PopupMenuEvent e) {
                    DynamicMenu.this.popupMenuCanceled();
                }
            });
        }
    }
}
