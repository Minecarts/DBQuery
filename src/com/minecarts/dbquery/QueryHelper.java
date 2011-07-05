package com.minecarts.dbquery;

import java.util.HashMap;
import java.util.ArrayList;

import java.lang.reflect.Method;

import java.sql.SQLException;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;

import com.minecarts.dbconnector.providers.Provider;

/**
 *
 * @author Kevin
 */
public class QueryHelper {
    
    public final Provider provider;
    
    public static final QueryFragment
            ColumnValues = new QueryFragment("`{0}`=VALUES(`{0}`)"),
            ColumnLastInsertId = new QueryFragment("`{0}`=LAST_INSERT_ID(`{0}`)");
    
    protected static Method AFFECTED, INSERT_ID, FETCH, GENERATED_KEYS;
    static {
        Class self = QueryHelper.class;
        try {
            AFFECTED = self.getMethod("getUpdateCount", PreparedStatement.class);
            INSERT_ID = self.getMethod("getInsertId", PreparedStatement.class);
            FETCH = self.getMethod("getResultSet", PreparedStatement.class);
            GENERATED_KEYS = self.getMethod("getGeneratedKeys", PreparedStatement.class);
        }
        catch(NoSuchMethodException e) {
            e.printStackTrace();
        }
    }
    
    
    public QueryHelper(Provider provider) {
        this.provider = provider;
    }
    
    
    // TODO: use Type... params and PreparedStatement's primitive methods
    public static PreparedStatement prepare(Connection conn, String sql, Object... params) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        for(int i = 0; i < params.length; i++) {
            stmt.setObject(i + 1, params[i]);
        }
        return stmt;
    }
    
    public Object execute(Method method, String sql, Object... params) throws SQLException {
        Connection conn = provider.getConnection();
        if(conn == null) return null;
        
        PreparedStatement stmt = prepare(conn, sql, params);
        try {
            stmt.execute();
            
            try {
                return method.invoke(this, stmt);
            }
            // TODO: Improve exception handling
            catch(Exception e) {
                e.printStackTrace();
                return null;
            }
        }
        finally {
            stmt.close();
            conn.close();
        }
    }
    
    
    public Integer affected(String sql, Object... params) throws SQLException {
        return (Integer) execute(AFFECTED, sql, params);
    }
    
    public Integer insertId(String sql, Object... params) throws SQLException {
        return (Integer) execute(INSERT_ID, sql, params);
    }
    
    public ArrayList<HashMap> fetch(String sql, Object... params) throws SQLException {
        return (ArrayList<HashMap>) execute(FETCH, sql, params);
    }
    
    public ArrayList<HashMap> generatedKeys(String sql, Object... params) throws SQLException {
        return (ArrayList<HashMap>) execute(GENERATED_KEYS, sql, params);
    }
    
    
    
    
    public static Integer getUpdateCount(PreparedStatement stmt) throws SQLException {
        return (Integer) stmt.getUpdateCount();
    }
    
    public static Integer getInsertId(PreparedStatement stmt) throws SQLException {
        ResultSet results = stmt.getGeneratedKeys();
        return results.last() ? (Integer) results.getInt(1) : null;
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
