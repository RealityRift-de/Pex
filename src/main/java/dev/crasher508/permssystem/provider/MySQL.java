package dev.crasher508.permssystem.provider;

import dev.crasher508.permssystem.Main;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.sql.*;

public class MySQL {

    public static Connection connection;

    private static boolean logged = false;

    // connect
    public static void connect() {
        FileConfiguration configuration = Main.getInstance().getConfig();
        if (!isConnected()) {
            String host = configuration.getString("mysql.host", "127.0.0.1");
            int port = configuration.getInt("mysql.port", 3306);
            String username = configuration.getString("mysql.username", "root");
            String password = configuration.getString("mysql.password", "");
            String database = configuration.getString("mysql.database", "pex");
            try {
                String url = "jdbc:mysql://" + host + ":" + port + "/" + database + "?autoReconnect=true";
                connection = DriverManager.getConnection(url, username, password);
                if (!logged) {
                    Bukkit.getConsoleSender().sendMessage(Main.getInstance().getPrefix() + "MySQL-Verbindung als " + username + "@" + host + ":" + port + " geöffnet!");
                    logged = true;
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static void disconnect() {
        if (isConnected()) {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static boolean isConnected() {
        return (connection != null);
    }

    public static void update(String query) throws SQLException {
        if (!isConnected())
            return;
        PreparedStatement ps = connection.prepareStatement(query);
        ps.executeUpdate();
    }

    public static ResultSet getResult(String query) throws SQLException {
        if (!isConnected())
            return null;
        return connection.prepareStatement(query).executeQuery();
    }
}
