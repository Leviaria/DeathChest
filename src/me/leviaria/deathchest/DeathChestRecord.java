package me.leviaria.deathchest;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

public class DeathChestRecord {
    private final String owner;
    private final String worldName;
    private final int chestX;
    private final int chestY;
    private final int chestZ;
    private final int signX;
    private final int signY;
    private final int signZ;

    public DeathChestRecord(String owner, String worldName, int chestX, int chestY, int chestZ, int signX, int signY, int signZ) {
        this.owner = owner;
        this.worldName = worldName;
        this.chestX = chestX;
        this.chestY = chestY;
        this.chestZ = chestZ;
        this.signX = signX;
        this.signY = signY;
        this.signZ = signZ;
    }

    public String getOwner() {
        return this.owner;
    }

    public String getWorldName() {
        return this.worldName;
    }

    public Location getChestLocation() {
        World world = Bukkit.getWorld(this.worldName);
        if (world == null) {
            return null;
        }
        return new Location(world, this.chestX, this.chestY, this.chestZ);
    }

    public Location getSignLocation() {
        World world = Bukkit.getWorld(this.worldName);
        if (world == null) {
            return null;
        }
        return new Location(world, this.signX, this.signY, this.signZ);
    }

    public Block getChestBlock() {
        Location location = getChestLocation();
        if (location == null) {
            return null;
        }
        return location.getBlock();
    }

    public boolean matchesChest(Location location) {
        return matches(location, this.chestX, this.chestY, this.chestZ);
    }

    public boolean matchesSign(Location location) {
        return matches(location, this.signX, this.signY, this.signZ);
    }

    private boolean matches(Location location, int x, int y, int z) {
        if (location == null || location.getWorld() == null) {
            return false;
        }
        return this.worldName.equals(location.getWorld().getName()) && location.getBlockX() == x && location.getBlockY() == y && location.getBlockZ() == z;
    }
}
