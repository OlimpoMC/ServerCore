package br.com.brunoxkk0.servercore.api;

import br.com.brunoxkk0.servercore.ServerCore;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class WorldHelper {

    private CraftWorld craftWorld;

    public WorldHelper(int id){
        for(World world : Bukkit.getWorlds()){
            if(((CraftWorld) world).getId() == id){
                craftWorld = (CraftWorld) world;
                break;
            }
        }
    }

    public World getWorld(){
        if(craftWorld != null){
            return Bukkit.getWorld(craftWorld.getUID());
        }else{
            return null;
        }
    }

    public static Object getHandle(World world){
        Object handle = null;
        Method method = null;

        for(Method m : world.getClass().getMethods()){
            if(m.getName().equalsIgnoreCase("getHandle")){
                method = m;
            }
        }

        if(method != null){
            try{
                handle = method.invoke(world);
            } catch (IllegalAccessException | InvocationTargetException e) {
                ServerCore.getLoggerHelper().warn("Error to getHandle for world " + world.getName());
            }
        }

        return handle;
    }
}
