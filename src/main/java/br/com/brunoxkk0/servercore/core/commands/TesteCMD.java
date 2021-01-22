package br.com.brunoxkk0.servercore.core.commands;

import br.com.brunoxkk0.servercore.ServerCore;
import br.com.brunoxkk0.servercore.api.commands.Command;
import br.com.brunoxkk0.servercore.api.commands.CommandImplementable;
import org.bukkit.command.CommandSender;

@Command(plugin = ServerCore.class, label = "teste", getAliases = {"tst", "test2", "test"})
public class TesteCMD implements CommandImplementable {

    @Override
    public String getDescription() {
        return "Teste command for server core";
    }

    @Override
    public String getUsage() {
        return "nothing is here";
    }

    @Override
    public boolean process(CommandSender sender, String label, String[] args) {

        sender.sendMessage("Hi " + sender.getName() + ", nice to meet you.");

        return true;
    }
}
