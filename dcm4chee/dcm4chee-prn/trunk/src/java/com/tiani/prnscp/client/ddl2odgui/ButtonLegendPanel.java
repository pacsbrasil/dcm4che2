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
