package com.github.lotqwerty.lottweaks.client;

import net.minecraft.item.ItemStack;

import java.util.List;

public class ItemRotation {
    private final String id;
    private final List<ItemStack> results;
    private int priority;

    public ItemRotation(String id, List<ItemStack> results, int priority) {
        this.id = id;
        this.results = results;
        this.priority = priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public String id() {
        return this.id;
    }

    public List<ItemStack> results() {
        return this.results;
    }

    public int priority() {
        return this.priority;
    }
}
