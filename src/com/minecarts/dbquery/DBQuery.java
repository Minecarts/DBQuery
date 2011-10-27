package com.minecarts.dbquery;

import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.Arrays;

import java.lang.reflect.Method;

import java.text.MessageFormat;

import java.sql.SQLException;

import org.bukkit.util.config.Configuration;
import org.bukkit.plugin.PluginDescriptionFile;

import com.minecarts.dbconnector.DBConnector;
import com.minecarts.dbconnector.providers.Provider;


public class DBQuery extends org.bukkit.plugin.java.JavaPlugin {
    private static final Logger logger = Logger.getLogger("com.minecarts.dbquery"); 
    
    private PluginDescriptionFile pdf;
    private Configuration config;
    private DBConnector dbc;

    public void onEnable() {
        pdf = getDescription();
        config = getConfiguration();
        dbc = (DBConnector) getServer().getPluginManager().getPlugin("DBConnector");
        
        logf("Enabled {0}", pdf.getVersion());
    }
    
    public void onDisable() {
    }
    
    
    public QueryHelper getConnection(String provider) {
        return new QueryHelper(dbc.getProvider(provider));
    }
    public AsyncQueryHelper getAsyncConnection(String provider) {
        return new AsyncQueryHelper(dbc.getProvider(provider));
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
    
    
    public class AsyncCallback extends Callback {
        public void schedule() {
            getServer().getScheduler().scheduleAsyncDelayedTask(DBQuery.this, this);
        }
    }
    
    public class AsyncQueryHelper extends QueryHelper {
        
        public AsyncQueryHelper(Provider provider) {
            super(provider);
        }
        
        public Object execute(final Method method, final String sql, Object... params) {
            final Callback callback;
            final Object[] newParams;
            
            if(params[params.length - 1] instanceof Callback) {
                callback = (Callback) params[params.length - 1];
                newParams = Arrays.copyOf(params, params.length - 1);
            }
            else {
                callback = null;
                newParams = params;
            }
            
            getServer().getScheduler().scheduleAsyncDelayedTask(DBQuery.this, new Runnable() {
                public void run() {
                    Object result;
                    try {
                        result = AsyncQueryHelper.super.execute(method, sql, newParams);
                    }
                    catch(Exception e) {
                        result = e;
                    }
                    
                    final Object finalResult = result;
                    
                    if(callback != null) {
                        getServer().getScheduler().scheduleSyncDelayedTask(DBQuery.this, new Runnable() {
                            public void run() {
                                callback.onComplete(finalResult);
                            }
                        });
                    }
                }
            });
            
            return null;
        }
        
    }
    
}