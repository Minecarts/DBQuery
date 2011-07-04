package com.minecarts.dbquery;

import java.util.logging.Logger;
import java.util.List;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.util.config.Configuration;

import com.minecarts.dbconnector.providers.*;
import com.minecarts.dbconnector.DBConnector;
import java.util.logging.Level;


public class DBQuery extends org.bukkit.plugin.java.JavaPlugin{
    public final Logger log = Logger.getLogger("com.minecarts.dbquery"); 
    
    private DBConnector dbc;
    private PluginDescriptionFile pdf;
    private Configuration config;

    public void onEnable() {
        PluginManager pm = getServer().getPluginManager();
        PluginDescriptionFile pdf = getDescription();
        
        this.config = getConfiguration();
        
        //Get our MySQLPool
        this.dbc = (DBConnector) pm.getPlugin("DBConnector");
        //Get the connection based upon.. a config?, or something.
        //this.config.getString("connection");
        Connection db = dbc.getConnection("minecarts");
        
        getServer().getScheduler().scheduleAsyncDelayedTask(this, new Runnable(){
            public void run(){
                //Do something
            }
        });
        this.log("Enabled successfully.");
    }
    
    public void onDisable(){
        
    }
    

    public void log(String message, java.util.logging.Level level){
        this.log.log(level, this.pdf.getName() + "> " + message);
    }
    public void log(String message){
        this.log(message,Level.INFO);
    }
    
    
}