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
 * Portions created by the Initial Developer are Copyright (C) 2009-2010
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
package in.raster.mayam.form;

import in.raster.mayam.context.ApplicationContext;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.Border;

/**
 *
 * @author Devishree
 * @version 2.0
 */
public class ImageLayoutPopupDesign {

    //Variables
    JPopupMenu popupMenu;
    int currentPopUpSize = 130;
    int tileSize = 30;
    JPanel tilePanel = new JPanel();
    JButton[][] tiles = new JButton[16][16];
    JLabel layoutDisplay = new JLabel();
    Border selectedBorder = BorderFactory.createLineBorder(Color.ORANGE);
    Border deSelectBorder = BorderFactory.createLineBorder(Color.BLACK);

    public ImageLayoutPopupDesign(JPopupMenu popupMenu) {
        this.popupMenu = popupMenu;
        initializeTiles();
        addPopupMenuListener();
    }

    private void initializeTiles() {
        tilePanel.setLayout(null);
        tilePanel.setBounds(1, 1, 490, 505);
        layoutDisplay.setBounds(1, 1, tilePanel.getBounds().width, 20);
        layoutDisplay.setText("Layout : ");
        layoutDisplay.setHorizontalAlignment(JLabel.CENTER);
        tilePanel.add(layoutDisplay);
        int xPos = 5, yPos = 23;
        for (int row = 0; row < 16; row++) {
            for (int column = 0; column < 16; column++) {
                tiles[row][column] = new JButton();
                tiles[row][column].setName(row + "," + column);
                tiles[row][column].setBorder(deSelectBorder);
                tiles[row][column].setBounds(xPos, yPos, tileSize, tileSize);
                tiles[row][column].addMouseMotionListener(addMouseMotionListener());
                tiles[row][column].addActionListener(addActionListener());
                tiles[row][column].setVisible(false);
                tilePanel.add(tiles[row][column]);
                xPos += tileSize;
            }
            yPos += tileSize;
            xPos = 5;
        }
        popupMenu.add(tilePanel);
        popupMenu.setPreferredSize(new Dimension(currentPopUpSize - 20, currentPopUpSize));
        showButtons();
    }

    private void addPopupMenuListener() {
        popupMenu.addMouseMotionListener(new MouseAdapter() {

            @Override
            public void mouseMoved(MouseEvent me) {
                if (tilePanel.getBounds().x < 465 && tilePanel.getBounds().y < 465 && me.getY() > 30 && me.getX() > 10) {
                    if (me.getX() > 480) {
                        currentPopUpSize = 515;
                    } else {
                        currentPopUpSize = me.getX() + 80;
                    }
                    showButtons();
                    popupMenu.setPreferredSize(new Dimension(currentPopUpSize - 20, currentPopUpSize));
                    popupMenu.pack();
                }
            }
        });
    }

    private void showButtons() {
        tilePanel.setBounds(1, 1, currentPopUpSize - 30, currentPopUpSize - 10);
        layoutDisplay.setBounds(1, 1, tilePanel.getBounds().width, 20);
        int squares = (currentPopUpSize - 20) / tileSize;
        if (squares >= 16) {
            squares = 16;
        }
        for (int i = 0; i < squares; i++) {
            for (int j = 0; j < squares; j++) {
                tiles[i][j].setVisible(true);
            }
        }
        for (int i = squares; i < 16; i++) {
            for (int j = 0; j < 16; j++) {
                tiles[i][j].setBorder(null);
                tiles[i][j].setVisible(false);
            }
        }

        for (int i = 0; i <= squares; i++) {
            for (int j = squares; j < 16; j++) {
                tiles[i][j].setBorder(null);
                tiles[i][j].setVisible(false);
            }
        }

        for (int i = 0; i < squares; i++) {
            for (int j = 0; j < squares; j++) {
                if (tiles[i][j].getBorder() == selectedBorder) {
                    layoutDisplay.setText("Layout : " + (i + 1) + "," + (j + 1));
                }
            }
        }
        tilePanel.revalidate();
        tilePanel.repaint();
    }

    private MouseMotionAdapter addMouseMotionListener() {
        MouseMotionAdapter adapter = new MouseMotionAdapter() {

            @Override
            public void mouseMoved(MouseEvent me) {
                JButton source = (JButton) me.getSource();
                String[] name = source.getName().split(",");
                int row = Integer.parseInt(name[0]);
                int column = Integer.parseInt(name[1]);
                layoutDisplay.setText("Layout : " + (row + 1) + "," + (column + 1));

                try {
                    for (int i = 0; i <= row; i++) {
                        for (int j = 0; j <= column; j++) {
                            tiles[i][j].setBorder(selectedBorder);
                        }
                    }

                    for (int i = row + 1; i < 16; i++) {
                        for (int j = 0; j < 16; j++) {
                            tiles[i][j].setBorder(null);
                        }
                    }

                    for (int i = 0; i <= row + 1; i++) {
                        for (int j = column + 1; j < 16; j++) {
                            tiles[i][j].setBorder(null);
                        }
                    }
                } catch (ArrayIndexOutOfBoundsException ex) {
                    //ignore
                }
            }
        };
        return adapter;
    }

    private ActionListener addActionListener() {
        ActionListener actionListener = new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                JButton source = (JButton) e.getSource();
                String[] name = source.getName().split(",");
                int row = Integer.parseInt(name[0]) + 1;
                int column = Integer.parseInt(name[1]) + 1;
                ApplicationContext.imgView.getImageToolbar().changeImageLayout(row, column);
            }
        };
        return actionListener;
    }

    public void resetPopupMenu() {
        currentPopUpSize = 130;
        showButtons();
        popupMenu.setPreferredSize(new Dimension(currentPopUpSize - 20, currentPopUpSize));
        popupMenu.pack();
    }
}
