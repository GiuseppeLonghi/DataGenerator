package com.tools.dataGenerator.gui;

/**
 * This class represents a Data Item
 */
public class DataItem {

    private String mItemName;
    private double mDimesion;

    /**
     * Default Constructor
     */
    public DataItem () {}

    /**
     * Constructor
     * @param itemName: name of the data item. This name follows a specific file naming convention
     * @param dimension: the dimension of the data item (expressed in MB)
     */
    public DataItem(String itemName, double dimension) {
        mItemName = itemName;
        mDimesion = dimension;
    }

    public String getItemName() {
        return mItemName;
    }

    public double getDimesion() {
        return mDimesion;
    }

}
