package net.devscape.playerwarpsreborn.menus.guis;

import de.tr7zw.nbtapi.NBT;
import de.tr7zw.nbtapi.NBTItem;
import me.arcaniax.hdb.api.HeadDatabaseAPI;
import me.clip.placeholderapi.PlaceholderAPI;
import net.devscape.playerwarpsreborn.PlayerWarpsReborn;
import net.devscape.playerwarpsreborn.menus.Menu;
import net.devscape.playerwarpsreborn.menus.MenuUtil;
import net.devscape.playerwarpsreborn.objects.PlayerWarp;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

import static net.devscape.playerwarpsreborn.menus.Paged.currentItemsOnPage;
import static net.devscape.playerwarpsreborn.utils.SkullUtil.createSkull;
import static net.devscape.playerwarpsreborn.utils.Utils.*;
import static net.devscape.playerwarpsreborn.utils.Utils.isVersionLessThan;

public class WarpEditorMenu extends Menu {

    private FileConfiguration guis = PlayerWarpsReborn.getInstance().getConfigManager().getConfig("guis.yml").get();

    public WarpEditorMenu(MenuUtil menuUtil, String warp) {
        super(menuUtil);
        menuUtil.setWarpName(warp);
    }

    @Override
    public String getMenuName() {
        return format(guis.getString("editor.title").replace("%warp_name%", menuUtil.getWarpName()));
    }

    @Override
    public int getSlots() {
        return guis.getInt("editor.size");
    }

    @Override
    public void handleMenu(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();

        NBTItem nbt = new NBTItem(e.getCurrentItem());

        boolean hasData = nbt.hasNBTData();
        boolean isName = nbt.hasTag("name");
        String name = nbt.getString("name");

        if (hasData) {
            if (isName) {
                PlayerWarp warp = PlayerWarpsReborn.getInstance().getPlayerWarpManager().get(menuUtil.getWarpName());

                if (name.equalsIgnoreCase("locked")) {
                    warp.setLocked(!warp.isLocked());
                }

                if (name.equalsIgnoreCase("password-mode")) {
                    warp.setPasswordMode(!warp.isPasswordMode());
                }

                PlayerWarpsReborn.getInstance().getPlayerWarpManager().saveWarp(warp);
            }
        }
    }

    @Override
    public void setMenuItems() {
        if (guis.getConfigurationSection("editor.edit-items") != null) {
            for (String cSTR : guis.getConfigurationSection("editor.edit-items").getKeys(false)) {
                boolean cEnable = guis.getBoolean("editor.edit-items." + cSTR + ".enable");

                if (cEnable) {
                    String item_material = guis.getString("editor.edit-items." + cSTR + ".material");
                    String item_displayname = guis.getString("editor.edit-items." + cSTR + ".displayname");
                    int item_custom_model_data = guis.getInt("editor.edit-items." + cSTR + ".custom-model-data");
                    int item_slot = guis.getInt("editor.edit-items." + cSTR + ".slot");

                    List<String> actions = new ArrayList<>(guis.getStringList("editor.edit-items." + cSTR + ".actions"));

                    List<String> item_lore = new ArrayList<>();

                    if (guis.isSet("editor.edit-items." + cSTR + ".lore")) {
                        item_lore = new ArrayList<>(guis.getStringList("editor.edit-items." + cSTR + ".lore"));
                    }

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
                    } else if (item_material.equalsIgnoreCase("%warp_icon%")) {
                        item = new ItemStack(Material.valueOf(PlayerWarpsReborn.getInstance().getPlayerWarpManager().get(menuUtil.getWarpName()).getIcon().toUpperCase()), 1);
                        itemMeta = item.getItemMeta();
                    } else {
                        item = new ItemStack(Material.valueOf(item_material.toUpperCase()), 1);
                        itemMeta = item.getItemMeta();
                    }

                    nbt = new NBTItem(item);
                    nbt.setString("name", cSTR);

                    if (item_custom_model_data > 0) {
                        if (itemMeta != null)
                            itemMeta.setCustomModelData(item_custom_model_data);
                    }

                    item_displayname = item_displayname.replace("%player%", menuUtil.getOwner().getName());

                    if (!item_lore.isEmpty()) {
                        if (PlayerWarpsReborn.getInstance().isPlaceholderAPI()) {
                            item_lore.replaceAll(s -> PlaceholderAPI.setPlaceholders(menuUtil.getOwner(), s));
                        }
                    }

                    itemMeta.setLore(color(item_lore));

                    itemMeta.setDisplayName(format(item_displayname));
                    itemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                    if (!isVersionLessThan("1.16")) {
                        itemMeta.addItemFlags(ItemFlag.HIDE_DYE);
                    }
                    itemMeta.addItemFlags(ItemFlag.HIDE_DESTROYS);
                    itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                    itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);

                    nbt.getItem().setItemMeta(itemMeta);
                    nbt.setString("name", cSTR);

                    if (guis.isSet("editor.edit-items." + cSTR + ".slots")) {
                        for (int slot : guis.getIntegerList("editor.edit-items." + cSTR + ".slots")) {
                            try {
                                inventory.setItem(slot, nbt.getItem());
                            } catch (NumberFormatException e) {
                                inventory.addItem(nbt.getItem());
                            }
                        }
                    } else {
                        inventory.setItem(item_slot, nbt.getItem());
                    }

                    inventory.setItem(item_slot, nbt.getItem());
                }
            }
        }
    }
}