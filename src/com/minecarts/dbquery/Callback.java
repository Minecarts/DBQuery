package com.minecarts.dbquery;

import java.util.HashMap;
import java.util.ArrayList;



public class Callback implements Runnable, Cloneable {
    
    protected Object result;
    
    public Callback() {
        // this space intentionally left blank
    }
    public Callback(Object result) {
        this.result = result;
    }
    
    public Callback clone(Object result) {
        try {
            Callback callback = (Callback) clone();
            callback.result = result;
            return callback;
        }
        catch(CloneNotSupportedException e) {
            // not sure what to return...
            return this;
            //return new Callback();
            //return null;
        }
    }
    
    public void run() {
        if(result instanceof ArrayList) {
            onComplete((ArrayList) result);
        }
        else if(result instanceof Integer) {
            onComplete((Integer) result);
        }
        else if(result instanceof Exception) {
            onError((Exception) result);
        }
    }
    
    public void onComplete(ArrayList<HashMap> rowsOrGeneratedKeys) {
        // do nothing!
    }
    public void onComplete(Integer affectedOrId) {
        // do nothing!
    }
    public void onError(Exception e) {
        // do nothing!
    }
    
}
