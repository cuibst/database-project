package database.rzotgorz.metaSystem;

import database.rzotgorz.managesystem.SQLTreeVisitor;
import database.rzotgorz.recordsystem.Record;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.*;

@Data
@Slf4j
public class TableInfo implements Serializable {
    private String name;
    private LinkedHashMap<String, ColumnInfo> columns;
    private Map<String, Integer> indicesMap;
    private List<String> primary;
    private Map<String, SQLTreeVisitor.ForeignKey> foreign;
    private List<String> index;
    private HashMap<String, List<String>> unique;
    private List<Integer> sizeList;
    private List<String> typeList;
    private HashMap<String, Integer> colIndex;
    private int totalSize;

    public TableInfo(String name, LinkedHashMap<String, ColumnInfo> columns) {
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
        this.indicesMap = new HashMap<>();
        this.updateParams();
    }

    public static class TableDescription {
        public LinkedHashMap<String, String[]> columns;
        public List<String> headers;
        public List<String[]> keyInfos;

        public TableDescription(LinkedHashMap<String, String[]> columns, List<String> headers, List<String[]> keyInfos) {
            this.columns = columns;
            this.headers = headers;
            this.keyInfos = keyInfos;
        }
    }

    public TableDescription describe() {
        LinkedHashMap<String, String[]> objects = new LinkedHashMap<>();
        List<String> header = new ArrayList<>();
        header.add("keyName");
        for (Map.Entry<String, ColumnInfo> entry : columns.entrySet()) {
            objects.put(entry.getKey(), entry.getValue().getDescription());
            header.add(entry.getKey());
        }
        List<String[]> keysInfo = new ArrayList<>();
        String[] pri = new String[header.size()];
        for (int i = 0; i < header.size(); i++)
            pri[i] = "";
        pri[0] = "PRIMARY";
        for (String value : primary) {
            pri[header.indexOf(value)] = "x";
        }
        keysInfo.add(pri);
        foreign.forEach((s, foreignKey) -> {
            String[] info = new String[header.size()];
            for (int i = 0; i < header.size(); i++)
                info[i] = "";
            info[0] = String.format("FOREIGN KEY '%s' TO TABLE '%s'", foreignKey.foreignKeyName, foreignKey.targetTable);
            for (int i = 0; i < foreignKey.columns.size(); i++) {
                info[header.indexOf(foreignKey.columns.get(i))] = foreignKey.targetColumns.get(i);
            }
            keysInfo.add(info);
        });
        unique.forEach((name, uni) -> {
            String[] info = new String[header.size()];
            for (int i = 0; i < header.size(); i++)
                info[i] = "";
            info[0] = String.format("UNIQUE %s", name);
            uni.forEach(column -> info[header.indexOf(column)] = "o");
            keysInfo.add(info);
        });
        return new TableDescription(objects, header, keysInfo);
    }

    public void updateParams() {
        int __ = 0;
        this.typeList.clear();
        this.colIndex.clear();
        this.sizeList.clear();
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


    public void addForeign(String name, SQLTreeVisitor.ForeignKey foreign) {
        this.foreign.put(name, foreign);
    }

    public void removeForeign(String name) {
        this.foreign.remove(name);
    }

    public void addUnique(String col, List<String> unique) {
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
        if (!colIndex.containsKey(name))
            throw new RuntimeException(String.format("Cannot find the column :%s", name));
        return this.colIndex.get(name);
    }

//    public void checkValueMap(HashMap<String, String> map) {
//        for (Map.Entry<String, String> entry : map.entrySet()) {
//            ColumnInfo columnInfo = columns.get(entry.getKey());
//            if (columnInfo == null) {
//                throw new RuntimeException(String.format("column named :%s is unknown", entry.getKey()));
//            }
//            if (columnInfo.getType().equals("DATE"))
//                map.put(entry.getKey(), Parser.parserDate(entry.getValue()).toString());
//        }
//    }

    public List<String> getHeader() {
        List<String> list = new ArrayList<>();
        for (Map.Entry<String, ColumnInfo> entry : this.columns.entrySet()) {
            list.add(this.name + "." + entry.getValue().getName());
        }
        return list;
    }

    public List<String> getColumnName() {
        List<String> list = new ArrayList<>();
        for (Map.Entry<String, ColumnInfo> entry : this.columns.entrySet()) {
            list.add(entry.getValue().getName());
        }
        return list;
    }

    public List<NameAndTypePack> getPack() {
        List<NameAndTypePack> list = new ArrayList<>();
        for (Map.Entry<String, ColumnInfo> entry : this.columns.entrySet()) {
            list.add(new NameAndTypePack(entry.getValue().getType(), entry.getValue().getName(), entry.getValue().getSize()));
        }
        return list;
    }


    public boolean existsIndex(String name) {
        return this.indicesMap.containsKey(name);
    }

    public void createIndex(List<String> columns, int rootId) {
        this.indicesMap.put(name + "." + columns.toString(), rootId);
    }

    public Integer getRootId(List<String> columns) {
        return this.indicesMap.get(name + "." + columns.toString());
    }

    public void removeIndex(List<String> columns) {
        this.indicesMap.remove(name + "." + columns.toString());
    }
}
