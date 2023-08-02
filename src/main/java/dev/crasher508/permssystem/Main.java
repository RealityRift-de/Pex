package dev.crasher508.permssystem;

import dev.crasher508.permssystem.commands.PexCommand;
import dev.crasher508.permssystem.dataTypes.GroupData;
import dev.crasher508.permssystem.dataTypes.UserData;
import dev.crasher508.permssystem.listeners.PlayerChatListener;
import dev.crasher508.permssystem.listeners.PlayerJoinListener;
import dev.crasher508.permssystem.listeners.PlayerQuitListener;
import dev.crasher508.permssystem.provider.MySQL;
import dev.crasher508.permssystem.provider.MySQLProvider;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.TabExecutor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import java.util.HashMap;
import java.util.Objects;

public class Main extends JavaPlugin {

    private static Main instance;

    public static Main getInstance() {
        return instance;
    }

    private int mode = 0;
    private boolean direction = true;

    private final HashMap<String, PermissionAttachment> attachments = new HashMap<>();

    private String defaultGroupName = null;

    @Override
    public void onEnable() {
        super.onEnable();
        this.saveDefaultConfig();
        instance = this;
        MySQL.connect();
        if (!MySQLProvider.init()) {
            this.getServer().getPluginManager().disablePlugin(this);
            this.getLogger().warning("Datenbank konnte nicht erstellt werden!");
            return;
        }
        FileConfiguration configuration = this.getConfig();
        String defaultGroupName = configuration.getString("defaultGroupName", "");
        if (defaultGroupName.equals("")) {
            this.getServer().getPluginManager().disablePlugin(this);
            this.getLogger().warning("Es muss eine Standardgruppe konfiguriert werden!");
            return;
        }
        if (!MySQLProvider.getGroupNames().contains(defaultGroupName)) {
            this.getServer().getPluginManager().disablePlugin(this);
            this.getLogger().warning("Die konfigurierte Standardgruppe existiert nicht!");
            return;
        }
        this.defaultGroupName = defaultGroupName;
        this.getServer().getPluginManager().registerEvents(new PlayerJoinListener(), this);
        this.getServer().getPluginManager().registerEvents(new PlayerChatListener(), this);
        this.getServer().getPluginManager().registerEvents(new PlayerQuitListener(), this);
        TabExecutor tabExecutor = new PexCommand();
        Objects.requireNonNull(this.getCommand("pex")).setExecutor(tabExecutor);
        Objects.requireNonNull(this.getCommand("pex")).setTabCompleter(tabExecutor);
        Objects.requireNonNull(this.getCommand("pex")).setPermissionMessage(Main.getInstance().getPrefix() + ChatColor.RED + "Du hast nicht die nötigen Berechtigungen!");
        this.startPlayerListAnimation();
        this.getLogger().warning(ChatColor.BLUE + "Autor: Crasher508 - BETA 1 - wird entfernt!");
    }

    @Override
    public void onDisable() {
        super.onDisable();
        MySQL.disconnect();
    }

    private void startPlayerListAnimation() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (Bukkit.getOnlinePlayers().size() < 1)
                    return;
                String header = switch (mode) {
                    case 0 -> "§5R";
                    case 1 -> "§5Re";
                    case 2 -> "§5Rea";
                    case 3 -> "§5Real";
                    case 4 -> "§5Reali";
                    case 5 -> "§5Realit";
                    case 6 -> "§5Reality";
                    case 7 -> "§5RealityR";
                    case 8 -> "§5RealityRi";
                    case 9 -> "§5RealityRif";
                    case 10 -> "§5RealityRift";
                    case 11 -> "§5RealityRift§d.";
                    case 12 -> "§5RealityRift§d.d";
                    default -> "§5RealityRift§d.de";
                };
                if (mode == 0) {
                    direction = true;
                } else if (mode == 15) {
                    direction = false;
                }
                if (direction) {
                    mode++;
                } else {
                    mode--;
                }
                for (Player player: Bukkit.getOnlinePlayers()) {
                    player.setPlayerListHeader(header);
                    player.setPlayerListFooter("§7Discord: §bhttps://discord.gg/RnZQ2FaH7N");
                }
            }
        }.runTaskTimer(this, 0, 5);
    }

    public String getPrefix() {
        return ChatColor.DARK_GRAY + "[" + ChatColor.DARK_PURPLE + "RealityRift" + ChatColor.DARK_GRAY + "] " + ChatColor.GOLD;
    }

    public HashMap<String, PermissionAttachment> getAttachments() {
        return attachments;
    }

    public String getDefaultGroupName() {
        return defaultGroupName;
    }

    public void setDefaultGroupName(String defaultGroupName) {
        this.defaultGroupName = defaultGroupName;
    }

    public void resetPermissions(Player player) {
        PermissionAttachment attachment = this.attachments.get(player.getName());
        if (attachment != null) {
            player.removeAttachment(attachment);
            this.attachments.remove(player.getName());
        }
        UserData userData = MySQLProvider.getUserData(player.getName());
        if (userData == null)
            return;
        GroupData groupData = MySQLProvider.getGroupData(userData.getGroupName());
        if (groupData == null)
            return;
        PermissionAttachment newAttachment = player.addAttachment(this);
        for (String permission: userData.getPermissions()) {
            newAttachment.setPermission(permission, true);
        }
        for (String permission: groupData.getPermissions()) {
            newAttachment.setPermission(permission, true);
        }
        this.attachments.put(player.getName(), newAttachment);
        player.recalculatePermissions();
    }
}