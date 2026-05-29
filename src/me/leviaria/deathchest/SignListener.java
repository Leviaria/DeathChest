package me.leviaria.deathchest;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.material.MaterialData;

public class SignListener implements Listener {
    private final DeathChestPlugin plugin;

    public SignListener(DeathChestPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = Event.Priority.Normal, ignoreCancelled = true)
    public void onSignChange(SignChangeEvent event) {
        if (!containsDeathChestMarker(event.getLines())) {
            return;
        }
        Player player = event.getPlayer();
        if (!Permissions.canCreate(player)) {
            deny(event, player, "You do not have permission to create a DeathChest.");
            return;
        }
        Block chestBlock = findChestForSign(event.getBlock());
        if (chestBlock == null) {
            deny(event, player, "No chest was found for this sign.");
            return;
        }
        if (!this.plugin.getLwcSupport().canAccess(player, chestBlock)) {
            deny(event, player, "LWC denied access to this chest.");
            return;
        }
        String owner = resolveOwner(event, player);
        if (!owner.equalsIgnoreCase(player.getName()) && !Permissions.canCreateOther(player)) {
            owner = player.getName();
        }
        this.plugin.getStore().register(owner, chestBlock, event.getBlock());
        event.setLine(0, owner);
        event.setLine(1, "[DeathChest]");
        event.setLine(2, event.getBlock().getWorld().getName());
        event.setLine(3, "");
        player.sendMessage(this.plugin.getSettings().getPrefix() + ChatColor.GREEN + "DeathChest registered for " + owner + ".");
    }

    @EventHandler(priority = Event.Priority.Normal, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        DeathChestRecord record = null;
        if (isSign(block)) {
            record = this.plugin.getStore().findBySign(block.getLocation());
        } else if (block.getType() == Material.CHEST) {
            record = this.plugin.getStore().findByChest(block.getLocation());
        }
        if (record == null) {
            return;
        }
        this.plugin.getStore().remove(record);
        Player owner = Bukkit.getPlayerExact(record.getOwner());
        if (owner != null && !owner.getName().equalsIgnoreCase(event.getPlayer().getName())) {
            owner.sendMessage(this.plugin.getSettings().getPrefix() + ChatColor.RED + "Your DeathChest in " + record.getWorldName() + " was removed by " + event.getPlayer().getName() + ".");
        }
    }

    @EventHandler(priority = Event.Priority.Normal, ignoreCancelled = true)
    public void onBlockPhysics(BlockPhysicsEvent event) {
        Block block = event.getBlock();
        if (!isSign(block)) {
            return;
        }
        DeathChestRecord record = this.plugin.getStore().findBySign(block.getLocation());
        if (record != null && isUnsupportedSign(block)) {
            this.plugin.getStore().remove(record);
        }
    }

    @EventHandler(priority = Event.Priority.Normal, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || event.getClickedBlock() == null || !isSign(event.getClickedBlock())) {
            return;
        }
        DeathChestRecord record = this.plugin.getStore().findBySign(event.getClickedBlock().getLocation());
        if (record == null) {
            return;
        }
        Player player = event.getPlayer();
        if (!record.getOwner().equalsIgnoreCase(player.getName()) && !Permissions.canCreateOther(player)) {
            player.sendMessage(this.plugin.getSettings().getPrefix() + ChatColor.RED + "This is not your DeathChest.");
            return;
        }
        Location location = record.getChestLocation();
        if (location == null) {
            player.sendMessage(this.plugin.getSettings().getPrefix() + ChatColor.RED + "This DeathChest points to an unloaded world.");
            return;
        }
        player.sendMessage(this.plugin.getSettings().getPrefix() + ChatColor.YELLOW + "DeathChest for " + record.getOwner() + " at " + format(location) + ".");
    }

    private void deny(SignChangeEvent event, Player player, String message) {
        event.setCancelled(true);
        player.sendMessage(this.plugin.getSettings().getPrefix() + ChatColor.RED + message);
    }

    private boolean containsDeathChestMarker(String[] lines) {
        for (int i = 0; i < lines.length; i++) {
            String line = clean(lines[i]);
            if (line.indexOf("deathchest") >= 0) {
                return true;
            }
        }
        return false;
    }

    private String resolveOwner(SignChangeEvent event, Player player) {
        String line = strip(event.getLine(1));
        String normalized = line.toLowerCase();
        if (line.length() == 0 || normalized.indexOf("deathchest") >= 0 || !isPlayerName(line)) {
            return player.getName();
        }
        return line;
    }

    private boolean isPlayerName(String value) {
        if (value.length() < 1 || value.length() > 16) {
            return false;
        }
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (!Character.isLetterOrDigit(c) && c != '_') {
                return false;
            }
        }
        return true;
    }

    private String clean(String line) {
        return strip(line).toLowerCase();
    }

    private String strip(String line) {
        if (line == null) {
            return "";
        }
        return ChatColor.stripColor(line).trim();
    }

    private Block findChestForSign(Block signBlock) {
        Block attached = getAttachedBlock(signBlock);
        if (attached != null && attached.getType() == Material.CHEST) {
            return attached;
        }
        Block below = signBlock.getRelative(BlockFace.DOWN);
        if (below.getType() == Material.CHEST) {
            return below;
        }
        BlockFace[] faces = new BlockFace[] { BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST };
        for (int i = 0; i < faces.length; i++) {
            Block relative = signBlock.getRelative(faces[i]);
            if (relative.getType() == Material.CHEST) {
                return relative;
            }
        }
        return null;
    }

    private Block getAttachedBlock(Block signBlock) {
        if (signBlock.getType() != Material.WALL_SIGN) {
            return null;
        }
        MaterialData data = signBlock.getState().getData();
        if (data instanceof org.bukkit.material.Sign) {
            org.bukkit.material.Sign signData = (org.bukkit.material.Sign) data;
            return signBlock.getRelative(signData.getAttachedFace());
        }
        return null;
    }

    private boolean isUnsupportedSign(Block block) {
        if (block.getType() == Material.SIGN_POST) {
            return block.getRelative(BlockFace.DOWN).getType() == Material.AIR;
        }
        Block attached = getAttachedBlock(block);
        return attached == null || attached.getType() == Material.AIR;
    }

    private boolean isSign(Block block) {
        return block.getType() == Material.WALL_SIGN || block.getType() == Material.SIGN_POST;
    }

    private String format(Location location) {
        return location.getWorld().getName() + " " + location.getBlockX() + ", " + location.getBlockY() + ", " + location.getBlockZ();
    }
}
