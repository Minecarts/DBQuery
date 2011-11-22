package com.minecarts.dbquery;

import com.minecarts.dbconnector.providers.Provider;

import java.util.HashMap;
import java.util.ArrayList;

import java.sql.SQLException;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;

import org.bukkit.plugin.Plugin;


public class Query {
    public final Plugin plugin;
    public final Provider provider;
    public final String sql;
    
    protected boolean async = true;
    
    
    public enum QueryType {
        FETCH, FETCH_ONE, AFFECTED, INSERT_ID, GENERATED_KEYS
    }
    
    public Query(Plugin plugin, Provider provider, String sql) {
        this.plugin = plugin;
        this.provider = provider;
        this.sql = sql;
    }
    
    
    
    public void onFetch(ArrayList<HashMap> rows) {
        // do nothing!
    }
    public void onFetchOne(HashMap row) {
        // do nothing!
    }
    public void onAffected(Integer affected) {
        // do nothing!
    }
    public void onInsertId(Integer id) {
        // do nothing!
    }
    public void onGeneratedKeys(ArrayList<HashMap> keys) {
        // do nothing!
    }
    public void onExecute() {
        // do nothing!
    }
    public void onException(Exception e, FinalQuery query) {
        // do nothing!
    }
    
    
    public Query async() {
        async = true;
        return this;
    }
    public Query sync() {
        async = false;
        return this;
    }
    
    private Query execute(QueryType type, Object... params) {
        new FinalQuery(type, params).run();
        return this;
    }
    
    public Query execute(Object... params) {
        return execute(null, params);
    }
    public Query affected(Object... params) {
        return execute(QueryType.AFFECTED, params);
    }
    public Query insertId(Object... params) {
        return execute(QueryType.INSERT_ID, params);
    }
    public Query fetch(Object... params) {
        return execute(QueryType.FETCH, params);
    }
    public Query fetchOne(Object... params) {
        return execute(QueryType.FETCH_ONE, params);
    }
    public Query generatedKeys(Object... params) {
        return execute(QueryType.GENERATED_KEYS, params);
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
        public final QueryType type;
        public final Object[] params;
        public final boolean async = Query.this.async;
        
        public FinalQuery(QueryType type, Object... params) {
            this.type = type;
            this.params = params;
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
            try {
                Connection conn = provider.getConnection();
                if(conn == null) throw new NoConnectionException();

                PreparedStatement stmt = prepare(conn, sql, params);
                try {
                    stmt.execute();
                    
                    if(plugin == null) { // sync
                        switch(type) {
                            case FETCH:
                                onFetch(getResultSet(stmt));
                            case FETCH_ONE:
                                onFetchOne(getRow(stmt));
                            case AFFECTED:
                                onAffected(getUpdateCount(stmt));
                            case INSERT_ID:
                                onInsertId(getInsertId(stmt));
                            case GENERATED_KEYS:
                                onGeneratedKeys(getGeneratedKeys(stmt));
                            default:
                                onExecute();
                        }
                    }
                    else { // async
                        switch(type) {
                            case FETCH:
                                final ArrayList<HashMap> rows = getResultSet(stmt);
                                plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                                    public void run() {
                                        onFetch(rows);
                                    }
                                });
                            case FETCH_ONE:
                                final HashMap row = getRow(stmt);
                                plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                                    public void run() {
                                        onFetchOne(row);
                                    }
                                });
                            case AFFECTED:
                                final Integer affected = getUpdateCount(stmt);
                                plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                                    public void run() {
                                        onAffected(affected);
                                    }
                                });
                            case INSERT_ID:
                                final Integer id = getInsertId(stmt);
                                plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                                    public void run() {
                                        onInsertId(id);
                                    }
                                });
                            case GENERATED_KEYS:
                                final ArrayList<HashMap> keys = getGeneratedKeys(stmt);
                                plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                                    public void run() {
                                        onGeneratedKeys(keys);
                                    }
                                });
                            default:
                                plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                                    public void run() {
                                        onExecute();
                                    }
                                });
                        }
                    }
                }
                finally {
                    stmt.close();
                    conn.close();
                }
            }
            catch(final Exception e) {
                if(plugin == null) { // sync
                    onException(e, this);
                }
                else { // async
                    plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                        public void run() {
                            onException(e, FinalQuery.this);
                        }
                    });
                }
            }
        }
        
        
    }
    
}
