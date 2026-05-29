package me.leviaria.deathchest;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class Permissions {
    public static final String USE = "deathchest.use";
    public static final String CREATE = "deathchest.create";
    public static final String CREATE_OTHER = "deathchest.create.other";
    public static final String RELOAD = "deathchest.reload";
    public static final String PORT = "deathchest.port";

    private Permissions() {
    }

    public static boolean canUse(CommandSender sender) {
        return has(sender, USE, "deathchest.signchest.use", "deathchest.signchest.saveto");
    }

    public static boolean canCreate(CommandSender sender) {
        return has(sender, CREATE, "deathchest.signchest.create.own", "deathchest.signchest.use");
    }

    public static boolean canCreateOther(CommandSender sender) {
        return has(sender, CREATE_OTHER, "deathchest.signchest.create.other");
    }

    public static boolean canReload(CommandSender sender) {
        return has(sender, RELOAD, "deathchest.commands.reload");
    }

    public static boolean canPort(CommandSender sender) {
        return has(sender, PORT, "deathchest.signchest.port");
    }

    public static boolean has(CommandSender sender, String first, String second) {
        return has(sender, new String[] { first, second });
    }

    public static boolean has(CommandSender sender, String first, String second, String third) {
        return has(sender, new String[] { first, second, third });
    }

    public static boolean has(CommandSender sender, String[] nodes) {
        if (!(sender instanceof Player)) {
            return true;
        }
        if (sender.isOp()) {
            return true;
        }
        for (int i = 0; i < nodes.length; i++) {
            if (sender.hasPermission(nodes[i])) {
                return true;
            }
        }
        return false;
    }
}
