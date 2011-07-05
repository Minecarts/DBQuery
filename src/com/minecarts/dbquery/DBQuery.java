package com.minecarts.dbquery;

import java.util.logging.Logger;
import java.util.logging.Level;

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
    
    
    // sync
    public QueryHelper getConnection(String provider) {
        return new QueryHelper(dbc.getProvider(provider));
    }
    // async
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
        
        protected RunnableCallback callback;
        
        public AsyncQueryHelper(Provider provider) {
            super(provider);
        }
        
        
        public AsyncQueryHelper callback(Object scope, String method, Object... args) {
            return callback(new RunnableCallback(scope, method, args));
        }
        public AsyncQueryHelper callback(RunnableCallback callback) {
            this.callback = callback;
            return this;
        }
        
        
        public Object execute(Method method, String sql, Object... params) throws SQLException {
            getServer().getScheduler().scheduleAsyncDelayedTask(DBQuery.this, new RunnableExecute(method, sql, params, callback));
            return null;
        }
        
        
        public class RunnableCallback implements Runnable {

            public Object scope;
            public String method;
            public Object result;
            public Object[] args;

            public RunnableCallback(Object scope, String method, Object... args) {
                this.scope = scope;
                this.method = method;
                this.args = args;
            }

            public RunnableCallback setResult(Object result) {
                this.result = result;
                return this;
            }
            
            public void run() {
                Object[] args = QueryFragment.concat(new Object[]{result}, this.args);
                Class[] classes = new Class[args.length];
                for(int i = 0; i < args.length; i++) {
                    classes[i] = args[i].getClass();
                }
                
                try {
                    (scope.getClass().getMethod(method, classes)).invoke(scope, args);
                }
                catch(Exception e) {
                    e.printStackTrace();
                }
            }
        }
        
        
        private class RunnableExecute implements Runnable {
            public Method method;
            public String sql;
            public Object[] params;
            public RunnableCallback callback;
            
            public RunnableExecute(Method method, String sql, Object[] params) {
                this(method, sql, params, null);
            }
            public RunnableExecute(Method method, String sql, Object[] params, RunnableCallback callback) {
                this.method = method;
                this.sql = sql;
                this.params = params;
                this.callback = callback;
            }
            
            public void run() {
                Object result;
                try {
                    result = AsyncQueryHelper.super.execute(method, sql, params);
                }
                catch(SQLException e) {
                    result = e;
                }
                
                if(callback != null) {
                    getServer().getScheduler().scheduleSyncDelayedTask(DBQuery.this, callback.setResult(result));
                }
            }
            
        }
        
    }
    
}