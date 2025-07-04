package net.devscape.playerwarpsreborn.listeners;

import net.devscape.playerwarpsreborn.PlayerWarpsReborn;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import static net.devscape.playerwarpsreborn.utils.Utils.msgPlayer;

public class DelayListener implements Listener {

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        Player player = e.getPlayer();

        if (PlayerWarpsReborn.getInstance().getPlayerWarpManager().getDelays().contains(player.getUniqueId())) {
            if (!e.getFrom().equals(e.getTo())) {
                PlayerWarpsReborn.getInstance().getPlayerWarpManager().getDelays().remove(player.getUniqueId());
                msgPlayer(player, "&#009dff&lPWR &8&l➟ &cTeleportation cancelled due to movement.");
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        PlayerWarpsReborn.getInstance().getPlayerWarpManager().getDelays().remove(e.getPlayer().getUniqueId());
    }
}
