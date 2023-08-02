package dev.crasher508.permssystem.dataTypes;

import java.util.ArrayList;

public class UserData {

    private final int id;

    private final String username;
    private String groupName;
    private final ArrayList<String> permissions;

    public UserData(int id, String username, String groupName, ArrayList<String> permissions) {
        this.id = id;
        this.username = username;
        this.groupName = groupName;
        this.permissions = permissions;
    }

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public ArrayList<String> getPermissions() {
        return permissions;
    }

    @Override
    public String toString() {
        return "UserData{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", groupName='" + groupName + '\'' +
                ", permissions=" + String.join(";", permissions) +
                '}';
    }
}
