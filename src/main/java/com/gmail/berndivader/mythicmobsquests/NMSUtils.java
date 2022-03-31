package com.gmail.berndivader.mythicmobsquests;

import io.lumine.xikage.mythicmobs.mobs.ActiveMob;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;

import java.lang.reflect.Field;

public class NMSUtils {

    protected static String vp = "";

    protected static Class<?> class_CraftItemStack;

    protected static Field class_CraftItemStack_handle;

    static {
        String cn = Bukkit.getServer().getClass().getName();
        String[] pkgs = StringUtils.split(cn, '.');
        if (pkgs.length == 5) vp = pkgs[3] + ".";
        try {
            class_CraftItemStack = fixBukkitClass("org.bukkit.craftbukkit.inventory.CraftItemStack");
            class_CraftItemStack_handle = class_CraftItemStack.getField("handle");
        } catch (ClassNotFoundException | SecurityException | NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    public static Class<?> fixBukkitClass(String s1) throws ClassNotFoundException {
        if (!vp.isEmpty()) {
            s1 = s1.replace("org.bukkit.craftbukkit.", "org.bukkit.craftbukkit." + vp);
        }
        return Class.forName(s1);
    }

    private static ItemStack getNMSItemStack(org.bukkit.inventory.ItemStack is) {
        try {
            return (ItemStack) class_CraftItemStack_handle.get(is);
        } catch (Throwable ex) {
            return null;
        }
    }

    private static NBTTagCompound getNBTTagCompound(ItemStack nmsItemStack) {
        if (nmsItemStack == null) return null;
        return nmsItemStack.u();
    }

    public static void setMeta(org.bukkit.inventory.ItemStack itemStack, String key, String value) {
        NBTTagCompound nbtTagCompound = getNBTTagCompound(getNMSItemStack(itemStack));
        if (nbtTagCompound == null) return;

        if (value == null || value.length() == 0) {
            nbtTagCompound.r(value);
        } else {
            nbtTagCompound.a(key, value);
        }
    }

    public static String getMeta(org.bukkit.inventory.ItemStack itemStack, String key) {
        NBTTagCompound nbtTagCompound = getNBTTagCompound(getNMSItemStack(itemStack));
        if (nbtTagCompound == null) return null;
        return nbtTagCompound.l(key);
    }

    public static int getActiveMobLevel(ActiveMob am) {
        return (int) am.getLevel();
    }

}
