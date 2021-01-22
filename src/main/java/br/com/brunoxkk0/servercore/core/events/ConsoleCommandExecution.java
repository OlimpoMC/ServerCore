package br.com.brunoxkk0.servercore.core.events;

import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerCommandEvent;


public class ConsoleCommandExecution implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onConsoleCommand(ServerCommandEvent event) {

        if (!event.isCancelled() && (event.getSender() instanceof ConsoleCommandSender)) {

            String command = event.getCommand();

            if(command.startsWith(" "))
                command = command.substring(1);

            if(command.startsWith("/"))
                command = command.substring(1);

            event.setCommand(command);
        }
    }
}
