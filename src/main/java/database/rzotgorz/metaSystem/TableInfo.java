package database.rzotgorz.metaSystem;

import database.rzotgorz.recordsystem.Record;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Data
@Slf4j
public class TableInfo implements Serializable {
    private String name;
    private Map<String, ColumnInfo> columns;
    private String primary;
    private Map<String, String> foreign;
    private ArrayList<String> index;
    private HashMap<String, String> unique;
    private ArrayList<Integer> sizeList;
    private ArrayList<String> typeList;
    private HashMap<String, Integer> colIndex;
    private int totalSize;

    public TableInfo(String name, Map<String, ColumnInfo> columns) {
        String[] names = name.split("\\\\");
        this.name = names[0];
        this.columns = columns;
        this.primary = "";
        this.foreign = new HashMap<>();
        this.sizeList = new ArrayList<>();
        this.typeList = new ArrayList<>();
        this.totalSize = 0;
        this.index = new ArrayList<>();
        this.unique = new HashMap<>();
        this.colIndex = new HashMap<>();
    }

    public void updateParams() {

    }

    public void insertColumn(ColumnInfo columnInfo) {
        if (columns.containsKey(columnInfo.getName()))
            throw new RuntimeException(String.format("Column {} already exits", columnInfo.getName()));
        columns.put(columnInfo.getName(), columnInfo);
    }

    public void removeColumn(String name) {
        if (!columns.containsKey(name)) {
            throw new RuntimeException(String.format("Column {} not exits", name));
        }
        columns.remove(name);
    }

    public void addForeign(String col, String foreign) {
        this.foreign.put(col, foreign);
    }

    public void removeForeign(String col) {
        if (this.foreign.containsKey(col))
            this.foreign.remove(col);
    }

    public void addUnique(String col, String unique) {
        this.unique.put(col, unique);
    }

    public byte[] buildRecord(ArrayList<String> list) {
        return Parser.encode(this.sizeList, this.typeList, this.totalSize, list);
    }

    public Map<Integer, String> loadRecord(Record record) throws UnsupportedEncodingException {
        return Parser.decode(this.sizeList, this.typeList, this.totalSize, record);
    }

    public Integer getIndex(String name) {
        return this.colIndex.get(name);
    }

    public void checkValueMap(HashMap<String, String> map) {
        for (Map.Entry<String, String> entry : map.entrySet()) {
            ColumnInfo columnInfo = columns.get(entry.getKey());
            if (columnInfo == null) {
                throw new RuntimeException(String.format("column name :{} is unknown", entry.getKey()));
            }
            if (columnInfo.getType() == "DATE")
                map.put(entry.getKey(), Parser.parserDate(entry.getValue()).toString());
        }
    }

    public ArrayList<String> getHeader() {
        ArrayList<String> list = new ArrayList<>();
        list.add(this.name);
        for (Map.Entry<String, ColumnInfo> entry : this.columns.entrySet()) {
            list.add(entry.getValue().getName());
        }
        return list;
    }

    public boolean existsIndex(String name) {
        return this.columns.containsKey(name);
    }

    public void createIndex(String name, int rootId) {
        this.colIndex.put(name, rootId);
    }

    public void removeIndex(String name) {
        this.colIndex.remove(name);
    }
}
