package net.devscape.playerwarpsreborn.managers;

import net.devscape.playerwarpsreborn.PlayerWarpsReborn;
import net.devscape.playerwarpsreborn.objects.PlayerWarp;
import net.devscape.playerwarpsreborn.storage.WarpData;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static net.devscape.playerwarpsreborn.utils.Utils.*;

public class PlayerWarpManager {

    private List<PlayerWarp> playerWarpList = new ArrayList<>();

    private FileConfiguration guis = PlayerWarpsReborn.getInstance().getConfigManager().getConfig("guis.yml").get();

    private List<UUID> delays = new ArrayList<>();

    public PlayerWarpManager() {
        loadWarps();
    }

    public void loadWarps() {
        playerWarpList.clear();

        playerWarpList = WarpData.loadAllWarps();
    }

    public void create(Player sender, String name) {
        if (isInDisabledWorld(sender, "create")) {
            msgPlayer(sender, getConfigMessage("disabled-worlds.create"));
            return;
        }

        if (doesExist(name)) {
            msgPlayer(sender, getConfigMessage("already-exist"));
            return;
        }

        if (hasPlayerReachedLimit(sender)) {
            msgPlayer(sender, getConfigMessage("limit-reached").replace("%limit%", String.valueOf(getPlayerWarpLimit(sender))));
            return;
        }

        if (createEconomy()) {
            if (!hasAmount(sender, createValueEconomy())) {
                msgPlayer(sender, getConfigMessage("insufficient-funds-create").replaceAll("%amount%", String.valueOf(createValueEconomy())));
                return;
            } else {
                take(sender, createValueEconomy());
            }
        }

        String default_category = PlayerWarpsReborn.getInstance().getConfig().getString("settings.default-category");

        PlayerWarp warp = new PlayerWarp(name, sender.getUniqueId(), sender.getLocation(), sender.getName() + "'s player warp!", "N/A", false, false, default_category, "RED_BED", 0, 0, 0);
        saveWarp(warp);

        playerWarpList.add(warp);
        msgPlayer(sender, getConfigMessage("created-warp"));
    }

    public void teleport(Player player, String name) {
        if (!doesExist(name)) {
            msgPlayer(player, getConfigMessage("does-not-exist"));
            return;
        }

        if (isInDisabledWorld(player, "visit")) {
            msgPlayer(player, getConfigMessage("disabled-worlds.visit"));
            return;
        }

        if (!playerWarpOwner(player, name)) {
            if (get(name).isLocked()) {
                msgPlayer(player, getConfigMessage("warp-locked"));
                return;
            }
        }

        if (!playerWarpOwner(player, name)) {
            if (get(name).isPasswordMode() && !get(name).getPassword().equalsIgnoreCase("N/A")) {
                msgPlayer(player, getConfigMessage("has-password"));
                return;
            }
        }

        if (!player.hasPermission("pw.admin.delays.bypass")) {
            if (PlayerWarpsReborn.getInstance().getConfig().getBoolean("settings.delays.teleport.enable")) {
                if (delays.contains(player.getUniqueId())) {
                    msgPlayer(player, getConfigMessage("slow-down-teleporting"));
                } else {
                    delays.add(player.getUniqueId());
                    int interval = PlayerWarpsReborn.getInstance().getConfig().getInt("settings.delays.teleport.interval");
                    msgPlayer(player, getConfigMessage("timer-teleport").replaceAll("%interval%", String.valueOf(interval)));

                    Location startLocation = player.getLocation();
                    asyncTeleport(player, name, startLocation, interval).thenRun(() -> {
                        delays.remove(player.getUniqueId());
                    });
                }
            } else {
                msgPlayer(player, getConfigMessage("teleporting"));
                Location startLocation = player.getLocation();
                asyncTeleport(player, name, startLocation, 0).thenRun(() -> delays.remove(player.getUniqueId()));
            }
        } else {
            Location startLocation = player.getLocation();
            asyncTeleport(player, name, startLocation, 0).thenRun(() -> delays.remove(player.getUniqueId()));
        }
    }

    public void teleportWithPassword(Player player, String name, String password) {
        if (!doesExist(name)) {
            msgPlayer(player, getConfigMessage("does-not-exist"));
            return;
        }

        boolean owner = get(name).isOwner(player.getUniqueId());

        if (isInDisabledWorld(player, "visit")) {
            msgPlayer(player, getConfigMessage("disabled-worlds.visit"));
            return;
        }

        if (!owner) {
            if (get(name).isLocked()) {
                msgPlayer(player, getConfigMessage("warp-locked"));
                return;
            }
        }

        if (!owner) {
            if (get(name).isPasswordMode() && !get(name).getPassword().equalsIgnoreCase(password)) {
                msgPlayer(player, getConfigMessage("invalid-password"));
                return;
            }
        }

        if (!player.hasPermission("pw.admin.delays.bypass")) {
            if (PlayerWarpsReborn.getInstance().getConfig().getBoolean("settings.delays.teleport.enable")) {
                if (delays.contains(player.getUniqueId())) {
                    msgPlayer(player, getConfigMessage("slow-down-teleporting"));
                } else {
                    delays.add(player.getUniqueId());
                    int interval = PlayerWarpsReborn.getInstance().getConfig().getInt("settings.delays.teleport.interval");
                    msgPlayer(player, getConfigMessage("timer-teleport").replaceAll("%interval%", String.valueOf(interval)));

                    Location startLocation = player.getLocation();
                    asyncTeleport(player, name, startLocation, interval).thenRun(() -> {
                        delays.remove(player.getUniqueId());
                    });
                }
            } else {
                msgPlayer(player, getConfigMessage("teleporting"));
                Location startLocation = player.getLocation();
                asyncTeleport(player, name, startLocation, 0).thenRun(() -> delays.remove(player.getUniqueId()));
            }
        } else {
            Location startLocation = player.getLocation();
            asyncTeleport(player, name, startLocation, 0).thenRun(() -> delays.remove(player.getUniqueId()));
        }
    }


    private CompletableFuture<Void> asyncTeleport(Player player, String name, Location startLocation, int delaySeconds) {
        return CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(delaySeconds * 1000L); // Delay in milliseconds

                Bukkit.getScheduler().runTask(PlayerWarpsReborn.getInstance(), () -> {
                    if (player.isOnline() && player.getLocation().equals(startLocation)) {
                        msgPlayer(player, getConfigMessage("teleporting"));
                        player.teleport(get(name).getLocation());
                        if (!get(name).isOwner(player.getUniqueId())) {
                            get(name).addVisits(1);
                            saveWarp(get(name));
                        }
                    } else {
                        msgPlayer(player, getConfigMessage("cancelled-teleport"));
                    }
                });
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                Bukkit.getScheduler().runTask(PlayerWarpsReborn.getInstance(), () -> {
                    msgPlayer(player, getConfigMessage("interrupted-teleport"));
                });
            }
        });
    }

    public void editWarp(Player player, String name, String key, String value) {
        if (!doesExist(name)) {
            msgPlayer(player, getConfigMessage("does-not-exist"));
            return;
        }

        if (!player.hasPermission("pw.admin.set")) {
            if (!get(name).isOwner(player.getUniqueId())) {
                msgPlayer(player, getConfigMessage("not-owner"));
                return;
            }
        }

        PlayerWarp warp = get(name);

        if (key.equalsIgnoreCase("description")) {
            warp.setDescription(value);
        } else if (key.equalsIgnoreCase("icon")) {
            warp.setIcon(value);
        } else if (key.equalsIgnoreCase("password")) {
            warp.setPassword(value);
        } else if (key.equalsIgnoreCase("category")) {
            if (!doesCategoryExist(value)) {
                String categories = getCategories().toString().replace("[", "").replace("]", "");

                msgPlayer(player, getConfigMessage("invalid-category").replaceAll("%categories%", categories));
                return;
            }

            warp.setCategory(value);
        } else if (key.equalsIgnoreCase("rename")) {
            if (doesExist(value)) {
                msgPlayer(player, getConfigMessage("already-exist"));
                return;
            }

            warp.setName(value);
            saveWarp(warp);

            WarpData.removeWarp(name);
        } else if (key.equalsIgnoreCase("owner")) {
            if (!Bukkit.getOfflinePlayer(value).hasPlayedBefore()) {
                msgPlayer(player, getConfigMessage("player-never-played"));
                return;
            }

            OfflinePlayer p = Bukkit.getOfflinePlayer(warp.getOwner());

            if (p.getName().equalsIgnoreCase(value)) {
                msgPlayer(player, getConfigMessage("is-owner"));
                return;
            }

            if (Bukkit.getOfflinePlayer(value).isOnline()) {
                if (hasPlayerReachedLimit(Bukkit.getPlayer(value))) {
                    msgPlayer(player, getConfigMessage("player-limit-reached").replace("%limit%", String.valueOf(getPlayerWarpLimit(Bukkit.getPlayer(value)))));
                    return;
                }
            }

            warp.setOwner(Bukkit.getOfflinePlayer(value).getUniqueId());
        }

        saveWarp(warp);
        msgPlayer(player, getConfigMessage("updated-value").replaceAll("%key%", key).replaceAll("%value%", value));
    }

    public void editWarp(Player player, String name, String key, boolean value) {
        if (!doesExist(name)) {
            msgPlayer(player, getConfigMessage("does-not-exist"));
            return;
        }

        if (!player.hasPermission("pw.admin.set")) {
            if (!get(name).isOwner(player.getUniqueId())) {
                msgPlayer(player, getConfigMessage("not-owner"));
                return;
            }
        }

        PlayerWarp warp = get(name);

        if (key.equalsIgnoreCase("locked")) {
            warp.setLocked(value);
        } else if (key.equalsIgnoreCase("password-mode")) {
            warp.setPasswordMode(value);
        }

        saveWarp(warp);

        msgPlayer(player, getConfigMessage("updated-value").replaceAll("%key%", key).replaceAll("%value%", String.valueOf(value)));
    }

    public void set(Player player, String name) {
        if (!doesExist(name)) {
            create(player, name);
            return;
        }

        PlayerWarp warp = get(name);

        if (!player.hasPermission("pw.admin.set")) {
            if (!get(name).isOwner(player.getUniqueId())) {
                msgPlayer(player, getConfigMessage("not-owner"));
                return;
            }
        }

        if (isInDisabledWorld(player, "set")) {
            msgPlayer(player, getConfigMessage("disabled-worlds.set"));
            return;
        }

        warp.setLocation(player.getLocation());
        saveWarp(warp);

        msgPlayer(player, getConfigMessage("updated-location"));
    }

    public void info(Player sender, String name) {
        if (!doesExist(name)) {
            msgPlayer(sender, getConfigMessage("does-not-exist"));
            return;
        }

        PlayerWarp warp = get(name);

        for (String msg : getConfigMessageList("info-message")) {
            msg = msg.replace("%owner%", Bukkit.getOfflinePlayer(warp.getOwner()).getName());
            msg = msg.replace("%description%", warp.getDescription());
            msg = msg.replace("%rating%", String.valueOf(warp.getAverageRating()));
            msg = msg.replace("%password-mode%", String.valueOf(warp.isPasswordMode()));
            msg = msg.replace("%locked%", String.valueOf(warp.isLocked()));
            msg = msg.replace("%category%", warp.getCategory());

            msgPlayer(sender, msg);
        }
    }

    public void remove(Player sender, String name) {
        if (!doesExist(name)) {
            msgPlayer(sender, getConfigMessage("does-not-exist"));
            return;
        }

        PlayerWarp warp = get(name);

        if (!sender.hasPermission("pw.admin.remove")) {
            if (!get(name).isOwner(sender.getUniqueId())) {
                msgPlayer(sender, getConfigMessage("not-owner"));
                return;
            }
        }

        playerWarpList.remove(warp);

        WarpData.removeWarp(name);

        msgPlayer(sender, getConfigMessage("remove").replaceAll("%warp%", name));
    }

    public boolean doesExist(String name) {
        return get(name) != null;
    }

    public boolean playerHasWarps(Player player) {
        return get(player).size() > 0;
    }

    public boolean playerWarpOwner(Player player, String name) {
        return get(name) != null && get(player).contains(get(name));
    }

    public List<PlayerWarp> getWarpsOfOwner(Player player) {
        List<PlayerWarp> list = new ArrayList<>();

        for (PlayerWarp pw : playerWarpList) {
            if (pw.getOwner().equals(player.getUniqueId())) {
                list.add(pw);
            }
        }

        return list;
    }

    public static int getPlayerWarpLimit(Player player) {
        int maxLimit = 0;

        if (player.isOp() || player.hasPermission("pw.limit.*")) {
            return Integer.MAX_VALUE;
        }

        for (PermissionAttachmentInfo permInfo : player.getEffectivePermissions()) {
            String perm = permInfo.getPermission();

            // Check for "pw.limit.#" pattern
            if (perm.startsWith("pw.limit.")) {
                if (perm.equals("pw.limit.*")) {
                    return Integer.MAX_VALUE;
                } else {
                    // Extract number from permission
                    try {
                        String[] parts = perm.split("\\.");
                        int limit = Integer.parseInt(parts[2]);
                        maxLimit = Math.max(maxLimit, limit); // Update maxLimit
                    } catch (NumberFormatException e) {
                        // Ignore invalid formats
                    }
                }
            }
        }

        return maxLimit; // Return the highest limit found
    }

    public boolean hasPlayerReachedLimit(Player player) {
        return getWarpsOfOwner(player).size() >= getPlayerWarpLimit(player);
    }

    public PlayerWarp get(String name) {
        for (PlayerWarp w : playerWarpList) {
            if (w.getName().equalsIgnoreCase(name)) {
                return w;
            }
        }

        return null;
    }

    public List<PlayerWarp> get(Player player) {
        List<PlayerWarp> pwlist = new ArrayList<>();

        for (PlayerWarp w : playerWarpList) {
            if (w.isOwner(player.getUniqueId())) {
                pwlist.add(w);
            }
        }

        return pwlist;
    }

    public List<String> getPlayerWarpsByNames() {
        List<String> list = new ArrayList<>();

        for (PlayerWarp warp : playerWarpList) {
            list.add(warp.getName());
        }

        return list;
    }

    public boolean doesCategoryExist(String cat) {
        return getCategories().contains(cat);
    }

    public List<String> getCategories() {
        return new ArrayList<>(guis.getConfigurationSection("categories.category-items").getKeys(false));
    }

    public List<PlayerWarp> getPlayerWarpList() {
        return playerWarpList;
    }

    public void forceSaveAll() {
        for (PlayerWarp warp : playerWarpList) {
            saveWarp(warp);
        }
    }

    public void saveWarp(PlayerWarp warp) {
        WarpData.saveWarp(warp);
    }

    public List<UUID> getDelays() { return delays; }
}