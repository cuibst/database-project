package database.rzotgorz.metaSystem;

import database.rzotgorz.managesystem.SQLTreeVisitor;
import database.rzotgorz.recordsystem.Record;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Slf4j
public class TableInfo implements Serializable {
    private String name;
    private Map<String, ColumnInfo> columns;
    private List<String> primary;
    private Map<String, SQLTreeVisitor.ForeignKey> foreign;
    private List<String> index;
    private HashMap<String, String> unique;
    private List<Integer> sizeList;
    private List<String> typeList;
    private HashMap<String, Integer> colIndex;
    private int totalSize;

    public TableInfo(String name, Map<String, ColumnInfo> columns) {
        this.name = name;
        this.columns = columns;
        this.primary = new ArrayList<>();
        this.foreign = new HashMap<>();
        this.sizeList = new ArrayList<>();
        this.typeList = new ArrayList<>();
        this.totalSize = 0;
        this.index = new ArrayList<>();
        this.unique = new HashMap<>();
        this.colIndex = new HashMap<>();
        this.updateParams();
    }

    public Map<String, String[]> describe() {
        Map<String, String[]> objects = new HashMap<>();
        for (Map.Entry<String, ColumnInfo> entry : columns.entrySet()) {
            objects.put(entry.getKey(), entry.getValue().getDescription());
        }
        for (int i = 0; i < this.primary.size(); i++) {
            objects.get(primary.get(i))[3] = "PRI";
        }
        for (int i = 0; i < this.foreign.size(); i++) {
            if (objects.get(foreign.get(i))[3].equals("")) {
                objects.get(foreign.get(i))[3] = "FOR";
            } else {
                objects.get(foreign.get(i))[3] = "MUL";
            }
        }
        for (Map.Entry<String, String> entry : unique.entrySet()) {
            if (objects.get(foreign.get(entry.getKey()))[3].equals("")) {
                objects.get(foreign.get(entry.getKey()))[3] = "UNI";
            } else {
                objects.get(foreign.get(entry.getKey()))[3] = "";
            }
        }
        return objects;
    }

    public void updateParams() {
        int __ = 0;
        for (Map.Entry<String, ColumnInfo> entry : columns.entrySet()) {
            this.colIndex.put(entry.getValue().getName(), __);
            __++;
            this.typeList.add(entry.getValue().getType());
            this.sizeList.add(entry.getValue().getSize());
        }
        this.totalSize = 0;
        for (int i = 0; i < this.sizeList.size(); i++)
            this.totalSize += this.sizeList.get(i);
    }

    public void insertColumn(ColumnInfo columnInfo) {
        if (columns.containsKey(columnInfo.getName()))
            throw new RuntimeException(String.format("Column %s already exits", columnInfo.getName()));
        columns.put(columnInfo.getName(), columnInfo);
        this.updateParams();
    }

    public void removeColumn(String name) {
        if (!columns.containsKey(name)) {
            throw new RuntimeException(String.format("Column %s not exits", name));
        }
        columns.remove(name);
        this.updateParams();
    }

    public void addForeign(String col, SQLTreeVisitor.ForeignKey foreign) {
        this.foreign.put(col, foreign);
    }

    public void removeForeign(String col) {
        this.foreign.remove(col);
    }

    public void addUnique(String col, String unique) {
        this.unique.put(col, unique);
    }

    public byte[] buildRecord(List<String> list) {
        return Parser.encode(this.sizeList, this.typeList, this.totalSize, list);
    }

    public void setPrimary(List<String> array) {
        this.primary = array;
    }

    public List<Object> loadRecord(Record record) throws UnsupportedEncodingException {
        return Parser.decode(this.sizeList, this.typeList, this.totalSize, record);
    }

    public Integer getIndex(String name) {
        return this.colIndex.get(name);
    }

    public void checkValueMap(HashMap<String, String> map) {
        for (Map.Entry<String, String> entry : map.entrySet()) {
            ColumnInfo columnInfo = columns.get(entry.getKey());
            if (columnInfo == null) {
                throw new RuntimeException(String.format("column named :%s is unknown", entry.getKey()));
            }
            if (columnInfo.getType().equals("DATE"))
                map.put(entry.getKey(), Parser.parserDate(entry.getValue()).toString());
        }
    }

    public ArrayList<String> getHeader() {
        ArrayList<String> list = new ArrayList<>();
        for (Map.Entry<String, ColumnInfo> entry : this.columns.entrySet()) {
            list.add(this.name + "." + entry.getValue().getName());
        }
        return list;
    }

    public boolean existsIndex(String name) {
        return this.index.contains(name);
    }

    public void createIndex(String name, int rootId) {
        this.colIndex.put(name, rootId);
    }

    public void removeIndex(String name) {
        this.colIndex.remove(name);
    }
}
