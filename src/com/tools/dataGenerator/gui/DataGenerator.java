package com.tools.dataGenerator.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Stream;

public class DataGenerator extends JFrame {
    private static final int SIZE_MB = 1_000_000;
    /**
     * This Map will contain as key the data item name and as value the object representing the data item
     */
    static private Map<String, DataItem> mDataItemMap = new TreeMap<>();
    /**
     * This list contains the full list of Data Item displayed on the left part of the main Window
     */
    static private List<String> mSelectedItemsOnTheLeftList = new ArrayList<>();
    /**
     * This list contains the list of Data Item displayed on the right part of the main Window that have been
     * selected. This list is used only to remove the items displayed on the right part of the main window and
     * that have been selected for deletion
     */
    static private List<String> mSelectedItemsOnTheRightList = new ArrayList<>();
    static private JFileChooser mFc;
    private JPanel mMainPane;
    private JButton mOkBtn;
    private JButton mCancelBtn;
    private JButton mSelectAllBtn;
    private JButton mAddAllBtn;
    private JButton mRemoveBtn;
    private JList mAllDataItemsJList;
    private JList mSelectedDataItemJList;
    private JPanel mAllDataItemJPane;
    private JPanel mSelectedDataItemJPane;
    private JScrollPane mAllDataItemJScrollPane;
    private JScrollPane mSelectedDataItemScrollJPane;


    /**
     * Constructor
     * <p>
     * The ListProductTypes configuration file containing the list of Data item to generate is read
     * Each item is later displayed in the left side of the main window.
     */
    private DataGenerator() {
        //readDataItemConfigurationFile();

        //setContentDataItemJList(mAllDataItemsJList);

        selectDataItemListener(mAddAllBtn, mAllDataItemsJList, mSelectedDataItemJList);

        selectedDataItemListener(mSelectedDataItemJList, mAddAllBtn);

        removeDataItemListener(mRemoveBtn, mSelectedDataItemJList);

        createDataItemListener(mOkBtn);

        selectAllDataItemListener(mSelectAllBtn, mAddAllBtn, mAllDataItemsJList);

        addAllDataItemListener(mAddAllBtn, mSelectedDataItemJList);

        cancelAllDataItemListener(mCancelBtn, mAllDataItemsJList, mAddAllBtn, mSelectedDataItemJList);
    }

    /**
     * Method used to keep trace of the Data Item selected on the right JList component.
     * The selected Data Items are stored into the mSelectedItemsOnTheRightList object.
     * Later the list is used to remove specific Data Items from the Data Items listed in the right part of the main window
     *
     * @param selectedDataItemJList reference to the mSelectedDataItemJList object
     * @param addAllBtn             reference to the mAddAllBtn object
     */
    private static void selectedDataItemListener(JList selectedDataItemJList, JButton addAllBtn) {
        selectedDataItemJList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                super.mouseClicked(mouseEvent);

                mSelectedItemsOnTheRightList.clear();
                JList list = (JList) mouseEvent.getSource();

                addAllBtn.setEnabled(false);

                if (mouseEvent.getClickCount() == 1) {
                    // one-click detected
                    if (!list.isSelectionEmpty()) {
                        String itemName = list.getSelectedValue().toString();
                        if (!mSelectedItemsOnTheRightList.contains(itemName)) {
                            mSelectedItemsOnTheRightList.add(itemName);
                        }
                    }
                }
            }
        });
    }

    /**
     * Method used to set the the JList component with the content read from the
     * configuration file
     *
     * @param productTypesJList reference to the JList component
     */
    private static void setContentDataItemJList(JList<Object> productTypesJList) {
        // Set content into the ProductTypes JList component
        List<String> itemNames = new ArrayList<>();
        for (DataItem item : mDataItemMap.values()) {
            itemNames.add(item.getItemName());
        }
        productTypesJList.setListData(itemNames.toArray());
    }

    /**
     * This method is used to cancel all the Data items added in the JTextPane
     *
     * @param cancelBtn             reference to the mCancelBtn component
     * @param productTypesJList     reference to the JList component
     * @param addAllBtn             reference to the mAddAllBtn component
     * @param selectedDataItemJList reference to the mSelectedDataItemJList
     */
    private static void cancelAllDataItemListener(JButton cancelBtn, JList productTypesJList, JButton addAllBtn, JList selectedDataItemJList) {
        cancelBtn.addActionListener(actionEvent -> {
            productTypesJList.clearSelection();
            addAllBtn.setEnabled(false);
            if (!mSelectedItemsOnTheLeftList.isEmpty()) {
                mSelectedItemsOnTheLeftList.clear();

                selectedDataItemJList.setListData(mSelectedItemsOnTheLeftList.toArray());
            }
        });
    }

    /**
     * This method is used to add all the Data Item to the JTextPane once a;ll the items have been selected
     *
     * @param addAllBtn             reference to the mAddAllBtn objec
     * @param selectedDataItemJList reference to the mSelectedDataItemJList object
     */
    private static void addAllDataItemListener(JButton addAllBtn, JList selectedDataItemJList) {
        addAllBtn.addActionListener(actionEvent -> {
            for (Map.Entry<String, DataItem> entry : mDataItemMap.entrySet()) {
                if (!mSelectedItemsOnTheLeftList.contains(entry.getKey())) {
                    mSelectedItemsOnTheLeftList.add(entry.getKey());
                    selectedDataItemJList.setListData(mSelectedItemsOnTheLeftList.toArray());
                }
            }
        });
    }

    /**
     * This method is used to select all the Data items listed in the JList component
     *
     * @param selectAllBtn      reference to the mSelectAllBtn cbject
     * @param addAllBtn         reference to the mAddAllBtn object
     * @param productTypesJList reference to the JList object
     */
    private static void selectAllDataItemListener(JButton selectAllBtn, JButton addAllBtn, JList productTypesJList) {
        selectAllBtn.addActionListener(actionEvent -> {
            int[] indecis = new int[mDataItemMap.size()];
            for (int i = 0; i < mDataItemMap.size(); i++) {
                indecis[i] = i;
            }
            productTypesJList.setSelectedIndices(indecis);
            addAllBtn.setEnabled(true);
        });
    }

    /**
     * Method used to remove from the JList on the right part of the main window either the last Data Item inserted
     * or the item selected
     *
     * @param removeBtn             reference to the mRemoveBtn objetc
     * @param selectedDataItemJList reference to the mSelectedDataItemJLis object
     */
    private static void removeDataItemListener(JButton removeBtn, JList selectedDataItemJList) {
        // This listener is use to remove from the right JList either the selected items or the last item
        // added in the list
        removeBtn.addActionListener(actionEvent -> {
            //Remove the selected items
            if (!mSelectedItemsOnTheRightList.isEmpty()) {
                for (String item : mSelectedItemsOnTheRightList) {
                    mSelectedItemsOnTheLeftList.remove(item);
                }
                mSelectedItemsOnTheRightList.clear();
                selectedDataItemJList.setListData(mSelectedItemsOnTheLeftList.toArray());
            }
            //Remove last selected data item from the right JList
            else if (!mSelectedItemsOnTheLeftList.isEmpty()) {
                mSelectedItemsOnTheLeftList.remove(mSelectedItemsOnTheLeftList.size() - 1);
                selectedDataItemJList.setListData(mSelectedItemsOnTheLeftList.toArray());
            }
        });
    }

    /**
     * Method defines a Mouse Listener for the JList element that contains all the Data Items
     * read from the configuration file. The Data Item list is displayed on the right part of the
     * main windows
     *
     * @param addAllBtn             reference to mAddAllBtn object
     * @param allDataItemsJList     reference to the JList containing all the Data Items read from the configuration file
     * @param selectedDataItemJList reference to the JList containing the selected Data Items that will be generated
     */
    private static void selectDataItemListener(JButton addAllBtn, JList allDataItemsJList, JList selectedDataItemJList) {
        // Here when an item in the JList is clicked, it will be added in the
        // JTextPane on the right
        allDataItemsJList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                super.mouseClicked(mouseEvent);
                JList list = (JList) mouseEvent.getSource();

                addAllBtn.setEnabled(false);

                if (mouseEvent.getClickCount() == 2) {
                    // Double-click detected
                    String itemName = list.getModel().getElementAt(list.locationToIndex(mouseEvent.getPoint())).toString();

                    if (!mSelectedItemsOnTheLeftList.contains(itemName)) {
                        mSelectedItemsOnTheLeftList.add(itemName);
                        selectedDataItemJList.setListData(mSelectedItemsOnTheLeftList.toArray());
                    }
                }
            }
        });
    }

    /**
     * Method used to read the ListDataItem.cnf file
     */
    private static void readDataItemConfigurationFile() {
        // Fill in a Map with the key equals to the Data Item name and the value equals to a DataItem object
        // representing the data item read from the resource file
        try (Stream<String> lines = Files.lines(Paths.get("./resources/ListDataItem.cnf"), StandardCharsets.UTF_8)) {
            lines.forEach(line -> {
                // split the String line (Ex: P4_1A_HR_____:10) into two strings. The first one containing the
                // data item name and the second one the dimension
                String[] tmp = line.split(":");
                mDataItemMap.put(tmp[0], new DataItem(tmp[0], Double.valueOf(tmp[1])));
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Iterate the list of the selected data item displayed in the right JList
     * and for each Data Item a file is created
     *
     * @param okBtn reference to the mOkBtn object
     */
    private void createDataItemListener(JButton okBtn) {
        okBtn.addActionListener(actionEvent -> {
            //Create a Data Item for each element displayed in the JTextPane
            if (!mSelectedItemsOnTheLeftList.isEmpty()) {
                //Create a file chooser
                mFc = new JFileChooser();
                mFc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                int ret = mFc.showSaveDialog(DataGenerator.this);
                if (ret == JFileChooser.APPROVE_OPTION) {
                    File selectedPath = mFc.getSelectedFile();
                    for (String s : mSelectedItemsOnTheLeftList) {
                        DataItem item = mDataItemMap.get(s);
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
            JOptionPane.showMessageDialog(this, "Data Items have been created!");
        });
    }

    /**
     * This method is used to create a regular menus, submenus
     *
     * @param frame reference to JFrame object
     */
    private JMenuBar createMenus() {
        JMenuBar menuBar = new JMenuBar();
        ImageIcon exitIcon = new ImageIcon("./resources/icons/exit_icon.png");
        ImageIcon newIcon = new ImageIcon("./resources/icons/new_icon.png");
        ImageIcon loadIcon = new ImageIcon("./resources/icons/load_icon.png");
        ImageIcon saveIcon = new ImageIcon("./resources/icons/save_icon.png");
        ImageIcon editIcon = new ImageIcon("./resources/icons/edit_icon.png");

        JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic(KeyEvent.VK_F);

        JMenuItem newMenuItem = new JMenuItem("New", newIcon);
        newMenuItem.setMnemonic(KeyEvent.VK_N);
        newMenuItem.setToolTipText("Create new configuration file");

        JMenuItem loadMenuItem = new JMenuItem("Load", loadIcon);
        loadMenuItem.setMnemonic(KeyEvent.VK_O);
        loadMenuItem.setToolTipText("Load a configuration file");
        loadMenuItem.addActionListener(actionEvent -> {
            // Fill in a Map with the key equals to the Data Item name and the value equals to a DataItem object
            // representing the data item read from the resource file
            try (Stream<String> lines = Files.lines(Paths.get("./resources/ListDataItem.cnf"), StandardCharsets.UTF_8)) {
                lines.forEach(line -> {
                    // split the String line (Ex: P4_1A_HR_____:10) into two strings. The first one containing the
                    // data item name and the second one the dimension
                    String[] tmp = line.split(":");
                    mDataItemMap.put(tmp[0], new DataItem(tmp[0], Double.valueOf(tmp[1])));
                });
            } catch (IOException e) {
                e.printStackTrace();
            }

            setContentDataItemJList(mAllDataItemsJList);
        });

        JMenuItem saveMenuItem = new JMenuItem("Save", saveIcon);
        saveMenuItem.setMnemonic(KeyEvent.VK_S);
        saveMenuItem.setToolTipText("Save configuration file");

        JMenuItem editMenuItem = new JMenuItem("Edit", editIcon);
        editMenuItem.setToolTipText("Edit configuration file");

        JMenuItem exitMenuItem = new JMenuItem("Exit", exitIcon);
        exitMenuItem.setMnemonic(KeyEvent.VK_E);
        exitMenuItem.setToolTipText("Exit application");
        exitMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, ActionEvent.CTRL_MASK));
        exitMenuItem.addActionListener((event) -> System.exit(0));

        fileMenu.add(newMenuItem);
        fileMenu.add(loadMenuItem);
        fileMenu.add(saveMenuItem);
        fileMenu.add(editMenuItem);
        fileMenu.addSeparator();
        fileMenu.add(exitMenuItem);
        menuBar.add(fileMenu);

        return menuBar;
    }

    /**
     * Main method
     *
     * @param args
     */
    public static void main(String[] args) {
        JFrame frame = new JFrame("DataGenerator");

        DataGenerator dataGenerator = new DataGenerator();

        frame.setContentPane(dataGenerator.mMainPane);
        frame.setPreferredSize(new Dimension(1500, 500));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.setJMenuBar(dataGenerator.createMenus());

        frame.pack();
        frame.setVisible(true);
    }
}