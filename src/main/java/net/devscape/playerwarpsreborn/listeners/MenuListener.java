package net.devscape.playerwarpsreborn.listeners;

import net.devscape.playerwarpsreborn.PlayerWarpsReborn;
import net.devscape.playerwarpsreborn.menus.Menu;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.InventoryHolder;

public class MenuListener implements Listener {

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (e.getClickedInventory() != null) {
            InventoryHolder holder = e.getClickedInventory().getHolder();

            Player player = (Player) e.getWhoClicked();

            if (PlayerWarpsReborn.getInstance().getMenuUtil().containsKey(player) && e.getClickedInventory() == e.getWhoClicked().getInventory()) {
                e.setCancelled(true); // Cancel the event to prevent any actions within the player's inventory
                return;
            }

            if (holder instanceof Menu) {
                e.setCancelled(true);

                if (e.isShiftClick()) {
                    e.setCancelled(true); // Cancel the event to prevent shift-clicking into the menu
                    return;
                }

                if (e.getCurrentItem() == null) {
                    return;
                }

                Menu menu = (Menu) holder;
                menu.handleMenu(e);
            }
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        Player p = (Player) e.getPlayer();

        if (e.getInventory().getHolder() != null) {
            InventoryHolder holder = e.getInventory().getHolder();

            if (holder instanceof Menu) {
                PlayerWarpsReborn.getInstance().getMenuUtil().remove(p);
            }
        }
    }
}