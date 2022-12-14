package database.rzotgorz.metaSystem;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MetaManager {
    Map<String, MetaHandler> metaMap;
    private String homeDir;

    public MetaManager() {
        metaMap = new HashMap<>();
        this.homeDir = "." + File.separator + "data" + File.separator;
    }

    public MetaHandler openMeta(String dbName) {
        if (!this.metaMap.containsKey(dbName)) {
            try {
                MetaHandler metaHandler = new MetaHandler(dbName);
                this.metaMap.put(dbName, metaHandler);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return this.metaMap.get(dbName);
    }

    public void closeMeta(String dbName) {
        if (this.metaMap.containsKey(dbName)) {
            this.metaMap.get(dbName).close();
        }
    }

    public void shutdown() {
        for (Map.Entry<String, MetaHandler> meta : this.metaMap.entrySet())
            this.closeMeta(meta.getKey());
    }

    public void removeAll(String dbName) {
        File file = new File(this.homeDir + dbName + File.separator + dbName + ".meta");
        if (file.exists()) {
            file.delete();
        }
    }
}
