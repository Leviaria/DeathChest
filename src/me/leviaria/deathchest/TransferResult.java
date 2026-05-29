package me.leviaria.deathchest;

public class TransferResult {
    private int stacks;
    private int items;

    public void add(int stackAmount, int itemAmount) {
        this.stacks += stackAmount;
        this.items += itemAmount;
    }

    public boolean hasStoredItems() {
        return this.items > 0;
    }

    public int getStacks() {
        return this.stacks;
    }

    public int getItems() {
        return this.items;
    }
}
