package database.rzotgorz.managesystem;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import database.rzotgorz.indexsystem.FileIndex;
import database.rzotgorz.indexsystem.IndexManager;
import database.rzotgorz.managesystem.clauses.OperatorClause;
import database.rzotgorz.managesystem.clauses.WhereClause;
import database.rzotgorz.managesystem.results.*;
import database.rzotgorz.metaSystem.*;
import database.rzotgorz.parser.SQLLexer;
import database.rzotgorz.parser.SQLParser;
import database.rzotgorz.recordsystem.FileHandler;
import database.rzotgorz.recordsystem.Record;
import database.rzotgorz.recordsystem.RecordManager;
import database.rzotgorz.utils.FileScanner;
import lombok.extern.slf4j.Slf4j;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.atn.ATNConfigSet;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.ParseCancellationException;

import javax.xml.transform.Result;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
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
    private SQLTreeVisitor visitor;

    public Set<String> getDatabases() {
        return databases;
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
        if (currentUsingDatabase.equals(name))
            return new DatabaseChangeResult("null");
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
        Map<String, String[]> data = pack.info.describe();
        List<List<String>> columns = new ArrayList<>();
        data.values().forEach(val -> columns.add(Arrays.asList(val)));
        return new TableResult(header, columns);
    }

    public void addUniqueConstraint(String tableName, String columnName, String constraintName) throws Exception {
        InfoAndHandler pack = getTableInfo(tableName);
        pack.handler.addUnique(pack.info, columnName, constraintName);
        if(!pack.info.existsIndex(constraintName))
            createIndex(constraintName, tableName, columnName);
    }

    public void addForeignKeyConstraint(String tableName, String columnName, SQLTreeVisitor.ForeignKey foreignKey, String constraintName) throws Exception {
        if(currentUsingDatabase == null)
            throw new RuntimeException("No database is being used!");
        MetaHandler metaHandler = metaManager.openMeta(currentUsingDatabase);
        metaHandler.addForeign(tableName, columnName, foreignKey);
        if(constraintName == null && !metaHandler.existsIndex(foreignKey.targetTable + '.' + foreignKey.name))
            createIndex(foreignKey.targetTable + '.' + foreignKey.name, foreignKey.targetTable, foreignKey.name);
        else if(!metaHandler.existsIndex(constraintName))
            createIndex(constraintName, foreignKey.targetTable, foreignKey.name);
    }

    public void setPrimary(String tableName, SQLTreeVisitor.PrimaryKey primaryKey) throws Exception {
        if(currentUsingDatabase == null)
            throw new RuntimeException("No database is being used!");
        MetaHandler metaHandler = metaManager.openMeta(currentUsingDatabase);
        metaHandler.setPrimary(tableName, primaryKey == null ? null : primaryKey.fields);
        if(primaryKey == null)
            return;
        for (String column : primaryKey.fields) {
            if (!metaHandler.existsIndex(tableName + "." + column))
                createIndex(tableName + "." + column, tableName, column);
        }
    }

    public void removePrimary(String tableName) {
        if(currentUsingDatabase == null)
            throw new RuntimeException("No database is being used!");
        MetaHandler metaHandler = metaManager.openMeta(currentUsingDatabase);
        List<String> key = metaHandler.getTable(tableName).getPrimary();
        key.forEach(column -> {
            if(metaHandler.existsIndex(tableName + "." + column))
                removeIndex(tableName + "." + column);
        });
    }

    public void addColumn(String tableName, ColumnInfo columnInfo) {

    }

    public void removeForeignKeyConstraint(String tableName, String column, String constraintName) {
        if(currentUsingDatabase == null)
            throw new RuntimeException("No database is being used!");
        if(constraintName == null) {
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
            Map<Integer, String> data = pack.info.loadRecord(record);
            String key = data.get(columnId);
            long keyId = Long.parseLong(key);
            index.insert(keyId, record.getRid());
        }
        pack.handler.createIndex(indexName, tableName, columnName);
    }

    public void removeIndex(String indexName) {
        if(currentUsingDatabase == null)
            throw new RuntimeException("No database is being used!");
        MetaHandler metaHandler = metaManager.openMeta(currentUsingDatabase);
        DbInfo.IndexInfo indexInfo = metaHandler.getIndexInfo(indexName);
        TableInfo tableInfo = metaHandler.getTable(indexInfo.tableName);
        if(!metaHandler.existsIndex(indexName))
            throw new RuntimeException(String.format("Index %s doesn't exist!", indexName));
        tableInfo.removeIndex(indexName);
        metaHandler.removeIndex(indexName);
        metaManager.closeMeta(currentUsingDatabase);
    }

    private interface Function {

    }

    public void buildFunctions(String tableName, List<WhereClause> clauses, MetaHandler metaHandler) {
        TableInfo tableInfo = metaHandler.getTable(tableName);
        for (WhereClause clause : clauses) {
            if(clause.getTableName() != null && !clause.getTableName().equals(tableName))
                continue;
            Integer index = tableInfo.getIndex(clause.getColumnName());
            if(index == null)
                throw new RuntimeException(String.format("Field %s for table %s is unknown.", clause.getColumnName(), clause.getTableName()));
            String type = tableInfo.getTypeList().get(index);
            if(clause.getClass() == OperatorClause.class) {
                if(clause.getTargetColumn() != null) {
                    if(!tableName.equals(clause.getTableName()))
                        continue;
                }
            }
        }
    }

    public void insertRecord(String tableName, List<Object> valueList) {
        try {
            InfoAndHandler pack = getTableInfo(tableName);
            List<String> stringList = new ArrayList<>();
            valueList.forEach(obj -> stringList.add(obj.toString()));
//            log.info("string list size: {}", stringList.size());
            byte[] data = pack.info.buildRecord(stringList);
            FileHandler fileHandler = recordManager.openFile(getTablePath(tableName));
            Record rid = fileHandler.insertRecord(data);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

//    public ResultItem selectRecord(List<Selector> selectors, List<String> tableNames, List<WhereClause> clauses) { //FIXME: NO GROUP BY support
//        if(currentUsingDatabase == null)
//            throw new RuntimeException("No database is being used!");
//        MetaHandler metaHandler = metaManager.openMeta(currentUsingDatabase);
//        JSONObject columnToTable = metaHandler.buildTable(tableNames);
//        clauses.forEach(clause -> {
//            String table = clause.getTableName();
//            String column = clause.getColumnName();
//            if(table == null) {
//                JSONArray tables = columnToTable.getJSONArray(column);
//                if(tables.size() > 1)
//                    throw new RuntimeException(String.format("Column %s is ambiguous.", column));
//                if(tables.size() == 0)
//                    throw new RuntimeException(String.format("Unknown column %s.", column));
//                clause.setTableName(tables.getString(0));
//            }
//            table = clause.getTargetTable();
//            column = clause.getTargetColumn();
//            if(column != null && table == null) {
//                JSONArray tables = columnToTable.getJSONArray(column);
//                if(tables.size() > 1)
//                    throw new RuntimeException(String.format("Column %s is ambiguous.", column));
//                if(tables.size() == 0)
//                    throw new RuntimeException(String.format("Unknown column %s.", column));
//                clause.setTargetTable(tables.getString(0));
//            }
//        });
//        selectors.forEach(selector -> {
//            String table = selector.getTableName();
//            String column = selector.getColumnName();
//            if(table == null) {
//                JSONArray tables = columnToTable.getJSONArray(column);
//                if(tables.size() > 1)
//                    throw new RuntimeException(String.format("Column %s is ambiguous.", column));
//                if(tables.size() == 0)
//                    throw new RuntimeException(String.format("Unknown column %s.", column));
//                selector.setTableName(tables.getString(0));
//            }
//        });
//        Set<Selector.SelectorType> types = new HashSet<>();
//        selectors.forEach(selector -> types.add(selector.getType()));
//        if(types.size() > 1 && types.contains(Selector.SelectorType.FIELD))
//            throw new RuntimeException("No group specified, can't resolve both aggregation and field");
//
//    }


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
            MessageResult result = new MessageResult(e.getMessage(), true);
            result.cost = visitor.getTimeDelta();
            return result;
        }

        try {
            return visitor.visit(context);
        } catch (ParseCancellationException e) {
            MessageResult result = new MessageResult(e.getMessage(), true);
            result.cost = visitor.getTimeDelta();
            return result;
        }
    }

}
