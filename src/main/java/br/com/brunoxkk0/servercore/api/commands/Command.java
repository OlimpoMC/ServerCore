package br.com.brunoxkk0.servercore.api.commands;

import org.bukkit.plugin.java.JavaPlugin;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Command {

    Class<? extends JavaPlugin> plugin();

    String label();

    String[] getRequiredPlugins() default {};

    String[] getAliases() default {};

    boolean hasPriority() default true;

}
