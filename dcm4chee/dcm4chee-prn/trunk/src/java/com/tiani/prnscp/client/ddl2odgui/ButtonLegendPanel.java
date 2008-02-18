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
 * The Original Code is part of dcm4che, an implementation of DICOM(TM) in
 * Java(TM), hosted at http://sourceforge.net/projects/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * TIANI Medgraph AG.
 * Portions created by the Initial Developer are Copyright (C) 2002-2005
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Gunter Zeilinger <gunter.zeilinger@tiani.com>
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

package com.tiani.prnscp.client.ddl2odgui;

import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

public class ButtonLegendPanel extends JPanel
{
    public static interface ButtonLegendCallback
    {
        public void remove(Object o);
        public void selected(Object o);
        public void export(Object o);
    }
    
    private static class TaggedJButton extends JButton
    {
        public final Object tag;
        TaggedJButton(Object o)
        {
            tag = o;
        }
    }
    
    private Collection keys;
    private MouseListener popListener;
    private final ButtonLegendCallback callback;
    private TaggedJButton lastBtn;
    
    ButtonLegendPanel(ButtonLegendCallback cb)
    {
        keys = new LinkedList();
        setLayout(new GridLayout(1, 5));
        callback = cb;
        //create right-click menu for button legend
        final JPopupMenu mnuLegendPopup = new JPopupMenu();
        popListener = new MouseAdapter()
            {
                public void mousePressed(MouseEvent e) {
                    showPopup(e);
                }

                public void mouseReleased(MouseEvent e) {
                    showPopup(e);
                }

                private void showPopup(MouseEvent e) {
                    if (e.isPopupTrigger()) {
                        lastBtn = (TaggedJButton)e.getComponent();
                        mnuLegendPopup.show(e.getComponent(),
                                            e.getX(), e.getY());
                    }
                }
            };
        //remove
        Action actRemove = new AbstractAction()
            {
                public void actionPerformed(ActionEvent e)
                {
                    callback.remove(lastBtn.tag);
                    keys.remove(lastBtn.tag);
                    ButtonLegendPanel.this.remove(lastBtn);
                    ButtonLegendPanel.this.repaint();
                }
            };
        actRemove.putValue(Action.NAME, "Remove");
        JMenuItem mnuRemove = new JMenuItem(actRemove);
        mnuLegendPopup.add(mnuRemove);
        //write curve to file
        Action actExport = new AbstractAction()
            {
                public void actionPerformed(ActionEvent e)
                {
                    callback.export(lastBtn.tag);
                }
            };
        actExport.putValue(Action.NAME, "Export");
        JMenuItem mnuExport = new JMenuItem(actExport);
        mnuLegendPopup.add(mnuExport);
    }
    
    public void addKey(String caption, Color color,
                       final Object o)
    {
        final JButton btn = new TaggedJButton(o);
        Action act = new AbstractAction()
        {
            public void actionPerformed(ActionEvent e)
            {
                callback.selected(o);
                btn.setSelected(true);
            }
        };
        act.putValue(Action.NAME, caption);
        btn.setAction(act);
        btn.setForeground(color);
        btn.addMouseListener(popListener);
        add(btn);
        keys.add(o);
        repaint();
    }
    
    public void removeKey(Object o)
    {
        Iterator i = keys.iterator();
        while (i.hasNext()) {
            if (o == i.next()) {
                i.remove();
                break;
            }
        }
    }
    
    public void removeAllKeys()
    {
        Iterator i = keys.iterator();
        while (i.hasNext()) {
            i.next();
            i.remove();
        }
        this.removeAll();
    }
}
