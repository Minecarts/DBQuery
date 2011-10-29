package com.minecarts.dbquery;

import java.util.Arrays;

import java.text.MessageFormat;


public class QueryFragment {
    
    protected String query;
    protected Object[] params;
    
    public static QueryFragment
            NOW = new QueryFragment("NOW()"),
            COLUMN_VALUES = new QueryFragment("`{0}`=VALUES(`{0}`)"),
            COLUMN_LAST_INSERT_ID = new QueryFragment("`{0}`=LAST_INSERT_ID(`{0}`)");
    
    public QueryFragment(String query, Object... params) {
        this.query = query;
        this.params = params;
    }
    
    public String getQuery(Object... args) {
        return args.length > 0 ? MessageFormat.format(query, args) : query;
    }
    public Object[] getParams() {
        return params;
    }
    
    public QueryFragment format(Object... args) {
        return new QueryFragment(getQuery(args));
    }
    
    public QueryFragment append(QueryFragment fragment, Object... args) {
        return append(fragment.getQuery(args), fragment.getParams());
    }
    public QueryFragment append(String query, Object... params) {
        return new QueryFragment(this.query.concat(query), concat(this.params, params));
    }
    
    public QueryFragment prepend(QueryFragment fragment, Object... args) {
        return prepend(fragment.getQuery(args), fragment.getParams());
    }
    public QueryFragment prepend(String query, Object... params) {
        return new QueryFragment(query.concat(this.query), concat(params, this.params));
    }
    
    public static <T> T[] concat(T[] first, T[] second) {
        T[] result = Arrays.copyOf(first, first.length + second.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }
}
