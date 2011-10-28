Examples
--------

### Synchronous queries:

    dbq = (DBQuery) getServer().getPluginManager().getPlugin("DBQuery");
    QueryHelper sync = dbq.getConnection("minecarts");

    try {
        ArrayList<HashMap> rows = sync.fetch("SELECT * FROM sessions WHERE NOW() < TIMESTAMPADD(SECOND, ?, end) LIMIT 100", 30);
        // do something with rows
    }
    catch(SQLException e) {
        // handle SQL exception
    }

### Asynchronous queries:

    dbq = (DBQuery) getServer().getPluginManager().getPlugin("DBQuery");
    AsyncQueryHelper async = dbq.getAsyncConnection("minecarts");

    async.fetch("SELECT * FROM sessions WHERE NOW() < TIMESTAMPADD(SECOND, ?, end) LIMIT 100", 30, new Callback() {
        public void onComplete(ArrayList<HashMap> rows) {
            // do something with rows
        }
        public void onError(Exception x) {
            try {
                throw x;
            }
            catch(SQLException e) {
                // handle SQL exception
            }
        }
    });
