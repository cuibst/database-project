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
        File databaseDirectory = new File(getDatabasePath(currentUsingDatabase));
        assert databaseDirectory.exists() && databaseDirectory.isDirectory();
        Set<String> indices = new HashSet<>();
        File[] files = databaseDirectory.listFiles();
        if (files != null && files.length > 0 && files[0] != null)
            for (File file : files) {
                if (file.getName().endsWith(INDEX_SUFFIX))
                    indices.add(file.getName().substring(0, file.getName().length() - INDEX_SUFFIX.length()));
            }
        return new ListResult(indices, "INDICES");
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
        recordManager.deleteFile(rootPath + File.separator + getTablePath(tableName));
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
            if(checkAnyUnique(tableName, columns, data, record.getRid()))
                throw new RuntimeException(String.format("Duplicated keys for %s", data));
        }
        pack.handler.addUnique(pack.info, constraintName, columns);
        for(String column : columns) {
            if(!pack.info.existsIndex(column))
                createIndex(tableName + "." + column, tableName, column);
        }
    }

    public void addForeignKeyConstraint(String tableName, SQLTreeVisitor.ForeignKey foreignKey) throws Exception {
        if (currentUsingDatabase == null)
            throw new RuntimeException("No database is being used!");
        InfoAndHandler pack = getTableInfo(tableName);
        InfoAndHandler targetPack = getTableInfo(foreignKey.targetTable);
        FileHandler fileHandler = recordManager.openFile(getTablePath(tableName));
        FileScanner fileScanner = new FileScanner(fileHandler);
        for (Record record : fileScanner) {
            List<Object> data = pack.info.loadRecord(record);
            Object[] tmp = new Object[targetPack.info.getColIndex().size()];
            for(int i=0;i<foreignKey.columns.size();i++) {
                tmp[targetPack.info.getIndex(foreignKey.targetColumns.get(i))] = data.get(pack.info.getIndex(foreignKey.columns.get(i)));
            }
            List<Object> targetData = new ArrayList<>(Arrays.asList(tmp));
            if(!checkAnyUnique(foreignKey.targetTable, foreignKey.targetColumns, targetData, null))
                throw new RuntimeException(String.format("Foreign Key for %s cannot find matching result.", data.toString()));
        }
        pack.handler.addForeign(tableName, foreignKey);
        for (String column : foreignKey.targetColumns) {
            if (!pack.handler.existsIndex(tableName + "." + column) && pack.info.getColumns().get(column).getType().equals("INT"))
                createIndex(foreignKey.targetTable + "." + column, foreignKey.targetTable, column);
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
            if(checkAnyUnique(tableName, primaryKey.fields, data, record.getRid()))
                throw new RuntimeException(String.format("Duplicated keys for %s", data));
        }
        pack.handler.setPrimary(tableName, primaryKey == null ? null : primaryKey.fields);
        if (primaryKey == null)
            return;
        for (String column : primaryKey.fields) {
            if (!pack.handler.existsIndex(tableName + "." + column) && pack.info.getColumns().get(column).getType().equals("INT"))
                createIndex(tableName + "." + column, tableName, column);
        }
    }

    public void removePrimary(String tableName) {
        if (currentUsingDatabase == null)
            throw new RuntimeException("No database is being used!");
        MetaHandler metaHandler = metaManager.openMeta(currentUsingDatabase);
        List<String> key = metaHandler.getTable(tableName).getPrimary();
//        key.forEach(column -> {
//            if (metaHandler.existsIndex(tableName + "." + column))
//                removeIndex(tableName + "." + column);
//        });
    }

    public void removeForeignKeyConstraint(String tableName, String constraintName) {
        if (currentUsingDatabase == null)
            throw new RuntimeException("No database is being used!");
        MetaHandler metaHandler = metaManager.openMeta(currentUsingDatabase);
        SQLTreeVisitor.ForeignKey key = metaHandler.getTable(tableName).getForeign().get(constraintName);
//        key.targetColumns.forEach(column -> {
//            removeIndex(key.targetTable + "." + column);
//        });
        metaHandler.removeForeign(tableName, constraintName);
    }

    public void createIndex(String indexName, String tableName, String columnName) throws Exception {
        InfoAndHandler pack = getTableInfo(tableName);
        log.info(indexName);
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
            Integer index = pack.info.getRootId(clause.getColumnName());
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
            FileIndex index = indexManager.openedIndex(currentUsingDatabase, tableName, pack.info.getRootId(entry.getKey()));
            if (result == null) {
                ArrayList<RID> res = index.range(entry.getValue().lower, entry.getValue().upper);
                if(res == null)
                    return null;
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

    public boolean checkAnyUnique(String tableName, List<String> columns, List<Object> values, RID currentRow) throws UnsupportedEncodingException {
        List<WhereClause> clauses = new ArrayList<>();
        for (int i = 0; i < columns.size(); i++) {
            clauses.add(new ValueOperatorClause(tableName, columns.get(i), "=", values.get(i)));
        }
        RecordDataPack pack = searchIndices(tableName, clauses);
        assert pack.records == null || pack.records.size() <= 1;
        return !((pack.records != null && pack.records.size() >= 1 && currentRow != null && currentRow.equals(pack.records.get(0).getRid())) || pack.records == null || pack.records.size() == 0);
    }

    public boolean checkPrimaryConstraint(String tableName, List<Object> values, RID currentRow) throws UnsupportedEncodingException {
        InfoAndHandler pack = getTableInfo(tableName);
        if (pack.info.getPrimary().size() == 0)
            return false;
        List<Object> checkValues = new ArrayList<>();
        pack.info.getPrimary().forEach(primary -> {
            checkValues.add(values.get(pack.info.getIndex(primary)));
            if(values.get(pack.info.getIndex(primary)) == null)
                throw new RuntimeException("Primary key found NULL value.");
        });
        return checkAnyUnique(tableName, pack.info.getPrimary(), checkValues, currentRow);
    }

    public boolean checkUniqueConstraint(String tableName, List<Object> values, RID currentRow) throws UnsupportedEncodingException {
        InfoAndHandler pack = getTableInfo(tableName);
        if (pack.info.getUnique().size() == 0)
            return false;
        for (List<String> unique : pack.info.getUnique().values()) {
            List<Object> value = new ArrayList<>();
            unique.forEach(column -> {
                value.add(values.get(pack.info.getIndex(column)));
            });
            if (checkAnyUnique(tableName, unique, value, currentRow))
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
            for (int i = 0; i < columns.size(); i++) {
                String column = columns.get(i);
                String targetColumn = entry.getValue().targetColumns.get(i);
                Object value = values.get(pack.info.getIndex(column));
                if(value == null)
                    throw new RuntimeException("Foreign key can't have null value.");
                clauses.add(new ValueOperatorClause(foreignKey.targetTable, targetColumn, "=", value));
            }
            RecordDataPack recordDataPack = searchIndices(foreignKey.targetTable, clauses);
            if(recordDataPack.records == null || recordDataPack.records.size() == 0)
                return true;
        }
        return false;
    }

    public void checkConstraints(String tableName, List<Object> values, RID currentRow) throws UnsupportedEncodingException {
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
            objects.forEach(object -> result.add(object == null ? "NULL" : object));
            valuesList.add(result);
        });
        return new TableResult(pack.info.getHeader(), valuesList);
    }

    public void insertRecord(String tableName, List<Object> valueList) throws UnsupportedEncodingException {
        InfoAndHandler pack = getTableInfo(tableName);
        checkConstraints(tableName, valueList, null);
        List<String> stringList = new ArrayList<>();
        valueList.forEach(obj -> {
            if(obj != null)
                stringList.add(obj.toString());
            else
                stringList.add(null);
        });
        byte[] data = pack.info.buildRecord(stringList);
        FileHandler fileHandler = recordManager.openFile(getTablePath(tableName));
        Record rid = fileHandler.insertRecord(data);
        insertIndices(pack.info, currentUsingDatabase, valueList, rid.getRid());
    }

    public void insertIndices(TableInfo tableInfo, String databaseName, List<Object> values, RID rid) {
        tableInfo.getIndicesMap().forEach((indexColumn, rootId) -> {
            FileIndex index = indexManager.openedIndex(databaseName, tableInfo.getName(), rootId);
            int columnId = tableInfo.getIndex(indexColumn);
            if (values.get(columnId) != null)
                index.insert((Integer) values.get(columnId), rid);
            else
                index.insert(Long.MIN_VALUE, rid);
        });
    }

    public void deleteIndices(TableInfo tableInfo, String databaseName, List<Object> values, RID rid) {
        tableInfo.getIndicesMap().forEach((indexColumn, rootId) -> {
            FileIndex index = indexManager.openedIndex(databaseName, tableInfo.getName(), rootId);
            int columnId = tableInfo.getIndex(indexColumn);
            if (values.get(columnId) != null)
                index.remove((Integer) values.get(columnId), rid);
            else
                index.remove(Long.MIN_VALUE, rid);
        });
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

//                    handler.deleteRecord(record.getRid());
                    List<String> headers = pack.info.getColumnName();
                    setClauses.forEach(clause -> {
                        for (int i = 0; i < headers.size(); i++) {
                            String name = headers.get(i);
                            if (name.equals(clause.getColumnName())) {
                                values.set(i, clause.getValue());
                            }
                        }
                    });
                    List<String> stringList = new ArrayList<>();
                    values.forEach(obj -> stringList.add(obj.toString()));
                    byte[] data = pack.info.buildRecord(stringList);
                    record.setData(data);
                    handler.updateRecord(record);
                    this.insertRecord(tableName, values);
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
            if(!(clause instanceof ColumnOperatorClause))
                return;
            ColumnOperatorClause subClause = (ColumnOperatorClause) clause;
            if(clause.getTableName().equals(clause.getTargetTable()))
                return;
            if(!subClause.getOperator().equals("="))
                throw new RuntimeException("Cross-table comparison must be equal.");
            Pair<String, String> tablePair;
            Pair<String, String> columnPair;
            if(clause.getTableName().compareTo(clause.getTargetTable()) < 0) {
                tablePair = new Pair<>(clause.getTableName(), clause.getTargetTable());
                columnPair = new Pair<>(clause.getColumnName(), clause.getTargetColumn());
            } else {
                tablePair = new Pair<>(clause.getTargetTable(), clause.getTableName());
                columnPair = new Pair<>(clause.getTargetColumn(), clause.getColumnName());
            }
            if(edgeMap.containsKey(tablePair))
                edgeMap.get(tablePair).add(columnPair);
            else
                edgeMap.put(tablePair, new ArrayList<>(List.of(columnPair)));
        });

        //Step2: Join constraint-based tables
        UnionFindSet<String> unionFindSet = new UnionFindSet<>(resultMap.keySet());

        Map<String, TableResult> mergedMap = new HashMap<>(Map.copyOf(resultMap));

        edgeMap.forEach((tablePair, constraints) -> {
            String outer = unionFindSet.getRoot(tablePair.first);
            String inner = unionFindSet.getRoot(tablePair.second);
            if(outer.equals(inner)) {
                List<List<Object>> nextResult = new ArrayList<>();
                mergedMap.get(outer).getData().forEach(record -> {
                    boolean flag = true;
                    for (Pair<String, String> columnPair : constraints) {
                        String outerColumn = tablePair.first + '.' + columnPair.first;
                        String innerColumn = tablePair.second + '.' + columnPair.second;
                        int outerId = mergedMap.get(outer).getHeaderIndex(outerColumn);
                        int innerId = mergedMap.get(outer).getHeaderIndex(innerColumn);
                        if(!record.get(outerId).equals(record.get(innerId))) {
                            flag = false;
                            break;
                        }
                    }
                    if(flag)
                        nextResult.add(record);
                });
                mergedMap.replace(outer, new TableResult(mergedMap.get(outer).getHeaders(), nextResult));
            } else {
                List<String> nextHeader = new ArrayList<>();
                List<List<Object>> nextData = new ArrayList<>();
                nextHeader.addAll(mergedMap.get(outer).getHeaders());
                nextHeader.addAll(mergedMap.get(inner).getHeaders());
                mergedMap.get(outer).getData().forEach(outerRecord -> {
                    mergedMap.get(inner).getData().forEach(innerRecord -> {
                        boolean flag = true;
                        for (Pair<String, String> columnPair : constraints) {
                            String outerColumn = tablePair.first + '.' + columnPair.first;
                            String innerColumn = tablePair.second + '.' + columnPair.second;
                            int outerId = mergedMap.get(outer).getHeaderIndex(outerColumn);
                            int innerId = mergedMap.get(inner).getHeaderIndex(innerColumn);
                            if(!outerRecord.get(outerId).equals(innerRecord.get(innerId))) {
                                flag = false;
                                break;
                            }
                        }
                        if(flag) {
                            List<Object> concatRecord = new ArrayList<>();
                            concatRecord.addAll(outerRecord);
                            concatRecord.addAll(innerRecord);
                            nextData.add(concatRecord);
                        }
                    });
                });
                String newFather = unionFindSet.addEdge(outer, inner);
                mergedMap.replace(newFather, new TableResult(nextHeader, nextData));
            }
        });

        //Step3: Join table without constraints
        List<TableResult> remainResult = new ArrayList<>();
        Set<String> mark = new HashSet<>();
        resultMap.keySet().forEach(key -> {
            String fkey = unionFindSet.getRoot(key);
            if(mark.contains(fkey))
                return;
            mark.add(fkey);
            remainResult.add(mergedMap.get(fkey));
        });
        while(remainResult.size() > 1) {
            TableResult inner = remainResult.get(0);
            TableResult outer = remainResult.get(1);
            List<String> nextHeader = new ArrayList<>();
            List<List<Object>> nextData = new ArrayList<>();
            nextHeader.addAll(outer.getHeaders());
            nextHeader.addAll(inner.getHeaders());
            outer.getData().forEach(outerRecord -> {
                inner.getData().forEach(innerRecord -> {
                    List<Object> concatRecord = new ArrayList<>();
                    concatRecord.addAll(outerRecord);
                    concatRecord.addAll(innerRecord);
                    nextData.add(concatRecord);
                });
            });
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

        if (group != null && group.tableName == null)
            group.tableName = tableNames.get(0);

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
        if(tableNames.size() == 1)
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
