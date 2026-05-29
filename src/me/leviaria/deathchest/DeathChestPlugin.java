package me.leviaria.deathchest;

import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class DeathChestPlugin extends JavaPlugin {
    private static DeathChestPlugin instance;

    private PluginSettings settings;
    private DeathChestStore store;
    private ChestTransfer transfer;
    private LwcSupport lwcSupport;
    private WorldGuardSupport worldGuardSupport;
    private Logger logger;

    public void onEnable() {
        instance = this;
        this.logger = Logger.getLogger("Minecraft");
        getDataFolder().mkdirs();
        this.settings = new PluginSettings(this);
        this.settings.load();
        this.lwcSupport = LwcSupport.create(this);
        this.worldGuardSupport = WorldGuardSupport.create(this);
        this.store = new DeathChestStore(this);
        this.store.load();
        this.transfer = new ChestTransfer(this);
        registerEvents();
        registerCommands();
        log(getDescription().getFullName() + " enabled for Poseidon Beta 1.7.3.");
    }

    public void onDisable() {
        if (this.store != null) {
            this.store.save();
        }
        log(getDescription().getFullName() + " disabled.");
    }

    public static DeathChestPlugin getInstance() {
        return instance;
    }

    public void reloadPlugin() {
        this.settings.load();
        this.lwcSupport = LwcSupport.create(this);
        this.worldGuardSupport = WorldGuardSupport.create(this);
        this.store.load();
    }

    public PluginSettings getSettings() {
        return this.settings;
    }

    public DeathChestStore getStore() {
        return this.store;
    }

    public ChestTransfer getTransfer() {
        return this.transfer;
    }

    public LwcSupport getLwcSupport() {
        return this.lwcSupport;
    }

    public WorldGuardSupport getWorldGuardSupport() {
        return this.worldGuardSupport;
    }

    public void log(String message) {
        this.logger.info("[DeathChest] " + message);
    }

    private void registerEvents() {
        PluginManager manager = getServer().getPluginManager();
        manager.registerEvents(new DeathListener(this), this);
        manager.registerEvents(new SignListener(this), this);
    }

    private void registerCommands() {
        DeathChestCommand command = new DeathChestCommand(this);
        setExecutor("dc", command);
        setExecutor("dcreload", command);
        setExecutor("dcport", command);
        setExecutor("dcversion", command);
    }

    private void setExecutor(String commandName, CommandExecutor executor) {
        PluginCommand command = getCommand(commandName);
        if (command != null) {
            command.setExecutor(executor);
            return;
        }
        PluginCommand serverCommand = Bukkit.getPluginCommand(commandName);
        if (serverCommand != null) {
            serverCommand.setExecutor(executor);
        }
    }
}
