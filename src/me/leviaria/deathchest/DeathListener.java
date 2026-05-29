package me.leviaria.deathchest;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

public class DeathListener implements Listener {
    private final DeathChestPlugin plugin;

    public DeathListener(DeathChestPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = Event.Priority.High, ignoreCancelled = false)
    public void onEntityDeath(EntityDeathEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        Player player = (Player) event.getEntity();
        if (!Permissions.canUse(player)) {
            return;
        }
        List<ItemStack> drops = event.getDrops();
        if (drops == null || drops.isEmpty()) {
            return;
        }
        int storedItems = 0;
        TransferResult registeredResult = storeInRegisteredChest(player, drops);
        if (registeredResult.hasStoredItems()) {
            storedItems += registeredResult.getItems();
            player.sendMessage(this.plugin.getSettings().getPrefix() + ChatColor.GREEN + "Stored " + registeredResult.getStacks() + " stack(s) in your registered DeathChest.");
        }
        if (!drops.isEmpty() && this.plugin.getSettings().isCreateEmergencyChest()) {
            TransferResult emergencyResult = storeInEmergencyChest(player, drops);
            if (emergencyResult.hasStoredItems()) {
                storedItems += emergencyResult.getItems();
                player.sendMessage(this.plugin.getSettings().getPrefix() + ChatColor.GREEN + "Stored " + emergencyResult.getStacks() + " stack(s) in a chest at your death location.");
            }
        }
        if (storedItems <= 0) {
            player.sendMessage(this.plugin.getSettings().getPrefix() + ChatColor.RED + "No DeathChest could be created or used. Your items were dropped normally.");
        } else if (!drops.isEmpty()) {
            player.sendMessage(this.plugin.getSettings().getPrefix() + ChatColor.YELLOW + "Some items did not fit and were dropped normally.");
        }
    }

    private TransferResult storeInRegisteredChest(Player player, List<ItemStack> drops) {
        TransferResult result = new TransferResult();
        if (!this.plugin.getSettings().isTransferToRegisteredChest()) {
            return result;
        }
        DeathChestRecord record = this.plugin.getStore().findByPlayer(player.getWorld(), player.getName());
        if (record == null) {
            return result;
        }
        Block chestBlock = record.getChestBlock();
        if (chestBlock == null || chestBlock.getType() != Material.CHEST) {
            player.sendMessage(this.plugin.getSettings().getPrefix() + ChatColor.RED + "Your registered DeathChest is missing.");
            return result;
        }
        if (!this.plugin.getLwcSupport().canAccess(player, chestBlock)) {
            player.sendMessage(this.plugin.getSettings().getPrefix() + ChatColor.RED + "LWC denied access to your registered DeathChest.");
            return result;
        }
        return this.plugin.getTransfer().moveDropsToChest(drops, chestBlock, this.plugin.getSettings().getMaxStoredStacks());
    }

    private TransferResult storeInEmergencyChest(Player player, List<ItemStack> drops) {
        TransferResult result = new TransferResult();
        Block chestBlock = findEmergencyChestBlock(player);
        if (chestBlock == null) {
            return result;
        }
        if (this.plugin.getSettings().isRequireChestItem() && !this.plugin.getTransfer().consumeChestFromDrops(drops)) {
            return result;
        }
        chestBlock.setType(Material.CHEST);
        result = this.plugin.getTransfer().moveDropsToChest(drops, chestBlock, this.plugin.getSettings().getMaxStoredStacks());
        if (result.hasStoredItems()) {
            this.plugin.getLwcSupport().protectPrivate(player, chestBlock);
        } else {
            chestBlock.setType(Material.AIR);
        }
        return result;
    }

    private Block findEmergencyChestBlock(Player player) {
        Location base = player.getLocation();
        int radius = this.plugin.getSettings().getEmergencySearchRadius();
        for (int y = 0; y <= 2; y++) {
            for (int x = -radius; x <= radius; x++) {
                for (int z = -radius; z <= radius; z++) {
                    Location candidate = new Location(base.getWorld(), base.getBlockX() + x, base.getBlockY() + y, base.getBlockZ() + z);
                    if (candidate.getBlockY() < 1 || candidate.getBlockY() >= candidate.getWorld().getMaxHeight()) {
                        continue;
                    }
                    Block block = candidate.getBlock();
                    if (block.getType() == Material.AIR && this.plugin.getWorldGuardSupport().canBuild(player, candidate)) {
                        return block;
                    }
                }
            }
        }
        return null;
    }
}
