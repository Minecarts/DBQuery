package com.minecarts.dbquery;

import java.util.logging.Level;
import java.text.MessageFormat;

import com.minecarts.dbconnector.DBConnector;
import com.minecarts.dbconnector.provider.Provider;


public class DBQuery extends org.bukkit.plugin.java.JavaPlugin {
    private DBConnector dbc;
    
    @Override
    public void onEnable() {
        dbc = (DBConnector) getServer().getPluginManager().getPlugin("DBConnector");
        
        log("Version {0} enabled.", getDescription().getVersion());
    }
    
    
    public Provider getProvider() {
        return dbc.getProvider();
    }
    public Provider getProvider(String name) {
        return dbc.getProvider(name);
    }
    
    
    public void log(String message) {
        log(Level.INFO, message);
    }
    public void log(Level level, String message) {
        getLogger().log(level, message);
    }
    public void log(String message, Object... args) {
        log(MessageFormat.format(message, args));
    }
    public void log(Level level, String message, Object... args) {
        log(level, MessageFormat.format(message, args));
    }
}