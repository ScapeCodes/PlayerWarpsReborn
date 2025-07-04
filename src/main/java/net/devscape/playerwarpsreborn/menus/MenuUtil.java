package net.devscape.playerwarpsreborn.menus;

import org.bukkit.entity.Player;

public class MenuUtil {

    private Player owner;
    private String category;

    private String searchResult;
    private String warpName;

    public MenuUtil(Player owner, String category) {
        this.owner = owner;
        this.category = category;
    }

    public MenuUtil(Player owner) {
        this.owner = owner;
    }

    public Player getOwner() {
        return owner;
    }

    public void setOwner(Player owner) {
        this.owner = owner;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getSearchResult() {
        return searchResult;
    }

    public void setSearchResult(String searchResult) {
        this.searchResult = searchResult;
    }

    public String getWarpName() {
        return warpName;
    }

    public void setWarpName(String warpName) {
        this.warpName = warpName;
    }
}