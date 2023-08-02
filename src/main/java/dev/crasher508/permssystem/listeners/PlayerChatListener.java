package dev.crasher508.permssystem.listeners;

import dev.crasher508.permssystem.dataTypes.GroupData;
import dev.crasher508.permssystem.dataTypes.UserData;
import dev.crasher508.permssystem.provider.MySQLProvider;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class PlayerChatListener implements Listener {

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        if (event.isCancelled())
            return;
        Player player = event.getPlayer();
        UserData userData = MySQLProvider.getUserData(player.getName());
        if (userData == null)
            return;
        System.out.println("Debug: " + userData.toString());
        GroupData groupData = MySQLProvider.getGroupData(userData.getGroupName());
        if (groupData == null)
            return;
        System.out.println("Debug: " + groupData.toString());
        String chatFormat = groupData.getChatMessageStyle()
                .replace("%PLAYER%", "%1$s")
                .replace("%MESSAGE%", "%2$s");
        event.setFormat(chatFormat);
    }
}
