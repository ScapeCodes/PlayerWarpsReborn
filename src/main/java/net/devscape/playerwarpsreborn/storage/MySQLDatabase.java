package net.devscape.playerwarpsreborn.storage;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import net.devscape.playerwarpsreborn.PlayerWarpsReborn;
import net.devscape.playerwarpsreborn.objects.PlayerWarp;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.json.JSONObject;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MySQLDatabase {

    protected final HikariConfig config = new HikariConfig();
    protected final HikariDataSource ds;

    public MySQLDatabase(String host, int port, String database, String username, String password, boolean ssl) {
        config.setIdleTimeout(PlayerWarpsReborn.getInstance().getConfig().getInt("data.mysql-pool-settings.timeouts.idle"));
        config.setMaxLifetime(PlayerWarpsReborn.getInstance().getConfig().getInt("data.mysql-pool-settings.timeouts.max-lifetime"));
        config.setConnectionTimeout(PlayerWarpsReborn.getInstance().getConfig().getInt("data.mysql-pool-settings.timeouts.connection"));
        config.setMinimumIdle(PlayerWarpsReborn.getInstance().getConfig().getInt("data.mysql-pool-settings.minimum-idle"));
        config.setRegisterMbeans(true);
        config.setMaximumPoolSize(PlayerWarpsReborn.getInstance().getConfig().getInt("data.mysql-pool-settings.maximum-pool-size"));

        config.setConnectionTestQuery("SELECT 1");
        config.setDataSourceClassName("com.mysql.cj.jdbc.MysqlDataSource");
        config.addDataSourceProperty("serverName", host);
        config.addDataSourceProperty("port", port);
        config.addDataSourceProperty("databaseName", database);
        config.addDataSourceProperty("user", username);
        config.addDataSourceProperty("password", password);

        config.addDataSourceProperty("characterEncoding", "utf8");

        // Prepare statement caching properties
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.addDataSourceProperty("useSSL", ssl);

        ds = new HikariDataSource(this.config);
        this.initialiseDatabase();
    }

    public Connection getConnection() throws SQLException {
        return ds.getConnection();
    }

    public void initialiseDatabase() {
        String warpTable = """
        CREATE TABLE IF NOT EXISTS `warp` (
            Name VARCHAR(255) NOT NULL,
            UUID VARCHAR(255) NOT NULL,
            Location TEXT NOT NULL,
            Description TEXT NOT NULL,
            Category TEXT NOT NULL,
            Visits INT NOT NULL DEFAULT 0,
            Icon TEXT NOT NULL,
            Password VARCHAR(255),
            PasswordMode BOOLEAN NOT NULL,
            IsLocked BOOLEAN NOT NULL,
            TotalRatingScore INT NOT NULL DEFAULT 0,
            NumberOfRatings INT NOT NULL DEFAULT 0,
            PRIMARY KEY (Name)
        )
        """;

        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(warpTable)) {
            preparedStatement.executeUpdate();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        try {
            ensureColumnsExist("warp", Map.of(
                    "Name", "VARCHAR(255) NOT NULL",
                    "UUID", "VARCHAR(255) NOT NULL",
                    "Description", "VARCHAR(255) NOT NULL",
                    "Category", "TEXT NOT NULL",
                    "Visits", "INT NOT NULL DEFAULT 0",
                    "Icon", "TEXT NOT NULL",
                    "Password", "VARCHAR(255)",
                    "PasswordMode", "BOOLEAN NOT NULL",
                    "IsLocked", "BOOLEAN NOT NULL",
                    "TotalRatingScore",  "INT NOT NULL DEFAULT 0"
            ));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void ensureColumnsExist(String tableName, Map<String, String> columns) throws SQLException {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            for (Map.Entry<String, String> column : columns.entrySet()) {
                String columnName = column.getKey();
                String columnType = column.getValue();

                String checkQuery = "SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS " +
                        "WHERE TABLE_NAME = '" + tableName + "' AND COLUMN_NAME = '" + columnName + "'";
                try (ResultSet rs = stmt.executeQuery(checkQuery)) {
                    if (rs.next() && rs.getInt(1) == 0) {
                        String alterQuery = "ALTER TABLE `" + tableName + "` ADD COLUMN `" + columnName + "` " + columnType;
                        stmt.execute(alterQuery);
                    }
                }
            }
        }
    }


    public boolean doesWarpExist(String name) {
        String query = "SELECT COUNT(*) FROM `warp` WHERE Name = ?";
        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, name);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void saveWarp(PlayerWarp warp) {
        String checkQuery = "SELECT COUNT(*) FROM `warp` WHERE Name = ?";
        String updateQuery = """
        UPDATE `warp`
        SET Location = ?, Description = ?, Category = ?, Visits = ?, Icon = ?, Password = ?, PasswordMode = ?, IsLocked = ?, TotalRatingScore = ?, NumberOfRatings = ?
        WHERE Name = ?;
        """;

        String insertQuery = """
        INSERT INTO `warp` (Name, UUID, Location, Description, Category, Visits, Icon, Password, PasswordMode, IsLocked, TotalRatingScore, NumberOfRatings)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);
        """;

        try (Connection connection = getConnection()) {
            boolean exists;
            try (PreparedStatement checkStatement = connection.prepareStatement(checkQuery)) {
                checkStatement.setString(1, warp.getName());
                try (ResultSet resultSet = checkStatement.executeQuery()) {
                    exists = resultSet.next() && resultSet.getInt(1) > 0;
                }
            }

            if (exists) {
                try (PreparedStatement updateStatement = connection.prepareStatement(updateQuery)) {
                    updateStatement.setString(1, serializeLocation(warp.getLocation()));
                    updateStatement.setString(2, warp.getDescription());
                    updateStatement.setString(3, warp.getCategory());
                    updateStatement.setInt(4, warp.getVisits());
                    updateStatement.setString(5, warp.getIcon());
                    updateStatement.setString(6, warp.getPassword());
                    updateStatement.setBoolean(7, warp.isPasswordMode());
                    updateStatement.setBoolean(8, warp.isLocked());
                    updateStatement.setInt(9, warp.getTotalRatingScore());
                    updateStatement.setInt(10, warp.getNumberOfRatings());
                    updateStatement.setString(11, warp.getName());
                    updateStatement.executeUpdate();
                }
            } else {
                try (PreparedStatement insertStatement = connection.prepareStatement(insertQuery)) {
                    insertStatement.setString(1, warp.getName());
                    insertStatement.setString(2, warp.getOwner().toString());
                    insertStatement.setString(3, serializeLocation(warp.getLocation()));
                    insertStatement.setString(4, warp.getDescription());
                    insertStatement.setString(5, warp.getCategory());
                    insertStatement.setInt(6, warp.getVisits());
                    insertStatement.setString(7, warp.getIcon());
                    insertStatement.setString(8, warp.getPassword());
                    insertStatement.setBoolean(9, warp.isPasswordMode());
                    insertStatement.setBoolean(10, warp.isLocked());
                    insertStatement.setInt(11, warp.getTotalRatingScore());
                    insertStatement.setInt(12, warp.getNumberOfRatings());
                    insertStatement.executeUpdate();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void close() {
        if (ds != null && !ds.isClosed()) {
            ds.close();
        }
    }

    // Remove a warp by name
    public boolean removeWarp(String name) {
        String query = "DELETE FROM `warp` WHERE Name = ?";
        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, name);
            int rowsAffected = preparedStatement.executeUpdate();
            return rowsAffected > 0; // Returns true if a warp was successfully removed
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false; // Returns false if an error occurs or no rows were deleted
    }

    // Load a warp by UUID
    public PlayerWarp loadWarp(UUID uuid) {
        String query = "SELECT * FROM `warp` WHERE Name = ?";
        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, uuid.toString());
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    String name = resultSet.getString("Name");
                    String locationData = resultSet.getString("Location");
                    Location location = deserializeLocation(locationData);
                    String description = resultSet.getString("Description");
                    String category = resultSet.getString("Category");
                    int visits = resultSet.getInt("Visits");
                    String icon = resultSet.getString("Icon");
                    String password = resultSet.getString("Password");
                    boolean passwordMode = resultSet.getBoolean("PasswordMode");
                    boolean isLocked = resultSet.getBoolean("IsLocked");
                    int totalRatingScore = resultSet.getInt("TotalRatingScore");
                    int numberOfRatings = resultSet.getInt("NumberOfRatings");

                    return new PlayerWarp(name, uuid, location, description, password, passwordMode, isLocked, category, icon, visits, totalRatingScore, numberOfRatings);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Load all warps into a list
    public List<PlayerWarp> loadAllWarps() {
        List<PlayerWarp> warps = new ArrayList<>();
        String query = "SELECT * FROM `warp`";

        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                String name = resultSet.getString("Name");
                UUID uuid = UUID.fromString(resultSet.getString("UUID"));
                String locationData = resultSet.getString("Location");
                Location location = deserializeLocation(locationData);
                String description = resultSet.getString("Description");
                String category = resultSet.getString("Category");
                int visits = resultSet.getInt("Visits");
                String icon = resultSet.getString("Icon");
                String password = resultSet.getString("Password");
                boolean passwordMode = resultSet.getBoolean("PasswordMode");
                boolean isLocked = resultSet.getBoolean("IsLocked");
                int totalRatingScore = resultSet.getInt("TotalRatingScore");
                int numberOfRatings = resultSet.getInt("NumberOfRatings");

                PlayerWarp warp = new PlayerWarp(name, uuid, location, description, password, passwordMode, isLocked, category, icon, visits, totalRatingScore, numberOfRatings);
                warps.add(warp);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        Bukkit.getConsoleSender().sendMessage("[PWR] loaded " + warps.size() + " warp(s) successfully.");

        return warps;
    }

    // Serialize a Location object to a storable string
    private String serializeLocation(Location location) {
        if (location == null) {
            return null;
        }
        JSONObject json = new JSONObject();
        json.put("world", location.getWorld().getName());
        json.put("x", location.getX());
        json.put("y", location.getY());
        json.put("z", location.getZ());
        json.put("yaw", location.getYaw());
        json.put("pitch", location.getPitch());
        return json.toString();
    }

    // Deserialize a stored string back into a Location object
    private Location deserializeLocation(String data) {
        if (data == null || data.isEmpty()) {
            return null;
        }
        try {
            JSONObject json = new JSONObject(data);
            String worldName = json.getString("world");
            World world = Bukkit.getWorld(worldName); // Ensure you handle null in case the world doesn't exist
            double x = json.getDouble("x");
            double y = json.getDouble("y");
            double z = json.getDouble("z");
            float yaw = (float) json.getDouble("yaw");
            float pitch = (float) json.getDouble("pitch");
            return new Location(world, x, y, z, yaw, pitch);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
