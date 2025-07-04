package net.devscape.playerwarpsreborn.menus.guis;

import de.tr7zw.nbtapi.NBTItem;
import me.arcaniax.hdb.api.HeadDatabaseAPI;
import me.clip.placeholderapi.PlaceholderAPI;
import net.devscape.playerwarpsreborn.PlayerWarpsReborn;
import net.devscape.playerwarpsreborn.menus.Menu;
import net.devscape.playerwarpsreborn.menus.MenuUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

import static net.devscape.playerwarpsreborn.utils.SkullUtil.createSkull;
import static net.devscape.playerwarpsreborn.utils.Utils.*;

public class CategoriesMenu extends Menu {

    private FileConfiguration guis = PlayerWarpsReborn.getInstance().getConfigManager().getConfig("guis.yml").get();

    public CategoriesMenu(MenuUtil menuUtil) {
        super(menuUtil);
    }

    @Override
    public String getMenuName() {
        return format(guis.getString("categories.title"));
    }

    @Override
    public int getSlots() {
        return guis.getInt("categories.size");
    }

    @Override
    public void handleMenu(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();

        NBTItem nbt = new NBTItem(e.getCurrentItem());

        if (nbt.hasNBTData()) {
            if (nbt.hasTag("category")) {
                String category = nbt.getString("category");

                menuUtil.setCategory(category);
                new WarpsMenu(PlayerWarpsReborn.getMenuUtil(player)).open();
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
        }
    }

    @Override
    public void setMenuItems() {
        applyLayout("categories");

        addCategories();

        fillEmpty(guis.getBoolean("categories.fill-empty"));
    }

    public void addCategories() {
        for (String cSTR : guis.getConfigurationSection("categories.category-items").getKeys(false)) {
            boolean cEnable = guis.getBoolean("categories.category-items." + cSTR + ".enable");

            if (cEnable) {
                String item_material = guis.getString("categories.category-items." + cSTR + ".material");
                String item_displayname = guis.getString("categories.category-items." + cSTR + ".displayname");
                int item_custom_model_data = guis.getInt("categories.category-items." + cSTR + ".custom-model-data");
                int item_slot = guis.getInt("categories.category-items." + cSTR + ".slot");

                List<String> item_lore = new ArrayList<>(guis.getStringList("categories.category-items." + cSTR + ".lore"));

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
                } else {
                    item = new ItemStack(Material.valueOf(item_material.toUpperCase()), 1);
                    itemMeta = item.getItemMeta();
                }

                nbt = new NBTItem(item);
                nbt.setString("category", cSTR);

                if (item_custom_model_data > 0) {
                    if (itemMeta != null)
                        itemMeta.setCustomModelData(item_custom_model_data);
                }

                itemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                if (!isVersionLessThan("1.16")) {
                    itemMeta.addItemFlags(ItemFlag.HIDE_DYE);
                }
                itemMeta.addItemFlags(ItemFlag.HIDE_DESTROYS);
                itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);

                item_displayname = item_displayname.replace("%player%", menuUtil.getOwner().getName());

                for (int l = 0; l < item_lore.size(); l++) {
                    String line = item_lore.get(l);

                    if (PlayerWarpsReborn.getInstance().isPlaceholderAPI()) {
                        item_lore.replaceAll(s -> PlaceholderAPI.setPlaceholders(menuUtil.getOwner(), s));
                    }

                    item_lore.set(l, line);
                }

                itemMeta.setLore(color(item_lore));
                itemMeta.setDisplayName(format(item_displayname));

                nbt.getItem().setItemMeta(itemMeta);
                nbt.setString("category", cSTR);

                inventory.setItem(item_slot, nbt.getItem());
            }
        }
    }
}