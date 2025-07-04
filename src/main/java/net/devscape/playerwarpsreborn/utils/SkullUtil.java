package net.devscape.playerwarpsreborn.utils;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.lang.reflect.Field;
import java.util.UUID;

public class SkullUtil {

    private static Field profileField;

    static {
        try {
            for (Field declaredField : SkullMeta.class.getDeclaredFields()) {
                if (declaredField.getType().equals(GameProfile.class)) {
                    profileField = declaredField;
                    profileField.setAccessible(true);
                    break;
                }
            }
            if (profileField == null) {
                System.out.println("Failed to find the GameProfile field in SkullMeta");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static ItemStack createSkull(String baseheadtexture64) {
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);

        if (baseheadtexture64 == null || baseheadtexture64.isEmpty()) {
            return skull;
        }

        SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();
        if (skullMeta != null) {
            GameProfile profile = new GameProfile(UUID.randomUUID(), "Dummy");
            profile.getProperties().put("textures", new Property("textures", baseheadtexture64));

            try {
                if (profileField != null) {
                    profileField.set(skullMeta, profile);
                } else {
                    System.out.println("Profile field is not available");
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }

            skull.setItemMeta(skullMeta);
        }

        return skull;
    }
}
