package net.devscape.playerwarpsreborn.commands;

import net.devscape.playerwarpsreborn.PlayerWarpsReborn;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class PWRTabCompleter implements TabCompleter {

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        if (!(sender instanceof Player)) {
            return null;
        }

        Player player = (Player) sender;

        if (cmd.getLabel().equalsIgnoreCase("playerwarpsreborn")) {
            List<String> suggestions = new ArrayList<>();

            if (args.length == 1) {
                // Main commands at the first level
                suggestions = Arrays.asList(
                        "reload",
                        "forcesaveall",
                        "merge",
                        "gui",
                        "help",
                        "list",
                        "set",
                        "remove",
                        "info",
                        "setpassword",
                        "setcategory",
                        "seticon",
                        "setowner",
                        "rename",
                        "lock",
                        "password-mode",
                        "setdesc"
                );

                // Filter based on permissions and partial input
                return suggestions.stream()
                        .filter(s -> player.hasPermission("pw." + s) || player.hasPermission("pw.admin." + s))
                        .filter(s -> s.startsWith(args[0].toLowerCase()))
                        .collect(Collectors.toList());

            } else if (args.length == 2) {
                if (Arrays.asList("set", "remove", "info", "setpassword", "setcategory", "seticon", "setowner", "rename", "lock", "password-mode", "setdesc").contains(args[0].toLowerCase())) {
                    // Warp names could be suggested here
                    return PlayerWarpsReborn.getInstance().getPlayerWarpManager()
                            .getPlayerWarpList()
                            .stream()
                            .map(warp -> warp.getName())
                            .filter(name -> name.startsWith(args[1].toLowerCase()))
                            .collect(Collectors.toList());
                }
            } else if (args.length == 3) {
                if (args[0].equalsIgnoreCase("seticon")) {
                    // Material names for icons
                    return Arrays.stream(org.bukkit.Material.values())
                            .map(Enum::name)
                            .filter(name -> name.startsWith(args[2].toUpperCase()))
                            .collect(Collectors.toList());
                } else if (args[0].equalsIgnoreCase("lock") || args[0].equalsIgnoreCase("password-mode")) {
                    // Boolean values for lock and password-mode
                    return Arrays.asList("true", "false")
                            .stream()
                            .filter(value -> value.startsWith(args[2].toLowerCase()))
                            .collect(Collectors.toList());
                }
            }
        }

        return null;
    }
}
