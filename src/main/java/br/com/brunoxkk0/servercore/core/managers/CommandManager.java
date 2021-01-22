package br.com.brunoxkk0.servercore.core.managers;


import br.com.brunoxkk0.servercore.ServerCore;
import br.com.brunoxkk0.servercore.api.commands.Command;
import br.com.brunoxkk0.servercore.api.commands.CommandImplementable;
import br.com.brunoxkk0.servercore.utils.BukkitUtils;
import br.com.brunoxkk0.servercore.utils.ClassUtils;
import br.com.brunoxkk0.servercore.utils.Reflection;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.SimplePluginManager;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.*;

public class CommandManager {

    private static Constructor<PluginCommand> pluginCommandConstructor;
    private static CommandMap commandMap;

    static {

        try {
            pluginCommandConstructor = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
            pluginCommandConstructor.setAccessible(true);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }

        try {
            Field field = SimplePluginManager.class.getDeclaredField("commandMap");
            field.setAccessible(true);
            commandMap = (CommandMap)(field.get(Bukkit.getServer().getPluginManager()));
        } catch(Exception e) {
            e.printStackTrace();
        }

    }

    private static CommandManager INSTANCE;

    private CommandManager(){
        //Seal the class
    }

    public static CommandManager getInstance(){
        return (INSTANCE != null) ? INSTANCE : (INSTANCE = new CommandManager());
    }



    private final static HashMap<Command, CommandImplementable> COMMANDS_MAP = new HashMap<>();

    private final static LinkedHashMap<Command, CommandImplementable> COMMANDS_REGISTER_QUEUE = new LinkedHashMap<>();



    public HashSet<Class<? extends CommandImplementable>> scan(String targetPackage, ClassLoader classLoader){

        HashSet<Class<? extends CommandImplementable>> commands = new HashSet<>();

        Reflection reflection = Reflection.builder()
                .setPackage(targetPackage)
                .setClassLoader(classLoader)
                .ignoreInterfaces(true)
                .ignoreAnnotations(false)
                .build();

        reflection.scan().stream().filter(clazz -> CommandImplementable.class.isAssignableFrom(clazz) && clazz.isAnnotationPresent(Command.class)).forEach((clazz) -> commands.add((Class<? extends CommandImplementable>) clazz));


        return commands;
    }

    public HashSet<Class<? extends CommandImplementable>> scan(String targetPackage) {
        return scan(targetPackage, Thread.currentThread().getContextClassLoader());
    }

    public HashSet<Class<? extends CommandImplementable>> scan(){

        return scan(ClassUtils.getCallerPackage(4));
    }

    public void load(HashSet<Class<? extends CommandImplementable>> commands){
        if(commands != null){
            for(Class<? extends CommandImplementable> clazz : commands){
                if(canRegister(clazz)){
                    CommandImplementable commandImplementable = createInstance(clazz);
                    if(commandImplementable != null){
                        COMMANDS_REGISTER_QUEUE.put(clazz.getAnnotation(Command.class), commandImplementable);
                    }
                }
            }
        }
    }

    public static void processQueue(){

        int init = COMMANDS_MAP.size();

        for(Map.Entry<Command, CommandImplementable> entry : COMMANDS_REGISTER_QUEUE.entrySet()){
            if(!COMMANDS_MAP.containsKey(entry.getKey())){
                PluginCommand pluginCommand = buildCommand(entry.getKey(), entry.getValue());
                registerOnBukkit(pluginCommand);
                COMMANDS_MAP.put(entry.getKey(), entry.getValue());
            }
        }

        init = COMMANDS_MAP.size() - init;

        COMMANDS_REGISTER_QUEUE.clear();

        ServerCore.getLoggerHelper().info("[CommandHandler] » Registered " + init + " commands from current queue. Total commands: " + COMMANDS_MAP.size());
    }


    private CommandImplementable createInstance(Class<? extends CommandImplementable> clazz){

        try {
            return clazz.newInstance();
        } catch (IllegalAccessException | InstantiationException ignored) {}

        return null;
    }

    private boolean isCommandAvailable(String label){

        if(commandMap != null){
            return commandMap.getCommand(label) == null;
        }

        return false;
    }

    private static PluginCommand buildCommand(Command command, CommandImplementable commandImplementable) {

        try{

            PluginCommand pluginCommand = pluginCommandConstructor.newInstance(command.label(), BukkitUtils.getPluginFromClass(command.plugin()));

            pluginCommand.setExecutor(SimpleCommandProcessor.getInstance());
            pluginCommand.setAliases(Arrays.asList(command.getAliases()));
            pluginCommand.setLabel(command.label());
            pluginCommand.setTabCompleter(commandImplementable.getTabCompleter());
            pluginCommand.setPermission(commandImplementable.getPermission());
            pluginCommand.setPermissionMessage(commandImplementable.getPermissionMessage());
            pluginCommand.setUsage(commandImplementable.getUsage());
            pluginCommand.setDescription(commandImplementable.getDescription());

            return pluginCommand;

        }catch (Exception e){
            ServerCore.getLoggerHelper().info("[CommandHandler] » Failed to build PluginCommand for [ Command: " + command.label() + ", Plugin: " + command.plugin().getName() + "]");
        }

        return null;
    }

    private static void registerOnBukkit(PluginCommand command){
        try{
            commandMap.register(command.getPlugin().getName().toLowerCase(), command);
        }catch (Exception e){
            ServerCore.getLoggerHelper().info("[CommandHandler] » Failed to register commands on bukkit. Command name:  " + command.getName() + ", command class: " + command.getClass().getName() + ".");
        }
    }

    private boolean canRegister(Class<? extends CommandImplementable> command){

        if(command != null && command.isAnnotationPresent(Command.class)){

            Command commandAnnotation = command.getAnnotation(Command.class);

            for(String plugin : commandAnnotation.getRequiredPlugins()){
                if(Bukkit.getPluginManager().getPlugin(plugin) == null)
                    return false;
            }

            if(!isCommandAvailable(commandAnnotation.label()) && !commandAnnotation.hasPriority())
                return false;


            return BukkitUtils.getPluginFromClass(commandAnnotation.plugin()) != null;
        }

        return false;
    }


    public void build(Plugin plugin){

        ServerCore.getLoggerHelper().info("[CommandHandler] » Starting command search..");


        HashSet<Class<? extends CommandImplementable>> commandsClasses = scan(ClassUtils.getPackage(plugin.getClass()),  plugin.getClass().getClassLoader());

        if(!commandsClasses.isEmpty()){

            ServerCore.getLoggerHelper().info("[CommandHandler] » Founded " + commandsClasses.size() + " command(s), registering...");
            load(commandsClasses);
            processQueue();

            return;
        }

        ServerCore.getLoggerHelper().info("[CommandHandler] » Couldn't find any commands..");
    }

    public static class SimpleCommandProcessor implements CommandExecutor{

        private static SimpleCommandProcessor INSTANCE;

        public static SimpleCommandProcessor getInstance(){
            return (INSTANCE != null) ? INSTANCE : (INSTANCE = new SimpleCommandProcessor());
        }

        @Override
        public boolean onCommand(CommandSender commandSender, org.bukkit.command.Command command, String s, String[] strings) {

            CommandImplementable cmd = null;

            for(Command commandAnnotation : COMMANDS_MAP.keySet()){
                if(command.getName().equals(commandAnnotation.label())){
                    cmd = COMMANDS_MAP.get(commandAnnotation);
                    break;
                }
            }

            if(cmd != null){

                if((commandSender instanceof Player) && cmd.getPermission() != null && !commandSender.hasPermission(cmd.getPermission())){

                    commandSender.sendMessage(cmd.getPermissionMessage().replace("&","\u00a7"));
                    return false;

                }

                return cmd.process(commandSender, s, strings);
            }

            return false;
        }
    }

}
