package database.rzotgorz.managesystem;

import database.rzotgorz.indexsystem.FileIndex;
import database.rzotgorz.indexsystem.IndexManager;
import database.rzotgorz.managesystem.clauses.*;
import database.rzotgorz.managesystem.functions.*;
import database.rzotgorz.managesystem.results.*;
import database.rzotgorz.metaSystem.*;
import database.rzotgorz.parser.SQLLexer;
import database.rzotgorz.parser.SQLParser;
import database.rzotgorz.recordsystem.FileHandler;
import database.rzotgorz.recordsystem.RID;
import database.rzotgorz.recordsystem.Record;
import database.rzotgorz.recordsystem.RecordManager;
import database.rzotgorz.utils.Csv;
import database.rzotgorz.utils.FileScanner;
import lombok.extern.slf4j.Slf4j;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.ATNConfigSet;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.ParseCancellationException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.NotDirectoryException;
import java.util.*;

@Slf4j
public class DatabaseController {

    public final static String TABLE_SUFFIX = ".table";
    public final static String INDEX_SUFFIX = ".index";

    private final RecordManager recordManager;
    private final IndexManager indexManager;
    private final MetaManager metaManager;
    private final String rootPath;
    private final Set<String> databases = new HashSet<>();
    private String currentUsingDatabase = null;
    private final SQLTreeVisitor visitor;

    public Set<String> getDatabases() {
        return databases;
    }

    public String getCurrentUsingDatabase() {
        return currentUsingDatabase;
    }

    public DatabaseController(SQLTreeVisitor visitor) throws NotDirectoryException {
        recordManager = new RecordManager();
        indexManager = new IndexManager(recordManager.getFileManager());
        metaManager = new MetaManager();
        rootPath = "data";
        this.visitor = visitor;
        visitor.controller = this;

        File rootDirectory = new File(rootPath);
        if (!rootDirectory.isDirectory())
            throw new NotDirectoryException("Path 'data' has already been used by a file");
        File[] files = rootDirectory.listFiles();
        if (files != null && files.length > 0 && files[0] != null)
            for (File file : files)
                if (file.isDirectory())
                    databases.add(file.getName());
    }

    private String getDatabasePath(String name) {
        return rootPath + File.separator + name;
    }

    private String getTablePath(String tableName) {
        return currentUsingDatabase + File.separator + tableName + TABLE_SUFFIX;
    }

    public void createDatabase(String name) throws FileAlreadyExistsException {
        if (databases.contains(name)) {
            throw new FileAlreadyExistsException("Database " + name + " already exists!");
        }
        String databasePath = getDatabasePath(name);
        File file = new File(databasePath);
        assert !file.exists();
        file.mkdir();
        databases.add(name);
    }

    public DatabaseChangeResult useDatabase(String name) throws FileNotFoundException {
        if (!databases.contains(name)) {
            throw new FileNotFoundException("Database " + name + " doesn't exist");
        }
        currentUsingDatabase = name;
        return new DatabaseChangeResult(name);
    }

    public DatabaseChangeResult dropDatabase(String name) throws FileNotFoundException {
        if (!databases.contains(name))
            throw new FileNotFoundException("Database " + name + " doesn't exist!");
        File databaseDirectory = new File(getDatabasePath(name));
        assert databaseDirectory.exists() && databaseDirectory.isDirectory();
        indexManager.closeHandler(name);
        metaManager.closeMeta(name);
        File[] files = databaseDirectory.listFiles();
        if (files != null && files.length > 0 && files[0] != null)
            for (File file : files) {
                recordManager.closeFile(file.getName());
                file.delete();
            }
        databaseDirectory.delete();
        databases.remove(name);
        if (currentUsingDatabase != null && currentUsingDatabase.equals(name)) {
            currentUsingDatabase = null;
            return new DatabaseChangeResult("null");
        }
        return null;
    }

    public ResultItem showTables() {
        if (currentUsingDatabase == null)
            return new MessageResult("No database is selected.", true);
        File databaseDirectory = new File(getDatabasePath(currentUsingDatabase));
        assert databaseDirectory.exists() && databaseDirectory.isDirectory();
        Set<String> tables = new HashSet<>();
        File[] files = databaseDirectory.listFiles();
        if (files != null && files.length > 0 && files[0] != null)
            for (File file : files) {
                if (file.getName().endsWith(TABLE_SUFFIX))
                    tables.add(file.getName().substring(0, file.getName().length() - TABLE_SUFFIX.length()));
            }
        return new ListResult(tables, "TABLES");
    }

    public ResultItem createTable(TableInfo bundle) {
        if (currentUsingDatabase == null)
            return new MessageResult("No database is being used!", true);
        MetaHandler handler = metaManager.openMeta(currentUsingDatabase);
        handler.addTable(bundle);
        int recordLength = bundle.getTotalSize();
        recordManager.createFile(getTablePath(bundle.getName()), recordLength);
        return new MessageResult("ok");
    }

    public ResultItem dropTable(String tableName) {
        if (currentUsingDatabase == null)
            return new MessageResult("No database is being used!", true);
        MetaHandler handler = metaManager.openMeta(currentUsingDatabase);
        handler.removeTable(tableName);
        recordManager.deleteFile(getTablePath(tableName));
        return new MessageResult("ok");
    }

    private static class InfoAndHandler {
        public TableInfo info;
        public MetaHandler handler;

        public InfoAndHandler(TableInfo info, MetaHandler handler) {
            this.info = info;
            this.handler = handler;
        }
    }

    public InfoAndHandler getTableInfo(String tableName) {
        if (currentUsingDatabase == null)
            throw new RuntimeException("No using database!");
        MetaHandler handler = metaManager.openMeta(currentUsingDatabase);
        return new InfoAndHandler(handler.getTable(tableName), handler);
    }

    public ResultItem describeTable(String tableName) {
        InfoAndHandler pack = getTableInfo(tableName);
        List<String> header = Arrays.asList("Field", "Type", "Null", "Key", "Default", "Extra");
        LinkedHashMap<String, String[]> data = pack.info.describe();
        List<List<Object>> columns = new ArrayList<>();
        data.forEach((s, strings) -> columns.add(Arrays.asList(strings)));
        return new TableResult(header, columns);
    }

    public void addUniqueConstraint(String tableName, String columnName, String constraintName) throws Exception {
        InfoAndHandler pack = getTableInfo(tableName);
        pack.handler.addUnique(pack.info, columnName, constraintName);
        if (!pack.info.existsIndex(constraintName))
            createIndex(constraintName, tableName, columnName);
    }

    public void addForeignKeyConstraint(String tableName, String columnName, SQLTreeVisitor.ForeignKey foreignKey, String constraintName) throws Exception {
        if (currentUsingDatabase == null)
            throw new RuntimeException("No database is being used!");
        MetaHandler metaHandler = metaManager.openMeta(currentUsingDatabase);
        metaHandler.addForeign(tableName, columnName, foreignKey);
        if (constraintName == null && !metaHandler.existsIndex(foreignKey.targetTable + '.' + foreignKey.name))
            createIndex(foreignKey.targetTable + '.' + foreignKey.name, foreignKey.targetTable, foreignKey.name);
        else if (!metaHandler.existsIndex(constraintName))
            createIndex(constraintName, foreignKey.targetTable, foreignKey.name);
    }

    public void setPrimary(String tableName, SQLTreeVisitor.PrimaryKey primaryKey) throws Exception {
        if (currentUsingDatabase == null)
            throw new RuntimeException("No database is being used!");
        MetaHandler metaHandler = metaManager.openMeta(currentUsingDatabase);
        metaHandler.setPrimary(tableName, primaryKey == null ? null : primaryKey.fields);
        if (primaryKey == null)
            return;
        for (String column : primaryKey.fields) {
            if (!metaHandler.existsIndex(tableName + "." + column))
                createIndex(tableName + "." + column, tableName, column);
        }
    }

    public void removePrimary(String tableName) {
        if (currentUsingDatabase == null)
            throw new RuntimeException("No database is being used!");
        MetaHandler metaHandler = metaManager.openMeta(currentUsingDatabase);
        List<String> key = metaHandler.getTable(tableName).getPrimary();
        key.forEach(column -> {
            if (metaHandler.existsIndex(tableName + "." + column))
                removeIndex(tableName + "." + column);
        });
    }

    public void removeForeignKeyConstraint(String tableName, String column, String constraintName) {
        if (currentUsingDatabase == null)
            throw new RuntimeException("No database is being used!");
        if (constraintName == null) {
            MetaHandler metaHandler = metaManager.openMeta(currentUsingDatabase);
            metaHandler.removeForeign(tableName, column);
            SQLTreeVisitor.ForeignKey key = metaHandler.getTable(tableName).getForeign().get(column);
            removeIndex(key.targetTable + "." + key.name);
        } else
            removeIndex(constraintName);
    }

    public void createIndex(String indexName, String tableName, String columnName) throws Exception {
        InfoAndHandler pack = getTableInfo(tableName);
        if (pack.handler.existsIndex(indexName))
            throw new RuntimeException(String.format("Indices %s already exists!", indexName));
        if (pack.info.existsIndex(columnName)) {
            pack.handler.createIndex(indexName, tableName, columnName);
            return;
        }
        FileIndex index = indexManager.createIndex(currentUsingDatabase, tableName);
        pack.info.createIndex(columnName, index.getRootId());
        Integer columnId = pack.info.getIndex(columnName);
        if (columnId == null)
            throw new RuntimeException(String.format("Column %s not exists", columnName));
        FileHandler fileHandler = recordManager.openFile(getTablePath(tableName));
        FileScanner fileScanner = new FileScanner(fileHandler);
        for (Record record : fileScanner) {
            List<Object> data = pack.info.loadRecord(record);
            Object key = data.get(columnId);
            long keyId = (Long) key;
            index.insert(keyId, record.getRid());
        }
        pack.handler.createIndex(indexName, tableName, columnName);
    }

    public void removeIndex(String indexName) {
        if (currentUsingDatabase == null)
            throw new RuntimeException("No database is being used!");
        MetaHandler metaHandler = metaManager.openMeta(currentUsingDatabase);
        DbInfo.IndexInfo indexInfo = metaHandler.getIndexInfo(indexName);
        TableInfo tableInfo = metaHandler.getTable(indexInfo.tableName);
        if (!metaHandler.existsIndex(indexName))
            throw new RuntimeException(String.format("Index %s doesn't exist!", indexName));
        tableInfo.removeIndex(indexName);
        metaHandler.removeIndex(indexName);
        metaManager.closeMeta(currentUsingDatabase);
    }


    public List<Function> buildFunctions(String tableName, List<WhereClause> clauses, MetaHandler metaHandler) {
        TableInfo tableInfo = metaHandler.getTable(tableName);
        List<Function> functions = new ArrayList<>();
        for (WhereClause clause : clauses) {
            if (clause.getTableName() != null && !clause.getTableName().equals(tableName))
                continue;
            Integer index = tableInfo.getIndex(clause.getColumnName());
            if (index == null)
                throw new RuntimeException(String.format("Field %s for table %s is unknown.", clause.getColumnName(), clause.getTableName()));
            String type = tableInfo.getTypeList().get(index);
            if (clause instanceof OperatorClause) {
                if (clause.getTargetColumn() != null) {
                    if (!tableName.equals(clause.getTableName()))
                        continue;
                    int index2 = tableInfo.getIndex(clause.getTargetColumn());
                    functions.add(new AttributeCompare(index, index2, ((OperatorClause) clause).getOperator()));
                } else {
                    Object value = ((ValueOperatorClause) clause).getValue();
                    switch (type) {
                        case "INT":
                        case "FLOAT":
                            if (value.getClass() != Integer.class && value.getClass() != Float.class)
                                throw new RuntimeException(String.format("Type %s expected but get %s instead.", type, value.getClass()));
                            break;
                        case "DATE":
                            if (value.getClass() != Long.class)
                                throw new RuntimeException(String.format("Type %s expected but get %s instead.", type, value.getClass()));
                            break;
                        case "VARCHAR":
                            if (value.getClass() != String.class)
                                throw new RuntimeException(String.format("Type %s expected but get %s instead.", type, value.getClass()));
                            break;
                    }
                    functions.add(new ValueCompare(value, index, ((ValueOperatorClause) clause).getOperator()));
                }
            } else if (clause instanceof WhereInClause) {
                Set<Object> values = ((WhereInClause) clause).getValues();
                functions.add(new InFunction(values, index));
            } else if (clause instanceof LikeClause) {
                functions.add(new LikeFunction(((LikeClause) clause).getPattern(), index));
            } else if (clause instanceof NullClause) {
                functions.add(new NullFunction(index, ((NullClause) clause).isNull()));
            }
        }
        return functions;
    }

    public static class Interval {
        public int lower;
        public int upper;

        public Interval(int lower, int upper) {
            this.lower = lower;
            this.upper = upper;
        }
    }

    public Set<RID> filterIndices(String tableName, List<WhereClause> clauses) {
        Map<String, Interval> indexMap = new HashMap<>();
        InfoAndHandler pack = getTableInfo(tableName);
        clauses.forEach(clause -> {
            if ((!(clause instanceof ValueOperatorClause)) || (clause.getTableName() != null && !clause.getTableName().equals(tableName)))
                return;
            Integer index = pack.info.getIndex(clause.getColumnName());
            if (index != null && pack.info.existsIndex(clause.getColumnName())) {
                String operator = ((ValueOperatorClause) clause).getOperator();
                String columnName = clause.getColumnName();
                Interval interval = indexMap.get(columnName);
                if (interval == null)
                    interval = new Interval(Integer.MIN_VALUE, Integer.MAX_VALUE);
                if (((ValueOperatorClause) clause).getValue().getClass() != Integer.class)
                    return;
                int value = (Integer) ((ValueOperatorClause) clause).getValue();
                switch (operator) {
                    case "=":
                        interval.lower = Math.max(interval.lower, value);
                        interval.upper = Math.min(interval.upper, value);
                        break;
                    case "<":
                        interval.upper = Math.min(interval.upper, value - 1);
                        break;
                    case ">":
                        interval.lower = Math.max(interval.lower, value + 1);
                        break;
                    case "<=":
                        interval.upper = Math.min(interval.upper, value);
                        break;
                    case ">=":
                        interval.lower = Math.max(interval.lower, value);
                        break;
                    default:
                        return;
                }
                indexMap.put(columnName, interval);
            }
        });
        Set<RID> result = null;
        for (Map.Entry<String, Interval> entry : indexMap.entrySet()) {
            FileIndex index = indexManager.openedIndex(currentUsingDatabase, tableName, pack.info.getIndex(entry.getKey()));
            if (result == null) {
                ArrayList<RID> res = index.range(entry.getValue().lower, entry.getValue().upper);
                (result = new HashSet<>()).addAll(Set.copyOf(index.range(entry.getValue().lower, entry.getValue().upper)));
            } else
                result.retainAll(index.range(entry.getValue().lower, entry.getValue().upper));
        }
        return result;
    }

    public static class RecordDataPack {
        public final List<Record> records;
        public final List<List<Object>> data;

        public RecordDataPack(List<Record> records, List<List<Object>> data) {
            this.records = records;
            this.data = data;
        }
    }

    public RecordDataPack searchIndices(String tableName, List<WhereClause> clauses) throws UnsupportedEncodingException {
        InfoAndHandler pack = getTableInfo(tableName);
        List<Function> functionList = buildFunctions(tableName, clauses, pack.handler);
        Set<RID> remainingRIDs = filterIndices(tableName, clauses);
        FileHandler handler = recordManager.openFile(getTablePath(tableName));
        Iterable<Record> recordIterable;
        if (remainingRIDs != null) {
            List<Record> recordList = new ArrayList<>();
            remainingRIDs.forEach(rid -> recordList.add(handler.getRecord(rid)));
            recordIterable = recordList;
        } else {
            recordIterable = new FileScanner(handler);
        }
        RecordDataPack recordDataPack = new RecordDataPack(new ArrayList<>(), new ArrayList<>());
        for (Record record : recordIterable) {
            List<Object> values = pack.info.loadRecord(record);
            boolean flag = true;
            for (Function function : functionList) {
                flag = flag && function.consume(values);
            }
            if (flag) {
                recordDataPack.records.add(record);
                recordDataPack.data.add(values);
            }
        }
        return recordDataPack;
    }

    public TableResult scanIndices(String tableName, List<WhereClause> clauses) throws UnsupportedEncodingException {
        InfoAndHandler pack = getTableInfo(tableName);
        RecordDataPack recordDataPack = searchIndices(tableName, clauses);
        List<List<Object>> valuesList = new ArrayList<>();
        recordDataPack.data.forEach(objects -> {
            List<Object> result = new ArrayList<>();
            objects.forEach(object -> result.add(object == null ? "NULL" : object));
            valuesList.add(result);
        });
        return new TableResult(pack.info.getHeader(), valuesList);
    }

    public void insertRecord(String tableName, List<Object> valueList) {
        try {
            InfoAndHandler pack = getTableInfo(tableName);
            List<String> stringList = new ArrayList<>();
            valueList.forEach(obj -> stringList.add(obj.toString()));
            byte[] data = pack.info.buildRecord(stringList);
            FileHandler fileHandler = recordManager.openFile(getTablePath(tableName));
            Record rid = fileHandler.insertRecord(data);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ResultItem updateRecord(String tableName, List<SetClause> setClauses, List<WhereClause> whereClauses) {
        int length = 0;
        try {
            InfoAndHandler pack = getTableInfo(tableName);
            RecordDataPack dataPack = searchIndices(tableName, whereClauses);
            length = dataPack.records.size();
            FileHandler handler = this.recordManager.openFile(getTablePath(tableName));
            dataPack.records.forEach(record -> {
                try {
                    List<Object> values = pack.info.loadRecord(record);
                    List<String> headers = pack.info.getHeader();
                    setClauses.forEach(clause -> {
                        for (int i = 0; i < headers.size(); i++) {
                            String name = tableName + "." + headers.get(i);
                            log.info("name :{} , askedName:{} ", name, clause.getColumnName());
                            if (name.equals(clause.getColumnName())) {
                                values.set(i, clause.getValue());
                            }
                        }
                    });
                    handler.deleteRecord(record.getRid());
                    insertRecord(tableName, values);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            });

            dataPack.records.forEach(record -> {
                handler.deleteRecord(record.getRid());
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new OperationResult("Update", length);
    }


    public ResultItem deleteRecord(String tableName, List<WhereClause> whereClauses) {
        int length = 0;
        try {
            RecordDataPack data = searchIndices(tableName, whereClauses);
            length = data.records.size();
            FileHandler handler = this.recordManager.openFile(getTablePath(tableName));
            log.info(String.valueOf(data.records.size()));
            data.records.forEach(record -> {
                RID rid = record.getRid();
                handler.deleteRecord(rid);
            });
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return new OperationResult("Delete", length);
    }

    public ResultItem loadData(String csvName, String tableName) {
        try {
            csvName = csvName.replace("'", "");
            csvName = "." + File.separator + "csv" + File.separator + csvName;
            this.createTable(new TableInfo(tableName, Csv.parserHeader(csvName)));
            List<Object[]> objects = Csv.readCsv("", csvName);
            for (int i = 0; i < objects.size(); i++) {
                List<Object> objectList = new ArrayList<>();
                for (int j = 0; j < objects.get(i).length; j++) {
                    objectList.add(objects.get(i)[j]);
                }
                this.insertRecord(tableName, objectList);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new OperationResult("load", 1);
    }


    public ResultItem storeData(String csvName, String tableName) {
        if (currentUsingDatabase == null)
            throw new RuntimeException("No database is being used!");
        MetaHandler metaHandler = metaManager.openMeta(currentUsingDatabase);
        TableInfo tableInfo = metaHandler.getTable(tableName);
        FileHandler fileHandler = recordManager.openFile(getTablePath(tableName));
        FileScanner fileScanner = new FileScanner(fileHandler);
        List<NameAndTypePack> list = tableInfo.getPack();
        String[] headers = Csv.processHeader(list);
        csvName = csvName.replace("\'", "");
        Csv.createCsv("", csvName, headers, null, false);
        for (Record record : fileScanner) {
            try {
                List<Object> data = tableInfo.loadRecord(record);
                Csv.createCsv("", csvName, headers, data, true);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        return new OperationResult("dump", 1);
    }

    public ResultItem selectRecord(List<Selector> selectors, List<String> tableNames, List<WhereClause> clauses, SQLTreeVisitor.Column group) {
        if (currentUsingDatabase == null)
            throw new RuntimeException("No database is being used!");

        MetaHandler metaHandler = metaManager.openMeta(currentUsingDatabase);
        Map<String, List<String>> columnToTable = metaHandler.buildTable(tableNames);
        clauses.forEach(clause ->
        {
            String table = clause.getTableName();
            String column = clause.getColumnName();
            if (table == null) {
                List<String> tables = columnToTable.get(column);
                if (tables != null && tables.size() > 1)
                    throw new RuntimeException(String.format("Column %s is ambiguous.", column));
                if (tables == null || tables.size() == 0)
                    throw new RuntimeException(String.format("Unknown column %s.", column));
                clause.setTableName(tables.get(0));
            }
            table = clause.getTargetTable();
            column = clause.getTargetColumn();
            if (column != null && table == null) {
                List<String> tables = columnToTable.get(column);
                if (tables != null && tables.size() > 1)
                    throw new RuntimeException(String.format("Column %s is ambiguous.", column));
                if (tables == null || tables.size() == 0)
                    throw new RuntimeException(String.format("Unknown column %s.", column));
                clause.setTargetTable(tables.get(0));
            }
        });
        selectors.forEach(selector ->
        {
            String table = selector.getTableName();
            String column = selector.getColumnName();
            if (table == null) {
                List<String> tables = columnToTable.get(column);
                if (tables != null && tables.size() > 1)
                    throw new RuntimeException(String.format("Column %s is ambiguous.", column));
                if (tables == null || tables.size() == 0)
                    throw new RuntimeException(String.format("Unknown column %s.", column));
                selector.setTableName(tables.get(0));
            }
        });

        if (group != null && group.tableName == null)
            group.tableName = tableNames.get(0);

        String groupBy = group == null ? null : group.tableName + "." + group.columnName;

        Set<Selector.SelectorType> types = new HashSet<>();
        selectors.forEach(selector -> types.add(selector.getType()));
        if (group == null && types.size() > 1 && types.contains(Selector.SelectorType.FIELD))
            throw new RuntimeException("No group specified, can't resolve both aggregation and field");

        Map<String, TableResult> resultMap = new HashMap<>();
        for (
                String tableName : tableNames) {
            try {
                resultMap.put(tableName, scanIndices(tableName, clauses));
            } catch (UnsupportedEncodingException e) {
                return new MessageResult(e.getMessage(), true);
            }
        }

        //FIXME: deal with join later on.
        TableResult result = resultMap.get(tableNames.get(0));
        List<String> headers;
        List<List<Object>> actualData = new ArrayList<>();
        if (group != null) {
            int index = result.getHeaderIndex(groupBy);
            Map<Object, List<List<Object>>> groups = new HashMap<>();
            for (List<Object> row : result.getData()) {
                Object id = row.get(index);
                List<List<Object>> rowList = groups.computeIfAbsent(id, k -> new ArrayList<>());
                rowList.add(row);
            }
            if (selectors.get(0).getType() == Selector.SelectorType.ALL) {
                assert selectors.size() == 1;
                groups.values().forEach(grp -> actualData.add(grp.get(0)));
                return new TableResult(result.getHeaders(), actualData);
            }
            for (List<List<Object>> grp : groups.values()) {
                Map<String, List<Object>> categorizedGroup = new HashMap<>();
                for (int i = 0; i < result.getHeaders().size(); i++) {
                    List<Object> objectList = new ArrayList<>();
                    for (List<Object> record : grp) {
                        objectList.add(record.get(i));
                    }
                    categorizedGroup.put(result.getHeaders().get(i), objectList);
                }
                categorizedGroup.put("*.*", categorizedGroup.get(result.getHeaders().get(0)));
                List<Object> data = new ArrayList<>();
                selectors.forEach(selector -> data.add(selector.select(categorizedGroup.get(selector.target()))));
                actualData.add(data);
            }
        } else {
            if (selectors.get(0).getType() == Selector.SelectorType.ALL) {
                assert selectors.size() == 1;
                return result;
            } else if (types.contains(Selector.SelectorType.FIELD)) {
                headers = new ArrayList<>();
                for (Selector selector : selectors) {
                    headers.add(selector.target());
                }
                List<Integer> indices = new ArrayList<>();
                headers.forEach(s -> indices.add(result.getHeaderIndex(s)));
                result.getData().forEach(data -> {
                    List<Object> data1 = new ArrayList<>();
                    indices.forEach(id -> data1.add(data.get(id)));
                    actualData.add(data1);
                });
            } else {
                if (result.getData() == null) {
                    List<Object> data1 = new ArrayList<>();
                    for (int i = 0; i < result.getHeaders().size(); i++)
                        data1.add(null);
                    actualData.add(data1);
                } else {
                    Map<String, List<Object>> valueMap = new HashMap<>();
                    for (int i = 0; i < result.getHeaders().size(); i++) {
                        List<Object> objectList = new ArrayList<>();
                        for (List<Object> data1 : result.getData()) {
                            objectList.add(data1.get(i));
                        }
                        valueMap.put(result.getHeaders().get(i), objectList);
                    }
                    valueMap.put("*.*", valueMap.get(result.getHeaders().get(0)));
                    List<Object> data = new ArrayList<>();
                    selectors.forEach(selector -> data.add(selector.select(valueMap.get(selector.target()))));
                    actualData.add(data);
                }
            }
        }
        headers = new ArrayList<>();
        for (Selector selector : selectors) {
            headers.add(selector.toString(tableNames.size() > 1));
        }
        return new TableResult(headers, actualData);
    }

    public ResultItem selectWithLimit
            (List<Selector> selectors, List<String> tableNames, List<WhereClause> conditions, int limit,
             int offset, SQLTreeVisitor.Column group) {
        ResultItem result = selectRecord(selectors, tableNames, conditions, group);
        if (!(result instanceof TableResult))
            return result;
        List<List<Object>> data = (List<List<Object>>) ((TableResult) result).getData();
        if (limit == -1)
            data = data.subList(offset, data.size());
        else
            data = data.subList(offset, offset + limit);
        return new TableResult(((TableResult) result).getHeaders(), data);
    }

    public void shutdown() {
        metaManager.shutdown();
        indexManager.shutdown();
        recordManager.shutdown();
    }

    public static class ErrorListener implements ANTLRErrorListener {
        @Override
        public void syntaxError(Recognizer<?, ?> recognizer, Object o, int i, int i1, String s, RecognitionException e) {
            throw new ParseCancellationException("line " + i + ":" + i1 + " " + s);
        }

        @Override
        public void reportAmbiguity(Parser parser, DFA dfa, int i, int i1, boolean b, BitSet bitSet, ATNConfigSet atnConfigSet) {
            throw new ParseCancellationException("line " + i + ":" + i1 + " ambiguity detected.");
        }

        @Override
        public void reportAttemptingFullContext(Parser parser, DFA dfa, int i, int i1, BitSet bitSet, ATNConfigSet atnConfigSet) {
            throw new ParseCancellationException("line " + i + ":" + i1 + " attempting full context.");
        }

        @Override
        public void reportContextSensitivity(Parser parser, DFA dfa, int i, int i1, int i2, ATNConfigSet atnConfigSet) {
            throw new ParseCancellationException("line " + i + ":" + i1 + " report sensitivity problems.");
        }
    }

    public Object execute(String sql) {
        System.out.printf("Executing: %s%n", sql);
        visitor.getTimeDelta();
        CharStream stream = CharStreams.fromString(sql);
        SQLLexer lexer = new SQLLexer(stream);
        lexer.removeErrorListeners();
        lexer.addErrorListener(new ErrorListener());

        CommonTokenStream tokens = new CommonTokenStream(lexer);
        SQLParser parser = new SQLParser(tokens);
        parser.removeErrorListeners();
        parser.addErrorListener(new ErrorListener());

        SQLParser.ProgramContext context;

        try {
            context = parser.program();
        } catch (ParseCancellationException e) {
            e.printStackTrace();
            MessageResult result = new MessageResult(e.getMessage(), true);
            result.cost = visitor.getTimeDelta();
            return result;
        }

        try {
            return visitor.visit(context);
        } catch (ParseCancellationException e) {
            e.printStackTrace();
            MessageResult result = new MessageResult(e.getMessage(), true);
            result.cost = visitor.getTimeDelta();
            return result;
        }
    }

}
