/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 *
 * The Initial Developer of the Original Code is
 * Raster Images
 * Portions created by the Initial Developer are Copyright (C) 2014
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Babu Hussain A
 * Devishree V
 * Meer Asgar Hussain B
 * Prakash J
 * Suresh V
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 *
 * ***** END LICENSE BLOCK ***** */
package in.raster.mayam.listeners;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.KeyEventDispatcher;
import java.awt.event.*;

/**
 *
 * @author Devishree
 * @version 2.0
 */
public final class CursorController {

    public final static Cursor busyCursor = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);
    public final static Cursor defaultCursor = Cursor.getDefaultCursor();

    private CursorController() {
    }

    /*
     * To set Busy cursor when pressing "Search" button
     */
    public static ActionListener createListener(final Component component, final ActionListener mainActionListener) {
        ActionListener actionListener = new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    component.setCursor(busyCursor);
                    mainActionListener.actionPerformed(e);
                } finally {
                    component.setCursor(defaultCursor);
                }
            }
        };
        return actionListener;
    }

    /*
     * To set Busy cursor when pressing Enter key to filter the studies from
     * server
     */
    public static KeyEventDispatcher createListener(final Component component, final KeyEventDispatcher keyEventDispatcher) {
        KeyEventDispatcher keyEventDispatcher1 = new KeyEventDispatcher() {

            @Override
            public boolean dispatchKeyEvent(KeyEvent e) {
                try {
                    component.setCursor(busyCursor);
                    keyEventDispatcher.dispatchKeyEvent(e);
                } finally {
                    component.setCursor(defaultCursor);
                }
                return false;
            }
        };
        return keyEventDispatcher1;
    }

    /*
     * Used to set the busy cursor when showing previews
     */
    public static MouseListener createListener(final Component component, final MouseListener mainActionListener) {
        MouseListener mouseListener = new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    component.setCursor(busyCursor);
                    mainActionListener.mouseClicked(e);
                } finally {
                    component.setCursor(defaultCursor);
                }
            }
        };
        return mouseListener;
    }

    /*
     * Used to set the Busy Cursor when showing previews on whole study download
     */
    public static Runnable createListener(final Component component, final Runnable mainRunnableThread) {
        Runnable runnable = new Runnable() {

            @Override
            public void run() {
                try {
                    component.setCursor(busyCursor);
                    mainRunnableThread.run();
                } finally {
                    component.setCursor(defaultCursor);
                }
            }
        };
        return runnable;
    }
}