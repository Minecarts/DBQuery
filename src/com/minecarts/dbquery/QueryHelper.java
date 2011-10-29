package com.minecarts.dbquery;

import java.util.HashMap;
import java.util.ArrayList;

import java.sql.SQLException;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;

import com.minecarts.dbconnector.providers.Provider;


public class QueryHelper {
    
    public final Provider provider;
    
    public enum QueryType {
        FETCH, FETCH_ONE, AFFECTED, INSERT_ID, GENERATED_KEYS
    }
    
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
    
    
    public Object execute(QueryType type, String sql, Object... params) throws SQLException, NoConnectionException {
        Connection conn = provider.getConnection();
        if(conn == null) throw new NoConnectionException();
        
        PreparedStatement stmt = prepare(conn, sql, params);
        try {
            stmt.execute();
            
            switch(type) {
                case FETCH:
                    return getResultSet(stmt);
                case FETCH_ONE:
                    return getRow(stmt);
                case AFFECTED:
                    return getUpdateCount(stmt);
                case INSERT_ID:
                    return getInsertId(stmt);
                case GENERATED_KEYS:
                    return getGeneratedKeys(stmt);
            }
            
            return null;
        }
        finally {
            stmt.close();
            conn.close();
        }
    }
    
    
    public Integer affected(String sql, Object... params) throws SQLException, NoConnectionException {
        return (Integer) execute(QueryType.AFFECTED, sql, params);
    }
    
    public Integer insertId(String sql, Object... params) throws SQLException, NoConnectionException {
        return (Integer) execute(QueryType.INSERT_ID, sql, params);
    }
    
    public ArrayList<HashMap> fetch(String sql, Object... params) throws SQLException, NoConnectionException {
        return (ArrayList<HashMap>) execute(QueryType.FETCH, sql, params);
    }
    
    public HashMap fetchOne(String sql, Object... params) throws SQLException, NoConnectionException {
        return (HashMap) execute(QueryType.FETCH_ONE, sql, params);
    }
    
    public ArrayList<HashMap> generatedKeys(String sql, Object... params) throws SQLException, NoConnectionException {
        return (ArrayList<HashMap>) execute(QueryType.GENERATED_KEYS, sql, params);
    }
    
    public Integer insert(String table, HashMap<String, Object> row, Object... args) throws SQLException, NoConnectionException {
        if(row == null || row.isEmpty()) return null;
        
        ArrayList<String> values = new ArrayList<String>();
        ArrayList<Object> params = new ArrayList<Object>();
        
        for(String column : row.keySet()) {
            Object value = row.get(column);
            
            if(value instanceof QueryFragment) {
                values.add(((QueryFragment) value).getQuery());
                params.add(((QueryFragment) value).getParams());
            }
            else {
                values.add("?");
                params.add(value);
            }
        }
        
        StringBuilder sql = new StringBuilder();
        
        sql.append("INSERT INTO `").append(table).append("`\n");
        sql.append("(").append(join((String[]) row.keySet().toArray(), ", ", "`")).append(")\n");
        sql.append("VALUES\n");
        sql.append("(").append(join((String[]) values.toArray(), ", ", "`")).append(")\n");
        
        return insertId(sql.toString(), QueryFragment.concat(params.toArray(), args));
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
    
    
    public static String join(String[] pieces) {
        return join(pieces, null);
    }
    public static String join(String[] pieces, String glue) {
        return join(pieces, glue, null);
    }
    public static String join(String[] pieces, String glue, String wrap) {
        if(pieces.length == 0) return "";
        if(glue == null) glue = "";
        
        StringBuilder output = new StringBuilder();
        for(String piece : pieces) {
            output.append(wrap).append(piece).append(wrap).append(glue);
        }
        
        return output.substring(0, output.length() - glue.length());
    }
    
}
