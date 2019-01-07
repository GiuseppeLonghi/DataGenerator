package com.tools.dataGenerator.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
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
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class DataGenerator extends JFrame {
    private static final int SIZE_MB = 1_000_000;

    private static final String mRegex = "^.*\\:.\\D*.*";
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
    /**
     * Create a File Chooser
     */
    static private JFileChooser mFileChooser = new JFileChooser();
    private JPanel mMainPane;
    private JButton mOkBtn;
    private JButton mCancelBtn;
    private JButton mSelectAllBtn;
    private JButton mAddAllBtn;
    private JButton mRemoveBtn;
    private JList mAllDataItemsJList;
    private JList mSelectedDataItemJList;
    private JPanel mAllDataItemJPane;
    private JPanel mCombinedJPane;
    private JScrollPane mAllDataItemJScrollPane;
    private JScrollPane mSelectedDataItemScrollJPane;
    private JEditorPane mJeditorPane;
    private JPanel mSelectedDataItemJPane;
    private JButton mSaveEditorBtn;
    private JButton mCancelEditorBtn;


    /**
     * Constructor
     * <p>
     * The ListProductTypes configuration file containing the list of Data item to generate is read
     * Each item is later displayed in the left side of the main window.
     */
    private DataGenerator() {

        selectDataItemListener(mAddAllBtn, mAllDataItemsJList, mSelectedDataItemJList);

        selectedDataItemListener(mSelectedDataItemJList, mAddAllBtn);

        removeDataItemListener(mRemoveBtn, mSelectedDataItemJList);

        createDataItemListener(mOkBtn);

        selectAllDataItemListener(mSelectAllBtn, mAddAllBtn, mAllDataItemsJList);

        addAllDataItemListener(mAddAllBtn, mSelectedDataItemJList);

        cancelAllDataItemListener(mCancelBtn, mAllDataItemsJList, mAddAllBtn, mSelectedDataItemJList);

        mJeditorPane.setVisible(false);

        saveEditorContentListener();

        cancelEditorContentListener();


    }

    /**
     * Method used to clear the Editor
     */
    private void cancelEditorContentListener() {
        mCancelEditorBtn.addActionListener(actionEvent -> {
            mJeditorPane.setText("");
        });
    }

    /**
     * Method used to save the editor content to a file
     */
    private void saveEditorContentListener() {
        mSaveEditorBtn.addActionListener(actionEvent -> {
            mFileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int ret = mFileChooser.showSaveDialog(DataGenerator.this);
            if (ret == JFileChooser.APPROVE_OPTION) {
                File selectedPath = mFileChooser.getSelectedFile();

                try {
                    Files.write(selectedPath.toPath(), mJeditorPane.getText().getBytes());

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
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
                mFileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                int ret = mFileChooser.showSaveDialog(DataGenerator.this);
                if (ret == JFileChooser.APPROVE_OPTION) {
                    File selectedPath = mFileChooser.getSelectedFile();
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
        //Menu Bar
        JMenuBar menuBar = new JMenuBar();
        ImageIcon exitIcon = new ImageIcon("./resources/icons/exit_icon.png");
        ImageIcon newIcon = new ImageIcon("./resources/icons/new_icon.png");
        ImageIcon loadIcon = new ImageIcon("./resources/icons/load_icon.png");
        ImageIcon saveIcon = new ImageIcon("./resources/icons/save_icon.png");
        ImageIcon editIcon = new ImageIcon("./resources/icons/edit_icon.png");

        //File menu
        JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic(KeyEvent.VK_F);

        //New menu item
        JMenuItem newMenuItem = new JMenuItem("New", newIcon);
        newMenuItem.setMnemonic(KeyEvent.VK_N);
        newMenuItem.setToolTipText("Create new configuration file");
        newMenuItemListener(newMenuItem, mJeditorPane, mSaveEditorBtn, mCancelEditorBtn);

        //Load menu item
        JMenuItem loadMenuItem = new JMenuItem("Load", loadIcon);
        loadMenuItem.setMnemonic(KeyEvent.VK_O);
        loadMenuItem.setToolTipText("Load a configuration file");
        loadMenuListener(loadMenuItem, mAllDataItemsJList);

        //Save menu item
        JMenuItem saveMenuItem = new JMenuItem("Save", saveIcon);
        saveMenuItem.setMnemonic(KeyEvent.VK_S);
        saveMenuItem.setToolTipText("Save configuration file");
        saveMenuItemListener(saveMenuItem);

        //Edit menu item
        JMenuItem editMenuItem = new JMenuItem("Edit", editIcon);
        editMenuItem.setToolTipText("Edit configuration file");
        editMenuItem.setEnabled(false);
        editMenuItemChangeListener(editMenuItem, mAllDataItemsJList, mJeditorPane);
        editMenuItemListener(editMenuItem, mAllDataItemsJList, mJeditorPane);

        //Exit menu item
        JMenuItem exitMenuItem = new JMenuItem("Exit", exitIcon);
        exitMenuItem.setMnemonic(KeyEvent.VK_E);
        exitMenuItem.setToolTipText("Exit application");
        exitMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, ActionEvent.CTRL_MASK));
        exitMenuItem.addActionListener((event) -> System.exit(0));

        //View menu item
        JMenu viewMenu = new JMenu("View");
        viewMenu.setMnemonic(KeyEvent.VK_K);

        //Enable configuration editor item
        JCheckBoxMenuItem enableJEditorPaneMenutItem = new JCheckBoxMenuItem("Enable Configuration Editor");
        enableJEditorPaneMenutItem.setMnemonic(KeyEvent.VK_I);
        enableJEditorPaneMenutItem.setSelected(false);
        setJeditorPaneListener(enableJEditorPaneMenutItem, mJeditorPane, mSaveEditorBtn, mCancelEditorBtn);
        viewMenu.add(enableJEditorPaneMenutItem);

        fileMenu.add(newMenuItem);
        fileMenu.add(loadMenuItem);
        fileMenu.add(saveMenuItem);
        fileMenu.add(editMenuItem);
        fileMenu.addSeparator();
        fileMenu.add(exitMenuItem);
        menuBar.add(fileMenu);
        menuBar.add(viewMenu);

        return menuBar;
    }

    /**
     * Method used to set the Action Listener to the editMenuItem object
     *
     * @param editMenuItem      reference to the editMenuItem object
     * @param allDataItemsJList reference to the mAllDataItemJList object
     * @param jEditorPane       reference to the mJeditorPane object
     */
    private static void editMenuItemListener(JMenuItem editMenuItem, JList allDataItemsJList, JEditorPane jEditorPane) {
        editMenuItem.addActionListener(actionEvent -> {
            for (int i = 0; i < allDataItemsJList.getModel().getSize(); i++) {
                System.out.println("CHECK: " + allDataItemsJList.getModel().getElementAt(i).toString());
                String str = jEditorPane.getText();
                str += allDataItemsJList.getModel().getElementAt(i).toString() + "\n";
                jEditorPane.setText(str);
            }
        });
    }

    /**
     * Method used to set a Property Change Listener to the editMenuItem
     *
     * @param editMenuItem      reference to editMenuItem object
     * @param allDataItemsJList reference to the mAllDataItemJList object
     * @param jEditorPane       reference to the mJeditorPane object
     */
    private static void editMenuItemChangeListener(JMenuItem editMenuItem, JList allDataItemsJList, JEditorPane jEditorPane) {
        editMenuItem.addPropertyChangeListener(propertyChangeEvent -> {
            if (allDataItemsJList.getModel().getSize() != 0 && jEditorPane.isVisible()) {
                editMenuItem.setEnabled(true);
            }
            if (!jEditorPane.isVisible()) {
                editMenuItem.setEnabled(false);
            }
        });
    }

    /**
     * Method used to set the Listener to the saveManuItem object
     *
     * @param saveMenuItem reference to the saveMenuItem object
     */
    private void saveMenuItemListener(JMenuItem saveMenuItem) {
        saveMenuItem.addActionListener(actionEvent -> {

            if (mJeditorPane.isVisible()) {
                mFileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                int ret = mFileChooser.showSaveDialog(DataGenerator.this);
                if (ret == JFileChooser.APPROVE_OPTION) {
                    File selectedPath = mFileChooser.getSelectedFile();

                    try {
                        Files.write(selectedPath.toPath(), mJeditorPane.getText().getBytes());

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    /**
     * Method used to set the Listener for the newMenuItem
     *
     * @param newMenuItem     reference to the newMenuItem object
     * @param jEditorPane     reference to the mJeditorPane object
     * @param saveEditorBtn   reference to the mSaveEditorBtn button object
     * @param cancelEditorBtn reference to the mCancelEditorBtn
     */
    private static void newMenuItemListener(JMenuItem newMenuItem, JEditorPane jEditorPane, JButton saveEditorBtn, JButton cancelEditorBtn) {
        newMenuItem.addActionListener(actionEvent -> {
            jEditorPane.setVisible(true);
            saveEditorBtn.setEnabled(true);
            cancelEditorBtn.setEnabled(true);
        });
    }

    /**
     * Method used to define a item listener for the checkbox sub-menu
     *
     * @param enableJEditorPaneMenutItem reference to the JCheckBocMenutItem
     * @param jEditorPane                reference to the mJeditorPane object
     * @param saveBtn                    reference to the save button object
     * @param cancelBtn                  reference to the cancel button object
     */
    private static void setJeditorPaneListener(JCheckBoxMenuItem enableJEditorPaneMenutItem, JEditorPane jEditorPane,
                                               JButton saveBtn, JButton cancelBtn) {
        enableJEditorPaneMenutItem.addItemListener(event -> {
            if (event.getStateChange() == ItemEvent.SELECTED) {
                jEditorPane.setVisible(true);
                jEditorPane.setPreferredSize(new Dimension(400, 500));
                saveBtn.setEnabled(true);
                cancelBtn.setEnabled(true);

            } else {
                jEditorPane.setVisible(false);
                saveBtn.setEnabled(false);
                cancelBtn.setEnabled(false);
            }
        });
    }

    /**
     * Method defines a listener for the Load sub-menu
     * Through this sub-menu it is possible to load whatever file
     * containing list of data item to generate
     *
     * @param loadMenuItem
     */
    private void loadMenuListener(JMenuItem loadMenuItem, JList allDataItemsJList) {
        loadMenuItem.addActionListener(actionEvent -> {

            List<String> wrongItems = new ArrayList<>();

            //Clear the Data Items map just to be sure that the previous elements are removed
            mDataItemMap.clear();

            //Check whether the JList on the left contains elements and in case delete them
            if (((ListModel) allDataItemsJList.getModel()).getSize() != 0) {
                allDataItemsJList.setListData(new Object[0]);
            }

            //Open the OpenDialog to choose the new configuration file
            int ret = mFileChooser.showOpenDialog(DataGenerator.this);
            if (ret == JFileChooser.APPROVE_OPTION) {
                File file = mFileChooser.getSelectedFile();

                Pattern pattern = Pattern.compile(mRegex);

                // Fill in a Map with the key equals to the Data Item name and the value equals to a DataItem object
                // representing the data item read from the resource file
                try (Stream<String> lines = Files.lines(Paths.get(file.toURI()))) {
                    lines.forEach(line -> {
                        if (pattern.matcher(line).find()) {
                            // split the String line (Ex: P4_1A_HR_____:10) into two strings. The first one containing the
                            // data item name and the second one the dimension
                            String[] tmp = line.split(":");
                            mDataItemMap.put(tmp[0], new DataItem(tmp[0], Double.valueOf(tmp[1])));
                        } else {
                            wrongItems.add(line);
                            System.out.println("WRONG FORMAT");
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }

                setContentDataItemJList(mAllDataItemsJList);


                if (!wrongItems.isEmpty()) {
                    String message = "";
                    for (String str : wrongItems) {
                        message += "\n" + str;
                    }

                    JOptionPane.showMessageDialog(this,
                            "Wrong entry/ies in in the loaded file \n" + message,
                            "Warning", JOptionPane.WARNING_MESSAGE);
                }

            }
        });
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
        frame.setPreferredSize(new Dimension(1200, 500));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setJMenuBar(dataGenerator.createMenus());
        frame.pack();
        frame.setVisible(true);
    }
}