package net.devscape.playerwarpsreborn;

import net.devscape.playerwarpsreborn.commands.PWRCommands;
import net.devscape.playerwarpsreborn.commands.PWRTabCompleter;
import net.devscape.playerwarpsreborn.listeners.DelayListener;
import net.devscape.playerwarpsreborn.listeners.MenuListener;
import net.devscape.playerwarpsreborn.managers.ConfigManager;
import net.devscape.playerwarpsreborn.managers.MergeManager;
import net.devscape.playerwarpsreborn.managers.PlayerWarpManager;
import net.devscape.playerwarpsreborn.menus.MenuUtil;
import net.devscape.playerwarpsreborn.objects.Metrics;
import net.devscape.playerwarpsreborn.storage.H2Database;
import net.devscape.playerwarpsreborn.storage.MySQLDatabase;
import net.milkbowl.vault.economy.Economy;
import org.black_ixx.playerpoints.PlayerPoints;
import org.black_ixx.playerpoints.PlayerPointsAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public final class PlayerWarpsReborn extends JavaPlugin {

    private static PlayerWarpsReborn instance;
    private ConfigManager configManager;
    private H2Database h2;
    private MySQLDatabase mysql;
    private PlayerWarpManager playerWarpManager;
    private MergeManager mergeManager;

    private static Economy econ = null;
    private PlayerPointsAPI ppAPI;

    private static final HashMap<Player, MenuUtil> menuUtilMap = new HashMap<>();

    private String host;
    private int port;
    private String database;
    private String username;
    private String password;
    private boolean useSSL;

    @Override
    public void onEnable() { init(); }

    private void init() {
        instance = this;

        Logger logger = Bukkit.getLogger();

        // load configs
        saveDefaultConfig();
        configManager = new ConfigManager(this);

        host = getConfig().getString("data.address");
        port = getConfig().getInt("data.port");
        database = getConfig().getString("data.database");
        username = getConfig().getString("data.username");
        password = getConfig().getString("data.password");
        useSSL = getConfig().getBoolean("data.useSSL");

        logger.info("  ______       ______ ");
        logger.info(" |  _ \\ \\    / /  _ \\");
        logger.info(" | |_) \\ \\  / /| |_) |");
        logger.info(" |  __/ \\ V  V /|  _ <");
        logger.info(" |_|     \\_/\\_/ |_| \\_\\");
        logger.info(" PlayerWarpsReborn - Allow players to create & visit warps!");
        logger.info("");
        logger.info("Thanks for downloading PWR!");
        logger.info("");

        this.callMetrics();

        loadDatabases();

        if (isH2()) {
            logger.info("> Database: H2!");
        } else if (isMySQL()) {
            logger.info("> Database: MySQL!");
        }

        // load managers
        playerWarpManager = new PlayerWarpManager();
        mergeManager = new MergeManager();

        if (Bukkit.getPluginManager().getPlugin("PlayerPoints") != null) {
            this.ppAPI = PlayerPoints.getInstance().getAPI();
            if (getConfig().getString("settings.economy.hook").equalsIgnoreCase("PLAYERPOINTS")) {
                logger.info("> PlayerPoints: Found! (ECONOMY)");
            } else {
                logger.info("> PlayerPoints: Found!");
            }
        } else {
            logger.info("> PlayerPoints: Not Found!");
        }

        if (getServer().getPluginManager().getPlugin("Vault") != null) {
            setupEconomy();
            if (getConfig().getString("settings.economy.hook").equalsIgnoreCase("VAULT")) {
                logger.info("> Vault: Found! (ECONOMY)");
            } else {
                logger.info("> Vault: Found!");
            }
        } else {
            logger.info("> Vault: Not Found!");
        }

        if (isPlaceholderAPI()) {
            logger.info("> PlaceholderAPI: Found");
        } else {
            logger.info("> PlaceholderAPI: Not Found!");
        }

        // load commands
        getCommand("playerwarpsreborn").setExecutor(new PWRCommands());
        getCommand("playerwarpsreborn").setTabCompleter(new PWRTabCompleter());

        getServer().getPluginManager().registerEvents(new DelayListener(), this);
        getServer().getPluginManager().registerEvents(new MenuListener(), this);
    }

    @Override
    public void onDisable() {
        playerWarpManager.forceSaveAll();

        if (isH2()) {
            try {
                h2.getConnection().close();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void reload() {
        super.reloadConfig();

        saveDefaultConfig();
        getConfig().options().copyDefaults(true);
        saveConfig();

        configManager.reloadConfig("guis.yml");

        playerWarpManager.loadWarps();
    }

    private void loadDatabases() {
        if (isH2() || isSQLite()) {
            h2 = new H2Database("jdbc:sqlite:" + getDataFolder().getAbsolutePath() + "/database.db");
        }

        if (isMySQL()) {
            mysql = new MySQLDatabase(host, port, database, username, password, useSSL);
        }
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }

    public PlayerPointsAPI getPpAPI() {
        return ppAPI;
    }


    public Boolean isH2() {
        return getConfig().getString("data.type").equalsIgnoreCase("H2");
    }

    public Boolean isSQLite() {
        return getConfig().getString("data.type").equalsIgnoreCase("sqlite");
    }


    public Boolean isMySQL() {
        return getConfig().getString("data.type").equalsIgnoreCase("MYSQL");
    }

    public static PlayerWarpsReborn getInstance() {
        return instance;
    }
    public ConfigManager getConfigManager() {
        return configManager;
    }
    public PlayerWarpManager getPlayerWarpManager() {
        return playerWarpManager;
    }

    public H2Database getH2() {
        return h2;
    }

    public static MenuUtil getMenuUtil(Player player) {
        MenuUtil menuUtil;

        if (menuUtilMap.containsKey(player)) {
            return menuUtilMap.get(player);
        } else {
            menuUtil = new MenuUtil(player);
            menuUtilMap.put(player, menuUtil);
        }

        return menuUtil;
    }

    public static MenuUtil getMenuUtil(Player player, String category) {
        MenuUtil menuUtil;

        if (menuUtilMap.containsKey(player)) {
            return menuUtilMap.get(player);
        } else {
            menuUtil = new MenuUtil(player, category);
            menuUtil.setCategory(category);
            menuUtilMap.put(player, menuUtil);
        }

        return menuUtil;
    }

    public static Economy getEconomy() {
        return econ;
    }

    public HashMap<Player, MenuUtil> getMenuUtil() {
        return menuUtilMap;
    }

    public boolean isPlaceholderAPI() {
        return getServer().getPluginManager().getPlugin("PlaceholderAPI") != null;
    }

    public MergeManager getMergeManager() {
        return mergeManager;
    }

    private void callMetrics() {
        int pluginId = 24182;
        Metrics metrics = new Metrics(this, pluginId);

        metrics.addCustomChart(new Metrics.SimplePie("used_language", () -> getConfig().getString("language", "en")));

        metrics.addCustomChart(new Metrics.DrilldownPie("java_version", () -> {
            Map<String, Map<String, Integer>> map = new HashMap<>();
            String javaVersion = System.getProperty("java.version");
            Map<String, Integer> entry = new HashMap<>();
            entry.put(javaVersion, 1);
            if (javaVersion.startsWith("1.7")) {
                map.put("Java 1.7", entry);
            } else if (javaVersion.startsWith("1.8")) {
                map.put("Java 1.8", entry);
            } else if (javaVersion.startsWith("1.9")) {
                map.put("Java 1.9", entry);
            } else {
                map.put("Other", entry);
            }
            return map;
        }));
    }

    public MySQLDatabase getMySQL() {
        return mysql;
    }
}