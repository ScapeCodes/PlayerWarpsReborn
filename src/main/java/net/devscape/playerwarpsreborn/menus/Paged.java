package net.devscape.playerwarpsreborn.menus;

import net.devscape.playerwarpsreborn.PlayerWarpsReborn;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;
import java.util.Objects;

public abstract class Paged extends Menu {

    private final FileConfiguration guis = PlayerWarpsReborn.getInstance().getConfigManager().getConfig("guis.yml").get();
    protected int page = 0;
    protected int maxItems = guis.getInt("gui.tag-menu.tags-per-page");
    protected int index = 0;
    public static int currentItemsOnPage = 0;
    protected boolean isLast;

    public Paged(MenuUtil menuUtil) {
        super(menuUtil);
    }

    public void applyEditorLayout() {
        String back = guis.getString("gui.items.back.displayname");
        int back_slot = guis.getInt("gui.items.back.slot");
        List<String> back_lore = guis.getStringList("gui.items.back.lore");

        String close = guis.getString("gui.items.close.displayname");
        int close_slot = guis.getInt("gui.items.close.slot");
        List<String> close_lore = guis.getStringList("gui.items.close.lore");

        String next = guis.getString("gui.items.next.displayname");
        int next_slot = guis.getInt("gui.items.next.slot");
        List<String> next_lore = guis.getStringList("gui.items.next.lore");

        inventory.setItem(back_slot, makeItem(Material.valueOf(Objects.requireNonNull(guis.getString("gui.items.back.material")).toUpperCase()), back, guis.getInt("gui.items.back.custom-model-data"), back_lore));

        inventory.setItem(close_slot, makeItem(Material.valueOf(Objects.requireNonNull(guis.getString("gui.items.close.material")).toUpperCase()), close, guis.getInt("gui.items.close.custom-model-data"), close_lore));

        if (getCurrentItemsOnPage() > maxItems) {
            inventory.setItem(next_slot, makeItem(Material.valueOf(Objects.requireNonNull(guis.getString("gui.items.next.material")).toUpperCase()), next, guis.getInt("gui.items.next.custom-model-data"), next_lore));
        }

        for (int i = 36; i <= 44; i++) {
            inventory.setItem(i, makeItem(Material.GRAY_STAINED_GLASS_PANE, "&6", 0));
        }
    }

    protected int getPage() {
        return page + 1;
    }

    public int getMaxItems() {
        return maxItems;
    }

    public int getCurrentItemsOnPage() {
        return currentItemsOnPage;
    }

    public void increaseCurrentItemsOnPage() {
        currentItemsOnPage++;
    }

    //public void openSearchSign(Player player) {
    //    SignGUI gui = SignGUI.builder()
    //            .setLines("§eSearch Result Below:", null, null)
    //            .setType(Material.OAK_SIGN)
    //            .setColor(DyeColor.YELLOW)
    //            .setHandler((p, result) -> {
    //                String line1 = result.getLineWithoutColor(1);
    //                if (!line1.isEmpty()) {
    //                    if (SupremeTagsPremium.getInstance().getCategoryManager().isCategoryNearName(line1) || SupremeTagsPremium.getInstance().getTagManager().tagExistsNearName(line1)) {
    //                        Bukkit.getScheduler().runTask(SupremeTagsPremium.getInstance(), () -> new SearchResultMenu(SupremeTagsPremium.getMenuUtil(player), line1).open());
    //                    } else {
    //                        String search_invalid = messages.getString("messages.search-invalid-1").replaceAll("%prefix%", Objects.requireNonNull(messages.getString("messages.prefix")));
    //                        msgPlayer(player, search_invalid);
    //                    }
    //                } else {
    //                    String search_invalid = messages.getString("messages.search-invalid-2").replaceAll("%prefix%", Objects.requireNonNull(messages.getString("messages.prefix")));
    //                    msgPlayer(player, search_invalid);
    //                }
    //                return Collections.emptyList();
    //            })
    //            .build();
    //    gui.open(player);
    //}
}