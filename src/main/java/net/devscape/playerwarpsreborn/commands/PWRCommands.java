package net.devscape.playerwarpsreborn.commands;

import net.devscape.playerwarpsreborn.PlayerWarpsReborn;
import net.devscape.playerwarpsreborn.menus.guis.CategoriesMenu;
import net.devscape.playerwarpsreborn.menus.guis.WarpsMenu;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static net.devscape.playerwarpsreborn.utils.Utils.*;

public class PWRCommands implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            return true;
        } else {

            Player player = (Player) sender;

            if (cmd.getLabel().equalsIgnoreCase("playerwarpsreborn")) {
                if (args.length == 0) {
                    if (isMainCommandGUI()) {
                        if (isCategories()) {
                            new CategoriesMenu(PlayerWarpsReborn.getMenuUtil(player)).open();
                        } else {
                            new WarpsMenu(PlayerWarpsReborn.getMenuUtil(player, "all")).open();
                        }
                    } else {
                        sendHelp(player);
                    }
                } else if (args.length == 1) {
                    if (args[0].equalsIgnoreCase("reload")) {
                        if (!player.hasPermission("pw.admin.reload")) {
                            msgPlayer(player, noPermission("pw.admin.reload"));
                            return true;
                        }

                        PlayerWarpsReborn.getInstance().reload();
                        msgPlayer(player, getConfigMessage("reloaded"));
                    } else if (args[0].equalsIgnoreCase("merge")) {
                        if (!player.hasPermission("pw.admin.merge")) {
                            msgPlayer(player, noPermission("pw.admin.merge"));
                            return true;
                        }

                        PlayerWarpsReborn.getInstance().getMergeManager().merge(player);
                    } else if (args[0].equalsIgnoreCase("forcesaveall")) {
                        if (!player.hasPermission("pw.admin.forcesaveall")) {
                            msgPlayer(player, noPermission("pw.admin.forcesaveall"));
                            return true;
                        }

                        PlayerWarpsReborn.getInstance().getPlayerWarpManager().forceSaveAll();
                        msgPlayer(player, getConfigMessage("forced-saved-all-warps"));
                    } else if (args[0].equalsIgnoreCase("gui")) {
                        if (!player.hasPermission("pw.gui") || !player.hasPermission("pw.commands")) {
                            msgPlayer(player, noPermission("pw.gui"));
                            return true;
                        }

                        if (isCategories()) {
                            new CategoriesMenu(PlayerWarpsReborn.getMenuUtil(player)).open();
                        } else {
                            new WarpsMenu(PlayerWarpsReborn.getMenuUtil(player, "all")).open();
                        }
                    } else if (args[0].equalsIgnoreCase("help")) {
                        if (!player.hasPermission("pw.help") || !player.hasPermission("pw.commands")) {
                            msgPlayer(player, noPermission("pw.help"));
                            return true;
                        }

                        sendHelp(player);
                    } else if (args[0].equalsIgnoreCase("list")) {
                        if (!player.hasPermission("pw.list") || !player.hasPermission("pw.commands") || !player.hasPermission("pw.admin.list")) {
                            msgPlayer(player, noPermission("pw.list"));
                            return true;
                        }

                        msgPlayer(player, "&#009dff&lPWR &8&l➟ &#80ceffThere are currently &f" + PlayerWarpsReborn.getInstance().getPlayerWarpManager().getPlayerWarpList().size() + " &#80ceffwarps loaded.");
                        msgPlayer(player, "&#80ceff" + PlayerWarpsReborn.getInstance().getPlayerWarpManager().getPlayerWarpsByNames().toString().replace("[", "").replace("]", ""));
                    } else {
                        if (args[0].equalsIgnoreCase("reload") || args[0].equalsIgnoreCase("list") || args[0].equalsIgnoreCase("gui") || args[0].equalsIgnoreCase("help") || args[0].equalsIgnoreCase("forcesaveall") || args[0].equalsIgnoreCase("merge")) {
                            return true;
                        }

                        String name = args[0];
                        PlayerWarpsReborn.getInstance().getPlayerWarpManager().teleport(player, name);
                    }
                } else if (args.length == 2) {
                    if (args[0].equalsIgnoreCase("set")) {
                        if (!player.hasPermission("pw.set") || !player.hasPermission("pw.commands") || !player.hasPermission("pw.admin.set")) {
                            msgPlayer(player, noPermission("pw.set"));
                            return true;
                        }

                        String name = args[1];
                        PlayerWarpsReborn.getInstance().getPlayerWarpManager().set(player, name);
                    } else if (args[0].equalsIgnoreCase("remove")) {
                        if (!player.hasPermission("pw.remove") || !player.hasPermission("pw.commands") || !player.hasPermission("pw.admin.remove")) {
                            msgPlayer(player, noPermission("pw.remove"));
                            return true;
                        }

                        String name = args[1];
                        PlayerWarpsReborn.getInstance().getPlayerWarpManager().remove(player, name);
                    } else if (args[0].equalsIgnoreCase("info")) {
                        if (!player.hasPermission("pw.info") || !player.hasPermission("pw.commands")) {
                            msgPlayer(player, noPermission("pw.info"));
                            return true;
                        }

                        String name = args[1];
                        PlayerWarpsReborn.getInstance().getPlayerWarpManager().info(player, name);
                    } else {
                        if (args[0].equalsIgnoreCase("reload") || args[0].equalsIgnoreCase("list") || args[0].equalsIgnoreCase("gui") || args[0].equalsIgnoreCase("help") || args[0].equalsIgnoreCase("forcesaveall") || args[0].equalsIgnoreCase("merge")) {
                            return true;
                        }

                        String name = args[0];
                        String password = args[1];

                        PlayerWarpsReborn.getInstance().getPlayerWarpManager().teleportWithPassword(player, name, password);
                    }
                } else if (args.length == 3) {
                    // /pwarp setpassword <warp> <password>
                    if (args[0].equalsIgnoreCase("setpassword")) {
                        if (!player.hasPermission("pw.setpassword") || !player.hasPermission("pw.commands") || !player.hasPermission("pw.admin.setpassword")) {
                            msgPlayer(player, noPermission("pw.setpassword"));
                            return true;
                        }

                        String name = args[1];
                        String password = args[2];
                        PlayerWarpsReborn.getInstance().getPlayerWarpManager().editWarp(player, name, "password", password);
                    } else if (args[0].equalsIgnoreCase("setcategory")) {
                        if (!player.hasPermission("pw.setcategory") || !player.hasPermission("pw.commands") || !player.hasPermission("pw.admin.setcategory")) {
                            msgPlayer(player, noPermission("pw.setcategory"));
                            return true;
                        }

                        String name = args[1];
                        String category = args[2];
                        PlayerWarpsReborn.getInstance().getPlayerWarpManager().editWarp(player, name, "category", category);
                    } else if (args[0].equalsIgnoreCase("seticon")) {
                        if (!player.hasPermission("pw.seticon") || !player.hasPermission("pw.commands") || !player.hasPermission("pw.admin.seticon")) {
                            msgPlayer(player, noPermission("pw.seticon"));
                            return true;
                        }

                        String name = args[1];
                        String icon = args[2];

                        Material mat = Material.matchMaterial(icon);

                        if (mat == null) {
                            msgPlayer(player, getConfigMessage("invalid-material-icon"));
                            return true;
                        }

                        PlayerWarpsReborn.getInstance().getPlayerWarpManager().editWarp(player, name, "icon", mat.name());
                    } else if (args[0].equalsIgnoreCase("setowner")) {
                        if (!player.hasPermission("pw.setowner") || !player.hasPermission("pw.commands") || !player.hasPermission("pw.admin.setowner")) {
                            msgPlayer(player, noPermission("pw.setowner"));
                            return true;
                        }

                        String name = args[1];
                        String newowner = args[2];
                        PlayerWarpsReborn.getInstance().getPlayerWarpManager().editWarp(player, name, "owner", newowner);
                    } else if (args[0].equalsIgnoreCase("rename")) {
                        if (!player.hasPermission("pw.rename") || !player.hasPermission("pw.commands") || !player.hasPermission("pw.admin.rename")) {
                            msgPlayer(player, noPermission("pw.rename"));
                            return true;
                        }

                        String name = args[1];
                        String newname = args[2];
                        PlayerWarpsReborn.getInstance().getPlayerWarpManager().editWarp(player, name, "rename", newname);
                    } else if (args[0].equalsIgnoreCase("lock")) {
                        if (!player.hasPermission("pw.lock") || !player.hasPermission("pw.commands") || !player.hasPermission("pw.admin.lock")) {
                            msgPlayer(player, noPermission("pw.lock"));
                            return true;
                        }

                        String name = args[1];
                        boolean value = Boolean.parseBoolean(args[2]);
                        PlayerWarpsReborn.getInstance().getPlayerWarpManager().editWarp(player, name, "locked", value);
                    } else if (args[0].equalsIgnoreCase("password-mode")) {
                        if (!player.hasPermission("pw.password-mode") || !player.hasPermission("pw.commands") || !player.hasPermission("pw.admin.password-mode")) {
                            msgPlayer(player, noPermission("pw.password-mode"));
                            return true;
                        }

                        String name = args[1];
                        boolean value = Boolean.parseBoolean(args[2]);
                        PlayerWarpsReborn.getInstance().getPlayerWarpManager().editWarp(player, name, "password-mode", value);
                    }
                } else if (args.length >= 3) {
                    // /pwarp setdesc <warp> <desc>
                    if (args[0].equalsIgnoreCase("setdesc")) {
                        if (!player.hasPermission("pw.setdesc") || !player.hasPermission("pw.commands") || !player.hasPermission("pw.admin.setdesc")) {
                            msgPlayer(player, noPermission("pw.setdesc"));
                            return true;
                        }

                        String name = args[1];

                        StringBuilder descriptionBuilder = new StringBuilder();
                        for (int i = 2; i < args.length; i++) {
                            descriptionBuilder.append(args[i]).append(" ");
                        }

                        String description = descriptionBuilder.toString().trim();
                        PlayerWarpsReborn.getInstance().getPlayerWarpManager().editWarp(player, name, "description", description);
                    }
                }
            }
        }
        return false;
    }

    public void sendHelp(Player player) {
        for (String msg : getConfigMessageList("help-message")) {
            msgPlayer(player, msg);
        }
    }

    public String noPermission(String permission) {
        return getConfigMessage("no-permission").replaceAll("%permission%", permission);
    }
}