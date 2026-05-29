package me.leviaria.deathchest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class ChestTransfer {
    private final DeathChestPlugin plugin;

    public ChestTransfer(DeathChestPlugin plugin) {
        this.plugin = plugin;
    }

    public TransferResult moveDropsToChest(List<ItemStack> drops, Block chestBlock, int maxStacks) {
        TransferResult result = new TransferResult();
        if (drops == null || chestBlock == null || chestBlock.getType() != Material.CHEST || maxStacks <= 0) {
            return result;
        }
        List<Inventory> inventories = getInventories(chestBlock);
        if (inventories.isEmpty()) {
            return result;
        }
        int movedStacks = 0;
        Iterator<ItemStack> iterator = drops.iterator();
        while (iterator.hasNext() && movedStacks < maxStacks) {
            ItemStack drop = iterator.next();
            if (isEmpty(drop)) {
                continue;
            }
            int originalAmount = drop.getAmount();
            ItemStack remaining = addToInventories(drop, inventories);
            int remainingAmount = amountOf(remaining);
            int movedItems = originalAmount - remainingAmount;
            if (movedItems <= 0) {
                continue;
            }
            movedStacks++;
            result.add(1, movedItems);
            if (remainingAmount <= 0) {
                iterator.remove();
            } else {
                drop.setAmount(remainingAmount);
            }
        }
        return result;
    }

    public boolean consumeChestFromDrops(List<ItemStack> drops) {
        Iterator<ItemStack> iterator = drops.iterator();
        while (iterator.hasNext()) {
            ItemStack stack = iterator.next();
            if (!isEmpty(stack) && stack.getType() == Material.CHEST) {
                if (stack.getAmount() <= 1) {
                    iterator.remove();
                } else {
                    stack.setAmount(stack.getAmount() - 1);
                }
                return true;
            }
        }
        return false;
    }

    private ItemStack addToInventories(ItemStack source, List<Inventory> inventories) {
        ItemStack remaining = source.clone();
        for (int i = 0; i < inventories.size(); i++) {
            if (isEmpty(remaining)) {
                return null;
            }
            HashMap<Integer, ItemStack> leftovers = inventories.get(i).addItem(new ItemStack[] { remaining.clone() });
            remaining = collapse(leftovers);
        }
        return remaining;
    }

    private ItemStack collapse(HashMap<Integer, ItemStack> leftovers) {
        if (leftovers == null || leftovers.isEmpty()) {
            return null;
        }
        ItemStack result = null;
        int amount = 0;
        for (ItemStack stack : leftovers.values()) {
            if (isEmpty(stack)) {
                continue;
            }
            if (result == null) {
                result = stack.clone();
            }
            amount += stack.getAmount();
        }
        if (result != null) {
            result.setAmount(amount);
        }
        return result;
    }

    private List<Inventory> getInventories(Block chestBlock) {
        List<Inventory> inventories = new ArrayList<Inventory>();
        addInventory(inventories, chestBlock);
        if (this.plugin.getSettings().isAllowDoubleChests()) {
            BlockFace[] faces = new BlockFace[] { BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST };
            for (int i = 0; i < faces.length; i++) {
                Block relative = chestBlock.getRelative(faces[i]);
                if (relative.getType() == Material.CHEST) {
                    addInventory(inventories, relative);
                }
            }
        }
        return inventories;
    }

    private void addInventory(List<Inventory> inventories, Block block) {
        if (block.getType() != Material.CHEST) {
            return;
        }
        Chest chest = (Chest) block.getState();
        Inventory inventory = chest.getInventory();
        if (inventory != null && !inventories.contains(inventory)) {
            inventories.add(inventory);
        }
    }

    private boolean isEmpty(ItemStack stack) {
        return stack == null || stack.getAmount() <= 0 || stack.getType() == Material.AIR;
    }

    private int amountOf(ItemStack stack) {
        if (isEmpty(stack)) {
            return 0;
        }
        return stack.getAmount();
    }
}
