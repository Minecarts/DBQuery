package com.minecarts.dbquery;

import java.util.logging.Logger;
import java.util.logging.Level;

import java.text.MessageFormat;

import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.util.config.Configuration;

import com.minecarts.dbconnector.DBConnector;



public class DBQuery extends org.bukkit.plugin.java.JavaPlugin {
    private final Logger logger = Logger.getLogger("com.minecarts.dbquery"); 
    
    private PluginDescriptionFile pdf;
    private Configuration config;
    private DBConnector dbc;

    public void onEnable() {
        pdf = getDescription();
        config = getConfiguration();
        dbc = (DBConnector) getServer().getPluginManager().getPlugin("DBConnector");
        
        logf("Enabled {0}", pdf.getVersion());
        
        
        try {
            QueryHelper db = new QueryHelper(dbc.getProvider("minecarts"));
            log(db.fetch("SELECT COUNT(*) FROM subscriptions").toString());
            log(db.insertId("INSERT INTO tests (name) VALUES (?)", "kevin").toString());
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    public void onDisable() {
        
    }
    
    
    
    public QueryHelper getConnection(String provider) {
        return new QueryHelper(dbc.getProvider(provider));
    }
    
    
    
    public void log(String message) {
        log(Level.INFO, message);
    }
    public void log(Level level, String message) {
        logger.log(level, MessageFormat.format("{0}> {1}", pdf.getName(), message));
    }
    
    public void logf(String message, Object... args) {
        logf(Level.INFO, message, args);
    }
    public void logf(Level level, String message, Object... args) {
        log(level, MessageFormat.format(message, args));
    }
    
}