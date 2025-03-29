package com.grabbing.state;

import java.util.ArrayList;

public class Inventory {
    private Item.NodeItem nearestItem;
    private NodeInventory playerInventory = new NodeInventory();
    private ArrayList<Item.NodeItem> worldItems = new ArrayList<>();
    private final float pickupRange = 1.5f;

    public Inventory(){

    }


}
