package com.tiani.prnscp.client.ddl2odgui;

import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JPanel;

public class ButtonLegendPanel extends JPanel
{
    public static interface ButtonLegendCallback
    {
        public void selected(Object o);
    }
    
    private Collection keys;
    private final ButtonLegendCallback callback;
    
    ButtonLegendPanel(final ButtonLegendCallback cb)
    {
        keys = new LinkedList();
        setLayout(new GridLayout(8, 1));
        callback = cb;
    }
    
    public void addKey(String caption, Color color,
                       final Object o)
    {
        final JButton btn = new JButton();
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
