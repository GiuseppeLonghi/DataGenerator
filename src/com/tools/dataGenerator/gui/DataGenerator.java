package com.tools.dataGenerator.gui;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

public class DataGenerator extends JFrame {
    private JPanel mMainPane;
    private JPanel mLeftPane;
    private JPanel mRightPane;
    private JList mProductTypes;
    private JTextPane mSelectedProductTypesTextPane;
    private JPanel mBottomPane;
    private JButton mCancel;
    private JButton mRemove;
    private JButton mOk;

    public DataGenerator() {

        java.util.List<String> selectedItemList = new ArrayList<>();
        Document doc = mSelectedProductTypesTextPane.getDocument();

        String[] productTypesArray = new String[]{"P4_1A_HR_____",
                "P4_0__ACQ____",
                "P4_0__HR_____",
                "P4_0__LR_____",
                "P4_0__CAL____",
                "MW_0__AMR____",
                "GN_0__GNS____"};
        mProductTypes.setListData(productTypesArray);

        mProductTypes.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                super.mouseClicked(mouseEvent);
                JList list = (JList) mouseEvent.getSource();
                if (mouseEvent.getClickCount() == 2) {
                    // Double-click detected
                    String itemName = list.getModel().getElementAt(list.locationToIndex(mouseEvent.getPoint())).toString();

                    if (!selectedItemList.contains(itemName)) {
                        selectedItemList.add(itemName);

                        try {
                            doc.insertString(doc.getLength(), itemName + "\n", null);
                        } catch (BadLocationException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });

        mRemove.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                //Remove last selected product type from the Selected item text pane
                if (!selectedItemList.isEmpty()) {
                    try {
                        doc.remove(0, doc.getLength());
                        selectedItemList.remove(selectedItemList.size() - 1);

                        for (String s : selectedItemList) {
                            doc.insertString(doc.getLength(), s + "\n", null);
                        }
                    } catch (BadLocationException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

    }


    public static void main(String[] args) {
        JFrame frame = new JFrame("DataGenerator");
        frame.setContentPane(new DataGenerator().mMainPane);
        frame.setPreferredSize(new Dimension(800, 500));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }
}
