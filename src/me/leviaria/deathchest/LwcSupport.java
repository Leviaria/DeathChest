package me.leviaria.deathchest;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.griefcraft.lwc.LWCPlugin;
import com.griefcraft.model.ProtectionTypes;

public class LwcSupport {
    private final DeathChestPlugin plugin;
    private final LWCPlugin lwcPlugin;
    private final boolean active;
    private boolean reportedFailure;

    private LwcSupport(DeathChestPlugin plugin, LWCPlugin lwcPlugin, boolean active) {
        this.plugin = plugin;
        this.lwcPlugin = lwcPlugin;
        this.active = active;
    }

    public static LwcSupport create(DeathChestPlugin plugin) {
        if (!plugin.getSettings().isUseLwc()) {
            return new LwcSupport(plugin, null, false);
        }
        try {
            Plugin candidate = Bukkit.getPluginManager().getPlugin("LWC");
            if (candidate instanceof LWCPlugin) {
                plugin.log("Hooked into LWC.");
                return new LwcSupport(plugin, (LWCPlugin) candidate, true);
            }
        } catch (Throwable throwable) {
            plugin.log("LWC hook failed: " + throwable.getMessage());
        }
        plugin.log("LWC was not found. LWC checks are inactive.");
        return new LwcSupport(plugin, null, false);
    }

    public boolean canAccess(Player player, Block block) {
        if (!this.active || !this.plugin.getSettings().isCheckLwcAccess()) {
            return true;
        }
        try {
            return this.lwcPlugin.getLWC().canAccessProtection(player, block);
        } catch (Throwable throwable) {
            reportFailure(throwable);
            return true;
        }
    }

    public void protectPrivate(Player player, Block block) {
        if (!this.active || !this.plugin.getSettings().isProtectEmergencyChests()) {
            return;
        }
        try {
            if (this.lwcPlugin.getLWC().findProtection(block) != null) {
                return;
            }
            this.lwcPlugin.getLWC().getPhysicalDatabase().registerProtection(block.getTypeId(), ProtectionTypes.PRIVATE, block.getWorld().getName(), player.getName(), "", block.getX(), block.getY(), block.getZ());
        } catch (Throwable throwable) {
            reportFailure(throwable);
        }
    }

    private void reportFailure(Throwable throwable) {
        if (!this.reportedFailure) {
            this.plugin.log("LWC operation failed: " + throwable.getMessage());
            this.reportedFailure = true;
        }
    }
}
