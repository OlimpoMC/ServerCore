package br.com.brunoxkk0.servercore;


import br.com.brunoxkk0.servercore.api.LoggerHelper;
import br.com.brunoxkk0.servercore.api.ServerProperties;
import br.com.brunoxkk0.servercore.core.managers.CommandManager;
import org.bukkit.plugin.java.JavaPlugin;


public class ServerCore extends JavaPlugin{

    private static ServerCore instance;

    private static final ServerProperties SERVER_PROPERTIES = new ServerProperties();

    private static final CommandManager commandManager = CommandManager.getInstance();

    private static LoggerHelper loggerHelper;

    public static ServerCore getInstance() {
        return instance;
    }

    public static LoggerHelper getLoggerHelper() {
        return loggerHelper;
    }

    public static ServerProperties getServerProperties(){
        return SERVER_PROPERTIES;
    }

    public static CommandManager getsCommandHandler(){
        return commandManager;
    }

    @Override
    public void onLoad() {

        instance = this;
        loggerHelper = new LoggerHelper(this);

        getLoggerHelper().info("Mundo principal setado como: " + getServerProperties().getProperties("level-name"));

    }

    @Override
    public void onEnable() {

        getLoggerHelper().info("Ativando Modulo [Comandos]");
        commandManager.build(this);
    }

    @Override
    public void onDisable() {
        instance = null;
    }

    public void disable(){
        this.getPluginLoader().disablePlugin(this);
    }

}
