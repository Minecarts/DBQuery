package com.minecarts.dbquery;

import java.util.logging.Logger;
import java.util.logging.Level;
import java.text.MessageFormat;

import com.minecarts.dbconnector.DBConnector;
import com.minecarts.dbconnector.provider.Provider;


public class DBQuery extends org.bukkit.plugin.java.JavaPlugin {
    private static final Logger logger = Logger.getLogger("com.minecarts.dbquery"); 
    
    private DBConnector dbc;
    
    
    public void onEnable() {
        dbc = (DBConnector) getServer().getPluginManager().getPlugin("DBConnector");
        
        log("Version {0} enabled.", getDescription().getVersion());
    }
    
    public void onDisable() {
    }
    
    
    public void log(String message) {
        log(Level.INFO, message);
    }
    public void log(Level level, String message) {
        logger.log(level, MessageFormat.format("{0}> {1}", getDescription().getName(), message));
    }
    public void log(String message, Object... args) {
        log(MessageFormat.format(message, args));
    }
    public void log(Level level, String message, Object... args) {
        log(level, MessageFormat.format(message, args));
    }
    
    
    public Provider getProvider() {
        return dbc.getProvider();
    }
    public Provider getProvider(String name) {
        return dbc.getProvider(name);
    }
    
}