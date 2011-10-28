package com.minecarts.dbquery;

import java.lang.reflect.Method;

import java.util.HashMap;
import java.util.ArrayList;


public class Callback implements Runnable {
    
    protected Object scope;
    protected Method method;
    protected Object[] args;
    
    public Callback() {
        // this space intentionally left blank
    }
    
    public void run() {
    }
    
    public void onComplete(ArrayList<HashMap> rowsOrGeneratedKeys) {
        // do nothing!
    }
    public void onComplete(Integer affectedOrId) {
        // do nothing!
    }
    
    public void onComplete(Exception e) {
        e.printStackTrace();
    }
    
}
