package database.rzotgorz.metaSystem;

import com.alibaba.fastjson.JSONObject;

import java.io.*;
import java.util.ArrayList;
import java.util.Map;

public class MetaHandler {
    private DbInfo dbInfo;
    private String dbName;
    private String location;

    public MetaHandler(String dbName) throws IOException {
        this.dbName = dbName;
        this.location = "." + File.separator + "data" + File.separator;
        File file = new File(this.location + this.dbName + ".meta");
        if (file.exists())
            this.load();
        else {
            this.dbInfo = new DbInfo(this.dbName, new ArrayList<>());
            this.dump();
        }
    }

    public void load() {
        try {
            ObjectInputStream ois = new ObjectInputStream(
                    new BufferedInputStream(new FileInputStream(this.location + this.dbName + ".meta")));
            this.dbInfo = (DbInfo) ois.readObject();
            ois.close();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void dump() {
        try {
            FileOutputStream ff = new FileOutputStream(this.location + this.dbName + ".meta");
            ObjectOutputStream ss = new ObjectOutputStream(ff);
            ss.writeObject(this.dbInfo);
            ss.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addTable(TableInfo tableInfo) {
        this.dbInfo.insertTable(dbName, tableInfo);
        this.dump();
    }

    public void addColumn(String tbName, ColumnInfo columnInfo) {
        this.dbInfo.insertColumn(tbName, columnInfo);
        this.dump();
    }

    public void removeTable(String tbName) {
        this.dbInfo.removeTable(tbName);
        this.dump();
    }

    public void removeColumn(String tbName, String colName) {
        this.dbInfo.removeColumn(tbName, colName);
        this.dump();
    }

    public ColumnInfo getColumn(String tbName, String colName) {
        if (!this.dbInfo.getTbMap().containsKey(tbName))
            return null;
        return this.dbInfo.getTbMap().get(tbName).getColumns().get(colName);
    }

    public Integer getColumnIndex(String tbName, String colName) {
        if (!this.dbInfo.getTbMap().containsKey(tbName))
            return null;
        return this.dbInfo.getTbMap().get(tbName).getColIndex().get(colName);
    }

    public TableInfo getTable(String tbName) {
        if (!this.dbInfo.getTbMap().containsKey(tbName))
            throw new RuntimeException(String.format("Can not find table named:{}", tbName));
        return this.dbInfo.getTbMap().get(tbName);
    }

    public boolean existsIndex(String name) {
        return this.dbInfo.getIndex().containsKey(name);
    }

    public void createIndex(String indexName, String tbName, String colName) {
        this.dbInfo.createIndex(indexName, tbName, colName);
        this.dump();
    }

    public void removeIndex(String indexName) {
        this.dbInfo.removeIndex(indexName);
        this.dump();
    }

    public JSONObject getIndexInfo(String indexName) {
        return this.dbInfo.getIndexInfo(indexName);
    }

    public void setPrimary(String tbName, String primary) {
        this.dbInfo.getTbMap().get(tbName).setPrimary(primary);
        this.dump();
    }

    public void removePrimary(String tbName) {
        this.dbInfo.getTbMap().get(tbName).setPrimary(null);
        this.dump();
    }

    public void addForeign(String tbName, String colName, String foreign) {
        this.dbInfo.getTbMap().get(tbName).addForeign(colName, foreign);
        this.dump();
    }

    public void removeForeign(String tbName, String colName) {
        this.dbInfo.getTbMap().get(tbName).removeForeign(colName);
        this.dump();
    }

    public void addUnique(TableInfo tableInfo, String colName, String unique) {
        tableInfo.addUnique(colName, unique);
        this.dump();
    }

    public void renameTable(String oldName, String newName) {
        if (!this.dbInfo.getTbMap().containsKey(oldName))
            throw new RuntimeException(String.format("Can not find table named:{}", oldName));
        TableInfo tableInfo = this.dbInfo.getTbMap().get(oldName);
        this.dbInfo.getTbMap().remove(oldName);
        this.dbInfo.getTbMap().put(newName, tableInfo);

        for (Map.Entry<String, JSONObject> set : this.dbInfo.getIndex().entrySet()) {
            if (set.getValue().get("tb").equals(oldName)) {
                JSONObject object = new JSONObject();
                object.put("tb", newName);
                object.put("col", set.getValue().get("col"));
                this.dbInfo.getIndex().put(set.getKey(), object);
            }
        }
    }

    public void renameIndex(String oldName, String newName) {
        this.dbInfo.getIndex().put(newName, this.dbInfo.getIndex().get(oldName));
        this.dbInfo.getIndex().remove(oldName);
        this.dump();
    }

    public void close() {
        this.dump();
    }

    public JSONObject buildTable(ArrayList<String> tableNames) {
        JSONObject object = new JSONObject();
        for (int i = 0; i < tableNames.size(); i++) {
            TableInfo tableInfo = this.getTable(tableNames.get(i));
            ArrayList<String> columns = new ArrayList<>();
            for (int j = 0; j < tableInfo.getColumns().size(); j++) {
                columns.add(tableNames.get(i));
            }
            object.put(tableNames.get(i), columns);
        }
        return object;
    }
}
