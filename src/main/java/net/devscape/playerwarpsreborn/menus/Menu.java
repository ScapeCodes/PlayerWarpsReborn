package net.devscape.playerwarpsreborn.menus;

import de.tr7zw.nbtapi.NBTItem;
import me.arcaniax.hdb.api.HeadDatabaseAPI;
import me.clip.placeholderapi.PlaceholderAPI;
import net.devscape.playerwarpsreborn.PlayerWarpsReborn;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static net.devscape.playerwarpsreborn.menus.Paged.currentItemsOnPage;
import static net.devscape.playerwarpsreborn.utils.SkullUtil.createSkull;
import static net.devscape.playerwarpsreborn.utils.Utils.*;

public abstract class Menu implements InventoryHolder {

    protected Inventory inventory;

    protected MenuUtil menuUtil;

    private final FileConfiguration guis = PlayerWarpsReborn.getInstance().getConfigManager().getConfig("guis.yml").get();

    public Menu(MenuUtil menuUtil) {
        this.menuUtil = menuUtil;
    }

    protected int maxItems = guis.getInt("warps.warps-per-page");

    public abstract String getMenuName();

    public abstract int getSlots();

    public abstract void handleMenu(InventoryClickEvent e);

    public abstract void setMenuItems();

    public void open() {
        inventory = Bukkit.createInventory(this, getSlots(), getMenuName());
        this.setMenuItems();
        menuUtil.getOwner().openInventory(inventory);
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public ItemStack makeItem(Material material, String displayName, int custom_model_data, String... lore) {

        ItemStack item = new ItemStack(material);
        ItemMeta itemMeta = item.getItemMeta();
        assert itemMeta != null;
        itemMeta.setDisplayName(format(displayName));

        if (custom_model_data > 0) {
            itemMeta.setCustomModelData(custom_model_data);
        }

        itemMeta.setLore(color(Arrays.asList(lore)));
        item.setItemMeta(itemMeta);

        return item;
    }

    public ItemStack makeItem(Material material, String displayName, int custom_model_data, List<String> lore) {

        ItemStack item = new ItemStack(material);
        ItemMeta itemMeta = item.getItemMeta();
        assert itemMeta != null;
        itemMeta.setDisplayName(format(displayName));

        if (custom_model_data > 0) {
            itemMeta.setCustomModelData(custom_model_data);
        }

        itemMeta.setLore(color(lore));
        item.setItemMeta(itemMeta);

        return item;
    }

    public ItemStack makeItem(Material material, String displayName, List<String> lore) {

        ItemStack item = new ItemStack(material);
        ItemMeta itemMeta = item.getItemMeta();
        assert itemMeta != null;
        itemMeta.setDisplayName(format(displayName));

        itemMeta.setLore(color(lore));
        item.setItemMeta(itemMeta);

        return item;
    }

    public void fillEmpty(boolean enabled) {
        if (enabled) {
            for (int i = 0; i < inventory.getSize(); i++) {
                if (inventory.getItem(i) == null) {
                    inventory.setItem(i, makeItem(Material.GRAY_STAINED_GLASS_PANE, "&6", 0));
                }
            }
        }
    }

    public void applyLayout(String menu) {
        //if (SupremeTagsPremium.getInstance().getLayout().equalsIgnoreCase("FULL")) {
        //    if (SupremeTagsPremium.getInstance().getConfig().getBoolean("gui.items.glass.enable")) {
        //        for (int i = 36; i <= 44; i++) {
        //            String item_material = guis.getString("gui.items.glass.material");
        //            String item_displayname = guis.getString("gui.items.glass.displayname");
        //            int item_custom_model_data = guis.getInt("gui.items.glass.custom-model-data");
        //            assert item_material != null;
        //            inventory.setItem(i, makeItem(Material.valueOf(item_material.toUpperCase()), item_displayname, item_custom_model_data));
        //        }
        //    }
        //} else if (SupremeTagsPremium.getInstance().getLayout().equalsIgnoreCase("BORDER")) {
        //    for (int i = 0; i < 54; i++) {
        //        if (inventory.getItem(i) == null) {
        //            if (i < 9 || i >= 45 || i % 9 == 0 || (i + 1) % 9 == 0) {
        //                String item_material = guis.getString("gui.items.glass.material");
        //                String item_displayname = guis.getString("gui.items.glass.displayname");
        //                int item_custom_model_data = guis.getInt("gui.items.glass.custom-model-data");
        //                assert item_material != null;
        //                inventory.setItem(i, makeItem(Material.valueOf(item_material.toUpperCase()), item_displayname, item_custom_model_data));
        //            }
        //        }
        //    }
        //}

        if (guis.getConfigurationSection(menu + ".other-items") != null) {
            for (String cSTR : guis.getConfigurationSection(menu + ".other-items").getKeys(false)) {
                boolean cEnable = guis.getBoolean(menu + ".other-items." + cSTR + ".enable");

                if (!(getWarpsCount() > maxItems & currentItemsOnPage >= maxItems)) {
                    if (cSTR.equalsIgnoreCase("next"))
                        continue;
                }

                if (cEnable) {
                    String item_material = guis.getString(menu + ".other-items." + cSTR + ".material");
                    String item_displayname = guis.getString(menu + ".other-items." + cSTR + ".displayname");
                    int item_custom_model_data = guis.getInt(menu + ".other-items." + cSTR + ".custom-model-data");
                    int item_slot = guis.getInt(menu + ".other-items." + cSTR + ".slot");

                    List<String> actions = new ArrayList<>(guis.getStringList(menu + ".other-items." + cSTR + ".actions"));

                    List<String> item_lore = new ArrayList<>();

                    if (guis.isSet("categories.other-items." + cSTR + ".lore")) {
                        item_lore = new ArrayList<>(guis.getStringList("categories.other-items." + cSTR + ".lore"));
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
                    } else {
                        item = new ItemStack(Material.valueOf(item_material.toUpperCase()), 1);
                        itemMeta = item.getItemMeta();
                    }

                    nbt = new NBTItem(item);
                    nbt.setString("name", cSTR);
                    nbt.setObject("actions", actions);

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

                    nbt.setObject("actions", actions);

                    if (guis.isSet(menu + ".other-items." + cSTR + ".slots")) {
                        for (int slot : guis.getIntegerList(menu + ".other-items." + cSTR + ".slots")) {
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

    public int getWarpsCount() {
        return PlayerWarpsReborn.getInstance().getPlayerWarpManager().getPlayerWarpList().size();
    }
}
