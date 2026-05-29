package me.leviaria.deathchest;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;

public class WorldGuardSupport {
    private final DeathChestPlugin plugin;
    private final WorldGuardPlugin worldGuardPlugin;
    private final boolean active;
    private boolean reportedFailure;

    private WorldGuardSupport(DeathChestPlugin plugin, WorldGuardPlugin worldGuardPlugin, boolean active) {
        this.plugin = plugin;
        this.worldGuardPlugin = worldGuardPlugin;
        this.active = active;
    }

    public static WorldGuardSupport create(DeathChestPlugin plugin) {
        if (!plugin.getSettings().isUseWorldGuard()) {
            return new WorldGuardSupport(plugin, null, false);
        }
        try {
            Plugin candidate = Bukkit.getPluginManager().getPlugin("WorldGuard");
            if (candidate instanceof WorldGuardPlugin) {
                plugin.log("Hooked into WorldGuard.");
                return new WorldGuardSupport(plugin, (WorldGuardPlugin) candidate, true);
            }
        } catch (Throwable throwable) {
            plugin.log("WorldGuard hook failed: " + throwable.getMessage());
        }
        plugin.log("WorldGuard was not found. Region checks are inactive.");
        return new WorldGuardSupport(plugin, null, false);
    }

    public boolean canBuild(Player player, Location location) {
        if (!this.active) {
            return true;
        }
        try {
            return this.worldGuardPlugin.canBuild(player, location);
        } catch (Throwable throwable) {
            reportFailure(throwable);
            return true;
        }
    }

    private void reportFailure(Throwable throwable) {
        if (!this.reportedFailure) {
            this.plugin.log("WorldGuard operation failed: " + throwable.getMessage());
            this.reportedFailure = true;
        }
    }
}
