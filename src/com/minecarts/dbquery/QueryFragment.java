package com.minecarts.dbquery;

import java.util.Arrays;

import java.text.MessageFormat;


public class QueryFragment {
    
    protected String query;
    protected Object[] params;
    
    public QueryFragment(String query, Object... params) {
        this.query = query;
        this.params = params;
    }
    
    public String getQuery(Object... args) {
        return params.length > 0 ? MessageFormat.format(query, args) : query;
    }
    public Object[] getParams() {
        return params;
    }
    
    public QueryFragment append(String query, Object... params) {
        return new QueryFragment(this.query.concat(query), concat(this.params, params));
    }
    public QueryFragment append(QueryFragment fragment, Object... args) {
        return append(fragment.getQuery(args), fragment.getParams());
    }
    
    public QueryFragment prepend(String query, Object... params) {
        return new QueryFragment(query.concat(this.query), concat(params, this.params));
    }
    public QueryFragment prepend(QueryFragment fragment, Object... args) {
        return prepend(fragment.getQuery(args), fragment.getParams());
    }
    
    public static <T> T[] concat(T[] first, T[] second) {
        T[] result = Arrays.copyOf(first, first.length + second.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }
}
