package me.leviaria.deathchest;

import org.bukkit.ChatColor;
import org.bukkit.util.config.Configuration;

public class PluginSettings {
    private final DeathChestPlugin plugin;

    private String prefix;
    private boolean useWorldGuard;
    private boolean useLwc;
    private boolean checkLwcAccess;
    private boolean protectEmergencyChests;
    private boolean transferToRegisteredChest;
    private boolean createEmergencyChest;
    private boolean requireChestItem;
    private boolean allowDoubleChests;
    private int maxStoredStacks;
    private int emergencySearchRadius;

    public PluginSettings(DeathChestPlugin plugin) {
        this.plugin = plugin;
    }

    public void load() {
        Configuration config = this.plugin.getConfiguration();
        config.load();
        setDefault(config, "messages.prefix", "&7[&cDeathChest&7]&f ");
        setDefault(config, "general.use-worldguard", Boolean.TRUE);
        setDefault(config, "general.use-lwc", Boolean.TRUE);
        setDefault(config, "deathchest.check-lwc-access", Boolean.TRUE);
        setDefault(config, "deathchest.protect-emergency-chests", Boolean.TRUE);
        setDefault(config, "deathchest.transfer-to-registered-chest", Boolean.TRUE);
        setDefault(config, "deathchest.create-emergency-chest", Boolean.TRUE);
        setDefault(config, "deathchest.require-chest-item", Boolean.FALSE);
        setDefault(config, "deathchest.allow-double-chests", Boolean.TRUE);
        setDefault(config, "deathchest.max-stored-stacks", Integer.valueOf(54));
        setDefault(config, "deathchest.emergency-search-radius", Integer.valueOf(3));
        config.save();
        this.prefix = ChatColor.translateAlternateColorCodes('&', config.getString("messages.prefix", "&7[&cDeathChest&7]&f "));
        this.useWorldGuard = config.getBoolean("general.use-worldguard", true);
        this.useLwc = config.getBoolean("general.use-lwc", true);
        this.checkLwcAccess = config.getBoolean("deathchest.check-lwc-access", true);
        this.protectEmergencyChests = config.getBoolean("deathchest.protect-emergency-chests", true);
        this.transferToRegisteredChest = config.getBoolean("deathchest.transfer-to-registered-chest", true);
        this.createEmergencyChest = config.getBoolean("deathchest.create-emergency-chest", true);
        this.requireChestItem = config.getBoolean("deathchest.require-chest-item", false);
        this.allowDoubleChests = config.getBoolean("deathchest.allow-double-chests", true);
        this.maxStoredStacks = Math.max(1, config.getInt("deathchest.max-stored-stacks", 54));
        this.emergencySearchRadius = Math.max(0, config.getInt("deathchest.emergency-search-radius", 3));
    }

    public String getPrefix() {
        return this.prefix;
    }

    public boolean isUseWorldGuard() {
        return this.useWorldGuard;
    }

    public boolean isUseLwc() {
        return this.useLwc;
    }

    public boolean isCheckLwcAccess() {
        return this.checkLwcAccess;
    }

    public boolean isProtectEmergencyChests() {
        return this.protectEmergencyChests;
    }

    public boolean isTransferToRegisteredChest() {
        return this.transferToRegisteredChest;
    }

    public boolean isCreateEmergencyChest() {
        return this.createEmergencyChest;
    }

    public boolean isRequireChestItem() {
        return this.requireChestItem;
    }

    public boolean isAllowDoubleChests() {
        return this.allowDoubleChests;
    }

    public int getMaxStoredStacks() {
        return this.maxStoredStacks;
    }

    public int getEmergencySearchRadius() {
        return this.emergencySearchRadius;
    }

    private void setDefault(Configuration config, String path, Object value) {
        if (config.getProperty(path) == null) {
            config.setProperty(path, value);
        }
    }
}
