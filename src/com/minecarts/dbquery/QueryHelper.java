package com.minecarts.dbquery;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.Arrays;

import java.lang.reflect.Method;

import java.sql.SQLException;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;

import com.minecarts.dbconnector.providers.Provider;


public class QueryHelper {
    
    public final Provider provider;
    
    public static final QueryFragment
            ColumnValues = new QueryFragment("`{0}`=VALUES(`{0}`)"),
            ColumnLastInsertId = new QueryFragment("`{0}`=LAST_INSERT_ID(`{0}`)");
    
    
    public QueryHelper(Provider provider) {
        this.provider = provider;
    }
    
    
    public static PreparedStatement prepare(Connection conn, String sql, Object... params) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        for(int i = 0; i < params.length; i++) {
            stmt.setObject(i + 1, params[i]);
        }
        return stmt;
    }
    
    
    public PreparedStatement execute(String sql, Object... params) throws SQLException {
        Connection conn = provider.getConnection();
        if(conn == null) return null;
        
        PreparedStatement stmt = prepare(conn, sql, params);
        try {
            stmt.execute();
            return stmt;
        }
        finally {
            stmt.close();
            conn.close();
        }
    }
    
    
    public Integer affected(String sql, Object... params) throws SQLException {
        PreparedStatement stmt = execute(sql, params);
        if(stmt == null) return null;
        
        return getUpdateCount(stmt);
    }
    
    public Integer insertId(String sql, Object... params) throws SQLException {
        PreparedStatement stmt = execute(sql, params);
        if(stmt == null) return null;
        
        return getInsertId(stmt);
    }
    
    public ArrayList<HashMap> fetch(String sql, Object... params) throws SQLException {
        PreparedStatement stmt = execute(sql, params);
        if(stmt == null) return null;
        
        return getResultSet(stmt);
    }
    
    public ArrayList<HashMap> generatedKeys(String sql, Object... params) throws SQLException {
        PreparedStatement stmt = execute(sql, params);
        if(stmt == null) return null;
        
        return getGeneratedKeys(stmt);
    }
    
    
    
    
    public static int getUpdateCount(PreparedStatement stmt) throws SQLException {
        return stmt.getUpdateCount();
    }
    
    public static int getInsertId(PreparedStatement stmt) throws SQLException {
        ResultSet results = stmt.getGeneratedKeys();
        return results.last() ? results.getInt(1) : null;
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
    
}
