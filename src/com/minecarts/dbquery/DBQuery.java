package com.minecarts.dbquery;

import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.Arrays;
import java.util.HashMap;
import java.util.ArrayList;

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
    
    
    public class AsyncQueryHelper extends QueryHelper {
        
        public AsyncQueryHelper(Provider provider) {
            super(provider);
        }
        
        
        public ArrayList<HashMap> fetch(String sql, Object... params) {
            if(params.length > 0 && params[params.length - 1] instanceof Callback) {
                fetch(sql, Arrays.copyOf(params, params.length - 1), (Callback) params[params.length - 1]);
            }
            else {
                fetch(sql, params, null);
            }
            
            return null;
        }
        
        public void fetch(final String sql, final Object[] params, final Callback callback) {
            getServer().getScheduler().scheduleAsyncDelayedTask(DBQuery.this, new Runnable() {
                public void run() {
                    Object result;
                    try {
                        result = AsyncQueryHelper.super.fetch(sql, params);
                    }
                    catch(final Exception e) {
                        result = e;
                    }
                    
                    if(callback != null) {
                        getServer().getScheduler().scheduleSyncDelayedTask(DBQuery.this, callback.clone(result));
                    }
                }
            });
        }
        
        
        public ArrayList<HashMap> generatedKeys(String sql, Object... params) {
            if(params.length > 0 && params[params.length - 1] instanceof Callback) {
                generatedKeys(sql, Arrays.copyOf(params, params.length - 1), (Callback) params[params.length - 1]);
            }
            else {
                generatedKeys(sql, params, null);
            }
            
            return null;
        }
        
        public void generatedKeys(final String sql, final Object[] params, final Callback callback) {
            getServer().getScheduler().scheduleAsyncDelayedTask(DBQuery.this, new Runnable() {
                public void run() {
                    Object result;
                    try {
                        result = AsyncQueryHelper.super.generatedKeys(sql, params);
                    }
                    catch(final Exception e) {
                        result = e;
                    }
                    
                    if(callback != null) {
                        getServer().getScheduler().scheduleSyncDelayedTask(DBQuery.this, callback.clone(result));
                    }
                }
            });
        }
        
        
        public Integer affected(String sql, Object... params) {
            if(params.length > 0 && params[params.length - 1] instanceof Callback) {
                affected(sql, Arrays.copyOf(params, params.length - 1), (Callback) params[params.length - 1]);
            }
            else {
                affected(sql, params, null);
            }
            
            return null;
        }
        
        public void affected(final String sql, final Object[] params, final Callback callback) {
            getServer().getScheduler().scheduleAsyncDelayedTask(DBQuery.this, new Runnable() {
                public void run() {
                    Object result;
                    try {
                        result = AsyncQueryHelper.super.affected(sql, params);
                    }
                    catch(final Exception e) {
                        result = e;
                    }
                    
                    if(callback != null) {
                        getServer().getScheduler().scheduleSyncDelayedTask(DBQuery.this, callback.clone(result));
                    }
                }
            });
        }
        
        
        public Integer insertId(String sql, Object... params) {
            if(params.length > 0 && params[params.length - 1] instanceof Callback) {
                insertId(sql, Arrays.copyOf(params, params.length - 1), (Callback) params[params.length - 1]);
            }
            else {
                insertId(sql, params, null);
            }
            
            return null;
        }
        
        public void insertId(final String sql, final Object[] params, final Callback callback) {
            getServer().getScheduler().scheduleAsyncDelayedTask(DBQuery.this, new Runnable() {
                public void run() {
                    Object result;
                    try {
                        result = AsyncQueryHelper.super.insertId(sql, params);
                    }
                    catch(final Exception e) {
                        result = e;
                    }
                    
                    if(callback != null) {
                        getServer().getScheduler().scheduleSyncDelayedTask(DBQuery.this, callback.clone(result));
                    }
                }
            });
        }
        
    }
    
}