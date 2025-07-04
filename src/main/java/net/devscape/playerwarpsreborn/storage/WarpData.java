package net.devscape.playerwarpsreborn.storage;

import net.devscape.playerwarpsreborn.PlayerWarpsReborn;
import net.devscape.playerwarpsreborn.objects.PlayerWarp;

import java.util.ArrayList;
import java.util.List;

public class WarpData {

    public static void saveWarp(PlayerWarp warp) {
        if (PlayerWarpsReborn.getInstance().isH2()) {
            PlayerWarpsReborn.getInstance().getH2().saveWarp(warp);
        } else if (PlayerWarpsReborn.getInstance().isMySQL()) {
            PlayerWarpsReborn.getInstance().getMySQL().saveWarp(warp);
        }
    }

    public static void removeWarp(String name) {
        if (PlayerWarpsReborn.getInstance().isH2()) {
            PlayerWarpsReborn.getInstance().getH2().removeWarp(name);
        } else if (PlayerWarpsReborn.getInstance().isMySQL()) {
            PlayerWarpsReborn.getInstance().getMySQL().removeWarp(name);
        }
    }

    public static List<PlayerWarp> loadAllWarps() {
        if (PlayerWarpsReborn.getInstance().isH2()) {
            return PlayerWarpsReborn.getInstance().getH2().loadAllWarps();
        } else if (PlayerWarpsReborn.getInstance().isMySQL()) {
            return PlayerWarpsReborn.getInstance().getMySQL().loadAllWarps();
        } else {
            return new ArrayList<>();
        }
    }
}
