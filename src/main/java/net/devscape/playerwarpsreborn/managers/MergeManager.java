package net.devscape.playerwarpsreborn.managers;

import net.devscape.playerwarpsreborn.PlayerWarpsReborn;
import net.devscape.playerwarpsreborn.objects.PlayerWarp;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Objects;
import java.util.UUID;

import static net.devscape.playerwarpsreborn.utils.Utils.msgPlayer;

public class MergeManager implements ConverterBase {

    private final String url = "jdbc:sqlite:" + "plugins/PlayerWarps/data/database.db";

    public Connection getConnection() {
        try {
            return DriverManager.getConnection(url);
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    @Override
    public void run(Player sender) {
        final String sql = "SELECT name, uuid, world, x, y, z, pitch, yaw, description, date FROM playerwarps_warps;";
        int warps = 0;
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(UUID.fromString(rs.getString(2)));

                    warps++;
                    Location location = new Location(Bukkit.getWorld(rs.getString(3)), rs.getDouble(4), rs.getDouble(5), rs.getDouble(6), rs.getFloat(7), rs.getFloat(8));

                    PlayerWarp warp = new PlayerWarp(rs.getString(1), offlinePlayer.getUniqueId(), location, rs.getString(9), "N/A", false, false, PlayerWarpsReborn.getInstance().getConfig().getString("settings.default-category"), "RED_BED", 0, 0, 0);
                    PlayerWarpsReborn.getInstance().getPlayerWarpManager().saveWarp(warp);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        PlayerWarpsReborn.getInstance().getPlayerWarpManager().loadWarps();
        msgPlayer(sender, "&#009dff&lPWR &8&l➟ &#80ceff" + warps + " warps successfully merged.");
    }

    public void merge(Player player) {
        File playerwarpsFile = new File(Bukkit.getServer().getWorldContainer().getAbsolutePath() + "/plugins/PlayerWarps/data.yml");

        if (doesDatabaseExist()) {
            run(player);
            return;
        }

        // Check if the file exists
        if (!playerwarpsFile.exists()) {
            msgPlayer(player, "&cPlayerWarps data.yml file not found!");
            return;
        }

        try {
            // Sanitize the YAML file by removing problematic lines
            String cleanedData = cleanYaml(playerwarpsFile);

            // Load the cleaned data into a FileConfiguration object
            FileConfiguration warpsConfig = YamlConfiguration.loadConfiguration(new StringReader(cleanedData));

            // Check for the warps section
            if (!warpsConfig.contains("warps")) {
                msgPlayer(player, "&cNo warps section found in data.yml!");
                return;
            }

            int count = 0;

            // Iterate through each warp in the configuration
            for (String key : warpsConfig.getConfigurationSection("warps").getKeys(false)) {
                String path = "warps." + key;

                try {
                    // Extract warp location details
                    String worldName = warpsConfig.getString(path + ".loc.world");
                    double x = warpsConfig.getDouble(path + ".loc.x");
                    double y = warpsConfig.getDouble(path + ".loc.y");
                    double z = warpsConfig.getDouble(path + ".loc.z");
                    float pitch = (float) warpsConfig.getDouble(path + ".loc.pitch");
                    float yaw = (float) warpsConfig.getDouble(path + ".loc.yaw");

                    // Get warp name
                    String name = warpsConfig.getString(path + ".name");
                    if (name == null || name.isEmpty()) {
                        Bukkit.getLogger().warning("Warp " + key + " skipped: Missing or invalid 'name'.");
                        continue;
                    }

                    // Get owner ID
                    UUID owner = UUID.fromString(warpsConfig.getString(path + ".owner-id"));

                    // Get category with a fallback to default
                    String category = Objects.requireNonNullElse(
                            warpsConfig.getString(path + ".category"),
                            PlayerWarpsReborn.getInstance().getConfig().getString("settings.default-category")
                    );

                    // Get visits count
                    int visits = warpsConfig.isSet(path + ".visits") ? warpsConfig.getInt(path + ".visits") : 0;

                    // Get item icon with a fallback
                    String icon = "RED_BED";
                    String itemType = warpsConfig.getString(path + ".item");
                    if (itemType != null && !"null".equalsIgnoreCase(itemType)) {
                        icon = warpsConfig.getString(path + ".item.type", "RED_BED");
                    }

                    // Create the warp location
                    Location location = new Location(Bukkit.getWorld(worldName), x, y, z, yaw, pitch);

                    // Create and save the warp
                    PlayerWarp warp = new PlayerWarp(
                            name, owner, location, Bukkit.getOfflinePlayer(owner).getName() + "'s player warp!",
                            "N/A", false, false, category, icon, visits, 0, 0
                    );

                    PlayerWarpsReborn.getInstance().getPlayerWarpManager().saveWarp(warp);
                    count++;
                } catch (Exception e) {
                    Bukkit.getLogger().warning("Error processing warp " + key + ": " + e.getMessage());
                }
            }

            PlayerWarpsReborn.getInstance().getPlayerWarpManager().loadWarps();

            msgPlayer(player, "&#009dff&lPWR &8&l➟ &#80ceff" + count + " warps successfully merged.");
        } catch (IOException e) {
            msgPlayer(player, "&cFailed to read data.yml: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Cleans the YAML file by removing lines starting with "==:".
     *
     * @param file The file to clean.
     * @return A sanitized YAML string.
     * @throws IOException If an I/O error occurs.
     */
    private String cleanYaml(File file) throws IOException {
        StringBuilder cleanedData = new StringBuilder();
        for (String line : Files.readAllLines(file.toPath())) {
            if (!line.trim().startsWith("==:")) { // Ignore lines starting with "==:"
                cleanedData.append(line).append(System.lineSeparator());
            }
        }
        return cleanedData.toString();
    }

    public void merge() {
        File playerwarpsFile = new File(Bukkit.getServer().getWorldContainer().getAbsolutePath() + "/plugins/PlayerWarps/data.yml");

        // Check if the file exists
        if (!playerwarpsFile.exists()) {
            Bukkit.getLogger().warning("PlayerWarps data.yml file not found!");
            return;
        }

        try {
            // Sanitize the YAML file by removing problematic lines
            String cleanedData = cleanYaml(playerwarpsFile);

            // Load the cleaned data into a FileConfiguration object
            FileConfiguration warpsConfig = YamlConfiguration.loadConfiguration(new StringReader(cleanedData));

            // Check for the warps section
            if (!warpsConfig.contains("warps")) {
                Bukkit.getLogger().warning("No warps section found in data.yml!");
                return;
            }

            int count = 0;

            // Iterate through each warp in the configuration
            for (String key : warpsConfig.getConfigurationSection("warps").getKeys(false)) {
                String path = "warps." + key;

                try {
                    // Extract warp location details
                    String worldName = warpsConfig.getString(path + ".loc.world");
                    double x = warpsConfig.getDouble(path + ".loc.x");
                    double y = warpsConfig.getDouble(path + ".loc.y");
                    double z = warpsConfig.getDouble(path + ".loc.z");
                    float pitch = (float) warpsConfig.getDouble(path + ".loc.pitch");
                    float yaw = (float) warpsConfig.getDouble(path + ".loc.yaw");

                    // Get warp name
                    String name = warpsConfig.getString(path + ".name");
                    if (name == null || name.isEmpty()) {
                        Bukkit.getLogger().warning("Warp " + key + " skipped: Missing or invalid 'name'.");
                        continue;
                    }

                    // Get owner ID
                    UUID owner = UUID.fromString(warpsConfig.getString(path + ".owner-id"));

                    // Get category with a fallback to default
                    String category = Objects.requireNonNullElse(
                            warpsConfig.getString(path + ".category"),
                            PlayerWarpsReborn.getInstance().getConfig().getString("settings.default-category")
                    );

                    // Get visits count
                    int visits = warpsConfig.isSet(path + ".visits") ? warpsConfig.getInt(path + ".visits") : 0;

                    // Get item icon with a fallback
                    String icon = "RED_BED";
                    String itemType = warpsConfig.getString(path + ".item");
                    if (itemType != null && !"null".equalsIgnoreCase(itemType)) {
                        icon = warpsConfig.getString(path + ".item.type", "RED_BED");
                    }

                    // Create the warp location
                    Location location = new Location(Bukkit.getWorld(worldName), x, y, z, yaw, pitch);

                    // Create and save the warp
                    PlayerWarp warp = new PlayerWarp(
                            name, owner, location, Bukkit.getOfflinePlayer(owner).getName() + "'s player warp!",
                            "N/A", false, false, category, icon, visits, 0, 0
                    );

                    PlayerWarpsReborn.getInstance().getPlayerWarpManager().saveWarp(warp);
                    count++;
                } catch (Exception e) {
                    Bukkit.getLogger().warning("Error processing warp " + key + ": " + e.getMessage());
                }
            }

            PlayerWarpsReborn.getInstance().getPlayerWarpManager().loadWarps();

            Bukkit.getLogger().warning("" + count + " warps successfully merged.");
        } catch (IOException e) {
            Bukkit.getLogger().warning("Failed to read data.yml: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static boolean doesDatabaseExist() {
        File databaseFile = new File("plugins/PlayerWarps/data/database.db");
        return databaseFile.exists() && databaseFile.isFile();
    }

}
