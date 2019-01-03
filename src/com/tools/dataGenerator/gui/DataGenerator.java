package com.tools.dataGenerator.gui;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

public class DataGenerator extends JFrame {
    private JPanel mMainPane;
    private JButton okBtn;
    private JButton cancelBtn;
    private JButton mRemoveBtn;
    private JList mProductTypesJList;
    private JTextPane mSelectedProductTypesTextPane;
    Map<String, Integer> mProductTypesMap = new TreeMap<String, Integer>();


    public DataGenerator() {
        java.util.List<String> selectedItemList = new ArrayList<>();
        Document doc = mSelectedProductTypesTextPane.getDocument();

        // Fill in a map with info from resource file
        try (Stream<String> lines = Files.lines(Paths.get("./resources/ListProductTypes.cnf"), StandardCharsets.UTF_8)) {
            lines.forEach(line -> {
               String[] tmp = line.split(":");
               mProductTypesMap.put(tmp[0], Integer.valueOf(tmp[1]));
                    });
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        // Set content into the ProductTypes JList component
        mProductTypesJList.setListData(mProductTypesMap.keySet().toArray());

        mProductTypesJList.addMouseListener(new MouseAdapter() {
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

        mRemoveBtn.addActionListener(new ActionListener() {
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
