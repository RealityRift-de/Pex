package dev.crasher508.permssystem.dataTypes;

import org.bukkit.ChatColor;

import java.util.ArrayList;

public class GroupData {

    private final int id;
    private final String name;
    private String displayStyle;
    private String playerListStyle;
    private String chatMessageStyle;
    private final ArrayList<String> permissions;
    private ChatColor color;

    public GroupData(int id, String name, String displayStyle, String playerListStyle, String chatMessageStyle, ArrayList<String> permissions, ChatColor color) {
        this.id = id;
        this.name = name.substring(0, Math.min(name.length(), 16));
        this.displayStyle = displayStyle;
        this.playerListStyle = playerListStyle;
        this.chatMessageStyle = chatMessageStyle;
        this.permissions = permissions;
        this.color = color;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDisplayStyle() {
        return displayStyle;
    }

    public void setDisplayStyle(String displayStyle) {
        this.displayStyle = displayStyle;
    }

    public String getPlayerListStyle() {
        return playerListStyle;
    }

    public void setPlayerListStyle(String playerListStyle) {
        this.playerListStyle = playerListStyle;
    }

    public String getChatMessageStyle() {
        return chatMessageStyle;
    }

    public void setChatMessageStyle(String chatMessageStyle) {
        this.chatMessageStyle = chatMessageStyle;
    }

    public ArrayList<String> getPermissions() {
        return permissions;
    }

    public ChatColor getColor() {
        return color;
    }

    public void setColor(ChatColor color) {
        this.color = color;
    }

    @Override
    public String toString() {
        return "GroupData{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", displayStyle='" + displayStyle + '\'' +
                ", playerListStyle='" + playerListStyle + '\'' +
                ", chatMessageStyle='" + chatMessageStyle + '\'' +
                ", permissions=" + String.join(";", permissions) +
                ", color=" + color +
                '}';
    }
}
