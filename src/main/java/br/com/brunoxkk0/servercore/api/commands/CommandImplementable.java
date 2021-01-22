package br.com.brunoxkk0.servercore.api.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

public interface CommandImplementable {

    default String getPermission(){
        return null;
    }

    default TabCompleter getTabCompleter(){
        return null;
    }

    default String getPermissionMessage(){
        return "&c&lVocê não pode executar esse comando, permissões insuficientes.";
    }

    default String getUsage(){
        return "No usage is defined.";
    };

    default String getDescription(){
        return "No description is defined";
    };

    boolean process(CommandSender sender, String label, String[] args);

}
