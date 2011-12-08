# Usage

### Set up defaults by extending `com.minecarts.dbquery.Query`

    public class ExamplePlugin extends org.bukkit.plugin.java.JavaPlugin {
        // ...
        private DBQuery dbq;

        public void onEnable() {
            // ...
            dbq = (DBQuery) getServer().getPluginManager().getPlugin("DBQuery");
        }
        
        class Query extends com.minecarts.dbquery.Query {
            public Query(String sql) {
                super(ExamplePlugin.this, dbq.getPool("mysql"), sql);
            }
        }
    }

### Prepare your query

    Query query = new Query("SELECT * FROM `users` LIMIT 100") {
        @Override
        public void onFetch(ArrayList<HashMap> rows) {
            System.out.println("Got rows: " + rows);
        }

        @Override
        public void onException(Exception x, FinalQuery query) {
            // rethrow
            try {
                throw x;
            }
            catch(Exception e) {
                e.printStackTrace();

                // retry query
                query.run();
            }
        }
    };

### Run query by calling the appropriate method with your query params

    query.fetch(30); // fetches sessions from the last 30 seconds asynchronously
    query.sync().fetch(30).async(); // toggles sync queries, fetches, then reverts to async
