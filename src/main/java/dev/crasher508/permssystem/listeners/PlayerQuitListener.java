package dev.crasher508.permssystem.listeners;

import dev.crasher508.permssystem.Main;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.permissions.PermissionAttachment;

public class PlayerQuitListener implements Listener {

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        PermissionAttachment permissionAttachment = Main.getInstance().getAttachments().remove(player.getName());
        if (permissionAttachment != null) {
            player.removeAttachment(permissionAttachment);
        }
        event.setQuitMessage(ChatColor.DARK_GRAY + "[" + ChatColor.RED + "-" + ChatColor.DARK_GRAY + "] " + ChatColor.GRAY + player.getName());
    }
}
