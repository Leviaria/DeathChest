package me.leviaria.deathchest;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DeathChestCommand implements CommandExecutor {
    private final DeathChestPlugin plugin;

    public DeathChestCommand(DeathChestPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String name = command.getName().toLowerCase();
        if ("dcversion".equals(name)) {
            sender.sendMessage(this.plugin.getSettings().getPrefix() + ChatColor.YELLOW + this.plugin.getDescription().getFullName());
            return true;
        }
        if ("dcreload".equals(name)) {
            return reload(sender);
        }
        if ("dcport".equals(name)) {
            return port(sender);
        }
        if (args.length > 0 && "reload".equalsIgnoreCase(args[0])) {
            return reload(sender);
        }
        if (args.length > 0 && "port".equalsIgnoreCase(args[0])) {
            return port(sender);
        }
        if (args.length > 0 && "info".equalsIgnoreCase(args[0])) {
            return info(sender);
        }
        help(sender);
        return true;
    }

    private boolean reload(CommandSender sender) {
        if (!Permissions.canReload(sender)) {
            sender.sendMessage(this.plugin.getSettings().getPrefix() + ChatColor.RED + "You do not have permission to reload DeathChest.");
            return true;
        }
        this.plugin.reloadPlugin();
        sender.sendMessage(this.plugin.getSettings().getPrefix() + ChatColor.GREEN + "DeathChest reloaded.");
        return true;
    }

    private boolean port(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }
        if (!Permissions.canPort(sender)) {
            sender.sendMessage(this.plugin.getSettings().getPrefix() + ChatColor.RED + "You do not have permission to teleport to your DeathChest.");
            return true;
        }
        Player player = (Player) sender;
        DeathChestRecord record = this.plugin.getStore().findByPlayer(player.getWorld(), player.getName());
        if (record == null) {
            player.sendMessage(this.plugin.getSettings().getPrefix() + ChatColor.RED + "You do not have a DeathChest in this world.");
            return true;
        }
        Location target = record.getChestLocation();
        if (target == null) {
            player.sendMessage(this.plugin.getSettings().getPrefix() + ChatColor.RED + "The DeathChest world is not loaded.");
            return true;
        }
        player.teleport(findSafeTeleportLocation(target));
        player.sendMessage(this.plugin.getSettings().getPrefix() + ChatColor.GREEN + "Teleported to your DeathChest.");
        return true;
    }

    private boolean info(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }
        Player player = (Player) sender;
        DeathChestRecord record = this.plugin.getStore().findByPlayer(player.getWorld(), player.getName());
        if (record == null) {
            player.sendMessage(this.plugin.getSettings().getPrefix() + ChatColor.YELLOW + "You do not have a DeathChest in this world.");
            return true;
        }
        Location location = record.getChestLocation();
        if (location == null) {
            player.sendMessage(this.plugin.getSettings().getPrefix() + ChatColor.RED + "The DeathChest world is not loaded.");
            return true;
        }
        player.sendMessage(this.plugin.getSettings().getPrefix() + ChatColor.YELLOW + "Your DeathChest is at " + format(location) + ".");
        return true;
    }

    private void help(CommandSender sender) {
        sender.sendMessage(this.plugin.getSettings().getPrefix() + ChatColor.YELLOW + "/dc info " + ChatColor.WHITE + "shows your DeathChest location.");
        sender.sendMessage(this.plugin.getSettings().getPrefix() + ChatColor.YELLOW + "/dc port " + ChatColor.WHITE + "teleports to your DeathChest.");
        if (Permissions.canReload(sender)) {
            sender.sendMessage(this.plugin.getSettings().getPrefix() + ChatColor.YELLOW + "/dc reload " + ChatColor.WHITE + "reloads the plugin.");
        }
    }

    private Location findSafeTeleportLocation(Location base) {
        for (int y = 0; y <= 2; y++) {
            for (int x = -2; x <= 2; x++) {
                for (int z = -2; z <= 2; z++) {
                    Location candidate = new Location(base.getWorld(), base.getBlockX() + x, base.getBlockY() + y, base.getBlockZ() + z);
                    Block feet = candidate.getBlock();
                    Block head = feet.getRelative(BlockFace.UP);
                    Block ground = feet.getRelative(BlockFace.DOWN);
                    if (feet.getType() == Material.AIR && head.getType() == Material.AIR && ground.getType() != Material.AIR) {
                        return candidate;
                    }
                }
            }
        }
        return base;
    }

    private String format(Location location) {
        return location.getWorld().getName() + " " + location.getBlockX() + ", " + location.getBlockY() + ", " + location.getBlockZ();
    }
}
