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
package in.raster.mayam.form.dialogs;

import in.raster.mayam.context.ApplicationContext;
import in.raster.mayam.models.ButtonsModel;
import in.raster.mayam.models.combo.StudyDateComboModel;
import in.raster.mayam.models.combo.StudyTimeComboModel;
import java.awt.GridLayout;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JSpinner;
import javax.swing.SpinnerDateModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 *
 * @author Devishree
 * @version 2.0
 */
public class QueryButtonSettings extends javax.swing.JDialog {

    StudyDateComboModel dateComboModel = new StudyDateComboModel();
    StudyTimeComboModel timeComboModel = new StudyTimeComboModel();
    SpinnerDateModel spinnerTimeModel1 = new SpinnerDateModel();
    SpinnerDateModel spinnerTimeModel2 = new SpinnerDateModel();
    ButtonsModel buttonsModel = new ButtonsModel();
    JCheckBox modalities[] = null;
    String[] possibleStudyDates = new String[]{ApplicationContext.currentBundle.getString("AddButton.selectComboItem.text"), ApplicationContext.currentBundle.getString("AddButton.anyDateComboItem.text"), ApplicationContext.currentBundle.getString("AddButton.todayComboItem.text"), ApplicationContext.currentBundle.getString("AddButton.todayAMComboItem.text"), ApplicationContext.currentBundle.getString("AddButton.todayPMComboItem.text"), ApplicationContext.currentBundle.getString("AddButton.yesterdayComboItem.text"), ApplicationContext.currentBundle.getString("AddButton.lastWeekComboItem.text"), ApplicationContext.currentBundle.getString("AddButton.lastMonthComboItem.text"), ApplicationContext.currentBundle.getString("AddButton.customDateComboItem.text")};
    String[] possibleStudyTimes = new String[]{ApplicationContext.currentBundle.getString("AddButton.selectComboItem.text"), ApplicationContext.currentBundle.getString("AddButton.anyTimeComboITem.text"), ApplicationContext.currentBundle.getString("AddButton.last30MinsComboItem.text"), ApplicationContext.currentBundle.getString("AddButton.last1HrComboItem.text"), ApplicationContext.currentBundle.getString("AddButton.last3HrsComboItem.text"), ApplicationContext.currentBundle.getString("AddButton.last6HrsComboItem.text"), ApplicationContext.currentBundle.getString("AddButton.last8HrsComboItem.text"), ApplicationContext.currentBundle.getString("AddButton.last12HrsComboItem.text"), ApplicationContext.currentBundle.getString("AddButton.customTimeComboItem.text")};
    boolean isCustomDate = false;
    boolean isCustomTime = false;
    boolean setStudyTime = true;

    /**
     * Creates new form QuerySettings
     */
    public QueryButtonSettings(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        ok.setEnabled(false);
        setModels();
        loadModalities();
        addListenerForButtonText();
        addDateChooserListeners();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonNameLabel = new javax.swing.JLabel();
        buttonText = new javax.swing.JTextField();
        studyDateLabel = new javax.swing.JLabel();
        dateCombo = new javax.swing.JComboBox();
        customDatePanel = new javax.swing.JPanel();
        customDateLabel = new javax.swing.JLabel();
        fromDateLabel = new javax.swing.JLabel();
        toDateLabel = new javax.swing.JLabel();
        fromDateChooser = new com.toedter.calendar.JDateChooser();
        toDateChooser = new com.toedter.calendar.JDateChooser();
        studyTimeLabel = new javax.swing.JLabel();
        timeCombo = new javax.swing.JComboBox();
        toTimePanel = new javax.swing.JPanel();
        customTimeLable = new javax.swing.JLabel();
        fromTimeLabel = new javax.swing.JLabel();
        fromTimeSpinner = new javax.swing.JSpinner();
        toTimeLable = new javax.swing.JLabel();
        toTimeSpinner = new javax.swing.JSpinner();
        modalitiesLabel = new javax.swing.JLabel();
        modalityPanel = new javax.swing.JPanel();
        selectedModalitiesLabel = new javax.swing.JLabel();
        ok = new javax.swing.JButton();
        cancel = new javax.swing.JButton();
        modalityText = new javax.swing.JTextField();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle(ApplicationContext.currentBundle.getString("AddButton.title.text")); // NOI18N

        buttonNameLabel.setText("Button Name");

        studyDateLabel.setText(ApplicationContext.currentBundle.getString("AddButton.studyDateLabel.text")); // NOI18N

        dateCombo.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                dateComboItemStateChanged(evt);
            }
        });

        customDatePanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        customDateLabel.setText(ApplicationContext.currentBundle.getString("AddButton.customDateLabel.text")); // NOI18N

        fromDateLabel.setText(ApplicationContext.currentBundle.getString("AddButton.fromLabel.text")); // NOI18N

        toDateLabel.setText(ApplicationContext.currentBundle.getString("AddButton.toLabel.text")); // NOI18N

        fromDateChooser.setDateFormatString("dd/MM/yyyy");
        fromDateChooser.setMaskFormatVisible(true);

        toDateChooser.setDateFormatString("dd/MM/yyyy");
        toDateChooser.setMaskFormatVisible(true);

        javax.swing.GroupLayout customDatePanelLayout = new javax.swing.GroupLayout(customDatePanel);
        customDatePanel.setLayout(customDatePanelLayout);
        customDatePanelLayout.setHorizontalGroup(
            customDatePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(customDatePanelLayout.createSequentialGroup()
                .addGroup(customDatePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(customDatePanelLayout.createSequentialGroup()
                        .addGap(115, 115, 115)
                        .addGroup(customDatePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(toDateLabel)
                            .addComponent(fromDateLabel))
                        .addGap(18, 18, 18)
                        .addGroup(customDatePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(fromDateChooser, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(toDateChooser, javax.swing.GroupLayout.DEFAULT_SIZE, 151, Short.MAX_VALUE)))
                    .addGroup(customDatePanelLayout.createSequentialGroup()
                        .addGap(44, 44, 44)
                        .addComponent(customDateLabel)))
                .addContainerGap(22, Short.MAX_VALUE))
        );
        customDatePanelLayout.setVerticalGroup(
            customDatePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, customDatePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(customDatePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(customDatePanelLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(toDateChooser, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(customDatePanelLayout.createSequentialGroup()
                        .addComponent(customDateLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(customDatePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(fromDateLabel)
                            .addComponent(fromDateChooser, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 18, Short.MAX_VALUE)
                        .addComponent(toDateLabel)))
                .addContainerGap())
        );

        studyTimeLabel.setText(ApplicationContext.currentBundle.getString("AddButton.studyTimeLabel.text")); // NOI18N

        timeCombo.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                timeComboItemStateChanged(evt);
            }
        });

        toTimePanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        customTimeLable.setText(ApplicationContext.currentBundle.getString("AddButton.customTimeLabel.text")); // NOI18N

        fromTimeLabel.setText(ApplicationContext.currentBundle.getString("AddButton.fromLabel.text")); // NOI18N

        toTimeLable.setText(ApplicationContext.currentBundle.getString("AddButton.toLabel.text")); // NOI18N

        javax.swing.GroupLayout toTimePanelLayout = new javax.swing.GroupLayout(toTimePanel);
        toTimePanel.setLayout(toTimePanelLayout);
        toTimePanelLayout.setHorizontalGroup(
            toTimePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(toTimePanelLayout.createSequentialGroup()
                .addContainerGap(82, Short.MAX_VALUE)
                .addGroup(toTimePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(toTimeLable, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(fromTimeLabel, javax.swing.GroupLayout.Alignment.TRAILING))
                .addGap(39, 39, 39)
                .addGroup(toTimePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(fromTimeSpinner, javax.swing.GroupLayout.DEFAULT_SIZE, 137, Short.MAX_VALUE)
                    .addComponent(toTimeSpinner))
                .addGap(47, 47, 47))
            .addGroup(toTimePanelLayout.createSequentialGroup()
                .addGap(56, 56, 56)
                .addComponent(customTimeLable)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        toTimePanelLayout.setVerticalGroup(
            toTimePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(toTimePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(customTimeLable)
                .addGap(1, 1, 1)
                .addGroup(toTimePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(fromTimeSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(fromTimeLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(toTimePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(toTimeLable)
                    .addComponent(toTimeSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(19, Short.MAX_VALUE))
        );

        modalitiesLabel.setText(ApplicationContext.currentBundle.getString("AddButton.modalitiesLabel.text")); // NOI18N

        modalityPanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        javax.swing.GroupLayout modalityPanelLayout = new javax.swing.GroupLayout(modalityPanel);
        modalityPanel.setLayout(modalityPanelLayout);
        modalityPanelLayout.setHorizontalGroup(
            modalityPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        modalityPanelLayout.setVerticalGroup(
            modalityPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 120, Short.MAX_VALUE)
        );

        selectedModalitiesLabel.setText(ApplicationContext.currentBundle.getString("AddButton.selectedModalities.text")); // NOI18N

        ok.setText(ApplicationContext.currentBundle.getString("AddButton.okButton.text")); // NOI18N
        ok.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okActionPerformed(evt);
            }
        });

        cancel.setText(ApplicationContext.currentBundle.getString("AddButton.cancelButton.text")); // NOI18N
        cancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(34, 34, 34)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(buttonNameLabel)
                                    .addComponent(studyDateLabel))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(dateCombo, javax.swing.GroupLayout.PREFERRED_SIZE, 179, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(buttonText, javax.swing.GroupLayout.PREFERRED_SIZE, 179, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(8, 8, 8))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addGap(0, 0, Short.MAX_VALUE)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(customDatePanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(toTimePanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(modalitiesLabel)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(studyTimeLabel)
                                        .addGap(120, 120, 120)
                                        .addComponent(timeCombo, javax.swing.GroupLayout.PREFERRED_SIZE, 175, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addComponent(selectedModalitiesLabel))
                                .addGap(0, 0, Short.MAX_VALUE))))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(55, 55, 55)
                        .addComponent(modalityPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(ok, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cancel))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(60, 60, 60)
                        .addComponent(modalityText, javax.swing.GroupLayout.PREFERRED_SIZE, 285, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(24, 24, 24)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(buttonNameLabel)
                    .addComponent(buttonText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(studyDateLabel)
                    .addComponent(dateCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(customDatePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(studyTimeLabel)
                    .addComponent(timeCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(toTimePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(modalitiesLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(modalityPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(selectedModalitiesLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(modalityText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 30, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cancel)
                    .addComponent(ok))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void cancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelActionPerformed
        setVisible(false);
        dispose();
    }//GEN-LAST:event_cancelActionPerformed

    private void okActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okActionPerformed
        saveQuerySettings();
    }//GEN-LAST:event_okActionPerformed

    private void dateComboItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_dateComboItemStateChanged
        setCustomDateVisibility();
        if (dateCombo.getSelectedIndex() != 2) {
            studyTimeLabel.setEnabled(false);
            timeCombo.setEnabled(false);
        } else {
            studyTimeLabel.setEnabled(true);
            timeCombo.setEnabled(true);
        }
    }//GEN-LAST:event_dateComboItemStateChanged

    private void timeComboItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_timeComboItemStateChanged
        setCustomTimeVisibility();
    }//GEN-LAST:event_timeComboItemStateChanged

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /*
         * Set the Nimbus look and feel
         */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /*
         * If Nimbus (introduced in Java SE 6) is not available, stay with the
         * default look and feel. For details see
         * http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(QueryButtonSettings.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(QueryButtonSettings.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(QueryButtonSettings.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(QueryButtonSettings.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /*
         * Create and display the dialog
         */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                QueryButtonSettings dialog = new QueryButtonSettings(new javax.swing.JFrame(), true);
                dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        System.exit(0);
                    }
                });
                dialog.setVisible(true);
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel buttonNameLabel;
    private javax.swing.JTextField buttonText;
    private javax.swing.JButton cancel;
    private javax.swing.JLabel customDateLabel;
    private javax.swing.JPanel customDatePanel;
    private javax.swing.JLabel customTimeLable;
    private javax.swing.JComboBox dateCombo;
    private com.toedter.calendar.JDateChooser fromDateChooser;
    private javax.swing.JLabel fromDateLabel;
    private javax.swing.JLabel fromTimeLabel;
    private javax.swing.JSpinner fromTimeSpinner;
    private javax.swing.JLabel modalitiesLabel;
    private javax.swing.JPanel modalityPanel;
    private javax.swing.JTextField modalityText;
    private javax.swing.JButton ok;
    private javax.swing.JLabel selectedModalitiesLabel;
    private javax.swing.JLabel studyDateLabel;
    private javax.swing.JLabel studyTimeLabel;
    private javax.swing.JComboBox timeCombo;
    private com.toedter.calendar.JDateChooser toDateChooser;
    private javax.swing.JLabel toDateLabel;
    private javax.swing.JLabel toTimeLable;
    private javax.swing.JPanel toTimePanel;
    private javax.swing.JSpinner toTimeSpinner;
    // End of variables declaration//GEN-END:variables

    private void loadModalities() {
        ArrayList<String> activeModalities = ApplicationContext.databaseRef.getActiveModalities();
        modalities = new JCheckBox[activeModalities.size()];
        int rows = 1;
        if (activeModalities.size() % 5 == 0) {
            rows = activeModalities.size() / 5;
        } else {
            rows += activeModalities.size() / 5;
        }
        modalityPanel.setLayout(new GridLayout(rows, 5));

        for (int i = 0; i < activeModalities.size(); i++) {
            JCheckBox chk = new JCheckBox(activeModalities.get(i));
            modalities[i] = chk;
            chk.addItemListener(new ItemListener() {
                @Override
                public void itemStateChanged(ItemEvent e) {
                    String modString = "";
                    for (JCheckBox chk : modalities) { //To identify which modalities were selected and which are not selected
                        if (chk.isSelected()) {
                            modString += "\\" + chk.getActionCommand();
                        }
                    }

                    if (modString.startsWith("\\")) {
                        modString = modString.substring(1);
                    }
                    modalityText.setText(modString);
                }
            });
            modalityPanel.add(chk);
        }
    }

    private void saveQuerySettings() {
        if (!buttonText.getText().equals("")) {
            if (!ApplicationContext.databaseRef.checkRecordExists("buttons", "description", buttonText.getText())) {
                buttonsModel.setButtonlabel(buttonText.getText());
                buttonsModel.setStudyDate(formatStudyDate());
                if (setStudyTime) {
                    buttonsModel.setStudyTime(formatStudyTime());
                }
                buttonsModel.setModality(modalityText.getText());
                buttonsModel.setIsCustomDate(isCustomDate);
                buttonsModel.setIsCustomTime(isCustomTime);
                ApplicationContext.databaseRef.insertButton(buttonsModel);
                ApplicationContext.mainScreenObj.settingsForm.setButtonListTableModel(ApplicationContext.databaseRef.getAllQueryButtons());
                ApplicationContext.mainScreenObj.createButtonsDelegate.loadButtons();
                setVisible(false);
                dispose();
            } else {
                JOptionPane.showMessageDialog(null, "Button '" + buttonText.getText() + "' already exist", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(null, "Please specify the button name", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void setModels() {
        dateComboModel.setFilters(possibleStudyDates);
        dateCombo.setModel(dateComboModel);
        dateCombo.setSelectedIndex(0);
        timeComboModel.setFilters(possibleStudyTimes);
        timeCombo.setModel(timeComboModel);
        timeCombo.setSelectedIndex(0);
        fromTimeSpinner.setModel(spinnerTimeModel1);
        fromTimeSpinner.setEditor(new JSpinner.DateEditor(fromTimeSpinner, "hh:mm"));
        toTimeSpinner.setModel(spinnerTimeModel2);
        toTimeSpinner.setEditor(new JSpinner.DateEditor(toTimeSpinner, "hh:mm"));
    }

    private void setCustomDateVisibility() {
        if (dateCombo.getSelectedIndex() != 8) {
            customDateLabel.setEnabled(false);
            fromDateLabel.setEnabled(false);
            toDateLabel.setEnabled(false);
            fromDateChooser.setEnabled(false);
            toDateChooser.setEnabled(false);
        } else {
            customDateLabel.setEnabled(true);
            fromDateLabel.setEnabled(true);
            toDateLabel.setEnabled(true);
            fromDateChooser.setEnabled(true);
            toDateChooser.setEnabled(true);
        }
        try {
            setCustomTimeVisibility();
        } catch (NullPointerException ex) {
        }
    }

    private void setCustomTimeVisibility() {
        if (timeCombo.getSelectedIndex() == 8 && dateCombo.getSelectedIndex() == 2) {
            customTimeLable.setEnabled(true);
            fromTimeLabel.setEnabled(true);
            toTimeLable.setEnabled(true);
            fromTimeSpinner.setEnabled(true);
            toTimeSpinner.setEnabled(true);
        } else {
            customTimeLable.setEnabled(false);
            fromTimeLabel.setEnabled(false);
            toTimeLable.setEnabled(false);
            fromTimeSpinner.setEnabled(false);
            toTimeSpinner.setEnabled(false);
        }
    }

    private String formatStudyDate() {
        switch (dateCombo.getSelectedIndex()) {
            case 1:
                isCustomDate = false;
                setStudyTime = true;
                return "";
            case 2:
                isCustomDate = false;
                setStudyTime = true;
                return "t";
            case 3:
                isCustomDate = false;
                setStudyTime = false;
                isCustomTime = true;
                buttonsModel.setStudyTime("000000-120000");
                return "t";
            case 4:
                isCustomDate = false;
                setStudyTime = false;
                isCustomTime = true;
                buttonsModel.setStudyTime("120000-235959");
                return "t";
            case 5:
                isCustomDate = false;
                setStudyTime = true;
                return "t-1";
            case 6:
                isCustomDate = false;
                setStudyTime = true;
                return "t-7";
            case 7:
                isCustomDate = false;
                setStudyTime = true;
                return "t-30";
            case 8:
                isCustomDate = true;
                setStudyTime = true;
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
                Date from = (Date) fromDateChooser.getDate();
                Date to = (Date) toDateChooser.getDate();
                if (from != null && to != null) {
                    return (sdf.format(from) + "-" + sdf.format(to));
                } else if (from != null) {
                    return (sdf.format(from));
                } else {
                    return (sdf.format(to));
                }

            default:
                return "";
        }
    }

    private String formatStudyTime() {
        SimpleDateFormat timeformat = new SimpleDateFormat("hhmmss");
        switch (timeCombo.getSelectedIndex()) {
            case 0:
            case 1:
                return "";
            case 2:
                isCustomTime = false;
                buttonsModel.setStudyDate("Today");
                return "m-30";
            case 3:
                isCustomTime = false;
                buttonsModel.setStudyDate("Today");
                return "h-1";
            case 4:
                isCustomTime = false;
                buttonsModel.setStudyDate("Today");
                return "h-3";
            case 5:
                isCustomTime = false;
                buttonsModel.setStudyDate("Today");
                return "h-6";
            case 6:
                isCustomTime = false;
                buttonsModel.setStudyDate("Today");
                return "h-8";
            case 7:
                isCustomTime = false;
                buttonsModel.setStudyDate("Today");
                return "h-12";
            case 8:
                isCustomTime = true;
                buttonsModel.setIsCustomTime(true);
                String from = timeformat.format(fromTimeSpinner.getModel().getValue());
                String to = timeformat.format(toTimeSpinner.getModel().getValue());
                return (from + "-" + to);
        }
        return "";
    }

    private void addListenerForButtonText() {
        buttonText.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                ok.setEnabled(true);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                if (buttonText.getText().equals("")) {
                    ok.setEnabled(false);
                }
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                if (buttonText.getText().equals("")) {
                    ok.setEnabled(false);
                }
            }
        });
    }

    private void addDateChooserListeners() {
        fromDateChooser.getTextBox().addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                fromDateChooser.getTextBox().setForeground(fromDateLabel.getForeground());
            }

            @Override
            public void focusLost(FocusEvent e) {
                fromDateChooser.getTextBox().setForeground(fromDateLabel.getForeground());
            }
        });

        toDateChooser.getTextBox().addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                toDateChooser.getTextBox().setForeground(toDateLabel.getForeground());
            }

            @Override
            public void focusLost(FocusEvent e) {
                toDateChooser.getTextBox().setForeground(toDateLabel.getForeground());
            }
        });
    }
}
