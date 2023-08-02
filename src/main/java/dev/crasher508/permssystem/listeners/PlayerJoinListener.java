package dev.crasher508.permssystem.listeners;

import dev.crasher508.permssystem.Main;
import dev.crasher508.permssystem.dataTypes.GroupData;
import dev.crasher508.permssystem.dataTypes.UserData;
import dev.crasher508.permssystem.provider.MySQLProvider;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.permissions.PermissionAttachment;

import java.util.ArrayList;

public class PlayerJoinListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UserData userData = MySQLProvider.getUserData(player.getName());
        if (userData == null) {
            String defaultGroupName = Main.getInstance().getDefaultGroupName();
            if (defaultGroupName == null)
                return;
            if (!MySQLProvider.addUserData(new UserData(-1, player.getName(), defaultGroupName, new ArrayList<>()))) {
                player.sendMessage(Main.getInstance().getPrefix() + ChatColor.RED + "Registrierung fehlgeschlagen!");
                Main.getInstance().getLogger().warning(player.getName() + " konnte nicht registriert werden!");
                return;
            }
            userData = MySQLProvider.getUserData(player.getName());
        }
        if (userData == null)
            return;
        System.out.println("Debug: " + userData.toString());
        GroupData groupData = MySQLProvider.getGroupData(userData.getGroupName());
        if (groupData == null)
            return;
        System.out.println("Debug: " + groupData.toString());
        player.setDisplayName(groupData.getDisplayStyle().replace("%PLAYER%", player.getName()));
        player.setPlayerListName(groupData.getPlayerListStyle().replace("%PLAYER%", player.getName()));
        PermissionAttachment permissionAttachment = player.addAttachment(Main.getInstance());
        for (String permission: userData.getPermissions()) {
            permissionAttachment.setPermission(permission, true);
        }
        for (String permission: groupData.getPermissions()) {
            permissionAttachment.setPermission(permission, true);
        }
        Main.getInstance().getAttachments().put(player.getName(), permissionAttachment);
        player.recalculatePermissions();
        event.setJoinMessage(ChatColor.DARK_GRAY + "[" + ChatColor.DARK_GREEN + "+" + ChatColor.DARK_GRAY + "] " + ChatColor.GRAY + player.getName());
    }
}
