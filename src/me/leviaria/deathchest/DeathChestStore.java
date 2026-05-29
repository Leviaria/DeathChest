package me.leviaria.deathchest;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.util.config.Configuration;

public class DeathChestStore {
    private final DeathChestPlugin plugin;
    private final File file;
    private final Map<String, DeathChestRecord> records;
    private Configuration configuration;

    public DeathChestStore(DeathChestPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "chests.yml");
        this.records = new LinkedHashMap<String, DeathChestRecord>();
    }

    public void load() {
        ensureFile();
        this.records.clear();
        this.configuration = new Configuration(this.file);
        this.configuration.load();
        List<String> worlds = getKeys("chests");
        for (int i = 0; i < worlds.size(); i++) {
            String worldName = worlds.get(i);
            List<String> players = getKeys("chests." + worldName);
            for (int j = 0; j < players.size(); j++) {
                loadRecord(worldName, players.get(j));
            }
        }
    }

    public void save() {
        if (this.configuration != null) {
            this.configuration.save();
        }
    }

    public DeathChestRecord findByPlayer(World world, String playerName) {
        if (world == null || playerName == null) {
            return null;
        }
        return this.records.get(key(world.getName(), playerName));
    }

    public DeathChestRecord findByChest(Location location) {
        for (DeathChestRecord record : this.records.values()) {
            if (record.matchesChest(location)) {
                return record;
            }
        }
        return null;
    }

    public DeathChestRecord findBySign(Location location) {
        for (DeathChestRecord record : this.records.values()) {
            if (record.matchesSign(location)) {
                return record;
            }
        }
        return null;
    }

    public void register(String owner, Block chestBlock, Block signBlock) {
        String worldName = chestBlock.getWorld().getName();
        String playerKey = normalize(owner);
        String path = "chests." + worldName + "." + playerKey;
        DeathChestRecord record = new DeathChestRecord(owner, worldName, chestBlock.getX(), chestBlock.getY(), chestBlock.getZ(), signBlock.getX(), signBlock.getY(), signBlock.getZ());
        this.records.put(key(worldName, owner), record);
        this.configuration.setProperty(path + ".owner", owner);
        this.configuration.setProperty(path + ".chest.x", Integer.valueOf(chestBlock.getX()));
        this.configuration.setProperty(path + ".chest.y", Integer.valueOf(chestBlock.getY()));
        this.configuration.setProperty(path + ".chest.z", Integer.valueOf(chestBlock.getZ()));
        this.configuration.setProperty(path + ".sign.x", Integer.valueOf(signBlock.getX()));
        this.configuration.setProperty(path + ".sign.y", Integer.valueOf(signBlock.getY()));
        this.configuration.setProperty(path + ".sign.z", Integer.valueOf(signBlock.getZ()));
        this.configuration.save();
    }

    public void remove(DeathChestRecord record) {
        if (record == null) {
            return;
        }
        String playerKey = normalize(record.getOwner());
        this.records.remove(key(record.getWorldName(), record.getOwner()));
        this.configuration.removeProperty("chests." + record.getWorldName() + "." + playerKey);
        this.configuration.save();
    }

    private void loadRecord(String worldName, String playerKey) {
        String path = "chests." + worldName + "." + playerKey;
        String owner = this.configuration.getString(path + ".owner", playerKey);
        int chestX = this.configuration.getInt(path + ".chest.x", Integer.MIN_VALUE);
        int chestY = this.configuration.getInt(path + ".chest.y", Integer.MIN_VALUE);
        int chestZ = this.configuration.getInt(path + ".chest.z", Integer.MIN_VALUE);
        int signX = this.configuration.getInt(path + ".sign.x", Integer.MIN_VALUE);
        int signY = this.configuration.getInt(path + ".sign.y", Integer.MIN_VALUE);
        int signZ = this.configuration.getInt(path + ".sign.z", Integer.MIN_VALUE);
        if (chestX == Integer.MIN_VALUE || chestY == Integer.MIN_VALUE || chestZ == Integer.MIN_VALUE || signX == Integer.MIN_VALUE || signY == Integer.MIN_VALUE || signZ == Integer.MIN_VALUE) {
            return;
        }
        DeathChestRecord record = new DeathChestRecord(owner, worldName, chestX, chestY, chestZ, signX, signY, signZ);
        this.records.put(key(worldName, owner), record);
    }

    private List<String> getKeys(String path) {
        List<String> keys = this.configuration.getKeys(path);
        if (keys == null) {
            return new ArrayList<String>();
        }
        return keys;
    }

    private void ensureFile() {
        this.file.getParentFile().mkdirs();
        if (!this.file.exists()) {
            try {
                this.file.createNewFile();
            } catch (IOException exception) {
                this.plugin.log("Could not create chests.yml: " + exception.getMessage());
            }
        }
    }

    private String key(String worldName, String playerName) {
        return worldName.toLowerCase() + ":" + normalize(playerName);
    }

    private String normalize(String playerName) {
        return playerName.toLowerCase();
    }
}
