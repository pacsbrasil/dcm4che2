package com.tiani.prnscp.client.plutgui;

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

import org.apache.log4j.*;

public class PLutGUIFrame extends JFrame
{
    PLutGUIFrame()
    {
        Container panel = this.getContentPane();
        panel.add(new PLutGUIPanel());
        setSize(new Dimension(800,600));
    }
    
    public static void main(String[] args)
    {
        BasicConfigurator.configure();
        Logger.getRootLogger().setLevel(Level.WARN);
        PLutGUIFrame fr = new PLutGUIFrame();
        fr.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        fr.show();
    }
}
