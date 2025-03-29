package com.grabbing.state;

import java.util.ArrayList;

public class NodeInventory{
    private ArrayList<Item.NodeItem> items = new ArrayList<>();

    public void addItem(Item.NodeItem item) {
        items.add(item);
        System.out.println("Inventory contains " + items.size() + " items");
    }

    public void removeItem(Item.NodeItem item) {
        items.remove(item);
    }
}
