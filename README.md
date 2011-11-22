# Usage

### Set up defaults by extending `com.minecarts.dbquery.Query`

    public class ExamplePlugin extends org.bukkit.plugin.java.JavaPlugin {
        // ...
        private DBConnector dbc;

        public void onEnable() {
            // ...
            dbc = (DBConnector) getServer().getPluginManager().getPlugin("DBConnector");
        }
        
        class Query extends com.minecarts.dbquery.Query {
            public Query(String sql) {
                super(ExamplePlugin.this, dbc.getProvider("minecarts"), sql);
            }
        }
    }

### Prepare your query

    Query query = new Query("SELECT * FROM sessions WHERE NOW() < TIMESTAMPADD(SECOND, ?, end) LIMIT 100") {
        // protected boolean async = false; // true by default

        @Override
        public void onFetch(ArrayList<HashMap> rows) {
            System.out.println("Got rows: " + rows);
        }

        @Override
        public void onException(Exception x, SchedulableQuery query) {
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
    query.sync().fetch(30); // sets queries to run synchronously, then fetches
