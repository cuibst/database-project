package database.rzotgorz.metaSystem;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Data
@Slf4j
public class DbInfo implements Serializable {
    private String name;
    private Map<String, TableInfo> tbMap;
    private Map<String, JSONObject> index;

    public DbInfo(String name, ArrayList<TableInfo> tableInfos) {
        this.name = name;
        this.tbMap = new HashMap<>();
//        if (this.tbMap == null)
//            log.info("yes");
        for (int i = 0; i < tableInfos.size(); i++) {
            this.tbMap.put(tableInfos.get(i).getName(), tableInfos.get(i));
        }
        this.index = new HashMap<>();
    }

    public void insertTable(String name, TableInfo tableInfo) {
        if (this.tbMap.containsKey(name))
            throw new RuntimeException(String.format("Table named :%s already exists", name));
        this.tbMap.put(name, tableInfo);
    }

    public void insertColumn(String tbName, ColumnInfo columnInfo) {
        if (!this.tbMap.containsKey(tbName))
            throw new RuntimeException(String.format("Can not find table named :%s", tbName));
        this.tbMap.get(tbName).insertColumn(columnInfo);
    }


    public void removeTable(String name) {
        if (!this.tbMap.containsKey(name))
            throw new RuntimeException(String.format("Can not find table named :%s", name));
        this.tbMap.remove(name);
    }

    public void removeColumn(String tbName, String colName) {
        if (!this.tbMap.containsKey(tbName))
            throw new RuntimeException(String.format("Can not find table named :%s", tbName));
        this.tbMap.get(tbName).removeColumn(colName);
    }

    public void createIndex(String indexName, String tbName, String colName) {
        if (this.index.containsKey(indexName))
            throw new RuntimeException(String.format("Index named :%s already exists", indexName));
        JSONObject object = new JSONObject();
        object.put("tb", tbName);
        object.put("col", colName);
        this.index.put(indexName, object);
    }

    public void removeIndex(String name) {
        if (!this.index.containsKey(name))
            throw new RuntimeException(String.format("Can not find index named :%s", name));
        this.index.remove(name);
    }

    public JSONObject getIndexInfo(String name) {
        if (!this.index.containsKey(name))
            throw new RuntimeException(String.format("Can not find index named :%s", name));
        return this.index.get(name);
    }
}
