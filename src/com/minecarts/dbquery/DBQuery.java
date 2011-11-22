package com.minecarts.dbquery;

import java.util.logging.Logger;
import java.util.logging.Level;

import java.text.MessageFormat;

import org.bukkit.plugin.PluginDescriptionFile;

import com.minecarts.dbconnector.DBConnector;
import com.minecarts.dbconnector.providers.Provider;


public class DBQuery extends org.bukkit.plugin.java.JavaPlugin {
    private static final Logger logger = Logger.getLogger("com.minecarts.dbquery"); 
    
    private PluginDescriptionFile pdf;
    private DBConnector dbc;

    public void onEnable() {
        pdf = getDescription();
        dbc = (DBConnector) getServer().getPluginManager().getPlugin("DBConnector");
        
        logf("Enabled {0}", pdf.getVersion());
    }
    
    public void onDisable() {
    }
    
    
    public Provider getProvider(String name) {
        return dbc.getProvider(name);
    }
    
    
    public void log(String message) {
        log(Level.INFO, message);
    }
    public void log(Level level, String message) {
        logger.log(level, MessageFormat.format("{0}> {1}", pdf.getName(), message));
    }
    
    public void logf(String message, Object... args) {
        log(MessageFormat.format(message, args));
    }
    public void logf(Level level, String message, Object... args) {
        log(level, MessageFormat.format(message, args));
    }
    
}