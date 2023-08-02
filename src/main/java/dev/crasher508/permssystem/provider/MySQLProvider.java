package dev.crasher508.permssystem.provider;

import dev.crasher508.permssystem.Main;
import dev.crasher508.permssystem.dataTypes.GroupData;
import dev.crasher508.permssystem.dataTypes.UserData;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MySQLProvider {

    public static boolean init() {
        if (!MySQL.isConnected())
            return false;
        boolean error = false;
        try {
            MySQL.update("CREATE TABLE IF NOT EXISTS groupData (`id` INT NOT NULL AUTO_INCREMENT, name VARCHAR(16), displayStyle TEXT, playerListStyle TEXT, chatMessageStyle TEXT, color TEXT, PRIMARY KEY (`id`));");
            MySQL.update("CREATE TABLE IF NOT EXISTS userGroupData (`id` INT NOT NULL AUTO_INCREMENT, username VARCHAR(16), groupName TEXT, PRIMARY KEY (`id`));");
            MySQL.update("CREATE TABLE IF NOT EXISTS permissions (`id` INT NOT NULL AUTO_INCREMENT, permission TEXT, PRIMARY KEY (`id`));");
            MySQL.update("CREATE TABLE IF NOT EXISTS userPermissions (`id` INT NOT NULL AUTO_INCREMENT, `userId` INT NOT NULL, permissionId INT NOT NULL, PRIMARY KEY (`id`));");
            MySQL.update("CREATE TABLE IF NOT EXISTS groupPermissions (`id` INT NOT NULL AUTO_INCREMENT, `groupId` INT NOT NULL, permissionId INT NOT NULL, PRIMARY KEY (`id`));");
        } catch (SQLException exception) {
            System.err.println(exception.getMessage());
            error = true;
        }
        if (!error) {
            if (getGroupNames().size() < 1) {
                FileConfiguration configuration = Main.getInstance().getConfig();
                String groupName = configuration.getString("defaultGroup.name", "");
                String displayName = configuration.getString("defaultGroup.displayStyle", "");
                String playerListName = configuration.getString("defaultGroup.playerListStyle", "");
                String chatMessage = configuration.getString("defaultGroup.chatMessageStyle", "");
                String color = configuration.getString("defaultGroup.color", "6");
                if (groupName.isEmpty() || displayName.isEmpty() || playerListName.isEmpty() || chatMessage.isEmpty() || color.isEmpty()) {
                    Main.getInstance().getLogger().warning("Fehler in der Konfiguration der Standardgruppe!");
                    return false;
                }
                if (groupName.length() > 16) {
                    Main.getInstance().getLogger().warning("Der Gruppenname der Standardgruppe darf höchsten 16 Zeichen lang sein!");
                    return false;
                }
                if (!displayName.contains("%PLAYER%") || !playerListName.contains("%PLAYER%") || !chatMessage.contains("%PLAYER%")) {
                    Main.getInstance().getLogger().warning("Fehler in der Konfiguration der Standardgruppe: Die Argumente \"displayname\", \"playerlistname\" und \"chatmessage\" müssen \"%PLAYER%\" enthalten!");
                    return true;
                }
                if (!chatMessage.contains("%MESSAGE%")) {
                    Main.getInstance().getLogger().warning("Fehler in der Konfiguration der Standardgruppe: Das Argument \"chatmessage\" muss \"%MESSAGE%\" enthalten!");
                    return true;
                }
                if (color.length() != 1) {
                    Main.getInstance().getLogger().warning("Fehler in der Konfiguration der Standardgruppe: Die Farbe darf nur ein Zeichen umfassen!");
                    return false;
                }
                char colorChar = color.charAt(0);
                ChatColor chatColor = ChatColor.getByChar(colorChar);
                if (chatColor == null) {
                    Main.getInstance().getLogger().warning("Fehler in der Konfiguration der Standardgruppe: Die Farbe existiert nicht!");
                    return false;
                }
                error = !addGroupData(new GroupData(-1, groupName, displayName, playerListName, chatMessage, new ArrayList<>(), chatColor));
            }
        }
        return !error;
    }

    public static UserData getUserData(String username) {
        UserData userData = null;
        try {
            ResultSet result = MySQL.getResult("SELECT * FROM userGroupData WHERE username = '" + username + "';");
            if (result != null) {
                while (result.next()) {
                    int id = result.getInt("id");
                    String foundUsername = result.getString("username");
                    String groupName = result.getString("groupName");
                    if (foundUsername == null || groupName == null)
                        continue;
                    if (!foundUsername.equals(username))
                        continue;
                    userData = new UserData(id, foundUsername, groupName, new ArrayList<>());
                }
            }
            if (userData != null) {
                ArrayList<Integer> permissionIds = getUserPermissionIds(userData.getId());
                ArrayList<String> permissions = new ArrayList<>();
                for (int permissionId: permissionIds) {
                    String permission = getPermission(permissionId);
                    if (permission == null)
                        continue;
                    if (permission.isEmpty())
                        continue;
                    if (permissions.contains(permission))
                        continue;
                    permissions.add(permission);
                }
                userData.getPermissions().addAll(permissions);
            }
        } catch (SQLException exception) {
            System.out.println(exception.getMessage());
        }
        return userData;
    }

    public static boolean updateUserData(UserData userData) {
        if (!MySQL.isConnected())
            return false;
        boolean error = false;
        try {
            MySQL.update("UPDATE userGroupData SET groupName = '" + userData.getGroupName() + "' WHERE username = '" + userData.getUsername() + "';");
        } catch (SQLException exception) {
            System.out.println(exception.getMessage());
            error = true;
        }
        if (!error) {
            if (!resetUserPermission(userData.getId())) {
                return false;
            }
            if (!userData.getPermissions().isEmpty()) {
                for (String permission: userData.getPermissions()) {
                    int id = getPermissionId(permission);
                    if (id == -1) {
                        if (!addPermission(permission)) {
                            error = true;
                            break;
                        } else {
                            id = getPermissionId(permission);
                            if (id == -1) {
                                error = true;
                                break;
                            }
                        }
                    }
                    if (!addUserPermission(userData.getId(), id)) {
                        error = true;
                        break;
                    }
                }
            }
        }
        return !error;
    }

    public static boolean addUserData(UserData userData) {
        if (!MySQL.isConnected())
            return false;
        boolean error = false;
        try {
            MySQL.update("INSERT INTO userGroupData (username, groupName) VALUES ('" + userData.getUsername() + "','" + userData.getGroupName() + "');");
        } catch (SQLException exception) {
            System.out.println(exception.getMessage());
            error = true;
        }
        if (!error) {
            if (!userData.getPermissions().isEmpty()) {
                for (String permission: userData.getPermissions()) {
                    int id = getPermissionId(permission);
                    if (id == -1) {
                        if (!addPermission(permission)) {
                            error = true;
                            break;
                        } else {
                            id = getPermissionId(permission);
                            if (id == -1) {
                                error = true;
                                break;
                            }
                        }
                    }
                    if (!addUserPermission(userData.getId(), id)) {
                        error = true;
                        break;
                    }
                }
            }
        }
        return !error;
    }

    public static boolean deleteUserData(UserData userData) {
        if (!MySQL.isConnected())
            return false;
        boolean error = false;
        try {
            MySQL.update("DELETE FROM userGroupData WHERE id = '" + String.valueOf(userData.getId()) + "' AND username = '" + userData.getUsername() + "';");
        } catch (SQLException exception) {
            System.out.println(exception.getMessage());
            error = true;
        }
        if (!error) {
            if (!resetUserPermission(userData.getId()))
                error = true;
        }
        return !error;
    }

    public static List<String> getGroupNames() {
        List<String> groupNames = new ArrayList<>();
        try {
            ResultSet result = MySQL.getResult("SELECT name FROM groupData;");
            if (result != null) {
                while (result.next()) {
                    String name = result.getString("name");
                    if (name == null)
                        continue;
                    if (groupNames.contains(name))
                        continue;
                    groupNames.add(name);
                }
            }
        } catch (SQLException exception) {
            System.out.println(exception.getMessage());
        }
        return groupNames;
    }

    public static GroupData getGroupData(String name) {
        GroupData groupData = null;
        try {
            ResultSet result = MySQL.getResult("SELECT * FROM groupData WHERE name = '" + name + "';");
            if (result != null) {
                while (result.next()) {
                    int id = result.getInt("id");
                    String foundName = result.getString("name");
                    String displayStyle = result.getString("displayStyle");
                    String playerListStyle = result.getString("playerListStyle");
                    String chatMessageStyle = result.getString("chatMessageStyle");
                    String color = result.getString("color");
                    if (foundName == null || displayStyle == null || playerListStyle == null || chatMessageStyle == null || color == null)
                        continue;
                    if (!foundName.equals(name))
                        continue;
                    if (color.length() < 1)
                        continue;
                    char colorChar = color.charAt(0);
                    ChatColor chatColor = ChatColor.getByChar(colorChar);
                    if (chatColor == null)
                        continue;
                    groupData = new GroupData(id, foundName, displayStyle, playerListStyle, chatMessageStyle, new ArrayList<>(), chatColor);
                }
            }
            if (groupData != null) {
                ArrayList<Integer> permissionIds = getGroupPermissionIds(groupData.getId());
                ArrayList<String> permissions = new ArrayList<>();
                for (int permissionId: permissionIds) {
                    String permission = getPermission(permissionId);
                    if (permission == null)
                        continue;
                    if (permission.isEmpty())
                        continue;
                    if (permissions.contains(permission))
                        continue;
                    permissions.add(permission);
                }
                groupData.getPermissions().addAll(permissions);
            }
        } catch (SQLException exception) {
            System.out.println(exception.getMessage());
        }
        return groupData;
    }

    public static boolean updateGroupData(GroupData groupData) {
        if (!MySQL.isConnected())
            return false;
        boolean error = false;
        try {
            MySQL.update("UPDATE groupData SET displayStyle = '" + groupData.getDisplayStyle() + "', playerListStyle = '" + groupData.getPlayerListStyle() + "', chatMessageStyle = '" + groupData.getChatMessageStyle() + "', color = '" + groupData.getColor().getChar() + "' WHERE name = '" + groupData.getName() + "';");
        } catch (SQLException exception) {
            System.out.println(exception.getMessage());
            error = true;
        }
        if (!error) {
            if (!resetGroupPermission(groupData.getId())) {
                return false;
            }
            if (!groupData.getPermissions().isEmpty()) {
                for (String permission: groupData.getPermissions()) {
                    int id = getPermissionId(permission);
                    if (id == -1) {
                        if (!addPermission(permission)) {
                            error = true;
                            break;
                        } else {
                            id = getPermissionId(permission);
                            if (id == -1) {
                                error = true;
                                break;
                            }
                        }
                    }
                    if (!addGroupPermission(groupData.getId(), id)) {
                        error = true;
                        break;
                    }
                }
            }
        }
        return !error;
    }

    public static boolean addGroupData(GroupData groupData) {
        if (!MySQL.isConnected())
            return false;
        boolean error = false;
        try {
            MySQL.update("INSERT INTO groupData (name, displayStyle, playerListStyle, chatMessageStyle, color) VALUES ('" + groupData.getName() + "','" + groupData.getDisplayStyle() + "','" + groupData.getPlayerListStyle() + "','" + groupData.getChatMessageStyle() + "','" + groupData.getColor().getChar() + "');");
        } catch (SQLException exception) {
            System.out.println(exception.getMessage());
            error = true;
        }
        if (!error) {
            if (!groupData.getPermissions().isEmpty()) {
                for (String permission: groupData.getPermissions()) {
                    int id = getPermissionId(permission);
                    if (id == -1) {
                        if (!addPermission(permission)) {
                            error = true;
                            break;
                        } else {
                            id = getPermissionId(permission);
                            if (id == -1) {
                                error = true;
                                break;
                            }
                        }
                    }
                    if (!addGroupPermission(groupData.getId(), id)) {
                        error = true;
                        break;
                    }
                }
            }
        }
        return !error;
    }

    public static boolean deleteGroupData(GroupData groupData) {
        if (!MySQL.isConnected())
            return false;
        boolean error = false;
        try {
            MySQL.update("DELETE FROM groupData WHERE name = '" + groupData.getName() + "';");
        } catch (SQLException exception) {
            System.out.println(exception.getMessage());
            error = true;
        }
        if (!error) {
            if (!resetGroupPermission(groupData.getId()))
                error = true;
        }
        return !error;
    }

    private static String getPermission(int permissionId) {
        try {
            ResultSet result = MySQL.getResult("SELECT permission FROM permissions WHERE id = '" + String.valueOf(permissionId) + "';");
            if (result != null) {
                String permission = null;
                while (result.next()) {
                    permission = result.getString("permission");
                }
                return permission;
            }
        } catch (SQLException exception) {
            System.out.println(exception.getMessage());
        }
        return null;
    }

    private static int getPermissionId(String permission) {
        try {
            ResultSet result = MySQL.getResult("SELECT id FROM permissions WHERE permission = '" + permission + "';");
            if (result != null) {
                int id = -1;
                while (result.next()) {
                    id = result.getInt("id");
                }
                if (id != -1)
                    return id;
            }
        } catch (SQLException exception) {
            System.out.println(exception.getMessage());
        }
        return -1;
    }

    private static boolean addPermission(String permission) {
        if (!MySQL.isConnected())
            return false;
        try {
            MySQL.update("INSERT INTO permissions (permission) VALUES ('" + permission + "');");
            return true;
        } catch (SQLException exception) {
            System.out.println(exception.getMessage());
            return false;
        }
    }

    private static boolean deletePermission(int id) {
        if (!MySQL.isConnected())
            return false;
        try {
            MySQL.update("DELETE FROM permissions WHERE id = '" + String.valueOf(id) + "';");
            return true;
        } catch (SQLException exception) {
            System.out.println(exception.getMessage());
            return false;
        }
    }

    private static ArrayList<Integer> getUserPermissionIds(int userId) {
        ArrayList<Integer> permissionIds = new ArrayList<>();
        if (!MySQL.isConnected())
            return permissionIds;
        try {
            ResultSet result = MySQL.getResult("SELECT permissionId FROM userPermissions WHERE userId = '" + userId + "';");
            if (result != null) {
                while (result.next()) {
                    int permissionId = result.getInt("permissionId");
                    if (permissionIds.contains(permissionId))
                        continue;
                    permissionIds.add(permissionId);
                }
            }
        } catch (SQLException exception) {
            System.out.println(exception.getMessage());
        }
        return permissionIds;
    }

    private static boolean addUserPermission(int userId , int permissionId) {
        if (!MySQL.isConnected())
            return false;
        try {
            MySQL.update("INSERT INTO userPermissions (userId, permissionId) VALUES ('" + String.valueOf(userId) + "','" + String.valueOf(permissionId) + "');");
            return true;
        } catch (SQLException exception) {
            System.out.println(exception.getMessage());
            return false;
        }
    }

    private static boolean resetUserPermission(int userId) {
        if (!MySQL.isConnected())
            return false;
        try {
            MySQL.update("DELETE FROM userPermissions WHERE userId = '" + String.valueOf(userId) + "';");
            return true;
        } catch (SQLException exception) {
            System.out.println(exception.getMessage());
            return false;
        }
    }

    private static ArrayList<Integer> getGroupPermissionIds(int groupId) {
        ArrayList<Integer> permissionIds = new ArrayList<>();
        if (!MySQL.isConnected())
            return permissionIds;
        try {
            ResultSet result = MySQL.getResult("SELECT permissionId FROM groupPermissions WHERE groupId = '" + groupId + "';");
            if (result != null) {
                while (result.next()) {
                    int permissionId = result.getInt("permissionId");
                    if (permissionIds.contains(permissionId))
                        continue;
                    permissionIds.add(permissionId);
                }
            }
        } catch (SQLException exception) {
            System.out.println(exception.getMessage());
        }
        return permissionIds;
    }

    private static boolean addGroupPermission(int groupId , int permissionId) {
        if (!MySQL.isConnected())
            return false;
        try {
            MySQL.update("INSERT INTO groupPermissions (groupId, permissionId) VALUES ('" + String.valueOf(groupId) + "','" + String.valueOf(permissionId) + "');");
            return true;
        } catch (SQLException exception) {
            System.out.println(exception.getMessage());
            return false;
        }
    }

    private static boolean resetGroupPermission(int groupId) {
        if (!MySQL.isConnected())
            return false;
        try {
            MySQL.update("DELETE FROM groupPermissions WHERE groupId = '" + String.valueOf(groupId) + "';");
            return true;
        } catch (SQLException exception) {
            System.out.println(exception.getMessage());
            return false;
        }
    }
}
