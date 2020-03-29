package com.kraigs.android.micamp.Order;

import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup;

import java.util.List;

public class Category extends ExpandableGroup<Item> {


    public Category(String title, List<Item> items) {
        super(title, items);
    }

}
