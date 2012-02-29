package com.minecarts.dbquery;

import com.minecarts.dbconnector.provider.Provider;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.Arrays;

import java.sql.SQLException;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;

import org.bukkit.plugin.Plugin;

import org.json.simple.JSONObject;


public class Query {
    public final Plugin plugin;
    public final Provider provider;
    public final String sql;
    
    protected boolean async = true;
    
    
    public enum CallbackType {
        EXECUTE, FETCH, FETCH_ONE, AFFECTED, INSERT_ID, GENERATED_KEYS
    }
    
    public Query(Plugin plugin, Provider provider, String sql) {
        this.plugin = plugin;
        this.provider = provider;
        this.sql = sql;
    }
    
    
    // Callbacks
    public void onBeforeCallback() { }
    public void onBeforeCallback(FinalQuery query) { }
    public void onExecute() { }
    public void onExecute(FinalQuery query) { }
    public void onException(Exception e) { }
    public void onException(Exception e, FinalQuery query) { }
    public void onFetch(ArrayList<HashMap> rows) { }
    public void onFetch(ArrayList<HashMap> rows, FinalQuery query) { }
    public void onFetchOne(HashMap row) { }
    public void onFetchOne(HashMap row, FinalQuery query) { }
    public void onAffected(Integer affected) { }
    public void onAffected(Integer affected, FinalQuery query) { }
    public void onInsertId(Integer id) { }
    public void onInsertId(Integer id, FinalQuery query) { }
    public void onGeneratedKeys(ArrayList<HashMap> keys) { }
    public void onGeneratedKeys(ArrayList<HashMap> keys, FinalQuery query) { }
    public void onComplete() { }
    public void onComplete(FinalQuery query) { }
    public void onAfterCallback() { }
    public void onAfterCallback(FinalQuery query) { }
    
    
    public Query async() {
        return async(true);
    }
    public Query async(boolean on) {
        async = on;
        return this;
    }
    public Query sync() {
        return async(false);
    }
    public Query sync(boolean on) {
        return async(!on);
    }
    
    private Query execute(CallbackType type, Object... params) {
        new FinalQuery(type, params).run();
        return this;
    }
    
    public Query execute(Object... params) {
        return execute(CallbackType.EXECUTE, params);
    }
    public Query affected(Object... params) {
        return execute(CallbackType.AFFECTED, params);
    }
    public Query insertId(Object... params) {
        return execute(CallbackType.INSERT_ID, params);
    }
    public Query fetch(Object... params) {
        return execute(CallbackType.FETCH, params);
    }
    public Query fetchOne(Object... params) {
        return execute(CallbackType.FETCH_ONE, params);
    }
    public Query generatedKeys(Object... params) {
        return execute(CallbackType.GENERATED_KEYS, params);
    }
    
    
    public static PreparedStatement prepare(Connection conn, String sql, Object... params) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        for(int i = 0; i < params.length; i++) {
            stmt.setObject(i + 1, params[i]);
        }
        return stmt;
    }
    
    public static Integer getUpdateCount(PreparedStatement stmt) throws SQLException {
        return stmt.getUpdateCount();
    }
    
    public static Integer getInsertId(PreparedStatement stmt) throws SQLException {
        ResultSet results = stmt.getGeneratedKeys();
        return results.last() ? results.getInt(1) : null;
    }
    
    public static HashMap getRow(PreparedStatement stmt) throws SQLException {
        ResultSet results = stmt.getResultSet();
        ResultSetMetaData meta = results.getMetaData();
        int columnCount = meta.getColumnCount();
        
        if(!results.first()) return null;
        
        HashMap<String, Object> row = new HashMap();
        for(int i = 0; i < columnCount; i++) {
            row.put(meta.getColumnName(i + 1), results.getObject(i + 1));
        }

        return row;
    }
    
    public static ArrayList<HashMap> getRows(ResultSet results) throws SQLException {
        ArrayList<HashMap> rows = new ArrayList();
        ResultSetMetaData meta = results.getMetaData();
        int columnCount = meta.getColumnCount();

        while(results.next()) {
            HashMap<String, Object> row = new HashMap();
            for(int i = 0; i < columnCount; i++) {
                row.put(meta.getColumnName(i + 1), results.getObject(i + 1));
            }
            rows.add(row);
        }

        return rows;
    }
    
    public static ArrayList<HashMap> getResultSet(PreparedStatement stmt) throws SQLException {
        return getRows(stmt.getResultSet());
    }
    
    public static ArrayList<HashMap> getGeneratedKeys(PreparedStatement stmt) throws SQLException {
        return getRows(stmt.getGeneratedKeys());
    }
    
    
    
    public class FinalQuery {
        public final CallbackType type;
        public final Object[] params;
        public final boolean async = Query.this.async;
        
        private Long start;
        private Long elapsed;
        
        public FinalQuery(CallbackType type, Object... params) {
            this.type = type;
            this.params = params;
        }
        
        @Override
        public String toString() {
            return new JSONObject() {{
                put("type", type);
                put("sql", sql);
                put("params", Arrays.asList(params));
            }}.toString();
        }
        
        public Long elapsed() {
            return elapsed;
        }
        
        public void run() {
            run(async);
        }
        public void run(boolean async) {
            if(async) {
                plugin.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, new Runnable() {
                    public void run() {
                        execute(plugin);
                    }
                });
            }
            else {
                execute(null);
            }
        }
        
        private void execute(Plugin plugin) {
            Object result = null;
            
            start = System.currentTimeMillis();
            elapsed = null;
            
            try {
                Connection conn = provider.getConnection();
                if(conn == null) throw new NoConnectionException();

                PreparedStatement stmt = prepare(conn, sql, params);
                try {
                    stmt.execute();
                    
                    switch(type) {
                        case FETCH:
                            result = getResultSet(stmt);
                            break;
                        case FETCH_ONE:
                            result = getRow(stmt);
                            break;
                        case AFFECTED:
                            result = getUpdateCount(stmt);
                            break;
                        case INSERT_ID:
                            result = getInsertId(stmt);
                            break;
                        case GENERATED_KEYS:
                            result = getGeneratedKeys(stmt);
                            break;
                    }
                }
                finally {
                    stmt.close();
                    conn.close();
                }
            }
            catch(Exception e) {
                result = e;
            }
            
            elapsed = System.currentTimeMillis() - start;
            
            
            if(plugin == null) {
                // sync query
                callback(type, result);
            }
            else {
                // async query, return to main thread
                final Object finalResult = result;
                final CallbackType finalType = type;
                plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                    public void run() {
                        callback(finalType, finalResult);
                    }
                });
            }
        }
        
        
        private void callback(CallbackType type, Object result) {
            onBeforeCallback();
            onBeforeCallback(this);
            
            if(result instanceof Exception) {
                onException((Exception) result);
                onException((Exception) result, this);
            }
            else {
                switch(type) {
                    case EXECUTE:
                        onExecute();
                        onExecute(this);
                    case FETCH:
                        onFetch((ArrayList<HashMap>) result);
                        onFetch((ArrayList<HashMap>) result, this);
                        break;
                    case FETCH_ONE:
                        onFetchOne((HashMap) result);
                        onFetchOne((HashMap) result, this);
                        break;
                    case AFFECTED:
                        onAffected((Integer) result);
                        onAffected((Integer) result, this);
                        break;
                    case INSERT_ID:
                        onInsertId((Integer) result);
                        onInsertId((Integer) result, this);
                        break;
                    case GENERATED_KEYS:
                        onGeneratedKeys((ArrayList<HashMap>) result);
                        onGeneratedKeys((ArrayList<HashMap>) result, this);
                        break;
                }
            }
            
            onComplete();
            onComplete(this);
            
            onAfterCallback();
            onAfterCallback(this);
        }
        
        
    }
    
}
