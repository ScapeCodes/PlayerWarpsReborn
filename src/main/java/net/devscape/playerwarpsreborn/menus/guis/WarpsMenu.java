package net.devscape.playerwarpsreborn.menus.guis;

import de.tr7zw.nbtapi.NBTItem;
import me.arcaniax.hdb.api.HeadDatabaseAPI;
import me.clip.placeholderapi.PlaceholderAPI;
import net.devscape.playerwarpsreborn.PlayerWarpsReborn;
import net.devscape.playerwarpsreborn.managers.PlayerWarpManager;
import net.devscape.playerwarpsreborn.menus.MenuUtil;
import net.devscape.playerwarpsreborn.menus.Paged;
import net.devscape.playerwarpsreborn.objects.PlayerWarp;
import net.devscape.playerwarpsreborn.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.lang.management.PlatformLoggingMXBean;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static net.devscape.playerwarpsreborn.utils.SkullUtil.createSkull;
import static net.devscape.playerwarpsreborn.utils.Utils.*;
import static net.devscape.playerwarpsreborn.utils.Utils.isVersionLessThan;

public class WarpsMenu extends Paged {

    private FileConfiguration guis = PlayerWarpsReborn.getInstance().getConfigManager().getConfig("guis.yml").get();

    public WarpsMenu(MenuUtil menuUtil) {
        super(menuUtil);
    }

    @Override
    public String getMenuName() {
        return format(guis.getString("warps.title").replaceAll("%category%", menuUtil.getCategory()));
    }

    @Override
    public int getSlots() {
        return guis.getInt("warps.size");
    }

    @Override
    public void handleMenu(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();

        NBTItem nbt = new NBTItem(e.getCurrentItem());

        if (nbt.hasNBTData()) {
            if (nbt.hasTag("warp")) {
                String warp_name = nbt.getString("warp");

                PlayerWarp warp = PlayerWarpsReborn.getInstance().getPlayerWarpManager().get(warp_name);

                if (e.getClick().isRightClick()) {
                    if (warp.isOwner(player.getUniqueId())) {
                        new WarpEditorMenu(PlayerWarpsReborn.getMenuUtil(player), warp_name).open();
                        return;
                    } else {
                       // do nothing
                    }
                }

                PlayerWarpManager warpManager = PlayerWarpsReborn.getInstance().getPlayerWarpManager();
                boolean isOwner = warpManager.playerWarpOwner(player, warp.getName());
                boolean isLocked = warp.isLocked();

                if (isLocked && !isOwner) {
                    msgPlayer(player, "&#009dff&lPWR &8&l➟ &cThis warp is locked.");
                    return;
                }

                if (visitEconomy()) {
                    if (!isOwner && visitFreeOwnerEconomy()) {
                        if (!hasAmount(player, visitValueEconomy())) {
                            msgPlayer(player, getConfigMessage("insufficient-funds-visit").replaceAll("%amount%", String.valueOf(visitValueEconomy())));
                            return;
                        } else {
                            take(player, visitValueEconomy());
                        }
                    } else if (isOwner && !visitFreeOwnerEconomy()) {
                        if (!hasAmount(player, visitValueEconomy())) {
                            msgPlayer(player, getConfigMessage("insufficient-funds-visit").replaceAll("%amount%", String.valueOf(visitValueEconomy())));
                            return;
                        } else {
                            take(player, visitValueEconomy());
                        }
                    }
                }

                player.closeInventory();
                warpManager.teleport(player, warp_name);
            }

            if (nbt.hasTag("actions")) {
                List<String> actions = new ArrayList<>(
                        nbt.getObject("actions", List.class)
                );

                for (String option : actions) {
                    if (option.startsWith("[message]")) {
                        String message = option.replace("[message] ", "");
                        message = replacePlaceholders(menuUtil.getOwner(), message);
                        msgPlayer(menuUtil.getOwner(), message);
                    }

                    if (option.startsWith("[player]")) {
                        String command = option.replace("[player] ", "");
                        command = command.replaceAll("%player%", menuUtil.getOwner().getName());
                        menuUtil.getOwner().performCommand(command);
                    }

                    if (option.startsWith("[console]")) {
                        String command = option.replace("[console] ", "");
                        command = command.replaceAll("%player%", menuUtil.getOwner().getName());
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replaceAll("%player%", menuUtil.getOwner().getName()));
                    }

                    if (option.startsWith("[broadcast]")) {
                        String message = option.replace("[message] ", "");
                        message = replacePlaceholders(menuUtil.getOwner(), message);
                        Bukkit.broadcastMessage(message);
                    }

                    if (option.startsWith("[close]")) {
                        menuUtil.getOwner().closeInventory();
                    }
                }
            }

            if (nbt.hasTag("name")) {
                String name = nbt.getString("name");

                if (name.equalsIgnoreCase("close")) {
                    player.closeInventory();
                }

                if (name.equalsIgnoreCase("manage")) {
                    menuUtil.setCategory("manage");
                    new WarpsMenu(PlayerWarpsReborn.getMenuUtil(player, "manage")).open();
                }

                if (name.equalsIgnoreCase("next")) {
                    if (getWarpsCount() > maxItems & currentItemsOnPage >= maxItems) {
                        if (!((index + 1) >= getWarpsCount())) {
                            page = page + 1;
                            super.open();
                        } else {
                            e.setCancelled(true);
                        }
                    } else {
                        e.setCancelled(true);
                    }
                }

                if (name.equalsIgnoreCase("back")) {
                    if (page != 0) {
                        page = page - 1;
                        super.open();
                    }
                }
            }
        }
    }


    @Override
    public void setMenuItems() {
        applyLayout("warps");

        addWarps();
    }

    public void addWarps() {
        List<PlayerWarp> warps = PlayerWarpsReborn.getInstance().getPlayerWarpManager().getPlayerWarpList();

        if (!warps.isEmpty()) {
            int maxItemsPerPage = guis.getInt("warps.warps-per-page");

            int startIndex = page * maxItemsPerPage;
            int endIndex = Math.min(startIndex + maxItemsPerPage, warps.size());

            //tag.sort(getTagComparator(SupremeTagsPremium.getInstance().getConfig().getBoolean("settings.prioritise-selected-tag"), menuUtil));

            currentItemsOnPage = 0;

            List<String> slots = guis.getStringList("warps.warp-slots.slots");

            for (int i = startIndex; i <= endIndex; i++) {
                if (i > warps.size() - 1) {
                    break;
                }

                PlayerWarp warp = warps.get(i);
                if (warp == null) break;

                if (i == endIndex) {
                    continue;
                }

                if (menuUtil.getCategory().equalsIgnoreCase("manage")) {
                    if (warp.isOwner(menuUtil.getOwner().getUniqueId())) {
                        continue;
                    }
                }

                if (!menuUtil.getCategory().equalsIgnoreCase("all")) {
                    if (!warp.getCategory().equalsIgnoreCase(menuUtil.getCategory())) {
                        continue;
                    }
                }

                String item_material = guis.getString("warps.warp-item.material");
                String item_displayname = guis.getString("warps.warp-item.displayname");
                int item_custom_model_data = guis.getInt("warps.warp-item.custom-model-data");

                List<String> item_lore = new ArrayList<>(guis.getStringList("warps.warp-item.lore"));

                ItemStack item;
                ItemMeta itemMeta;
                NBTItem nbt;

                if (item_material.contains("hdb-")) {
                    int id = Integer.parseInt(item_material.replaceAll("hdb-", ""));
                    HeadDatabaseAPI api = new HeadDatabaseAPI();
                    item = api.getItemHead(String.valueOf(id));
                    itemMeta = item.getItemMeta();
                } else if (item_material.contains("basehead-")) {
                    String id = item_material.replaceAll("basehead-", "");
                    item = createSkull(id);
                    itemMeta = item.getItemMeta();
                } else if (item_material.contains("itemsadder-")) {
                    String id = item_material.replaceAll("itemsadder-", "");
                    item = getItemWithIA(id);
                    itemMeta = item.getItemMeta();
                } else if (item_material.contains("nexo-")) {
                    String id = item_material.replace("nexo-", "");
                    item = getItemWithNexo(id);
                    itemMeta = item.getItemMeta();
                } else if (item_material.equalsIgnoreCase("%warp_icon%")) {
                    item = new ItemStack(Material.valueOf(warp.getIcon().toUpperCase()), 1);
                    itemMeta = item.getItemMeta();
                } else {
                    item = new ItemStack(Material.valueOf(item_material.toUpperCase()), 1);
                    itemMeta = item.getItemMeta();
                }

                nbt = new NBTItem(item);

                nbt.setString("warp", warp.getName());

                if (item_custom_model_data > 0) {
                    if (itemMeta != null) {
                        itemMeta.setCustomModelData(item_custom_model_data);
                    }
                }


                item_displayname = item_displayname.replace("%warp_name%", warp.getName());
                item_displayname = replacePlaceholders(menuUtil.getOwner(), item_displayname);

                itemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                if (!isVersionLessThan("1.16")) {
                    itemMeta.addItemFlags(ItemFlag.HIDE_DYE);
                }
                itemMeta.addItemFlags(ItemFlag.HIDE_DESTROYS);
                itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);

                String ownerPlaceholder = "%warp_owner%";
                String descriptionPlaceholder = "%warp_description%";
                String identifierPlaceholder = "%warp_name%";
                String worldPlaceholder = "%warp_world%";
                String starsPlaceholder = "%warp_stars%";
                String ratingPlaceholder = "%warp_rating%";
                String categoryPlaceholder = "%warp_category%";
                String safePlaceholder = "%safe%";
                String visitsPlaceholder = "%warp_visits%";

                for (int l = 0; l < item_lore.size(); l++) {
                    String line = item_lore.get(l);

                    line = line.replace(ownerPlaceholder, Bukkit.getOfflinePlayer(warp.getOwner()).getName());
                    line = line.replace(identifierPlaceholder, warp.getName());
                    line = line.replace(descriptionPlaceholder, warp.getDescription());
                    line = line.replace(categoryPlaceholder, warp.getCategory());
                    line = line.replace(worldPlaceholder, warp.getLocation().getWorld().getName());
                    line = line.replace(starsPlaceholder, warp.getStars());
                    line = line.replace(ratingPlaceholder, String.valueOf(warp.getAverageRating()));
                    line = line.replace(visitsPlaceholder, String.valueOf(warp.getVisits()));

                    if (isLocationSafe(warp.getLocation())) {
                        line = line.replace(safePlaceholder, getConfigMessage("safe"));
                    } else {
                        line = line.replace(safePlaceholder, getConfigMessage("unsafe"));
                    }

                    if (PlayerWarpsReborn.getInstance().isPlaceholderAPI()) {
                        item_lore.replaceAll(s -> PlaceholderAPI.setPlaceholders(menuUtil.getOwner(), s));
                    }

                    item_lore.set(l, line);
                }

                itemMeta.setLore(color(item_lore));
                itemMeta.setDisplayName(format(item_displayname));

                nbt.getItem().setItemMeta(itemMeta);
                nbt.setString("warp", warp.getName());

                if (guis.getBoolean("warps.warp-slots.enable")) {
                    if (currentItemsOnPage < slots.size()) {
                        try {
                            int slot = Integer.parseInt(slots.get(currentItemsOnPage));
                            inventory.setItem(slot, nbt.getItem());
                        } catch (NumberFormatException e) {
                            inventory.addItem(nbt.getItem());
                        }
                    } else {
                        inventory.addItem(nbt.getItem());
                    }
                } else {
                    inventory.addItem(nbt.getItem());
                }

                currentItemsOnPage++;
            }
        }
    }
}
