package com.tools.dataGenerator.gui;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;
import java.util.stream.Stream;

public class DataGenerator extends JFrame {
    private static final int SIZE_MB = 1_000_000;
    private JPanel mMainPane;
    private JButton mOkBtn;
    private JButton mCancelBtn;
    private JButton mSelectAllBtn;
    private JButton mAddAllBtn;
    private JButton mRemoveBtn;
    private JList<Object> mProductTypesJList;
    private JTextPane mSelectedProductTypesTextPane;
    /**
     * This Map will contain as key the data item name and as value the object representing the data item
     */
    static private Map<String, DataItem> mProductTypesMap = new TreeMap<>();
    /**
     * This list contains all the data item selected from the full list of data item displayed on the left
     */
    static private List<String> mSelectedItemsList = new ArrayList<>();
    static private JFileChooser mFc;


    /**
     * Constructor
     * <p>
     * The ListProductTypes configuration file containing the list of Data item to generate is read
     * Each item is later displayed in the left side of the main window.
     */
    private DataGenerator() {
        Document doc = mSelectedProductTypesTextPane.getDocument();

        readDataItemConfigurationFile();

        setContentDataItemList(mProductTypesJList);

        selectDataItemListener(doc, mAddAllBtn, mProductTypesJList);

        removeDataItemListener(doc, mRemoveBtn);

        createDataItemListener(mOkBtn);

        selectAllDataItemListener(mSelectAllBtn, mAddAllBtn, mProductTypesJList);

        addAllDataItemListener(doc, mAddAllBtn);

        cancelAllDataItemListener(doc, mCancelBtn, mProductTypesJList, mAddAllBtn);
    }

    /**
     * Method used to set the the JList component with the content read from the
     * configuration file
     * @param productTypesJList reference to the JList component
     */
    private static void setContentDataItemList(JList<Object> productTypesJList) {
        // Set content into the ProductTypes JList component
        List<String> itemNames = new ArrayList<>();
        for (DataItem item : mProductTypesMap.values()) {
            itemNames.add(item.getItemName());
        }
        productTypesJList.setListData(itemNames.toArray());
    }

    /**
     * This method is used to cancel all the Data items added in the JTextPane
     * @param doc model associated with the editor.
     * @param cancelBtn reference to the mCancelBtn component
     * @param productTypesJList reference to the JList component
     * @param addAllBtn reference to the mAddAllBtn component
     */
    private static void cancelAllDataItemListener(Document doc, JButton cancelBtn, JList<Object> productTypesJList, JButton addAllBtn) {
        cancelBtn.addActionListener(actionEvent -> {
            productTypesJList.clearSelection();
            addAllBtn.setEnabled(false);
            if (!mSelectedItemsList.isEmpty()) {
                try {
                    doc.remove(0, doc.getLength());
                    mSelectedItemsList.clear();
                } catch (BadLocationException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * This method is used to add all the Data Item to the JTextPane once a;ll the items have been selected
     * @param doc model associsated with the editor
     * @param addAllBtn reference to the mAddAllBtn component
     */
    private static void addAllDataItemListener(Document doc, JButton addAllBtn) {
        addAllBtn.addActionListener(actionEvent -> {
            for (Map.Entry<String, DataItem> entry : mProductTypesMap.entrySet()) {
                if (!mSelectedItemsList.contains(entry.getKey())) {
                    mSelectedItemsList.add(entry.getKey());
                    try {
                        doc.insertString(doc.getLength(), entry.getKey() + "\n", null);
                    } catch (BadLocationException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    /**
     * This method is used to select all the Data items listed in the JList component
     *
     * @param selectAllBtn reference to the mSelectAllBtn component
     * @param productTypesJList reference to the JList component
     */
    private static void selectAllDataItemListener(JButton selectAllBtn, JButton addAllBtn, JList<Object> productTypesJList) {
        selectAllBtn.addActionListener(actionEvent -> {
            int[] indecis = new int[mProductTypesMap.size()];
            for (int i = 0; i < mProductTypesMap.size(); i++) {
                indecis[i] = i;

            }
            productTypesJList.setSelectedIndices(indecis);
            addAllBtn.setEnabled(true);
        });
    }

    /**
     * Iterate the list of the selected data item displayed in the JTextPane
     * and for each data item create a file
     * @param okBtn reference to the mOkBtn component
     */
    private void createDataItemListener(JButton okBtn) {
        okBtn.addActionListener(actionEvent -> {
            //Create a Data Item for each element displayed in the JTextPane
            if (!mSelectedItemsList.isEmpty()) {
                //Create a file chooser
                mFc = new JFileChooser();
                mFc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                int ret = mFc.showSaveDialog(DataGenerator.this);
                if (ret == JFileChooser.APPROVE_OPTION) {
                    File selectedPath = mFc.getSelectedFile();
                    for (String s : mSelectedItemsList) {
                        DataItem item = mProductTypesMap.get(s);
                        System.out.println(item.getItemName() + " " + item.getDimesion() + " MB");

                        RandomAccessFile file;
                        try {
                            file = new RandomAccessFile(new File(selectedPath + "\\" + item.getItemName()), "rw");
                            file.setLength((long) (item.getDimesion() * SIZE_MB));
                            file.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
    }

    /**
     * Method used to remove from the JTextPane the last data item inserted
     * @param doc model associated with the editor.
     */
    private static void removeDataItemListener(Document doc, JButton removeBtn) {
        // This listener is use to remove from the JtextPane the last item
        // added in the list
        removeBtn.addActionListener(actionEvent -> {
            //Remove last selected product type from the Selected item text pane
            if (!mSelectedItemsList.isEmpty()) {
                try {
                    doc.remove(0, doc.getLength());
                    mSelectedItemsList.remove(mSelectedItemsList.size() - 1);

                    for (String s : mSelectedItemsList) {
                        doc.insertString(doc.getLength(), s + "\n", null);
                    }
                } catch (BadLocationException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Method defines a Mouse Listener for the JList element that contains all the data items
     * read from the configuration file.
     *
     * @param doc model associated with the editor.
     */
    private static void selectDataItemListener(Document doc, JButton addAllBtn, JList<Object> productTypesJList) {
        // Here when an item in the JList is clicked, it will be added in the
        // JTextPane on the right
        productTypesJList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                super.mouseClicked(mouseEvent);
                JList list = (JList) mouseEvent.getSource();

                addAllBtn.setEnabled(false);

                if (mouseEvent.getClickCount() == 2) {
                    // Double-click detected
                    String itemName = list.getModel().getElementAt(list.locationToIndex(mouseEvent.getPoint())).toString();

                    if (!mSelectedItemsList.contains(itemName)) {
                        mSelectedItemsList.add(itemName);

                        try {
                            doc.insertString(doc.getLength(), itemName + "\n", null);
                        } catch (BadLocationException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
    }

    /**
     * Method used to read the ListDataItem.cnf file
     */
    private static void readDataItemConfigurationFile() {
        // Fill in a map with data item name from resource file
        try (Stream<String> lines = Files.lines(Paths.get("./resources/ListDataItem.cnf"), StandardCharsets.UTF_8)) {
            lines.forEach(line -> {
                // split the String line (Ex: P4_1A_HR_____:10) into two strings. The first one containing the
                // data item name and the second one the dimension
                String[] tmp = line.split(":");
                mProductTypesMap.put(tmp[0], new DataItem(tmp[0], Double.valueOf(tmp[1])));
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
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
