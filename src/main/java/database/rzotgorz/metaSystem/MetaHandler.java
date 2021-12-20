package database.rzotgorz.metaSystem;

import database.rzotgorz.managesystem.SQLTreeVisitor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Slf4j
public class MetaHandler {
    private DbInfo dbInfo;
    private String dbName;
    private String location;

    public MetaHandler(String dbName) throws IOException {
        this.dbName = dbName;
        this.location = "." + File.separator + "data" + File.separator + dbName + File.separator;
        File file = new File(this.location + this.dbName + ".meta");
        if (file.exists()) {
            this.load();
        } else {
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
        this.dbInfo.insertTable(tableInfo.getName(), tableInfo);
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
        if (this.dbInfo == null)
            log.info("dbinfo is null");
        if (!this.dbInfo.getTbMap().containsKey(tbName))
            throw new RuntimeException(String.format("Can not find table named: %s", tbName));
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

    public DbInfo.IndexInfo getIndexInfo(String indexName) {
        return this.dbInfo.getIndexInfo(indexName);
    }

    public void setPrimary(String tbName, List<String> primary) {
        this.dbInfo.getTbMap().get(tbName).setPrimary(primary);
        this.dump();
    }

    public void removePrimary(String tbName) {
        this.dbInfo.getTbMap().get(tbName).setPrimary(null);
        this.dump();
    }

    public void addForeign(String tbName, String colName, SQLTreeVisitor.ForeignKey foreign) {
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
            throw new RuntimeException(String.format("Can not find table named:%s", oldName));
        TableInfo tableInfo = this.dbInfo.getTbMap().get(oldName);
        this.dbInfo.getTbMap().remove(oldName);
        this.dbInfo.getTbMap().put(newName, tableInfo);

        for (Map.Entry<String, DbInfo.IndexInfo> set : this.dbInfo.getIndex().entrySet()) {
            if (set.getValue().tableName.equals(oldName)) {
                this.dbInfo.getIndex().put(set.getKey(), new DbInfo.IndexInfo(newName, set.getValue().columnName));
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

    public Map<String, List<String>> buildTable(List<String> tableNames) {
        Map<String, List<String>> tableMap = new HashMap<>();
        for (String tableName : tableNames) {
            TableInfo tableInfo = this.getTable(tableName);
            for (ColumnInfo column : tableInfo.getColumns().values()) {
                String name = column.getName();
                List<String> list = tableMap.get(name);
                if (list == null)
                    list = new ArrayList<>();
                list.add(tableName);
                tableMap.put(name, list);
            }
        }
        return tableMap;
    }
}
