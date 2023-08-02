package dev.crasher508.permssystem.commands;

import dev.crasher508.permssystem.Main;
import dev.crasher508.permssystem.dataTypes.GroupData;
import dev.crasher508.permssystem.dataTypes.UserData;
import dev.crasher508.permssystem.provider.MySQLProvider;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class PexCommand implements TabExecutor {

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (!(commandSender instanceof Player player)) {
            commandSender.sendMessage(Main.getInstance().getPrefix() + Main.getInstance().getLanguage().get("use.ingame"));
            return true;
        }
        if (!player.hasPermission("pex.command")) {
            player.sendMessage(Main.getInstance().getPrefix() + Main.getInstance().getLanguage().get("no.perms"));
            return true;
        }
        if (strings.length > 0) {
            switch (strings[0]) {
                case "user" -> {
                    if (strings.length < 2) {
                        player.sendMessage(Main.getInstance().getPrefix() + ChatColor.RED + "Nutze: /pex user <username> <addperm|delperm|info|setgroup> <permission|group>");
                        return true;
                    }
                    Player target = Bukkit.getPlayer(strings[1]);
                    if (target == null) {
                        player.sendMessage(Main.getInstance().getPrefix() + ChatColor.RED + strings[1] + ChatColor.RESET + ChatColor.RED + " ist nicht Online!");
                        return true;
                    }
                    if (strings.length < 4) {
                        if (strings.length < 3) {
                            player.sendMessage(Main.getInstance().getPrefix() + ChatColor.RED + "Nutze: /pex user <username> <addperm|delperm|info|setgroup> <permission|group>");
                            return true;
                        } else {
                            if (!strings[2].equals("info")) {
                                player.sendMessage(Main.getInstance().getPrefix() + ChatColor.RED + "Nutze: /pex user <username> <addperm|delperm|info|setgroup> <permission|group>");
                                return true;
                            }
                        }
                    }
                    UserData userData = MySQLProvider.getUserData(target.getName());
                    if (userData == null) {
                        player.sendMessage(Main.getInstance().getPrefix() + ChatColor.RED + "Spielerdaten konnten nicht geladen werden!");
                        return true;
                    }
                    switch (strings[2]) {
                        case "addperm" -> {
                            if (userData.getPermissions().contains(strings[3])) {
                                player.sendMessage(Main.getInstance().getPrefix() + ChatColor.RED + "Dem Spieler wurde diese Berechtigung bereits erteilt!");
                                return true;
                            }
                            if (userData.getPermissions().size() > 9) {
                                player.sendMessage(Main.getInstance().getPrefix() + ChatColor.RED + "Es können höchstens 10 Sonderrechte hinzugefügt werden!");
                                return true;
                            }
                            userData.getPermissions().add(strings[3]);
                            if (MySQLProvider.updateUserData(userData)) {
                                player.sendMessage(Main.getInstance().getPrefix() + ChatColor.GOLD + "Berechtigung erfolgreich hinzugefügt.");
                                Main.getInstance().resetPermissions(target);
                            } else {
                                player.sendMessage(Main.getInstance().getPrefix() + ChatColor.RED + "Etwas ist schief gelaufen!");
                            }
                        }
                        case "delperm" -> {
                            if (!userData.getPermissions().contains(strings[3])) {
                                player.sendMessage(Main.getInstance().getPrefix() + ChatColor.RED + "Dem Spieler wurde diese Berechtigung nicht zugeteilt!");
                                return true;
                            }
                            userData.getPermissions().remove(strings[3]);
                            if (MySQLProvider.updateUserData(userData)) {
                                player.sendMessage(Main.getInstance().getPrefix() + ChatColor.GOLD + "Berechtigung erfolgreich entfernt.");
                                Main.getInstance().resetPermissions(target);
                            } else {
                                player.sendMessage(Main.getInstance().getPrefix() + ChatColor.RED + "Etwas ist schief gelaufen!");
                            }
                        }
                        case "info" -> {
                            player.sendMessage(ChatColor.RED + "Spieler Informationen:");
                            player.sendMessage(ChatColor.GOLD + "ID: " + userData.getId());
                            player.sendMessage(ChatColor.GOLD + "Name: " + userData.getUsername());
                            player.sendMessage(ChatColor.GOLD + "Gruppe: " + userData.getGroupName());
                            player.sendMessage(ChatColor.GOLD + "Berechtigungen: " + String.join(", ", userData.getPermissions()));
                        }
                        case "setgroup" -> {
                            if (!getGroupList().contains(strings[3])) {
                                player.sendMessage(Main.getInstance().getPrefix() + ChatColor.RED + "Es existiert keine Gruppe mit diesem Namen!");
                                return true;
                            }
                            userData.setGroupName(strings[3]);
                            if (MySQLProvider.updateUserData(userData)) {
                                player.sendMessage(Main.getInstance().getPrefix() + ChatColor.GOLD + "Gruppe erfolgreich gesetzt.");
                                Main.getInstance().resetPermissions(target);
                                GroupData groupData = MySQLProvider.getGroupData(userData.getGroupName());
                                if (groupData == null)
                                    return true;
                                target.setDisplayName(groupData.getDisplayStyle().replace("%PLAYER%", target.getName()));
                                target.setPlayerListName(groupData.getPlayerListStyle().replace("%PLAYER%", target.getName()));
                            } else {
                                player.sendMessage(Main.getInstance().getPrefix() + ChatColor.RED + "Etwas ist schief gelaufen!");
                            }
                        }
                        default -> player.sendMessage(Main.getInstance().getPrefix() + "Nutze: /pex user <username> <addperm|delperm|info|setgroup> <permission|group>");
                    }
                }
                case "group" -> {
                    if (strings.length < 2) {
                        player.sendMessage(Main.getInstance().getPrefix() + ChatColor.RED + "Nutze: /pex group <add|delete|edit|info>");
                        return true;
                    }
                    switch (strings[1]) {
                        case "add" -> {
                            if (strings.length < 6) {
                                player.sendMessage(Main.getInstance().getPrefix() + ChatColor.RED + "Nutze: /pex group add <groupname> <displayname> <playerlistname> <chatmessage>");
                                return true;
                            }
                            String groupName = strings[2];
                            String displayName = strings[3];
                            String playerListName = strings[4];
                            String chatMessage = strings[5];
                            if (groupName.isEmpty() || displayName.isEmpty() || playerListName.isEmpty() || chatMessage.isEmpty()) {
                                player.sendMessage(Main.getInstance().getPrefix() + ChatColor.RED + "Nutze: /pex group add <groupname> <displayname> <playerlistname> <chatmessage>");
                                return true;
                            }
                            displayName = displayName.replaceAll("__", " ").replaceAll("&", "§");
                            playerListName = playerListName.replaceAll("__", " ").replaceAll("&", "§");
                            chatMessage = chatMessage.replaceAll("__", " ").replaceAll("&", "§");
                            if (groupName.length() > 16) {
                                player.sendMessage(Main.getInstance().getPrefix() + ChatColor.RED + "Der Gruppenname darf höchsten 16 Zeichen lang sein!");
                                return true;
                            }
                            if (getGroupList().contains(groupName)) {
                                player.sendMessage(Main.getInstance().getPrefix() + ChatColor.RED + "Eine Gruppe mit diesem Namen existiert bereits!");
                                return true;
                            }
                            if (!displayName.contains("%PLAYER%") || !playerListName.contains("%PLAYER%") || !chatMessage.contains("%PLAYER%")) {
                                player.sendMessage(Main.getInstance().getPrefix() + ChatColor.RED + "Die Argumente \"displayname\", \"playerlistname\" und \"chatmessage\" müssen \"%PLAYER%\" enthalten!");
                                return true;
                            }
                            if (!chatMessage.contains("%MESSAGE%")) {
                                player.sendMessage(Main.getInstance().getPrefix() + ChatColor.RED + "Das Argument \"chatmessage\" muss \"%MESSAGE%\" enthalten!");
                                return true;
                            }
                            if (MySQLProvider.addGroupData(new GroupData(-1, groupName, displayName, playerListName, chatMessage, new ArrayList<>(), ChatColor.GOLD))) {
                                player.sendMessage(Main.getInstance().getPrefix() + ChatColor.GOLD + "Gruppe erfolgreich gespeichert.");
                            } else {
                                player.sendMessage(Main.getInstance().getPrefix() + ChatColor.RED + "Etwas ist schief gelaufen!");
                            }
                        }
                        case "delete" -> {
                            if (strings.length < 3) {
                                player.sendMessage(Main.getInstance().getPrefix() + ChatColor.RED + "Nutze: /pex group delete <groupname>");
                                return true;
                            }
                            GroupData groupData = MySQLProvider.getGroupData(strings[2]);
                            if (groupData == null) {
                                player.sendMessage(Main.getInstance().getPrefix() + ChatColor.RED + "Diese Gruppe existiert nicht!");
                                return true;
                            }
                            if (MySQLProvider.deleteGroupData(groupData)) {
                                player.sendMessage(Main.getInstance().getPrefix() + ChatColor.GOLD + "Gruppe erfolgreich gelöscht.");
                            } else {
                                player.sendMessage(Main.getInstance().getPrefix() + ChatColor.RED + "Etwas ist schief gelaufen!");
                            }
                        }
                        case "edit" -> {
                            if (strings.length < 5) {
                                player.sendMessage(Main.getInstance().getPrefix() + ChatColor.RED + "Nutze: /pex group edit <groupname> <addperm|delperm|setdisplayname|setplayerlistname|setchatmessage|setcolor> <value>");
                                return true;
                            }
                            String groupName = strings[2];
                            GroupData groupData = MySQLProvider.getGroupData(groupName);
                            if (groupData == null) {
                                player.sendMessage(Main.getInstance().getPrefix() + ChatColor.RED + "Es existiert keine Gruppe mit diesem Namen!");
                                return true;
                            }
                            switch (strings[3]){
                                case "addperm" -> {
                                    if (groupData.getPermissions().contains(strings[4])) {
                                        player.sendMessage(Main.getInstance().getPrefix() + ChatColor.RED + "Die Gruppe besitzt bereits eine Berechtigung mit diesem Namen!");
                                        return true;
                                    }
                                    if (groupData.getPermissions().size() > 29) {
                                        player.sendMessage(Main.getInstance().getPrefix() + ChatColor.RED + "Die Gruppe besitzt bereits 30 Berechtigung!");
                                        return true;
                                    }
                                    groupData.getPermissions().add(strings[4]);
                                }
                                case "delperm" -> {
                                    if (!groupData.getPermissions().contains(strings[4])) {
                                        player.sendMessage(Main.getInstance().getPrefix() + ChatColor.RED + "Die Gruppe besitzt keine Berechtigung mit diesem Namen!");
                                        return true;
                                    }
                                    groupData.getPermissions().remove(strings[4]);
                                }
                                case "setdisplayname" -> {
                                    if (!strings[4].contains("%PLAYER%")) {
                                        player.sendMessage(Main.getInstance().getPrefix() + ChatColor.RED + "Das Argument \"%PLAYER%\" muss enthalten sein!");
                                        return true;
                                    }
                                    groupData.setDisplayStyle(strings[4].replaceAll("__", " ").replaceAll("&", "§"));
                                }
                                case "setplayerlistname" -> {
                                    if (!strings[4].contains("%PLAYER%")) {
                                        player.sendMessage(Main.getInstance().getPrefix() + ChatColor.RED + "Das Argument \"%PLAYER%\" muss enthalten sein!");
                                        return true;
                                    }
                                    groupData.setPlayerListStyle(strings[4].replaceAll("__", " ").replaceAll("&", "§"));
                                }
                                case "setchatmessage" -> {
                                    if (!strings[4].contains("%PLAYER%")) {
                                        player.sendMessage(Main.getInstance().getPrefix() + ChatColor.RED + "Das Argument \"%PLAYER%\" muss enthalten sein!");
                                        return true;
                                    }
                                    if (!strings[4].contains("%MESSAGE%")) {
                                        player.sendMessage(Main.getInstance().getPrefix() + ChatColor.RED + "Das Argument \"%MESSAGE%\" muss enthalten sein!");
                                        return true;
                                    }
                                    groupData.setChatMessageStyle(strings[4].replaceAll("__", " ").replaceAll("&", "§"));
                                }
                                case "setcolor" -> {
                                    if (strings[4].length() != 1) {
                                        player.sendMessage(Main.getInstance().getPrefix() + ChatColor.RED + "Die Farbe darf nur ein Zeichen umfassen!");
                                        return true;
                                    }
                                    char colorChar = strings[4].charAt(0);
                                    ChatColor chatColor = ChatColor.getByChar(colorChar);
                                    if (chatColor == null) {
                                        player.sendMessage(Main.getInstance().getPrefix() + ChatColor.RED + "Die gewählte Farbe existiert nicht!");
                                        return true;
                                    }
                                    groupData.setColor(chatColor);
                                }
                                default -> {
                                    player.sendMessage(Main.getInstance().getPrefix() + ChatColor.RED + "Nutze: /pex group edit <groupname> <addperm|delperm|setdisplayname|setplayerlistname|setchatmessage> <value>");
                                    return true;
                                }
                            }
                            if (MySQLProvider.updateGroupData(groupData)) {
                                player.sendMessage(Main.getInstance().getPrefix() + ChatColor.GOLD + "Gruppe erfolgreich aktualisiert.");
                                for (Player online: Bukkit.getOnlinePlayers()) {
                                    Main.getInstance().resetPermissions(online);
                                }
                            } else {
                                player.sendMessage(Main.getInstance().getPrefix() + ChatColor.RED + "Etwas ist schief gelaufen!");
                            }
                        }
                        case "info" -> {
                            if (strings.length < 3) {
                                player.sendMessage(Main.getInstance().getPrefix() + ChatColor.RED + "Nutze: /pex group info <groupname>");
                                return true;
                            }
                            GroupData groupData = MySQLProvider.getGroupData(strings[2]);
                            if (groupData == null) {
                                player.sendMessage(Main.getInstance().getPrefix() + ChatColor.RED + "Diese Gruppe existiert nicht!");
                                return true;
                            }
                            player.sendMessage(ChatColor.RED + "Gruppen Informationen:");
                            player.sendMessage(ChatColor.GOLD + "ID: " + groupData.getId());
                            player.sendMessage(ChatColor.GOLD + "Name: " + groupData.getName());
                            player.sendMessage(ChatColor.GOLD + "DisplayNameStyle: " + groupData.getDisplayStyle());
                            player.sendMessage(ChatColor.GOLD + "PlayerListStyle: " + groupData.getPlayerListStyle());
                            player.sendMessage(ChatColor.GOLD + "ChatMessageStyle: " + groupData.getChatMessageStyle());
                            player.sendMessage(ChatColor.GOLD + "Berechtigungen: " + String.join(", ", groupData.getPermissions()));
                            player.sendMessage(ChatColor.GOLD + "ChatColor: " + groupData.getColor().getChar());
                        }
                        default -> player.sendMessage(Main.getInstance().getPrefix() + ChatColor.RED + "Nutze: /pex group <add|delete|edit>");
                    }
                }
                case "setdefaultgroup" -> {
                    if (strings.length < 2) {
                        player.sendMessage(Main.getInstance().getPrefix() + ChatColor.RED + "Nutze: /pex setdefaultgroup <group>");
                        return true;
                    }
                    if (!getGroupList().contains(strings[1])) {
                        player.sendMessage(Main.getInstance().getPrefix() + ChatColor.RED + "Es existiert keine Gruppe mit diesem Namen!");
                        return true;
                    }
                    FileConfiguration configuration = Main.getInstance().getConfig();
                    configuration.set("defaultGroupName", strings[1]);
                    Main.getInstance().saveConfig();
                    Main.getInstance().reloadConfig();
                    Main.getInstance().setDefaultGroupName(strings[1]);
                    player.sendMessage(Main.getInstance().getPrefix() + ChatColor.GOLD + "Die Standardgruppe wurde zu " + ChatColor.RED + strings[1] + ChatColor.GOLD + " geändert.");
                }
                default -> {
                    player.sendMessage(Main.getInstance().getPrefix() + ChatColor.BOLD + ChatColor.RED + "Nutze:");
                    player.sendMessage(ChatColor.GOLD + "/pex user <username> <addperm|delperm> <permission>");
                    player.sendMessage(ChatColor.GOLD + "/pex user <username> setgroup <groupname>");
                    player.sendMessage(" ");
                    player.sendMessage(ChatColor.RED + "displayname, playerlistname und chatmessage müssen %PLAYER% enthalten, letzteres zusätzlich %MESSAGE%");
                    player.sendMessage(ChatColor.RED + "Leerzeichen sind bitte als \"__\" und Farbzeichen als & zu schreiben!");
                    player.sendMessage(ChatColor.GOLD + "/pex group add <groupname> <displayname> <playerlistname> <chatmessage>");
                    player.sendMessage(ChatColor.GOLD + "/pex group delete <groupname>");
                    player.sendMessage(ChatColor.GOLD + "/pex group edit <groupname> <addperm|delperm|setdisplayname|setplayerlistname|setchatmessage|setcolor> <value>");
                    player.sendMessage(ChatColor.GOLD + "/pex group info <groupname>");
                    player.sendMessage(" ");
                    player.sendMessage(ChatColor.GOLD + "/pex setdefaultgroup <group>");
                    player.sendMessage(" ");
                    player.sendMessage(ChatColor.BLUE + "Autor: Crasher508 - BETA 2 - wird entfernt!");
                }
            }
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
        List<String> completions = new ArrayList<>();
        if (!commandSender.hasPermission("pex.command"))
            return completions;
        if (strings.length == 1) {
            List<String> commands = new ArrayList<>(Arrays.asList("user", "group", "help", "setdefaultgroup"));
            StringUtil.copyPartialMatches(strings[0], commands, completions);
        } else if (strings.length == 2) {
            if (strings[0].equals("user")) {
                StringUtil.copyPartialMatches(strings[1], getPlayerList(), completions);
            } else if (strings[0].equals("group")) {
                List<String> commands = new ArrayList<>(Arrays.asList("add", "delete", "edit", "info"));
                StringUtil.copyPartialMatches(strings[1], commands, completions);
            } else if (strings[0].equals("setdefaultgroup")) {
                StringUtil.copyPartialMatches(strings[1], getGroupList(), completions);
            }
        } else if (strings.length == 3) {
            if (strings[0].equals("user")) {
                List<String> commands = new ArrayList<>(Arrays.asList("addperm", "delperm", "info", "setgroup"));
                StringUtil.copyPartialMatches(strings[2], commands, completions);
            } else if (strings[0].equals("group")) {
                if (strings[1].equals("delete")) {
                    StringUtil.copyPartialMatches(strings[2], getGroupList(), completions);
                } else if (strings[1].equals("edit")) {
                    StringUtil.copyPartialMatches(strings[2], getGroupList(), completions);
                } else if (strings[1].equals("info")) {
                    StringUtil.copyPartialMatches(strings[2], getGroupList(), completions);
                }
            }
        } else if (strings.length == 4) {
            if (strings[0].equals("user")) {
                if (strings[2].equals("setgroup")) {
                    StringUtil.copyPartialMatches(strings[3], getGroupList(), completions);
                } else if (strings[2].equals("delperm")) {
                    StringUtil.copyPartialMatches(strings[3], getPermissionList(strings[1]), completions);
                }
            } else if (strings[0].equals("group")) {
                if (strings[1].equals("edit")) {
                    List<String> commands = new ArrayList<>(Arrays.asList("addperm", "delperm", "setdisplayname", "setplayerlistname", "setchatmessage", "setcolor"));
                    StringUtil.copyPartialMatches(strings[3], commands, completions);
                }
            }
        }
        Collections.sort(completions);
        return completions;
    }

    public List<String> getPlayerList() {
        List<String> list = new ArrayList<>();
        for (Player p : Bukkit.getOnlinePlayers()) {
            list.add(p.getName());
        }
        return list;
    }

    public List<String> getGroupList() {
        return MySQLProvider.getGroupNames();
    }

    public List<String> getPermissionList(String playerName) {
        Player player = Bukkit.getPlayer(playerName);
        if (player == null)
            return new ArrayList<>();
        UserData userData = MySQLProvider.getUserData(player.getName());
        if (userData == null)
            return new ArrayList<>();
        return userData.getPermissions();
    }
}
