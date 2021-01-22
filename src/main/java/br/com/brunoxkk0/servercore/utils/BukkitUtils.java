package br.com.brunoxkk0.servercore.utils;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class BukkitUtils {

    public static Plugin getPluginFromClass(Class<? extends JavaPlugin> clazz){
        return JavaPlugin.getProvidingPlugin(clazz);
    }


}
