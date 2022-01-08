package database.rzotgorz.managesystem;

import database.rzotgorz.indexsystem.FileIndex;
import database.rzotgorz.indexsystem.IndexContent;
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
import database.rzotgorz.utils.Pair;
import database.rzotgorz.utils.UnionFindSet;
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
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
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

    public ResultItem showIndices() {
        if (currentUsingDatabase == null)
            return new MessageResult("No database is selected.", true);
        MetaHandler handler = metaManager.openMeta(currentUsingDatabase);
        DbInfo info = handler.getDbInfo();
        List<String> header = new ArrayList<>(List.of("NAME", "TABLE", "COLUMNS"));
        List<List<Object>> data = new ArrayList<>();
        info.getIndex().forEach((name, indexInfo) -> {
            data.add(new ArrayList<>(List.of(name, indexInfo.tableName, indexInfo.columnName.toString())));
        });
        return new TableResult(header, data);
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
        InfoAndHandler pack = getTableInfo(tableName);
        MetaHandler handler = metaManager.openMeta(currentUsingDatabase);
        pack.info.getIndicesMap().values().forEach(rootId -> {
            indexManager.closeIndex(tableName, rootId);
        });
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
        List<String> header = Arrays.asList("Field", "Type", "Null", "Default");
        TableInfo.TableDescription data = pack.info.describe();
        List<List<Object>> columns = new ArrayList<>();
        data.columns.forEach((s, strings) -> columns.add(Arrays.asList(strings)));
        TableResult columnInfo = new TableResult(header, columns);
        List<List<Object>> keys = new ArrayList<>();
        data.keyInfos.forEach(strings -> keys.add(Arrays.asList(strings)));
        TableResult keyInfo = new TableResult(data.headers, keys);
        return new DescriptionResult(columnInfo, keyInfo);
    }

    public void addUniqueConstraint(String tableName, String constraintName, List<String> columns) throws Exception {
        InfoAndHandler pack = getTableInfo(tableName);
        FileHandler fileHandler = recordManager.openFile(getTablePath(tableName));
        FileScanner fileScanner = new FileScanner(fileHandler);
        for (Record record : fileScanner) {
            List<Object> data = pack.info.loadRecord(record);
            if (checkAnyUnique(tableName, columns, data, record.getRid()))
                throw new RuntimeException(String.format("Duplicated keys for %s", data));
        }
        pack.handler.addUnique(pack.info, constraintName, columns);
        if (!pack.info.existsIndex(tableName + "." + columns))
            createIndex(tableName + "." + columns, tableName, columns);
    }

    public void dropUniqueConstraint(String tableName, String constraintName, List<String> columns) {
        InfoAndHandler pack = getTableInfo(tableName);
        pack.handler.addUnique(pack.info, constraintName, columns);
        if (pack.info.existsIndex(tableName + "." + columns))
            removeIndex(tableName + "." + columns);
    }

    public void addForeignKeyConstraint(String tableName, SQLTreeVisitor.ForeignKey foreignKey) throws UnsupportedEncodingException {
        if (currentUsingDatabase == null)
            throw new RuntimeException("No database is being used!");
        InfoAndHandler pack = getTableInfo(tableName);
        InfoAndHandler targetPack = getTableInfo(foreignKey.targetTable);
        FileHandler fileHandler = recordManager.openFile(getTablePath(tableName));
        FileScanner fileScanner = new FileScanner(fileHandler);
        for (Record record : fileScanner) {
            List<Object> data = pack.info.loadRecord(record);
            Object[] tmp = new Object[targetPack.info.getColIndex().size()];
            for (int i = 0; i < foreignKey.columns.size(); i++) {
                tmp[targetPack.info.getIndex(foreignKey.targetColumns.get(i))] = data.get(pack.info.getIndex(foreignKey.columns.get(i)));
            }
            List<Object> targetData = new ArrayList<>(Arrays.asList(tmp));
            if (!checkAnyUnique(foreignKey.targetTable, foreignKey.targetColumns, targetData, null))
                throw new RuntimeException(String.format("Foreign Key for %s cannot find matching result.", data.toString()));
        }
        pack.handler.addForeign(tableName, foreignKey);
        if (!pack.handler.existsIndex(tableName + "." + foreignKey.columns))
            createIndex(tableName + "." + foreignKey.columns, tableName, foreignKey.columns);
        if (!targetPack.handler.existsIndex(foreignKey.targetTable + "." + foreignKey.targetColumns))
            createIndex(foreignKey.targetTable + "." + foreignKey.targetColumns, foreignKey.targetTable, foreignKey.targetColumns);
    }

    public void insertColumn(String tableName, ColumnInfo columnInfo) {
        try {
            InfoAndHandler pack = getTableInfo(tableName);
            FileHandler fileHandler = recordManager.openFile(getTablePath(tableName));
            FileScanner fileScanner = new FileScanner(fileHandler);
            List<List<Object>> datas = new ArrayList<>();
            for (Record record : fileScanner) {
                List<Object> data = pack.info.loadRecord(record);
                deleteIndices(tableName, currentUsingDatabase, data, record.getRid());
                data.add(columnInfo.getDefaultValue());
                datas.add(data);
            }
            dropTable(tableName);
            pack.info.insertColumn(columnInfo);
            createTable(pack.info);
            datas.forEach(data -> {
                try {
                    insertRecord(tableName, data);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            });
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public void dropColumn(String tableName, String columnName) {
        try {
            InfoAndHandler pack = getTableInfo(tableName);
            FileHandler fileHandler = recordManager.openFile(getTablePath(tableName));
            FileScanner fileScanner = new FileScanner(fileHandler);
            List<List<Object>> datas = new ArrayList<>();
            for (Record record : fileScanner) {
                List<Object> data = pack.info.loadRecord(record);
                List<Object> oldData = new ArrayList<>();
                oldData.addAll(data);
                data.set(pack.info.getIndex(columnName), null);
                if (checkReverseForeignKeyConstraint(tableName, oldData, data)) {
                    throw new RuntimeException("Cannot drop this column due to reverse foreign key");
                }
                if (pack.info.getPrimary().contains(columnName)) {
                    throw new RuntimeException("Cannot drop this column because this column is in primary key");
                }
                pack.info.getUnique().forEach((s, item) -> {
                    if (item.contains(columnName)) {
                        throw new RuntimeException("Cannot drop this column because this column is in unique constraint");
                    }
                });
                pack.info.getForeign().forEach((s, foreignKey) -> {
                    if (foreignKey.columns.contains(columnName)) {
                        throw new RuntimeException("Cannot drop this column because this column is in foreign key");
                    }
                });
                deleteIndices(tableName, currentUsingDatabase, data, record.getRid());
                data.remove((int) pack.info.getIndex(columnName));
                datas.add(data);
            }
            dropTable(tableName);
            pack.info.removeColumn(columnName);
            createTable(pack.info);
            datas.forEach(data -> {
                try {
                    insertRecord(tableName, data);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            });
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public void setPrimary(String tableName, SQLTreeVisitor.PrimaryKey primaryKey) throws Exception {
        if (currentUsingDatabase == null)
            throw new RuntimeException("No database is being used!");
        InfoAndHandler pack = getTableInfo(tableName);
        FileHandler fileHandler = recordManager.openFile(getTablePath(tableName));
        FileScanner fileScanner = new FileScanner(fileHandler);
        for (Record record : fileScanner) {
            List<Object> data = pack.info.loadRecord(record);
            if (checkAnyUnique(tableName, primaryKey.fields, data, record.getRid()))
                throw new RuntimeException(String.format("Duplicated keys for %s", data));
        }
        pack.handler.setPrimary(tableName, primaryKey == null ? null : primaryKey.fields);
        if (primaryKey == null)
            return;
        if (!pack.handler.existsIndex(tableName + "." + primaryKey.fields))
            createIndex(tableName + "." + primaryKey.fields, tableName, primaryKey.fields);
    }

    public void removePrimary(String tableName) {
        if (currentUsingDatabase == null)
            throw new RuntimeException("No database is being used!");
        MetaHandler metaHandler = metaManager.openMeta(currentUsingDatabase);
        List<String> key = metaHandler.getTable(tableName).getPrimary();
        removeIndex(tableName + "." + key);
    }

    public void removeForeignKeyConstraint(String tableName, String targetTableName, List<String> columns) {
        if (currentUsingDatabase == null)
            throw new RuntimeException("No database is being used!");
        MetaHandler metaHandler = metaManager.openMeta(currentUsingDatabase);
        SQLTreeVisitor.ForeignKey key = metaHandler.getTable(tableName).getForeign().get(targetTableName + "." + columns);
        removeIndex(tableName + "." + key.columns);
        removeIndex(key.targetTable + "." + key.targetColumns);
        metaHandler.removeForeign(tableName, targetTableName + "." + columns);
    }

    public void createIndex(String indexName, String tableName, List<String> columnName) throws UnsupportedEncodingException {
        InfoAndHandler pack = getTableInfo(tableName);
        if (pack.handler.existsIndex(indexName))
            throw new RuntimeException(String.format("Indices %s already exists!", indexName));
        if (pack.info.existsIndex(columnName.toString())) {
            pack.handler.createIndex(indexName, tableName, columnName);
            return;
        }
        List<String> indexType = new ArrayList<>();
        List<Integer> columnId = new ArrayList<>();
        columnName.forEach(column -> {
            String type = pack.info.getColumns().get(column).getType();
            if (type.equals("VARCHAR"))
                indexType.add(String.format("%s(%d)", type, pack.info.getColumns().get(column).getSize()));
            else
                indexType.add(type);
            Integer id = pack.info.getIndex(column);
            if (id == null)
                throw new RuntimeException(String.format("Column %s not exists", column));
            columnId.add(id);
        });


        FileIndex index = indexManager.createIndex(currentUsingDatabase, tableName, indexType, columnName.toString());
        pack.info.createIndex(columnName, index.getRootId());
        FileHandler fileHandler = recordManager.openFile(getTablePath(tableName));
        FileScanner fileScanner = new FileScanner(fileHandler);
        int cnt = 0;
        for (Record record : fileScanner) {
            List<Object> data = pack.info.loadRecord(record);
            List<Comparable> content = new ArrayList<>();
            columnId.forEach(id -> content.add((Comparable) data.get(id)));
//            log.info("insert {}", content.get(0));
            if (content.get(0).equals(3))
                cnt++;
            index.insert(new IndexContent(content), record.getRid());
            int res = index.search(new IndexContent(new ArrayList<>(List.of(3)))).size();
//            log.info("true: {} index: {}", cnt, res);
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
        tableInfo.removeIndex(metaHandler.getIndexInfo(indexName).columnName);
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
                    if (!tableName.equals(clause.getTargetTable()))
                        continue;
                    log.info(tableInfo.getColIndex().toString());
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
        public Object lower;
        public Object upper;

        public boolean leftClose;
        public boolean rightClose;

        public Interval(Object lower, Object upper) {
            this.lower = lower;
            this.upper = upper;
            leftClose = rightClose = true;
        }
    }

    public Set<RID> filterIndices(String tableName, List<WhereClause> clauses) {
        Map<String, Interval> indexMap = new HashMap<>();
        InfoAndHandler pack = getTableInfo(tableName);
        clauses.forEach(clause -> {
            if ((!(clause instanceof ValueOperatorClause)) || (clause.getTableName() != null && !clause.getTableName().equals(tableName)))
                return;
            Integer index = pack.info.getRootId(new ArrayList<>(List.of(clause.getColumnName())));
            if (index != null && pack.info.existsIndex(tableName + "." + new ArrayList<>(List.of(clause.getColumnName())))) {
                String operator = ((ValueOperatorClause) clause).getOperator();
                String columnName = clause.getColumnName();
                Interval interval = indexMap.get(columnName);

                if (((ValueOperatorClause) clause).getValue().getClass() != Integer.class && ((ValueOperatorClause) clause).getValue().getClass() != Float.class)
                    return;
                if (pack.info.getColumns().get(columnName).getType().equals("INT")) {
                    if (interval == null)
                        interval = new Interval(Integer.MIN_VALUE, Integer.MAX_VALUE);
                    int value;
                    if (((ValueOperatorClause) clause).getValue().getClass() == Integer.class)
                        value = (Integer) ((ValueOperatorClause) clause).getValue();
                    else
                        value = ((Float) ((ValueOperatorClause) clause).getValue()).intValue();
                    switch (operator) {
                        case "=":
                            interval.lower = Math.max((Integer) interval.lower, value);
                            interval.upper = Math.min((Integer) interval.upper, value);
                            break;
                        case "<":
                            interval.upper = Math.min((Integer) interval.upper, value - 1);
                            break;
                        case ">":
                            interval.lower = Math.max((Integer) interval.lower, value + 1);
                            break;
                        case "<=":
                            interval.upper = Math.min((Integer) interval.upper, value);
                            break;
                        case ">=":
                            interval.lower = Math.max((Integer) interval.lower, value);
                            break;
                        default:
                            return;
                    }
                    indexMap.put(columnName, interval);
                } else if (pack.info.getColumns().get(columnName).getType().equals("FLOAT")) {
                    if (interval == null)
                        interval = new Interval(Float.MIN_VALUE, Float.MAX_VALUE);
                    float value;
                    if (((ValueOperatorClause) clause).getValue().getClass() == Integer.class)
                        value = ((Integer) ((ValueOperatorClause) clause).getValue()).floatValue();
                    else
                        value = ((Float) ((ValueOperatorClause) clause).getValue());
                    switch (operator) {
                        case "=":
                            if (value > (Float) interval.lower) {
                                interval.lower = value;
                                interval.leftClose = true;
                            }
                            if (value < (Float) interval.upper) {
                                interval.upper = value;
                                interval.rightClose = true;
                            }
                            break;
                        case "<":
                            if (value <= (Float) interval.upper) {
                                interval.upper = value;
                                interval.rightClose = false;
                            }
                            break;
                        case ">":
                            if (value >= (Float) interval.lower) {
                                interval.lower = value;
                                interval.leftClose = false;
                            }
                            break;
                        case "<=":
                            if (value < (Float) interval.upper) {
                                interval.upper = value;
                                interval.rightClose = true;
                            }
                            break;
                        case ">=":
                            if (value > (Float) interval.lower) {
                                interval.lower = value;
                                interval.leftClose = true;
                            }
                            break;
                        default:
                            return;
                    }
                    indexMap.put(columnName, interval);
                } else if (pack.info.getColumns().get(columnName).getType().equals("DATE")) {
                    if (interval == null)
                        interval = new Interval(Long.MIN_VALUE, Long.MAX_VALUE);
                    int value;
                    if (((ValueOperatorClause) clause).getValue().getClass() == Integer.class)
                        value = (Integer) ((ValueOperatorClause) clause).getValue();
                    else
                        value = ((Float) ((ValueOperatorClause) clause).getValue()).intValue();
                    switch (operator) {
                        case "=":
                            interval.lower = Math.max((Long) interval.lower, value);
                            interval.upper = Math.min((Long) interval.upper, value);
                            break;
                        case "<":
                            interval.upper = Math.min((Long) interval.upper, value - 1);
                            break;
                        case ">":
                            interval.lower = Math.max((Long) interval.lower, value + 1);
                            break;
                        case "<=":
                            interval.upper = Math.min((Long) interval.upper, value);
                            break;
                        case ">=":
                            interval.lower = Math.max((Long) interval.lower, value);
                            break;
                        default:
                            return;
                    }
                    indexMap.put(columnName, interval);
                }
            }
        });
        Set<RID> result = null;
        for (Map.Entry<String, Interval> entry : indexMap.entrySet()) {
            FileIndex index = indexManager.openedIndex(currentUsingDatabase, tableName, pack.info.getRootId(new ArrayList<>(List.of(entry.getKey()))), new ArrayList<>(List.of(entry.getKey())).toString());
            IndexContent lower = new IndexContent(new ArrayList<>(List.of((Comparable) entry.getValue().lower)));
            IndexContent upper = new IndexContent(new ArrayList<>(List.of((Comparable) entry.getValue().upper)));
//            log.info("{} {} {} {}", lower, upper, entry.getValue().leftClose, entry.getValue().rightClose);
            if (result == null) {
                ArrayList<RID> res = index.range(lower, upper, entry.getValue().leftClose, entry.getValue().rightClose);
                if (res == null)
                    return null;
                (result = new TreeSet<>(Comparator.naturalOrder())).addAll(res);
            } else
                result.retainAll(index.range(lower, upper, entry.getValue().leftClose, entry.getValue().rightClose));
        }
//        log.info("Filter size: {}", result == null ? 0 : result.size());
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
        int cnt = 0;
        for (Record record : recordIterable) {
            cnt++;
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
//        log.info("{} records received", cnt);
        return recordDataPack;
    }

    public boolean checkAnyUnique(String tableName, List<String> columns, List<Object> values, RID currentRow) throws UnsupportedEncodingException {
        InfoAndHandler infoPack = getTableInfo(tableName);
        if (infoPack.info.getRootId(columns) != null) {
            int rootId = infoPack.info.getRootId(columns);
            FileIndex index = indexManager.openedIndex(currentUsingDatabase, tableName, rootId, columns.toString());
            List<Comparable> value = new ArrayList<>();
            DbInfo.IndexInfo info = infoPack.handler.getDbInfo().getIndexInfo(tableName + "." + columns);
            info.columnName.forEach(column -> value.add((Comparable) values.get(infoPack.info.getIndex(column))));
//            for (int i = 0; i < values.size(); i++) {
//                value.add((Comparable) values.get(i));
//            }
//            log.info("{}", value);
            List<RID> rids = index.search(new IndexContent(value));
            if (rids == null || rids.isEmpty())
                return false;
            return rids.size() != 1 || !rids.contains(currentRow);
        }
        List<WhereClause> clauses = new ArrayList<>();
        for (int i = 0; i < columns.size(); i++) {
            clauses.add(new ValueOperatorClause(tableName, columns.get(i), "=", values.get(i)));
        }
        RecordDataPack pack = searchIndices(tableName, clauses);
        assert pack.records == null || pack.records.size() <= 1;
        return !((pack.records != null && pack.records.size() >= 1 && currentRow != null && currentRow.equals(pack.records.get(0).getRid())) || pack.records == null || pack.records.size() == 0);
    }

    public boolean checkNotNullConstraint(String tableName, List<Object> values) {
        InfoAndHandler pack = getTableInfo(tableName);
        pack.info.getColumns().forEach((name, columnInfo) -> {
            Object value = values.get(pack.info.getIndex(name));
//            log.info("value: {}, not null: {}", value, columnInfo.isNotNull());
            if (value == null && columnInfo.isNotNull())
                throw new RuntimeException(String.format("Not Null constraint violated on column %d, %s", pack.info.getIndex(name), name));
        });
        return false;
    }

    public boolean checkPrimaryConstraint(String tableName, List<Object> values, RID currentRow) throws UnsupportedEncodingException {
        InfoAndHandler pack = getTableInfo(tableName);
        if (pack.info.getPrimary().size() == 0)
            return false;
        pack.info.getPrimary().forEach(primary -> {
            if (values.get(pack.info.getIndex(primary)) == null)
                throw new RuntimeException("Primary key found NULL value.");
        });
        return checkAnyUnique(tableName, pack.info.getPrimary(), values, currentRow);
    }

    public boolean checkUniqueConstraint(String tableName, List<Object> values, RID currentRow) throws UnsupportedEncodingException {
        InfoAndHandler pack = getTableInfo(tableName);
        if (pack.info.getUnique().size() == 0)
            return false;
        for (List<String> unique : pack.info.getUnique().values()) {
            if (checkAnyUnique(tableName, unique, values, currentRow))
                return true;
        }
        return false;
    }

    public boolean checkForeignKeyConstraint(String tableName, List<Object> values) throws UnsupportedEncodingException {
        InfoAndHandler pack = getTableInfo(tableName);
        if (pack.info.getForeign().size() == 0)
            return false;
        for (Map.Entry<String, SQLTreeVisitor.ForeignKey> entry : pack.info.getForeign().entrySet()) {
            List<String> columns = entry.getValue().columns;
            SQLTreeVisitor.ForeignKey foreignKey = entry.getValue();
            List<WhereClause> clauses = new ArrayList<>();
            List<Comparable> indexValue = new ArrayList<>();
            for (int i = 0; i < columns.size(); i++) {
                String column = columns.get(i);
                String targetColumn = entry.getValue().targetColumns.get(i);
                Object value = values.get(pack.info.getIndex(column));
//                log.info("column:{}", column);
//                log.info("targetColumn:{}", targetColumn);
                indexValue.add((Comparable) value);
                if (value == null)
                    throw new RuntimeException("Foreign key can't have null value.");
                clauses.add(new ValueOperatorClause(foreignKey.targetTable, targetColumn, "=", value));
            }
            InfoAndHandler targetPack = getTableInfo(foreignKey.targetTable);
            if (targetPack.info.getRootId(foreignKey.targetColumns) != null) {
                int rootId = targetPack.info.getRootId(foreignKey.targetColumns);
//                log.info(targetPack.info.getPrimary().toString());
                FileIndex index = indexManager.openedIndex(currentUsingDatabase, foreignKey.targetTable, rootId, foreignKey.targetColumns.toString());
//                log.info("targetTable:{},rootId:{},targetColumns:{}", foreignKey.targetTable, rootId, foreignKey.targetColumns.toString());
                List<RID> rids = index.search(new IndexContent(indexValue));
                if (rids == null || rids.isEmpty()) {
                    return true;
                }
            } else {
                RecordDataPack recordDataPack = searchIndices(foreignKey.targetTable, clauses);
                if (recordDataPack.records == null || recordDataPack.records.size() == 0)
                    return true;
            }
        }
        return false;
    }

    /**
     * @param tableName Table's name.
     * @param values    original record value.
     * @param newValues new value, null if delete
     * @return true when violate the constraint after delete
     * @throws UnsupportedEncodingException ???
     */
    public boolean checkReverseForeignKeyConstraint(String tableName, List<Object> values, List<Object> newValues) throws UnsupportedEncodingException {
        InfoAndHandler pack = getTableInfo(tableName);
        for (TableInfo tableInfo : pack.handler.getDbInfo().getTbMap().values()) {
            if (tableInfo.getName().equals(tableName))
                continue;
            for (SQLTreeVisitor.ForeignKey foreignKey : tableInfo.getForeign().values()) {
                if (foreignKey.targetTable.equals(tableName)) {
                    List<Comparable> originalValue = new ArrayList<>();
                    List<Comparable> newValue = new ArrayList<>();
                    foreignKey.targetColumns.forEach(column -> {
                        originalValue.add((Comparable) values.get(tableInfo.getIndex(column)));
                        if (newValues != null)
                            newValue.add((Comparable) newValues.get(tableInfo.getIndex(column)));
                    });
                    IndexContent originContent = new IndexContent(originalValue);
                    if (newValues != null) {
                        IndexContent newContent = new IndexContent(newValue);
                        if (originContent.compareTo(newContent) == 0)
                            continue;
                    }
                    if (tableInfo.getRootId(foreignKey.targetColumns) != null) {
                        int rootId = tableInfo.getRootId(foreignKey.targetColumns);
                        FileIndex index = indexManager.openedIndex(currentUsingDatabase, tableInfo.getName(), rootId, foreignKey.columns.toString());
                        List<RID> rids = index.search(originContent);
                        if (rids != null && !rids.isEmpty())
                            return true;
                    } else {
                        List<WhereClause> clauses = new ArrayList<>();
                        for (int i = 0; i < foreignKey.columns.size(); i++) {
                            String column = foreignKey.columns.get(i);
                            Object value = values.get(pack.info.getIndex(column));
                            clauses.add(new ValueOperatorClause(tableInfo.getName(), column, "=", value));
                        }
                        RecordDataPack recordDataPack = searchIndices(foreignKey.targetTable, clauses);
                        if (recordDataPack.records != null && recordDataPack.records.size() != 0)
                            return true;
                    }
                }
            }
        }
        return false;
    }

    public void checkConstraints(String tableName, List<Object> values, RID currentRow) throws UnsupportedEncodingException {
        if (checkNotNullConstraint(tableName, values))
            throw new RuntimeException("Not Null constraint violated in table " + tableName + ".");
        if (checkPrimaryConstraint(tableName, values, currentRow))
            throw new RuntimeException("Primary key constraint violated in table " + tableName + ".");
        if (checkUniqueConstraint(tableName, values, currentRow))
            throw new RuntimeException("Unique constraint violated in table " + tableName + ".");
        if (checkForeignKeyConstraint(tableName, values))
            throw new RuntimeException("Foreign key constraint violated in table " + tableName + ".");
    }

    public TableResult scanIndices(String tableName, List<WhereClause> clauses) throws UnsupportedEncodingException {
        InfoAndHandler pack = getTableInfo(tableName);
        RecordDataPack recordDataPack = searchIndices(tableName, clauses);
        List<List<Object>> valuesList = new ArrayList<>();
        recordDataPack.data.forEach(objects -> {
            List<Object> result = new ArrayList<>();
            for (int i = 0; i < objects.size(); i++) {
                Object object = objects.get(i);
                if (object == null)
                    object = "NULL";
                else if (pack.info.getTypeList().get(i).equals("DATE")) {
                    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                    ParsePosition pos = new ParsePosition(0);
                    object = formatter.format(new Date((Long) object));
                }
                result.add(object);
            }
            valuesList.add(result);
        });
        return new TableResult(pack.info.getHeader(), valuesList);
    }

    public void insertRecord(String tableName, List<Object> valueList) throws UnsupportedEncodingException {
        InfoAndHandler pack = getTableInfo(tableName);
        checkConstraints(tableName, valueList, null);
        List<String> stringList = new ArrayList<>();
        valueList.forEach(obj -> {
            if (obj != null)
                stringList.add(obj.toString());
            else
                stringList.add(null);
        });
        if (pack.info.getColumns().size() != valueList.size()) {
            throw new RuntimeException("Value list length mismatch");
        }
        byte[] data = pack.info.buildRecord(stringList);
        FileHandler fileHandler = recordManager.openFile(getTablePath(tableName));
        Record rid = fileHandler.insertRecord(data);
        insertIndices(tableName, currentUsingDatabase, valueList, rid.getRid());
    }

    public void insertIndices(String tableName, String databaseName, List<Object> values, RID rid) {
        InfoAndHandler pack = getTableInfo(tableName);
        pack.info.getIndicesMap().forEach((indexName, rootId) -> {
            DbInfo.IndexInfo info = pack.handler.getDbInfo().getIndexInfo(indexName);
            List<Comparable> value = new ArrayList<>();
            info.columnName.forEach(column -> value.add((Comparable) values.get(pack.info.getIndex(column))));
            FileIndex index = indexManager.openedIndex(databaseName, tableName, rootId, info.columnName.toString());
            index.insert(new IndexContent(value), rid);
        });
    }

    public void deleteIndices(String tableName, String databaseName, List<Object> values, RID rid) {
        InfoAndHandler pack = getTableInfo(tableName);
        pack.info.getIndicesMap().forEach((indexName, rootId) -> {
            DbInfo.IndexInfo info = pack.handler.getDbInfo().getIndexInfo(indexName);
            List<Comparable> value = new ArrayList<>();
            info.columnName.forEach(column -> value.add((Comparable) values.get(pack.info.getIndex(column))));
            FileIndex index = indexManager.openedIndex(databaseName, tableName, rootId, info.columnName.toString());
            index.remove(new IndexContent(value), rid);
        });
    }

    public ResultItem updateRecord(String tableName, List<SetClause> setClauses, List<WhereClause> whereClauses) {
        int length = 0;
        try {
            InfoAndHandler pack = getTableInfo(tableName);
            RecordDataPack dataPack = searchIndices(tableName, whereClauses);
            length = dataPack.records.size();
            FileHandler handler = this.recordManager.openFile(getTablePath(tableName));
            boolean flag;
            for (int i = 0; i < dataPack.records.size(); i++) {
                List<Object> values = pack.info.loadRecord(dataPack.records.get(i));
                setClauses.forEach(clause -> {
                    int cnt = 0;
                    for (Map.Entry<String, ColumnInfo> entry : pack.info.getColumns().entrySet()) {
                        if (entry.getValue().getName().equals(clause.getColumnName())) {
                            switch (entry.getValue().getType()) {
                                case "DATE":
                                    values.set(cnt, clause.getValue());
                                    break;
                                case "INT":
                                    values.set(cnt, clause.getValue());
                                    break;
                                case "FLOAT":
                                    values.set(cnt, clause.getValue());
                                    break;
                                case "VARCHAR":
                                    values.set(cnt, clause.getValue());
                                    break;
                            }
                        }
                        cnt++;
                    }
                });
                checkConstraints(tableName, values, dataPack.records.get(i).getRid());
                flag = checkReverseForeignKeyConstraint(tableName, dataPack.data.get(i), values);
                if (flag)
                    throw new RuntimeException("Cannot update line due to reverse foreign key");
            }
            for (int j = 0; j < dataPack.records.size(); j++) {
                Record record = dataPack.records.get(j);
                try {
                    List<Object> values = pack.info.loadRecord(record);
                    List<Object> values1 = pack.info.loadRecord(dataPack.records.get(j));
                    List<String> headers = pack.info.getColumnName();
                    log.info(values.toString());
                    setClauses.forEach(clause -> {
                        for (int i = 0; i < headers.size(); i++) {
                            String name = headers.get(i);
                            if (name.equals(clause.getColumnName())) {
                                values.set(i, clause.getValue());
                            }
                        }
                        int cnt = 0;
                        for (Map.Entry<String, ColumnInfo> entry : pack.info.getColumns().entrySet()) {
                            if (entry.getValue().getName().equals(clause.getColumnName())) {
                                switch (entry.getValue().getType()) {
                                    case "INT":
                                        values1.set(cnt, clause.getValue());
                                        break;
                                    case "FLOAT":
                                        values1.set(cnt, clause.getValue());
                                        break;
                                    case "VARCHAR":
                                        values1.set(cnt, clause.getValue());
                                        break;
                                    case "DATE":
                                        values1.set(cnt, clause.getValue());
                                        break;
                                }
                            }
                            cnt++;
                        }
                    });
                    List<String> stringList = new ArrayList<>();
                    values.forEach(obj -> {
                        if (obj != null)
                            stringList.add(obj.toString());
                        else
                            stringList.add("null");
                    });
                    deleteIndices(tableName, currentUsingDatabase, dataPack.data.get(j), record.getRid());
                    this.insertRecord(tableName, values);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
            dataPack.records.forEach(record -> handler.deleteRecord(record.getRid()));
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
            InfoAndHandler pack = getTableInfo(tableName);
            FileHandler handler = this.recordManager.openFile(getTablePath(tableName));
            boolean flag = false;
            for (int i = 0; i < data.records.size(); i++) {
                flag |= checkReverseForeignKeyConstraint(tableName, data.data.get(i), null);
            }
            if (flag) {
                throw new RuntimeException("Cannot delete line due to ForeignKey constraint");
            }
            for (int i = 0; i < data.records.size(); i++) {
                RID rid = data.records.get(i).getRid();
                this.deleteIndices(tableName, currentUsingDatabase, data.data.get(i), rid);
                handler.deleteRecord(rid);
            }
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
            for (Object[] object : objects) {
                List<Object> objectList = new ArrayList<>(Arrays.asList(object));
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
        csvName = csvName.replace("'", "");
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

    public TableResult bruteForceJoin(Map<String, TableResult> resultMap, List<WhereClause> clauses) {
        //Step1: create edge map
        Map<Pair<String, String>, List<Pair<String, String>>> edgeMap = new HashMap<>();
        clauses.forEach(clause -> {
            if (!(clause instanceof ColumnOperatorClause))
                return;
            ColumnOperatorClause subClause = (ColumnOperatorClause) clause;
            if (clause.getTableName().equals(clause.getTargetTable()))
                return;
            if (!subClause.getOperator().equals("="))
                throw new RuntimeException("Cross-table comparison must be equal.");
            Pair<String, String> tablePair;
            Pair<String, String> columnPair;
            if (clause.getTableName().compareTo(clause.getTargetTable()) < 0) {
                tablePair = new Pair<>(clause.getTableName(), clause.getTargetTable());
                columnPair = new Pair<>(clause.getColumnName(), clause.getTargetColumn());
            } else {
                tablePair = new Pair<>(clause.getTargetTable(), clause.getTableName());
                columnPair = new Pair<>(clause.getTargetColumn(), clause.getColumnName());
            }
            if (edgeMap.containsKey(tablePair))
                edgeMap.get(tablePair).add(columnPair);
            else
                edgeMap.put(tablePair, new ArrayList<>(List.of(columnPair)));
        });

        //Step2: Join constraint-based tables
        UnionFindSet<String> unionFindSet = new UnionFindSet<>(resultMap.keySet());

        Map<String, TableResult> mergedMap = new HashMap<>(Map.copyOf(resultMap));
        Map<String, Boolean> marker = new HashMap<>();
        resultMap.keySet().forEach(key -> marker.put(key, false));

        List<Pair<String, String>> tablePairs = new ArrayList<>(List.copyOf(edgeMap.keySet()));
        tablePairs.sort((o1, o2) -> {
            int size1 = Math.max(resultMap.get(o1.first).getSize(), resultMap.get(o1.second).getSize());
            int size2 = Math.max(resultMap.get(o2.first).getSize(), resultMap.get(o2.second).getSize());
            if (size1 != size2)
                return size1 < size2 ? -1 : 1;
            return 0;
        });

        tablePairs.forEach(tablePair -> {
            List<Pair<String, String>> constraints = edgeMap.get(tablePair);
            String outer = unionFindSet.getRoot(tablePair.first);
            String inner = unionFindSet.getRoot(tablePair.second);
            log.info("table pair: {} {}", tablePair.first, tablePair.second);
            log.info("outer inner: {} {}", outer, inner);
            if (outer.equals(inner)) {
                List<List<Object>> nextResult = new ArrayList<>();
                for (List<Object> record : mergedMap.get(outer).getData()) {
                    boolean flag = true;
                    for (Pair<String, String> columnPair : constraints) {
                        String outerColumn = tablePair.first + '.' + columnPair.first;
                        String innerColumn = tablePair.second + '.' + columnPair.second;
                        int outerId = mergedMap.get(outer).getHeaderIndex(outerColumn);
                        int innerId = mergedMap.get(outer).getHeaderIndex(innerColumn);
                        if (!record.get(outerId).equals(record.get(innerId))) {
                            flag = false;
                            break;
                        }
                    }
                    if (flag)
                        nextResult.add(record);
                }
                mergedMap.replace(outer, new TableResult(mergedMap.get(outer).getHeaders(), nextResult));
            } else {
                int outerSize = mergedMap.get(outer).getSize();
                int innerSize = mergedMap.get(inner).getSize();
                log.info("{} {}", inner, outer);
                log.info("{} {}", outerSize, innerSize);
                boolean flag1 = false;
                List<String> nextHeader = new ArrayList<>();
                List<List<Object>> nextData = new ArrayList<>();
                if (!marker.get(outer) && !marker.get(inner)) {
//                    log.info("in double");
                    if (innerSize < outerSize) {
                        String tmp;
                        tmp = outer;
                        outer = inner;
                        inner = tmp;
                        flag1 = true;
                    }
                    InfoAndHandler innerPack = getTableInfo(inner);
                    InfoAndHandler outerPack = getTableInfo(outer);
                    for (Pair<String, String> constraintPair : constraints) {
                        List<String> columnList = new ArrayList<>();
                        if (flag1)
                            columnList.add(constraintPair.first);
                        else
                            columnList.add(constraintPair.second);
                        if (!innerPack.info.existsIndex(inner + "." + columnList)) {
                            try {
                                createIndex(inner + "." + columnList, inner, columnList);
                            } catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    nextHeader.addAll(mergedMap.get(outer).getHeaders());
                    nextHeader.addAll(mergedMap.get(inner).getHeaders());
                    for (List<Object> outerRecord : mergedMap.get(outer).getData()) {
                        List<WhereClause> whereClauses = new ArrayList<>();
                        for (Pair<String, String> constraint : constraints) {
                            String columnName = flag1 ? constraint.second : constraint.first;
                            String targetName = flag1 ? constraint.first : constraint.second;
//                            log.info("outer: {} inner: {}", outer, inner);
//                            log.info("outerC: {} innerC: {}", columnName, targetName);
//                            log.info("{}", mergedMap.get(outer).getHeaders());
                            whereClauses.add(new ValueOperatorClause(inner, targetName, "=", outerRecord.get(mergedMap.get(outer).getHeaderIndex((flag1 ? tablePair.second : tablePair.first) + "." + columnName))));
                        }
                        try {
                            RecordDataPack recordDataPack = searchIndices(inner, whereClauses);
//                            log.info("{}", recordDataPack.data.size());
                            for (List<Object> datum : recordDataPack.data) {
                                List<Object> concatRecord = new ArrayList<>();
                                concatRecord.addAll(outerRecord);
                                concatRecord.addAll(datum);
                                nextData.add(concatRecord);
                            }
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                    }
                } else if (!marker.get(outer) || !marker.get(inner)) {
//                    log.info("in single");
                    if (!marker.get(outer)) {
                        String tmp;
                        tmp = outer;
                        outer = inner;
                        inner = tmp;
                        flag1 = true;
                    }
                    InfoAndHandler innerPack = getTableInfo(inner);
                    InfoAndHandler outerPack = getTableInfo(outer);
                    for (Pair<String, String> constraintPair : constraints) {
                        List<String> columnList = new ArrayList<>();
                        if (flag1)
                            columnList.add(constraintPair.first);
                        else
                            columnList.add(constraintPair.second);
                        if (!innerPack.info.existsIndex(inner + "." + columnList)) {
                            try {
                                createIndex(inner + "." + columnList, inner, columnList);
                            } catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    nextHeader.addAll(mergedMap.get(outer).getHeaders());
                    nextHeader.addAll(mergedMap.get(inner).getHeaders());
                    for (List<Object> outerRecord : mergedMap.get(outer).getData()) {
                        List<WhereClause> whereClauses = new ArrayList<>();
                        for (Pair<String, String> constraint : constraints) {
                            String columnName = flag1 ? constraint.second : constraint.first;
                            String targetName = flag1 ? constraint.first : constraint.second;
//                            log.info("outer: {} inner: {}", outer, inner);
//                            log.info("outerC: {} innerC: {}", columnName, targetName);
//                            log.info("{}", mergedMap.get(outer).getHeaders());
                            whereClauses.add(new ValueOperatorClause(inner, targetName, "=", outerRecord.get(mergedMap.get(outer).getHeaderIndex((flag1 ? tablePair.second : tablePair.first) + "." + columnName))));
                        }
                        try {
                            RecordDataPack recordDataPack = searchIndices(inner, whereClauses);
//                            log.info("{}", recordDataPack.data.size());
                            for (List<Object> datum : recordDataPack.data) {
                                List<Object> concatRecord = new ArrayList<>();
                                concatRecord.addAll(outerRecord);
                                concatRecord.addAll(datum);
                                nextData.add(concatRecord);
                            }
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    nextHeader.addAll(mergedMap.get(outer).getHeaders());
                    nextHeader.addAll(mergedMap.get(inner).getHeaders());
                    for (List<Object> outerRecord : mergedMap.get(outer).getData()) {
                        int cnt = 0;
                        for (List<Object> innerRecord : mergedMap.get(inner).getData()) {
                            boolean flag = true;
                            for (Pair<String, String> columnPair : constraints) {
                                String outerColumn = tablePair.first + '.' + columnPair.first;
                                String innerColumn = tablePair.second + '.' + columnPair.second;
                                int outerId = mergedMap.get(outer).getHeaderIndex(outerColumn);
                                int innerId = mergedMap.get(inner).getHeaderIndex(innerColumn);
                                if (!outerRecord.get(outerId).equals(innerRecord.get(innerId))) {
                                    flag = false;
                                    break;
                                }
                            }
                            if (flag) {
                                cnt++;
                                List<Object> concatRecord = new ArrayList<>();
                                concatRecord.addAll(outerRecord);
                                concatRecord.addAll(innerRecord);
                                nextData.add(concatRecord);
                            }
                        }
//                        log.info("{}", cnt);
                    }
                }
                String newFather = unionFindSet.addEdge(outer, inner);
                mergedMap.replace(newFather, new TableResult(nextHeader, nextData));
                log.info("{}", mergedMap.get(newFather).getSize());
                marker.replace(inner, true);
                marker.replace(outer, true);
            }
            log.info("edge: {} {}", tablePair.first, tablePair.second);
            log.info("merge: {} {}", outer, inner);
            mergedMap.forEach((s, result) -> {
                log.info("s: {}, result header: {}", s, result.getHeaders());
            });
            log.info("");
        });


        //Step3: Join table without constraints
        List<TableResult> remainResult = new ArrayList<>();
        Set<String> mark = new HashSet<>();
        resultMap.keySet().forEach(key -> {
            String fkey = unionFindSet.getRoot(key);
            log.info("fkey: {}", fkey);
            if (mark.contains(fkey))
                return;
            mark.add(fkey);
            remainResult.add(mergedMap.get(fkey));
        });
        while (remainResult.size() > 1) {
            TableResult inner = remainResult.get(0);
            TableResult outer = remainResult.get(1);
            List<String> nextHeader = new ArrayList<>();
            List<List<Object>> nextData = new ArrayList<>();
            nextHeader.addAll(outer.getHeaders());
            nextHeader.addAll(inner.getHeaders());
            outer.getData().forEach(outerRecord -> inner.getData().forEach(innerRecord -> {
                List<Object> concatRecord = new ArrayList<>();
                concatRecord.addAll(outerRecord);
                concatRecord.addAll(innerRecord);
                nextData.add(concatRecord);
            }));
            remainResult.remove(0);
            remainResult.remove(0);
            remainResult.add(new TableResult(nextHeader, nextData));
        }
        return remainResult.get(0);
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

        if (group != null && group.tableName == null) {
            if (columnToTable.get(group.columnName).size() != 1)
                throw new RuntimeException(String.format("Group by column %s is ambiguous.", group.columnName));
            group.tableName = columnToTable.get(group.columnName).get(0);
        }

        String groupBy = group == null ? null : group.tableName + "." + group.columnName;

        Set<Selector.SelectorType> types = new HashSet<>();
        selectors.forEach(selector -> types.add(selector.getType()));
        if (group == null && types.size() > 1 && types.contains(Selector.SelectorType.FIELD))
            throw new RuntimeException("No group specified, can't resolve both aggregation and field");

        Map<String, TableResult> resultMap = new HashMap<>();
        for (String tableName : tableNames) {
            try {
                resultMap.put(tableName, scanIndices(tableName, clauses));
            } catch (UnsupportedEncodingException e) {
                return new MessageResult(e.getMessage(), true);
            }
        }

        TableResult result;
        //FIXME: deal with join later on.
        if (tableNames.size() == 1)
            result = resultMap.get(tableNames.get(0));
        else {
            result = bruteForceJoin(resultMap, clauses);
        }


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
            data = data.subList(offset, Math.min(offset + limit, data.size()));
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
