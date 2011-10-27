package com.minecarts.dbquery;

import java.lang.reflect.Method;

public class Callback implements Runnable {
    
    protected Object scope;
    protected Method method;
    protected Object[] args;
    
    public Callback() {
        // this space intentionally left blank
    }
    
    public Callback(Object scope, String method, Object... args) {
        Class[] classes = new Class[args.length];
        for(int i = 0; i < args.length; i++) {
            classes[i] = args[i].getClass();
        }
        
        try {
            this.scope = scope;
            this.method = scope.getClass().getMethod(method, classes);
            this.args = args;
            schedule();
        }
        catch(NoSuchMethodException e) {
            onComplete(e);
        }
    }
    
    public void schedule() {
        run();
    }
    
    public void run() {
        try {
            onComplete(method.invoke(scope, args));
        }
        catch(Exception e) {
            onComplete(e);
        }
    }
    
    public void onComplete(Object... args) {
        // do nothing!
        System.out.print("onComplete");
    }
    public void onComplete(Exception e) {
        e.printStackTrace();
    }
    
}
